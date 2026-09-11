package bloodbuddy.backend.service;

import bloodbuddy.backend.common.PagedResponse;
import bloodbuddy.backend.dto.donor.DonorRegistrationRequest;
import bloodbuddy.backend.dto.donor.DonorResponse;
import bloodbuddy.backend.entity.BloodDonorDetails;
import bloodbuddy.backend.entity.masters.BloodGroup;
import bloodbuddy.backend.exception.ResourceNotFoundException;
import bloodbuddy.backend.repository.BloodDonorDetailsRepository;
import bloodbuddy.backend.repository.BloodGroupRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        return DonorResponse.fromEntity(bloodDonorDetailsRepository.save(donor));
    }

    @Transactional(readOnly = true)
    public List<DonorResponse> listAll() {
        return bloodDonorDetailsRepository.findAll().stream()
                .map(DonorResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public PagedResponse<DonorResponse> list(Pageable pageable) {
        return PagedResponse.fromPage(
                bloodDonorDetailsRepository.findAll(pageable).map(DonorResponse::fromEntity));
    }
}
