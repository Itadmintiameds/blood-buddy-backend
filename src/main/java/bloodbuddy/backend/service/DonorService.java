package bloodbuddy.backend.service;

import bloodbuddy.backend.common.PagedResponse;
import bloodbuddy.backend.dto.common.LocationOptionsResponse;
import bloodbuddy.backend.dto.donor.DonorFilterRequest;
import bloodbuddy.backend.dto.donor.DonorRegistrationRequest;
import bloodbuddy.backend.dto.donor.DonorResponse;
import bloodbuddy.backend.dto.donor.DonorStatsResponse;
import bloodbuddy.backend.entity.BloodDonorDetails;
import bloodbuddy.backend.entity.masters.BloodGroup;
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
        donor.setCreatedAt(LocalDateTime.now());
        donor.setCreatedBy("SELF");

        return DonorMapper.toResponse(bloodDonorDetailsRepository.save(donor));
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
        LocalDate since = LocalDate.now().minusDays(RECENT_DONATION_WINDOW_DAYS);
        return DonorStatsResponse.builder()
                .totalDonors(bloodDonorDetailsRepository.count())
                .distinctBloodGroupCount(bloodDonorDetailsRepository.countDistinctBloodGroups())
                .recentDonationCount(
                        bloodDonorDetailsRepository.countByLastBloodDonationDateGreaterThanEqual(since))
                .build();
    }
}
