#!/bin/bash

err=1
until [ $err == 0 ]; 
do
	java -Dfile.encoding=UTF-8 -Djava.util.logging.config.file=config/others/log.cfg -Xms128m -Xmx128m -cp lib/*:LoginServer.jar com.l2jmobius.loginserver.L2LoginServer > log/stdout.log 2>&1
	err=$?
#	/etc/init.d/mysql restart
	sleep 10;
done
