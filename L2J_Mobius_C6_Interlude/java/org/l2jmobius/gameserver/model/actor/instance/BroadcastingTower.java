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
package org.l2jmobius.gameserver.model.actor.instance;

import java.util.StringTokenizer;

import org.l2jmobius.gameserver.instancemanager.SiegeManager;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.templates.NpcTemplate;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import org.l2jmobius.gameserver.network.serverpackets.ItemList;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * The Class BroadcastingTower.
 * @author NightMarez
 * @version $Revision: 1.3.2.2.2.5 $ $Date: 2005/03/27 15:29:32 $
 */
public class BroadcastingTower extends Folk
{
	/**
	 * Instantiates a new observation instance.
	 * @param objectId the object id
	 * @param template the template
	 */
	public BroadcastingTower(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if (player.isInOlympiadMode())
		{
			player.sendMessage("You already participated in Olympiad!");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isRegisteredOnEvent())
		{
			player.sendMessage("You already participate in an event!");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isInCombat() || (player.getPvpFlag() > 0))
		{
			player.sendMessage("You are in combat now!");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (command.startsWith("observeSiege"))
		{
			final StringTokenizer st = new StringTokenizer(command);
			st.nextToken(); // Command
			
			final int x = Integer.parseInt(st.nextToken()); // X location
			final int y = Integer.parseInt(st.nextToken()); // Y location
			final int z = Integer.parseInt(st.nextToken()); // Z location
			if (SiegeManager.getInstance().getSiege(x, y, z) != null)
			{
				doObserve(player, command);
			}
			else
			{
				player.sendPacket(new SystemMessage(SystemMessageId.OBSERVATION_IS_ONLY_POSSIBLE_DURING_A_SIEGE));
			}
		}
		else if (command.startsWith("observe"))
		{
			doObserve(player, command);
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
	
	@Override
	public String getHtmlPath(int npcId, int value)
	{
		String pom = "";
		if (value == 0)
		{
			pom = "" + npcId;
		}
		else
		{
			pom = npcId + "-" + value;
		}
		return "data/html/observation/" + pom + ".htm";
	}
	
	/**
	 * Do observe.
	 * @param player the player
	 * @param value the value
	 */
	private void doObserve(Player player, String value)
	{
		final StringTokenizer st = new StringTokenizer(value);
		st.nextToken(); // Command
		final int x = Integer.parseInt(st.nextToken());
		final int y = Integer.parseInt(st.nextToken());
		final int z = Integer.parseInt(st.nextToken());
		final int cost = Integer.parseInt(st.nextToken());
		if (player.reduceAdena("Broadcast", cost, this, true))
		{
			// enter mode
			player.enterObserverMode(x, y, z);
			player.sendPacket(new ItemList(player, false));
		}
		
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
}
