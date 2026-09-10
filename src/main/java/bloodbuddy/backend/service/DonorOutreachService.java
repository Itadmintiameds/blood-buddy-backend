package bloodbuddy.backend.service;

import bloodbuddy.backend.dto.donor.DonorResponse;
import bloodbuddy.backend.dto.outreach.CreateOutreachRequest;
import bloodbuddy.backend.dto.outreach.OutreachDetailResponse;
import bloodbuddy.backend.dto.outreach.UpdateOutreachRequest;
import bloodbuddy.backend.entity.BloodDonorDetails;
import bloodbuddy.backend.entity.BloodRequest;
import bloodbuddy.backend.entity.BloodRequestDetails;
import bloodbuddy.backend.exception.BadRequestException;
import bloodbuddy.backend.exception.ResourceNotFoundException;
import bloodbuddy.backend.repository.BloodDonorDetailsRepository;
import bloodbuddy.backend.repository.BloodRequestDetailsRepository;
import bloodbuddy.backend.repository.BloodRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DonorOutreachService {

    private static final String DEFAULT_STATUS = "contacted";

    private final BloodRequestRepository bloodRequestRepository;
    private final BloodDonorDetailsRepository bloodDonorDetailsRepository;
    private final BloodRequestDetailsRepository bloodRequestDetailsRepository;

    public DonorOutreachService(BloodRequestRepository bloodRequestRepository,
                                BloodDonorDetailsRepository bloodDonorDetailsRepository,
                                BloodRequestDetailsRepository bloodRequestDetailsRepository) {
        this.bloodRequestRepository = bloodRequestRepository;
        this.bloodDonorDetailsRepository = bloodDonorDetailsRepository;
        this.bloodRequestDetailsRepository = bloodRequestDetailsRepository;
    }

    /** Candidate donors for a request: same blood group, in the recipient's pincode/city/district. */
    @Transactional(readOnly = true)
    public List<DonorResponse> findCandidates(Long bloodRequestId) {
        BloodRequest request = requireRequest(bloodRequestId);
        return bloodDonorDetailsRepository.findCandidateDonors(
                        request.getBloodGroup().getBloodGroupId(),
                        request.getPincode(),
                        request.getCity(),
                        request.getDistrict()).stream()
                .map(DonorResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OutreachDetailResponse> listDetails(Long bloodRequestId) {
        requireRequest(bloodRequestId);
        return bloodRequestDetailsRepository.findByBloodRequest_BloodRequestId(bloodRequestId).stream()
                .map(OutreachDetailResponse::fromEntity)
                .toList();
    }

    /** Each admin contact = one detail row: donor set, centre null (outreach path). */
    @Transactional
    public OutreachDetailResponse createOutreach(CreateOutreachRequest request, String actor) {
        BloodRequest bloodRequest = requireRequest(request.getBloodRequestId());
        BloodDonorDetails donor = bloodDonorDetailsRepository.findById(request.getBloodDonorDetailsId())
                .orElseThrow(() -> new ResourceNotFoundException("Donor not found: " + request.getBloodDonorDetailsId()));

        BloodRequestDetails details = new BloodRequestDetails();
        details.setBloodRequest(bloodRequest);
        details.setBloodDonorDetails(donor);
        details.setBloodCentre(null);
        details.setStatus(request.getStatus() != null ? request.getStatus() : DEFAULT_STATUS);
        details.setRemarks(request.getRemarks());
        details.setCreatedAt(LocalDateTime.now());
        details.setCreatedBy(actor);

        // Exactly one of centre/donor must be set; this is the donor path.
        validateCentreXorDonor(details);

        return OutreachDetailResponse.fromEntity(bloodRequestDetailsRepository.save(details));
    }

    @Transactional
    public OutreachDetailResponse updateOutreach(Long detailsId, UpdateOutreachRequest request, String actor) {
        if (request.getStatus() == null && request.getRemarks() == null) {
            throw new BadRequestException("Provide status and/or remarks to update");
        }
        BloodRequestDetails details = bloodRequestDetailsRepository.findById(detailsId)
                .orElseThrow(() -> new ResourceNotFoundException("Request detail not found: " + detailsId));

        if (request.getStatus() != null) {
            details.setStatus(request.getStatus());
        }
        if (request.getRemarks() != null) {
            details.setRemarks(request.getRemarks());
        }
        details.setModifiedAt(LocalDateTime.now());
        details.setModifiedBy(actor);

        return OutreachDetailResponse.fromEntity(bloodRequestDetailsRepository.save(details));
    }

    private BloodRequest requireRequest(Long bloodRequestId) {
        return bloodRequestRepository.findById(bloodRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("Blood request not found: " + bloodRequestId));
    }

    // Mirror of the DB check constraint: never both, never neither.
    private void validateCentreXorDonor(BloodRequestDetails details) {
        boolean hasCentre = details.getBloodCentre() != null;
        boolean hasDonor = details.getBloodDonorDetails() != null;
        if (hasCentre == hasDonor) {
            throw new BadRequestException("A request detail must reference exactly one of a centre or a donor");
        }
    }
}
