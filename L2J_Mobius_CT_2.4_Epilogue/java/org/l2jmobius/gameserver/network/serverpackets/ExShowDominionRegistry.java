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

import java.util.Calendar;
import java.util.List;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.instancemanager.TerritoryWarManager;
import org.l2jmobius.gameserver.instancemanager.TerritoryWarManager.Territory;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.clan.Clan;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * @author GodKratos
 */
public class ExShowDominionRegistry implements IClientOutgoingPacket
{
	private static final int MINID = 80;
	
	private final int _castleId;
	private int _clanReq = 0x00;
	private int _mercReq = 0x00;
	private int _isMercRegistered = 0x00;
	private int _isClanRegistered = 0x00;
	private int _warTime = (int) (Calendar.getInstance().getTimeInMillis() / 1000);
	private final int _currentTime = (int) (Calendar.getInstance().getTimeInMillis() / 1000);
	
	public ExShowDominionRegistry(int castleId, Player player)
	{
		_castleId = castleId;
		if (TerritoryWarManager.getInstance().getRegisteredClans(castleId) != null)
		{
			_clanReq = TerritoryWarManager.getInstance().getRegisteredClans(castleId).size();
			if (player.getClan() != null)
			{
				_isClanRegistered = (TerritoryWarManager.getInstance().getRegisteredClans(castleId).contains(player.getClan()) ? 1 : 0);
			}
		}
		if (TerritoryWarManager.getInstance().getRegisteredMercenaries(castleId) != null)
		{
			_mercReq = TerritoryWarManager.getInstance().getRegisteredMercenaries(castleId).size();
			_isMercRegistered = (TerritoryWarManager.getInstance().getRegisteredMercenaries(castleId).contains(player.getObjectId()) ? 1 : 0);
		}
		_warTime = (int) (TerritoryWarManager.getInstance().getTWStartTimeInMillis() / 1000);
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_SHOW_DOMINION_REGISTRY.writeId(packet);
		packet.writeD(MINID + _castleId); // Current Territory Id
		if (TerritoryWarManager.getInstance().getTerritory(_castleId) == null)
		{
			// something is wrong
			packet.writeS("No Owner"); // Owners Clan
			packet.writeS("No Owner"); // Owner Clan Leader
			packet.writeS("No Ally"); // Owner Alliance
		}
		else
		{
			final Clan clan = TerritoryWarManager.getInstance().getTerritory(_castleId).getOwnerClan();
			if (clan == null)
			{
				// something is wrong
				packet.writeS("No Owner"); // Owners Clan
				packet.writeS("No Owner"); // Owner Clan Leader
				packet.writeS("No Ally"); // Owner Alliance
			}
			else
			{
				packet.writeS(clan.getName()); // Owners Clan
				packet.writeS(clan.getLeaderName()); // Owner Clan Leader
				packet.writeS(clan.getAllyName()); // Owner Alliance
			}
		}
		packet.writeD(_clanReq); // Clan Request
		packet.writeD(_mercReq); // Merc Request
		packet.writeD(_warTime); // War Time
		packet.writeD(_currentTime); // Current Time
		packet.writeD(_isClanRegistered); // is Cancel clan registration
		packet.writeD(_isMercRegistered); // is Cancel mercenaries registration
		packet.writeD(1); // unknown
		final List<Territory> territoryList = TerritoryWarManager.getInstance().getAllTerritories();
		packet.writeD(territoryList.size()); // Territory Count
		for (Territory t : territoryList)
		{
			packet.writeD(t.getTerritoryId()); // Territory Id
			packet.writeD(t.getOwnedWardIds().size()); // Emblem Count
			for (int i : t.getOwnedWardIds())
			{
				packet.writeD(i); // Emblem ID - should be in for loop for emblem count
			}
		}
		return true;
	}
}
