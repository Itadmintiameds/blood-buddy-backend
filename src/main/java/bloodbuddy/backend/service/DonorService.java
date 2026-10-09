package bloodbuddy.backend.service;

import bloodbuddy.backend.common.PagedResponse;
import bloodbuddy.backend.dto.common.LocationOptionsResponse;
import bloodbuddy.backend.dto.donor.DonorDeactivateRequest;
import bloodbuddy.backend.dto.donor.DonorFilterRequest;
import bloodbuddy.backend.dto.donor.DonorLockRequest;
import bloodbuddy.backend.dto.donor.DonorRegistrationRequest;
import bloodbuddy.backend.dto.donor.DonorResponse;
import bloodbuddy.backend.dto.donor.DonorStatsResponse;
import bloodbuddy.backend.entity.BloodDonorDetails;
import bloodbuddy.backend.entity.DonorStatus;
import bloodbuddy.backend.entity.DonorUnavailabilityReason;
import bloodbuddy.backend.entity.masters.BloodGroup;
import bloodbuddy.backend.exception.BadRequestException;
import bloodbuddy.backend.exception.ResourceNotFoundException;
import bloodbuddy.backend.mapper.DonorMapper;
import bloodbuddy.backend.repository.BloodDonorDetailsRepository;
import bloodbuddy.backend.repository.BloodGroupRepository;
import bloodbuddy.backend.repository.specification.DonorSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DonorService {

    private final BloodDonorDetailsRepository bloodDonorDetailsRepository;
    private final BloodGroupRepository bloodGroupRepository;

    public DonorService(BloodDonorDetailsRepository bloodDonorDetailsRepository,
                        BloodGroupRepository bloodGroupRepository) {
        this.bloodDonorDetailsRepository = bloodDonorDetailsRepository;
        this.bloodGroupRepository = bloodGroupRepository;
    }

    @Transactional
    public DonorResponse register(DonorRegistrationRequest request) {
        BloodGroup bloodGroup = bloodGroupRepository.findById(request.getBloodGroupId())
                .orElseThrow(() -> new ResourceNotFoundException("Blood group not found: " + request.getBloodGroupId()));

        BloodDonorDetails donor = new BloodDonorDetails();
        donor.setFullName(request.getFullName());
        donor.setMobileNumber(request.getMobileNumber());
        donor.setAlternativeMobileNumber(request.getAlternativeMobileNumber());
        donor.setBloodGroup(bloodGroup);
        donor.setDob(request.getDob());
        donor.setAddress(request.getAddress());
        donor.setCity(request.getCity());
        donor.setDistrict(request.getDistrict());
        donor.setPincode(request.getPincode());
        donor.setLastBloodDonationDate(request.getLastBloodDonationDate());
        donor.setStatus(DonorStatus.ACTIVE);
        donor.setCreatedAt(LocalDateTime.now());
        donor.setCreatedBy("SELF");

        return DonorMapper.toResponse(bloodDonorDetailsRepository.save(donor));
    }

    /**
     * Put a donor on a temporary lock-in period (illness / out-of-station). The donor drops
     * off the admin and candidate lists and auto-reappears once {@code lockedUntil} passes.
     */
    @Transactional
    public DonorResponse lockDonor(Long donorId, DonorLockRequest request, String actor) {
        BloodDonorDetails donor = requireDonor(donorId);

        if (!request.getReason().isLockReason()) {
            throw new BadRequestException(
                    "Reason " + request.getReason() + " is not valid for a lock; use one of "
                            + DonorUnavailabilityReason.LOCK_REASONS);
        }

        LocalDate from = request.getLockedFrom() != null ? request.getLockedFrom() : LocalDate.now();
        LocalDate until = request.getLockedUntil();
        if (until.isBefore(from)) {
            throw new BadRequestException("lockedUntil cannot be before lockedFrom");
        }

        donor.setStatus(DonorStatus.LOCKED);
        donor.setUnavailabilityReason(request.getReason());
        donor.setLockedFrom(from);
        donor.setLockedUntil(until);
        donor.setRemarks(request.getRemarks());
        touch(donor, actor);

        return DonorMapper.toResponse(bloodDonorDetailsRepository.save(donor));
    }

    /** Permanently deactivate a donor (medical grounds / deceased / relocated). */
    @Transactional
    public DonorResponse deactivateDonor(Long donorId, DonorDeactivateRequest request, String actor) {
        BloodDonorDetails donor = requireDonor(donorId);

        if (!request.getReason().isDeactivationReason()) {
            throw new BadRequestException(
                    "Reason " + request.getReason() + " is not valid for deactivation; use one of "
                            + DonorUnavailabilityReason.DEACTIVATION_REASONS);
        }

        donor.setStatus(DonorStatus.DEACTIVATED);
        donor.setUnavailabilityReason(request.getReason());
        donor.setRemarks(request.getRemarks());
        // Lock dates are meaningless once deactivated.
        donor.setLockedFrom(null);
        donor.setLockedUntil(null);
        touch(donor, actor);

        return DonorMapper.toResponse(bloodDonorDetailsRepository.save(donor));
    }

    /** Restore a donor to ACTIVE, clearing any lock window / reason (e.g. early recovery). */
    @Transactional
    public DonorResponse reactivateDonor(Long donorId, String actor) {
        BloodDonorDetails donor = requireDonor(donorId);

        donor.setStatus(DonorStatus.ACTIVE);
        donor.setUnavailabilityReason(null);
        donor.setLockedFrom(null);
        donor.setLockedUntil(null);
        donor.setRemarks(null);
        touch(donor, actor);

        return DonorMapper.toResponse(bloodDonorDetailsRepository.save(donor));
    }

    private BloodDonorDetails requireDonor(Long donorId) {
        return bloodDonorDetailsRepository.findById(donorId)
                .orElseThrow(() -> new ResourceNotFoundException("Donor not found: " + donorId));
    }

    private void touch(BloodDonorDetails donor, String actor) {
        donor.setModifiedAt(LocalDateTime.now());
        donor.setModifiedBy(actor);
    }

    @Transactional(readOnly = true)
    public List<DonorResponse> listAll() {
        return bloodDonorDetailsRepository.findAll().stream()
                .map(DonorMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PagedResponse<DonorResponse> list(DonorFilterRequest filter, Pageable pageable) {
        return PagedResponse.fromPage(
                bloodDonorDetailsRepository.findAll(DonorSpecifications.withFilters(filter), pageable)
                        .map(DonorMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public LocationOptionsResponse getLocationOptions() {
        return LocationOptionsResponse.builder()
                .cities(bloodDonorDetailsRepository.findDistinctCities())
                .districts(bloodDonorDetailsRepository.findDistinctDistricts())
                .build();
    }

    /** Trailing window (days) counted as a "recent" donation for the dashboard. */
    private static final int RECENT_DONATION_WINDOW_DAYS = 30;

    @Transactional(readOnly = true)
    public DonorStatsResponse getStats() {
        LocalDate today = LocalDate.now();
        LocalDate since = today.minusDays(RECENT_DONATION_WINDOW_DAYS);
        return DonorStatsResponse.builder()
                .totalDonors(bloodDonorDetailsRepository.count())
                .distinctBloodGroupCount(bloodDonorDetailsRepository.countDistinctBloodGroups())
                .recentDonationCount(
                        bloodDonorDetailsRepository.countByLastBloodDonationDateGreaterThanEqual(since))
                .lockedDonors(bloodDonorDetailsRepository
                        .countByStatusAndLockedUntilGreaterThanEqual(DonorStatus.LOCKED, today))
                .deactivatedDonors(bloodDonorDetailsRepository.countByStatus(DonorStatus.DEACTIVATED))
                .build();
    }
}
