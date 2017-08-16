ALTER TABLE pdp_policies ADD type VARCHAR(255) DEFAULT "reg";
UPDATE pdp_policies SET type = "reg";
