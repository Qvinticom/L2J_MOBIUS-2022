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

import java.util.ArrayList;
import java.util.List;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.data.xml.ManorSeedData;
import org.l2jmobius.gameserver.instancemanager.CastleManorManager.CropProcure;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * Format: ch cddd[ddddcdcdcd] c - id (0xFE) h - sub id (0x1D) c d - manor id d d - size [ d - crop id d - residual buy d - start buy d - buy price c - reward type d - seed level c - reward 1 items d - reward 1 item id c - reward 2 items d - reward 2 item id ]
 * @author l3x
 */
public class ExShowCropInfo implements IClientOutgoingPacket
{
	private List<CropProcure> _crops;
	private final int _manorId;
	
	public ExShowCropInfo(int manorId, List<CropProcure> crops)
	{
		_manorId = manorId;
		_crops = crops;
		if (_crops == null)
		{
			_crops = new ArrayList<>();
		}
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_SHOW_CROP_INFO.writeId(packet);
		packet.writeC(0);
		packet.writeD(_manorId); // Manor ID
		packet.writeD(0);
		packet.writeD(_crops.size());
		for (CropProcure crop : _crops)
		{
			packet.writeD(crop.getId()); // Crop id
			packet.writeD(crop.getAmount()); // Buy residual
			packet.writeD(crop.getStartAmount()); // Buy
			packet.writeD(crop.getPrice()); // Buy price
			packet.writeC(crop.getReward()); // Reward
			packet.writeD(ManorSeedData.getInstance().getSeedLevelByCrop(crop.getId())); // Seed Level
			packet.writeC(1); // reward 1 Type
			packet.writeD(ManorSeedData.getInstance().getRewardItem(crop.getId(), 1)); // Reward 1 Type Item Id
			packet.writeC(1); // reward 2 Type
			packet.writeD(ManorSeedData.getInstance().getRewardItem(crop.getId(), 2)); // Reward 2 Type Item Id
		}
		return true;
	}
}
