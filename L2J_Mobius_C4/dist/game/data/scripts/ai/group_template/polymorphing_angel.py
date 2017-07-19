import sys
from com.l2jmobius.gameserver.ai import CtrlIntention
from com.l2jmobius.gameserver.model.quest.jython import QuestJython as JQuest

# Angel spawns...when one of the angels in the keys dies, the other angel will spawn.

class polymorphing_angel(JQuest) :

    # init function.  Add in here variables that you'd like to be inherited by subclasses (if any)
    def __init__(self,id,name,descr):
        self.AngelSpawns ={
                830:859,
                1067:1068,
                1062:1063,
                831:860,
                1070:1071
                }
        # finally, don't forget to call the parent constructor to prepare the event triggering
        # mechanisms etc.
        JQuest.__init__(self,id,name,descr)

    def onKill (self,npc,player,isPet):
        npcId = npc.getNpcId()
        if self.AngelSpawns.has_key(npcId) :
            newNpc = self.addSpawn(self.AngelSpawns[npcId],npc)
            killer = player
            if isPet :
                killer = player.getPet()
            newNpc.setRunning()
            newNpc.addDamageHate(killer,0,99999)
            newNpc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, killer)
        return 

# now call the constructor (starts up the ai)
QUEST = polymorphing_angel(-1,"polymorphing_angel","ai")

for i in QUEST.AngelSpawns.keys() :
    QUEST.addKillId(i)