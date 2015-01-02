@echo off
title Mobius - Login Server Console

:start
echo Starting Login Server.
echo.

REM java -Xms128m -Xmx256m -jar l2jlogin.jar
java -version:1.8 -server -Xms128m -Xmx256m -jar l2jlogin.jar

if ERRORLEVEL 2 goto restart
if ERRORLEVEL 1 goto error
goto end

:restart
echo.
echo Admin Restarted Login Server.
echo.
goto start

:error
echo.
echo Login Server terminated abnormally!
echo.

:end
echo.
echo Login Server Terminated.
echo.
pause