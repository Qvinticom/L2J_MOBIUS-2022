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
package ai.areas.Hellbound.AI;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.instance.Monster;

import ai.AbstractNpcAI;

/**
 * Naia Lock AI.<br>
 * Removes minions after master's death.
 * @author GKR
 */
public class NaiaLock extends AbstractNpcAI
{
	// NPCs
	private static final int LOCK = 18491;
	
	public NaiaLock()
	{
		addKillId(LOCK);
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		((Monster) npc).getMinionList().onMasterDie(true);
		return super.onKill(npc, killer, isSummon);
	}
}