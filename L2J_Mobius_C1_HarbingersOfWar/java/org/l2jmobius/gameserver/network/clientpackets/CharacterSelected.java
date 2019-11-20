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
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.serverpackets.CharSelected;

public class CharacterSelected extends ClientBasePacket
{
	private static final String _C__0D_CHARACTERSELECTED = "[C] 0D CharacterSelected";
	
	public CharacterSelected(byte[] decrypt, ClientThread client) throws IOException
	{
		super(decrypt);
		int charSlot = readD();
		PlayerInstance cha = client.loadCharFromDisk(charSlot);
		cha.setMoveType(1);
		cha.setWaitType(1);
		CharSelected cs = new CharSelected(cha, client.getSessionId());
		client.getConnection().sendPacket(cs);
		client.setActiveChar(cha);
	}
	
	@Override
	public String getType()
	{
		return _C__0D_CHARACTERSELECTED;
	}
}
