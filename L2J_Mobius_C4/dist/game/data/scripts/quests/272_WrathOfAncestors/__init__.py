# Made by Mr. - Version 0.3 by DrLecter
import sys
from com.l2jmobius.gameserver.model.quest import State
from com.l2jmobius.gameserver.model.quest import QuestState
from com.l2jmobius.gameserver.model.quest.jython import QuestJython as JQuest

GRAVE_ROBBERS_HEAD = 1474
ADENA = 57

class Quest (JQuest) :

 def __init__(self,id,name,descr):
     JQuest.__init__(self,id,name,descr)
     self.questItemIds = [GRAVE_ROBBERS_HEAD]

 def onEvent (self,event,st) :
    htmltext = event
    if event == "7572-03.htm" :
      st.set("cond","1")
      st.setState(STARTED)
      st.playSound("ItemSound.quest_accept")
    return htmltext

 def onTalk (Self,npc,st):
   htmltext = "<html><body>I have nothing to say to you.</body></html>"
   id = st.getState()
   if id == CREATED :
     st.set("cond","0")
   if st.getInt("cond") == 0 :
     if st.getPlayer().getRace().ordinal() != 3 :
        htmltext = "7572-00.htm"
        st.exitQuest(1)
     else :
        if st.getPlayer().getLevel() < 5 :
          htmltext = "7572-01.htm"
          st.exitQuest(1)
        else:
          htmltext = "7572-02.htm"
   else :
     if st.getQuestItemsCount(GRAVE_ROBBERS_HEAD) < 50 :
        htmltext = "7572-04.htm"
     else:
        htmltext = "7572-05.htm"
        st.exitQuest(1)
        st.playSound("ItemSound.quest_finish")
        st.giveItems(ADENA,1500)
        st.takeItems(GRAVE_ROBBERS_HEAD,-1)
   return htmltext

 def onKill (self,npc,player,isPet):
   st = player.getQuestState("272_WrathOfAncestors")
   if st :
     if st.getState() != STARTED : return
     count = st.getQuestItemsCount(GRAVE_ROBBERS_HEAD)  
     if count < 50 :
        st.giveItems(GRAVE_ROBBERS_HEAD,1)
        if count < 49 :
           st.playSound("ItemSound.quest_itemget")
        else:
           st.playSound("ItemSound.quest_middle")
           st.set("cond","2")
   return

QUEST       = Quest(272,"272_WrathOfAncestors","Wrath Of Ancestors")
CREATED     = State('Start', QUEST)
STARTING    = State('Starting', QUEST)
STARTED     = State('Started', QUEST)
COMPLETED   = State('Completed', QUEST)

QUEST.setInitialState(CREATED)
QUEST.addStartNpc(7572)

QUEST.addTalkId(7572)

QUEST.addKillId(319)
QUEST.addKillId(320)