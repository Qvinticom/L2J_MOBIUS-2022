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
package com.l2jmobius.gameserver.network.clientpackets.dailymission;

import java.util.Collection;

import com.l2jmobius.commons.network.PacketReader;
import com.l2jmobius.gameserver.data.xml.impl.OneDayRewardData;
import com.l2jmobius.gameserver.model.OneDayRewardDataHolder;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.client.L2GameClient;
import com.l2jmobius.gameserver.network.clientpackets.IClientIncomingPacket;
import com.l2jmobius.gameserver.network.serverpackets.dailymission.ExOneDayReceiveRewardList;

/**
 * @author Sdw
 */
public class RequestOneDayRewardReceive implements IClientIncomingPacket
{
	private int _reward;
	
	@Override
	public boolean read(L2GameClient client, PacketReader packet)
	{
		_reward = packet.readC();
		return true;
	}
	
	@Override
	public void run(L2GameClient client)
	{
		final L2PcInstance player = client.getActiveChar();
		if (player == null)
		{
			return;
		}
		
		final Collection<OneDayRewardDataHolder> reward = OneDayRewardData.getInstance().getOneDayRewardData(_reward);
		if (reward.isEmpty())
		{
			return;
		}
		
		reward.stream().filter(o -> o.isDisplayable(player)).forEach(r -> r.requestReward(player));
		player.sendPacket(new ExOneDayReceiveRewardList(player));
	}
}
