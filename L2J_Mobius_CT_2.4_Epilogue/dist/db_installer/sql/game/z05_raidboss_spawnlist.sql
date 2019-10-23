-- Raidbosses that where removed in Hi5
-- taken from l2j Epilogue branch and client comparison

DELETE FROM raidboss_spawnlist WHERE boss_id in(25073,25109,25176,25217,25234,25407,25412,25696,25697,25698,25725,25726,25727);

INSERT INTO `raidboss_spawnlist` (`boss_id`,`loc_x`,`loc_y`,`loc_z`,`heading`,`respawn_delay`,`respawn_random`,`currentHp`,`currentMp`) VALUES
(25073,143265,110044,-3944,0,129600,86400,875948,2917), -- Bloody Priest Rudelto (69)
(25109,152660,110387,-5520,0,129600,86400,935092,3274), -- Antharas Priest Cloe (74)
(25176,92544,115232,-3200,0,129600,86400,451391,1975), -- Black Lily (55)
(25217,89904,105712,-3292,0,129600,86400,369009,1660), -- Cursed Clara (50)
(25234,120080,111248,-3047,0,129600,86400,1052436,2301), -- Ancient Weird Drake (60)
(25407,115072,112272,-3018,0,129600,86400,526218,2301), -- Lord Ishka (60)
(25412,81920,113136,-3056,0,129600,86400,319791,2301); -- Necrosentinel Royal Guard (47)
