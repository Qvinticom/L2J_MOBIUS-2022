/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.l2jmobius.gameserver.model.actor.instance;

import org.l2jmobius.gameserver.data.SkillTreeTable;
import org.l2jmobius.gameserver.model.SkillLearn;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import org.l2jmobius.gameserver.network.serverpackets.AquireSkillList;
import org.l2jmobius.gameserver.templates.Npc;

public class TrainerInstance extends NpcInstance
{
	public TrainerInstance(Npc template)
	{
		super(template);
	}
	
	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String pom = "";
		pom = val == 0 ? "" + npcId : npcId + "-" + val;
		return "data/html/trainer/" + pom + ".htm";
	}
	
	public void showSkillList(PlayerInstance player)
	{
		final AquireSkillList asl = new AquireSkillList();
		for (SkillLearn skill : SkillTreeTable.getInstance().getAvailableSkills(player))
		{
			asl.addSkill(skill.getId(), skill.getLevel(), skill.getLevel(), skill.getSpCost(), 0);
		}
		player.sendPacket(asl);
		player.sendPacket(new ActionFailed());
	}
	
	@Override
	public void onBypassFeedback(PlayerInstance player, String command)
	{
		if (command.startsWith("SkillList"))
		{
			showSkillList(player);
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
}
