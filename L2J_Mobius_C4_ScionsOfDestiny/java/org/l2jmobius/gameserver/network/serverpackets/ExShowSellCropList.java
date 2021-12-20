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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.data.xml.ManorSeedData;
import org.l2jmobius.gameserver.instancemanager.CastleManorManager.CropProcure;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * format(packet 0xFE) ch dd [ddddcdcdddc] c - id h - sub id d - manor id d - size [ d - Object id d - crop id d - seed level c d - reward 1 id c d - reward 2 id d - manor d - buy residual d - buy price d - reward ]
 * @author l3x
 */
public class ExShowSellCropList implements IClientOutgoingPacket
{
	private int _manorId = 1;
	private final Map<Integer, Item> _cropsItems;
	private final Map<Integer, CropProcure> _castleCrops;
	
	public ExShowSellCropList(Player player, int manorId, List<CropProcure> crops)
	{
		_manorId = manorId;
		_castleCrops = new HashMap<>();
		_cropsItems = new HashMap<>();
		final List<Integer> allCrops = ManorSeedData.getInstance().getAllCrops();
		for (int cropId : allCrops)
		{
			final Item item = player.getInventory().getItemByItemId(cropId);
			if (item != null)
			{
				_cropsItems.put(cropId, item);
			}
		}
		for (CropProcure crop : crops)
		{
			if (_cropsItems.containsKey(crop.getId()) && (crop.getAmount() > 0))
			{
				_castleCrops.put(crop.getId(), crop);
			}
		}
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_SHOW_SELL_CROP_LIST.writeId(packet);
		packet.writeD(_manorId); // manor id
		packet.writeD(_cropsItems.size()); // size
		for (Item item : _cropsItems.values())
		{
			packet.writeD(item.getObjectId()); // Object id
			packet.writeD(item.getItemId()); // crop id
			packet.writeD(ManorSeedData.getInstance().getSeedLevelByCrop(item.getItemId())); // seed level
			packet.writeC(1);
			packet.writeD(ManorSeedData.getInstance().getRewardItem(item.getItemId(), 1)); // reward 1 id
			packet.writeC(1);
			packet.writeD(ManorSeedData.getInstance().getRewardItem(item.getItemId(), 2)); // reward 2 id
			if (_castleCrops.containsKey(item.getItemId()))
			{
				final CropProcure crop = _castleCrops.get(item.getItemId());
				packet.writeD(_manorId); // manor
				packet.writeD(crop.getAmount()); // buy residual
				packet.writeD(crop.getPrice()); // buy price
				packet.writeC(crop.getReward()); // reward
			}
			else
			{
				packet.writeD(0xFFFFFFFF); // manor
				packet.writeD(0); // buy residual
				packet.writeD(0); // buy price
				packet.writeC(0); // reward
			}
			packet.writeD(item.getCount()); // my crops
		}
		return true;
	}
}
