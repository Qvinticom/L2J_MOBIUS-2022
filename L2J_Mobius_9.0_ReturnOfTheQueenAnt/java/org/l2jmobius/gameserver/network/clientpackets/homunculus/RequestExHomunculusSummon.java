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
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.data.xml.HomunculusData;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.homunculus.Homunculus;
import org.l2jmobius.gameserver.model.homunculus.HomunculusTemplate;
import org.l2jmobius.gameserver.model.variables.PlayerVariables;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.clientpackets.IClientIncomingPacket;
import org.l2jmobius.gameserver.network.serverpackets.homunculus.ExHomunculusSummonResult;
import org.l2jmobius.gameserver.network.serverpackets.homunculus.ExShowHomunculusBirthInfo;
import org.l2jmobius.gameserver.network.serverpackets.homunculus.ExShowHomunculusList;

/**
 * @author Mobius
 */
public class RequestExHomunculusSummon implements IClientIncomingPacket
{
	private static int _hpPoints;
	private static int _spPoints;
	private static int _vpPoints;
	private static int _homunculusCreateTime;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		// packet.readC();
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
		
		_hpPoints = player.getVariables().getInt(PlayerVariables.HOMUNCULUS_HP_POINTS, 0);
		_spPoints = player.getVariables().getInt(PlayerVariables.HOMUNCULUS_SP_POINTS, 0);
		_vpPoints = player.getVariables().getInt(PlayerVariables.HOMUNCULUS_VP_POINTS, 0);
		_homunculusCreateTime = (int) (player.getVariables().getLong(PlayerVariables.HOMUNCULUS_CREATION_TIME, 0) / 1000);
		
		if (_homunculusCreateTime > 0)
		{
			if (((System.currentTimeMillis() / 1000) >= _homunculusCreateTime) && (_hpPoints == 100) && (_spPoints == 10) && (_vpPoints == 5))
			{
				int homunculusId = 0;
				final int chance = Rnd.get(100);
				if (chance >= 60) // Basic Homunculus
				{
					int chance2 = Rnd.get(100);
					if (chance2 >= 80)
					{
						homunculusId = 1;
					}
					else if (chance2 >= 60)
					{
						homunculusId = 4;
					}
					else if (chance2 >= 40)
					{
						homunculusId = 7;
					}
					else if (chance2 >= 20)
					{
						homunculusId = 10;
					}
					else
					{
						homunculusId = 13;
					}
				}
				else if (chance >= 10) // Water Homunculus
				{
					int chance2 = Rnd.get(100);
					if (chance2 >= 80)
					{
						homunculusId = 2;
					}
					else if (chance2 >= 60)
					{
						homunculusId = 5;
					}
					else if (chance2 >= 40)
					{
						homunculusId = 8;
					}
					else if (chance2 >= 20)
					{
						homunculusId = 11;
					}
					else
					{
						homunculusId = 14;
					}
				}
				else // Luminous Homunculus
				{
					int chance2 = Rnd.get(100);
					if (chance2 >= 80)
					{
						homunculusId = 3;
					}
					else if (chance2 >= 60)
					{
						homunculusId = 6;
					}
					else if (chance2 >= 40)
					{
						homunculusId = 9;
					}
					else if (chance2 >= 20)
					{
						homunculusId = 12;
					}
					else
					{
						homunculusId = 15;
					}
				}
				
				final HomunculusTemplate template = HomunculusData.getInstance().getTemplate(homunculusId);
				if (template == null)
				{
					LOGGER.warning("Counld not find Homunculus template " + homunculusId + ".");
					return;
				}
				
				final Homunculus homunculus = new Homunculus(template, player.getHomunculusList().size(), 1, 0, 0, 0, 0, 0, 0, false);
				if (player.getHomunculusList().add(homunculus))
				{
					player.getVariables().set(PlayerVariables.HOMUNCULUS_CREATION_TIME, 0);
					player.getVariables().set(PlayerVariables.HOMUNCULUS_HP_POINTS, 0);
					player.getVariables().set(PlayerVariables.HOMUNCULUS_SP_POINTS, 0);
					player.getVariables().set(PlayerVariables.HOMUNCULUS_VP_POINTS, 0);
					player.sendPacket(new ExShowHomunculusBirthInfo(player));
					player.sendPacket(new ExShowHomunculusList(player));
					player.sendPacket(new ExHomunculusSummonResult());
				}
			}
		}
	}
}
