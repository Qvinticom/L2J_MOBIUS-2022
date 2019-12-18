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

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.l2jmobius.gameserver.network.ClientThread;
import org.l2jmobius.gameserver.network.serverpackets.AllyCrest;

public class RequestAllyCrest extends ClientBasePacket
{
	private static final Logger _log = Logger.getLogger(RequestAllyCrest.class.getName());
	
	public RequestAllyCrest(byte[] rawPacket, ClientThread client) throws IOException
	{
		super(rawPacket);
		final int crestId = readD();
		final File crestFile = new File("data/allycrest_" + crestId + ".bmp");
		if (crestFile.exists())
		{
			client.getConnection().sendPacket(new AllyCrest(crestId, crestFile));
		}
		else
		{
			_log.warning("allycrest file is missing:" + crestFile.getAbsolutePath());
		}
	}
}
