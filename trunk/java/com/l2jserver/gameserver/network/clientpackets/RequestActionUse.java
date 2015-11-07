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

import java.util.Arrays;

import com.l2jserver.Config;
import com.l2jserver.gameserver.ai.CtrlEvent;
import com.l2jserver.gameserver.ai.CtrlIntention;
import com.l2jserver.gameserver.ai.L2SummonAI;
import com.l2jserver.gameserver.ai.NextAction;
import com.l2jserver.gameserver.data.sql.impl.SummonSkillsTable;
import com.l2jserver.gameserver.data.xml.impl.PetDataTable;
import com.l2jserver.gameserver.datatables.BotReportTable;
import com.l2jserver.gameserver.datatables.SkillData;
import com.l2jserver.gameserver.enums.ChatType;
import com.l2jserver.gameserver.enums.MountType;
import com.l2jserver.gameserver.enums.PrivateStoreType;
import com.l2jserver.gameserver.instancemanager.AirShipManager;
import com.l2jserver.gameserver.model.L2Object;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.L2Summon;
import com.l2jserver.gameserver.model.actor.instance.L2BabyPetInstance;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.actor.instance.L2PetInstance;
import com.l2jserver.gameserver.model.actor.instance.L2SiegeFlagInstance;
import com.l2jserver.gameserver.model.actor.instance.L2StaticObjectInstance;
import com.l2jserver.gameserver.model.effects.AbstractEffect;
import com.l2jserver.gameserver.model.effects.L2EffectType;
import com.l2jserver.gameserver.model.skills.AbnormalType;
import com.l2jserver.gameserver.model.skills.BuffInfo;
import com.l2jserver.gameserver.network.NpcStringId;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.serverpackets.ActionFailed;
import com.l2jserver.gameserver.network.serverpackets.ChairSit;
import com.l2jserver.gameserver.network.serverpackets.ExAskCoupleAction;
import com.l2jserver.gameserver.network.serverpackets.ExBasicActionList;
import com.l2jserver.gameserver.network.serverpackets.ExInzoneWaiting;
import com.l2jserver.gameserver.network.serverpackets.NpcSay;
import com.l2jserver.gameserver.network.serverpackets.RecipeShopManageList;
import com.l2jserver.gameserver.network.serverpackets.SocialAction;
import com.l2jserver.gameserver.network.serverpackets.SystemMessage;
import com.l2jserver.gameserver.taskmanager.AttackStanceTaskManager;
import com.l2jserver.util.Rnd;

/**
 * This class manages the action use request packet.
 * @author Zoey76
 */
public final class RequestActionUse extends L2GameClientPacket
{
	private static final String _C__56_REQUESTACTIONUSE = "[C] 56 RequestActionUse";
	
	private static final int SIN_EATER_ID = 12564;
	private static final int SWITCH_STANCE_ID = 6054;
	private static final NpcStringId[] NPC_STRINGS =
	{
		NpcStringId.USING_A_SPECIAL_SKILL_HERE_COULD_TRIGGER_A_BLOODBATH,
		NpcStringId.HEY_WHAT_DO_YOU_EXPECT_OF_ME,
		NpcStringId.UGGGGGH_PUSH_IT_S_NOT_COMING_OUT,
		NpcStringId.AH_I_MISSED_THE_MARK
	};
	
	private int _actionId;
	private boolean _ctrlPressed;
	private boolean _shiftPressed;
	
