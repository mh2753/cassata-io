CREATE TABLE if not exists `_saas_eventlog` (
      `id` int(11) NOT NULL AUTO_INCREMENT,
      `_saas_events_id` int(11),
      `http_response_code` integer NOT NULL,
      `http_response`  varchar(2000) DEFAULT NULL,
      `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
      PRIMARY KEY (`id`),
      KEY `idx_events_id` (`_saas_events_id`)
    ) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=latin1
;
