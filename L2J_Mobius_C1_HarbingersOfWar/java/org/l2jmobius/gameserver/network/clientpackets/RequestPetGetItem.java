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

import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.instance.ItemInstance;
import org.l2jmobius.gameserver.network.ClientThread;

public class RequestPetGetItem extends ClientBasePacket
{
	private static final String _C__8f_REQUESTPETGETITEM = "[C] 8F RequestPetGetItem";
	
	public RequestPetGetItem(byte[] decrypt, ClientThread client)
	{
		super(decrypt);
		int objectId = readD();
		World world = World.getInstance();
		ItemInstance item = (ItemInstance) world.findObject(objectId);
		client.getActiveChar().getPet().setTarget(item);
		client.getActiveChar().getPet().setCurrentState((byte) 1);
		client.getActiveChar().getPet().moveTo(item.getX(), item.getY(), item.getZ(), 0);
	}
	
	@Override
	public String getType()
	{
		return _C__8f_REQUESTPETGETITEM;
	}
}
