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
package org.l2jmobius.gameserver.network.clientpackets;

import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.serverpackets.ExAutoPlaySettingSend;

/**
 * @author Mobius
 */
public class ExAutoPlaySetting implements IClientIncomingPacket
{
	private int _options;
	private boolean _active;
	private boolean _pickUp;
	private int _nextTargetMode;
	private boolean _longRange;
	private int _potionPercent;
	private boolean _respectfulHunting;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_options = packet.readH();
		_active = packet.readC() == 1;
		_pickUp = packet.readC() == 1;
		_nextTargetMode = packet.readH();
		_longRange = packet.readC() == 0;
		_potionPercent = packet.readD();
		_respectfulHunting = packet.readC() == 1;
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
		
		player.sendPacket(new ExAutoPlaySettingSend(_options, _active, _pickUp, _nextTargetMode, _longRange, _potionPercent, _respectfulHunting));
		
		if (_active)
		{
			player.startAutoPlayTask(_longRange, _respectfulHunting);
		}
		else
		{
			player.stopAutoPlayTask();
		}
	}
}
