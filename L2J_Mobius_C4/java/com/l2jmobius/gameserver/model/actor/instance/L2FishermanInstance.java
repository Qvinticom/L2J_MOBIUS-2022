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
package com.l2jmobius.gameserver.model.actor.instance;

import com.l2jmobius.gameserver.datatables.SkillTable;
import com.l2jmobius.gameserver.datatables.SkillTreeTable;
import com.l2jmobius.gameserver.model.L2Skill;
import com.l2jmobius.gameserver.model.L2SkillLearn;
import com.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import com.l2jmobius.gameserver.network.serverpackets.AquireSkillList;
import com.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import com.l2jmobius.gameserver.templates.L2NpcTemplate;

import javolution.text.TextBuilder;

public class L2FishermanInstance extends L2FolkInstance
{
	/**
	 * @param objectId
	 * @param template
	 */
	public L2FishermanInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String pom = "";
		
		if (val == 0)
		{
			pom = "" + npcId;
		}
		else
		{
			pom = npcId + "-" + val;
		}
		
		return "data/html/fisherman/" + pom + ".htm";
	}
	
	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		if (command.startsWith("FishSkillList"))
		{
			player.setSkillLearningClassId(player.getClassId());
			showSkillList(player);
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
	
	public void showSkillList(L2PcInstance player)
	{
		final L2SkillLearn[] skills = SkillTreeTable.getInstance().getAvailableSkills(player);
		final AquireSkillList asl = new AquireSkillList(true);
		
		int counts = 0;
		
		for (final L2SkillLearn s : skills)
		{
			final L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
			
			if (sk == null)
			{
				continue;
			}
			
			counts++;
			asl.addSkill(s.getId(), s.getLevel(), s.getLevel(), s.getSpCost(), 1);
		}
		
		if (counts == 0)
		{
			final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			final int minlevel = SkillTreeTable.getInstance().getMinLevelForNewSkill(player);
			
			if (minlevel > 0)
			{
				// No more skills to learn, come back when you level.
				final SystemMessage sm = new SystemMessage(SystemMessage.DO_NOT_HAVE_FURTHER_SKILLS_TO_LEARN);
				sm.addNumber(minlevel);
				player.sendPacket(sm);
			}
			else
			{
				final TextBuilder sb = new TextBuilder();
				sb.append("<html><head><body>");
				sb.append("You've learned all skills.<br>");
				sb.append("</body></html>");
				html.setHtml(sb.toString());
				player.sendPacket(html);
			}
		}
		else
		{
			player.sendPacket(asl);
		}
		
		player.sendPacket(new ActionFailed());
	}
}