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

import org.l2jmobius.gameserver.datatables.sql.HennaTreeTable;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.templates.NpcTemplate;
import org.l2jmobius.gameserver.network.serverpackets.HennaEquipList;
import org.l2jmobius.gameserver.network.serverpackets.ItemList;

public class SymbolMakerInstance extends FolkInstance
{
	@Override
	public void onBypassFeedback(PlayerInstance player, String command)
	{
		if (command.equals("Draw"))
		{
			final HennaInstance[] henna = HennaTreeTable.getInstance().getAvailableHenna(player.getClassId());
			final HennaEquipList hel = new HennaEquipList(player, henna);
			player.sendPacket(hel);
			
			player.sendPacket(new ItemList(player, false));
		}
		else if (command.equals("RemoveList"))
		{
			showRemoveChat(player);
		}
		else if (command.startsWith("Remove "))
		{
			if (!player.getClient().getFloodProtectors().getTransaction().tryPerformAction("HennaRemove"))
			{
				return;
			}
			
			final int slot = Integer.parseInt(command.substring(7));
			player.removeHenna(slot);
			
			player.sendPacket(new ItemList(player, false));
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
	
	private void showRemoveChat(PlayerInstance player)
	{
		final StringBuilder html1 = new StringBuilder("<html><body>");
		html1.append("Select symbol you would like to remove:<br><br>");
		boolean hasHennas = false;
		
		for (int i = 1; i <= 3; i++)
		{
			final HennaInstance henna = player.getHennas(i);
			
			if (henna != null)
			{
				hasHennas = true;
				html1.append("<a action=\"bypass -h npc_%objectId%_Remove " + i + "\">" + henna.getName() + "</a><br>");
			}
		}
		if (!hasHennas)
		{
			html1.append("You don't have any symbol to remove!");
		}
		
		html1.append("</body></html>");
		insertObjectIdAndShowChatWindow(player, html1.toString());
	}
	
	public SymbolMakerInstance(int objectID, NpcTemplate template)
	{
		super(objectID, template);
	}
	
	@Override
	public String getHtmlPath(int npcId, int val)
	{
		return "data/html/symbolmaker/SymbolMaker.htm";
	}
	
	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		return false;
	}
}
