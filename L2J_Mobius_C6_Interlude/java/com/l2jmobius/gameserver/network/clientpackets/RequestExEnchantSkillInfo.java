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
package com.l2jmobius.gameserver.network.clientpackets;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.datatables.SkillTable;
import com.l2jmobius.gameserver.datatables.sql.SkillTreeTable;
import com.l2jmobius.gameserver.model.EnchantSkillLearn;
import com.l2jmobius.gameserver.model.Skill;
import com.l2jmobius.gameserver.model.actor.instance.FolkInstance;
import com.l2jmobius.gameserver.model.actor.instance.NpcInstance;
import com.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import com.l2jmobius.gameserver.network.serverpackets.ExEnchantSkillInfo;

/**
 * Format chdd c: (id) 0xD0 h: (subid) 0x06 d: skill id d: skill lvl
 * @author -Wooden-
 */
public final class RequestExEnchantSkillInfo extends GameClientPacket
{
	private int _skillId;
	private int _skillLvl;
	
	@Override
	protected void readImpl()
	{
		_skillId = readD();
		_skillLvl = readD();
	}
	
	@Override
	protected void runImpl()
	{
		if ((_skillId <= 0) || (_skillLvl <= 0))
		{
			return;
		}
		
		final PlayerInstance player = getClient().getPlayer();
		if (player == null)
		{
			return;
		}
		
		if (player.getLevel() < 76)
		{
			return;
		}
		
		final FolkInstance trainer = player.getLastFolkNPC();
		if (trainer == null)
		{
			return;
		}
		
		if (!player.isInsideRadius(trainer, NpcInstance.INTERACTION_DISTANCE, false, false) && !player.isGM())
		{
			return;
		}
		
		boolean canteach = false;
		
		final Skill skill = SkillTable.getInstance().getInfo(_skillId, _skillLvl);
		if ((skill == null) || (skill.getId() != _skillId))
		{
			return;
		}
		
		if (!trainer.getTemplate().canTeach(player.getClassId()))
		{
			return; // cheater
		}
		
		final EnchantSkillLearn[] skills = SkillTreeTable.getInstance().getAvailableEnchantSkills(player);
		
		for (EnchantSkillLearn s : skills)
		{
			if ((s.getId() == _skillId) && (s.getLevel() == _skillLvl))
			{
				canteach = true;
				break;
			}
		}
		
		if (!canteach)
		{
			return; // cheater
		}
		
		final int requiredSp = SkillTreeTable.getInstance().getSkillSpCost(player, skill);
		final int requiredExp = SkillTreeTable.getInstance().getSkillExpCost(player, skill);
		final byte rate = SkillTreeTable.getInstance().getSkillRate(player, skill);
		final ExEnchantSkillInfo asi = new ExEnchantSkillInfo(skill.getId(), skill.getLevel(), requiredSp, requiredExp, rate);
		
		if (Config.ES_SP_BOOK_NEEDED && ((skill.getLevel() == 101) || (skill.getLevel() == 141))) // only first lvl requires book
		{
			final int spbId = 6622;
			asi.addRequirement(4, spbId, 1, 0);
		}
		sendPacket(asi);
	}
}
