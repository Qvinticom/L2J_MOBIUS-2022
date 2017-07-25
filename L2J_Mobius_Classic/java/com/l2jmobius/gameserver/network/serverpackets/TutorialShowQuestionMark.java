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

import com.l2jmobius.commons.network.PacketWriter;
import com.l2jmobius.gameserver.network.OutgoingPackets;

public final class TutorialShowQuestionMark implements IClientOutgoingPacket
{
	private final int _number1; // cond?
	private final int _number2; // quest id?
	
	public TutorialShowQuestionMark(int number1)
	{
		_number1 = number1;
		_number2 = 0;
	}
	
	public TutorialShowQuestionMark(int number1, int number2)
	{
		_number1 = number1;
		_number2 = number2;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.TUTORIAL_SHOW_QUESTION_MARK.writeId(packet);
		
		packet.writeC(_number1);
		packet.writeD(_number2);
		return true;
	}
}