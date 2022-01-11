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
package org.l2jmobius.gameserver.network;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.commons.network.ChannelInboundHandler;
import org.l2jmobius.commons.network.ICrypt;
import org.l2jmobius.commons.network.IIncomingPacket;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.commons.util.Chronos;
import org.l2jmobius.gameserver.LoginServerThread;
import org.l2jmobius.gameserver.LoginServerThread.SessionKey;
import org.l2jmobius.gameserver.data.SkillTable;
import org.l2jmobius.gameserver.data.sql.ClanTable;
import org.l2jmobius.gameserver.data.sql.OfflineTraderTable;
import org.l2jmobius.gameserver.model.CharSelectInfoPackage;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.clan.Clan;
import org.l2jmobius.gameserver.model.olympiad.Olympiad;
import org.l2jmobius.gameserver.model.zone.ZoneId;
import org.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.util.FloodProtectors;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

/**
 * Represents a client connected on Game Server.
 * @author KenM
 */
public class GameClient extends ChannelInboundHandler<GameClient>
{
	protected static final Logger LOGGER = Logger.getLogger(GameClient.class.getName());
	protected static final Logger LOGGER_ACCOUNTING = Logger.getLogger("accounting");
	
	protected Player _player;
	private final FloodProtectors _floodProtectors = new FloodProtectors(this);
	private final ReentrantLock _playerLock = new ReentrantLock();
	private final Crypt _crypt = new Crypt();
	private InetAddress _addr;
	private Channel _channel;
	private String _accountName;
	private SessionKey _sessionId;
	private final List<Integer> _charSlotMapping = new ArrayList<>();
	private volatile boolean _isDetached = false;
	private boolean _isAuthedGG;
	private int _protocolVersion;
	private ScheduledFuture<?> _cleanupTask = null;
	
