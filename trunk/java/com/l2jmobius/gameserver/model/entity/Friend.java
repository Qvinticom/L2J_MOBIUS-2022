/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jmobius.gameserver.model.entity;

import com.l2jmobius.gameserver.data.sql.impl.CharNameTable;
import com.l2jmobius.gameserver.model.L2World;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author Erlandys
 */
public class Friend
{
	int _relation;
	int _friendOID;
	String _memo;
	
	int _level = 1;
	int _classId = 0;
	int _clanId = 0;
	int _clanCrestId = 0;
	int _allyId = 0;
	int _allyCrestId = 0;
	String _name = "";
	String _clanName = "";
	String _allyName = "";
	long _createDate = -1;
	long _lastLogin = -1;
	
	public Friend(int relation, int friendOID, String memo)
	{
		_relation = relation;
		_friendOID = friendOID;
		_memo = memo;
	}
	
	public L2PcInstance getFriend()
	{
		return L2World.getInstance().getPlayer(_friendOID);
	}
	
	public int getFriendOID()
	{
		return _friendOID;
	}
	
	public String getMemo()
	{
		return _memo;
	}
	
	public void setMemo(String memo)
	{
		_memo = memo;
	}
	
	public int getLevel()
	{
		return _level;
	}
	
	public void setLevel(int level)
	{
		_level = level;
	}
	
	public int getClassId()
	{
		return _classId;
	}
	
	public void setClassId(int classId)
	{
		_classId = classId;
	}
	
	public int getClanId()
	{
		return _clanId;
	}
	
	public void setClanId(int clanId)
	{
		_clanId = clanId;
	}
	
	public int getClanCrestId()
	{
		return _clanCrestId;
	}
	
	public void setClanCrestId(int clanCrestId)
	{
		_clanCrestId = clanCrestId;
	}
	
	public int getAllyId()
	{
		return _allyId;
	}
	
	public void setAllyId(int allyId)
	{
		_allyId = allyId;
	}
	
	public int getAllyCrestId()
	{
		return _allyCrestId;
	}
	
	public void setAllyCrestId(int allyCrestId)
	{
		_allyCrestId = allyCrestId;
	}
	
	public String getName()
	{
		if (_name == "")
		{
			_name = CharNameTable.getInstance().getNameById(_friendOID);
		}
		return _name;
	}
	
	public String getClanName()
	{
		return _clanName;
	}
	
	public void setClanName(String clanName)
	{
		_clanName = clanName;
	}
	
	public String getAllyName()
	{
		return _allyName;
	}
	
	public void setAllyName(String allyName)
	{
		_allyName = allyName;
	}
	
	public long getCreateDate()
	{
		return _createDate;
	}
	
	public void setCreateDate(long createDate)
	{
		_createDate = createDate;
	}
	
	public long getLastLogin()
	{
		return _lastLogin;
	}
	
	public void setLastLogin(long lastLogin)
	{
		_lastLogin = lastLogin;
	}
}
