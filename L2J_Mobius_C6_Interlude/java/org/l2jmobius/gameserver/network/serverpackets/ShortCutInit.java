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
package org.l2jmobius.gameserver.network.serverpackets;

import java.util.Collection;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.model.ShortCut;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * ShortCutInit format d *(1dddd)/(2ddddd)/(3dddd)
 * @version $Revision: 1.3.2.1.2.4 $ $Date: 2005/03/27 15:29:39 $
 */
public class ShortCutInit implements IClientOutgoingPacket
{
	private Collection<ShortCut> _shortCuts;
	private Player _player;
	
	public ShortCutInit(Player player)
	{
		_player = player;
		if (_player == null)
		{
			return;
		}
		_shortCuts = _player.getAllShortCuts();
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.SHORT_CUT_INIT.writeId(packet);
		packet.writeD(_shortCuts.size());
		for (ShortCut sc : _shortCuts)
		{
			packet.writeD(sc.getType());
			packet.writeD(sc.getSlot() + (sc.getPage() * 12));
			switch (sc.getType())
			{
				case ShortCut.TYPE_ITEM: // 1
				{
					packet.writeD(sc.getId());
					packet.writeD(1);
					packet.writeD(-1);
					packet.writeD(0);
					packet.writeD(0);
					packet.writeH(0);
					packet.writeH(0);
					break;
				}
				case ShortCut.TYPE_SKILL: // 2
				{
					packet.writeD(sc.getId());
					packet.writeD(sc.getLevel());
					packet.writeC(0); // C5
					packet.writeD(1); // C6
					break;
				}
				case ShortCut.TYPE_ACTION: // 3
				{
					packet.writeD(sc.getId());
					packet.writeD(1); // C6
					break;
				}
				case ShortCut.TYPE_MACRO: // 4
				{
					packet.writeD(sc.getId());
					packet.writeD(1); // C6
					break;
				}
				case ShortCut.TYPE_RECIPE: // 5
				{
					packet.writeD(sc.getId());
					packet.writeD(1); // C6
					break;
				}
				default:
				{
					packet.writeD(sc.getId());
					packet.writeD(1); // C6
				}
			}
		}
		return true;
	}
}
