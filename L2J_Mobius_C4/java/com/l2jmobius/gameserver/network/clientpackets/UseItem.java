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
import com.l2jmobius.gameserver.GameTimeController;
import com.l2jmobius.gameserver.ThreadPoolManager;
import com.l2jmobius.gameserver.handler.IItemHandler;
import com.l2jmobius.gameserver.handler.ItemHandler;
import com.l2jmobius.gameserver.instancemanager.CastleManager;
import com.l2jmobius.gameserver.model.Inventory;
import com.l2jmobius.gameserver.model.L2Clan;
import com.l2jmobius.gameserver.model.L2ItemInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import com.l2jmobius.gameserver.network.serverpackets.ItemList;
import com.l2jmobius.gameserver.network.serverpackets.ShowCalculator;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import com.l2jmobius.gameserver.templates.L2Armor;
import com.l2jmobius.gameserver.templates.L2ArmorType;
import com.l2jmobius.gameserver.templates.L2Item;
import com.l2jmobius.gameserver.templates.L2Weapon;
import com.l2jmobius.gameserver.templates.L2WeaponType;

/**
 * This class ...
 * @version $Revision: 1.18.2.7.2.9 $ $Date: 2005/03/27 15:29:30 $
 */
public class UseItem extends L2GameClientPacket
{
	private static Logger _log = Logger.getLogger(UseItem.class.getName());
	private static final String _C__14_USEITEM = "[C] 14 UseItem";
	
	private int _objectId;
	
	/** Weapon Equip Task */
	public class WeaponEquipTask implements Runnable
	{
		L2ItemInstance item;
		L2PcInstance activeChar;
		
		public WeaponEquipTask(L2ItemInstance it, L2PcInstance character)
		{
			item = it;
			activeChar = character;
		}
		
		@Override
		public void run()
		{
			// If character is still engaged in strike we should not change weapon
			if (activeChar.isAttackingNow())
			{
				return;
			}
			
			// Equip or unEquip
			activeChar.useEquippableItem(item, false);
		}
	}
	
	@Override
	protected void readImpl()
	{
		_objectId = readD();
	}
	
	@Override
	public void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		// Flood protect UseItem
		if (!getClient().getFloodProtectors().getUseItem().tryPerformAction("use item"))
		{
			return;
		}
		
