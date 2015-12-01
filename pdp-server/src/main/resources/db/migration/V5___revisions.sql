--
-- Add latest_revision in order to track changes
--
ALTER TABLE pdp_policies ADD revision_parent_id MEDIUMINT;
ALTER TABLE pdp_policies ADD revision_nbr MEDIUMINT DEFAULT 0;
ALTER TABLE pdp_policies ADD latest_revision tinyint(1) DEFAULT 0;

ALTER TABLE pdp_policies DROP INDEX pdp_policy_name_unique;
ALTER TABLE pdp_policies ADD UNIQUE INDEX pdp_policy_name_revision_unique (name, revision_nbr);

ALTER TABLE pdp_policies ADD INDEX pdp_policy_revision_parent_id (revision_parent_id);

