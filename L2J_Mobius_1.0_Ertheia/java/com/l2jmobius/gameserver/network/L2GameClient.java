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
package com.l2jmobius.gameserver.network;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jmobius.Config;
import com.l2jmobius.commons.database.DatabaseFactory;
import com.l2jmobius.commons.network.ChannelInboundHandler;
import com.l2jmobius.commons.network.ICrypt;
import com.l2jmobius.commons.network.IIncomingPacket;
import com.l2jmobius.commons.network.IOutgoingPacket;
import com.l2jmobius.gameserver.LoginServerThread;
import com.l2jmobius.gameserver.LoginServerThread.SessionKey;
import com.l2jmobius.gameserver.ThreadPoolManager;
import com.l2jmobius.gameserver.data.sql.impl.CharNameTable;
import com.l2jmobius.gameserver.data.sql.impl.ClanTable;
import com.l2jmobius.gameserver.data.sql.impl.OfflineTradersTable;
import com.l2jmobius.gameserver.data.xml.impl.SecondaryAuthData;
import com.l2jmobius.gameserver.enums.CharacterDeleteFailType;
import com.l2jmobius.gameserver.idfactory.IdFactory;
import com.l2jmobius.gameserver.instancemanager.AntiFeedManager;
import com.l2jmobius.gameserver.instancemanager.CommissionManager;
import com.l2jmobius.gameserver.instancemanager.MailManager;
import com.l2jmobius.gameserver.instancemanager.MentorManager;
import com.l2jmobius.gameserver.model.CharSelectInfoPackage;
import com.l2jmobius.gameserver.model.L2Clan;
import com.l2jmobius.gameserver.model.L2World;
import com.l2jmobius.gameserver.model.actor.L2Summon;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.entity.L2Event;
import com.l2jmobius.gameserver.model.holders.ClientHardwareInfoHolder;
import com.l2jmobius.gameserver.model.olympiad.OlympiadManager;
import com.l2jmobius.gameserver.model.zone.ZoneId;
import com.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import com.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;
import com.l2jmobius.gameserver.network.serverpackets.ServerClose;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import com.l2jmobius.gameserver.security.SecondaryPasswordAuth;
import com.l2jmobius.gameserver.util.FloodProtectors;
import com.l2jmobius.gameserver.util.Util;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

/**
 * Represents a client connected on Game Server.
 * @author KenM
 */
public final class L2GameClient extends ChannelInboundHandler<L2GameClient>
{
	protected static final Logger LOGGER = Logger.getLogger(L2GameClient.class.getName());
	protected static final Logger _logAccounting = Logger.getLogger("accounting");
	
	private final int _objectId;
	
	// Info
	private InetAddress _addr;
	private Channel _channel;
	private String _accountName;
	private SessionKey _sessionId;
	private L2PcInstance _activeChar;
	private final ReentrantLock _activeCharLock = new ReentrantLock();
	private SecondaryPasswordAuth _secondaryAuth;
	private ClientHardwareInfoHolder _hardwareInfo;
	private boolean _isAuthedGG;
	private final long _connectionStartTime = System.currentTimeMillis();
	private CharSelectInfoPackage[] _charSlotMapping = null;
	
	// flood protectors
	private final FloodProtectors _floodProtectors = new FloodProtectors(this);
	
	// Task
	protected final ScheduledFuture<?> _autoSaveInDB;
	protected ScheduledFuture<?> _cleanupTask = null;
	
	// Crypt
	private final Crypt _crypt;
	
	private boolean _isDetached = false;
	
	private boolean _protocol;
	
	private int[][] trace;
	
	public L2GameClient()
	{
		_objectId = IdFactory.getInstance().getNextId();
		_crypt = new Crypt(this);
		if (Config.CHAR_DATA_STORE_INTERVAL > 0)
		{
			_autoSaveInDB = ThreadPoolManager.scheduleAtFixedRate(new AutoSaveTask(), 300000L, Config.CHAR_DATA_STORE_INTERVAL);
		}
		else
		{
			_autoSaveInDB = null;
		}
	}
	
	public int getObjectId()
	{
		return _objectId;
	}
	
