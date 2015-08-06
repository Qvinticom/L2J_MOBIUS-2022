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
 * YeSagira AI.
 * @author Stayway
 */
public class YeSagira extends AbstractNpcAI
{
	// Npcs
	private static final int STALKER = 22992;
	private static final int CRITTER = 22993;
	private static final int CRAWLER = 22991;
	private static final int KRAPHER = 22996;
	private static final int AVIAN = 22994;
	private static final int EYESAROCH = 23112;
	private static final int GUARD_1 = 19152;
	private static final int GUARD_2 = 19153;
	
	public YeSagira()
	{
		super(YeSagira.class.getSimpleName(), "ai/group_template");
		addSpawnId(GUARD_1, GUARD_2);
		addSpawnId(STALKER, CRAWLER);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equals("GUARD_AGGRO") && (npc != null) && !npc.isDead())
		{
			for (L2Character nearby : npc.getKnownList().getKnownCharactersInRadius(npc.getAggroRange()))
			{
				if (npc.isInCombat())
				{
					break;
				}
				if (nearby.isMonster() && ((nearby.getId() == STALKER) || (nearby.getId() == EYESAROCH) || (nearby.getId() == CRITTER) || (nearby.getId() == AVIAN) || (nearby.getId() == KRAPHER) || (nearby.getId() == CRAWLER)))
				{
					((L2MonsterInstance) npc).addDamage(nearby, 1, null);
					break;
				}
			}
			startQuestTimer("GUARD_AGGRO", 10000, npc, null);
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		if ((npc.getId() == GUARD_1) || (npc.getId() == GUARD_2))
		{
			startQuestTimer("GUARD_AGGRO", 5000, npc, null);
		}
		else
		{
			npc.setState(1);
		}
		return super.onSpawn(npc);
	}
	
	public static void main(String[] args)
	{
		new YeSagira();
	}
}