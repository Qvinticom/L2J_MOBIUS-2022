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

import org.l2jmobius.Config;
import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.SystemMessageId;

/**
 * @author Dezmond_snz - Packet Format: cddd
 */
public class DlgAnswer implements IClientIncomingPacket
{
	private int _messageId;
	private int _answer;
	// private int _requesterId;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_messageId = packet.readD();
		_answer = packet.readD();
		// _requesterId = packet.readD();
		return true;
	}
	
	@Override
	public void run(GameClient client)
	{
		final Player player = client.getPlayer();
		if (player == null)
		{
			return;
		}
		
		// final Long answerTime = player.getConfirmDlgRequestTime(_requesterId);
		// if ((_answer == 1) && (answerTime != null) && (Chronos.currentTimeMillis() > answerTime))
		// {
		// _answer = 0;
		// }
		// player.removeConfirmDlgRequestTime(_requesterId);
		
		if (_messageId == SystemMessageId.S1_IS_MAKING_AN_ATTEMPT_AT_RESURRECTION_DO_YOU_WANT_TO_CONTINUE_WITH_THIS_RESURRECTION.getId())
		{
			player.reviveAnswer(_answer);
		}
		// else if (_messageId == SystemMessageId.S1_WISHES_TO_SUMMON_YOU_FROM_S2_DO_YOU_ACCEPT.getId())
		// {
		// player.teleportAnswer(_answer, _requesterId);
		// }
		else if (_messageId == SystemMessageId.WOULD_YOU_LIKE_TO_OPEN_THE_GATE.getId())
		{
			player.gatesAnswer(_answer, 1);
		}
		else if (_messageId == SystemMessageId.WOULD_YOU_LIKE_TO_CLOSE_THE_GATE.getId())
		{
			player.gatesAnswer(_answer, 0);
		}
		else if ((_messageId == SystemMessageId.S1_S2.getId()) && Config.ALLOW_WEDDING)
		{
			player.engageAnswer(_answer);
		}
		else if (_messageId == SystemMessageId.S1.getId())
		{
			if (player.dialog != null)
			{
				player.dialog.onDlgAnswer(player);
				player.dialog = null;
			}
		}
	}
}