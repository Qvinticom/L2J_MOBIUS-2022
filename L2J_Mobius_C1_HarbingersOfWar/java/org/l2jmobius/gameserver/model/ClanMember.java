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
package org.l2jmobius.gameserver.model;

import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;

public class ClanMember
{
	private int _objectId;
	private String _name;
	private int _level;
	private int _classId;
	private PlayerInstance _player;
	
	public ClanMember(String name, int level, int classId, int objectId)
	{
		_name = name;
		_level = level;
		_classId = classId;
		_objectId = objectId;
	}
	
	public ClanMember(PlayerInstance player)
	{
		_player = player;
	}
	
	public void setPlayerInstance(PlayerInstance player)
	{
		if ((player == null) && (_player != null))
		{
			_name = _player.getName();
			_level = _player.getLevel();
			_classId = _player.getClassId();
			_objectId = _player.getObjectId();
		}
		_player = player;
	}
	
	public PlayerInstance getPlayerInstance()
	{
		return _player;
	}
	
	public boolean isOnline()
	{
		return _player != null;
	}
	
	public int getClassId()
	{
		if (_player != null)
		{
			return _player.getClassId();
		}
		return _classId;
	}
	
	public int getLevel()
	{
		if (_player != null)
		{
			return _player.getLevel();
		}
		return _level;
	}
	
	public String getName()
	{
		if (_player != null)
		{
			return _player.getName();
		}
		return _name;
	}
	
	public int getObjectId()
	{
		if (_player != null)
		{
			return _player.getObjectId();
		}
		return _objectId;
	}
	
	public String getTitle()
	{
		if (_player != null)
		{
			return _player.getTitle();
		}
		return " ";
	}
}
