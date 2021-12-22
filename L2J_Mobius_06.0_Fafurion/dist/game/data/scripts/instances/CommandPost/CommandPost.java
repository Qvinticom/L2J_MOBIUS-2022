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
package instances.CommandPost;

import java.util.List;

import org.l2jmobius.commons.util.Chronos;
import org.l2jmobius.commons.util.CommonUtil;
import org.l2jmobius.gameserver.instancemanager.InstanceManager;
import org.l2jmobius.gameserver.instancemanager.ZoneManager;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.Party;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.instancezone.Instance;
import org.l2jmobius.gameserver.model.zone.ZoneId;
import org.l2jmobius.gameserver.model.zone.ZoneType;
import org.l2jmobius.gameserver.model.zone.type.ScriptZone;
import org.l2jmobius.gameserver.network.NpcStringId;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

import instances.AbstractInstance;

/**
 * @author NasSeKa
 */
public class CommandPost extends AbstractInstance
{
	// NPCs
	private static final int DEVIANNE = 34089;
	private static final int GEORK = 26135;
	private static final int BURNSTEIN = 26136;
	private static final int ELITE_KNIGHT = 23605;
	private static final int ELITE_WARRIOR = 23606;
	private static final int ELITE_ARCHER = 23607;
	private static final int ELITE_WIZARD = 23608;
	private static final int ADOLPH = 23590;
	private static final int BARTON = 23591;
	private static final int HAYUK = 23592;
	private static final int ELISE = 23593;
	private static final int ELRYAH = 23594;
	private static final int[] FIRST_FLOOR =
	{
		23595,
		23596,
		23597,
		23600,
	};
	private static final int[] GROUP_1 =
	{
		23605,
		23606,
		23607,
		23608,
	};
	private static final int[] GROUP_2 =
	{
		23590,
		23591,
		23592,
		23593,
		23594,
	};
	private static final int[] GROUP_3 =
	{
		23605,
		23606,
		23607,
		23608,
	};
	private static final int[] GROUP_4 =
	{
		23610,
		23612,
		23613,
		23614,
		23615,
	};
	// Items
	// private static final int EMERGENCY_WHISTLE = 46404;
	// Location
	private static final Location FLOOR_2_SPAWN = new Location(-44037, 44009, -8097);
	private static final Location FLOOR_3_SPAWN = new Location(-44035, 45439, -6971);
	private static final Location GEORK_FLOOR_2_SPAWN = new Location(-44035, 45365, -8031);
	private static final Location GROUP_1_MOVE = new Location(-43540, 44519, -8097);
	private static final Location GROUP_2_MOVE = new Location(-43514, 44116, -8097);
	private static final Location GROUP_3_MOVE = new Location(-44532, 44510, -8097);
	private static final Location GROUP_4_MOVE = new Location(-44532, 44109, -8097);
	private static final Location ADOLPH_MOVE = new Location(-44020, 45085, -8097);
	private static final Location BARTON_MOVE = new Location(-43922, 44994, -8097);
	private static final Location HAYUK_MOVE = new Location(-44014, 44998, -8097);
	private static final Location ELISE_MOVE = new Location(-44120, 44999, -8097);
	private static final Location ELRYAH_MOVE = new Location(-44072, 45004, -8097);
	// Zones
	private static final ScriptZone FLOOR_1_TP = ZoneManager.getInstance().getZoneById(25901, ScriptZone.class);
	private static final ScriptZone FLOOR_2_TP = ZoneManager.getInstance().getZoneById(25902, ScriptZone.class);
	private static final ScriptZone FLOOR_2_START = ZoneManager.getInstance().getZoneById(25903, ScriptZone.class);
	// Misc
	private static final int TEMPLATE_ID = 259;
	
