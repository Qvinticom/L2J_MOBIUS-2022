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
package handlers.communityboard;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringJoiner;

import com.l2jmobius.gameserver.cache.HtmCache;
import com.l2jmobius.gameserver.data.xml.impl.NpcData;
import com.l2jmobius.gameserver.datatables.ItemTable;
import com.l2jmobius.gameserver.datatables.SpawnTable;
import com.l2jmobius.gameserver.handler.CommunityBoardHandler;
import com.l2jmobius.gameserver.handler.IParseBoardHandler;
import com.l2jmobius.gameserver.model.L2Spawn;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.actor.templates.L2NpcTemplate;
import com.l2jmobius.gameserver.model.drops.DropListScope;
import com.l2jmobius.gameserver.model.drops.GeneralDropItem;
import com.l2jmobius.gameserver.model.drops.GroupedGeneralDropItem;
import com.l2jmobius.gameserver.model.drops.IDropItem;
import com.l2jmobius.gameserver.model.itemcontainer.Inventory;
import com.l2jmobius.gameserver.model.items.L2Item;

/**
 * @author yksdtc
 */
public class DropSearchBoard implements IParseBoardHandler
{
	private static final String NAVIGATION_PATH = "data/html/CommunityBoard/Custom/navigation.html";
	private static final String[] COMMAND =
	{
		"_bbs_search_item",
		"_bbs_search_drop",
		"_bbs_npc_trace"
	};
	
	class DropHolder
	{
		int itemId;
		int npcId;
		byte npcLevel;
		long basemin;
		long basemax;
		double baseGroupChance;
		double basechance;
		boolean isSweep;
		
		public DropHolder(L2NpcTemplate npc, GeneralDropItem item, double groupChance, boolean isSweep)
		{
			itemId = item.getItemId();
			npcId = npc.getId();
			npcLevel = npc.getLevel();
			basemin = item.getMin();
			basemax = item.getMax();
			baseGroupChance = groupChance;
			basechance = item.getChance();
			this.isSweep = isSweep;
		}
		
		/**
		 * only for debug'/;
		 */
		@Override
		public String toString()
		{
			return "DropHolder [itemId=" + itemId + ", npcId=" + npcId + ", npcLevel=" + npcLevel + ", basemin=" + basemin + ", basemax=" + basemax + ", baseGroupChance=" + baseGroupChance + ", basechance=" + basechance + ", isSweep=" + isSweep + "]";
		}
	}
	
	private final Map<Integer, List<DropHolder>> DROP_INDEX_CACHE = new HashMap<>();
	
	// nonsupport items
	private final Set<Integer> BLOCK_ID = new HashSet<>();
	{
		BLOCK_ID.add(Inventory.ADENA_ID);
	}
	
	public DropSearchBoard()
	{
		buildDropIndex();
	}
	
	private void buildDropIndex()
	{
		NpcData.getInstance().getTemplates(npc -> npc.getDropLists() != null).forEach(npcTemplate ->
		{
			for (Entry<DropListScope, List<IDropItem>> entry : npcTemplate.getDropLists().entrySet())
			{
				entry.getValue().forEach(idrop ->
				{
					if (idrop instanceof GroupedGeneralDropItem)
					{
						GroupedGeneralDropItem ggd = (GroupedGeneralDropItem) idrop;
						ggd.getItems().stream().forEach(gd -> addToDropList(npcTemplate, gd, ggd.getChance(), entry.getKey() == DropListScope.CORPSE));
					}
					else
					{
						GeneralDropItem gd = (GeneralDropItem) idrop;
						addToDropList(npcTemplate, gd, 100.0, entry.getKey() == DropListScope.CORPSE);
					}
				});
			}
		});
		
		DROP_INDEX_CACHE.values().stream().forEach(l -> l.sort((d1, d2) -> Byte.valueOf(d1.npcLevel).compareTo(Byte.valueOf(d2.npcLevel))));
	}
	
	private void addToDropList(L2NpcTemplate npcTemplate, GeneralDropItem gd, double groupChance, boolean isSweep)
	{
		if (BLOCK_ID.contains(gd.getItemId()))
		{
			return;
		}
		
		List<DropHolder> dropList = DROP_INDEX_CACHE.get(gd.getItemId());
		if (dropList == null)
		{
			dropList = new ArrayList<>();
			DROP_INDEX_CACHE.put(gd.getItemId(), dropList);
		}
		
		dropList.add(new DropHolder(npcTemplate, gd, groupChance, isSweep));
	}
	
