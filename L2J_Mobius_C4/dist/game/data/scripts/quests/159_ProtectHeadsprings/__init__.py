# Made by Mr. Have fun! Version 0.2
# version 0.3 - fixed on 2005.11.08
# version 0.4 by DrLecter

import sys
from com.l2jmobius.gameserver.model.quest        import State
from com.l2jmobius.gameserver.model.quest        import QuestState
from com.l2jmobius.gameserver.model.quest.jython import QuestJython as JQuest

ADENA           = 57
PLAGUE_DUST     = 1035
HYACINTH_CHARM1 = 1071
HYACINTH_CHARM2 = 1072

class Quest (JQuest) :

 def __init__(self,id,name,descr):
     JQuest.__init__(self,id,name,descr)
     self.questItemIds = [PLAGUE_DUST, HYACINTH_CHARM1, HYACINTH_CHARM2]

 def onEvent (self,event,st) :
    htmltext = event
    if event == "1" :
        st.set("cond","1")
        st.setState(STARTED)
        st.playSound("ItemSound.quest_accept")
        if st.getQuestItemsCount(HYACINTH_CHARM1) == 0 :
           st.giveItems(HYACINTH_CHARM1,1)
        htmltext = "7154-04.htm"
    return htmltext

 def onTalk (Self,npc,st):
   npcId = npc.getNpcId()
   htmltext = "<html><body>I have nothing to say to you.</body></html>"
   id = st.getState()
   cond = st.getInt("cond")
   count = st.getQuestItemsCount(PLAGUE_DUST)
   if id == COMPLETED :
      htmltext = "<html><body>This quest has already been completed.</body></html>"
   elif cond == 0 :
      if st.getPlayer().getRace().ordinal() != 1 :
         htmltext = "7154-00.htm"
         st.exitQuest(1)
      elif st.getPlayer().getLevel() >= 12 :
         htmltext = "7154-03.htm"
      else:
         st.exitQuest(1)
   elif cond == 1 :
      htmltext = "7154-05.htm"
   elif cond == 2 and count :
      st.takeItems(PLAGUE_DUST,-1)
      st.takeItems(HYACINTH_CHARM1,-1)
      if st.getQuestItemsCount(HYACINTH_CHARM2) == 0 :
         st.giveItems(HYACINTH_CHARM2,1)
      st.set("cond","3")
      htmltext = "7154-06.htm"
   elif cond == 3 :
      htmltext = "7154-07.htm"
   elif cond == 4 and count >= 5 :
      st.takeItems(PLAGUE_DUST,-1)
      st.takeItems(HYACINTH_CHARM2,-1)
      st.giveItems(ADENA,18250)
      htmltext = "7154-08.htm"
      st.unset("cond")
      st.setState(COMPLETED)
      st.playSound("ItemSound.quest_finish")
   return htmltext

 def onKill (self,npc,player,isPet):
   st = player.getQuestState("159_ProtectHeadsprings")
   if st :
      if st.getState() != STARTED : return
      npcId = npc.getNpcId()
      cond = st.getInt("cond")
      count = st.getQuestItemsCount(PLAGUE_DUST)
      if cond == 1 and st.getRandom(100) < 40 and not count :
         st.giveItems(PLAGUE_DUST,1)
         st.playSound("ItemSound.quest_middle")
         st.set("cond","2")
      elif cond == 3 and st.getRandom(100) < 40 and count < 5 :
         if count == 4 :
            st.playSound("ItemSound.quest_middle")
            st.set("cond","4")
         else:
            st.playSound("ItemSound.quest_itemget")
         st.giveItems(PLAGUE_DUST,1)
   return

QUEST     = Quest(159,"159_ProtectHeadsprings","Protect Headsprings")
CREATED   = State('Start',     QUEST)
STARTING  = State('Starting',  QUEST)
STARTED   = State('Started',   QUEST)
COMPLETED = State('Completed', QUEST)


QUEST.setInitialState(CREATED)
QUEST.addStartNpc(7154)

QUEST.addTalkId(7154)

QUEST.addKillId(5017)