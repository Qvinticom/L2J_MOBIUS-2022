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
package handlers.bypasshandlers;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.l2jmobius.Config;
import org.l2jmobius.commons.util.Chronos;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.handler.IBypassHandler;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.zone.ZoneId;
import org.l2jmobius.gameserver.network.serverpackets.CreatureSay;

/**
 * @author Mobius (based on Tenkai pvpzone)
 */
public class FindPvP implements IBypassHandler
{
	private static final String[] COMMANDS =
	{
		"FindPvP"
	};
	
	@Override
	public boolean useBypass(String command, Player player, Creature target)
	{
		if (!Config.ENABLE_FIND_PVP || !target.isNpc())
		{
			return false;
		}
		
		Player mostPvP = null;
		int max = -1;
		for (Player plr : World.getInstance().getPlayers())
		{
			if ((plr == null) //
				|| (plr.getPvpFlag() == 0) //
				|| (plr.getInstanceId() != 0) //
				|| plr.isGM() //
				|| plr.isInsideZone(ZoneId.PEACE) //
				|| plr.isInsideZone(ZoneId.SIEGE) //
				|| plr.isInsideZone(ZoneId.NO_SUMMON_FRIEND))
			{
				continue;
			}
			
			int count = 0;
			for (Player pl : World.getInstance().getVisibleObjects(plr, Player.class))
			{
				if ((pl.getPvpFlag() > 0) && !pl.isInsideZone(ZoneId.PEACE))
				{
					count++;
				}
			}
			
			if (count > max)
			{
				max = count;
				mostPvP = plr;
			}
		}
		
		if (mostPvP != null)
		{
			// Check if the player's clan is already outnumbering the PvP
			if (player.getClan() != null)
			{
				final Map<Integer, Integer> clanNumbers = new HashMap<>();
				int allyId = player.getAllyId();
				if (allyId == 0)
				{
					allyId = player.getClanId();
				}
				clanNumbers.put(allyId, 1);
				for (Player known : World.getInstance().getVisibleObjects(mostPvP, Player.class))
				{
					int knownAllyId = known.getAllyId();
					if (knownAllyId == 0)
					{
						knownAllyId = known.getClanId();
					}
					if (knownAllyId != 0)
					{
						if (clanNumbers.containsKey(knownAllyId))
						{
							clanNumbers.put(knownAllyId, clanNumbers.get(knownAllyId) + 1);
						}
						else
						{
							clanNumbers.put(knownAllyId, 1);
						}
					}
				}
				
				int biggestAllyId = 0;
				int biggestAmount = 2;
				for (Entry<Integer, Integer> clanNumber : clanNumbers.entrySet())
				{
					if (clanNumber.getValue() > biggestAmount)
					{
						biggestAllyId = clanNumber.getKey();
						biggestAmount = clanNumber.getValue();
					}
				}
				
				if (biggestAllyId == allyId)
				{
					player.sendPacket(new CreatureSay(null, ChatType.WHISPER, target.getName(), "Sorry, your clan/ally is outnumbering the place already so you can't move there."));
					return true;
				}
			}
			
			player.teleToLocation((mostPvP.getX() + Rnd.get(300)) - 150, (mostPvP.getY() + Rnd.get(300)) - 150, mostPvP.getZ());
			player.setSpawnProtection(true);
			if (!player.isGM())
			{
				player.setPvpFlagLasts(Chronos.currentTimeMillis() + Config.PVP_PVP_TIME);
				player.startPvPFlag();
			}
		}
		else
		{
			player.sendPacket(new CreatureSay(null, ChatType.WHISPER, target.getName(), "Sorry, I can't find anyone in flag status right now."));
		}
		return false;
	}
	
	@Override
	public String[] getBypassList()
	{
		return COMMANDS;
	}
}
