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

import org.mmocore.network.SendablePacket;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.network.L2GameClient;

/**
 * @author KenM
 */
public abstract class L2GameServerPacket extends SendablePacket<L2GameClient>
{
	private static Logger _log = Logger.getLogger(L2GameServerPacket.class.getName());
	
	protected L2GameServerPacket()
	{
		if (Config.DEBUG)
		{
			_log.fine(getType());
		}
	}
	
	@Override
	protected void write()
	{
		try
		{
			writeImpl();
		}
		catch (final Exception e)
		{
			_log.log(Level.SEVERE, "Client: " + getClient().toString() + " - Failed writing: " + getType() + " ; " + e.getMessage(), e);
		}
	}
	
	public void runImpl()
	{
	}
	
	protected abstract void writeImpl();
	
	/**
	 * @return A String with this packet name for debuging purposes
	 */
	public abstract String getType();
}