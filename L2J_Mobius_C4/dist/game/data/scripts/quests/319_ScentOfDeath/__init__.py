# Made by Mr. - Version 0.3 by DrLecter
import sys
from com.l2jmobius.gameserver.model.quest import State
from com.l2jmobius.gameserver.model.quest import QuestState
from com.l2jmobius.gameserver.model.quest.jython import QuestJython as JQuest

ZOMBIE_SKIN = 1045
ADENA = 57
HEALING_POTION = 1061

class Quest (JQuest) :

 def __init__(self,id,name,descr):
     JQuest.__init__(self,id,name,descr)
     self.questItemIds = [ZOMBIE_SKIN]

 def onEvent (self,event,st) :
    htmltext = event
    if event == "7138-04.htm" :
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
   if st.getInt("cond")==0 :
     if st.getPlayer().getLevel() >= 11 :
       htmltext = "7138-03.htm"
     else:
       htmltext = "7138-02.htm"
       st.exitQuest(1)
   else :
     if st.getQuestItemsCount(ZOMBIE_SKIN)<5 :
       htmltext = "7138-05.htm"
     else :
       htmltext = "7138-06.htm"
       st.giveItems(ADENA,2000)
       st.giveItems(HEALING_POTION,1)
       st.takeItems(ZOMBIE_SKIN,-1)
       st.exitQuest(1)
       st.playSound("ItemSound.quest_finish")
   return htmltext

 def onKill (self,npc,player,isPet):
   st = player.getQuestState("319_ScentOfDeath")
   if st :
     if st.getState() != STARTED : return
     count = st.getQuestItemsCount(ZOMBIE_SKIN)
     if count < 5 and st.getRandom(10) > 7 :
       st.giveItems(ZOMBIE_SKIN,1)
       if count == 4 :
         st.playSound("ItemSound.quest_middle")
         st.set("cond","2")
       else :
         st.playSound("ItemSound.quest_itemget")
   return

QUEST       = Quest(319,"319_ScentOfDeath","Scent Of Death")
CREATED     = State('Start', QUEST)
STARTING    = State('Starting', QUEST)
STARTED     = State('Started', QUEST)
COMPLETED   = State('Completed', QUEST)

QUEST.setInitialState(CREATED)
QUEST.addStartNpc(7138)

QUEST.addTalkId(7138)

QUEST.addKillId(15)
QUEST.addKillId(20)