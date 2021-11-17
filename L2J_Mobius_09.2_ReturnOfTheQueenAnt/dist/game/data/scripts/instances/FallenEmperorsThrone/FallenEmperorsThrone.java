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
package instances.FallenEmperorsThrone;

import org.l2jmobius.gameserver.enums.Movie;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.SkillHolder;
import org.l2jmobius.gameserver.model.instancezone.Instance;
import org.l2jmobius.gameserver.network.NpcStringId;
import org.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;
import org.l2jmobius.gameserver.util.Broadcast;

import instances.AbstractInstance;

/**
 * @author CostyKiller
 * @URL: https://l2wiki.com/Fallen_Emperors_Throne
 */
public class FallenEmperorsThrone extends AbstractInstance
{
	// NPC
	private static final int KEKROPUS = 34222;
	// Raid
	private static final int FE_HELIOS1 = 26333;
	private static final int FE_HELIOS2 = 26334;
	private static final int FE_HELIOS3 = 26335;
	// Minions
	private static final int LEOPOLD = 26336;
	private static final int HELIOS_RED_LIGHTNING = 26337;
	private static final int HELIOS_BLUE_LIGHTNING = 26338;
	// Skills
	private static final SkillHolder AUDIENCE_DEBUFF = new SkillHolder(16613, 1);
	private static final SkillHolder RED_LIGHTNING_SPEAR = new SkillHolder(16617, 1);
	private static final SkillHolder BLUE_LIGHTNING_SPEAR = new SkillHolder(16618, 1);
	private static final SkillHolder PRANARACH = new SkillHolder(16624, 1);
	private static final SkillHolder HELIOS_RAGE1 = new SkillHolder(16625, 1);
	private static final SkillHolder HELIOS_RAGE2 = new SkillHolder(16625, 2);
	private static final SkillHolder HELIOS_RAGE3 = new SkillHolder(16625, 3);
	private static final SkillHolder HELIOS_RAGE4 = new SkillHolder(16625, 4);
	private static final SkillHolder LEOPOLD_BOMB = new SkillHolder(16629, 1);
	private static final SkillHolder LEOPOLD_PLASMA_BOMB = new SkillHolder(16630, 1);
	private static final SkillHolder LEOPOLD_ENERGY_BOMB = new SkillHolder(16631, 1);
	private static final SkillHolder LEOPOLD_MINI_GUN = new SkillHolder(16632, 1);
	private static final SkillHolder LEOPOLD_SPRAY_SHOT = new SkillHolder(16633, 1);
	private static final SkillHolder LEOPOLD_HARPOON = new SkillHolder(16634, 1);
	// Misc
	private static final int TEMPLATE_ID = 283;
	
	public FallenEmperorsThrone()
	{
		super(TEMPLATE_ID);
		addInstanceCreatedId(TEMPLATE_ID);
		addStartNpc(KEKROPUS);
		addTalkId(KEKROPUS);
		addAttackId(FE_HELIOS1, FE_HELIOS2, FE_HELIOS3);
		addKillId(FE_HELIOS1, FE_HELIOS2, FE_HELIOS3);
	}
	
