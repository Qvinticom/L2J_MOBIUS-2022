### ---------------------------------------------------------------------------
###  Create by Skeleton!!! (adapted for L2JLisvus by roko91)
### ---------------------------------------------------------------------------

import sys
from com.l2jmobius.gameserver.model.quest import State
from com.l2jmobius.gameserver.model.quest import QuestState
from com.l2jmobius.gameserver.model.quest.jython import QuestJython as JQuest

qn = "29_ChestCaughtWithABaitOfEarth"

# NPC List
Willie =8574
Anabel =7909
# ~~~
# Item List
SmallPurpleTreasureChest =6507
SmallGlassBox =7627
PlatedLeatherGloves =2455
# ~~~

class Quest (JQuest) :

    def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)

    def onEvent (self,event,st) :
        htmltext =event
        if event =="8574-04.htm" :
            st.set("cond","1")
            st.playSound("ItemSound.quest_accept")
        elif event=="8574-07.htm" :
            if st.getQuestItemsCount(SmallPurpleTreasureChest) :
                st.set("cond","2")
                st.takeItems(SmallPurpleTreasureChest,1)
                st.giveItems(SmallGlassBox,1)
            else :
                htmltext="8574-08.htm"
        elif event =="7909-02.htm" :
            if st.getQuestItemsCount(SmallGlassBox)==1 :
                st.takeItems(SmallGlassBox,-1)
                st.giveItems(PlatedLeatherGloves,1)
                st.unset("cond")
                st.setState(COMPLETED)
                st.playSound("ItemSound.quest_finish")
            else :
                htmltext ="7909-03.htm"
        return htmltext

    def onTalk (self,npc,st):
        htmltext = "<html><body>You are either not carrying out your quest or don't meet the criteria.</body></html>"
        npcId = npc.getNpcId()
        id = st.getState()
        if id==CREATED :
            st.setState(STARTED)
            st.set("cond","0")
        cond=st.getInt("cond")
        id = st.getState()
        if npcId ==Willie :
            if cond==0 and id==STARTED :
                if st.getPlayer().getLevel() >= 48 :
                    WilliesSpecialBait = st.getPlayer().getQuestState("52_WilliesSpecialBait")
                    if WilliesSpecialBait:
                        if WilliesSpecialBait.getState().getName() == 'Completed':
                            htmltext="8574-01.htm"
                        else :
                            htmltext="8574-02.htm"
                            st.exitQuest(1)
                    else :
                        htmltext="8574-03.htm"
                        st.exitQuest(1)
                else :
                   htmltext="8574-02.htm"
                   st.exitQuest(1) 
            elif cond==0 and id==COMPLETED :
                htmltext ="<html><body>This quest has already been completed.</body></html>"
            elif cond==1 :
                htmltext="8574-05.htm"
                if st.getQuestItemsCount(SmallPurpleTreasureChest)==0 :
                    htmltext ="8574-06.htm"
            elif cond==2 :
                htmltext="8574-09.htm"
        elif npcId ==Anabel :
            if cond==2 :
                htmltext="7909-01.htm"
        return htmltext

QUEST       = Quest(29,qn,"Chest Caught With A Bait Of Earth")
CREATED     = State('Start', QUEST)
STARTED     = State('Started', QUEST)
COMPLETED   = State('Completed', QUEST)

QUEST.setInitialState(CREATED)
QUEST.addStartNpc(Willie)
QUEST.addTalkId(Willie)
QUEST.addTalkId(Anabel)