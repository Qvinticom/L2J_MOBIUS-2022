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
package instances.KrofinNest;

import java.util.List;

import org.l2jmobius.commons.util.Chronos;
import org.l2jmobius.commons.util.CommonUtil;
import org.l2jmobius.gameserver.instancemanager.InstanceManager;
import org.l2jmobius.gameserver.model.Party;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.ItemHolder;
import org.l2jmobius.gameserver.model.instancezone.Instance;
import org.l2jmobius.gameserver.network.NpcStringId;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

import instances.AbstractInstance;

/**
 * @author CostyKiller, NasSeKa
 * @URL https://www.youtube.com/watch?v=UCI2lnm-Bc0
 */
public class KrofinNest extends AbstractInstance
{
	// NPCs
	private static final int BENUSTA = 34542;
	private static final int KROSHA_FIRST_FORM = 26389;
	private static final int KROSHA_FINAL_FORM = 26390;
	private static final int KROPION = 26396;
	private static final int[] FIRST_AREA =
	{
		KROPION,
		26398
	};
	private static final int[] SECOND_AREA =
	{
		KROPION,
		26398
	};
	private static final int[] THIRD_AREA =
	{
		26395,
		26397
	};
	private static final int[] FOURTH_AREA =
	{
		26395,
		KROPION,
		26397,
		26398
	};
	private static final int[] KROSHA_FIRST_FORM_MINIONS =
	{
		26393,
		26394
	};
	private static final int[] ENHANCED_MINIONS =
	{
		26391,
		26392
	};
	// Item
	private static final ItemHolder BENUSTAS_REWARD_BOX = new ItemHolder(81151, 1);
	// Misc
	private static final int TEMPLATE_ID = 291; // Krofin Nest
	private static final int DOOR1 = 23220101;
	private static final int DOOR2 = 24250002;
	private static final int DOOR3 = 24250004;
	private static final int DOOR4 = 24250006;
	
