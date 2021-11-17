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
package org.l2jmobius.gameserver.network.clientpackets.homunculus;

import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.stat.PlayerStat;
import org.l2jmobius.gameserver.model.variables.PlayerVariables;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.clientpackets.IClientIncomingPacket;
import org.l2jmobius.gameserver.network.serverpackets.homunculus.ExHomunculusGetEnchantPointResult;
import org.l2jmobius.gameserver.network.serverpackets.homunculus.ExHomunculusHPSPVP;
import org.l2jmobius.gameserver.network.serverpackets.homunculus.ExHomunculusPointInfo;

/**
 * @author Mobius
 */
public class RequestExHomunculusGetEnchantPoint implements IClientIncomingPacket
{
	private int _type;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_type = packet.readD();
		return true;
	}
	
	@Override
	public void run(GameClient client)
	{
		final Player player = client.getPlayer();
		if (player == null)
		{
			return;
		}
		
		if (_type == 0) // mobs
		{
			int killedMobs = player.getVariables().getInt(PlayerVariables.HOMUNCULUS_KILLED_MOBS, 0);
			if (killedMobs < 500)
			{
				return;
			}
			int usedKillConverts = player.getVariables().getInt(PlayerVariables.HOMUNCULUS_USED_KILL_CONVERT, 0);
			if (usedKillConverts >= 5)
			{
				return;
			}
			
			int upgradePoints = player.getVariables().getInt(PlayerVariables.HOMUNCULUS_UPGRADE_POINTS, 0) + 1;
			player.getVariables().set(PlayerVariables.HOMUNCULUS_UPGRADE_POINTS, upgradePoints);
			player.getVariables().set(PlayerVariables.HOMUNCULUS_KILLED_MOBS, 0);
			player.getVariables().set(PlayerVariables.HOMUNCULUS_USED_KILL_CONVERT, usedKillConverts + 1);
		}
		else if (_type == 1) // vitality
		{
			int usedVpPoints = player.getVariables().getInt(PlayerVariables.HOMUNCULUS_USED_VP_POINTS, 0);
			if (usedVpPoints < 2)
			{
				return;
			}
			int usedVpConverts = player.getVariables().getInt(PlayerVariables.HOMUNCULUS_USED_VP_CONVERT, 0);
			if (usedVpConverts >= 5)
			{
				return;
			}
			
			int upgradePoints = player.getVariables().getInt(PlayerVariables.HOMUNCULUS_UPGRADE_POINTS, 0) + 1;
			player.getVariables().set(PlayerVariables.HOMUNCULUS_UPGRADE_POINTS, upgradePoints);
			player.getVariables().set(PlayerVariables.HOMUNCULUS_USED_VP_POINTS, 0);
			player.getVariables().set(PlayerVariables.HOMUNCULUS_USED_VP_CONVERT, usedVpConverts + 1);
		}
		else if (_type == 2) // vitality consume
		{
			int usedVpPoints = player.getVariables().getInt(PlayerVariables.HOMUNCULUS_USED_VP_POINTS, 0);
			if (usedVpPoints >= 2)
			{
				return;
			}
			
			if (player.getVitalityPoints() >= (PlayerStat.MAX_VITALITY_POINTS / 4))
			{
				player.setVitalityPoints(player.getVitalityPoints() - (PlayerStat.MAX_VITALITY_POINTS / 4), false);
				player.getVariables().set(PlayerVariables.HOMUNCULUS_USED_VP_POINTS, usedVpPoints + 1);
			}
		}
		
		if (_type == 2)
		{
			player.sendPacket(new ExHomunculusHPSPVP(player));
		}
		player.sendPacket(new ExHomunculusPointInfo(player));
		player.sendPacket(new ExHomunculusGetEnchantPointResult(_type));
	}
}
