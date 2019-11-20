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
package org.l2jmobius.gameserver.model.actor.instance;

import java.util.logging.Logger;

import org.l2jmobius.gameserver.model.WorldObject;

public class DoorInstance extends WorldObject
{
	private static Logger _log = Logger.getLogger(DoorInstance.class.getName());
	private int _damage;
	private int _open;
	private int _enemy;
	private int _unknown;
	
	@Override
	public void onAction(PlayerInstance player)
	{
		_log.fine("Door activated");
	}
	
	public int getDamage()
	{
		return _damage;
	}
	
	public void setDamage(int damage)
	{
		_damage = damage;
	}
	
	public int getEnemy()
	{
		return _enemy;
	}
	
	public void setEnemy(int enemy)
	{
		_enemy = enemy;
	}
	
	public int getOpen()
	{
		return _open;
	}
	
	public void setOpen(int open)
	{
		_open = open;
	}
	
	public int getUnknown()
	{
		return _unknown;
	}
	
	public void setUnknown(int unknown)
	{
		_unknown = unknown;
	}
}
