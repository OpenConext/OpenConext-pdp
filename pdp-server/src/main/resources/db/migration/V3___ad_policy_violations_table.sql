CREATE TABLE pdp_policy_violations (
  id                   MEDIUMINT    NOT NULL AUTO_INCREMENT PRIMARY KEY,
  associated_advice_id VARCHAR(255) NOT NULL,
  json_request         TEXT         NOT NULL
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  COLLATE = latin1_general_cs;
