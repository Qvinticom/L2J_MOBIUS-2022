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
package ai.group_template;

import com.l2jmobius.gameserver.model.actor.L2Character;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2QuestGuardInstance;

import ai.npc.AbstractNpcAI;

/**
 * YeSagira AI.
 * @author Stayway, Mobius
 */
public class YeSagira extends AbstractNpcAI
{
	// Npcs
	private static final int GUARD_1 = 19152;
	private static final int GUARD_2 = 19153;
	
	public YeSagira()
	{
		super(YeSagira.class.getSimpleName(), "ai/group_template");
		addSpawnId(GUARD_1, GUARD_2);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equals("GUARD_AGGRO") && (npc != null) && !npc.isDead())
		{
			for (L2Character nearby : npc.getKnownList().getKnownCharactersInRadius(npc.getAggroRange()))
			{
				if (npc.isInCombat())
				{
					break;
				}
				if (nearby.isMonster())
				{
					((L2QuestGuardInstance) npc).addDamage(nearby, 1, null);
					break;
				}
			}
			startQuestTimer("GUARD_AGGRO", 10000, npc, null);
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		startQuestTimer("GUARD_AGGRO", 5000, npc, null);
		return super.onSpawn(npc);
	}
	
	public static void main(String[] args)
	{
		new YeSagira();
	}
}