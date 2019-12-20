/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.l2jmobius.gameserver.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.l2jmobius.gameserver.IdFactory;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.instance.ItemInstance;
import org.l2jmobius.gameserver.templates.Armor;
import org.l2jmobius.gameserver.templates.EtcItem;
import org.l2jmobius.gameserver.templates.Item;
import org.l2jmobius.gameserver.templates.Weapon;

public class ItemTable
{
	private static Logger _log = Logger.getLogger(ItemTable.class.getName());
	
	private static final int TYPE_ETC_ITEM = 0;
	private static final int TYPE_ARMOR = 1;
	private static final int TYPE_WEAPON = 2;
	
	private static final HashMap<String, Integer> _materials = new HashMap<>();
	static
	{
		_materials.put("steel", Item.MATERIAL_STEEL);
		_materials.put("fine_steel", Item.MATERIAL_FINE_STEEL);
		_materials.put("cotton", Item.MATERIAL_FINE_STEEL);
		_materials.put("blood_steel", Item.MATERIAL_BLOOD_STEEL);
		_materials.put("bronze", Item.MATERIAL_BRONZE);
		_materials.put("silver", Item.MATERIAL_SILVER);
		_materials.put("gold", Item.MATERIAL_GOLD);
		_materials.put("mithril", Item.MATERIAL_MITHRIL);
		_materials.put("oriharukon", Item.MATERIAL_ORIHARUKON);
		_materials.put("paper", Item.MATERIAL_PAPER);
		_materials.put("wood", Item.MATERIAL_WOOD);
		_materials.put("cloth", Item.MATERIAL_CLOTH);
		_materials.put("leather", Item.MATERIAL_LEATHER);
		_materials.put("bone", Item.MATERIAL_BONE);
		_materials.put("horn", Item.MATERIAL_HORN);
		_materials.put("damascus", Item.MATERIAL_DAMASCUS);
		_materials.put("adamantaite", Item.MATERIAL_ADAMANTAITE);
		_materials.put("chrysolite", Item.MATERIAL_CHRYSOLITE);
		_materials.put("crystal", Item.MATERIAL_CRYSTAL);
		_materials.put("liquid", Item.MATERIAL_LIQUID);
		_materials.put("scale_of_dragon", Item.MATERIAL_SCALE_OF_DRAGON);
		_materials.put("dyestuff", Item.MATERIAL_DYESTUFF);
		_materials.put("cobweb", Item.MATERIAL_COBWEB);
	}
	private static final HashMap<String, Integer> _crystalTypes = new HashMap<>();
	static
	{
		_crystalTypes.put("none", Item.CRYSTAL_NONE);
		_crystalTypes.put("d", Item.CRYSTAL_D);
		_crystalTypes.put("c", Item.CRYSTAL_C);
		_crystalTypes.put("b", Item.CRYSTAL_B);
		_crystalTypes.put("a", Item.CRYSTAL_A);
		_crystalTypes.put("s", Item.CRYSTAL_S);
	}
	private static final HashMap<String, Integer> _weaponTypes = new HashMap<>();
	static
	{
		_weaponTypes.put("none", Weapon.WEAPON_TYPE_NONE);
		_weaponTypes.put("sword", Weapon.WEAPON_TYPE_SWORD);
		_weaponTypes.put("blunt", Weapon.WEAPON_TYPE_BLUNT);
		_weaponTypes.put("dagger", Weapon.WEAPON_TYPE_DAGGER);
		_weaponTypes.put("bow", Weapon.WEAPON_TYPE_BOW);
		_weaponTypes.put("pole", Weapon.WEAPON_TYPE_POLE);
		_weaponTypes.put("etc", Weapon.WEAPON_TYPE_ETC);
		_weaponTypes.put("fist", Weapon.WEAPON_TYPE_FIST);
		_weaponTypes.put("dual", Weapon.WEAPON_TYPE_DUAL);
		_weaponTypes.put("dualfist", Weapon.WEAPON_TYPE_DUALFIST);
	}
	private static final HashMap<String, Integer> _armorTypes = new HashMap<>();
	static
	{
		_armorTypes.put("none", Armor.ARMORTYPE_NONE);
		_armorTypes.put("light", Armor.ARMORTYPE_LIGHT);
		_armorTypes.put("heavy", Armor.ARMORTYPE_HEAVY);
		_armorTypes.put("magic", Armor.ARMORTYPE_MAGIC);
	}
	private static final HashMap<String, Integer> _slots = new HashMap<>();
	static
	{
		_slots.put("none", Item.SLOT_NONE);
		_slots.put("underwear", Item.SLOT_UNDERWEAR);
		_slots.put("rear,lear", Item.SLOT_R_EAR + Item.SLOT_L_EAR);
		_slots.put("neck", Item.SLOT_NECK);
		_slots.put("rfinger,lfinger", Item.SLOT_R_FINGER + Item.SLOT_L_FINGER);
		_slots.put("head", Item.SLOT_HEAD);
		_slots.put("rhand", Item.SLOT_R_HAND);
		_slots.put("lhand", Item.SLOT_L_HAND);
		_slots.put("gloves", Item.SLOT_GLOVES);
		_slots.put("chest", Item.SLOT_CHEST);
		_slots.put("legs", Item.SLOT_LEGS);
		_slots.put("chest,legs", Item.SLOT_CHEST + Item.SLOT_LEGS);
		_slots.put("feet", Item.SLOT_FEET);
		_slots.put("back", Item.SLOT_BACK);
		_slots.put("lrhand", Item.SLOT_LR_HAND);
		_slots.put("fullarmor", Item.SLOT_FULL_ARMOR);
	}
	
