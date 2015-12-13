/*
 * Copyright (C) 2004-2015 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package instances.KartiasLabyrinth;

import instances.AbstractInstance;
import instances.KartiasLabyrinth.AI.Fighter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import quests.Q00494_IncarnationOfGreedZellakaGroup.Q00494_IncarnationOfGreedZellakaGroup;
import quests.Q00495_IncarnationOfJealousyPellineGroup.Q00495_IncarnationOfJealousyPellineGroup;
import quests.Q00496_IncarnationOfGluttonyKaliosGroup.Q00496_IncarnationOfGluttonyKaliosGroup;
import quests.Q00497_IncarnationOfGreedZellakaSolo.Q00497_IncarnationOfGreedZellakaSolo;
import quests.Q00498_IncarnationOfJealousyPellineSolo.Q00498_IncarnationOfJealousyPellineSolo;
import quests.Q00499_IncarnationOfGluttonyKaliosSolo.Q00499_IncarnationOfGluttonyKaliosSolo;

import com.l2jserver.Config;
import com.l2jserver.gameserver.ThreadPoolManager;
import com.l2jserver.gameserver.instancemanager.InstanceManager;
import com.l2jserver.gameserver.model.Location;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.actor.instance.L2QuestGuardInstance;
import com.l2jserver.gameserver.model.instancezone.InstanceWorld;
import com.l2jserver.gameserver.model.quest.QuestState;
import com.l2jserver.gameserver.model.quest.State;
import com.l2jserver.gameserver.model.skills.AbnormalVisualEffect;
import com.l2jserver.gameserver.network.NpcStringId;
import com.l2jserver.gameserver.network.serverpackets.ExShowScreenMessage;
import com.l2jserver.gameserver.util.Util;

/**
 * Kartia's Labyrinth
 * @author Mobius
 */
public final class KartiasLabyrinth extends AbstractInstance
{
	// NPCs
	private static final int RESEARCHER = 33647;
	private static final int ADOLPH = 33608;
	private static final int BARTON = 33610;
	private static final int HAYUK = 33612;
	private static final int ELIYAH = 33614;
	private static final int ELISE = 33616;
	private static final int ADOLPH_88 = 33609;
	private static final int BARTON_88 = 33611;
	private static final int HAYUK_88 = 33613;
	private static final int ELIYAH_88 = 33615;
	private static final int ELISE_88 = 33617;
	private static final int ADOLPH_93 = 33620;
	private static final int BARTON_93 = 33622;
	private static final int HAYUK_93 = 33624;
	private static final int ELIYAH_93 = 33626;
	private static final int ELISE_93 = 33628;
	private static final int ADOLPH_98 = 33631;
	private static final int BARTON_98 = 33633;
	private static final int HAYUK_98 = 33635;
	private static final int ELIYAH_98 = 33637;
	private static final int ELISE_98 = 33639;
	private static final List<Integer> BOSSES = new ArrayList<>();
	static
	{
		BOSSES.add(19253);
		BOSSES.add(19254);
		BOSSES.add(19255);
		BOSSES.add(25882);
		BOSSES.add(25883);
		BOSSES.add(25884);
	}
	private static final List<Integer> MONSTERS = new ArrayList<>();
	static
	{
		MONSTERS.add(19220);
		MONSTERS.add(19221);
		MONSTERS.add(19222);
		MONSTERS.add(19223);
		MONSTERS.add(19224);
		MONSTERS.add(19225);
		MONSTERS.add(19226);
		MONSTERS.add(19227);
		MONSTERS.add(19228);
		MONSTERS.add(19229);
		MONSTERS.add(19230);
		MONSTERS.add(19231);
		MONSTERS.add(19232);
		MONSTERS.add(19233);
		MONSTERS.add(19234);
		MONSTERS.add(19235);
		MONSTERS.add(19236);
		MONSTERS.add(19237);
	}
	// Locations
	private static final Location START_LOC = new Location(-107481, -10435, -12064);
	private static final Location VANGUARD_SPAWN_LOC = new Location(-109032, -10440, -11949, 32800);
	// Misc
	private static final int DOOR_1 = 16170002;
	private static final int DOOR_2 = 16170003;
	private static final int SOLO_85_TEMPLATE_ID = 205;
	private static final int SOLO_90_TEMPLATE_ID = 206;
	private static final int SOLO_95_TEMPLATE_ID = 207;
	private static final int PARTY_85_TEMPLATE_ID = 208;
	private static final int PARTY_90_TEMPLATE_ID = 209;
	private static final int PARTY_95_TEMPLATE_ID = 210;
	private static final int MIN_LVL_85 = 85;
	private static final int MIN_LVL_90 = 90;
	private static final int MIN_LVL_95 = 95;
	private static final int MAX_LVL_85 = 89;
	private static final int MAX_LVL_90 = 94;
	private static final int MAX_LVL_95 = 99;
	private static final String KARTIA_ENTRY_VAR = "Last_Kartia_entry";
	private static final String KARTIA_PARTY_ENTRY_VAR = "Last_Kartia_party_entry";
	
