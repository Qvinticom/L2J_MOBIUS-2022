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
package org.l2jmobius.gameserver.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.item.Armor;
import org.l2jmobius.gameserver.model.item.EtcItem;
import org.l2jmobius.gameserver.model.item.ItemTemplate;
import org.l2jmobius.gameserver.model.item.Weapon;
import org.l2jmobius.gameserver.model.item.type.ArmorType;
import org.l2jmobius.gameserver.model.item.type.EtcItemType;
import org.l2jmobius.gameserver.model.item.type.WeaponType;

/**
 * @author mkizub, JIV
 */
public class DocumentItem extends DocumentBase
{
	private DocumentItemDataHolder _currentItem = null;
	private final List<ItemTemplate> _itemsInFile = new ArrayList<>();
	
	private class DocumentItemDataHolder
	{
		public DocumentItemDataHolder()
		{
		}
		
		public int id;
		public Enum<?> type;
		public StatSet set;
		public int currentLevel;
		public ItemTemplate item;
	}
	
	private static final Map<String, Integer> _slots = new HashMap<>();
	static
	{
		_slots.put("chest", ItemTemplate.SLOT_CHEST);
		_slots.put("fullarmor", ItemTemplate.SLOT_FULL_ARMOR);
		_slots.put("head", ItemTemplate.SLOT_HEAD);
		_slots.put("hair", ItemTemplate.SLOT_HAIR);
		_slots.put("face", ItemTemplate.SLOT_FACE);
		_slots.put("dhair", ItemTemplate.SLOT_DHAIR);
		_slots.put("underwear", ItemTemplate.SLOT_UNDERWEAR);
		_slots.put("back", ItemTemplate.SLOT_BACK);
		_slots.put("neck", ItemTemplate.SLOT_NECK);
		_slots.put("legs", ItemTemplate.SLOT_LEGS);
		_slots.put("feet", ItemTemplate.SLOT_FEET);
		_slots.put("gloves", ItemTemplate.SLOT_GLOVES);
		_slots.put("chest,legs", ItemTemplate.SLOT_CHEST | ItemTemplate.SLOT_LEGS);
		_slots.put("rhand", ItemTemplate.SLOT_R_HAND);
		_slots.put("lhand", ItemTemplate.SLOT_L_HAND);
		_slots.put("lrhand", ItemTemplate.SLOT_LR_HAND);
		_slots.put("rear,lear", ItemTemplate.SLOT_R_EAR | ItemTemplate.SLOT_L_EAR);
		_slots.put("rfinger,lfinger", ItemTemplate.SLOT_R_FINGER | ItemTemplate.SLOT_L_FINGER);
		_slots.put("none", ItemTemplate.SLOT_NONE);
		_slots.put("wolf", ItemTemplate.SLOT_WOLF); // for wolf
		_slots.put("hatchling", ItemTemplate.SLOT_HATCHLING); // for hatchling
		_slots.put("strider", ItemTemplate.SLOT_STRIDER); // for strider
		_slots.put("babypet", ItemTemplate.SLOT_BABYPET); // for babypet
	}
	private static final Map<String, WeaponType> _weaponTypes = new HashMap<>();
	static
	{
		_weaponTypes.put("blunt", WeaponType.BLUNT);
		_weaponTypes.put("bow", WeaponType.BOW);
		_weaponTypes.put("dagger", WeaponType.DAGGER);
		_weaponTypes.put("dual", WeaponType.DUAL);
		_weaponTypes.put("dualfist", WeaponType.DUALFIST);
		_weaponTypes.put("etc", WeaponType.ETC);
		_weaponTypes.put("fist", WeaponType.FIST);
		_weaponTypes.put("none", WeaponType.NONE); // these are shields!
		_weaponTypes.put("pole", WeaponType.POLE);
		_weaponTypes.put("sword", WeaponType.SWORD);
		_weaponTypes.put("bigsword", WeaponType.BIGSWORD); // Two-Handed Swords
		_weaponTypes.put("pet", WeaponType.PET); // Pet Weapon
		_weaponTypes.put("rod", WeaponType.ROD); // Fishing Rods
		_weaponTypes.put("bigblunt", WeaponType.BIGBLUNT); // Two handed blunt
	}
	private static final Map<String, ArmorType> _armorTypes = new HashMap<>();
	static
	{
		_armorTypes.put("none", ArmorType.NONE);
		_armorTypes.put("light", ArmorType.LIGHT);
		_armorTypes.put("heavy", ArmorType.HEAVY);
		_armorTypes.put("magic", ArmorType.MAGIC);
		_armorTypes.put("pet", ArmorType.PET);
	}
	