	@Override
	public void channelActive(ChannelHandlerContext ctx)
	{
		super.channelActive(ctx);
		
		setConnectionState(ConnectionState.CONNECTED);
		final InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
		_addr = address.getAddress();
		_channel = ctx.channel();
		LOGGER.finer("Client Connected: " + ctx.channel());
	}
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx)
	{
		LOGGER.finer("Client Disconnected: " + ctx.channel());
		
		// no long running tasks here, do it async
		try
		{
			ThreadPoolManager.execute(new DisconnectTask());
		}
		catch (RejectedExecutionException e)
		{
			// server is closing
		}
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, IIncomingPacket<L2GameClient> packet)
	{
		try
		{
			packet.run(this);
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, "Exception for: " + toString() + " on packet.run: " + packet.getClass().getSimpleName(), e);
		}
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
	{
	}
	
	public Channel getChannel()
	{
		return _channel;
	}
	
	public byte[] enableCrypt()
	{
		final byte[] key = BlowFishKeygen.getRandomKey();
		_crypt.setKey(key);
		return key;
	}
	
	/**
	 * For loaded offline traders returns localhost address.
	 * @return cached connection IP address, for checking detached clients.
	 */
	public InetAddress getConnectionAddress()
	{
		return _addr;
	}
	
	public long getConnectionStartTime()
	{
		return _connectionStartTime;
	}
	
	public L2PcInstance getActiveChar()
	{
		return _activeChar;
	}
	
	public void setActiveChar(L2PcInstance pActiveChar)
	{
		_activeChar = pActiveChar;
	}
	
	public ReentrantLock getActiveCharLock()
	{
		return _activeCharLock;
	}
	
	public FloodProtectors getFloodProtectors()
	{
		return _floodProtectors;
	}
	
	public void setGameGuardOk(boolean val)
	{
		_isAuthedGG = val;
	}
	
	public boolean isAuthedGG()
	{
		return _isAuthedGG;
	}
	
	public void setAccountName(String pAccountName)
	{
		_accountName = pAccountName;
		
		if (SecondaryAuthData.getInstance().isEnabled())
		{
			_secondaryAuth = new SecondaryPasswordAuth(this);
		}
	}
	
	public String getAccountName()
	{
		return _accountName;
	}
	
	public void setSessionId(SessionKey sk)
	{
		_sessionId = sk;
	}
	
	public SessionKey getSessionId()
	{
		return _sessionId;
	}
	
	public void sendPacket(IClientOutgoingPacket packet)
	{
		if (_isDetached || (packet == null))
		{
			return;
		}
		
		// Write into the channel.
		_channel.writeAndFlush(packet);
		
		// Run packet implementation.
		packet.runImpl(getActiveChar());
	}
	
	/**
	 * @param smId
	 */
	public void sendPacket(SystemMessageId smId)
	{
		sendPacket(SystemMessage.getSystemMessage(smId));
	}
	
	public boolean isDetached()
	{
		return _isDetached;
	}
	
	public void setDetached(boolean b)
	{
		_isDetached = b;
	}
	
	/**
	 * Method to handle character deletion
	 * @param charslot
	 * @return a byte:
	 *         <li>-1: Error: No char was found for such charslot, caught exception, etc...
	 *         <li>0: character is not member of any clan, proceed with deletion
	 *         <li>1: character is member of a clan, but not clan leader
	 *         <li>2: character is clan leader
	 */
	public CharacterDeleteFailType markToDeleteChar(int charslot)
	{
		final int objectId = getObjectIdForSlot(charslot);
		if (objectId < 0)
		{
			return CharacterDeleteFailType.UNKNOWN;
		}
		
		if (MentorManager.getInstance().isMentor(objectId))
		{
			return CharacterDeleteFailType.MENTOR;
		}
		else if (MentorManager.getInstance().isMentee(objectId))
		{
			return CharacterDeleteFailType.MENTEE;
		}
		else if (CommissionManager.getInstance().hasCommissionItems(objectId))
		{
			return CharacterDeleteFailType.COMMISSION;
		}
		else if (MailManager.getInstance().getMailsInProgress(objectId) > 0)
		{
			return CharacterDeleteFailType.MAIL;
		}
		else
		{
			final int clanId = CharNameTable.getInstance().getClassIdById(objectId);
			if (clanId > 0)
			{
				final L2Clan clan = ClanTable.getInstance().getClan(clanId);
				if (clan != null)
				{
					if (clan.getLeaderId() == objectId)
					{
						return CharacterDeleteFailType.PLEDGE_MASTER;
					}
					return CharacterDeleteFailType.PLEDGE_MEMBER;
				}
			}
		}
		
		if (Config.DELETE_DAYS == 0)
		{
			deleteCharByObjId(objectId);
		}
		else
		{
			try (Connection con = DatabaseFactory.getInstance().getConnection();
				PreparedStatement ps2 = con.prepareStatement("UPDATE characters SET deletetime=? WHERE charId=?"))
			{
				ps2.setLong(1, System.currentTimeMillis() + (Config.DELETE_DAYS * 86400000L)); // 24*60*60*1000 = 86400000
				ps2.setInt(2, objectId);
				ps2.execute();
			}
			catch (SQLException e)
			{
				LOGGER.log(Level.WARNING, "Failed to update char delete time: ", e);
			}
		}
		
		_logAccounting.info("Delete, " + objectId + ", " + this);
		return CharacterDeleteFailType.NONE;
	}
	
	/**
	 * Save the L2PcInstance to the database.
	 */
	public void saveCharToDisk()
	{
		try
		{
			if (getActiveChar() != null)
			{
				getActiveChar().storeMe();
				getActiveChar().storeRecommendations();
				if (Config.UPDATE_ITEMS_ON_CHAR_STORE)
				{
					getActiveChar().getInventory().updateDatabase();
					getActiveChar().getWarehouse().updateDatabase();
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.log(Level.SEVERE, "Error saving character..", e);
		}
	}
	
	public void markRestoredChar(int charslot)
	{
		final int objid = getObjectIdForSlot(charslot);
		if (objid < 0)
		{
			return;
		}
		
		try (Connection con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("UPDATE characters SET deletetime=0 WHERE charId=?"))
		{
			statement.setInt(1, objid);
			statement.execute();
		}
		catch (Exception e)
		{
			LOGGER.log(Level.SEVERE, "Error restoring character.", e);
		}
		
		_logAccounting.info("Restore, " + objid + ", " + this);
	}
	
	public static void deleteCharByObjId(int objid)
	{
		if (objid < 0)
		{
			return;
		}
		
		CharNameTable.getInstance().removeName(objid);
		
		try (Connection con = DatabaseFactory.getInstance().getConnection())
		{
			try (PreparedStatement ps = con.prepareStatement("DELETE FROM character_contacts WHERE charId=? OR contactId=?"))
			{
				ps.setInt(1, objid);
				ps.setInt(2, objid);
				ps.execute();
			}
			
			try (PreparedStatement ps = con.prepareStatement("DELETE FROM character_friends WHERE charId=? OR friendId=?"))
			{
				ps.setInt(1, objid);
				ps.setInt(2, objid);
				ps.execute();
			}
			
			try (PreparedStatement ps = con.prepareStatement("DELETE FROM character_hennas WHERE charId=?"))
			{
				ps.setInt(1, objid);
				ps.execute();
			}
			
			try (PreparedStatement ps = con.prepareStatement("DELETE FROM character_macroses WHERE charId=?"))
			{
				ps.setInt(1, objid);
				ps.execute();
			}
			
			try (PreparedStatement ps = con.prepareStatement("DELETE FROM character_quests WHERE charId=?"))
			{
				ps.setInt(1, objid);
				ps.execute();
			}
			
			try (PreparedStatement ps = con.prepareStatement("DELETE FROM character_recipebook WHERE charId=?"))
			{
				ps.setInt(1, objid);
				ps.execute();
			}
			
			try (PreparedStatement ps = con.prepareStatement("DELETE FROM character_shortcuts WHERE charId=?"))
			{
				ps.setInt(1, objid);
				ps.execute();
			}
			
			try (PreparedStatement ps = con.prepareStatement("DELETE FROM character_skills WHERE charId=?"))
			{
				ps.setInt(1, objid);
				ps.execute();
			}
			
			try (PreparedStatement ps = con.prepareStatement("DELETE FROM character_skills_save WHERE charId=?"))
			{
				ps.setInt(1, objid);
				ps.execute();
			}
			
			try (PreparedStatement ps = con.prepareStatement("DELETE FROM character_subclasses WHERE charId=?"))
			{
				ps.setInt(1, objid);
				ps.execute();
			}
			
			try (PreparedStatement ps = con.prepareStatement("DELETE FROM heroes WHERE charId=?"))
			{
				ps.setInt(1, objid);
				ps.execute();
			}
			
			try (PreparedStatement ps = con.prepareStatement("DELETE FROM olympiad_nobles WHERE charId=?"))
			{
				ps.setInt(1, objid);
				ps.execute();
			}
			
			try (PreparedStatement ps = con.prepareStatement("DELETE FROM pets WHERE item_obj_id IN (SELECT object_id FROM items WHERE items.owner_id=?)"))
			{
				ps.setInt(1, objid);
				ps.execute();
			}
			
			try (PreparedStatement ps = con.prepareStatement("DELETE FROM item_attributes WHERE itemId IN (SELECT object_id FROM items WHERE items.owner_id=?)"))
			{
				ps.setInt(1, objid);
				ps.execute();
			}
			
			try (PreparedStatement ps = con.prepareStatement("DELETE FROM item_special_abilities WHERE objectId IN (SELECT object_id FROM items WHERE items.owner_id=?)"))
			{
				ps.setInt(1, objid);
				ps.execute();
			}
			
			try (PreparedStatement ps = con.prepareStatement("DELETE FROM item_variables WHERE id IN (SELECT object_id FROM items WHERE items.owner_id=?)"))
			{
				ps.setInt(1, objid);
				ps.execute();
			}
			
			try (PreparedStatement ps = con.prepareStatement("DELETE FROM items WHERE owner_id=?"))
			{
				ps.setInt(1, objid);
				ps.execute();
			}
			
			try (PreparedStatement ps = con.prepareStatement("DELETE FROM merchant_lease WHERE player_id=?"))
			{
				ps.setInt(1, objid);
				ps.execute();
			}
			
			try (PreparedStatement ps = con.prepareStatement("DELETE FROM character_reco_bonus WHERE charId=?"))
			{
				ps.setInt(1, objid);
				ps.execute();
			}
			
			try (PreparedStatement ps = con.prepareStatement("DELETE FROM character_instance_time WHERE charId=?"))
			{
				ps.setInt(1, objid);
				ps.execute();
			}
			
			try (PreparedStatement ps = con.prepareStatement("DELETE FROM character_variables WHERE charId=?"))
			{
				ps.setInt(1, objid);
				ps.execute();
			}
			
			try (PreparedStatement ps = con.prepareStatement("DELETE FROM characters WHERE charId=?"))
			{
				ps.setInt(1, objid);
				ps.execute();
			}
		}
		catch (Exception e)
		{
			LOGGER.log(Level.SEVERE, "Error deleting character.", e);
		}
	}
	
	public L2PcInstance loadCharFromDisk(int charslot)
	{
		final int objId = getObjectIdForSlot(charslot);
		if (objId < 0)
		{
			return null;
		}
		
		L2PcInstance character = L2World.getInstance().getPlayer(objId);
		if (character != null)
		{
			// exploit prevention, should not happens in normal way
			LOGGER.severe("Attempt of double login: " + character.getName() + "(" + objId + ") " + getAccountName());
			if (character.getClient() != null)
			{
				character.getClient().closeNow();
			}
			else
			{
				character.deleteMe();
			}
			return null;
		}
		
		character = L2PcInstance.load(objId);
		if (character == null)
		{
			LOGGER.severe("could not restore in slot: " + charslot);
		}
		
		// setCharacter(character);
		return character;
	}
	
	/**
	 * @param chars
	 */
	public void setCharSelection(CharSelectInfoPackage[] chars)
	{
		_charSlotMapping = chars;
	}
	
	public CharSelectInfoPackage getCharSelection(int charslot)
	{
		if ((_charSlotMapping == null) || (charslot < 0) || (charslot >= _charSlotMapping.length))
		{
			return null;
		}
		return _charSlotMapping[charslot];
	}
	
	public SecondaryPasswordAuth getSecondaryAuth()
	{
		return _secondaryAuth;
	}
	
	public void close(IOutgoingPacket packet)
	{
		if (packet != null)
		{
			_channel.writeAndFlush(packet);
		}
		_channel.close();
	}
	
	/**
	 * @param charslot
	 * @return
	 */
	private int getObjectIdForSlot(int charslot)
	{
		final CharSelectInfoPackage info = getCharSelection(charslot);
		if (info == null)
		{
			LOGGER.warning(toString() + " tried to delete Character in slot " + charslot + " but no characters exits at that slot.");
			return -1;
		}
		return info.getObjectId();
	}
	
	/**
	 * Close client connection with {@link ServerClose} packet
	 */
	public void closeNow()
	{
		_isDetached = true; // prevents more packets execution
		close(ServerClose.STATIC_PACKET);
		synchronized (this)
		{
			if (_cleanupTask != null)
			{
				cancelCleanup();
			}
			_cleanupTask = ThreadPoolManager.schedule(new CleanupTask(), 0); // instant
		}
	}
	
	/**
	 * Produces the best possible string representation of this client.
	 */
	@Override
	public String toString()
	{
		try
		{
			final InetAddress address = _addr;
			final ConnectionState state = (ConnectionState) getConnectionState();
			switch (state)
			{
				case CONNECTED:
				{
					return "[IP: " + (address == null ? "disconnected" : address.getHostAddress()) + "]";
				}
				case AUTHENTICATED:
				{
					return "[Account: " + getAccountName() + " - IP: " + (address == null ? "disconnected" : address.getHostAddress()) + "]";
				}
				case IN_GAME:
				{
					return "[Character: " + (getActiveChar() == null ? "disconnected" : getActiveChar().getName() + "[" + getActiveChar().getObjectId() + "]") + " - Account: " + getAccountName() + " - IP: " + (address == null ? "disconnected" : address.getHostAddress()) + "]";
				}
				default:
				{
					throw new IllegalStateException("Missing state on switch");
				}
			}
		}
		catch (NullPointerException e)
		{
			return "[Character read failed due to disconnect]";
		}
	}
	
	protected class DisconnectTask implements Runnable
	{
		@Override
		public void run()
		{
			boolean fast = true;
			try
			{
				if ((getActiveChar() != null) && !isDetached())
				{
					setDetached(true);
					if (offlineMode(getActiveChar()))
					{
						getActiveChar().leaveParty();
						OlympiadManager.getInstance().unRegisterNoble(getActiveChar());
						
						// If the L2PcInstance has Pet, unsummon it
						L2Summon pet = getActiveChar().getPet();
						if (pet != null)
						{
							pet.setRestoreSummon(true);
							
							pet.unSummon(getActiveChar());
							pet = getActiveChar().getPet();
							// Dead pet wasn't unsummoned, broadcast npcinfo changes (pet will be without owner name - means owner offline)
							if (pet != null)
							{
								pet.broadcastNpcInfo(0);
							}
						}
						
						getActiveChar().getServitors().values().forEach(s ->
						{
							s.setRestoreSummon(true);
							s.unSummon(getActiveChar());
						});
						
						if (Config.OFFLINE_SET_NAME_COLOR)
						{
							getActiveChar().getAppearance().setNameColor(Config.OFFLINE_NAME_COLOR);
							getActiveChar().broadcastUserInfo();
						}
						
						if (getActiveChar().getOfflineStartTime() == 0)
						{
							getActiveChar().setOfflineStartTime(System.currentTimeMillis());
						}
						
						// Store trade on exit, if realtime saving is enabled.
						if (Config.STORE_OFFLINE_TRADE_IN_REALTIME)
						{
							OfflineTradersTable.onTransaction(getActiveChar(), false, true);
						}
						
						_logAccounting.info("Entering offline mode, " + L2GameClient.this);
						return;
					}
					fast = !getActiveChar().isInCombat() && !getActiveChar().isLocked();
				}
				cleanMe(fast);
			}
			catch (Exception e1)
			{
				LOGGER.log(Level.WARNING, "Error while disconnecting client.", e1);
			}
			
			IdFactory.getInstance().releaseId(getObjectId());
		}
	}
	
	/**
	 * @param player the player to be check.
	 * @return {@code true} if the player is allowed to remain as off-line shop.
	 */
	protected boolean offlineMode(L2PcInstance player)
	{
		if (player.isInOlympiadMode() || player.isBlockedFromExit() || player.isJailed() || (player.getVehicle() != null))
		{
			return false;
		}
		
		boolean canSetShop = false;
		switch (player.getPrivateStoreType())
		{
			case SELL:
			case PACKAGE_SELL:
			case BUY:
			{
				canSetShop = Config.OFFLINE_TRADE_ENABLE;
				break;
			}
			case MANUFACTURE:
			{
				canSetShop = Config.OFFLINE_TRADE_ENABLE;
				break;
			}
			default:
			{
				canSetShop = Config.OFFLINE_CRAFT_ENABLE && player.isInCraftMode();
				break;
			}
		}
		
		if (Config.OFFLINE_MODE_IN_PEACE_ZONE && !player.isInsideZone(ZoneId.PEACE))
		{
			canSetShop = false;
		}
		return canSetShop;
	}
	
	public void cleanMe(boolean fast)
	{
		try
		{
			synchronized (this)
			{
				if (_cleanupTask == null)
				{
					_cleanupTask = ThreadPoolManager.schedule(new CleanupTask(), fast ? 5 : 15000L);
				}
			}
		}
		catch (Exception e1)
		{
			LOGGER.log(Level.WARNING, "Error during cleanup.", e1);
		}
	}
	
	protected class CleanupTask implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				// we are going to manually save the char bellow thus we can force the cancel
				if (_autoSaveInDB != null)
				{
					_autoSaveInDB.cancel(true);
					// ThreadPoolManager.getInstance().removeGeneral((Runnable) _autoSaveInDB);
				}
				
				if (getActiveChar() != null) // this should only happen on connection loss
				{
					if (getActiveChar().isLocked())
					{
						LOGGER.warning("Player " + getActiveChar().getName() + " still performing subclass actions during disconnect.");
					}
					
					// we store all data from players who are disconnected while in an event in order to restore it in the next login
					if (L2Event.isParticipant(getActiveChar()))
					{
						L2Event.savePlayerEventStatus(getActiveChar());
					}
					
					if (getActiveChar().isOnline())
					{
						getActiveChar().deleteMe();
						AntiFeedManager.getInstance().onDisconnect(L2GameClient.this);
					}
					
					// prevent closing again
					getActiveChar().setClient(null);
				}
				setActiveChar(null);
			}
			catch (Exception e1)
			{
				LOGGER.log(Level.WARNING, "Error while cleanup client.", e1);
			}
			finally
			{
				LoginServerThread.getInstance().sendLogout(getAccountName());
			}
		}
	}
	
	protected class AutoSaveTask implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				final L2PcInstance player = getActiveChar();
				if ((player != null) && player.isOnline()) // safety precaution
				{
					saveCharToDisk();
					final L2Summon pet = player.getPet();
					if (pet != null)
					{
						pet.storeMe();
					}
					player.getServitors().values().forEach(L2Summon::storeMe);
				}
			}
			catch (Exception e)
			{
				LOGGER.log(Level.SEVERE, "Error on AutoSaveTask.", e);
			}
		}
	}
	
	public boolean isProtocolOk()
	{
		return _protocol;
	}
	
	public void setProtocolOk(boolean b)
	{
		_protocol = b;
	}
	
	public boolean handleCheat(String punishment)
	{
		if (_activeChar != null)
		{
			Util.handleIllegalPlayerAction(_activeChar, toString() + ": " + punishment, Config.DEFAULT_PUNISH);
			return true;
		}
		
		final Logger logAudit = Logger.getLogger("audit");
		logAudit.info("AUDIT: Client " + toString() + " kicked for reason: " + punishment);
		closeNow();
		return false;
	}
	
	public void setClientTracert(int[][] tracert)
	{
		trace = tracert;
	}
	
	public int[][] getTrace()
	{
		return trace;
	}
	
	private boolean cancelCleanup()
	{
		final Future<?> task = _cleanupTask;
		if (task != null)
		{
			_cleanupTask = null;
			return task.cancel(true);
		}
		return false;
	}
	
	public void sendActionFailed()
	{
		sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	public ICrypt getCrypt()
	{
		return _crypt;
	}
	
	/**
	 * @return the hardwareInfo
	 */
	public ClientHardwareInfoHolder getHardwareInfo()
	{
		return _hardwareInfo;
	}
	
	/**
	 * @param hardwareInfo
	 */
	public void setHardwareInfo(ClientHardwareInfoHolder hardwareInfo)
	{
		_hardwareInfo = hardwareInfo;
	}
}
