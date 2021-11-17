/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.l2jmobius.gameserver.network.clientpackets;

import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.instancemanager.CastleManager;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.instance.StaticObject;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import org.l2jmobius.gameserver.network.serverpackets.ChairSit;

public class ChangeWaitType2 implements IClientIncomingPacket
{
	private boolean _typeStand;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_typeStand = packet.readD() == 1;
		return true;
	}
	
	@Override
	public void run(GameClient client)
	{
		final Player player = client.getPlayer();
		if (player == null)
		{
			return;
		}
		
		final WorldObject target = player.getTarget();
		if (player.isOutOfControl())
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.getMountType() != 0)
		{
			return;
		}
		
		if ((target != null) && !player.isSitting() && (target instanceof StaticObject) && (((StaticObject) target).getType() == 1) && (CastleManager.getInstance().getCastle(target) != null) && player.isInsideRadius2D(target, StaticObject.INTERACTION_DISTANCE))
		{
			final ChairSit cs = new ChairSit(player, ((StaticObject) target).getStaticObjectId());
			player.sendPacket(cs);
			player.sitDown();
			player.broadcastPacket(cs);
		}
		
		if (_typeStand)
		{
			player.standUp();
		}
		else
		{
			player.sitDown();
		}
	}
}