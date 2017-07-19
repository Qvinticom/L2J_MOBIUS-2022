#
# Created by DraX on 2005.07.27. updated by mr.#

import sys

from com.l2jmobius.gameserver.model.actor.instance import L2PcInstance
from com.l2jmobius.gameserver.model.quest        import State
from com.l2jmobius.gameserver.model.quest        import QuestState
from com.l2jmobius.gameserver.model.quest.jython import QuestJython as JQuest

RACE_MANAGER = 7995

TELEPORTERS = {
    7320:1,    # RICHLIN
    7256:2,    # BELLA
    7059:3,    # TRISHA
    7080:4,    # CLARISSA
    7899:5,    # FLAUEN
    7177:6,    # VALENTINA
    7848:7,    # ELISA
    7233:8,    # ESMERALDA
    8320:9,    # ILYANA
    8275:10,   # TATIANA
    7727:11,    # VERONA
    7836:12,   # MINERVA
    8210:13    # RACE TRACK GK
}

RETURN_LOCS = [[-80826,149775,-3043],[-12672,122776,-3116],[15670,142983,-2705],[83400,147943,-3404], \
              [111409,219364,-3545],[82956,53162,-1495],[146331,25762,-2018],[116819,76994,-2714], \
              [43835,-47749,-792],[147930,-55281,-2728],[85335,16177,-3694],[105857,109763,-3202], \
              [12882,181053,-3560]]

class Quest (JQuest) :

 def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)

 def onTalk (self,npc,st):

   npcId = npc.getNpcId()

   ###################
   # Start Locations #
   ###################
   if TELEPORTERS.has_key(npcId) :
       st.getPlayer().teleToLocation(12661,181687,-3560)
       st.setState(STARTED)
       st.set("id",str(TELEPORTERS[npcId]))
   ############################
   # Monster Derby Race Track #
   ############################
   elif st.getState() == STARTED and npcId == RACE_MANAGER:
       # back to start location
       return_id = st.getInt("id") - 1
       st.getPlayer().teleToLocation(RETURN_LOCS[return_id][0],RETURN_LOCS[return_id][1],RETURN_LOCS[return_id][2])
       st.exitQuest(1)
       return

QUEST       = Quest(1101,"1101_teleport_to_race_track","Teleports")
CREATED     = State('Start',     QUEST)
STARTED     = State('Started',   QUEST)

QUEST.setInitialState(CREATED)

for npcId in TELEPORTERS.keys() :
    QUEST.addStartNpc(npcId)
    QUEST.addTalkId(npcId)

QUEST.addTalkId(RACE_MANAGER)