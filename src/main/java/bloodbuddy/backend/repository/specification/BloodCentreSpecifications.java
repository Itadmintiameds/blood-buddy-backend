package bloodbuddy.backend.repository.specification;

import bloodbuddy.backend.dto.centre.BloodCentreFilterRequest;
import bloodbuddy.backend.entity.BloodCentres;
import bloodbuddy.backend.entity.Inventory;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds a dynamic {@link Specification} for the superadmin blood-centre listing. Each filter is
 * applied only when supplied, so any combination of status / cities / blood group / component /
 * search works. Blood group and component are matched through an EXISTS subquery against inventory
 * (units in stock) rather than a join, which keeps centre rows unique and the paginated count exact.
 */
public final class BloodCentreSpecifications {

    private BloodCentreSpecifications() {
    }

    public static Specification<BloodCentres> withFilters(BloodCentreFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getIsActive() != null) {
                predicates.add(cb.equal(root.get("isActive"), filter.getIsActive()));
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

            // Blood group / component live only on inventory: match centres that have units in stock.
            if (!CollectionUtils.isEmpty(filter.getBloodGroupIds())
                    || !CollectionUtils.isEmpty(filter.getBloodComponentIds())) {
                Subquery<Long> sub = query.subquery(Long.class);
                Root<Inventory> inv = sub.from(Inventory.class);
                sub.select(inv.get("inventoryId"));

                List<Predicate> invPredicates = new ArrayList<>();
                invPredicates.add(cb.equal(inv.get("bloodCentre"), root));
                invPredicates.add(cb.greaterThan(inv.get("availableUnits"), 0L));
                if (!CollectionUtils.isEmpty(filter.getBloodGroupIds())) {
                    invPredicates.add(inv.get("bloodGroup").get("bloodGroupId").in(filter.getBloodGroupIds()));
                }
                if (!CollectionUtils.isEmpty(filter.getBloodComponentIds())) {
                    invPredicates.add(inv.get("bloodComponent").get("bloodComponentId").in(filter.getBloodComponentIds()));
                }
                sub.where(invPredicates.toArray(new Predicate[0]));
                predicates.add(cb.exists(sub));
            }

            if (StringUtils.hasText(filter.getSearch())) {
                String term = "%" + filter.getSearch().trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("bloodCentreName")), term),
                        cb.like(cb.lower(root.get("email")), term),
                        cb.like(cb.lower(root.get("mobileNumber")), term),
                        cb.like(cb.lower(root.get("address")), term),
                        cb.like(cb.lower(root.get("city")), term),
                        cb.like(cb.lower(root.get("district")), term),
                        cb.like(cb.lower(root.get("pincode")), term),
                        cb.like(cb.lower(root.get("bloodCentreLicenceNumber")), term)));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
