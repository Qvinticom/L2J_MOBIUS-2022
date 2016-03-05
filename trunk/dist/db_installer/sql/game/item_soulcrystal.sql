DROP TABLE IF EXISTS `item_soulcrystal`;
CREATE TABLE `item_soulcrystal` (
  `object_id` int(11) NOT NULL,
  `slot_id` int(1) NOT NULL,
  `is_special` bit(1) NOT NULL,
  `effect_id` int(4) NOT NULL,
  PRIMARY KEY (`object_id`,`slot_id`,`is_special`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;