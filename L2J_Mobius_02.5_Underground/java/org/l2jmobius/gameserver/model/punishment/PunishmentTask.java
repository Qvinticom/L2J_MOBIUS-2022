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
package org.l2jmobius.gameserver.model.punishment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.commons.util.Chronos;
import org.l2jmobius.gameserver.handler.IPunishmentHandler;
import org.l2jmobius.gameserver.handler.PunishmentHandler;
import org.l2jmobius.gameserver.instancemanager.PunishmentManager;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.skill.AbnormalVisualEffect;

/**
 * @author UnAfraid
 */
public class PunishmentTask implements Runnable
{
	protected static final Logger LOGGER = Logger.getLogger(PunishmentTask.class.getName());
	
	private static final String INSERT_QUERY = "INSERT INTO punishments (`key`, `affect`, `type`, `expiration`, `reason`, `punishedBy`) VALUES (?, ?, ?, ?, ?, ?)";
	private static final String UPDATE_QUERY = "UPDATE punishments SET expiration = ? WHERE id = ?";
	
	private int _id;
	private final String _key;
	private final PunishmentAffect _affect;
	private final PunishmentType _type;
	private final long _expirationTime;
	private final String _reason;
	private final String _punishedBy;
	private boolean _isStored;
	private ScheduledFuture<?> _task = null;
	
	public PunishmentTask(Object key, PunishmentAffect affect, PunishmentType type, long expirationTime, String reason, String punishedBy)
	{
		this(0, key, affect, type, expirationTime, reason, punishedBy, false);
	}
	
	public PunishmentTask(int id, Object key, PunishmentAffect affect, PunishmentType type, long expirationTime, String reason, String punishedBy, boolean isStored)
	{
		_id = id;
		_key = String.valueOf(key);
		_affect = affect;
		_type = type;
		_expirationTime = expirationTime;
		_reason = reason;
		_punishedBy = punishedBy;
		_isStored = isStored;
		startPunishment();
	}
	
	/**
	 * @return affection value charId, account, ip, etc..
	 */
	public Object getKey()
	{
		return _key;
	}
	
	/**
	 * @return {@link PunishmentAffect} affection type, account, character, ip, etc..
	 */
	public PunishmentAffect getAffect()
	{
		return _affect;
	}
	
	/**
	 * @return {@link PunishmentType} type of current punishment.
	 */
	public PunishmentType getType()
	{
		return _type;
	}
	
	/**
	 * @return milliseconds to the end of the current punishment, -1 for infinity.
	 */
	public long getExpirationTime()
	{
		return _expirationTime;
	}
	
	/**
	 * @return the reason for this punishment.
	 */
	public String getReason()
	{
		return _reason;
	}
	
	/**
	 * @return name of the punishment issuer.
	 */
	public String getPunishedBy()
	{
		return _punishedBy;
	}
	
	/**
	 * @return {@code true} if current punishment task is stored in database, {@code false} otherwise.
	 */
	public boolean isStored()
	{
		return _isStored;
	}
	
	/**
	 * @return {@code true} if current punishment task has expired, {@code false} otherwise.
	 */
	public boolean isExpired()
	{
		return (_expirationTime > 0) && (Chronos.currentTimeMillis() > _expirationTime);
	}
	
	/**
	 * Activates the punishment task.
	 */
	private void startPunishment()
	{
		if (isExpired())
		{
			return;
		}
		
		onStart();
		if (_expirationTime > 0) // Has expiration?
		{
			_task = ThreadPool.schedule(this, (_expirationTime - Chronos.currentTimeMillis()));
		}
	}
	
	/**
	 * Stops the punishment task.
	 */
	public void stopPunishment()
	{
		abortTask();
		onEnd();
	}
	
	/**
	 * Aborts the scheduled task.
	 */
	private void abortTask()
	{
		if (_task != null)
		{
			if (!_task.isCancelled() && !_task.isDone())
			{
				_task.cancel(false);
			}
			_task = null;
		}
	}
	
	/**
	 * Store and activate punishment upon start.
	 */
	private void onStart()
	{
		if (!_isStored)
		{
			try (Connection con = DatabaseFactory.getConnection();
				PreparedStatement st = con.prepareStatement(INSERT_QUERY, Statement.RETURN_GENERATED_KEYS))
			{
				st.setString(1, _key);
				st.setString(2, _affect.name());
				st.setString(3, _type.name());
				st.setLong(4, _expirationTime);
				st.setString(5, _reason);
				st.setString(6, _punishedBy);
				st.execute();
				try (ResultSet rset = st.getGeneratedKeys())
				{
					if (rset.next())
					{
						_id = rset.getInt(1);
					}
				}
				_isStored = true;
			}
			catch (SQLException e)
			{
				LOGGER.log(Level.WARNING, getClass().getSimpleName() + ": Couldn't store punishment task for: " + _affect + " " + _key, e);
			}
		}
		
		final IPunishmentHandler handler = PunishmentHandler.getInstance().getHandler(_type);
		if (handler != null)
		{
			handler.onStart(this);
		}
	}
	
	/**
	 * Remove and deactivate punishment when it ends.
	 */
	private void onEnd()
	{
		if (_isStored)
		{
			try (Connection con = DatabaseFactory.getConnection();
				PreparedStatement st = con.prepareStatement(UPDATE_QUERY))
			{
				st.setLong(1, Chronos.currentTimeMillis());
				st.setLong(2, _id);
				st.execute();
			}
			catch (SQLException e)
			{
				LOGGER.log(Level.WARNING, getClass().getSimpleName() + ": Couldn't update punishment task for: " + _affect + " " + _key + " id: " + _id, e);
			}
		}
		
		if ((_type == PunishmentType.CHAT_BAN) && (_affect == PunishmentAffect.CHARACTER))
		{
			final Player player = World.getInstance().getPlayer(Integer.parseInt(_key));
			if (player != null)
			{
				player.getEffectList().stopAbnormalVisualEffect(AbnormalVisualEffect.NO_CHAT);
			}
		}
		
		final IPunishmentHandler handler = PunishmentHandler.getInstance().getHandler(_type);
		if (handler != null)
		{
			handler.onEnd(this);
		}
	}
	
	/**
	 * Runs when punishment task ends in order to stop and remove it.
	 */
	@Override
	public void run()
	{
		PunishmentManager.getInstance().stopPunishment(_key, _affect, _type);
	}
}
