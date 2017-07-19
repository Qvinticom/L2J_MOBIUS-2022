# Made by Mr. Have fun! Version 0.2
import sys
from com.l2jmobius.gameserver.model.quest import State
from com.l2jmobius.gameserver.model.quest import QuestState
from com.l2jmobius.gameserver.model.quest.jython import QuestJython as JQuest

HATOSS_ORDER1_ID = 1553
HATOSS_ORDER2_ID = 1554
HATOSS_ORDER3_ID = 1555
LETTER_TO_HUMAN_ID = 1557
LETTER_TO_DARKELF_ID = 1556
LETTER_TO_ELF_ID = 1558
BUTCHER_ID = 1510
LESSER_HEALING_ID = 1060
CRYSTAL_BATTLE = 4412
CRYSTAL_LOVE = 4413
CRYSTAL_SOLITUDE = 4414
CRYSTAL_FEAST = 4415
CRYSTAL_CELEBRATION = 4416
SPIRITSHOT_FOR_BEGINNERS = 5790
SOULSHOT_FOR_BEGINNERS = 5789

class Quest (JQuest) :

 def __init__(self,id,name,descr):
     JQuest.__init__(self,id,name,descr)
     self.questItemIds = [HATOSS_ORDER2_ID, LETTER_TO_DARKELF_ID, LETTER_TO_HUMAN_ID, LETTER_TO_ELF_ID, HATOSS_ORDER1_ID, HATOSS_ORDER3_ID]

 def onEvent (self,event,st) :
    htmltext = event
    if event == "1" :
          st.set("id","0")
          htmltext = "7568-03.htm"
          st.giveItems(HATOSS_ORDER1_ID,1)
          st.set("cond","1")
          st.setState(STARTED)
          st.playSound("ItemSound.quest_accept")
    elif event == "7568_1" :
            htmltext = "7568-06.htm"
            st.takeItems(HATOSS_ORDER2_ID,1)
            st.takeItems(LETTER_TO_DARKELF_ID,1)
            st.takeItems(LETTER_TO_HUMAN_ID,1)
            st.takeItems(LETTER_TO_ELF_ID,1)
            st.takeItems(HATOSS_ORDER1_ID,1)
            st.takeItems(HATOSS_ORDER3_ID,1)
            st.set("cond","0")
            st.playSound("ItemSound.quest_giveup")
    elif event == "7568_2" :
            htmltext = "7568-07.htm"
            st.takeItems(HATOSS_ORDER1_ID,1)
            if st.getQuestItemsCount(HATOSS_ORDER2_ID) == 0 :
              st.giveItems(HATOSS_ORDER2_ID,1)
    elif event == "7568_3" :
            htmltext = "7568-06.htm"
            st.takeItems(HATOSS_ORDER1_ID,1)
            st.takeItems(LETTER_TO_DARKELF_ID,1)
            st.takeItems(LETTER_TO_HUMAN_ID,1)
            st.takeItems(LETTER_TO_ELF_ID,1)
            st.takeItems(HATOSS_ORDER2_ID,1)
            st.takeItems(HATOSS_ORDER3_ID,1)
            st.set("cond","0")
            st.playSound("ItemSound.quest_giveup")
    elif event == "7568_4" :
            htmltext = "7568-09.htm"
            st.takeItems(HATOSS_ORDER2_ID,1)
            if st.getQuestItemsCount(HATOSS_ORDER3_ID) == 0 :
              st.giveItems(HATOSS_ORDER3_ID,1)
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
   if npcId == 7568 and st.getInt("cond")==0 and st.getInt("onlyone")==0 :
        if st.getInt("cond") < 15 :
          if st.getPlayer().getRace().ordinal() != 3 :
            htmltext = "7568-00.htm"
            st.exitQuest(1)
          elif st.getPlayer().getLevel() >= 12 :
            htmltext = "7568-02.htm"
            return htmltext
          else:
            htmltext = "7568-01.htm"
            st.exitQuest(1)
        else:
          htmltext = "7568-01.htm"
          st.exitQuest(1)
   elif npcId == 7568 and st.getInt("cond")==0 and st.getInt("onlyone")==1 :
      htmltext = "<html><body>This quest has already been completed.</body></html>"
   elif npcId == 7568 and st.getInt("cond")==1 and (st.getQuestItemsCount(HATOSS_ORDER1_ID) or st.getQuestItemsCount(HATOSS_ORDER2_ID) or st.getQuestItemsCount(HATOSS_ORDER3_ID)) and ((st.getQuestItemsCount(LETTER_TO_ELF_ID)+st.getQuestItemsCount(LETTER_TO_HUMAN_ID)+st.getQuestItemsCount(LETTER_TO_DARKELF_ID))==0) :
          htmltext = "7568-04.htm"
   elif npcId == 7568 and st.getInt("cond")==1 and (st.getQuestItemsCount(HATOSS_ORDER1_ID) or st.getQuestItemsCount(HATOSS_ORDER2_ID) or st.getQuestItemsCount(HATOSS_ORDER3_ID)) and ((st.getQuestItemsCount(LETTER_TO_ELF_ID)+st.getQuestItemsCount(LETTER_TO_HUMAN_ID)+st.getQuestItemsCount(LETTER_TO_DARKELF_ID))==1) :
          htmltext = "7568-05.htm"
   elif npcId == 7568 and st.getInt("cond")==1 and (st.getQuestItemsCount(HATOSS_ORDER1_ID) or st.getQuestItemsCount(HATOSS_ORDER2_ID) or st.getQuestItemsCount(HATOSS_ORDER3_ID)) and ((st.getQuestItemsCount(LETTER_TO_ELF_ID)+st.getQuestItemsCount(LETTER_TO_HUMAN_ID)+st.getQuestItemsCount(LETTER_TO_DARKELF_ID))==2) :
          htmltext = "7568-08.htm"
   elif npcId == 7568 and st.getInt("cond")==1 and (st.getQuestItemsCount(HATOSS_ORDER1_ID) or st.getQuestItemsCount(HATOSS_ORDER2_ID) or st.getQuestItemsCount(HATOSS_ORDER3_ID)) and ((st.getQuestItemsCount(LETTER_TO_ELF_ID)+st.getQuestItemsCount(LETTER_TO_HUMAN_ID)+st.getQuestItemsCount(LETTER_TO_DARKELF_ID))==3) and st.getInt("onlyone")==0 :
          if st.getInt("id") != 107 :
            st.set("id","107")
            htmltext = "7568-10.htm"
            st.takeItems(LETTER_TO_DARKELF_ID,1)
            st.takeItems(LETTER_TO_HUMAN_ID,1)
            st.takeItems(LETTER_TO_ELF_ID,1)
            st.takeItems(HATOSS_ORDER3_ID,1)
            st.giveItems(BUTCHER_ID,1)
            st.giveItems(LESSER_HEALING_ID,100)
            st.giveItems(CRYSTAL_BATTLE,10)
            st.giveItems(CRYSTAL_LOVE,10)
            st.giveItems(CRYSTAL_SOLITUDE,10)
            st.giveItems(CRYSTAL_FEAST,10)
            st.giveItems(CRYSTAL_CELEBRATION,10)
            st.set("cond","0")
            st.setState(COMPLETED)
            st.playSound("ItemSound.quest_finish")
            st.set("onlyone","1")
            qs = st.getPlayer().getQuestState("255_Tutorial")
            if qs :
               newbiegift=qs.getInt("newbiegift")
               if newbiegift != 2 and st.getPlayer().getNewbieState() == 1 :
                  st.showQuestionMark(26)
                  if st.getPlayer().getClassId().isMage() :
                     st.playTutorialVoice("tutorial_voice_027")
                     st.giveItems(SPIRITSHOT_FOR_BEGINNERS,3000)
                  else :
                     st.playTutorialVoice("tutorial_voice_026")
                     st.giveItems(SOULSHOT_FOR_BEGINNERS,7000)
                  qs.set("newbiegift","2")
   elif npcId == 7580 and st.getInt("cond")==1 and (st.getQuestItemsCount(HATOSS_ORDER1_ID) or st.getQuestItemsCount(HATOSS_ORDER2_ID) or st.getQuestItemsCount(HATOSS_ORDER3_ID)) :
          htmltext = "7580-01.htm"
   return htmltext

 def onKill (self,npc,player,isPet):
   st = player.getQuestState("107_MercilessPunishment")
   if st :
      if st.getState() != STARTED : return
      npcId = npc.getNpcId()
      if npcId == 5041 :
        st.set("id","0")
        if st.getInt("cond") == 1 :
          if st.getQuestItemsCount(HATOSS_ORDER1_ID) and st.getQuestItemsCount(LETTER_TO_HUMAN_ID) == 0 :
            st.giveItems(LETTER_TO_HUMAN_ID,1)
            st.playSound("ItemSound.quest_itemget")
          if st.getQuestItemsCount(HATOSS_ORDER2_ID) and st.getQuestItemsCount(LETTER_TO_DARKELF_ID) == 0 :
            st.giveItems(LETTER_TO_DARKELF_ID,1)
            st.playSound("ItemSound.quest_itemget")
          if st.getQuestItemsCount(HATOSS_ORDER3_ID) and st.getQuestItemsCount(LETTER_TO_ELF_ID) == 0 :
            st.giveItems(LETTER_TO_ELF_ID,1)
            st.playSound("ItemSound.quest_itemget")
   return

QUEST       = Quest(107,"107_MercilessPunishment","Merciless Punishment")
CREATED     = State('Start', QUEST)
STARTING     = State('Starting', QUEST)
STARTED     = State('Started', QUEST)
COMPLETED   = State('Completed', QUEST)


QUEST.setInitialState(CREATED)
QUEST.addStartNpc(7568)

QUEST.addTalkId(7568)
QUEST.addTalkId(7580)

QUEST.addKillId(5041)