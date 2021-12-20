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

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.data.xml.ManorSeedData;
import org.l2jmobius.gameserver.instancemanager.CastleManager;
import org.l2jmobius.gameserver.instancemanager.CastleManorManager;
import org.l2jmobius.gameserver.instancemanager.CastleManorManager.SeedProduction;
import org.l2jmobius.gameserver.model.siege.Castle;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * format(packet 0xFE) ch dd [ddcdcdddddddd] c - id h - sub id d - manor id d - size [ d - seed id d - level c d - reward 1 id c d - reward 2 id d - next sale limit d - price for castle to produce 1 d - min seed price d - max seed price d - today sales d - today price d - next sales d - next price ]
 * @author l3x
 */
public class ExShowSeedSetting implements IClientOutgoingPacket
{
	private final int _manorId;
	private final int _count;
	private final int[] _seedData; // data to send, size:_count*12
	
	public ExShowSeedSetting(int manorId)
	{
		_manorId = manorId;
		final Castle c = CastleManager.getInstance().getCastleById(_manorId);
		final List<Integer> seeds = ManorSeedData.getInstance().getSeedsForCastle(_manorId);
		_count = seeds.size();
		_seedData = new int[_count * 12];
		int i = 0;
		for (int s : seeds)
		{
			_seedData[(i * 12) + 0] = s;
			_seedData[(i * 12) + 1] = ManorSeedData.getInstance().getSeedLevel(s);
			_seedData[(i * 12) + 2] = ManorSeedData.getInstance().getRewardItemBySeed(s, 1);
			_seedData[(i * 12) + 3] = ManorSeedData.getInstance().getRewardItemBySeed(s, 2);
			_seedData[(i * 12) + 4] = ManorSeedData.getInstance().getSeedSaleLimit(s);
			_seedData[(i * 12) + 5] = ManorSeedData.getInstance().getSeedBuyPrice(s);
			_seedData[(i * 12) + 6] = (ManorSeedData.getInstance().getSeedBasicPrice(s) * 60) / 100;
			_seedData[(i * 12) + 7] = ManorSeedData.getInstance().getSeedBasicPrice(s) * 10;
			SeedProduction seedPr = c.getSeed(s, CastleManorManager.PERIOD_CURRENT);
			if (seedPr != null)
			{
				_seedData[(i * 12) + 8] = seedPr.getStartProduce();
				_seedData[(i * 12) + 9] = seedPr.getPrice();
			}
			else
			{
				_seedData[(i * 12) + 8] = 0;
				_seedData[(i * 12) + 9] = 0;
			}
			seedPr = c.getSeed(s, CastleManorManager.PERIOD_NEXT);
			if (seedPr != null)
			{
				_seedData[(i * 12) + 10] = seedPr.getStartProduce();
				_seedData[(i * 12) + 11] = seedPr.getPrice();
			}
			else
			{
				_seedData[(i * 12) + 10] = 0;
				_seedData[(i * 12) + 11] = 0;
			}
			i++;
		}
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_SHOW_SEED_SETTING.writeId(packet);
		packet.writeD(_manorId); // manor id
		packet.writeD(_count); // size
		for (int i = 0; i < _count; i++)
		{
			packet.writeD(_seedData[(i * 12) + 0]); // seed id
			packet.writeD(_seedData[(i * 12) + 1]); // level
			packet.writeC(1);
			packet.writeD(_seedData[(i * 12) + 2]); // reward 1 id
			packet.writeC(1);
			packet.writeD(_seedData[(i * 12) + 3]); // reward 2 id
			packet.writeD(_seedData[(i * 12) + 4]); // next sale limit
			packet.writeD(_seedData[(i * 12) + 5]); // price for castle to produce 1
			packet.writeD(_seedData[(i * 12) + 6]); // min seed price
			packet.writeD(_seedData[(i * 12) + 7]); // max seed price
			packet.writeD(_seedData[(i * 12) + 8]); // today sales
			packet.writeD(_seedData[(i * 12) + 9]); // today price
			packet.writeD(_seedData[(i * 12) + 10]); // next sales
			packet.writeD(_seedData[(i * 12) + 11]); // next price
		}
		return true;
	}
}
