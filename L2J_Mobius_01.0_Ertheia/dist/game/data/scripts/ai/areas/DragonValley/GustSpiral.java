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
package ai.areas.DragonValley;

import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.enums.Race;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.NpcStringId;

import ai.AbstractNpcAI;

/**
 * AI for Gust Spiral (23447)
 * @author Gigi, Mobius
 */
public class GustSpiral extends AbstractNpcAI
{
	// NPC
	private static final int GUST_SPIRAL = 23447;
	
	private GustSpiral()
	{
		addAttackId(GUST_SPIRAL);
	}
	
	@Override
	public String onAttack(Npc npc, PlayerInstance attacker, int damage, boolean isSummon)
	{
		if (attacker.getRace() == Race.ERTHEIA)
		{
			if (getRandom(100) < 30)
			{
				npc.broadcastSay(ChatType.NPC_GENERAL, NpcStringId.MY_WIND_BARRIER_HOW_ONLY_THE_ERTHEIA_CAN_WAIT_UNLESS_YOU_ARE);
			}
			npc.setInvul(false);
		}
		else
		{
			npc.setInvul(true);
		}
		return super.onAttack(npc, attacker, damage, isSummon);
	}
	
	public static void main(String[] args)
	{
		new GustSpiral();
	}
}