package bloodbuddy.backend.mapper;

import bloodbuddy.backend.dto.donor.DonorResponse;
import bloodbuddy.backend.entity.BloodDonorDetails;
import bloodbuddy.backend.entity.DonorStatus;

import java.time.LocalDate;

/** Maps {@link BloodDonorDetails} entities to their response DTOs. */
public final class DonorMapper {

    private DonorMapper() {
    }

    public static DonorResponse toResponse(BloodDonorDetails donor) {
        return DonorResponse.builder()
                .bloodDonorDetailsId(donor.getBloodDonorDetailsId())
                .fullName(donor.getFullName())
                .mobileNumber(donor.getMobileNumber())
                .alternativeMobileNumber(donor.getAlternativeMobileNumber())
                .bloodGroupId(donor.getBloodGroup() != null ? donor.getBloodGroup().getBloodGroupId() : null)
                .bloodGroupName(donor.getBloodGroup() != null ? donor.getBloodGroup().getBloodGroupName() : null)
                .dob(donor.getDob())
                .address(donor.getAddress())
                .city(donor.getCity())
                .district(donor.getDistrict())
                .pincode(donor.getPincode())
                .lastBloodDonationDate(donor.getLastBloodDonationDate())
                .createdAt(donor.getCreatedAt())
                .status(donor.getStatus())
                .unavailabilityReason(donor.getUnavailabilityReason())
                .remarks(donor.getRemarks())
                .lockedFrom(donor.getLockedFrom())
                .lockedUntil(donor.getLockedUntil())
                .available(isAvailable(donor))
                .build();
    }

    /**
     * A donor is available when not deactivated and not inside an active lock window. Null
     * status is treated as ACTIVE; a lock whose end date has passed counts as available.
     */
    private static boolean isAvailable(BloodDonorDetails donor) {
        DonorStatus status = donor.getStatus();
        if (status == null || status == DonorStatus.ACTIVE) {
            return true;
        }
        if (status == DonorStatus.LOCKED) {
            return donor.getLockedUntil() != null && donor.getLockedUntil().isBefore(LocalDate.now());
        }
        return false;
    }
}
