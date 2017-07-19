# Made by Mr. - Version 0.3 by DrLecter
import sys
from com.l2jmobius.gameserver.model.quest import State
from com.l2jmobius.gameserver.model.quest import QuestState
from com.l2jmobius.gameserver.model.quest.jython import QuestJython as JQuest

WIND_SHARD = 1078
ADENA = 57

class Quest (JQuest) :

 def __init__(self,id,name,descr):
     JQuest.__init__(self,id,name,descr)
     self.questItemIds = [WIND_SHARD]

 def onEvent (self,event,st) :
    htmltext = event
    if event == "7361-04.htm" :
      st.set("cond","1")
      st.setState(STARTED)
      st.playSound("ItemSound.quest_accept")
    elif event == "7361-08.htm" :
      st.playSound("ItemSound.quest_finish")
      st.exitQuest(1)
    return htmltext

 def onTalk (Self,npc,st):
   htmltext = "<html><body>I have nothing to say to you.</body></html>"
   cond=st.getInt("cond")
   if cond == 0 :
     if st.getPlayer().getLevel() >= 18 :
       htmltext = "7361-03.htm"
     else:
       htmltext = "7361-02.htm"
       st.exitQuest(1)
   else :
     count = st.getQuestItemsCount(WIND_SHARD)
     if count :
       st.giveItems(ADENA,30*count)
       st.takeItems(WIND_SHARD,-1)
       htmltext = "7361-07.htm"
     else :
       htmltext = "7361-05.htm"
   return htmltext

 def onKill (self,npc,player,isPet):
   st = player.getQuestState("317_CatchTheWind")
   if st :
     if st.getState() != STARTED : return
     st.giveItems(WIND_SHARD,1)
     st.playSound("ItemSound.quest_itemget")
   return

QUEST       = Quest(317,"317_CatchTheWind","Catch The Wind")
CREATED     = State('Start', QUEST)
STARTING     = State('Starting', QUEST)
STARTED     = State('Started', QUEST)
COMPLETED   = State('Completed', QUEST)

QUEST.setInitialState(CREATED)
QUEST.addStartNpc(7361)

QUEST.addTalkId(7361)

QUEST.addKillId(36)
QUEST.addKillId(44)