	@Override
	public void onInstanceCreated(Instance world, Player player)
	{
		world.setStatus(0);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		switch (event)
		{
			case "enterInstance":
			{
				enterInstance(player, npc, TEMPLATE_ID);
				startQuestTimer("beginning", 10000, null, player);
				break;
			}
			case "beginning":
			{
				final Instance world = player.getInstanceWorld();
				if (isInInstance(world) && !world.getParameters().getBoolean("SPAWNED", false))
				{
					world.getParameters().set("SPAWNED", true);
					world.spawnGroup("FE_HELIOS1");
					world.spawnGroup("MINIONS");
				}
				break;
			}
			case "stage2":
			{
				final Instance world = player.getInstanceWorld();
				if (isInInstance(world))
				{
					world.spawnGroup("FE_HELIOS2");
					world.spawnGroup("MINIONS");
					showOnScreenMsg(world, NpcStringId.HELIOS_APPEARANCE_CHANGES_AND_HE_BEGINS_TO_GROW_STRONGER, ExShowScreenMessage.TOP_CENTER, 10000, true);
					startQuestTimer("spheresSpawn", 10000, null, player);
				}
				break;
			}
			case "stage3":
			{
				final Instance world = player.getInstanceWorld();
				if (isInInstance(world))
				{
					
					world.getParameters().set("ACTIVATED", false);
					world.spawnGroup("FE_HELIOS3");
					showOnScreenMsg(world, NpcStringId.HELIOS_APPEARANCE_CHANGES_AND_HE_BEGINS_TO_GROW_STRONGER, ExShowScreenMessage.TOP_CENTER, 10000, true);
					startQuestTimer("leopoldSpawn", 10000, null, player);
				}
				break;
			}
			case "spheresSpawn":
			{
				final Instance world = player.getInstanceWorld();
				if (isInInstance(world))
				{
					world.spawnGroup("BLUE_LIGHTNING");
					world.spawnGroup("RED_LIGHTNING");
					world.getNpc(HELIOS_BLUE_LIGHTNING).setInvul(true);
					world.getNpc(HELIOS_RED_LIGHTNING).setInvul(true);
					showOnScreenMsg(world, NpcStringId.THE_ENUMA_ELISH_SPEAR_ON_THE_THRONE_OF_HELIOS_IS_PREPARED_AND_PLACED_IN_POSITION, ExShowScreenMessage.TOP_CENTER, 10000, true);
					startQuestTimer("leopoldSpawn", 10000, null, player);
				}
				break;
			}
			case "leopoldSpawn":
			{
				final Instance world = player.getInstanceWorld();
				if (isInInstance(world))
				{
					world.spawnGroup("LEOPOLD");
					showOnScreenMsg(world, NpcStringId.THE_SIEGE_CANNON_LEOPOLD_ON_THE_THRONE_OF_HELIOS_BEGINS_TO_PREPARE_TO_FIRE, ExShowScreenMessage.TOP_CENTER, 10000, true);
				}
				break;
			}
			case "LEOPOLD_TASK":
			{
				final Instance world = player.getInstanceWorld();
				if (isInInstance(world))
				{
					int count = world.getPlayersCount();
					if (count > 0)
					{
						final Player randomPlayer = world.getPlayers().stream().findAny().get();
						final Npc leopold = world.getNpc(LEOPOLD);
						if (leopold != null)
						{
							leopold.setTarget(randomPlayer);
							int rnd = getRandom(100);
							if (rnd < 16)
							{
								leopold.doCast(LEOPOLD_BOMB.getSkill());
							}
							else if (rnd < 32)
							{
								leopold.doCast(LEOPOLD_PLASMA_BOMB.getSkill());
							}
							else if (rnd < 48)
							{
								leopold.doCast(LEOPOLD_ENERGY_BOMB.getSkill());
							}
							else if (rnd < 64)
							{
								leopold.doCast(LEOPOLD_MINI_GUN.getSkill());
							}
							else if (rnd < 80)
							{
								leopold.doCast(LEOPOLD_SPRAY_SHOT.getSkill());
							}
							else
							{
								leopold.doCast(LEOPOLD_HARPOON.getSkill());
							}
						}
					}
				}
				break;
			}
			case "BLUE_SPEAR_TASK":
			{
				final Instance world = player.getInstanceWorld();
				if (isInInstance(world))
				{
					int count = world.getPlayersCount();
					if (count > 0)
					{
						final Player randomPlayer = world.getPlayers().stream().findAny().get();
						final Npc blueLightning = world.getNpc(HELIOS_BLUE_LIGHTNING);
						if (blueLightning != null)
						{
							blueLightning.setTarget(randomPlayer);
							blueLightning.doCast(BLUE_LIGHTNING_SPEAR.getSkill());
						}
					}
					showOnScreenMsg(world, NpcStringId.HELIOS_PICKS_UP_THE_BLUE_LIGHTNING_SPEAR_AND_BEGINS_GATHERING_HIS_POWER, ExShowScreenMessage.TOP_CENTER, 10000, true);
				}
				break;
			}
			case "RED_SPEAR_TASK":
			{
				final Instance world = player.getInstanceWorld();
				if (isInInstance(world))
				{
					int count = world.getPlayersCount();
					if (count > 0)
					{
						final Player randomPlayer = world.getPlayers().stream().findAny().get();
						final Npc redLightning = world.getNpc(HELIOS_RED_LIGHTNING);
						if (redLightning != null)
						{
							redLightning.setTarget(randomPlayer);
							redLightning.doCast(RED_LIGHTNING_SPEAR.getSkill());
						}
					}
					showOnScreenMsg(world, NpcStringId.HELIOS_PICKS_UP_THE_RED_LIGHTNING_SPEAR_AND_BEGINS_GATHERING_HIS_POWER, ExShowScreenMessage.TOP_CENTER, 10000, true);
				}
				break;
			}
			case "DEBUFF_TASK":
			{
				final Instance world = player.getInstanceWorld();
				if (isInInstance(world))
				{
					world.getPlayers().forEach(plr ->
					{
						AUDIENCE_DEBUFF.getSkill().applyEffects(player, player);
					});
				}
				break;
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
			final boolean ACTIVATED = world.getParameters().getBoolean("ACTIVATED", false);
			final boolean STAGE1_50 = world.getParameters().getBoolean("STAGE1_50", false);
			final boolean STAGE2_50 = world.getParameters().getBoolean("STAGE2_50", false);
			final boolean HELIOS_80 = world.getParameters().getBoolean("HELIOS_80", false);
			final boolean HELIOS_50 = world.getParameters().getBoolean("HELIOS_50", false);
			final boolean ANNOUNCE = world.getParameters().getBoolean("ANNOUNCE", false);
			
			if ((npc.getId() == FE_HELIOS1) && !ANNOUNCE)
			{
				world.getParameters().set("ANNOUNCE", true);
				startQuestTimer("DEBUFF_TASK", 20000, npc, attacker);
				Broadcast.toAllOnlinePlayers(new ExShowScreenMessage(NpcStringId.THE_ADEN_WARRIORS_BEGIN_BATTLE_WITH_THE_GIANT_EMPEROR_HELIOS, ExShowScreenMessage.TOP_CENTER, 10000, true));
			}
			if ((npc.getId() == FE_HELIOS1) && !STAGE1_50 && (npc.getCurrentHp() <= (npc.getMaxHp() * 0.5)))
			{
				world.getParameters().set("ANNOUNCE", true);
				world.getParameters().set("STAGE1_50", true);
				HELIOS_RAGE1.getSkill().applyEffects(world.getNpc(FE_HELIOS1), world.getNpc(FE_HELIOS1));
			}
			if ((npc.getId() == FE_HELIOS2) && !ACTIVATED)
			{
				world.getParameters().set("ACTIVATED", true);
				HELIOS_RAGE1.getSkill().applyEffects(world.getNpc(FE_HELIOS2), world.getNpc(FE_HELIOS2));
				startQuestTimer("BLUE_SPEAR_TASK", 120000, npc, attacker);
				startQuestTimer("RED_SPEAR_TASK", 120000, npc, attacker);
				startQuestTimer("LEOPOLD_TASK", 120000, npc, attacker);
			}
			if ((npc.getId() == FE_HELIOS2) && !STAGE2_50 && (npc.getCurrentHp() <= (npc.getMaxHp() * 0.5)))
			{
				world.getParameters().set("STAGE2_50", true);
				HELIOS_RAGE2.getSkill().applyEffects(world.getNpc(FE_HELIOS2), world.getNpc(FE_HELIOS2));
			}
			if ((npc.getId() == FE_HELIOS3) && !ACTIVATED)
			{
				world.getParameters().set("ACTIVATED", true);
				HELIOS_RAGE3.getSkill().applyEffects(world.getNpc(FE_HELIOS3), world.getNpc(FE_HELIOS3));
				startQuestTimer("LEOPOLD_TASK", 120000, npc, attacker);
			}
			if ((npc.getId() == FE_HELIOS3) && !HELIOS_80 && (npc.getCurrentHp() <= (npc.getMaxHp() * 0.8)))
			{
				world.getParameters().set("HELIOS_80", true);
				world.spawnGroup("LEOPOLD_ORIGIN");
				world.spawnGroup("ENUMA_ELISH_ORIGIN");
				showOnScreenMsg(world, NpcStringId.THE_KAMAEL_ORIGINS_ABOVE_THE_THRONE_OF_HELIOS_BEGIN_TO_SOAR, ExShowScreenMessage.TOP_CENTER, 10000, true);
			}
			else if ((npc.getId() == FE_HELIOS3) && !HELIOS_50 && (npc.getCurrentHp() <= (npc.getMaxHp() * 0.5)))
			{
				final Npc helios3 = world.getNpc(FE_HELIOS3);
				world.getParameters().set("HELIOS_50", true);
				HELIOS_RAGE4.getSkill().applyEffects(helios3, helios3);
				showOnScreenMsg(world, NpcStringId.HELIOS_USES_THE_PRANARACH_SHIELD_OF_LIGHT_TO_MINIMIZE_DAMAGE, ExShowScreenMessage.TOP_CENTER, 10000, true);
				helios3.abortCast();
				helios3.doCast(PRANARACH.getSkill());
			}
		}
		return super.onAttack(npc, attacker, damage, isSummon);
	}
	
	@Override
	public String onKill(Npc npc, Player player, boolean isSummon)
	{
		final Instance world = player.getInstanceWorld();
		if (isInInstance(world))
		{
			switch (npc.getId())
			{
				case FE_HELIOS1:
				{
					world.getNpc(FE_HELIOS1).deleteMe();
					playMovie(world.getPlayers(), Movie.SC_HELIOS_TRANS_A);
					startQuestTimer("stage2", 15000, null, player);
					break;
				}
				case FE_HELIOS2:
				{
					world.getNpc(FE_HELIOS2).deleteMe();
					if (world.getNpc(LEOPOLD) != null)
					{
						world.getNpc(LEOPOLD).deleteMe();
					}
					if (world.getNpc(HELIOS_RED_LIGHTNING) != null)
					{
						world.getNpc(HELIOS_RED_LIGHTNING).deleteMe();
					}
					if (world.getNpc(HELIOS_BLUE_LIGHTNING) != null)
					{
						world.getNpc(HELIOS_BLUE_LIGHTNING).deleteMe();
					}
					playMovie(world.getPlayers(), Movie.SC_HELIOS_TRANS_B);
					startQuestTimer("stage3", 15000, null, player);
					break;
				}
				case FE_HELIOS3:
				{
					showOnScreenMsg(world, NpcStringId.HELIOS_DEFEATED_TAKES_FLIGHT_DEEP_IN_TO_THE_SUPERION_FORT_HIS_THRONE_IS_RENDERED_INACTIVE, ExShowScreenMessage.TOP_CENTER, 10000, true);
					world.getAliveNpcs().forEach(mob ->
					{
						mob.deleteMe();
					});
					if (getQuestTimer("BLUE_SPEAR_TASK", npc, player) != null)
					{
						cancelQuestTimer("BLUE_SPEAR_TASK", npc, player);
					}
					if (getQuestTimer("RED_SPEAR_TASK", npc, player) != null)
					{
						cancelQuestTimer("RED_SPEAR_TASK", npc, player);
					}
					if (getQuestTimer("LEOPOLD_TASK", npc, player) != null)
					{
						cancelQuestTimer("LEOPOLD_TASK", npc, player);
					}
					if (getQuestTimer("DEBUFF_TASK", npc, player) != null)
					{
						cancelQuestTimer("DEBUFF_TASK", npc, player);
					}
					world.finishInstance(2);
					break;
				}
			}
		}
		return super.onKill(npc, player, isSummon);
	}
	
	public static void main(String[] args)
	{
		new FallenEmperorsThrone();
	}
}
