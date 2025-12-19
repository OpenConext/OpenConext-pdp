-- Drop obsolete tables
DROP TABLE IF EXISTS pdp_migrated_policies;
DROP TABLE IF EXISTS pdp_policy_violations;

-- Remove obsolete columns from pdp_policies
ALTER TABLE pdp_policies
DROP COLUMN authenticating_authority,
    DROP COLUMN user_identifier,
    DROP COLUMN user_display_name,
    DROP COLUMN revision_parent_id,
    DROP COLUMN revision_nbr,
    DROP COLUMN latest_revision;
