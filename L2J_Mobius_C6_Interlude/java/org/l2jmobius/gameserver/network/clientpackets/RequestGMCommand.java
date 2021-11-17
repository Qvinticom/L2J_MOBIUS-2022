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
package org.l2jmobius.gameserver.network.clientpackets;

import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.data.sql.ClanTable;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.clan.Clan;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.serverpackets.GMViewCharacterInfo;
import org.l2jmobius.gameserver.network.serverpackets.GMViewHennaInfo;
import org.l2jmobius.gameserver.network.serverpackets.GMViewItemList;
import org.l2jmobius.gameserver.network.serverpackets.GMViewPledgeInfo;
import org.l2jmobius.gameserver.network.serverpackets.GMViewQuestList;
import org.l2jmobius.gameserver.network.serverpackets.GMViewSkillInfo;
import org.l2jmobius.gameserver.network.serverpackets.GMViewWarehouseWithdrawList;

public class RequestGMCommand implements IClientIncomingPacket
{
	private String _targetName;
	private int _command;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_targetName = packet.readS();
		_command = packet.readD();
		return true;
	}
	
	@Override
	public void run(GameClient client)
	{
		// Prevent non GM or low level GMs from view.
		if (!client.getPlayer().isGM() || !client.getPlayer().getAccessLevel().allowAltG())
		{
			return;
		}
		
		final Player player = World.getInstance().getPlayer(_targetName);
		final Clan clan = ClanTable.getInstance().getClanByName(_targetName);
		
		// Player name was incorrect.
		if ((player == null) && ((clan == null) || (_command != 6)))
		{
			return;
		}
		
		switch (_command)
		{
			case 1: // player status
			{
				client.sendPacket(new GMViewCharacterInfo(player));
				client.sendPacket(new GMViewHennaInfo(player));
				break;
			}
			case 2: // player clan
			{
				if ((player != null) && (player.getClan() != null))
				{
					client.sendPacket(new GMViewPledgeInfo(player.getClan(), player));
				}
				break;
			}
			case 3: // player skills
			{
				client.sendPacket(new GMViewSkillInfo(player));
				break;
			}
			case 4: // player quests
			{
				client.sendPacket(new GMViewQuestList(player));
				break;
			}
			case 5: // player inventory
			{
				client.sendPacket(new GMViewItemList(player));
				client.sendPacket(new GMViewHennaInfo(player));
				break;
			}
			case 6: // player warehouse
			{
				// GM warehouse view to be implemented
				if (player != null)
				{
					client.sendPacket(new GMViewWarehouseWithdrawList(player));
				}
				else // clan warehouse
				{
					client.sendPacket(new GMViewWarehouseWithdrawList(clan));
				}
				break;
			}
		}
	}
}
