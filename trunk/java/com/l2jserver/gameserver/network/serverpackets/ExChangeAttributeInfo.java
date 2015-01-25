/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jserver.gameserver.network.serverpackets;

/**
 * @author Erlandys
 */
public class ExChangeAttributeInfo extends L2GameServerPacket
{
	private final int itemOID;
	private final int attributeOID;
	private final int attributes;
	
	public ExChangeAttributeInfo(int _attributeOID, int _itemOID, int _attribute)
	{
		itemOID = _itemOID;
		attributeOID = _attributeOID;
		switch (_attribute)
		{
			case 0:
				attributes = -2;
				break;
			case 1:
				attributes = -3;
				break;
			case 2:
				attributes = -5;
				break;
			case 3:
				attributes = -9;
				break;
			case 4:
				attributes = -17;
				break;
			default:
				attributes = -33;
				break;
		}
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x114);
		writeD(attributeOID);
		writeD(attributes);
		
	}
}
