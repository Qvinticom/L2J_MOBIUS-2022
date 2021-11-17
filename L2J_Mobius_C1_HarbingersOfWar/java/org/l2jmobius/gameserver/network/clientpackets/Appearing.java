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
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.instance.Npc;
import org.l2jmobius.gameserver.model.actor.instance.Pet;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.network.ClientThread;
import org.l2jmobius.gameserver.network.serverpackets.CharInfo;
import org.l2jmobius.gameserver.network.serverpackets.NpcInfo;
import org.l2jmobius.gameserver.network.serverpackets.SpawnItem;
import org.l2jmobius.gameserver.network.serverpackets.UserInfo;

public class Appearing extends ClientBasePacket
{
	public Appearing(byte[] decrypt, ClientThread client)
	{
		super(decrypt);
		final Player activeChar = client.getActiveChar();
		activeChar.removeAllKnownObjects();
		
		activeChar.sendPacket(new UserInfo(activeChar));
		for (WorldObject worldObject : World.getInstance().getVisibleObjects(activeChar, 2000))
		{
			activeChar.addKnownObject(worldObject);
			if (worldObject instanceof Item)
			{
				activeChar.sendPacket(new SpawnItem((Item) worldObject));
				continue;
			}
			if (worldObject instanceof Npc)
			{
				activeChar.sendPacket(new NpcInfo((Npc) worldObject));
				final Npc npc = (Npc) worldObject;
				npc.addKnownObject(activeChar);
				continue;
			}
			if (worldObject instanceof Pet)
			{
				activeChar.sendPacket(new NpcInfo((Pet) worldObject));
				final Pet pet = (Pet) worldObject;
				pet.addKnownObject(activeChar);
				continue;
			}
			if (worldObject instanceof Player)
			{
				final Player player = (Player) worldObject;
				activeChar.sendPacket(new CharInfo(player));
				player.addKnownObject(activeChar);
				player.sendPacket(new CharInfo(activeChar));
			}
		}
		World.getInstance().addVisibleObject(activeChar);
	}
}
