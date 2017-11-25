CREATE TABLE IF NOT EXISTS `bifrost_history` (
  `id` smallint(5) unsigned NOT NULL DEFAULT '0',
  `discord_user` varchar(50) NOT NULL DEFAULT '',
  `command` varchar(50) NOT NULL DEFAULT '',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `UniqueUser` (`discord_user`)
) ENGINE=MyISAM;

CREATE TABLE IF NOT EXISTS `bifrost_ws_history` (
  `id` smallint(5) unsigned NOT NULL DEFAULT '0',
  `discord_user` varchar(50) NOT NULL DEFAULT '',
  `command` varchar(50) NOT NULL DEFAULT '',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `UniqueUser` (`discord_user`)
) ENGINE=MyISAM;