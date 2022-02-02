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
package ai.bosses.Lindvior;

import java.util.Collection;

import org.l2jmobius.gameserver.ai.CtrlIntention;
import org.l2jmobius.gameserver.enums.FlyType;
import org.l2jmobius.gameserver.geoengine.GeoEngine;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.serverpackets.FlyToLocation;
import org.l2jmobius.gameserver.network.serverpackets.ValidateLocation;

import ai.AbstractNpcAI;

/**
 * Vortex AI
 * @author Gigi
 * @date 2017-07-23 - [10:32:50]
 */
public class Vortex extends AbstractNpcAI
{
	private static final int SMALL_VORTEX = 25898;
	private static final int BIG_VORTEX = 19427;
	
	public Vortex()
	{
		super();
		addSpawnId(SMALL_VORTEX, BIG_VORTEX);
		addCreatureSeeId(SMALL_VORTEX, BIG_VORTEX);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		switch (event)
		{
			case "rnd_small":
			{
				World.getInstance().forEachVisibleObjectInRange(npc, Player.class, 250, attackers ->
				{
					if ((attackers != null) && !attackers.isDead() && !attackers.isAlikeDead())
					{
						attackers.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
						final int radians = (int) Math.toRadians(npc.calculateDirectionTo(attackers));
						final int x = (int) (attackers.getX() + (600 * Math.cos(radians)));
						final int y = (int) (attackers.getY() + (600 * Math.sin(radians)));
						final int z = attackers.getZ();
						final Location loc = GeoEngine.getInstance().getValidLocation(attackers.getX(), attackers.getY(), attackers.getZ(), x, y, z, attackers.getInstanceWorld());
						attackers.broadcastPacket(new FlyToLocation(attackers, x, y, z, FlyType.THROW_UP, 800, 800, 800));
						attackers.setXYZ(loc);
						attackers.broadcastPacket(new ValidateLocation(attackers));
						npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
						startQuestTimer("stop_knock_down", 5000, npc, attackers);
						startQuestTimer("despawn_small", 5000, npc, null);
					}
				});
				break;
			}
			case "rnd_big":
			{
				World.getInstance().forEachVisibleObjectInRange(npc, Player.class, 500, attackers ->
				{
					if ((attackers != null) && !attackers.isDead() && !attackers.isAlikeDead())
					{
						attackers.setCurrentHp(attackers.getMaxHp() * 0.2);
						attackers.setCurrentMp(attackers.getMaxMp() * 0.2);
						attackers.setCurrentCp(1.0);
						startQuestTimer("despawn_big", 60000, npc, null);
					}
				});
				break;
			}
			case "despawn_small":
			{
				if (npc != null)
				{
					cancelQuestTimers("rnd_small");
					npc.getSpawn().stopRespawn();
					npc.doDie(null);
				}
				break;
			}
			case "despawn_big":
			{
				if (npc != null)
				{
					cancelQuestTimers("despawn_big");
					npc.getSpawn().stopRespawn();
					npc.deleteMe();
				}
				break;
			}
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onCreatureSee(Npc npc, Creature creature)
	{
		switch (npc.getId())
		{
			case SMALL_VORTEX:
			{
				startQuestTimer("rnd_small", 5000, npc, null, true);
				break;
			}
			case BIG_VORTEX:
			{
				startQuestTimer("rnd_big", 10000, npc, null, true);
				break;
			}
		}
		return super.onCreatureSee(npc, creature);
	}
	
	@Override
	public String onSpawn(Npc npc)
	{
		attackRandomTarget(npc);
		npc.setRandomWalking(true);
		npc.setRunning();
		return super.onSpawn(npc);
	}
	
	private void attackRandomTarget(Npc npc)
	{
		final Collection<Player> players = World.getInstance().getVisibleObjects(npc, Player.class);
		if ((players == null) || players.isEmpty())
		{
			return;
		}
		
		if (!players.isEmpty())
		{
			addAttackPlayerDesire(npc, players.stream().findAny().get());
		}
	}
	
	public static void main(String[] args)
	{
		new Vortex();
	}
}
