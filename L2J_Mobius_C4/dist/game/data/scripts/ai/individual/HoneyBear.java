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

import com.l2jmobius.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.network.serverpackets.CreatureSay;
import com.l2jmobius.util.Rnd;

/**
 * @by Karakan for L2JLisvus
 */
public class HoneyBear extends Quest
{
	private static final int HONEY_BEAR = 5058;
	
	public HoneyBear(int questId, String name, String descr)
	{
		super(questId, name, descr);
		final int[] mobs =
		{
			HONEY_BEAR
		};
		registerMobs(mobs);
	}
	
	@Override
	public String onSpawn(L2NpcInstance npc)
	{
		if ((npc.getNpcId() == HONEY_BEAR) && (Rnd.get(10) == 0))
		{
			npc.broadcastPacket(new CreatureSay(npc.getObjectId(), 1, npc.getName(), "What does honey of this place taste like?!"));
		}
		else
		{
			npc.broadcastPacket(new CreatureSay(npc.getObjectId(), 1, npc.getName(), "Give me some sweet, delicious golden honey!"));
		}
		
		return super.onSpawn(npc);
	}
	
	@Override
	public String onAttack(L2NpcInstance npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if ((npc.getCurrentHp() / npc.getMaxHp()) > 0.99)
		{
			npc.broadcastPacket(new CreatureSay(npc.getObjectId(), 1, npc.getName(), "If you give me some honey, I'll at least spare your life..."));
		}
		
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	@Override
	public String onKill(L2NpcInstance npc, L2PcInstance killer, boolean isPet)
	{
		if (npc.getNpcId() == HONEY_BEAR)
		{
			npc.broadcastPacket(new CreatureSay(npc.getObjectId(), 1, npc.getName(), "Only for lack of honey did I lose to the likes of you."));
		}
		
		return super.onKill(npc, killer, isPet);
	}
	
	public static void main(String[] args)
	{
		// now call the constructor (starts up the ai)
		new HoneyBear(-1, "honeybear", "ai");
	}
}