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

import java.util.HashMap;
import java.util.Map;

import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.holders.ItemChanceHolder;

import ai.AbstractNpcAI;

/**
 * Pagan Key AI.
 * @author Zoey76. Adapted to PaganKey by Notorionn
 */
final class PaganKey extends AbstractNpcAI
{
	// Items
	private static final int PAGAN_KEY = 8273;
	// Monsters
	private static final Map<Integer, ItemChanceHolder> MONSTERS = new HashMap<>();
	static
	{
		MONSTERS.put(22140, new ItemChanceHolder(PAGAN_KEY, 7000)); // Resurrected Worker
		MONSTERS.put(22141, new ItemChanceHolder(PAGAN_KEY, 6500)); // Forgotten Victim
		MONSTERS.put(22139, new ItemChanceHolder(PAGAN_KEY, 5200)); // Old Aristocrat's Soldier
	}
	
	private PaganKey()
	{
		super(PaganKey.class.getSimpleName(), "ai/group_template");
		addKillId(MONSTERS.keySet());
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		final ItemChanceHolder holder = MONSTERS.get(npc.getId());
		if (getRandom(10000) <= holder.getChance())
		{
			npc.dropItem(killer, holder);
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	public static void main(String[] args)
	{
		new PaganKey();
	}
}
