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
package com.l2jmobius.gameserver.model;

import java.util.Set;

import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;

import javolution.util.FastSet;

/**
 * This class ...
 * @version $Revision: 1.2 $ $Date: 2004/06/27 08:12:59 $
 */

public class BlockList
{
	private final Set<String> blockSet;
	private boolean blockAll;
	
	public BlockList()
	{
		blockSet = new FastSet<>();
		blockAll = false;
	}
	
	private void addToBlockList(L2PcInstance character)
	{
		if (character != null)
		{
			blockSet.add(new String(character.getName()));
		}
	}
	
	private void removeFromBlockList(L2PcInstance character)
	{
		if (character != null)
		{
			blockSet.remove(character.getName());
		}
	}
	
	private boolean isInBlockList(L2PcInstance character)
	{
		return blockSet.contains(character.getName());
	}
	
	private boolean isBlockAll()
	{
		return blockAll;
	}
	
	public static boolean isBlocked(L2PcInstance listOwner, L2PcInstance character)
	{
		final BlockList blockList = listOwner.getBlockList();
		return blockList.isBlockAll() || blockList.isInBlockList(character);
	}
	
	private void setBlockAll(boolean state)
	{
		blockAll = state;
	}
	
	private Set<String> getBlockList()
	{
		return blockSet;
	}
	
	public static void addToBlockList(L2PcInstance listOwner, L2PcInstance character)
	{
		listOwner.getBlockList().addToBlockList(character);
		
		SystemMessage sm = new SystemMessage(SystemMessage.S1_HAS_ADDED_YOU_TO_IGNORE_LIST);
		sm.addString(listOwner.getName());
		character.sendPacket(sm);
		
		sm = new SystemMessage(SystemMessage.S1_WAS_ADDED_TO_YOUR_IGNORE_LIST);
		sm.addString(character.getName());
		listOwner.sendPacket(sm);
	}
	
	public static void removeFromBlockList(L2PcInstance listOwner, L2PcInstance character)
	{
		listOwner.getBlockList().removeFromBlockList(character);
		
		final SystemMessage sm = new SystemMessage(SystemMessage.S1_WAS_REMOVED_FROM_YOUR_IGNORE_LIST);
		sm.addString(character.getName());
		listOwner.sendPacket(sm);
	}
	
	public static boolean isInBlockList(L2PcInstance listOwner, L2PcInstance character)
	{
		return listOwner.getBlockList().isInBlockList(character);
	}
	
	public static boolean isBlockAll(L2PcInstance listOwner)
	{
		return listOwner.getBlockList().isBlockAll();
	}
	
	public static void setBlockAll(L2PcInstance listOwner, boolean newValue)
	{
		listOwner.getBlockList().setBlockAll(newValue);
	}
	
	public static void sendListToOwner(L2PcInstance listOwner)
	{
		for (final String playerName : listOwner.getBlockList().getBlockList())
		{
			listOwner.sendPacket(new SystemMessage(614).addString(playerName));
		}
	}
}
