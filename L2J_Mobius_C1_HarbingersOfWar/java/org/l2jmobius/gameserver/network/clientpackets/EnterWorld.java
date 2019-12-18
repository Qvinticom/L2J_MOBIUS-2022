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

import org.l2jmobius.gameserver.Announcements;
import org.l2jmobius.gameserver.data.MapRegionTable;
import org.l2jmobius.gameserver.managers.GmListManager;
import org.l2jmobius.gameserver.model.Clan;
import org.l2jmobius.gameserver.model.ShortCut;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.ClientThread;
import org.l2jmobius.gameserver.network.serverpackets.Die;
import org.l2jmobius.gameserver.network.serverpackets.ItemList;
import org.l2jmobius.gameserver.network.serverpackets.ShortCutInit;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.network.serverpackets.UserInfo;

public class EnterWorld extends ClientBasePacket
{
	private static final Logger _log = Logger.getLogger(EnterWorld.class.getName());
	
	public EnterWorld(byte[] decrypt, ClientThread client)
	{
		super(decrypt);
		final PlayerInstance activeChar = client.getActiveChar();
		if (client.getAccessLevel() >= 100)
		{
			activeChar.setIsGM(true);
			GmListManager.getInstance().addGm(activeChar);
		}
		
		activeChar.sendPacket(new SystemMessage(SystemMessage.WELCOME_TO_LINEAGE));
		Announcements.getInstance().showAnnouncements(activeChar);
		
		final ItemList il = new ItemList(activeChar, false);
		activeChar.sendPacket(il);
		
		final ShortCutInit sci = new ShortCutInit();
		for (ShortCut shortcut : activeChar.getAllShortCuts())
		{
			switch (shortcut.getType())
			{
				case ShortCut.TYPE_ACTION:
				{
					sci.addActionShotCut(shortcut.getSlot(), shortcut.getId(), shortcut.getUnk());
					continue;
				}
				case ShortCut.TYPE_SKILL:
				{
					sci.addSkillShotCut(shortcut.getSlot(), shortcut.getId(), shortcut.getLevel(), shortcut.getUnk());
					continue;
				}
				case ShortCut.TYPE_ITEM:
				{
					sci.addItemShotCut(shortcut.getSlot(), shortcut.getId(), shortcut.getUnk());
					continue;
				}
				default:
				{
					_log.warning("unknown shortcut type " + shortcut.getType());
				}
			}
		}
		activeChar.sendPacket(sci);
		
		activeChar.sendPacket(new UserInfo(activeChar));
		if (activeChar.isDead())
		{
			activeChar.sendPacket(new Die(activeChar));
		}
		World.getInstance().addVisibleObject(activeChar);
		notifyClanMembers(activeChar);
		
		// Fallen in game graphics?
		if (activeChar.getZ() < -16000)
		{
			final int[] townCords = MapRegionTable.getInstance().getClosestTownCords(activeChar);
			activeChar.teleToLocation(townCords[0], townCords[1], townCords[2]);
		}
		
		// Water check.
		activeChar.checkWaterState();
	}
	
	private void notifyClanMembers(PlayerInstance activeChar)
	{
		final Clan clan = activeChar.getClan();
		if (clan != null)
		{
			clan.getClanMember(activeChar.getName()).setPlayerInstance(activeChar);
			final SystemMessage msg = new SystemMessage(SystemMessage.CLAN_MEMBER_S1_LOGGED_IN);
			msg.addString(activeChar.getName());
			for (PlayerInstance clanMember : clan.getOnlineMembers(activeChar.getName()))
			{
				clanMember.sendPacket(msg);
			}
		}
	}
}
