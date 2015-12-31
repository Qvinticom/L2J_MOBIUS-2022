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
package com.l2jmobius.gameserver.network.serverpackets;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import com.l2jmobius.gameserver.instancemanager.InstanceManager;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.instancezone.InstanceWorld;

/**
 * @author UnAfraid
 */
public class ExInzoneWaiting extends L2GameServerPacket
{
	private final int _currentTemplateId;
	private final Map<Integer, Long> _instanceTimes;
	
	public ExInzoneWaiting(L2PcInstance activeChar)
	{
		final InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(activeChar);
		_currentTemplateId = (world != null) && (world.getTemplateId() >= 0) ? world.getTemplateId() : -1;
		_instanceTimes = InstanceManager.getInstance().getAllInstanceTimes(activeChar.getObjectId());
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x11E);
		writeD(_currentTemplateId);
		writeD(_instanceTimes.size());
		for (Entry<Integer, Long> entry : _instanceTimes.entrySet())
		{
			final long instanceTime = TimeUnit.MILLISECONDS.toSeconds(entry.getValue() - System.currentTimeMillis());
			writeD(entry.getKey());
			writeD((int) instanceTime);
		}
	}
}
