# Made by Mr. Have fun! - Version 0.4 by kmarty
import sys
from com.l2jmobius.gameserver.model.quest import State
from com.l2jmobius.gameserver.model.quest import QuestState
from com.l2jmobius.gameserver.model.quest.jython import QuestJython as JQuest

GALLINS_OAK_WAND_ID = 748
WAND_SPIRITBOUND1_ID = 1135
WAND_SPIRITBOUND2_ID = 1136
WAND_SPIRITBOUND3_ID = 1137
WAND_OF_ADEPT_ID = 747
SPIRITSHOT_FOR_BEGINNERS = 5790

DROPLIST = {
5003: (WAND_SPIRITBOUND1_ID),
5004: (WAND_SPIRITBOUND2_ID),
5005: (WAND_SPIRITBOUND3_ID)
}

# Helper function - If player have all quest items returns 1, otherwise 0
def HaveAllQuestItems (st) :
  for mobId in DROPLIST.keys() :
    if st.getQuestItemsCount(DROPLIST[mobId]) == 0 :
      return 0
  return 1

# Main Quest code
class Quest (JQuest) :

 def __init__(self,id,name,descr):
     JQuest.__init__(self,id,name,descr)
     self.questItemIds = [GALLINS_OAK_WAND_ID, WAND_SPIRITBOUND1_ID, WAND_SPIRITBOUND2_ID, WAND_SPIRITBOUND3_ID]

 def onEvent (self,event,st) :
    htmltext = event
    if event == "7017-03.htm" :
      st.set("cond","1")
      st.setState(STARTED)
      st.playSound("ItemSound.quest_accept")
      st.giveItems(GALLINS_OAK_WAND_ID,1)
      st.giveItems(GALLINS_OAK_WAND_ID,1)
      st.giveItems(GALLINS_OAK_WAND_ID,1)
    return htmltext

 def onTalk (Self,npc,st):
   npcId = npc.getNpcId()
   htmltext = "<html><body>I have nothing to say to you.</body></html>"
   id = st.getState()
   if id == CREATED :
     st.set("cond","0")
     st.set("onlyone","0")
   if npcId == 7017 and st.getInt("cond")==0 and st.getInt("onlyone")==0 :
     if st.getPlayer().getRace().ordinal() != 0 :
        htmltext = "7017-00.htm"
     elif st.getPlayer().getLevel() >= 10 :
        htmltext = "7017-02.htm"
        return htmltext
     else:
        htmltext = "7017-06.htm"
        st.exitQuest(1)
   elif npcId == 7017 and st.getInt("cond")==0 and st.getInt("onlyone")==1 :
      htmltext = "<html><body>This quest has already been completed.</body></html>"
   elif npcId == 7017 and st.getInt("cond") and st.getQuestItemsCount(GALLINS_OAK_WAND_ID)>=1 and not HaveAllQuestItems(st) :
      htmltext = "7017-04.htm"
   elif npcId == 7017 and st.getInt("cond")==3 and HaveAllQuestItems(st) :
      for mobId in DROPLIST.keys() :
        st.takeItems(DROPLIST[mobId],-1)
      st.giveItems(WAND_OF_ADEPT_ID,1)
      htmltext = "7017-05.htm"
      st.set("cond","0")
      st.setState(COMPLETED)
      st.playSound("ItemSound.quest_finish")
      st.set("onlyone","1")
      qs = st.getPlayer().getQuestState("255_Tutorial")
      if qs :
         newbiegift=qs.getInt("newbiegift")
         if newbiegift != 2 and st.getPlayer().getNewbieState() == 1 :
            st.showQuestionMark(26)
            st.playTutorialVoice("tutorial_voice_027")
            st.giveItems(SPIRITSHOT_FOR_BEGINNERS,3000)
            qs.set("newbiegift","2")
   elif npcId == 7045 and st.getInt("cond") :
      htmltext = "7045-01.htm"
      st.set("cond","2")
   elif npcId == 7043 and st.getInt("cond") :
      htmltext = "7043-01.htm"
      st.set("cond","2")
   elif npcId == 7041 and st.getInt("cond") :
      htmltext = "7041-01.htm"
      st.set("cond","2")
   return htmltext

 def onKill (self,npc,player,isPet):
   st = player.getQuestState("104_SpiritOfMirrors")
   if st :
      if st.getState() != STARTED : return
      npcId = npc.getNpcId()
      if st.getInt("cond") >= 1 and st.getItemEquipped(7) == GALLINS_OAK_WAND_ID and not st.getQuestItemsCount(DROPLIST[npcId]) : # (7) means weapon slot
        st.takeItems(GALLINS_OAK_WAND_ID,1)
        st.giveItems(DROPLIST[npcId],1)
        if HaveAllQuestItems(st) :
          st.set("cond","3")
          st.playSound("ItemSound.quest_middle")
        else :
          st.playSound("ItemSound.quest_itemget")
   return

QUEST       = Quest(104,"104_SpiritOfMirrors","Spirit Of Mirrors")
CREATED     = State('Start', QUEST)
STARTED     = State('Started', QUEST)
COMPLETED   = State('Completed', QUEST)


QUEST.setInitialState(CREATED)
QUEST.addStartNpc(7017)

QUEST.addTalkId(7017)
QUEST.addTalkId(7041)
QUEST.addTalkId(7043)
QUEST.addTalkId(7045)

for mobId in DROPLIST.keys():
  QUEST.addKillId(mobId)