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
package org.l2jmobius.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.commons.util.Chronos;
import org.l2jmobius.gameserver.util.PrimeFinder;

/**
 * @author Mobius (reworked from L2J IdFactory)
 */
public class IdManager
{
	private static final Logger LOGGER = Logger.getLogger(IdManager.class.getName());
	
	// @formatter:off
	private static final String[][] ID_EXTRACTS =
	{
		{"characters","charId"},
		{"items","object_id"},
		{"clan_data","clan_id"},
		{"itemsonground","object_id"}
	};
	// @formatter:on
	
	private static final String[] TIMESTAMPS_CLEAN =
	{
		"DELETE FROM character_skills_save WHERE restore_type = 1 AND systime <= ?"
	};
	
	private static final int FIRST_OID = 0x10000000;
	private static final int LAST_OID = 0x7FFFFFFF;
	private static final int FREE_OBJECT_ID_SIZE = LAST_OID - FIRST_OID;
	
	private static BitSet _freeIds;
	private static AtomicInteger _freeIdCount;
	private static AtomicInteger _nextFreeId;
	private static boolean _initialized;
	
	public IdManager()
	{
		// Update characters online status.
		try (Connection con = DatabaseFactory.getConnection();
			Statement statement = con.createStatement())
		{
			statement.executeUpdate("UPDATE characters SET online = 0");
			LOGGER.info("Updated characters online status.");
		}
		catch (Exception e)
		{
			LOGGER.warning("IdManager: Could not update characters online status: " + e);
		}
		
		// Cleanup database.
		try (Connection con = DatabaseFactory.getConnection();
			Statement statement = con.createStatement())
		{
			final long cleanupStart = Chronos.currentTimeMillis();
			int cleanCount = 0;
			
			// Characters
			cleanCount += statement.executeUpdate("DELETE FROM character_friends WHERE character_friends.char_id NOT IN (SELECT charId FROM characters);");
			cleanCount += statement.executeUpdate("DELETE FROM character_hennas WHERE character_hennas.char_obj_id NOT IN (SELECT charId FROM characters);");
			cleanCount += statement.executeUpdate("DELETE FROM character_macroses WHERE character_macroses.char_obj_id NOT IN (SELECT charId FROM characters);");
			cleanCount += statement.executeUpdate("DELETE FROM character_quests WHERE character_quests.char_id NOT IN (SELECT charId FROM characters);");
			cleanCount += statement.executeUpdate("DELETE FROM character_recipebook WHERE character_recipebook.char_id NOT IN (SELECT charId FROM characters);");
			cleanCount += statement.executeUpdate("DELETE FROM character_shortcuts WHERE character_shortcuts.char_obj_id NOT IN (SELECT charId FROM characters);");
			cleanCount += statement.executeUpdate("DELETE FROM character_skills WHERE character_skills.char_obj_id NOT IN (SELECT charId FROM characters);");
			cleanCount += statement.executeUpdate("DELETE FROM character_skills_save WHERE character_skills_save.char_obj_id NOT IN (SELECT charId FROM characters);");
			cleanCount += statement.executeUpdate("DELETE FROM character_subclasses WHERE character_subclasses.char_obj_id NOT IN (SELECT charId FROM characters);");
			cleanCount += statement.executeUpdate("DELETE FROM heroes WHERE heroes.charId NOT IN (SELECT charId FROM characters);");
			cleanCount += statement.executeUpdate("DELETE FROM olympiad_nobles WHERE olympiad_nobles.charId NOT IN (SELECT charId FROM characters);");
			cleanCount += statement.executeUpdate("DELETE FROM pets WHERE pets.item_obj_id NOT IN (SELECT object_id FROM items);");
			cleanCount += statement.executeUpdate("DELETE FROM seven_signs WHERE seven_signs.char_obj_id NOT IN (SELECT charId FROM characters);");
			
			// Auction
			cleanCount += statement.executeUpdate("DELETE FROM auction WHERE auction.id IN (SELECT id FROM clanhall WHERE ownerId <> 0);");
			cleanCount += statement.executeUpdate("DELETE FROM auction_bid WHERE auctionId IN (SELECT id FROM clanhall WHERE ownerId <> 0)");
			
			// Clan
			statement.executeUpdate("UPDATE clan_data SET auction_bid_at = 0 WHERE auction_bid_at NOT IN (SELECT auctionId FROM auction_bid);");
			cleanCount += statement.executeUpdate("DELETE FROM clan_data WHERE clan_data.leader_id NOT IN (SELECT charId FROM characters);");
			cleanCount += statement.executeUpdate("DELETE FROM auction_bid WHERE auction_bid.bidderId NOT IN (SELECT clan_id FROM clan_data);");
			cleanCount += statement.executeUpdate("DELETE FROM clanhall_functions WHERE clanhall_functions.hall_id NOT IN (SELECT id FROM clanhall WHERE ownerId <> 0);");
			cleanCount += statement.executeUpdate("DELETE FROM clan_privs WHERE clan_privs.clan_id NOT IN (SELECT clan_id FROM clan_data);");
			cleanCount += statement.executeUpdate("DELETE FROM clan_skills WHERE clan_skills.clan_id NOT IN (SELECT clan_id FROM clan_data);");
			cleanCount += statement.executeUpdate("DELETE FROM clan_subpledges WHERE clan_subpledges.clan_id NOT IN (SELECT clan_id FROM clan_data);");
			cleanCount += statement.executeUpdate("DELETE FROM clan_wars WHERE clan_wars.clan1 NOT IN (SELECT clan_id FROM clan_data);");
			cleanCount += statement.executeUpdate("DELETE FROM clan_wars WHERE clan_wars.clan2 NOT IN (SELECT clan_id FROM clan_data);");
			cleanCount += statement.executeUpdate("DELETE FROM siege_clans WHERE siege_clans.clan_id NOT IN (SELECT clan_id FROM clan_data);");
			statement.executeUpdate("UPDATE castle SET taxpercent=0 WHERE castle.id NOT IN (SELECT hasCastle FROM clan_data);");
			
			// Forums
			cleanCount += statement.executeUpdate("DELETE FROM forums WHERE forums.forum_owner_id NOT IN (SELECT clan_id FROM clan_data) AND forums.forum_parent=2;");
			cleanCount += statement.executeUpdate("DELETE FROM topic WHERE topic.topic_forum_id NOT IN (SELECT forum_id FROM forums);");
			cleanCount += statement.executeUpdate("DELETE FROM posts WHERE posts.post_forum_id NOT IN (SELECT forum_id FROM forums);");
			
			// Update needed items after cleaning has taken place.
			cleanCount += statement.executeUpdate("DELETE FROM items WHERE items.owner_id NOT IN (SELECT charId FROM characters) AND items.owner_id NOT IN (SELECT clan_id FROM clan_data);");
			statement.executeUpdate("UPDATE characters SET clanid=0 WHERE characters.clanid NOT IN (SELECT clan_id FROM clan_data);");
			
			LOGGER.info("IdManager: Cleaned " + cleanCount + " elements from database in " + ((Chronos.currentTimeMillis() - cleanupStart) / 1000) + " seconds.");
		}
		catch (Exception e)
		{
			LOGGER.warning("IdManager: Could not clean up database: " + e);
		}
		
		// Cleanup timestamps.
		try (Connection con = DatabaseFactory.getConnection())
		{
			int cleanCount = 0;
			for (String line : TIMESTAMPS_CLEAN)
			{
				try (PreparedStatement statement = con.prepareStatement(line))
				{
					statement.setLong(1, Chronos.currentTimeMillis());
					cleanCount += statement.executeUpdate();
				}
			}
			LOGGER.info("IdManager: Cleaned " + cleanCount + " expired timestamps from database.");
		}
		catch (Exception e)
		{
			LOGGER.warning("IdManager: Could not clean expired timestamps from database. " + e);
		}
		
		// Initialize.
		try
		{
			_freeIds = new BitSet(PrimeFinder.nextPrime(100000));
			_freeIds.clear();
			_freeIdCount = new AtomicInteger(FREE_OBJECT_ID_SIZE);
			
			// Collect already used ids.
			final List<Integer> usedIds = new ArrayList<>();
			try (Connection con = DatabaseFactory.getConnection();
				Statement statement = con.createStatement())
			{
				String extractUsedObjectIdsQuery = "";
				for (String[] tblClmn : ID_EXTRACTS)
				{
					extractUsedObjectIdsQuery += "SELECT " + tblClmn[1] + " FROM " + tblClmn[0] + " UNION ";
				}
				extractUsedObjectIdsQuery = extractUsedObjectIdsQuery.substring(0, extractUsedObjectIdsQuery.length() - 7); // Remove the last " UNION "
				try (ResultSet result = statement.executeQuery(extractUsedObjectIdsQuery))
				{
					while (result.next())
					{
						usedIds.add(result.getInt(1));
					}
				}
			}
			Collections.sort(usedIds);
			
			// Register used ids.
			for (int usedObjectId : usedIds)
			{
				final int objectId = usedObjectId - FIRST_OID;
				if (objectId < 0)
				{
					LOGGER.warning("IdManager: Object ID " + usedObjectId + " in DB is less than minimum ID of " + FIRST_OID);
					continue;
				}
				_freeIds.set(usedObjectId - FIRST_OID);
				_freeIdCount.decrementAndGet();
			}
			
			_nextFreeId = new AtomicInteger(_freeIds.nextClearBit(0));
			_initialized = true;
		}
		catch (Exception e)
		{
			_initialized = false;
			LOGGER.severe("IdManager: Could not be initialized properly: " + e.getMessage());
		}
		
		// Schedule increase capacity task.
		ThreadPool.scheduleAtFixedRate(() ->
		{
			synchronized (_nextFreeId)
			{
				if (PrimeFinder.nextPrime((usedIdCount() * 11) / 10) > _freeIds.size())
				{
					increaseBitSetCapacity();
				}
			}
		}, 30000, 30000);
		
		LOGGER.info("IdManager: " + _freeIds.size() + " id's available.");
	}
	
