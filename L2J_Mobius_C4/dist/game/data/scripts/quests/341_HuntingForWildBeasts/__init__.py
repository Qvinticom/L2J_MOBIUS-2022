# Made by mtrix
import sys
from com.l2jmobius.gameserver.model.quest import State
from com.l2jmobius.gameserver.model.quest import QuestState
from com.l2jmobius.gameserver.model.quest.jython import QuestJython as JQuest

BEAR_SKIN = 4259
ADENA = 57
CHANCE = 400000

class Quest (JQuest) :

 def __init__(self,id,name,descr):
     JQuest.__init__(self,id,name,descr)
     self.questItemIds = [BEAR_SKIN]

 def onEvent (self,event,st) :
     htmltext = event
     if event == "7078-02.htm" :
        st.setState(STARTED)
        st.set("cond","1")
        st.playSound("ItemSound.quest_accept")
     return htmltext

 def onTalk (Self,npc,st):
     npcId = npc.getNpcId()
     htmltext = "<html><body>I have nothing to say to you.</body></html>"
     id = st.getState()
     level = st.getPlayer().getLevel()
     cond = st.getInt("cond")
     if id == CREATED :
         if level>=20 :
             htmltext = "7078-01.htm"
         else:
             htmltext = "<html><body>This quest can only be taken by characters of level 20 and higher!</body></html>"
             st.exitQuest(1)
     elif cond==1 :
         if st.getQuestItemsCount(BEAR_SKIN)>=20 :
            htmltext = "7078-04.htm"
            st.giveItems(ADENA,3710)
            st.takeItems(BEAR_SKIN,-1)
            st.playSound("ItemSound.quest_finish")
            st.exitQuest(1)
         else :
            htmltext = "7078-03.htm"
     return htmltext

 def onKill (self,npc,player,isPet):
     st = player.getQuestState("341_HuntingForWildBeasts")
     if st :
       if st.getState() != STARTED : return
       npcId = npc.getNpcId()
       cond = st.getInt("cond")
       if cond==1 :
           st.dropQuestItems(BEAR_SKIN,1,20,CHANCE,1)
     return

QUEST       = Quest(341,"341_HuntingForWildBeasts","Hunting For Wild Beasts")
CREATED     = State('Start', QUEST)
STARTED     = State('Started', QUEST)

QUEST.setInitialState(CREATED)
QUEST.addStartNpc(7078)

QUEST.addTalkId(7078)

QUEST.addKillId(21)
QUEST.addKillId(203)
QUEST.addKillId(310)
QUEST.addKillId(335)