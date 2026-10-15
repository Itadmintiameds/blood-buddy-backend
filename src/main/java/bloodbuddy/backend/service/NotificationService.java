package bloodbuddy.backend.service;

import bloodbuddy.backend.entity.BloodRequest;
import bloodbuddy.backend.entity.Inventory;

import java.util.List;

/**
 * Integration point for delivering matched-centre details to a recipient over WhatsApp.
 * The matched inventory rows carry both the centre and its available units, which the
 * message renders per centre.
 */
public interface NotificationService {

    NotificationResult sendCentreDetails(BloodRequest request, List<Inventory> matchedInventory);
}
