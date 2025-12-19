-- Drop obsolete tables
DROP TABLE IF EXISTS pdp_migrated_policies;
DROP TABLE IF EXISTS pdp_policy_violations;

-- Remove obsolete columns from pdp_policies
ALTER TABLE pdp_policies DROP INDEX pdp_policy_name_revision_unique;
ALTER TABLE pdp_policies DROP COLUMN authenticating_authority;
ALTER TABLE pdp_policies DROP COLUMN user_identifier;
ALTER TABLE pdp_policies DROP COLUMN user_display_name;
ALTER TABLE pdp_policies DROP COLUMN revision_parent_id;
ALTER TABLE pdp_policies DROP COLUMN revision_nbr;
ALTER TABLE pdp_policies DROP COLUMN latest_revision;
