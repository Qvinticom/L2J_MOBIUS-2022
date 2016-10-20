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
package com.l2jmobius.gameserver.network.serverpackets;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jmobius.commons.mmocore.SendablePacket;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.interfaces.IPositionable;
import com.l2jmobius.gameserver.model.interfaces.IUpdateTypeComponent;
import com.l2jmobius.gameserver.network.L2GameClient;

/**
 * @author KenM
 */
public abstract class L2GameServerPacket extends SendablePacket<L2GameClient>
{
	protected static final Logger _log = Logger.getLogger(L2GameServerPacket.class.getName());
	
	private boolean _invisible = false;
	
	/**
	 * @return True if packet originated from invisible character.
	 */
	public boolean isInvisible()
	{
		return _invisible;
	}
	
	/**
	 * Set "invisible" boolean flag in the packet.<br>
	 * Packets from invisible characters will not be broadcasted to players.
	 * @param b
	 */
	public void setInvisible(boolean b)
	{
		_invisible = b;
	}
	
	/**
	 * Writes 3 D (int32) with current location x, y, z
	 * @param loc
	 */
	protected void writeLoc(IPositionable loc)
	{
		writeD(loc.getX());
		writeD(loc.getY());
		writeD(loc.getZ());
	}
	
	/**
	 * Write String
	 * @param str
	 */
	protected void writeString(String str)
	{
		if ((str == null) || str.isEmpty())
		{
			writeH(0x00);
			return;
		}
		final char[] chars = str.toCharArray();
		writeH(chars.length);
		for (char ch : chars)
		{
			_buf.putChar(ch);
		}
	}
	
	@Override
	protected void write()
	{
		try
		{
			writeImpl();
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Client: " + getClient().toString() + " - Failed writing: " + getClass().getSimpleName() + " ; " + e.getMessage(), e);
		}
	}
	
	public void runImpl()
	{
	}
	
	protected abstract void writeImpl();
	
	/**
	 * @param masks
	 * @param type
	 * @return {@code true} if the mask contains the current update component type
	 */
	protected static boolean containsMask(int masks, IUpdateTypeComponent type)
	{
		return (masks & type.getMask()) == type.getMask();
	}
	
	/**
	 * Sends this packet to the target player, useful for lambda operations like <br>
	 * {@code L2World.getInstance().getPlayers().forEach(packet::sendTo)}
	 * @param player
	 */
	public void sendTo(L2PcInstance player)
	{
		player.sendPacket(this);
	}
}
