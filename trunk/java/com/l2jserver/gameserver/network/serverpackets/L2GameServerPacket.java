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
package com.l2jserver.gameserver.network.serverpackets;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jserver.commons.mmocore.SendablePacket;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.interfaces.IPositionable;
import com.l2jserver.gameserver.model.interfaces.IUpdateTypeComponent;
import com.l2jserver.gameserver.model.itemcontainer.Inventory;
import com.l2jserver.gameserver.network.L2GameClient;

/**
 * @author KenM
 */
public abstract class L2GameServerPacket extends SendablePacket<L2GameClient>
{
	protected static final Logger _log = Logger.getLogger(L2GameServerPacket.class.getName());
	
	private boolean _invisible = false;
	
	private static final int[] PAPERDOLL_ORDER = new int[]
	{
		Inventory.PAPERDOLL_UNDER,
		Inventory.PAPERDOLL_REAR,
		Inventory.PAPERDOLL_LEAR,
		Inventory.PAPERDOLL_NECK,
		Inventory.PAPERDOLL_RFINGER,
		Inventory.PAPERDOLL_LFINGER,
		Inventory.PAPERDOLL_HEAD,
		Inventory.PAPERDOLL_RHAND,
		Inventory.PAPERDOLL_LHAND,
		Inventory.PAPERDOLL_GLOVES,
		Inventory.PAPERDOLL_CHEST,
		Inventory.PAPERDOLL_LEGS,
		Inventory.PAPERDOLL_FEET,
		Inventory.PAPERDOLL_CLOAK,
		Inventory.PAPERDOLL_RHAND,
		Inventory.PAPERDOLL_HAIR,
		Inventory.PAPERDOLL_HAIR2,
		Inventory.PAPERDOLL_RBRACELET,
		Inventory.PAPERDOLL_LBRACELET,
		Inventory.PAPERDOLL_DECO1,
		Inventory.PAPERDOLL_DECO2,
		Inventory.PAPERDOLL_DECO3,
		Inventory.PAPERDOLL_DECO4,
		Inventory.PAPERDOLL_DECO5,
		Inventory.PAPERDOLL_DECO6,
		Inventory.PAPERDOLL_BELT,
		Inventory.PAPERDOLL_BROOCH,
		Inventory.PAPERDOLL_BROOCH_JEWEL1,
		Inventory.PAPERDOLL_BROOCH_JEWEL2,
		Inventory.PAPERDOLL_BROOCH_JEWEL3,
		Inventory.PAPERDOLL_BROOCH_JEWEL4,
		Inventory.PAPERDOLL_BROOCH_JEWEL5,
		Inventory.PAPERDOLL_BROOCH_JEWEL6
	
	};
	
	private static final int[] PAPERDOLL_ORDER_AUGMENT = new int[]
	{
		Inventory.PAPERDOLL_RHAND,
		Inventory.PAPERDOLL_LHAND,
		Inventory.PAPERDOLL_RHAND
	};
	
	private static final int[] PAPERDOLL_ORDER_VISUAL_ID = new int[]
	{
		Inventory.PAPERDOLL_RHAND,
		Inventory.PAPERDOLL_LHAND,
		Inventory.PAPERDOLL_RHAND,
		Inventory.PAPERDOLL_GLOVES,
		Inventory.PAPERDOLL_CHEST,
		Inventory.PAPERDOLL_LEGS,
		Inventory.PAPERDOLL_FEET,
		Inventory.PAPERDOLL_HAIR,
		Inventory.PAPERDOLL_HAIR2
	};
	
	/**
	 * @return True if packet originated from invisible character.
	 */
	public boolean isInvisible()
	{
		return _invisible;
	}
	
	/**
	 * Set "invisible" boolean flag in the packet.<br>
	 * Packets from invisible characters will not be broadcasted to players.
	 * @param b
	 */
	public void setInvisible(boolean b)
	{
		_invisible = b;
	}
	
	/**
	 * Writes 3 D (int32) with current location x, y, z
	 * @param loc
	 */
	protected void writeLoc(IPositionable loc)
	{
		writeD(loc.getX());
		writeD(loc.getY());
		writeD(loc.getZ());
	}
	
	/**
	 * Write String
	 * @param str
	 */
	protected void writeString(String str)
	{
		if ((str == null) || str.isEmpty())
		{
			writeH(0x00);
			return;
		}
		final char[] chars = str.toCharArray();
		writeH(chars.length);
		for (char ch : chars)
		{
			_buf.putChar(ch);
		}
	}
	
	protected int[] getPaperdollOrder()
	{
		return PAPERDOLL_ORDER;
	}
	
	protected int[] getPaperdollOrderAugument()
	{
		return PAPERDOLL_ORDER_AUGMENT;
	}
	
	protected int[] getPaperdollOrderVisualId()
	{
		return PAPERDOLL_ORDER_VISUAL_ID;
	}
	
	@Override
	protected void write()
	{
		try
		{
			writeImpl();
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Client: " + getClient().toString() + " - Failed writing: " + getClass().getSimpleName() + " ; " + e.getMessage(), e);
		}
	}
	
	public void runImpl()
	{
		
	}
	
	protected abstract void writeImpl();
	
	/**
	 * @param masks
	 * @param type
	 * @return {@code true} if the mask contains the current update component type
	 */
	protected static boolean containsMask(int masks, IUpdateTypeComponent type)
	{
		return (masks & type.getMask()) == type.getMask();
	}
	
	/**
	 * Sends this packet to the target player, useful for lambda operations like <br>
	 * {@code L2World.getInstance().getPlayers().forEach(packet::sendTo)}
	 * @param player
	 */
	public void sendTo(L2PcInstance player)
	{
		player.sendPacket(this);
	}
}
