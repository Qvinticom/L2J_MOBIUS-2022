@echo off
title L2J Mobius - Game Server Console

:start
echo Starting Game Server.
echo.

java -version:1.8 -server -Dfile.encoding=UTF-8 -Djava.util.logging.manager=com.l2jmobius.log.L2LogManager -XX:+AggressiveOpts -Xnoclassgc -XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled -XX:+UseParNewGC -XX:SurvivorRatio=8 -Xmx4g -Xms2g -Xmn1g -jar ../libs/GameServer.jar

if ERRORLEVEL 2 goto restart
if ERRORLEVEL 1 goto error
goto end

:restart
echo.
echo Admin Restarted Game Server.
echo.
goto start

:error
echo.
echo Game Server Terminated Abnormally!
echo.

:end
echo.
echo Game Server Terminated.
echo.
pause