	@Override
	protected void readImpl()
	{
		_actionId = readD();
		_ctrlPressed = (readD() == 1);
		_shiftPressed = (readC() == 1);
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if (Config.DEBUG)
		{
			_log.info(getType() + ": " + activeChar + " requested action use ID: " + _actionId + " Ctrl pressed:" + _ctrlPressed + " Shift pressed:" + _shiftPressed);
		}
		
		// Don't do anything if player is dead or confused
		if ((activeChar.isFakeDeath() && (_actionId != 0)) || activeChar.isDead() || activeChar.isOutOfControl())
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final BuffInfo info = activeChar.getEffectList().getBuffInfoByAbnormalType(AbnormalType.BOT_PENALTY);
		if (info != null)
		{
			for (AbstractEffect effect : info.getEffects())
			{
				if (!effect.checkCondition(_actionId))
				{
					activeChar.sendPacket(SystemMessageId.YOU_HAVE_BEEN_REPORTED_AS_AN_ILLEGAL_PROGRAM_USER_SO_YOUR_ACTIONS_HAVE_BEEN_RESTRICTED);
					activeChar.sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
			}
		}
		
		// Don't allow to do some action if player is transformed
		if (activeChar.isTransformed())
		{
			int[] allowedActions = activeChar.isTransformed() ? ExBasicActionList.ACTIONS_ON_TRANSFORM : ExBasicActionList.DEFAULT_ACTION_LIST;
			if (!(Arrays.binarySearch(allowedActions, _actionId) >= 0))
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				_log.warning("Player " + activeChar + " used action which he does not have! Id = " + _actionId + " transform: " + activeChar.getTransformation());
				return;
			}
		}
		
		final L2Summon pet = activeChar.getPet();
		final L2Summon servitor = activeChar.getAnyServitor();
		final L2Object target = activeChar.getTarget();
		switch (_actionId)
		{
			case 0: // Sit/Stand
				if (activeChar.isSitting() || !activeChar.isMoving() || activeChar.isFakeDeath())
				{
					useSit(activeChar, target);
				}
				else
				{
					// Sit when arrive using next action.
					// Creating next action class.
					final NextAction nextAction = new NextAction(CtrlEvent.EVT_ARRIVED, CtrlIntention.AI_INTENTION_MOVE_TO, () -> useSit(activeChar, target));
					
					// Binding next action to AI.
					activeChar.getAI().setNextAction(nextAction);
				}
				break;
			case 1: // Walk/Run
				if (activeChar.isRunning())
				{
					activeChar.setWalking();
				}
				else
				{
					activeChar.setRunning();
				}
				break;
			case 10: // Private Store - Sell
				activeChar.tryOpenPrivateSellStore(false);
				break;
			case 15: // Change Movement Mode (Pets)
				if (validateSummon(pet, true))
				{
					((L2SummonAI) pet.getAI()).notifyFollowStatusChange();
				}
				break;
			case 16: // Attack (Pets)
				if (validateSummon(pet, true))
				{
					if (pet.canAttack(_ctrlPressed))
					{
						pet.doAttack();
					}
				}
				break;
			case 17: // Stop (Pets)
				if (validateSummon(pet, true))
				{
					pet.cancelAction();
				}
				break;
			case 19: // Unsummon Pet
				
				if (!validateSummon(pet, true))
				{
					break;
				}
				
				if (pet.isDead())
				{
					sendPacket(SystemMessageId.DEAD_PETS_CANNOT_BE_RETURNED_TO_THEIR_SUMMONING_ITEM);
					break;
				}
				
				if (pet.isAttackingNow() || pet.isInCombat() || pet.isMovementDisabled())
				{
					sendPacket(SystemMessageId.A_PET_CANNOT_BE_UNSUMMONED_DURING_BATTLE);
					break;
				}
				
				if (pet.isHungry())
				{
					if (!((L2PetInstance) pet).getPetData().getFood().isEmpty())
					{
						sendPacket(SystemMessageId.YOU_MAY_NOT_RESTORE_A_HUNGRY_PET);
					}
					else
					{
						sendPacket(SystemMessageId.THE_MINION_PET_CANNOT_BE_RETURNED_BECAUSE_THERE_IS_NOT_MUCH_TIME_REMAINING_UNTIL_IT_LEAVES);
					}
					break;
				}
				
				pet.unSummon(activeChar);
				break;
			case 21: // Change Movement Mode (Servitors)
				if (validateSummon(servitor, false))
				{
					((L2SummonAI) servitor.getAI()).notifyFollowStatusChange();
				}
				break;
			case 22: // Attack (Servitors)
				if (validateSummon(servitor, false))
				{
					if (servitor.canAttack(_ctrlPressed))
					{
						servitor.doAttack();
					}
				}
				break;
			case 23: // Stop (Servitors)
				if (validateSummon(servitor, false))
				{
					servitor.cancelAction();
				}
				break;
			case 28: // Private Store - Buy
				activeChar.tryOpenPrivateBuyStore();
				break;
			case 32: // Wild Hog Cannon - Wild Cannon
				useSkill(4230, false);
				break;
			case 36: // Soulless - Toxic Smoke
				useSkill(4259, false);
				break;
			case 37: // Dwarven Manufacture
				if (activeChar.isAlikeDead())
				{
					sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
				if (activeChar.getPrivateStoreType() != PrivateStoreType.NONE)
				{
					activeChar.setPrivateStoreType(PrivateStoreType.NONE);
					activeChar.broadcastUserInfo();
				}
				if (activeChar.isSitting())
				{
					activeChar.standUp();
				}
				
				sendPacket(new RecipeShopManageList(activeChar, true));
				break;
			case 38: // Mount/Dismount
				activeChar.mountPlayer(pet);
				break;
			case 39: // Soulless - Parasite Burst
				useSkill(4138, false);
				break;
			case 41: // Wild Hog Cannon - Attack
				if (validateSummon(servitor, false))
				{
					if ((target != null) && (target.isDoor() || (target instanceof L2SiegeFlagInstance)))
					{
						useSkill(4230, false);
					}
					else
					{
						sendPacket(SystemMessageId.INVALID_TARGET);
					}
				}
				break;
			case 42: // Kai the Cat - Self Damage Shield
				useSkill(4378, activeChar, false);
				break;
			case 43: // Unicorn Merrow - Hydro Screw
				useSkill(4137, false);
				break;
			case 44: // Big Boom - Boom Attack
				useSkill(4139, false);
				break;
			case 45: // Unicorn Boxer - Master Recharge
				useSkill(4025, activeChar, false);
				break;
			case 46: // Mew the Cat - Mega Storm Strike
				useSkill(4261, false);
				break;
			case 47: // Silhouette - Steal Blood
				useSkill(4260, false);
				break;
			case 48: // Mechanic Golem - Mech. Cannon
				useSkill(4068, false);
				break;
			case 51: // General Manufacture
				// Player shouldn't be able to set stores if he/she is alike dead (dead or fake death)
				if (activeChar.isAlikeDead())
				{
					sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
				if (activeChar.getPrivateStoreType() != PrivateStoreType.NONE)
				{
					activeChar.setPrivateStoreType(PrivateStoreType.NONE);
					activeChar.broadcastUserInfo();
				}
				if (activeChar.isSitting())
				{
					activeChar.standUp();
				}
				
				sendPacket(new RecipeShopManageList(activeChar, false));
				break;
			case 52: // Unsummon Servitor
				if (validateSummon(servitor, false))
				{
					if (servitor.isAttackingNow() || servitor.isInCombat())
					{
						sendPacket(SystemMessageId.A_SERVITOR_WHOM_IS_ENGAGED_IN_BATTLE_CANNOT_BE_DE_ACTIVATED);
						break;
					}
					servitor.unSummon(activeChar);
				}
				break;
			case 53: // Move to target (Servitors)
				if (validateSummon(servitor, false))
				{
					if ((target != null) && (servitor != target) && !servitor.isMovementDisabled())
					{
						servitor.setFollowStatus(false);
						servitor.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, target.getLocation());
					}
				}
				break;
			case 54: // Move to target (Pets)
				if (validateSummon(pet, true))
				{
					if ((target != null) && (pet != target) && !pet.isMovementDisabled())
					{
						pet.setFollowStatus(false);
						pet.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, target.getLocation());
					}
				}
				break;
			case 61: // Private Store Package Sell
				activeChar.tryOpenPrivateSellStore(true);
				break;
			case 65: // Bot Report Button
				if (Config.BOTREPORT_ENABLE)
				{
					BotReportTable.getInstance().reportBot(activeChar);
				}
				else
				{
					activeChar.sendMessage("This feature is disabled.");
				}
				break;
			case 67: // Steer
				if (activeChar.isInAirShip())
				{
					if (activeChar.getAirShip().setCaptain(activeChar))
					{
						activeChar.broadcastUserInfo();
					}
				}
				break;
			case 68: // Cancel Control
				if (activeChar.isInAirShip() && activeChar.getAirShip().isCaptain(activeChar))
				{
					if (activeChar.getAirShip().setCaptain(null))
					{
						activeChar.broadcastUserInfo();
					}
				}
				break;
			case 69: // Destination Map
				AirShipManager.getInstance().sendAirShipTeleportList(activeChar);
				break;
			case 70: // Exit Airship
				if (activeChar.isInAirShip())
				{
					if (activeChar.getAirShip().isCaptain(activeChar))
					{
						if (activeChar.getAirShip().setCaptain(null))
						{
							activeChar.broadcastUserInfo();
						}
					}
					else if (activeChar.getAirShip().isInDock())
					{
						activeChar.getAirShip().oustPlayer(activeChar);
					}
				}
				break;
			case 71:
			case 72:
			case 73:
				useCoupleSocial(_actionId - 55);
				break;
			case 78:
			case 79:
			case 80:
			case 81:
				if ((activeChar.getParty() != null) && (activeChar.getTarget() != null) && (activeChar.getTarget().isCharacter()))
				{
					activeChar.getParty().addTacticalSign(_actionId - 77, (L2Character) activeChar.getTarget());
				}
				else
				{
					sendPacket(ActionFailed.STATIC_PACKET);
				}
				break;
			case 82:
			case 83:
			case 84:
			case 85:
				if (activeChar.getParty() != null)
				{
					activeChar.getParty().setTargetBasedOnTacticalSignId(activeChar, _actionId - 81);
				}
				else
				{
					sendPacket(ActionFailed.STATIC_PACKET);
				}
				break;
			case 90: // /instancedzone action since Lindvior
				activeChar.sendPacket(new ExInzoneWaiting(activeChar));
				break;
			case 1000: // Siege Golem - Siege Hammer
				if ((target != null) && target.isDoor())
				{
					useSkill(4079, false);
				}
				break;
			case 1001: // Sin Eater - Ultimate Bombastic Buster
				if (validateSummon(pet, true) && (pet.getId() == SIN_EATER_ID))
				{
					pet.broadcastPacket(new NpcSay(pet.getObjectId(), ChatType.NPC_GENERAL, pet.getId(), NPC_STRINGS[Rnd.get(NPC_STRINGS.length)]));
				}
				break;
			case 1003: // Wind Hatchling/Strider - Wild Stun
				useSkill(4710, true);
				break;
			case 1004: // Wind Hatchling/Strider - Wild Defense
				useSkill(4711, activeChar, true);
				break;
			case 1005: // Star Hatchling/Strider - Bright Burst
				useSkill(4712, true);
				break;
			case 1006: // Star Hatchling/Strider - Bright Heal
				useSkill(4713, activeChar, true);
				break;
			case 1007: // Cat Queen - Blessing of Queen
				useSkill(4699, activeChar, false);
				break;
			case 1008: // Cat Queen - Gift of Queen
				useSkill(4700, activeChar, false);
				break;
			case 1009: // Cat Queen - Cure of Queen
				useSkill(4701, false);
				break;
			case 1010: // Unicorn Seraphim - Blessing of Seraphim
				useSkill(4702, activeChar, false);
				break;
			case 1011: // Unicorn Seraphim - Gift of Seraphim
				useSkill(4703, activeChar, false);
				break;
			case 1012: // Unicorn Seraphim - Cure of Seraphim
				useSkill(4704, false);
				break;
			case 1013: // Nightshade - Curse of Shade
				useSkill(4705, false);
				break;
			case 1014: // Nightshade - Mass Curse of Shade
				useSkill(4706, false);
				break;
			case 1015: // Nightshade - Shade Sacrifice
				useSkill(4707, false);
				break;
			case 1016: // Cursed Man - Cursed Blow
				useSkill(4709, false);
				break;
			case 1017: // Cursed Man - Cursed Strike/Stun
				useSkill(4708, false);
				break;
			case 1031: // Feline King - Slash
				useSkill(5135, false);
				break;
			case 1032: // Feline King - Spinning Slash
				useSkill(5136, false);
				break;
			case 1033: // Feline King - Grip of the Cat
				useSkill(5137, false);
				break;
			case 1034: // Magnus the Unicorn - Whiplash
				useSkill(5138, false);
				break;
			case 1035: // Magnus the Unicorn - Tridal Wave
				useSkill(5139, false);
				break;
			case 1036: // Spectral Lord - Corpse Kaboom
				useSkill(5142, false);
				break;
			case 1037: // Spectral Lord - Dicing Death
				useSkill(5141, false);
				break;
			case 1038: // Spectral Lord - Force Curse
				useSkill(5140, false);
				break;
			case 1039: // Swoop Cannon - Cannon Fodder
				if ((target != null) && target.isDoor())
				{
					useSkill(5110, false);
				}
				break;
			case 1040: // Swoop Cannon - Big Bang
				if ((target != null) && target.isDoor())
				{
					useSkill(5111, false);
				}
				break;
			case 1041: // Great Wolf - Bite Attack
				useSkill(5442, true);
				break;
			case 1042: // Great Wolf - Maul
				useSkill(5444, true);
				break;
			case 1043: // Great Wolf - Cry of the Wolf
				useSkill(5443, true);
				break;
			case 1044: // Great Wolf - Awakening
				useSkill(5445, true);
				break;
			case 1045: // Great Wolf - Howl
				useSkill(5584, true);
				break;
			case 1046: // Strider - Roar
				useSkill(5585, true);
				break;
			case 1047: // Divine Beast - Bite
				useSkill(5580, false);
				break;
			case 1048: // Divine Beast - Stun Attack
				useSkill(5581, false);
				break;
			case 1049: // Divine Beast - Fire Breath
				useSkill(5582, false);
				break;
			case 1050: // Divine Beast - Roar
				useSkill(5583, false);
				break;
			case 1051: // Feline Queen - Bless The Body
				useSkill(5638, false);
				break;
			case 1052: // Feline Queen - Bless The Soul
				useSkill(5639, false);
				break;
			case 1053: // Feline Queen - Haste
				useSkill(5640, false);
				break;
			case 1054: // Unicorn Seraphim - Acumen
				useSkill(5643, false);
				break;
			case 1055: // Unicorn Seraphim - Clarity
				useSkill(5647, false);
				break;
			case 1056: // Unicorn Seraphim - Empower
				useSkill(5648, false);
				break;
			case 1057: // Unicorn Seraphim - Wild Magic
				useSkill(5646, false);
				break;
			case 1058: // Nightshade - Death Whisper
				useSkill(5652, false);
				break;
			case 1059: // Nightshade - Focus
				useSkill(5653, false);
				break;
			case 1060: // Nightshade - Guidance
				useSkill(5654, false);
				break;
			case 1061: // Wild Beast Fighter, White Weasel - Death blow
				useSkill(5745, true);
				break;
			case 1062: // Wild Beast Fighter - Double attack
				useSkill(5746, true);
				break;
			case 1063: // Wild Beast Fighter - Spin attack
				useSkill(5747, true);
				break;
			case 1064: // Wild Beast Fighter - Meteor Shower
				useSkill(5748, true);
				break;
			case 1065: // Fox Shaman, Wild Beast Fighter, White Weasel, Fairy Princess - Awakening
				useSkill(5753, true);
				break;
			case 1066: // Fox Shaman, Spirit Shaman - Thunder Bolt
				useSkill(5749, true);
				break;
			case 1067: // Fox Shaman, Spirit Shaman - Flash
				useSkill(5750, true);
				break;
			case 1068: // Fox Shaman, Spirit Shaman - Lightning Wave
				useSkill(5751, true);
				break;
			case 1069: // Fox Shaman, Fairy Princess - Flare
				useSkill(5752, true);
				break;
			case 1070: // White Weasel, Fairy Princess, Improved Baby Buffalo, Improved Baby Kookaburra, Improved Baby Cougar, Spirit Shaman, Toy Knight, Turtle Ascetic - Buff control
				useSkill(5771, true);
				break;
			case 1071: // Tigress - Power Strike
				useSkill(5761, true);
				break;
			case 1072: // Toy Knight - Piercing attack
				useSkill(6046, true);
				break;
			case 1073: // Toy Knight - Whirlwind
				useSkill(6047, true);
				break;
			case 1074: // Toy Knight - Lance Smash
				useSkill(6048, true);
				break;
			case 1075: // Toy Knight - Battle Cry
				useSkill(6049, true);
				break;
			case 1076: // Turtle Ascetic - Power Smash
				useSkill(6050, true);
				break;
			case 1077: // Turtle Ascetic - Energy Burst
				useSkill(6051, true);
				break;
			case 1078: // Turtle Ascetic - Shockwave
				useSkill(6052, true);
				break;
			case 1079: // Turtle Ascetic - Howl
				useSkill(6053, true);
				break;
			case 1080: // Phoenix Rush
				useSkill(6041, false);
				break;
			case 1081: // Phoenix Cleanse
				useSkill(6042, false);
				break;
			case 1082: // Phoenix Flame Feather
				useSkill(6043, false);
				break;
			case 1083: // Phoenix Flame Beak
				useSkill(6044, false);
				break;
			case 1084: // Switch State
				if (pet instanceof L2BabyPetInstance)
				{
					useSkill(SWITCH_STANCE_ID, true);
				}
				break;
			case 1086: // Panther Cancel
				useSkill(6094, false);
				break;
			case 1087: // Panther Dark Claw
				useSkill(6095, false);
				break;
			case 1088: // Panther Fatal Claw
				useSkill(6096, false);
				break;
			case 1089: // Deinonychus - Tail Strike
				useSkill(6199, true);
				break;
			case 1090: // Guardian's Strider - Strider Bite
				useSkill(6205, true);
				break;
			case 1091: // Guardian's Strider - Strider Fear
				useSkill(6206, true);
				break;
			case 1092: // Guardian's Strider - Strider Dash
				useSkill(6207, true);
				break;
			case 1093: // Maguen - Maguen Strike
				useSkill(6618, true);
				break;
			case 1094: // Maguen - Maguen Wind Walk
				useSkill(6681, true);
				break;
			case 1095: // Elite Maguen - Maguen Power Strike
				useSkill(6619, true);
				break;
			case 1096: // Elite Maguen - Elite Maguen Wind Walk
				useSkill(6682, true);
				break;
			case 1097: // Maguen - Maguen Return
				useSkill(6683, true);
				break;
			case 1098: // Elite Maguen - Maguen Party Return
				useSkill(6684, true);
				break;
			case 1099: // All servitor attack
				activeChar.getServitors().values().forEach(s ->
				{
					if (validateSummon(s, false))
					{
						if (s.canAttack(_ctrlPressed))
						{
							s.doAttack();
						}
					}
				});
				break;
			case 1100: // All servitor move to
				activeChar.getServitors().values().forEach(s ->
				{
					if (validateSummon(s, false))
					{
						if ((target != null) && (s != target) && !s.isMovementDisabled())
						{
							s.setFollowStatus(false);
							s.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, target.getLocation());
						}
					}
				});
				break;
			case 1101: // All servitor stop
				activeChar.getServitors().values().forEach(summon ->
				{
					if (validateSummon(summon, false))
					{
						summon.cancelAction();
					}
				});
				break;
			case 1102: // Unsummon all servitors
				boolean canUnsummon = true;
				OUT: for (L2Summon s : activeChar.getServitors().values())
				{
					if (validateSummon(s, false))
					{
						if (s.isAttackingNow() || s.isInCombat())
						{
							sendPacket(SystemMessageId.A_SERVITOR_WHOM_IS_ENGAGED_IN_BATTLE_CANNOT_BE_DE_ACTIVATED);
							canUnsummon = false;
							break OUT;
						}
						s.unSummon(activeChar);
					}
				}
				if (canUnsummon)
				{
					activeChar.getServitors().values().stream().forEach(s ->
					{
						s.unSummon(activeChar);
					});
				}
				break;
			case 1103: // seems to be passive mode
				break;
			case 1104: // seems to be defend mode
				break;
			case 1106: // Cute Bear - Bear Claw
				useServitorsSkill(11278);
				break;
			case 1107: // Cute Bear - Bear Tumbling
				useServitorsSkill(11279);
				break;
			case 1108: // Saber Tooth Cougar- Cougar Bite
				useServitorsSkill(11280);
				break;
			case 1109: // Saber Tooth Cougar - Cougar Pounce
				useServitorsSkill(11281);
				break;
			case 1110: // Grim Reaper - Reaper Touch
				useServitorsSkill(11282);
				break;
			case 1111: // Grim Reaper - Reaper Power
				useServitorsSkill(11283);
				break;
			case 1113: // Golden Lion - Lion Roar
				useSkill(10051, false);
				break;
			case 1114: // Golden Lion - Lion Claw
				useSkill(10052, false);
				break;
			case 1115: // Golden Lion - Lion Dash
				useSkill(10053, false);
				break;
			case 1116: // Golden Lion - Lion Flame
				useSkill(10054, false);
				break;
			case 1117: // Thunder Hawk - Thunder Flight
				useSkill(10794, false);
				break;
			case 1118: // Thunder Hawk - Thunder Purity
				useSkill(10795, false);
				break;
			case 1120: // Thunder Hawk - Thunder Feather Blast
				useSkill(10797, false);
				break;
			case 1121: // Thunder Hawk - Thunder Sharp Claw
				useSkill(10798, false);
				break;
			case 1122: // Tree of Life - Blessing of Tree
				useServitorsSkill(11806);
				break;
			case 1124: // Wynn Kai the Cat - Feline Aggression
				useServitorsSkill(11323);
				break;
			case 1125: // Wynn Kai the Cat - Feline Stun
				useServitorsSkill(11324);
				break;
			case 1126: // Wynn Feline King - Feline Bite
				useServitorsSkill(11325);
				break;
			case 1127: // Wynn Feline King - Feline Pounce
				useServitorsSkill(11326);
				break;
			case 1128: // Wynn Feline Queen - Feline Touch
				useServitorsSkill(11327);
				break;
			case 1129: // Wynn Feline Queen - Feline Power
				useServitorsSkill(11328);
				break;
			case 1130: // Wynn Merrow - Unicorn's Aggression
				useServitorsSkill(11332);
				break;
			case 1131: // Wynn Merrow - Unicorn's Stun
				useServitorsSkill(11333);
				break;
			case 1132: // Wynn Magnus - Unicorn's Bite
				useServitorsSkill(11334);
				break;
			case 1133: // Wynn Magnus - Unicorn's Pounce
				useServitorsSkill(11335);
				break;
			case 1134: // Wynn Seraphim - Unicorn's Touch
				useServitorsSkill(11336);
				break;
			case 1135: // Wynn Seraphim - Unicorn's Power
				useServitorsSkill(11337);
				break;
			case 1136: // Wynn Nightshade - Phantom Aggression
				useServitorsSkill(11341);
				break;
			case 1137: // Wynn Nightshade - Phantom Stun
				useServitorsSkill(11342);
				break;
			case 1138: // Wynn Spectral Lord - Phantom Bite
				useServitorsSkill(11343);
				break;
			case 1139: // Wynn Spectral Lord - Phantom Pounce
				useServitorsSkill(11344);
				break;
			case 1140: // Wynn Soulless - Phantom Touch
				useServitorsSkill(11345);
				break;
			case 1141: // Wynn Soulless - Phantom Power
				useServitorsSkill(11346);
				break;
			case 1142: // Blood Panther - Panther Roar
				useServitorsSkill(10087);
				break;
			case 1143: // Blood Panther - Panther Rush
				useServitorsSkill(10088);
				break;
			case 5000: // Baby Rudolph - Reindeer Scratch
				useSkill(23155, true);
				break;
			case 5001: // Deseloph, Hyum, Rekang, Lilias, Lapham, Mafum - Rosy Seduction
				useSkill(23167, true);
				break;
			case 5002: // Deseloph, Hyum, Rekang, Lilias, Lapham, Mafum - Critical Seduction
				useSkill(23168, true);
				break;
			case 5003: // Hyum, Lapham, Hyum, Lapham - Thunder Bolt
				useSkill(5749, true);
				break;
			case 5004: // Hyum, Lapham, Hyum, Lapham - Flash
				useSkill(5750, true);
				break;
			case 5005: // Hyum, Lapham, Hyum, Lapham - Lightning Wave
				useSkill(5751, true);
				break;
			case 5006: // Deseloph, Hyum, Rekang, Lilias, Lapham, Mafum, Deseloph, Hyum, Rekang, Lilias, Lapham, Mafum - Buff Control
				useSkill(5771, true);
				break;
			case 5007: // Deseloph, Lilias, Deseloph, Lilias - Piercing Attack
				useSkill(6046, true);
				break;
			case 5008: // Deseloph, Lilias, Deseloph, Lilias - Spin Attack
				useSkill(6047, true);
				break;
			case 5009: // Deseloph, Lilias, Deseloph, Lilias - Smash
				useSkill(6048, true);
				break;
			case 5010: // Deseloph, Lilias, Deseloph, Lilias - Ignite
				useSkill(6049, true);
				break;
			case 5011: // Rekang, Mafum, Rekang, Mafum - Power Smash
				useSkill(6050, true);
				break;
			case 5012: // Rekang, Mafum, Rekang, Mafum - Energy Burst
				useSkill(6051, true);
				break;
			case 5013: // Rekang, Mafum, Rekang, Mafum - Shockwave
				useSkill(6052, true);
				break;
			case 5014: // Rekang, Mafum, Rekang, Mafum - Ignite
				useSkill(6053, true);
				break;
			case 5015: // Deseloph, Hyum, Rekang, Lilias, Lapham, Mafum, Deseloph, Hyum, Rekang, Lilias, Lapham, Mafum - Switch Stance
				useSkill(6054, true);
				break;
			// Social Packets
			case 12: // Greeting
				tryBroadcastSocial(2);
				break;
			case 13: // Victory
				tryBroadcastSocial(3);
				break;
			case 14: // Advance
				tryBroadcastSocial(4);
				break;
			case 24: // Yes
				tryBroadcastSocial(6);
				break;
			case 25: // No
				tryBroadcastSocial(5);
				break;
			case 26: // Bow
				tryBroadcastSocial(7);
				break;
			case 29: // Unaware
				tryBroadcastSocial(8);
				break;
			case 30: // Social Waiting
				tryBroadcastSocial(9);
				break;
			case 31: // Laugh
				tryBroadcastSocial(10);
				break;
			case 33: // Applaud
				tryBroadcastSocial(11);
				break;
			case 34: // Dance
				tryBroadcastSocial(12);
				break;
			case 35: // Sorrow
				tryBroadcastSocial(13);
				break;
			case 62: // Charm
				tryBroadcastSocial(14);
				break;
			case 66: // Shyness
				tryBroadcastSocial(15);
				break;
			case 87: // Propose
				tryBroadcastSocial(28);
				break;
			case 88: // Provoke
				tryBroadcastSocial(29);
				break;
			case 89: // Beauty Shop
				tryBroadcastSocial(30);
				activeChar.broadcastInfo();
				break;
			default:
				_log.warning(activeChar.getName() + ": unhandled action type " + _actionId);
				break;
		}
	}
	
	/**
	 * Use the sit action.
	 * @param activeChar the player trying to sit
	 * @param target the target to sit, throne, bench or chair
	 * @return {@code true} if the player can sit, {@code false} otherwise
	 */
	protected boolean useSit(L2PcInstance activeChar, L2Object target)
	{
		if (activeChar.getMountType() != MountType.NONE)
		{
			return false;
		}
		
		if (!activeChar.isSitting() && (target instanceof L2StaticObjectInstance) && (((L2StaticObjectInstance) target).getType() == 1) && activeChar.isInsideRadius(target, L2StaticObjectInstance.INTERACTION_DISTANCE, false, false))
		{
			final ChairSit cs = new ChairSit(activeChar, target.getId());
			sendPacket(cs);
			activeChar.sitDown();
			activeChar.broadcastPacket(cs);
			return true;
		}
		
		if (activeChar.isFakeDeath())
		{
			activeChar.stopEffects(L2EffectType.FAKE_DEATH);
		}
		else if (activeChar.isSitting())
		{
			activeChar.standUp();
		}
		else
		{
			activeChar.sitDown();
		}
		return true;
	}
	
	/**
	 * Cast a skill for active summon.<br>
	 * Target is specified as a parameter but can be overwrited or ignored depending on skill type.
	 * @param skillId the skill Id to be casted by the summon
	 * @param target the target to cast the skill on, overwritten or ignored depending on skill type
	 * @param pet if {@code true} it'll validate a pet, if {@code false} it will validate a servitor
	 */
	private void useSkill(int skillId, L2Object target, boolean pet)
	{
		final L2PcInstance activeChar = getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if (pet)
		{
			final L2Summon summon = activeChar.getPet();
			if (!validateSummon(summon, pet))
			{
				return;
			}
			
			if (summon instanceof L2BabyPetInstance)
			{
				if (!((L2BabyPetInstance) summon).isInSupportMode())
				{
					sendPacket(SystemMessageId.A_PET_ON_AUXILIARY_MODE_CANNOT_USE_SKILLS);
					return;
				}
			}
			
			if ((summon.getLevel() - activeChar.getLevel()) > 20)
			{
				sendPacket(SystemMessageId.YOUR_PET_IS_TOO_HIGH_LEVEL_TO_CONTROL);
				return;
			}
			
			final int lvl = PetDataTable.getInstance().getPetData(summon.getId()).getAvailableLevel(skillId, summon.getLevel());
			
			if (lvl > 0)
			{
				summon.setTarget(target);
				summon.useMagic(SkillData.getInstance().getSkill(skillId, lvl), _ctrlPressed, _shiftPressed);
			}
			
			if (skillId == SWITCH_STANCE_ID)
			{
				summon.switchMode();
			}
		}
		else
		{
			final L2Summon servitor = activeChar.getAnyServitor();
			if (!validateSummon(servitor, pet))
			{
				return;
			}
			
			final int lvl = SummonSkillsTable.getInstance().getAvailableLevel(servitor, skillId);
			
			if (lvl > 0)
			{
				servitor.setTarget(target);
				servitor.useMagic(SkillData.getInstance().getSkill(skillId, lvl), _ctrlPressed, _shiftPressed);
			}
		}
	}
	
	/**
	 * Cast a skill for active summon.<br>
	 * Target is retrieved from owner's target, then validated by overloaded method useSkill(int, L2Character).
	 * @param skillId the skill Id to use
	 * @param pet if {@code true} it'll validate a pet, if {@code false} it will validate a servitor
	 */
	private void useSkill(int skillId, boolean pet)
	{
		final L2PcInstance activeChar = getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		useSkill(skillId, activeChar.getTarget(), pet);
	}
	
	/**
	 * Cast a skill for all active summon.<br>
	 * Target is retrieved from owner's target
	 * @param skillId the skill Id to use
	 */
	private void useServitorsSkill(int skillId)
	{
		final L2PcInstance activeChar = getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		activeChar.getServitors().values().forEach(servitor ->
		{
			if (!validateSummon(servitor, false))
			{
				return;
			}
			
			final int lvl = SummonSkillsTable.getInstance().getAvailableLevel(servitor, skillId);
			
			if (lvl > 0)
			{
				servitor.setTarget(activeChar.getTarget());
				servitor.useMagic(SkillData.getInstance().getSkill(skillId, lvl), _ctrlPressed, _shiftPressed);
			}
		});
	}
	
	/**
	 * Validates the given summon and sends a system message to the master.
	 * @param summon the summon to validate
	 * @param checkPet if {@code true} it'll validate a pet, if {@code false} it will validate a servitor
	 * @return {@code true} if the summon is not null and whether is a pet or a servitor depending on {@code checkPet} value, {@code false} otherwise
	 */
	private boolean validateSummon(L2Summon summon, boolean checkPet)
	{
		if ((summon != null) && ((checkPet && summon.isPet()) || summon.isServitor()))
		{
			if (summon.isPet() && ((L2PetInstance) summon).isUncontrollable())
			{
				sendPacket(SystemMessageId.WHEN_YOUR_PET_S_HUNGER_GAUGE_IS_AT_0_YOU_CANNOT_USE_YOUR_PET);
				return false;
			}
			if (summon.isBetrayed())
			{
				sendPacket(SystemMessageId.YOUR_PET_SERVITOR_IS_UNRESPONSIVE_AND_WILL_NOT_OBEY_ANY_ORDERS);
				return false;
			}
			return true;
		}
		
		if (checkPet)
		{
			sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_A_PET);
		}
		else
		{
			sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_A_SERVITOR);
		}
		return false;
	}
	
	/**
	 * Try to broadcast SocialAction packet.
	 * @param id the social action Id to broadcast
	 */
	private void tryBroadcastSocial(int id)
	{
		final L2PcInstance activeChar = getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		if (activeChar.isFishing())
		{
			sendPacket(SystemMessageId.YOU_CANNOT_DO_THAT_WHILE_FISHING);
			return;
		}
		
		if (activeChar.canMakeSocialAction())
		{
			activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), id));
		}
	}
	
	/**
	 * Perform a couple social action.
	 * @param id the couple social action Id
	 */
	private void useCoupleSocial(final int id)
	{
		final L2PcInstance requester = getActiveChar();
		if (requester == null)
		{
			return;
		}
		
		final L2Object target = requester.getTarget();
		if ((target == null) || !target.isPlayer())
		{
			sendPacket(SystemMessageId.INVALID_TARGET);
			return;
		}
		
		final int distance = (int) requester.calculateDistance(target, false, false);
		if ((distance > 125) || (distance < 15) || (requester.getObjectId() == target.getObjectId()))
		{
			sendPacket(SystemMessageId.THE_REQUEST_CANNOT_BE_COMPLETED_BECAUSE_THE_TARGET_DOES_NOT_MEET_LOCATION_REQUIREMENTS);
			return;
		}
		
		SystemMessage sm;
		if (requester.isInStoreMode() || requester.isInCraftMode())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_PRIVATE_STORE_MODE_OR_IN_A_BATTLE_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
			sm.addPcName(requester);
			sendPacket(sm);
			return;
		}
		
		if (requester.isInCombat() || requester.isInDuel() || AttackStanceTaskManager.getInstance().hasAttackStanceTask(requester))
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_A_BATTLE_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
			sm.addPcName(requester);
			sendPacket(sm);
			return;
		}
		
		if (requester.isFishing())
		{
			sendPacket(SystemMessageId.YOU_CANNOT_DO_THAT_WHILE_FISHING);
			return;
		}
		
		if (requester.getReputation() < 0)
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_A_CHAOTIC_STATE_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
			sm.addPcName(requester);
			sendPacket(sm);
			return;
		}
		
		if (requester.isInOlympiadMode())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_PARTICIPATING_IN_THE_OLYMPIAD_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
			sm.addPcName(requester);
			sendPacket(sm);
			return;
		}
		
		if (requester.isInSiege())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_A_CASTLE_SIEGE_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
			sm.addPcName(requester);
			sendPacket(sm);
			return;
		}
		
		if (requester.isInHideoutSiege())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_PARTICIPATING_IN_A_CLAN_HALL_SIEGE_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
			sm.addPcName(requester);
			sendPacket(sm);
		}
		
		if (requester.isMounted() || requester.isFlyingMounted() || requester.isInBoat() || requester.isInAirShip())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_RIDING_A_SHIP_STEED_OR_STRIDER_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
			sm.addPcName(requester);
			sendPacket(sm);
			return;
		}
		
		if (requester.isTransformed())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_CURRENTLY_TRANSFORMING_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
			sm.addPcName(requester);
			sendPacket(sm);
			return;
		}
		
		if (requester.isAlikeDead())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_CURRENTLY_DEAD_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
			sm.addPcName(requester);
			sendPacket(sm);
			return;
		}
		
		// Checks for partner.
		final L2PcInstance partner = target.getActingPlayer();
		if (partner.isInStoreMode() || partner.isInCraftMode())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_PRIVATE_STORE_MODE_OR_IN_A_BATTLE_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
			sm.addPcName(partner);
			sendPacket(sm);
			return;
		}
		
		if (partner.isInCombat() || partner.isInDuel() || AttackStanceTaskManager.getInstance().hasAttackStanceTask(partner))
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_A_BATTLE_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
			sm.addPcName(partner);
			sendPacket(sm);
			return;
		}
		
		if (partner.getMultiSociaAction() > 0)
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_ALREADY_PARTICIPATING_IN_A_COUPLE_ACTION_AND_CANNOT_BE_REQUESTED_FOR_ANOTHER_COUPLE_ACTION);
			sm.addPcName(partner);
			sendPacket(sm);
			return;
		}
		
		if (partner.isFishing())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_FISHING_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
			sm.addPcName(partner);
			sendPacket(sm);
			return;
		}
		
		if (partner.getReputation() < 0)
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_A_CHAOTIC_STATE_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
			sm.addPcName(partner);
			sendPacket(sm);
			return;
		}
		
		if (partner.isInOlympiadMode())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_PARTICIPATING_IN_THE_OLYMPIAD_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
			sm.addPcName(partner);
			sendPacket(sm);
			return;
		}
		
		if (partner.isInHideoutSiege())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_PARTICIPATING_IN_A_CLAN_HALL_SIEGE_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
			sm.addPcName(partner);
			sendPacket(sm);
			return;
		}
		
		if (partner.isInSiege())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_A_CASTLE_SIEGE_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
			sm.addPcName(partner);
			sendPacket(sm);
			return;
		}
		
		if (partner.isMounted() || partner.isFlyingMounted() || partner.isInBoat() || partner.isInAirShip())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_RIDING_A_SHIP_STEED_OR_STRIDER_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
			sm.addPcName(partner);
			sendPacket(sm);
			return;
		}
		
		if (partner.isTeleporting())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_CURRENTLY_TELEPORTING_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
			sm.addPcName(partner);
			sendPacket(sm);
			return;
		}
		
		if (partner.isTransformed())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_CURRENTLY_TRANSFORMING_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
			sm.addPcName(partner);
			sendPacket(sm);
			return;
		}
		
		if (partner.isAlikeDead())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_CURRENTLY_DEAD_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
			sm.addPcName(partner);
			sendPacket(sm);
			return;
		}
		
		if (requester.isAllSkillsDisabled() || partner.isAllSkillsDisabled())
		{
			sendPacket(SystemMessageId.THE_COUPLE_ACTION_WAS_CANCELLED);
			return;
		}
		
		requester.setMultiSocialAction(id, partner.getObjectId());
		sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_REQUESTED_A_COUPLE_ACTION_WITH_C1);
		sm.addPcName(partner);
		sendPacket(sm);
		
		if ((requester.getAI().getIntention() != CtrlIntention.AI_INTENTION_IDLE) || (partner.getAI().getIntention() != CtrlIntention.AI_INTENTION_IDLE))
		{
			final NextAction nextAction = new NextAction(CtrlEvent.EVT_ARRIVED, CtrlIntention.AI_INTENTION_MOVE_TO, () -> partner.sendPacket(new ExAskCoupleAction(requester.getObjectId(), id)));
			requester.getAI().setNextAction(nextAction);
			return;
		}
		
		if (requester.isCastingNow() || requester.isCastingSimultaneouslyNow())
		{
			final NextAction nextAction = new NextAction(CtrlEvent.EVT_FINISH_CASTING, CtrlIntention.AI_INTENTION_CAST, () -> partner.sendPacket(new ExAskCoupleAction(requester.getObjectId(), id)));
			requester.getAI().setNextAction(nextAction);
			return;
		}
		
		partner.sendPacket(new ExAskCoupleAction(requester.getObjectId(), id));
	}
	
	@Override
	public String getType()
	{
		return _C__56_REQUESTACTIONUSE;
	}
	
	@Override
	protected boolean triggersOnActionRequest()
	{
		return (_actionId != 10) && (_actionId != 28);
	}
}
