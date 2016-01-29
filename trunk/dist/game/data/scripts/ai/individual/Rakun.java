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

import com.l2jmobius.gameserver.enums.ChatType;
import com.l2jmobius.gameserver.model.actor.L2Character;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.NpcStringId;

import ai.npc.AbstractNpcAI;

/**
 * Rakun AI.
 * @author Mobius
 */
final class Rakun extends AbstractNpcAI
{
	// NPC
	private static final int RAKUN = 33972;
	
	private Rakun()
	{
		super(Rakun.class.getSimpleName(), "ai/individual");
		addSeeCreatureId(RAKUN);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equals("BROADCAST_TEXT") && (npc != null))
		{
			broadcastNpcSay(npc, ChatType.NPC_GENERAL, NpcStringId.THIS_PLACE_SWARMS_WITH_DRAGONS_BY_DAY_AND_UNDEAD_BY_NIGHT, 1000);
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onSeeCreature(L2Npc npc, L2Character creature, boolean isSummon)
	{
		if (creature.isPlayer())
		{
			startQuestTimer("BROADCAST_TEXT", 3000, npc, null, true);
		}
		return super.onSeeCreature(npc, creature, isSummon);
	}
	
	public static void main(String[] args)
	{
		new Rakun();
	}
}