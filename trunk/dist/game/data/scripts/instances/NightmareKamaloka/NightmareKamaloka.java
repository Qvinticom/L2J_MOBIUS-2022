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
package instances.NightmareKamaloka;

import java.util.ArrayList;
import java.util.List;

import com.l2jmobius.gameserver.instancemanager.InstanceManager;
import com.l2jmobius.gameserver.model.L2Party;
import com.l2jmobius.gameserver.model.Location;
import com.l2jmobius.gameserver.model.PcCondOverride;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2RaidBossInstance;
import com.l2jmobius.gameserver.model.instancezone.InstanceWorld;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.model.quest.State;
import com.l2jmobius.gameserver.network.SystemMessageId;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import com.l2jmobius.gameserver.util.Util;

import instances.AbstractInstance;
import quests.Q00760_BlockTheExit.Q00760_BlockTheExit;

/**
 * @author NviX
 */
public final class NightmareKamaloka extends AbstractInstance
{
	class NKWorld extends InstanceWorld
	{
		L2RaidBossInstance sola = null;
		L2RaidBossInstance ariarc = null;
		L2RaidBossInstance sirra = null;
		L2RaidBossInstance dark_rider = null;
		final List<L2PcInstance> playersInside = new ArrayList<>();
	}
	
	// NPC
	private static final int KURTIZ = 30870;
	// Bosses & Monsters
	private static final int MINO = 26093;
	private static final int SOLA = 26094;
	private static final int ARIARC = 26096;
	private static final int SIRRA = 26099;
	private static final int DARK_RIDER = 26102;
	// Instance
	private static final int TEMPLATE_ID = 258;
	// Doors
	private static final int DOOR_1 = 18170002;
	private static final int DOOR_2 = 18170004;
	private static final int DOOR_3 = 18170006;
	private static final int DOOR_4 = 18170008;
	// Locations
	private static final Location START_LOC = new Location(-42921, -16243, -12212);
	private static final Location SOLA_SPAWN_LOC = new Location(-45719, -16241, -11965);
	private static final Location ARIARC_SPAWN_LOC = new Location(-48055, -16243, -11702);
	private static final Location SIRRA_SPAWN_LOC = new Location(-50385, -16240, -11439);
	private static final Location DARK_RIDER_SPAWN_LOC = new Location(-54172, -16241, -10995);
	
	public NightmareKamaloka()
	{
		super(NightmareKamaloka.class.getSimpleName());
		addStartNpc(KURTIZ);
		addTalkId(KURTIZ);
		addKillId(MINO);
		addKillId(SOLA);
		addKillId(ARIARC);
		addKillId(SIRRA);
		addKillId(DARK_RIDER);
	}
	
	@Override
	public void onEnterInstance(L2PcInstance player, InstanceWorld world, boolean firstEntrance)
	{
		if (firstEntrance)
		{
			final NKWorld tmpworld = (NKWorld) world;
			if (!player.isInParty())
			{
				tmpworld.addAllowed(player.getObjectId());
				tmpworld.playersInside.add(player);
				teleportPlayer(player, START_LOC, tmpworld.getInstanceId(), false);
			}
			else
			{
				for (L2PcInstance member : player.getParty().getMembers())
				{
					tmpworld.addAllowed(member.getObjectId());
					tmpworld.playersInside.add(member);
					teleportPlayer(member, START_LOC, tmpworld.getInstanceId(), false);
				}
			}
		}
		else
		{
			teleportPlayer(player, START_LOC, world.getInstanceId(), false);
		}
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equals("enter"))
		{
			enterInstance(player, new NKWorld(), "NightmareKamaloka.xml", TEMPLATE_ID);
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	protected boolean checkConditions(L2PcInstance player)
	{
		if (player.canOverrideCond(PcCondOverride.INSTANCE_CONDITIONS))
		{
			return true;
		}
		final L2Party party = player.getParty();
		if (party == null)
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_CURRENTLY_IN_A_PARTY_SO_YOU_CANNOT_ENTER);
			return false;
		}
		if (party.getLeader() != player)
		{
			player.sendPacket(SystemMessageId.ONLY_A_PARTY_LEADER_CAN_MAKE_THE_REQUEST_TO_ENTER);
			return false;
		}
		for (L2PcInstance member : party.getMembers())
		{
			if (member.getLevel() < 99)
			{
				party.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_S_LEVEL_DOES_NOT_CORRESPOND_TO_THE_REQUIREMENTS_FOR_ENTRY).addPcName(member));
				return false;
			}
			if (!Util.checkIfInRange(1000, party.getLeader(), member, true))
			{
				party.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_A_LOCATION_WHICH_CANNOT_BE_ENTERED_THEREFORE_IT_CANNOT_BE_PROCESSED).addPcName(member));
				return false;
			}
			final Long reenterTime = InstanceManager.getInstance().getInstanceTime(member.getObjectId(), TEMPLATE_ID);
			if (System.currentTimeMillis() < reenterTime)
			{
				party.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_MAY_NOT_RE_ENTER_YET));
				return false;
			}
		}
		return true;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		final InstanceWorld tmpWorld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if ((tmpWorld != null) && (tmpWorld instanceof NKWorld))
		{
			final NKWorld world = (NKWorld) tmpWorld;
			switch (npc.getId())
			{
				case MINO:
				{
					openDoor(DOOR_1, world.getInstanceId());
					world.sola = (L2RaidBossInstance) addSpawn(SOLA, SOLA_SPAWN_LOC, false, 0, true, world.getInstanceId());
					break;
				}
				case SOLA:
				{
					openDoor(DOOR_2, world.getInstanceId());
					world.ariarc = (L2RaidBossInstance) addSpawn(ARIARC, ARIARC_SPAWN_LOC, false, 0, true, world.getInstanceId());
					break;
				}
				case ARIARC:
				{
					openDoor(DOOR_3, world.getInstanceId());
					world.sirra = (L2RaidBossInstance) addSpawn(SIRRA, SIRRA_SPAWN_LOC, false, 0, true, world.getInstanceId());
					break;
				}
				case SIRRA:
				{
					openDoor(DOOR_4, world.getInstanceId());
					world.dark_rider = (L2RaidBossInstance) addSpawn(DARK_RIDER, DARK_RIDER_SPAWN_LOC, false, 0, true, world.getInstanceId());
					break;
				}
				case DARK_RIDER:
				{
					for (L2PcInstance plr : world.playersInside)
					{
						if ((plr != null) && (plr.getInstanceId() == world.getInstanceId()))
						{
							final QuestState qs = plr.getQuestState(Q00760_BlockTheExit.class.getSimpleName());
							if ((qs != null) && (qs.getState() == State.STARTED) && qs.isCond(1))
							{
								qs.setCond(2);
							}
						}
					}
					finishInstance(world);
					break;
				}
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
}
