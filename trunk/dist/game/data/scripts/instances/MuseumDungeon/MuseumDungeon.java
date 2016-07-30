/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package instances.MuseumDungeon;

import java.util.List;

import com.l2jmobius.gameserver.enums.ChatType;
import com.l2jmobius.gameserver.model.actor.L2Attackable;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.events.EventType;
import com.l2jmobius.gameserver.model.events.ListenerRegisterType;
import com.l2jmobius.gameserver.model.events.annotations.Id;
import com.l2jmobius.gameserver.model.events.annotations.RegisterEvent;
import com.l2jmobius.gameserver.model.events.annotations.RegisterType;
import com.l2jmobius.gameserver.model.events.impl.character.OnCreatureDeath;
import com.l2jmobius.gameserver.model.instancezone.Instance;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.model.skills.Skill;
import com.l2jmobius.gameserver.network.NpcStringId;
import com.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;

import instances.AbstractInstance;
import quests.Q10327_IntruderWhoWantsTheBookOfGiants.Q10327_IntruderWhoWantsTheBookOfGiants;

/**
 * Museum Dungeon Instance Zone.
 * @author Gladicek
 */
public final class MuseumDungeon extends AbstractInstance
{
	// NPCs
	private static final int PANTHEON = 32972;
	private static final int TOYRON = 33004;
	private static final int DESK = 33126;
	private static final int THIEF = 23121;
	// Items
	private static final int THE_WAR_OF_GODS_AND_GIANTS = 17575;
	// Misc
	private static final int TEMPLATE_ID = 182;
	
	private static final NpcStringId[] THIEF_SHOUT =
	{
		NpcStringId.YOU_LL_NEVER_LEAVE_WITH_THAT_BOOK,
		NpcStringId.FINALLY_I_THOUGHT_I_WAS_GOING_TO_DIE_WAITING
	};
	
	public MuseumDungeon()
	{
		addStartNpc(PANTHEON);
		addFirstTalkId(DESK);
		addTalkId(PANTHEON, TOYRON);
		addAttackId(THIEF);
	}
	
