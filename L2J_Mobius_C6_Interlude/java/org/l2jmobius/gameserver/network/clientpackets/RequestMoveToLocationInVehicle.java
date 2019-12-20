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

import org.l2jmobius.gameserver.ai.CtrlIntention;
import org.l2jmobius.gameserver.instancemanager.BoatManager;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.actor.instance.BoatInstance;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.items.type.WeaponType;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;

public class RequestMoveToLocationInVehicle extends GameClientPacket
{
	private final Location _pos = new Location(0, 0, 0);
	private final Location _originPos = new Location(0, 0, 0);
	private int _boatId;
	
	@Override
	protected void readImpl()
	{
		int x;
		int y;
		int z;
		_boatId = readD(); // objectId of boat
		x = readD();
		y = readD();
		z = readD();
		_pos.setXYZ(x, y, z);
		x = readD();
		y = readD();
		z = readD();
		_originPos.setXYZ(x, y, z);
	}
	
	@Override
	protected void runImpl()
	{
		final PlayerInstance player = getClient().getPlayer();
		if (player == null)
		{
			return;
		}
		
		if (player.isAttackingNow() && (player.getActiveWeaponItem() != null) && (player.getActiveWeaponItem().getItemType() == WeaponType.BOW))
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
		else
		{
			final BoatInstance boat = BoatManager.getInstance().GetBoat(_boatId);
			if (boat == null)
			{
				return;
			}
			player.setBoat(boat);
			player.setInBoat(true);
			player.setInBoatPosition(_pos);
			player.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO_IN_A_BOAT, new Location(_pos.getX(), _pos.getY(), _pos.getZ(), 0), new Location(_originPos.getX(), _originPos.getY(), _originPos.getZ(), 0));
		}
	}
}
