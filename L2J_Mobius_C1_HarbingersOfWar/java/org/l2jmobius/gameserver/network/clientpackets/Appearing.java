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

import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.instance.ItemInstance;
import org.l2jmobius.gameserver.model.actor.instance.NpcInstance;
import org.l2jmobius.gameserver.model.actor.instance.PetInstance;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.ClientThread;
import org.l2jmobius.gameserver.network.Connection;
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
		final PlayerInstance activeChar = client.getActiveChar();
		activeChar.removeAllKnownObjects();
		final Connection con = client.getConnection();
		final UserInfo ui = new UserInfo(activeChar);
		con.sendPacket(ui);
		for (WorldObject worldObject : World.getInstance().getVisibleObjects(activeChar, 2000))
		{
			NpcInfo ni;
			Creature npc;
			activeChar.addKnownObject(worldObject);
			if (worldObject instanceof ItemInstance)
			{
				final SpawnItem si = new SpawnItem((ItemInstance) worldObject);
				con.sendPacket(si);
				continue;
			}
			if (worldObject instanceof NpcInstance)
			{
				ni = new NpcInfo((NpcInstance) worldObject);
				con.sendPacket(ni);
				npc = (NpcInstance) worldObject;
				npc.addKnownObject(activeChar);
				continue;
			}
			if (worldObject instanceof PetInstance)
			{
				ni = new NpcInfo((PetInstance) worldObject);
				con.sendPacket(ni);
				npc = (PetInstance) worldObject;
				npc.addKnownObject(activeChar);
				continue;
			}
			if (!(worldObject instanceof PlayerInstance))
			{
				continue;
			}
			final PlayerInstance player = (PlayerInstance) worldObject;
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
