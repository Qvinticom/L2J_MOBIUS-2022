# Made by Mr. Have fun!
# Version 0.3 by H1GHL4ND3R
import sys
from com.l2jmobius.gameserver.model.quest import State
from com.l2jmobius.gameserver.model.quest import QuestState
from com.l2jmobius.gameserver.model.quest.jython import QuestJython as JQuest

ANDELLRIAS_LETTER = 1036
MOTHERTREE_FRUIT = 1037
ADENA = 57

class Quest (JQuest) :

 def __init__(self,id,name,descr):
     JQuest.__init__(self,id,name,descr)
     self.questItemIds = [MOTHERTREE_FRUIT, ANDELLRIAS_LETTER]

 def onEvent (self,event,st) :
    htmltext = event
    if event == "7362-04.htm" :
      st.set("cond","1")
      st.setState(STARTED)
      st.giveItems(ANDELLRIAS_LETTER,1)
      st.playSound("ItemSound.quest_accept")
    return htmltext

 def onTalk (Self,npc,st):
   npcId = npc.getNpcId()
   htmltext = "<html><body>I have nothing to say to you.</body></html>"
   id = st.getState()
   if id == CREATED :
     if st.getPlayer().getRace().ordinal() != 1 :
       htmltext = "7362-00.htm"
     elif st.getPlayer().getLevel() >= 3 :
       htmltext = "7362-03.htm"
       st.set("cond","0")
     else:
       htmltext = "7362-02.htm"
       st.exitQuest(1)
   elif id == COMPLETED :
     htmltext = "<html><body>This quest has already been completed.</body></html>"
   else :
     try :
       cond = st.getInt("cond")
     except :
       cond = None
     if cond == 1 :
       if npcId == 7362 :
         htmltext = "7362-05.htm"
       elif npcId == 7371 and st.getQuestItemsCount(ANDELLRIAS_LETTER) :
         htmltext = "7371-01.htm"
         st.takeItems(ANDELLRIAS_LETTER,1)
         st.giveItems(MOTHERTREE_FRUIT,1)
         st.set("cond", "2")
     elif cond == 2 :
       if npcId == 7362 and st.getQuestItemsCount(MOTHERTREE_FRUIT) :
         htmltext = "7362-06.htm"
         st.giveItems(ADENA,500)
         st.addExpAndSp(1000,0)
         st.takeItems(MOTHERTREE_FRUIT,1)
         st.unset("cond")
         st.setState(COMPLETED)
         st.playSound("ItemSound.quest_finish")
       elif npcId == 7371 and st.getQuestItemsCount(MOTHERTREE_FRUIT) :
         htmltext = "7371-02.htm"
   return htmltext

QUEST       = Quest(161,"161_FruitsOfMothertree","Fruits Of Mothertree")
CREATED     = State('Start', QUEST)
STARTING    = State('Starting', QUEST)
STARTED     = State('Started', QUEST)
COMPLETED   = State('Completed', QUEST)

QUEST.setInitialState(CREATED)
QUEST.addStartNpc(7362)

QUEST.addTalkId(7362)
QUEST.addTalkId(7371)