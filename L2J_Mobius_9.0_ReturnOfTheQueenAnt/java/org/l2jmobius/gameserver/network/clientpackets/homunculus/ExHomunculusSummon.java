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
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.variables.PlayerVariables;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.clientpackets.IClientIncomingPacket;
import org.l2jmobius.gameserver.network.serverpackets.homunculus.ExHomonculusList;
import org.l2jmobius.gameserver.network.serverpackets.homunculus.ExHomonculusSummonResult;

/**
 * @author Mobius
 */
public class ExHomunculusSummon implements IClientIncomingPacket
{
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
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
		
		final int homunculus = player.getVariables().getInt(PlayerVariables.HOMUNCULUS_ID, 0);
		if (homunculus == 0)
		{
			final int status = player.getVariables().getInt(PlayerVariables.HOMUNCULUS_STATUS, 0);
			final int hp = player.getVariables().getInt(PlayerVariables.HOMUNCULUS_HP, 0);
			final int sp = player.getVariables().getInt(PlayerVariables.HOMUNCULUS_SP, 0);
			final int vp = player.getVariables().getInt(PlayerVariables.HOMUNCULUS_VP, 0);
			if ((status == 2) && ((hp == 100) || (sp == 10) || (vp == 5)))
			{
				player.getVariables().set(PlayerVariables.HOMUNCULUS_ID, 1);
				
				int quality = 2;
				if (Rnd.get(100) < 50)
				{
					quality = 0;
				}
				else if (Rnd.get(100) < 30)
				{
					quality = 1;
				}
				player.getVariables().set(PlayerVariables.HOMUNCULUS_QUALITY, quality);
				
				player.calculateHomunculusBonuses();
				player.getStat().recalculateStats(true);
			}
		}
		
		client.sendPacket(new ExHomonculusSummonResult(player));
		client.sendPacket(new ExHomonculusList(player));
	}
}
