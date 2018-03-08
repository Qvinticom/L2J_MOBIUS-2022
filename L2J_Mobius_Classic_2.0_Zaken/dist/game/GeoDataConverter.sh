#! /bin/sh

java -Xmx512m -cp ../libs/*:GameServer.jar com.l2jmobius.tools.geodataconverter.GeoDataConverter > log/stdout.log 2>&1

