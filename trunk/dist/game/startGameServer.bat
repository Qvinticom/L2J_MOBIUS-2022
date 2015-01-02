@echo off
title Mobius - Game Server Console

:start
echo Starting Game Server.
echo.

REM java -Djava.util.logging.manager=com.l2jserver.util.L2LogManager -Dpython.cachedir=../cachedir -Xms1024m -Xmx1536m -jar l2jserver.jar
java -version:1.8 -server -Djava.util.logging.manager=com.l2jserver.util.L2LogManager -Dpython.cachedir=../cachedir -XX:+AggressiveOpts -Xnoclassgc -XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled -XX:+UseParNewGC -XX:SurvivorRatio=8 -Xmx4g -Xms2g -Xmn1g -jar l2jserver.jar

REM NOTE: If you have a powerful machine, you could modify/add some extra parameters for performance, like:
REM -Xms1536m
REM -Xmx3072m
REM -XX:+AggressiveOpts
REM Use this parameters carefully, some of them could cause abnormal behavior, deadlocks, etc.
REM More info here: http://www.oracle.com/technetwork/java/javase/tech/vmoptions-jsp-140102.html

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