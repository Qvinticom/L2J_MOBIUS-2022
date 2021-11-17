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
package instances.PailakaSongOfIceAndFire;

import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.instancemanager.InstanceManager;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.instancezone.InstanceWorld;
import org.l2jmobius.gameserver.model.zone.ZoneType;
import org.l2jmobius.gameserver.network.NpcStringId;

import instances.AbstractInstance;

/**
 * Pailaka Song of Ice and Fire Instance zone.
 * @author Gnacik, St3eT
 */
public class PailakaSongOfIceAndFire extends AbstractInstance
{
	// NPCs
	private static final int ADLER1 = 32497;
	private static final int GARGOS = 18607;
	private static final int BLOOM = 18616;
	private static final int BOTTLE = 32492;
	private static final int BRAZIER = 32493;
	// Items
	private static final int FIRE_ENHANCER = 13040;
	private static final int WATER_ENHANCER = 13041;
	private static final int SHIELD_POTION = 13032;
	private static final int HEAL_POTION = 13033;
	// Location
	private static final Location TELEPORT = new Location(-52875, 188232, -4696);
	// Misc
	private static final int TEMPLATE_ID = 43;
	private static final int ZONE = 20108;
	
	private PailakaSongOfIceAndFire()
	{
		addStartNpc(ADLER1);
		addTalkId(ADLER1);
		addAttackId(BOTTLE, BRAZIER);
		addExitZoneId(ZONE);
		addSpawnId(BLOOM);
		addKillId(BLOOM);
		addCreatureSeeId(GARGOS);
	}
	
	@Override
	public void onEnterInstance(Player player, InstanceWorld world, boolean firstEntrance)
	{
		if (firstEntrance)
		{
			world.addAllowed(player);
		}
		teleportPlayer(player, TELEPORT, world.getInstanceId());
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		switch (event)
		{
			case "enter":
			{
				enterInstance(player, TEMPLATE_ID);
				break;
			}
			case "GARGOS_LAUGH":
			{
				npc.broadcastSay(ChatType.NPC_SHOUT, NpcStringId.OHH_OH_OH);
				break;
			}
			case "TELEPORT":
			{
				teleportPlayer(player, TELEPORT, player.getInstanceId());
				break;
			}
			case "DELETE":
			{
				if (npc != null)
				{
					npc.deleteMe();
				}
				break;
			}
			case "BLOOM_TIMER":
			{
				startQuestTimer("BLOOM_TIMER2", getRandom(2, 4) * 60 * 1000, npc, null);
				break;
			}
			case "BLOOM_TIMER2":
			{
				npc.setInvisible(!npc.isInvisible());
				startQuestTimer("BLOOM_TIMER", 5000, npc, null);
				break;
			}
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onAttack(Npc npc, Player player, int damage, boolean isSummon)
	{
		if ((damage > 0) && npc.isScriptValue(0))
		{
			switch (getRandom(6))
			{
				case 0:
				{
					if (npc.getId() == BOTTLE)
					{
						npc.dropItem(player, WATER_ENHANCER, getRandom(1, 6));
					}
					break;
				}
				case 1:
				{
					if (npc.getId() == BRAZIER)
					{
						npc.dropItem(player, FIRE_ENHANCER, getRandom(1, 6));
					}
					break;
				}
				case 2:
				case 3:
				{
					npc.dropItem(player, SHIELD_POTION, getRandom(1, 10));
					break;
				}
				case 4:
				case 5:
				{
					npc.dropItem(player, HEAL_POTION, getRandom(1, 10));
					break;
				}
			}
			npc.setScriptValue(1);
			startQuestTimer("DELETE", 3000, npc, null);
		}
		return super.onAttack(npc, player, damage, isSummon);
	}
	
	@Override
	public String onKill(Npc npc, Player player, boolean isSummon)
	{
		npc.dropItem(player, getRandomBoolean() ? SHIELD_POTION : HEAL_POTION, getRandom(1, 7));
		return super.onKill(npc, player, isSummon);
	}
	
	@Override
	public String onExitZone(Creature creature, ZoneType zone)
	{
		if ((creature.isPlayer()) && !creature.isDead() && !creature.isTeleporting() && ((Player) creature).isOnline())
		{
			final InstanceWorld world = InstanceManager.getInstance().getWorld(creature);
			if ((world != null) && (world.getTemplateId() == TEMPLATE_ID))
			{
				startQuestTimer("TELEPORT", 1000, null, (Player) creature);
			}
		}
		return super.onExitZone(creature, zone);
	}
	
	@Override
	public String onCreatureSee(Npc npc, Creature creature)
	{
		if (creature.isPlayer() && npc.isScriptValue(0))
		{
			npc.setScriptValue(1);
			startQuestTimer("GARGOS_LAUGH", 1000, npc, creature.getActingPlayer());
		}
		return super.onCreatureSee(npc, creature);
	}
	
	@Override
	public String onSpawn(Npc npc)
	{
		npc.setInvisible(true);
		startQuestTimer("BLOOM_TIMER", 1000, npc, null);
		return super.onSpawn(npc);
	}
	
	public static void main(String[] args)
	{
		new PailakaSongOfIceAndFire();
	}
}
