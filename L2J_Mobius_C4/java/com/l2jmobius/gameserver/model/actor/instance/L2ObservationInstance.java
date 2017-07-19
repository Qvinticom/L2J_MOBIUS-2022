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
package com.l2jmobius.gameserver.model.actor.instance;

import java.util.StringTokenizer;

import com.l2jmobius.gameserver.Olympiad;
import com.l2jmobius.gameserver.instancemanager.SiegeManager;
import com.l2jmobius.gameserver.model.entity.TvTEvent;
import com.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import com.l2jmobius.gameserver.network.serverpackets.ItemList;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import com.l2jmobius.gameserver.templates.L2NpcTemplate;

/**
 * @author NightMarez
 * @version $Revision: 1.3.2.2.2.5 $ $Date: 2005/03/27 15:29:32 $
 */
public final class L2ObservationInstance extends L2FolkInstance
{
	// private static Logger _log = Logger.getLogger(L2TeleporterInstance.class.getName());
	
	/**
	 * @param objectId
	 * @param template
	 */
	public L2ObservationInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		if (player == null)
		{
			return;
		}
		
		if (Olympiad.getInstance().isRegisteredInComp(player))
		{
			return;
		}
		
		if (TvTEvent.isRegistered(player))
		{
			return;
		}
		
		if (player.getEventTeam() > 0)
		{
			return;
		}
		
		if (command.startsWith("observeSiege"))
		{
			final String val = command.substring(13);
			final StringTokenizer st = new StringTokenizer(val);
			st.nextToken(); // Bypass cost
			
			if (SiegeManager.getInstance().getSiege(Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken())) != null)
			{
				doObserve(player, val);
			}
			else
			{
				player.sendPacket(new SystemMessage(SystemMessage.ONLY_VIEW_SIEGE));
			}
			
		}
		else if (command.startsWith("observe"))
		{
			doObserve(player, command.substring(8));
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
	
	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String pom = "";
		if (val == 0)
		{
			pom = "" + npcId;
		}
		else
		{
			pom = npcId + "-" + val;
		}
		
		return "data/html/observation/" + pom + ".htm";
	}
	
	private void doObserve(L2PcInstance player, String val)
	{
		final StringTokenizer st = new StringTokenizer(val);
		final int cost = Integer.parseInt(st.nextToken());
		final int x = Integer.parseInt(st.nextToken());
		final int y = Integer.parseInt(st.nextToken());
		final int z = Integer.parseInt(st.nextToken());
		if (player.reduceAdena("Broadcast", cost, this, true))
		{
			// enter mode
			if (!player.isTeleporting())
			{
				player.enterObserverMode(x, y, z);
			}
			final ItemList il = new ItemList(player, false);
			player.sendPacket(il);
		}
		player.sendPacket(new ActionFailed());
	}
}