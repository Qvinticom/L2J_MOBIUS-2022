# Originally created by Ham Wong on 2007.03.07 #
import sys
from com.l2jmobius.gameserver.model.quest          import State
from com.l2jmobius.gameserver.model.quest          import QuestState
from com.l2jmobius.gameserver.model.quest.jython   import QuestJython as JQuest

qn = "1103_OracleTeleport"

TOWN_DAWN = [8078,8079,8080,8081,8083,8084,8082,8692,8694,8168]
TOWN_DUSK = [8085,8086,8087,8088,8090,8091,8089,8693,8695,8169]
TEMPLE_PRIEST = [8127,8128,8129,8130,8131,8137,8138,8139,8140,8141]

TELEPORTERS = {
# Dawn
8078:1,
8079:2,
8080:3,
8081:4,
8083:5,
8084:6,
8082:7,
8692:8,
8694:9,
8168:10,
# Dusk
8085:11,
8086:12,
8087:13,
8088:14,
8090:15,
8091:16,
8089:17,
8693:18,
8695:19,
8169:20
}

RETURN_LOCS = [[-80555,150337,-3040],[-13953,121404,-2984],[16354,142820,-2696],[83369,149253,-3400], \
              [83106,53965,-1488],[146983,26595,-2200],[111386,220858,-3544],[148256,-55454,-2779], \
              [45664,-50318,-800],[115136,74717,-2608],[-82368,151568,-3120],[-14748,123995,-3112], \
              [18482,144576,-3056],[81623,148556,-3464],[82819,54607,-1520],[147570,28877,-2264], \
              [112486,220123,-3592],[149888,-56574,-2979],[44528,-48370,-800],[116642,77510,-2688]]
class Quest (JQuest) :

 def __init__(self, id, name, descr): JQuest.__init__(self, id, name, descr)

 def onTalk (Self, npc, st):
    npcId = npc.getNpcId()
    ##################
    # Dawn Locations #
    ##################
    if npcId in TOWN_DAWN: 
       st.setState(STARTED)
       st.set("id",str(TELEPORTERS[npcId]))
       st.getPlayer().teleToLocation(-80157,111344,-4901)
    ##################
    # Dusk Locations #
    ##################
    elif npcId in TOWN_DUSK: 
       st.setState(STARTED)
       st.set("id",str(TELEPORTERS[npcId]))
       st.getPlayer().teleToLocation(-81261,86531,-5157)
    #######################
    # Oracle of Dusk/Dawn #
    #######################
    elif npcId in TEMPLE_PRIEST and st.getState() == STARTED :
       return_id = st.getInt("id") - 1
       st.getPlayer().teleToLocation(RETURN_LOCS[return_id][0],RETURN_LOCS[return_id][1],RETURN_LOCS[return_id][2])
       st.exitQuest(1)
    return
   
        
QUEST      = Quest(1103, qn, "Teleports")
CREATED    = State('Start', QUEST)
STARTED    = State('Started', QUEST)

QUEST.setInitialState(CREATED)

for i in TELEPORTERS :
    QUEST.addStartNpc(i)
    QUEST.addTalkId(i)

for j in TEMPLE_PRIEST :
    QUEST.addTalkId(j)