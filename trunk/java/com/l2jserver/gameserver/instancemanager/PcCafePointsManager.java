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

import com.l2jserver.Config;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.zone.ZoneId;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.serverpackets.ExPCCafePointInfo;
import com.l2jserver.gameserver.network.serverpackets.SystemMessage;
import com.l2jserver.util.Rnd;

public class PcCafePointsManager
{
	private static PcCafePointsManager _instance;
	
	public static PcCafePointsManager getInstance()
	{
		if (_instance == null)
		{
			_instance = new PcCafePointsManager();
		}
		return _instance;
	}
	
	public PcCafePointsManager()
	{
	}
	
	public void givePcCafePoint(final L2PcInstance player, final long givedexp)
	{
		if (!Config.PC_BANG_ENABLED)
		{
			return;
		}
		
		if (player.isInsideZone(ZoneId.PEACE) || player.isInsideZone(ZoneId.PVP) || player.isInsideZone(ZoneId.SIEGE) || (player.isOnlineInt() == 0) || player.isJailed())
		{
			return;
		}
		
		if (player.getPcBangPoints() >= Config.MAX_PC_BANG_POINTS)
		{
			final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_EARNED_THE_MAXIMUM_NUMBER_OF_PC_POINTS);
			player.sendPacket(sm);
			return;
		}
		int _points = (int) (givedexp * 0.0001 * Config.PC_BANG_POINT_RATE);
		
		// TODO: Mage class balance?
		// if ((player.getActiveClass() == ClassId.ARCHMAGE.getId()) || (player.getActiveClass() == ClassId.SOULTAKER.getId()) || (player.getActiveClass() == ClassId.STORM_SCREAMER.getId()) || (player.getActiveClass() == ClassId.MYSTIC_MUSE.getId()))
		// {
		// _points /= 2;
		// }
		
		if (Config.RANDOM_PC_BANG_POINT)
		{
			_points = Rnd.get(_points / 2, _points);
		}
		
		@SuppressWarnings("unused")
		boolean doublepoint = false;
		SystemMessage sm = null;
		if (_points > 0)
		{
			if (Config.ENABLE_DOUBLE_PC_BANG_POINTS && (Rnd.get(100) < Config.DOUBLE_PC_BANG_POINTS_CHANCE))
			{
				_points *= 2;
				sm = SystemMessage.getSystemMessage(SystemMessageId.DOUBLE_POINTS_YOU_EARNED_S1_PC_POINT_S);
				doublepoint = true;
			}
			else
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_EARNED_S1_PC_POINT_S2);
			}
			if ((player.getPcBangPoints() + _points) > Config.MAX_PC_BANG_POINTS)
			{
				_points = Config.MAX_PC_BANG_POINTS - player.getPcBangPoints();
			}
			sm.addLong(_points);
			player.sendPacket(sm);
			player.setPcBangPoints(player.getPcBangPoints() + _points);
			player.sendPacket(new ExPCCafePointInfo(player.getPcBangPoints(), _points, 1));
		}
	}
}