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
package org.l2jmobius.gameserver.network.serverpackets;

import java.util.Collection;

import org.l2jmobius.gameserver.model.Timestamp;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;

public class SkillCoolTime extends GameServerPacket
{
	public Collection<Timestamp> _reuseTimestamps;
	
	public SkillCoolTime(PlayerInstance player)
	{
		_reuseTimestamps = player.getReuseTimeStamps();
	}
	
	@Override
	protected final void writeImpl()
	{
		final PlayerInstance player = getClient().getPlayer();
		if (player == null)
		{
			return;
		}
		writeC(0xc1);
		writeD(_reuseTimestamps.size());
		for (Timestamp reuseTimestamp : _reuseTimestamps)
		{
			writeD(reuseTimestamp.getSkillId());
			writeD(reuseTimestamp.getSkillLevel());
			writeD((int) reuseTimestamp.getReuse() / 1000);
			writeD((int) reuseTimestamp.getRemaining() / 1000);
		}
	}
}