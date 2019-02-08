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
package com.l2jmobius.commons.crypt;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ScheduledFuture;

import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.L2GameClient;
import com.l2jmobius.gameserver.network.serverpackets.GameGuardQuery;

/**
 * The main "engine" of protection ...
 * @author Nick
 */
public class nProtect
{
	public enum RestrictionType
	{
		RESTRICT_ENTER,
		RESTRICT_EVENT,
		RESTRICT_OLYMPIAD,
		RESTRICT_SIEGE
	}
	
	public class nProtectAccessor
	{
		public nProtectAccessor()
		{
		}
		
		public void setCheckGameGuardQuery(Method m)
		{
			_checkGameGuardQuery = m;
		}
		
		public void setStartTask(Method m)
		{
			_startTask = m;
		}
		
		public void setCheckRestriction(Method m)
		{
			_checkRestriction = m;
		}
		
		public void setSendRequest(Method m)
		{
			_sendRequest = m;
		}
		
		public void setCloseSession(Method m)
		{
			_closeSession = m;
		}
		
		public void setSendGGQuery(Method m)
		{
			_sendGGQuery = m;
		}
	}
	
	protected Method _checkGameGuardQuery = null;
	protected Method _startTask = null;
	protected Method _checkRestriction = null;
	protected Method _sendRequest = null;
	protected Method _closeSession = null;
	protected Method _sendGGQuery = null;
	private static nProtect _instance = null;
	
	private static boolean enabled = false;
	
	public static nProtect getInstance()
	{
		if (_instance == null)
		{
			_instance = new nProtect();
		}
		return _instance;
	}
	
	private nProtect()
	{
		Class<?> clazz = null;
		try
		{
			clazz = Class.forName("com.l2jmobius.protection.main");
			
			if (clazz != null)
			{
				final Method m = clazz.getMethod("init", nProtectAccessor.class);
				if (m != null)
				{
					m.invoke(null, new nProtectAccessor());
					enabled = true;
				}
			}
		}
		catch (ClassNotFoundException e)
		{
			// LOGGER.warning("nProtect System will be not loaded due to ClassNotFoundException of 'com.l2jmobius.protection.main' class");
		}
		catch (SecurityException | InvocationTargetException | IllegalAccessException | IllegalArgumentException | NoSuchMethodException e)
		{
			e.printStackTrace();
		}
	}
	
	public void sendGameGuardQuery(GameGuardQuery pkt)
	{
		try
		{
			if (_sendGGQuery != null)
			{
				_sendGGQuery.invoke(pkt);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public boolean checkGameGuardRepy(L2GameClient cl, int[] reply)
	{
		try
		{
			if (_checkGameGuardQuery != null)
			{
				return (Boolean) _checkGameGuardQuery.invoke(null, cl, reply);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return true;
	}
	
	public ScheduledFuture<?> startTask(L2GameClient client)
	{
		try
		{
			if (_startTask != null)
			{
				return (ScheduledFuture<?>) _startTask.invoke(null, client);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	public void sendRequest(L2GameClient cl)
	{
		if (_sendRequest != null)
		{
			try
			{
				_sendRequest.invoke(null, cl);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public void closeSession(L2GameClient cl)
	{
		if (_closeSession != null)
		{
			try
			{
				_closeSession.invoke(null, cl);
			}
			catch (Exception e)
			{
			}
		}
	}
	
	public boolean checkRestriction(L2PcInstance player, RestrictionType type, Object... params)
	{
		try
		{
			if (_checkRestriction != null)
			{
				return (Boolean) _checkRestriction.invoke(null, player, type, params);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return true;
	}
	
	/**
	 * @return the enabled
	 */
	public static boolean isEnabled()
	{
		return enabled;
	}
}
