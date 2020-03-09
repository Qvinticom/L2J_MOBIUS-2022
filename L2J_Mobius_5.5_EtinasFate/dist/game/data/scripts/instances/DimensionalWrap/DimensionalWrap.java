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
package instances.DimensionalWrap;

import java.util.List;

import org.l2jmobius.Config;
import org.l2jmobius.commons.util.CommonUtil;
import org.l2jmobius.gameserver.data.xml.impl.SkillData;
import org.l2jmobius.gameserver.enums.CategoryType;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.Party;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.instancezone.Instance;
import org.l2jmobius.gameserver.model.skills.Skill;
import org.l2jmobius.gameserver.network.NpcStringId;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.Earthquake;
import org.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

import instances.AbstractInstance;

/**
 * Dimensional Wrap instance
 * @URL https://l2wiki.com/Dimensional_Warp
 * @Video https://www.youtube.com/watch?v=hOnzk0ELwIg
 * @author Gigi, Mobius
 * @date 2018-09-04 - [14:33:31]
 */
public class DimensionalWrap extends AbstractInstance
{
	// NPCs
	private static final int RESED = 33974;
	private static final int EINSTER = 33975;
	// Monsters
	private static final int DEMINSIONAL_INVISIBLE_FRAGMENT = 19564;
	private static final int SALAMANDRA_GENERATOR = 19563;
	private static final int SALAMANDRA_GENERATOR_DUMMY = 19480;
	private static final int DIMENSIONAL_SALAMANDRA = 23466;
	private static final int UNWORDLY_SALAMANDER = 23473;
	private static final int DIMENSIONAL_IMP = 19553;
	private static final int UNWORDLY_IMP = 19554;
	private static final int ABYSSAL_IMP = 19555;
	private static final int ABYSSAL_MAKKUM = 26090;
	private static final int[] MONSTERS =
	{
		23462, // Dimensional Orc Butcher
		23463, // Dimensional Orc Hunter
		23464, // Dimensional Shaman
		23465, // Dimensional Bugbear
		23467, // Dimensional Binder
		23468, // Dimensional Demon
		23469, // Dimensional Archon
		23470, // Unworldly Demon
		23471, // Unworldly Etin
		23472, // Unworldly Shaman
		23474, // Unworldly Golem
		23475, // Unworldly Archon
		23476, // Unworldly Harpy
		23477, // Abyssal Shaman
		23478, // Abyssal Berserker
		23480, // Abyssal Harpy
		23481, // Abyssal Binder
		23482, // Abyssal Archon
		23483, // Abyssal Golem
	};
	private static final int[] TRAPS =
	{
		19556, // Debuff trap, power 1
		19557, // Debuff trap, power 2
		19558, // Debuff trap, power 3
		19559, // Damage trap, power 1
		19560, // Damage trap, power 2
		19561, // Damage trap, power 3
		19562, // Heal Trap
	};
	// Location
	private static final Location TELEPORTS = new Location(-76136, -216216, 4040);
	private static final Location FIRST_TELEPORT = new Location(-219544, 248776, 3360);
	private static final Location SECOND_TELEPORT = new Location(-206756, 242009, 6584);
	private static final Location THIRD_TELEPORT = new Location(-219813, 248484, 9928);
	private static final Location FOURTH_TELEPORT = new Location(-87191, -210129, 6984);
	// Misc
	private static final int TEMPLATE_ID = 250;
	private static final int DIMENSIONAL_DARK_FORCES = 16415;
	private static final int WARP_CRYSTAL = 39597;
	
