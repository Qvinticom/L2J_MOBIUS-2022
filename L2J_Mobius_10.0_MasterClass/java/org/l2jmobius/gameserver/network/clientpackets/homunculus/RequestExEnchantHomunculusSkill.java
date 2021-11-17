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
import org.l2jmobius.gameserver.model.variables.PlayerVariables;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.clientpackets.IClientIncomingPacket;
import org.l2jmobius.gameserver.network.serverpackets.homunculus.ExEnchantHomunculusSkillResult;
import org.l2jmobius.gameserver.network.serverpackets.homunculus.ExHomunculusHPSPVP;
import org.l2jmobius.gameserver.network.serverpackets.homunculus.ExHomunculusPointInfo;
import org.l2jmobius.gameserver.network.serverpackets.homunculus.ExShowHomunculusList;

/**
 * @author Mobius
 */
public class RequestExEnchantHomunculusSkill implements IClientIncomingPacket
{
	private static final int SP_COST = 1000000000;
	
	private int _slot;
	private int _skillNumber;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		packet.readD();
		_slot = packet.readD();
		_skillNumber = packet.readD();
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
		
		if (player.getSp() < SP_COST)
		{
			return;
		}
		
		int points = player.getVariables().getInt(PlayerVariables.HOMUNCULUS_UPGRADE_POINTS, 0);
		if (points < 1)
		{
			player.sendMessage("Not enough upgrade points.");
			return;
		}
		
		player.getVariables().set(PlayerVariables.HOMUNCULUS_UPGRADE_POINTS, points - 1);
		player.setSp(player.getSp() - SP_COST);
		player.sendPacket(new ExEnchantHomunculusSkillResult(player, _slot, _skillNumber));
		player.sendPacket(new ExHomunculusHPSPVP(player));
		player.sendPacket(new ExShowHomunculusList(player));
		player.sendPacket(new ExHomunculusPointInfo(player));
	}
}
