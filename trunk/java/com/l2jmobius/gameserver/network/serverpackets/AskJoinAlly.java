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

public class AskJoinAlly extends L2GameServerPacket
{
	private final String _requestorName;
	private final int _requestorObjId;
	private final String _requestorAllyName;
	
	/**
	 * @param requestorObjId
	 * @param requestorAllyName
	 * @param requestorName
	 */
	public AskJoinAlly(int requestorObjId, String requestorAllyName, String requestorName)
	{
		_requestorName = requestorName;
		_requestorObjId = requestorObjId;
		_requestorAllyName = requestorAllyName;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xbb);
		writeD(_requestorObjId);
		writeS(_requestorAllyName);
		writeS(null); // TODO: Find me!
		writeS(_requestorName);
	}
}
