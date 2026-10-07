package bloodbuddy.backend.repository.specification;

import bloodbuddy.backend.dto.request.BloodRequestFilterRequest;
import bloodbuddy.backend.entity.BloodRequest;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds a dynamic {@link Specification} for the superadmin blood-request listing. Each filter is
 * applied only when supplied, so any combination of status / blood group / component / cities /
 * districts / search works. Status, blood group, component, city and district are matched directly
 * against the request row; search is a case-insensitive substring match OR'd across its text fields.
 */
public final class BloodRequestSpecifications {

    private BloodRequestSpecifications() {
    }

    public static Specification<BloodRequest> withFilters(BloodRequestFilterRequest filter) {
        return (root, query, cb) -> {
            // Keep bloodGroup + bloodComponent eager on the content query so summary mapping doesn't
            // trigger N+1 lazy loads; skip the fetch on the count query where it is neither needed nor allowed.
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("bloodGroup", JoinType.LEFT);
                root.fetch("bloodComponent", JoinType.LEFT);
            }

            List<Predicate> predicates = new ArrayList<>();

            if (!CollectionUtils.isEmpty(filter.getStatuses())) {
                predicates.add(root.get("status").in(filter.getStatuses()));
            }

            if (!CollectionUtils.isEmpty(filter.getBloodGroupIds())) {
                predicates.add(root.get("bloodGroup").get("bloodGroupId").in(filter.getBloodGroupIds()));
            }

            if (!CollectionUtils.isEmpty(filter.getBloodComponentIds())) {
                predicates.add(root.get("bloodComponent").get("bloodComponentId").in(filter.getBloodComponentIds()));
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
                        cb.like(cb.lower(root.get("recipientName")), term),
                        cb.like(cb.lower(root.get("mobileNumber")), term),
                        cb.like(cb.lower(root.get("hospitalName")), term),
                        cb.like(cb.lower(root.get("address")), term),
                        cb.like(cb.lower(root.get("city")), term),
                        cb.like(cb.lower(root.get("district")), term),
                        cb.like(cb.lower(root.get("pincode")), term)));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
