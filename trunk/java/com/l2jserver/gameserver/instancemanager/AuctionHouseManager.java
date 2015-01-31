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
	private static HashMap<String, Integer> categoryType;
	private static HashMap<Integer, Integer> categoryConvert;
	private static HashMap<Integer, Integer> massCategoryConvert;
	
	public AuctionHouseManager()
	{
		_log.info(getClass().getSimpleName() + ": Initializing");
		loadCategories();
		_log.info(getClass().getSimpleName() + ": Loaded " + categoryType.size() + " Auction Categories.");
		loadCategoryConverter();
		_log.info(getClass().getSimpleName() + ": Loaded " + categoryConvert.size() + " Converts for Auction Categories.");
		loadMassCategoryConverter();
		_log.info(getClass().getSimpleName() + ": Loaded " + massCategoryConvert.size() + " Auction Sections.");
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
			PreparedStatement statement = con.prepareStatement("SELECT * FROM auctions_info");)
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
			PreparedStatement statement = con.prepareStatement("INSERT INTO auctions_info (auctionID, sellerID, itemName, itemOID, price, count, category, duration, finishTime, itemID) values (?,?,?,?,?,?,?,?,?,?)");)
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
			PreparedStatement statement = con.prepareStatement("DELETE FROM auctions_info WHERE auctionID=?");)
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
	
	private void loadCategories()
	{
		categoryType = new HashMap<>();
		
		categoryType.put("sword10", 1); /* 1-H Sword */
		categoryType.put("sword11", 2); /* 1-H Magic Sword */
		categoryType.put("dagger40", 3); /* Dagger */
		categoryType.put("rapier20480", 4); /* Rapier */
		categoryType.put("big sword10485760", 5); /* 2-H Sword */
		categoryType.put("ancient40960", 6); /* 2-H Sword */
		categoryType.put("dual sword640", 7); /* Dual Swords */
		categoryType.put("dual dagger655360", 8); /* Dual Daggers */
		categoryType.put("blunt20", 9); /* 1-H Blunt */
		categoryType.put("blunt21", 10); /* 1-H Magic Blunt */
		categoryType.put("big blunt5242880", 11); /* 2-H Blunt */
		categoryType.put("big blunt5242881", 12); /* 2-H Magic Blunt */
		categoryType.put("dual blunt2621440", 13); /* Dual Blunt */
		categoryType.put("bow80", 14); /* Bow */
		categoryType.put("crossbow81920", 15); /* Crossbow */
		categoryType.put("dual fist5120", 16); /* Fists */
		categoryType.put("pole160", 17); /* Pole */
		categoryType.put("etc1281", 18); /* Etc */
		categoryType.put("fist2560", 18); /* Fist */
		categoryType.put("rod10240", 18); /* Rod */
		categoryType.put("flag163840", 18); /* Flag */
		categoryType.put("ownthing327680", 18); /* Ownthing */
		
		categoryType.put("head2097152", 19); /* Helmet */
		categoryType.put("chest8388608", 20); /* Chest */
		categoryType.put("legs8388608", 21); /* Leggings */
		categoryType.put("onepiece8388608", 22); /* Full Body */
		categoryType.put("gloves2097152", 23); /* Gloves */
		categoryType.put("feet2097152", 24); /* Boots */
		categoryType.put("lhand67108864", 25); /* Shield */
		categoryType.put("lhand33554432", 26); /* Sigil */
		categoryType.put("underwear2097152", 27); /* Shirt */
		categoryType.put("back2097152", 28); /* Cloak */
		
		categoryType.put("rfinger;lfinger2097152", 29); /* Rings */
		categoryType.put("rear;lear2097152", 30); /* Earrings */
		categoryType.put("neck2097152", 31); /* Necklace */
		categoryType.put("waist4194304", 32); /* Belt */
		categoryType.put("rbracelet2097152", 33); /* Bracelet */
		categoryType.put("hair22097152", 34); /* Hair1 */
		categoryType.put("hair2097152", 34); /* Hair2 */
		categoryType.put("hairall2097152", 34); /* Hair All */
		
		categoryType.put("potion", 35); /* Potion */
		categoryType.put("elixir", 35); /* Elixir */
		categoryType.put("scrl_enchant_wp", 36); /* Weapon Enchant */
		categoryType.put("bless_scrl_enchant_wp", 36); /* Blessed Weapon enchant */
		categoryType.put("scrl_inc_enchant_prop_wp", 36); /* Inc Weapon Enchant */
		categoryType.put("ancient_crystal_enchant_wp", 36); /* Ancient Crystal Weapon Enchant */
		categoryType.put("scrl_enchant_am", 37); /* Armor Enchant */
		categoryType.put("bless_scrl_enchant_am", 37); /* Blessed Armor Enchant */
		categoryType.put("scrl_inc_enchant_prop_am", 37); /* Inc Armor Enchant */
		categoryType.put("ancient_crystal_enchant_am", 37); /* Crystal Enchant */
		categoryType.put("scroll", 38); /* Scroll: Other */
		
		/* 39/40 Categories - SoulShot/Spiritshot... */
		/* 41 Category - Pet Weapons, Armors and Etc Items... */
		
		categoryType.put("pet_collar", 42); /* Pets */
		
		/* 44 Category - All Grade Crystals. */
		
		categoryType.put("recipe", 44); /* Recipe */
		
		/* 46 Category - Crafting main ingredients (all parts, not stem or other materials) */
		/* 47 Category - Life Stones */
		
		categoryType.put("scrl_enchant_attr", 48); /* Scroll Attr */
		
		/* 49/50 Categories - Weapon/Armor Enchant Stones TODO: need to create this */
		/* 51 Category - Spellbooks */
		/* 52 Category - Gemstones */
		/* 53 Category - Pins */
		/* 54 Category - Rune Clip */
		/* 55 Category - Magic Ornament */
		
		categoryType.put("dye", 57); /* Dyes */
	}
	
	private void loadCategoryConverter()
	{
		categoryConvert = new HashMap<>();
		
		categoryConvert.put(4294967, 1);
		categoryConvert.put(8589934, 2);
		categoryConvert.put(12884901, 3);
		categoryConvert.put(17179869, 4);
		categoryConvert.put(21474836, 5);
		categoryConvert.put(25769803, 6);
		categoryConvert.put(30064771, 7);
		categoryConvert.put(34359738, 8);
		categoryConvert.put(38654705, 9);
		categoryConvert.put(42949672, 10);
		categoryConvert.put(47244640, 11);
		categoryConvert.put(51539607, 12);
		categoryConvert.put(55834574, 13);
		categoryConvert.put(60129542, 14);
		categoryConvert.put(64424509, 15);
		categoryConvert.put(68719476, 16);
		categoryConvert.put(73014444, 17);
		categoryConvert.put(77309411, 18);
		
		categoryConvert.put(81604378, 19);
		categoryConvert.put(85899345, 20);
		categoryConvert.put(90194313, 21);
		categoryConvert.put(94489280, 22);
		categoryConvert.put(98784247, 23);
		categoryConvert.put(103079215, 24);
		categoryConvert.put(107374182, 25);
		categoryConvert.put(111669149, 26);
		categoryConvert.put(115964116, 27);
		categoryConvert.put(120259084, 28);
		categoryConvert.put(124554051, 29);
		categoryConvert.put(128849018, 30);
		categoryConvert.put(133143986, 31);
		categoryConvert.put(137438953, 32);
		categoryConvert.put(141733920, 33);
		categoryConvert.put(146028888, 34);
		
		categoryConvert.put(150323855, 35);
		categoryConvert.put(154618822, 36);
		categoryConvert.put(158913789, 37);
		categoryConvert.put(163208757, 38);
		categoryConvert.put(167503724, 39);
		categoryConvert.put(171798691, 40);
		
		categoryConvert.put(180388626, 41);
		categoryConvert.put(184683593, 42);
		
		categoryConvert.put(188978561, 43);
		categoryConvert.put(193273528, 44);
		categoryConvert.put(197568495, 45);
		categoryConvert.put(201863462, 46);
		categoryConvert.put(206158430, 47);
		categoryConvert.put(210453397, 48);
		categoryConvert.put(214748364, 49);
		categoryConvert.put(219043332, 50);
		categoryConvert.put(223338299, 51);
		categoryConvert.put(227633266, 52);
		categoryConvert.put(231928233, 53);
		categoryConvert.put(236223201, 54);
		categoryConvert.put(240518168, 55);
		categoryConvert.put(244813135, 56);
		categoryConvert.put(249108103, 57);
		categoryConvert.put(253403070, 58);
	}
	
	private void loadMassCategoryConverter()
	{
		massCategoryConvert = new HashMap<>();
		
		massCategoryConvert.put(4294967, 61);
		massCategoryConvert.put(8589934, 62);
		massCategoryConvert.put(12884901, 63);
		massCategoryConvert.put(17179869, 64);
		massCategoryConvert.put(21474836, 65);
	}
	
	public int convertCategory(int category)
	{
		if (categoryConvert.get(category) != null)
		{
			return categoryConvert.get(category);
		}
		return -1;
	}
	
	public int convertMassCategory(int category)
	{
		if (massCategoryConvert.get(category) != null)
		{
			return massCategoryConvert.get(category);
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
	
	public int getAuctionsSizeById(int id, int grade, String search)
	{
		int ids[][] =
		{
			{
				1,
				2,
				3,
				4,
				5,
				6,
				7,
				8,
				9,
				10,
				11,
				12,
				13,
				14,
				15,
				16,
				17,
				18
			},
			{
				19,
				20,
				21,
				22,
				23,
				24,
				25,
				26,
				27,
				28
			},
			{
				29,
				30,
				31,
				32,
				33,
				34
			},
			{
				35,
				36,
				37,
				38,
				39,
				40
			},
			{
				41,
				42
			},
			{
				43,
				44,
				45,
				46,
				47,
				48,
				49,
				50,
				51,
				52,
				53,
				54,
				55,
				56,
				57,
				58
			}
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
		else if (id == 1)
		{
			IDS = ids[0];
		}
		
		if ((((id > 60) && (id < 66)) || (id == 1)) && (IDS != null))
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
			{
				1,
				2,
				3,
				4,
				5,
				6,
				7,
				8,
				9,
				10,
				11,
				12,
				13,
				14,
				15,
				16,
				17,
				18
			},
			{
				19,
				20,
				21,
				22,
				23,
				24,
				25,
				26,
				27,
				28
			},
			{
				29,
				30,
				31,
				32,
				33,
				34
			},
			{
				35,
				36,
				37,
				38,
				39,
				40
			},
			{
				41,
				42
			},
			{
				43,
				44,
				45,
				46,
				47,
				48,
				49,
				50,
				51,
				52,
				53,
				54,
				55,
				56,
				57,
				58
			}
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
		if (item.isArmor())
		{
			if (item.getItem().isPetItem())
			{
				return 41;
			}
			if (categoryType.get(item.getArmorItem().getBodyPartName().toLowerCase() + "" + item.getArmorItem().getItemType().mask()) != null)
			{
				return categoryType.get(item.getArmorItem().getBodyPartName().toLowerCase() + "" + item.getArmorItem().getItemType().mask());
			}
		}
		else if (item.isWeapon())
		{
			if (item.getItem().isPetItem())
			{
				return 41;
			}
			if (categoryType.get(item.getWeaponItem().getItemType().toString().toLowerCase() + "" + item.getWeaponItem().getItemType().mask() + "" + (item.getWeaponItem().isMagicWeapon() ? 1 : 0)) != null)
			{
				return categoryType.get(item.getWeaponItem().getItemType().toString().toLowerCase() + "" + item.getWeaponItem().getItemType().mask() + "" + (item.getWeaponItem().isMagicWeapon() ? 1 : 0));
			}
		}
		else if (item.isEtcItem())
		{
			if (item.getItem().isPetItem())
			{
				return 41;
			}
			if (item.getEtcItem().getHandlerName() != null)
			{
				if (item.getEtcItem().getHandlerName().toLowerCase().contains("petfood"))
				{
					return 41;
				}
			}
			if (((item.getId() > 1457) && (item.getId() < 1462)) || (item.getId() == 17371))
			{
				return 43;
			}
			if (item.getName().toLowerCase().contains("life stone"))
			{
				return 46;
			}
			if (item.getName().toLowerCase().contains("spellbook") || item.getName().toLowerCase().contains("forgotten scroll"))
			{
				return 51;
			}
			if (((item.getId() > 2129) && (item.getId() < 2135)) || (item.getId() == 19440))
			{
				return 52;
			}
			if (item.getName().toLowerCase().contains("magic pouch ("))
			{
				return 53;
			}
			if (item.getName().toLowerCase().contains("magic pin ("))
			{
				return 54;
			}
			if (item.getName().toLowerCase().contains("magic rune clip ("))
			{
				return 55;
			}
			if (item.getName().toLowerCase().contains("magic ornament ("))
			{
				return 56;
			}
			if (item.getEtcItem().getDefaultAction() != null)
			{
				if (item.getEtcItem().getDefaultAction().toString().toLowerCase().contains("spiritshot"))
				{
					return 40;
				}
				else if (item.getEtcItem().getDefaultAction().toString().toLowerCase().contains("soulshot"))
				{
					return 39;
				}
			}
			if (item.getName().toLowerCase().contains("soul crystal"))
			{
				return 47;
			}
			if (item.getName().toLowerCase().contains("ingredient") || item.getName().toLowerCase().contains("piece") || item.getName().toLowerCase().contains("edge") || item.getName().toLowerCase().contains("beads") || item.getName().toLowerCase().contains("stave") || item.getName().toLowerCase().contains("design") || item.getName().toLowerCase().contains("fragment") || item.getName().toLowerCase().contains("blade") || item.getName().toLowerCase().contains("head") || item.getName().toLowerCase().contains("part") || item.getName().toLowerCase().contains("gem") || item.getName().toLowerCase().contains("shaft") || item.getName().toLowerCase().contains("stone") || item.getName().toLowerCase().contains("fabric") || item.getName().toLowerCase().contains("pattern") || item.getName().toLowerCase().contains("lining"))
			{
				return 45;
			}
			if (categoryType.containsKey(item.getEtcItem().getItemType().toString().toLowerCase()))
			{
				return categoryType.get(item.getEtcItem().getItemType().toString().toLowerCase());
			}
			return 58;
		}
		
		return 59;
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