	@Override
	protected void onEnter(L2PcInstance player, Instance instance, boolean firstEnter)
	{
		super.onEnter(player, instance, firstEnter);
		
		final L2Attackable toyron = (L2Attackable) instance.getNpc(TOYRON);
		if (firstEnter)
		{
			// Set desk status
			final List<L2Npc> desks = instance.getNpcs(DESK);
			final L2Npc desk = desks.get(getRandom(desks.size()));
			desk.getVariables().set("book", true);
			
			// Set Toyron
			toyron.setIsRunning(true);
			toyron.setCanReturnToSpawnPoint(false);
		}
		
		final QuestState qs = player.getQuestState(Q10327_IntruderWhoWantsTheBookOfGiants.class.getSimpleName());
		if (qs != null)
		{
			if (qs.isCond(1))
			{
				showOnScreenMsg(player, NpcStringId.AMONG_THE_4_BOOKSHELVES_FIND_THE_ONE_CONTAINING_A_VOLUME_CALLED_THE_WAR_OF_GODS_AND_GIANTS, ExShowScreenMessage.TOP_CENTER, 4500);
			}
			else if (qs.isCond(2))
			{
				startQuestTimer("TOYRON_FOLLOW", 500, toyron, player);
				if (instance.getNpcs(THIEF).isEmpty())
				{
					startQuestTimer("SPAWN_THIEFS_STAGE_2", 500, null, player);
				}
			}
		}
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equals("enter_instance"))
		{
			enterInstance(player, npc, TEMPLATE_ID);
		}
		else
		{
			final Instance world = player.getInstanceWorld();
			if (world != null)
			{
				switch (event)
				{
					case "TOYRON_FOLLOW":
					{
						npc.getAI().startFollow(player);
						break;
					}
					case "SPAWN_THIEFS_STAGE_1":
					{
						final List<L2Npc> thiefs = world.spawnGroup("thiefs");
						for (L2Npc thief : thiefs)
						{
							thief.setIsRunning(true);
							addAttackPlayerDesire(thief, player);
							thief.broadcastSay(ChatType.NPC_GENERAL, THIEF_SHOUT[getRandom(2)]);
						}
						final L2Npc toyron = world.getNpc(TOYRON);
						toyron.broadcastSay(ChatType.NPC_GENERAL, NpcStringId.WHEN_DID_THEY_GET_IN_HERE);
						startQuestTimer("TOYRON_MSG_1", 2500, toyron, player);
						startQuestTimer("SKILL_MSG", 4500, toyron, player);
						break;
					}
					case "SPAWN_THIEFS_STAGE_2":
					{
						final List<L2Npc> thiefs = world.spawnGroup("thiefs");
						for (L2Npc thief : thiefs)
						{
							thief.setIsRunning(true);
						}
						break;
					}
					case "SKILL_MSG":
					{
						showOnScreenMsg(player, NpcStringId.USE_YOUR_SKILL_ATTACKS_AGAINST_THEM, ExShowScreenMessage.TOP_CENTER, 4500);
						break;
					}
					case "TOYRON_MSG_1":
					{
						npc.broadcastSay(ChatType.NPC_GENERAL, NpcStringId.YOUR_NORMAL_ATTACKS_AREN_T_WORKING);
						startQuestTimer("TOYRON_MSG_2", 2500, npc, player);
						break;
					}
					case "TOYRON_MSG_2":
					{
						npc.broadcastSay(ChatType.NPC_GENERAL, NpcStringId.LOOKS_LIKE_ONLY_SKILL_BASED_ATTACKS_DAMAGE_THEM);
						break;
					}
				}
			}
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		final Instance world = npc.getInstanceWorld();
		if (world == null)
		{
			return null;
		}
		
		String htmltext = null;
		final QuestState qs = player.getQuestState(Q10327_IntruderWhoWantsTheBookOfGiants.class.getSimpleName());
		if ((qs == null) || qs.isCond(2))
		{
			htmltext = "33126.html";
		}
		else if (qs.isCond(1))
		{
			if (npc.getVariables().getBoolean("book", false) && !hasQuestItems(player, THE_WAR_OF_GODS_AND_GIANTS))
			{
				qs.setCond(2);
				giveItems(player, THE_WAR_OF_GODS_AND_GIANTS, 1);
				showOnScreenMsg(player, NpcStringId.WATCH_OUT_YOU_ARE_BEING_ATTACKED, ExShowScreenMessage.TOP_CENTER, 4500);
				htmltext = "33126-01.html";
				
				final L2Npc toyron = world.getNpc(TOYRON);
				startQuestTimer("SPAWN_THIEFS_STAGE_1", 500, null, player);
				startQuestTimer("TOYRON_FOLLOW", 500, toyron, player);
			}
			else
			{
				htmltext = "33126-02.html";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon, Skill skill)
	{
		final Instance instance = npc.getInstanceWorld();
		if (instance != null)
		{
			final L2Npc toyron = instance.getNpc(TOYRON);
			if (!toyron.isInCombat())
			{
				toyron.broadcastSay(ChatType.NPC_GENERAL, NpcStringId.ENOUGH_OF_THIS_COME_AT_ME);
				toyron.reduceCurrentHp(1, npc, null); // TODO: Find better way for attack
				npc.reduceCurrentHp(1, toyron, null);
				startQuestTimer("TOYRON_FOLLOW", 3000, toyron, attacker);
			}
		}
		return super.onAttack(npc, attacker, damage, isSummon, skill);
	}
	
	@RegisterEvent(EventType.ON_CREATURE_DEATH)
	@RegisterType(ListenerRegisterType.NPC)
	@Id(THIEF)
	public void onCreatureKill(OnCreatureDeath event)
	{
		final L2Npc npc = (L2Npc) event.getTarget();
		
		final Instance world = npc.getInstanceWorld();
		if (world != null)
		{
			final L2PcInstance player = world.getFirstPlayer();
			final QuestState qs = player.getQuestState(Q10327_IntruderWhoWantsTheBookOfGiants.class.getSimpleName());
			if ((qs != null) && qs.isCond(2) && (world.getAliveNpcs(THIEF).size() <= 1))
			{
				qs.setCond(3, true);
				showOnScreenMsg(player, NpcStringId.TALK_TO_TOYRON_TO_RETURN_TO_THE_MUSEUM_LOBBY, ExShowScreenMessage.TOP_CENTER, 4500);
			}
		}
	}
	
	public static void main(String[] args)
	{
		new MuseumDungeon();
	}
}