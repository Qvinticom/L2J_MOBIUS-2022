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

import org.l2jmobius.gameserver.model.actor.Creature;

public class FinishRotation extends ServerBasePacket
{
	private static final String _S__78_FINISHROTATION = "[S] 78 FinishRotation";
	private final int _heading;
	private final int _objectId;
	
	public FinishRotation(Creature cha)
	{
		_objectId = cha.getObjectId();
		_heading = cha.getHeading();
	}
	
	@Override
	public byte[] getContent()
	{
		writeC(120);
		writeD(_objectId);
		writeD(_heading);
		return _bao.toByteArray();
	}
	
	@Override
	public String getType()
	{
		return _S__78_FINISHROTATION;
	}
}
