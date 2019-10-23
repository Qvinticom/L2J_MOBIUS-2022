-- Andreas Van Halter

DELETE FROM raidboss_spawnlist WHERE boss_id in (29062);
INSERT INTO `raidboss_spawnlist` (`boss_id`,`loc_x`,`loc_y`,`loc_z`,`heading`,`respawn_delay`,`respawn_random`,`currentHp`,`currentMp`) VALUES
(29062,-16373,-53562,-10447,0,129600,86400,275385,9999);
