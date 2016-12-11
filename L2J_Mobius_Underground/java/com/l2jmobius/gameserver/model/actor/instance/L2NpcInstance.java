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

import java.util.List;
import java.util.Map;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.cache.HtmCache;
import com.l2jmobius.gameserver.data.xml.impl.SkillTreesData;
import com.l2jmobius.gameserver.enums.InstanceType;
import com.l2jmobius.gameserver.model.L2SkillLearn;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.status.FolkStatus;
import com.l2jmobius.gameserver.model.actor.templates.L2NpcTemplate;
import com.l2jmobius.gameserver.model.base.AcquireSkillType;
import com.l2jmobius.gameserver.model.base.ClassId;
import com.l2jmobius.gameserver.network.SystemMessageId;
import com.l2jmobius.gameserver.network.serverpackets.ExAcquirableSkillListByClass;
import com.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;

public class L2NpcInstance extends L2Npc
{
	public L2NpcInstance(L2NpcTemplate template)
	{
		super(template);
		setInstanceType(InstanceType.L2NpcInstance);
		setIsInvul(false);
	}
	
	@Override
	public FolkStatus getStatus()
	{
		return (FolkStatus) super.getStatus();
	}
	
	@Override
	public void initCharStatus()
	{
		setStatus(new FolkStatus(this));
	}
	
	public List<ClassId> getClassesToTeach()
	{
		return getTemplate().getTeachInfo();
	}
	
	/**
	 * Displays Skill Tree for a given player, npc and class Id.
	 * @param player the active character.
	 * @param npc the last folk.
	 * @param classId player's active class id.
	 */
	public static void showSkillList(L2PcInstance player, L2Npc npc, ClassId classId)
	{
		if (Config.DEBUG)
		{
			_log.finer("SkillList activated on: " + npc.getObjectId());
		}
		
		final int npcId = npc.getTemplate().getId();
		if (npcId == 32611) // Tolonis (Officer)
		{
			final List<L2SkillLearn> skills = SkillTreesData.getInstance().getAvailableCollectSkills(player);
			
			if (skills.isEmpty()) // No more skills to learn, come back when you level.
			{
				final int minLevel = SkillTreesData.getInstance().getMinLevelForNewSkill(player, SkillTreesData.getInstance().getCollectSkillTree());
				if (minLevel > 0)
				{
					final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_DO_NOT_HAVE_ANY_FURTHER_SKILLS_TO_LEARN_COME_BACK_WHEN_YOU_HAVE_REACHED_LEVEL_S1);
					sm.addInt(minLevel);
					player.sendPacket(sm);
				}
				else
				{
					player.sendPacket(SystemMessageId.THERE_ARE_NO_OTHER_SKILLS_TO_LEARN);
				}
			}
			else
			{
				player.sendPacket(new ExAcquirableSkillListByClass(skills, AcquireSkillType.COLLECT));
			}
			return;
		}
		
		if (!npc.getTemplate().canTeach(classId))
		{
			String html = "";
			
			if (npc instanceof L2WarehouseInstance)
			{
				html = HtmCache.getInstance().getHtm(player.getHtmlPrefix(), "data/html/warehouse/" + npcId + "-noteach.htm");
			}
			
			final NpcHtmlMessage noTeachMsg = new NpcHtmlMessage(npc.getObjectId());
			if (html == null)
			{
				_log.warning("Npc " + npcId + " missing noTeach html!");
				noTeachMsg.setHtml("<html><body>I cannot teach you any skills.<br>You must find your current class teachers.</body></html>");
			}
			else
			{
				noTeachMsg.setHtml(html);
				noTeachMsg.replace("%objectId%", String.valueOf(npc.getObjectId()));
			}
			player.sendPacket(noTeachMsg);
			return;
		}
		
		if (((L2NpcInstance) npc).getClassesToTeach().isEmpty())
		{
			final NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
			final String sb = "<html><body>I cannot teach you. My class list is empty.<br>Ask admin to fix it. Need add my npcid and classes to skill_learn.sql.<br>NpcId:" + npcId + ", Your classId:" + player.getClassId().getId() + "</body></html>";
			html.setHtml(sb);
			player.sendPacket(html);
			return;
		}
		
		// Normal skills, No LearnedByFS, no AutoGet skills.
		final List<L2SkillLearn> skills = SkillTreesData.getInstance().getAvailableSkills(player, classId, false, false);
		player.setLearningClass(classId);
		
		if (skills.isEmpty())
		{
			final Map<Long, L2SkillLearn> skillTree = SkillTreesData.getInstance().getCompleteClassSkillTree(classId);
			final int minLevel = SkillTreesData.getInstance().getMinLevelForNewSkill(player, skillTree);
			if (minLevel > 0)
			{
				final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_DO_NOT_HAVE_ANY_FURTHER_SKILLS_TO_LEARN_COME_BACK_WHEN_YOU_HAVE_REACHED_LEVEL_S1);
				sm.addInt(minLevel);
				player.sendPacket(sm);
			}
			else if (player.getClassId().level() == 1)
			{
				final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.THERE_ARE_NO_OTHER_SKILLS_TO_LEARN_PLEASE_COME_BACK_AFTER_S1ND_CLASS_CHANGE);
				sm.addInt(2);
				player.sendPacket(sm);
			}
			else
			{
				player.sendPacket(SystemMessageId.THERE_ARE_NO_OTHER_SKILLS_TO_LEARN);
			}
		}
		else
		{
			player.sendPacket(new ExAcquirableSkillListByClass(skills, AcquireSkillType.CLASS));
		}
	}
}
