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
package com.l2jmobius.gameserver.communitybbs.Manager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.logging.Logger;

import com.l2jmobius.L2DatabaseFactory;
import com.l2jmobius.gameserver.communitybbs.BB.Forum;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;

import javolution.util.FastList;

public class ForumsBBSManager extends BaseBBSManager
{
	private static Logger _log = Logger.getLogger(ForumsBBSManager.class.getName());
	private final List<Forum> _table;
	private static ForumsBBSManager _Instance;
	private int lastid = 1;
	
	/**
	 * @return
	 */
	public static ForumsBBSManager getInstance()
	{
		if (_Instance == null)
		{
			_Instance = new ForumsBBSManager();
		}
		return _Instance;
	}
	
	public ForumsBBSManager()
	{
		_table = new FastList<>();
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT forum_id FROM forums WHERE forum_type=0");
			ResultSet result = statement.executeQuery())
		{
			while (result.next())
			{
				final int forumId = result.getInt("forum_id");
				final Forum f = new Forum(forumId, null);
				addForum(f);
			}
		}
		catch (final Exception e)
		{
			_log.warning("data error on Forum (root): " + e);
			e.printStackTrace();
		}
	}
	
	public void initRoot()
	{
		for (final Forum f : _table)
		{
			f.vload();
		}
		_log.info("Loaded " + _table.size() + " forums. Last forum id used: " + lastid);
	}
	
	public void addForum(Forum ff)
	{
		if (ff == null)
		{
			return;
		}
		
		_table.add(ff);
		
		if (ff.getID() > lastid)
		{
			lastid = ff.getID();
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.communitybbs.Manager.BaseBBSManager#parsecmd(java.lang.String, com.l2jmobius.gameserver.model.actor.instance.L2PcInstance)
	 */
	@Override
	public void parsecmd(String command, L2PcInstance activeChar)
	{
		// TODO Auto-generated method stub
	}
	
	/**
	 * @param Name
	 * @return
	 */
	public Forum getForumByName(String Name)
	{
		for (final Forum f : _table)
		{
			if (f.getName().equals(Name))
			{
				return f;
			}
		}
		return null;
	}
	
	/**
	 * @param name
	 * @param parent
	 * @param type
	 * @param perm
	 * @param oid
	 * @return
	 */
	public Forum CreateNewForum(String name, Forum parent, int type, int perm, int oid)
	{
		final Forum forum = new Forum(name, parent, type, perm, oid);
		forum.insertindb();
		return forum;
	}
	
	/**
	 * @return
	 */
	public int GetANewID()
	{
		return ++lastid;
	}
	
	/**
	 * @param idf
	 * @return
	 */
	public Forum getForumByID(int idf)
	{
		for (final Forum f : _table)
		{
			if (f.getID() == idf)
			{
				return f;
			}
		}
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.communitybbs.Manager.BaseBBSManager#parsewrite(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, com.l2jmobius.gameserver.model.actor.instance.L2PcInstance)
	 */
	@Override
	public void parsewrite(String ar1, String ar2, String ar3, String ar4, String ar5, L2PcInstance activeChar)
	{
		// TODO Auto-generated method stub
	}
}