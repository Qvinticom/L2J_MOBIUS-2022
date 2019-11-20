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
package org.l2jmobius.gameserver.network.serverpackets;

public class CreatureSay extends ServerBasePacket
{
	private static final String _S__5D_CREATURESAY = "[S] 5D CreatureSay";
	private final int _objectId;
	private final int _textType;
	private final String _charName;
	private final String _text;
	
	public CreatureSay(int objectId, int messageType, String charName, String text)
	{
		_objectId = objectId;
		_textType = messageType;
		_charName = charName;
		_text = text;
	}
	
	@Override
	public byte[] getContent()
	{
		_bao.write(93);
		writeD(_objectId);
		writeD(_textType);
		writeS(_charName);
		writeS(_text);
		return _bao.toByteArray();
	}
	
	@Override
	public String getType()
	{
		return _S__5D_CREATURESAY;
	}
}
