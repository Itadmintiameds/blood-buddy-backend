package bloodbuddy.backend.service;

import bloodbuddy.backend.dto.request.BloodRequestResponse;
import bloodbuddy.backend.dto.request.BloodRequestSubmission;
import bloodbuddy.backend.entity.BloodRequest;
import bloodbuddy.backend.entity.masters.BloodComponents;
import bloodbuddy.backend.entity.masters.BloodGroup;
import bloodbuddy.backend.exception.ResourceNotFoundException;
import bloodbuddy.backend.repository.BloodComponentsRepository;
import bloodbuddy.backend.repository.BloodGroupRepository;
import bloodbuddy.backend.repository.BloodRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class BloodRequestService {

    private final BloodRequestRepository bloodRequestRepository;
    private final BloodGroupRepository bloodGroupRepository;
    private final BloodComponentsRepository bloodComponentsRepository;
    private final MatchingService matchingService;

    public BloodRequestService(BloodRequestRepository bloodRequestRepository,
                               BloodGroupRepository bloodGroupRepository,
                               BloodComponentsRepository bloodComponentsRepository,
                               MatchingService matchingService) {
        this.bloodRequestRepository = bloodRequestRepository;
        this.bloodGroupRepository = bloodGroupRepository;
        this.bloodComponentsRepository = bloodComponentsRepository;
        this.matchingService = matchingService;
    }

    @Transactional
    public BloodRequestResponse submit(BloodRequestSubmission submission) {
        BloodGroup bloodGroup = bloodGroupRepository.findById(submission.getBloodGroupId())
                .orElseThrow(() -> new ResourceNotFoundException("Blood group not found: " + submission.getBloodGroupId()));
        BloodComponents bloodComponent = bloodComponentsRepository.findById(submission.getBloodComponentId())
                .orElseThrow(() -> new ResourceNotFoundException("Blood component not found: " + submission.getBloodComponentId()));

        BloodRequest request = new BloodRequest();
        request.setRecipientName(submission.getRecipientName());
        request.setMobileNumber(submission.getMobileNumber());
        request.setBloodComponent(bloodComponent);
        request.setBloodGroup(bloodGroup);
        request.setRequiredUnits(submission.getRequiredUnits());
        request.setDob(submission.getDob());
        request.setHospitalName(submission.getHospitalName());
        request.setAddress(submission.getAddress());
        request.setCity(submission.getCity());
        request.setDistrict(submission.getDistrict());
        request.setPincode(submission.getPincode());
        request.setCreatedAt(LocalDateTime.now());
        request.setCreatedBy("PUBLIC");
        request = bloodRequestRepository.save(request);

        int matchedCentres = matchingService.matchAndNotify(request);

        String message = matchedCentres > 0
                ? "Request matched with " + matchedCentres + " blood centre(s); they are being notified"
                : "No centre matched; our team will arrange donor outreach";

        return BloodRequestResponse.builder()
                .bloodRequestId(request.getBloodRequestId())
                .matched(matchedCentres > 0)
                .matchedCentreCount(matchedCentres)
                .message(message)
                .build();
    }
}
