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
import org.l2jmobius.gameserver.model.ShortCut;
import org.l2jmobius.gameserver.model.Skill;
import org.l2jmobius.gameserver.model.SkillLearn;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.instance.Fisherman;
import org.l2jmobius.gameserver.model.actor.instance.Folk;
import org.l2jmobius.gameserver.model.actor.instance.VillageMaster;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.PacketLogger;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ExStorageMaxCount;
import org.l2jmobius.gameserver.network.serverpackets.PledgeSkillList;
import org.l2jmobius.gameserver.network.serverpackets.ShortCutRegister;
import org.l2jmobius.gameserver.network.serverpackets.StatusUpdate;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.util.IllegalPlayerAction;
import org.l2jmobius.gameserver.util.Util;

public class RequestAquireSkill implements IClientIncomingPacket
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
		
		final int npcid = trainer.getNpcId();
		if (!player.isInsideRadius2D(trainer, Npc.INTERACTION_DISTANCE) && !player.isGM())
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
		
		final Skill skill = SkillTable.getInstance().getSkill(_id, _level);
		int counts = 0;
		int requiredSp = 10000000;
		if (_skillType == 0)
		{
			for (SkillLearn s : SkillTreeTable.getInstance().getAvailableSkills(player, player.getSkillLearningClassId()))
			{
				final Skill sk = SkillTable.getInstance().getSkill(s.getId(), s.getLevel());
				if ((sk == null) || (sk != skill) || !sk.getCanLearn(player.getSkillLearningClassId()) || !sk.canTeachBy(npcid))
				{
					continue;
				}
				counts++;
				requiredSp = SkillTreeTable.getInstance().getSkillCost(player, skill);
			}
			
			if ((counts == 0) && !Config.ALT_GAME_SKILL_LEARN)
			{
				player.sendMessage("You are trying to learn skill that u can't..");
				Util.handleIllegalPlayerAction(player, "Player " + player.getName() + " tried to learn skill that he can't!!!", IllegalPlayerAction.PUNISH_KICK);
				return;
			}
			
			if (player.getSp() >= requiredSp)
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
					final Item spb = player.getInventory().getItemByItemId(spbId);
					if (spb == null)
					{
						// Haven't spellbook
						player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_THE_NECESSARY_MATERIALS_OR_PREREQUISITES_TO_LEARN_THIS_SKILL);
						return;
					}
					
					// ok
					player.destroyItem("Consume", spb, trainer, true);
				}
			}
			else
			{
				player.sendPacket(new SystemMessage(SystemMessageId.YOU_DO_NOT_HAVE_ENOUGH_SP_TO_LEARN_THIS_SKILL));
				return;
			}
		}
		else if (_skillType == 1)
		{
			int costid = 0;
			int costcount = 0;
			// Skill Learn bug Fix
			for (SkillLearn s : SkillTreeTable.getInstance().getAvailableSkills(player))
			{
				final Skill sk = SkillTable.getInstance().getSkill(s.getId(), s.getLevel());
				if ((sk == null) || (sk != skill))
				{
					continue;
				}
				
				counts++;
				costid = s.getIdCost();
				costcount = s.getCostCount();
				requiredSp = s.getSpCost();
			}
			
			if (counts == 0)
			{
				player.sendMessage("You are trying to learn skill that u can't..");
				Util.handleIllegalPlayerAction(player, "Player " + player.getName() + " tried to learn skill that he can't!!!", IllegalPlayerAction.PUNISH_KICK);
				return;
			}
			
			if (player.getSp() >= requiredSp)
			{
				if (!player.destroyItemByItemId("Consume", costid, costcount, trainer, false))
				{
					// Haven't spellbook
					player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_THE_NECESSARY_MATERIALS_OR_PREREQUISITES_TO_LEARN_THIS_SKILL);
					return;
				}
				
				final SystemMessage sm = new SystemMessage(SystemMessageId.S2_S1_HAS_DISAPPEARED);
				sm.addNumber(costcount);
				sm.addItemName(costid);
				player.sendPacket(sm);
			}
			else
			{
				player.sendPacket(new SystemMessage(SystemMessageId.YOU_DO_NOT_HAVE_ENOUGH_SP_TO_LEARN_THIS_SKILL));
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
			for (PledgeSkillLearn s : SkillTreeTable.getInstance().getAvailablePledgeSkills(player))
			{
				final Skill sk = SkillTable.getInstance().getSkill(s.getId(), s.getLevel());
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
						player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_THE_NECESSARY_MATERIALS_OR_PREREQUISITES_TO_LEARN_THIS_SKILL);
						return;
					}
					
					final SystemMessage sm = new SystemMessage(SystemMessageId.S2_S1_HAS_DISAPPEARED);
					sm.addItemName(itemId);
					sm.addNumber(1);
					player.sendPacket(sm);
				}
			}
			else
			{
				player.sendPacket(new SystemMessage(SystemMessageId.THE_ATTEMPT_TO_ACQUIRE_THE_SKILL_HAS_FAILED_BECAUSE_OF_AN_INSUFFICIENT_CLAN_REPUTATION_SCORE));
				return;
			}
			player.getClan().setReputationScore(player.getClan().getReputationScore() - repCost);
			player.getClan().addNewSkill(skill);
			
			final SystemMessage cr = new SystemMessage(SystemMessageId.S1_POINTS_HAVE_BEEN_DEDUCTED_FROM_THE_CLAN_S_REPUTATION_SCORE);
			cr.addNumber(repCost);
			player.sendPacket(cr);
			final SystemMessage sm = new SystemMessage(SystemMessageId.THE_CLAN_SKILL_S1_HAS_BEEN_ADDED);
			sm.addSkillName(_id);
			player.sendPacket(sm);
			
			player.getClan().broadcastToOnlineMembers(new PledgeSkillList(player.getClan()));
			for (Player member : player.getClan().getOnlineMembers())
			{
				member.sendSkillList();
			}
			
			if (trainer instanceof VillageMaster)
			{
				((VillageMaster) trainer).showPledgeSkillList(player);
			}
			
			return;
		}
		else
		{
			PacketLogger.warning("Recived Wrong Packet Data in Aquired Skill - unk1:" + _skillType);
			return;
		}
		
		player.addSkill(skill, true);
		player.setSp(player.getSp() - requiredSp);
		
		final StatusUpdate su = new StatusUpdate(player.getObjectId());
		su.addAttribute(StatusUpdate.SP, player.getSp());
		player.sendPacket(su);
		
		final SystemMessage sp = new SystemMessage(SystemMessageId.YOUR_SP_HAS_DECREASED_BY_S1);
		sp.addNumber(requiredSp);
		player.sendPacket(sp);
		
		final SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_EARNED_S1_2);
		sm.addSkillName(_id);
		player.sendPacket(sm);
		
		// update all the shortcuts to this skill
		if (_level > 1)
		{
			for (ShortCut sc : player.getAllShortCuts())
			{
				if ((sc.getId() == _id) && (sc.getType() == ShortCut.TYPE_SKILL))
				{
					final ShortCut newsc = new ShortCut(sc.getSlot(), sc.getPage(), sc.getType(), sc.getId(), _level);
					player.sendPacket(new ShortCutRegister(newsc));
					player.registerShortCut(newsc);
				}
			}
		}
		
		if (trainer instanceof Fisherman)
		{
			((Fisherman) trainer).showSkillList(player);
		}
		else
		{
			trainer.showSkillList(player, player.getSkillLearningClassId());
		}
		
		if ((_id >= 1368) && (_id <= 1372)) // if skill is expand sendpacket :)
		{
			player.sendPacket(new ExStorageMaxCount(player));
		}
		
		player.sendSkillList();
	}
}
