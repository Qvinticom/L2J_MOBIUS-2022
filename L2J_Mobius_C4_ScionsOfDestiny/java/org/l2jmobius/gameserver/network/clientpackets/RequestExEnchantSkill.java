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
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.data.SkillTable;
import org.l2jmobius.gameserver.data.sql.SkillTreeTable;
import org.l2jmobius.gameserver.data.xml.ExperienceData;
import org.l2jmobius.gameserver.model.EnchantSkillLearn;
import org.l2jmobius.gameserver.model.ShortCut;
import org.l2jmobius.gameserver.model.Skill;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.instance.Folk;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ShortCutRegister;
import org.l2jmobius.gameserver.network.serverpackets.StatusUpdate;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.network.serverpackets.UserInfo;
import org.l2jmobius.gameserver.util.IllegalPlayerAction;
import org.l2jmobius.gameserver.util.Util;

/**
 * Format chdd c: (id) 0xD0 h: (subid) 0x06 d: skill id d: skill level
 * @author -Wooden-
 */
public class RequestExEnchantSkill implements IClientIncomingPacket
{
	private int _skillId;
	private int _skillLevel;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_skillId = packet.readD();
		_skillLevel = packet.readD();
		return true;
	}
	
	@Override
	public void run(GameClient client)
	{
		if (!client.getFloodProtectors().canPerformPlayerAction())
		{
			return;
		}
		
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
		
		final int npcid = trainer.getNpcId();
		if (!player.isInsideRadius2D(trainer, Npc.INTERACTION_DISTANCE) && !player.isGM())
		{
			return;
		}
		
		if (player.getSkillLevel(_skillId) >= _skillLevel)
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
		
		final Skill skill = SkillTable.getInstance().getSkill(_skillId, _skillLevel);
		int counts = 0;
		int requiredSp = 10000000;
		int requiredExp = 100000;
		byte rate = 0;
		int baseLevel = 1;
		
		for (EnchantSkillLearn s : SkillTreeTable.getInstance().getAvailableEnchantSkills(player))
		{
			final Skill sk = SkillTable.getInstance().getSkill(s.getId(), s.getLevel());
			if ((sk == null) || (sk != skill) || !sk.getCanLearn(player.getClassId()) || !sk.canTeachBy(npcid))
			{
				continue;
			}
			
			counts++;
			requiredSp = s.getSpCost();
			requiredExp = s.getExp();
			rate = s.getRate(player);
			baseLevel = s.getBaseLevel();
		}
		
		if ((counts == 0) && !Config.ALT_GAME_SKILL_LEARN)
		{
			player.sendMessage("You are trying to learn skill that you can't...");
			Util.handleIllegalPlayerAction(player, player + " tried to learn skill that he can't!!!", IllegalPlayerAction.PUNISH_KICK);
			return;
		}
		
		if (player.getSp() >= requiredSp)
		{
			// Like L2OFF you can't delevel during skill enchant
			final long expAfter = player.getExp() - requiredExp;
			if ((player.getExp() >= requiredExp) && (expAfter >= ExperienceData.getInstance().getExpForLevel(player.getLevel())))
			{
				if (Config.ES_SP_BOOK_NEEDED && ((_skillLevel == 101) || (_skillLevel == 141))) // only first level requires book
				{
					final int spbId = 6622;
					final Item spb = player.getInventory().getItemByItemId(spbId);
					if (spb == null)// Haven't spellbook
					{
						player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL);
						return;
					}
					// ok
					player.destroyItem("Consume", spb, trainer, true);
				}
			}
			else
			{
				player.sendPacket(new SystemMessage(SystemMessageId.YOU_DO_NOT_HAVE_ENOUGH_EXPERIENCE_EXP_TO_ENCHANT_THAT_SKILL));
				return;
			}
		}
		else
		{
			player.sendPacket(new SystemMessage(SystemMessageId.YOU_DO_NOT_HAVE_ENOUGH_SP_TO_ENCHANT_THAT_SKILL));
			return;
		}
		if (Rnd.get(100) <= rate)
		{
			player.addSkill(skill, true);
			player.getStat().removeExpAndSp(requiredExp, requiredSp);
			
			final StatusUpdate su = new StatusUpdate(player.getObjectId());
			su.addAttribute(StatusUpdate.SP, player.getSp());
			player.sendPacket(su);
			
			final SystemMessage ep = new SystemMessage(SystemMessageId.YOUR_EXPERIENCE_HAS_DECREASED_BY_S1);
			ep.addNumber(requiredExp);
			player.sendPacket(ep);
			
			final SystemMessage sp = new SystemMessage(SystemMessageId.YOUR_SP_HAS_DECREASED_BY_S1);
			sp.addNumber(requiredSp);
			player.sendPacket(sp);
			
			final SystemMessage sm = new SystemMessage(SystemMessageId.SKILL_ENCHANT_WAS_SUCCESSFUL_S1_HAS_BEEN_ENCHANTED);
			sm.addSkillName(_skillId);
			player.sendPacket(sm);
		}
		else
		{
			if (skill.getLevel() > 100)
			{
				_skillLevel = baseLevel;
				player.addSkill(SkillTable.getInstance().getSkill(_skillId, _skillLevel), true);
				player.sendSkillList();
			}
			final SystemMessage sm = new SystemMessage(SystemMessageId.SKILL_ENCHANT_FAILED_THE_SKILL_WILL_BE_INITIALIZED);
			sm.addSkillName(_skillId);
			player.sendPacket(sm);
		}
		trainer.showEnchantSkillList(player, player.getClassId());
		player.sendPacket(new UserInfo(player));
		player.sendSkillList();
		
		// update all the shortcuts to this skill
		for (ShortCut sc : player.getAllShortCuts())
		{
			if ((sc.getId() == _skillId) && (sc.getType() == ShortCut.TYPE_SKILL))
			{
				final ShortCut newsc = new ShortCut(sc.getSlot(), sc.getPage(), sc.getType(), sc.getId(), _skillLevel);
				player.sendPacket(new ShortCutRegister(newsc));
				player.registerShortCut(newsc);
			}
		}
	}
}
