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
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import org.l2jmobius.commons.util.IXmlReader;
import org.l2jmobius.gameserver.data.ItemTable;
import org.l2jmobius.gameserver.enums.StatFunction;
import org.l2jmobius.gameserver.model.items.ItemTemplate;
import org.l2jmobius.gameserver.model.items.instance.Item;
import org.l2jmobius.gameserver.model.items.type.CrystalType;
import org.l2jmobius.gameserver.model.stats.Stat;
import org.l2jmobius.gameserver.model.stats.functions.FuncTemplate;

/**
 * This class holds the Enchant HP Bonus Data.
 * @author MrPoke, Zoey76
 */
public class EnchantItemHPBonusData implements IXmlReader
{
	private final Map<CrystalType, List<Integer>> _armorHPBonuses = new EnumMap<>(CrystalType.class);
	
	private static final float FULL_ARMOR_MODIFIER = 1.5f; // TODO: Move it to config!
	
	/**
	 * Instantiates a new enchant hp bonus data.
	 */
	protected EnchantItemHPBonusData()
	{
		load();
	}
	
	@Override
	public void parseDocument(Document doc, File f)
	{
		for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("enchantHP".equalsIgnoreCase(d.getNodeName()))
					{
						final List<Integer> bonuses = new ArrayList<>(12);
						for (Node e = d.getFirstChild(); e != null; e = e.getNextSibling())
						{
							if ("bonus".equalsIgnoreCase(e.getNodeName()))
							{
								bonuses.add(Integer.parseInt(e.getTextContent()));
							}
						}
						_armorHPBonuses.put(parseEnum(d.getAttributes(), CrystalType.class, "grade"), bonuses);
					}
				}
			}
		}
		
		if (!_armorHPBonuses.isEmpty())
		{
			final ItemTable it = ItemTable.getInstance();
			// Armors
			final Collection<Integer> armorIds = it.getAllArmorsId();
			for (Integer itemId : armorIds)
			{
				final ItemTemplate item = it.getTemplate(itemId);
				if ((item != null) && (item.getCrystalType() != CrystalType.NONE))
				{
					switch (item.getBodyPart())
					{
						case ItemTemplate.SLOT_CHEST:
						case ItemTemplate.SLOT_FEET:
						case ItemTemplate.SLOT_GLOVES:
						case ItemTemplate.SLOT_HEAD:
						case ItemTemplate.SLOT_LEGS:
						case ItemTemplate.SLOT_BACK:
						case ItemTemplate.SLOT_FULL_ARMOR:
						case ItemTemplate.SLOT_UNDERWEAR:
						case ItemTemplate.SLOT_L_HAND:
						case ItemTemplate.SLOT_BELT:
						{
							item.attach(new FuncTemplate(null, null, StatFunction.ENCHANTHP.getName(), -1, Stat.MAX_HP, 0));
							break;
						}
						default:
						{
							break;
						}
					}
				}
			}
		}
	}
	
	@Override
	public void load()
	{
		_armorHPBonuses.clear();
		parseDatapackFile("data/stats/enchantHPBonus.xml");
		LOGGER.info(getClass().getSimpleName() + ": Loaded " + _armorHPBonuses.size() + " enchant HP bonuses.");
	}
	
	/**
	 * Gets the HP bonus.
	 * @param item the item
	 * @return the HP bonus
	 */
	public int getHPBonus(Item item)
	{
		final List<Integer> values = _armorHPBonuses.get(item.getItem().getCrystalTypePlus());
		if ((values == null) || values.isEmpty() || (item.getOlyEnchantLevel() <= 0))
		{
			return 0;
		}
		
		final int bonus = values.get(Math.min(item.getOlyEnchantLevel(), values.size()) - 1);
		if (item.getItem().getBodyPart() == ItemTemplate.SLOT_FULL_ARMOR)
		{
			return (int) (bonus * FULL_ARMOR_MODIFIER);
		}
		return bonus;
	}
	
	/**
	 * Gets the single instance of EnchantHPBonusData.
	 * @return single instance of EnchantHPBonusData
	 */
	public static EnchantItemHPBonusData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final EnchantItemHPBonusData INSTANCE = new EnchantItemHPBonusData();
	}
}
