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
package com.l2jmobius.gameserver.engines;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.l2jmobius.gameserver.templates.StatsSet;
import com.l2jmobius.gameserver.templates.item.L2Armor;
import com.l2jmobius.gameserver.templates.item.L2ArmorType;
import com.l2jmobius.gameserver.templates.item.L2EtcItem;
import com.l2jmobius.gameserver.templates.item.L2EtcItemType;
import com.l2jmobius.gameserver.templates.item.L2Item;
import com.l2jmobius.gameserver.templates.item.L2Weapon;
import com.l2jmobius.gameserver.templates.item.L2WeaponType;

/**
 * @author mkizub, JIV
 */
final class DocumentItem extends DocumentBase
{
	private Item _currentItem = null;
	private final List<L2Item> _itemsInFile = new ArrayList<>();
	
	private static final Map<String, Integer> _slots = new HashMap<>();
	static
	{
		_slots.put("chest", L2Item.SLOT_CHEST);
		_slots.put("fullarmor", L2Item.SLOT_FULL_ARMOR);
		_slots.put("head", L2Item.SLOT_HEAD);
		_slots.put("hair", L2Item.SLOT_HAIR);
		_slots.put("face", L2Item.SLOT_FACE);
		_slots.put("dhair", L2Item.SLOT_DHAIR);
		_slots.put("underwear", L2Item.SLOT_UNDERWEAR);
		_slots.put("back", L2Item.SLOT_BACK);
		_slots.put("neck", L2Item.SLOT_NECK);
		_slots.put("legs", L2Item.SLOT_LEGS);
		_slots.put("feet", L2Item.SLOT_FEET);
		_slots.put("gloves", L2Item.SLOT_GLOVES);
		_slots.put("chest,legs", L2Item.SLOT_CHEST | L2Item.SLOT_LEGS);
		_slots.put("rhand", L2Item.SLOT_R_HAND);
		_slots.put("lhand", L2Item.SLOT_L_HAND);
		_slots.put("lrhand", L2Item.SLOT_LR_HAND);
		_slots.put("rear,lear", L2Item.SLOT_R_EAR | L2Item.SLOT_L_EAR);
		_slots.put("rfinger,lfinger", L2Item.SLOT_R_FINGER | L2Item.SLOT_L_FINGER);
		_slots.put("none", L2Item.SLOT_NONE);
		_slots.put("wolf", L2Item.SLOT_WOLF); // for wolf
		_slots.put("hatchling", L2Item.SLOT_HATCHLING); // for hatchling
		_slots.put("strider", L2Item.SLOT_STRIDER); // for strider
		_slots.put("babypet", L2Item.SLOT_BABYPET); // for babypet
	}
	private static final Map<String, L2WeaponType> _weaponTypes = new HashMap<>();
	static
	{
		_weaponTypes.put("blunt", L2WeaponType.BLUNT);
		_weaponTypes.put("bow", L2WeaponType.BOW);
		_weaponTypes.put("dagger", L2WeaponType.DAGGER);
		_weaponTypes.put("dual", L2WeaponType.DUAL);
		_weaponTypes.put("dualfist", L2WeaponType.DUALFIST);
		_weaponTypes.put("etc", L2WeaponType.ETC);
		_weaponTypes.put("fist", L2WeaponType.FIST);
		_weaponTypes.put("none", L2WeaponType.NONE); // these are shields!
		_weaponTypes.put("pole", L2WeaponType.POLE);
		_weaponTypes.put("sword", L2WeaponType.SWORD);
		_weaponTypes.put("bigsword", L2WeaponType.BIGSWORD); // Two-Handed Swords
		_weaponTypes.put("pet", L2WeaponType.PET); // Pet Weapon
		_weaponTypes.put("rod", L2WeaponType.ROD); // Fishing Rods
		_weaponTypes.put("bigblunt", L2WeaponType.BIGBLUNT); // Two handed blunt
	}
	private static final Map<String, L2ArmorType> _armorTypes = new HashMap<>();
	static
	{
		_armorTypes.put("none", L2ArmorType.NONE);
		_armorTypes.put("light", L2ArmorType.LIGHT);
		_armorTypes.put("heavy", L2ArmorType.HEAVY);
		_armorTypes.put("magic", L2ArmorType.MAGIC);
		_armorTypes.put("pet", L2ArmorType.PET);
	}
	
	public DocumentItem(File file)
	{
		super(file);
	}
	
