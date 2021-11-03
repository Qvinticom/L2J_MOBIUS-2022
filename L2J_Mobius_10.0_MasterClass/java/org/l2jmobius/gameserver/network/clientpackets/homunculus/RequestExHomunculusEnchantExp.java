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
import org.l2jmobius.gameserver.model.homunculus.Homunculus;
import org.l2jmobius.gameserver.model.variables.PlayerVariables;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.clientpackets.IClientIncomingPacket;
import org.l2jmobius.gameserver.network.serverpackets.homunculus.ExHomunculusEnchantEXPResult;
import org.l2jmobius.gameserver.network.serverpackets.homunculus.ExHomunculusPointInfo;
import org.l2jmobius.gameserver.network.serverpackets.homunculus.ExShowHomunculusList;

/**
 * @author Mobius
 */
public class RequestExHomunculusEnchantExp implements IClientIncomingPacket
{
	private static final int EXP_PER_POINT = 675;
	
	private int _slot;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_slot = packet.readD();
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
		if (player.getVariables().getInt(PlayerVariables.HOMUNCULUS_UPGRADE_POINTS, 0) == 0)
		{
			player.sendPacket(new ExHomunculusEnchantEXPResult(false, false));
		}
		else
		{
			int points = player.getVariables().getInt(PlayerVariables.HOMUNCULUS_UPGRADE_POINTS, 0) - 1;
			player.getVariables().set(PlayerVariables.HOMUNCULUS_UPGRADE_POINTS, points);
			final Homunculus homunculus = player.getHomunculusList().get(_slot);
			if (homunculus == null)
			{
				player.sendPacket(new ExHomunculusEnchantEXPResult(false, false));
				return;
			}
			
			homunculus.setExp(homunculus.getExp() + EXP_PER_POINT);
			player.sendPacket(new ExHomunculusPointInfo(player));
			switch (homunculus.getLevel())
			{
				case 1:
				{
					if (homunculus.getExp() >= homunculus.getTemplate().getExpToLevel2())
					{
						homunculus.setLevel(2);
						player.sendPacket(new ExHomunculusEnchantEXPResult(true, true));
					}
					else
					{
						player.sendPacket(new ExHomunculusEnchantEXPResult(true, false));
					}
					break;
				}
				case 2:
				{
					if (homunculus.getExp() >= homunculus.getTemplate().getExpToLevel3())
					{
						homunculus.setLevel(3);
						player.sendPacket(new ExHomunculusEnchantEXPResult(true, true));
					}
					else
					{
						player.sendPacket(new ExHomunculusEnchantEXPResult(true, false));
					}
					break;
				}
				case 3:
				{
					if (homunculus.getExp() >= homunculus.getTemplate().getExpToLevel4())
					{
						homunculus.setLevel(4);
						player.sendPacket(new ExHomunculusEnchantEXPResult(true, true));
					}
					else
					{
						player.sendPacket(new ExHomunculusEnchantEXPResult(true, false));
					}
					break;
				}
				case 4:
				{
					if (homunculus.getExp() >= homunculus.getTemplate().getExpToLevel5())
					{
						homunculus.setLevel(5);
						player.sendPacket(new ExHomunculusEnchantEXPResult(true, true));
					}
					else
					{
						player.sendPacket(new ExHomunculusEnchantEXPResult(true, false));
					}
					break;
				}
				case 5:
				{
					if ((homunculus.getTemplate().getExpToLevel6() - homunculus.getExp()) < EXP_PER_POINT)
					{
						homunculus.setExp(homunculus.getExp() + (homunculus.getTemplate().getExpToLevel6() - homunculus.getExp()));
						player.sendPacket(new ExHomunculusPointInfo(player));
					}
					if (homunculus.getExp() >= homunculus.getTemplate().getExpToLevel6())
					{
						return;
					}
					break;
				}
			}
			player.getHomunculusList().update(homunculus);
			player.getHomunculusList().refreshStats(true);
			player.sendPacket(new ExShowHomunculusList(player));
		}
	}
}
