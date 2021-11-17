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
package instances.ChamberOfProphecies;

import org.l2jmobius.gameserver.ai.CtrlIntention;
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Attackable;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.instance.Monster;
import org.l2jmobius.gameserver.model.instancezone.Instance;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.network.NpcStringId;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;
import org.l2jmobius.gameserver.network.serverpackets.ExShowUsm;
import org.l2jmobius.gameserver.network.serverpackets.NpcSay;
import org.l2jmobius.gameserver.network.serverpackets.PlaySound;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

import instances.AbstractInstance;
import quests.Q10753_WindsOfFateChoices.Q10753_WindsOfFateChoices;

/**
 * Chamber of Prophecies instance.
 * @author Gigi, Mobius
 */
public class ChamberOfProphecies extends AbstractInstance
{
	// NPCs
	private static final int KAIN_VAN_HALTER = 31639;
	private static final int VAN_HALTER = 33999;
	private static final int FERIN = 34001;
	private static final int GRAIL = 33996;
	private static final int MYSTERIOUS_WIZARD = 33980;
	// Misc
	private static final int DOOR_2 = 17230102;
	private static final int DOOR_3 = 17230103;
	private static final int DOOR_4 = 17230104;
	private static final int TEMPLATE_ID = 255;
	private static final int PROPHECY_MACHINE = 39540;
	private static final int ATELIA = 39542;
	private static final Location FIRST_ROOM_LOC = new Location(-88503, 184754, -10440, 48891);
	private static final Location THIRD_ROOM_LOC = new Location(-88506, 177151, -10445, 48891);
	
