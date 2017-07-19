# author DnR
import sys

from com.l2jmobius.gameserver                    import SevenSigns
from com.l2jmobius.gameserver.datatables         import MapRegionTable
from com.l2jmobius.gameserver.model.quest        import State
from com.l2jmobius.gameserver.model.quest        import QuestState
from com.l2jmobius.gameserver.model.quest.jython import QuestJython as JQuest

qn = "5000_GatekeeperSpirit"

class Quest (JQuest) :

 def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)

 def onAdvEvent (self,event,npc, player) :
    playerCabal = SevenSigns.getInstance().getPlayerCabal(player)
    sealAvariceOwner = SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_AVARICE)
    compWinner = SevenSigns.getInstance().getCabalHighestScore()
    if playerCabal == sealAvariceOwner and playerCabal == compWinner :
       if sealAvariceOwner == SevenSigns.CABAL_DAWN :
          zone = GrandBossManager.getInstance().getZone(184397, -11957, -5498)
          if zone :
             zone.allowPlayerEntry(player,30000)
          player.teleToLocation(184397, -11957, -5498)
       elif sealAvariceOwner == SevenSigns.CABAL_DUSK :
          zone = GrandBossManager.getInstance().getZone(185551, -9298, -5498)
          if zone :
             zone.allowPlayerEntry(player,30000)
          player.teleToLocation(185551, -9298, -5498)
    return

 def onFirstTalk (self,npc,player) :
    htmltext = "default.htm"
    playerCabal = SevenSigns.getInstance().getPlayerCabal(player)
    sealAvariceOwner = SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_AVARICE)
    compWinner = SevenSigns.getInstance().getCabalHighestScore()
    if playerCabal == sealAvariceOwner and playerCabal == compWinner :
       if sealAvariceOwner == SevenSigns.CABAL_DAWN :
          htmltext = "dawn.htm"
       elif sealAvariceOwner == SevenSigns.CABAL_DUSK :
          htmltext = "dusk.htm"
    return htmltext

 # Raid Boss protection
 def onAggroRangeEnter(self,npc,player,isPet) :
    if not player.isGM() :
       bossId = SevenSigns.getInstance().getBossId()
       if npc.getNpcId() != bossId :
          player.teleToLocation(MapRegionTable.TeleportWhereType.Town)
    return

QUEST = Quest(5000,qn,"Teleports")

QUEST.addFirstTalkId(8111)
QUEST.addAggroRangeEnterId(10283)
QUEST.addAggroRangeEnterId(10286)