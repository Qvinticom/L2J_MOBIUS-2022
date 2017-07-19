# Made by Mr. - Version 0.3 by DrLecter
import sys
from com.l2jmobius.gameserver.model.quest import State
from com.l2jmobius.gameserver.model.quest import QuestState
from com.l2jmobius.gameserver.model.quest.jython import QuestJython as JQuest

BONE_FRAGMENT3 = 1158
ELF_SKULL = 1159
BONE_SHIELD = 625

class Quest (JQuest) :

 def __init__(self,id,name,descr):
     JQuest.__init__(self,id,name,descr)
     self.questItemIds = [ELF_SKULL, BONE_FRAGMENT3]

 def onEvent (self,event,st) :
    htmltext = event
    if event == "7147-04.htm" :
      st.set("cond","1")
      st.setState(STARTED)
      st.playSound("ItemSound.quest_accept")
    return htmltext

 def onTalk (Self,npc,st):
   npcId = npc.getNpcId()
   htmltext = "<html><body>I have nothing to say to you.</body></html>"
   id = st.getState()
   if id == CREATED :
     st.set("cond","0")
   if id == COMPLETED :
      htmltext = "<html><body>This quest has already been completed.</body></html>"
   elif st.getInt("cond") == 0 :
      if st.getPlayer().getRace().ordinal() == 2 :
         htmltext = "7147-00.htm"
         st.exitQuest(1)
      elif st.getPlayer().getLevel() >= 12 :
         htmltext = "7147-02.htm"
      else:
         htmltext = "7147-01.htm"
         st.exitQuest(1)
   else :
      if st.getQuestItemsCount(ELF_SKULL) < 3 and st.getQuestItemsCount(BONE_FRAGMENT3) < 10 :
         htmltext = "7147-05.htm"
      else  :
         htmltext = "7147-06.htm"
         st.giveItems(BONE_SHIELD,1)
         st.giveItems(57,24000)
         st.takeItems(ELF_SKULL,-1)
         st.takeItems(BONE_FRAGMENT3,-1)
         st.unset("cond")
         st.setState(COMPLETED)
         st.playSound("ItemSound.quest_finish")
   return htmltext

 def onKill (self,npc,player,isPet):
   st = player.getQuestState("162_CurseOfFortress")
   if st :
      if st.getState() != STARTED : return
      if st.getRandom(4) == 1 :
        npcId = npc.getNpcId()
        bones = st.getQuestItemsCount(BONE_FRAGMENT3)
        skulls = st.getQuestItemsCount(ELF_SKULL)
        if npcId in [464,463,504] :
          if bones < 10 :
            st.giveItems(BONE_FRAGMENT3,1)
            if bones == 9 and skulls == 3 :
              st.playSound("ItemSound.quest_middle")
              st.set ("cond","2")
            else:
              st.playSound("ItemSound.quest_itemget")
        elif skulls < 3 :
          st.giveItems(ELF_SKULL,1)
          if bones == 10 and skulls == 2 :
            st.playSound("ItemSound.quest_middle")
            st.set ("cond","2")
          else:
            st.playSound("ItemSound.quest_itemget")
   return

QUEST       = Quest(162,"162_CurseOfFortress","Curse Of Fortress")
CREATED     = State('Start', QUEST)
STARTING    = State('Starting', QUEST)
STARTED     = State('Started', QUEST)
COMPLETED   = State('Completed', QUEST)

QUEST.setInitialState(CREATED)
QUEST.addStartNpc(7147)

QUEST.addTalkId(7147)

QUEST.addKillId(33)
QUEST.addKillId(345)
QUEST.addKillId(371)
QUEST.addKillId(463)
QUEST.addKillId(464)
QUEST.addKillId(504)