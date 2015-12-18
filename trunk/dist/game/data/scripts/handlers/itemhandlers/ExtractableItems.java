/*
 * Copyright (C) 2004-2015 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package handlers.itemhandlers;

import java.util.ArrayList;
import java.util.List;

import com.l2jserver.Config;
import com.l2jserver.gameserver.handler.IItemHandler;
import com.l2jserver.gameserver.model.L2ExtractableProduct;
import com.l2jserver.gameserver.model.actor.L2Playable;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.items.L2EtcItem;
import com.l2jserver.gameserver.model.items.instance.L2ItemInstance;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jserver.gameserver.network.serverpackets.SystemMessage;
import com.l2jserver.util.Rnd;

/**
 * Extractable Items handler.
 * @author HorridoJoho, Mobius
 */
public class ExtractableItems implements IItemHandler
{
	@Override
	public boolean useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
	{
		if (!playable.isPlayer())
		{
			playable.sendPacket(SystemMessageId.YOUR_PET_CANNOT_CARRY_THIS_ITEM);
			return false;
		}
		
		final L2PcInstance activeChar = playable.getActingPlayer();
		final L2EtcItem etcitem = (L2EtcItem) item.getItem();
		final List<L2ExtractableProduct> exitems = etcitem.getExtractableItems();
		if (exitems == null)
		{
			_log.info("No extractable data defined for " + etcitem);
			return false;
		}
		
		// destroy item
		if (!activeChar.destroyItem("Extract", item.getObjectId(), 1, activeChar, true))
		{
			return false;
		}
		
		List<L2ItemInstance> extractedItems = new ArrayList<>();
		List<L2ItemInstance> enchantedItems = new ArrayList<>();
		if (etcitem.getExtractableCountMin() > 0)
		{
			while (extractedItems.size() < etcitem.getExtractableCountMin())
			{
				for (L2ExtractableProduct expi : exitems)
				{
					if ((etcitem.getExtractableCountMax() > 0) && (extractedItems.size() == etcitem.getExtractableCountMax()))
					{
						break;
					}
					
					if (Rnd.get(100000) <= expi.getChance())
					{
						final int min = (int) (expi.getMin() * Config.RATE_EXTRACTABLE);
						final int max = (int) (expi.getMax() * Config.RATE_EXTRACTABLE);
						
						int createItemAmount = (max == min) ? min : (Rnd.get((max - min) + 1) + min);
						if (createItemAmount == 0)
						{
							continue;
						}
						
						// Do not extract the same item.
						boolean alreadyExtracted = false;
						for (L2ItemInstance i : extractedItems)
						{
							if (i.getItem().getId() == expi.getId())
							{
								alreadyExtracted = true;
								break;
							}
						}
						if (alreadyExtracted && (exitems.size() >= etcitem.getExtractableCountMax()))
						{
							continue;
						}
						
						if (item.isStackable() || (createItemAmount == 1))
						{
							final L2ItemInstance newItem = activeChar.addItem("Extract", expi.getId(), createItemAmount, activeChar, false);
							if (expi.getMaxEnchant() > 0)
							{
								newItem.setEnchantLevel(Rnd.get(expi.getMinEnchant(), expi.getMaxEnchant()));
								enchantedItems.add(newItem);
							}
							extractedItems.add(newItem);
							sendMessage(activeChar, newItem);
						}
						else
						{
							while (createItemAmount > 0)
							{
								final L2ItemInstance newItem = activeChar.addItem("Extract", expi.getId(), 1, activeChar, false);
								if (expi.getMaxEnchant() > 0)
								{
									newItem.setEnchantLevel(Rnd.get(expi.getMinEnchant(), expi.getMaxEnchant()));
									enchantedItems.add(newItem);
								}
								extractedItems.add(newItem);
								sendMessage(activeChar, newItem);
								createItemAmount--;
							}
						}
					}
				}
			}
		}
		else
		{
			for (L2ExtractableProduct expi : exitems)
			{
				if ((etcitem.getExtractableCountMax() > 0) && (extractedItems.size() == etcitem.getExtractableCountMax()))
				{
					break;
				}
				
				if (Rnd.get(100000) <= expi.getChance())
				{
					final int min = (int) (expi.getMin() * Config.RATE_EXTRACTABLE);
					final int max = (int) (expi.getMax() * Config.RATE_EXTRACTABLE);
					
					int createItemAmount = (max == min) ? min : (Rnd.get((max - min) + 1) + min);
					if (createItemAmount == 0)
					{
						continue;
					}
					
					if (item.isStackable() || (createItemAmount == 1))
					{
						final L2ItemInstance newItem = activeChar.addItem("Extract", expi.getId(), createItemAmount, activeChar, false);
						if (expi.getMaxEnchant() > 0)
						{
							newItem.setEnchantLevel(Rnd.get(expi.getMinEnchant(), expi.getMaxEnchant()));
							enchantedItems.add(newItem);
						}
						extractedItems.add(newItem);
						sendMessage(activeChar, newItem);
					}
					else
					{
						while (createItemAmount > 0)
						{
							final L2ItemInstance newItem = activeChar.addItem("Extract", expi.getId(), 1, activeChar, false);
							if (expi.getMaxEnchant() > 0)
							{
								newItem.setEnchantLevel(Rnd.get(expi.getMinEnchant(), expi.getMaxEnchant()));
								enchantedItems.add(newItem);
							}
							extractedItems.add(newItem);
							sendMessage(activeChar, newItem);
							createItemAmount--;
						}
					}
				}
			}
		}
		
		if (extractedItems.size() == 0)
		{
			activeChar.sendPacket(SystemMessageId.THERE_WAS_NOTHING_FOUND_INSIDE);
		}
		if (!enchantedItems.isEmpty())
		{
			InventoryUpdate playerIU = new InventoryUpdate();
			for (L2ItemInstance i : enchantedItems)
			{
				playerIU.addModifiedItem(i);
			}
			activeChar.sendPacket(playerIU);
		}
		
		return true;
	}
	
	private void sendMessage(L2PcInstance player, L2ItemInstance item)
	{
		if (item.getCount() > 1)
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_OBTAINED_S2_S1);
			sm.addItemName(item);
			sm.addLong(item.getCount());
			player.sendPacket(sm);
		}
		else if (item.getEnchantLevel() > 0)
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_OBTAINED_A_S1_S2);
			sm.addInt(item.getEnchantLevel());
			sm.addItemName(item);
			player.sendPacket(sm);
		}
		else
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_OBTAINED_S1);
			sm.addItemName(item);
			player.sendPacket(sm);
		}
	}
}
