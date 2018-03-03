#!/bin/bash

DBHOST=localhost
USER=root
PASS=
DBNAME=l2jmobiusc6

err=1
until [ $err == 0 ];
do
        java -Dfile.encoding=UTF-8 -Djava.util.logging.config.file=config/others/log.cfg -Xms2048m -Xmx4096m -cp ./libs/*:GameServer.jar com.l2jmobius.gameserver.GameServer > log/stdout.log 2>&1
        err=$?
        sleep 10
done
