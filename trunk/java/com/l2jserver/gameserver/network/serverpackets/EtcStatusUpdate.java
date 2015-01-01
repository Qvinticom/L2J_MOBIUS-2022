/*
 * Copyright (C) 2004-2014 L2J Server
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

import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.zone.ZoneId;

/**
 * @author Luca Baldi
 */
public class EtcStatusUpdate extends L2GameServerPacket
{
	private final L2PcInstance _activeChar;
	private int _mask;
	
	public EtcStatusUpdate(L2PcInstance activeChar)
	{
		_activeChar = activeChar;
		_mask = _activeChar.getMessageRefusal() || _activeChar.isChatBanned() || _activeChar.isSilenceMode() ? 1 : 0;
		_mask |= _activeChar.isInsideZone(ZoneId.DANGER_AREA) ? 2 : 0;
		_mask |= _activeChar.hasCharmOfCourage() ? 4 : 0;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xF9); // several icons to a separate line (0 = disabled)
		writeC(_activeChar.getCharges()); // 1-7 increase force, lvl
		writeD(_activeChar.getWeightPenalty()); // 1-4 weight penalty, lvl (1=50%, 2=66.6%, 3=80%, 4=100%)
		writeC(_activeChar.getExpertiseWeaponPenalty()); // Weapon Grade Penalty [1-4]
		writeC(_activeChar.getExpertiseArmorPenalty()); // Armor Grade Penalty [1-4]
		writeC(0); // Death Penalty [1-15, 0 = disabled)], not used anymore in Ertheia
		writeC(_activeChar.getChargedSouls());
		writeC(_mask);
	}
}