# Made by Mr. - Version 0.3 by DrLecter
import sys
from com.l2jmobius.gameserver.model.quest import State
from com.l2jmobius.gameserver.model.quest import QuestState
from com.l2jmobius.gameserver.model.quest.jython import QuestJython as JQuest

FUNGUS_SAC = 1118
ADENA = 57

class Quest (JQuest) :

 def __init__(self,id,name,descr):
     JQuest.__init__(self,id,name,descr)
     self.questItemIds = [FUNGUS_SAC]

 def onEvent (self,event,st) :
    htmltext = event
    if event == "7150-05.htm" :
      st.set("cond","1")
      st.setState(STARTED)
      st.playSound("ItemSound.quest_accept")
    return htmltext

 def onTalk (Self,npc,st):
   htmltext = "<html><body>I have nothing to say to you.</body></html>"
   id = st.getState()
   cond=st.getInt("cond")
   if cond == 0 :
     if st.getPlayer().getLevel() >= 8 :
       htmltext = "7150-03.htm"
     else:
       htmltext = "7150-02.htm"
       st.exitQuest(1)
   else :
     if st.getQuestItemsCount(FUNGUS_SAC)<10 :
       htmltext = "7150-06.htm"
     else :
       st.takeItems(FUNGUS_SAC,-1)
       st.playSound("ItemSound.quest_finish")
       st.giveItems(ADENA,3500)
       htmltext = "7150-07.htm"
       st.exitQuest(1)
   return htmltext

 def onKill(self,npc,player,isPet) :
   st = player.getQuestState("313_CollectSpores")
   if st :
     if st.getState() != STARTED : return
     if st.getQuestItemsCount(FUNGUS_SAC)<10 and st.getRandom(100) < 50 :
       st.giveItems(FUNGUS_SAC,1)
       if st.getQuestItemsCount(FUNGUS_SAC) == 10 :
         st.playSound("ItemSound.quest_middle")
       else:
         st.playSound("ItemSound.quest_itemget")
   return

QUEST       = Quest(313,"313_CollectSpores","Collect Spores")
CREATED     = State('Start', QUEST)
STARTING    = State('Starting', QUEST)
STARTED     = State('Started', QUEST)
COMPLETED   = State('Completed', QUEST)

QUEST.setInitialState(CREATED)
QUEST.addStartNpc(7150)

QUEST.addTalkId(7150)

QUEST.addKillId(509)