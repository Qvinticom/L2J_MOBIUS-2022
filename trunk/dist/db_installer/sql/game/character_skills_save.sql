DROP TABLE IF EXISTS `character_skills_save`;
CREATE TABLE IF NOT EXISTS `character_skills_save` (
  `charId` INT NOT NULL DEFAULT 0,
  `skill_id` INT NOT NULL DEFAULT 0,
  `skill_level` INT(3) NOT NULL DEFAULT 1,
  `remaining_time` INT NOT NULL DEFAULT 0,
  `reuse_delay` INT(8) NOT NULL DEFAULT 0,
  `systime` bigint(13) unsigned NOT NULL DEFAULT '0',
  `restore_type` INT(1) NOT NULL DEFAULT 0,
  `class_index` INT(1) NOT NULL DEFAULT 0,
  `buff_index` INT(2) NOT NULL DEFAULT 0,
  PRIMARY KEY (`charId`,`skill_id`,`skill_level`,`class_index`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
ALTER TABLE `character_skills_save` MODIFY COLUMN `skill_level` INT(4);
UPDATE `character_skills_save` SET skill_level=((skill_level % 100) + (round(skill_level / 100) * 1000)) WHERE skill_level <= 1000 AND skill_level >= 100;