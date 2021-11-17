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
package org.l2jmobius.gameserver.network.clientpackets;

import org.l2jmobius.Config;
import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.data.SkillTable;
import org.l2jmobius.gameserver.data.sql.SkillSpellbookTable;
import org.l2jmobius.gameserver.data.sql.SkillTreeTable;
import org.l2jmobius.gameserver.model.PledgeSkillLearn;
import org.l2jmobius.gameserver.model.Skill;
import org.l2jmobius.gameserver.model.SkillLearn;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.instance.Folk;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.serverpackets.AquireSkillInfo;

public class RequestAquireSkillInfo implements IClientIncomingPacket
{
	private int _id;
	private int _level;
	private int _skillType;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_id = packet.readD();
		_level = packet.readD();
		_skillType = packet.readD();
		return true;
	}
	
	@Override
	public void run(GameClient client)
	{
		final Player player = client.getPlayer();
		if (player == null)
		{
			return;
		}
		
		final Folk trainer = player.getLastFolkNPC();
		if (trainer == null)
		{
			return;
		}
		
		if (!player.isGM() && !player.isInsideRadius2D(trainer, Npc.INTERACTION_DISTANCE))
		{
			return;
		}
		
		boolean canteach = false;
		final Skill skill = SkillTable.getInstance().getSkill(_id, _level);
		if (skill == null)
		{
			return;
		}
		
		if (_skillType == 0)
		{
			if (!trainer.getTemplate().canTeach(player.getSkillLearningClassId()))
			{
				return; // cheater
			}
			
			for (SkillLearn s : SkillTreeTable.getInstance().getAvailableSkills(player, player.getSkillLearningClassId()))
			{
				if ((s.getId() == _id) && (s.getLevel() == _level))
				{
					canteach = true;
					break;
				}
			}
			
			if (!canteach)
			{
				return; // cheater
			}
			
			final int requiredSp = SkillTreeTable.getInstance().getSkillCost(player, skill);
			final AquireSkillInfo asi = new AquireSkillInfo(skill.getId(), skill.getLevel(), requiredSp, 0);
			int spbId = -1;
			if (Config.DIVINE_SP_BOOK_NEEDED && (skill.getId() == Skill.SKILL_DIVINE_INSPIRATION))
			{
				spbId = SkillSpellbookTable.getInstance().getBookForSkill(skill, _level);
			}
			else if (Config.SP_BOOK_NEEDED && (skill.getLevel() == 1))
			{
				spbId = SkillSpellbookTable.getInstance().getBookForSkill(skill);
			}
			
			if (spbId > -1)
			{
				asi.addRequirement(99, spbId, 1, 50);
			}
			
			player.sendPacket(asi);
		}
		else if (_skillType == 2)
		{
			int requiredRep = 0;
			int itemId = 0;
			for (PledgeSkillLearn s : SkillTreeTable.getInstance().getAvailablePledgeSkills(player))
			{
				if ((s.getId() == _id) && (s.getLevel() == _level))
				{
					canteach = true;
					requiredRep = s.getRepCost();
					itemId = s.getItemId();
					break;
				}
			}
			
			if (!canteach)
			{
				return; // cheater
			}
			
			final AquireSkillInfo asi = new AquireSkillInfo(skill.getId(), skill.getLevel(), requiredRep, 2);
			if (Config.LIFE_CRYSTAL_NEEDED)
			{
				asi.addRequirement(1, itemId, 1, 0);
			}
			player.sendPacket(asi);
		}
		else // Common Skills
		{
			int costid = 0;
			int costcount = 0;
			int spcost = 0;
			
			for (SkillLearn s : SkillTreeTable.getInstance().getAvailableSkills(player))
			{
				final Skill sk = SkillTable.getInstance().getSkill(s.getId(), s.getLevel());
				if ((sk == null) || (sk != skill))
				{
					continue;
				}
				
				canteach = true;
				costid = s.getIdCost();
				costcount = s.getCostCount();
				spcost = s.getSpCost();
			}
			
			final AquireSkillInfo asi = new AquireSkillInfo(skill.getId(), skill.getLevel(), spcost, 1);
			asi.addRequirement(4, costid, costcount, 0);
			player.sendPacket(asi);
		}
	}
}
