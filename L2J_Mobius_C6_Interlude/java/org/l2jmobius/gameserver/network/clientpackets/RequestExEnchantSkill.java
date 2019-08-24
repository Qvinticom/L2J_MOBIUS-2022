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
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.datatables.SkillTable;
import org.l2jmobius.gameserver.datatables.sql.SkillTreeTable;
import org.l2jmobius.gameserver.datatables.xml.ExperienceData;
import org.l2jmobius.gameserver.model.EnchantSkillLearn;
import org.l2jmobius.gameserver.model.ShortCut;
import org.l2jmobius.gameserver.model.Skill;
import org.l2jmobius.gameserver.model.actor.instance.FolkInstance;
import org.l2jmobius.gameserver.model.actor.instance.NpcInstance;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.items.instance.ItemInstance;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ShortCutRegister;
import org.l2jmobius.gameserver.network.serverpackets.StatusUpdate;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.network.serverpackets.UserInfo;
import org.l2jmobius.gameserver.util.IllegalPlayerAction;
import org.l2jmobius.gameserver.util.Util;

/**
 * Format chdd c: (id) 0xD0 h: (subid) 0x06 d: skill id d: skill lvl
 * @author -Wooden-
 */
public class RequestExEnchantSkill extends GameClientPacket
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
		
		if (player.getSkillLevel(_skillId) >= _skillLvl)
		{
			return;
		}
		
		if (player.getClassId().getId() < 88)
		{
			return;
		}
		
		if (player.getLevel() < 76)
		{
			return;
		}
		
		final Skill skill = SkillTable.getInstance().getInfo(_skillId, _skillLvl);
		
		int counts = 0;
		int _requiredSp = 10000000;
		int _requiredExp = 100000;
		byte _rate = 0;
		int _baseLvl = 1;
		
		final EnchantSkillLearn[] skills = SkillTreeTable.getInstance().getAvailableEnchantSkills(player);
		
		for (EnchantSkillLearn s : skills)
		{
			final Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
			
			if ((sk == null) || (sk != skill) || !sk.getCanLearn(player.getClassId()) || !sk.canTeachBy(npcid))
			{
				continue;
			}
			
			counts++;
			_requiredSp = s.getSpCost();
			_requiredExp = s.getExp();
			_rate = s.getRate(player);
			_baseLvl = s.getBaseLevel();
		}
		
		if ((counts == 0) && !Config.ALT_GAME_SKILL_LEARN)
		{
			player.sendMessage("You are trying to learn skill that u can't..");
			Util.handleIllegalPlayerAction(player, "Player " + player.getName() + " tried to learn skill that he can't!!!", IllegalPlayerAction.PUNISH_KICK);
			return;
		}
		
		if (player.getSp() >= _requiredSp)
		{
			// Like L2OFF you can't delevel during skill enchant
			final long expAfter = player.getExp() - _requiredExp;
			if ((player.getExp() >= _requiredExp) && (expAfter >= ExperienceData.getInstance().getExpForLevel(player.getLevel())))
			{
				if (Config.ES_SP_BOOK_NEEDED && ((_skillLvl == 101) || (_skillLvl == 141))) // only first lvl requires book
				{
					final int spbId = 6622;
					
					final ItemInstance spb = player.getInventory().getItemByItemId(spbId);
					
					if (spb == null)// Haven't spellbook
					{
						player.sendPacket(SystemMessageId.YOU_DONT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL);
						return;
					}
					// ok
					player.destroyItem("Consume", spb, trainer, true);
				}
			}
			else
			{
				final SystemMessage sm = new SystemMessage(SystemMessageId.YOU_DONT_HAVE_ENOUGH_EXP_TO_ENCHANT_THAT_SKILL);
				player.sendPacket(sm);
				return;
			}
		}
		else
		{
			final SystemMessage sm = new SystemMessage(SystemMessageId.YOU_DONT_HAVE_ENOUGH_SP_TO_ENCHANT_THAT_SKILL);
			player.sendPacket(sm);
			return;
		}
		if (Rnd.get(100) <= _rate)
		{
			player.addSkill(skill, true);
			player.getStat().removeExpAndSp(_requiredExp, _requiredSp);
			
			final StatusUpdate su = new StatusUpdate(player.getObjectId());
			su.addAttribute(StatusUpdate.SP, player.getSp());
			player.sendPacket(su);
			
			final SystemMessage ep = new SystemMessage(SystemMessageId.EXP_DECREASED_BY_S1);
			ep.addNumber(_requiredExp);
			sendPacket(ep);
			
			final SystemMessage sp = new SystemMessage(SystemMessageId.SP_DECREASED_S1);
			sp.addNumber(_requiredSp);
			sendPacket(sp);
			
			final SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_SUCCEEDED_IN_ENCHANTING_THE_SKILL_S1);
			sm.addSkillName(_skillId);
			player.sendPacket(sm);
		}
		else
		{
			if (skill.getLevel() > 100)
			{
				_skillLvl = _baseLvl;
				player.addSkill(SkillTable.getInstance().getInfo(_skillId, _skillLvl), true);
				player.sendSkillList();
			}
			final SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_FAILED_TO_ENCHANT_THE_SKILL_S1);
			sm.addSkillName(_skillId);
			player.sendPacket(sm);
		}
		trainer.showEnchantSkillList(player, player.getClassId());
		
		player.sendPacket(new UserInfo(player));
		player.sendSkillList();
		
		// update all the shortcuts to this skill
		final ShortCut[] allShortCuts = player.getAllShortCuts();
		
		for (ShortCut sc : allShortCuts)
		{
			if ((sc.getId() == _skillId) && (sc.getType() == ShortCut.TYPE_SKILL))
			{
				final ShortCut newsc = new ShortCut(sc.getSlot(), sc.getPage(), sc.getType(), sc.getId(), _skillLvl, 1);
				player.sendPacket(new ShortCutRegister(newsc));
				player.registerShortCut(newsc);
			}
		}
	}
}
