# # # # # # # # # # #
# Chest AI implementation.
# Written by Fulminus
# # # # # # # # # # #
import sys
from com.l2jmobius.gameserver.ai import CtrlIntention
from com.l2jmobius.gameserver.model.quest import QuestState
from com.l2jmobius.gameserver.model.quest.jython import QuestJython as JQuest
from com.l2jmobius.util import Rnd;

SKILL_DELUXE_KEY = 2229

#Base chance for BOX to be opened
BASE_CHANCE = 100

# Percent to decrease base chance when grade of DELUXE key not match
LEVEL_DECREASE = 40

# Chance for a chest to actually be a BOX (as opposed to being a mimic).
IS_BOX = 40

class chests(JQuest) :

    # init function.  Add in here variables that you'd like to be inherited by subclasses (if any)
    def __init__(self,id,name,descr):
        # firstly, don't forget to call the parent constructor to prepare the event triggering
        # mechanisms etc.
        JQuest.__init__(self,id,name,descr)

        self.chests = [13100,13101,13102,13103,13104,13105,13106,13107,13108,13109, \
                       13110,13111,13112,13113,13114,13115,13116,13117,13118,13119, \
                       13120,13121,1801,1802,1803,1804,1805,1806,1807,1808,1809,1810, \
                       1671,1694,1717,1740,1763,1786,13213,13215,13217,13219,13221,13223, \
                       1811,1812,1813,1814,1815,1816,1817,1818,1819,1820,1821,1822]

        for i in self.chests :
            self.addSkillSeeId(i)
            self.addAttackId(i)

    def onSkillSee (self,npc,player,skill,targets,isPet):
        if npc not in targets :
            return
        npcId = npc.getNpcId()
        skillId = skill.getId()
        skillLevel= skill.getLevel()
        # check if the npc and skills used are valid for this script.  Exit if invalid.
        if npcId not in self.chests :
            return
        # if this has already been interacted, no further ai decisions are needed
        # if it's the first interaction, check if this is a box or mimic
        if not npc.isInteracted() :
            npc.setInteracted()
            if Rnd.get(100) < IS_BOX :
                # if it's a box, either it will be successfully openned by a proper key, or instantly disappear
                if skillId == SKILL_DELUXE_KEY :
                    # check the chance to open the box
                    keyLevelNeeded = int(npc.getLevel()/10)
                    levelDiff = keyLevelNeeded - skillLevel
                    if levelDiff < 0 :
                        levelDiff = levelDiff * (-1)
                    chance = BASE_CHANCE - levelDiff * LEVEL_DECREASE
                    # success, pretend-death with rewards:  npc.reduceCurrentHp(99999999, player)
                    if Rnd.get(100) < chance :
                        npc.setMustRewardExpSp(False)
                        npc.setSpecialDrop();
                        npc.reduceCurrentHp(99999999, player)
                        return
                # used a skill other than chest-key, or used a chest-key but failed to open: disappear with no rewards    
                npc.onDecay()
            else :
                attacker = player
                if npc.getAttackByList().contains(player.getPet()):
                    attacker = player.getPet()
                npc.setRunning()
                npc.addDamageHate(attacker,0,999)
                npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker)
        return

    def onAttack(self,npc,player,damage,isPet) :
        npcId = npc.getNpcId()
        # check if the npc and skills used are valid for this script.  Exit if invalid.
        if npcId not in self.chests :
            return
        # if this was a mimic, set the target, start the skills and become agro
        if not npc.isInteracted() :
            npc.setInteracted()
            if Rnd.get(100) < IS_BOX :
                npc.onDecay()
            else :  # if this weren't a box, upon interaction start the mimic behaviors...
                # todo: perhaps a self-buff (skill id 4245) with random chance goes here?
                attacker = player
                if isPet:
                    attacker = player.getPet()
                npc.setRunning()
                npc.addDamageHate(attacker,0,(damage*100)/(npc.getLevel()+7))
                npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker)
        return

# now call the constructor (starts up the ai)
QUEST = chests(-1,"chests","ai")