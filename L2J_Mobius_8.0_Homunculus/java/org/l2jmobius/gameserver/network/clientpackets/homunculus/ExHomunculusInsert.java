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
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.variables.PlayerVariables;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.clientpackets.IClientIncomingPacket;
import org.l2jmobius.gameserver.network.serverpackets.homunculus.ExHomonculusBirthInfo;
import org.l2jmobius.gameserver.network.serverpackets.homunculus.ExHomunculusInsertHpSpVp;

/**
 * @author Mobius
 */
public class ExHomunculusInsert implements IClientIncomingPacket
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
		final PlayerInstance player = client.getPlayer();
		if (player == null)
		{
			return;
		}
		
		final int time = player.getVariables().getInt(PlayerVariables.HOMUNCULUS_TIME, 0);
		if (((System.currentTimeMillis() / 1000) - time) < 86400)
		{
			player.sendMessage("Waiting time has not passed.");
			return;
		}
		
		switch (_type)
		{
			case 0: // hp
			{
				if (player.getCurrentHp() >= 10000)
				{
					player.setCurrentHp(player.getCurrentHp() - 10000);
					final int hp = player.getVariables().getInt(PlayerVariables.HOMUNCULUS_HP, 0);
					if (hp < 99)
					{
						player.getVariables().set(PlayerVariables.HOMUNCULUS_HP, hp + 1);
					}
					else
					{
						player.getVariables().set(PlayerVariables.HOMUNCULUS_HP, 100);
						
						final int status = player.getVariables().getInt(PlayerVariables.HOMUNCULUS_STATUS, 0);
						if (status == 1)
						{
							player.getVariables().set(PlayerVariables.HOMUNCULUS_STATUS, 2);
						}
					}
					player.getVariables().set(PlayerVariables.HOMUNCULUS_TIME, System.currentTimeMillis() / 1000);
				}
				else
				{
					return;
				}
				break;
			}
			case 1: // sp
			{
				if (player.getSp() >= 5000000000L)
				{
					player.setSp(player.getSp() - 5000000000L);
					final int sp = player.getVariables().getInt(PlayerVariables.HOMUNCULUS_SP, 0);
					if (sp < 9)
					{
						player.getVariables().set(PlayerVariables.HOMUNCULUS_SP, sp + 1);
					}
					else
					{
						player.getVariables().set(PlayerVariables.HOMUNCULUS_SP, 10);
						
						final int status = player.getVariables().getInt(PlayerVariables.HOMUNCULUS_STATUS, 0);
						if (status == 1)
						{
							player.getVariables().set(PlayerVariables.HOMUNCULUS_STATUS, 2);
						}
					}
					player.getVariables().set(PlayerVariables.HOMUNCULUS_TIME, System.currentTimeMillis() / 1000);
				}
				else
				{
					return;
				}
				break;
			}
			case 2: // vp
			{
				if (player.getVitalityPoints() >= 35000)
				{
					player.setVitalityPoints(player.getVitalityPoints() - 35000, true);
					final int vp = player.getVariables().getInt(PlayerVariables.HOMUNCULUS_VP, 0);
					if (vp < 4)
					{
						player.getVariables().set(PlayerVariables.HOMUNCULUS_VP, vp + 1);
					}
					else
					{
						player.getVariables().set(PlayerVariables.HOMUNCULUS_VP, 5);
						
						final int status = player.getVariables().getInt(PlayerVariables.HOMUNCULUS_STATUS, 0);
						if (status == 1)
						{
							player.getVariables().set(PlayerVariables.HOMUNCULUS_STATUS, 2);
						}
					}
					player.getVariables().set(PlayerVariables.HOMUNCULUS_TIME, System.currentTimeMillis() / 1000);
				}
				else
				{
					return;
				}
				break;
			}
		}
		
		player.sendPacket(new ExHomunculusInsertHpSpVp(player));
		player.sendPacket(new ExHomonculusBirthInfo(player));
	}
}
