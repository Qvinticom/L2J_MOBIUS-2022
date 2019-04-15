@echo off
title L2D geodata converter

java -Xmx512m -cp ./../libs/* org.l2jmobius.tools.geodataconverter.GeoDataConverter

pause
