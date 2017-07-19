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
package com.l2jmobius.gameserver.handler.itemhandlers;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.GameTimeController;
import com.l2jmobius.gameserver.ThreadPoolManager;
import com.l2jmobius.gameserver.ai.CtrlIntention;
import com.l2jmobius.gameserver.datatables.MapRegionTable;
import com.l2jmobius.gameserver.datatables.SkillTable;
import com.l2jmobius.gameserver.handler.IItemHandler;
import com.l2jmobius.gameserver.instancemanager.DimensionalRiftManager;
import com.l2jmobius.gameserver.model.L2ItemInstance;
import com.l2jmobius.gameserver.model.L2Skill;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jmobius.gameserver.network.serverpackets.MagicSkillUse;
import com.l2jmobius.gameserver.network.serverpackets.SetupGauge;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * This class ...
 * @version $Revision: 1.2.2.3.2.5 $ $Date: 2005/03/27 15:30:07 $
 */
public class ScrollOfEscape implements IItemHandler
{
	// all the items ids that this handler knowns
	private static int[] _itemIds =
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
		7554,
		7555,
		7556,
		7557,
		7558,
		7559,
		7618,
		7619
	};
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.handler.IItemHandler#useItem(com.l2jmobius.gameserver.model.L2PcInstance, com.l2jmobius.gameserver.model.L2ItemInstance)
	 */
	@Override
	public void useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
		{
			return;
		}
		final L2PcInstance activeChar = (L2PcInstance) playable;
		
		if (activeChar.isMovementDisabled() || activeChar.isAlikeDead() || activeChar.isAllSkillsDisabled())
		{
			return;
		}
		
		if (activeChar.isSitting())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.CANT_MOVE_SITTING));
			return;
		}
		
		if ((activeChar.isInParty() && activeChar.getParty().isInDimensionalRift()) || DimensionalRiftManager.getInstance().checkIfInRiftZone(activeChar.getX(), activeChar.getY(), activeChar.getZ(), true))
		{
			activeChar.sendMessage("Once a party is ported in another dimension, its members cannot get out of it.");
			return;
		}
		
		if (activeChar.isInOlympiadMode())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT));
			return;
		}
		
		if (activeChar.getEventTeam() > 0)
		{
			activeChar.sendMessage("You may not use an escape skill in TvT Event.");
			return;
		}
		
		// Check to see if the player is in a festival.
		if (activeChar.isFestivalParticipant())
		{
			activeChar.sendMessage("You may not use an escape skill in a festival.");
			return;
		}
		
		// Check to see if player is in jail
		if (activeChar.isInJail())
		{
			activeChar.sendMessage("You cannot escape from jail.");
			return;
		}
		
		// Modified by Tempy - 28 Jul 05 \\
		// Check if this is a blessed scroll, if it is then shorten the cast time.
		
		final int itemId = item.getItemId();
		final int escapeSkill = ((itemId == 1538) || (itemId == 5858) || (itemId == 5859) || (itemId == 3958)) ? 2036 : 2013;
		
		final L2Skill skill = SkillTable.getInstance().getInfo(escapeSkill, 1);
		
		if (!activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false))
		{
			return;
		}
		
		activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		
		activeChar.setTarget(activeChar);
		
		activeChar.disableAllSkills();
		
		activeChar.broadcastPacket(new MagicSkillUse(activeChar, skill.getId(), 1, skill.getHitTime(), 0));
		
		activeChar.sendPacket(new SetupGauge(0, skill.getHitTime()));
		
		final SystemMessage sm = new SystemMessage(SystemMessage.S1_DISAPPEARED);
		sm.addItemName(itemId);
		activeChar.sendPacket(sm);
		
		final EscapeFinalizer ef = new EscapeFinalizer(activeChar, itemId);
		// continue execution later
		activeChar.setSkillCast(ThreadPoolManager.getInstance().scheduleEffect(ef, skill.getHitTime()));
		activeChar.setSkillCastEndTime(10 + GameTimeController.getGameTicks() + (skill.getHitTime() / GameTimeController.MILLIS_IN_TICK));
	}
	
	static class EscapeFinalizer implements Runnable
	{
		private final L2PcInstance _activeChar;
		private final int _itemId;
		
		EscapeFinalizer(L2PcInstance activeChar, int itemId)
		{
			_activeChar = activeChar;
			_itemId = itemId;
		}
		
		@Override
		public void run()
		{
			if (_activeChar.isDead())
			{
				return;
			}
			
			_activeChar.enableAllSkills();
			
			try
			{
				
				switch (_itemId)
				{
					case 1829:
					case 5858:
						if (_activeChar.getClan().getHasHideout() == 0)
						{
							_activeChar.sendMessage("The clan does not own a clan hall.");
						}
						
						_activeChar.teleToLocation(MapRegionTable.TeleportWhereType.ClanHall);
						
						break;
					case 1830:
					case 5859:
						if (_activeChar.getClan().getHasCastle() == 0)
						{
							_activeChar.sendMessage("The clan does not own a castle.");
						}
						_activeChar.teleToLocation(MapRegionTable.TeleportWhereType.Castle);
						
						break;
					
					case 7117:
						_activeChar.teleToLocation(-84318, 244579, -3730, true); // Talking Island
						break;
					case 7554:
						_activeChar.teleToLocation(-84318, 244579, -3730, true); // Talking Island quest scroll
						break;
					case 7118:
						_activeChar.teleToLocation(46934, 51467, -2977, true); // Elven Village
						break;
					case 7555:
						_activeChar.teleToLocation(46934, 51467, -2977, true); // Elven Village quest scroll
						break;
					case 7119:
						_activeChar.teleToLocation(9745, 15606, -4574, true); // Dark Elven Village
						break;
					case 7556:
						_activeChar.teleToLocation(9745, 15606, -4574, true); // Dark Elven Village quest scroll
						break;
					case 7120:
						_activeChar.teleToLocation(-44836, -112524, -235, true); // Orc Village
						break;
					case 7557:
						_activeChar.teleToLocation(-44836, -112524, -235, true); // Orc Village quest scroll
						break;
					case 7121:
						_activeChar.teleToLocation(115113, -178212, -901, true); // Dwarven Village
						break;
					case 7558:
						_activeChar.teleToLocation(115113, -178212, -901, true); // Dwarven Village quest scroll
						break;
					case 7122:
						_activeChar.teleToLocation(-80826, 149775, -3043, true); // Gludin Village
						break;
					case 7123:
						_activeChar.teleToLocation(-12678, 122776, -3116, true); // Gludio Castle Town
						break;
					case 7124:
						_activeChar.teleToLocation(15670, 142983, -2705, true); // Dion Castle Town
						break;
					case 7125:
						_activeChar.teleToLocation(17836, 170178, -3507, true); // Floran
						break;
					case 7126:
						_activeChar.teleToLocation(83400, 147943, -3404, true); // Giran Castle Town
						break;
					case 7559:
						_activeChar.teleToLocation(83400, 147943, -3404, true); // Giran Castle Town quest scroll
						break;
					case 7127:
						_activeChar.teleToLocation(105918, 109759, -3207, true); // Hardin's Private Academy
						break;
					case 7128:
						_activeChar.teleToLocation(111409, 219364, -3545, true); // Heine
						break;
					case 7129:
						_activeChar.teleToLocation(82956, 53162, -1495, true); // Oren Castle Town
						break;
					case 7130:
						_activeChar.teleToLocation(85348, 16142, -3699, true); // Ivory Tower
						break;
					case 7131:
						_activeChar.teleToLocation(116819, 76994, -2714, true); // Hunters Village
						break;
					case 7132:
						_activeChar.teleToLocation(146331, 25762, -2018, true); // Aden Castle Town
						break;
					case 7133:
						_activeChar.teleToLocation(147928, -55273, -2734, true); // Goddard Castle Town
						break;
					case 7134:
						_activeChar.teleToLocation(43799, -47727, -798, true); // Rune Castle Town
						break;
					case 7618:
						
						_activeChar.teleToLocation(149864, -81062, -5618, true); // Ketra Orc Village
						
						break;
					
					case 7619:
						
						_activeChar.teleToLocation(108275, -53785, -2524, true); // Varka Silenos Village
						
						break;
					default:
						_activeChar.teleToLocation(MapRegionTable.TeleportWhereType.Town);
						
						break;
				}
			}
			catch (final Throwable e)
			{
				if (Config.DEBUG)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	@Override
	public int[] getItemIds()
	{
		return _itemIds;
	}
}