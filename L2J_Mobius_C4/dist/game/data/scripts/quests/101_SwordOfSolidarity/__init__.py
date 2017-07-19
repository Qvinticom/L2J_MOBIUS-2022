# Made by Mr. Have fun! - Version 0.3 by DrLecter

import sys
from com.l2jmobius.gameserver.model.quest import State
from com.l2jmobius.gameserver.model.quest import QuestState
from com.l2jmobius.gameserver.model.quest.jython import QuestJython as JQuest

ROIENS_LETTER_ID = 796
HOWTOGO_RUINS_ID = 937
BROKEN_SWORD_HANDLE_ID = 739
BROKEN_BLADE_BOTTOM_ID = 740
BROKEN_BLADE_TOP_ID = 741
ALLTRANS_NOTE_ID = 742
SWORD_OF_SOLIDARITY_ID = 738
SOULSHOT_FOR_BEGINNERS = 5789

class Quest (JQuest) :

 def __init__(self,id,name,descr):
     JQuest.__init__(self,id,name,descr)
     self.questItemIds = [ALLTRANS_NOTE_ID, HOWTOGO_RUINS_ID, BROKEN_BLADE_TOP_ID, BROKEN_BLADE_BOTTOM_ID, ROIENS_LETTER_ID, BROKEN_SWORD_HANDLE_ID]

 def onEvent (self,event,st) :
    htmltext = event
    if event == "7008-04.htm" :
        st.set("cond","1")
        st.setState(STARTED)
        st.playSound("ItemSound.quest_accept")
        st.giveItems(ROIENS_LETTER_ID,1)
    elif event == "7283-02.htm" :
        st.set("cond","2")
        st.takeItems(ROIENS_LETTER_ID,st.getQuestItemsCount(ROIENS_LETTER_ID))
        st.giveItems(HOWTOGO_RUINS_ID,1)
    elif event == "7283-07.htm" :
        st.takeItems(BROKEN_SWORD_HANDLE_ID,-1)
        st.giveItems(SWORD_OF_SOLIDARITY_ID,1)
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

 def onTalk (Self,npc,st) :
   npcId = npc.getNpcId()
   htmltext = "<html><body>I have nothing to say to you.</body></html>"
   id = st.getState()
   if id == CREATED :
     st.set("cond","0")
     st.set("onlyone","0")
   if npcId == 7008 and st.getInt("cond")==0 and st.getInt("onlyone")==0 :
      if st.getPlayer().getRace().ordinal() != 0 :
        htmltext = "7008-00.htm"
      elif st.getPlayer().getLevel() >= 9 :
        htmltext = "7008-02.htm"
        return htmltext
      else:
        htmltext = "7008-08.htm"
        st.exitQuest(1)
   elif npcId == 7008 and st.getInt("cond")==0 and st.getInt("onlyone")==1 :
        htmltext = "<html><body>This quest has already been completed.</body></html>"
   if id == STARTED:
      if npcId == 7008 and st.getInt("cond")==1 and (st.getQuestItemsCount(ROIENS_LETTER_ID)==1) :
        htmltext = "7008-05.htm"
      elif npcId == 7008 and st.getInt("cond")>=2 and st.getQuestItemsCount(ROIENS_LETTER_ID)==0 and st.getQuestItemsCount(ALLTRANS_NOTE_ID)==0 :
        if st.getQuestItemsCount(BROKEN_BLADE_TOP_ID) and st.getQuestItemsCount(BROKEN_BLADE_BOTTOM_ID) :
          htmltext = "7008-12.htm"
        if (st.getQuestItemsCount(BROKEN_BLADE_TOP_ID) + st.getQuestItemsCount(BROKEN_BLADE_BOTTOM_ID)) <= 1 :
          htmltext = "7008-11.htm"
        if st.getQuestItemsCount(BROKEN_SWORD_HANDLE_ID) > 0 :
          htmltext = "7008-07.htm"
        if st.getQuestItemsCount(HOWTOGO_RUINS_ID) == 1 :
          htmltext = "7008-10.htm"
      elif npcId == 7008 and st.getInt("cond")==4 and st.getQuestItemsCount(ROIENS_LETTER_ID)==0 and st.getQuestItemsCount(ALLTRANS_NOTE_ID) :
        htmltext = "7008-06.htm"
        st.set("cond","5")
        st.takeItems(ALLTRANS_NOTE_ID,st.getQuestItemsCount(ALLTRANS_NOTE_ID))
        st.giveItems(BROKEN_SWORD_HANDLE_ID,1)
      elif npcId == 7283 and st.getInt("cond")==1 and st.getQuestItemsCount(ROIENS_LETTER_ID)>0 :
        htmltext = "7283-01.htm"
      elif npcId == 7283 and st.getInt("cond")>=2 and st.getQuestItemsCount(ROIENS_LETTER_ID)==0 and st.getQuestItemsCount(HOWTOGO_RUINS_ID)>0 :
        if (st.getQuestItemsCount(BROKEN_BLADE_TOP_ID) + st.getQuestItemsCount(BROKEN_BLADE_BOTTOM_ID)) == 1 :
          htmltext = "7283-08.htm"
        if (st.getQuestItemsCount(BROKEN_BLADE_TOP_ID) + st.getQuestItemsCount(BROKEN_BLADE_BOTTOM_ID)) == 0 :
          htmltext = "7283-03.htm"
        if st.getQuestItemsCount(BROKEN_BLADE_TOP_ID) and st.getQuestItemsCount(BROKEN_BLADE_BOTTOM_ID) :
          htmltext = "7283-04.htm"
          st.set("cond","4")
          st.takeItems(HOWTOGO_RUINS_ID,st.getQuestItemsCount(HOWTOGO_RUINS_ID))
          st.takeItems(BROKEN_BLADE_TOP_ID,st.getQuestItemsCount(BROKEN_BLADE_TOP_ID))
          st.takeItems(BROKEN_BLADE_BOTTOM_ID,st.getQuestItemsCount(BROKEN_BLADE_BOTTOM_ID))
          st.giveItems(ALLTRANS_NOTE_ID,1)
      elif npcId == 7283 and st.getInt("cond")==4 and st.getQuestItemsCount(ALLTRANS_NOTE_ID) :
        htmltext = "7283-05.htm"
      elif npcId == 7283 and st.getInt("cond")==5 and st.getQuestItemsCount(BROKEN_SWORD_HANDLE_ID) :
        htmltext = "7283-06.htm"
   return htmltext

 def onKill (self,npc,player,isPet):
   st = player.getQuestState("101_SwordOfSolidarity")
   if st:
      if st.getState() != STARTED : return
      npcId = npc.getNpcId()
      if npcId in [361,362] :
         if st.getQuestItemsCount(HOWTOGO_RUINS_ID) :
            if st.getQuestItemsCount(BROKEN_BLADE_TOP_ID) == 0 :
               if st.getRandom(5) == 0 :
                  st.giveItems(BROKEN_BLADE_TOP_ID,1)
                  st.playSound("ItemSound.quest_middle")
            elif st.getQuestItemsCount(BROKEN_BLADE_BOTTOM_ID) == 0 :
               if st.getRandom(5) == 0 :
                  st.giveItems(BROKEN_BLADE_BOTTOM_ID,1)
                  st.playSound("ItemSound.quest_middle")
         if st.getQuestItemsCount(BROKEN_BLADE_TOP_ID) and st.getQuestItemsCount(BROKEN_BLADE_BOTTOM_ID) :
            st.set("cond","3")
   return

QUEST       = Quest(101,"101_SwordOfSolidarity","Sword Of Solidarity")
CREATED     = State('Start', QUEST)
STARTED     = State('Started', QUEST)
COMPLETED   = State('Completed', QUEST)


QUEST.setInitialState(CREATED)
QUEST.addStartNpc(7008)

QUEST.addTalkId(7008)
QUEST.addTalkId(7283)

QUEST.addKillId(361)
QUEST.addKillId(362)