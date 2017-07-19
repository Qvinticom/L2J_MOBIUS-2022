# Made by disKret (adapted for L2JLisvus by roko91)
import sys
from com.l2jmobius.gameserver.model.quest import State
from com.l2jmobius.gameserver.model.quest import QuestState
from com.l2jmobius.gameserver.model.quest.jython import QuestJython as JQuest

qn = "14_WhereaboutsOfTheArchaeologist"

#NPC
LIESEL = 8263
GHOST_OF_ADVENTURER = 8538

#QUEST ITEM
LETTER = 7253

class Quest (JQuest) :

 def __init__(self,id,name,descr):
     JQuest.__init__(self,id,name,descr)
     self.questItemIds = [LETTER]

 def onEvent (self,event,st) :
   htmltext = event
   cond = st.getInt("cond")
   if event == "8263-2.htm" and cond == 0 :
     st.set("cond","1")
     st.setState(STARTED)
     st.giveItems(LETTER,1)
     st.playSound("ItemSound.quest_accept")
   elif event == "8538-1.htm" :
     if cond == 1 and st.getQuestItemsCount(LETTER) == 1 :
       st.takeItems(LETTER,1)
       st.giveItems(57,113228)
       st.setState(COMPLETED)
       st.set("cond","0")
       st.playSound("ItemSound.quest_finish")
     else :
       htmltext = "You don't have required items"
   return htmltext

 def onTalk (self,npc,st):
   htmltext = "<html><body>You are either not carrying out your quest or don't meet the criteria.</body></html>"
   npcId = npc.getNpcId()
   id = st.getState()
   level = st.getPlayer().getLevel()
   cond = st.getInt("cond")
   if npcId == LIESEL and cond == 0 :
     if id == COMPLETED :
       htmltext = "<html><body>This quest has already been completed.</body></html>"
     elif level < 74 : 
       htmltext = "8263-1.htm"
       st.exitQuest(1)
     elif level >= 74 : 
       htmltext = "8263-0.htm"
   elif npcId == LIESEL and cond == 1 :
     htmltext = "8263-2.htm"
   elif npcId == GHOST_OF_ADVENTURER and cond == 1 and id == STARTED:
     htmltext = "8538-0.htm"
   return htmltext

QUEST       = Quest(14,qn,"Whereabouts Of The Archaeologist")
CREATED     = State('Start', QUEST)
STARTED     = State('Started', QUEST)
COMPLETED   = State('Completed', QUEST)

QUEST.setInitialState(CREATED)
QUEST.addStartNpc(LIESEL)
QUEST.addTalkId(LIESEL)
QUEST.addTalkId(GHOST_OF_ADVENTURER)