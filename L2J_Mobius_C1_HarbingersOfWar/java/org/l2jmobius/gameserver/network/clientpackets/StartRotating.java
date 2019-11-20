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

import org.l2jmobius.gameserver.ClientThread;
import org.l2jmobius.gameserver.network.serverpackets.BeginRotation;

public class StartRotating extends ClientBasePacket
{
	private static final String _C__4A_STARTROTATING = "[C] 4A StartRotating";
	
	public StartRotating(byte[] decrypt, ClientThread client)
	{
		super(decrypt);
		int degree = readD();
		int side = readD();
		BeginRotation br = new BeginRotation(client.getActiveChar(), degree, side);
		client.getActiveChar().sendPacket(br);
		client.getActiveChar().broadcastPacket(br);
	}
	
	@Override
	public String getType()
	{
		return _C__4A_STARTROTATING;
	}
}
