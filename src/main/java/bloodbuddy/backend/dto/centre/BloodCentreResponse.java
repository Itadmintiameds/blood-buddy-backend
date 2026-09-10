package bloodbuddy.backend.dto.centre;

import bloodbuddy.backend.entity.BloodCentres;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BloodCentreResponse {

    private final Long bloodCentreId;
    private final String bloodCentreName;
    private final String bloodBankCategory;
    private final String mobileNumber;
    private final String email;
    private final String address;
    private final String district;
    private final String city;
    private final String pincode;
    private final Boolean isActive;

    public static BloodCentreResponse fromEntity(BloodCentres centre) {
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
                .isActive(centre.getIsActive())
                .build();
    }
}
