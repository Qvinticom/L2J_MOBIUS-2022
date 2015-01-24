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

import com.l2jserver.gameserver.enums.PrivateStoreType;
import com.l2jserver.gameserver.model.actor.L2Summon;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.items.L2Item;
import com.l2jserver.gameserver.model.items.instance.L2ItemInstance;
import com.l2jserver.gameserver.model.items.type.ActionType;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.serverpackets.ExAutoSoulShot;
import com.l2jserver.gameserver.network.serverpackets.SystemMessage;

/**
 * @author Unknown, UnAfraid
 */
public final class RequestAutoSoulShot extends L2GameClientPacket
{
	// format cd
	private int _itemId;
	private int _type; // 1 = on : 0 = off;
	
	@Override
	protected void readImpl()
	{
		_itemId = readD();
		_type = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if ((activeChar.getPrivateStoreType() == PrivateStoreType.NONE) && (activeChar.getActiveRequester() == null) && !activeChar.isDead())
		{
			final L2ItemInstance item = activeChar.getInventory().getItemByItemId(_itemId);
			if (item == null)
			{
				return;
			}
			
			if (_type == 1)
			{
				if (!activeChar.getInventory().canManipulateWithItemId(item.getId()))
				{
					activeChar.sendMessage("Cannot use this item.");
					return;
				}
				
				if (isSummonShot(item.getItem()))
				{
					if (activeChar.hasSummon())
					{
						final boolean isSoulshot = item.getEtcItem().getDefaultAction() == ActionType.SUMMON_SOULSHOT;
						final boolean isSpiritshot = item.getEtcItem().getDefaultAction() == ActionType.SUMMON_SPIRITSHOT;
						if (isSoulshot)
						{
							int soulshotCount = 0;
							final L2Summon pet = activeChar.getPet();
							if (pet != null)
							{
								soulshotCount += pet.getSoulShotsPerHit();
							}
							for (L2Summon servitor : activeChar.getServitors().values())
							{
								soulshotCount += servitor.getSoulShotsPerHit();
							}
							if (soulshotCount > item.getCount())
							{
								activeChar.sendPacket(SystemMessageId.YOU_DON_T_HAVE_ENOUGH_SOULSHOTS_NEEDED_FOR_A_PET_SERVITOR);
								return;
							}
						}
						else if (isSpiritshot)
						{
							int spiritshotCount = 0;
							final L2Summon pet = activeChar.getPet();
							if (pet != null)
							{
								spiritshotCount += pet.getSpiritShotsPerHit();
							}
							for (L2Summon servitor : activeChar.getServitors().values())
							{
								spiritshotCount += servitor.getSpiritShotsPerHit();
							}
							if (spiritshotCount > item.getCount())
							{
								activeChar.sendPacket(SystemMessageId.YOU_DON_T_HAVE_ENOUGH_SOULSHOTS_NEEDED_FOR_A_PET_SERVITOR);
								return;
							}
						}
						
						// Activate shots
						activeChar.addAutoSoulShot(_itemId);
						activeChar.sendPacket(new ExAutoSoulShot(_itemId, _type));
						
						// Send message
						final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.THE_AUTOMATIC_USE_OF_S1_HAS_BEEN_ACTIVATED);
						sm.addItemName(item);
						activeChar.sendPacket(sm);
						
						// Recharge summon's shots
						final L2Summon pet = activeChar.getPet();
						if (pet != null)
						{
							pet.rechargeShots(isSoulshot, isSpiritshot);
						}
						activeChar.getServitors().values().forEach(s ->
						{
							s.rechargeShots(isSoulshot, isSpiritshot);
						});
					}
					else
					{
						activeChar.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_A_SERVITOR_OR_PET_AND_THEREFORE_CANNOT_USE_THE_AUTOMATIC_USE_FUNCTION);
					}
				}
				else if (isPlayerShot(item.getItem()))
				{
					final boolean isSoulshot = (item.getEtcItem().getDefaultAction() == ActionType.SOULSHOT) || (item.getEtcItem().getDefaultAction() == ActionType.FISHINGSHOT);
					final boolean isSpiritshot = item.getEtcItem().getDefaultAction() == ActionType.SPIRITSHOT;
					if ((activeChar.getActiveWeaponItem() == activeChar.getFistsWeaponItem()) || (item.getItem().getCrystalType() != activeChar.getActiveWeaponItem().getItemGradeSPlus()))
					{
						activeChar.sendPacket(isSoulshot ? SystemMessageId.YOUR_SPIRITSHOT_DOES_NOT_MATCH_THE_WEAPON_S_GRADE : SystemMessageId.THE_SOULSHOT_YOU_ARE_ATTEMPTING_TO_USE_DOES_NOT_MATCH_THE_GRADE_OF_YOUR_EQUIPPED_WEAPON);
						return;
					}
					
					// Activate shots
					activeChar.addAutoSoulShot(_itemId);
					activeChar.sendPacket(new ExAutoSoulShot(_itemId, _type));
					
					// Send message
					final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.THE_AUTOMATIC_USE_OF_S1_HAS_BEEN_ACTIVATED);
					sm.addItemName(item);
					activeChar.sendPacket(sm);
					
					// Recharge player's shots
					activeChar.rechargeShots(isSoulshot, isSpiritshot);
				}
			}
			else if (_type == 0)
			{
				// Cancel auto shots
				activeChar.removeAutoSoulShot(_itemId);
				activeChar.sendPacket(new ExAutoSoulShot(_itemId, _type));
				
				// Send message
				final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.THE_AUTOMATIC_USE_OF_S1_HAS_BEEN_DEACTIVATED);
				sm.addItemName(item);
				activeChar.sendPacket(sm);
			}
		}
	}
	
	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
	
	public static boolean isPlayerShot(L2Item item)
	{
		switch (item.getDefaultAction())
		{
			case SPIRITSHOT:
			case SOULSHOT:
			case FISHINGSHOT:
				return true;
			default:
				return false;
		}
	}
	
	public static boolean isSummonShot(L2Item item)
	{
		switch (item.getDefaultAction())
		{
			case SUMMON_SPIRITSHOT:
			case SUMMON_SOULSHOT:
				return true;
			default:
				return false;
		}
	}
	
	@Override
	public String getType()
	{
		return getClass().getSimpleName();
	}
}
