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
package custom.RaidbossInfo;

import java.util.HashMap;
import java.util.Map;

import com.l2jmobius.gameserver.datatables.sql.NpcTable;
import com.l2jmobius.gameserver.datatables.sql.SpawnTable;
import com.l2jmobius.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.actor.position.Location;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.model.spawn.L2Spawn;
import com.l2jmobius.gameserver.templates.chars.L2NpcTemplate;
import com.l2jmobius.gameserver.util.Util;

public class RaidbossInfo extends Quest
{
	private static final String qn = "RaidbossInfo";
	private static final String BOSS_CLASS_TYPE = "L2RaidBoss";
	
	private static final Map<Integer, Location> RADARS = new HashMap<>();
	
	private static final int[] NPCs =
	{
		31729,
		31730,
		31731,
		31732,
		31733,
		31734,
		31735,
		31736,
		31737,
		31738,
		31775,
		31776,
		31777,
		31778,
		31779,
		31780,
		31781,
		31782,
		31783,
		31784,
		31785,
		31786,
		31787,
		31788,
		31789,
		31790,
		31791,
		31792,
		31793,
		31794,
		31795,
		31796,
		31797,
		31798,
		31799,
		31800,
		31801,
		31802,
		31803,
		31804,
		31805,
		31806,
		31807,
		31808,
		31809,
		31810,
		31811,
		31812,
		31813,
		31814,
		31815,
		31816,
		31817,
		31818,
		31819,
		31820,
		31821,
		31822,
		31823,
		31824,
		31825,
		31826,
		31827,
		31828,
		31829,
		31830,
		31831,
		31832,
		31833,
		31834,
		31835,
		31836,
		31837,
		31838,
		31839,
		31840,
		31841
	};
	
	public RaidbossInfo()
	{
		super(-1, qn, "custom");
		
		for (int npcId : NPCs)
		{
			addStartNpc(npcId);
			addTalkId(npcId);
		}
		
		// Add all Raid Bosses locations.
		for (L2Spawn spawn : SpawnTable.getInstance().getAllTemplates().values())
		{
			final L2NpcTemplate template = NpcTable.getInstance().getTemplate(spawn.getNpcId());
			if ((template != null) && template.type.equals(BOSS_CLASS_TYPE))
			{
				RADARS.put(spawn.getNpcId(), new Location(spawn.getX(), spawn.getY(), spawn.getZ()));
			}
		}
	}
	
	@Override
	public String onAdvEvent(String event, L2NpcInstance npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return event;
		}
		
		if (Util.isDigit(event))
		{
			int rbid = Integer.parseInt(event);
			
			if (RADARS.containsKey(rbid))
			{
				Location loc = RADARS.get(rbid);
				st.addRadar(loc.getX(), loc.getY(), loc.getZ());
			}
			st.exitQuest(true);
			return null;
		}
		return event;
	}
	
	@Override
	public String onTalk(L2NpcInstance npc, L2PcInstance player)
	{
		return "info.htm";
	}
	
	public static void main(String[] args)
	{
		new RaidbossInfo();
	}
}