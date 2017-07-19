import sys
from com.l2jmobius.gameserver.ai import CtrlIntention
from com.l2jmobius.gameserver.datatables import SkillTable
from com.l2jmobius.gameserver.model.quest import Quest as JQuest
from com.l2jmobius.util import Rnd

class trees(JQuest) :

    def __init__(self,id,name,descr):
        JQuest.__init__(self,id,name,descr)

    def onKill (self,npc,player,isPet):
        npcId = npc.getNpcId()
        if npcId in range(5185,5189) :
           for x in xrange(20):
               newNpc = self.addSpawn(5189,npc.getX(),npc.getY(),npc.getZ(),0,False,30000)
               killer = player
               if isPet :
                   killer = player.getPet()
               newNpc.setRunning()
               newNpc.addDamageHate(killer,0,999)
               newNpc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, killer)
               if Rnd.get(2) :
                  skill = SkillTable.getInstance().getInfo(4243,1)
                  if skill != None and killer:
                     skill.getEffects(newNpc, killer)
        return

QUEST = trees(-1,"fairy_trees","ai")

for i in range(5185,5189):
    QUEST.addKillId(i)