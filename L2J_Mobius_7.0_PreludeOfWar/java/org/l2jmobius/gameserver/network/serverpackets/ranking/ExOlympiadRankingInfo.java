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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.l2jmobius.Config;
import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.instancemanager.RankManager;
import org.l2jmobius.gameserver.model.StatsSet;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.OutgoingPackets;
import org.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;

/**
 * @author NviX
 */
public class ExOlympiadRankingInfo implements IClientOutgoingPacket
{
	private final PlayerInstance _player;
	
	private final int _tabId;
	private final int _rankingType;
	private final int _unk;
	private final int _classId;
	private final int _serverId;
	private final Map<Integer, StatsSet> _playerList;
	private final Map<Integer, StatsSet> _snapshotList;
	
	public ExOlympiadRankingInfo(PlayerInstance player, int tabId, int rankingType, int unk, int classId, int serverId)
	{
		_player = player;
		_tabId = tabId;
		_rankingType = rankingType;
		_unk = unk;
		_classId = classId;
		_serverId = serverId;
		_playerList = RankManager.getInstance().getOlyRankList();
		_snapshotList = RankManager.getInstance().getSnapshotOlyList();
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_OLYMPIAD_RANKING_INFO.writeId(packet);
		
		packet.writeC(_tabId); // Tab id
		packet.writeC(_rankingType); // ranking type
		packet.writeC(_unk); // unk, shows 1 all time
		packet.writeD(_classId); // class id (default 148) or caller class id for personal rank
		packet.writeD(_serverId); // 0 - all servers, server id - for caller server
		packet.writeD(933); // unk, 933 all time
		
		if (_playerList.size() > 0)
		{
			switch (_tabId)
			{
				case 0:
				{
					if (_rankingType == 0)
					{
						packet.writeD(_playerList.size() > 100 ? 100 : _playerList.size());
						
						for (Integer id : _playerList.keySet())
						{
							final StatsSet player = _playerList.get(id);
							
							packet.writeString(player.getString("name")); // name
							packet.writeString(player.getString("clanName")); // clan name
							packet.writeD(id); // rank
							
							if (_snapshotList.size() > 0)
							{
								for (Integer id2 : _snapshotList.keySet())
								{
									final StatsSet snapshot = _snapshotList.get(id2);
									
									if (player.getInt("charId") == snapshot.getInt("charId"))
									{
										packet.writeD(id2); // previous rank
									}
								}
							}
							else
							{
								packet.writeD(id);
							}
							
							packet.writeD(Config.SERVER_ID);// server id
							packet.writeD(player.getInt("level"));// level
							packet.writeD(player.getInt("classId"));// class id
							packet.writeD(player.getInt("clanLevel"));// clan level
							packet.writeD(player.getInt("competitions_won"));// win count
							packet.writeD(player.getInt("competitions_lost"));// lose count
							packet.writeD(player.getInt("olympiad_points"));// points
							packet.writeD(player.getInt("count"));// hero counts
							packet.writeD(player.getInt("legend_count"));// legend counts
						}
					}
					else
					{
						boolean found = false;
						for (Integer id : _playerList.keySet())
						{
							final StatsSet player = _playerList.get(id);
							
							if (player.getInt("charId") == _player.getObjectId())
							{
								found = true;
								
								int first = id > 10 ? (id - 9) : 1;
								int last = _playerList.size() >= (id + 10) ? id + 10 : id + (_playerList.size() - id);
								if (first == 1)
								{
									packet.writeD(last - (first - 1));
								}
								else
								{
									packet.writeD(last - first);
								}
								
								for (int id2 = first; id2 <= last; id2++)
								{
									final StatsSet plr = _playerList.get(id2);
									
									packet.writeString(plr.getString("name"));
									packet.writeString(plr.getString("clanName"));
									packet.writeD(id2);
									if (_snapshotList.size() > 0)
									{
										for (Integer id3 : _snapshotList.keySet())
										{
											final StatsSet snapshot = _snapshotList.get(id3);
											if (player.getInt("charId") == snapshot.getInt("charId"))
											{
												packet.writeD(id3); // class rank snapshot
											}
										}
									}
									else
									{
										packet.writeD(id2);
									}
									
									packet.writeD(Config.SERVER_ID);
									packet.writeD(plr.getInt("level"));
									packet.writeD(plr.getInt("classId"));
									packet.writeD(plr.getInt("clanLevel"));// clan level
									packet.writeD(plr.getInt("competitions_won"));// win count
									packet.writeD(plr.getInt("competitions_lost"));// lose count
									packet.writeD(plr.getInt("olympiad_points"));// points
									packet.writeD(plr.getInt("count"));// hero counts
									packet.writeD(plr.getInt("legend_count"));// legend counts
								}
							}
						}
						if (!found)
						{
							packet.writeD(0);
						}
					}
					break;
				}
				case 1:
				{
					if (_rankingType == 0)
					{
						int count = 0;
						
						for (int i = 1; i <= _playerList.size(); i++)
						{
							final StatsSet player = _playerList.get(i);
							if (_classId == player.getInt("classId"))
							{
								count++;
							}
						}
						packet.writeD(count > 50 ? 50 : count);
						
						int i = 1;
						for (Integer id : _playerList.keySet())
						{
							final StatsSet player = _playerList.get(id);
							
							if (_classId == player.getInt("classId"))
							{
								packet.writeString(player.getString("name"));
								packet.writeString(player.getString("clanName"));
								packet.writeD(i); // class rank
								if (_snapshotList.size() > 0)
								{
									final Map<Integer, StatsSet> snapshotRaceList = new ConcurrentHashMap<>();
									int j = 1;
									for (Integer id2 : _snapshotList.keySet())
									{
										final StatsSet snapshot = _snapshotList.get(id2);
										
										if (_classId == snapshot.getInt("classId"))
										{
											snapshotRaceList.put(j, _snapshotList.get(id2));
											j++;
										}
									}
									for (Integer id2 : snapshotRaceList.keySet())
									{
										final StatsSet snapshot = snapshotRaceList.get(id2);
										
										if (player.getInt("charId") == snapshot.getInt("charId"))
										{
											packet.writeD(id2); // class rank snapshot
										}
									}
								}
								else
								{
									packet.writeD(i);
								}
								
								packet.writeD(Config.SERVER_ID);
								packet.writeD(player.getInt("level"));
								packet.writeD(player.getInt("classId"));
								packet.writeD(player.getInt("clanLevel"));// clan level
								packet.writeD(player.getInt("competitions_won"));// win count
								packet.writeD(player.getInt("competitions_lost"));// lose count
								packet.writeD(player.getInt("olympiad_points"));// points
								packet.writeD(player.getInt("count"));// hero counts
								packet.writeD(player.getInt("legend_count"));// legend counts
								i++;
							}
						}
					}
					else
					{
						boolean found = false;
						final Map<Integer, StatsSet> classList = new ConcurrentHashMap<>();
						int i = 1;
						for (Integer id : _playerList.keySet())
						{
							final StatsSet set = _playerList.get(id);
							
							if (_player.getBaseClass() == set.getInt("classId"))
							{
								classList.put(i, _playerList.get(id));
								i++;
							}
						}
						
						for (Integer id : classList.keySet())
						{
							final StatsSet player = classList.get(id);
							
							if (player.getInt("charId") == _player.getObjectId())
							{
								found = true;
								int first = id > 10 ? (id - 9) : 1;
								int last = classList.size() >= (id + 10) ? id + 10 : id + (classList.size() - id);
								if (first == 1)
								{
									packet.writeD(last - (first - 1));
								}
								else
								{
									packet.writeD(last - first);
								}
								for (int id2 = first; id2 <= last; id2++)
								{
									final StatsSet plr = classList.get(id2);
									
									packet.writeString(plr.getString("name"));
									packet.writeString(plr.getString("clanName"));
									packet.writeD(id2); // class rank
									packet.writeD(id2);
									packet.writeD(Config.SERVER_ID);
									packet.writeD(player.getInt("level"));
									packet.writeD(player.getInt("classId"));
									packet.writeD(player.getInt("clanLevel"));// clan level
									packet.writeD(player.getInt("competitions_won"));// win count
									packet.writeD(player.getInt("competitions_lost"));// lose count
									packet.writeD(player.getInt("olympiad_points"));// points
									packet.writeD(player.getInt("count"));// hero counts
									packet.writeD(player.getInt("legend_count"));// legend counts
								}
							}
						}
						if (!found)
						{
							packet.writeD(0);
						}
					}
					break;
				}
			}
		}
		return true;
	}
}
