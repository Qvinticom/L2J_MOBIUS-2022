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

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.model.Effect;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.skills.effects.EffectCharge;
import org.l2jmobius.gameserver.model.zone.ZoneId;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * @author Luca Baldi
 */
public class EtcStatusUpdate implements IClientOutgoingPacket
{
	private final Player _player;
	private final EffectCharge _effect;
	
	public EtcStatusUpdate(Player player)
	{
		_player = player;
		_effect = (EffectCharge) _player.getFirstEffect(Effect.EffectType.CHARGE);
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.ETC_STATUS_UPDATE.writeId(packet);
		// several icons to a separate line (0 = disabled)
		if (_effect != null)
		{
			packet.writeD(_effect.getLevel()); // 1-7 increase force, level
		}
		else
		{
			packet.writeD(0x00); // 1-7 increase force, level
		}
		packet.writeD(_player.getWeightPenalty()); // 1-4 weight penalty, level (1=50%, 2=66.6%, 3=80%, 4=100%)
		packet.writeD(_player.isInRefusalMode() || _player.isChatBanned() ? 1 : 0); // 1 = block all chat
		// writeD(0x00); // 1 = danger area
		packet.writeD(_player.isInsideZone(ZoneId.DANGER_AREA)/* || _player.isInDangerArea() */ ? 1 : 0); // 1 = danger area
		packet.writeD(Math.min(_player.getExpertisePenalty() + _player.getMasteryPenalty() + _player.getMasteryWeapPenalty(), 1)); // 1 = grade penalty
		packet.writeD(_player.getCharmOfCourage() ? 1 : 0); // 1 = charm of courage (no xp loss in siege..)
		packet.writeD(_player.getDeathPenaltyBuffLevel()); // 1-15 death penalty, level (combat ability decreased due to death)
		return true;
	}
}