	private Item[] _allTemplates;
	private HashMap<Integer, Item> _etcItems;
	private HashMap<Integer, Item> _armors;
	private HashMap<Integer, Item> _weapons;
	private boolean _initialized = true;
	private static ItemTable _instance;
	
	public static ItemTable getInstance()
	{
		if (_instance == null)
		{
			_instance = new ItemTable();
		}
		return _instance;
	}
	
	public boolean isInitialized()
	{
		return _initialized;
	}
	
	public ItemTable()
	{
		File weaponFile;
		File armorFile;
		final File etcItemFile = new File("data/etcitem.csv");
		if (!etcItemFile.isFile() && !etcItemFile.canRead())
		{
			_initialized = false;
		}
		if (!(armorFile = new File("data/armor.csv")).isFile() && !armorFile.canRead())
		{
			_initialized = false;
		}
		if (!(weaponFile = new File("data/weapon.csv")).isFile() && !weaponFile.canRead())
		{
			_initialized = false;
		}
		parseEtcItems(etcItemFile);
		parseArmors(armorFile);
		parseWeapons(weaponFile);
		buildFastLookupTable();
	}
	
	private void parseEtcItems(File data)
	{
		_etcItems = parseFile(data, TYPE_ETC_ITEM);
		_log.config("Loaded " + _etcItems.size() + " etc items.");
		fixEtcItems(_etcItems);
	}
	
	private void parseArmors(File data)
	{
		_armors = parseFile(data, TYPE_ARMOR);
		_log.config("Loaded " + _armors.size() + " armors.");
	}
	
	private void parseWeapons(File data)
	{
		_weapons = parseFile(data, TYPE_WEAPON);
		_log.config("Laoded " + _weapons.size() + " weapons.");
	}
	
	private void fixEtcItems(HashMap<Integer, Item> items)
	{
		for (Item i : items.values())
		{
			final EtcItem item = (EtcItem) i;
			if ((item.getWeight() == 0) && (item.getEtcItemType() != EtcItem.TYPE_MONEY) && !item.getName().startsWith("world_map") && !item.getName().startsWith("crystal_"))
			{
				item.setType2(Item.TYPE2_QUEST);
				item.setEtcItemType(EtcItem.TYPE_QUEST);
				continue;
			}
			if (item.getName().startsWith("sb_"))
			{
				item.setType2(Item.TYPE2_OTHER);
				item.setEtcItemType(EtcItem.TYPE_SPELLBOOK);
				continue;
			}
			if (item.getName().startsWith("rp_"))
			{
				item.setType2(Item.TYPE2_OTHER);
				item.setEtcItemType(EtcItem.TYPE_RECIPE);
				continue;
			}
			if (!item.getName().startsWith("q_"))
			{
				continue;
			}
			item.setType2(Item.TYPE2_QUEST);
			item.setEtcItemType(EtcItem.TYPE_QUEST);
		}
	}
	
	private HashMap<Integer, Item> parseFile(File dataFile, int type)
	{
		final HashMap<Integer, Item> result = new HashMap<>();
		LineNumberReader lnr = null;
		Item temp = null;
		try
		{
			lnr = new LineNumberReader(new BufferedReader(new FileReader(dataFile)));
			String line = null;
			while ((line = lnr.readLine()) != null)
			{
				try
				{
					if (line.trim().isEmpty() || line.startsWith("#"))
					{
						continue;
					}
					switch (type)
					{
						case TYPE_ETC_ITEM:
						{
							temp = parseEtcLine(line);
							break;
						}
						case TYPE_ARMOR:
						{
							temp = parseArmorLine(line);
							break;
						}
						case TYPE_WEAPON:
						{
							temp = parseWeaponLine(line);
						}
					}
					if (temp != null)
					{
						result.put(temp.getItemId(), temp);
					}
				}
				catch (Exception e)
				{
					_log.warning("Error while parsing item:" + line + " " + e);
				}
			}
		}
		catch (Exception e)
		{
			_log.warning("Error while parsing items:" + e);
		}
		return result;
	}
	
