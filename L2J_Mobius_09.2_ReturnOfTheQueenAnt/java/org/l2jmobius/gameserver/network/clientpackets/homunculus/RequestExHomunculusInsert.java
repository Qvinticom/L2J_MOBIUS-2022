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
import org.l2jmobius.gameserver.network.serverpackets.homunculus.ExHomunculusHPSPVP;
import org.l2jmobius.gameserver.network.serverpackets.homunculus.ExHomunculusInsertResult;
import org.l2jmobius.gameserver.network.serverpackets.homunculus.ExShowHomunculusBirthInfo;

/**
 * @author Mobius
 */
public class RequestExHomunculusInsert implements IClientIncomingPacket
{
	private static final short HP_COST = 10000;
	private static final long SP_COST = 5000000000L;
	private static final int VP_COST = PlayerStat.MAX_VITALITY_POINTS / 4;
	
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
		int hpPoints = player.getVariables().getInt(PlayerVariables.HOMUNCULUS_HP_POINTS, 0);
		int spPoints = player.getVariables().getInt(PlayerVariables.HOMUNCULUS_SP_POINTS, 0);
		int vpPoints = player.getVariables().getInt(PlayerVariables.HOMUNCULUS_VP_POINTS, 0);
		switch (_type)
		{
			case 0:
			{
				if ((player.getCurrentHp() > HP_COST) && (hpPoints < 100))
				{
					int newHp = (int) (player.getCurrentHp()) - HP_COST;
					player.setCurrentHp(newHp, true);
					hpPoints += 1;
					player.getVariables().set(PlayerVariables.HOMUNCULUS_HP_POINTS, hpPoints);
				}
				else
				{
					return;
				}
				break;
			}
			case 1:
			{
				if ((player.getSp() >= SP_COST) && (spPoints < 10))
				{
					player.setSp(player.getSp() - SP_COST);
					spPoints += 1;
					player.getVariables().set(PlayerVariables.HOMUNCULUS_SP_POINTS, spPoints);
				}
				else
				{
					return;
				}
				break;
			}
			case 2:
			{
				if ((player.getVitalityPoints() >= VP_COST) && (vpPoints < 5))
				{
					int newVitality = player.getVitalityPoints() - VP_COST;
					player.setVitalityPoints(newVitality, true);
					vpPoints += 1;
					player.getVariables().set(PlayerVariables.HOMUNCULUS_VP_POINTS, vpPoints);
				}
				else
				{
					return;
				}
				break;
			}
		}
		player.getHomunculusList().refreshStats(true);
		
		player.sendPacket(new ExShowHomunculusBirthInfo(player));
		player.sendPacket(new ExHomunculusHPSPVP(player));
		player.sendPacket(new ExHomunculusInsertResult(_type));
	}
}