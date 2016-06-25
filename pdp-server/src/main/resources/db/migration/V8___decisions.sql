CREATE TABLE pdp_decisions (
  id              MEDIUMINT     NOT NULL AUTO_INCREMENT PRIMARY KEY,
  decision_json   TEXT          NOT NULL,
  created         TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  COLLATE = latin1_general_cs;
