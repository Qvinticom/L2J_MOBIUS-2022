/*
 * Copyright (C) 2004-2015 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jserver.gameserver.network.clientpackets;

import java.util.List;

import com.l2jserver.Config;
import com.l2jserver.gameserver.data.xml.impl.SkillTreesData;
import com.l2jserver.gameserver.datatables.SkillData;
import com.l2jserver.gameserver.enums.CategoryType;
import com.l2jserver.gameserver.enums.IllegalActionPunishmentType;
import com.l2jserver.gameserver.enums.Race;
import com.l2jserver.gameserver.enums.UserInfoType;
import com.l2jserver.gameserver.model.ClanPrivilege;
import com.l2jserver.gameserver.model.L2Clan;
import com.l2jserver.gameserver.model.L2SkillLearn;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2FishermanInstance;
import com.l2jserver.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.actor.instance.L2VillageMasterInstance;
import com.l2jserver.gameserver.model.base.AcquireSkillType;
import com.l2jserver.gameserver.model.events.EventDispatcher;
import com.l2jserver.gameserver.model.events.impl.character.player.OnPlayerSkillLearn;
import com.l2jserver.gameserver.model.holders.ItemHolder;
import com.l2jserver.gameserver.model.holders.SkillHolder;
import com.l2jserver.gameserver.model.quest.QuestState;
import com.l2jserver.gameserver.model.skills.CommonSkill;
import com.l2jserver.gameserver.model.skills.Skill;
import com.l2jserver.gameserver.model.variables.PlayerVariables;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.serverpackets.AcquireSkillDone;
import com.l2jserver.gameserver.network.serverpackets.AcquireSkillList;
import com.l2jserver.gameserver.network.serverpackets.ExAcquirableSkillListByClass;
import com.l2jserver.gameserver.network.serverpackets.ExAlchemySkillList;
import com.l2jserver.gameserver.network.serverpackets.ExBasicActionList;
import com.l2jserver.gameserver.network.serverpackets.ExStorageMaxCount;
import com.l2jserver.gameserver.network.serverpackets.ItemList;
import com.l2jserver.gameserver.network.serverpackets.PledgeSkillList;
import com.l2jserver.gameserver.network.serverpackets.ShortCutInit;
import com.l2jserver.gameserver.network.serverpackets.SystemMessage;
import com.l2jserver.gameserver.network.serverpackets.UserInfo;
import com.l2jserver.gameserver.util.Util;

/**
 * Request Acquire Skill client packet implementation.
 * @author Zoey76
 */
public final class RequestAcquireSkill extends L2GameClientPacket
{
	private static final String _C__7C_REQUESTACQUIRESKILL = "[C] 7C RequestAcquireSkill";
	private static final String[] REVELATION_VAR_NAMES =
	{
		"RevelationSkill1",
		"RevelationSkill2"
	};
	
	private static final String[] DUALCLASS_REVELATION_VAR_NAMES =
	{
		"DualclassRevelationSkill1",
		"DualclassRevelationSkill2"
	};
	
	private int _id;
	private int _level;
	private AcquireSkillType _skillType;
	private int _subType;
	
