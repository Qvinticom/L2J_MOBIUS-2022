# Made by Mr. Have fun! Version 0.2
# Version 0.3 by H1GHL4ND3R
import sys
from com.l2jmobius.gameserver.model.quest import State
from com.l2jmobius.gameserver.model.quest import QuestState
from com.l2jmobius.gameserver.model.quest.jython import QuestJython as JQuest

ONYX_TALISMAN1,      ONYX_TALISMAN2,     ANCIENT_SCROLL,  \
ANCIENT_CLAY_TABLET, KARTAS_TRANSLATION, ELDRITCH_DAGGER  \
= range(984,990)

ORC = 5070
SPIRITSHOT_FOR_BEGINNERS = 5790

class Quest (JQuest) :

 def __init__(self,id,name,descr):
     JQuest.__init__(self,id,name,descr)
     self.questItemIds = [KARTAS_TRANSLATION, ONYX_TALISMAN1, ONYX_TALISMAN2, ANCIENT_SCROLL, ANCIENT_CLAY_TABLET]

 def onEvent (self,event,st) :
    htmltext = event
    if event == "7358-05.htm" :
        st.giveItems(ONYX_TALISMAN1,1)
        st.set("cond","1")
        st.setState(STARTED)
        st.playSound("ItemSound.quest_accept")
    return htmltext

 def onTalk (Self,npc,st):
   npcId = npc.getNpcId()
   htmltext = "<html><body>I have nothing to say to you.</body></html>"
   id = st.getState()
   if id == CREATED :                                      # Check if is starting the quest
     st.set("cond","0")
     if st.getPlayer().getRace().ordinal() == 2 :
       if st.getPlayer().getLevel() >= 10 :
         htmltext = "7358-03.htm"
       else:
         htmltext = "7358-02.htm"
         st.exitQuest(1)
     else :
       htmltext = "7358-00.htm"
       st.exitQuest(1)
   elif id == COMPLETED :                                  # Check if the quest is already made
     htmltext = "<html><body>This quest has already been completed.</body></html>"
   else :                                                  # The quest itself
     try :
       cond = st.getInt("cond")
     except :
       cond = None
     if cond == 1 :
       if npcId == 7358 :
         htmltext = "7358-06.htm"
       elif npcId == 7133 and st.getQuestItemsCount(ONYX_TALISMAN1) :
         htmltext = "7133-01.htm"
         st.takeItems(ONYX_TALISMAN1,1)
         st.giveItems(ONYX_TALISMAN2,1)
         st.set("cond","2")
     elif cond == 2 :
       if npcId == 7358 :
         htmltext = "7358-06.htm"
       elif npcId == 7133 :
         htmltext = "7133-02.htm"
     elif cond == 3 :
       if npcId == 7358 :
         htmltext = "7358-06.htm"
       elif npcId == 7133 and st.getQuestItemsCount(ANCIENT_SCROLL) and st.getQuestItemsCount(ANCIENT_CLAY_TABLET) :
         htmltext = "7133-03.htm"
         st.takeItems(ONYX_TALISMAN2,1)
         st.takeItems(ANCIENT_SCROLL,1)
         st.takeItems(ANCIENT_CLAY_TABLET,1)
         st.giveItems(KARTAS_TRANSLATION,1)
         st.set("cond","4")
     elif cond == 4 :
       if npcId == 7358 and st.getQuestItemsCount(KARTAS_TRANSLATION) :
         htmltext = "7358-07.htm"
         st.takeItems(KARTAS_TRANSLATION,1)
         st.giveItems(ELDRITCH_DAGGER,1)
         for item in range(4412,4417) :
               st.giveItems(item,10)
         st.giveItems(1060,100)
         if st.getPlayer().getClassId().isMage():
           item = 2509
           qty = 300
         else :
           item = 1835
           qty = 100
         st.giveItems(item,qty)
         st.unset("cond")
         st.setState(COMPLETED)
         qs = st.getPlayer().getQuestState("255_Tutorial")
         if qs :
            newbiegift=qs.getInt("newbiegift")
            if newbiegift != 2 and st.getPlayer().getNewbieState() == 1 :
               st.showQuestionMark(26)
               st.playTutorialVoice("tutorial_voice_027")
               st.giveItems(SPIRITSHOT_FOR_BEGINNERS,3000)
               qs.set("newbiegift","2")
         st.playSound("ItemSound.quest_finish")
       elif npcId == 7133 :
         htmltext = "7133-04.htm"
   return htmltext

 def onKill (self,npc,player,isPet):
   st = player.getQuestState("106_ForgottenTruth")
   if st:
     if st.getState() != STARTED : return
     if st.getInt("cond") == 2 :
       if st.getRandom(100) < 20 :
         if st.getQuestItemsCount(ANCIENT_SCROLL) == 0 :
           st.giveItems(ANCIENT_SCROLL,1)
           st.playSound("Itemsound.quest_itemget")
         elif st.getQuestItemsCount(ANCIENT_CLAY_TABLET) == 0 :
           st.giveItems(ANCIENT_CLAY_TABLET,1)
           st.playSound("ItemSound.quest_middle")
           st.set("cond","3")
   return

QUEST       = Quest(106,"106_ForgottenTruth","Forgotten Truth")
CREATED     = State('Start', QUEST)
STARTING    = State('Starting', QUEST)
STARTED     = State('Started', QUEST)
COMPLETED   = State('Completed', QUEST)

QUEST.setInitialState(CREATED)
QUEST.addStartNpc(7358)

QUEST.addTalkId(7133)
QUEST.addTalkId(7358)

QUEST.addKillId(5070)