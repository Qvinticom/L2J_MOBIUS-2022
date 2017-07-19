# Created by Emperorc
# Finished by Kerberos_20 10/23/07
import sys
from com.l2jmobius.gameserver.model.quest import State
from com.l2jmobius.gameserver.model.quest import QuestState
from com.l2jmobius.gameserver.model.quest.jython import QuestJython as JQuest
from com.l2jmobius.gameserver.datatables import SkillTable
from com.l2jmobius.gameserver.network.serverpackets import WareHouseWithdrawalList
from com.l2jmobius.gameserver.network.serverpackets import ActionFailed

qn = "6050_KetraOrcSupport"

Kadun = 8370 #Hierarch
Wahkan= 8371 #Messenger
Asefa = 8372 #Soul Guide
Atan  = 8373 #Grocer
Jaff  = 8374 #Warehouse Keeper
Jumara= 8375 #Trader
Kurfa = 8376 #Gate Keeper
NPCS = range(8370,8377)

Horn = 7186
#"event number":[Buff Id,Buff Level,Cost]
BUFF={
"1":[4359,1,2],#Focus: Requires 2 Buffalo Horns
"2":[4360,1,2],#Death Whisper: Requires 2 Buffalo Horns
"3":[4345,1,3],#Might: Requires 3 Buffalo Horns
"4":[4355,1,3],#Acumen: Requires 3 Buffalo Horns
"5":[4352,1,3],#Berserker: Requires 3 Buffalo Horns
"6":[4354,1,3],#Vampiric Rage: Requires 3 Buffalo Horns
"7":[4356,1,6],#Empower: Requires 6 Buffalo Horns
"8":[4357,1,6],#Haste: Requires 6 Buffalo Horns
}
class Quest (JQuest) :

 def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)

 def onAdvEvent (self,event,npc,player) :
    htmltext = "8372-2.htm"
    st = player.getQuestState(qn)
    if not st: return
    Alevel = player.getAllianceWithVarkaKetra()
    if str(event) in BUFF.keys() :
        skillId,level,horns=BUFF[event]
        if st.getQuestItemsCount(Horn) >= horns :
            st.takeItems(Horn,horns)
            npc.setTarget(player)
            npc.doCast(SkillTable.getInstance().getInfo(skillId,level))
            npc.setCurrentHpMp(npc.getMaxHp(), npc.getMaxMp())
            htmltext = "8372-4.htm"
    elif event == "Withdraw" :
        if player.getWarehouse().getSize() == 0 :
            htmltext = "8374-0.htm"
        else :
            player.sendPacket(ActionFailed())
            player.setActiveWarehouse(player.getWarehouse())
            player.sendPacket(WareHouseWithdrawalList(player, 1))
    elif event == "Teleport" :
        if Alevel == 4 :
            htmltext = "8376-4.htm"
        elif Alevel == 5 :
            htmltext = "8376-5.htm"
    return htmltext

 def onFirstTalk (self,npc,player):
    htmltext = "<html><body>You are either not carrying out your quest or don't meet the criteria.</body></html>"
    st = player.getQuestState(qn)
    if not st :
        st = self.newQuestState(player)
    npcId = npc.getNpcId()
    Alevel = player.getAllianceWithVarkaKetra()
    Horns = st.getQuestItemsCount(Horn)
    if npcId == Kadun :
        if Alevel > 0 :
            htmltext = "8370-friend.htm"
        else :
            htmltext = "8370-no.htm"
    elif npcId == Wahkan :
        if Alevel > 0 :
            htmltext = "8371-friend.htm"
        else :
            htmltext = "8371-no.htm"
    elif npcId == Asefa :
        st.setState(STARTED)
        if Alevel < 1 :
            htmltext = "8372-3.htm"
        elif Alevel < 3 and Alevel > 0:
            htmltext = "8372-1.htm"
        elif Alevel > 2 :
            htmltext = "8372-4.htm"
    elif npcId == Atan :
        if player.getKarma() >= 1: 
            htmltext = "8373-pk.htm"
        elif Alevel <= 0 :
            htmltext = "8373-no.htm"
        elif Alevel == 1 or Alevel == 2:
            htmltext = "8373-1.htm"
        else:
            htmltext = "8373-2.htm"
    elif npcId == Jaff :
        if Alevel <= 0 :
            htmltext = "8374-no.htm"
        elif Alevel == 1 :
            htmltext = "8374-1.htm"
        elif player.getWarehouse().getSize() == 0 :
            htmltext = "8374-3.htm"
        elif Alevel == 2 or Alevel == 3:
            htmltext = "8374-2.htm"
        else :
            htmltext = "8374-4.htm"
    elif npcId == Jumara :
        if Alevel == 2 :
           htmltext = "8375-1.htm"
        elif Alevel == 3 or Alevel == 4 :
            htmltext = "8375-2.htm"
        elif Alevel == 5 :
            htmltext = "8375-3.htm"
        else :
            htmltext = "8375-no.htm"
    elif npcId == Kurfa :
        if Alevel <= 0 :
            htmltext = "8376-no.htm"
        elif Alevel > 0 and Alevel < 4 :
            htmltext = "8376-1.htm"
        elif Alevel == 4 :
            htmltext = "8376-2.htm"
        else :
            htmltext = "8376-3.htm"
    return htmltext

QUEST       = Quest(6050, qn, "custom")
CREATED     = State('Start', QUEST)
STARTED     = State('Started', QUEST)

QUEST.setInitialState(CREATED)
for i in NPCS:
   QUEST.addFirstTalkId(i)
QUEST.addTalkId(Asefa)
QUEST.addTalkId(Kurfa)
QUEST.addTalkId(Jaff)