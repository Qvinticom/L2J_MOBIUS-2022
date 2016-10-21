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
package ai.npc.Teleports.KargosTeleport;

import com.l2jmobius.gameserver.model.Location;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;

import ai.AbstractNpcAI;

/**
 * @author hlwrave
 */
public final class KargosTeleport extends AbstractNpcAI
{
	// NPC
	private static final int KARGOS = 33821;
	// Items
	private static final int PAGAN_MARK = 8067;
	private static final int VISITOR_MARK = 8064;
	// Locations
	private static final Location PAGAN_TEMPLE = new Location(-16350, -37579, -10725);
	private static final Location PAGAN_ROOM = new Location(-12766, -35840, -10851);
	
	private KargosTeleport()
	{
		super(KargosTeleport.class.getSimpleName(), "ai/npc/Teleports");
		addStartNpc(KARGOS);
		addTalkId(KARGOS);
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		if (hasQuestItems(player, PAGAN_MARK, VISITOR_MARK))
		{
			player.teleToLocation(PAGAN_TEMPLE);
		}
		else if (hasQuestItems(player, VISITOR_MARK))
		{
			player.teleToLocation(PAGAN_ROOM);
		}
		else
		{
			return "noItem.htm";
		}
		return super.onTalk(npc, player);
	}
	
	public static void main(String[] args)
	{
		new KargosTeleport();
	}
}
