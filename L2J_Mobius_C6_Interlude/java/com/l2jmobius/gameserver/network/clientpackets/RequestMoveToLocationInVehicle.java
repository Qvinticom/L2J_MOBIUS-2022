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
package com.l2jmobius.gameserver.network.clientpackets;

import com.l2jmobius.commons.util.Point3D;
import com.l2jmobius.gameserver.ai.CtrlIntention;
import com.l2jmobius.gameserver.instancemanager.BoatManager;
import com.l2jmobius.gameserver.model.actor.instance.BoatInstance;
import com.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import com.l2jmobius.gameserver.model.actor.position.Location;
import com.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import com.l2jmobius.gameserver.templates.item.WeaponType;
import com.l2jmobius.gameserver.thread.TaskPriority;

public final class RequestMoveToLocationInVehicle extends GameClientPacket
{
	private final Point3D _pos = new Point3D(0, 0, 0);
	private final Point3D _origin_pos = new Point3D(0, 0, 0);
	private int _boatId;
	
	public TaskPriority getPriority()
	{
		return TaskPriority.PR_HIGH;
	}
	
	@Override
	protected void readImpl()
	{
		int _x;
		int _y;
		int _z;
		_boatId = readD(); // objectId of boat
		_x = readD();
		_y = readD();
		_z = readD();
		_pos.setXYZ(_x, _y, _z);
		_x = readD();
		_y = readD();
		_z = readD();
		_origin_pos.setXYZ(_x, _y, _z);
	}
	
	@Override
	protected void runImpl()
	{
		final PlayerInstance player = getClient().getPlayer();
		
		if (player == null)
		{
			return;
		}
		else if (player.isAttackingNow() && (player.getActiveWeaponItem() != null) && (player.getActiveWeaponItem().getItemType() == WeaponType.BOW))
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
			player.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO_IN_A_BOAT, new Location(_pos.getX(), _pos.getY(), _pos.getZ(), 0), new Location(_origin_pos.getX(), _origin_pos.getY(), _origin_pos.getZ(), 0));
		}
	}
}
