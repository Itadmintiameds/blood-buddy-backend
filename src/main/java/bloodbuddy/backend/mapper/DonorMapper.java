package bloodbuddy.backend.mapper;

import bloodbuddy.backend.dto.donor.DonorAvailabilityStatus;
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
                .status(availabilityStatus(donor))
                .unavailabilityReason(donor.getUnavailabilityReason())
                .remarks(donor.getRemarks())
                .lockedFrom(donor.getLockedFrom())
                .lockedUntil(donor.getLockedUntil())
                .available(availabilityStatus(donor) == DonorAvailabilityStatus.ACTIVE)
                .build();
    }

    /**
     * Effective status today: DEACTIVATED when deactivated, LOCKED when today falls inside the
     * lock window, otherwise ACTIVE (null status is treated as ACTIVE).
     */
    private static DonorAvailabilityStatus availabilityStatus(BloodDonorDetails donor) {
        if (donor.getStatus() == DonorStatus.DEACTIVATED) {
            return DonorAvailabilityStatus.DEACTIVATED;
        }
        LocalDate today = LocalDate.now();
        boolean locked = donor.getLockedUntil() != null
                && !donor.getLockedUntil().isBefore(today)
                && (donor.getLockedFrom() == null || !donor.getLockedFrom().isAfter(today));
        return locked ? DonorAvailabilityStatus.LOCKED : DonorAvailabilityStatus.ACTIVE;
    }
}
