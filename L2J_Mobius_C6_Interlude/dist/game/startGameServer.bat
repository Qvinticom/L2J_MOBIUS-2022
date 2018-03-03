@echo off
title L2J Mobius - Game Server Console

:start
echo Starting Game Server.
echo.

java -version:1.8 -server -Dfile.encoding=UTF-8 -Djava.util.logging.config.file=config/others/log.cfg -XX:+AggressiveOpts -Xnoclassgc -XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled -XX:+UseParNewGC -XX:SurvivorRatio=8 -Xmx4g -Xms2g -Xmn1g -cp ./libs/*;Gameserver.jar com.l2jmobius.gameserver.GameServer

if ERRORLEVEL 7 goto telldown
if ERRORLEVEL 6 goto tellrestart
if ERRORLEVEL 5 goto taskrestart
if ERRORLEVEL 4 goto taskdown
REM 3 - abort
if ERRORLEVEL 2 goto restart
if ERRORLEVEL 1 goto error
goto end
:tellrestart
echo.
echo Telnet server Restart ...
echo.
goto start
:taskrestart
echo.
echo Auto Task Restart ...
echo.
goto start
:restart
echo.
echo Admin Restart ...
echo.
goto start
:taskdown
echo .
echo Server terminated (Auto task)
echo .
:telldown
echo .
echo Server terminated (Telnet)
echo .
:error
echo.
echo Server terminated abnormally
echo.
:end
echo.
echo server terminated
echo.
:question
set choix=q
set /p choix=Restart(r) or Quit(q)
if /i %choix%==r goto start
if /i %choix%==q goto exit
:exit
exit
pause
