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
package instances.FrostLordEasyMode;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.holders.SkillHolder;
import org.l2jmobius.gameserver.model.instancezone.Instance;
import org.l2jmobius.gameserver.model.skills.SkillCaster;
import org.l2jmobius.gameserver.network.NpcStringId;
import org.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;
import org.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;

import instances.AbstractInstance;

/**
 * @author Serenitty
 */
public class FrostLordEasyMode extends AbstractInstance
{
	// NPCs
	private static final int GLAKIAS = 29136; // GrandBoss
	private static final int GLAKIAS_MINION = 22348; // Minions tank
	private static final int GLAKIAS_ARCHER = 22352; // Minions archer
	// Skills
	private static final SkillHolder SUMMON_GLAKIAS_LVL1 = new SkillHolder(48373, 1); // When spawn Minion range attack
	private static final int ICE_SWEEP = 48376;
	private static final SkillHolder ICE_SWEEP_LV_1 = new SkillHolder(48376, 1); // When player in Radius target boss atack
	// private static final SkillHolder GLAKIAS_ENCHANCEMENT_LV_1 = new SkillHolder(48372, 1); // glakias up stats test only
	// Misc
	private static final int TEMPLATE_ID = 100;
	
	public FrostLordEasyMode()
	{
		super(TEMPLATE_ID);
		addStartNpc(GLAKIAS);
		addKillId(GLAKIAS, GLAKIAS_MINION, GLAKIAS_ARCHER);
		addAttackId(GLAKIAS, GLAKIAS_MINION, GLAKIAS_ARCHER);
		addInstanceEnterId(TEMPLATE_ID);
		addInstanceLeaveId(TEMPLATE_ID);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, PlayerInstance player)
	{
		switch (event)
		{
			case "ENTER":
			{
				enterInstance(player, npc, TEMPLATE_ID);
				final Instance world = player.getInstanceWorld();
				if (world != null)
				{
					broadcastPacket(world, new ExShowScreenMessage(NpcStringId.GLAKIAS_LV_85, 2, 7000));
					addSpawn(GLAKIAS, 114700, -114792, -11207, 49151, false, 0, true, player.getInstanceId());
				}
				break;
			}
			case "GLAKIAS_MINION":
			{
				final Instance world = npc.getInstanceWorld();
				if (world != null)
				{
					world.getNpc(GLAKIAS).doCast(SUMMON_GLAKIAS_LVL1.getSkill());
					addSpawn(GLAKIAS_MINION, world.getNpc(GLAKIAS).getX() + getRandom(-500, 500), world.getNpc(GLAKIAS).getY() + getRandom(-500, 500), world.getNpc(GLAKIAS).getZ(), 31011, true, 0, true, npc.getInstanceId());
					addSpawn(GLAKIAS_MINION, world.getNpc(GLAKIAS).getX() + getRandom(-500, 500), world.getNpc(GLAKIAS).getY() + getRandom(-500, 500), world.getNpc(GLAKIAS).getZ(), 31011, true, 0, true, npc.getInstanceId());
					addSpawn(GLAKIAS_MINION, world.getNpc(GLAKIAS).getX() + getRandom(-500, 500), world.getNpc(GLAKIAS).getY() + getRandom(-500, 500), world.getNpc(GLAKIAS).getZ(), 31011, true, 0, true, npc.getInstanceId());
					addSpawn(GLAKIAS_MINION, world.getNpc(GLAKIAS).getX() + getRandom(-500, 500), world.getNpc(GLAKIAS).getY() + getRandom(-500, 500), world.getNpc(GLAKIAS).getZ(), 31011, true, 0, true, npc.getInstanceId());
					addSpawn(GLAKIAS_MINION, world.getNpc(GLAKIAS).getX() + getRandom(-500, 500), world.getNpc(GLAKIAS).getY() + getRandom(-500, 500), world.getNpc(GLAKIAS).getZ(), 31011, true, 0, true, npc.getInstanceId());
				}
				break;
			}
			case "GLAKIAS_ARCHER":
			{
				final Instance world = npc.getInstanceWorld();
				if (world != null)
				{
					broadcastPacket(world, new ExShowScreenMessage(NpcStringId.YOU_KILLED_MANY_OF_MY_SUBORDINATES, 2, 9000));
					addSpawn(GLAKIAS_ARCHER, world.getNpc(GLAKIAS).getX() + getRandom(-500, 500), world.getNpc(GLAKIAS).getY() + getRandom(-500, 500), world.getNpc(GLAKIAS).getZ(), 31011, true, 0, true, npc.getInstanceId());
					addSpawn(GLAKIAS_ARCHER, world.getNpc(GLAKIAS).getX() + getRandom(-500, 500), world.getNpc(GLAKIAS).getY() + getRandom(-500, 500), world.getNpc(GLAKIAS).getZ(), 31011, true, 0, true, npc.getInstanceId());
					addSpawn(GLAKIAS_ARCHER, world.getNpc(GLAKIAS).getX() + getRandom(-500, 500), world.getNpc(GLAKIAS).getY() + getRandom(-500, 500), world.getNpc(GLAKIAS).getZ(), 31011, true, 0, true, npc.getInstanceId());
					addSpawn(GLAKIAS_ARCHER, world.getNpc(GLAKIAS).getX() + getRandom(-500, 500), world.getNpc(GLAKIAS).getY() + getRandom(-500, 500), world.getNpc(GLAKIAS).getZ(), 31011, true, 0, true, npc.getInstanceId());
					addSpawn(GLAKIAS_ARCHER, world.getNpc(GLAKIAS).getX() + getRandom(-500, 500), world.getNpc(GLAKIAS).getY() + getRandom(-500, 500), world.getNpc(GLAKIAS).getZ(), 31011, true, 0, true, npc.getInstanceId());
				}
				break;
			}
			case "ICE_SWEEP":
			{
				final Instance world = npc.getInstanceWorld();
				if (world != null)
				{
					final PlayerInstance gamers = world.getPlayers().stream().findAny().orElse(null);
					if ((gamers != null) && (gamers.isInsideRadius3D(npc, 300)))
					{
						npc.abortAttack();
						npc.abortCast();
						npc.setTarget(gamers);
						if (gamers.getAffectedSkillLevel(ICE_SWEEP) == 1)
						{
							npc.abortCast();
							startQuestTimer("ICE_SWEEP", 400, npc, player); // All time checking
						}
						else
						{
							if (SkillCaster.checkUseConditions(npc, ICE_SWEEP_LV_1.getSkill()))
							{
								npc.doCast(ICE_SWEEP_LV_1.getSkill());
							}
							break;
						}
					}
				}
			}
		}
		return event;
	}
	
