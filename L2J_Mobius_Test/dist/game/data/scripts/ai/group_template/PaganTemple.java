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

import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;

import ai.AbstractNpcAI;

/**
 * Pagan Temple AI.
 * @author Mobius
 */
final class PaganTemple extends AbstractNpcAI
{
	// Npc
	private static final int TRIOL_HIGH_PRIEST = 19410;
	private static final int CHAPEL_GATEKEEPER = 22138;
	
	public PaganTemple()
	{
		super(PaganTemple.class.getSimpleName(), "ai/group_template");
		addSpawnId(TRIOL_HIGH_PRIEST, CHAPEL_GATEKEEPER);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equals("DESPAWN_TRIOL") && (npc != null))
		{
			if (npc.isInCombat())
			{
				startQuestTimer("DESPAWN_TRIOL", 10000, npc, null, false); // 10 seconds delay
			}
			else
			{
				npc.deleteMe();
			}
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		switch (npc.getId())
		{
			case CHAPEL_GATEKEEPER:
			{
				npc.setIsNoRndWalk(true);
				break;
			}
			case TRIOL_HIGH_PRIEST:
			{
				startQuestTimer("DESPAWN_TRIOL", 10000, npc, null, false); // 10 seconds delay
				break;
			}
		}
		return super.onSpawn(npc);
	}
	
	public static void main(String[] args)
	{
		new PaganTemple();
	}
}