package bloodbuddy.backend.bootstrap;

import bloodbuddy.backend.util.MobileNumbers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Database guards that ddl-auto can't express, applied idempotently on startup (Postgres):
 * <ul>
 *   <li>Rewrites donor mobile numbers saved before normalization (e.g. "+91 98765 43210") to the
 *       canonical 10-digit form, then adds a unique index on blood_donor_details.mobile_number. If
 *       existing rows still share a number the index is skipped and the donor ids are logged so the
 *       duplicates can be merged by hand; it is created on the next startup once they are gone.</li>
 *   <li>A partial unique index allowing each superadmin only one PENDING donor import, so two
 *       simultaneous uploads can't both get past the service-level check.</li>
 * </ul>
 */
@Component
@Order(3)
public class DonorConstraintInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DonorConstraintInitializer.class);

    private final JdbcTemplate jdbcTemplate;

    public DonorConstraintInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // Profiles without ddl-auto (e.g. prod) may not have the tables yet; never block startup on that.
        if (tableExists("blood_donor_details")) {
            normalizeLegacyMobileNumbers();
            createDonorMobileUniqueIndex();
        } else {
            log.warn("blood_donor_details not found; skipped mobile number normalization and unique index");
        }
        if (tableExists("donor_import_batch")) {
            jdbcTemplate.execute("CREATE UNIQUE INDEX IF NOT EXISTS uk_donor_import_one_pending_per_user "
                    + "ON donor_import_batch (uploaded_by) WHERE status = 'PENDING'");
        } else {
            log.warn("donor_import_batch not found; skipped the one-pending-import-per-user index");
        }
    }

    private boolean tableExists(String table) {
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(
                "SELECT to_regclass(?) IS NOT NULL", Boolean.class, table));
    }

    private void normalizeLegacyMobileNumbers() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT blood_donor_details_id, mobile_number FROM blood_donor_details "
                        + "WHERE mobile_number IS NOT NULL AND mobile_number !~ '^[6-9][0-9]{9}$'");
        int updated = 0;
        int invalid = 0;
        for (Map<String, Object> row : rows) {
            String normalized = MobileNumbers.normalize((String) row.get("mobile_number"));
            if (normalized == null) {
                invalid++;
                continue;
            }
            jdbcTemplate.update("UPDATE blood_donor_details SET mobile_number = ? WHERE blood_donor_details_id = ?",
                    normalized, row.get("blood_donor_details_id"));
            updated++;
        }
        if (updated > 0) {
            log.info("Normalized {} donor mobile number(s) to the 10-digit form", updated);
        }
        if (invalid > 0) {
            log.warn("{} donor(s) have a mobile number that isn't a valid Indian mobile; fix them via donor edit",
                    invalid);
        }
    }

    private void createDonorMobileUniqueIndex() {
        // Ids only: phone numbers are personal data and stay out of the logs.
        List<String> duplicateGroups = jdbcTemplate.queryForList(
                "SELECT string_agg(blood_donor_details_id::text, ',' ORDER BY blood_donor_details_id) "
                        + "FROM blood_donor_details WHERE mobile_number IS NOT NULL "
                        + "GROUP BY mobile_number HAVING COUNT(*) > 1",
                String.class);
        if (!duplicateGroups.isEmpty()) {
            log.warn("Unique index on donor mobile_number NOT created: {} number(s) are shared by several donors "
                    + "(donor ids per number: {}). Merge or fix them and restart.",
                    duplicateGroups.size(), duplicateGroups);
            return;
        }
        jdbcTemplate.execute("CREATE UNIQUE INDEX IF NOT EXISTS uk_donor_mobile_number "
                + "ON blood_donor_details (mobile_number)");
    }
}
