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
package ai.areas.BlackbirdCampsite.SoulSummonStone;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;

import ai.AbstractNpcAI;

/**
 * Soul Summon Stone AI.
 * @author NviX
 */
public final class SoulSummonStone extends AbstractNpcAI
{
	// NPCs
	private static final int SOUL_SUMMON_STONE = 34434;
	// Bosses
	private static final int SUMMONED_HARPAS = 26347;
	private static final int SUMMONED_GARP = 26348;
	private static final int SUMMONED_MORICKS = 26349;
	// Items
	private static final int SOUL_QUARTZ = 48536;
	// Misc
	private Npc _boss;
	
	private SoulSummonStone()
	{
		addStartNpc(SOUL_SUMMON_STONE);
		addFirstTalkId(SOUL_SUMMON_STONE);
		addTalkId(SOUL_SUMMON_STONE);
		addKillId(SUMMONED_HARPAS, SUMMONED_GARP, SUMMONED_MORICKS);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		switch (event)
		{
			case "request_boss":
			{
				if ((_boss != null) && !_boss.isDead())
				{
					return "34434-04.html";
				}
				if (hasQuestItems(player, SOUL_QUARTZ))
				{
					takeItems(player, SOUL_QUARTZ, 1);
					int i = getRandom(100);
					if (i < 40)
					{
						_boss = addSpawn(SUMMONED_HARPAS, player.getX() + getRandom(-300, 300), player.getY() + getRandom(-300, 300), player.getZ() + 10, getRandom(64000), false, 0, true);
						return "34434-01.html";
					}
					else if (i < 80)
					{
						_boss = addSpawn(SUMMONED_GARP, player.getX() + getRandom(-300, 300), player.getY() + getRandom(-300, 300), player.getZ() + 10, getRandom(64000), false, 0, true);
						return "34434-02.html";
					}
					else
					{
						_boss = addSpawn(SUMMONED_MORICKS, player.getX() + getRandom(-300, 300), player.getY() + getRandom(-300, 300), player.getZ() + 10, getRandom(64000), false, 0, true);
						return "34434-03.html";
					}
				}
				return "34434-05.html";
			}
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		return "34434.html";
	}
	
	public static void main(String[] args)
	{
		new SoulSummonStone();
	}
}