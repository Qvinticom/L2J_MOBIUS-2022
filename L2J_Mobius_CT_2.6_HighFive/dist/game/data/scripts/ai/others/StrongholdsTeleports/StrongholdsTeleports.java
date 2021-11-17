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
package ai.others.StrongholdsTeleports;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;

import ai.AbstractNpcAI;

/**
 * Strongholds teleport AI.<br>
 * Original Jython script by Kerberos.
 * @author Plim
 */
public class StrongholdsTeleports extends AbstractNpcAI
{
	// NPCs
	private static final int[] NPCs =
	{
		32163,
		32181,
		32184,
		32186
	};
	
	private StrongholdsTeleports()
	{
		addFirstTalkId(NPCs);
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		if (player.getLevel() < 20)
		{
			return npc.getId() + ".htm";
		}
		return npc.getId() + "-no.htm";
	}
	
	public static void main(String[] args)
	{
		new StrongholdsTeleports();
	}
}
