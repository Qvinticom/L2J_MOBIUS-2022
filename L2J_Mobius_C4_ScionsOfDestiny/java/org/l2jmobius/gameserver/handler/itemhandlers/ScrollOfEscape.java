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
package org.l2jmobius.gameserver.handler.itemhandlers;

import org.l2jmobius.Config;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.ai.CtrlIntention;
import org.l2jmobius.gameserver.data.SkillTable;
import org.l2jmobius.gameserver.data.sql.ClanHallTable;
import org.l2jmobius.gameserver.enums.TeleportWhereType;
import org.l2jmobius.gameserver.handler.IItemHandler;
import org.l2jmobius.gameserver.instancemanager.CastleManager;
import org.l2jmobius.gameserver.instancemanager.FortManager;
import org.l2jmobius.gameserver.model.Skill;
import org.l2jmobius.gameserver.model.actor.Playable;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.items.instance.Item;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.MagicSkillUse;
import org.l2jmobius.gameserver.network.serverpackets.SetupGauge;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.taskmanager.GameTimeTaskManager;

public class ScrollOfEscape implements IItemHandler
{
	// All the items ids that this handler knows
	private static final int[] ITEM_IDS =
	{
		736,
		1830,
		1829,
		1538,
		3958,
		5858,
		5859,
		7117,
		7118,
		7119,
		7120,
		7121,
		7122,
		7123,
		7124,
		7125,
		7126,
		7127,
		7128,
		7129,
		7130,
		7131,
		7132,
		7133,
		7134,
		7135,
		7554,
		7555,
		7556,
		7557,
		7558,
		7559,
		7618,
		7619
	};
	
