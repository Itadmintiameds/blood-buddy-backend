package bloodbuddy.backend.service;

import bloodbuddy.backend.entity.BloodCentres;
import bloodbuddy.backend.entity.BloodRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Placeholder implementation. The blood_request_details rows and the notification
 * record are still persisted by the matching flow; only the outbound message send
 * is deferred until the WhatsApp provider is wired in as a separate step.
 */
@Service
public class DeferredNotificationService implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(DeferredNotificationService.class);

    @Override
    public void sendCentreDetails(BloodRequest request, List<BloodCentres> matchedCentres) {
        log.info("Message send deferred for blood request {} ({} matched centre(s)); "
                + "WhatsApp provider not yet configured", request.getBloodRequestId(), matchedCentres.size());
    }
}