	@Override
	protected void readImpl()
	{
		_id = readD();
		_level = readD();
		_skillType = AcquireSkillType.getAcquireSkillType(readD());
		if (_skillType == AcquireSkillType.SUBPLEDGE)
		{
			_subType = readD();
		}
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if ((_level < 1) || (_level > 10000) || (_id < 1) || (_id > 32000))
		{
			Util.handleIllegalPlayerAction(activeChar, "Wrong Packet Data in Aquired Skill", Config.DEFAULT_PUNISH);
			_log.warning("Recived Wrong Packet Data in Aquired Skill - id: " + _id + " level: " + _level + " for " + activeChar);
			return;
		}
		
		final L2Npc trainer = activeChar.getLastFolkNPC();
		if (!(trainer instanceof L2NpcInstance) && (_skillType != AcquireSkillType.CLASS))
		{
			return;
		}
		
		if ((_skillType != AcquireSkillType.CLASS) && !trainer.canInteract(activeChar) && !activeChar.isGM())
		{
			return;
		}
		
		final Skill skill = SkillData.getInstance().getSkill(_id, _level);
		if (skill == null)
		{
			_log.warning(RequestAcquireSkill.class.getSimpleName() + ": Player " + activeChar.getName() + " is trying to learn a null skill Id: " + _id + " level: " + _level + "!");
			return;
		}
		
		// Hack check. Doesn't apply to all Skill Types
		final int prevSkillLevel = activeChar.getSkillLevel(_id);
		if ((_skillType != AcquireSkillType.TRANSFER) && (_skillType != AcquireSkillType.SUBPLEDGE))
		{
			if (prevSkillLevel == _level)
			{
				_log.warning("Player " + activeChar.getName() + " is trying to learn a skill that already knows, Id: " + _id + " level: " + _level + "!");
				return;
			}
			
			final int tmpLv = (prevSkillLevel == -1) ? 0 : prevSkillLevel;
			if (tmpLv != (_level - 1))
			{
				// The previous level skill has not been learned.
				activeChar.sendPacket(SystemMessageId.THE_PREVIOUS_LEVEL_SKILL_HAS_NOT_BEEN_LEARNED);
				Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " is requesting skill Id: " + _id + " level " + _level + " without knowing it's previous level!", IllegalActionPunishmentType.NONE);
				return;
			}
		}
		
		final L2SkillLearn s = SkillTreesData.getInstance().getSkillLearn(_skillType, _id, _level, activeChar);
		if (s == null)
		{
			return;
		}
		
		switch (_skillType)
		{
			case CLASS:
			{
				if (checkPlayerSkill(activeChar, trainer, s))
				{
					giveSkill(activeChar, trainer, skill);
				}
				break;
			}
			case TRANSFORM:
			{
				// Hack check.
				if (!canTransform(activeChar))
				{
					activeChar.sendPacket(SystemMessageId.YOU_HAVE_NOT_COMPLETED_THE_NECESSARY_QUEST_FOR_SKILL_ACQUISITION);
					Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " is requesting skill Id: " + _id + " level " + _level + " without required quests!", IllegalActionPunishmentType.NONE);
					return;
				}
				
				if (checkPlayerSkill(activeChar, trainer, s))
				{
					giveSkill(activeChar, trainer, skill);
				}
				break;
			}
			case FISHING:
			{
				if (checkPlayerSkill(activeChar, trainer, s))
				{
					giveSkill(activeChar, trainer, skill);
				}
				break;
			}
			case PLEDGE:
			{
				if (!activeChar.isClanLeader())
				{
					return;
				}
				
				final L2Clan clan = activeChar.getClan();
				int repCost = s.getLevelUpSp();
				if (clan.getReputationScore() >= repCost)
				{
					if (Config.LIFE_CRYSTAL_NEEDED)
					{
						for (ItemHolder item : s.getRequiredItems())
						{
							if (!activeChar.destroyItemByItemId("Consume", item.getId(), item.getCount(), trainer, false))
							{
								// Doesn't have required item.
								activeChar.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_THE_NECESSARY_MATERIALS_OR_PREREQUISITES_TO_LEARN_THIS_SKILL);
								L2VillageMasterInstance.showPledgeSkillList(activeChar);
								return;
							}
							
							final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_S1_S_DISAPPEARED);
							sm.addItemName(item.getId());
							sm.addLong(item.getCount());
							activeChar.sendPacket(sm);
						}
					}
					
					clan.takeReputationScore(repCost, true);
					
					final SystemMessage cr = SystemMessage.getSystemMessage(SystemMessageId.S1_POINT_S_HAVE_BEEN_DEDUCTED_FROM_THE_CLAN_S_REPUTATION);
					cr.addInt(repCost);
					activeChar.sendPacket(cr);
					
					clan.addNewSkill(skill);
					
					clan.broadcastToOnlineMembers(new PledgeSkillList(clan));
					
