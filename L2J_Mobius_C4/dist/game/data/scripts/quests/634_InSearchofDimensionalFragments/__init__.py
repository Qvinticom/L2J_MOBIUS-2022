import sys
from com.l2jmobius import Config
from com.l2jmobius.gameserver.model.quest import State
from com.l2jmobius.gameserver.model.quest import QuestState
from com.l2jmobius.gameserver.model.quest.jython import QuestJython as JQuest

DIMENSION_FRAGMENT_ID = 7079

class Quest (JQuest) :

 def __init__(self,id,name,descr):
     JQuest.__init__(self,id,name,descr)
     self.questItemIds = [DIMENSION_FRAGMENT_ID]

 def onEvent (self,event,st) :
    htmltext = event
    if event == "2a.htm" :
      st.setState(STARTED)
      st.playSound("ItemSound.quest_accept")
      st.set("cond","1")
    elif event == "5.htm" :
      st.playSound("ItemSound.quest_finish")
      st.exitQuest(1)
    return htmltext

 def onTalk (Self,npc,st):
   npcId = npc.getNpcId()
   htmltext = "<html><body>I have nothing to say to you.</body></html>"
   id = st.getState()
   if id == CREATED :
      if st.getPlayer().getLevel() < 20 :
         htmltext="1.htm"
         st.exitQuest(1)
      else :
         htmltext="2.htm"
   elif id == STARTED :
      htmltext = "4.htm"
   return htmltext

 def onKill (self,npc,player,isPet):
    partyMember = self.getRandomPartyMemberState(player, STARTED)
    if partyMember :
       st = partyMember.getQuestState("634_InSearchofDimensionalFragments")
       if st :
          if st.getState() == STARTED :
             itemMultiplier,chance = divmod(80*Config.RATE_DROP_QUEST,1000)
             if st.getRandom(1000) < chance :
                itemMultiplier += 1
             numItems = int(itemMultiplier * (npc.getLevel() * 0.15 +1.6))
             if numItems > 0 :
                st.giveItems(DIMENSION_FRAGMENT_ID,numItems)
    return


QUEST       = Quest(634, "634_InSearchofDimensionalFragments", "In Search of Dimensional Fragments")
CREATED     = State('Start', QUEST)
STARTED     = State('Started', QUEST)
COMPLETED   = State('Completed', QUEST)


QUEST.setInitialState(CREATED)

for npcId in range(8494,8508):
  QUEST.addTalkId(npcId)
  QUEST.addStartNpc(npcId)

for mobs in range(1208,1256):
  QUEST.addKillId(mobs)