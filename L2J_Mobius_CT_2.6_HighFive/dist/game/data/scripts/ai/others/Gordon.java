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
package ai.others;

import org.l2jmobius.gameserver.model.actor.Attackable;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;

import ai.AbstractNpcAI;

/**
 * Gordon AI
 * @author TOFIZ, malyelfik
 */
public class Gordon extends AbstractNpcAI
{
	private static final int GORDON = 29095;
	
	private Gordon()
	{
		addSpawnId(GORDON);
		addCreatureSeeId(GORDON);
	}
	
	@Override
	public String onCreatureSee(Npc npc, Creature creature)
	{
		if (creature.isPlayer() && ((Player) creature).isCursedWeaponEquipped())
		{
			addAttackDesire(npc, creature);
		}
		return super.onCreatureSee(npc, creature);
	}
	
	@Override
	public String onSpawn(Npc npc)
	{
		((Attackable) npc).setCanReturnToSpawnPoint(false);
		return super.onSpawn(npc);
	}
	
	public static void main(String[] args)
	{
		new Gordon();
	}
}