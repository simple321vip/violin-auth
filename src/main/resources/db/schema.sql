CREATE TABLE IF NOT EXISTS auth_master(
     auth_id INT NOT NULL AUTO_INCREMENT,
     phone_number CHAR(20) NOT NULL,
     PRIMARY KEY (auth_id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;