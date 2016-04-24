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
package ai.individual;

import com.l2jmobius.gameserver.ai.CtrlIntention;
import com.l2jmobius.gameserver.model.Location;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;

import ai.npc.AbstractNpcAI;

/**
 * Handermonkey AI.
 * @author Gladicek
 */
final class Handermonkey extends AbstractNpcAI
{
	// NPC
	private static final int HANDERMONKEY = 33203;
	
	private Handermonkey()
	{
		super(Handermonkey.class.getSimpleName(), "ai/npc");
		addSpawnId(HANDERMONKEY);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("npc_move"))
		{
			if (getRandom(100) > 30)
			{
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(((npc.getSpawn().getX() - 70) + getRandom(100)), ((npc.getSpawn().getY() - 70) + getRandom(100)), npc.getZ(), 0));
			}
			else
			{
				npc.broadcastSocialAction(9);
			}
		}
		return null;
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		npc.setRunning();
		startQuestTimer("npc_move", 5000, npc, null, true);
		return super.onSpawn(npc);
	}
	
	public static void main(String[] args)
	{
		new Handermonkey();
	}
}