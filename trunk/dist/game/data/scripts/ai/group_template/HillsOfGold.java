/*
 * Copyright (C) 2004-2015 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package ai.group_template;

import ai.npc.AbstractNpcAI;

import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2MonsterInstance;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;

/**
 * Hills of Gold AI.
 * @author Mobius
 */
public class HillsOfGold extends AbstractNpcAI
{
	// Npcs
	private static final int GOLEM_OF_REPAIRS = 19309;
	private static final int EXCAVATOR_GOLEM = 19312;
	private static final int DRILL_GOLEM = 19310;
	private static final int SPICULA_1 = 23246;
	private static final int SPICULA_2 = 23247;
	private static final int YIN_FRAGMENT = 19308;
	private static final int SPICULA_ELITE_GUARD = 23303;
	private static final int[] GOLEMS =
	{
		23255,
		23257,
		23259,
		23261,
		23263,
		23264,
		23266,
		23267,
	};
	
	public HillsOfGold()
	{
		super(HillsOfGold.class.getSimpleName(), "ai/group_template");
		addAttackId(YIN_FRAGMENT);
		addSpawnId(SPICULA_1, SPICULA_2);
		addSpawnId(GOLEMS);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equals("SPICULA_AGGRO") && (npc != null) && !npc.isDead())
		{
			for (L2Character nearby : npc.getKnownList().getKnownCharactersInRadius(npc.getAggroRange()))
			{
				if (npc.isInCombat())
				{
					break;
				}
				if (nearby.isMonster() && ((nearby.getId() == GOLEM_OF_REPAIRS) || (nearby.getId() == EXCAVATOR_GOLEM) || (nearby.getId() == DRILL_GOLEM)))
				{
					((L2MonsterInstance) npc).addDamage(nearby, 1, null);
					break;
				}
			}
			startQuestTimer("SPICULA_AGGRO", 10000, npc, null);
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon)
	{
		L2Npc mob1 = addSpawn(SPICULA_ELITE_GUARD, npc.getX(), npc.getY(), npc.getZ(), attacker.getHeading() + 32500, true, npc.getSpawn().getRespawnDelay());
		((L2MonsterInstance) mob1).addDamage(attacker, 1, null);
		L2Npc mob2 = addSpawn(SPICULA_ELITE_GUARD, npc.getX(), npc.getY(), npc.getZ(), attacker.getHeading() + 32500, true, npc.getSpawn().getRespawnDelay());
		((L2MonsterInstance) mob2).addDamage(attacker, 1, null);
		L2Npc mob3 = addSpawn(SPICULA_ELITE_GUARD, npc.getX(), npc.getY(), npc.getZ(), attacker.getHeading() + 32500, true, npc.getSpawn().getRespawnDelay());
		((L2MonsterInstance) mob3).addDamage(attacker, 1, null);
		L2Npc mob4 = addSpawn(SPICULA_ELITE_GUARD, npc.getX(), npc.getY(), npc.getZ(), attacker.getHeading() + 32500, true, npc.getSpawn().getRespawnDelay());
		((L2MonsterInstance) mob4).addDamage(attacker, 1, null);
		npc.deleteMe();
		return super.onAttack(npc, attacker, damage, isSummon);
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		if ((npc.getId() == SPICULA_1) || (npc.getId() == SPICULA_2))
		{
			startQuestTimer("SPICULA_AGGRO", 5000, npc, null);
		}
		else
		{
			npc.setDisplayEffect(1);
		}
		return super.onSpawn(npc);
	}
	
	public static void main(String[] args)
	{
		new HillsOfGold();
	}
}