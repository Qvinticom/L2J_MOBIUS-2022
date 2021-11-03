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
package org.l2jmobius.gameserver.enums;

import java.util.function.Function;

import org.l2jmobius.gameserver.data.xml.ClanRewardData;
import org.l2jmobius.gameserver.model.clan.Clan;
import org.l2jmobius.gameserver.model.clan.ClanRewardBonus;

/**
 * @author UnAfraid
 */
public enum ClanRewardType
{
	MEMBERS_ONLINE(0, Clan::getPreviousMaxOnlinePlayers),
	HUNTING_MONSTERS(1, Clan::getPreviousHuntingPoints);
	
	final int _clientId;
	final int _mask;
	final Function<Clan, Integer> _pointsFunction;
	
	ClanRewardType(int clientId, Function<Clan, Integer> pointsFunction)
	{
		_clientId = clientId;
		_mask = 1 << clientId;
		_pointsFunction = pointsFunction;
	}
	
	public int getClientId()
	{
		return _clientId;
	}
	
	public int getMask()
	{
		return _mask;
	}
	
	public ClanRewardBonus getAvailableBonus(Clan clan)
	{
		ClanRewardBonus availableBonus = null;
		for (ClanRewardBonus bonus : ClanRewardData.getInstance().getClanRewardBonuses(this))
		{
			if (bonus.getRequiredAmount() <= _pointsFunction.apply(clan))
			{
				if ((availableBonus == null) || (availableBonus.getLevel() < bonus.getLevel()))
				{
					availableBonus = bonus;
				}
			}
		}
		return availableBonus;
	}
	
	public static int getDefaultMask()
	{
		int mask = 0;
		for (ClanRewardType type : values())
		{
			mask |= type.getMask();
		}
		return mask;
	}
}
