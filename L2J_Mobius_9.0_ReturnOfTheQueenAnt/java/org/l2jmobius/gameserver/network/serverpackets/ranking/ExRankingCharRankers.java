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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.enums.ClassId;
import org.l2jmobius.gameserver.enums.Race;
import org.l2jmobius.gameserver.enums.RankingCategory;
import org.l2jmobius.gameserver.enums.RankingScope;
import org.l2jmobius.gameserver.instancemanager.RankManager;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.OutgoingPackets;
import org.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;

/**
 * @author Berezkin Nikolay
 */
public class ExRankingCharRankers implements IClientOutgoingPacket
{
	private final PlayerInstance _player;
	private final int _group;
	private final int _scope;
	private final int _ordinal;
	private final Map<Integer, StatSet> _playerList;
	private final Map<Integer, StatSet> _snapshotList;
	
	public ExRankingCharRankers(PlayerInstance player, int group, int scope, int ordinal)
	{
		_player = player;
		_group = group;
		_scope = scope;
		_ordinal = ordinal;
		_playerList = RankManager.getInstance().getRankList();
		_snapshotList = RankManager.getInstance().getSnapshotList();
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_RANKING_CHAR_RANKERS.writeId(packet);
		
		packet.writeC(_group);
		packet.writeC(_scope);
		packet.writeD(_ordinal);
		packet.writeD(_player.getClassId().getId());
		
		if (!_playerList.isEmpty())
		{
			final RankingCategory category = RankingCategory.values()[_group];
			writeFilteredRankingData(packet, category, category.getScopeByGroup(_scope));
		}
		else
		{
			packet.writeD(0);
		}
		return true;
	}
	
	private void writeFilteredRankingData(PacketWriter packet, RankingCategory category, RankingScope scope)
	{
		switch (category)
		{
			case SERVER:
			{
				writeScopeData(packet, scope, new ArrayList<>(_playerList.entrySet()), new ArrayList<>(_snapshotList.entrySet()));
				break;
			}
			case RACE:
			{
				final Race race = Race.values()[_ordinal];
				writeScopeData(packet, scope, _playerList.entrySet().stream().filter(it -> it.getValue().getInt("race") == race.ordinal()).collect(Collectors.toList()), _snapshotList.entrySet().stream().filter(it -> it.getValue().getInt("race") == race.ordinal()).collect(Collectors.toList()));
				break;
			}
			case CLASS: // TODO: Check if this works.
			{
				final ClassId classId = ClassId.getClassId(_ordinal);
				writeScopeData(packet, scope, _playerList.entrySet().stream().filter(it -> it.getValue().getInt("classId") == classId.getId()).collect(Collectors.toList()), _snapshotList.entrySet().stream().filter(it -> it.getValue().getInt("classId") == classId.getId()).collect(Collectors.toList()));
				break;
			}
			case CLAN:
			{
				writeScopeData(packet, scope, _player.getClan() == null ? Collections.emptyList() : _playerList.entrySet().stream().filter(it -> it.getValue().getString("clanName").equals(_player.getClan().getName())).collect(Collectors.toList()), _player.getClan() == null ? Collections.emptyList() : _snapshotList.entrySet().stream().filter(it -> it.getValue().getString("clanName").equals(_player.getClan().getName())).collect(Collectors.toList()));
				break;
			}
			case FRIEND:
			{
				writeScopeData(packet, scope, _playerList.entrySet().stream().filter(it -> _player.getFriendList().contains(it.getValue().getInt("charId"))).collect(Collectors.toList()), _snapshotList.entrySet().stream().filter(it -> _player.getFriendList().contains(it.getValue().getInt("charId"))).collect(Collectors.toList()));
				break;
			}
		}
	}
	
	private void writeScopeData(PacketWriter packet, RankingScope scope, List<Entry<Integer, StatSet>> list, List<Entry<Integer, StatSet>> snapshot)
	{
		Entry<Integer, StatSet> playerData = list.stream().filter(it -> it.getValue().getInt("charId", 0) == _player.getObjectId()).findFirst().orElse(null);
		final int indexOf = list.indexOf(playerData);
		
		final List<Entry<Integer, StatSet>> limited;
		switch (scope)
		{
			case TOP_100:
			{
				limited = list.stream().limit(100).collect(Collectors.toList());
				break;
			}
			case ALL:
			{
				limited = list;
				break;
			}
			case TOP_150:
			{
				limited = list.stream().limit(150).collect(Collectors.toList());
				break;
			}
			case SELF:
			{
				limited = playerData == null ? Collections.emptyList() : list.subList(Math.max(0, indexOf - 10), Math.min(list.size(), indexOf + 10));
				break;
			}
			default:
			{
				limited = Collections.emptyList();
			}
		}
		
		packet.writeD(limited.size());
		
		int rank = 1;
		for (Entry<Integer, StatSet> data : limited.stream().sorted(Entry.comparingByKey()).collect(Collectors.toList()))
		{
			int curRank = rank++;
			final StatSet player = data.getValue();
			packet.writeString(player.getString("name"));
			packet.writeString(player.getString("clanName"));
			packet.writeD(player.getInt("level"));
			packet.writeD(player.getInt("classId"));
			packet.writeD(player.getInt("race"));
			packet.writeD(scope == RankingScope.SELF ? data.getKey() : curRank); // server rank
			if (!snapshot.isEmpty())
			{
				int snapshotRank = 1;
				for (Entry<Integer, StatSet> ssData : snapshot.stream().sorted(Entry.comparingByKey()).collect(Collectors.toList()))
				{
					final StatSet snapshotData = ssData.getValue();
					if (player.getInt("charId") == snapshotData.getInt("charId"))
					{
						packet.writeD(scope == RankingScope.SELF ? ssData.getKey() : snapshotRank++); // server rank snapshot
						packet.writeD(snapshotData.getInt("raceRank", 0)); // race rank snapshot
						packet.writeD(0); // TODO: nClassRank_Snapshot
					}
				}
			}
			else
			{
				packet.writeD(scope == RankingScope.SELF ? data.getKey() : curRank);
				packet.writeD(0);
				packet.writeD(0);
			}
		}
	}
}
