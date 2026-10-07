package bloodbuddy.backend.repository.specification;

import bloodbuddy.backend.dto.donor.DonorFilterRequest;
import bloodbuddy.backend.entity.BloodDonorDetails;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds a dynamic {@link Specification} for the superadmin donor listing. Each filter is applied
 * only when supplied, so any combination of blood group / cities / districts / search works. Blood
 * group, city and district are matched directly against the donor row; search is a case-insensitive
 * substring match OR'd across the donor's text fields.
 */
public final class DonorSpecifications {

    private DonorSpecifications() {
    }

    public static Specification<BloodDonorDetails> withFilters(DonorFilterRequest filter) {
        return (root, query, cb) -> {
            // Keep bloodGroup eager on the content query so DonorResponse mapping doesn't trigger
            // N+1 lazy loads; skip the fetch on the count query where it is neither needed nor allowed.
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("bloodGroup", JoinType.LEFT);
            }

            List<Predicate> predicates = new ArrayList<>();

            if (!CollectionUtils.isEmpty(filter.getBloodGroupIds())) {
                predicates.add(root.get("bloodGroup").get("bloodGroupId").in(filter.getBloodGroupIds()));
            }

            if (!CollectionUtils.isEmpty(filter.getCities())) {
                List<String> cities = filter.getCities().stream()
                        .filter(StringUtils::hasText)
                        .map(c -> c.trim().toLowerCase())
                        .toList();
                if (!cities.isEmpty()) {
                    predicates.add(cb.lower(root.get("city")).in(cities));
                }
            }

            if (!CollectionUtils.isEmpty(filter.getDistricts())) {
                List<String> districts = filter.getDistricts().stream()
                        .filter(StringUtils::hasText)
                        .map(d -> d.trim().toLowerCase())
                        .toList();
                if (!districts.isEmpty()) {
                    predicates.add(cb.lower(root.get("district")).in(districts));
                }
            }

            if (StringUtils.hasText(filter.getSearch())) {
                String term = "%" + filter.getSearch().trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("fullName")), term),
                        cb.like(cb.lower(root.get("mobileNumber")), term),
                        cb.like(cb.lower(root.get("alternativeMobileNumber")), term),
                        cb.like(cb.lower(root.get("address")), term),
                        cb.like(cb.lower(root.get("city")), term),
                        cb.like(cb.lower(root.get("district")), term),
                        cb.like(cb.lower(root.get("pincode")), term)));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
