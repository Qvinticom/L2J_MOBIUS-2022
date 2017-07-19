import sys

from com.l2jmobius.gameserver.model.quest        import State
from com.l2jmobius.gameserver.model.quest        import QuestState
from com.l2jmobius.gameserver.model.quest.jython import QuestJython as JQuest


NPC=[7006,7059,7080,7134,7146,7177,7233,7256,7320,7540,7576,7836,7848,7878,7899,8275,8320]

class Quest (JQuest) :

 def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)

 def onTalk (Self,npc,st):
    if st.getPlayer().isNoble() == 1 :
      htmltext="noble.htm"
    else :
      htmltext="nobleteleporter-no.htm"
    st.exitQuest(1)
    return htmltext

QUEST       = Quest(2000,"2000_NoblesseTeleport","Teleports")
CREATED     = State('Start', QUEST)

QUEST.setInitialState(CREATED)

for item in NPC:
   QUEST.addStartNpc(item)
   QUEST.addTalkId(item)