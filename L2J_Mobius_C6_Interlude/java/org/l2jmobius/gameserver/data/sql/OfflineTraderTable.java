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
package org.l2jmobius.gameserver.data.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.commons.util.Chronos;
import org.l2jmobius.gameserver.LoginServerThread;
import org.l2jmobius.gameserver.model.ManufactureItem;
import org.l2jmobius.gameserver.model.ManufactureList;
import org.l2jmobius.gameserver.model.TradeList.TradeItem;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.GameClient;

public class OfflineTraderTable
{
	private static final Logger LOGGER = Logger.getLogger(OfflineTraderTable.class.getName());
	
	// SQL DEFINITIONS
	private static final String SAVE_OFFLINE_STATUS = "INSERT INTO character_offline_trade (`charId`,`time`,`type`,`title`) VALUES (?,?,?,?)";
	private static final String SAVE_ITEMS = "INSERT INTO character_offline_trade_items (`charId`,`item`,`count`,`price`,`enchant`) VALUES (?,?,?,?,?)";
	private static final String CLEAR_OFFLINE_TABLE = "DELETE FROM character_offline_trade";
	private static final String CLEAR_OFFLINE_TABLE_PLAYER = "DELETE FROM character_offline_trade WHERE `charId`=?";
	private static final String CLEAR_OFFLINE_TABLE_ITEMS = "DELETE FROM character_offline_trade_items";
	private static final String CLEAR_OFFLINE_TABLE_ITEMS_PLAYER = "DELETE FROM character_offline_trade_items WHERE `charId`=?";
	private static final String LOAD_OFFLINE_STATUS = "SELECT * FROM character_offline_trade";
	private static final String LOAD_OFFLINE_ITEMS = "SELECT * FROM character_offline_trade_items WHERE `charId`=?";
	
	protected OfflineTraderTable()
	{
	}
	
