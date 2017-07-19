import sys
from com.l2jmobius.gameserver.ai import CtrlIntention
from com.l2jmobius.gameserver.model.quest.jython import QuestJython as JQuest
from com.l2jmobius.util import Rnd

class polymorphing_onAttack(JQuest) :
    # init function.  Add in here variables that you'd like to be inherited by subclasses (if any)
    def __init__(self,id,name,descr):
        self.MobSpawns ={
            1258: [1259, 100, 100], #Fallen Orc Shaman -> Sharp Talon Tiger
            1261: [1262, 100, 20], #Ol Mahum Transcender 1st stage
            1262: [1263, 100, 10], #Ol Mahum Transcender 2st stage
            1263: [1264, 100, 5], #Ol Mahum Transcender 3rd stage
            1265: [1271, 100, 33], #Cave Ant Larva -> Cave Ant
            1266: [1269, 100, 100], #Cave Ant Larva -> Cave Ant (always polymorphs)
            1267: [1270, 100, 100], #Cave Ant Larva -> Cave Ant Soldier (always polymorphs)
            1271: [1272, 66, 10], #Cave Ant -> Cave Ant Soldier
            1272: [1273, 33, 5], #Cave Ant Soldier -> Cave Noble Ant
            1521: [1522, 100, 30], #Claws of Splendor -> Claws of Splendor Panther
            1524: [1525, 100, 30], #Blade of Splendor -> Blade of Splendor 2nd
            1527: [1528, 100, 30], #Anger of Splendor -> Anger of Splendor Panther
            1533: [1534, 100, 30], #Alliance of Splendor -> Alliance of Splendor Panther
            1537: [1538, 100, 30] #Fang of Splendor -> Fang of Splendor Panther
            }
        # finally, don't forget to call the parent constructor to prepare the event triggering
        # mechanisms etc.
        JQuest.__init__(self,id,name,descr)

    def onAttack(self,npc,player,damage,isPet) :
        if npc.isVisible() and not npc.isDead() :
            newId,hpRate,chance = self.MobSpawns[npc.getNpcId()]
            if npc.getCurrentHp() <= (npc.getMaxHp() * hpRate/100.0) and Rnd.get(100) < chance :
                # It's the only way for 1st level mob to respawn after it's deleted
                npc.doDie(npc)
                npc.deleteMe()
                newNpc = self.addSpawn(newId,npc)
                killer = player
                if isPet :
                    killer = player.getPet()
                newNpc.setRunning()
                newNpc.addDamageHate(killer,0,99999)
                newNpc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, killer)
        return 

# now call the constructor (starts up the ai)
QUEST = polymorphing_onAttack(-1,"polymorphing_onAttack","ai")

for i in QUEST.MobSpawns.keys() :
    QUEST.addAttackId(i)