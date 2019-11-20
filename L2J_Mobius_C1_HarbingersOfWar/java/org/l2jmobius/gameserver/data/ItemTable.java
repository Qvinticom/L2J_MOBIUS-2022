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
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.l2jmobius.gameserver.IdFactory;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.instance.ItemInstance;
import org.l2jmobius.gameserver.templates.L2Armor;
import org.l2jmobius.gameserver.templates.L2EtcItem;
import org.l2jmobius.gameserver.templates.L2Item;
import org.l2jmobius.gameserver.templates.L2Weapon;

public class ItemTable
{
	private static Logger _log = Logger.getLogger(ItemTable.class.getName());
	
	// private static final int TYPE_ETC_ITEM = 0;
	// private static final int TYPE_ARMOR = 1;
	// private static final int TYPE_WEAPON = 2;
	
	private static final HashMap<String, Integer> _materials = new HashMap<>();
	static
	{
		_materials.put("steel", 0);
		_materials.put("fine_steel", 1);
		_materials.put("cotton", 1);
		_materials.put("blood_steel", 2);
		_materials.put("bronze", 3);
		_materials.put("silver", 4);
		_materials.put("gold", 5);
		_materials.put("mithril", 6);
		_materials.put("oriharukon", 7);
		_materials.put("paper", 8);
		_materials.put("wood", 9);
		_materials.put("cloth", 10);
		_materials.put("leather", 11);
		_materials.put("bone", 12);
		_materials.put("horn", 13);
		_materials.put("damascus", 14);
		_materials.put("adamantaite", 15);
		_materials.put("chrysolite", 16);
		_materials.put("crystal", 17);
		_materials.put("liquid", 18);
		_materials.put("scale_of_dragon", 19);
		_materials.put("dyestuff", 20);
		_materials.put("cobweb", 21);
	}
	private static final HashMap<String, Integer> _crystalTypes = new HashMap<>();
	static
	{
		_crystalTypes.put("none", 1);
		_crystalTypes.put("d", 2);
		_crystalTypes.put("c", 3);
		_crystalTypes.put("b", 4);
		_crystalTypes.put("a", 5);
		_crystalTypes.put("s", 6);
	}
	private static final HashMap<String, Integer> _weaponTypes = new HashMap<>();
	static
	{
		_weaponTypes.put("none", 1);
		_weaponTypes.put("sword", 2);
		_weaponTypes.put("blunt", 3);
		_weaponTypes.put("dagger", 4);
		_weaponTypes.put("bow", 5);
		_weaponTypes.put("pole", 6);
		_weaponTypes.put("etc", 7);
		_weaponTypes.put("fist", 8);
		_weaponTypes.put("dual", 9);
		_weaponTypes.put("dualfist", 10);
	}
	private static final HashMap<String, Integer> _armorTypes = new HashMap<>();
	static
	{
		_armorTypes.put("none", 1);
		_armorTypes.put("light", 2);
		_armorTypes.put("heavy", 3);
		_armorTypes.put("magic", 4);
	}
	private static final HashMap<String, Integer> _slots = new HashMap<>();
	static
	{
		_slots.put("none", 0);
		_slots.put("underwear", 1);
		_slots.put("rear,lear", 6);
		_slots.put("neck", 8);
		_slots.put("rfinger,lfinger", 48);
		_slots.put("head", 64);
		_slots.put("rhand", 128);
		_slots.put("lhand", 256);
		_slots.put("gloves", 512);
		_slots.put("chest", 1024);
		_slots.put("legs", 2048);
		_slots.put("chest,legs", 3072);
		_slots.put("feet", 4096);
		_slots.put("back", 8192);
		_slots.put("lrhand", 16384);
		_slots.put("fullarmor", 32768);
	}
	private L2Item[] _allTemplates;
	private HashMap<Integer, L2Item> _etcItems;
	private HashMap<Integer, L2Item> _armors;
	private HashMap<Integer, L2Item> _weapons;
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
	