	@Override
	public void channelActive(ChannelHandlerContext ctx)
	{
		super.channelActive(ctx);
		
		setConnectionState(ConnectionState.CONNECTED);
		final InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
		_addr = address.getAddress();
		_channel = ctx.channel();
		LOGGER_ACCOUNTING.finer("Client Connected: " + ctx.channel());
	}
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx)
	{
		LOGGER_ACCOUNTING.finer("Client Disconnected: " + ctx.channel());
		LoginServerThread.getInstance().sendLogout(getAccountName());
		
		if ((_player == null) || !_player.isInOfflineMode())
		{
			// no long running tasks here, do it async
			try
			{
				ThreadPool.execute(new DisconnectTask());
			}
			catch (RejectedExecutionException e)
			{
				// server is closing
			}
		}
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, IIncomingPacket<GameClient> packet)
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
	
	public void closeNow()
	{
		if (_channel != null)
		{
			_channel.close();
		}
		
		synchronized (this)
		{
			if (_cleanupTask != null)
			{
				cancelCleanup();
			}
			_cleanupTask = ThreadPool.schedule(new CleanupTask(), 0); // delayed?
		}
	}
	
	public void close(IClientOutgoingPacket packet)
	{
		sendPacket(packet);
		closeNow();
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
	
	public Player getPlayer()
	{
		return _player;
	}
	
	public void setPlayer(Player player)
	{
		_player = player;
		if (_player != null)
		{
			World.getInstance().storeObject(_player);
		}
	}
	
	public ReentrantLock getPlayerLock()
	{
		return _playerLock;
	}
	
	public void setGameGuardOk(boolean value)
	{
		_isAuthedGG = value;
	}
	
	public boolean isAuthedGG()
	{
		return _isAuthedGG;
	}
	
	public void setAccountName(String pAccountName)
	{
		_accountName = pAccountName;
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
		packet.runImpl(_player);
	}
	
	/**
	 * @param smId
	 */
	public void sendPacket(SystemMessageId smId)
	{
		sendPacket(new SystemMessage(smId));
	}
	
	public boolean isDetached()
	{
		return _isDetached;
	}
	
	public void setDetached(boolean value)
	{
		_isDetached = value;
	}
	
	/**
	 * Method to handle character deletion
	 * @param characterSlot
	 * @return a byte:
	 *         <li>-1: Error: No char was found for such charslot, caught exception, etc...
	 *         <li>0: character is not member of any clan, proceed with deletion
	 *         <li>1: character is member of a clan, but not clan leader
	 *         <li>2: character is clan leader
	 */
	public byte markToDeleteChar(int characterSlot)
	{
		final int objectId = getObjectIdForSlot(characterSlot);
		if (objectId < 0)
		{
			return -1;
		}
		
		byte answer = -1;
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT clanId from characters WHERE charId=?");
			statement.setInt(1, objectId);
			final ResultSet rs = statement.executeQuery();
			rs.next();
			
			final int clanId = rs.getInt(1);
			answer = 0;
			if (clanId != 0)
			{
				final Clan clan = ClanTable.getInstance().getClan(clanId);
				if (clan == null)
				{
					answer = 0; // jeezes!
				}
				else if (clan.getLeaderId() == objectId)
				{
					answer = 2;
				}
				else
				{
					answer = 1;
				}
			}
			
			// Setting delete time
			if (answer == 0)
			{
				if (Config.DELETE_DAYS == 0)
				{
					deleteCharByObjId(objectId);
				}
				else
				{
					statement = con.prepareStatement("UPDATE characters SET deletetime=? WHERE charId=?");
					statement.setLong(1, Chronos.currentTimeMillis() + (Config.DELETE_DAYS * 86400000)); // 24*60*60*1000 = 86400000
					statement.setInt(2, objectId);
					statement.execute();
					statement.close();
					rs.close();
				}
			}
			else
			{
				statement.close();
				rs.close();
			}
		}
		catch (Exception e)
		{
			LOGGER.warning("Data error on update delete time of char: " + e);
			answer = -1;
		}
		
		return answer;
	}
	
	public void markRestoredChar(int characterSlot)
	{
		// have to make sure active character must be nulled
		final int objectId = getObjectIdForSlot(characterSlot);
		if (objectId < 0)
		{
			return;
		}
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement statement = con.prepareStatement("UPDATE characters SET deletetime=0 WHERE charId=?"))
		{
			statement.setInt(1, objectId);
			statement.execute();
		}
		catch (Exception e)
		{
			LOGGER.warning("Data error on restoring char " + e);
		}
	}
	
	public static void deleteCharByObjId(int objectId)
	{
		if (objectId < 0)
		{
			return;
		}
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement ps;
			
			ps = con.prepareStatement("DELETE FROM character_friends WHERE char_id=? OR friend_id=?");
			ps.setInt(1, objectId);
			ps.setInt(2, objectId);
			ps.execute();
			ps.close();
			
			ps = con.prepareStatement("DELETE FROM character_hennas WHERE char_obj_id=?");
			ps.setInt(1, objectId);
			ps.execute();
			ps.close();
			
			ps = con.prepareStatement("DELETE FROM character_macroses WHERE char_obj_id=?");
			ps.setInt(1, objectId);
			ps.execute();
			ps.close();
			
			ps = con.prepareStatement("DELETE FROM character_quests WHERE char_id=?");
			ps.setInt(1, objectId);
			ps.execute();
			ps.close();
			
			ps = con.prepareStatement("DELETE FROM character_recipebook WHERE char_id=?");
			ps.setInt(1, objectId);
			ps.execute();
			ps.close();
			
			ps = con.prepareStatement("DELETE FROM character_shortcuts WHERE char_obj_id=?");
			ps.setInt(1, objectId);
			ps.execute();
			ps.close();
			
			ps = con.prepareStatement("DELETE FROM character_skills WHERE char_obj_id=?");
			ps.setInt(1, objectId);
			ps.execute();
			ps.close();
			
			ps = con.prepareStatement("DELETE FROM character_skills_save WHERE char_obj_id=?");
			ps.setInt(1, objectId);
			ps.execute();
			ps.close();
			
			ps = con.prepareStatement("DELETE FROM character_subclasses WHERE char_obj_id=?");
			ps.setInt(1, objectId);
			ps.execute();
			ps.close();
			
			ps = con.prepareStatement("DELETE FROM heroes WHERE charId=?");
			ps.setInt(1, objectId);
			ps.execute();
			ps.close();
			
			ps = con.prepareStatement("DELETE FROM olympiad_nobles WHERE charId=?");
			ps.setInt(1, objectId);
			ps.execute();
			ps.close();
			
			ps = con.prepareStatement("DELETE FROM seven_signs WHERE char_obj_id=?");
			ps.setInt(1, objectId);
			ps.execute();
			ps.close();
			
			ps = con.prepareStatement("DELETE FROM pets WHERE item_obj_id IN (SELECT object_id FROM items WHERE items.owner_id=?)");
			ps.setInt(1, objectId);
			ps.execute();
			ps.close();
			
			ps = con.prepareStatement("DELETE FROM augmentations WHERE item_id IN (SELECT object_id FROM items WHERE items.owner_id=?)");
			ps.setInt(1, objectId);
			ps.execute();
			ps.close();
			
			ps = con.prepareStatement("DELETE FROM items WHERE owner_id=?");
			ps.setInt(1, objectId);
			ps.execute();
			ps.close();
			
			ps = con.prepareStatement("DELETE FROM merchant_lease WHERE player_id=?");
			ps.setInt(1, objectId);
			ps.execute();
			ps.close();
			
			ps = con.prepareStatement("DELETE FROM characters WHERE charId=?");
			ps.setInt(1, objectId);
			ps.execute();
			ps.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("Data error on deleting char: " + e);
		}
	}
	
	public Player loadCharFromDisk(int characterSlot)
	{
		final int objectId = getObjectIdForSlot(characterSlot);
		if (objectId < 0)
		{
			return null;
		}
		
		Player player = World.getInstance().getPlayer(objectId);
		if (player != null)
		{
			// This can happen when offline system is enabled.
			// LOGGER.warning("Attempt of double login: " + player.getName() + "(" + objectId + ") " + _accountName);
			
			if (player.getClient() != null)
			{
				player.getClient().closeNow();
			}
			else
			{
				player.deleteMe();
				
				try
				{
					player.store();
				}
				catch (Exception e)
				{
					LOGGER.warning("GameClient: Count not store player. " + e);
				}
			}
		}
		
		player = Player.load(objectId);
		return player;
	}
	
	public void setCharSelection(List<CharSelectInfoPackage> characters)
	{
		_charSlotMapping.clear();
		for (CharSelectInfoPackage character : characters)
		{
			_charSlotMapping.add(character.getObjectId());
		}
	}
	
	private int getObjectIdForSlot(int charslot)
	{
		if ((charslot < 0) || (charslot >= _charSlotMapping.size()))
		{
			LOGGER.warning(this + " tried to delete Character in slot " + charslot + " but no characters exits at that slot.");
			return -1;
		}
		
		final Integer objectId = _charSlotMapping.get(charslot);
		return objectId.intValue();
	}
	
	public String getIpAddress()
	{
		final InetAddress address = _addr;
		String ip;
		if (address == null)
		{
			ip = "N/A";
		}
		else
		{
			ip = address.getHostAddress();
		}
		return ip;
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
					return "[Account: " + _accountName + " - IP: " + (address == null ? "disconnected" : address.getHostAddress()) + "]";
				}
				case ENTERING:
				case IN_GAME:
				{
					return "[Character: " + (_player == null ? "disconnected" : _player.getName() + "[" + _player.getObjectId() + "]") + " - Account: " + _accountName + " - IP: " + (address == null ? "disconnected" : address.getHostAddress()) + "]";
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
	
	protected class CleanupTask implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				// we are going to manually save the char below thus we can force the cancel
				
				final Player player = _player;
				if (player != null) // this should only happen on connection loss
				{
					if (player.isFlying())
					{
						player.removeSkill(SkillTable.getInstance().getSkill(4289, 1));
					}
					
					if (Olympiad.getInstance().isRegistered(player))
					{
						Olympiad.getInstance().unRegisterNoble(player);
					}
					
					// Decrease boxes number
					if (player._activeBoxes != -1)
					{
						player.decreaseBoxes();
					}
					
					player.setClient(null);
					
					if ((player.isInStoreMode() && Config.OFFLINE_TRADE_ENABLE) //
						|| (player.isCrafting() && Config.OFFLINE_CRAFT_ENABLE))
					{
						if (!Config.OFFLINE_MODE_IN_PEACE_ZONE || (Config.OFFLINE_MODE_IN_PEACE_ZONE && player.isInsideZone(ZoneId.PEACE)))
						{
							return;
						}
					}
					
					// prevent closing again
					player.deleteMe();
					player.store(true);
				}
				
				setPlayer(null);
				setDetached(true);
			}
			catch (Exception e1)
			{
				LOGGER.warning("Error while cleanup client. " + e1);
			}
			finally
			{
				LoginServerThread.getInstance().sendLogout(getAccountName());
			}
		}
	}
	
	protected class DisconnectTask implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				// we are going to manually save the char bellow thus we can force the cancel
				
				final Player player = _player;
				if (player != null) // this should only happen on connection loss
				{
					if (player.isFlying())
					{
						player.removeSkill(SkillTable.getInstance().getSkill(4289, 1));
					}
					
					if (Olympiad.getInstance().isRegistered(player))
					{
						Olympiad.getInstance().unRegisterNoble(player);
					}
					
					// Decrease boxes number
					if (player._activeBoxes != -1)
					{
						player.decreaseBoxes();
					}
					
					if (!player.isKicked() //
						&& !Olympiad.getInstance().isRegistered(player) //
						&& !player.isInOlympiadMode() //
						&& ((player.isInStoreMode() && Config.OFFLINE_TRADE_ENABLE) //
							|| (player.isCrafting() && Config.OFFLINE_CRAFT_ENABLE)))
					{
						if (!Config.OFFLINE_MODE_IN_PEACE_ZONE || (Config.OFFLINE_MODE_IN_PEACE_ZONE && player.isInsideZone(ZoneId.PEACE)))
						{
							player.setOnlineStatus(false);
							player.leaveParty();
							player.store();
							
							if (Config.OFFLINE_MODE_SET_INVULNERABLE)
							{
								player.setInvul(true);
							}
							if (Config.OFFLINE_SET_NAME_COLOR)
							{
								player._originalNameColorOffline = player.getAppearance().getNameColor();
								player.getAppearance().setNameColor(Config.OFFLINE_NAME_COLOR);
								player.broadcastUserInfo();
							}
							
							if (player.getOfflineStartTime() == 0)
							{
								player.setOfflineStartTime(Chronos.currentTimeMillis());
							}
							
							OfflineTraderTable.getInstance().storeOffliner(player);
							World.OFFLINE_TRADE_COUNT++;
							return;
						}
					}
					
					// notify the world about our disconnect
					player.deleteMe();
					
					// store operation
					try
					{
						player.store();
					}
					catch (Exception e2)
					{
					}
				}
				
				setPlayer(null);
				setDetached(true);
			}
			catch (Exception e1)
			{
				LOGGER.warning("error while disconnecting client " + e1);
			}
			finally
			{
				LoginServerThread.getInstance().sendLogout(getAccountName());
			}
		}
	}
	
	public FloodProtectors getFloodProtectors()
	{
		return _floodProtectors;
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
	
	public void setProtocolVersion(int version)
	{
		_protocolVersion = version;
	}
	
	public int getProtocolVersion()
	{
		return _protocolVersion;
	}
	
	public ICrypt getCrypt()
	{
		return _crypt;
	}
}
