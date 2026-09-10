-- BloodBuddy database constraints.
--
-- These are enforced in the service layer as well, but should also live in the
-- database as the authoritative guardrail. Apply manually (there is no migration
-- framework wired in) after the tables exist. All statements are idempotent-safe
-- to run once; re-running will error if the constraint already exists.

-- inventory: one running stock row per (centre, group, component).
-- Backs the add-availability upsert.
ALTER TABLE inventory
    ADD CONSTRAINT uq_inventory_centre_group_component
    UNIQUE (blood_centre_id, blood_group_id, blood_component_id);

-- inventory: available units must never go negative.
ALTER TABLE inventory
    ADD CONSTRAINT chk_inventory_units_non_negative
    CHECK (available_units >= 0);

-- blood_request_details: a detail row points at EITHER a centre (match found)
-- OR a donor (admin outreach) -- exactly one, never both, never neither.
ALTER TABLE blood_request_details
    ADD CONSTRAINT chk_request_details_centre_xor_donor
    CHECK (
        (blood_centre_id IS NOT NULL AND blood_donor_details_id IS NULL)
        OR
        (blood_centre_id IS NULL AND blood_donor_details_id IS NOT NULL)
    );