	public ItemTable()
	{
		File weaponFile;
		File armorFile;
		File etcItemFile = new File("data/etcitem.csv");
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
	
	public boolean isInitialized()
	{
		return _initialized;
	}
	
	private void parseEtcItems(File data)
	{
		_etcItems = parseFile(data, 0);
		_log.config("Loaded " + _etcItems.size() + " etc items.");
		fixEtcItems(_etcItems);
	}
	
	private void fixEtcItems(HashMap<Integer, L2Item> items)
	{
		Iterator<Integer> iter = items.keySet().iterator();
		while (iter.hasNext())
		{
			Integer key = iter.next();
			L2EtcItem item = (L2EtcItem) items.get(key);
			if ((item.getWeight() == 0) && (item.getEtcItemType() != 7) && !item.getName().startsWith("world_map") && !item.getName().startsWith("crystal_"))
			{
				item.setType2(3);
				item.setEtcItemType(6);
				continue;
			}
			if (item.getName().startsWith("sb_"))
			{
				item.setType2(5);
				item.setEtcItemType(9);
				continue;
			}
			if (item.getName().startsWith("rp_"))
			{
				item.setType2(5);
				item.setEtcItemType(4);
				continue;
			}
			if (!item.getName().startsWith("q_"))
			{
				continue;
			}
			item.setType2(3);
			item.setEtcItemType(6);
		}
	}
	
	private HashMap<Integer, L2Item> parseFile(File dataFile, int type)
	{
		HashMap<Integer, L2Item> result = new HashMap<>();
		LineNumberReader lnr = null;
		L2Item temp = null;
		try
		{
			lnr = new LineNumberReader(new BufferedReader(new FileReader(dataFile)));
			String line = null;
			while ((line = lnr.readLine()) != null)
			{
				try
				{
					if ((line.trim().length() == 0) || line.startsWith("#"))
					{
						continue;
					}
					switch (type)
					{
						case 0:
						{
							temp = parseEtcLine(line);
							break;
						}
						case 1:
						{
							temp = parseArmorLine(line);
							break;
						}
						case 2:
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
	
	private L2EtcItem parseEtcLine(String line)
	{
		L2EtcItem result = new L2EtcItem();
		try
		{
			StringTokenizer st = new StringTokenizer(line, ";");
			result.setItemId(Integer.parseInt(st.nextToken()));
			result.setName(st.nextToken());
			result.setCrystallizable(Boolean.valueOf(st.nextToken()));
			String itemType = st.nextToken();
			result.setType1(4);
			if (itemType.equals("none"))
			{
				result.setType2(5);
				result.setEtcItemType(8);
			}
			else if (itemType.equals("arrow"))
			{
				result.setType2(5);
				result.setEtcItemType(0);
				result.setBodyPart(256);
			}
			else if (itemType.equals("castle_guard"))
			{
				result.setType2(5);
				result.setEtcItemType(5);
			}
			else if (itemType.equals("material"))
			{
				result.setType2(5);
				result.setEtcItemType(1);
			}
			else if (itemType.equals("pet_collar"))
			{
				result.setType2(5);
				result.setEtcItemType(2);
			}
			else if (itemType.equals("potion"))
			{
				result.setType2(5);
				result.setEtcItemType(3);
			}
			else if (itemType.equals("recipe"))
			{
				result.setType2(5);
				result.setEtcItemType(4);
			}
			else if (itemType.equals("scroll"))
			{
				result.setType2(5);
				result.setEtcItemType(5);
			}
			else
			{
				_log.warning("Unknown etcitem type:" + itemType);
			}
			result.setWeight(Integer.parseInt(st.nextToken()));
			String consume = st.nextToken();
			if (consume.equals("asset"))
			{
				result.setStackable(true);
				result.setEtcItemType(7);
				result.setType2(4);
			}
			else if (consume.equals("stackable"))
			{
				result.setStackable(true);
			}
			Integer material = _materials.get(st.nextToken());
			result.setMaterialType(material);
			Integer crystal = _crystalTypes.get(st.nextToken());
			result.setCrystalType(crystal);
			result.setDurability(Integer.parseInt(st.nextToken()));
		}
		catch (Exception e)
		{
			_log.warning("Data error on etc item:" + result + " " + e);
		}
		return result;
	}
	
	private L2Armor parseArmorLine(String line)
	{
		L2Armor result = new L2Armor();
		try
		{
			StringTokenizer st = new StringTokenizer(line, ";");
			result.setItemId(Integer.parseInt(st.nextToken()));
			result.setName(st.nextToken());
			Integer bodyPart = _slots.get(st.nextToken());
			result.setBodyPart(bodyPart);
			result.setCrystallizable(Boolean.valueOf(st.nextToken()));
			Integer armor = _armorTypes.get(st.nextToken());
			result.setArmorType(armor);
			int slot = result.getBodyPart();
			if ((slot == 8) || ((slot & 4) != 0) || ((slot & 0x20) != 0))
			{
				result.setType1(0);
				result.setType2(2);
			}
			else
			{
				result.setType1(1);
				result.setType2(1);
			}
			result.setWeight(Integer.parseInt(st.nextToken()));
			Integer material = _materials.get(st.nextToken());
			result.setMaterialType(material);
			Integer crystal = _crystalTypes.get(st.nextToken());
			result.setCrystalType(crystal);
			result.setAvoidModifier(Integer.parseInt(st.nextToken()));
			result.setDurability(Integer.parseInt(st.nextToken()));
			result.setPDef(Integer.parseInt(st.nextToken()));
			result.setMDef(Integer.parseInt(st.nextToken()));
			result.setMpBonus(Integer.parseInt(st.nextToken()));
		}
		catch (Exception e)
		{
			_log.warning("Data error on armor:" + result + " line: " + line);
			e.printStackTrace();
		}
		return result;
	}
	
	private L2Weapon parseWeaponLine(String line)
	{
		L2Weapon result = new L2Weapon();
		try
		{
			StringTokenizer st = new StringTokenizer(line, ";");
			result.setItemId(Integer.parseInt(st.nextToken()));
			result.setName(st.nextToken());
			result.setType1(0);
			result.setType2(0);
			Integer bodyPart = _slots.get(st.nextToken());
			result.setBodyPart(bodyPart);
			result.setCrystallizable(Boolean.valueOf(st.nextToken()));
			result.setWeight(Integer.parseInt(st.nextToken()));
			result.setSoulShotCount(Integer.parseInt(st.nextToken()));
			result.setSpiritShotCount(Integer.parseInt(st.nextToken()));
			Integer material = _materials.get(st.nextToken());
			result.setMaterialType(material);
			Integer crystal = _crystalTypes.get(st.nextToken());
			result.setCrystalType(crystal);
			result.setPDamage(Integer.parseInt(st.nextToken()));
			result.setRandomDamage(Integer.parseInt(st.nextToken()));
			Integer weapon = _weaponTypes.get(st.nextToken());
			result.setWeaponType(weapon);
			if (weapon == 1)
			{
				result.setType1(1);
				result.setType2(1);
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
			_log.warning("Data error on weapon:" + result + " line: " + line);
			e.printStackTrace();
		}
		return result;
	}
	
	private void parseArmors(File data)
	{
		_armors = parseFile(data, 1);
		_log.config("Loaded " + _armors.size() + " armors.");
	}
	
	private void parseWeapons(File data)
	{
		_weapons = parseFile(data, 2);
		_log.config("Laoded " + _weapons.size() + " weapons.");
	}
	
	private void buildFastLookupTable()
	{
		L2Item item;
		Integer id;
		int highestId = 0;
		Iterator<Integer> iter = _armors.keySet().iterator();
		while (iter.hasNext())
		{
			id = iter.next();
			item = _armors.get(id);
			if (item.getItemId() <= highestId)
			{
				continue;
			}
			highestId = item.getItemId();
		}
		iter = _weapons.keySet().iterator();
		while (iter.hasNext())
		{
			id = iter.next();
			item = _weapons.get(id);
			if (item.getItemId() <= highestId)
			{
				continue;
			}
			highestId = item.getItemId();
		}
		iter = _etcItems.keySet().iterator();
		while (iter.hasNext())
		{
			id = iter.next();
			item = _etcItems.get(id);
			if (item.getItemId() <= highestId)
			{
				continue;
			}
			highestId = item.getItemId();
		}
		_log.fine("Highest item id used: " + highestId);
		_allTemplates = new L2Item[highestId + 1];
		iter = _armors.keySet().iterator();
		while (iter.hasNext())
		{
			id = iter.next();
			_allTemplates[id.intValue()] = item = _armors.get(id);
		}
		iter = _weapons.keySet().iterator();
		while (iter.hasNext())
		{
			id = iter.next();
			_allTemplates[id.intValue()] = item = _weapons.get(id);
		}
		iter = _etcItems.keySet().iterator();
		while (iter.hasNext())
		{
			id = iter.next();
			_allTemplates[id.intValue()] = item = _etcItems.get(id);
		}
	}
	
	public L2Item getTemplate(int id)
	{
		return _allTemplates[id];
	}
	
	public ItemInstance createItem(int itemId)
	{
		ItemInstance temp = new ItemInstance();
		temp.setObjectId(IdFactory.getInstance().getNextId());
		temp.setItem(getTemplate(itemId));
		_log.fine("Item created  oid: " + temp.getObjectId() + " itemid: " + itemId);
		World.getInstance().storeObject(temp);
		return temp;
	}
	
	public ItemInstance createDummyItem(int itemId)
	{
		ItemInstance temp = new ItemInstance();
		temp.setObjectId(0);
		L2Item item = null;
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
