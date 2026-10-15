package bloodbuddy.backend.service;

import bloodbuddy.backend.config.WhatsAppProperties;
import bloodbuddy.backend.entity.BloodCentres;
import bloodbuddy.backend.entity.BloodRequest;
import bloodbuddy.backend.entity.Inventory;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.socialmessaging.SocialMessagingClient;
import software.amazon.awssdk.services.socialmessaging.model.SendWhatsAppMessageRequest;
import software.amazon.awssdk.services.socialmessaging.model.SendWhatsAppMessageResponse;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.List;

/**
 * Sends the approved {@code blood_centres_matched} WhatsApp template via AWS End User
 * Messaging Social. The template body has four variables:
 * <pre>
 *   {{1}} recipient name   {{2}} blood group   {{3}} component   {{4}} centre list
 * </pre>
 * {{4}} renders one entry per matched centre with its available units, e.g.
 * {@code "1) City Blood Centre, MG Road, ph +91..., 4 units, map https://...; 2) ..."}.
 *
 * <p>When {@code aws.whatsapp.*} is unconfigured (still {@code dummy}/blank) the call is
 * delegated to {@link DeferredNotificationService}, which just logs the skip, so the
 * matching flow behaves identically in local/dev environments.
 */
@Service
@Primary
@RequiredArgsConstructor
public class WhatsAppNotificationService implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppNotificationService.class);
    private static final String UNSET = "dummy";
    /** WhatsApp caps a template parameter at 1024 chars; stay under it with headroom for the suffix. */
    private static final int MAX_PARAM_LENGTH = 1000;

    private final SocialMessagingClient socialMessagingClient;
    private final DeferredNotificationService fallback;
    private final ObjectMapper objectMapper;
    private final WhatsAppProperties props;

    @Override
    public NotificationResult sendCentreDetails(BloodRequest request, List<Inventory> matchedInventory) {
        if (!isConfigured()) {
            return fallback.sendCentreDetails(request, matchedInventory);
        }

        String to = normalizeMsisdn(request.getMobileNumber());
        if (to == null) {
            log.warn("Skipping WhatsApp send for blood request {}: unusable mobile number '{}'",
                    request.getBloodRequestId(), mask(request.getMobileNumber()));
            return NotificationResult.failed("Unusable mobile number");
        }

        try {
            String payload = buildTemplatePayload(request, matchedInventory, to);
            SendWhatsAppMessageResponse response = socialMessagingClient.sendWhatsAppMessage(
                    SendWhatsAppMessageRequest.builder()
                            .originationPhoneNumberId(props.getOriginationPhoneNumberId())
                            .metaApiVersion(props.getMetaApiVersion())
                            .message(SdkBytes.fromUtf8String(payload))
                            .build());
            log.info("Sent WhatsApp '{}' for blood request {} to {} ({} centre(s)); messageId={}",
                    props.getTemplateName(), request.getBloodRequestId(), mask(to),
                    matchedInventory.size(), response.messageId());
            return NotificationResult.sent(response.messageId());
        } catch (Exception e) {
            // Never propagate: the match/notification rows are already persisted in the caller's
            // transaction and a send failure must not roll them back.
            log.error("WhatsApp send failed for blood request {} to {}: {}",
                    request.getBloodRequestId(), mask(to), e.getMessage(), e);
            return NotificationResult.failed(e.getMessage() != null ? e.getMessage() : e.toString());
        }
    }

    private boolean isConfigured() {
        return isSet(props.getOriginationPhoneNumberId()) && isSet(props.getTemplateName());
    }

    private static boolean isSet(String value) {
        return value != null && !value.isBlank() && !UNSET.equalsIgnoreCase(value);
    }

    /** Builds the Meta Cloud API message object the SendWhatsAppMessage API wraps. */
    private String buildTemplatePayload(BloodRequest request, List<Inventory> matchedInventory, String to) {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("messaging_product", "whatsapp");
        root.put("recipient_type", "individual");
        root.put("to", to);
        root.put("type", "template");

        ObjectNode template = root.putObject("template");
        template.put("name", props.getTemplateName());
        template.putObject("language").put("code", props.getTemplateLanguage());

        ArrayNode components = template.putArray("components");
        ObjectNode body = components.addObject();
        body.put("type", "body");
        ArrayNode params = body.putArray("parameters");
        addTextParam(params, safe(request.getRecipientName()));
        addTextParam(params, request.getBloodGroup() != null
                ? safe(request.getBloodGroup().getBloodGroupName()) : "");
        addTextParam(params, request.getBloodComponent() != null
                ? safe(request.getBloodComponent().getBloodComponentName()) : "");
        addTextParam(params, renderCentreList(matchedInventory));

        return objectMapper.writeValueAsString(root);
    }

    private static void addTextParam(ArrayNode params, String text) {
        ObjectNode p = params.addObject();
        p.put("type", "text");
        p.put("text", text);
    }

    /**
     * Renders {{4}}: a list of centres with units. WhatsApp template parameters may not contain
     * newlines, tabs, or 4+ consecutive spaces, so a true line break is impossible here — the value
     * is always one wrapping paragraph, and the text is NOT clickable/expandable. To keep centres
     * distinguishable, each entry is anchored with a 📍 marker and a bolded name (WhatsApp
     * {@code *bold*}), and fields use emoji icons. As many centres as fit under WhatsApp's 1024-char
     * limit are listed; only a genuine overflow is summarised as "…and N more centre(s)".
     */
    private static String renderCentreList(List<Inventory> matchedInventory) {
        List<Inventory> centres = matchedInventory.stream()
                .filter(inv -> inv.getBloodCentre() != null)
                .toList();

        StringBuilder sb = new StringBuilder();
        int shown = 0;
        for (Inventory inv : centres) {
            String entry = renderCentre(inv, shown + 1);
            int separatorLength = sb.length() > 0 ? 3 : 0;
            // Stop before exceeding the limit, but always show at least one centre.
            if (shown > 0 && sb.length() + separatorLength + entry.length() > MAX_PARAM_LENGTH) {
                break;
            }
            if (sb.length() > 0) {
                sb.append("   "); // 3 spaces (< 4-space limit) between centres
            }
            sb.append(entry);
            shown++;
        }

        int remaining = centres.size() - shown;
        if (remaining > 0) {
            sb.append("   *...and ").append(remaining).append(" more centre(s)*");
        }
        return sb.toString();
    }

    /** Renders a single centre entry (no separators); see {@link #renderCentreList}. */
    private static String renderCentre(Inventory inv, int index) {
        BloodCentres centre = inv.getBloodCentre();
        StringBuilder sb = new StringBuilder();
        sb.append("📍 ").append(index).append(") *").append(safe(centre.getBloodCentreName())).append("*");
        appendIfPresent(sb, " - ", centre.getAddress());
        appendIfPresent(sb, " 📞 ", centre.getMobileNumber());
        if (inv.getAvailableUnits() != null) {
            sb.append(" 🩸 ").append(inv.getAvailableUnits()).append(" units");
        }
        appendIfPresent(sb, " 🗺 ", centre.getLocationUrl());
        return sb.toString();
    }

    private static void appendIfPresent(StringBuilder sb, String prefix, String value) {
        if (value != null && !value.isBlank()) {
            sb.append(prefix).append(value.trim());
        }
    }

    /** Collapses any internal whitespace so a value never breaks the template-param rules. */
    private static String safe(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ");
    }

    /**
     * Normalizes an Indian mobile number to the E.164 form AWS End User Messaging Social expects:
     * a leading '+', country code, then the 10-digit subscriber number (e.g. {@code +919876543210}).
     * Handles the common stored forms — bare 10 digits, a domestic trunk "0" prefix, a "00"/"+"
     * international prefix, and an already-prefixed "91...". Returns {@code null} for anything that
     * isn't a valid Indian number, so junk is never sent to AWS.
     */
    private static String normalizeMsisdn(String raw) {
        if (raw == null) {
            return null;
        }
        String digits = raw.replaceAll("\\D", "");
        if (digits.startsWith("00")) {                        // 00 international prefix
            digits = digits.substring(2);
        }
        if (digits.length() == 11 && digits.startsWith("0")) { // domestic trunk 0
            digits = digits.substring(1);
        }
        if (digits.length() == 10) {                          // bare subscriber number
            digits = "91" + digits;
        }
        return digits.length() == 12 && digits.startsWith("91") ? "+" + digits : null;
    }

    /** Masks all but the last 4 digits for logging, so numbers never appear in full in logs. */
    private static String mask(String value) {
        if (value == null || value.length() <= 4) {
            return "****";
        }
        return "*".repeat(value.length() - 4) + value.substring(value.length() - 4);
    }
}
