@echo off
title L2J Mobius - Login Server Console

:start
echo Starting Login Server.
echo.

java -version:1.8 -server -Dfile.encoding=UTF-8 -Djava.util.logging.config.file=config/others/log.cfg -Xms128m -Xmx256m -cp ./lib/*;LoginServer.jar com.l2jmobius.loginserver.L2LoginServer

if ERRORLEVEL 2 goto restart
if ERRORLEVEL 1 goto error
goto end
:restart
echo.
echo Admin Restarted ...
ping -n 5 localhost > nul
echo.
goto start
:error
echo.
echo LoginServer terminated abnormaly
ping -n 5 localhost > nul
echo.
goto start
:end
echo.
echo LoginServer terminated
echo.
:question
set choix=q
set /p choix=Restart(r) or Quit(q)
if /i %choix%==r goto start
if /i %choix%==q goto exit
:exit
exit
pause
