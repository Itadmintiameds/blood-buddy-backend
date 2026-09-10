package bloodbuddy.backend.service;

import bloodbuddy.backend.entity.BloodCentres;
import bloodbuddy.backend.entity.BloodRequest;
import bloodbuddy.backend.entity.BloodRequestDetails;
import bloodbuddy.backend.entity.Inventory;
import bloodbuddy.backend.entity.Notification;
import bloodbuddy.backend.repository.BloodRequestDetailsRepository;
import bloodbuddy.backend.repository.InventoryRepository;
import bloodbuddy.backend.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MatchingService {

    private static final String SYSTEM_ACTOR = "SYSTEM";

    private final InventoryRepository inventoryRepository;
    private final BloodRequestDetailsRepository bloodRequestDetailsRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;

    public MatchingService(InventoryRepository inventoryRepository,
                           BloodRequestDetailsRepository bloodRequestDetailsRepository,
                           NotificationRepository notificationRepository,
                           NotificationService notificationService) {
        this.inventoryRepository = inventoryRepository;
        this.bloodRequestDetailsRepository = bloodRequestDetailsRepository;
        this.notificationRepository = notificationRepository;
        this.notificationService = notificationService;
    }

    /**
     * Finds centres near the recipient that stock the requested group + component, writes one
     * detail row per matched centre plus a single notification, and hands off to the (deferred)
     * message sender. Returns the number of matched centres; zero means admin donor outreach.
     */
    @Transactional
    public int matchAndNotify(BloodRequest request) {
        List<Inventory> matches = inventoryRepository.findMatchingStock(
                request.getBloodGroup().getBloodGroupId(),
                request.getBloodComponent().getBloodComponentId(),
                request.getPincode(),
                request.getCity(),
                request.getDistrict());

        if (matches.isEmpty()) {
            return 0;
        }

        LocalDateTime now = LocalDateTime.now();
        List<BloodCentres> matchedCentres = matches.stream().map(Inventory::getBloodCentre).toList();

        // One detail row per matched centre: centre set, donor null (match path).
        for (BloodCentres centre : matchedCentres) {
            BloodRequestDetails details = new BloodRequestDetails();
            details.setBloodRequest(request);
            details.setBloodCentre(centre);
            details.setBloodDonorDetails(null);
            details.setStatus("sent");
            details.setCreatedAt(now);
            details.setCreatedBy(SYSTEM_ACTOR);
            bloodRequestDetailsRepository.save(details);
        }

        // One notification per request (one message); the message lists every matched centre,
        // so no per-centre notification rows and no join table are needed.
        Notification notification = new Notification();
        notification.setBloodRequest(request);
        notification.setChannel("WHATSAPP");
        notification.setToMobile(request.getMobileNumber());
        notification.setDeliveryStatus("PENDING");
        notification.setCreatedAt(now);
        notification.setCreatedBy(SYSTEM_ACTOR);
        notificationRepository.save(notification);

        // Actual send is deferred (stub); rows above are already persisted.
        notificationService.sendCentreDetails(request, matchedCentres);

        return matchedCentres.size();
    }
}