	public void storeOffliners()
	{
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement stm1 = con.prepareStatement(CLEAR_OFFLINE_TABLE);
			PreparedStatement stm2 = con.prepareStatement(CLEAR_OFFLINE_TABLE_ITEMS);
			PreparedStatement stm3 = con.prepareStatement(SAVE_OFFLINE_STATUS);
			PreparedStatement stmItems = con.prepareStatement(SAVE_ITEMS))
		{
			stm1.execute();
			stm2.execute();
			con.setAutoCommit(false); // avoid halfway done
			for (Player pc : World.getInstance().getAllPlayers())
			{
				try
				{
					if ((pc.getPrivateStoreType() != Player.STORE_PRIVATE_NONE))
					{
						stm3.setInt(1, pc.getObjectId());
						stm3.setLong(2, pc.getOfflineStartTime());
						stm3.setInt(3, pc.getPrivateStoreType());
						String title = null;
						
						switch (pc.getPrivateStoreType())
						{
							case Player.STORE_PRIVATE_BUY:
							{
								if (!Config.OFFLINE_TRADE_ENABLE)
								{
									continue;
								}
								title = pc.getBuyList().getTitle();
								for (TradeItem i : pc.getBuyList().getItems())
								{
									stmItems.setInt(1, pc.getObjectId());
									stmItems.setInt(2, i.getItem().getItemId());
									stmItems.setLong(3, i.getCount());
									stmItems.setLong(4, i.getPrice());
									stmItems.setLong(5, i.getEnchant());
									stmItems.executeUpdate();
									stmItems.clearParameters();
								}
								break;
							}
							case Player.STORE_PRIVATE_SELL:
							case Player.STORE_PRIVATE_PACKAGE_SELL:
							{
								if (!Config.OFFLINE_TRADE_ENABLE)
								{
									continue;
								}
								title = pc.getSellList().getTitle();
								pc.getSellList().updateItems();
								for (TradeItem i : pc.getSellList().getItems())
								{
									stmItems.setInt(1, pc.getObjectId());
									stmItems.setInt(2, i.getObjectId());
									stmItems.setLong(3, i.getCount());
									stmItems.setLong(4, i.getPrice());
									stmItems.setLong(5, i.getEnchant());
									stmItems.executeUpdate();
									stmItems.clearParameters();
								}
								break;
							}
							case Player.STORE_PRIVATE_MANUFACTURE:
							{
								if (!Config.OFFLINE_CRAFT_ENABLE)
								{
									continue;
								}
								title = pc.getCreateList().getStoreName();
								for (ManufactureItem i : pc.getCreateList().getList())
								{
									stmItems.setInt(1, pc.getObjectId());
									stmItems.setInt(2, i.getRecipeId());
									stmItems.setLong(3, 0);
									stmItems.setLong(4, i.getCost());
									stmItems.setLong(5, 0);
									stmItems.executeUpdate();
									stmItems.clearParameters();
								}
								break;
							}
							default:
							{
								// LOGGER.info(getClass().getSimpleName() + ": Error while saving offline trader: " + pc.getObjectId() + ", store type: "+pc.getPrivateStoreType());
								continue;
							}
						}
						stm3.setString(4, title);
						stm3.executeUpdate();
						stm3.clearParameters();
						con.commit();
					}
				}
				catch (Exception e)
				{
					LOGGER.log(Level.WARNING, getClass().getSimpleName() + ": Error while saving offline trader: " + pc.getObjectId() + " " + e, e);
				}
			}
			LOGGER.info(getClass().getSimpleName() + ": Offline traders stored.");
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, getClass().getSimpleName() + ": Error while saving offline traders: " + e, e);
		}
	}
	
	public void restoreOfflineTraders()
	{
		LOGGER.info(getClass().getSimpleName() + ": Loading offline traders...");
		int nTraders = 0;
		try (Connection con = DatabaseFactory.getConnection();
			Statement stm = con.createStatement();
			ResultSet rs = stm.executeQuery(LOAD_OFFLINE_STATUS))
		{
			while (rs.next())
			{
				final long time = rs.getLong("time");
				if (Config.OFFLINE_MAX_DAYS > 0)
				{
					final Calendar cal = Calendar.getInstance();
					cal.setTimeInMillis(time);
					cal.add(Calendar.DAY_OF_YEAR, Config.OFFLINE_MAX_DAYS);
					if (cal.getTimeInMillis() <= Chronos.currentTimeMillis())
					{
						continue;
					}
				}
				
				final int type = rs.getInt("type");
				if (type == Player.STORE_PRIVATE_NONE)
				{
					continue;
				}
				
				Player player = null;
				
				try
				{
					final GameClient client = new GameClient();
					client.setDetached(true);
					player = Player.load(rs.getInt("charId"));
					client.setPlayer(player);
					client.setAccountName(player.getAccountName());
					player.setClient(client);
					player.setOfflineMode(true);
					player.setOnlineStatus(false);
					player.setOfflineStartTime(time);
					if (Config.OFFLINE_SLEEP_EFFECT)
					{
						player.startAbnormalEffect(Creature.ABNORMAL_EFFECT_SLEEP);
					}
					player.spawnMe(player.getX(), player.getY(), player.getZ());
					LoginServerThread.getInstance().addGameServerLogin(player.getAccountName(), client);
					try (PreparedStatement stmItems = con.prepareStatement(LOAD_OFFLINE_ITEMS))
					{
						stmItems.setInt(1, player.getObjectId());
						try (ResultSet items = stmItems.executeQuery())
						{
							switch (type)
							{
								case Player.STORE_PRIVATE_BUY:
								{
									while (items.next())
									{
										player.getBuyList().addItemByItemId(items.getInt(2), items.getInt(3), items.getInt(4), items.getInt(5));
									}
									player.getBuyList().setTitle(rs.getString("title"));
									break;
								}
								case Player.STORE_PRIVATE_SELL:
								case Player.STORE_PRIVATE_PACKAGE_SELL:
								{
									while (items.next())
									{
										player.getSellList().addItem(items.getInt(2), items.getInt(3), items.getInt(4));
									}
									player.getSellList().setTitle(rs.getString("title"));
									player.getSellList().setPackaged(type == Player.STORE_PRIVATE_PACKAGE_SELL);
									break;
								}
								case Player.STORE_PRIVATE_MANUFACTURE:
								{
									final ManufactureList createList = new ManufactureList();
									while (items.next())
									{
										createList.add(new ManufactureItem(items.getInt(2), items.getInt(4)));
									}
									player.setCreateList(createList);
									player.getCreateList().setStoreName(rs.getString("title"));
									break;
								}
								default:
								{
									LOGGER.info("Offline trader " + player.getName() + " finished to sell his items");
								}
							}
						}
					}
					player.sitDown();
					if (Config.OFFLINE_MODE_SET_INVULNERABLE)
					{
						player.setInvul(true);
					}
					if (Config.OFFLINE_SET_NAME_COLOR)
					{
						player._originalNameColorOffline = player.getAppearance().getNameColor();
						player.getAppearance().setNameColor(Config.OFFLINE_NAME_COLOR);
					}
					player.setPrivateStoreType(type);
					player.setOnlineStatus(true);
					player.restoreEffects();
					player.broadcastUserInfo();
					nTraders++;
				}
				catch (Exception e)
				{
					LOGGER.log(Level.WARNING, getClass().getSimpleName() + ": Error loading trader: " + player, e);
					if (player != null)
					{
						player.logout();
					}
				}
			}
			
			World.OFFLINE_TRADE_COUNT = nTraders;
			LOGGER.info(getClass().getSimpleName() + ": Loaded " + nTraders + " offline traders.");
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, getClass().getSimpleName() + ": Error while loading offline traders: ", e);
		}
	}
	
	public void storeOffliner(Player trader)
	{
		if ((trader.getPrivateStoreType() == Player.STORE_PRIVATE_NONE) || (!trader.isInOfflineMode()))
		{
			return;
		}
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement stm1 = con.prepareStatement(CLEAR_OFFLINE_TABLE_ITEMS_PLAYER);
			PreparedStatement stm2 = con.prepareStatement(CLEAR_OFFLINE_TABLE_PLAYER);
			PreparedStatement stm3 = con.prepareStatement(SAVE_ITEMS);
			PreparedStatement stm4 = con.prepareStatement(SAVE_OFFLINE_STATUS))
		{
			stm1.setInt(1, trader.getObjectId());
			stm1.execute();
			stm2.setInt(1, trader.getObjectId());
			stm2.execute();
			con.setAutoCommit(false); // avoid halfway done
			boolean save = true;
			try
			{
				String title = null;
				switch (trader.getPrivateStoreType())
				{
					case Player.STORE_PRIVATE_BUY:
					{
						if (!Config.OFFLINE_TRADE_ENABLE)
						{
							break;
						}
						title = trader.getBuyList().getTitle();
						for (TradeItem i : trader.getBuyList().getItems())
						{
							stm3.setInt(1, trader.getObjectId());
							stm3.setInt(2, i.getItem().getItemId());
							stm3.setLong(3, i.getCount());
							stm3.setLong(4, i.getPrice());
							stm3.setLong(5, i.getEnchant());
							stm3.executeUpdate();
							stm3.clearParameters();
						}
						break;
					}
					case Player.STORE_PRIVATE_SELL:
					case Player.STORE_PRIVATE_PACKAGE_SELL:
					{
						if (!Config.OFFLINE_TRADE_ENABLE)
						{
							break;
						}
						title = trader.getSellList().getTitle();
						trader.getSellList().updateItems();
						for (TradeItem i : trader.getSellList().getItems())
						{
							stm3.setInt(1, trader.getObjectId());
							stm3.setInt(2, i.getObjectId());
							stm3.setLong(3, i.getCount());
							stm3.setLong(4, i.getPrice());
							stm3.setLong(5, i.getEnchant());
							stm3.executeUpdate();
							stm3.clearParameters();
						}
						break;
					}
					case Player.STORE_PRIVATE_MANUFACTURE:
					{
						if (!Config.OFFLINE_CRAFT_ENABLE)
						{
							break;
						}
						title = trader.getCreateList().getStoreName();
						for (ManufactureItem i : trader.getCreateList().getList())
						{
							stm3.setInt(1, trader.getObjectId());
							stm3.setInt(2, i.getRecipeId());
							stm3.setLong(3, 0);
							stm3.setLong(4, i.getCost());
							stm3.setLong(5, 0);
							stm3.executeUpdate();
							stm3.clearParameters();
						}
						break;
					}
					default:
					{
						// LOGGER.info(getClass().getSimpleName() + ": Error while saving offline trader: " + pc.getObjectId() + ", store type: "+pc.getPrivateStoreType());
						save = false;
					}
				}
				
				if (save)
				{
					stm4.setInt(1, trader.getObjectId()); // Char Id
					stm4.setLong(2, trader.getOfflineStartTime());
					stm4.setInt(3, trader.getPrivateStoreType()); // store type
					stm4.setString(4, title);
					stm4.executeUpdate();
					stm4.clearParameters();
					con.commit();
				}
			}
			catch (Exception e)
			{
				LOGGER.warning(getClass().getSimpleName() + ": Error while saving offline trader: " + trader.getObjectId() + " " + e);
			}
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, getClass().getSimpleName() + ": Error while saving offline trader: " + trader.getObjectId() + " " + e);
		}
	}
	
	/**
	 * Gets the single instance of OfflineTradersTable.
	 * @return single instance of OfflineTradersTable
	 */
	public static OfflineTraderTable getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final OfflineTraderTable INSTANCE = new OfflineTraderTable();
	}
}
