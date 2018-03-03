#!/bin/sh
java -Dfile.encoding=UTF-8 -Djava.util.logging.config.file=config/others/log.cfg -cp lib/*:lib/LoginServer.jar com.l2jmobius.tools.gsregistering.GameServerRegister
