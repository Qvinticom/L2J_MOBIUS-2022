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

import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.datatables.SkillTable;
import org.l2jmobius.gameserver.datatables.sql.SkillSpellbookTable;
import org.l2jmobius.gameserver.datatables.sql.SkillTreeTable;
import org.l2jmobius.gameserver.model.PledgeSkillLearn;
import org.l2jmobius.gameserver.model.ShortCut;
import org.l2jmobius.gameserver.model.Skill;
import org.l2jmobius.gameserver.model.SkillLearn;
import org.l2jmobius.gameserver.model.actor.instance.FishermanInstance;
import org.l2jmobius.gameserver.model.actor.instance.FolkInstance;
import org.l2jmobius.gameserver.model.actor.instance.NpcInstance;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.actor.instance.VillageMasterInstance;
import org.l2jmobius.gameserver.model.items.instance.ItemInstance;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ExStorageMaxCount;
import org.l2jmobius.gameserver.network.serverpackets.PledgeSkillList;
import org.l2jmobius.gameserver.network.serverpackets.ShortCutRegister;
import org.l2jmobius.gameserver.network.serverpackets.StatusUpdate;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.util.IllegalPlayerAction;
import org.l2jmobius.gameserver.util.Util;

public class RequestAquireSkill extends GameClientPacket
{
	private static Logger LOGGER = Logger.getLogger(RequestAquireSkill.class.getName());
	
	private int _id;
	
	private int _level;
	
	private int _skillType;
	
	@Override
	protected void readImpl()
	{
		_id = readD();
		_level = readD();
		_skillType = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final PlayerInstance player = getClient().getPlayer();
		
		if (player == null)
		{
			return;
		}
		
		final FolkInstance trainer = player.getLastFolkNPC();
		
		if (trainer == null)
		{
			return;
		}
		
		final int npcid = trainer.getNpcId();
		
		if (!player.isInsideRadius(trainer, NpcInstance.INTERACTION_DISTANCE, false, false) && !player.isGM())
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
		
		final Skill skill = SkillTable.getInstance().getInfo(_id, _level);
		
		int counts = 0;
		int _requiredSp = 10000000;
		
		if (_skillType == 0)
		{
			final SkillLearn[] skills = SkillTreeTable.getInstance().getAvailableSkills(player, player.getSkillLearningClassId());
			
			for (SkillLearn s : skills)
			{
				final Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
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
				int spbId = -1;
				// divine inspiration require book for each level
				if (Config.DIVINE_SP_BOOK_NEEDED && (skill.getId() == Skill.SKILL_DIVINE_INSPIRATION))
				{
					spbId = SkillSpellbookTable.getInstance().getBookForSkill(skill, _level);
				}
				else if (Config.SP_BOOK_NEEDED && (skill.getLevel() == 1))
				{
					spbId = SkillSpellbookTable.getInstance().getBookForSkill(skill);
				}
				
				// spellbook required
				if (spbId > -1)
				{
					final ItemInstance spb = player.getInventory().getItemByItemId(spbId);
					
					if (spb == null)
					{
						// Haven't spellbook
						player.sendPacket(SystemMessageId.ITEM_MISSING_TO_LEARN_SKILL);
						return;
					}
					
					// ok
					player.destroyItem("Consume", spb, trainer, true);
				}
			}
			else
			{
				final SystemMessage sm = new SystemMessage(SystemMessageId.NOT_ENOUGH_SP_TO_LEARN_SKILL);
				player.sendPacket(sm);
				
				return;
			}
		}
		else if (_skillType == 1)
		{
			int costid = 0;
			int costcount = 0;
			// Skill Learn bug Fix
			final SkillLearn[] skillsc = SkillTreeTable.getInstance().getAvailableSkills(player);
			
			for (SkillLearn s : skillsc)
			{
				final Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
				
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
					player.sendPacket(SystemMessageId.ITEM_MISSING_TO_LEARN_SKILL);
					return;
				}
				
				final SystemMessage sm = new SystemMessage(SystemMessageId.DISSAPEARED_ITEM);
				sm.addNumber(costcount);
				sm.addItemName(costid);
				sendPacket(sm);
			}
			else
			{
				final SystemMessage sm = new SystemMessage(SystemMessageId.NOT_ENOUGH_SP_TO_LEARN_SKILL);
				player.sendPacket(sm);
				return;
			}
		}
		else if (_skillType == 2) // pledgeskills TODO: Find appropriate system messages.
		{
			if (!player.isClanLeader())
			{
				// TODO: Find and add system msg
				player.sendMessage("This feature is available only for the clan leader");
				return;
			}
			
			int itemId = 0;
			int repCost = 100000000;
			// Skill Learn bug Fix
			final PledgeSkillLearn[] skills = SkillTreeTable.getInstance().getAvailablePledgeSkills(player);
			
			for (PledgeSkillLearn s : skills)
			{
				final Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
				
				if ((sk == null) || (sk != skill))
				{
					continue;
				}
				
				counts++;
				itemId = s.getItemId();
				repCost = s.getRepCost();
			}
			
			if (counts == 0)
			{
				player.sendMessage("You are trying to learn skill that u can't..");
				Util.handleIllegalPlayerAction(player, "Player " + player.getName() + " tried to learn skill that he can't!!!", IllegalPlayerAction.PUNISH_KICK);
				return;
			}
			
			if (player.getClan().getReputationScore() >= repCost)
			{
				if (Config.LIFE_CRYSTAL_NEEDED)
				{
					if (!player.destroyItemByItemId("Consume", itemId, 1, trainer, false))
					{
						// Haven't spellbook
						player.sendPacket(SystemMessageId.ITEM_MISSING_TO_LEARN_SKILL);
						return;
					}
					
					final SystemMessage sm = new SystemMessage(SystemMessageId.DISSAPEARED_ITEM);
					sm.addItemName(itemId);
					sm.addNumber(1);
					sendPacket(sm);
				}
			}
			else
			{
				final SystemMessage sm = new SystemMessage(SystemMessageId.ACQUIRE_SKILL_FAILED_BAD_CLAN_REP_SCORE);
				player.sendPacket(sm);
				return;
			}
			player.getClan().setReputationScore(player.getClan().getReputationScore() - repCost, true);
			player.getClan().addNewSkill(skill);
			
			final SystemMessage cr = new SystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP);
			cr.addNumber(repCost);
			player.sendPacket(cr);
			final SystemMessage sm = new SystemMessage(SystemMessageId.CLAN_SKILL_S1_ADDED);
			sm.addSkillName(_id);
			player.sendPacket(sm);
			
