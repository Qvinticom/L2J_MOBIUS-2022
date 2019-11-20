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

import java.io.IOException;

import org.l2jmobius.gameserver.Announcements;
import org.l2jmobius.gameserver.ClientThread;
import org.l2jmobius.gameserver.Connection;
import org.l2jmobius.gameserver.GmListTable;
import org.l2jmobius.gameserver.model.Clan;
import org.l2jmobius.gameserver.model.ShortCut;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.serverpackets.Die;
import org.l2jmobius.gameserver.network.serverpackets.ItemList;
import org.l2jmobius.gameserver.network.serverpackets.ShortCutInit;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.network.serverpackets.UserInfo;

public class EnterWorld extends ClientBasePacket
{
	private static final String _C__03_ENTERWORLD = "[C] 03 EnterWorld";
	
	public EnterWorld(byte[] decrypt, ClientThread client) throws IOException
	{
		super(decrypt);
		PlayerInstance activeChar = client.getActiveChar();
		Connection con = client.getConnection();
		if (client.getAccessLevel() >= 100)
		{
			activeChar.setIsGM(true);
			GmListTable.getInstance().addGm(activeChar);
		}
		SystemMessage sm = new SystemMessage(34);
		con.sendPacket(sm);
		Announcements.getInstance().showAnnouncements(activeChar);
		ItemList il = new ItemList(activeChar, false);
		activeChar.sendPacket(il);
		ShortCutInit sci = new ShortCutInit();
		ShortCut[] shortcuts = activeChar.getAllShortCuts();
		block5: for (ShortCut shortcut : shortcuts)
		{
			switch (shortcut.getType())
			{
				case 3:
				{
					sci.addActionShotCut(shortcut.getSlot(), shortcut.getId(), shortcut.getUnk());
					continue block5;
				}
				case 2:
				{
					sci.addSkillShotCut(shortcut.getSlot(), shortcut.getId(), shortcut.getLevel(), shortcut.getUnk());
					continue block5;
				}
				case 1:
				{
					sci.addItemShotCut(shortcut.getSlot(), shortcut.getId(), shortcut.getUnk());
					continue block5;
				}
				default:
				{
					_log.warning("unknown shortcut type " + shortcut.getType());
				}
			}
		}
		con.sendPacket(sci);
		UserInfo ui = new UserInfo(activeChar);
		con.sendPacket(ui);
		if (activeChar.isDead())
		{
			activeChar.sendPacket(new Die(activeChar));
		}
		World.getInstance().addVisibleObject(activeChar);
		notifyClanMembers(activeChar);
	}
	
	private void notifyClanMembers(PlayerInstance activeChar)
	{
		Clan clan = activeChar.getClan();
		if (clan != null)
		{
			clan.getClanMember(activeChar.getName()).setPlayerInstance(activeChar);
			SystemMessage msg = new SystemMessage(304);
			msg.addString(activeChar.getName());
			PlayerInstance[] clanMembers = clan.getOnlineMembers(activeChar.getName());
			for (PlayerInstance clanMember : clanMembers)
			{
				clanMember.sendPacket(msg);
			}
		}
	}
	
	@Override
	public String getType()
	{
		return _C__03_ENTERWORLD;
	}
}
