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
import com.l2jmobius.gameserver.model.L2ItemInstance;
import com.l2jmobius.gameserver.model.L2ShortCut;
import com.l2jmobius.gameserver.model.L2Skill;
import com.l2jmobius.gameserver.model.L2SkillLearn;
import com.l2jmobius.gameserver.model.actor.instance.L2FishermanInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2FolkInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.serverpackets.ExStorageMaxCount;
import com.l2jmobius.gameserver.network.serverpackets.ShortCutRegister;
import com.l2jmobius.gameserver.network.serverpackets.StatusUpdate;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import com.l2jmobius.gameserver.util.IllegalPlayerAction;
import com.l2jmobius.gameserver.util.Util;

/**
 * This class ...
 * @version $Revision: 1.7.2.1.2.4 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestAquireSkill extends L2GameClientPacket
{
	private static final String _C__6C_REQUESTAQUIRESKILL = "[C] 6C RequestAquireSkill";
	private static Logger _log = Logger.getLogger(RequestAquireSkill.class.getName());
	
	private int _id;
	private int _level;
	private int _fisherman;
	
	@Override
	protected void readImpl()
	{
		_id = readD();
		_level = readD();
		_fisherman = readD();
	}
	
	@Override
	public void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		
		final L2FolkInstance trainer = player.getLastFolkNPC();
		if (trainer == null)
		{
			return;
		}
		
		final int npcid = trainer.getNpcId();
		
		if (!player.isInsideRadius(trainer, L2NpcInstance.INTERACTION_DISTANCE, false, false) && !player.isGM())
		{
			return;
		}
		
		if (!Config.ALT_GAME_SKILL_LEARN)
		{
			player.setSkillLearningClassId(player.getClassId());
		}
		
		if (player.getSkillLevel(_id) >= _level)
		{
			// already knows the skill with this level
			return;
		}
		
		final L2Skill skill = SkillTable.getInstance().getInfo(_id, _level);
		
		int counts = 0;
		int _requiredSp = 10000000;
		
		if (_fisherman == 0)
		{
			// Skill Learn bug Fix
			final L2SkillLearn[] skills = SkillTreeTable.getInstance().getAvailableSkills(player, player.getSkillLearningClassId());
			
			for (final L2SkillLearn s : skills)
			{
				final L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
				if ((sk == null) || (sk != skill) || !sk.getCanLearn(player.getSkillLearningClassId()) || !sk.canTeachBy(npcid))
				{
					continue;
				}
				counts++;
				_requiredSp = SkillTreeTable.getInstance().getSkillCost(player, skill);
			}
			
			if ((counts == 0) && !Config.ALT_GAME_SKILL_LEARN)
			{
				player.sendMessage("You are trying to learn skill that u can't..");
				Util.handleIllegalPlayerAction(player, "Player " + player.getName() + " tried to learn skill that he can't!!!", IllegalPlayerAction.PUNISH_KICK);
				return;
			}
			
			if (player.getSp() >= _requiredSp)
			{
				if (Config.SP_BOOK_NEEDED)
				{
					final int spbId = SkillSpellbookTable.getInstance().getBookForSkill(skill);
					
					if ((skill.getLevel() == 1) && (spbId > -1))
					{
						final L2ItemInstance spb = player.getInventory().getItemByItemId(spbId);
						
						if (spb == null)
						{
							// Haven't spellbook
							player.sendPacket(new SystemMessage(SystemMessage.ITEM_MISSING_TO_LEARN_SKILL));
							return;
						}
						
						// ok
						player.destroyItem("Consume", spb, trainer, true);
					}
				}
			}
			else
			{
				SystemMessage sm = new SystemMessage(SystemMessage.NOT_ENOUGH_SP_TO_LEARN_SKILL);
				player.sendPacket(sm);
				sm = null;
				return;
			}
		}
		else if (_fisherman == 1)
		{
			int costid = 0;
			int costcount = 0;
			// Skill Learn bug Fix
			final L2SkillLearn[] skillsc = SkillTreeTable.getInstance().getAvailableSkills(player);
			
			for (final L2SkillLearn s : skillsc)
			{
				final L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
				
				if ((sk == null) || (sk != skill))
				{
					continue;
				}
				
				counts++;
				costid = s.getIdCost();
				costcount = s.getCostCount();
				_requiredSp = s.getSpCost();
			}
			
			if (counts == 0)
			{
				player.sendMessage("You are trying to learn skill that u can't..");
				Util.handleIllegalPlayerAction(player, "Player " + player.getName() + " tried to learn skill that he can't!!!", IllegalPlayerAction.PUNISH_KICK);
				return;
			}
			
			if (player.getSp() >= _requiredSp)
			{
				if (!player.destroyItemByItemId("Consume", costid, costcount, trainer, false))
				{
					// Haven't spellbook
					player.sendPacket(new SystemMessage(SystemMessage.ITEM_MISSING_TO_LEARN_SKILL));
					return;
				}
				
				SystemMessage sm = new SystemMessage(SystemMessage.DISSAPEARED_ITEM);
				sm.addNumber(costcount);
				sm.addItemName(costid);
				sendPacket(sm);
				sm = null;
			}
			else
			{
				SystemMessage sm = new SystemMessage(SystemMessage.NOT_ENOUGH_SP_TO_LEARN_SKILL);
				player.sendPacket(sm);
				sm = null;
				return;
			}
		}
		else
		{
			_log.warning("Recived Wrong Packet Data in Aquired Skill - unk1:" + _fisherman);
			return;
		}
		
		player.addSkill(skill, true);
		
		if (Config.DEBUG)
		{
			_log.fine("Learned skill " + _id + " for " + _requiredSp + " SP.");
		}
		
		player.setSp(player.getSp() - _requiredSp);
		
		final StatusUpdate su = new StatusUpdate(player.getObjectId());
		su.addAttribute(StatusUpdate.SP, player.getSp());
		player.sendPacket(su);
		
		SystemMessage sm = new SystemMessage(SystemMessage.LEARNED_SKILL_S1);
		sm.addSkillName(_id);
		player.sendPacket(sm);
		sm = null;
		
		// update all the shortcuts to this skill
		if (_level > 1)
		{
			final L2ShortCut[] allShortCuts = player.getAllShortCuts();
			
			for (final L2ShortCut sc : allShortCuts)
			{
				if (sc == null)
				{
					continue;
				}
				
				if ((sc.getId() == _id) && (sc.getType() == L2ShortCut.TYPE_SKILL))
				{
					final L2ShortCut newsc = new L2ShortCut(sc.getSlot(), sc.getPage(), sc.getType(), sc.getId(), _level, 1);
					player.sendPacket(new ShortCutRegister(newsc));
					player.registerShortCut(newsc);
				}
			}
		}
		
		if (trainer instanceof L2FishermanInstance)
		{
			((L2FishermanInstance) trainer).showSkillList(player);
		}
		else
		{
			trainer.showSkillList(player, player.getSkillLearningClassId());
		}
		
		if ((_id >= 1368) && (_id <= 1372)) // if skill is expand sendpacket :)
		{
			final ExStorageMaxCount esmc = new ExStorageMaxCount(player);
			player.sendPacket(esmc);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.network.clientpackets.L2GameClientPacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__6C_REQUESTAQUIRESKILL;
	}
}