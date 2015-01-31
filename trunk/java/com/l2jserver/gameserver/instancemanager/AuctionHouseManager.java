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
package com.l2jserver.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jserver.L2DatabaseFactory;
import com.l2jserver.gameserver.enums.MailType;
import com.l2jserver.gameserver.model.entity.Message;
import com.l2jserver.gameserver.model.items.L2Item;
import com.l2jserver.gameserver.model.items.instance.L2ItemInstance;
import com.l2jserver.gameserver.network.SystemMessageId;

/**
 * @author Erlandys
 */
public class AuctionHouseManager
{
	Connection con = null;
	private static final Logger _log = Logger.getLogger(AuctionHouseManager.class.getName());
	private static ArrayList<Auctions> auctions;
	private static HashMap<Integer, Integer> convertedCategories;
	private static HashMap<Integer, Integer> mainCategories;
	
	public AuctionHouseManager()
	{
		_log.info(getClass().getSimpleName() + ": Initializing.");
		loadCategoryConverter();
		loadMainCategoriesConverter();
		_log.info(getClass().getSimpleName() + ": Loaded " + mainCategories.size() + " Auction Sections.");
		load();
		_log.info(getClass().getSimpleName() + ": Loaded " + auctions.size() + " Auctions.");
	}
	
	private void load()
	{
		auctions = new ArrayList<>();
		int auctionID = 0;
		int sellerID = 0;
		int count = 0;
		int category = 0;
		int duration = 0;
		int itemOID = 0;
		int itemID = 0;
		long price = 0;
		long finishTime = 0;
		String itemName = "";
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT * FROM auction_house");)
		{
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				auctionID = (rset.getInt("auctionID"));
				sellerID = (rset.getInt("sellerID"));
				itemName = (rset.getString("itemName"));
				itemOID = (rset.getInt("itemOID"));
				price = (rset.getLong("price"));
				count = (rset.getInt("count"));
				category = (rset.getInt("category"));
				duration = (rset.getInt("duration"));
				finishTime = (rset.getLong("finishTime"));
				itemID = (rset.getInt("itemID"));
				L2ItemInstance item = new L2ItemInstance(itemOID, itemID);
				createAuction(auctionID, sellerID, itemOID, item, itemName, price, count, duration, finishTime, category);
			}
			statement.execute();
			
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			System.out.println("Failed loading auction. " + e);
		}
	}
	
	public void insertAuction(Auctions auction)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("INSERT INTO auction_house (auctionID, sellerID, itemName, itemOID, price, count, category, duration, finishTime, itemID) values (?,?,?,?,?,?,?,?,?,?)");)
		{
			statement.setInt(1, auction.getAuctionId());
			statement.setInt(2, auction.getPlayerID());
			statement.setString(3, auction.getItemName());
			statement.setInt(4, auction.getItemOID());
			statement.setLong(5, auction.getPrice());
			statement.setLong(6, auction.getCount());
			statement.setInt(7, auction.getCategory());
			statement.setInt(8, auction.getDuration());
			statement.setLong(9, auction.getFinishTime());
			statement.setInt(10, auction.getItem().getId());
			statement.executeUpdate();
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Could not insert auction: " + e.getMessage(), e);
		}
	}
	
	public void removeAuction(int auctionID)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE FROM auction_house WHERE auctionID=?");)
		{
			
			statement.setInt(1, auctionID);
			statement.executeUpdate();
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Could not delete auction: " + e.getMessage(), e);
		}
	}
	
	private void loadCategoryConverter()
	{
		convertedCategories = new HashMap<>();
		
		convertedCategories.put(4294967, 1);
		convertedCategories.put(8589934, 2);
		convertedCategories.put(12884901, 3);
		convertedCategories.put(17179869, 4);
		convertedCategories.put(21474836, 5);
		convertedCategories.put(25769803, 6);
		convertedCategories.put(30064771, 7);
		convertedCategories.put(34359738, 8);
		convertedCategories.put(38654705, 9);
		convertedCategories.put(42949672, 10);
		convertedCategories.put(47244640, 11);
		convertedCategories.put(51539607, 12);
		convertedCategories.put(55834574, 13);
		convertedCategories.put(60129542, 14);
		convertedCategories.put(64424509, 15);
		convertedCategories.put(68719476, 16);
		convertedCategories.put(73014444, 17);
		convertedCategories.put(77309411, 18);
		
		convertedCategories.put(81604378, 19);
		convertedCategories.put(85899345, 20);
		convertedCategories.put(90194313, 21);
		convertedCategories.put(94489280, 22);
		convertedCategories.put(98784247, 23);
		convertedCategories.put(103079215, 24);
		convertedCategories.put(107374182, 25);
		convertedCategories.put(111669149, 26);
		convertedCategories.put(115964116, 27);
		convertedCategories.put(120259084, 28);
		convertedCategories.put(124554051, 29);
		convertedCategories.put(128849018, 30);
		convertedCategories.put(133143986, 31);
		convertedCategories.put(137438953, 32);
		convertedCategories.put(141733920, 33);
		convertedCategories.put(146028888, 34);
		
		convertedCategories.put(150323855, 35);
		convertedCategories.put(154618822, 36);
		convertedCategories.put(158913789, 37);
		convertedCategories.put(163208757, 38);
		convertedCategories.put(167503724, 39);
		convertedCategories.put(171798691, 40);
		
		convertedCategories.put(180388626, 41);
		convertedCategories.put(184683593, 42);
		
		convertedCategories.put(188978561, 43);
		convertedCategories.put(193273528, 44);
		convertedCategories.put(197568495, 45);
		convertedCategories.put(201863462, 46);
		convertedCategories.put(206158430, 47);
		convertedCategories.put(210453397, 48);
		convertedCategories.put(214748364, 49);
		convertedCategories.put(219043332, 50);
		convertedCategories.put(223338299, 51);
		convertedCategories.put(227633266, 52);
		convertedCategories.put(231928233, 53);
		convertedCategories.put(236223201, 54);
		convertedCategories.put(240518168, 55);
		convertedCategories.put(244813135, 56);
		convertedCategories.put(249108103, 57);
		convertedCategories.put(253403070, 58);
	}
	
	private void loadMainCategoriesConverter()
	{
		mainCategories = new HashMap<>();
		
		mainCategories.put(4294967, 61);
		mainCategories.put(8589934, 62);
		mainCategories.put(12884901, 63);
		mainCategories.put(17179869, 64);
		mainCategories.put(21474836, 65);
	}
	
	public int getClientCategory(int category)
	{
		if (convertedCategories.get(category) != null)
		{
			return convertedCategories.get(category);
		}
		return -1;
	}
	
	public int getMainClientCategory(int category)
	{
		if (mainCategories.get(category) != null)
		{
			return mainCategories.get(category);
		}
		return -1;
	}
	
	public void createAuction(int auctionID, int playerOID, int itemOID, L2ItemInstance item, String itemName, long price, long count, int duration, long finishTime, int category)
	{
		auctions.add(new Auctions(auctionID, itemName, itemOID, item, price, count, duration, playerOID, finishTime, category));
	}
	
	public void deleteAuction(long auctionID)
	{
		for (int i = 0; i < auctions.size(); i++)
		{
			if (auctions.get(i).getAuctionId() == auctionID)
			{
				deleteItemFromPlayer(auctions.get(i).getItemOID(), auctions.get(i).getPlayerID());
				removeAuction(auctions.get(i).getAuctionId());
				auctions.remove(i);
			}
		}
	}
	
	public void deleteItemFromPlayer(int playerID, int itemOID)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE FROM items WHERE owner_id=? AND object_id=? AND loc='AUCTION_HOUSE'");)
		{
			statement.setInt(1, playerID);
			statement.setInt(2, itemOID);
			statement.executeUpdate();
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Could not delete auction: " + e.getMessage(), e);
		}
	}
	
	public void checkForAuctionsDeletion()
	{
		int i = 0;
		if (!auctions.isEmpty())
		{
			for (Auctions auction : auctions)
			{
				if ((System.currentTimeMillis() / 1000) >= auction.getFinishTime())
				{
					Message msg = new Message(auction.getPlayerID(), "CommissionBuyTitle", "Auction Manager", SystemMessageId.THE_REGISTRATION_PERIOD_FOR_THE_ITEM_YOU_REGISTERED_HAS_EXPIRED.getId(), SystemMessageId.THE_AUCTION_HOUSE_REGISTRATION_PERIOD_HAS_EXPIRED_AND_THE_CORRESPONDING_ITEM_IS_BEING_FORWARDED.getId(), MailType.SYSTEM);
					msg.createAttachments().addItem("DeleteAuction", auction.getItem().getId(), auction.getCount(), null, null);
					MailManager.getInstance().sendMessage(msg);
					removeAuction(auction.getAuctionId());
					deleteItemFromPlayer(auction.getItemOID(), auction.getPlayerID());
					auctions.remove(i);
				}
				i++;
			}
		}
	}
	
	public Auctions getAuctionById(int id)
	{
		for (Auctions auction : auctions)
		{
			if (auction.getAuctionId() == id)
			{
				return auction;
			}
		}
		
		return null;
	}
	
	public Auctions getAuctionById(long id)
	{
		for (Auctions auction : auctions)
		{
			if (auction.getAuctionId() == id)
			{
				return auction;
			}
		}
		
		return null;
	}
	
	public int getAuctionsSizeById(int grade, String search)
	{
		int i = 0;
		for (Auctions auction : auctions)
		{
			if (grade == -1)
			{
				if (auction.getItem().getName().contains(search))
				{
					i++;
				}
			}
			if (grade != -1)
			{
				if ((grade == auction.getItem().getItem().getCrystalType().getId()) && auction.getItem().getName().contains(search))
				{
					i++;
				}
			}
		}
		return i;
	}
	
	public int getAuctionsSizeById(long id, int grade, String search)
	{
		int ids[][] =
		{
			//@formatter:off
			{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18},
			{19, 20, 21, 22, 23, 24, 25, 26, 27, 28},
			{29, 30, 31, 32, 33, 34},
			{35, 36, 37, 38, 39, 40},
			{41, 42},
			{43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58}
			//@formatter:on
		};
		int i = 0;
		int IDS[] = null;
		if (id == 61)
		{
			IDS = ids[1];
		}
		else if (id == 62)
		{
			IDS = ids[2];
		}
		else if (id == 63)
		{
			IDS = ids[3];
		}
		else if (id == 64)
		{
			IDS = ids[4];
		}
		else if (id == 65)
		{
			IDS = ids[5];
		}
		else if (id == 101)
		{
			IDS = ids[0];
		}
		
		if ((((id > 60) && (id < 66)) || (id == 101)) && (IDS != null))
		{
			for (int ID : IDS)
			{
				for (Auctions auction : auctions)
				{
					if ((grade == -1) && search.equals(""))
					{
						if (auction.getCategory() == ID)
						{
							i++;
						}
					}
					else
					{
						if (grade != -1)
						{
							if (search.equals(""))
							{
								if ((auction.getCategory() == ID) && (grade == auction.getItem().getItem().getCrystalType().getId()))
								{
									i++;
								}
							}
							if (!search.equals(""))
							{
								if ((auction.getCategory() == ID) && (grade == auction.getItem().getItem().getCrystalType().getId()) && auction.getItem().getName().contains(search))
								{
									i++;
								}
							}
						}
						else if (!search.equals(""))
						{
							if ((auction.getCategory() == ID) && auction.getItem().getName().contains(search))
							{
								i++;
							}
						}
					}
				}
			}
		}
		else
		{
			for (Auctions auction : auctions)
			{
				if ((grade == -1) && search.equals(""))
				{
					if (auction.getCategory() == id)
					{
						i++;
					}
				}
				else
				{
					if (grade != -1)
					{
						if (search.equals(""))
						{
							if ((auction.getCategory() == id) && (grade == auction.getItem().getItem().getCrystalType().getId()))
							{
								i++;
							}
						}
						if (!search.equals(""))
						{
							if ((auction.getCategory() == id) && (grade == auction.getItem().getItem().getCrystalType().getId()) && auction.getItem().getName().contains(search))
							{
								i++;
							}
						}
					}
					else if (!search.equals(""))
					{
						if ((auction.getCategory() == id) && auction.getItem().getName().contains(search))
						{
							i++;
						}
					}
				}
			}
		}
		
		return i;
	}
	
	public int getCategoryByItem(L2ItemInstance item)
	{
		final String itemName = item.getName().toLowerCase();
		final int itemId = item.getId();
		
		if (item.isWeapon())
		{
			if (item.getItem().isPetItem())
			{
				return 41; // Pet Equipment
			}
			
			switch (item.getWeaponItem().getItemType())
			{
				case SWORD:
				{
					if (item.getWeaponItem().getBodyPart() == L2Item.SLOT_LR_HAND)
					{
						return 5; // 2-H Sword
					}
					
					if (item.getWeaponItem().isMagicWeapon())
					{
						return 2; // 1-H Magic Sword
					}
					
					return 1; // 1-H Sword
				}
				case DAGGER:
				{
					return 3; // Dagger
				}
				case RAPIER:
				{
					return 4; // Rapier
				}
				case ANCIENTSWORD:
				{
					return 6; // Ancient
				}
				case DUAL:
				{
					return 7; // Dual Swords
				}
				case DUALDAGGER:
				{
					return 8; // Dual Daggers
				}
				case BLUNT:
				{
					if (item.getWeaponItem().getBodyPart() == L2Item.SLOT_LR_HAND)
					{
						if (item.getWeaponItem().isMagicWeapon())
						{
							return 12; // 2-H Magic Blunt
						}
						
						return 11; // 2-H Blunt
					}
					
					if (item.getWeaponItem().isMagicWeapon())
					{
						return 10; // 1-H Magic Blunt
					}
					
					return 9; // 1-H Blunt
				}
				case DUALBLUNT:
				{
					return 13; // Dual Blunt
				}
				case BOW:
				{
					return 14; // Bow
				}
				case CROSSBOW:
				{
					return 15; // Crossbow
				}
				case DUALFIST:
				{
					return 16; // Fist Weapon
				}
				case POLE:
				{
					return 17; // Spear
				}
				default:
				{
					return 18; // Other Weapon
				}
			}
		}
		else if (item.isArmor())
		{
			if (item.getItem().isPetItem())
			{
				return 41; // Pet Equipment
			}
			
			switch (item.getArmorItem().getBodyPart())
			{
				case L2Item.SLOT_HEAD:
				{
					return 19; // Helmet
				}
				case L2Item.SLOT_CHEST:
				{
					return 20; // Armor Top
				}
				case L2Item.SLOT_LEGS:
				{
					return 21; // Armor Pants
				}
				case L2Item.SLOT_FULL_ARMOR:
				case L2Item.SLOT_ALLDRESS:
				{
					return 22; // Full Body
				}
				case L2Item.SLOT_GLOVES:
				{
					return 23; // Gloves
				}
				case L2Item.SLOT_FEET:
				{
					return 24; // Feet
				}
				case L2Item.SLOT_L_HAND:
				{
					if (itemName.contains("sigil"))
					{
						return 26; // Sigil
					}
					
					return 25; // Shield
				}
				case L2Item.SLOT_UNDERWEAR:
				{
					return 27; // Underwear
				}
				case L2Item.SLOT_BACK:
				{
					return 28; // Cloak
				}
			}
		}
		else if (item.isEtcItem())
		{
			// Accessory
			switch (item.getEtcItem().getBodyPart())
			{
				case L2Item.SLOT_R_FINGER:
				case L2Item.SLOT_L_FINGER:
				case L2Item.SLOT_LR_FINGER:
				{
					return 29; // Ring
				}
				case L2Item.SLOT_R_EAR:
				case L2Item.SLOT_L_EAR:
				case L2Item.SLOT_LR_EAR:
				{
					return 30; // Earring
				}
				case L2Item.SLOT_NECK:
				{
					return 31; // Necklace
				}
				case L2Item.SLOT_BELT:
				{
					return 32; // Belt
				}
				case L2Item.SLOT_R_BRACELET:
				case L2Item.SLOT_L_BRACELET:
				{
					return 33; // Bracelet
				}
				case L2Item.SLOT_HAIR:
				case L2Item.SLOT_HAIR2:
				case L2Item.SLOT_HAIRALL:
				{
					return 34; // Hair Accessory
				}
			}
			
			// Supplies
			if (item.getEtcItem().isPotion() || item.getEtcItem().isElixir())
			{
				return 35; // Potion
			}
			
			if (item.getEtcItem().getHandlerName() != null)
			{
				switch (item.getEtcItem().getHandlerName())
				{
					case "EnchantScrolls":
					{
						if (itemName.contains("weapon"))
						{
							return 36; // Scroll: Enchant Weapon
						}
						
						if (itemName.contains("armor"))
						{
							return 37; // Scroll: Enchant Armor
						}
					}
					case "SoulShots":
					{
						return 39; // Soulshot
					}
					case "SpiritShot":
					{
						return 40; // SpiritShot
					}
					case "PetFood":
					case "BeastSoulShot":
					case "BeastSpiritShot":
					case "SummonItems":
					{
						return 42; // Pet Supplies
					}
				}
			}
			
			if (itemName.contains("scroll"))
			{
				return 38; // Scroll: Other
			}
			
			// Misc
			if (itemName.contains("crystal") && itemName.contains("-grade"))
			{
				return 43; // Crystal
			}
			if (itemName.contains("recipe"))
			{
				return 44; // Recipe
			}
			// TODO: Add all materials
			if ((itemId > 1863) && (itemId < 2130))
			{
				return 45; // Major Crafting Ingredients
			}
			if (itemName.contains("life stone"))
			{
				return 46; // Life Stone
			}
			if (itemName.contains("soul crystal"))
			{
				return 47; // Soul Crystal
			}
			if (itemName.contains("stone") && (itemName.contains("fire") || itemName.contains("water") || itemName.contains("earth") || itemName.contains("wind") || itemName.contains("dark") || itemName.contains("holy")))
			{
				return 48; // Attribute Stone
			}
			if (itemName.contains("weapon") && itemName.contains("enchant") && itemName.contains("stone"))
			{
				return 49; // Weapon Enchant Stone
			}
			if (itemName.contains("armor") && itemName.contains("enchant") && itemName.contains("stone"))
			{
				return 50; // Armor Enchant Stone
			}
			if (itemName.contains("spellbook") || itemName.contains("forgotten scroll"))
			{
				return 51; // Spellbook
			}
			if (itemName.contains("gemstone") && itemName.contains("-grade"))
			{
				return 52; // Gemstone
			}
			if (itemName.contains("magic pouch"))
			{
				return 53; // Pouch
			}
			if (itemName.contains("magic pin"))
			{
				return 54; // Pin
			}
			if (itemName.contains("magic rune clip"))
			{
				return 55; // Magic Rune Clip
			}
			if (itemName.contains("magic ornament"))
			{
				return 56; // Magic Ornament
			}
			if (itemName.contains("dye") && (itemName.contains("str") || itemName.contains("dex") || itemName.contains("con") || itemName.contains("int") || itemName.contains("wit") || itemName.contains("men") || itemName.contains("luc") || itemName.contains("cha")))
			{
				return 57; // Dye
			}
		}
		
		return 58; // Other Item
	}
	
	public ArrayList<Auctions> getAuctions()
	{
		return auctions;
	}
	
	public class Auctions
	{
		int auctionID;
		int itemOID;
		int duration;
		int playerID;
		int category;
		L2ItemInstance item;
		long price;
		long count;
		long finishTime;
		String itemName;
		
		public Auctions(int _auctionID, String _itemName, int _itemOID, L2ItemInstance _item, long _price, long _count, int _duration, int _playerID, long _finishTime, int _category)
		{
			auctionID = _auctionID;
			itemName = _itemName;
			itemOID = _itemOID;
			item = _item;
			price = _price;
			count = _count;
			duration = _duration;
			playerID = _playerID;
			finishTime = _finishTime;
			category = _category;
		}
		
		public int getAuctionId()
		{
			return auctionID;
		}
		
		public String getItemName()
		{
			return itemName;
		}
		
		public int getItemOID()
		{
			return itemOID;
		}
		
		public L2ItemInstance getItem()
		{
			return item;
		}
		
		public long getPrice()
		{
			return price;
		}
		
		public long getCount()
		{
			return count;
		}
		
		public int getDuration()
		{
			return duration;
		}
		
		public int getPlayerID()
		{
			return playerID;
		}
		
		public long getFinishTime()
		{
			return finishTime;
		}
		
		public int getCategory()
		{
			return category;
		}
	}
	
	public static final AuctionHouseManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final AuctionHouseManager _instance = new AuctionHouseManager();
	}
}