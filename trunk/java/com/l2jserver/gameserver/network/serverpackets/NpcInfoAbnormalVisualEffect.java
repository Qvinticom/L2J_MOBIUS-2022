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

import java.util.Set;

import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.skills.AbnormalVisualEffect;

/**
 * @author Sdw
 */
public class NpcInfoAbnormalVisualEffect extends L2GameServerPacket
{
	private final L2Npc _npc;
	
	public NpcInfoAbnormalVisualEffect(L2Npc npc)
	{
		_npc = npc;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x4B);
		
		writeD(_npc.getObjectId());
		writeD(_npc.getTransformation() == null ? 0 : _npc.getTransformation().getId());
		
		final Set<AbnormalVisualEffect> abnormalVisualEffects = _npc.getCurrentAbnormalVisualEffects();
		writeD(abnormalVisualEffects.size());
		for (AbnormalVisualEffect abnormalVisualEffect : abnormalVisualEffects)
		{
			writeH(abnormalVisualEffect.getClientId());
		}
	}
}