	public CommandPost()
	{
		super(TEMPLATE_ID);
		addStartNpc(DEVIANNE);
		addTalkId(DEVIANNE);
		addEnterZoneId(FLOOR_1_TP.getId(), FLOOR_2_TP.getId(), FLOOR_2_START.getId());
		addMoveFinishedId(GROUP_1);
		addMoveFinishedId(GROUP_2);
		addMoveFinishedId(GROUP_3);
		addMoveFinishedId(GROUP_4);
		addKillId(GEORK, BURNSTEIN);
		addInstanceLeaveId(TEMPLATE_ID);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		switch (event)
		{
			case "enterInstance":
			{
				final Party party = player.getParty();
				if (player.isInParty())
				{
					final long currentTime = Chronos.currentTimeMillis();
					
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
					
					final List<Player> members = party.getMembers();
					for (Player member : members)
					{
						if (!member.isInsideRadius3D(npc, 1000))
						{
							player.sendMessage("Player " + member.getName() + " must go closer to Gatekeeper Spirit.");
							return null;
						}
						
						if (currentTime < InstanceManager.getInstance().getInstanceTime(member, TEMPLATE_ID))
						{
							final SystemMessage msg = new SystemMessage(SystemMessageId.C1_YOU_HAVE_ENTERED_ANOTHER_INSTANT_ZONE_THEREFORE_YOU_CANNOT_ENTER_CORRESPONDING_DUNGEON);
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
					startQuestTimer("check_status", 3000, null, player);
				}
				break;
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
						world.spawnGroup("geork");
						if (world.getNpc(GEORK) != null)
						{
							world.getNpc(GEORK).setInvul(true);
							world.getNpc(GEORK).setImmobilized(true);
							world.getNpc(GEORK).setRandomWalking(false);
							world.getNpc(GEORK).setTargetable(false);
						}
						startQuestTimer("check_status", 3000, null, player);
						break;
					}
					case 1:
					{
						if (world.getAliveNpcs(FIRST_FLOOR).isEmpty())
						{
							showOnScreenMsg(world, NpcStringId.THE_TELEPORT_GATE_TO_THE_2ND_FLOOR_HAS_BEEN_ACTIVATED, ExShowScreenMessage.TOP_CENTER, 2000, true);
							world.setStatus(2);
							world.getNpc(GEORK).teleToLocation(GEORK_FLOOR_2_SPAWN);
							for (Npc monster : world.spawnGroup("group_1"))
							{
								monster.setInvul(true);
								monster.setImmobilized(true);
								monster.setRandomWalking(false);
								monster.setTargetable(false);
								monster.setScriptValue(1);
							}
							for (Npc monster : world.spawnGroup("group_2"))
							{
								monster.setInvul(true);
								monster.setImmobilized(true);
								monster.setRandomWalking(false);
								monster.setTargetable(false);
							}
							for (Npc monster : world.spawnGroup("group_3"))
							{
								monster.setInvul(true);
								monster.setImmobilized(true);
								monster.setRandomWalking(false);
								monster.setTargetable(false);
							}
							for (Npc monster : world.spawnGroup("group_4"))
							{
								monster.setInvul(true);
								monster.setImmobilized(true);
								monster.setRandomWalking(false);
								monster.setTargetable(false);
							}
						}
						startQuestTimer("check_status", 3000, null, player);
						break;
					}
					case 2:
					{
						if (!player.isGM())
						{
							final Party party = player.getParty();
							final List<Player> members = party.getMembers();
							for (Player member : members)
							{
								if (FLOOR_1_TP.isInsideZone(member))
								{
									member.teleToLocation(FLOOR_2_SPAWN);
								}
							}
						}
						else if (FLOOR_1_TP.isInsideZone(player))
						{
							player.teleToLocation(FLOOR_2_SPAWN);
						}
						
						startQuestTimer("check_status", 3000, null, player);
						break;
					}
					case 3:
					{
						for (Npc monster : world.getNpcsOfGroup("group_1"))
						{
							monster.setImmobilized(false);
							monster.setWalking();
							monster.getAI().moveTo(GROUP_1_MOVE);
						}
						
						startQuestTimer("check_status", 3000, null, player);
						break;
					}
					case 4:
					{
						if ((world.getStatus() == 4) && //
							(!world.getNpc(ELITE_KNIGHT).isInsideZone(ZoneId.SCRIPT)) && //
							(!world.getNpc(ELITE_WARRIOR).isInsideZone(ZoneId.SCRIPT)) && //
							(!world.getNpc(ELITE_ARCHER).isInsideZone(ZoneId.SCRIPT)) && //
							(!world.getNpc(ELITE_WIZARD).isInsideZone(ZoneId.SCRIPT)))
						{
							for (Npc monster : world.getNpcsOfGroup("group_2"))
							{
								monster.setImmobilized(false);
								monster.setWalking();
								monster.getAI().moveTo(GROUP_2_MOVE);
							}
							world.openCloseDoor(world.getTemplateParameters().getInt("secondGroupId"), true);
							world.setStatus(5);
						}
						
						startQuestTimer("check_status", 3000, null, player);
						break;
					}
					case 5:
					{
						if (world.getAliveNpcs(GROUP_2).isEmpty())
						{
							for (Npc monster : world.getNpcsOfGroup("group_3"))
							{
								monster.setImmobilized(false);
								monster.setWalking();
								monster.getAI().moveTo(GROUP_3_MOVE);
							}
							world.openCloseDoor(world.getTemplateParameters().getInt("thirdGroupId"), true);
							world.setStatus(6);
						}
						
						startQuestTimer("check_status", 3000, null, player);
						break;
					}
					case 6:
					{
						if (world.getAliveNpcs(GROUP_3).isEmpty())
						{
							for (Npc monster : world.getNpcsOfGroup("group_4"))
							{
								monster.setImmobilized(false);
								monster.setWalking();
								monster.getAI().moveTo(GROUP_4_MOVE);
							}
							world.openCloseDoor(world.getTemplateParameters().getInt("fourthGroupId"), true);
							world.setStatus(7);
						}
						
						startQuestTimer("check_status", 3000, null, player);
						break;
					}
					case 7:
					{
						// if (world.getAliveNpcs(GROUP_4).isEmpty())
						// {
						// System.out.println("Status is 7.");
						// }
						
						startQuestTimer("check_status", 3000, null, player);
						break;
					}
					default:
					{
						startQuestTimer("check_status", 3000, null, player);
					}
				}
				break;
			}
		}
		return null;
	}
	
