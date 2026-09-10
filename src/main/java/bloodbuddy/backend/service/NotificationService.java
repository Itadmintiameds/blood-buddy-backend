package bloodbuddy.backend.service;

import bloodbuddy.backend.entity.BloodCentres;
import bloodbuddy.backend.entity.BloodRequest;

import java.util.List;

/**
 * Integration point for delivering matched-centre details to a recipient.
 * The actual provider (WhatsApp) is out of scope for now; see the deferred stub.
 */
public interface NotificationService {

    void sendCentreDetails(BloodRequest request, List<BloodCentres> matchedCentres);
}
