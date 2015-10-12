CREATE TABLE pdp_policy_violations (
  id           MEDIUMINT    NOT NULL AUTO_INCREMENT PRIMARY KEY,
  policy_id    VARCHAR(255) NOT NULL,
  policy_name  VARCHAR(255) NOT NULL,
  json_request TEXT         NOT NULL,
  response     TEXT         NOT NULL,
  created      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  COLLATE = latin1_general_cs;