	@Override
	public void onMoveFinished(Npc npc)
	{
		final Instance world = npc.getInstanceWorld();
		if (world != null)
		{
			npc.setInvul(false);
			npc.setTargetable(true);
			if (CommonUtil.contains(GROUP_1, npc.getId()))
			{
				world.setStatus(4);
			}
		}
		super.onMoveFinished(npc);
	}
	
	@Override
	public String onEnterZone(Creature creature, ZoneType zone)
	{
		final Instance world = creature.getInstanceWorld();
		if (world != null)
		{
			switch (zone.getId())
			{
				case 25901:
				{
					if (creature.isPlayer() && isInInstance(world) && (world.getStatus() >= 2))
					{
						creature.teleToLocation(FLOOR_2_SPAWN);
					}
					break;
				}
				case 25902:
				{
					if (creature.isPlayer() && isInInstance(world) && (world.getStatus() >= 3))
					{
						creature.teleToLocation(FLOOR_3_SPAWN);
					}
					break;
				}
				case 25903:
				{
					if (creature.isPlayer() && isInInstance(world) && (world.getStatus() == 2))
					{
						world.setStatus(3);
						world.openCloseDoor(world.getTemplateParameters().getInt("firstGroupId"), true);
					}
					break;
				}
			}
		}
		return super.onEnterZone(creature, zone);
	}
	
	@Override
	public String onKill(Npc npc, Player player, boolean isPet)
	{
		final Instance world = npc.getInstanceWorld();
		if (isInInstance(world))
		{
			switch (npc.getId())
			{
				case BURNSTEIN:
				{
					world.finishInstance();
					break;
				}
				case ADOLPH:
				{
					world.spawnGroup("adolph");
					world.getNpc(ADOLPH).setInvul(true);
					world.getNpc(ADOLPH).setRandomWalking(false);
					world.getNpc(ADOLPH).setTargetable(false);
					addMoveToDesire(world.getNpc(ADOLPH), ADOLPH_MOVE, 6);
					break;
				}
				case BARTON:
				{
					world.spawnGroup("barton");
					world.getNpc(BARTON).setInvul(true);
					world.getNpc(BARTON).setRandomWalking(false);
					world.getNpc(BARTON).setTargetable(false);
					addMoveToDesire(world.getNpc(BARTON), BARTON_MOVE, 6);
					break;
				}
				case HAYUK:
				{
					world.spawnGroup("hayuk");
					world.getNpc(HAYUK).setInvul(true);
					world.getNpc(HAYUK).setRandomWalking(false);
					world.getNpc(HAYUK).setTargetable(false);
					addMoveToDesire(world.getNpc(HAYUK), HAYUK_MOVE, 6);
					break;
				}
				case ELISE:
				{
					world.spawnGroup("elise");
					world.getNpc(ELISE).setInvul(true);
					world.getNpc(ELISE).setRandomWalking(false);
					world.getNpc(ELISE).setTargetable(false);
					addMoveToDesire(world.getNpc(ELISE), ELISE_MOVE, 6);
					break;
				}
				case ELRYAH:
				{
					world.spawnGroup("elryah");
					world.getNpc(ELRYAH).setInvul(true);
					world.getNpc(ELRYAH).setRandomWalking(false);
					world.getNpc(ELRYAH).setTargetable(false);
					addMoveToDesire(world.getNpc(ELRYAH), ELRYAH_MOVE, 6);
					break;
				}
			}
			startQuestTimer("check_status", 3000, null, player);
		}
		return super.onKill(npc, player, isPet);
	}
	
	public static void main(String[] args)
	{
		new CommandPost();
	}
}
