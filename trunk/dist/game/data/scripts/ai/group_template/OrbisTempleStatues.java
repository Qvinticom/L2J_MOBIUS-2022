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

import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author Mobius
 */
public class OrbisTempleStatues extends AbstractNpcAI
{
	// Npcs
	private static final int VICTIM = 22913;
	private static final int GUARD = 22916;
	private static final int THROWER = 22919;
	private static final int ANCIENT_HERO = 22925;
	// Items
	private static final int SWORD = 15280;
	private static final int SPEAR = 17372;
	
	public OrbisTempleStatues()
	{
		super(OrbisTempleStatues.class.getSimpleName(), "ai/group_template");
		addSpawnId(VICTIM, GUARD, THROWER, ANCIENT_HERO);
		addAttackId(VICTIM, GUARD, THROWER, ANCIENT_HERO);
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		switch (npc.getId())
		{
			case VICTIM:
			case GUARD:
			{
				npc.setRHandId(SWORD); // TODO: Find better way to change animation.
				break;
			}
			case THROWER:
			{
				npc.setRHandId(SPEAR);
				break;
			}
		}
		
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		npc.setIsNoRndWalk(true);
		return super.onSpawn(npc);
	}
	
	public static void main(String[] args)
	{
		new OrbisTempleStatues();
	}
}