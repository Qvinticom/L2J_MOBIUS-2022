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

public class MyTargetSelected extends ServerBasePacket
{
	private static final String _S__BF_MYTARGETSELECTED = "[S] BF MyTargetSelected";
	private final int _objectId;
	private final int _color;
	
	public MyTargetSelected(int objectId, int color)
	{
		_objectId = objectId;
		_color = color;
	}
	
	@Override
	public byte[] getContent()
	{
		_bao.write(191);
		writeD(_objectId);
		writeH(_color);
		return _bao.toByteArray();
	}
	
	@Override
	public String getType()
	{
		return _S__BF_MYTARGETSELECTED;
	}
}
