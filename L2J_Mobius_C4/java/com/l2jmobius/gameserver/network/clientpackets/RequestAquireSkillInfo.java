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

import java.util.logging.Logger;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.datatables.SkillSpellbookTable;
import com.l2jmobius.gameserver.datatables.SkillTable;
import com.l2jmobius.gameserver.datatables.SkillTreeTable;
import com.l2jmobius.gameserver.model.L2Skill;
import com.l2jmobius.gameserver.model.L2SkillLearn;
import com.l2jmobius.gameserver.model.actor.instance.L2FolkInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.serverpackets.AquireSkillInfo;

/**
 * This class ...
 * @version $Revision: 1.5.2.1.2.5 $ $Date: 2005/04/06 16:13:48 $
 */
public class RequestAquireSkillInfo extends L2GameClientPacket
{
	private static final String _C__6B_REQUESTAQUIRESKILLINFO = "[C] 6B RequestAquireSkillInfo";
	private static Logger _log = Logger.getLogger(RequestAquireSkillInfo.class.getName());
	
	private int _id;
	private int _level;
	private int _fisherman;
	
	@Override
	protected void readImpl()
	{
		_id = readD();
		_level = readD();
		_fisherman = readD();// normal(0) learn or fisherman(1)
	}
	
	@Override
	public void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
		{
			return;
		}
		
		final L2FolkInstance trainer = activeChar.getLastFolkNPC();
		
		if (((trainer == null) || !activeChar.isInsideRadius(trainer, L2NpcInstance.INTERACTION_DISTANCE, false, false)) && !activeChar.isGM())
		{
			return;
		}
		
		final L2Skill skill = SkillTable.getInstance().getInfo(_id, _level);
		
		boolean canteach = false;
		
		if (skill == null)
		{
			_log.warning("skill id " + _id + " level " + _level + " is undefined. aquireSkillInfo failed.");
			return;
		}
		
		if (_fisherman == 0)
		{
			if ((trainer != null) && !trainer.getTemplate().canTeach(activeChar.getSkillLearningClassId()))
			{
				return; // cheater
			}
			
			final L2SkillLearn[] skills = SkillTreeTable.getInstance().getAvailableSkills(activeChar, activeChar.getSkillLearningClassId());
			
			for (final L2SkillLearn s : skills)
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
			
			final int requiredSp = SkillTreeTable.getInstance().getSkillCost(activeChar, skill);
			final AquireSkillInfo asi = new AquireSkillInfo(skill.getId(), skill.getLevel(), requiredSp, 0);
			
			if (Config.SP_BOOK_NEEDED)
			{
				final int spbId = SkillSpellbookTable.getInstance().getBookForSkill(skill);
				
				if ((skill.getLevel() == 1) && (spbId > -1))
				{
					asi.addRequirement(99, spbId, 1, 50);
				}
			}
			
			sendPacket(asi);
		}
		else // Common Skills
		{
			int costid = 0;
			int costcount = 0;
			int spcost = 0;
			
			final L2SkillLearn[] skillsc = SkillTreeTable.getInstance().getAvailableSkills(activeChar);
			
			for (final L2SkillLearn s : skillsc)
			{
				final L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
				
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
			sendPacket(asi);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.network.clientpackets.L2GameClientPacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__6B_REQUESTAQUIRESKILLINFO;
	}
}
