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

import java.util.List;

import org.l2jmobius.gameserver.datatables.xml.SeedData;

/**
 * format(packet 0xFE) ch cd [ddddcdcd] c - id h - sub id c d - size [ d - level d - seed price d - seed level d - crop price c d - reward 1 id c d - reward 2 id ]
 * @author l3x
 */
public class ExShowManorDefaultInfo extends GameServerPacket
{
	private List<Integer> _crops = null;
	
	public ExShowManorDefaultInfo()
	{
		_crops = SeedData.getInstance().getAllCrops();
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x1E);
		writeC(0);
		writeD(_crops.size());
		for (int cropId : _crops)
		{
			writeD(cropId); // crop Id
			writeD(SeedData.getInstance().getSeedLevelByCrop(cropId)); // level
			writeD(SeedData.getInstance().getSeedBasicPriceByCrop(cropId)); // seed price
			writeD(SeedData.getInstance().getCropBasicPrice(cropId)); // crop price
			writeC(1); // reward 1 Type
			writeD(SeedData.getInstance().getRewardItem(cropId, 1)); // Reward 1 Type Item Id
			writeC(1); // reward 2 Type
			writeD(SeedData.getInstance().getRewardItem(cropId, 2)); // Reward 2 Type Item Id
		}
	}
}
