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
package com.l2jmobius.gameserver.network.serverpackets.adenadistribution;

import com.l2jmobius.gameserver.network.serverpackets.L2GameServerPacket;

/**
 * @author Sdw
 */
public class ExDivideAdenaDone extends L2GameServerPacket
{
	private final long _adenaCount;
	private final long _distributedAdenaCount;
	private final int _memberCount;
	private final String _distributorName;
	
	public ExDivideAdenaDone(long adenaCount, long distributedAdenaCount, int memberCount, String distributorName)
	{
		_adenaCount = adenaCount;
		_distributedAdenaCount = distributedAdenaCount;
		_memberCount = memberCount;
		_distributorName = distributorName;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x15D);
		
		writeC(0x00); // TODO: Find me / type ??
		writeC(0x00); // TODO: Find me
		writeD(_memberCount);
		writeQ(_distributedAdenaCount);
		writeQ(_adenaCount);
		writeS(_distributorName);
	}
}
