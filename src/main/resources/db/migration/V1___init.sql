CREATE TABLE pdp_policies (
  id          MEDIUMINT     NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name        VARCHAR(255)  NOT NULL,
  policy_id   VARCHAR(255)  NOT NULL,
  policy_xml  TEXT          NOT NULL
)
  ENGINE = InnoDB
  DEFAULT CHARSET = latin1
  COLLATE = latin1_general_cs;
