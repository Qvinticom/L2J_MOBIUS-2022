CREATE TABLE IF NOT EXISTS `gameservers` (
  `server_id` int(11) NOT NULL default '0',
  `hexid` varchar(50) NOT NULL default '',
  `host` varchar(50) NOT NULL default '',
  PRIMARY KEY  (`server_id`)
);

-- ----------------------------
-- Records of gameservers
-- ----------------------------
INSERT INTO `gameservers` VALUES ('1', 'f23de3c2a05a974a1b5369a8fe2eb16', '*');