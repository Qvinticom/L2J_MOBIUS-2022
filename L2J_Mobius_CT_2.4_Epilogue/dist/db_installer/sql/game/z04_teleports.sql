-- taken from l2j epilogue
DELETE FROM teleport WHERE id in(
69,1060,1076,9041,9042,9043,9044,9045,9941,9942,9943,9944,9945);
INSERT INTO `teleport` VALUES
('Giran -> Dragon Valley',69,73024,118485,-3688,1800,0,57),
('Giran -> Antharas Lair',1060,131557,114509,-3712,7000,0,57),
('Oren -> Skyshadow Meadow',1076,89914,46276,-3616,780,0,57),
('Antharas Lair 1 - 1000 adena',9041,147071,120156,-4520,1000,1,57),
('Antharas Lair 2 - 1000 adena',9042,151689,112615,-5520,1000,1,57),
('Antharas Lair,Magic Force Field Bridge - 1000 adena',9043,146494,109285,-3448,1000,1,57),
('Antharas Lair,Heart of Warding - 1000 adena',9044,154396,121235,-3808,1000,1,57),
('Dragon Valley,Traders Post - 1000 adena',9045,122824,110836,-3720,1000,1,57),
('Antharas Lair 1 - 1 Noble Gate Pass',9941,147071,120156,-4520,1,1,13722),
('Antharas Lair 2 - 1 Noble Gate Pass',9942,151689,112615,-5520,1,1,13722),
('Antharas Lair, Magic Force Field Bridge - 1 Noble Gate Pass',9943,146494,109285,-3448,1,1,13722),
('Antharas Lair,Heart of Warding - 1 Noble Gate Pass',9944,154396,121235,-3808,1,1,13722),
('Dragon Valley,Traders Post - 1 Noble Gate Pass',9945,122824,110836,-3720,1,1,13722);
