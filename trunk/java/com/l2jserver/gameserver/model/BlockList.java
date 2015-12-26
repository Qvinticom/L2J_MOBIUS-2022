/*
 * Copyright (C) 2004-2015 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jserver.gameserver.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jserver.commons.database.pool.impl.ConnectionFactory;
import com.l2jserver.gameserver.data.sql.impl.CharNameTable;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.serverpackets.SystemMessage;
import com.l2jserver.gameserver.network.serverpackets.friend.BlockListPacket;

public class BlockList
{
	private static Logger _log = Logger.getLogger(BlockList.class.getName());
	private static Map<Integer, HashMap<Integer, String>> OFFLINE_LIST = new HashMap<>();
	
	private final L2PcInstance _owner;
	private HashMap<Integer, String> _blockList;
	private final ArrayList<Integer> _updateMemos = new ArrayList<>();
	
	public BlockList(L2PcInstance owner)
	{
		_owner = owner;
		_blockList = OFFLINE_LIST.get(owner.getObjectId());
		if (_blockList == null)
		{
			_blockList = loadList(_owner.getObjectId());
		}
	}
	
	private void addToBlockList(int target)
	{
		_blockList.put(target, "");
		persistInDB(target);
	}
	
	private void removeFromBlockList(int target)
	{
		_blockList.remove(Integer.valueOf(target));
		if (_updateMemos.contains(target))
		{
			_updateMemos.remove(Integer.valueOf(target));
		}
		removeFromDB(target);
	}
	
	public void setBlockMemo(int target, String memo)
	{
		if (_blockList.containsKey(target))
		{
			_blockList.put(target, memo);
			_updateMemos.add(target);
		}
	}
	
	public void updateBlockMemos()
	{
		try (Connection con = ConnectionFactory.getInstance().getConnection())
		{
			for (int target : _updateMemos)
			{
				try (PreparedStatement ps = con.prepareStatement("UPDATE character_friends SET memo=? WHERE charId=? AND friendId=? AND relation=1"))
				{
					ps.setString(1, _blockList.get(target));
					ps.setInt(2, _owner.getObjectId());
					ps.setInt(3, target);
					ps.execute();
				}
			}
		}
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		_blockList.clear();
	}
	
	public void playerLogout()
	{
		OFFLINE_LIST.put(_owner.getObjectId(), _blockList);
	}
	
	private static HashMap<Integer, String> loadList(int ObjId)
	{
		final HashMap<Integer, String> list = new HashMap<>();
		try (Connection con = ConnectionFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT friendId, memo FROM character_friends WHERE charId=? AND relation=1"))
		{
			ps.setInt(1, ObjId);
			try (ResultSet rs = ps.executeQuery())
			{
				int friendId;
				while (rs.next())
				{
					friendId = rs.getInt("friendId");
					if (friendId == ObjId)
					{
						continue;
					}
					final String memo = rs.getString("memo");
					list.put(friendId, memo);
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Error found in " + ObjId + " FriendList while loading BlockList: " + e.getMessage(), e);
		}
		return list;
	}
	
	private void removeFromDB(int targetId)
	{
		try (Connection con = ConnectionFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("DELETE FROM character_friends WHERE charId=? AND friendId=? AND relation=1"))
		{
			ps.setInt(1, _owner.getObjectId());
			ps.setInt(2, targetId);
			ps.execute();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Could not remove blocked player: " + e.getMessage(), e);
		}
	}
	
	private void persistInDB(int targetId)
	{
		try (Connection con = ConnectionFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("INSERT INTO character_friends (charId, friendId, relation) VALUES (?, ?, 1)"))
		{
			ps.setInt(1, _owner.getObjectId());
			ps.setInt(2, targetId);
			ps.execute();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Could not add blocked player: " + e.getMessage(), e);
		}
	}
	
	public boolean isInBlockList(L2PcInstance target)
	{
		return _blockList.containsKey(target.getObjectId());
	}
	
	public boolean isInBlockList(int targetId)
	{
		return _blockList.containsKey(targetId);
	}
	
	public boolean isBlockAll()
	{
		return _owner.getMessageRefusal();
	}
	
	public static boolean isBlocked(L2PcInstance listOwner, L2PcInstance target)
	{
		final BlockList blockList = listOwner.getBlockList();
		return blockList.isBlockAll() || blockList.isInBlockList(target);
	}
	
	public static boolean isBlocked(L2PcInstance listOwner, int targetId)
	{
		final BlockList blockList = listOwner.getBlockList();
		return blockList.isBlockAll() || blockList.isInBlockList(targetId);
	}
	
	private void setBlockAll(boolean state)
	{
		_owner.setMessageRefusal(state);
	}
	
	public HashMap<Integer, String> getBlockList()
	{
		return _blockList;
	}
	
	public static void addToBlockList(L2PcInstance listOwner, int targetId)
	{
		if (listOwner == null)
		{
			return;
		}
		
		final String charName = CharNameTable.getInstance().getNameById(targetId);
		
		if (listOwner.getFriendList().containsKey(targetId))
		{
			final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.THIS_PLAYER_IS_ALREADY_REGISTERED_ON_YOUR_FRIENDS_LIST);
			sm.addString(charName);
			listOwner.sendPacket(sm);
			return;
		}
		
		if (listOwner.getBlockList().getBlockList().containsKey(targetId))
		{
			listOwner.sendMessage("Already in ignore list.");
			return;
		}
		
		listOwner.getBlockList().addToBlockList(targetId);
		
		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_BEEN_ADDED_TO_YOUR_IGNORE_LIST);
		sm.addString(charName);
		listOwner.sendPacket(sm);
		
		final L2PcInstance player = L2World.getInstance().getPlayer(targetId);
		
		if (player != null)
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_HAS_PLACED_YOU_ON_HIS_HER_IGNORE_LIST);
			sm.addString(listOwner.getName());
			player.sendPacket(sm);
		}
	}
	
	public static void removeFromBlockList(L2PcInstance listOwner, int targetId)
	{
		if (listOwner == null)
		{
			return;
		}
		
		SystemMessage sm;
		
		final String charName = CharNameTable.getInstance().getNameById(targetId);
		
		if (!listOwner.getBlockList().getBlockList().containsKey(targetId))
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.THAT_IS_AN_INCORRECT_TARGET);
			listOwner.sendPacket(sm);
			return;
		}
		
		listOwner.getBlockList().removeFromBlockList(targetId);
		
		sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_BEEN_REMOVED_FROM_YOUR_IGNORE_LIST);
		sm.addString(charName);
		listOwner.sendPacket(sm);
	}
	
	public static boolean isInBlockList(L2PcInstance listOwner, L2PcInstance target)
	{
		return listOwner.getBlockList().isInBlockList(target);
	}
	
	public boolean isBlockAll(L2PcInstance listOwner)
	{
		return listOwner.getBlockList().isBlockAll();
	}
	
	public static void setBlockAll(L2PcInstance listOwner, boolean newValue)
	{
		listOwner.getBlockList().setBlockAll(newValue);
	}
	
	public static void sendListToOwner(L2PcInstance listOwner)
	{
		listOwner.sendPacket(new BlockListPacket(listOwner));
	}
	
	/**
	 * @param ownerId object id of owner block list
	 * @param targetId object id of potential blocked player
	 * @return true if blocked
	 */
	public static boolean isInBlockList(int ownerId, int targetId)
	{
		final L2PcInstance player = L2World.getInstance().getPlayer(ownerId);
		if (player != null)
		{
			return BlockList.isBlocked(player, targetId);
		}
		if (!OFFLINE_LIST.containsKey(ownerId))
		{
			OFFLINE_LIST.put(ownerId, loadList(ownerId));
		}
		return OFFLINE_LIST.get(ownerId).containsKey(targetId);
	}
}
