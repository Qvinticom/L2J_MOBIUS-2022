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
package org.l2jmobius.gameserver.handler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.instance.SiegeGuard;
import org.l2jmobius.gameserver.model.sevensigns.SevenSigns;
import org.l2jmobius.gameserver.model.spawn.Spawn;
import org.l2jmobius.gameserver.model.spawn.SpawnListener;
import org.l2jmobius.gameserver.network.serverpackets.CreatureSay;

/**
 * Auto Chat Handler Allows NPCs to automatically send messages to nearby players at a set time interval.
 * @author Tempy
 */
public class AutoChatHandler implements SpawnListener
{
	protected static final Logger LOGGER = Logger.getLogger(AutoChatHandler.class.getName());
	
	private static final long DEFAULT_CHAT_DELAY = 30000; // 30 secs by default
	protected Map<Integer, AutoChatInstance> _registeredChats;
	
	protected AutoChatHandler()
	{
		_registeredChats = new HashMap<>();
		restoreChatData();
		Spawn.addSpawnListener(this);
	}
	
	private void restoreChatData()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("SELECT * FROM auto_chat ORDER BY groupId ASC");
			final ResultSet rs = statement.executeQuery();
			
			while (rs.next())
			{
				final PreparedStatement statement2 = con.prepareStatement("SELECT * FROM auto_chat_text WHERE groupId=?");
				statement2.setInt(1, rs.getInt("groupId"));
				final ResultSet rs2 = statement2.executeQuery();
				rs2.last();
				final String[] chatTexts = new String[rs2.getRow()];
				int i = 0;
				rs2.first();
				while (rs2.next())
				{
					chatTexts[i++] = rs2.getString("chatText");
				}
				registerGlobalChat(rs.getInt("npcId"), chatTexts, rs.getLong("chatDelay"));
				statement2.close();
				rs2.close();
			}
			
