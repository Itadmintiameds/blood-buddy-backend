package bloodbuddy.backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Binds the {@code aws.whatsapp.*} settings for AWS End User Messaging Social. Relaxed binding
 * maps kebab-case keys (e.g. {@code origination-phone-number-id}) onto these camelCase fields.
 */
@Component
@ConfigurationProperties(prefix = "aws.whatsapp")
@Getter
@Setter
public class WhatsAppProperties {

    private String region;
    private String originationPhoneNumberId;
    private String templateName;
    private String templateLanguage;
    private String metaApiVersion;
}
