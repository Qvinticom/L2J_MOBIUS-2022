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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.instance.MonsterInstance;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.ClientThread;

public class ValidatePosition extends ClientBasePacket
{
	public ValidatePosition(byte[] decrypt, ClientThread client)
	{
		super(decrypt);
		@SuppressWarnings("unused")
		final int x = readD();
		@SuppressWarnings("unused")
		final int y = readD();
		final int z = readD();
		@SuppressWarnings("unused")
		final int heading = readD();
		@SuppressWarnings("unused")
		final int data = readD();
		final PlayerInstance activeChar = client.getActiveChar();
		if (activeChar != null)
		{
			activeChar.setZ(z);
			activeChar.updateKnownCounter = (byte) (activeChar.updateKnownCounter + 1);
			if (activeChar.updateKnownCounter > 3)
			{
				int i;
				int delete = 0;
				final Set<WorldObject> known = activeChar.getKnownObjects();
				final List<WorldObject> toBeDeleted = new ArrayList<>();
				for (WorldObject obj : known)
				{
					if (distance(activeChar, obj) <= 16000000L)
					{
						continue;
					}
					toBeDeleted.add(obj);
					++delete;
				}
				if (delete > 0)
				{
					for (i = 0; i < toBeDeleted.size(); ++i)
					{
						activeChar.removeKnownObject(toBeDeleted.get(i));
						if (toBeDeleted.get(i) instanceof MonsterInstance)
						{
							((MonsterInstance) toBeDeleted.get(i)).removeKnownObject(activeChar);
							continue;
						}
						toBeDeleted.get(i).removeKnownObject(activeChar);
					}
				}
				for (WorldObject worldObject : World.getInstance().getVisibleObjects(activeChar, 3000))
				{
					if (activeChar.knownsObject(worldObject))
					{
						continue;
					}
					activeChar.addKnownObject(worldObject);
					worldObject.addKnownObject(activeChar);
				}
				activeChar.updateKnownCounter = 0;
			}
			
			// Water check.
			activeChar.checkWaterState();
		}
	}
	
	private long distance(WorldObject a, WorldObject b)
	{
		final long dX = a.getX() - b.getX();
		final long dY = a.getY() - b.getY();
		return (dX * dX) + (dY * dY);
	}
}
