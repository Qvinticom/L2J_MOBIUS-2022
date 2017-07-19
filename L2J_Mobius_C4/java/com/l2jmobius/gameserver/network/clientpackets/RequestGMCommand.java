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
package com.l2jmobius.gameserver.network.clientpackets;

import com.l2jmobius.gameserver.model.L2World;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.serverpackets.GMViewCharacterInfo;
import com.l2jmobius.gameserver.network.serverpackets.GMViewItemList;
import com.l2jmobius.gameserver.network.serverpackets.GMViewPledgeInfo;
import com.l2jmobius.gameserver.network.serverpackets.GMViewQuestList;
import com.l2jmobius.gameserver.network.serverpackets.GMViewSkillInfo;
import com.l2jmobius.gameserver.network.serverpackets.GMViewWarehouseWithdrawList;

/**
 * This class ...
 * @version $Revision: 1.1.2.2.2.2 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestGMCommand extends L2GameClientPacket
{
	private static final String _C__6E_REQUESTGMCOMMAND = "[C] 6e RequestGMCommand";
	// private static Logger _log = Logger.getLogger(RequestGMCommand.class.getName());
	
	private String _targetName;
	private int _command;
	
	@Override
	protected void readImpl()
	{
		_targetName = readS();
		_command = readD();
	}
	
	@Override
	public void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if (!activeChar.isGM())
		{
			return;
		}
		
		final L2PcInstance player = L2World.getInstance().getPlayer(_targetName);
		if (player == null)
		{
			return;
		}
		
		switch (_command)
		{
			case 1: // player status
			{
				sendPacket(new GMViewCharacterInfo(player));
				break;
			}
			case 2: // player clan
			{
				if (player.getClan() != null)
				{
					sendPacket(new GMViewPledgeInfo(player.getClan(), player));
				}
				
				break;
			}
			case 3: // player skills
			{
				sendPacket(new GMViewSkillInfo(player));
				break;
			}
			case 4: // player quests
			{
				sendPacket(new GMViewQuestList(player));
				break;
			}
			case 5: // player inventory
			{
				sendPacket(new GMViewItemList(player));
				break;
			}
			case 6: // player warehouse
			{
				// defective packet
				sendPacket(new GMViewWarehouseWithdrawList(player));
				break;
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.network.clientpackets.L2GameClientPacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__6E_REQUESTGMCOMMAND;
	}
}