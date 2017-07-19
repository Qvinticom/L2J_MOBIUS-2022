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

import com.l2jmobius.gameserver.model.L2Clan;
import com.l2jmobius.gameserver.model.L2World;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.serverpackets.AskJoinAlly;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * This class ...
 * @version $Revision: 1.3.4.2 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestJoinAlly extends L2GameClientPacket
{
	private static final String _C__82_REQUESTJOINALLY = "[C] 82 RequestJoinAlly";
	// private static Logger _log = Logger.getLogger(RequestJoinAlly.class.getName());
	
	private int _id;
	
	@Override
	protected void readImpl()
	{
		_id = readD();
	}
	
	@Override
	public void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
		{
			return;
		}
		
		if (!(L2World.getInstance().findObject(_id) instanceof L2PcInstance))
		{
			return;
		}
		
		final L2Clan clan = activeChar.getClan();
		if (clan == null)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_ARE_NOT_A_CLAN_MEMBER));
			return;
		}
		
		final L2PcInstance target = (L2PcInstance) L2World.getInstance().findObject(_id);
		
		if (!clan.CheckAllyJoinCondition(activeChar, target))
		{
			return;
		}
		
		if (!activeChar.getRequest().setRequest(target, this))
		{
			return;
		}
		
		SystemMessage sm = new SystemMessage(SystemMessage.S1_ALLIANCE_LEADER_OF_S2_REQUESTED_ALLIANCE);
		sm.addString(activeChar.getName());
		sm.addString(clan.getAllyName());
		target.sendPacket(sm);
		sm = null;
		
		target.sendPacket(new AskJoinAlly(activeChar.getObjectId(), activeChar.getName(), clan.getAllyName()));
	}
	
	@Override
	public String getType()
	{
		return _C__82_REQUESTJOINALLY;
	}
}