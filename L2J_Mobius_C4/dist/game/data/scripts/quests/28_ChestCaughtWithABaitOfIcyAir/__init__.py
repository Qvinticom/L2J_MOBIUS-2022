### ---------------------------------------------------------------------------
###  Create by Skeleton!!! (adapted for L2JLisvus by roko91)
### ---------------------------------------------------------------------------

import sys
from com.l2jmobius.gameserver.model.quest import State
from com.l2jmobius.gameserver.model.quest import QuestState
from com.l2jmobius.gameserver.model.quest.jython import QuestJython as JQuest

qn = "28_ChestCaughtWithABaitOfIcyAir"

# NPC List
OFulle=8572
Kiki=8442
# ~~~
# Item List
BigYellowTreasureChest=6503
KikisLetter=7626
ElvenRing=881
# ~~~

class Quest (JQuest) :

    def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)

    def onEvent (self,event,st) :
        htmltext=event
        if event=="8572-04.htm" :
            st.set("cond","1")
            st.playSound("ItemSound.quest_accept")
        elif event=="8572-07.htm" :
            if st.getQuestItemsCount(BigYellowTreasureChest) :
                st.set("cond","2")
                st.takeItems(BigYellowTreasureChest,1)
                st.giveItems(KikisLetter,1)
            else :
                htmltext="8572-08.htm"
        elif event=="8442-02.htm" :
            if st.getQuestItemsCount(KikisLetter)==1 :
                htmltext="8442-02.htm"
                st.takeItems(KikisLetter,-1)
                st.giveItems(ElvenRing,1)
                st.unset("cond")
                st.setState(COMPLETED)
                st.playSound("ItemSound.quest_finish")
            else :
                htmltext="8442-03.htm"
        return htmltext

    def onTalk (self,npc,st):
        htmltext = "<html><body>You are either not carrying out your quest or don't meet the criteria.</body></html>"
        npcId = npc.getNpcId()
        id=st.getState()
        if id==CREATED :
            st.setState(STARTED)
            st.set("cond","0")
        cond=st.getInt("cond")
        id = st.getState()
        if npcId==OFulle :
            if cond==0 and id==STARTED:
                if st.getPlayer().getLevel() >= 36 :
                    OFullesSpecialBait= st.getPlayer().getQuestState("51_OFullesSpecialBait")
                    if OFullesSpecialBait :
                        if OFullesSpecialBait.getState().getName() == 'Completed':
                            htmltext="8572-01.htm"
                        else :
                            htmltext="8572-02.htm"
                            st.exitQuest(1)
                    else :
                        htmltext="8572-03.htm"
                        st.exitQuest(1)
                else :
                    htmltext="8572-02.htm"
            elif cond==1 :
                htmltext="8572-05.htm"
                if st.getQuestItemsCount(BigYellowTreasureChest)==0 :
                    htmltext="8572-06.htm"
            elif cond==2 :
                htmltext="8572-09.htm"
            elif cond==0 and id==COMPLETED :
                htmltext="<html><body>This quest has already been completed.</body></html>"
        elif npcId==Kiki :
            if cond==2 :
                htmltext="8442-01.htm"
        return htmltext

QUEST      = Quest(28,qn,"Chest Caught With A Bait Of Icy Air")
CREATED    = State('Start', QUEST)
STARTED    = State('Started', QUEST)
COMPLETED  = State('Completed', QUEST)

QUEST.setInitialState(CREATED)
QUEST.addStartNpc(OFulle)
QUEST.addTalkId(OFulle)
QUEST.addTalkId(Kiki)