		if (activeChar.getPrivateStoreType() != 0)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE));
			activeChar.sendPacket(new ActionFailed());
			return;
		}
		
		if (activeChar.getActiveTradeList() != null)
		{
			activeChar.cancelActiveTrade();
		}
		
		final L2ItemInstance item = activeChar.getInventory().getItemByObjectId(_objectId);
		
		if (item == null)
		{
			return;
		}
		
		if (item.isWear())
		{
			return;
		}
		
		if (item.getItem().getType2() == L2Item.TYPE2_QUEST)
		{
			activeChar.sendPacket(new SystemMessage(148));
			
			return;
		}
		
		final int itemId = item.getItemId();
		
		// Items that cannot be used
		if (itemId == 57)
		{
			return;
		}
		
		// Alt game - Karma punishment // SOE
		if (!Config.ALT_GAME_KARMA_PLAYER_CAN_TELEPORT && (activeChar.getKarma() > 0))
		{
			switch (itemId)
			{
				case 736:
				case 1538:
				case 1829:
				case 1830:
				case 3958:
				case 5858:
				case 5859:
					return;
			}
		}
		
		if (activeChar.isFishing() && ((itemId < 6535) || (itemId > 6540)))
		{
			// You cannot do anything else while fishing
			activeChar.sendPacket(new SystemMessage(SystemMessage.CANNOT_DO_WHILE_FISHING_3));
			return;
		}
		
		// Char cannot use item when dead
		if (activeChar.isDead())
		{
			SystemMessage sm = new SystemMessage(SystemMessage.S1_CANNOT_BE_USED);
			sm.addItemName(itemId);
			activeChar.sendPacket(sm);
			
			sm = null;
			return;
		}
		
		if (activeChar.isStunned() || activeChar.isSleeping() || activeChar.isParalyzed() || activeChar.isAlikeDead())
		{
			return;
		}
		
		if (Config.DEBUG)
		{
			_log.finest(activeChar.getObjectId() + ": use item " + _objectId);
		}
		
		if (item.isEquipable())
		{
			// No unequipping/equipping while the player is casting
			if (activeChar.isCastingNow())
			{
				return;
			}
			
			final int bodyPart = item.getItem().getBodyPart();
			
			// Prevent player to remove the weapon while mounted
			if (activeChar.isMounted() && ((bodyPart == L2Item.SLOT_LR_HAND) || (bodyPart == L2Item.SLOT_L_HAND) || (bodyPart == L2Item.SLOT_R_HAND)))
			{
				return;
			}
			
			// Don't allow weapon/shield equipment if wearing formal wear
			if (activeChar.isWearingFormalWear() && ((bodyPart == L2Item.SLOT_LR_HAND) || (bodyPart == L2Item.SLOT_L_HAND) || (bodyPart == L2Item.SLOT_R_HAND)))
			{
				activeChar.sendPacket(new SystemMessage(SystemMessage.CANNOT_USE_ITEMS_SKILLS_WITH_FORMALWEAR));
				return;
			}
			
			SystemMessage sm = null;
			
			final L2Clan cl = activeChar.getClan();
			// A shield that can only be used by the members of a clan that owns a castle.
			if (((cl == null) || (cl.getHasCastle() == 0)) && (itemId == 7015))
			{
				sm = new SystemMessage(SystemMessage.S1_CANNOT_BE_USED);
				sm.addItemName(itemId);
				activeChar.sendPacket(sm);
				
				sm = null;
				return;
			}
			
			// A shield that can only be used by the members of a clan that owns a clan hall.
			if (((cl == null) || (cl.getHasHideout() == 0)) && (itemId == 6902))
			{
				sm = new SystemMessage(SystemMessage.S1_CANNOT_BE_USED);
				sm.addItemName(itemId);
				activeChar.sendPacket(sm);
				
				sm = null;
				return;
			}
			
			// The Lord's Crown used by castle lords only
			if ((itemId == 6841) && ((cl == null) || ((cl.getHasCastle() == 0) || !activeChar.isClanLeader())))
			{
				sm = new SystemMessage(SystemMessage.S1_CANNOT_BE_USED);
				sm.addItemName(itemId);
				activeChar.sendPacket(sm);
				
				sm = null;
				return;
			}
			
			// Castle circlets used by the members of a clan that owns a castle.
			if ((itemId >= 6834) && (itemId <= 6840))
			{
				if (cl == null)
				{
					sm = new SystemMessage(SystemMessage.S1_CANNOT_BE_USED);
					sm.addItemName(itemId);
					activeChar.sendPacket(sm);
					
					sm = null;
					return;
				}
				
				final int circletId = CastleManager.getInstance().getCircletByCastleId(cl.getHasCastle());
				if (circletId != itemId)
				{
					sm = new SystemMessage(SystemMessage.S1_CANNOT_BE_USED);
					sm.addItemName(itemId);
					activeChar.sendPacket(sm);
					
					sm = null;
					return;
				}
			}
			
			// Char cannot use pet items
			if (((item.getItem() instanceof L2Armor) && (item.getItem().getItemType() == L2ArmorType.PET)) || ((item.getItem() instanceof L2Weapon) && (item.getItem().getItemType() == L2WeaponType.PET)))
			{
				sm = new SystemMessage(600); // You cannot equip a pet item.
				sm.addItemName(itemId);
				activeChar.sendPacket(sm);
				
				sm = null;
				return;
			}
			
			// Don't allow hero equipment during Olympiad
			if (activeChar.isInOlympiadMode() && ((item.getItemId() >= 6611) && (item.getItemId() <= 6621)))
			{
				return;
			}
			
			// Cannot use Traveler's/Adventurer's weapons when PK Count > 0
			if ((activeChar.getPkKills() > 0) && ((itemId >= 7816) && (itemId <= 7831)))
			{
				sm = new SystemMessage(SystemMessage.S1_CANNOT_BE_USED);
				sm.addItemName(itemId);
				activeChar.sendPacket(sm);
				sm = null;
				return;
			}
			
			if (activeChar.isAttackingNow())
			{
				ThreadPoolManager.getInstance().scheduleGeneral(new WeaponEquipTask(item, activeChar), (activeChar.getAttackEndTime() - GameTimeController.getGameTicks()) * GameTimeController.MILLIS_IN_TICK);
				return;
			}
			
			// Equip or unEquip
			activeChar.useEquippableItem(item, true);
		}
		else
		{
			final L2Weapon weaponItem = activeChar.getActiveWeaponItem();
			
			if (itemId == 4393)
			{
				activeChar.sendPacket(new ShowCalculator(4393));
			}
			else if (((weaponItem != null) && (weaponItem.getItemType() == L2WeaponType.ROD)) && (((itemId >= 6519) && (itemId <= 6527)) || ((itemId >= 7610) && (itemId <= 7613)) || ((itemId >= 7807) && (itemId <= 7809))))
			{
				
				activeChar.getInventory().setPaperdollItem(Inventory.PAPERDOLL_LHAND, item);
				activeChar.broadcastUserInfo();
				
				// Send a Server->Client packet ItemList to this L2PcINstance to update left hand equipment
				final ItemList il = new ItemList(activeChar, false);
				sendPacket(il);
				return;
			}
			else
			{
				final IItemHandler handler = ItemHandler.getInstance().getItemHandler(itemId);
				
				if (handler == null)
				{
					if (Config.DEBUG)
					{
						_log.warning("No item handler registered for item ID " + itemId + ".");
					}
				}
				else
				{
					handler.useItem(activeChar, item);
				}
			}
		}
	}
	
	@Override
	public String getType()
	{
		return _C__14_USEITEM;
	}
}