	public DimensionalWrap()
	{
		super(TEMPLATE_ID);
		addStartNpc(RESED);
		addTalkId(EINSTER);
		addKillId(MONSTERS);
		addKillId(ABYSSAL_MAKKUM);
		addSpawnId(SALAMANDRA_GENERATOR, DEMINSIONAL_INVISIBLE_FRAGMENT, SALAMANDRA_GENERATOR_DUMMY);
		addFirstTalkId(EINSTER);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, PlayerInstance player)
	{
		String htmltext = null;
		switch (event)
		{
			case "enterInstance":
			{
				if (!player.isInCategory(CategoryType.SIXTH_CLASS_GROUP))
				{
					htmltext = "no_awakened.html";
				}
				else if (!player.isInParty())
				{
					enterInstance(player, npc, TEMPLATE_ID);
				}
				else if (player.isInParty())
				{
					if (!player.getParty().isLeader(player))
					{
						player.sendPacket(new SystemMessage(SystemMessageId.ONLY_A_PARTY_LEADER_CAN_MAKE_THE_REQUEST_TO_ENTER));
					}
					else
					{
						final Party party = player.getParty();
						final List<PlayerInstance> members = party.getMembers();
						for (PlayerInstance member : members)
						{
							if (member.isInsideRadius3D(npc, Config.ALT_PARTY_RANGE))
							{
								enterInstance(member, npc, TEMPLATE_ID);
							}
						}
					}
				}
				break;
			}
			case "33975-01.html":
			{
				htmltext = event;
				break;
			}
			case "12_warp_crystals":
			{
				final Instance world = npc.getInstanceWorld();
				if (!isInInstance(world))
				{
					return null;
				}
				
				world.setParameter("chance", 0.3);
				if (!player.isInParty())
				{
					world.setParameter("count", 12);
					checkCrystallCount(world, npc);
					break;
				}
				else if (player.isInParty() && !player.getParty().isLeader(player))
				{
					player.sendPacket(new SystemMessage(SystemMessageId.ONLY_A_PARTY_LEADER_CAN_MAKE_THE_REQUEST_TO_ENTER));
					break;
				}
				switch (player.getParty().getMemberCount())
				{
					case 2:
					{
						world.setParameter("count", 6);
						checkCrystallCount(world, npc);
						break;
					}
					case 3:
					{
						world.setParameter("count", 4);
						checkCrystallCount(world, npc);
						break;
					}
					case 4:
					{
						world.setParameter("count", 3);
						checkCrystallCount(world, npc);
						break;
					}
				}
				break;
			}
			case "240_warp_crystals":
			{
				final Instance world = npc.getInstanceWorld();
				if (!isInInstance(world))
				{
					return null;
				}
				
				world.setParameter("chance", 0.6);
				if (!player.isInParty())
				{
					world.setParameter("count", 240);
					checkCrystallCount(world, npc);
					break;
				}
				else if (player.isInParty() && !player.getParty().isLeader(player))
				{
					player.sendPacket(new SystemMessage(SystemMessageId.ONLY_A_PARTY_LEADER_CAN_MAKE_THE_REQUEST_TO_ENTER));
					break;
				}
				switch (player.getParty().getMemberCount())
				{
					case 2:
					{
						world.setParameter("count", 120);
						checkCrystallCount(world, npc);
						break;
					}
					case 3:
					{
						world.setParameter("count", 80);
						checkCrystallCount(world, npc);
						break;
					}
					case 4:
					{
						world.setParameter("count", 60);
						checkCrystallCount(world, npc);
						break;
					}
				}
				break;
			}
			case "1200_warp_crystals":
			{
				final Instance world = npc.getInstanceWorld();
				if (!isInInstance(world))
				{
					return null;
				}
				
				world.setParameter("chance", 0.9);
				if (!player.isInParty())
				{
					world.setParameter("count", 1200);
					checkCrystallCount(world, npc);
					break;
				}
				else if (player.isInParty() && !player.getParty().isLeader(player))
				{
					player.sendPacket(new SystemMessage(SystemMessageId.ONLY_A_PARTY_LEADER_CAN_MAKE_THE_REQUEST_TO_ENTER));
					break;
				}
				switch (player.getParty().getMemberCount())
				{
					case 2:
					{
						world.setParameter("count", 600);
						checkCrystallCount(world, npc);
						break;
					}
					case 3:
					{
						world.setParameter("count", 400);
						checkCrystallCount(world, npc);
						break;
					}
					case 4:
					{
						world.setParameter("count", 300);
						checkCrystallCount(world, npc);
						break;
					}
				}
				break;
			}
			case "send_6_f":
			{
				final Instance world = npc.getInstanceWorld();
				if (!isInInstance(world))
				{
					return null;
				}
				
				if (world.getParameters().getInt("worldState", 0) == 0)
				{
					htmltext = "33975-02.html";
					break;
				}
				if (player.getVariables().getInt("DIMENSIONAL_WRAP_LEVEL", 0) == 0)
				{
					htmltext = "33975-05.html";
					break;
				}
				
				for (Npc n : world.getAliveNpcs())
				{
					if (n.getId() != EINSTER)
					{
						n.deleteMe();
					}
				}
				if (world.getStatus() < 5)
				{
					world.setStatus(5);
					cancelQuestTimer("SECOND_SPAWN", null, world.getFirstPlayer());
					cancelQuestTimer("THIRD_SPAWN", null, world.getFirstPlayer());
					startQuestTimer("START_STAGE", 5000, null, world.getFirstPlayer());
				}
				for (PlayerInstance pl : world.getPlayers())
				{
					pl.teleToLocation(FIRST_TELEPORT, world.getTemplateId());
				}
				break;
			}
			case "send_11_f":
			{
				final Instance world = npc.getInstanceWorld();
				if (!isInInstance(world))
				{
					return null;
				}
				
				if (world.getParameters().getInt("worldState", 0) == 0)
				{
					htmltext = "33975-02.html";
					break;
				}
				if ((player.getVariables().getInt("DIMENSIONAL_WRAP_LEVEL", 0) == 0) || (player.getVariables().getInt("DIMENSIONAL_WRAP_LEVEL") < 2))
				{
					htmltext = "33975-05.html";
					break;
				}
				
				for (Npc n : world.getAliveNpcs())
				{
					if (n.getId() != EINSTER)
					{
						n.deleteMe();
					}
				}
				if (world.getStatus() < 10)
				{
					world.setStatus(10);
					cancelQuestTimer("SECOND_SPAWN", null, world.getFirstPlayer());
					cancelQuestTimer("THIRD_SPAWN", null, world.getFirstPlayer());
					startQuestTimer("START_STAGE", 5000, null, world.getFirstPlayer());
				}
				for (PlayerInstance pl : world.getPlayers())
				{
					pl.teleToLocation(SECOND_TELEPORT, world.getTemplateId());
				}
				break;
			}
			case "send_16_f":
			{
				final Instance world = npc.getInstanceWorld();
				if (!isInInstance(world))
				{
					return null;
				}
				
				if (world.getParameters().getInt("worldState", 0) == 0)
				{
					htmltext = "33975-02.html";
					break;
				}
				if ((player.getVariables().getInt("DIMENSIONAL_WRAP_LEVEL", 0) == 0) || (player.getVariables().getInt("DIMENSIONAL_WRAP_LEVEL") < 3))
				{
					htmltext = "33975-05.html";
					break;
				}
				
				for (Npc n : world.getAliveNpcs())
				{
					if (n.getId() != EINSTER)
					{
						n.deleteMe();
					}
				}
				if (world.getStatus() < 15)
				{
					world.setStatus(15);
					cancelQuestTimer("SECOND_SPAWN", null, world.getFirstPlayer());
					cancelQuestTimer("THIRD_SPAWN", null, world.getFirstPlayer());
					startQuestTimer("START_STAGE", 5000, null, world.getFirstPlayer());
				}
				for (PlayerInstance pl : world.getPlayers())
				{
					pl.teleToLocation(THIRD_TELEPORT, world.getTemplateId());
				}
				break;
			}
			case "send_21_f":
			{
				final Instance world = npc.getInstanceWorld();
				if (!isInInstance(world))
				{
					return null;
				}
				
				if (world.getParameters().getInt("worldState", 0) == 0)
				{
					htmltext = "33975-02.html";
					break;
				}
				if ((player.getVariables().getInt("DIMENSIONAL_WRAP_LEVEL", 0) == 0) || (player.getVariables().getInt("DIMENSIONAL_WRAP_LEVEL") < 4))
				{
					htmltext = "33975-05.html";
					break;
				}
				
				for (Npc n : world.getAliveNpcs())
				{
					if (n.getId() != EINSTER)
					{
						n.deleteMe();
					}
				}
				if (world.getStatus() < 20)
				{
					world.setStatus(20);
					cancelQuestTimer("SECOND_SPAWN", null, world.getFirstPlayer());
					cancelQuestTimer("THIRD_SPAWN", null, world.getFirstPlayer());
					startQuestTimer("START_STAGE", 5000, null, world.getFirstPlayer());
				}
				for (PlayerInstance pl : world.getPlayers())
				{
					pl.teleToLocation(TELEPORTS, world.getTemplateId());
				}
				break;
			}
			case "send_26_f":
			{
				final Instance world = player.getInstanceWorld();
				if (!isInInstance(world))
				{
					return null;
				}
				
				if (world.getParameters().getInt("worldState", 0) == 0)
				{
					htmltext = "33975-02.html";
					break;
				}
				if ((player.getVariables().getInt("DIMENSIONAL_WRAP_LEVEL", 0) == 0) || (player.getVariables().getInt("DIMENSIONAL_WRAP_LEVEL") < 5))
				{
					htmltext = "33975-05.html";
					break;
				}
				
				for (Npc n : world.getAliveNpcs())
				{
					if (n.getId() != EINSTER)
					{
						n.deleteMe();
					}
				}
				if (world.getStatus() < 25)
				{
					world.setStatus(25);
					cancelQuestTimer("SECOND_SPAWN", null, world.getFirstPlayer());
					cancelQuestTimer("THIRD_SPAWN", null, world.getFirstPlayer());
					startQuestTimer("START_STAGE", 5000, null, world.getFirstPlayer());
				}
				for (PlayerInstance pl : world.getPlayers())
				{
					pl.teleToLocation(FOURTH_TELEPORT, world.getTemplateId());
				}
				break;
			}
			case "jump_location":
			{
				final Instance world = player.getInstanceWorld();
				if (!isInInstance(world))
				{
					return null;
				}
				player.teleToLocation(TELEPORTS, world.getTemplateId());
				break;
			}
			case "SALAMANDRA_SPAWN":
			case "SALAMANDRA_SPAWN_DUMMY":
			{
				final Instance world = player.getInstanceWorld();
				if (!isInInstance(world))
				{
					return null;
				}
				if (getRandom(100) < (world.getParameters().getInt("worldState", 0) / 2))
				{
					final Npc salamandra = addSpawn(world.getParameters().getInt("worldState", 0) < 17 ? DIMENSIONAL_SALAMANDRA : UNWORDLY_SALAMANDER, npc, false, 0, false, world.getId());
					salamandra.setRunning();
					World.getInstance().forEachVisibleObjectInRange(salamandra, PlayerInstance.class, 2500, p ->
					{
						if ((p != null) && !p.isDead())
						{
							addAttackPlayerDesire(salamandra, p);
						}
					});
				}
				break;
			}
			case "START_STAGE":
			{
				final Instance world = player.getInstanceWorld();
				if (!isInInstance(world))
				{
					return null;
				}
				world.setStatus(world.getStatus() + 1);
				world.setParameter("worldState", world.getStatus());
				world.broadcastPacket(new ExShowScreenMessage(NpcStringId.DIMENSIONAL_WARP_LV_S1, ExShowScreenMessage.TOP_CENTER, 10000, true, String.valueOf(world.getStatus())));
				startQuestTimer("FIRST_SPAWN", 1500, null, world.getFirstPlayer());
				break;
			}
			case "FIRST_SPAWN":
			{
				final Instance world = player.getInstanceWorld();
				if (!isInInstance(world))
				{
					return null;
				}
				final int worldState = world.getParameters().getInt("worldState", 0);
				world.spawnGroup(worldState + "_first_spawn");
				world.spawnGroup(worldState + "_trap_spawn");
				startQuestTimer("SECOND_SPAWN", 40000, null, world.getFirstPlayer());
				startQuestTimer("DEBUFF_TASK", 10000, null, world.getFirstPlayer(), true);
				break;
			}
			case "DEBUFF_TASK":
			{
				final Instance world = player.getInstanceWorld();
				if (!isInInstance(world))
				{
					return null;
				}
				int skilllevel = 1;
				final int worldState = world.getParameters().getInt("worldState", 0);
				if ((worldState > 0) && (worldState <= 11))
				{
					skilllevel = 1;
				}
				else if ((worldState > 11) && (worldState <= 20))
				{
					skilllevel = 2;
				}
				else if ((worldState > 20) && (worldState <= 30))
				{
					skilllevel = 3;
				}
				else if (worldState > 30)
				{
					skilllevel = 4;
				}
				final Skill skill = SkillData.getInstance().getSkill(DIMENSIONAL_DARK_FORCES, skilllevel);
				for (PlayerInstance p : world.getPlayers())
				{
					if ((p != null) && !p.isDead())
					{
						skill.applyEffects(p, p);
					}
				}
				break;
			}
			case "SECOND_SPAWN":
			{
				final Instance world = player.getInstanceWorld();
				if (!isInInstance(world))
				{
					return null;
				}
				world.spawnGroup(world.getParameters().getInt("worldState", 0) + "_second_spawn");
				startQuestTimer("THIRD_SPAWN", 40000, null, world.getFirstPlayer());
				break;
			}
			case "THIRD_SPAWN":
			{
				final Instance world = player.getInstanceWorld();
				if (!isInInstance(world))
				{
					return null;
				}
				world.spawnGroup(world.getParameters().getInt("worldState", 0) + "_thred_spawn");
				break;
			}
			case "CHANGE_LOCATION":
			{
				final Instance world = player.getInstanceWorld();
				if (!isInInstance(world))
				{
					return null;
				}
				world.getAliveNpcs(TRAPS).forEach(Npc::deleteMe);
				world.spawnGroup(world.getParameters().getInt("worldState", 0) + "_trap_spawn");
				startQuestTimer("CHANGE_LOCATION", 60000 - (world.getParameters().getInt("worldState", 0) * 1430), null, world.getFirstPlayer());
				break;
			}
			case "SWITCH_STAGE":
			{
				final Instance world = player.getInstanceWorld();
				if (!isInInstance(world))
				{
					return null;
				}
				if (world.getAliveNpcs(MONSTERS).isEmpty())
				{
					world.broadcastPacket(new ExShowScreenMessage(NpcStringId.THE_SURROUNDING_ENERGY_HAS_DISSIPATED, ExShowScreenMessage.TOP_CENTER, 5000, true));
					world.broadcastPacket(new Earthquake(player, 50, 5));
					world.openCloseDoor(world.getTemplateParameters().getInt(world.getParameters().getInt("worldState", 0) + "_st_door"), true);
					clean(world.getFirstPlayer());
					if (world.getParameters().getInt("worldState", 0) < 35)
					{
						startQuestTimer("NEXT_STAGE", 5000, null, world.getFirstPlayer());
					}
				}
				break;
			}
			case "NEXT_STAGE":
			{
				final Instance world = player.getInstanceWorld();
				if (!isInInstance(world))
				{
					return null;
				}
				world.broadcastPacket(new ExShowScreenMessage(NpcStringId.S1_SECONDS_HAVE_BEEN_ADDED_TO_THE_INSTANCE_ZONE_DURATION, ExShowScreenMessage.TOP_CENTER, 5000, true, String.valueOf(180)));
				world.setDuration((int) ((world.getRemainingTime() / 60000) + 3));
				startQuestTimer("START_STAGE", 8000, null, world.getFirstPlayer());
				for (Npc n : world.getAliveNpcs())
				{
					if (n.getId() != EINSTER)
					{
						n.deleteMe();
					}
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, PlayerInstance killer, boolean isSummon)
	{
		final Instance world = npc.getInstanceWorld();
		if (isInInstance(world))
		{
			if (CommonUtil.contains(MONSTERS, npc.getId()) && (getRandom(100) < world.getParameters().getDouble("chance", 0)))
			{
				final int worldState = world.getParameters().getInt("worldState", 0);
				if (worldState < 9)
				{
					addSpawn(DIMENSIONAL_IMP, npc, true, 0, false, world.getId());
					world.broadcastPacket(new ExShowScreenMessage(NpcStringId.DIMENSIONAL_IMP, ExShowScreenMessage.TOP_CENTER, 5000, true));
				}
				else if ((worldState >= 9) && (worldState < 20))
				{
					addSpawn(UNWORDLY_IMP, npc, true, 0, false, world.getId());
					world.broadcastPacket(new ExShowScreenMessage(NpcStringId.UNWORLDLY_IMP, ExShowScreenMessage.TOP_CENTER, 5000, true));
				}
				else if (worldState >= 20)
				{
					addSpawn(ABYSSAL_IMP, npc, true, 0, false, world.getId());
					world.broadcastPacket(new ExShowScreenMessage(NpcStringId.ABYSSAL_IMP, ExShowScreenMessage.TOP_CENTER, 5000, true));
				}
			}
			else if (npc.getId() == ABYSSAL_MAKKUM)
			{
				world.broadcastPacket(new ExShowScreenMessage(NpcStringId.THE_INSTANCE_ZONE_WILL_CLOSE_SOON, ExShowScreenMessage.TOP_CENTER, 10000, true));
				world.finishInstance(3);
				clean(world.getFirstPlayer());
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	@Override
	public String onFirstTalk(Npc npc, PlayerInstance player)
	{
		final Instance world = npc.getInstanceWorld();
		if (isInInstance(world) && (world.getParameters().getInt("worldState", 0) == 20))
		{
			return "33975-04.html";
		}
		return "33975.html";
	}
	
	@Override
	public String onSpawn(Npc npc)
	{
		final Instance world = npc.getInstanceWorld();
		if (isInInstance(world))
		{
			switch (npc.getId())
			{
				case SALAMANDRA_GENERATOR:
				{
					startQuestTimer("SALAMANDRA_SPAWN", 25000, null, world.getFirstPlayer(), true);
					startQuestTimer("CHANGE_LOCATION", 60000 - (world.getParameters().getInt("worldState", 0) * 1300), null, world.getFirstPlayer());
					break;
				}
				case SALAMANDRA_GENERATOR_DUMMY:
				{
					startQuestTimer("SALAMANDRA_SPAWN_DUMMY", 20000, null, world.getFirstPlayer(), true);
					break;
				}
				case DEMINSIONAL_INVISIBLE_FRAGMENT:
				{
					startQuestTimer("SWITCH_STAGE", 5000, null, world.getFirstPlayer(), true);
					break;
				}
			}
		}
		return super.onSpawn(npc);
	}
	
	public void checkCrystallCount(Instance world, Npc npc)
	{
		boolean canStart = true;
		for (PlayerInstance p : world.getPlayers())
		{
			if (p.getInventory().getInventoryItemCount(WARP_CRYSTAL, -1) < world.getParameters().getInt("count", 0))
			{
				for (PlayerInstance ps : world.getPlayers())
				{
					final NpcHtmlMessage packet = new NpcHtmlMessage(npc.getObjectId());
					packet.setHtml(getHtm(ps, "33975-03.html"));
					packet.replace("%count%", Integer.toString(world.getParameters().getInt("count", 0)));
					ps.sendPacket(packet);
					ps.sendPacket(new SystemMessage(SystemMessageId.C1_S_ITEM_REQUIREMENT_IS_NOT_SUFFICIENT_AND_CANNOT_BE_ENTERED).addString(ps.getName()));
					canStart = false;
				}
			}
		}
		if (!canStart || (world.getParameters().getInt("worldState", 0) != 0))
		{
			return;
		}
		
		startQuestTimer("START_STAGE", 1000, null, world.getFirstPlayer());
		for (PlayerInstance p : world.getPlayers())
		{
			takeItems(p, WARP_CRYSTAL, world.getParameters().getInt("count", 0));
		}
	}
	
	protected void clean(PlayerInstance player)
	{
		cancelQuestTimer("SWITCH_STAGE", null, player);
		cancelQuestTimer("SALAMANDRA_SPAWN", null, player);
		cancelQuestTimer("SALAMANDRA_SPAWN_DUMMY", null, player);
		cancelQuestTimer("CHANGE_LOCATION", null, player);
		cancelQuestTimer("DEBUFF_TASK", null, player);
	}
	
	public static void main(String[] args)
	{
		new DimensionalWrap();
	}
}
