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
package org.l2jmobius.gameserver.model;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.enums.CrestType;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.interfaces.IIdentifiable;
import org.l2jmobius.gameserver.network.serverpackets.AllyCrest;
import org.l2jmobius.gameserver.network.serverpackets.ExPledgeEmblem;
import org.l2jmobius.gameserver.network.serverpackets.PledgeCrest;

/**
 * @author NosBit
 */
public class Crest implements IIdentifiable
{
	private final int _id;
	private final byte[] _data;
	private final CrestType _type;
	
	public Crest(int id, byte[] data, CrestType type)
	{
		_id = id;
		_data = data;
		_type = type;
	}
	
	@Override
	public int getId()
	{
		return _id;
	}
	
	public byte[] getData()
	{
		return _data;
	}
	
	public CrestType getType()
	{
		return _type;
	}
	
	/**
	 * Gets the client path to crest for use in html and sends the crest to {@code Player}
	 * @param player the @{code Player} where html is send to.
	 * @return the client path to crest
	 */
	public String getClientPath(Player player)
	{
		String path = null;
		switch (_type)
		{
			case PLEDGE:
			{
				player.sendPacket(new PledgeCrest(_id, _data));
				path = "Crest.crest_" + Config.SERVER_ID + "_" + _id;
				break;
			}
			case PLEDGE_LARGE:
			{
				if (_data != null)
				{
					for (int i = 0; i <= 4; i++)
					{
						if (i < 4)
						{
							final byte[] fullChunk = new byte[14336];
							System.arraycopy(_data, (14336 * i), fullChunk, 0, 14336);
							player.sendPacket(new ExPledgeEmblem(_id, fullChunk, 0, i));
						}
						else
						{
							final byte[] lastChunk = new byte[8320];
							System.arraycopy(_data, (14336 * i), lastChunk, 0, 8320);
							player.sendPacket(new ExPledgeEmblem(_id, lastChunk, 0, i));
						}
					}
				}
				path = "Crest.crest_" + Config.SERVER_ID + "_" + _id + "_l";
				break;
			}
			case ALLY:
			{
				player.sendPacket(new AllyCrest(_id, _data));
				path = "Crest.crest_" + Config.SERVER_ID + "_" + _id;
				break;
			}
		}
		return path;
	}
}