create TABLE IF NOT EXISTS `auth_master`(
   `phone_number` CHAR(20) NOT NULL,
   PRIMARY KEY ( `phone_number` )
)ENGINE=InnoDB DEFAULT CHARSET=utf8;