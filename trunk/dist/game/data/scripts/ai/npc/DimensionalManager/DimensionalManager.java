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
package ai.npc.DimensionalManager;

import java.util.HashMap;
import java.util.Map;

import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.holders.SkillHolder;
import com.l2jmobius.gameserver.network.SystemMessageId;

import ai.npc.AbstractNpcAI;

/**
 * Dimensional Manager AI.
 * @author Stayway, Mobius
 */
public final class DimensionalManager extends AbstractNpcAI
{
	// Npc
	private static final int DIMENSIONAL_MANAGER = 32478;
	// Items
	private static final int MINION_COUPON = 21275;
	private static final int SUPERIOR_MINION_COUPON = 21279;
	private static final int ROSE_SPIRIT = 21887;
	// Rewards
	private static final Map<String, SkillHolder> TICKET_REWARDS = new HashMap<>();
	
	{
		// Using SkillHolder to store ticket and reward ids.
		// buypass - coupon(skillId) - reward(skillLvl)
		TICKET_REWARDS.put("whiteweasel", new SkillHolder(MINION_COUPON, 13017));
		TICKET_REWARDS.put("fairyprincess", new SkillHolder(MINION_COUPON, 13018));
		TICKET_REWARDS.put("wildbeastfighter", new SkillHolder(MINION_COUPON, 16044));
		TICKET_REWARDS.put("foxshaman", new SkillHolder(MINION_COUPON, 13020));
		TICKET_REWARDS.put("turtleascetic", new SkillHolder(SUPERIOR_MINION_COUPON, 14064));
		TICKET_REWARDS.put("spiritshaman", new SkillHolder(SUPERIOR_MINION_COUPON, 14062));
		TICKET_REWARDS.put("toyknight", new SkillHolder(SUPERIOR_MINION_COUPON, 14061));
		TICKET_REWARDS.put("desheloph", new SkillHolder(ROSE_SPIRIT, 20915));
		TICKET_REWARDS.put("hyum", new SkillHolder(ROSE_SPIRIT, 20916));
		TICKET_REWARDS.put("lekang", new SkillHolder(ROSE_SPIRIT, 20917));
		TICKET_REWARDS.put("lilias", new SkillHolder(ROSE_SPIRIT, 20918));
		TICKET_REWARDS.put("lapham", new SkillHolder(ROSE_SPIRIT, 20919));
		TICKET_REWARDS.put("mafum", new SkillHolder(ROSE_SPIRIT, 20920));
	}
	
	private DimensionalManager()
	{
		super(DimensionalManager.class.getSimpleName(), "ai/npc");
		addStartNpc(DIMENSIONAL_MANAGER);
		addTalkId(DIMENSIONAL_MANAGER);
		addFirstTalkId(DIMENSIONAL_MANAGER);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = null;
		switch (event)
		{
			case "32478-01.htm":
			case "32478-02.htm":
			case "32478-03.htm":
			case "32478-04.htm":
			case "32478-05.htm":
			case "32478-06.htm":
			case "32478-07.htm":
			case "32478-08.htm":
			case "32478-09.htm":
			case "32478-10.htm":
			case "32478-11.htm":
			case "32478-12.htm":
			case "32478-13.htm":
			case "32478-14.htm":
			case "32478-15.htm":
			case "32478-16.htm":
			case "32478-17.htm":
			case "32478-18.htm":
			case "32478-19.htm":
			case "32478-20.htm":
			case "32478-21.htm":
			{
				htmltext = event;
				break;
			}
			case "turtleascetic":
			case "spiritshaman":
			case "desheloph":
			case "hyum":
			case "lekang":
			case "lilias":
			case "lapham":
			case "mafum":
			case "foxshaman":
			case "whiteweasel":
			case "fairyprincess":
			case "toyknight":
			case "wildbeastfighter":
			{
				if (hasQuestItems(player, TICKET_REWARDS.get(event).getSkillId()))
				{
					takeItems(player, TICKET_REWARDS.get(event).getSkillId(), 1);
					giveItems(player, TICKET_REWARDS.get(event).getSkillLvl(), 1);
				}
				else
				{
					player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		return "32478.htm";
	}
	
	public static void main(String[] args)
	{
		new DimensionalManager();
	}
}
