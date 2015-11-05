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
package ai.individual;

import ai.npc.AbstractNpcAI;

import com.l2jserver.gameserver.model.actor.L2Npc;

/**
 * Embryo Purifier AI.
 * @author Mobius
 */
public class EmbryoPurifier extends AbstractNpcAI
{
	// Npc
	private static final int EMBRYO_PURIFIER_1 = 27540;
	private static final int EMBRYO_PURIFIER_2 = 27541;
	
	public EmbryoPurifier()
	{
		super(EmbryoPurifier.class.getSimpleName(), "ai/individual");
		addSpawnId(EMBRYO_PURIFIER_1, EMBRYO_PURIFIER_2);
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		npc.setIsNoRndWalk(true);
		return super.onSpawn(npc);
	}
	
	public static void main(String[] args)
	{
		new EmbryoPurifier();
	}
}