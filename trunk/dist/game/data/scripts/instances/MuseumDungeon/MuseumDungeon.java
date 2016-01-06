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

import com.l2jmobius.gameserver.ai.CtrlIntention;
import com.l2jmobius.gameserver.enums.ChatType;
import com.l2jmobius.gameserver.instancemanager.InstanceManager;
import com.l2jmobius.gameserver.model.Location;
import com.l2jmobius.gameserver.model.actor.L2Character;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2MonsterInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2QuestGuardInstance;
import com.l2jmobius.gameserver.model.instancezone.InstanceWorld;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.model.skills.Skill;
import com.l2jmobius.gameserver.network.NpcStringId;
import com.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;

import instances.AbstractInstance;
import quests.Q10327_IntruderWhoWantsTheBookOfGiants.Q10327_IntruderWhoWantsTheBookOfGiants;

/**
 * @author Mobius
 */
public final class MuseumDungeon extends AbstractInstance
{
	// Npcs
	private static final int PANTHEON = 32972;
	private static final int TOYRON = 33004;
	private static final int DESK = 33126;
	// Monster
	private static final int THIEF = 23121;
	// Item
	private static final int THE_WAR_OF_GODS_AND_GIANTS = 17575;
	// Others
	private static final int TEMPLATE_ID = 182;
	private static final Location START_LOC = new Location(-114711, 243911, -7968);
	private static final Location TOYRON_SPAWN_LOC = new Location(-114707, 245428, -7968);
	
	protected class MDWorld extends InstanceWorld
	{
		protected L2QuestGuardInstance toyron = null;
		protected List<L2Npc> deskSpawns = null;
		protected List<L2Npc> thiefSpawns = null;
		protected int randomDesk = 0;
	}
	
	public MuseumDungeon()
	{
		super(MuseumDungeon.class.getSimpleName());
		addStartNpc(PANTHEON);
		addTalkId(PANTHEON);
		addFirstTalkId(DESK);
		addAttackId(THIEF);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equals("assist_player"))
		{
			final InstanceWorld tmpworld = InstanceManager.getInstance().getPlayerWorld(player);
			if ((tmpworld == null) || !(tmpworld instanceof MDWorld))
			{
				return null;
			}
			final MDWorld world = (MDWorld) tmpworld;
			
			world.toyron.setIsRunning(true);
			if (player.isInCombat() && (player.getTarget() != null) && player.getTarget().isMonster() && !((L2MonsterInstance) player.getTarget()).isAlikeDead())
			{
				if (world.toyron.calculateDistance(player.getTarget(), false, false) > 50)
				{
					world.toyron.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, player.getTarget().getLocation());
					broadcastNpcSay(world.toyron, ChatType.NPC_GENERAL, NpcStringId.ENOUGH_OF_THIS_COME_AT_ME);
				}
				else if (world.toyron.getTarget() != player.getTarget())
				{
					world.toyron.addDamageHate((L2Character) player.getTarget(), 0, 1000);
				}
			}
			else
			{
				world.toyron.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, player);
			}
			
			if (world.thiefSpawns.get(0).isDead() && world.thiefSpawns.get(1).isDead())
			{
				final QuestState qs = player.getQuestState(Q10327_IntruderWhoWantsTheBookOfGiants.class.getSimpleName());
				if ((qs != null) && qs.isCond(2))
				{
					qs.setCond(3, true);
					showOnScreenMsg(player, NpcStringId.TALK_TO_TOYRON_TO_RETURN_TO_THE_MUSEUM_LOBBY, ExShowScreenMessage.TOP_CENTER, 5000);
				}
			}
			else
			{
				startQuestTimer("assist_player", 1000, world.toyron, player);
			}
		}
		else if (event.equals("enter_instance"))
		{
			if (npc.getId() != PANTHEON)
			{
				return null;
			}
			
			InstanceWorld tmpworld = InstanceManager.getInstance().getPlayerWorld(player);
			if ((tmpworld == null) || !(tmpworld instanceof MDWorld))
			{
				tmpworld = new MDWorld();
			}
			final MDWorld world = (MDWorld) tmpworld;
			enterInstance(player, world, "MuseumDungeon.xml", TEMPLATE_ID);
			
			final QuestState qs = player.getQuestState(Q10327_IntruderWhoWantsTheBookOfGiants.class.getSimpleName());
			if (qs.isCond(1))
			{
				showOnScreenMsg(player, NpcStringId.AMONG_THE_4_BOOKSHELVES_FIND_THE_ONE_CONTAINING_A_VOLUME_CALLED_THE_WAR_OF_GODS_AND_GIANTS, ExShowScreenMessage.TOP_CENTER, 10000);
			}
			else if (qs.isCond(2))
			{
				if ((world.thiefSpawns != null) && (world.thiefSpawns.get(0) != null))
				{
					world.thiefSpawns.get(0).deleteMe();
				}
				if ((world.thiefSpawns != null) && (world.thiefSpawns.get(1) != null))
				{
					world.thiefSpawns.get(1).deleteMe();
				}
				world.toyron.setIsRunning(true);
				world.thiefSpawns = spawnGroup("thiefs", world.getInstanceId());
				for (L2Npc thief : world.thiefSpawns)
				{
					((L2MonsterInstance) thief).addDamage(player, 1, null);
				}
				startQuestTimer("assist_player", 2000, world.toyron, player);
			}
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		final MDWorld world = (MDWorld) InstanceManager.getInstance().getWorld(npc.getInstanceId());
		final QuestState qs = player.getQuestState(Q10327_IntruderWhoWantsTheBookOfGiants.class.getSimpleName());
		String htmltext = null;
		
		if (qs.isCond(1))
		{
			if (world.deskSpawns.get(world.randomDesk) == npc)
			{
				qs.setCond(2);
				giveItems(player, THE_WAR_OF_GODS_AND_GIANTS, 1);
				world.thiefSpawns = spawnGroup("thiefs", world.getInstanceId());
				for (L2Npc thief : world.thiefSpawns)
				{
					((L2MonsterInstance) thief).addDamage(player, 1, null);
				}
				showOnScreenMsg(player, NpcStringId.WATCH_OUT_YOU_ARE_BEING_ATTACKED, ExShowScreenMessage.TOP_CENTER, 5000);
				startQuestTimer("assist_player", 2000, world.toyron, player);
				htmltext = "desk_correct.html";
			}
			else
			{
				htmltext = "desk_wrong.html";
			}
		}
		else
		{
			htmltext = "desk_normal.html";
		}
		return htmltext;
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon, Skill skill)
	{
		if (skill == null)
		{
			((L2MonsterInstance) npc).setCurrentHp(npc.getCurrentHp() + damage);
			showOnScreenMsg(attacker, NpcStringId.USE_YOUR_SKILL_ATTACKS_AGAINST_THEM, ExShowScreenMessage.TOP_CENTER, 5000);
		}
		return super.onAttack(npc, attacker, damage, isSummon, skill);
	}
	
	@Override
	public void onEnterInstance(L2PcInstance player, InstanceWorld world, boolean firstEntrance)
	{
		((MDWorld) world).toyron = (L2QuestGuardInstance) addSpawn(TOYRON, TOYRON_SPAWN_LOC, false, 0, false, world.getInstanceId());
		((MDWorld) world).toyron.setSpawn(null);
		if (firstEntrance)
		{
			world.addAllowed(player.getObjectId());
			((MDWorld) world).deskSpawns = spawnGroup("desks", world.getInstanceId());
			((MDWorld) world).randomDesk = getRandom(3) + 1;
		}
		teleportPlayer(player, START_LOC, world.getInstanceId());
	}
}