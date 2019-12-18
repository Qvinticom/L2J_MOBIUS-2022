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

import java.util.logging.Logger;

import org.l2jmobius.gameserver.data.SkillTable;
import org.l2jmobius.gameserver.model.Skill;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.ClientThread;

public class RequestMagicSkillUse extends ClientBasePacket
{
	private static final Logger _log = Logger.getLogger(RequestMagicSkillUse.class.getName());
	
	public RequestMagicSkillUse(byte[] rawPacket, ClientThread client)
	{
		super(rawPacket);
		final int magicId = readD();
		@SuppressWarnings("unused")
		final int data2 = readD();
		@SuppressWarnings("unused")
		final int data3 = readC();
		
		final PlayerInstance activeChar = client.getActiveChar();
		final int level = activeChar.getSkillLevel(magicId);
		final Skill skill = SkillTable.getInstance().getInfo(magicId, level);
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
}