	protected class KartiaWorld extends InstanceWorld
	{
		protected L2QuestGuardInstance adolph = null;
		protected L2QuestGuardInstance barton = null;
		protected L2QuestGuardInstance hayuk = null;
		protected L2QuestGuardInstance eliyah = null;
		protected L2QuestGuardInstance elise = null;
		protected List<L2PcInstance> playersInside = new ArrayList<>();
		protected List<L2Npc> savedSpawns = new CopyOnWriteArrayList<>();
	}
	
	public KartiasLabyrinth()
	{
		super(KartiasLabyrinth.class.getSimpleName());
		addStartNpc(RESEARCHER, ADOLPH);
		addFirstTalkId(RESEARCHER, ADOLPH, BARTON, HAYUK, ELIYAH, ELISE);
		addTalkId(RESEARCHER, ADOLPH, BARTON, HAYUK, ELIYAH, ELISE);
		addKillId(MONSTERS);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = null;
		switch (event)
		{
			case "33608-1.htm":
			case "33608-2.htm":
			case "33647-1.htm":
			case "33647-2.htm":
			case "33647-3.htm":
			case "33647-4.htm":
			{
				htmltext = event;
				break;
			}
			case "request_zellaka_solo":
			{
				if (!checkConditions(player, SOLO_85_TEMPLATE_ID))
				{
					htmltext = "33647-8.htm";
				}
				else if ((player.getLevel() >= MIN_LVL_85) && (player.getLevel() <= MAX_LVL_85))
				{
					enterInstance(player, new KartiaWorld(), "KartiasLabyrinthIncarnationOfGreedZellaka.xml", SOLO_85_TEMPLATE_ID);
					htmltext = "33647-5.htm";
				}
				else
				{
					htmltext = "33647-6.htm";
				}
				break;
			}
			case "request_pelline_solo":
			{
				if (!checkConditions(player, SOLO_90_TEMPLATE_ID))
				{
					htmltext = "33647-8.htm";
				}
				else if ((player.getLevel() >= MIN_LVL_90) && (player.getLevel() <= MAX_LVL_90))
				{
					enterInstance(player, new KartiaWorld(), "KartiasLabyrinthIncarnationOfJealousyPelline.xml", SOLO_90_TEMPLATE_ID);
					htmltext = "33647-5.htm";
				}
				else
				{
					htmltext = "33647-6.htm";
				}
				break;
			}
			case "request_kalios_solo":
			{
				if (!checkConditions(player, SOLO_95_TEMPLATE_ID))
				{
					htmltext = "33647-8.htm";
				}
				else if ((player.getLevel() >= MIN_LVL_95) && (player.getLevel() <= MAX_LVL_95))
				{
					enterInstance(player, new KartiaWorld(), "KartiasLabyrinthIncarnationOfGluttonyKalios.xml", SOLO_95_TEMPLATE_ID);
					htmltext = "33647-5.htm";
				}
				else
				{
					htmltext = "33647-6.htm";
				}
				break;
			}
			case "request_zellaka_party":
			{
				if ((player.getParty() == null) || (!player.getParty().isLeader(player)))
				{
					htmltext = "33647-7.htm";
				}
				else
				{
					for (L2PcInstance member : player.getParty().getMembers())
					{
						if (!checkConditions(member, PARTY_85_TEMPLATE_ID) || (member.getLevel() < MIN_LVL_85) || (member.getLevel() > MAX_LVL_85) || (Util.calculateDistance(player, member, false, false) > 500))
						{
							return "33647-8.htm";
						}
					}
					final KartiaWorld world = new KartiaWorld();
					for (L2PcInstance member : player.getParty().getMembers())
					{
						enterInstance(member, world, "KartiasLabyrinthIncarnationOfGreedZellakaParty.xml", PARTY_85_TEMPLATE_ID);
					}
					htmltext = "33647-5.htm";
				}
				break;
			}
			case "request_pelline_party":
			{
				if ((player.getParty() == null) || (!player.getParty().isLeader(player)))
				{
					htmltext = "33647-7.htm";
				}
				else
				{
					for (L2PcInstance member : player.getParty().getMembers())
					{
						if (!checkConditions(member, PARTY_90_TEMPLATE_ID) || (member.getLevel() < MIN_LVL_90) || (member.getLevel() > MAX_LVL_90) || (Util.calculateDistance(player, member, false, false) > 500))
						{
							return "33647-8.htm";
						}
					}
					final KartiaWorld world = new KartiaWorld();
					for (L2PcInstance member : player.getParty().getMembers())
					{
						enterInstance(member, world, "KartiasLabyrinthIncarnationOfJealousyPellineParty.xml", PARTY_90_TEMPLATE_ID);
					}
					htmltext = "33647-5.htm";
				}
				break;
			}
			case "request_kalios_party":
			{
				if ((player.getParty() == null) || (!player.getParty().isLeader(player)))
				{
					htmltext = "33647-7.htm";
				}
				else
				{
					for (L2PcInstance member : player.getParty().getMembers())
					{
						if (!checkConditions(member, PARTY_95_TEMPLATE_ID) || (member.getLevel() < MIN_LVL_95) || (member.getLevel() > MAX_LVL_95) || (Util.calculateDistance(player, member, false, false) > 500))
						{
							return "33647-8.htm";
						}
					}
					final KartiaWorld world = new KartiaWorld();
					for (L2PcInstance member : player.getParty().getMembers())
					{
						enterInstance(member, world, "KartiasLabyrinthIncarnationOfGluttonyKaliosParty.xml", PARTY_95_TEMPLATE_ID);
					}
					htmltext = "33647-5.htm";
				}
				break;
			}
			case "remove_barton":
			{
				htmltext = removeVanguard(player, BARTON);
				break;
			}
			case "remove_hayuk":
			{
				htmltext = removeVanguard(player, HAYUK);
				break;
			}
			case "remove_eliyah":
			{
				htmltext = removeVanguard(player, ELIYAH);
				break;
			}
			case "remove_elise":
			{
				htmltext = removeVanguard(player, ELISE);
				break;
			}
			case "checkStatus":
			{
				final InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
				if ((world == null) || !(world instanceof KartiaWorld) || (world.getStatus() > 28))
				{
					cancelQuestTimers("checkStatus");
					return null;
				}
				// Remove monsters killed by guards.
				for (L2Npc spawn : ((KartiaWorld) world).savedSpawns)
				{
					if (spawn.isDead())
					{
						((KartiaWorld) world).savedSpawns.remove(spawn);
					}
				}
				// Act according to world status.
				switch (world.getStatus())
				{
					case 2:
					{
						if (((KartiaWorld) world).savedSpawns.isEmpty())
						{
							world.incStatus();
							((KartiaWorld) world).savedSpawns.addAll(spawnGroup("wave2", world.getInstanceId()));
						}
						break;
					}
					case 3:
					{
						if (((KartiaWorld) world).savedSpawns.isEmpty())
						{
							world.incStatus();
							((KartiaWorld) world).savedSpawns.addAll(spawnGroup("wave3", world.getInstanceId()));
						}
						break;
					}
					case 4:
					{
						if (((KartiaWorld) world).savedSpawns.isEmpty())
						{
							world.incStatus();
							((KartiaWorld) world).savedSpawns.addAll(spawnGroup("wave4", world.getInstanceId()));
						}
						break;
					}
					case 5:
					{
						if (((KartiaWorld) world).savedSpawns.isEmpty())
						{
							world.incStatus();
							((KartiaWorld) world).savedSpawns.addAll(spawnGroup("wave5", world.getInstanceId()));
						}
						break;
					}
					case 6:
					{
						if (((KartiaWorld) world).savedSpawns.isEmpty())
						{
							world.incStatus();
							((KartiaWorld) world).savedSpawns.addAll(spawnGroup("wave6", world.getInstanceId()));
						}
						break;
					}
					case 7:
					{
						if (((KartiaWorld) world).savedSpawns.isEmpty())
						{
							world.incStatus();
							((KartiaWorld) world).savedSpawns.addAll(spawnGroup("wave7", world.getInstanceId()));
						}
						break;
					}
					case 8:
					{
						if (((KartiaWorld) world).savedSpawns.isEmpty())
						{
							world.incStatus();
							((KartiaWorld) world).savedSpawns.addAll(spawnGroup("wave8", world.getInstanceId()));
						}
						break;
					}
					case 9:
					{
						if (((KartiaWorld) world).savedSpawns.isEmpty())
						{
							world.incStatus();
							((KartiaWorld) world).savedSpawns.addAll(spawnGroup("wave9", world.getInstanceId()));
						}
						break;
					}
					case 10:
					{
						if (((KartiaWorld) world).savedSpawns.isEmpty())
						{
							world.incStatus();
							((KartiaWorld) world).savedSpawns.addAll(spawnGroup("wave10", world.getInstanceId()));
						}
						break;
					}
					case 11:
					{
						if (((KartiaWorld) world).savedSpawns.isEmpty())
						{
							world.incStatus();
							((KartiaWorld) world).savedSpawns.addAll(spawnGroup("wave11", world.getInstanceId()));
						}
						break;
					}
					case 12:
					{
						if (((KartiaWorld) world).savedSpawns.isEmpty())
						{
							world.incStatus();
							((KartiaWorld) world).savedSpawns.addAll(spawnGroup("wave12", world.getInstanceId()));
						}
						break;
					}
					case 13:
					{
						if (((KartiaWorld) world).savedSpawns.isEmpty())
						{
							world.incStatus();
							((KartiaWorld) world).savedSpawns.addAll(spawnGroup("wave13", world.getInstanceId()));
						}
						break;
					}
					case 14:
					{
						if (((KartiaWorld) world).savedSpawns.isEmpty())
						{
							world.incStatus();
							((KartiaWorld) world).savedSpawns.addAll(spawnGroup("wave14", world.getInstanceId()));
						}
						break;
					}
					case 15:
					{
						if (((KartiaWorld) world).savedSpawns.isEmpty())
						{
							world.incStatus();
							((KartiaWorld) world).savedSpawns.addAll(spawnGroup("wave15", world.getInstanceId()));
						}
						break;
					}
					case 16:
					{
						if (((KartiaWorld) world).savedSpawns.isEmpty())
						{
							world.incStatus();
							((KartiaWorld) world).savedSpawns.addAll(spawnGroup("wave16", world.getInstanceId()));
						}
						break;
					}
					case 17:
					{
						if (((KartiaWorld) world).savedSpawns.isEmpty())
						{
							world.incStatus();
							((KartiaWorld) world).savedSpawns.addAll(spawnGroup("wave17", world.getInstanceId()));
						}
						break;
					}
					case 18:
					{
						if (((KartiaWorld) world).savedSpawns.isEmpty())
						{
							world.incStatus();
							((KartiaWorld) world).savedSpawns.addAll(spawnGroup("wave18", world.getInstanceId()));
						}
						break;
					}
					case 19:
					{
						if (((KartiaWorld) world).savedSpawns.isEmpty())
						{
							world.incStatus();
							((KartiaWorld) world).savedSpawns.addAll(spawnGroup("wave19", world.getInstanceId()));
							broadcastScreenMessage(player, NpcStringId.STAGE_2);
							openDoor(DOOR_1, world.getInstanceId());
						}
						break;
					}
					case 20:
					{
						if (((KartiaWorld) world).savedSpawns.isEmpty())
						{
							world.incStatus();
							((KartiaWorld) world).savedSpawns.addAll(spawnGroup("wave20", world.getInstanceId()));
							broadcastScreenMessage(player, NpcStringId.STAGE_3);
							openDoor(DOOR_2, world.getInstanceId());
						}
						break;
					}
					case 21:
					{
						if (((KartiaWorld) world).savedSpawns.isEmpty())
						{
							world.incStatus();
							((KartiaWorld) world).savedSpawns.addAll(spawnGroup("wave21", world.getInstanceId()));
						}
						break;
					}
					case 22:
					{
						if (((KartiaWorld) world).savedSpawns.isEmpty())
						{
							world.incStatus();
							((KartiaWorld) world).savedSpawns.addAll(spawnGroup("wave22", world.getInstanceId()));
						}
						break;
					}
					case 23:
					{
						if (((KartiaWorld) world).savedSpawns.isEmpty())
						{
							world.incStatus();
							((KartiaWorld) world).savedSpawns.addAll(spawnGroup("wave23", world.getInstanceId()));
						}
						break;
					}
					case 24:
					{
						if (((KartiaWorld) world).savedSpawns.isEmpty())
						{
							world.incStatus();
							((KartiaWorld) world).savedSpawns.addAll(spawnGroup("wave24", world.getInstanceId()));
						}
						break;
					}
					case 25:
					{
						if (((KartiaWorld) world).savedSpawns.isEmpty())
						{
							world.incStatus();
							((KartiaWorld) world).savedSpawns.addAll(spawnGroup("wave25", world.getInstanceId()));
						}
						break;
					}
					case 26:
					{
						if (((KartiaWorld) world).savedSpawns.isEmpty())
						{
							world.incStatus();
							((KartiaWorld) world).savedSpawns.addAll(spawnGroup("wave26", world.getInstanceId()));
							for (L2Npc monster : ((KartiaWorld) world).savedSpawns)
							{
								if (BOSSES.contains(monster.getId()))
								{
									monster.startAbnormalVisualEffect(AbnormalVisualEffect.FLESH_STONE);
									monster.setIsImmobilized(true);
									monster.setIsInvul(true);
									monster.broadcastInfo();
									break;
								}
							}
						}
						break;
					}
					case 27:
					{
						if (((KartiaWorld) world).savedSpawns.size() == 1)
						{
							world.incStatus();
							final L2Character boss = ((KartiaWorld) world).savedSpawns.get(0);
							boss.stopAbnormalVisualEffect(AbnormalVisualEffect.FLESH_STONE);
							boss.setIsImmobilized(false);
							boss.setIsInvul(false);
							boss.broadcastInfo();
						}
						break;
					}
					case 28:
					{
						if (((KartiaWorld) world).savedSpawns.isEmpty())
						{
							// Check Instance Quests.
							for (L2PcInstance plr : ((KartiaWorld) world).playersInside)
							{
								switch (world.getTemplateId())
								{
									case SOLO_85_TEMPLATE_ID:
									{
										if ((plr != null) && (plr.getInstanceId() == world.getInstanceId()))
										{
											final QuestState qs = player.getQuestState(Q00497_IncarnationOfGreedZellakaSolo.class.getSimpleName());
											if ((qs != null) && (qs.getState() == State.STARTED) && qs.isCond(1))
											{
												qs.setCond(2, true);
											}
										}
										break;
									}
									case SOLO_90_TEMPLATE_ID:
									{
										if ((plr != null) && (plr.getInstanceId() == world.getInstanceId()))
										{
											final QuestState qs = player.getQuestState(Q00498_IncarnationOfJealousyPellineSolo.class.getSimpleName());
											if ((qs != null) && (qs.getState() == State.STARTED) && qs.isCond(1))
											{
												qs.setCond(2, true);
											}
										}
										break;
									}
									case SOLO_95_TEMPLATE_ID:
									{
										if ((plr != null) && (plr.getInstanceId() == world.getInstanceId()))
										{
											final QuestState qs = player.getQuestState(Q00499_IncarnationOfGluttonyKaliosSolo.class.getSimpleName());
											if ((qs != null) && (qs.getState() == State.STARTED) && qs.isCond(1))
											{
												qs.setCond(2, true);
											}
										}
										break;
									}
									case PARTY_85_TEMPLATE_ID:
									{
										if ((plr != null) && (plr.getInstanceId() == world.getInstanceId()))
										{
											final QuestState qs = player.getQuestState(Q00494_IncarnationOfGreedZellakaGroup.class.getSimpleName());
											if ((qs != null) && (qs.getState() == State.STARTED) && qs.isCond(1))
											{
												qs.setCond(2, true);
											}
										}
										break;
									}
									case PARTY_90_TEMPLATE_ID:
									{
										if ((plr != null) && (plr.getInstanceId() == world.getInstanceId()))
										{
											final QuestState qs = player.getQuestState(Q00495_IncarnationOfJealousyPellineGroup.class.getSimpleName());
											if ((qs != null) && (qs.getState() == State.STARTED) && qs.isCond(1))
											{
												qs.setCond(2, true);
											}
										}
										break;
									}
									case PARTY_95_TEMPLATE_ID:
									{
										if ((plr != null) && (plr.getInstanceId() == world.getInstanceId()))
										{
											final QuestState qs = player.getQuestState(Q00496_IncarnationOfGluttonyKaliosGroup.class.getSimpleName());
											if ((qs != null) && (qs.getState() == State.STARTED) && qs.isCond(1))
											{
												qs.setCond(2, true);
											}
										}
										break;
									}
								}
							}
							// 5 minute exit timer.
							InstanceManager.getInstance().getInstance(world.getInstanceId()).setDuration(Config.INSTANCE_FINISH_TIME);
							InstanceManager.getInstance().getInstance(world.getInstanceId()).setEmptyDestroyTime(0);
							// Stop quest timer.
							world.incStatus();
						}
						break;
					}
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		return npc.getId() + ".htm";
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		final InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(killer);
		if ((world == null) || !(world instanceof KartiaWorld))
		{
			return null;
		}
		((KartiaWorld) world).savedSpawns.remove(npc);
		return super.onKill(npc, killer, isSummon);
	}
	
	@Override
	public void onEnterInstance(L2PcInstance player, InstanceWorld world, boolean firstEntrance)
	{
		if ((world.getTemplateId() == SOLO_85_TEMPLATE_ID) || (world.getTemplateId() == SOLO_90_TEMPLATE_ID) || (world.getTemplateId() == SOLO_95_TEMPLATE_ID))
		{
			player.getVariables().set(KARTIA_ENTRY_VAR, System.currentTimeMillis());
		}
		else
		{
			player.getVariables().set(KARTIA_PARTY_ENTRY_VAR, System.currentTimeMillis());
		}
		world.addAllowed(player.getObjectId());
		((KartiaWorld) world).playersInside.add(player);
		teleportPlayer(player, START_LOC, world.getInstanceId(), false);
	}
	
	@Override
	protected boolean checkConditions(L2PcInstance player, int templateId)
	{
		long lastEntry = 0;
		if ((templateId == SOLO_85_TEMPLATE_ID) || (templateId == SOLO_90_TEMPLATE_ID) || (templateId == SOLO_95_TEMPLATE_ID))
		{
			lastEntry = player.getVariables().getLong(KARTIA_ENTRY_VAR, 0);
		}
		else
		{
			lastEntry = player.getVariables().getLong(KARTIA_PARTY_ENTRY_VAR, 0);
		}
		Calendar entryResetTime = Calendar.getInstance();
		entryResetTime.set(Calendar.HOUR, 6);
		entryResetTime.set(Calendar.MINUTE, 30);
		entryResetTime.set(Calendar.AM_PM, Calendar.AM);
		if (lastEntry < entryResetTime.getTimeInMillis())
		{
			return true;
		}
		return false;
	}
	
	private String removeVanguard(L2PcInstance player, int removedNpcId)
	{
		final InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		if ((world == null) || !(world instanceof KartiaWorld) || (world.getStatus() > 0))
		{
			return null;
		}
		world.setStatus(1);
		switch (world.getTemplateId())
		{
			case SOLO_85_TEMPLATE_ID:
			case PARTY_85_TEMPLATE_ID:
			{
				if (removedNpcId != BARTON)
				{
					((KartiaWorld) world).barton = (L2QuestGuardInstance) addSpawn(BARTON_88, VANGUARD_SPAWN_LOC, true, 0, false, world.getInstanceId());
				}
				if (removedNpcId != HAYUK)
				{
					((KartiaWorld) world).hayuk = (L2QuestGuardInstance) addSpawn(HAYUK_88, VANGUARD_SPAWN_LOC, true, 0, false, world.getInstanceId());
				}
				if (removedNpcId != ELIYAH)
				{
					((KartiaWorld) world).eliyah = (L2QuestGuardInstance) addSpawn(ELIYAH_88, VANGUARD_SPAWN_LOC, true, 0, false, world.getInstanceId());
				}
				if (removedNpcId != ELISE)
				{
					((KartiaWorld) world).elise = (L2QuestGuardInstance) addSpawn(ELISE_88, VANGUARD_SPAWN_LOC, true, 0, false, world.getInstanceId());
				}
				((KartiaWorld) world).adolph = (L2QuestGuardInstance) addSpawn(ADOLPH_88, VANGUARD_SPAWN_LOC, true, 0, false, world.getInstanceId());
				break;
			}
			case SOLO_90_TEMPLATE_ID:
			case PARTY_90_TEMPLATE_ID:
			{
				if (removedNpcId != BARTON)
				{
					((KartiaWorld) world).barton = (L2QuestGuardInstance) addSpawn(BARTON_93, VANGUARD_SPAWN_LOC, true, 0, false, world.getInstanceId());
				}
				if (removedNpcId != HAYUK)
				{
					((KartiaWorld) world).hayuk = (L2QuestGuardInstance) addSpawn(HAYUK_93, VANGUARD_SPAWN_LOC, true, 0, false, world.getInstanceId());
				}
				if (removedNpcId != ELIYAH)
				{
					((KartiaWorld) world).eliyah = (L2QuestGuardInstance) addSpawn(ELIYAH_93, VANGUARD_SPAWN_LOC, true, 0, false, world.getInstanceId());
				}
				if (removedNpcId != ELISE)
				{
					((KartiaWorld) world).elise = (L2QuestGuardInstance) addSpawn(ELISE_93, VANGUARD_SPAWN_LOC, true, 0, false, world.getInstanceId());
				}
				((KartiaWorld) world).adolph = (L2QuestGuardInstance) addSpawn(ADOLPH_93, VANGUARD_SPAWN_LOC, true, 0, false, world.getInstanceId());
				break;
			}
			case SOLO_95_TEMPLATE_ID:
			case PARTY_95_TEMPLATE_ID:
			{
				if (removedNpcId != BARTON)
				{
					((KartiaWorld) world).barton = (L2QuestGuardInstance) addSpawn(BARTON_98, VANGUARD_SPAWN_LOC, true, 0, false, world.getInstanceId());
				}
				if (removedNpcId != HAYUK)
				{
					((KartiaWorld) world).hayuk = (L2QuestGuardInstance) addSpawn(HAYUK_98, VANGUARD_SPAWN_LOC, true, 0, false, world.getInstanceId());
				}
				if (removedNpcId != ELIYAH)
				{
					((KartiaWorld) world).eliyah = (L2QuestGuardInstance) addSpawn(ELIYAH_98, VANGUARD_SPAWN_LOC, true, 0, false, world.getInstanceId());
				}
				if (removedNpcId != ELISE)
				{
					((KartiaWorld) world).elise = (L2QuestGuardInstance) addSpawn(ELISE_98, VANGUARD_SPAWN_LOC, true, 0, false, world.getInstanceId());
				}
				((KartiaWorld) world).adolph = (L2QuestGuardInstance) addSpawn(ADOLPH_98, VANGUARD_SPAWN_LOC, true, 0, false, world.getInstanceId());
				break;
			}
		}
		// Start combat.
		world.incStatus();
		if (player.getParty() != null)
		{
			for (L2PcInstance member : player.getParty().getMembers())
			{
				member.teleToLocation(VANGUARD_SPAWN_LOC, true);
			}
		}
		else
		{
			player.teleToLocation(VANGUARD_SPAWN_LOC, true);
		}
		if (((KartiaWorld) world).adolph != null)
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new Fighter(player, ((KartiaWorld) world).adolph), 1000);
		}
		if (((KartiaWorld) world).barton != null)
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new Fighter(player, ((KartiaWorld) world).barton), 1000);
		}
		if (((KartiaWorld) world).hayuk != null)
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new Fighter(player, ((KartiaWorld) world).hayuk), 1000);
		}
		if (((KartiaWorld) world).eliyah != null)
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new Fighter(player, ((KartiaWorld) world).eliyah), 1000);
		}
		if (((KartiaWorld) world).elise != null)
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new Fighter(player, ((KartiaWorld) world).elise), 1000);
		}
		((KartiaWorld) world).savedSpawns.addAll(spawnGroup("wave1", world.getInstanceId()));
		startQuestTimer("checkStatus", 5000, null, player, true);
		broadcastScreenMessage(player, NpcStringId.STAGE_1);
		return "33608-1.htm";
	}
	
	private void broadcastScreenMessage(L2PcInstance player, NpcStringId stringId)
	{
		if (player.getParty() != null)
		{
			for (L2PcInstance member : player.getParty().getMembers())
			{
				if (member == null)
				{
					continue;
				}
				member.sendPacket(new ExShowScreenMessage(2, -1, 2, 0, 0, 0, 0, true, 5000, false, null, stringId, null));
			}
		}
		else
		{
			player.sendPacket(new ExShowScreenMessage(2, -1, 2, 0, 0, 0, 0, true, 5000, false, null, stringId, null));
		}
	}
}