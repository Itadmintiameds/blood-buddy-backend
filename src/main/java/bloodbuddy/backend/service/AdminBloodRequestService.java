package bloodbuddy.backend.service;

import bloodbuddy.backend.common.PagedResponse;
import bloodbuddy.backend.dto.centre.BloodCentreResponse;
import bloodbuddy.backend.dto.common.LocationOptionsResponse;
import bloodbuddy.backend.dto.donor.DonorResponse;
import bloodbuddy.backend.dto.request.BloodRequestDetailResponse;
import bloodbuddy.backend.dto.request.BloodRequestFilterRequest;
import bloodbuddy.backend.dto.request.BloodRequestStatsResponse;
import bloodbuddy.backend.dto.request.BloodRequestSummaryResponse;
import bloodbuddy.backend.entity.BloodDonorDetails;
import bloodbuddy.backend.entity.BloodRequest;
import bloodbuddy.backend.entity.BloodRequestCentre;
import bloodbuddy.backend.entity.BloodRequestDonation;
import bloodbuddy.backend.entity.BloodRequestStatus;
import bloodbuddy.backend.exception.BadRequestException;
import bloodbuddy.backend.exception.ResourceNotFoundException;
import bloodbuddy.backend.mapper.BloodCentreMapper;
import bloodbuddy.backend.mapper.BloodRequestMapper;
import bloodbuddy.backend.mapper.DonorMapper;
import bloodbuddy.backend.repository.BloodDonorDetailsRepository;
import bloodbuddy.backend.repository.BloodRequestCentreRepository;
import bloodbuddy.backend.repository.BloodRequestDonationRepository;
import bloodbuddy.backend.repository.BloodRequestRepository;
import bloodbuddy.backend.repository.specification.BloodRequestSpecifications;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
                .map(BloodRequestMapper::toSummaryResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PagedResponse<BloodRequestSummaryResponse> list(BloodRequestFilterRequest filter, Pageable pageable) {
        return PagedResponse.fromPage(
                bloodRequestRepository.findAll(BloodRequestSpecifications.withFilters(filter), pageable)
                        .map(BloodRequestMapper::toSummaryResponse));
    }

    @Transactional(readOnly = true)
    public LocationOptionsResponse getLocationOptions() {
        return LocationOptionsResponse.builder()
                .cities(bloodRequestRepository.findDistinctCities())
                .districts(bloodRequestRepository.findDistinctDistricts())
                .build();
    }

    /** Statuses treated as "open" (still being worked) on the dashboard. */
    private static final List<BloodRequestStatus> OPEN_STATUSES =
            List.of(BloodRequestStatus.CENTRES_FOUND, BloodRequestStatus.NO_CENTRES_FOUND);

    /** Statuses treated as "closed" (fully or partially fulfilled) on the dashboard. */
    private static final List<BloodRequestStatus> CLOSED_STATUSES =
            List.of(BloodRequestStatus.CLOSED, BloodRequestStatus.PARTIALLY_CLOSED);

    @Transactional(readOnly = true)
    public BloodRequestStatsResponse getStats() {
        return BloodRequestStatsResponse.builder()
                .totalRequests(bloodRequestRepository.count())
                .openRequests(bloodRequestRepository.countByStatusIn(OPEN_STATUSES))
                .closedRequests(bloodRequestRepository.countByStatusIn(CLOSED_STATUSES))
                .closedUnits(bloodRequestRepository.sumClosedUnitsByStatusIn(CLOSED_STATUSES))
                .build();
    }

    @Transactional(readOnly = true)
    public BloodRequestDetailResponse getDetail(Long bloodRequestId) {
        BloodRequest request = requireRequest(bloodRequestId);
        return toDetail(request);
    }

    /**
     * Minimum gap (days) between two donations by the same donor. Whole-blood donors must wait
     * ~3 months before donating again; this also drives which donors are offered as candidates.
     */
    private static final int DONATION_ELIGIBILITY_GAP_DAYS = 90;

    /** Log a donor who agreed to and donated for this request. */
    @Transactional
    public BloodRequestDetailResponse recordDonation(Long bloodRequestId, Long bloodDonorDetailsId, String actor) {
        BloodRequest request = requireRequest(bloodRequestId);
        BloodDonorDetails donor = bloodDonorDetailsRepository.findById(bloodDonorDetailsId)
                .orElseThrow(() -> new ResourceNotFoundException("Donor not found: " + bloodDonorDetailsId));

        // Same donor cannot be recorded twice for one request.
        if (bloodRequestDonationRepository
                .existsByBloodRequest_BloodRequestIdAndBloodDonorDetails_BloodDonorDetailsId(
                        bloodRequestId, bloodDonorDetailsId)) {
            throw new BadRequestException("This donor is already recorded for this request");
        }

        // Enforce the eligibility gap: block if the donor donated within the last N days.
        LocalDate today = LocalDate.now();
        LocalDate lastDonation = donor.getLastBloodDonationDate();
        if (lastDonation != null && lastDonation.isAfter(today.minusDays(DONATION_ELIGIBILITY_GAP_DAYS))) {
            LocalDate nextEligible = lastDonation.plusDays(DONATION_ELIGIBILITY_GAP_DAYS);
            throw new BadRequestException(
                    "Donor last donated on " + lastDonation + " and is not eligible until " + nextEligible);
        }

        LocalDateTime donatedAt = LocalDateTime.now();

        BloodRequestDonation donation = new BloodRequestDonation();
        donation.setBloodRequest(request);
        donation.setBloodDonorDetails(donor);
        donation.setDonatedAt(donatedAt);
        donation.setCreatedBy(actor);
        bloodRequestDonationRepository.save(donation);

        // Recording a donation means the donor just donated, so refresh their last donation date.
        donor.setLastBloodDonationDate(donatedAt.toLocalDate());
        donor.setModifiedAt(donatedAt);
        donor.setModifiedBy(actor);
        bloodDonorDetailsRepository.save(donor);

        return toDetail(request);
    }

    /** Admin closes the request after following up with the recipient. */
    @Transactional
    public BloodRequestDetailResponse close(Long bloodRequestId, String remarks, Long closedUnits, String actor) {
        BloodRequest request = requireRequest(bloodRequestId);
        if (CLOSED_STATUSES.contains(request.getStatus())) {
            throw new BadRequestException("Request is already closed");
        }
        if (remarks != null) {
            request.setRemarks(remarks);
        }
        if (closedUnits != null) {
            if (closedUnits < 0) {
                throw new BadRequestException("Closed units cannot be negative");
            }
            if (request.getRequiredUnits() != null && closedUnits > request.getRequiredUnits()) {
                throw new BadRequestException(
                        "Closed units (" + closedUnits + ") cannot exceed required units ("
                                + request.getRequiredUnits() + ")");
            }
            request.setClosedUnits(closedUnits);
        }
        // Partially closed when fewer units were fulfilled than required; otherwise fully closed.
        request.setStatus(isPartiallyFulfilled(request)
                ? BloodRequestStatus.PARTIALLY_CLOSED
                : BloodRequestStatus.CLOSED);
        request.setModifiedAt(LocalDateTime.now());
        request.setModifiedBy(actor);
        return toDetail(request);
    }

    private boolean isPartiallyFulfilled(BloodRequest request) {
        Long required = request.getRequiredUnits();
        Long closed = request.getClosedUnits();
        return required != null && closed != null && closed < required;
    }

    private BloodRequestDetailResponse toDetail(BloodRequest request) {
        List<BloodCentreResponse> matchedCentres = bloodRequestCentreRepository
                .findByBloodRequest_BloodRequestId(request.getBloodRequestId()).stream()
                .map(BloodRequestCentre::getBloodCentre)
                .map(BloodCentreMapper::toResponse)
                .toList();

        List<DonorResponse> donatedBy = bloodRequestDonationRepository
                .findByBloodRequest_BloodRequestId(request.getBloodRequestId()).stream()
                .map(BloodRequestDonation::getBloodDonorDetails)
                .map(DonorMapper::toResponse)
                .toList();

        // Only surface donor candidates when no centre matched; otherwise the centres are the answer.
        LocalDate today = LocalDate.now();
        List<DonorResponse> donorCandidates = request.getStatus() == BloodRequestStatus.NO_CENTRES_FOUND
                ? bloodDonorDetailsRepository.findCandidateDonors(
                        request.getBloodGroup().getBloodGroupId(),
                        request.getPincode(),
                        request.getCity(),
                        request.getDistrict(),
                        today,
                        today.minusDays(DONATION_ELIGIBILITY_GAP_DAYS)).stream()
                        .map(DonorMapper::toResponse)
                        .toList()
                : List.of();

        return BloodRequestMapper.toDetailResponse(request, matchedCentres, donatedBy, donorCandidates);
    }

    private BloodRequest requireRequest(Long bloodRequestId) {
        return bloodRequestRepository.findById(bloodRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("Blood request not found: " + bloodRequestId));
    }
}
