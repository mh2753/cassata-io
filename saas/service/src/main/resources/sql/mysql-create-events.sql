CREATE TABLE if not exists `_saas_events` (
      `id` int(11) NOT NULL AUTO_INCREMENT,
      `event_id` varchar(50) NOT NULL,
      `application` varchar(100) DEFAULT NULL,
      `event_json` json NOT NULL,
      `http_method` varchar(15) DEFAULT NULL,
      `http_headers` json DEFAULT NULL,
      `destination_url` varchar(2000) DEFAULT NULL,
      `status` varchar(30) NOT NULL,
      `expiry` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
      `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
      PRIMARY KEY (`id`),
      UNIQUE KEY `idx_id_app` (`event_id`,`application`),
      KEY `idx_expirty` (`expiry`),
      KEY `idx_status` (`status`)
    ) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=latin1
;
