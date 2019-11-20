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

import org.l2jmobius.gameserver.ClientThread;
import org.l2jmobius.gameserver.Connection;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.instance.ItemInstance;
import org.l2jmobius.gameserver.model.actor.instance.NpcInstance;
import org.l2jmobius.gameserver.model.actor.instance.PetInstance;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.serverpackets.CharInfo;
import org.l2jmobius.gameserver.network.serverpackets.NpcInfo;
import org.l2jmobius.gameserver.network.serverpackets.SpawnItem;
import org.l2jmobius.gameserver.network.serverpackets.UserInfo;

public class Appearing extends ClientBasePacket
{
	private static final String _C__30_APPEARING = "[C] 30 Appearing";
	
	public Appearing(byte[] decrypt, ClientThread client) throws IOException
	{
		super(decrypt);
		PlayerInstance activeChar = client.getActiveChar();
		activeChar.removeAllKnownObjects();
		Connection con = client.getConnection();
		UserInfo ui = new UserInfo(activeChar);
		con.sendPacket(ui);
		WorldObject[] visible = World.getInstance().getVisibleObjects(activeChar, 2000);
		_log.fine("npc in range:" + visible.length);
		for (int i = 0; i < visible.length; ++i)
		{
			NpcInfo ni;
			Creature npc;
			activeChar.addKnownObject(visible[i]);
			if (visible[i] instanceof ItemInstance)
			{
				SpawnItem si = new SpawnItem((ItemInstance) visible[i]);
				con.sendPacket(si);
				continue;
			}
			if (visible[i] instanceof NpcInstance)
			{
				ni = new NpcInfo((NpcInstance) visible[i]);
				con.sendPacket(ni);
				npc = (NpcInstance) visible[i];
				npc.addKnownObject(activeChar);
				continue;
			}
			if (visible[i] instanceof PetInstance)
			{
				ni = new NpcInfo((PetInstance) visible[i]);
				con.sendPacket(ni);
				npc = (PetInstance) visible[i];
				npc.addKnownObject(activeChar);
				continue;
			}
			if (!(visible[i] instanceof PlayerInstance))
			{
				continue;
			}
			PlayerInstance player = (PlayerInstance) visible[i];
			con.sendPacket(new CharInfo(player));
			player.addKnownObject(activeChar);
			player.getNetConnection().sendPacket(new CharInfo(activeChar));
		}
		World.getInstance().addVisibleObject(activeChar);
	}
	
	@Override
	public String getType()
	{
		return _C__30_APPEARING;
	}
}
