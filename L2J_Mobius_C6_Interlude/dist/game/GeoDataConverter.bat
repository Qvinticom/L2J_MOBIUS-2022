@echo off
title L2D geodata converter

java -version:1.8 -Xmx512m -cp ./../libs/* com.l2jmobius.tools.geodataconverter.GeoDataConverter

pause
