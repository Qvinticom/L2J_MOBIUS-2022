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
package ai.npc.Teleports.DimensionalWarpTeleport;

import com.l2jmobius.gameserver.model.Location;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;

import ai.AbstractNpcAI;

/**
 * @author hlwrave
 */
public final class DimensionalWarpTeleport extends AbstractNpcAI
{
	// NPC
	private static final int RESED = 33974;
	// Misc
	private static final int MIN_LEVEL = 99;
	// Items
	private static final int WARP_CRYSTAL = 39597;
	private static final int WARP_CRYSTAL_COUNT = 3;
	// Location
	private static final Location DIMENSIONAL_WARP = new Location(-76785, -217420, 4016);
	
	private DimensionalWarpTeleport()
	{
		super(DimensionalWarpTeleport.class.getSimpleName(), "ai/npc/Teleports");
		addStartNpc(RESED);
		addTalkId(RESED);
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		if (hasQuestItems(player, WARP_CRYSTAL) && (player.getLevel() >= MIN_LEVEL) && (getQuestItemsCount(player, WARP_CRYSTAL) >= WARP_CRYSTAL_COUNT) && player.isAwaken())
		{
			takeItems(player, WARP_CRYSTAL, 3);
			player.teleToLocation(DIMENSIONAL_WARP);
		}
		else if (player.getLevel() < MIN_LEVEL)
		{
			return "no_level.htm";
		}
		else if (getQuestItemsCount(player, WARP_CRYSTAL) < WARP_CRYSTAL_COUNT)
		{
			return "no_item.htm";
		}
		else
		{
			return "non_awakened.htm";
		}
		return super.onTalk(npc, player);
	}
	
	public static void main(String[] args)
	{
		new DimensionalWarpTeleport();
	}
}
