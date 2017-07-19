import sys
from com.l2jmobius.gameserver.datatables         import DoorTable
from com.l2jmobius.gameserver.instancemanager    import GrandBossManager
from com.l2jmobius.gameserver.instancemanager    import QuestManager
from com.l2jmobius.gameserver.model.quest        import State
from com.l2jmobius.gameserver.model.quest        import QuestState
from com.l2jmobius.gameserver.model.quest.jython import QuestJython as JQuest
from com.l2jmobius.util                          import Rnd

NPCs = [
    8384,  #Gatekeeper of Fire Dragon : Opening some doors
    8385,  #Heart of Volcano : Teleport into Lair of Valakas
    8540,  #Watcher of Valakas Klein : Teleport into Hall of Flames
    8686,  #Gatekeeper of Fire Dragon : Opens doors to Heart of Volcano
    8687,  #Gatekeeper of Fire Dragon : Opens doors to Heart of Volcano
    8759,  #Teleportation Cubic : Teleport out of Lair of Valakas
    12250, #Heart of Warding : Teleport into Lair of Antharas
    12324  #Teleport Cube : Teleport out of Lair of Antharas
    ]

FLOATING_STONE = 7267

class Quest (JQuest) :

 def __init__(self,id,name,descr):
   self.antharasAI = QuestManager.getInstance().getQuest("antharas")
   self.valakasAI = QuestManager.getInstance().getQuest("valakas")
   JQuest.__init__(self,id,name,descr)

 def onTalk (self,npc,st):
   npcId = npc.getNpcId()
   if npcId == 8540 :
     htmltext = "1.htm"
     if st.getQuestItemsCount(FLOATING_STONE) > 0:
       st.takeItems(FLOATING_STONE,1)
       st.getPlayer().teleToLocation(183813,-115157,-3303)
       return
     else:
       st.exitQuest(1)
   elif npcId == 12324 : #antharas teleport cube
       x = 79800 + Rnd.get(600)
       y = 151200 + Rnd.get(1100)
       st.getPlayer().teleToLocation(x,y,-3534)
       st.exitQuest(1)
       return
   elif npcId == 12250 : #heart of warding
     htmltext = "12250-01.htm"
     if self.antharasAI :
        status = GrandBossManager.getInstance().getBossStatus(12211)
        if status == 0 or status == 1 : #If entrance to see Antharas is unlocked (he is Dormant or Waiting)
           if st.getQuestItemsCount(3865) > 0 :
              st.takeItems(3865,1)
              zone = GrandBossManager.getInstance().getZone(179700,113800,-7709)
              if zone :
                 zone.allowPlayerEntry(st.getPlayer(),30000)
              x = 179700 + Rnd.get(700)
              y = 113800 + Rnd.get(2100)
              st.getPlayer().teleToLocation(x,y,-7709)
              if status == 0 :
                 antharas = GrandBossManager.getInstance().getBoss(12211)
                 self.antharasAI.startQuestTimer("waiting",1800000, antharas, None)
                 GrandBossManager.getInstance().setBossStatus(12211,1)
              st.exitQuest(1)
              return
           else :
              htmltext = "12250-03.htm"
        elif status == 2 :
           htmltext = "12250-02.htm"
     st.exitQuest(1)
   elif npcId == 8385 : #heart of volcano
     htmltext = "8385-01.htm"
     if self.valakasAI :
        status = GrandBossManager.getInstance().getBossStatus(12899)
        if status == 0 or status == 1 : #If entrance to see Valakas is unlocked (he is Dormant or Waiting)
           zone = GrandBossManager.getInstance().getZone(212852,-114842,-1632)
           if zone :
              zone.allowPlayerEntry(st.getPlayer(),30000)
           x = 204328 + Rnd.get(600)
           y = -111874 + Rnd.get(600)
           st.getPlayer().teleToLocation(x,y,70)
           if status == 0 :
              valakas = GrandBossManager.getInstance().getBoss(12899)
              self.valakasAI.startQuestTimer("1001",1800000, valakas, None)
              GrandBossManager.getInstance().setBossStatus(12899,1)
           st.exitQuest(1)
           return
        elif status == 2 :
           htmltext = "8385-02.htm"
   elif npcId == 8384 : #Gatekeeper of Fire Dragon
     DoorTable.getInstance().getDoor(24210004).openMe()
     return
   elif npcId == 8686 : #Gatekeeper of Fire Dragon
     DoorTable.getInstance().getDoor(24210006).openMe()
     return
   elif npcId == 8687 : #Gatekeeper of Fire Dragon
     DoorTable.getInstance().getDoor(24210005).openMe()
     return
   elif npcId == 8759 : #valakas teleport cube
     x = 150037 + Rnd.get(500)
     y = -57720 + Rnd.get(500)
     st.getPlayer().teleToLocation(x,y,-2976)
     st.exitQuest(1)
     return
   return htmltext

QUEST       = Quest(6000,"6000_GrandBossTeleporters","Teleports")
CREATED     = State('Start',QUEST)

QUEST.setInitialState(CREATED)

QUEST.addStartNpc(8540)
QUEST.addStartNpc(8759)
QUEST.addStartNpc(12250)
QUEST.addStartNpc(12324)

for npcid in NPCs :
   QUEST.addTalkId(npcid)