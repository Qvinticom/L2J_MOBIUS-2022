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

import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;

/**
 * @version $Revision: 1.4.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public class CreatureSay extends GameServerPacket
{
	private final int _objectId;
	private final int _textType;
	private final String _charName;
	private final String _text;
	
	/**
	 * @param objectId
	 * @param messageType
	 * @param charName
	 * @param text
	 */
	public CreatureSay(int objectId, int messageType, String charName, String text)
	{
		_objectId = objectId;
		_textType = messageType;
		_charName = charName;
		_text = text;
		// setLifeTime(0);
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x4a);
		writeD(_objectId);
		writeD(_textType);
		writeS(_charName);
		writeS(_text);
		
		final PlayerInstance player = getClient().getPlayer();
		if (player != null)
		{
			player.broadcastSnoop(_textType, _charName, _text, this);
		}
	}
}