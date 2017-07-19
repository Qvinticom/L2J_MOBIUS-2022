# Growth-capable mobs: Polymorphing upon successful feeding.
# Written by Fulminus
# # # # # # # # # # #
import sys
from com.l2jmobius.gameserver.ai import CtrlIntention
from com.l2jmobius.gameserver.datatables import NpcTable
from com.l2jmobius.gameserver.idfactory import IdFactory
from com.l2jmobius.gameserver.model.actor.instance import L2TamedBeastInstance
from com.l2jmobius.gameserver.model.quest.jython import QuestJython as JQuest
from com.l2jmobius.gameserver.network.serverpackets import CreatureSay
from com.l2jmobius.gameserver.network.serverpackets import SocialAction
from com.l2jmobius.util import Rnd;

GOLDEN_SPICE = 6643
CRYSTAL_SPICE = 6644
SKILL_GOLDEN_SPICE = 2188
SKILL_CRYSTAL_SPICE = 2189
foodSkill = {GOLDEN_SPICE:SKILL_GOLDEN_SPICE, CRYSTAL_SPICE:SKILL_CRYSTAL_SPICE}

class feedable_beasts(JQuest) :

    # init function.  Add in here variables that you'd like to be inherited by subclasses (if any)
    def __init__(self,id,name,descr):
        # firstly, don't forget to call the parent constructor to prepare the event triggering
        # mechanisms etc.
        JQuest.__init__(self,id,name,descr)
        # DEFINE MEMBER VARIABLES FOR THIS AI
        # all mobs that can eat...
        self.tamedBeasts = range(12783,12789)
        self.feedableBeasts = range(1451,1508)+ self.tamedBeasts
        # all mobs that grow by eating
        # mobId: current_growth_level, {food: [list of possible mobs[possible sublist of tamed pets]]}, chance of growth
        self.growthCapableMobs = {
            # Alpen Kookabura
            1451: [0,{GOLDEN_SPICE:[1452,1453, 1454, 1455],CRYSTAL_SPICE:[1456,1457, 1458, 1459]},100],
            1452: [1,{GOLDEN_SPICE:[1460,1462],CRYSTAL_SPICE:[]},40],
            1453: [1,{GOLDEN_SPICE:[1461,1463],CRYSTAL_SPICE:[]},40],
            1454: [1,{GOLDEN_SPICE:[1460,1462],CRYSTAL_SPICE:[]},40],
            1455: [1,{GOLDEN_SPICE:[1461,1463],CRYSTAL_SPICE:[]},40],
            1456: [1,{GOLDEN_SPICE:[],CRYSTAL_SPICE:[1464,1466]},40],
            1457: [1,{GOLDEN_SPICE:[],CRYSTAL_SPICE:[1465,1467]},40],
            1458: [1,{GOLDEN_SPICE:[],CRYSTAL_SPICE:[1464,1466]},40],
            1459: [1,{GOLDEN_SPICE:[],CRYSTAL_SPICE:[1465,1467]},40],
            1460: [2,{GOLDEN_SPICE:[[1468,1469],[12783,12784]],CRYSTAL_SPICE:[]},25],
            1461: [2,{GOLDEN_SPICE:[[1468,1469],[12783,12784]],CRYSTAL_SPICE:[]},25],
            1462: [2,{GOLDEN_SPICE:[[1468,1469],[12783,12784]],CRYSTAL_SPICE:[]},25],
            1463: [2,{GOLDEN_SPICE:[[1468,1469],[12783,12784]],CRYSTAL_SPICE:[]},25],
            1464: [2,{GOLDEN_SPICE:[],CRYSTAL_SPICE:[[1468,1469],[12783,12784]]},25],
            1465: [2,{GOLDEN_SPICE:[],CRYSTAL_SPICE:[[1468,1469],[12783,12784]]},25],
            1466: [2,{GOLDEN_SPICE:[],CRYSTAL_SPICE:[[1468,1469],[12783,12784]]},25],
            1467: [2,{GOLDEN_SPICE:[],CRYSTAL_SPICE:[[1468,1469],[12783,12784]]},25],
            # Alpen Buffalo
            1470: [0,{GOLDEN_SPICE:[1471,1472, 1473, 1474],CRYSTAL_SPICE:[1475,1476, 1477, 1478]},100],
            1471: [1,{GOLDEN_SPICE:[1479,1481],CRYSTAL_SPICE:[]},40],
            1472: [1,{GOLDEN_SPICE:[1481,1482],CRYSTAL_SPICE:[]},40],
            1473: [1,{GOLDEN_SPICE:[1479,1481],CRYSTAL_SPICE:[]},40],
            1474: [1,{GOLDEN_SPICE:[1480,1482],CRYSTAL_SPICE:[]},40],
            1475: [1,{GOLDEN_SPICE:[],CRYSTAL_SPICE:[1483,1485]},40],
            1476: [1,{GOLDEN_SPICE:[],CRYSTAL_SPICE:[1484,1486]},40],
            1477: [1,{GOLDEN_SPICE:[],CRYSTAL_SPICE:[1483,1485]},40],
            1478: [1,{GOLDEN_SPICE:[],CRYSTAL_SPICE:[1484,1486]},40],
            1479: [2,{GOLDEN_SPICE:[[1487,1488],[12785,12786]],CRYSTAL_SPICE:[]},25],
            1480: [2,{GOLDEN_SPICE:[[1487,1488],[12785,12786]],CRYSTAL_SPICE:[]},25],
            1481: [2,{GOLDEN_SPICE:[[1487,1488],[12785,12786]],CRYSTAL_SPICE:[]},25],
            1482: [2,{GOLDEN_SPICE:[[1487,1488],[12785,12786]],CRYSTAL_SPICE:[]},25],
            1483: [2,{GOLDEN_SPICE:[],CRYSTAL_SPICE:[[1487,1488],[12785,12786]]},25],
            1484: [2,{GOLDEN_SPICE:[],CRYSTAL_SPICE:[[1487,1488],[12785,12786]]},25],
            1485: [2,{GOLDEN_SPICE:[],CRYSTAL_SPICE:[[1487,1488],[12785,12786]]},25],
            1486: [2,{GOLDEN_SPICE:[],CRYSTAL_SPICE:[[1487,1488],[12785,12786]]},25],
            # Alpen Cougar
            1489: [0,{GOLDEN_SPICE:[1490,1491, 1492, 1493],CRYSTAL_SPICE:[1494,1495, 1496, 1497]},100],
            1490: [1,{GOLDEN_SPICE:[1498,1500],CRYSTAL_SPICE:[]},40],
            1491: [1,{GOLDEN_SPICE:[1499,1501],CRYSTAL_SPICE:[]},40],
            1492: [1,{GOLDEN_SPICE:[1498,1500],CRYSTAL_SPICE:[]},40],
            1493: [1,{GOLDEN_SPICE:[1499,1501],CRYSTAL_SPICE:[]},40],
            1494: [1,{GOLDEN_SPICE:[],CRYSTAL_SPICE:[1502,1504]},40],
            1495: [1,{GOLDEN_SPICE:[],CRYSTAL_SPICE:[1503,1505]},40],
            1496: [1,{GOLDEN_SPICE:[],CRYSTAL_SPICE:[1502,1504]},40],
            1497: [1,{GOLDEN_SPICE:[],CRYSTAL_SPICE:[1503,1505]},40],
            1498: [2,{GOLDEN_SPICE:[[1506,1507],[12787,12788]],CRYSTAL_SPICE:[]},25],
            1499: [2,{GOLDEN_SPICE:[[1506,1507],[12787,12788]],CRYSTAL_SPICE:[]},25],
            1500: [2,{GOLDEN_SPICE:[[1506,1507],[12787,12788]],CRYSTAL_SPICE:[]},25],
            1501: [2,{GOLDEN_SPICE:[[1506,1507],[12787,12788]],CRYSTAL_SPICE:[]},25],
            1502: [2,{GOLDEN_SPICE:[],CRYSTAL_SPICE:[[1506,1507],[12787,12788]]},25],
            1503: [2,{GOLDEN_SPICE:[],CRYSTAL_SPICE:[[1506,1507],[12787,12788]]},25],
            1504: [2,{GOLDEN_SPICE:[],CRYSTAL_SPICE:[[1506,1507],[12787,12788]]},25],
            1505: [2,{GOLDEN_SPICE:[],CRYSTAL_SPICE:[[1506,1507],[12787,12788]]},25]
            }
        self.Text = [["What did you just do to me?","You want to tame me, huh?","Do not give me this. Perhaps you will be in danger.","Bah bah. What is this unpalatable thing?","My belly has been complaining.  This hit the spot.","What is this? Can I eat it?","You don't need to worry about me.","Delicious food, thanks.","I am starting to like you!","Gulp"], 
                    ["I do not think you have given up on the idea of taming me.","That is just food to me.  Perhaps I can eat your hand too.","Will eating this make me fat? Ha ha","Why do you always feed me?","Do not trust me.  I may betray you"], 
                    ["Destroy","Look what you have done!","Strange feeling...!  Evil intentions grow in my heart...!","It is happenning!","This is sad...Good is sad...!"]]

        self.feedInfo = {} # : feedInfo[objectId of mob] = objectId of player feeding it

        for i in self.feedableBeasts :
            self.addSkillSeeId(i)
            self.addKillId(i)

    def spawnNext(self, npc, growthLevel,player,food) :
        st = player.getQuestState("feedable_beasts")
        npcId = npc.getNpcId()
        nextNpcId = 0
        # find the next mob to spawn, based on the current npcId, growthlevel, and food.
        if growthLevel == 2:
            rand = Rnd.get(2)
            # if tamed, the mob that will spawn depends on the class type (fighter/mage) of the player!
            if rand == 1 :
                if player.getClassId().isMage() :
                    nextNpcId = self.growthCapableMobs[npcId][1][food][1][1]
                else :
                    nextNpcId = self.growthCapableMobs[npcId][1][food][1][0]
            # if not tamed, there is a small chance that have "mad cow" disease.
            # that is a stronger-than-normal animal that attacks its feeder
            else :
                if Rnd.get(5) == 0 :
                    nextNpcId = self.growthCapableMobs[npcId][1][food][0][1]
                else :
                    nextNpcId = self.growthCapableMobs[npcId][1][food][0][0]
        # all other levels of growth are straight-forward
        else :            
            nextNpcId = self.growthCapableMobs[npcId][1][food][Rnd.get(len(self.growthCapableMobs[npcId][1][food]))]
        # remove the feedinfo of the mob that got despawned, if any
        if self.feedInfo.has_key(npc.getObjectId()) :
            if self.feedInfo[npc.getObjectId()] == player.getObjectId() :
                self.feedInfo.pop(npc.getObjectId())
        # despawn the old mob
        if self.growthCapableMobs[npcId][0] == 0 :
            npc.onDecay()
        else :
            npc.deleteMe()
        # if this is finally a trained mob, then despawn any other trained mobs that the
        # player might have and initialize the Tamed Beast.
        if nextNpcId in self.tamedBeasts :
            oldTrained = player.getTrainedBeast()
            if oldTrained :
                oldTrained.doDespawn()
            template = NpcTable.getInstance().getTemplate(nextNpcId)
            nextNpc = L2TamedBeastInstance(IdFactory.getInstance().getNextId(), template, player, foodSkill[food], npc.getX(), npc.getY(), npc.getZ())
            nextNpc.setRunning()
            objectId = nextNpc.getObjectId()
            st = player.getQuestState("20_BringUpWithLove")
            if st :
                if Rnd.get(100) <= 5 and st.getQuestItemsCount(7185) == 0 :
                    st.giveItems(7185,1) #if player has quest 20 going, give quest item
                    st.set("cond","2")   #it's easier to hardcode it in here than to try and repeat this stuff in the quest
            # also, perform a rare random chat
            rand = Rnd.get(20)
            if rand > 4 : pass
            elif rand == 0 : npc.broadcastPacket(CreatureSay(objectId,0,nextNpc.getName(), player.getName()+", will you show me your hideaway?"))
            elif rand == 1 : npc.broadcastPacket(CreatureSay(objectId,0,nextNpc.getName(), player.getName()+", whenever I look at spice, I think about you."))
            elif rand == 2 : npc.broadcastPacket(CreatureSay(objectId,0,nextNpc.getName(), player.getName()+", you do not need to return to the village.  I will give you strength"))
            elif rand == 3 : npc.broadcastPacket(CreatureSay(objectId,0,nextNpc.getName(), "Thanks, "+player.getName()+".  I hope I can help you"))
            elif rand == 4 : npc.broadcastPacket(CreatureSay(objectId,0,nextNpc.getName(), player.getName()+", what can I do to help you?"))
        # if not trained, the newly spawned mob will automatically be agro against its feeder
        # (what happened to "never bite the hand that feeds you" anyway?!)
        else :
            # spawn the new mob
            nextNpc = self.addSpawn(nextNpcId,npc)
            # register the player in the feedinfo for the mob that just spawned
            self.feedInfo[nextNpc.getObjectId()] = player.getObjectId()
            nextNpc.setRunning()
            nextNpc.addDamageHate(player,0,99999)
            nextNpc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player)

    def onSkillSee (self,npc,player,skill,targets,isPet):
        if npc not in targets :
            return
        # gather some values on local variables
        npcId = npc.getNpcId()
        skillId = skill.getId()
        # check if the npc and skills used are valid for this script.  Exit if invalid.
        if npcId not in self.feedableBeasts : return
        if skillId not in [SKILL_GOLDEN_SPICE,SKILL_CRYSTAL_SPICE] :
            return
        # first gather some values on local variables
        objectId = npc.getObjectId()
        growthLevel = 3  # if a mob is in feedableBeasts but not in growthCapableMobs, then it's at max growth (3)
        if self.growthCapableMobs.has_key(npcId) :
            growthLevel = self.growthCapableMobs[npcId][0]
        # prevent exploit which allows 2 players to simultaneously raise the same 0-growth beast
        # If the mob is at 0th level (when it still listens to all feeders) lock it to the first feeder!       
        if (growthLevel==0) and self.feedInfo.has_key(objectId):
            return
        else :
            self.feedInfo[objectId] = player.getObjectId()
        food = 0
        if skillId == SKILL_GOLDEN_SPICE :
            food = GOLDEN_SPICE
        elif skillId == SKILL_CRYSTAL_SPICE :
            food = CRYSTAL_SPICE
        # display the social action of the beast eating the food.
        npc.broadcastPacket(SocialAction(objectId,2))
        # if this pet can't grow, it's all done.
        if npcId in self.growthCapableMobs.keys() :
            # do nothing if this mob doesn't eat the specified food (food gets consumed but has no effect).
            if len(self.growthCapableMobs[npcId][1][food]) == 0 :
                return
            # rare random talk...
            if Rnd.get(20) == 0 :
                npc.broadcastPacket(CreatureSay(objectId,0,npc.getName(),self.Text[growthLevel][Rnd.get(len(self.Text[growthLevel]))]))
            if growthLevel > 0 :
                # check if this is the same player as the one who raised it from growth 0.
                # if no, then do not allow a chance to raise the pet (food gets consumed but has no effect).
                if self.feedInfo[objectId] != player.getObjectId() : return
            # Polymorph the mob, with a certain chance, given its current growth level
            if Rnd.get(100) < self.growthCapableMobs[npcId][2] :
                self.spawnNext(npc, growthLevel,player,food)
        elif npcId in self.tamedBeasts :
            if skillId == npc.getFoodType() :
                npc.onReceiveFood()
                mytext = ["Refills! Yeah!","I am such a gluttonous beast, it is embarrassing! Ha ha",
                          "Your cooperative feeling has been getting better and better.",
                          "I will help you!",
                          "The weather is really good.  Wanna go for a picnic?",
                          "I really like you! This is tasty...",
                          "If you do not have to leave this place, then I can help you.",
                          "What can I help you with?",
                          "I am not here only for food!",
                          "Yam, yam, yam, yam, yam!"]
                npc.broadcastPacket(CreatureSay(objectId,0,npc.getName(),mytext[Rnd.get(len(mytext))]))
        return

    def onKill (self,npc,player,isPet):
        # remove the feedinfo of the mob that got killed, if any
        if self.feedInfo.has_key(npc.getObjectId()) :
            self.feedInfo.pop(npc.getObjectId())

# now call the constructor (starts up the ai)
QUEST		= feedable_beasts(-1,"feedable_beasts","ai")