package bloodbuddy.backend.service;

import bloodbuddy.backend.entity.BloodRequest;
import bloodbuddy.backend.entity.Inventory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Fallback used when the WhatsApp provider is not configured. The matching flow has
 * already persisted the match rows and the notification record; this only logs that
 * the outbound send was skipped. {@link WhatsAppNotificationService} delegates here
 * when {@code aws.whatsapp.*} is still unset.
 */
@Service
public class DeferredNotificationService implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(DeferredNotificationService.class);

    @Override
    public NotificationResult sendCentreDetails(BloodRequest request, List<Inventory> matchedInventory) {
        log.info("WhatsApp send skipped for blood request {} ({} matched centre(s)); "
                + "aws.whatsapp.* not configured", request.getBloodRequestId(), matchedInventory.size());
        return NotificationResult.skipped();
    }
}
