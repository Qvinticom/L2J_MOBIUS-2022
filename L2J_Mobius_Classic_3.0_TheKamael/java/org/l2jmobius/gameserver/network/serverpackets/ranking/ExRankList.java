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
package org.l2jmobius.gameserver.network.serverpackets.ranking;

import java.util.ArrayList;
import java.util.List;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.network.OutgoingPackets;
import org.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;

/**
 * @author JoeAlisson
 */
public class ExRankList implements IClientOutgoingPacket
{
	private final int _race;
	private final int _group;
	private final int _scope;
	
	public ExRankList(int group, int scope, int race)
	{
		_group = group;
		_scope = scope;
		_race = race;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_RANKING_CHAR_RANKERS.writeId(packet);
		
		packet.writeC(_group);
		packet.writeC(_scope);
		packet.writeD(_race);
		
		List<Ranker> rankers = new ArrayList<>();
		for (int i = 0; i < 5; i++)
		{
			addRanker(rankers);
		}
		
		packet.writeD(rankers.size());
		
		for (Ranker ranker : rankers)
		{
			packet.writeString(ranker.name);
			packet.writeString(ranker.pledgeName);
			packet.writeD(ranker.level);
			packet.writeD(ranker.rClass);
			packet.writeD(ranker.race);
			packet.writeD(ranker.rank);
			packet.writeD(ranker.serverRankSnapshot);
			packet.writeD(ranker.raceRankSnapshot);
		}
		
		return true;
	}
	
	private static void addRanker(List<Ranker> rankers)
	{
		final Ranker ranker = new Ranker();
		ranker.name = "Ranker" + rankers.size();
		ranker.pledgeName = "ClanRanker" + rankers.size();
		ranker.level = 80 - rankers.size();
		ranker.race = rankers.size();
		ranker.rClass = 20 + rankers.size();
		ranker.rank = 1 + rankers.size();
		ranker.serverRankSnapshot = ranker.rank + ((rankers.size() % 2) == 0 ? 2 : -1);
		ranker.raceRankSnapshot = rankers.size();
		rankers.add(ranker);
	}
	
	private static class Ranker
	{
		String name;
		String pledgeName;
		int level;
		int rClass;
		int rank;
		int race;
		int serverRankSnapshot;
		int raceRankSnapshot;
	}
}
