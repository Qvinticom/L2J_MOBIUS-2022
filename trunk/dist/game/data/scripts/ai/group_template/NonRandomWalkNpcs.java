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

/**
 * Non Random Walk Npcs AI.
 * @author Mobius
 */
public class NonRandomWalkNpcs extends AbstractNpcAI
{
	private static final int[] NPCS =
	{
		27540, // Fields of Massacre - Embryo Purifier
		27541, // Fields of Massacre - Embryo Purifier
		23537, // Atelia Fortress - Atelia Infuser
		23588, // Atelia Fortress - Hummel
	};
	
	public NonRandomWalkNpcs()
	{
		super(NonRandomWalkNpcs.class.getSimpleName(), "ai/group_template");
		addSpawnId(NPCS);
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		npc.setIsNoRndWalk(true);
		return super.onSpawn(npc);
	}
	
	public static void main(String[] args)
	{
		new NonRandomWalkNpcs();
	}
}