					activeChar.sendPacket(new AcquireSkillDone());
					
					L2VillageMasterInstance.showPledgeSkillList(activeChar);
				}
				else
				{
					activeChar.sendPacket(SystemMessageId.THE_ATTEMPT_TO_ACQUIRE_THE_SKILL_HAS_FAILED_BECAUSE_OF_AN_INSUFFICIENT_CLAN_REPUTATION);
					L2VillageMasterInstance.showPledgeSkillList(activeChar);
				}
				break;
			}
			case SUBPLEDGE:
			{
				if (!activeChar.isClanLeader() || !activeChar.hasClanPrivilege(ClanPrivilege.CL_TROOPS_FAME))
				{
					return;
				}
				
				final L2Clan clan = activeChar.getClan();
				if ((clan.getFortId() == 0) && (clan.getCastleId() == 0))
				{
					return;
				}
				
				// Hack check. Check if SubPledge can accept the new skill:
				if (!clan.isLearnableSubPledgeSkill(skill, _subType))
				{
					activeChar.sendPacket(SystemMessageId.THIS_SQUAD_SKILL_HAS_ALREADY_BEEN_LEARNED);
					Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " is requesting skill Id: " + _id + " level " + _level + " without knowing it's previous level!", IllegalActionPunishmentType.NONE);
					return;
				}
				
				final int repCost = s.getLevelUpSp();
				if (clan.getReputationScore() < repCost)
				{
					activeChar.sendPacket(SystemMessageId.THE_ATTEMPT_TO_ACQUIRE_THE_SKILL_HAS_FAILED_BECAUSE_OF_AN_INSUFFICIENT_CLAN_REPUTATION);
					return;
				}
				
				for (ItemHolder item : s.getRequiredItems())
				{
					if (!activeChar.destroyItemByItemId("SubSkills", item.getId(), item.getCount(), trainer, false))
					{
						activeChar.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_THE_NECESSARY_MATERIALS_OR_PREREQUISITES_TO_LEARN_THIS_SKILL);
						return;
					}
					
					final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_S1_S_DISAPPEARED);
					sm.addItemName(item.getId());
					sm.addLong(item.getCount());
					activeChar.sendPacket(sm);
				}
				
				if (repCost > 0)
				{
					clan.takeReputationScore(repCost, true);
					final SystemMessage cr = SystemMessage.getSystemMessage(SystemMessageId.S1_POINT_S_HAVE_BEEN_DEDUCTED_FROM_THE_CLAN_S_REPUTATION);
					cr.addInt(repCost);
					activeChar.sendPacket(cr);
				}
				
				clan.addNewSkill(skill, _subType);
				clan.broadcastToOnlineMembers(new PledgeSkillList(clan));
				activeChar.sendPacket(new AcquireSkillDone());
				
				showSubUnitSkillList(activeChar);
				break;
			}
			case TRANSFER:
			{
				if (checkPlayerSkill(activeChar, trainer, s))
				{
					giveSkill(activeChar, trainer, skill);
				}
				
				final List<L2SkillLearn> skills = SkillTreesData.getInstance().getAvailableTransferSkills(activeChar);
				if (skills.isEmpty())
				{
					activeChar.sendPacket(SystemMessageId.THERE_ARE_NO_OTHER_SKILLS_TO_LEARN);
				}
				else
				{
					activeChar.sendPacket(new ExAcquirableSkillListByClass(skills, AcquireSkillType.TRANSFER));
				}
				break;
			}
			case SUBCLASS:
			{
				if (activeChar.isSubClassActive())
				{
					activeChar.sendPacket(SystemMessageId.THIS_SKILL_CANNOT_BE_LEARNED_WHILE_IN_THE_SUBCLASS_STATE_PLEASE_TRY_AGAIN_AFTER_CHANGING_TO_THE_MAIN_CLASS);
					Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " is requesting skill Id: " + _id + " level " + _level + " while Sub-Class is active!", IllegalActionPunishmentType.NONE);
					return;
				}
				
				if (checkPlayerSkill(activeChar, trainer, s))
				{
					final PlayerVariables vars = activeChar.getVariables();
					String list = vars.getString("SubSkillList", "");
					if ((prevSkillLevel > 0) && list.contains(_id + "-" + prevSkillLevel))
					{
						list = list.replace(_id + "-" + prevSkillLevel, _id + "-" + _level);
					}
					else
					{
						if (!list.isEmpty())
						{
							list += ";";
						}
						list += _id + "-" + _level;
					}
					vars.set("SubSkillList", list);
					giveSkill(activeChar, trainer, skill, false);
				}
				break;
			}
			case DUALCLASS:
			{
				if (activeChar.isSubClassActive())
				{
					activeChar.sendPacket(SystemMessageId.THIS_SKILL_CANNOT_BE_LEARNED_WHILE_IN_THE_SUBCLASS_STATE_PLEASE_TRY_AGAIN_AFTER_CHANGING_TO_THE_MAIN_CLASS);
					Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " is requesting skill Id: " + _id + " level " + _level + " while Sub-Class is active!", IllegalActionPunishmentType.NONE);
					return;
				}
				
				if (checkPlayerSkill(activeChar, trainer, s))
				{
					final PlayerVariables vars = activeChar.getVariables();
					String list = vars.getString("DualSkillList", "");
					if ((prevSkillLevel > 0) && list.contains(_id + "-" + prevSkillLevel))
					{
						list = list.replace(_id + "-" + prevSkillLevel, _id + "-" + _level);
					}
					else
					{
						if (!list.isEmpty())
						{
							list += ";";
						}
						list += _id + "-" + _level;
					}
					vars.set("DualSkillList", list);
					giveSkill(activeChar, trainer, skill, false);
				}
				break;
			}
			case COLLECT:
			{
				if (checkPlayerSkill(activeChar, trainer, s))
				{
					giveSkill(activeChar, trainer, skill);
				}
				break;
			}
			case ALCHEMY:
			{
				if (activeChar.getRace() != Race.ERTHEIA)
				{
					return;
				}
				
				if (checkPlayerSkill(activeChar, trainer, s))
				{
					giveSkill(activeChar, trainer, skill);
					
					activeChar.sendPacket(new AcquireSkillDone());
					activeChar.sendPacket(new ExAlchemySkillList(activeChar));
					
					final List<L2SkillLearn> alchemySkills = SkillTreesData.getInstance().getAvailableAlchemySkills(activeChar);
					
					if (alchemySkills.isEmpty())
					{
						activeChar.sendPacket(SystemMessageId.THERE_ARE_NO_OTHER_SKILLS_TO_LEARN);
					}
					else
					{
						activeChar.sendPacket(new ExAcquirableSkillListByClass(alchemySkills, AcquireSkillType.ALCHEMY));
					}
				}
				
				break;
			}
			case REVELATION:
			{
				if (activeChar.isSubClassActive())
				{
					activeChar.sendPacket(SystemMessageId.THIS_SKILL_CANNOT_BE_LEARNED_WHILE_IN_THE_SUBCLASS_STATE_PLEASE_TRY_AGAIN_AFTER_CHANGING_TO_THE_MAIN_CLASS);
					Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " is requesting skill Id: " + _id + " level " + _level + " while Sub-Class is active!", IllegalActionPunishmentType.NONE);
					return;
				}
				if ((activeChar.getLevel() < 85) || !activeChar.isInCategory(CategoryType.AWAKEN_GROUP))
				{
					activeChar.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_THE_NECESSARY_MATERIALS_OR_PREREQUISITES_TO_LEARN_THIS_SKILL);
					Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " is requesting skill Id: " + _id + " level " + _level + " while not being level 85 or awaken!", IllegalActionPunishmentType.NONE);
					return;
				}
				
				int count = 0;
				
				for (String varName : REVELATION_VAR_NAMES)
				{
					if (activeChar.getVariables().getInt(varName, 0) > 0)
					{
						count++;
					}
				}
				
				if (count >= 2)
				{
					activeChar.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_THE_NECESSARY_MATERIALS_OR_PREREQUISITES_TO_LEARN_THIS_SKILL);
					Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " is requesting skill Id: " + _id + " level " + _level + " while having already learned 2 skills!", IllegalActionPunishmentType.NONE);
					return;
				}
				
				if (checkPlayerSkill(activeChar, trainer, s))
				{
					final String varName = count == 0 ? REVELATION_VAR_NAMES[0] : REVELATION_VAR_NAMES[1];
					
					activeChar.getVariables().set(varName, skill.getId());
					
					giveSkill(activeChar, trainer, skill);
				}
				break;
			}
			case REVELATION_DUALCLASS:
			{
				if (activeChar.isSubClassActive() && !activeChar.isDualClassActive())
				{
					activeChar.sendPacket(SystemMessageId.THIS_SKILL_CANNOT_BE_LEARNED_WHILE_IN_THE_SUBCLASS_STATE_PLEASE_TRY_AGAIN_AFTER_CHANGING_TO_THE_MAIN_CLASS);
					Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " is requesting skill Id: " + _id + " level " + _level + " while Sub-Class is active!", IllegalActionPunishmentType.NONE);
					return;
				}
				
				if ((activeChar.getLevel() < 85) || !activeChar.isInCategory(CategoryType.AWAKEN_GROUP))
				{
					activeChar.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_THE_NECESSARY_MATERIALS_OR_PREREQUISITES_TO_LEARN_THIS_SKILL);
					Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " is requesting skill Id: " + _id + " level " + _level + " while not being level 85 or awaken!", IllegalActionPunishmentType.NONE);
					return;
				}
				
				int count = 0;
				
				for (String varName : DUALCLASS_REVELATION_VAR_NAMES)
				{
					if (activeChar.getVariables().getInt(varName, 0) > 0)
					{
						count++;
					}
				}
				
				if (count >= 2)
				{
					activeChar.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_THE_NECESSARY_MATERIALS_OR_PREREQUISITES_TO_LEARN_THIS_SKILL);
					Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " is requesting skill Id: " + _id + " level " + _level + " while having already learned 2 skills!", IllegalActionPunishmentType.NONE);
					return;
				}
				
				if (checkPlayerSkill(activeChar, trainer, s))
				{
					final String varName = count == 0 ? DUALCLASS_REVELATION_VAR_NAMES[0] : DUALCLASS_REVELATION_VAR_NAMES[1];
					
					activeChar.getVariables().set(varName, skill.getId());
					
					giveSkill(activeChar, trainer, skill);
				}
				break;
			}
			default:
			{
				_log.warning("Recived Wrong Packet Data in Aquired Skill, unknown skill type:" + _skillType);
				break;
			}
		}
	}
	
	public static void showSubUnitSkillList(L2PcInstance activeChar)
	{
		final List<L2SkillLearn> skills = SkillTreesData.getInstance().getAvailableSubPledgeSkills(activeChar.getClan());
		
		if (skills.isEmpty())
		{
			activeChar.sendPacket(SystemMessageId.THERE_ARE_NO_OTHER_SKILLS_TO_LEARN);
		}
		else
		{
			activeChar.sendPacket(new ExAcquirableSkillListByClass(skills, AcquireSkillType.SUBPLEDGE));
		}
	}
	
	public final static void showSubSkillList(L2PcInstance activeChar)
	{
		final List<L2SkillLearn> skills = SkillTreesData.getInstance().getAvailableSubClassSkills(activeChar);
		if (!skills.isEmpty())
		{
			activeChar.sendPacket(new ExAcquirableSkillListByClass(skills, AcquireSkillType.SUBCLASS));
		}
		else
		{
			activeChar.sendPacket(SystemMessageId.THERE_ARE_NO_OTHER_SKILLS_TO_LEARN);
		}
	}
	
	public final static void showDualSkillList(L2PcInstance activeChar)
	{
		final List<L2SkillLearn> skills = SkillTreesData.getInstance().getAvailableDualClassSkills(activeChar);
		if (!skills.isEmpty())
		{
			activeChar.sendPacket(new ExAcquirableSkillListByClass(skills, AcquireSkillType.DUALCLASS));
		}
		else
		{
			activeChar.sendPacket(SystemMessageId.THERE_ARE_NO_OTHER_SKILLS_TO_LEARN);
		}
	}
	
	/**
	 * Perform a simple check for current player and skill.<br>
	 * Takes the needed SP if the skill require it and all requirements are meet.<br>
	 * Consume required items if the skill require it and all requirements are meet.<br>
	 * @param player the skill learning player.
	 * @param trainer the skills teaching Npc.
	 * @param s the skill to be learn.
	 * @return {@code true} if all requirements are meet, {@code false} otherwise.
	 */
	private boolean checkPlayerSkill(L2PcInstance player, L2Npc trainer, L2SkillLearn s)
	{
		if (s != null)
		{
			if ((s.getSkillId() == _id) && (s.getSkillLevel() == _level))
			{
				// Hack check.
				if (s.getGetLevel() > player.getLevel())
				{
					player.sendPacket(SystemMessageId.YOU_DO_NOT_MEET_THE_SKILL_LEVEL_REQUIREMENTS);
					Util.handleIllegalPlayerAction(player, "Player " + player.getName() + ", level " + player.getLevel() + " is requesting skill Id: " + _id + " level " + _level + " without having minimum required level, " + s.getGetLevel() + "!", IllegalActionPunishmentType.NONE);
					return false;
				}
				
				// First it checks that the skill require SP and the player has enough SP to learn it.
				final int levelUpSp = s.getCalculatedLevelUpSp(player.getClassId(), player.getLearningClass());
				if ((levelUpSp > 0) && (levelUpSp > player.getSp()))
				{
					player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ENOUGH_SP_TO_LEARN_THIS_SKILL);
					showSkillList(trainer, player);
					return false;
				}
				
				if (!Config.DIVINE_SP_BOOK_NEEDED && (_id == CommonSkill.DIVINE_INSPIRATION.getId()))
				{
					return true;
				}
				
				// Check for required skills.
				if (!s.getPreReqSkills().isEmpty())
				{
					for (SkillHolder skill : s.getPreReqSkills())
					{
						if (player.getSkillLevel(skill.getSkillId()) != skill.getSkillLvl())
						{
							if (skill.getSkillId() == CommonSkill.ONYX_BEAST_TRANSFORMATION.getId())
							{
								player.sendPacket(SystemMessageId.YOU_MUST_LEARN_THE_ONYX_BEAST_SKILL_BEFORE_YOU_CAN_LEARN_FURTHER_SKILLS);
							}
							else
							{
								player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_THE_NECESSARY_MATERIALS_OR_PREREQUISITES_TO_LEARN_THIS_SKILL);
							}
							return false;
						}
					}
				}
				
				// Check for required items.
				if (!s.getRequiredItems().isEmpty())
				{
					// Then checks that the player has all the items
					long reqItemCount = 0;
					for (ItemHolder item : s.getRequiredItems())
					{
						reqItemCount = player.getInventory().getInventoryItemCount(item.getId(), -1);
						if (reqItemCount < item.getCount())
						{
							// Player doesn't have required item.
							player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_THE_NECESSARY_MATERIALS_OR_PREREQUISITES_TO_LEARN_THIS_SKILL);
							showSkillList(trainer, player);
							return false;
						}
					}
					// If the player has all required items, they are consumed.
					for (ItemHolder itemIdCount : s.getRequiredItems())
					{
						if (!player.destroyItemByItemId("SkillLearn", itemIdCount.getId(), itemIdCount.getCount(), trainer, true))
						{
							Util.handleIllegalPlayerAction(player, "Somehow player " + player.getName() + ", level " + player.getLevel() + " lose required item Id: " + itemIdCount.getId() + " to learn skill while learning skill Id: " + _id + " level " + _level + "!", IllegalActionPunishmentType.NONE);
						}
					}
				}
				
				if (!s.getRemoveSkills().isEmpty())
				{
					s.getRemoveSkills().forEach(skillId ->
					{
						if (player.getSkillLevel(skillId) > 0)
						{
							final Skill skillToRemove = player.getKnownSkill(skillId);
							if (skillToRemove != null)
							{
								player.removeSkill(skillToRemove, true);
							}
						}
					});
				}
				
				// If the player has SP and all required items then consume SP.
				if (levelUpSp > 0)
				{
					player.setSp(player.getSp() - levelUpSp);
					UserInfo ui = new UserInfo(player);
					ui.addComponentType(UserInfoType.CURRENT_HPMPCP_EXP_SP);
					player.sendPacket(ui);
				}
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Add the skill to the player and makes proper updates.
	 * @param player the player acquiring a skill.
	 * @param trainer the Npc teaching a skill.
	 * @param skill the skill to be learn.
	 */
	private void giveSkill(L2PcInstance player, L2Npc trainer, Skill skill)
	{
		giveSkill(player, trainer, skill, true);
	}
	
	/**
	 * Add the skill to the player and makes proper updates.
	 * @param player the player acquiring a skill.
	 * @param trainer the Npc teaching a skill.
	 * @param skill the skill to be learn.
	 * @param store
	 */
	private void giveSkill(L2PcInstance player, L2Npc trainer, Skill skill, boolean store)
	{
		// Send message.
		final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_EARNED_S12);
		sm.addSkillName(skill);
		player.sendPacket(sm);
		
		player.addSkill(skill, store);
		
		player.sendPacket(new ItemList(player, false));
		player.sendPacket(new ShortCutInit(player));
		player.sendPacket(new ExBasicActionList(ExBasicActionList.DEFAULT_ACTION_LIST));
		player.sendSkillList(skill.getId());
		
		player.updateShortCuts(_id, _level);
		showSkillList(trainer, player);
		
		// If skill is expand type then sends packet:
		if ((_id >= 1368) && (_id <= 1372))
		{
			player.sendPacket(new ExStorageMaxCount(player));
		}
		
		// Notify scripts of the skill learn.
		if (trainer != null)
		{
			EventDispatcher.getInstance().notifyEventAsync(new OnPlayerSkillLearn(trainer, player, skill, _skillType), trainer);
		}
		else
		{
			EventDispatcher.getInstance().notifyEventAsync(new OnPlayerSkillLearn(trainer, player, skill, _skillType), player);
		}
	}
	
	/**
	 * Wrapper for returning the skill list to the player after it's done with current skill.
	 * @param trainer the Npc which the {@code player} is interacting
	 * @param player the active character
	 */
	private void showSkillList(L2Npc trainer, L2PcInstance player)
	{
		if ((_skillType == AcquireSkillType.TRANSFORM) || (_skillType == AcquireSkillType.TRANSFER))
		{
			// Managed in Datapack.
			return;
		}
		
		if (_skillType == AcquireSkillType.SUBCLASS)
		{
			showSubSkillList(player);
		}
		else if (_skillType == AcquireSkillType.DUALCLASS)
		{
			showDualSkillList(player);
		}
		else if (trainer instanceof L2FishermanInstance)
		{
			L2FishermanInstance.showFishSkillList(player);
		}
		else
		{
			player.sendPacket(new AcquireSkillList(player));
		}
	}
	
	/**
	 * Verify if the player can transform.
	 * @param player the player to verify
	 * @return {@code true} if the player meets the required conditions to learn a transformation, {@code false} otherwise
	 */
	public static boolean canTransform(L2PcInstance player)
	{
		if (Config.ALLOW_TRANSFORM_WITHOUT_QUEST)
		{
			return true;
		}
		final QuestState st = player.getQuestState("Q00136_MoreThanMeetsTheEye");
		return (st != null) && st.isCompleted();
	}
	
	@Override
	public String getType()
	{
		return _C__7C_REQUESTACQUIRESKILL;
	}
}
