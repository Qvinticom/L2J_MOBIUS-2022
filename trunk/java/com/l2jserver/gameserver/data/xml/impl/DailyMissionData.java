/*
 * Copyright (C) 2004-2015 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jserver.gameserver.data.xml.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.base.ClassId;
import com.l2jserver.gameserver.model.holders.DailyMissionHolder;
import com.l2jserver.gameserver.network.serverpackets.dailymission.ExOneDayReceiveRewardList;
import com.l2jserver.util.data.xml.IXmlReader;

/**
 * The Class DailyMissionData.
 * @author Mobius
 */
public class DailyMissionData implements IXmlReader
{
	private final List<DailyMissionHolder> _dailyMissions = new ArrayList<>();
	private final List<DailyMissionHolder> _dailyLevelUpMissions = new ArrayList<>();
	
	/**
	 * Instantiates new daily mission data.
	 */
	protected DailyMissionData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_dailyMissions.clear();
		_dailyLevelUpMissions.clear();
		parseDatapackFile("dailyMissions.xml");
		LOGGER.info(getClass().getSimpleName() + ": Loaded " + _dailyMissions.size() + " daily missions.");
	}
	
	@Override
	public void parseDocument(Document doc)
	{
		int id;
		int clientId;
		String type;
		int level;
		List<Integer> classesList;
		Map<Integer, Integer> rewards;
		
		for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("mission".equalsIgnoreCase(d.getNodeName()))
					{
						final NamedNodeMap attrs = d.getAttributes();
						Node att;
						id = -1;
						clientId = 0;
						type = "";
						level = 1;
						classesList = new ArrayList<>();
						rewards = new HashMap<>();
						
						att = attrs.getNamedItem("id");
						if (att == null)
						{
							LOGGER.severe(getClass().getSimpleName() + ": Missing id for daily mission, skipping");
							continue;
						}
						id = Integer.parseInt(att.getNodeValue());
						
						att = attrs.getNamedItem("clientId");
						if (att == null)
						{
							LOGGER.severe(getClass().getSimpleName() + ": Missing clientId for daily mission id: " + id + ", skipping");
							continue;
						}
						clientId = Integer.parseInt(att.getNodeValue());
						
						att = attrs.getNamedItem("type");
						if (att == null)
						{
							LOGGER.severe(getClass().getSimpleName() + ": Missing type for daily mission id: " + id + ", skipping");
							continue;
						}
						type = att.getNodeValue();
						
						att = attrs.getNamedItem("level");
						if (att == null)
						{
							LOGGER.severe(getClass().getSimpleName() + ": Missing level for daily mission id: " + id + ", skipping");
							continue;
						}
						level = Integer.parseInt(att.getNodeValue());
						
						att = attrs.getNamedItem("classes");
						if (att == null)
						{
							LOGGER.severe(getClass().getSimpleName() + ": Missing classes for daily mission id: " + id + ", skipping");
							continue;
						}
						if (att.getNodeValue().equalsIgnoreCase("ALL"))
						{
							for (ClassId cid : ClassId.values())
							{
								classesList.add(cid.getId());
							}
						}
						else
						{
							final String[] s = att.getNodeValue().split(",");
							for (String element : s)
							{
								classesList.add(Integer.parseInt(element));
							}
						}
						
						for (Node c = d.getFirstChild(); c != null; c = c.getNextSibling())
						{
							if ("reward".equalsIgnoreCase(c.getNodeName()))
							{
								final int itemId = Integer.parseInt(c.getAttributes().getNamedItem("item").getNodeValue());
								final int itemCount = Integer.parseInt(c.getAttributes().getNamedItem("count").getNodeValue());
								rewards.put(itemId, itemCount);
							}
						}
						
						if (type.equalsIgnoreCase("LEVEL"))
						{
							_dailyLevelUpMissions.add(new DailyMissionHolder(id, clientId, type, level, classesList, rewards));
						}
						_dailyMissions.add(new DailyMissionHolder(id, clientId, type, level, classesList, rewards));
					}
				}
			}
		}
	}
	
	/**
	 * @param id int
	 * @param player L2PcInstance
	 * @return int
	 */
	public int RewardStatus(int id, L2PcInstance player)
	{
		if (player.getLevel() < _dailyMissions.get(id - 1).getLevel())
		{
			return 2; // Not Available
		}
		if (player.getVariables().getString("DailyMission" + id, null) != null)
		{
			return 3; // Complete
		}
		return 1; // Available
	}
	
	/**
	 * @param rewardId1 int
	 * @param player L2PcInstance
	 */
	public void rewardPlayer(int rewardId1, L2PcInstance player)
	{
		for (DailyMissionHolder mission : _dailyMissions)
		{
			if ((mission.getClientId() == rewardId1) && (RewardStatus(mission.getId(), player) == 1))
			{
				for (int classId : mission.getAvailableClasses())
				{
					if (player.getClassId().getId() == classId)
					{
						for (int itemId : mission.getRewards().keySet())
						{
							player.addItem("DailyMission", itemId, mission.getRewards().get(itemId), player, true);
						}
						for (DailyMissionHolder m : _dailyMissions)
						{
							if (mission.getClientId() == m.getClientId())
							{
								player.getVariables().set("DailyMission" + m.getId(), System.currentTimeMillis());
							}
						}
						player.sendPacket(new ExOneDayReceiveRewardList(player));
						break;
					}
				}
			}
		}
	}
	
	/**
	 * Gets the daily missions.
	 * @param classId int
	 * @return the daily missions
	 */
	public List<DailyMissionHolder> getDailyMissions(int classId)
	{
		final List<DailyMissionHolder> missions = new ArrayList<>();
		for (DailyMissionHolder mission : _dailyMissions)
		{
			if (mission.getAvailableClasses().contains(classId))
			{
				missions.add(mission);
			}
		}
		return missions;
	}
	
	/**
	 * Gets the daily level up missions.
	 * @param classId int
	 * @return the daily level up missions
	 */
	public List<DailyMissionHolder> getDailyLevelUpMissions(int classId)
	{
		final List<DailyMissionHolder> missions = new ArrayList<>();
		for (DailyMissionHolder mission : _dailyLevelUpMissions)
		{
			if (mission.getAvailableClasses().contains(classId))
			{
				missions.add(mission);
			}
		}
		return missions;
	}
	
	/**
	 * Gets the single instance of DailyMissionData.
	 * @return single instance of DailyMissionData
	 */
	public static DailyMissionData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	/**
	 * The Class SingletonHolder.
	 */
	private static class SingletonHolder
	{
		protected static final DailyMissionData _instance = new DailyMissionData();
	}
}