	private EtcItem parseEtcLine(String line)
	{
		final EtcItem result = new EtcItem();
		try
		{
			final StringTokenizer st = new StringTokenizer(line, ";");
			result.setItemId(Integer.parseInt(st.nextToken()));
			result.setName(st.nextToken());
			result.setCrystallizable(Boolean.parseBoolean(st.nextToken()));
			final String itemType = st.nextToken();
			result.setType1(Item.TYPE1_ITEM_QUESTITEM_ADENA);
			if (itemType.equals("none"))
			{
				result.setType2(Item.TYPE2_OTHER);
				result.setEtcItemType(EtcItem.TYPE_OTHER);
			}
			else if (itemType.equals("arrow"))
			{
				result.setType2(Item.TYPE2_OTHER);
				result.setEtcItemType(EtcItem.TYPE_ARROW);
				result.setBodyPart(Item.SLOT_L_HAND);
			}
			else if (itemType.equals("castle_guard"))
			{
				result.setType2(Item.TYPE2_OTHER);
				result.setEtcItemType(EtcItem.TYPE_SCROLL);
			}
			else if (itemType.equals("material"))
			{
				result.setType2(Item.TYPE2_OTHER);
				result.setEtcItemType(EtcItem.TYPE_MATERIAL);
			}
			else if (itemType.equals("pet_collar"))
			{
				result.setType2(Item.TYPE2_OTHER);
				result.setEtcItemType(EtcItem.TYPE_PET_COLLAR);
			}
			else if (itemType.equals("potion"))
			{
				result.setType2(Item.TYPE2_OTHER);
				result.setEtcItemType(EtcItem.TYPE_POTION);
			}
			else if (itemType.equals("recipe"))
			{
				result.setType2(Item.TYPE2_OTHER);
				result.setEtcItemType(EtcItem.TYPE_RECIPE);
			}
			else if (itemType.equals("scroll"))
			{
				result.setType2(Item.TYPE2_OTHER);
				result.setEtcItemType(EtcItem.TYPE_SCROLL);
			}
			else
			{
				_log.warning("Unknown etcitem type:" + itemType);
			}
			result.setWeight(Integer.parseInt(st.nextToken()));
			final String consume = st.nextToken();
			if (consume.equals("asset"))
			{
				result.setStackable(true);
				result.setEtcItemType(EtcItem.TYPE_MONEY);
				result.setType2(Item.TYPE2_MONEY);
			}
			else if (consume.equals("stackable"))
			{
				result.setStackable(true);
			}
			final Integer material = _materials.get(st.nextToken());
			result.setMaterialType(material);
			final Integer crystal = _crystalTypes.get(st.nextToken());
			result.setCrystalType(crystal);
			result.setDurability(Integer.parseInt(st.nextToken()));
		}
		catch (Exception e)
		{
			_log.warning("Data error on etc item:" + result + " line: " + line + " " + e);
		}
		return result;
	}
	
	private Armor parseArmorLine(String line)
	{
		final Armor result = new Armor();
		try
		{
			final StringTokenizer st = new StringTokenizer(line, ";");
			result.setItemId(Integer.parseInt(st.nextToken()));
			result.setName(st.nextToken());
			final Integer bodyPart = _slots.get(st.nextToken());
			result.setBodyPart(bodyPart);
			result.setCrystallizable(Boolean.parseBoolean(st.nextToken()));
			final Integer armor = _armorTypes.get(st.nextToken());
			result.setArmorType(armor);
			final int slot = result.getBodyPart();
			if ((slot == Item.SLOT_NECK) || ((slot & Item.SLOT_L_EAR) != 0) || ((slot & Item.SLOT_L_FINGER) != 0))
			{
				result.setType1(Item.TYPE1_WEAPON_RING_EARRING_NECKLACE);
				result.setType2(Item.TYPE2_ACCESSORY);
			}
			else
			{
				result.setType1(Item.TYPE1_SHIELD_ARMOR);
				result.setType2(Item.TYPE2_SHIELD_ARMOR);
			}
			result.setWeight(Integer.parseInt(st.nextToken()));
			final Integer material = _materials.get(st.nextToken());
			result.setMaterialType(material);
			final Integer crystal = _crystalTypes.get(st.nextToken());
			result.setCrystalType(crystal);
			result.setAvoidModifier(Integer.parseInt(st.nextToken()));
			result.setDurability(Integer.parseInt(st.nextToken()));
			result.setPDef(Integer.parseInt(st.nextToken()));
			result.setMDef(Integer.parseInt(st.nextToken()));
			result.setMpBonus(Integer.parseInt(st.nextToken()));
		}
		catch (Exception e)
		{
			_log.warning("Data error on armor:" + result + " line: " + line + " " + e);
		}
		return result;
	}
	
