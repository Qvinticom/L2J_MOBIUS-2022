DROP TABLE IF EXISTS `character_skills`;
CREATE TABLE IF NOT EXISTS `character_skills` (
  `charId` INT UNSIGNED NOT NULL DEFAULT 0,
  `skill_id` INT NOT NULL DEFAULT 0,
  `skill_level` INT(3) NOT NULL DEFAULT 1,
  `class_index` INT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`charId`,`skill_id`,`class_index`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
ALTER TABLE `character_skills` MODIFY COLUMN `skill_level` INT(4);
UPDATE `character_skills` SET skill_level=((skill_level % 100) + (round(skill_level / 100) * 1000)) WHERE skill_level <= 1000 AND skill_level >= 100;