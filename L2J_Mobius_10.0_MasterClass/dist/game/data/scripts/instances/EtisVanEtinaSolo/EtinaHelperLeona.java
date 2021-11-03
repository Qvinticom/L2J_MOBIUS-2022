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
package instances.EtisVanEtinaSolo;

import org.l2jmobius.commons.util.CommonUtil;
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.geoengine.GeoEngine;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.instance.FriendlyNpcInstance;
import org.l2jmobius.gameserver.model.actor.instance.MonsterInstance;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.events.impl.creature.OnCreatureAttacked;
import org.l2jmobius.gameserver.model.events.impl.instance.OnInstanceStatusChange;
import org.l2jmobius.gameserver.model.instancezone.Instance;
import org.l2jmobius.gameserver.network.NpcStringId;

import ai.AbstractNpcAI;

/**
 * Etina Helper Leona AI.
 * @author CostyKiller
 */
public class EtinaHelperLeona extends AbstractNpcAI
{
	// NPCs
	private static final int ETINA_HELPER_LEONA = 34471;
	private static final int[] NOT_ATK_NPCS =
	{
		33798, // Common Decoration - Barricade
		19677, // Seal of Gnosis
		19678, // Seal of Strife
		19679, // Seal of Avarice
		19680, // Seal of Punishment
		19681, // Seal of Awakening
		19682, // Seal of Calamity
		19683, // Seal of Destruction
	};
	private static final int[] ETINA_HELPERS =
	{
		34471, // Leona Blackbird
		34472, // Devianne
		34473, // Elikia
		34474, // Sporcha
		34475, // Aliber
	};
	// RB
	private static final int ETIS_VAN_ETINA1 = 26321;
	private static final int ETIS_VAN_ETINA2 = 26322;
	private static final int KAIN_VAN_HALTER = 26235;
	private static final int CAMILLE = 26236;
	// Misc
	private static final int[] ETINA_SOLO_INSTANCES =
	{
		292, // Fall of Etina (Solo)
		293, // Fall of Etina (Solo)
	};
	
	private EtinaHelperLeona()
	{
		addSpellFinishedId(ETINA_HELPER_LEONA);
		addCreatureSeeId(ETINA_HELPER_LEONA);
		setCreatureAttackedId(this::onCreatureAttacked, ETINA_HELPER_LEONA);
		setInstanceStatusChangeId(this::onInstanceStatusChange, ETINA_SOLO_INSTANCES);
	}
	
	@Override
	public void onTimerEvent(String event, StatSet params, Npc npc, PlayerInstance player)
	{
		final Instance instance = npc.getInstanceWorld();
		if ((instance != null) && event.equals("CHECK_ACTION"))
		{
			final StatSet npcVars = npc.getVariables();
			final PlayerInstance plr = npcVars.getObject("PLAYER_OBJECT", PlayerInstance.class);
			final MonsterInstance monster = getRandomEntry(World.getInstance().getVisibleObjectsInRange(npc, MonsterInstance.class, 2500));
			if (plr != null)
			{
				final double distance = npc.calculateDistance2D(plr);
				if (!npc.isAttackingNow() && !npc.isMoving() && (distance > 250))
				{
					final Location loc = new Location(plr.getX(), plr.getY(), plr.getZ() + 50);
					final Location randLoc = new Location(loc.getX() + getRandom(-100, 100), loc.getY() + getRandom(-100, 100), loc.getZ());
					if (distance > 600)
					{
						npc.teleToLocation(loc);
					}
					else
					{
						npc.setRunning();
					}
					addMoveToDesire(npc, randLoc, 23);
					((FriendlyNpcInstance) npc).setCanReturnToSpawnPoint(false);
				}
				else if (!npc.isInCombat() || !npc.isAttackingNow() || (npc.getTarget() == null))
				{
					WorldObject target = npc.getTarget();
					if (target == null)
					{
						npc.setTarget(monster);
					}
					if ((target != null) && !target.isInvul() && target.isTargetable() && GeoEngine.getInstance().canSeeTarget(npc, target) && !CommonUtil.contains(NOT_ATK_NPCS, target.getId()) && !CommonUtil.contains(ETINA_HELPERS, target.getId()))
					{
						npc.setInvul(true);
						npc.setRunning();
						((FriendlyNpcInstance) npc).setCanReturnToSpawnPoint(false);
						addAttackDesire(npc, (Creature) target);
						// npc.reduceCurrentHp(1, monster, null);
					}
				}
			}
		}
	}
	
	public void onCreatureAttacked(OnCreatureAttacked event)
	{
		final FriendlyNpcInstance npc = (FriendlyNpcInstance) event.getTarget();
		final Npc attacker = (Npc) event.getAttacker();
		if (npc != null)
		{
			final Instance instance = npc.getInstanceWorld();
			if ((instance != null) && !event.getAttacker().isPlayable() && !CommonUtil.contains(ETINA_HELPERS, event.getAttacker().getId()))
			{
				if (!npc.isInCombat())
				{
					switch (attacker.getId())
					{
						default:
						{
							if (getRandom(50) < 5)
							{
								npc.broadcastSay(ChatType.NPC_GENERAL, NpcStringId.DON_T_GET_IN_MY_WAY);
							}
							break;
						}
						case CAMILLE:
						{
							npc.broadcastSay(ChatType.NPC_GENERAL, NpcStringId.GISELLE_NO_IS_SHE_CAMILLE_IN_THAT_STATE);
							break;
						}
						case KAIN_VAN_HALTER:
						{
							break;
						}
						case ETIS_VAN_ETINA1:
						{
							npc.broadcastSay(ChatType.NPC_GENERAL, NpcStringId.EVEN_THE_MIGHTY_ETINA_SEEMS_TO_FEAR_US);
							break;
						}
						case ETIS_VAN_ETINA2:
						{
							npc.broadcastSay(ChatType.NPC_GENERAL, NpcStringId.ETIS_VAN_ETINA_WE_FINALLY_MEET);
							break;
						}
					}
				}
			}
		}
	}
	
	public void onInstanceStatusChange(OnInstanceStatusChange event)
	{
		final Instance instance = event.getWorld();
		final int status = event.getStatus();
		if ((status == 1) || (status == 2) || (status == 3))
		{
			instance.getAliveNpcs(ETINA_HELPER_LEONA).forEach(etinaHelperLeona -> getTimers().addRepeatingTimer("CHECK_ACTION", 3000, etinaHelperLeona, null));
			// instance.getAliveNpcs(ETINA_HELPER_LEONA).forEach(etinaHelperLeona -> getTimers().addRepeatingTimer("USE_SKILL", 6000, etinaHelperLeona, null));
		}
	}
	
	@Override
	public String onCreatureSee(Npc npc, Creature creature)
	{
		if (creature.isPlayer())
		{
			npc.getVariables().set("PLAYER_OBJECT", creature.getActingPlayer());
		}
		return super.onCreatureSee(npc, creature);
	}
	
	public static void main(String[] args)
	{
		new EtinaHelperLeona();
	}
}