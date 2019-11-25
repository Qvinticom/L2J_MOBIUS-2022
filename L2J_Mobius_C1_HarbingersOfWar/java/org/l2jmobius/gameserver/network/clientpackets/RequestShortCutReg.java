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

import org.l2jmobius.gameserver.model.ShortCut;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.ClientThread;
import org.l2jmobius.gameserver.network.serverpackets.ShortCutRegister;

public class RequestShortCutReg extends ClientBasePacket
{
	public RequestShortCutReg(byte[] rawPacket, ClientThread client)
	{
		super(rawPacket);
		final int type = readD();
		final int slot = readD();
		final int id = readD();
		final int unk = readD();
		
		final PlayerInstance activeChar = client.getActiveChar();
		switch (type)
		{
			case ShortCut.TYPE_ITEM:
			case ShortCut.TYPE_ACTION:
			{
				activeChar.sendPacket(new ShortCutRegister(slot, type, id, unk));
				activeChar.registerShortCut(new ShortCut(slot, type, id, -1, unk));
				break;
			}
			case ShortCut.TYPE_SKILL:
			{
				final int level = activeChar.getSkillLevel(id);
				if (level <= 0)
				{
					break;
				}
				activeChar.sendPacket(new ShortCutRegister(slot, type, id, level, unk));
				activeChar.registerShortCut(new ShortCut(slot, type, id, level, unk));
				break;
			}
		}
	}
}
