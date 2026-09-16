package bloodbuddy.backend.mapper;

import bloodbuddy.backend.dto.centre.BloodCentreResponse;
import bloodbuddy.backend.entity.BloodCentres;

/** Maps {@link BloodCentres} entities to their response DTOs. */
public final class BloodCentreMapper {

    private BloodCentreMapper() {
    }

    public static BloodCentreResponse toResponse(BloodCentres centre) {
        return BloodCentreResponse.builder()
                .bloodCentreId(centre.getBloodCentreId())
                .bloodCentreName(centre.getBloodCentreName())
                .bloodBankCategory(centre.getBloodBankCategory())
                .mobileNumber(centre.getMobileNumber())
                .email(centre.getEmail())
                .address(centre.getAddress())
                .district(centre.getDistrict())
                .city(centre.getCity())
                .pincode(centre.getPincode())
                .latitude(centre.getLatitude())
                .longitude(centre.getLongitude())
                .locationUrl(centre.getLocationUrl())
                .isActive(centre.getIsActive())
                .build();
    }
}
