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
package handlers.itemhandlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.l2jmobius.Config;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.data.ItemTable;
import org.l2jmobius.gameserver.handler.IItemHandler;
import org.l2jmobius.gameserver.model.ExtractableProduct;
import org.l2jmobius.gameserver.model.actor.Playable;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.item.EtcItem;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.InventoryUpdate;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * Extractable Items handler.
 * @author HorridoJoho, Mobius
 */
public class ExtractableItems implements IItemHandler
{
	@Override
	public boolean useItem(Playable playable, Item item, boolean forceUse)
	{
		if (!playable.isPlayer())
		{
			playable.sendPacket(SystemMessageId.YOUR_PET_CANNOT_CARRY_THIS_ITEM);
			return false;
		}
		
		final Player player = playable.getActingPlayer();
		final EtcItem etcitem = (EtcItem) item.getItem();
		final List<ExtractableProduct> exitems = etcitem.getExtractableItems();
		if (exitems == null)
		{
			LOGGER.info("No extractable data defined for " + etcitem);
			return false;
		}
		
		if (!player.isInventoryUnder80(false))
		{
			player.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_THE_ITEM_OWNERSHIP_LIMIT_AND_YOU_CANNOT_TAKE_THE_ITEM_CHECK_ITEM_OWNERSHIP_TIME_LIMITS_FOR_THE_INVENTORY_PLEASE);
			return false;
		}
		
		// destroy item
		if (!player.destroyItem("Extract", item.getObjectId(), 1, player, true))
		{
			return false;
		}
		
		boolean primeReward = false;
		final Map<Item, Long> extractedItems = new HashMap<>();
		final List<Item> enchantedItems = new ArrayList<>();
		if (etcitem.getExtractableCountMin() > 0)
		{
			while (extractedItems.size() < etcitem.getExtractableCountMin())
			{
				for (ExtractableProduct expi : exitems)
				{
					if ((etcitem.getExtractableCountMax() > 0) && (extractedItems.size() == etcitem.getExtractableCountMax()))
					{
						break;
					}
					
					if (Rnd.get(100000) <= expi.getChance())
					{
						final long min = (long) (expi.getMin() * Config.RATE_EXTRACTABLE);
						final long max = (long) (expi.getMax() * Config.RATE_EXTRACTABLE);
						long createItemAmount = (max == min) ? min : (Rnd.get((max - min) + 1) + min);
						if (createItemAmount == 0)
						{
							continue;
						}
						
						// Do not extract the same item.
						boolean alreadyExtracted = false;
						for (Item i : extractedItems.keySet())
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
						
						if (expi.getId() == -1) // Prime points
						{
							player.setPrimePoints(player.getPrimePoints() + (int) createItemAmount);
							player.sendMessage("You have obtained " + (createItemAmount / 100) + " Euro!");
							primeReward = true;
							continue;
						}
						
						if (ItemTable.getInstance().getTemplate(expi.getId()).isStackable() || (createItemAmount == 1))
						{
							final Item newItem = player.addItem("Extract", expi.getId(), createItemAmount, player, false);
							if (expi.getMaxEnchant() > 0)
							{
								newItem.setEnchantLevel(Rnd.get(expi.getMinEnchant(), expi.getMaxEnchant()));
								enchantedItems.add(newItem);
							}
							addItem(extractedItems, newItem, createItemAmount);
						}
						else
						{
							while (createItemAmount > 0)
							{
								final Item newItem = player.addItem("Extract", expi.getId(), 1, player, false);
								if (expi.getMaxEnchant() > 0)
								{
									newItem.setEnchantLevel(Rnd.get(expi.getMinEnchant(), expi.getMaxEnchant()));
									enchantedItems.add(newItem);
								}
								addItem(extractedItems, newItem, 1);
								createItemAmount--;
							}
						}
					}
				}
			}
		}
		else
		{
			for (ExtractableProduct expi : exitems)
			{
				if ((etcitem.getExtractableCountMax() > 0) && (extractedItems.size() == etcitem.getExtractableCountMax()))
				{
					break;
				}
				
				if (Rnd.get(100000) <= expi.getChance())
				{
					final long min = (long) (expi.getMin() * Config.RATE_EXTRACTABLE);
					final long max = (long) (expi.getMax() * Config.RATE_EXTRACTABLE);
					long createItemAmount = (max == min) ? min : (Rnd.get((max - min) + 1) + min);
					if (createItemAmount == 0)
					{
						continue;
					}
					
					if (expi.getId() == -1) // Prime points
					{
						player.setPrimePoints(player.getPrimePoints() + (int) createItemAmount);
						player.sendMessage("You have obtained " + (createItemAmount / 100) + " Euro!");
						primeReward = true;
						continue;
					}
					
					if (ItemTable.getInstance().getTemplate(expi.getId()).isStackable() || (createItemAmount == 1))
					{
						final Item newItem = player.addItem("Extract", expi.getId(), createItemAmount, player, false);
						if (expi.getMaxEnchant() > 0)
						{
							newItem.setEnchantLevel(Rnd.get(expi.getMinEnchant(), expi.getMaxEnchant()));
							enchantedItems.add(newItem);
						}
						addItem(extractedItems, newItem, createItemAmount);
					}
					else
					{
						while (createItemAmount > 0)
						{
							final Item newItem = player.addItem("Extract", expi.getId(), 1, player, false);
							if (expi.getMaxEnchant() > 0)
							{
								newItem.setEnchantLevel(Rnd.get(expi.getMinEnchant(), expi.getMaxEnchant()));
								enchantedItems.add(newItem);
							}
							addItem(extractedItems, newItem, 1);
							createItemAmount--;
						}
					}
				}
			}
		}
		
		if (extractedItems.isEmpty() && !primeReward)
		{
			player.sendPacket(SystemMessageId.FAILED_TO_CHANGE_THE_ITEM);
		}
		if (!enchantedItems.isEmpty())
		{
			final InventoryUpdate playerIU = new InventoryUpdate();
			for (Item i : enchantedItems)
			{
				playerIU.addModifiedItem(i);
			}
			player.sendPacket(playerIU);
		}
		
		for (Entry<Item, Long> entry : extractedItems.entrySet())
		{
			sendMessage(player, entry.getKey(), entry.getValue().longValue());
		}
		
		return true;
	}
	
	private void addItem(Map<Item, Long> extractedItems, Item newItem, long count)
	{
		if (extractedItems.containsKey(newItem))
		{
			extractedItems.put(newItem, extractedItems.get(newItem) + count);
		}
		else
		{
			extractedItems.put(newItem, count);
		}
	}
	
	private void sendMessage(Player player, Item item, long count)
	{
		final SystemMessage sm;
		if (count > 1)
		{
			sm = new SystemMessage(SystemMessageId.YOU_HAVE_OBTAINED_S2_S1);
			sm.addItemName(item);
			sm.addLong(count);
		}
		else if (item.getEnchantLevel() > 0)
		{
			sm = new SystemMessage(SystemMessageId.YOU_HAVE_OBTAINED_S1_S2);
			sm.addInt(item.getEnchantLevel());
			sm.addItemName(item);
		}
		else
		{
			sm = new SystemMessage(SystemMessageId.YOU_HAVE_OBTAINED_S1);
			sm.addItemName(item);
		}
		player.sendPacket(sm);
	}
}
