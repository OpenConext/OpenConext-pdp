CREATE TABLE `pdp_policy_push_version`
(
    `id`         int NOT NULL,
    `version`    int NOT NULL,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `unique_version` (`version`)
) ENGINE = InnoDB;

INSERT INTO pdp_policy_push_version (id, version) VALUES (1,1);