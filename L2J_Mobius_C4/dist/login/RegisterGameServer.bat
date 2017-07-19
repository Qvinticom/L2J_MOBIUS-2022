@echo off
title Register Game Server
color 17
java -version:1.8 -Djava.util.logging.config.file=console.cfg -cp ./../libs/*;LoginServer.jar com.l2jmobius.tools.gsregistering.GameServerRegister -c
pause