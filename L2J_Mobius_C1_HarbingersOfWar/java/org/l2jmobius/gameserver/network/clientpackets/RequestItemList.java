/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.l2jmobius.gameserver.network.clientpackets;

import java.io.IOException;

import org.l2jmobius.gameserver.ClientThread;
import org.l2jmobius.gameserver.network.serverpackets.ItemList;

public class RequestItemList extends ClientBasePacket
{
	private static final String _C__0F_REQUESTITEMLIST = "[C] 0F RequestItemList";
	
	public RequestItemList(byte[] rawPacket, ClientThread client) throws IOException
	{
		super(rawPacket);
		ItemList il = new ItemList(client.getActiveChar(), true);
		client.getConnection().sendPacket(il);
	}
	
	@Override
	public String getType()
	{
		return _C__0F_REQUESTITEMLIST;
	}
}
