CREATE TABLE pdp_policy_violations (
  id           MEDIUMINT    NOT NULL AUTO_INCREMENT PRIMARY KEY,
  policy_id    MEDIUMINT    NOT NULL,
  json_request TEXT         NOT NULL,
  response     TEXT         NOT NULL,
  created      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  COLLATE = latin1_general_cs;

ALTER TABLE pdp_policy_violations ADD INDEX pdp_policy_violations_policy_id (policy_id);