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

public class AutoAttackStart extends ServerBasePacket
{
	private static final String _S__3B_AUTOATTACKSTART = "[S] 3B AutoAttackStart";
	private final int _targetId;
	
	public AutoAttackStart(int targetId)
	{
		_targetId = targetId;
	}
	
	@Override
	public byte[] getContent()
	{
		_bao.write(59);
		writeD(_targetId);
		return _bao.toByteArray();
	}
	
	@Override
	public String getType()
	{
		return _S__3B_AUTOATTACKSTART;
	}
}
