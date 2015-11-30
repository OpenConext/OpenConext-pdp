--
-- Store the authenticatingAuthority and identity of the person who created this policy
--
-- At this moment in development we can permit to clean up the policies
--
DELETE FROM pdp_policies;
DELETE FROM pdp_policy_violations;
ALTER TABLE pdp_policies ADD authenticating_authority varchar(256) NOT NULL;
ALTER TABLE pdp_policies ADD user_identifier varchar(256) NOT NULL;
ALTER TABLE pdp_policies ADD user_display_name varchar(256) NOT NULL;
ALTER TABLE pdp_policies ADD ts TIMESTAMP DEFAULT CURRENT_TIMESTAMP;