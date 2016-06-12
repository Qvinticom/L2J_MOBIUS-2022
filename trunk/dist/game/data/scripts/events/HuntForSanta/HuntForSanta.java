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
package events.HuntForSanta;

import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.holders.SkillHolder;
import com.l2jmobius.gameserver.model.quest.LongTimeEvent;
import com.l2jmobius.gameserver.model.skills.BuffInfo;
import com.l2jmobius.gameserver.model.skills.Skill;
import com.l2jmobius.gameserver.network.serverpackets.MagicSkillUse;
import com.l2jmobius.gameserver.util.Util;

/**
 * The Hunt for Santa Begins!<br>
 * Info - http://www.lineage2.com/en/news/events/hunt-for-santa.php
 * @author Mobius
 */
public final class HuntForSanta extends LongTimeEvent
{
	// NPC
	private static final int NOELLE = 34008;
	// Skills
	private static final SkillHolder BUFF_STOCKING = new SkillHolder(16419, 1);
	private static final SkillHolder BUFF_TREE = new SkillHolder(16420, 1);
	private static final SkillHolder BUFF_SNOWMAN = new SkillHolder(16421, 1);
	
	private HuntForSanta()
	{
		addStartNpc(NOELLE);
		addFirstTalkId(NOELLE);
		addTalkId(NOELLE);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = null;
		switch (event)
		{
			case "34008.htm":
			case "34008-1.htm":
			{
				htmltext = event;
				break;
			}
			case "receiveBuffStocking":
			{
				htmltext = applyBuff(npc, player, BUFF_STOCKING.getSkill());
				break;
			}
			case "receiveBuffTree":
			{
				htmltext = applyBuff(npc, player, BUFF_TREE.getSkill());
				break;
			}
			case "receiveBuffSnowman":
			{
				htmltext = applyBuff(npc, player, BUFF_SNOWMAN.getSkill());
				break;
			}
			case "receiveBuffAll":
			{
				htmltext = applyAllBuffs(npc, player);
				break;
			}
			case "changeBuff":
			{
				removeBuffs(player);
				htmltext = "34008-1.htm";
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		return "34008.htm";
	}
	
	private String applyBuff(L2Npc npc, L2PcInstance player, Skill skill)
	{
		removeBuffs(player);
		npc.broadcastPacket(new MagicSkillUse(npc, player, skill.getId(), 1, 0, 0));
		skill.applyEffects(npc, player);
		return "34008-2.htm";
	}
	
	private String applyAllBuffs(L2Npc npc, L2PcInstance player)
	{
		if ((player.getParty() != null) && (player.getParty().getLeader() == player) && ((player.getParty().getMemberCount() > 6) || (player.getParty().getRaceCount() > 2)))
		{
			for (L2PcInstance member : player.getParty().getMembers())
			{
				if (Util.calculateDistance(npc, member, false, false) < 500)
				{
					removeBuffs(member);
					npc.broadcastPacket(new MagicSkillUse(npc, member, BUFF_STOCKING.getSkillId(), 1, 0, 0));
					BUFF_STOCKING.getSkill().applyEffects(npc, member);
					BUFF_TREE.getSkill().applyEffects(npc, member);
					BUFF_SNOWMAN.getSkill().applyEffects(npc, member);
				}
			}
			return "34008-2.htm";
		}
		else if (player.getParty() == null)
		{
			return "34008-3.htm";
		}
		return "34008-4.htm";
	}
	
	private void removeBuffs(L2PcInstance player)
	{
		final BuffInfo buffStocking = player.getEffectList().getBuffInfoBySkillId(BUFF_STOCKING.getSkillId());
		final BuffInfo buffTree = player.getEffectList().getBuffInfoBySkillId(BUFF_TREE.getSkillId());
		final BuffInfo buffSnowman = player.getEffectList().getBuffInfoBySkillId(BUFF_SNOWMAN.getSkillId());
		if (buffStocking != null)
		{
			player.getEffectList().remove(true, buffStocking, null);
		}
		if (buffTree != null)
		{
			player.getEffectList().remove(true, buffTree, null);
		}
		if (buffSnowman != null)
		{
			player.getEffectList().remove(true, buffSnowman, null);
		}
	}
	
	public static void main(String[] args)
	{
		new HuntForSanta();
	}
}
