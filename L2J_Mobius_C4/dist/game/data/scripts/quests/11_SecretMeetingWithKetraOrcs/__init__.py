#made by Emperorc (adapted for L2JLisvus by roko91)
import sys
from com.l2jmobius.gameserver.model.quest import State
from com.l2jmobius.gameserver.model.quest import QuestState
from com.l2jmobius.gameserver.model.quest.jython import QuestJython as JQuest

qn = "11_SecretMeetingWithKetraOrcs"

#NPCs
Cadmon = 8296
Leon = 8256
Wahkan = 8371

#Item
Box = 7231

class Quest (JQuest) :

 def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)

 def onEvent (self,event,st) :
     htmltext = event
     if event == "8296-03.htm" :
       if st.getPlayer().getLevel() >= 74 :
            st.set("cond","1")
            htmltext = "8296-03.htm"
            st.setState(STARTED)
            st.playSound("ItemSound.quest_accept")
       else :
            htmltext = "8296-02.htm"
            st.exitQuest(1)
     elif event == "8256-02.htm" :
         st.set("cond","2")
         htmltext = "8256-02.htm"
         st.giveItems(Box,1)
     elif event == "8371-02.htm" :
         htmltext = "8371-02.htm"
         st.takeItems(Box,-1)
         st.addExpAndSp(22787,0) #Despite what stratics may say, this is the correct reward for this quest.
         st.set("cond","0")
         st.set("onlyone","1")
         st.setState(COMPLETED)
         st.playSound("ItemSound.quest_finish")
     return htmltext

 def onTalk (self,npc,st):
     npcId = npc.getNpcId()
     htmltext = "<html><body>You are either not carrying out your quest or don't meet the criteria.</body></html>"
     cond = st.getInt("cond")
     onlyone = st.getInt("onlyone")
     if st.getState() == CREATED :
        st.set("cond","0")
        st.set("onlyone","0")
     if onlyone == 0 :
         if npcId == Cadmon :
             if cond == 0 :
                 htmltext = "8296-01.htm"
             elif cond == 1 :
                 htmltext = "8296-04.htm"
         if st.getState() == STARTED :
             if npcId == Leon :
                 if cond == 1 :
                     htmltext = "8256-01.htm"
                 elif cond == 2 :
                     htmltext = "8256-03.htm"
             elif npcId == Wahkan and cond == 2 :
                 htmltext = "8371-01.htm"
     return htmltext
     
QUEST       = Quest(11, qn, "Secret Meeting With Ketra Orcs")
CREATED     = State('Start',    QUEST)
STARTED     = State('Started',  QUEST)
COMPLETED   = State('Completed',QUEST)

QUEST.setInitialState(CREATED)
QUEST.addStartNpc(Cadmon)

QUEST.addTalkId(Cadmon)

QUEST.addTalkId(Leon)
QUEST.addTalkId(Wahkan)