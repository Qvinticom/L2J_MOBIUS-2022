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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.l2jserver.gameserver.instancemanager.CHSiegeManager;
import com.l2jserver.gameserver.instancemanager.CastleManager;
import com.l2jserver.gameserver.instancemanager.FortManager;
import com.l2jserver.gameserver.model.L2Clan;
import com.l2jserver.gameserver.model.L2SiegeClan;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.entity.Castle;
import com.l2jserver.gameserver.model.entity.Fort;
import com.l2jserver.gameserver.model.entity.clanhall.SiegableHall;

/**
 * @author UnAfraid, Nos
 */
public class Die extends L2GameServerPacket
{
	private final int _objectId;
	private boolean _toVillage;
	private boolean _toClanHall;
	private boolean _toCastle;
	private boolean _toOutpost;
	private final boolean _isSweepable;
	private boolean _useFeather;
	private boolean _toFortress;
	private List<Integer> _items = null;
	
	public Die(L2Character activeChar)
	{
		_objectId = activeChar.getObjectId();
		if (activeChar.isPlayer())
		{
			final L2Clan clan = activeChar.getActingPlayer().getClan();
			boolean isInCastleDefense = false;
			boolean isInFortDefense = false;
			
			L2SiegeClan siegeClan = null;
			final Castle castle = CastleManager.getInstance().getCastle(activeChar);
			final Fort fort = FortManager.getInstance().getFort(activeChar);
			final SiegableHall hall = CHSiegeManager.getInstance().getNearbyClanHall(activeChar);
			if ((castle != null) && castle.getSiege().isInProgress())
			{
				siegeClan = castle.getSiege().getAttackerClan(clan);
				isInCastleDefense = (siegeClan == null) && castle.getSiege().checkIsDefender(clan);
			}
			else if ((fort != null) && fort.getSiege().isInProgress())
			{
				siegeClan = fort.getSiege().getAttackerClan(clan);
				isInFortDefense = (siegeClan == null) && fort.getSiege().checkIsDefender(clan);
			}
			
			_toVillage = activeChar.canRevive() && !activeChar.isPendingRevive();
			_toClanHall = (clan != null) && (clan.getHideoutId() > 0);
			_toCastle = ((clan != null) && (clan.getCastleId() > 0)) || isInCastleDefense;
			_toOutpost = ((siegeClan != null) && !isInCastleDefense && !isInFortDefense && !siegeClan.getFlag().isEmpty()) || ((hall != null) && hall.getSiege().checkIsAttacker(clan));
			_useFeather = activeChar.getAccessLevel().allowFixedRes();
			_toFortress = ((clan != null) && (clan.getFortId() > 0)) || isInFortDefense;
		}
		
		_isSweepable = activeChar.isAttackable() && activeChar.isSweepActive();
	}
	
	public void addItem(int itemId)
	{
		if (_items == null)
		{
			_items = new ArrayList<>(8);
		}
		
		if (_items.size() < 8)
		{
			_items.add(itemId);
		}
		else
		{
			throw new IndexOutOfBoundsException("Die packet doesn't support more then 8 items!");
		}
	}
	
	public List<Integer> getItems()
	{
		return _items != null ? _items : Collections.emptyList();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x00);
		writeD(_objectId);
		writeD(_toVillage ? 0x01 : 0x00);
		writeD(_toClanHall ? 0x01 : 0x00);
		writeD(_toCastle ? 0x01 : 0x00);
		writeD(_toOutpost ? 0x01 : 0x00);
		writeD(_isSweepable ? 0x01 : 0x00);
		writeD(_useFeather ? 0x01 : 0x00);
		writeD(_toFortress ? 0x01 : 0x00);
		writeD(0x00);
		writeD(0x00);
		writeC(0x00);
		writeD(0x00);
		writeD(0x00);
	}
}
