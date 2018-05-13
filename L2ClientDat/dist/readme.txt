L2ClientDat decoder
Originally known as Godworld L2ClientDataDecoder v0.4b

For ru we choose chronicles ru_helios

If the dat files are original then the crypt is v413_original
if not then v413_encdec

After full unpacking, you need to correct Korean errors by replacing all files (or by searching)
[]] -> [None]
[skill_end] -> [SpAtk01]
they are also visible in the log when unpacking.


It is not recommended to use the text fields [and].

When DAT_REPLACEMENT_NAMES=true [config_debug.ini]
You can not change the dates one by one if they contain MAP_INT (only full repacking).

If the file is not going to be collected or disassembled, we turn on the config DAT_DEBUG_MSG and DAT_DEBUG_POS [config_debug.ini].
