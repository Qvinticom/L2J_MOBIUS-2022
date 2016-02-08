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
package ai.npc.Teleports.HarnakUnderground;

import com.l2jmobius.gameserver.model.Location;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;

import ai.npc.AbstractNpcAI;

/**
 * @author hlwrave
 */
final class HarnakUnderground extends AbstractNpcAI
{
	// NPC
	private final static int HADEL = 33344;
	// Misc
	private final static int MIN_LEVEL = 85;
	// Location
	private final static Location HARNAK_UNDERGROUND = new Location(-114700, 147909, -7720);
	
	private HarnakUnderground()
	{
		super(HarnakUnderground.class.getSimpleName(), "ai/npc/Teleports");
		addStartNpc(HADEL);
		addTalkId(HADEL);
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		if ((player.getLevel() >= MIN_LEVEL) && (player.isAwaken()))
		{
			player.teleToLocation(HARNAK_UNDERGROUND);
		}
		else if (player.getLevel() < MIN_LEVEL)
		{
			return "no_level.htm";
		}
		else
		{
			return "non_awakened.htm";
		}
		return super.onTalk(npc, player);
	}
	
	public static void main(String[] args)
	{
		new HarnakUnderground();
	}
}
