# Made by Mr. Have fun! - Version 0.3 by DrLecter

import sys
from com.l2jmobius import Config
from com.l2jmobius.gameserver.model.quest import State
from com.l2jmobius.gameserver.model.quest import QuestState
from com.l2jmobius.gameserver.model.quest.jython import QuestJython as JQuest

KAROYDS_LETTER_ID = 968
CECKTINONS_VOUCHER1_ID = 969
CECKTINONS_VOUCHER2_ID = 970
BONE_FRAGMENT1_ID = 1107
SOUL_CATCHER_ID = 971
PRESERVE_OIL_ID = 972
ZOMBIE_HEAD_ID = 973
STEELBENDERS_HEAD_ID = 974
BLOODSABER_ID = 975
SOULSHOT_FOR_BEGINNERS = 5789

class Quest (JQuest) :

 def __init__(self,id,name,descr):
     JQuest.__init__(self,id,name,descr)
     self.questItemIds = [KAROYDS_LETTER_ID, CECKTINONS_VOUCHER1_ID, CECKTINONS_VOUCHER2_ID, BONE_FRAGMENT1_ID, SOUL_CATCHER_ID, PRESERVE_OIL_ID, ZOMBIE_HEAD_ID, STEELBENDERS_HEAD_ID]

 def onEvent (self,event,st) :
    htmltext = event
    if event == "7307-05.htm" :
        st.giveItems(KAROYDS_LETTER_ID,1)
        st.set("cond","1")
        st.setState(STARTED)
        st.playSound("ItemSound.quest_accept")
    return htmltext

 def onTalk (Self,npc,st) :
   npcId = npc.getNpcId()
   htmltext = "<html><body>I have nothing to say to you.</body></html>"
   id = st.getState()
   if id == CREATED :
     st.set("cond","0")
     st.set("onlyone","0")
   if npcId == 7307 and st.getInt("cond")==0 and st.getInt("onlyone")==0 :
     if st.getPlayer().getRace().ordinal() != 2 :
        htmltext = "7307-00.htm"
     elif st.getPlayer().getLevel() >= 11 :
        htmltext = "7307-03.htm"
        return htmltext
     else:
        htmltext = "7307-02.htm"
        st.exitQuest(1)
   elif npcId == 7307 and st.getInt("cond")==0 and st.getInt("onlyone")==1 :
        htmltext = "<html><body>This quest has already been completed.</body></html>"
   elif npcId == 7307 and st.getInt("cond")>=1 and (st.getQuestItemsCount(KAROYDS_LETTER_ID)>=1 or st.getQuestItemsCount(CECKTINONS_VOUCHER1_ID)>=1 or st.getQuestItemsCount(CECKTINONS_VOUCHER2_ID)>=1) :
        htmltext = "7307-06.htm"
   elif npcId == 7132 and st.getInt("cond")==1 and st.getQuestItemsCount(KAROYDS_LETTER_ID)==1 :
        htmltext = "7132-01.htm"
        st.set("cond","2")
        st.takeItems(KAROYDS_LETTER_ID,1)
        st.giveItems(CECKTINONS_VOUCHER1_ID,1)
   elif npcId == 7132 and st.getInt("cond")>=2 and (st.getQuestItemsCount(CECKTINONS_VOUCHER1_ID)>=1 or st.getQuestItemsCount(CECKTINONS_VOUCHER2_ID)>=1) :
        htmltext = "7132-02.htm"
   elif npcId == 7144 and st.getInt("cond")==2 and st.getQuestItemsCount(CECKTINONS_VOUCHER1_ID)>=1 :
        htmltext = "7144-01.htm"
        st.set("cond","3")
        st.takeItems(CECKTINONS_VOUCHER1_ID,1)
        st.giveItems(CECKTINONS_VOUCHER2_ID,1)
   elif npcId == 7144 and st.getInt("cond")==3 and st.getQuestItemsCount(CECKTINONS_VOUCHER2_ID)>=1 and st.getQuestItemsCount(BONE_FRAGMENT1_ID)<10 :
        htmltext = "7144-02.htm"
   elif npcId == 7144 and st.getInt("cond")==4 and st.getQuestItemsCount(CECKTINONS_VOUCHER2_ID)==1 and st.getQuestItemsCount(BONE_FRAGMENT1_ID)>=10 :
        htmltext = "7144-03.htm"
        st.set("cond","5")
        st.takeItems(CECKTINONS_VOUCHER2_ID,1)
        st.takeItems(BONE_FRAGMENT1_ID,10)
        st.giveItems(SOUL_CATCHER_ID,1)
   elif npcId == 7144 and st.getInt("cond")==5 and st.getQuestItemsCount(SOUL_CATCHER_ID)==1 :
        htmltext = "7144-04.htm"
   elif npcId == 7132 and st.getInt("cond")==5 and st.getQuestItemsCount(SOUL_CATCHER_ID)==1 :
        htmltext = "7132-03.htm"
        st.set("cond","6")
        st.takeItems(SOUL_CATCHER_ID,1)
        st.giveItems(PRESERVE_OIL_ID,1)
   elif npcId == 7132 and st.getInt("cond")==6 and st.getQuestItemsCount(PRESERVE_OIL_ID)==1 and st.getQuestItemsCount(ZOMBIE_HEAD_ID)==0 and st.getQuestItemsCount(STEELBENDERS_HEAD_ID)==0 :
        htmltext = "7132-04.htm"
   elif npcId == 7132 and st.getInt("cond")==7 and st.getQuestItemsCount(ZOMBIE_HEAD_ID)==1 :
        htmltext = "7132-05.htm"
        st.set("cond","8")
        st.takeItems(ZOMBIE_HEAD_ID,1)
        st.giveItems(STEELBENDERS_HEAD_ID,1)
   elif npcId == 7132 and st.getInt("cond")==8 and st.getQuestItemsCount(STEELBENDERS_HEAD_ID)==1 :
        htmltext = "7132-06.htm"
   elif npcId == 7307 and st.getInt("cond")==8 and st.getQuestItemsCount(STEELBENDERS_HEAD_ID)==1 :
        htmltext = "7307-07.htm"
        st.takeItems(STEELBENDERS_HEAD_ID,1)
        st.giveItems(BLOODSABER_ID,1)
        st.set("cond","0")
        st.setState(COMPLETED)
        st.playSound("ItemSound.quest_finish")
        st.set("onlyone","1")
        qs = st.getPlayer().getQuestState("255_Tutorial")
        if qs :
           newbiegift=qs.getInt("newbiegift")
           if newbiegift != 1 and st.getPlayer().getNewbieState() == 1 :
              st.showQuestionMark(26)
              st.playTutorialVoice("tutorial_voice_026")
              st.giveItems(SOULSHOT_FOR_BEGINNERS,7000)
              qs.set("newbiegift","1")
   return htmltext

 def onKill (self,npc,player,isPet):
   st = player.getQuestState("103_SpiritOfCraftsman")
   if st :
      if st.getState() != STARTED : return
      npcId = npc.getNpcId()
      if npcId in [517,518,455] :
         bones = st.getQuestItemsCount(BONE_FRAGMENT1_ID)
         if st.getQuestItemsCount(CECKTINONS_VOUCHER2_ID) == 1 and bones < 10 :
            numItems, chance = divmod(30*Config.RATE_DROP_QUEST,100)
            if st.getRandom(100) <= chance :
               numItems += 1
            numItems = int(numItems)
            if numItems != 0 :
               if 10 <= (bones + numItems) :
                  numItems = 10 - bones
                  st.playSound("ItemSound.quest_middle")
                  st.set("cond","4")
               else:
                  st.playSound("ItemSound.quest_itemget")
               st.giveItems(BONE_FRAGMENT1_ID,numItems)
      elif npcId in [15,20] :
         if st.getQuestItemsCount(PRESERVE_OIL_ID) == 1 :
            if st.getRandom(10)<3*Config.RATE_DROP_QUEST :
               st.set("cond","7")
               st.giveItems(ZOMBIE_HEAD_ID,1)
               st.playSound("ItemSound.quest_middle")
               st.takeItems(PRESERVE_OIL_ID,1)
   return

QUEST       = Quest(103,"103_SpiritOfCraftsman","Spirit Of Craftsman")
CREATED     = State('Start', QUEST)
STARTED     = State('Started', QUEST)
COMPLETED   = State('Completed', QUEST)


QUEST.setInitialState(CREATED)
QUEST.addStartNpc(7307)

QUEST.addTalkId(7132)
QUEST.addTalkId(7144)
QUEST.addTalkId(7307)

QUEST.addKillId(15)
QUEST.addKillId(20)
QUEST.addKillId(455)
QUEST.addKillId(517)
QUEST.addKillId(518)