	@Override
	protected StatsSet getStatsSet()
	{
		return _currentItem.set;
	}
	
	@Override
	protected String getTableValue(String name)
	{
		return _tables.get(name)[_currentItem.currentLevel];
	}
	
	@Override
	protected String getTableValue(String name, int idx)
	{
		return _tables.get(name)[idx - 1];
	}
	
	@Override
	protected void parseDocument(Document doc)
	{
		for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("item".equalsIgnoreCase(d.getNodeName()))
					{
						try
						{
							_currentItem = new Item();
							parseItem(d);
							_itemsInFile.add(_currentItem.item);
							resetTable();
						}
						catch (Exception e)
						{
							LOGGER.log(Level.WARNING, "Cannot create item " + _currentItem.id, e);
						}
					}
				}
			}
		}
	}
	
	protected void parseItem(Node n)
	{
		int itemId = Integer.parseInt(n.getAttributes().getNamedItem("id").getNodeValue());
		String className = n.getAttributes().getNamedItem("type").getNodeValue();
		String itemName = n.getAttributes().getNamedItem("name").getNodeValue();
		
		_currentItem.id = itemId;
		_currentItem.name = itemName;
		_currentItem.set = new StatsSet();
		_currentItem.set.set("item_id", itemId);
		_currentItem.set.set("name", itemName);
		
		Node first = n.getFirstChild();
		for (n = first; n != null; n = n.getNextSibling())
		{
			if ("set".equals(n.getNodeName()))
			{
				if (_currentItem.item != null)
				{
					throw new IllegalStateException("Item created but set node found! Item " + itemId);
				}
				parseBeanSet(n, _currentItem.set, 1);
			}
		}
		
		if (className.equals("Weapon"))
		{
			_currentItem.type = _weaponTypes.get(_currentItem.set.getString("weapon_type"));
			
			// lets see if this is a shield
			if (_currentItem.type == L2WeaponType.NONE)
			{
				_currentItem.set.set("type1", L2Item.TYPE1_SHIELD_ARMOR);
				_currentItem.set.set("type2", L2Item.TYPE2_SHIELD_ARMOR);
			}
			else
			{
				_currentItem.set.set("type1", L2Item.TYPE1_WEAPON_RING_EARRING_NECKLACE);
				_currentItem.set.set("type2", L2Item.TYPE2_WEAPON);
			}
			
			if (_currentItem.type == L2WeaponType.PET)
			{
				_currentItem.set.set("type1", L2Item.TYPE1_WEAPON_RING_EARRING_NECKLACE);
				
				switch (_currentItem.set.getString("bodypart"))
				{
					case "wolf":
					{
						_currentItem.set.set("type2", L2Item.TYPE2_PET_WOLF);
						break;
					}
					case "hatchling":
					{
						_currentItem.set.set("type2", L2Item.TYPE2_PET_HATCHLING);
						break;
					}
					case "babypet":
					{
						_currentItem.set.set("type2", L2Item.TYPE2_PET_BABY);
						break;
					}
					default:
					{
						_currentItem.set.set("type2", L2Item.TYPE2_PET_STRIDER);
						break;
					}
				}
				
				_currentItem.set.set("bodypart", L2Item.SLOT_R_HAND);
			}
		}
		else if (className.equals("Armor"))
		{
			_currentItem.type = _armorTypes.get(_currentItem.set.getString("armor_type"));
			
			final int bodypart = _slots.get(_currentItem.set.getString("bodypart"));
			if ((bodypart == L2Item.SLOT_NECK) || (bodypart == L2Item.SLOT_HAIR) || (bodypart == L2Item.SLOT_FACE) || (bodypart == L2Item.SLOT_DHAIR) || ((bodypart & L2Item.SLOT_L_EAR) != 0) || ((bodypart & L2Item.SLOT_L_FINGER) != 0))
			{
				_currentItem.set.set("type1", L2Item.TYPE1_WEAPON_RING_EARRING_NECKLACE);
				_currentItem.set.set("type2", L2Item.TYPE2_ACCESSORY);
			}
			else
			{
				_currentItem.set.set("type1", L2Item.TYPE1_SHIELD_ARMOR);
				_currentItem.set.set("type2", L2Item.TYPE2_SHIELD_ARMOR);
			}
			
			if (_currentItem.type == L2ArmorType.PET)
			{
				_currentItem.set.set("type1", L2Item.TYPE1_SHIELD_ARMOR);
				
				switch (_currentItem.set.getString("bodypart"))
				{
					case "wolf":
					{
						_currentItem.set.set("type2", L2Item.TYPE2_PET_WOLF);
						break;
					}
					case "hatchling":
					{
						_currentItem.set.set("type2", L2Item.TYPE2_PET_HATCHLING);
						break;
					}
					case "babypet":
					{
						_currentItem.set.set("type2", L2Item.TYPE2_PET_BABY);
						break;
					}
					default:
					{
						_currentItem.set.set("type2", L2Item.TYPE2_PET_STRIDER);
						break;
					}
				}
				
				_currentItem.set.set("bodypart", L2Item.SLOT_CHEST);
			}
		}
		else
		{
			_currentItem.set.set("type1", L2Item.TYPE1_ITEM_QUESTITEM_ADENA);
			_currentItem.set.set("type2", L2Item.TYPE2_OTHER);
			
			final String itemType = _currentItem.set.getString("item_type");
			switch (itemType)
			{
				case "none":
				{
					_currentItem.type = L2EtcItemType.OTHER; // only for default
					break;
				}
				case "castle_guard":
				{
					_currentItem.type = L2EtcItemType.SCROLL; // dummy
					break;
				}
				case "pet_collar":
				{
					_currentItem.type = L2EtcItemType.PET_COLLAR;
					break;
				}
				case "potion":
				{
					_currentItem.type = L2EtcItemType.POTION;
					break;
				}
				case "recipe":
				{
					_currentItem.type = L2EtcItemType.RECEIPE;
					break;
				}
				case "scroll":
				{
					_currentItem.type = L2EtcItemType.SCROLL;
					break;
				}
				case "seed":
				{
					_currentItem.type = L2EtcItemType.SEED;
					break;
				}
				case "shot":
				{
					_currentItem.type = L2EtcItemType.SHOT;
					break;
				}
				case "spellbook":
				{
					_currentItem.type = L2EtcItemType.SPELLBOOK; // Spellbook, Amulet, Blueprint
					break;
				}
				case "herb":
				{
					_currentItem.type = L2EtcItemType.HERB;
					break;
				}
				case "arrow":
				{
					_currentItem.type = L2EtcItemType.ARROW;
					_currentItem.set.set("bodypart", L2Item.SLOT_L_HAND);
					break;
				}
				case "quest":
				{
					_currentItem.type = L2EtcItemType.QUEST;
					_currentItem.set.set("type2", L2Item.TYPE2_QUEST);
					break;
				}
				case "lure":
				{
					_currentItem.type = L2EtcItemType.OTHER;
					_currentItem.set.set("bodypart", L2Item.SLOT_L_HAND);
					break;
				}
				default:
				{
					_currentItem.type = L2EtcItemType.OTHER;
					break;
				}
			}
			
			final String consume = _currentItem.set.getString("consume_type");
			switch (consume)
			{
				case "asset":
				{
					_currentItem.type = L2EtcItemType.MONEY;
					_currentItem.set.set("stackable", true);
					_currentItem.set.set("type2", L2Item.TYPE2_MONEY);
					break;
				}
				case "stackable":
				{
					_currentItem.set.set("stackable", true);
					break;
				}
				default:
				{
					_currentItem.set.set("stackable", false);
					break;
				}
			}
		}
		
		for (n = first; n != null; n = n.getNextSibling())
		{
			if ("for".equals(n.getNodeName()))
			{
				makeItem();
				parseTemplate(n, _currentItem.item);
			}
		}
		
		// bah! in this point item doesn't have to be still created
		makeItem();
	}
	
	private void makeItem()
	{
		if (_currentItem.item != null)
		{
			return; // item is already created
		}
		
		if (_currentItem.type instanceof L2ArmorType)
		{
			_currentItem.item = new L2Armor((L2ArmorType) _currentItem.type, _currentItem.set);
		}
		else if (_currentItem.type instanceof L2WeaponType)
		{
			_currentItem.item = new L2Weapon((L2WeaponType) _currentItem.type, _currentItem.set);
		}
		else if (_currentItem.type instanceof L2EtcItemType)
		{
			_currentItem.item = new L2EtcItem((L2EtcItemType) _currentItem.type, _currentItem.set);
		}
		else
		{
			throw new Error("Unknown item type for " + _currentItem.set.getInteger("item_id") + " " + _currentItem.type);
		}
	}
	
	public List<L2Item> getItemList()
	{
		return _itemsInFile;
	}
}
