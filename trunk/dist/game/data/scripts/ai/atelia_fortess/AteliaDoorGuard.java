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
package ai.atelia_fortess;

import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;

import ai.npc.AbstractNpcAI;

/**
 * @author hlwrave
 */
final class AteliaDoorGuard extends AbstractNpcAI
{
	private static final int GUARD = 23539;
	private static int _killCount = 0;
	
	private AteliaDoorGuard()
	{
		super(AteliaDoorGuard.class.getSimpleName(), "ai/atelia_fortess");
		addKillId(GUARD);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		switch (event)
		{
			case "Close_Atelia_Door":
			{
				closeDoor(18190002, 0);
				closeDoor(18190004, 0);
				break;
			}
		}
		return "";
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		if (npc.getId() == GUARD)
		{
			_killCount++;
		}
		if (_killCount == 2)
		{
			openDoor(18190002, 0);
			openDoor(18190004, 0);
			startQuestTimer("Close_Atelia_Door", 3600000, null, null);
			_killCount = 0;
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	public static void main(String[] args)
	{
		new AteliaDoorGuard();
	}
}