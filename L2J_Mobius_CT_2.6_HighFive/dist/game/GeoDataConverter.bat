@echo off
title L2D geodata converter

java -Xmx512m -cp ./../libs/*;GameServer.jar com.l2jmobius.tools.geodataconverter.GeoDataConverter

pause
