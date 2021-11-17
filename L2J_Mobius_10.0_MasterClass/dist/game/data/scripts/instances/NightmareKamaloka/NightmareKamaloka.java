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
package instances.NightmareKamaloka;

import java.util.HashMap;
import java.util.Map;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.ItemHolder;
import org.l2jmobius.gameserver.model.instancezone.Instance;

import instances.AbstractInstance;

/**
 * Nightmare Kamaloka instance zone.
 * @author St3eT
 */
public class NightmareKamaloka extends AbstractInstance
{
	// NPCs
	private static final int BENUSTA = 34542;
	private static final int DARK_RIDER = 26102;
	private static final int SIONE_ULAF = 26465;
	private static final int INVISIBLE_NPC = 18919;
	// Items
	private static final ItemHolder BENUSTAS_REWARD_BOX = new ItemHolder(81151, 1);
	private static final ItemHolder BENUSTAS_REWARD_BOX_110 = new ItemHolder(81741, 1);
	// Misc
	private static final Map<Integer, Integer> BOSS_MAP = new HashMap<>();
	static
	{
		BOSS_MAP.put(26093, 18170002); // Mino
		BOSS_MAP.put(26094, 18170004); // Sola
		BOSS_MAP.put(26096, 18170006); // Ariarc
		BOSS_MAP.put(26099, 18170008); // Sirra
		BOSS_MAP.put(DARK_RIDER, -1); // Dark Rider
	}
	private static final Map<Integer, Integer> BOSS_MAP_110 = new HashMap<>();
	static
	{
		BOSS_MAP_110.put(26461, 18170002); // Noegg
		BOSS_MAP_110.put(26462, 18170004); // Kyshis
		BOSS_MAP_110.put(26463, 18170006); // Ssizz Chronizel
		BOSS_MAP_110.put(26464, 18170008); // Kanan Chronizel
		BOSS_MAP_110.put(SIONE_ULAF, -1); // Sir Sione Ulaf
	}
	private static final int[] TEMPLATE_IDS =
	{
		258,
		313
	};
	
	public NightmareKamaloka()
	{
		super(TEMPLATE_IDS);
		addStartNpc(BENUSTA);
		addTalkId(BENUSTA);
		addSpawnId(INVISIBLE_NPC);
		addKillId(BOSS_MAP.keySet());
		addKillId(BOSS_MAP_110.keySet());
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		if (event.contains("enterInstance"))
		{
			enterInstance(player, npc, event.contains("110") ? TEMPLATE_IDS[1] : TEMPLATE_IDS[0]);
		}
		else if ("SPAWN_BOSSES".equals(event))
		{
			final Instance instance = npc.getInstanceWorld();
			if (isInInstance(instance))
			{
				instance.spawnGroup("BOSSES");
			}
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onSpawn(Npc npc)
	{
		final Instance instance = npc.getInstanceWorld();
		if (isInInstance(instance) && (npc.getId() == INVISIBLE_NPC))
		{
			startQuestTimer("SPAWN_BOSSES", 10000, npc, null);
		}
		return super.onSpawn(npc);
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		final Instance instance = npc.getInstanceWorld();
		if (isInInstance(instance))
		{
			final int nextDoorId = instance.getTemplateId() == TEMPLATE_IDS[0] ? BOSS_MAP.getOrDefault(npc.getId(), -1) : BOSS_MAP_110.getOrDefault(npc.getId(), -1);
			if (nextDoorId == -1)
			{
				for (Player member : instance.getPlayers())
				{
					giveItems(member, instance.getTemplateId() == TEMPLATE_IDS[0] ? BENUSTAS_REWARD_BOX : BENUSTAS_REWARD_BOX_110);
				}
				instance.finishInstance();
			}
			else
			{
				instance.openCloseDoor(nextDoorId, true);
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	public static void main(String[] args)
	{
		new NightmareKamaloka();
	}
}