# Made by disKret
import sys
from com.l2jmobius.gameserver.model.quest import State
from com.l2jmobius.gameserver.model.quest import QuestState
from com.l2jmobius.gameserver.model.quest.jython import QuestJython as JQuest

#NPC
VLADIMIR = 8302
TUNATUN = 8537

#ITEMS
BEAST_MEAT = 7547

class Quest (JQuest) :

 def __init__(self,id,name,descr):
     JQuest.__init__(self,id,name,descr)
     self.questItemIds = [BEAST_MEAT]

 def onEvent (self,event,st) :
   htmltext = event
   if event == "8302-1.htm" :
     st.giveItems(BEAST_MEAT,1)
     st.set("cond","1")
     st.setState(STARTED)
     st.playSound("ItemSound.quest_accept")
   if event == "8537-1.htm" :
     st.takeItems(BEAST_MEAT,1)
     st.giveItems(57,30000)
     st.unset("cond")
     st.setState(COMPLETED)
     st.playSound("ItemSound.quest_finish")
   return htmltext

 def onTalk (Self,npc,st):
   htmltext = "<html><body>I have nothing to say to you.</body></html>"
   npcId = npc.getNpcId()
   id = st.getState()
   cond = st.getInt("cond")
   if npcId == VLADIMIR :
     if cond == 0 :
       if id == COMPLETED :
         htmltext = "<html><body>This quest has already been completed.</body></html>"
       elif st.getPlayer().getLevel() >= 63 :
         htmltext = "8302-0.htm"
       else:
         htmltext = "<html><body>Quest for characters of level 63 or above.</body></html>"
         st.exitQuest(1)
     else :
       htmltext = "8302-2.htm"
   else :
       htmltext = "8537-0.htm"
   return htmltext

QUEST       = Quest(19,"19_GoToThePastureland","Go To The Pastureland")
CREATED     = State('Start', QUEST)
STARTED     = State('Started', QUEST)
COMPLETED   = State('Completed', QUEST)

QUEST.setInitialState(CREATED)
QUEST.addStartNpc(VLADIMIR)

QUEST.addTalkId(VLADIMIR)
QUEST.addTalkId(TUNATUN)