			player.getClan().broadcastToOnlineMembers(new PledgeSkillList(player.getClan()));
			
			for (PlayerInstance member : player.getClan().getOnlineMembers())
			{
				member.sendSkillList();
			}
			
			if (trainer instanceof VillageMasterInstance)
			{
				((VillageMasterInstance) trainer).showPledgeSkillList(player);
			}
			
			return;
		}
		else
		{
			LOGGER.warning("Recived Wrong Packet Data in Aquired Skill - unk1:" + _skillType);
			return;
		}
		
		player.addSkill(skill, true);
		player.setSp(player.getSp() - _requiredSp);
		
		final StatusUpdate su = new StatusUpdate(player.getObjectId());
		su.addAttribute(StatusUpdate.SP, player.getSp());
		player.sendPacket(su);
		
		final SystemMessage sp = new SystemMessage(SystemMessageId.SP_DECREASED_S1);
		sp.addNumber(_requiredSp);
		sendPacket(sp);
		
		final SystemMessage sm = new SystemMessage(SystemMessageId.LEARNED_SKILL_S1);
		sm.addSkillName(_id);
		player.sendPacket(sm);
		
		// update all the shortcuts to this skill
		if (_level > 1)
		{
			final ShortCut[] allShortCuts = player.getAllShortCuts();
			
			for (ShortCut sc : allShortCuts)
			{
				if ((sc.getId() == _id) && (sc.getType() == ShortCut.TYPE_SKILL))
				{
					final ShortCut newsc = new ShortCut(sc.getSlot(), sc.getPage(), sc.getType(), sc.getId(), _level, 1);
					player.sendPacket(new ShortCutRegister(newsc));
					player.registerShortCut(newsc);
				}
			}
		}
		
		if (trainer instanceof FishermanInstance)
		{
			((FishermanInstance) trainer).showSkillList(player);
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
		
		player.sendSkillList();
	}
}
