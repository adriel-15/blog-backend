use `arblog-db`;

CREATE TABLE `authority` (
  `id` int NOT NULL AUTO_INCREMENT,
  `auth` varchar(128) NOT NULL,
  PRIMARY KEY (`id`)
) AUTO_INCREMENT=1;