	public ChamberOfProphecies()
	{
		super(TEMPLATE_ID);
		addStartNpc(KAIN_VAN_HALTER);
		addFirstTalkId(KAIN_VAN_HALTER, GRAIL, MYSTERIOUS_WIZARD);
		addTalkId(KAIN_VAN_HALTER, GRAIL, MYSTERIOUS_WIZARD);
		addCreatureSeeId(FERIN, VAN_HALTER);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = null;
		switch (event)
		{
			case "31639-01.html":
			case "33996-01.html":
			case "33980-01.html":
			case "33980-02.html":
			{
				htmltext = event;
				break;
			}
			case "33996-02.html":
			{
				final Instance world = player.getInstanceWorld();
				if (!isInInstance(world))
				{
					return null;
				}
				world.openCloseDoor(DOOR_4, false);
				world.broadcastPacket(ExShowUsm.USM_Q015_E);
				world.despawnGroup("q10753_16_instance_grail");
				world.spawnGroup("q10753_16_instance_wizard");
				giveItems(player, ATELIA, 1);
				showOnScreenMsg(player, NpcStringId.TALK_TO_THE_MYSTERIOUS_WIZARD, ExShowScreenMessage.TOP_CENTER, 6000);
				htmltext = event;
				break;
			}
			case "33980-03.html":
			{
				showOnScreenMsg(player, NpcStringId.THIS_CHOICE_CANNOT_BE_REVERSED, ExShowScreenMessage.TOP_CENTER, 6000);
				htmltext = event;
				break;
			}
			case "33980-04.html":
			{
				showOnScreenMsg(player, NpcStringId.THIS_CHOICE_CANNOT_BE_REVERSED, ExShowScreenMessage.TOP_CENTER, 6000);
				htmltext = event;
				break;
			}
			case "33980-05.html":
			{
				final Instance world = player.getInstanceWorld();
				if (!isInInstance(world))
				{
					return null;
				}
				world.spawnGroup("q10753_16_instance_halter_2");
				world.setStatus(6);
				startQuestTimer("DESPAWN_WIZARD", 2000, npc, player);
				htmltext = event;
				break;
			}
			case "enterInstance":
			{
				final QuestState qs = player.getQuestState(Q10753_WindsOfFateChoices.class.getSimpleName());
				if (qs != null)
				{
					enterInstance(player, npc, TEMPLATE_ID);
					if (hasQuestItems(player, PROPHECY_MACHINE))
					{
						takeItems(player, PROPHECY_MACHINE, 1);
					}
					qs.setCond(16, true);
				}
				break;
			}
			case "teleport":
			{
				final Instance world = player.getInstanceWorld();
				if (!isInInstance(world))
				{
					return null;
				}
				final Npc valHalter = world.getNpc(VAN_HALTER);
				if (valHalter != null)
				{
					valHalter.deleteMe(); // probably needs another npc id for initial room
				}
				final Npc ferin = world.getNpc(FERIN);
				if (ferin != null)
				{
					ferin.deleteMe(); // probably needs another npc id for initial room
				}
				if (world.isStatus(0) && world.getAliveNpcs(Monster.class).isEmpty())
				{
					world.spawnGroup("q10753_16_instance_halter_1_1");
					world.spawnGroup("wof_room1");
				}
				if (world.getStatus() < 3)
				{
					player.teleToLocation(FIRST_ROOM_LOC);
				}
				else
				{
					player.teleToLocation(THIRD_ROOM_LOC);
				}
				cancelQuestTimer("CHECK_STATUS", npc, player);
				startQuestTimer("CHECK_STATUS", 7000, world.getNpc(KAIN_VAN_HALTER), player);
				break;
			}
			case "status":
			{
				final Instance world = player.getInstanceWorld();
				if (!isInInstance(world))
				{
					return null;
				}
				if (world.getStatus() < 5)
				{
					htmltext = "31639-01.html";
					break;
				}
				htmltext = "31639-02.html";
				break;
			}
			case "CHECK_STATUS":
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
						if (world.getAliveNpcs(Monster.class).isEmpty())
						{
							startQuestTimer("SEY2", 14000, world.getNpc(FERIN), player);
							startQuestTimer("SEY_KAIN", 24000, world.getNpc(VAN_HALTER), player);
							startQuestTimer("OPEN_DOOR1", 5000, npc, player);
						}
						startQuestTimer("CHECK_STATUS", 7000, npc, player);
						break;
					}
					case 1:
					{
						if (world.getAliveNpcs(Monster.class).isEmpty())
						{
							world.spawnGroup("wof_room2_1");
							world.setStatus(2);
						}
						startQuestTimer("CHECK_STATUS", 7000, npc, player);
						break;
					}
					case 2:
					{
						if (world.getAliveNpcs(Monster.class).isEmpty())
						{
							startQuestTimer("SEY3", 8000, world.getNpc(FERIN), player);
							startQuestTimer("OPEN_DOOR2", 5000, npc, player);
						}
						startQuestTimer("CHECK_STATUS", 7000, npc, player);
						break;
					}
					case 3:
					{
						if (world.getAliveNpcs(Monster.class).isEmpty())
						{
							world.setStatus(4);
							world.spawnGroup("wof_room3_2");
							world.openCloseDoor(DOOR_3, false);
							startQuestTimer("SEY_KAIN_1", 5000, world.getNpc(VAN_HALTER), player);
						}
						startQuestTimer("CHECK_STATUS", 7000, npc, player);
						break;
					}
					case 4:
					{
						if (world.getAliveNpcs(Monster.class).isEmpty())
						{
							world.setStatus(5);
							world.spawnGroup("wof_room4");
							startQuestTimer("SEY_KAIN_2", 3000, world.getNpc(VAN_HALTER), player);
							startQuestTimer("SEY4", 7000, world.getNpc(FERIN), player);
						}
						else
						{
							startQuestTimer("CHECK_STATUS", 7000, npc, player);
						}
						break;
					}
				}
				break;
			}
			case "ATTACK":
			case "ATTACK1":
			case "ATTACK2":
			{
				final Instance world = player.getInstanceWorld();
				if (!isInInstance(world) || (npc == null))
				{
					return null;
				}
				npc.setRunning();
				((Attackable) npc).setCanReturnToSpawnPoint(false);
				if (npc.isScriptValue(0) && world.getAliveNpcs(Monster.class).isEmpty())
				{
					npc.setTarget(player);
					npc.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, player);
				}
				else if (npc.getAI().getIntention() != CtrlIntention.AI_INTENTION_ATTACK)
				{
					World.getInstance().forEachVisibleObjectInRange(npc, Monster.class, 3000, monster ->
					{
						addAttackDesire(npc, monster);
						return;
					});
				}
				break;
			}
			case "OPEN_DOOR1":
			{
				final Instance world = player.getInstanceWorld();
				if (!isInInstance(world))
				{
					return null;
				}
				cancelQuestTimer("ATTACK", npc, player);
				world.setStatus(1);
				world.openCloseDoor(DOOR_2, true);
				world.spawnGroup("wof_room2");
				break;
			}
			case "OPEN_DOOR2":
			{
				final Instance world = player.getInstanceWorld();
				if (!isInInstance(world))
				{
					return null;
				}
				cancelQuestTimer("ATTACK1", npc, player);
				startQuestTimer("ATTACK2", 200, world.getNpc(VAN_HALTER), player, true);
				world.setStatus(3);
				world.spawnGroup("wof_room3");
				world.openCloseDoor(DOOR_3, true);
				break;
			}
			case "BROADCAST_TEXT":
			{
				npc.setTarget(player);
				npc.setRunning();
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, player);
				npc.broadcastPacket(new NpcSay(npc.getObjectId(), ChatType.NPC_GENERAL, npc.getId(), NpcStringId.THAT_GUY_KAIN_HAS_A_SMARMY_FACE));
				player.sendPacket(new PlaySound(3, "Npcdialog1.apple_quest_7", 0, 0, 0, 0, 0));
				break;
			}
			case "SEY2":
			{
				if ((npc != null) && (npc.getId() == FERIN))
				{
					npc.broadcastPacket(new NpcSay(npc.getObjectId(), ChatType.NPC_GENERAL, npc.getId(), NpcStringId.YOU_CAN_T_DIE_HERE_I_DIDN_T_LEARN_RESURRECT_YET));
					player.sendPacket(new PlaySound(3, "Npcdialog1.apple_quest_4", 0, 0, 0, 0, 0));
				}
				break;
			}
			case "SEY_KAIN":
			{
				if ((npc != null) && (npc.getId() == VAN_HALTER))
				{
					npc.broadcastPacket(new NpcSay(npc.getObjectId(), ChatType.NPC_GENERAL, npc.getId(), NpcStringId.GISELLE_WAS_SUCH_A_SWEET_CHILD));
					player.sendPacket(new PlaySound(3, "Npcdialog1.holter_quest_1", 0, 0, 0, 0, 0));
				}
				startQuestTimer("ATTACK1", 200, npc, player, true);
				break;
			}
			case "SEY3":
			{
				if ((npc != null) && (npc.getId() == FERIN))
				{
					npc.broadcastPacket(new NpcSay(npc.getObjectId(), ChatType.NPC_GENERAL, npc.getId(), NpcStringId.DO_YOU_THINK_I_LL_GROW_TALLER_IF_I_EAT_LOTS_AND_LOTS));
					player.sendPacket(new PlaySound(3, "Npcdialog1.apple_quest_6", 0, 0, 0, 0, 0));
				}
				break;
			}
			case "SEY_KAIN_1":
			{
				if ((npc != null) && (npc.getId() == VAN_HALTER))
				{
					npc.broadcastPacket(new NpcSay(npc.getObjectId(), ChatType.NPC_GENERAL, npc.getId(), NpcStringId.SUCH_MONSTERS_IN_A_PLACE_LIKE_THIS_UNBELIEVABLE));
				}
				break;
			}
			case "SEY_KAIN_2":
			{
				if ((npc != null) && (npc.getId() == VAN_HALTER))
				{
					npc.broadcastPacket(new NpcSay(npc.getObjectId(), ChatType.NPC_GENERAL, npc.getId(), NpcStringId.THAT_S_THE_MONSTER_THAT_ATTACKED_FAERON_YOU_RE_OUTMATCHED_HERE_GO_AHEAD_I_LL_CATCH_UP));
					player.sendPacket(new PlaySound(3, "Npcdialog1.holter_quest_6", 0, 0, 0, 0, 0));
				}
				startQuestTimer("SEY_KAIN_3", 7000, npc, player);
				break;
			}
			case "SEY4":
			{
				if ((npc != null) && (npc.getId() == FERIN))
				{
					npc.broadcastPacket(new NpcSay(npc.getObjectId(), ChatType.NPC_GENERAL, npc.getId(), NpcStringId.GO_NOW_KAIN_CAN_HANDLE_THIS));
					npc.setScriptValue(1);
				}
				startQuestTimer("REST", 5000, npc, player);
				break;
			}
			case "SEY_KAIN_3":
			{
				if ((npc != null) && (npc.getId() == VAN_HALTER))
				{
					npc.broadcastPacket(new NpcSay(npc.getObjectId(), ChatType.NPC_GENERAL, npc.getId(), NpcStringId.LEAVE_THIS_TO_ME_GO));
					npc.setScriptValue(1);
				}
				startQuestTimer("SEY_KAIN_4", 1000, npc, player);
				break;
			}
			case "REST":
			{
				if ((npc != null) && (npc.getId() == FERIN))
				{
					npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, player);
				}
				cancelQuestTimer("BROADCAST_TEXT", npc, player);
				break;
			}
			case "SEY_KAIN_4":
			{
				final Instance world = player.getInstanceWorld();
				if (!isInInstance(world))
				{
					return null;
				}
				world.setStatus(5);
				world.spawnGroup("q10753_16_instance_grail");
				showOnScreenMsg(player, NpcStringId.LEAVE_THIS_PLACE_TO_KAIN_NGO_TO_THE_NEXT_ROOM, ExShowScreenMessage.TOP_CENTER, 6000);
				world.openCloseDoor(DOOR_4, true);
				cancelQuestTimer("ATTACK2", npc, player);
				if (npc != null)
				{
					npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, player);
				}
				startQuestTimer("CLOSE", 15000, null, player);
				break;
			}
			case "CLOSE":
			{
				final Instance world = player.getInstanceWorld();
				if (!isInInstance(world))
				{
					return null;
				}
				final Npc grail = world.getNpc(GRAIL);
				if ((grail != null) && (player.calculateDistance2D(grail) < 390))
				{
					world.openCloseDoor(DOOR_4, false);
					world.despawnGroup("q10753_16_instance_halter_1_1");
					world.despawnGroup("wof_room4");
				}
				else
				{
					startQuestTimer("CLOSE", 3000, npc, player);
				}
				break;
			}
			case "DESPAWN_WIZARD":
			{
				final Instance world = player.getInstanceWorld();
				if (!isInInstance(world))
				{
					return null;
				}
				world.despawnGroup("q10753_16_instance_wizard");
				break;
			}
			case "exit":
			{
				startQuestTimer("finish", 3000, npc, player);
				player.sendPacket(new SystemMessage(SystemMessageId.THIS_DUNGEON_WILL_EXPIRE_IN_S1_MINUTE_S_YOU_WILL_BE_FORCED_OUT_OF_THE_DUNGEON_WHEN_THE_TIME_EXPIRES).addInt(1));
				final QuestState qs = player.getQuestState(Q10753_WindsOfFateChoices.class.getSimpleName());
				if (qs != null)
				{
					qs.setCond(17, true);
				}
				break;
			}
			case "finish":
			{
				final Instance world = player.getInstanceWorld();
				if (!isInInstance(world))
				{
					return null;
				}
				world.finishInstance(0);
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		final QuestState qs = player.getQuestState(Q10753_WindsOfFateChoices.class.getSimpleName());
		String htmltext = null;
		switch (npc.getId())
		{
			case KAIN_VAN_HALTER:
			{
				if ((qs != null) && qs.isCond(16))
				{
					htmltext = "31639.html";
				}
				break;
			}
			case GRAIL:
			{
				htmltext = "33996.html";
				break;
			}
			case MYSTERIOUS_WIZARD:
			{
				if ((qs != null) && qs.isCond(16))
				{
					htmltext = "33980.html";
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onCreatureSee(Npc npc, Creature creature)
	{
		final Instance world = npc.getInstanceWorld();
		if (world != null)
		{
			switch (npc.getId())
			{
				case FERIN:
				{
					if (creature.isPlayer() && !creature.isDead() && npc.isScriptValue(0))
					{
						startQuestTimer("BROADCAST_TEXT", 12000, npc, (Player) creature);
					}
					break;
				}
				case VAN_HALTER:
				{
					if (creature.isPlayer() && !creature.isDead() && world.isStatus(0))
					{
						startQuestTimer("ATTACK", 2000, npc, (Player) creature, true);
					}
					break;
				}
			}
		}
		return super.onCreatureSee(npc, creature);
	}
	
	public static void main(String[] args)
	{
		new ChamberOfProphecies();
	}
}