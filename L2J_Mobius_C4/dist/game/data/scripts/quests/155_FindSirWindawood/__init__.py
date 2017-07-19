# Made by Mr. Have fun! Version 0.2
import sys
from com.l2jmobius.gameserver.model.quest import State
from com.l2jmobius.gameserver.model.quest import QuestState
from com.l2jmobius.gameserver.model.quest.jython import QuestJython as JQuest

OFFICIAL_LETTER_ID = 1019
HASTE_POTION_ID = 734

class Quest (JQuest) :

 def __init__(self,id,name,descr):
     JQuest.__init__(self,id,name,descr)
     self.questItemIds = [OFFICIAL_LETTER_ID]

 def onEvent (self,event,st) :
    htmltext = event
    if event == "1" :
      st.set("id","0")
      st.giveItems(OFFICIAL_LETTER_ID,1)
      htmltext = "7042-04.htm"
      st.set("cond","1")
      st.setState(STARTED)
      st.playSound("ItemSound.quest_accept")
    return htmltext

 def onTalk (Self,npc,st):
   npcId = npc.getNpcId()
   htmltext = "<html><body>I have nothing to say to you.</body></html>"
   id = st.getState()
   if id == CREATED :
     st.setState(STARTING)
     st.set("cond","0")
     st.set("onlyone","0")
     st.set("id","0")
   if npcId == 7042 and st.getInt("cond")==0 and st.getInt("onlyone")==0 :
      if st.getInt("cond")<15 :
        if st.getPlayer().getLevel() >= 3 :
          htmltext = "7042-03.htm"
          return htmltext
        else:
          htmltext = "7042-02.htm"
          st.exitQuest(1)
      else:
        htmltext = "7042-02.htm"
        st.exitQuest(1)
   elif npcId == 7042 and st.getInt("cond")==0 and st.getInt("onlyone")==1 :
      htmltext = "<html><body>This quest has already been completed.</body></html>"
   elif npcId == 7042 and st.getInt("cond")==1 and st.getQuestItemsCount(OFFICIAL_LETTER_ID)==1 :
      htmltext = "7042-05.htm"
   elif npcId == 7311 and st.getInt("cond")==1 and st.getQuestItemsCount(OFFICIAL_LETTER_ID)==1 and st.getInt("onlyone")==0 :
      if st.getInt("id") != 155 :
        st.set("id","155")
        st.takeItems(OFFICIAL_LETTER_ID,st.getQuestItemsCount(OFFICIAL_LETTER_ID))
        st.giveItems(HASTE_POTION_ID,1)
        st.set("cond","0")
        st.setState(COMPLETED)
        st.playSound("ItemSound.quest_finish")
        st.set("onlyone","1")
        htmltext = "7311-01.htm"
   return htmltext

QUEST       = Quest(155,"155_FindSirWindawood","Find Sir Windawood")
CREATED     = State('Start', QUEST)
STARTING     = State('Starting', QUEST)
STARTED     = State('Started', QUEST)
COMPLETED   = State('Completed', QUEST)


QUEST.setInitialState(CREATED)
QUEST.addStartNpc(7042)

QUEST.addTalkId(7042)
QUEST.addTalkId(7311)