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
package org.l2jmobius.gameserver.network.serverpackets;

import java.util.List;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.commons.util.Chronos;
import org.l2jmobius.gameserver.instancemanager.MailManager;
import org.l2jmobius.gameserver.model.Message;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * @author Migi, DS
 */
public class ExShowReceivedPostList implements IClientOutgoingPacket
{
	private final List<Message> _inbox;
	
	public ExShowReceivedPostList(int objectId)
	{
		_inbox = MailManager.getInstance().getInbox(objectId);
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_SHOW_RECEIVED_POST_LIST.writeId(packet);
		packet.writeD((int) (Chronos.currentTimeMillis() / 1000));
		if ((_inbox != null) && !_inbox.isEmpty())
		{
			packet.writeD(_inbox.size());
			for (Message msg : _inbox)
			{
				packet.writeD(msg.getId());
				packet.writeS(msg.getSubject());
				packet.writeS(msg.getSenderName());
				packet.writeD(msg.isLocked() ? 1 : 0);
				packet.writeD(msg.getExpirationSeconds());
				packet.writeD(msg.isUnread() ? 1 : 0);
				packet.writeD(1);
				packet.writeD(msg.hasAttachments() ? 1 : 0);
				packet.writeD(msg.getSendBySystem());
				packet.writeD(msg.isReturned() ? 1 : 0);
			}
		}
		else
		{
			packet.writeD(0);
		}
		return true;
	}
}
