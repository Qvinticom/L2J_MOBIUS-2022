DROP TABLE IF EXISTS `character_friends`;
CREATE TABLE IF NOT EXISTS `character_friends` (
  `charId` INT UNSIGNED NOT NULL DEFAULT 0,
  `friendId` INT UNSIGNED NOT NULL DEFAULT 0,
  `relation` INT UNSIGNED NOT NULL DEFAULT 0,
  `memo` text,
  PRIMARY KEY (`charId`,`friendId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;