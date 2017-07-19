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

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.datatables.GmListTable;
import com.l2jmobius.gameserver.instancemanager.PetitionManager;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * <p>
 * Format: (c) Sd
 * <ul>
 * <li>S: content</li>
 * <li>d: type</li>
 * </ul>
 * </p>
 * @author -Wooden-, TempyIncursion
 */
public class RequestPetition extends L2GameClientPacket
{
	private static final String _C__7F_RequestPetition = "[C] 7F RequestPetition";
	// private static Logger _log = Logger.getLogger(RequestPetition.class.getName());
	
	private String _content;
	private int _type; // 1 = on : 0 = off;
	
	@Override
	protected void readImpl()
	{
		_content = readS();
		_type = readD();
	}
	
	@Override
	public void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if (!GmListTable.getInstance().isGmOnline(false))
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.NO_GM_PROVIDING_SERVICE_NOW));
			return;
		}
		
		if (!PetitionManager.getInstance().isPetitioningAllowed())
		{
			activeChar.sendPacket(new SystemMessage(381));
			return;
		}
		
		if (PetitionManager.getInstance().isPlayerPetitionPending(activeChar))
		{
			activeChar.sendPacket(new SystemMessage(390));
			return;
		}
		
		if (PetitionManager.getInstance().getPendingPetitionCount() == Config.MAX_PETITIONS_PENDING)
		{
			activeChar.sendPacket(new SystemMessage(602));
			return;
		}
		
		final int totalPetitions = PetitionManager.getInstance().getPlayerTotalPetitionCount(activeChar) + 1;
		
		if (totalPetitions > Config.MAX_PETITIONS_PER_PLAYER)
		{
			SystemMessage sm = new SystemMessage(733);
			sm.addNumber(totalPetitions);
			activeChar.sendPacket(sm);
			sm = null;
			return;
		}
		
		if (_content.length() > 255)
		{
			activeChar.sendPacket(new SystemMessage(971));
			return;
		}
		
		final int petitionId = PetitionManager.getInstance().submitPetition(activeChar, _content, _type);
		
		SystemMessage sm = new SystemMessage(389);
		sm.addNumber(petitionId);
		activeChar.sendPacket(sm);
		
		sm = new SystemMessage(730);
		sm.addNumber(totalPetitions);
		sm.addNumber(Config.MAX_PETITIONS_PER_PLAYER - totalPetitions);
		activeChar.sendPacket(sm);
		
		sm = new SystemMessage(601);
		sm.addNumber(PetitionManager.getInstance().getPendingPetitionCount());
		activeChar.sendPacket(sm);
		sm = null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.network.clientpackets.L2GameClientPacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__7F_RequestPetition;
	}
}