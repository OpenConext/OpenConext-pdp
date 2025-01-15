CREATE TABLE `pdp_migrated_policies`
(
    `id`                       mediumint    NOT NULL AUTO_INCREMENT,
    `name`                     varchar(255) NOT NULL,
    `policy_id`                varchar(255) NOT NULL,
    `policy_xml`               text         NOT NULL,
    `authenticating_authority` varchar(256) NOT NULL,
    `user_identifier`          varchar(256) NOT NULL,
    `user_display_name`        varchar(256) NOT NULL,
    `ts`                       timestamp    NULL DEFAULT CURRENT_TIMESTAMP,
    `is_active`                tinyint(1)        DEFAULT '0',
    `type`                     varchar(255)      DEFAULT 'reg',
    PRIMARY KEY (`id`),
    UNIQUE KEY `pdp_policy_name` (`name`)
) ENGINE = InnoDB;
