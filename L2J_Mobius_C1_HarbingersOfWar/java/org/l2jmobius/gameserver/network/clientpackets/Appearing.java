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

import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.instance.ItemInstance;
import org.l2jmobius.gameserver.model.actor.instance.NpcInstance;
import org.l2jmobius.gameserver.model.actor.instance.PetInstance;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.ClientThread;
import org.l2jmobius.gameserver.network.serverpackets.CharInfo;
import org.l2jmobius.gameserver.network.serverpackets.NpcInfo;
import org.l2jmobius.gameserver.network.serverpackets.SpawnItem;
import org.l2jmobius.gameserver.network.serverpackets.UserInfo;

public class Appearing extends ClientBasePacket
{
	private static final String _C__30_APPEARING = "[C] 30 Appearing";
	
	public Appearing(byte[] decrypt, ClientThread client)
	{
		super(decrypt);
		final PlayerInstance activeChar = client.getActiveChar();
		activeChar.removeAllKnownObjects();
		
		activeChar.sendPacket(new UserInfo(activeChar));
		for (WorldObject worldObject : World.getInstance().getVisibleObjects(activeChar, 2000))
		{
			activeChar.addKnownObject(worldObject);
			if (worldObject instanceof ItemInstance)
			{
				activeChar.sendPacket(new SpawnItem((ItemInstance) worldObject));
				continue;
			}
			if (worldObject instanceof NpcInstance)
			{
				activeChar.sendPacket(new NpcInfo((NpcInstance) worldObject));
				final NpcInstance npc = (NpcInstance) worldObject;
				npc.addKnownObject(activeChar);
				continue;
			}
			if (worldObject instanceof PetInstance)
			{
				activeChar.sendPacket(new NpcInfo((PetInstance) worldObject));
				final PetInstance pet = (PetInstance) worldObject;
				pet.addKnownObject(activeChar);
				continue;
			}
			if (worldObject instanceof PlayerInstance)
			{
				final PlayerInstance player = (PlayerInstance) worldObject;
				activeChar.sendPacket(new CharInfo(player));
				player.addKnownObject(activeChar);
				player.sendPacket(new CharInfo(activeChar));
			}
		}
		World.getInstance().addVisibleObject(activeChar);
	}
	
	@Override
	public String getType()
	{
		return _C__30_APPEARING;
	}
}