	@Override
	public boolean parseCommunityBoardCommand(String command, L2PcInstance player)
	{
		final String navigation = HtmCache.getInstance().getHtm(player.getHtmlPrefix(), NAVIGATION_PATH);
		String[] params = command.split(" ");
		String html = HtmCache.getInstance().getHtm(player.getHtmlPrefix(), "data/html/CommunityBoard/Custom/dropsearch/main.html");
		switch (params[0])
		{
			case "_bbs_search_item":
			{
				String itemName = buildItemName(params);
				String result = buildItemSearchResult(itemName);
				html = html.replace("%searchResult%", result);
				break;
			}
			case "_bbs_search_drop":
			{
				final DecimalFormat chanceFormat = new DecimalFormat("0.00##");
				int itemId = Integer.parseInt(params[1]);
				int page = Integer.parseInt(params[2]);
				List<DropHolder> list = DROP_INDEX_CACHE.get(itemId);
				int pages = list.size() / 14;
				if (pages == 0)
				{
					pages++;
				}
				
				int start = (page - 1) * 14;
				int end = Math.min(list.size() - 1, start + 14);
				StringBuilder builder = new StringBuilder();
				for (int index = start; index <= end; index++)
				{
					DropHolder dropHolder = list.get(index);
					builder.append("<tr>");
					builder.append("<td width=30>").append(dropHolder.npcLevel).append("</td>");
					builder.append("<td width=170>").append("<a action=\"bypass _bbs_npc_trace " + dropHolder.npcId + "\">").append("&@").append(dropHolder.npcId).append(";").append("</a>").append("</td>");
					builder.append("<td width=80 align=CENTER>").append(dropHolder.basemin).append("-").append(dropHolder.basemax).append("</td>");
					builder.append("<td width=50 align=CENTER>").append(chanceFormat.format((dropHolder.basechance * dropHolder.baseGroupChance) / 100)).append("%").append("</td>");
					builder.append("<td width=50 align=CENTER>").append(dropHolder.isSweep ? "Sweep" : "Drop").append("</td>");
					builder.append("</tr>");
				}
				
				html = html.replace("%searchResult%", builder.toString());
				builder.setLength(0);
				
				builder.append("<tr>");
				for (page = 1; page <= pages; page++)
				{
					builder.append("<td>").append("<a action=\"bypass -h _bbs_search_drop " + itemId + " " + page + " $order $level\">").append(page).append("</a>").append("</td>");
				}
				builder.append("</tr>");
				html = html.replace("%pages%", builder.toString());
				break;
			}
			case "_bbs_npc_trace":
			{
				int npcId = Integer.parseInt(params[1]);
				L2Spawn spawn = SpawnTable.getInstance().findAny(npcId);
				if (spawn == null)
				{
					player.sendMessage("cant find any spawn maybe boss or instance mob");
				}
				else
				{
					player.getRadar().addMarker(spawn.getX(), spawn.getY(), spawn.getZ());
				}
				break;
			}
		}
		
		if (html != null)
		{
			html = html.replace("%navigation%", navigation);
			CommunityBoardHandler.separateAndSend(html, player);
		}
		
		return false;
	}
	
	/**
	 * @param itemName
	 * @return
	 */
	private String buildItemSearchResult(String itemName)
	{
		int limit = 0;
		Set<Integer> existInDropData = DROP_INDEX_CACHE.keySet();
		List<L2Item> items = new ArrayList<>();
		for (L2Item item : ItemTable.getInstance().getAllItems())
		{
			if (item == null)
			{
				continue;
			}
			
			if (!existInDropData.contains(item.getId()))
			{
				continue;
			}
			
			if (item.getName().toLowerCase().contains(itemName.toLowerCase()))
			{
				items.add(item);
				limit++;
			}
			
			if (limit == 14)
			{
				break;
			}
		}
		
		if (items.isEmpty())
		{
			return "<tr><td width=100 align=CENTER>No Match</td></tr>";
		}
		
		int line = 0;
		
		StringBuilder builder = new StringBuilder(items.size() * 28);
		int i = 0;
		for (L2Item item : items)
		{
			i++;
			if (i == 1)
			{
				line++;
				builder.append("<tr>");
			}
			
			String icon = item.getIcon();
			if (icon == null)
			{
				icon = "icon.etc_question_mark_i00";
			}
			
			builder.append("<td>");
			builder.append("<button value=\".\" action=\"bypass _bbs_search_drop " + item.getId() + " 1 $order $level\" width=32 height=32 back=\"" + icon + "\" fore=\"" + icon + "\">");
			builder.append("</td>");
			builder.append("<td width=200>");
			builder.append("&#").append(item.getId()).append(";");
			builder.append("</td>");
			
			if (i == 2)
			{
				builder.append("</tr>");
				i = 0;
			}
		}
		
		if ((i % 2) == 1)
		{
			builder.append("</tr>");
		}
		
		if (line < 7)
		{
			for (i = 0; i < (7 - line); i++)
			{
				builder.append("<tr><td height=36></td></tr>");
			}
		}
		
		return builder.toString();
	}
	
	/**
	 * @param params
	 * @return
	 */
	private String buildItemName(String[] params)
	{
		StringJoiner joiner = new StringJoiner(" ");
		for (int i = 1; i < params.length; i++)
		{
			joiner.add(params[i]);
		}
		return joiner.toString();
	}
	
	@Override
	public String[] getCommunityBoardCommands()
	{
		return COMMAND;
	}
}
