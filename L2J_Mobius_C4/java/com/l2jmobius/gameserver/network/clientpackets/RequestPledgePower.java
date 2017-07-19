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

import java.util.logging.Logger;

import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.serverpackets.ManagePledgePower;

public class RequestPledgePower extends L2GameClientPacket
{
	static Logger _log = Logger.getLogger(ManagePledgePower.class.getName());
	private static final String _C__C0_REQUESTPLEDGEPOWER = "[C] C0 RequestPledgePower";
	
	private int _clanMemberId;
	private int _action;
	private int _privs;
	
	@Override
	protected void readImpl()
	{
		_clanMemberId = readD();
		_action = readD();
		
		if (_action == 3)
		{
			_privs = readD();
		}
		else
		{
			_privs = 0;
		}
	}
	
	@Override
	public void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		
		if (player.getClan() != null)
		{
			L2PcInstance member = null;
			if (player.getClan().getClanMember(_clanMemberId) != null)
			{
				member = player.getClan().getClanMember(_clanMemberId).getPlayerInstance();
			}
			
			switch (_action)
			{
				case 1:
				{
					player.sendPacket(new ManagePledgePower(player.getClanPrivileges()));
					break;
				}
				
				case 2:
				{
					
					if (member != null)
					{
						player.sendPacket(new ManagePledgePower(member.getClanPrivileges()));
					}
					
					break;
					
				}
				case 3:
				{
					
					if (player.isClanLeader())
					
					{
						
						if (member != null)
						{
							member.setClanPrivileges(_privs);
						}
						
					}
					
					break;
					
				}
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
		return _C__C0_REQUESTPLEDGEPOWER;
	}
}