	public DocumentItem(File file)
	{
		super(file);
	}
	
	@Override
	protected StatSet getStatSet()
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
							_currentItem = new DocumentItemDataHolder();
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
	
	private void parseItem(Node node)
	{
		Node n = node;
		final int itemId = Integer.parseInt(n.getAttributes().getNamedItem("id").getNodeValue());
		final String className = n.getAttributes().getNamedItem("type").getNodeValue();
		final String itemName = n.getAttributes().getNamedItem("name").getNodeValue();
		
		_currentItem.id = itemId;
		_currentItem.set = new StatSet();
		_currentItem.set.set("item_id", itemId);
		_currentItem.set.set("name", itemName);
		
		final Node first = n.getFirstChild();
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
			int bodypart = _slots.get(_currentItem.set.getString("bodypart"));
			_currentItem.type = _weaponTypes.get(_currentItem.set.getString("weapon_type"));
			
			// lets see if this is a shield
			if (_currentItem.type == WeaponType.NONE)
			{
				_currentItem.set.set("type1", ItemTemplate.TYPE1_SHIELD_ARMOR);
				_currentItem.set.set("type2", ItemTemplate.TYPE2_SHIELD_ARMOR);
			}
			else
			{
				_currentItem.set.set("type1", ItemTemplate.TYPE1_WEAPON_RING_EARRING_NECKLACE);
				_currentItem.set.set("type2", ItemTemplate.TYPE2_WEAPON);
			}
			
			if (_currentItem.type == WeaponType.PET)
			{
				_currentItem.set.set("type1", ItemTemplate.TYPE1_WEAPON_RING_EARRING_NECKLACE);
				
				switch (_currentItem.set.getString("bodypart"))
				{
					case "wolf":
					{
						_currentItem.set.set("type2", ItemTemplate.TYPE2_PET_WOLF);
						break;
					}
					case "hatchling":
					{
						_currentItem.set.set("type2", ItemTemplate.TYPE2_PET_HATCHLING);
						break;
					}
					case "babypet":
					{
						_currentItem.set.set("type2", ItemTemplate.TYPE2_PET_BABY);
						break;
					}
					default:
					{
						_currentItem.set.set("type2", ItemTemplate.TYPE2_PET_STRIDER);
						break;
					}
				}
				
				bodypart = ItemTemplate.SLOT_R_HAND;
			}
			
			_currentItem.set.set("bodypart", bodypart);
		}
		else if (className.equals("Armor"))
		{
			_currentItem.type = _armorTypes.get(_currentItem.set.getString("armor_type"));
			
			int bodypart = _slots.get(_currentItem.set.getString("bodypart"));
			if ((bodypart == ItemTemplate.SLOT_NECK) || (bodypart == ItemTemplate.SLOT_HAIR) || (bodypart == ItemTemplate.SLOT_FACE) || (bodypart == ItemTemplate.SLOT_DHAIR) || ((bodypart & ItemTemplate.SLOT_L_EAR) != 0) || ((bodypart & ItemTemplate.SLOT_L_FINGER) != 0))
			{
				_currentItem.set.set("type1", ItemTemplate.TYPE1_WEAPON_RING_EARRING_NECKLACE);
				_currentItem.set.set("type2", ItemTemplate.TYPE2_ACCESSORY);
			}
			else
			{
				_currentItem.set.set("type1", ItemTemplate.TYPE1_SHIELD_ARMOR);
				_currentItem.set.set("type2", ItemTemplate.TYPE2_SHIELD_ARMOR);
			}
			
			if (_currentItem.type == ArmorType.PET)
			{
				_currentItem.set.set("type1", ItemTemplate.TYPE1_SHIELD_ARMOR);
				
				switch (_currentItem.set.getString("bodypart"))
				{
					case "wolf":
					{
						_currentItem.set.set("type2", ItemTemplate.TYPE2_PET_WOLF);
						break;
					}
					case "hatchling":
					{
						_currentItem.set.set("type2", ItemTemplate.TYPE2_PET_HATCHLING);
						break;
					}
					case "babypet":
					{
						_currentItem.set.set("type2", ItemTemplate.TYPE2_PET_BABY);
						break;
					}
					default:
					{
						_currentItem.set.set("type2", ItemTemplate.TYPE2_PET_STRIDER);
						break;
					}
				}
				
				bodypart = ItemTemplate.SLOT_CHEST;
			}
			
			_currentItem.set.set("bodypart", bodypart);
		}
		else
		{
			_currentItem.set.set("type1", ItemTemplate.TYPE1_ITEM_QUESTITEM_ADENA);
			_currentItem.set.set("type2", ItemTemplate.TYPE2_OTHER);
			
			final String itemType = _currentItem.set.getString("item_type");
			switch (itemType)
			{
				case "none":
				{
					_currentItem.type = EtcItemType.OTHER; // only for default
					break;
				}
				case "castle_guard":
				{
					_currentItem.type = EtcItemType.SCROLL; // dummy
					break;
				}
				case "pet_collar":
				{
					_currentItem.type = EtcItemType.PET_COLLAR;
					break;
				}
				case "potion":
				{
					_currentItem.type = EtcItemType.POTION;
					break;
				}
				case "recipe":
				{
					_currentItem.type = EtcItemType.RECEIPE;
					break;
				}
				case "scroll":
				{
					_currentItem.type = EtcItemType.SCROLL;
					break;
				}
				case "seed":
				{
					_currentItem.type = EtcItemType.SEED;
					break;
				}
				case "shot":
				{
					_currentItem.type = EtcItemType.SHOT;
					break;
				}
				case "spellbook":
				{
					_currentItem.type = EtcItemType.SPELLBOOK; // Spellbook, Amulet, Blueprint
					break;
				}
				case "herb":
				{
					_currentItem.type = EtcItemType.HERB;
					break;
				}
				case "arrow":
				{
					_currentItem.type = EtcItemType.ARROW;
					_currentItem.set.set("bodypart", ItemTemplate.SLOT_L_HAND);
					break;
				}
				case "quest":
				{
					_currentItem.type = EtcItemType.QUEST;
					_currentItem.set.set("type2", ItemTemplate.TYPE2_QUEST);
					break;
				}
				case "lure":
				{
					_currentItem.type = EtcItemType.OTHER;
					_currentItem.set.set("bodypart", ItemTemplate.SLOT_L_HAND);
					break;
				}
				default:
				{
					_currentItem.type = EtcItemType.OTHER;
					break;
				}
			}
			
			final String consume = _currentItem.set.getString("consume_type");
			switch (consume)
			{
				case "asset":
				{
					_currentItem.type = EtcItemType.MONEY;
					_currentItem.set.set("stackable", true);
					_currentItem.set.set("type2", ItemTemplate.TYPE2_MONEY);
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
		
		if (_currentItem.type instanceof ArmorType)
		{
			_currentItem.item = new Armor((ArmorType) _currentItem.type, _currentItem.set);
		}
		else if (_currentItem.type instanceof WeaponType)
		{
			_currentItem.item = new Weapon((WeaponType) _currentItem.type, _currentItem.set);
		}
		else if (_currentItem.type instanceof EtcItemType)
		{
			_currentItem.item = new EtcItem((EtcItemType) _currentItem.type, _currentItem.set);
		}
		else
		{
			throw new Error("Unknown item type for " + _currentItem.set.getInt("item_id") + " " + _currentItem.type);
		}
	}
	
	public List<ItemTemplate> getItemList()
	{
		return _itemsInFile;
	}
}
