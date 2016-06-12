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
package com.l2jmobius.gameserver.data.xml.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.w3c.dom.Document;

import com.l2jmobius.commons.util.IGameXmlReader;
import com.l2jmobius.gameserver.model.OneDayRewardDataHolder;
import com.l2jmobius.gameserver.model.StatsSet;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.base.ClassId;
import com.l2jmobius.gameserver.model.holders.ItemHolder;

/**
 * @author Sdw
 */
public class OneDayRewardData implements IGameXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(OneDayRewardData.class.getName());
	private final Map<Integer, List<OneDayRewardDataHolder>> _oneDayReward = new LinkedHashMap<>();
	
	protected OneDayRewardData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_oneDayReward.clear();
		parseDatapackFile("data/OneDayReward.xml");
		LOGGER.info(getClass().getSimpleName() + ": Loaded " + _oneDayReward.size() + " one day rewards.");
	}
	
	@Override
	public void parseDocument(Document doc, File f)
	{
		forEach(doc, "list", listNode -> forEach(listNode, "reward", rewardNode ->
		{
			final StatsSet set = new StatsSet(parseAttributes(rewardNode));
			
			final List<ItemHolder> items = new ArrayList<>(1);
			forEach(rewardNode, "items", itemsNode -> forEach(itemsNode, "item", itemNode ->
			{
				final int itemId = parseInteger(itemNode.getAttributes(), "id");
				final int itemCount = parseInteger(itemNode.getAttributes(), "count");
				items.add(new ItemHolder(itemId, itemCount));
			}));
			
			set.set("items", items);
			
			final List<ClassId> classRestriction = new ArrayList<>(1);
			forEach(rewardNode, "classId", classRestrictionNode ->
			{
				classRestriction.add(ClassId.getClassId(Integer.parseInt(classRestrictionNode.getTextContent())));
			});
			set.set("classRestriction", classRestriction);
			
			// Initial values in case handler doesn't exists
			set.set("handler", "");
			set.set("params", StatsSet.EMPTY_STATSET);
			
			// Parse handler and parameters
			forEach(rewardNode, "handler", handlerNode ->
			{
				set.set("handler", parseString(handlerNode.getAttributes(), "name"));
				
				final StatsSet params = new StatsSet();
				set.set("params", params);
				forEach(handlerNode, "param", paramNode -> params.set(parseString(paramNode.getAttributes(), "name"), paramNode.getTextContent()));
			});
			
			final OneDayRewardDataHolder holder = new OneDayRewardDataHolder(set);
			_oneDayReward.computeIfAbsent(holder.getId(), k -> new ArrayList<>()).add(holder);
		}));
	}
	
	public Collection<OneDayRewardDataHolder> getOneDayRewardData()
	{
		//@formatter:off
		return _oneDayReward.values()
			.stream()
			.flatMap(List::stream)
			.collect(Collectors.toList());
		//@formatter:on
	}
	
	public Collection<OneDayRewardDataHolder> getOneDayRewardData(L2PcInstance player)
	{
		//@formatter:off
		return _oneDayReward.values()
			.stream()
			.flatMap(List::stream)
			.filter(o -> o.isDisplayable(player))
			.collect(Collectors.toList());
		//@formatter:on
	}
	
	public Collection<OneDayRewardDataHolder> getOneDayRewardData(int id)
	{
		return _oneDayReward.get(id);
	}
	
	/**
	 * Gets the single instance of OneDayRewardData.
	 * @return single instance of OneDayRewardData
	 */
	public static final OneDayRewardData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final OneDayRewardData _instance = new OneDayRewardData();
	}
}