	public void releaseId(int objectId)
	{
		synchronized (_nextFreeId)
		{
			if ((objectId - FIRST_OID) > -1)
			{
				_freeIds.clear(objectId - FIRST_OID);
				_freeIdCount.incrementAndGet();
			}
			else
			{
				LOGGER.warning("IdManager: Release objectID " + objectId + " failed (< " + FIRST_OID + ")");
			}
		}
	}
	
	public int getNextId()
	{
		synchronized (_nextFreeId)
		{
			final int newId = _nextFreeId.get();
			_freeIds.set(newId);
			_freeIdCount.decrementAndGet();
			
			final int nextFree = _freeIds.nextClearBit(newId) < 0 ? _freeIds.nextClearBit(0) : _freeIds.nextClearBit(newId);
			if (nextFree < 0)
			{
				if (_freeIds.size() >= FREE_OBJECT_ID_SIZE)
				{
					throw new NullPointerException("IdManager: Ran out of valid ids.");
				}
				increaseBitSetCapacity();
			}
			_nextFreeId.set(nextFree);
			
			return newId + FIRST_OID;
		}
	}
	
	private void increaseBitSetCapacity()
	{
		final BitSet newBitSet = new BitSet(PrimeFinder.nextPrime((usedIdCount() * 11) / 10));
		newBitSet.or(_freeIds);
		_freeIds = newBitSet;
	}
	
	private int usedIdCount()
	{
		return _freeIdCount.get() - FIRST_OID;
	}
	
	public static int size()
	{
		return _freeIdCount.get();
	}
	
	public static boolean hasInitialized()
	{
		return _initialized;
	}
	
	public static IdManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final IdManager INSTANCE = new IdManager();
	}
}
