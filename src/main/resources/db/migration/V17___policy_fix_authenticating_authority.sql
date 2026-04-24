ALTER TABLE pdp_policies
MODIFY authenticating_authority VARCHAR(256) NULL,
MODIFY user_identifier          VARCHAR(256) NULL,
MODIFY user_display_name        VARCHAR(256) NULL;
