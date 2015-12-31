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
import com.l2jmobius.gameserver.instancemanager.DuelManager;
import com.l2jmobius.gameserver.model.L2World;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.SystemMessageId;
import com.l2jmobius.gameserver.network.serverpackets.ExDuelAskStart;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * Format:(ch) Sd
 * @author -Wooden-
 */
public final class RequestDuelStart extends L2GameClientPacket
{
	private static final String _C__D0_1B_REQUESTDUELSTART = "[C] D0:1B RequestDuelStart";
	
	private String _player;
	private int _partyDuel;
	
	@Override
	protected void readImpl()
	{
		_player = readS();
		_partyDuel = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		final L2PcInstance targetChar = L2World.getInstance().getPlayer(_player);
		final boolean isPartyDuel = _partyDuel == 1 ? true : false;
		if ((activeChar == null) || (targetChar == null))
		{
			return;
		}
		if (activeChar == targetChar)
		{
			if (isPartyDuel)
			{
				activeChar.sendPacket(SystemMessageId.THERE_IS_NO_OPPONENT_TO_RECEIVE_YOUR_CHALLENGE_FOR_A_DUEL);
			}
			return;
		}
		if (!activeChar.isInsideRadius(targetChar, 250, false, false))
		{
			final SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_TOO_FAR_AWAY_TO_RECEIVE_A_DUEL_CHALLENGE);
			msg.addString(targetChar.getName());
			activeChar.sendPacket(msg);
			return;
		}
		if (Config.FACTION_SYSTEM_ENABLED && ((activeChar.isEvil() && targetChar.isGood()) || (activeChar.isGood() && targetChar.isEvil())))
		{
			activeChar.sendPacket(SystemMessageId.YOU_ARE_UNABLE_TO_REQUEST_A_DUEL_AT_THIS_TIME);
			return;
		}
		// Check if duel is possible
		if (!DuelManager.canDuel(activeChar, activeChar, isPartyDuel))
		{
			return;
		}
		if (!DuelManager.canDuel(activeChar, targetChar, isPartyDuel))
		{
			return;
		}
		
		// Duel is a party duel
		if (isPartyDuel)
		{
			// Player must be in a party & the party leader
			if (!activeChar.isInParty() || !activeChar.getParty().isLeader(activeChar) || !targetChar.isInParty() || activeChar.getParty().containsPlayer(targetChar))
			{
				activeChar.sendPacket(SystemMessageId.YOU_ARE_UNABLE_TO_REQUEST_A_DUEL_AT_THIS_TIME);
				return;
			}
			
			// Check if every player is ready for a duel
			for (L2PcInstance temp : activeChar.getParty().getMembers())
			{
				if (!DuelManager.canDuel(activeChar, temp, isPartyDuel))
				{
					return;
				}
			}
			final L2PcInstance partyLeader = targetChar.getParty().getLeader(); // snatch party leader of targetChar's party
			
			for (L2PcInstance temp : targetChar.getParty().getMembers())
			{
				if (!DuelManager.canDuel(activeChar, temp, isPartyDuel))
				{
					return;
				}
			}
			
			// Send request to targetChar's party leader
			if (partyLeader != null)
			{
				if (!partyLeader.isProcessingRequest())
				{
					activeChar.onTransactionRequest(partyLeader);
					partyLeader.sendPacket(new ExDuelAskStart(activeChar.getName(), _partyDuel));
					
					if (Config.DEBUG)
					{
						_log.fine(activeChar.getName() + " requested a duel with " + partyLeader.getName());
					}
					
					SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.C1_S_PARTY_HAS_BEEN_CHALLENGED_TO_A_DUEL);
					msg.addString(partyLeader.getName());
					activeChar.sendPacket(msg);
					
					msg = SystemMessage.getSystemMessage(SystemMessageId.C1_S_PARTY_HAS_CHALLENGED_YOUR_PARTY_TO_A_DUEL);
					msg.addString(activeChar.getName());
					targetChar.sendPacket(msg);
				}
				else
				{
					final SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_ON_ANOTHER_TASK_PLEASE_TRY_AGAIN_LATER);
					msg.addString(partyLeader.getName());
					activeChar.sendPacket(msg);
				}
			}
		}
		else
		// 1vs1 duel
		{
			if (!targetChar.isProcessingRequest())
			{
				activeChar.onTransactionRequest(targetChar);
				targetChar.sendPacket(new ExDuelAskStart(activeChar.getName(), _partyDuel));
				
				if (Config.DEBUG)
				{
					_log.fine(activeChar.getName() + " requested a duel with " + targetChar.getName());
				}
				
				SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.C1_HAS_BEEN_CHALLENGED_TO_A_DUEL);
				msg.addString(targetChar.getName());
				activeChar.sendPacket(msg);
				
				msg = SystemMessage.getSystemMessage(SystemMessageId.C1_HAS_CHALLENGED_YOU_TO_A_DUEL);
				msg.addString(activeChar.getName());
				targetChar.sendPacket(msg);
			}
			else
			{
				final SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_ON_ANOTHER_TASK_PLEASE_TRY_AGAIN_LATER);
				msg.addString(targetChar.getName());
				activeChar.sendPacket(msg);
			}
		}
	}
	
	@Override
	public String getType()
	{
		return _C__D0_1B_REQUESTDUELSTART;
	}
}
