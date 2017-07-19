#
# Created by DraX on 2005.08.08
#
# Updated by ElgarL on 28.09.2005
#

import sys

from com.l2jmobius.gameserver.model.quest        import State
from com.l2jmobius.gameserver.model.quest        import QuestState
from com.l2jmobius.gameserver.model.quest.jython import QuestJython as JQuest

GRAND_MASTER_BITZ = 7026

class Quest (JQuest) :

 def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)

 def onEvent (self,event,st):
   if event in ["7026-01.htm","7026-02.htm","7026-03.htm","7026-04.htm","7026-05.htm","7026-06.htm","7026-07.htm"] :
     htmltext = event
   else :
     htmltext = "No Quest"
     st.exitQuest(1)
   return htmltext

 def onTalk (Self,npc,st):
   npcId = npc.getNpcId()
   Race  = st.getPlayer().getRace()
   pcId  = st.getPlayer().getClassId().getId()
   # Human fighters get accepted
   if npcId == GRAND_MASTER_BITZ and Race in [Race.Human] and pcId in range(0x0a)+range(88,94) :
     #fighter
     if pcId == 0x00:
       htmltext = "7026-01.htm"
     #warrior, knight, rogue
     elif pcId in [1, 4, 7] :
       htmltext = "7026-08.htm"
     #warlord, paladin, treasureHunter
     elif pcId in [3, 5, 8, 93, 91, 89 ] :
       htmltext = "7026-09.htm"
     #gladiator, darkAvenger, hawkeye
     elif pcId in [2, 6, 9, 92, 90, 88 ] :
       htmltext = "7026-09.htm"
     st.setState(STARTED)
     return htmltext
   # All other Races and classes must be out
   else :
     st.exitQuest(1)
     return "7026-10.htm"

QUEST     = Quest(7026,"7026_bitz_occupation_change","village_master")
CREATED   = State('Start',     QUEST)
STARTED   = State('Started',   QUEST)
COMPLETED = State('Completed', QUEST)

QUEST.setInitialState(CREATED)
QUEST.addStartNpc(GRAND_MASTER_BITZ)
QUEST.addTalkId(GRAND_MASTER_BITZ)