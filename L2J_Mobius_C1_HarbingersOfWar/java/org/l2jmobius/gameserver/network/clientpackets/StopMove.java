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

import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.ClientThread;
import org.l2jmobius.gameserver.network.serverpackets.StopMoveWithLocation;
import org.l2jmobius.gameserver.network.serverpackets.StopRotation;

public class StopMove extends ClientBasePacket
{
	private static final String _C__36_STOPMOVE = "[C] 36 StopMove";
	
	public StopMove(byte[] decrypt, ClientThread client)
	{
		super(decrypt);
		@SuppressWarnings("unused")
		int x = readD();
		@SuppressWarnings("unused")
		int y = readD();
		@SuppressWarnings("unused")
		int z = readD();
		int heading = readD();
		
		PlayerInstance player = client.getActiveChar();
		player.stopMove();
		StopMoveWithLocation smwl = new StopMoveWithLocation(player);
		client.getActiveChar().sendPacket(smwl);
		client.getActiveChar().broadcastPacket(smwl);
		StopRotation sr = new StopRotation(client.getActiveChar(), heading);
		client.getActiveChar().sendPacket(sr);
		client.getActiveChar().broadcastPacket(sr);
	}
	
	@Override
	public String getType()
	{
		return _C__36_STOPMOVE;
	}
}
