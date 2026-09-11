package bloodbuddy.backend.service;

import bloodbuddy.backend.common.PagedResponse;
import bloodbuddy.backend.dto.centre.BloodCentreResponse;
import bloodbuddy.backend.dto.donor.DonorResponse;
import bloodbuddy.backend.dto.request.BloodRequestDetailResponse;
import bloodbuddy.backend.dto.request.BloodRequestSummaryResponse;
import bloodbuddy.backend.entity.BloodDonorDetails;
import bloodbuddy.backend.entity.BloodRequest;
import bloodbuddy.backend.entity.BloodRequestCentre;
import bloodbuddy.backend.entity.BloodRequestDonation;
import bloodbuddy.backend.entity.BloodRequestStatus;
import bloodbuddy.backend.exception.BadRequestException;
import bloodbuddy.backend.exception.ResourceNotFoundException;
import bloodbuddy.backend.repository.BloodDonorDetailsRepository;
import bloodbuddy.backend.repository.BloodRequestCentreRepository;
import bloodbuddy.backend.repository.BloodRequestDonationRepository;
import bloodbuddy.backend.repository.BloodRequestRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/** Superadmin management of blood requests: list, drill-in, record donations, and close. */
@Service
public class AdminBloodRequestService {

    private final BloodRequestRepository bloodRequestRepository;
    private final BloodRequestCentreRepository bloodRequestCentreRepository;
    private final BloodRequestDonationRepository bloodRequestDonationRepository;
    private final BloodDonorDetailsRepository bloodDonorDetailsRepository;

    public AdminBloodRequestService(BloodRequestRepository bloodRequestRepository,
                                    BloodRequestCentreRepository bloodRequestCentreRepository,
                                    BloodRequestDonationRepository bloodRequestDonationRepository,
                                    BloodDonorDetailsRepository bloodDonorDetailsRepository) {
        this.bloodRequestRepository = bloodRequestRepository;
        this.bloodRequestCentreRepository = bloodRequestCentreRepository;
        this.bloodRequestDonationRepository = bloodRequestDonationRepository;
        this.bloodDonorDetailsRepository = bloodDonorDetailsRepository;
    }

    @Transactional(readOnly = true)
    public List<BloodRequestSummaryResponse> listAll() {
        return bloodRequestRepository.findAll().stream()
                .map(BloodRequestSummaryResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public PagedResponse<BloodRequestSummaryResponse> list(Pageable pageable) {
        return PagedResponse.fromPage(
                bloodRequestRepository.findAll(pageable).map(BloodRequestSummaryResponse::fromEntity));
    }

    @Transactional(readOnly = true)
    public BloodRequestDetailResponse getDetail(Long bloodRequestId) {
        BloodRequest request = requireRequest(bloodRequestId);
        return toDetail(request);
    }

    /** Log a donor who agreed to and donated for this request. */
    @Transactional
    public BloodRequestDetailResponse recordDonation(Long bloodRequestId, Long bloodDonorDetailsId, String actor) {
        BloodRequest request = requireRequest(bloodRequestId);
        BloodDonorDetails donor = bloodDonorDetailsRepository.findById(bloodDonorDetailsId)
                .orElseThrow(() -> new ResourceNotFoundException("Donor not found: " + bloodDonorDetailsId));

        BloodRequestDonation donation = new BloodRequestDonation();
        donation.setBloodRequest(request);
        donation.setBloodDonorDetails(donor);
        donation.setDonatedAt(LocalDateTime.now());
        donation.setCreatedBy(actor);
        bloodRequestDonationRepository.save(donation);

        return toDetail(request);
    }

    /** Admin closes the request after following up with the recipient. */
    @Transactional
    public BloodRequestDetailResponse close(Long bloodRequestId, String remarks, String actor) {
        BloodRequest request = requireRequest(bloodRequestId);
        if (request.getStatus() == BloodRequestStatus.CLOSED) {
            throw new BadRequestException("Request is already closed");
        }
        request.setStatus(BloodRequestStatus.CLOSED);
        if (remarks != null) {
            request.setRemarks(remarks);
        }
        request.setModifiedAt(LocalDateTime.now());
        request.setModifiedBy(actor);
        return toDetail(request);
    }

    private BloodRequestDetailResponse toDetail(BloodRequest request) {
        List<BloodCentreResponse> matchedCentres = bloodRequestCentreRepository
                .findByBloodRequest_BloodRequestId(request.getBloodRequestId()).stream()
                .map(BloodRequestCentre::getBloodCentre)
                .map(BloodCentreResponse::fromEntity)
                .toList();

        List<DonorResponse> donatedBy = bloodRequestDonationRepository
                .findByBloodRequest_BloodRequestId(request.getBloodRequestId()).stream()
                .map(BloodRequestDonation::getBloodDonorDetails)
                .map(DonorResponse::fromEntity)
                .toList();

        // Only surface donor candidates when no centre matched; otherwise the centres are the answer.
        List<DonorResponse> donorCandidates = request.getStatus() == BloodRequestStatus.NO_CENTRES_FOUND
                ? bloodDonorDetailsRepository.findCandidateDonors(
                        request.getBloodGroup().getBloodGroupId(),
                        request.getPincode(),
                        request.getCity(),
                        request.getDistrict()).stream()
                        .map(DonorResponse::fromEntity)
                        .toList()
                : List.of();

        return BloodRequestDetailResponse.fromEntity(request, matchedCentres, donatedBy, donorCandidates);
    }

    private BloodRequest requireRequest(Long bloodRequestId) {
        return bloodRequestRepository.findById(bloodRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("Blood request not found: " + bloodRequestId));
    }
}
