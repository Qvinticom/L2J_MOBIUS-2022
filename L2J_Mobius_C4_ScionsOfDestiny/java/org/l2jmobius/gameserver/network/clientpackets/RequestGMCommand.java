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
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
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
		// _unknown = packet.readD();
		return true;
	}
	
	@Override
	public void run(GameClient client)
	{
		final PlayerInstance player = World.getInstance().getPlayer(_targetName);
		
		// prevent non GM or low level GMs from vieweing player stuff
		if ((player == null) || !player.getAccessLevel().allowAltG())
		{
			return;
		}
		
		switch (_command)
		{
			case 1: // player status
			{
				player.sendPacket(new GMViewCharacterInfo(player));
				player.sendPacket(new GMViewHennaInfo(player));
				break;
			}
			case 2: // player clan
			{
				if (player.getClan() != null)
				{
					player.sendPacket(new GMViewPledgeInfo(player.getClan(), player));
				}
				break;
			}
			case 3: // player skills
			{
				player.sendPacket(new GMViewSkillInfo(player));
				break;
			}
			case 4: // player quests
			{
				player.sendPacket(new GMViewQuestList(player));
				break;
			}
			case 5: // player inventory
			{
				player.sendPacket(new GMViewItemList(player));
				player.sendPacket(new GMViewHennaInfo(player));
				break;
			}
			case 6: // player warehouse
			{
				// GM warehouse view to be implemented
				player.sendPacket(new GMViewWarehouseWithdrawList(player));
				break;
			}
		}
	}
}
