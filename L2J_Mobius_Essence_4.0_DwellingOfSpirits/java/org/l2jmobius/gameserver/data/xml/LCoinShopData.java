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
package org.l2jmobius.gameserver.data.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import org.l2jmobius.commons.util.IXmlReader;
import org.l2jmobius.gameserver.data.ItemTable;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.holders.LCoinShopProductHolder;
import org.l2jmobius.gameserver.model.items.Item;

/**
 * @author Mobius
 */
public class LCoinShopData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(LCoinShopData.class.getName());
	
	private final List<LCoinShopProductHolder> _products = new ArrayList<>();
	
	protected LCoinShopData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_products.clear();
		parseDatapackFile("data/LCoinShop.xml");
		
		if (!_products.isEmpty())
		{
			LOGGER.info(getClass().getSimpleName() + ": Loaded " + _products.size() + " items.");
		}
		else
		{
			LOGGER.info(getClass().getSimpleName() + ": System is disabled.");
		}
	}
	
	@Override
	public void parseDocument(Document doc, File f)
	{
		for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				final NamedNodeMap at = n.getAttributes();
				final Node attribute = at.getNamedItem("enabled");
				if ((attribute != null) && Boolean.parseBoolean(attribute.getNodeValue()))
				{
					for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					{
						if ("product".equalsIgnoreCase(d.getNodeName()))
						{
							NamedNodeMap attrs = d.getAttributes();
							Node att;
							final StatSet set = new StatSet();
							for (int i = 0; i < attrs.getLength(); i++)
							{
								att = attrs.item(i);
								set.set(att.getNodeName(), att.getNodeValue());
							}
							
							final int id = parseInteger(attrs, "id");
							final int category = parseInteger(attrs, "category");
							final int minLevel = parseInteger(attrs, "minLevel", 1);
							final int maxLevel = parseInteger(attrs, "maxLevel", 999);
							final int[] ingredientIds = new int[3];
							ingredientIds[0] = 0;
							ingredientIds[1] = 0;
							ingredientIds[2] = 0;
							final long[] ingredientQuantities = new long[3];
							ingredientQuantities[0] = 0;
							ingredientQuantities[1] = 0;
							ingredientQuantities[2] = 0;
							int productionId = 0;
							int accountDailyLimit = 0;
							int accountBuyLimit = 0;
							for (Node b = d.getFirstChild(); b != null; b = b.getNextSibling())
							{
								attrs = b.getAttributes();
								
								if ("ingredient".equalsIgnoreCase(b.getNodeName()))
								{
									final int ingredientId = parseInteger(attrs, "id");
									final long ingredientQuantity = parseLong(attrs, "count", 1L);
									
									final Item item = ItemTable.getInstance().getTemplate(ingredientId);
									if (item == null)
									{
										LOGGER.severe(getClass().getSimpleName() + ": Item template null for itemId: " + productionId + " productId: " + id);
										continue;
									}
									
									if (ingredientIds[0] == 0)
									{
										ingredientIds[0] = ingredientId;
									}
									else if (ingredientIds[1] == 0)
									{
										ingredientIds[1] = ingredientId;
									}
									else
									{
										ingredientIds[2] = ingredientId;
									}
									
									if (ingredientQuantities[0] == 0)
									{
										ingredientQuantities[0] = ingredientQuantity;
									}
									else if (ingredientQuantities[1] == 0)
									{
										ingredientQuantities[1] = ingredientQuantity;
									}
									else
									{
										ingredientQuantities[2] = ingredientQuantity;
									}
								}
								else if ("production".equalsIgnoreCase(b.getNodeName()))
								{
									productionId = parseInteger(attrs, "id");
									accountDailyLimit = parseInteger(attrs, "accountDailyLimit", 0);
									accountBuyLimit = parseInteger(attrs, "accountBuyLimit", 0);
									
									final Item item = ItemTable.getInstance().getTemplate(productionId);
									if (item == null)
									{
										LOGGER.severe(getClass().getSimpleName() + ": Item template null for itemId: " + productionId + " productId: " + id);
										continue;
									}
								}
							}
							
							_products.add(new LCoinShopProductHolder(id, category, minLevel, maxLevel, ingredientIds, ingredientQuantities, productionId, 0, 0, 0, 0, 0, 0, 0, accountDailyLimit, accountBuyLimit));
						}
					}
				}
			}
		}
	}
	
	public LCoinShopProductHolder getProduct(int id)
	{
		for (LCoinShopProductHolder product : _products)
		{
			if (product.getId() == id)
			{
				return product;
			}
		}
		return null;
	}
	
	public Collection<LCoinShopProductHolder> getProducts()
	{
		return _products;
	}
	
	public static LCoinShopData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final LCoinShopData INSTANCE = new LCoinShopData();
	}
}
