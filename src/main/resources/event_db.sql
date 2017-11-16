DROP TABLE IF EXISTS `event_db`;
CREATE TABLE `event_db` (
  `id` smallint(5) unsigned NOT NULL DEFAULT '0',
  `name` varchar(50) NOT NULL DEFAULT '',
  `schedule` text NOT NULL,
  `duration` int(10) NOT NULL DEFAULT 0,
  `start` timestamp NOT NULL,
  `end` timestamp,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `EventName` (`name`)
) ENGINE=MyISAM;

REPLACE INTO `event_db` VALUES (1, 'Disguise Event', '0 5 4,13,19,22 * * ?', (1000*60*60), timestamp '2015-11-14 00:00:00', NULL);
REPLACE INTO `event_db` VALUES (2, 'PvP Tournament', '0 0 9 1 * ?', (1000*60*60), timestamp '2015-11-14 00:00:00', timestamp '2015-11-14 00:00:00');
REPLACE INTO `event_db` VALUES (3, 'Dice Event', '0 5 2-18/6 ? * *', (1000*60*60), timestamp '2015-11-14 00:00:00', NULL);
REPLACE INTO `event_db` VALUES (4, 'War Of Emperium 1', '0 0 7 ? * SAT', (1000*60*60), timestamp '2015-11-14 00:00:00', NULL);
REPLACE INTO `event_db` VALUES (5, 'War Of Emperium 2', '0 0 12 ? * SAT', (1000*60*60), timestamp '2015-11-14 00:00:00', NULL);
REPLACE INTO `event_db` VALUES (6, 'Draft War Of Emperium', '0 0 8 ? * SUN', (1000*60*60), timestamp '2015-11-14 00:00:00', NULL);
REPLACE INTO `event_db` VALUES (7, 'Mysterious Slot Machine', '0 5 5/6 ? * *', (1000*60*60), timestamp '2015-11-14 00:00:00', NULL);
REPLACE INTO `event_db` VALUES (8, 'Happy Hour: Monster Hunter', '0 1 3,9,16,21 * * ?', (1000*60*60), timestamp '2015-11-14 00:00:00', NULL);
REPLACE INTO `event_db` VALUES (9, 'Happy Hour: Battlegrounds', '0 0 1,6,12,18 * * ?', (1000*60*60), timestamp '2015-11-14 00:00:00', NULL);
REPLACE INTO `event_db` VALUES (10, 'Halloween Invasion', '0 */45 * ? * *', (1000*60*60), timestamp '2017-10-21 00:00:00', timestamp '2017-11-16 00:00:00');