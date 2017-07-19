# Made by disKret, Ancient Legion Server (adapted for L2JLisvus by roko91)
import sys
from com.l2jmobius.gameserver.model.quest import State
from com.l2jmobius.gameserver.model.quest import QuestState
from com.l2jmobius.gameserver.model.quest.jython import QuestJython as JQuest

qn = "16_TheComingDarkness"

#NPC
HIERARCH = 8517
EVIL_ALTAR_1 = 8512
EVIL_ALTAR_2 = 8513
EVIL_ALTAR_3 = 8514
EVIL_ALTAR_4 = 8515
EVIL_ALTAR_5 = 8516

#ITEMS
CRYSTAL_OF_SEAL = 7167

class Quest (JQuest) :

 def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)

 def onEvent (self,event,st) :
   htmltext = event
   cond = st.getInt("cond")
   if event == "8517-1.htm" :
     return htmltext
   if event == "8517-2.htm" :
     st.giveItems(CRYSTAL_OF_SEAL,5)
     st.set("cond","1")
     st.setState(STARTED)
     st.playSound("ItemSound.quest_accept")
   if event == "8512-1.htm" :
     if cond == 1 :
       st.takeItems(CRYSTAL_OF_SEAL,1)
       st.set("cond","2")
       st.playSound("ItemSound.quest_middle")
   if event == "8513-1.htm" :
     if cond == 2 :
       st.takeItems(CRYSTAL_OF_SEAL,1)
       st.set("cond","3")
       st.playSound("ItemSound.quest_middle")
   if event == "8514-1.htm" :
     if cond == 3 :
       st.takeItems(CRYSTAL_OF_SEAL,1)
       st.set("cond","4")
       st.playSound("ItemSound.quest_middle")
   if event == "8515-1.htm" :
     if cond == 4 :
       st.takeItems(CRYSTAL_OF_SEAL,1)
       st.set("cond","5")
       st.playSound("ItemSound.quest_middle")
   if event == "8516-1.htm" :
     if cond == 5 :
       st.takeItems(CRYSTAL_OF_SEAL,1)
       st.set("cond","6")
       st.playSound("ItemSound.quest_middle")
   return htmltext

 def onTalk (self,npc,st):
   htmltext = "<html><body>You are either not carrying out your quest or don't meet the criteria.</body></html>"
   npcId = npc.getNpcId()
   cond = st.getInt("cond")
   id = st.getState()
   level = st.getPlayer().getLevel()
   if id == CREATED :
     st.set("cond","0")
   if npcId == HIERARCH and st.getInt("cond") == 0 :
     if level >= 62 :
       htmltext = "8517-0.htm"
     if id == COMPLETED :
       htmltext = "<html><body>This quest has already been completed.</body></html>"
     else:
       return htmltext
       st.exitQuest(1)
   if id == STARTED :    
       if npcId == EVIL_ALTAR_1 and cond == 1 :
         htmltext = "8512-0.htm"
       if npcId == EVIL_ALTAR_2 and cond == 2 :
         htmltext = "8513-0.htm"
       if npcId == EVIL_ALTAR_3 and cond == 3 :
         htmltext = "8514-0.htm"
       if npcId == EVIL_ALTAR_4 and cond== 4 :
         htmltext = "8515-0.htm"
       if npcId == EVIL_ALTAR_5 and cond == 5 :
         htmltext = "8516-0.htm"
       if npcId == HIERARCH and cond == 6 :
         st.addExpAndSp(221958,0)
         st.set("cond","0")
         st.setState(COMPLETED)
         st.playSound("ItemSound.quest_finish")
         htmltext = "8517-3.htm"
   return htmltext

QUEST       = Quest(16,qn,"The Coming Darkness")
CREATED     = State('Start', QUEST)
STARTED     = State('Started', QUEST)
COMPLETED   = State('Completed', QUEST)

QUEST.setInitialState(CREATED)
QUEST.addStartNpc(8517)
QUEST.addTalkId(8517)

for altars in range(8512,8517):
  QUEST.addTalkId(altars)