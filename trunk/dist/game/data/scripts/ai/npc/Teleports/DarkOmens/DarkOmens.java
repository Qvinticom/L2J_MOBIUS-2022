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
package ai.npc.Teleports.DarkOmens;

import ai.npc.AbstractNpcAI;

import com.l2jserver.gameserver.model.Location;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;

/**
 * Dark Omens teleport AI.
 * @author Stayway
 */
public final class DarkOmens extends AbstractNpcAI
{
	// NPC
	private static final int ZIGURAT_IN = 31118;
	private static final int ZIGURAT_OUT = 31124;
	// Locations
	private static final Location TELEPORT_LOC1 = new Location(-19203, 13517, -4899);
	private static final Location TELEPORT_LOC2 = new Location(-20091, 13499, -4901);
	
	private DarkOmens()
	{
		super(DarkOmens.class.getSimpleName(), "ai/npc/Teleports");
		addFirstTalkId(ZIGURAT_IN, ZIGURAT_OUT);
		addStartNpc(ZIGURAT_IN, ZIGURAT_OUT);
		addTalkId(ZIGURAT_IN, ZIGURAT_OUT);
	}
	
	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equals("chat1"))
		{
			if ((player.getLevel() < 90) && (player.getLevel() >= 85))
			{
				player.teleToLocation(TELEPORT_LOC1, true);
				return null;
			}
			return "31118-1.htm";
		}
		else if (event.equals("chat2"))
		{
			if ((player.getLevel() < 90) && (player.getLevel() >= 85))
			{
				player.teleToLocation(TELEPORT_LOC2, true);
				return null;
			}
		}
		return event;
	}
	
	public static void main(String[] args)
	{
		new DarkOmens();
	}
}