	@Override
	public String onAttack(Npc npc, PlayerInstance attacker, int damage, boolean isSummon)
	{
		final Instance world = npc.getInstanceWorld();
		if (world == null)
		{
			return null;
		}
		if (npc.isAttackable())
		{
			if (npc.getId() == GLAKIAS)
			{
				if (npc.getCurrentHp() < (npc.getMaxHp() * 0.85))
				
				{
					startQuestTimer("GLAKIAS_MINION", 1000, world.getNpc(GLAKIAS), null);
				}
				if (npc.getCurrentHp() < (npc.getMaxHp() * 0.75))
				
				{
					startQuestTimer("GLAKIAS_MINION", 1000, world.getNpc(GLAKIAS), null);
				}
				
				if (npc.getCurrentHp() < (npc.getMaxHp() * 0.55))
				
				{
					startQuestTimer("GLAKIAS_MINION", 1000, world.getNpc(GLAKIAS), null);
				}
				if (npc.getCurrentHp() < (npc.getMaxHp() * 0.45))
				
				{
					startQuestTimer("ICE_SWEEP", 400, npc, attacker);
				}
				if (npc.getCurrentHp() < (npc.getMaxHp() * 0.40))
				
				{
					startQuestTimer("ICE_SWEEP", 400, npc, attacker);
				}
				if (npc.getCurrentHp() < (npc.getMaxHp() * 0.34))
				
				{
					startQuestTimer("ICE_SWEEP", 400, npc, attacker);
				}
				if (npc.getCurrentHp() < (npc.getMaxHp() * 0.25))
				{
					startQuestTimer("GLAKIAS_ARCHER", 1000, world.getNpc(GLAKIAS), null);
				}
				else if (npc.getCurrentHp() < (npc.getMaxHp() * 0.15))
				{
					startQuestTimer("GLAKIAS_ARCHER", 1000, world.getNpc(GLAKIAS), null);
				}
			}
		}
		
		return super.onAttack(npc, attacker, damage, isSummon);
	}
	
	void broadcastPacket(Instance world, IClientOutgoingPacket packet)
	{
		for (PlayerInstance player : world.getPlayers())
		{
			if ((player != null) && player.isOnline())
			{
				player.sendPacket(packet);
			}
		}
	}
	
	@Override
	public String onKill(Npc npc, PlayerInstance player, boolean isSummon)
	{
		final Instance world = npc.getInstanceWorld();
		if (world == null)
		{
			return null;
		}
		
		if (npc.getId() == GLAKIAS)
		{
			cancelQuestTimer("GLAKIAS_MINION", npc, player);
			cancelQuestTimer("GLAKIAS_ARCHER", npc, player);
			world.finishInstance();
		}
		return super.onKill(npc, player, isSummon);
	}
	
	public static void main(String[] args)
	{
		new FrostLordEasyMode();
	}
}