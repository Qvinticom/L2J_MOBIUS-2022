# Made by Mr. Have fun! Version 0.2
import sys
from com.l2jmobius.gameserver.model.quest import State
from com.l2jmobius.gameserver.model.quest import QuestState
from com.l2jmobius.gameserver.model.quest.jython import QuestJython as JQuest

GOUPHS_CONTRACT_ID = 1559
REEPS_CONTRACT_ID = 1560
ELVEN_WINE_ID = 1561
BRONPS_DICE_ID = 1562
BRONPS_CONTRACT_ID = 1563
AQUAMARINE_ID = 1564
CHRYSOBERYL_ID = 1565
GEM_BOX1_ID = 1566
COAL_PIECE_ID = 1567
BRONPS_LETTER_ID = 1568
BERRY_TART_ID = 1569
BAT_DIAGRAM_ID = 1570
STAR_DIAMOND_ID = 1571
SILVERSMITH_HAMMER_ID = 1511
SPIRITSHOT_FOR_BEGINNERS = 5790
SOULSHOT_FOR_BEGINNERS = 5789

class Quest (JQuest) :

 def __init__(self,id,name,descr):
     JQuest.__init__(self,id,name,descr)
     self.questItemIds = [GEM_BOX1_ID, STAR_DIAMOND_ID, GOUPHS_CONTRACT_ID, REEPS_CONTRACT_ID, ELVEN_WINE_ID, BRONPS_CONTRACT_ID, AQUAMARINE_ID, CHRYSOBERYL_ID, COAL_PIECE_ID, BRONPS_DICE_ID, BRONPS_LETTER_ID, BERRY_TART_ID, BAT_DIAGRAM_ID]

 def onEvent (self,event,st) :
    htmltext = event
    if event == "1" :
          htmltext = "7523-03.htm"
          st.giveItems(GOUPHS_CONTRACT_ID,1)
          st.set("cond","1")
          st.setState(STARTED)
          st.playSound("ItemSound.quest_accept")
    elif event == "7555_1" :
          htmltext = "7555-02.htm"
          st.takeItems(REEPS_CONTRACT_ID,1)
          st.giveItems(ELVEN_WINE_ID,1)
    elif event == "7526_1" :
          htmltext = "7526-02.htm"
          st.takeItems(BRONPS_DICE_ID,1)
          st.giveItems(BRONPS_CONTRACT_ID,1)
    return htmltext

 def onTalk (Self,npc,st):
   npcId = npc.getNpcId()
   htmltext = "<html><body>I have nothing to say to you.</body></html>"
   id = st.getState()
   if id == CREATED :
     st.set("cond","0")
   if npcId == 7523 and id == COMPLETED :
      htmltext = "<html><body>This quest has already been completed.</body></html>"
   elif npcId == 7523 and st.getInt("cond")==0 :
          if st.getPlayer().getRace().ordinal() != 4 :
            htmltext = "7523-00.htm"
            st.exitQuest(1)
          elif st.getPlayer().getLevel() >= 10 :
            htmltext = "7523-02.htm"
            return htmltext
          else:
            htmltext = "7523-01.htm"
            st.exitQuest(1)
   elif npcId == 7523 and st.getInt("cond")==1 and st.getQuestItemsCount(GOUPHS_CONTRACT_ID) :
          htmltext = "7523-04.htm"
   elif npcId == 7523 and st.getInt("cond")==1 and (st.getQuestItemsCount(REEPS_CONTRACT_ID) or st.getQuestItemsCount(ELVEN_WINE_ID) or st.getQuestItemsCount(BRONPS_DICE_ID) or st.getQuestItemsCount(BRONPS_CONTRACT_ID)) :
          htmltext = "7523-05.htm"
   elif npcId == 7523 and st.getInt("cond")==1 and st.getQuestItemsCount(GEM_BOX1_ID) :
          htmltext = "7523-06.htm"
          st.takeItems(GEM_BOX1_ID,1)
          st.giveItems(COAL_PIECE_ID,1)
   elif npcId == 7523 and st.getInt("cond")==1 and (st.getQuestItemsCount(BRONPS_LETTER_ID) or st.getQuestItemsCount(COAL_PIECE_ID) or st.getQuestItemsCount(BERRY_TART_ID) or st.getQuestItemsCount(BAT_DIAGRAM_ID)) :
          htmltext = "7523-07.htm"
   elif npcId == 7523 and st.getInt("cond")==1 and st.getQuestItemsCount(STAR_DIAMOND_ID) :
            htmltext = "7523-08.htm"
            st.giveItems(SILVERSMITH_HAMMER_ID,1)
            for item in range(4412,4417) :
               st.giveItems(item,10)   # Echo crystals
            st.giveItems(1060,100)     # Lesser Healing Potions
            st.takeItems(STAR_DIAMOND_ID,-1)
            st.set("cond","0")
            st.setState(COMPLETED)
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
            st.playSound("ItemSound.quest_finish")
   elif npcId == 7516 and st.getInt("cond")==1 and st.getQuestItemsCount(GOUPHS_CONTRACT_ID) and st.getQuestItemsCount(REEPS_CONTRACT_ID)==0 :
          htmltext = "7516-01.htm"
          st.giveItems(REEPS_CONTRACT_ID,1)
          st.takeItems(GOUPHS_CONTRACT_ID,1)
   elif npcId == 7516 and st.getInt("cond")==1 and st.getQuestItemsCount(GOUPHS_CONTRACT_ID)==0 and st.getQuestItemsCount(REEPS_CONTRACT_ID) :
          htmltext = "7516-02.htm"
   elif npcId == 7516 and st.getInt("cond")==1 and st.getQuestItemsCount(GOUPHS_CONTRACT_ID)==0 and st.getQuestItemsCount(REEPS_CONTRACT_ID)==0 :
          htmltext = "7516-03.htm"
   elif npcId == 7555 and st.getInt("cond")==1 and st.getQuestItemsCount(REEPS_CONTRACT_ID)==0 and st.getQuestItemsCount(ELVEN_WINE_ID)==0 :
          htmltext = "7555-01.htm"
   elif npcId == 7555 and st.getInt("cond")==1 and st.getQuestItemsCount(REEPS_CONTRACT_ID) and st.getQuestItemsCount(ELVEN_WINE_ID)==0 :
          htmltext = "7555-02.htm"
          st.giveItems(ELVEN_WINE_ID,1)
          st.takeItems(REEPS_CONTRACT_ID,1)
   elif npcId == 7555 and st.getInt("cond")==1 and st.getQuestItemsCount(REEPS_CONTRACT_ID)==0 and st.getQuestItemsCount(ELVEN_WINE_ID) :
          htmltext = "7555-03.htm"
   elif npcId == 7555 and st.getInt("cond")==1 and st.getQuestItemsCount(GEM_BOX1_ID)==1 :
          htmltext = "7555-04.htm"
   elif npcId == 7555 and st.getInt("cond")==1 and st.getQuestItemsCount(GEM_BOX1_ID)==0 and st.getQuestItemsCount(REEPS_CONTRACT_ID)==0 and st.getQuestItemsCount(ELVEN_WINE_ID)==0 :
          htmltext = "7555-05.htm"
   elif npcId == 7529 and st.getInt("cond")==1 and st.getQuestItemsCount(ELVEN_WINE_ID) and st.getQuestItemsCount(BRONPS_DICE_ID)==0 :
          htmltext = "7529-01.htm"
          st.giveItems(BRONPS_DICE_ID,1)
          st.takeItems(ELVEN_WINE_ID,1)
   elif npcId == 7529 and st.getInt("cond")==1 and st.getQuestItemsCount(ELVEN_WINE_ID)==0 and st.getQuestItemsCount(BRONPS_DICE_ID) :
          htmltext = "7529-02.htm"
   elif npcId == 7529 and st.getInt("cond")==1 and st.getQuestItemsCount(ELVEN_WINE_ID)==0 and st.getQuestItemsCount(BRONPS_DICE_ID)==0 :
          htmltext = "7529-03.htm"
   elif npcId == 7526 and st.getInt("cond")==1 and st.getQuestItemsCount(BRONPS_DICE_ID) :
          htmltext = "7526-01.htm"
   elif npcId == 7526 and st.getInt("cond")==1 and st.getQuestItemsCount(BRONPS_CONTRACT_ID) and (st.getQuestItemsCount(AQUAMARINE_ID)+st.getQuestItemsCount(CHRYSOBERYL_ID)<20) :
          htmltext = "7526-03.htm"
   elif npcId == 7526 and st.getInt("cond")==1 and st.getQuestItemsCount(BRONPS_CONTRACT_ID) and (st.getQuestItemsCount(AQUAMARINE_ID)+st.getQuestItemsCount(CHRYSOBERYL_ID)>=20) :
          htmltext = "7526-04.htm"
          st.takeItems(BRONPS_CONTRACT_ID,1)
          st.takeItems(AQUAMARINE_ID,st.getQuestItemsCount(AQUAMARINE_ID))
          st.takeItems(CHRYSOBERYL_ID,st.getQuestItemsCount(CHRYSOBERYL_ID))
          st.giveItems(GEM_BOX1_ID,1)
   elif npcId == 7526 and st.getInt("cond")==1 and st.getQuestItemsCount(GEM_BOX1_ID) :
          htmltext = "7526-05.htm"
   elif npcId == 7526 and st.getInt("cond")==1 and st.getQuestItemsCount(COAL_PIECE_ID) :
          htmltext = "7526-06.htm"
          st.takeItems(COAL_PIECE_ID,1)
          st.giveItems(BRONPS_LETTER_ID,1)
   elif npcId == 7526 and st.getInt("cond")==1 and st.getQuestItemsCount(BRONPS_LETTER_ID) :
          htmltext = "7526-07.htm"
   elif npcId == 7526 and st.getInt("cond")==1 and st.getQuestItemsCount(BERRY_TART_ID) or st.getQuestItemsCount(BAT_DIAGRAM_ID) or st.getQuestItemsCount(STAR_DIAMOND_ID) :
          htmltext = "7526-08.htm"
   elif npcId == 7521 and st.getInt("cond")==1 and st.getQuestItemsCount(BRONPS_LETTER_ID) and st.getQuestItemsCount(BERRY_TART_ID)==0 :
          htmltext = "7521-01.htm"
          st.giveItems(BERRY_TART_ID,1)
          st.takeItems(BRONPS_LETTER_ID,1)
   elif npcId == 7521 and st.getInt("cond")==1 and st.getQuestItemsCount(BRONPS_LETTER_ID)==0 and st.getQuestItemsCount(BERRY_TART_ID) :
          htmltext = "7521-02.htm"
   elif npcId == 7521 and st.getInt("cond")==1 and st.getQuestItemsCount(BRONPS_LETTER_ID)==0 and st.getQuestItemsCount(BERRY_TART_ID)==0 :
          htmltext = "7521-03.htm"
   elif npcId == 7522 and st.getInt("cond")==1 and st.getQuestItemsCount(BAT_DIAGRAM_ID)==0 and st.getQuestItemsCount(BERRY_TART_ID) and st.getQuestItemsCount(STAR_DIAMOND_ID)==0 :
          htmltext = "7522-01.htm"
          st.giveItems(BAT_DIAGRAM_ID,1)
          st.takeItems(BERRY_TART_ID,1)
   elif npcId == 7522 and st.getInt("cond")==1 and st.getQuestItemsCount(BAT_DIAGRAM_ID) and st.getQuestItemsCount(BERRY_TART_ID)==0 and st.getQuestItemsCount(STAR_DIAMOND_ID)==0 :
          htmltext = "7522-02.htm"
   elif npcId == 7522 and st.getInt("cond")==1 and st.getQuestItemsCount(BAT_DIAGRAM_ID)==0 and st.getQuestItemsCount(BERRY_TART_ID)==0 and st.getQuestItemsCount(STAR_DIAMOND_ID) :
          htmltext = "7522-03.htm"
   elif npcId == 7522 and st.getInt("cond")==1 and st.getQuestItemsCount(BAT_DIAGRAM_ID)==0 and st.getQuestItemsCount(BERRY_TART_ID)==0 and st.getQuestItemsCount(STAR_DIAMOND_ID)==0 :
          htmltext = "7522-04.htm"
   return htmltext

 def onKill (self,npc,player,isPet):
   st = player.getQuestState("108_JumbleTumbleDiamondFuss")
   if st :
      if st.getState() != STARTED : return
      npcId = npc.getNpcId()
      if npcId == 323 :
        if st.getInt("cond") == 1 and st.getQuestItemsCount(BRONPS_CONTRACT_ID) :
          if st.getRandom(10) < 8 :
            if st.getQuestItemsCount(AQUAMARINE_ID)+st.getQuestItemsCount(CHRYSOBERYL_ID) == 19 :
              if st.getQuestItemsCount(AQUAMARINE_ID) < 10 :
                st.giveItems(AQUAMARINE_ID,1)
                st.playSound("ItemSound.quest_middle")
            else:
              if st.getQuestItemsCount(AQUAMARINE_ID) < 10 :
                st.giveItems(AQUAMARINE_ID,1)
                st.playSound("ItemSound.quest_itemget")
          if st.getRandom(10) < 8 :
            if st.getQuestItemsCount(AQUAMARINE_ID)+st.getQuestItemsCount(CHRYSOBERYL_ID) == 19 :
              if st.getQuestItemsCount(CHRYSOBERYL_ID) < 10 :
                st.giveItems(CHRYSOBERYL_ID,1)
                st.playSound("ItemSound.quest_middle")
            elif st.getQuestItemsCount(AQUAMARINE_ID)+st.getQuestItemsCount(CHRYSOBERYL_ID) < 20 :
                if st.getQuestItemsCount(CHRYSOBERYL_ID) < 10 :
                  st.giveItems(CHRYSOBERYL_ID,1)
                  st.playSound("ItemSound.quest_itemget")
      elif npcId == 324 :
        if st.getInt("cond") == 1 and st.getQuestItemsCount(BRONPS_CONTRACT_ID) :
          if st.getRandom(10) < 6 :
            if st.getQuestItemsCount(AQUAMARINE_ID)+st.getQuestItemsCount(CHRYSOBERYL_ID) == 19 :
              if st.getQuestItemsCount(AQUAMARINE_ID) < 10 :
                st.giveItems(AQUAMARINE_ID,1)
                st.playSound("ItemSound.quest_middle")
            else:
              if st.getQuestItemsCount(AQUAMARINE_ID) < 10 :
                st.giveItems(AQUAMARINE_ID,1)
                st.playSound("ItemSound.quest_itemget")
          if st.getRandom(10) < 6 :
            if st.getQuestItemsCount(AQUAMARINE_ID)+st.getQuestItemsCount(CHRYSOBERYL_ID) == 19 :
              if st.getQuestItemsCount(CHRYSOBERYL_ID) < 10 :
                st.giveItems(CHRYSOBERYL_ID,1)
                st.playSound("ItemSound.quest_middle")
            elif st.getQuestItemsCount(AQUAMARINE_ID)+st.getQuestItemsCount(CHRYSOBERYL_ID) < 20 :
                if st.getQuestItemsCount(CHRYSOBERYL_ID) < 10 :
                  st.giveItems(CHRYSOBERYL_ID,1)
                  st.playSound("ItemSound.quest_itemget")
      elif npcId == 480 :
        if st.getInt("cond") == 1 and st.getQuestItemsCount(BAT_DIAGRAM_ID) and st.getQuestItemsCount(STAR_DIAMOND_ID) == 0 :
          if st.getRandom(10) < 2 :
            st.giveItems(STAR_DIAMOND_ID,1)
            st.takeItems(BAT_DIAGRAM_ID,1)
            st.playSound("ItemSound.quest_middle")
   return

QUEST       = Quest(108,"108_JumbleTumbleDiamondFuss","Jumble Tumble Diamond Fuss")
CREATED     = State('Start', QUEST)
STARTING     = State('Starting', QUEST)
STARTED     = State('Started', QUEST)
COMPLETED   = State('Completed', QUEST)


QUEST.setInitialState(CREATED)
QUEST.addStartNpc(7523)

QUEST.addTalkId(7516)
QUEST.addTalkId(7521)
QUEST.addTalkId(7522)
QUEST.addTalkId(7523)
QUEST.addTalkId(7526)
QUEST.addTalkId(7529)
QUEST.addTalkId(7555)

QUEST.addKillId(323)
QUEST.addKillId(324)
QUEST.addKillId(480)