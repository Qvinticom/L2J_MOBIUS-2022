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
import org.l2jmobius.commons.util.Chronos;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.data.xml.HomunculusData;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.homunculus.Homunculus;
import org.l2jmobius.gameserver.model.homunculus.HomunculusTemplate;
import org.l2jmobius.gameserver.model.variables.PlayerVariables;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.PacketLogger;
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
		final Player player = client.getPlayer();
		if (player == null)
		{
			return;
		}
		
		_hpPoints = player.getVariables().getInt(PlayerVariables.HOMUNCULUS_HP_POINTS, 0);
		_spPoints = player.getVariables().getInt(PlayerVariables.HOMUNCULUS_SP_POINTS, 0);
		_vpPoints = player.getVariables().getInt(PlayerVariables.HOMUNCULUS_VP_POINTS, 0);
		_homunculusCreateTime = (int) (player.getVariables().getLong(PlayerVariables.HOMUNCULUS_CREATION_TIME, 0) / 1000);
		
		if ((_homunculusCreateTime > 0) && ((Chronos.currentTimeMillis() / 1000) >= _homunculusCreateTime) && (_hpPoints == 100) && (_spPoints == 10) && (_vpPoints == 5))
		{
			int chance;
			int random;
			int homunculusId = 0;
			while (homunculusId == 0)
			{
				chance = Rnd.get(100);
				random = Rnd.get(100);
				
				// Basic Homunculus
				if (chance >= 60)
				{
					if ((random >= 90) && !player.getHomunculusList().hasHomunculus(1))
					{
						homunculusId = 1;
					}
					else if ((random >= 80) && !player.getHomunculusList().hasHomunculus(4))
					{
						homunculusId = 4;
					}
					else if ((random >= 70) && !player.getHomunculusList().hasHomunculus(7))
					{
						homunculusId = 7;
					}
					else if ((random >= 60) && !player.getHomunculusList().hasHomunculus(10))
					{
						homunculusId = 10;
					}
					else if ((random >= 50) && !player.getHomunculusList().hasHomunculus(13))
					{
						homunculusId = 13;
					}
					else if ((random >= 40) && !player.getHomunculusList().hasHomunculus(16))
					{
						homunculusId = 16;
					}
					else if ((random >= 30) && !player.getHomunculusList().hasHomunculus(19))
					{
						homunculusId = 19;
					}
					else if ((random >= 20) && !player.getHomunculusList().hasHomunculus(22))
					{
						homunculusId = 22;
					}
					else if ((random >= 10) && !player.getHomunculusList().hasHomunculus(25))
					{
						homunculusId = 25;
					}
					else if (!player.getHomunculusList().hasHomunculus(28))
					{
						homunculusId = 28;
					}
				}
				
				// Water Homunculus
				if ((homunculusId == 0) && (chance >= 10))
				{
					if ((random >= 90) && !player.getHomunculusList().hasHomunculus(2))
					{
						homunculusId = 2;
					}
					else if ((random >= 80) && !player.getHomunculusList().hasHomunculus(5))
					{
						homunculusId = 5;
					}
					else if ((random >= 70) && !player.getHomunculusList().hasHomunculus(8))
					{
						homunculusId = 8;
					}
					else if ((random >= 60) && !player.getHomunculusList().hasHomunculus(11))
					{
						homunculusId = 11;
					}
					else if ((random >= 50) && !player.getHomunculusList().hasHomunculus(14))
					{
						homunculusId = 14;
					}
					else if ((random >= 40) && !player.getHomunculusList().hasHomunculus(17))
					{
						homunculusId = 17;
					}
					else if ((random >= 30) && !player.getHomunculusList().hasHomunculus(20))
					{
						homunculusId = 20;
					}
					else if ((random >= 20) && !player.getHomunculusList().hasHomunculus(23))
					{
						homunculusId = 23;
					}
					else if ((random >= 10) && !player.getHomunculusList().hasHomunculus(26))
					{
						homunculusId = 26;
					}
					else if (!player.getHomunculusList().hasHomunculus(29))
					{
						homunculusId = 29;
					}
				}
				
				// Luminous Homunculus
				if (homunculusId == 0)
				{
					if ((random >= 90) && !player.getHomunculusList().hasHomunculus(3))
					{
						homunculusId = 3;
					}
					else if ((random >= 80) && !player.getHomunculusList().hasHomunculus(6))
					{
						homunculusId = 6;
					}
					else if ((random >= 70) && !player.getHomunculusList().hasHomunculus(9))
					{
						homunculusId = 9;
					}
					else if ((random >= 60) && !player.getHomunculusList().hasHomunculus(12))
					{
						homunculusId = 12;
					}
					else if ((random >= 50) && !player.getHomunculusList().hasHomunculus(15))
					{
						homunculusId = 15;
					}
					else if ((random >= 40) && !player.getHomunculusList().hasHomunculus(18))
					{
						homunculusId = 18;
					}
					else if ((random >= 30) && !player.getHomunculusList().hasHomunculus(21))
					{
						homunculusId = 21;
					}
					else if ((random >= 20) && !player.getHomunculusList().hasHomunculus(24))
					{
						homunculusId = 24;
					}
					else if ((random >= 10) && !player.getHomunculusList().hasHomunculus(27))
					{
						homunculusId = 27;
					}
					else if (!player.getHomunculusList().hasHomunculus(30))
					{
						homunculusId = 30;
					}
				}
			}
			
			final HomunculusTemplate template = HomunculusData.getInstance().getTemplate(homunculusId);
			if (template == null)
			{
				PacketLogger.warning("Counld not find Homunculus template " + homunculusId + ".");
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
