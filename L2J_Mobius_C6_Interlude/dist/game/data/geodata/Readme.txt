##############################################
GEODATA COMPENDIUM
##############################################

Comprehensive guide for geodata.

How to configure it
	a - Prerequisites
	b - Make it work

##############################################
How to configure it
##############################################

----------------------------------------------
a - Prerequisites
----------------------------------------------

* A 64bits Windows/Java JDK is a must-have to run server with geodata. Linux servers don't have the issue.
* The server can start (hardly) with -Xmx3000m. -Xmx4g is recommended.

----------------------------------------------
b - Make it work
----------------------------------------------

To make geodata working:
* unpack your geodata files into "/data/geodata" folder
* open "/config/main/GeoEngine.ini" with your favorite text editor and then edit following config:
  - CoordSynchronize = 2
* If you do not use any geodata files, the server will automatically change this setting to -1.