	public KrofinNest()
	{
		super(TEMPLATE_ID);
		addStartNpc(BENUSTA);
		addTalkId(BENUSTA);
		addAttackId(FIRST_AREA);
		addAttackId(SECOND_AREA);
		addAttackId(THIRD_AREA);
		addAttackId(FOURTH_AREA);
		addAttackId(KROSHA_FIRST_FORM);
		addAttackId(KROSHA_FINAL_FORM);
		addKillId(KROSHA_FIRST_FORM_MINIONS);
		addKillId(ENHANCED_MINIONS);
		addKillId(KROSHA_FINAL_FORM);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		switch (event)
		{
			case "enterInstance":
			{
				if (player.isInParty())
				{
					final Party party = player.getParty();
					if (!party.isLeader(player))
					{
						player.sendPacket(new SystemMessage(SystemMessageId.ONLY_A_PARTY_LEADER_CAN_MAKE_THE_REQUEST_TO_ENTER));
						return null;
					}
					
					if (player.isInCommandChannel())
					{
						player.sendPacket(new SystemMessage(SystemMessageId.YOU_CANNOT_ENTER_BECAUSE_YOU_DO_NOT_MEET_THE_REQUIREMENTS));
						return null;
					}
					
					final long currentTime = Chronos.currentTimeMillis();
					final List<Player> members = party.getMembers();
					for (Player member : members)
					{
						if (!member.isInsideRadius3D(npc, 1000))
						{
							player.sendMessage("Player " + member.getName() + " must come closer.");
							return null;
						}
						
						if (currentTime < InstanceManager.getInstance().getInstanceTime(member, TEMPLATE_ID))
						{
							final SystemMessage msg = new SystemMessage(SystemMessageId.SINCE_C1_ENTERED_ANOTHER_INSTANCE_ZONE_THEREFORE_YOU_CANNOT_ENTER_THIS_DUNGEON);
							msg.addString(member.getName());
							party.broadcastToPartyMembers(member, msg);
							return null;
						}
					}
					
					for (Player member : members)
					{
						enterInstance(member, npc, TEMPLATE_ID);
					}
				}
				else if (player.isGM())
				{
					enterInstance(player, npc, TEMPLATE_ID);
				}
				else
				{
					player.sendPacket(new SystemMessage(SystemMessageId.YOU_ARE_NOT_CURRENTLY_IN_A_PARTY_SO_YOU_CANNOT_ENTER));
				}
				
				if (player.getInstanceWorld() != null)
				{
					startQuestTimer("check_status", 1000, null, player);
				}
				return null;
			}
			case "check_status":
			{
				final Instance world = player.getInstanceWorld();
				if (!isInInstance(world))
				{
					return null;
				}
				
				switch (world.getStatus())
				{
					case 0:
					{
						world.setStatus(1);
						world.spawnGroup("FIRST_AREA");
						startQuestTimer("check_status", 10000, null, player);
						break;
					}
					case 1:
					{
						if (world.getAliveNpcs(FIRST_AREA).isEmpty())
						{
							showOnScreenMsg(world, NpcStringId.THE_WATER_ENERGY_IS_NO_LONGER_ACTIVE_THE_WAY_IS_CLEAR, ExShowScreenMessage.TOP_CENTER, 10000, true);
							world.setStatus(2);
							world.getDoor(DOOR1).openMe();
							world.spawnGroup("SECOND_AREA");
						}
						startQuestTimer("check_status", 1000, null, player);
						break;
					}
					case 2:
					{
						if (world.getAliveNpcs(SECOND_AREA).isEmpty())
						{
							showOnScreenMsg(world, NpcStringId.THE_WATER_ENERGY_IS_NO_LONGER_ACTIVE_THE_WAY_IS_CLEAR, ExShowScreenMessage.TOP_CENTER, 10000, true);
							world.setStatus(3);
							world.getDoor(DOOR2).openMe();
							world.spawnGroup("THIRD_AREA");
						}
						startQuestTimer("check_status", 10000, null, player);
						break;
					}
					case 3:
					{
						if (world.getAliveNpcs(THIRD_AREA).isEmpty())
						{
							showOnScreenMsg(world, NpcStringId.THE_WATER_ENERGY_IS_NO_LONGER_ACTIVE_THE_WAY_IS_CLEAR, ExShowScreenMessage.TOP_CENTER, 10000, true);
							world.setStatus(4);
							world.getDoor(DOOR3).openMe();
							world.spawnGroup("FOURTH_AREA");
						}
						startQuestTimer("check_status", 10000, null, player);
						break;
					}
					case 4:
					{
						if (world.getAliveNpcs(FOURTH_AREA).isEmpty())
						{
							showOnScreenMsg(world, NpcStringId.THE_WATER_ENERGY_IS_NO_LONGER_ACTIVE_THE_WAY_IS_CLEAR, ExShowScreenMessage.TOP_CENTER, 10000, true);
							world.setStatus(5);
							world.getDoor(DOOR4).openMe();
							world.spawnGroup("KROSHA_FIRST_FORM");
						}
						startQuestTimer("check_status", 10000, null, player);
						break;
					}
				}
				return null;
			}
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onAttack(Npc npc, Player attacker, int damage, boolean isSummon)
	{
		final Instance world = attacker.getInstanceWorld();
		if (isInInstance(world))
		{
			final boolean kropionMinionsSpawned = world.getParameters().getBoolean("KROPION_MINIONS_SPAWNED", false);
			final boolean kroshaFirstFormMinionsSpawned = world.getParameters().getBoolean("KROSHA_FIRST_FORM_MINIONS_SPAWNED", false);
			final boolean kroshaFinalFormMinionsSpawned = world.getParameters().getBoolean("KROSHA_FINAL_FORM_MINIONS_SPAWNED", false);
			if ((world.getStatus() == 2) && (npc.getId() == KROPION))
			{
				if (!kropionMinionsSpawned)
				{
					world.getParameters().set("KROPION_MINIONS_SPAWNED", true);
					world.spawnGroup("KROPION_MINIONS");
				}
			}
			else if (world.getStatus() == 5)
			{
				if ((npc.getId() == KROSHA_FIRST_FORM) && !kroshaFirstFormMinionsSpawned)
				{
					world.getParameters().set("KROSHA_FIRST_FORM_MINIONS_SPAWNED", true);
					world.spawnGroup("KROSHA_FIRST_FORM_MINIONS");
				}
				else if ((npc.getId() == KROSHA_FINAL_FORM) && !kroshaFinalFormMinionsSpawned)
				{
					world.getParameters().set("KROSHA_FINAL_FORM_MINIONS_SPAWNED", true);
					world.spawnGroup("KROSHA_FINAL_FORM_MINIONS");
				}
			}
		}
		return super.onAttack(npc, attacker, damage, isSummon);
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isPet)
	{
		final Instance world = npc.getInstanceWorld();
		if (isInInstance(world))
		{
			final boolean kroshaFirstFormMinionsSpawnedTwice = world.getParameters().getBoolean("KROSHA_FIRST_FORM_MINIONS_SPAWNED_TWICE", false);
			if (world.getStatus() == 5)
			{
				if (CommonUtil.contains(KROSHA_FIRST_FORM_MINIONS, npc.getId()))
				{
					if (world.getAliveNpcs(KROSHA_FIRST_FORM_MINIONS).isEmpty() && !kroshaFirstFormMinionsSpawnedTwice)
					{
						world.getParameters().set("KROSHA_FIRST_FORM_MINIONS_SPAWNED_TWICE", true);
						world.spawnGroup("KROSHA_FIRST_FORM_MINIONS");
					}
					else if (world.getAliveNpcs(KROSHA_FIRST_FORM_MINIONS).isEmpty() && kroshaFirstFormMinionsSpawnedTwice)
					{
						world.despawnGroup("KROSHA_FIRST_FORM");
						showOnScreenMsg(world, NpcStringId.QUEEN_KROSHA_HAS_DISAPPEARED, ExShowScreenMessage.TOP_CENTER, 7000, true);
						world.spawnGroup("ENHANCED_MINIONS");
					}
				}
				else if (CommonUtil.contains(ENHANCED_MINIONS, npc.getId()))
				{
					if ((world.getAliveNpcs(ENHANCED_MINIONS).isEmpty()))
					{
						world.spawnGroup("KROSHA_FINAL_FORM");
						showOnScreenMsg(world, NpcStringId.QUEEN_KROSHA_HAS_RETURNED_MORE_POWERFUL_THAN_EVER, ExShowScreenMessage.TOP_CENTER, 7000, true);
					}
				}
				else if (npc.getId() == KROSHA_FINAL_FORM)
				{
					for (Player member : world.getPlayers())
					{
						giveItems(member, BENUSTAS_REWARD_BOX);
					}
					showOnScreenMsg(world, NpcStringId.THE_WATER_POWER_PROTECTING_QUEEN_KROSHA_HAS_DISAPPEARED, ExShowScreenMessage.TOP_CENTER, 7000, true);
					world.finishInstance();
				}
				else
				{
					world.setReenterTime();
				}
			}
		}
		return super.onKill(npc, killer, isPet);
	}
	
	public static void main(String[] args)
	{
		new KrofinNest();
	}
}
