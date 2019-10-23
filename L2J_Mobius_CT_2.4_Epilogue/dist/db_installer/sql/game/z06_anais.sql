-- Anais

DELETE FROM raidboss_spawnlist WHERE boss_id in(25701,29096);
INSERT INTO `raidboss_spawnlist` (`boss_id`,`loc_x`,`loc_y`,`loc_z`,`heading`,`respawn_delay`,`respawn_random`,`currentHp`,`currentMp`) VALUES
(29096,112798,-76800,-10,-15544,129600,86400,2231403,48422);
