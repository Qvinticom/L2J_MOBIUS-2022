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
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.variables.PlayerVariables;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.clientpackets.IClientIncomingPacket;
import org.l2jmobius.gameserver.network.serverpackets.homunculus.ExHomunculusInitPointResult;
import org.l2jmobius.gameserver.network.serverpackets.homunculus.ExHomunculusPointInfo;

/**
 * @author Mobius
 */
public class RequestExHomunculusInitPoint implements IClientIncomingPacket
{
	private static final int POWERFUL_FISH = 47552;
	private static final int FISH_COUNT = 5;
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
		
		if (_type == 0)
		{
			final int usedResetKills = player.getVariables().getInt(PlayerVariables.HOMUNCULUS_USED_RESET_KILLS, 0);
			final int usedKillConvert = player.getVariables().getInt(PlayerVariables.HOMUNCULUS_USED_KILL_CONVERT, 0);
			final Item fish = player.getInventory().getItemByItemId(POWERFUL_FISH);
			if (((fish == null) || (fish.getCount() < FISH_COUNT)) || ((usedResetKills <= 3) && (usedKillConvert == 0)))
			{
				player.sendPacket(new ExHomunculusInitPointResult(false, _type));
			}
			else if ((usedResetKills <= 3) && (usedKillConvert == 5))
			{
				player.destroyItemByItemId("Homunculus Points", POWERFUL_FISH, FISH_COUNT, player, true);
				player.getVariables().set(PlayerVariables.HOMUNCULUS_USED_KILL_CONVERT, 0);
				player.getVariables().set(PlayerVariables.HOMUNCULUS_USED_RESET_KILLS, usedResetKills + 1);
				player.sendPacket(new ExHomunculusInitPointResult(true, _type));
				player.sendPacket(new ExHomunculusPointInfo(player));
			}
		}
		else
		{
			final int usedResetVp = player.getVariables().getInt(PlayerVariables.HOMUNCULUS_USED_RESET_VP, 0);
			final int usedVpConvert = player.getVariables().getInt(PlayerVariables.HOMUNCULUS_USED_VP_CONVERT, 0);
			final Item fish = player.getInventory().getItemByItemId(POWERFUL_FISH);
			if (((fish == null) || (fish.getCount() < FISH_COUNT)) || ((usedResetVp <= 3) && (usedVpConvert == 0)))
			{
				player.sendPacket(new ExHomunculusInitPointResult(false, _type));
			}
			else if ((usedResetVp <= 3) && (usedVpConvert == 5))
			{
				player.destroyItemByItemId("Homunculus Points", POWERFUL_FISH, FISH_COUNT, player, true);
				player.getVariables().set(PlayerVariables.HOMUNCULUS_USED_VP_CONVERT, 0);
				player.getVariables().set(PlayerVariables.HOMUNCULUS_USED_RESET_VP, usedResetVp + 1);
				player.sendPacket(new ExHomunculusInitPointResult(true, _type));
				player.sendPacket(new ExHomunculusPointInfo(player));
			}
		}
	}
}