	@Override
	public void useItem(Playable playable, Item item)
	{
		if (!(playable instanceof Player))
		{
			return;
		}
		
		final Player player = (Player) playable;
		if (checkConditions(player))
		{
			return;
		}
		
		// Check to see if player is sitting
		if (player.isSitting())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_MOVE_WHILE_SITTING);
			return;
		}
		
		if (player.isOnEvent())
		{
			player.sendMessage("You can't use Scroll of Escape in an event.");
			return;
		}
		
		// Check to see if player is on olympiad
		if (player.isInOlympiadMode())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_USE_THAT_ITEM_IN_A_GRAND_OLYMPIAD_GAMES_MATCH);
			return;
		}
		
		if (!Config.ALLOW_SOE_IN_PVP && (player.getPvpFlag() != 0))
		{
			player.sendMessage("You Can't Use SOE In PvP!");
			return;
		}
		
		// Check to see if the player is in a festival.
		if (player.isFestivalParticipant())
		{
			player.sendPacket(SystemMessage.sendString("You may not use an escape skill in a festival."));
			return;
		}
		
		// Check to see if player is in jail
		if (player.isInJail())
		{
			player.sendPacket(SystemMessage.sendString("You can not escape from jail."));
			return;
		}
		
		if (player.isParalyzed())
		{
			player.sendPacket(SystemMessage.sendString("You may not use an escape skill in a paralyzed."));
			return;
		}
		
		if (player.isCastingNow())
		{
			player.sendMessage("You may not use this item while casting a skill.");
			return;
		}
		
		// Check if this is a blessed scroll, if it is then shorten the cast time.
		final int itemId = item.getItemId();
		final SystemMessage sm3 = new SystemMessage(SystemMessageId.USE_S1);
		sm3.addItemName(itemId);
		player.sendPacket(sm3);
		
		int escapeSkill = (itemId == 1538) || (itemId == 5858) || (itemId == 5859) || (itemId == 3958) || (itemId == 10130) ? 2036 : 2013;
		// C4 adjustment.
		if ((escapeSkill == 2036) || (escapeSkill == 2177) || (escapeSkill == 2178))
		{
			escapeSkill = 2013;
		}
		if (!player.destroyItem("Consume", item.getObjectId(), 1, null, false))
		{
			return;
		}
		
		// Send consume message.
		final SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_DISAPPEARED);
		sm.addItemName(itemId);
		player.sendPacket(sm);
		
		// Abort combat.
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.abortAttack();
		player.disableAllSkills();
		
		final Skill skill = SkillTable.getInstance().getSkill(escapeSkill, 1);
		int hitTime = skill.getHitTime();
		// C4 adjustment.
		if ((escapeSkill == 2036) || (escapeSkill == 2177) || (escapeSkill == 2178))
		{
			hitTime = 500;
		}
		
		// Cast escape animation.
		player.broadcastPacket(new MagicSkillUse(player, player, escapeSkill, 1, hitTime, 0));
		player.sendPacket(new SetupGauge(0, hitTime));
		player.setTarget(null);
		
		// Continue execution later.
		final EscapeFinalizer escapeFinalizer = new EscapeFinalizer(player, itemId);
		player.setSkillCast(ThreadPool.schedule(escapeFinalizer, hitTime));
		player.setSkillCastEndTime(10 + GameTimeTaskManager.getGameTicks() + (hitTime / GameTimeTaskManager.MILLIS_IN_TICK));
	}
	
	static class EscapeFinalizer implements Runnable
	{
		private final Player _player;
		private final int _itemId;
		
		EscapeFinalizer(Player player, int itemId)
		{
			_player = player;
			_itemId = itemId;
		}
		
		@Override
		public void run()
		{
			if (_player.isDead())
			{
				return;
			}
			
			_player.enableAllSkills();
			_player.setIn7sDungeon(false);
			
			try
			{
				// escape to castle if own's one
				if (((_itemId == 1830) || (_itemId == 5859)))
				{
					if (CastleManager.getInstance().getCastleByOwner(_player.getClan()) != null)
					{
						_player.teleToLocation(TeleportWhereType.CASTLE);
					}
					else
					{
						_player.teleToLocation(TeleportWhereType.TOWN);
					}
				}
				// escape to fortress if own's one if own's one
				else if (((_itemId == 1830) || (_itemId == 5859)))
				{
					if (FortManager.getInstance().getFortByOwner(_player.getClan()) != null)
					{
						_player.teleToLocation(TeleportWhereType.FORTRESS);
					}
					else
					{
						_player.teleToLocation(TeleportWhereType.TOWN);
					}
				}
				else if (((_itemId == 1829) || (_itemId == 5858)) && (_player.getClan() != null) && (ClanHallTable.getInstance().getClanHallByOwner(_player.getClan()) != null)) // escape to clan hall if own's one
				{
					_player.teleToLocation(TeleportWhereType.CLANHALL);
				}
				else if (_itemId == 5858) // do nothing
				{
					_player.sendPacket(SystemMessageId.THE_CLAN_DOES_NOT_OWN_A_CLAN_HALL);
				}
				else if ((_player.getKarma() > 0) && Config.ALT_KARMA_TELEPORT_TO_FLORAN)
				{
					_player.teleToLocation(17836, 170178, -3507, true); // Floran
				}
				else if (_itemId < 7117)
				{
					_player.teleToLocation(TeleportWhereType.TOWN);
				}
				else
				{
					switch (_itemId)
					{
						case 7117:
						{
							_player.teleToLocation(-84318, 244579, -3730, true); // Talking Island
							break;
						}
						case 7554:
						{
							_player.teleToLocation(-84318, 244579, -3730, true); // Talking Island quest scroll
							break;
						}
						case 7118:
						{
							_player.teleToLocation(46934, 51467, -2977, true); // Elven Village
							break;
						}
						case 7555:
						{
							_player.teleToLocation(46934, 51467, -2977, true); // Elven Village quest scroll
							break;
						}
						case 7119:
						{
							_player.teleToLocation(9745, 15606, -4574, true); // Dark Elven Village
							break;
						}
						case 7556:
						{
							_player.teleToLocation(9745, 15606, -4574, true); // Dark Elven Village quest scroll
							break;
						}
						case 7120:
						{
							_player.teleToLocation(-44836, -112524, -235, true); // Orc Village
							break;
						}
						case 7557:
						{
							_player.teleToLocation(-44836, -112524, -235, true); // Orc Village quest scroll
							break;
						}
						case 7121:
						{
							_player.teleToLocation(115113, -178212, -901, true); // Dwarven Village
							break;
						}
						case 7558:
						{
							_player.teleToLocation(115113, -178212, -901, true); // Dwarven Village quest scroll
							break;
						}
						case 7122:
						{
							_player.teleToLocation(-80826, 149775, -3043, true); // Gludin Village
							break;
						}
						case 7123:
						{
							_player.teleToLocation(-12678, 122776, -3116, true); // Gludio Castle Town
							break;
						}
						case 7124:
						{
							_player.teleToLocation(15670, 142983, -2705, true); // Dion Castle Town
							break;
						}
						case 7125:
						{
							_player.teleToLocation(17836, 170178, -3507, true); // Floran
							break;
						}
						case 7126:
						{
							_player.teleToLocation(83400, 147943, -3404, true); // Giran Castle Town
							break;
						}
						case 7559:
						{
							_player.teleToLocation(83400, 147943, -3404, true); // Giran Castle Town quest scroll
							break;
						}
						case 7127:
						{
							_player.teleToLocation(105918, 109759, -3207, true); // Hardin's Private Academy
							break;
						}
						case 7128:
						{
							_player.teleToLocation(111409, 219364, -3545, true); // Heine
							break;
						}
						case 7129:
						{
							_player.teleToLocation(82956, 53162, -1495, true); // Oren Castle Town
							break;
						}
						case 7130:
						{
							_player.teleToLocation(85348, 16142, -3699, true); // Ivory Tower
							break;
						}
						case 7131:
						{
							_player.teleToLocation(116819, 76994, -2714, true); // Hunters Village
							break;
						}
						case 7132:
						{
							_player.teleToLocation(146331, 25762, -2018, true); // Aden Castle Town
							break;
						}
						case 7133:
						{
							_player.teleToLocation(147928, -55273, -2734, true); // Goddard Castle Town
							break;
						}
						case 7134:
						{
							_player.teleToLocation(43799, -47727, -798, true); // Rune Castle Town
							break;
						}
						case 7135:
						{
							_player.teleToLocation(87331, -142842, -1317, true); // Schuttgart Castle Town
							break;
						}
						case 7618:
						{
							_player.teleToLocation(149864, -81062, -5618, true); // Ketra Orc Village
							break;
						}
						case 7619:
						{
							_player.teleToLocation(108275, -53785, -2524, true); // Varka Silenos Village
							break;
						}
						default:
						{
							_player.teleToLocation(TeleportWhereType.TOWN);
							break;
						}
					}
				}
			}
			catch (Throwable e)
			{
			}
		}
	}
	
	private static boolean checkConditions(Player actor)
	{
		return actor.isStunned() || actor.isSleeping() || actor.isParalyzed() || actor.isFakeDeath() || actor.isTeleporting() || actor.isMuted() || actor.isAlikeDead() || actor.isAllSkillsDisabled();
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
