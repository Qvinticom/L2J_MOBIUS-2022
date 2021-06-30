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
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.clientpackets.IClientIncomingPacket;
import org.l2jmobius.gameserver.network.serverpackets.homunculus.ExHomunculusSummonResult;
import org.l2jmobius.gameserver.network.serverpackets.homunculus.ExShowHomunculusBirthInfo;
import org.l2jmobius.gameserver.network.serverpackets.homunculus.ExShowHomunculusList;
import org.l2jmobius.gameserver.network.serverpackets.homunculus.ExSummonHomunculusCouponResult;

/**
 * @author NasSeKa, Manax
 */
public class RequestExSummonHomunculusCouponResult implements IClientIncomingPacket
{
	private static final int HOURGLASS = 81815;
	private static final int HOURGLASS_SHINY = 81879;
	private static final int ADENA = 57;
	private static final int ADENA_COUNT = 2000000;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		// packet.readD();
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
		
		int homunculusId = 0;
		if ((homunculusId == 0) && (player.getInventory().getItemByItemId(HOURGLASS) != null) && (player.getInventory().getItemByItemId(ADENA).getCount() > ADENA_COUNT))
		{
			player.destroyItemByItemId("Homunculus Hourglass", HOURGLASS, 1, player, true);
			player.destroyItemByItemId("Adena", ADENA, 2000000, player, true);
			final int chance = Rnd.get(100);
			if (chance >= 60) // Basic Homunculus
			{
				final int random = Rnd.get(100);
				if (random >= 90)
				{
					homunculusId = 1;
				}
				else if (random >= 80)
				{
					homunculusId = 4;
				}
				else if (random >= 70)
				{
					homunculusId = 7;
				}
				else if (random >= 60)
				{
					homunculusId = 10;
				}
				else if (random >= 50)
				{
					homunculusId = 13;
				}
				else if (random >= 40)
				{
					homunculusId = 16;
				}
				else if (random >= 30)
				{
					homunculusId = 19;
				}
				else if (random >= 20)
				{
					homunculusId = 22;
				}
				else if (random >= 10)
				{
					homunculusId = 25;
				}
				else
				{
					homunculusId = 28;
				}
			}
			else if (chance >= 10) // Water Homunculus
			{
				final int random = Rnd.get(100);
				if (random >= 90)
				{
					homunculusId = 2;
				}
				else if (random >= 80)
				{
					homunculusId = 5;
				}
				else if (random >= 70)
				{
					homunculusId = 8;
				}
				else if (random >= 60)
				{
					homunculusId = 11;
				}
				else if (random >= 50)
				{
					homunculusId = 14;
				}
				else if (random >= 40)
				{
					homunculusId = 17;
				}
				else if (random >= 30)
				{
					homunculusId = 20;
				}
				else if (random >= 20)
				{
					homunculusId = 23;
				}
				else if (random >= 10)
				{
					homunculusId = 26;
				}
				else
				{
					homunculusId = 29;
				}
			}
			else // Luminous Homunculus
			{
				final int random = Rnd.get(100);
				if (random >= 90)
				{
					homunculusId = 3;
				}
				else if (random >= 80)
				{
					homunculusId = 6;
				}
				else if (random >= 70)
				{
					homunculusId = 9;
				}
				else if (random >= 60)
				{
					homunculusId = 12;
				}
				else if (random >= 50)
				{
					homunculusId = 15;
				}
				else if (random >= 40)
				{
					homunculusId = 18;
				}
				else if (random >= 30)
				{
					homunculusId = 21;
				}
				else if (random >= 20)
				{
					homunculusId = 24;
				}
				else if (random >= 10)
				{
					homunculusId = 27;
				}
				else
				{
					homunculusId = 30;
				}
			}
			
			final HomunculusTemplate template = HomunculusData.getInstance().getTemplate(homunculusId);
			if (template == null)
			{
				LOGGER.warning("Could not find Homunculus template " + homunculusId + ".");
				return;
			}
			
			final Homunculus homunculus = new Homunculus(template, player.getHomunculusList().size(), 1, 0, 0, 0, 0, 0, 0, false);
			if (player.getHomunculusList().add(homunculus))
			{
				player.sendPacket(new ExShowHomunculusBirthInfo(player));
				player.sendPacket(new ExShowHomunculusList(player));
				player.sendPacket(new ExHomunculusSummonResult());
			}
		}
		else if ((homunculusId == 1) && (player.getInventory().getItemByItemId(HOURGLASS_SHINY) != null) && (player.getInventory().getItemByItemId(ADENA).getCount() >= ADENA_COUNT))
		{
			final int chance = Rnd.get(100);
			if (chance >= 70) // Water Homunculus
			{
				player.destroyItemByItemId("Shiny Homunculus Hourglass", HOURGLASS_SHINY, 1, player, true);
				player.destroyItemByItemId("Adena", ADENA, 2000000, player, true);
				final int random = Rnd.get(100);
				if (random >= 90)
				{
					homunculusId = 2;
				}
				else if (random >= 80)
				{
					homunculusId = 5;
				}
				else if (random >= 70)
				{
					homunculusId = 8;
				}
				else if (random >= 60)
				{
					homunculusId = 11;
				}
				else if (random >= 50)
				{
					homunculusId = 14;
				}
				else if (random >= 40)
				{
					homunculusId = 17;
				}
				else if (random >= 30)
				{
					homunculusId = 20;
				}
				else if (random >= 20)
				{
					homunculusId = 23;
				}
				else if (random >= 10)
				{
					homunculusId = 26;
				}
				else
				{
					homunculusId = 29;
				}
			}
			else // Luminous Homunculus
			{
				final int random = Rnd.get(100);
				if (random >= 90)
				{
					homunculusId = 3;
				}
				else if (random >= 80)
				{
					homunculusId = 6;
				}
				else if (random >= 70)
				{
					homunculusId = 9;
				}
				else if (random >= 60)
				{
					homunculusId = 12;
				}
				else if (random >= 50)
				{
					homunculusId = 15;
				}
				else if (random >= 40)
				{
					homunculusId = 18;
				}
				else if (random >= 30)
				{
					homunculusId = 21;
				}
				else if (random >= 20)
				{
					homunculusId = 24;
				}
				else if (random >= 10)
				{
					homunculusId = 27;
				}
				else
				{
					homunculusId = 30;
				}
			}
			
			final HomunculusTemplate template = HomunculusData.getInstance().getTemplate(homunculusId);
			if (template == null)
			{
				LOGGER.warning("Could not find Homunculus template " + homunculusId + ".");
				return;
			}
			
			final Homunculus homunculus = new Homunculus(template, player.getHomunculusList().size(), 1, 0, 0, 0, 0, 0, 0, false);
			if (player.getHomunculusList().add(homunculus))
			{
				player.sendPacket(new ExShowHomunculusBirthInfo(player));
				player.sendPacket(new ExShowHomunculusList(player));
				player.sendPacket(new ExSummonHomunculusCouponResult());
			}
		}
	}
}