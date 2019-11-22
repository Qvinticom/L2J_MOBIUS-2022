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
package org.l2jmobius.gameserver.network.clientpackets;

import org.l2jmobius.gameserver.data.SkillTable;
import org.l2jmobius.gameserver.model.Skill;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.ClientThread;

public class RequestMagicSkillUse extends ClientBasePacket
{
	private static final String _C__2F_REQUESTMAGICSKILLUSE = "[C] 2F RequestMagicSkillUse";
	
	public RequestMagicSkillUse(byte[] rawPacket, ClientThread client)
	{
		super(rawPacket);
		int magicId = readD();
		@SuppressWarnings("unused")
		int data2 = readD();
		@SuppressWarnings("unused")
		int data3 = readC();
		
		PlayerInstance activeChar = client.getActiveChar();
		int level = activeChar.getSkillLevel(magicId);
		Skill skill = SkillTable.getInstance().getInfo(magicId, level);
		if (skill != null)
		{
			activeChar.stopMove();
			activeChar.useMagic(skill);
		}
		else
		{
			_log.warning(activeChar + " tried to cast skill " + magicId);
		}
	}
	
	@Override
	public String getType()
	{
		return _C__2F_REQUESTMAGICSKILLUSE;
	}
}
