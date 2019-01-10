CREATE TABLE IF NOT EXISTS `bifrost_history` (
  `id` smallint(5) unsigned NOT NULL DEFAULT '0',
  `discord_user` varchar(50) NOT NULL DEFAULT '',
  `command` varchar(50) NOT NULL DEFAULT '',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `UniqueUser` (`discord_user`)
) ENGINE=MyISAM;

CREATE TABLE IF NOT EXISTS `bifrost_user_weapon` (
  `id` smallint(5) unsigned NOT NULL DEFAULT '0',
  `discord_user` varchar(50) NOT NULL DEFAULT '',
  `weapon` smallint(5) NOT NULL DEFAULT '0',
  `refine` smallint(2) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `UniqueUserWeapon` (`discord_user`)
) ENGINE=MyISAM;