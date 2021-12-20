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
import org.l2jmobius.gameserver.instancemanager.CastleManager;
import org.l2jmobius.gameserver.instancemanager.FortManager;
import org.l2jmobius.gameserver.model.SiegeClan;
import org.l2jmobius.gameserver.model.actor.Attackable;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.clan.Clan;
import org.l2jmobius.gameserver.model.siege.Castle;
import org.l2jmobius.gameserver.model.siege.Fort;
import org.l2jmobius.gameserver.network.OutgoingPackets;

public class Die implements IClientOutgoingPacket
{
	private final int _objectId;
	private final boolean _fake;
	private boolean _sweepable;
	private boolean _canTeleport;
	private boolean _allowFixedRes;
	private Clan _clan;
	Creature _creature;
	
	public Die(Creature creature)
	{
		_creature = creature;
		if (creature instanceof Player)
		{
			final Player player = creature.getActingPlayer();
			_allowFixedRes = player.getAccessLevel().allowFixedRes();
			_clan = player.getClan();
			_canTeleport = !player.isPendingRevive();
		}
		_objectId = creature.getObjectId();
		_fake = !creature.isDead();
		if (creature instanceof Attackable)
		{
			_sweepable = ((Attackable) creature).isSweepActive();
		}
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		if (_fake)
		{
			return false;
		}
		OutgoingPackets.DIE.writeId(packet);
		packet.writeD(_objectId);
		// NOTE:
		// 6d 00 00 00 00 - to nearest village
		// 6d 01 00 00 00 - to hide away
		// 6d 02 00 00 00 - to castle
		// 6d 03 00 00 00 - to siege HQ
		// sweepable
		// 6d 04 00 00 00 - FIXED
		packet.writeD(_canTeleport ? 1 : 0); // 6d 00 00 00 00 - to nearest village
		if (_canTeleport && (_clan != null))
		{
			SiegeClan siegeClan = null;
			boolean isInDefense = false;
			final Castle castle = CastleManager.getInstance().getCastle(_creature);
			final Fort fort = FortManager.getInstance().getFort(_creature);
			if ((castle != null) && castle.getSiege().isInProgress())
			{
				// siege in progress
				siegeClan = castle.getSiege().getAttackerClan(_clan);
				if ((siegeClan == null) && castle.getSiege().checkIsDefender(_clan))
				{
					isInDefense = true;
				}
			}
			else if ((fort != null) && fort.getSiege().isInProgress())
			{
				// siege in progress
				siegeClan = fort.getSiege().getAttackerClan(_clan);
				if ((siegeClan == null) && fort.getSiege().checkIsDefender(_clan))
				{
					isInDefense = true;
				}
			}
			packet.writeD(_clan.getHideoutId() > 0 ? 1 : 0); // 6d 01 00 00 00 - to hide away
			packet.writeD((_clan.getCastleId() > 0) || (_clan.getFortId() > 0) || isInDefense ? 1 : 0); // 6d 02 00 00 00 - to castle
			packet.writeD((siegeClan != null) && !isInDefense && !siegeClan.getFlag().isEmpty() ? 1 : 0); // 6d 03 00 00 00 - to siege HQ
		}
		else
		{
			packet.writeD(0); // 6d 01 00 00 00 - to hide away
			packet.writeD(0); // 6d 02 00 00 00 - to castle
			packet.writeD(0); // 6d 03 00 00 00 - to siege HQ
		}
		packet.writeD(_sweepable ? 1 : 0); // sweepable (blue glow)
		packet.writeD(_allowFixedRes ? 1 : 0); // 6d 04 00 00 00 - to FIXED
		return true;
	}
}