	private Weapon parseWeaponLine(String line)
	{
		final Weapon result = new Weapon();
		try
		{
			final StringTokenizer st = new StringTokenizer(line, ";");
			result.setItemId(Integer.parseInt(st.nextToken()));
			result.setName(st.nextToken());
			result.setType1(Item.TYPE1_WEAPON_RING_EARRING_NECKLACE);
			result.setType2(Item.TYPE2_WEAPON);
			final Integer bodyPart = _slots.get(st.nextToken());
			result.setBodyPart(bodyPart);
			result.setCrystallizable(Boolean.parseBoolean(st.nextToken()));
			result.setWeight(Integer.parseInt(st.nextToken()));
			result.setSoulShotCount(Integer.parseInt(st.nextToken()));
			result.setSpiritShotCount(Integer.parseInt(st.nextToken()));
			final Integer material = _materials.get(st.nextToken());
			result.setMaterialType(material);
			final Integer crystal = _crystalTypes.get(st.nextToken());
			result.setCrystalType(crystal);
			result.setPDamage(Integer.parseInt(st.nextToken()));
			result.setRandomDamage(Integer.parseInt(st.nextToken()));
			final Integer weapon = _weaponTypes.get(st.nextToken());
			result.setWeaponType(weapon);
			if (weapon == 1)
			{
				result.setType1(Item.TYPE1_SHIELD_ARMOR);
				result.setType2(Item.TYPE2_SHIELD_ARMOR);
			}
			result.setCritical(Integer.parseInt(st.nextToken()));
			result.setHitModifier(Double.parseDouble(st.nextToken()));
			result.setAvoidModifier(Integer.parseInt(st.nextToken()));
			result.setShieldDef(Integer.parseInt(st.nextToken()));
			result.setShieldDefRate(Integer.parseInt(st.nextToken()));
			result.setAttackSpeed(Integer.parseInt(st.nextToken()));
			result.setMpConsume(Integer.parseInt(st.nextToken()));
			result.setMDamage(Integer.parseInt(st.nextToken()));
			result.setDurability(Integer.parseInt(st.nextToken()));
		}
		catch (Exception e)
		{
			_log.warning("Data error on weapon:" + result + " line: " + line + " " + e);
		}
		return result;
	}
	
	private void buildFastLookupTable()
	{
		int highestId = 0;
		for (int id : _armors.keySet())
		{
			if (id <= highestId)
			{
				continue;
			}
			highestId = id;
		}
		for (int id : _weapons.keySet())
		{
			if (id <= highestId)
			{
				continue;
			}
			highestId = id;
		}
		for (int id : _etcItems.keySet())
		{
			if (id <= highestId)
			{
				continue;
			}
			highestId = id;
		}
		
		// Create a FastLookUp Table called _allTemplates of size : value of the highest item ID
		_allTemplates = new Item[highestId + 1];
		
		// Insert armor item in Fast Look Up Table
		for (Item armor : _armors.values())
		{
			_allTemplates[armor.getItemId()] = armor;
		}
		
		// Insert weapon item in Fast Look Up Table
		for (Item weapon : _weapons.values())
		{
			_allTemplates[weapon.getItemId()] = weapon;
		}
		
		// Insert etcItem item in Fast Look Up Table
		for (Item etcItem : _etcItems.values())
		{
			_allTemplates[etcItem.getItemId()] = etcItem;
		}
	}
	
	public Item getTemplate(int id)
	{
		return _allTemplates[id];
	}
	
	public ItemInstance createItem(int itemId)
	{
		final ItemInstance temp = new ItemInstance();
		temp.setObjectId(IdFactory.getInstance().getNextId());
		temp.setItem(getTemplate(itemId));
		World.getInstance().storeObject(temp);
		return temp;
	}
	
	public ItemInstance createDummyItem(int itemId)
	{
		final ItemInstance temp = new ItemInstance();
		temp.setObjectId(0);
		Item item = null;
		try
		{
			item = getTemplate(itemId);
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			// empty catch block
		}
		if (item == null)
		{
			_log.warning("Item template missing. id: " + itemId);
		}
		else
		{
			temp.setItem(item);
		}
		return temp;
	}
}