			rs.close();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("AutoSpawnHandler: Could not restore chat data: " + e);
		}
	}
	
	public int size()
	{
		return _registeredChats.size();
	}
	
	/**
	 * Registers a globally active auto chat for ALL instances of the given NPC ID.<br>
	 * Returns the associated auto chat instance.
	 * @param npcId
	 * @param chatTexts
	 * @param chatDelay (-1 = default delay)
	 * @return AutoChatInstance chatInst
	 */
	public AutoChatInstance registerGlobalChat(int npcId, String[] chatTexts, long chatDelay)
	{
		return registerChat(npcId, null, chatTexts, chatDelay);
	}
	
	/**
	 * Registers a NON globally-active auto chat for the given NPC instance, and adds to the currently assigned chat instance for this NPC ID, otherwise creates a new instance if a previous one is not found.<br>
	 * Returns the associated auto chat instance.
	 * @param npcInst
	 * @param chatTexts
	 * @param chatDelay (-1 = default delay)
	 * @return AutoChatInstance chatInst
	 */
	public AutoChatInstance registerChat(Npc npcInst, String[] chatTexts, long chatDelay)
	{
		return registerChat(npcInst.getNpcId(), npcInst, chatTexts, chatDelay);
	}
	
	private final AutoChatInstance registerChat(int npcId, Npc npcInst, String[] chatTexts, long chatDelay)
	{
		AutoChatInstance chatInst = null;
		if (_registeredChats.containsKey(npcId))
		{
			chatInst = _registeredChats.get(npcId);
		}
		else
		{
			chatInst = new AutoChatInstance(npcId, chatTexts, chatDelay < 0 ? DEFAULT_CHAT_DELAY : chatDelay, (npcInst == null));
		}
		
		if (npcInst != null)
		{
			chatInst.addChatDefinition(npcInst);
		}
		
		_registeredChats.put(npcId, chatInst);
		return chatInst;
	}
	
	/**
	 * Removes and cancels ALL auto chat definition for the given NPC ID, and removes its chat instance if it exists.
	 * @param npcId
	 * @return boolean removedSuccessfully
	 */
	public boolean removeChat(int npcId)
	{
		final AutoChatInstance chatInst = _registeredChats.get(npcId);
		return removeChat(chatInst);
	}
	
	/**
	 * Removes and cancels ALL auto chats for the given chat instance.
	 * @param chatInst
	 * @return removedSuccessfully
	 */
	public boolean removeChat(AutoChatInstance chatInst)
	{
		if (chatInst == null)
		{
			return false;
		}
		
		_registeredChats.remove(chatInst.getNPCId());
		chatInst.setActive(false);
		return true;
	}
	
	/**
	 * Returns the associated auto chat instance either by the given NPC ID or object ID.
	 * @param id
	 * @param byObjectId
	 * @return chatInst
	 */
	public AutoChatInstance getAutoChatInstance(int id, boolean byObjectId)
	{
		if (!byObjectId)
		{
			return _registeredChats.get(id);
		}
		
		for (AutoChatInstance chatInst : _registeredChats.values())
		{
			if (chatInst.getChatDefinition(id) != null)
			{
				return chatInst;
			}
		}
		
		return null;
	}
	
	/**
	 * Sets the active state of all auto chat instances to that specified, and cancels the scheduled chat task if necessary.
	 * @param isActive
	 */
	public void setAutoChatActive(boolean isActive)
	{
		for (AutoChatInstance chatInst : _registeredChats.values())
		{
			chatInst.setActive(isActive);
		}
	}
	
	/**
	 * Used in conjunction with a SpawnListener, this method is called every time an NPC is spawned in the world.<br>
	 * If an auto chat instance is set to be "global", all instances matching the registered NPC ID will be added to that chat instance.
	 */
	@Override
	public void npcSpawned(Npc npc)
	{
		synchronized (_registeredChats)
		{
			if (npc == null)
			{
				return;
			}
			
			final int npcId = npc.getNpcId();
			if (_registeredChats.containsKey(npcId))
			{
				final AutoChatInstance chatInst = _registeredChats.get(npcId);
				if ((chatInst != null) && chatInst.isGlobal())
				{
					chatInst.addChatDefinition(npc);
				}
			}
		}
	}
	
	/**
	 * Auto Chat Instance<br>
	 * Manages the auto chat instances for a specific registered NPC ID.
	 * @author Tempy
	 */
	public class AutoChatInstance
	{
		protected int _npcId;
		private long _defaultDelay = DEFAULT_CHAT_DELAY;
		private String[] _defaultTexts;
		private boolean _defaultRandom = false;
		
		private boolean _globalChat = false;
		private boolean _isActive;
		
		private final Map<Integer, AutoChatDefinition> _chatDefinitions = new HashMap<>();
		protected ScheduledFuture<?> _chatTask;
		
		protected AutoChatInstance(int npcId, String[] chatTexts, long chatDelay, boolean isGlobal)
		{
			_defaultTexts = chatTexts;
			_npcId = npcId;
			_defaultDelay = chatDelay;
			_globalChat = isGlobal;
			setActive(true);
		}
		
		protected AutoChatDefinition getChatDefinition(int objectId)
		{
			return _chatDefinitions.get(objectId);
		}
		
		protected Collection<AutoChatDefinition> getChatDefinitions()
		{
			return _chatDefinitions.values();
		}
		
		/**
		 * Defines an auto chat for an instance matching this auto chat instance's registered NPC ID, and launches the scheduled chat task.<br>
		 * Returns the object ID for the NPC instance, with which to refer to the created chat definition.<br>
		 * <b>Note</b>: Uses pre-defined default values for texts and chat delays from the chat instance.
		 * @param npcInst
		 * @return objectId
		 */
		public int addChatDefinition(Npc npcInst)
		{
			return addChatDefinition(npcInst, null, 0);
		}
		
		/**
		 * Defines an auto chat for an instance matching this auto chat instance's registered NPC ID, and launches the scheduled chat task.<br>
		 * Returns the object ID for the NPC instance, with which to refer to the created chat definition.
		 * @param npcInst
		 * @param chatTexts
		 * @param chatDelay
		 * @return objectId
		 */
		public int addChatDefinition(Npc npcInst, String[] chatTexts, long chatDelay)
		{
			final int objectId = npcInst.getObjectId();
			final AutoChatDefinition chatDef = new AutoChatDefinition(this, npcInst, chatTexts, chatDelay);
			if (npcInst instanceof SiegeGuard)
			{
				chatDef.setRandomChat(true);
			}
			
			_chatDefinitions.put(objectId, chatDef);
			
			return objectId;
		}
		
		/**
		 * Removes a chat definition specified by the given object ID.
		 * @param objectId
		 * @return removedSuccessfully
		 */
		public boolean removeChatDefinition(int objectId)
		{
			if (!_chatDefinitions.containsKey(objectId))
			{
				return false;
			}
			
			final AutoChatDefinition chatDefinition = _chatDefinitions.get(objectId);
			chatDefinition.setActive(false);
			
			_chatDefinitions.remove(objectId);
			
			return true;
		}
		
		/**
		 * Tests if this auto chat instance is active.
		 * @return boolean isActive
		 */
		public boolean isActive()
		{
			return _isActive;
		}
		
		/**
		 * Tests if this auto chat instance applies to ALL currently spawned instances of the registered NPC ID.
		 * @return boolean isGlobal
		 */
		public boolean isGlobal()
		{
			return _globalChat;
		}
		
		/**
		 * Tests if random order is the DEFAULT for new chat definitions.
		 * @return boolean isRandom
		 */
		public boolean isDefaultRandom()
		{
			return _defaultRandom;
		}
		
		/**
		 * Tests if the auto chat definition given by its object ID is set to be random.
		 * @param objectId
		 * @return isRandom
		 */
		public boolean isRandomChat(int objectId)
		{
			if (!_chatDefinitions.containsKey(objectId))
			{
				return false;
			}
			return _chatDefinitions.get(objectId).isRandomChat();
		}
		
		/**
		 * Returns the ID of the NPC type managed by this auto chat instance.
		 * @return int npcId
		 */
		public int getNPCId()
		{
			return _npcId;
		}
		
		/**
		 * Returns the number of auto chat definitions stored for this instance.
		 * @return int definitionCount
		 */
		public int getDefinitionCount()
		{
			return _chatDefinitions.size();
		}
		
		/**
		 * Returns a list of all NPC instances handled by this auto chat instance.
		 * @return Npc[] npcInsts
		 */
		public List<Npc> getNpcList()
		{
			final List<Npc> npcInsts = new ArrayList<>();
			for (AutoChatDefinition chatDefinition : _chatDefinitions.values())
			{
				npcInsts.add(chatDefinition._npcInstance);
			}
			return npcInsts;
		}
		
		/**
		 * A series of methods used to get and set default values for new chat definitions.
		 * @return
		 */
		public long getDefaultDelay()
		{
			return _defaultDelay;
		}
		
		public String[] getDefaultTexts()
		{
			return _defaultTexts;
		}
		
		public void setDefaultChatDelay(long delayValue)
		{
			_defaultDelay = delayValue;
		}
		
		public void setDefaultChatTexts(String[] textsValue)
		{
			_defaultTexts = textsValue;
		}
		
		public void setDefaultRandom(boolean randValue)
		{
			_defaultRandom = randValue;
		}
		
		/**
		 * Sets a specific chat delay for the specified auto chat definition given by its object ID.
		 * @param objectId
		 * @param delayValue
		 */
		public void setChatDelay(int objectId, long delayValue)
		{
			final AutoChatDefinition chatDef = getChatDefinition(objectId);
			if (chatDef != null)
			{
				chatDef.setChatDelay(delayValue);
			}
		}
		
		/**
		 * Sets a specific set of chat texts for the specified auto chat definition given by its object ID.
		 * @param objectId
		 * @param textsValue
		 */
		public void setChatTexts(int objectId, String[] textsValue)
		{
			final AutoChatDefinition chatDef = getChatDefinition(objectId);
			if (chatDef != null)
			{
				chatDef.setChatTexts(textsValue);
			}
		}
		
		/**
		 * Sets specifically to use random chat order for the auto chat definition given by its object ID.
		 * @param objectId
		 * @param randValue
		 */
		public void setRandomChat(int objectId, boolean randValue)
		{
			final AutoChatDefinition chatDef = getChatDefinition(objectId);
			if (chatDef != null)
			{
				chatDef.setRandomChat(randValue);
			}
		}
		
		/**
		 * Sets the activity of ALL auto chat definitions handled by this chat instance.
		 * @param activeValue
		 */
		public void setActive(boolean activeValue)
		{
			if (_isActive == activeValue)
			{
				return;
			}
			
			_isActive = activeValue;
			if (!_globalChat)
			{
				for (AutoChatDefinition chatDefinition : _chatDefinitions.values())
				{
					chatDefinition.setActive(activeValue);
				}
				
				return;
			}
			
			if (isActive())
			{
				final AutoChatRunner acr = new AutoChatRunner(_npcId, -1);
				_chatTask = ThreadPool.scheduleAtFixedRate(acr, _defaultDelay, _defaultDelay);
			}
			else
			{
				_chatTask.cancel(false);
			}
		}
		
		/**
		 * Auto Chat Definition<br>
		 * <br>
		 * Stores information about specific chat data for an instance of the NPC ID specified by the containing auto chat instance.<br>
		 * Each NPC instance of this type should be stored in a subsequent AutoChatDefinition class.
		 * @author Tempy
		 */
		private class AutoChatDefinition
		{
			protected int _chatIndex = 0;
			protected Npc _npcInstance;
			
			protected AutoChatInstance _chatInstance;
			
			private long _chatDelay = 0;
			private String[] _chatTexts = null;
			private boolean _isActiveDefinition;
			private boolean _randomChat;
			
			protected AutoChatDefinition(AutoChatInstance chatInst, Npc npcInst, String[] chatTexts, long chatDelay)
			{
				_npcInstance = npcInst;
				_chatInstance = chatInst;
				_randomChat = chatInst.isDefaultRandom();
				_chatDelay = chatDelay;
				_chatTexts = chatTexts;
				
				// If global chat isn't enabled for the parent instance, then handle the chat task locally.
				if (!chatInst.isGlobal())
				{
					setActive(true);
				}
			}
			
			protected String[] getChatTexts()
			{
				if (_chatTexts != null)
				{
					return _chatTexts;
				}
				return _chatInstance.getDefaultTexts();
			}
			
			private long getChatDelay()
			{
				if (_chatDelay > 0)
				{
					return _chatDelay;
				}
				return _chatInstance.getDefaultDelay();
			}
			
			private boolean isActive()
			{
				return _isActiveDefinition;
			}
			
			boolean isRandomChat()
			{
				return _randomChat;
			}
			
			void setRandomChat(boolean randValue)
			{
				_randomChat = randValue;
			}
			
			void setChatDelay(long delayValue)
			{
				_chatDelay = delayValue;
			}
			
			void setChatTexts(String[] textsValue)
			{
				_chatTexts = textsValue;
			}
			
			void setActive(boolean activeValue)
			{
				if (isActive() == activeValue)
				{
					return;
				}
				
				if (activeValue)
				{
					final AutoChatRunner acr = new AutoChatRunner(_npcId, _npcInstance.getObjectId());
					if (getChatDelay() == 0)
					{
						// Schedule it set to 5Ms, isn't error, if use 0 sometimes chatDefinition return null in AutoChatRunner
						_chatTask = ThreadPool.schedule(acr, 5);
					}
					else
					{
						_chatTask = ThreadPool.scheduleAtFixedRate(acr, getChatDelay(), getChatDelay());
					}
				}
				else
				{
					_chatTask.cancel(false);
				}
				
				_isActiveDefinition = activeValue;
			}
		}
		
		/**
		 * Auto Chat Runner<br>
		 * <br>
		 * Represents the auto chat scheduled task for each chat instance.
		 * @author Tempy
		 */
		private class AutoChatRunner implements Runnable
		{
			private final int _runnerNpcId;
			private final int _objectId;
			
			protected AutoChatRunner(int pNpcId, int pObjectId)
			{
				_runnerNpcId = pNpcId;
				_objectId = pObjectId;
			}
			
			@Override
			public synchronized void run()
			{
				final AutoChatInstance chatInst = _registeredChats.get(_runnerNpcId);
				Collection<AutoChatDefinition> chatDefinitions;
				if (chatInst.isGlobal())
				{
					chatDefinitions = chatInst.getChatDefinitions();
				}
				else
				{
					final AutoChatDefinition chatDef = chatInst.getChatDefinition(_objectId);
					if (chatDef == null)
					{
						LOGGER.warning("AutoChatHandler: Auto chat definition is NULL for NPC ID " + _npcId + ".");
						return;
					}
					
					chatDefinitions = new ArrayList<>();
					chatDefinitions.add(chatDef);
				}
				
				for (AutoChatDefinition chatDef : chatDefinitions)
				{
					try
					{
						final Npc chatNpc = chatDef._npcInstance;
						final List<Player> nearbyPlayers = new ArrayList<>();
						final List<Player> nearbyGMs = new ArrayList<>();
						for (Creature creature : chatNpc.getKnownList().getKnownCharactersInRadius(1500))
						{
							if (!(creature instanceof Player))
							{
								continue;
							}
							
							if (((Player) creature).isGM())
							{
								nearbyGMs.add((Player) creature);
							}
							else
							{
								nearbyPlayers.add((Player) creature);
							}
						}
						
						final int maxIndex = chatDef.getChatTexts().length;
						int lastIndex = Rnd.get(maxIndex);
						
						final String creatureName = chatNpc.getName();
						String text;
						if (!chatDef.isRandomChat())
						{
							lastIndex = chatDef._chatIndex;
							lastIndex++;
							
							if (lastIndex == maxIndex)
							{
								lastIndex = 0;
							}
							
							chatDef._chatIndex = lastIndex;
						}
						
						text = chatDef.getChatTexts()[lastIndex];
						if (text == null)
						{
							return;
						}
						
						if (!nearbyPlayers.isEmpty())
						{
							final int randomPlayerIndex = Rnd.get(nearbyPlayers.size());
							final Player randomPlayer = nearbyPlayers.get(randomPlayerIndex);
							final int winningCabal = SevenSigns.getInstance().getCabalHighestScore();
							int losingCabal = SevenSigns.CABAL_NULL;
							if (winningCabal == SevenSigns.CABAL_DAWN)
							{
								losingCabal = SevenSigns.CABAL_DUSK;
							}
							else if (winningCabal == SevenSigns.CABAL_DUSK)
							{
								losingCabal = SevenSigns.CABAL_DAWN;
							}
							
							if (text.indexOf("%player_random%") > -1)
							{
								text = text.replace("%player_random%", randomPlayer.getName());
							}
							
							if (text.indexOf("%player_cabal_winner%") > -1)
							{
								for (Player nearbyPlayer : nearbyPlayers)
								{
									if (SevenSigns.getInstance().getPlayerCabal(nearbyPlayer) == winningCabal)
									{
										text = text.replace("%player_cabal_winner%", nearbyPlayer.getName());
										break;
									}
								}
							}
							
							if (text.indexOf("%player_cabal_loser%") > -1)
							{
								for (Player nearbyPlayer : nearbyPlayers)
								{
									if (SevenSigns.getInstance().getPlayerCabal(nearbyPlayer) == losingCabal)
									{
										text = text.replace("%player_cabal_loser%", nearbyPlayer.getName());
										break;
									}
								}
							}
						}
						
						if (text == null)
						{
							return;
						}
						
						if (text.contains("%player_cabal_loser%") || text.contains("%player_cabal_winner%") || text.contains("%player_random%"))
						{
							return;
						}
						
						final CreatureSay cs = new CreatureSay(chatNpc.getObjectId(), ChatType.GENERAL, creatureName, text);
						for (Player nearbyPlayer : nearbyPlayers)
						{
							nearbyPlayer.sendPacket(cs);
						}
						for (Player nearbyGM : nearbyGMs)
						{
							nearbyGM.sendPacket(cs);
						}
					}
					catch (Exception e)
					{
						LOGGER.info("Problem with AutoChatHandler: " + e.getMessage());
						return;
					}
				}
			}
		}
	}
	
	public static AutoChatHandler getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final AutoChatHandler INSTANCE = new AutoChatHandler();
	}
}
