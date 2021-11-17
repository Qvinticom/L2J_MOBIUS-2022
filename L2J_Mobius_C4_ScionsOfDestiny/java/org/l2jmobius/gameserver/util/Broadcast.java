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
package org.l2jmobius.gameserver.util;

import org.l2jmobius.gameserver.data.xml.ZoneData;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.zone.ZoneType;
import org.l2jmobius.gameserver.network.serverpackets.CharInfo;
import org.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;
import org.l2jmobius.gameserver.network.serverpackets.RelationChanged;

public class Broadcast
{
	/**
	 * Send a packet to all Player in the _KnownPlayers of the Creature that have the Character targeted.<br>
	 * <br>
	 * <b><u>Concept</u>:</b><br>
	 * <br>
	 * Player in the detection area of the Creature are identified in <b>_knownPlayers</b>.<br>
	 * In order to inform other players of state modification on the Creature, server just need to go through _knownPlayers to send Server->Client Packet<br>
	 * <font color=#FF0000><b><u>Caution</u>: This method DOESN'T SEND Server->Client packet to this Creature (to do this use method toSelfAndKnownPlayers)</b></font>
	 * @param creature
	 * @param mov
	 */
	public static void toPlayersTargettingMyself(Creature creature, IClientOutgoingPacket mov)
	{
		for (Player player : creature.getKnownList().getKnownPlayers().values())
		{
			if ((player == null) || (player.getTarget() != creature))
			{
				continue;
			}
			
			player.sendPacket(mov);
		}
	}
	
	/**
	 * Send a packet to all Player in the _KnownPlayers of the Creature.<br>
	 * <br>
	 * <b><u>Concept</u>:</b><br>
	 * <br>
	 * Player in the detection area of the Creature are identified in <b>_knownPlayers</b>.<br>
	 * In order to inform other players of state modification on the Creature, server just need to go through _knownPlayers to send Server->Client Packet<br>
	 * <font color=#FF0000><b><u>Caution</u>: This method DOESN'T SEND Server->Client packet to this Creature (to do this use method toSelfAndKnownPlayers)</b></font>
	 * @param creature
	 * @param mov
	 */
	public static void toKnownPlayers(Creature creature, IClientOutgoingPacket mov)
	{
		for (Player player : creature.getKnownList().getKnownPlayers().values())
		{
			if (player == null)
			{
				continue;
			}
			
			// TEMP FIX: If player is not visible don't send packets broadcast to all his KnowList. This will avoid GM detection with l2net and olympiad's crash. We can now find old problems with invisible mode.
			if ((creature instanceof Player) && !player.isGM() && (((Player) creature).getAppearance().isInvisible() || ((Player) creature).inObserverMode()))
			{
				return;
			}
			
			try
			{
				player.sendPacket(mov);
				if ((mov instanceof CharInfo) && (creature instanceof Player))
				{
					final int relation = ((Player) creature).getRelation(player);
					if ((creature.getKnownList().getKnownRelations().get(player.getObjectId()) != null) && (creature.getKnownList().getKnownRelations().get(player.getObjectId()) != relation))
					{
						player.sendPacket(new RelationChanged((Player) creature, relation, player.isAutoAttackable(creature)));
					}
				}
			}
			catch (NullPointerException e)
			{
			}
		}
	}
	
	/**
	 * Send a packet to all Player in the _KnownPlayers (in the specified radius) of the Creature.<br>
	 * <br>
	 * <b><u>Concept</u>:</b><br>
	 * <br>
	 * Player in the detection area of the Creature are identified in <b>_knownPlayers</b>.<br>
	 * In order to inform other players of state modification on the Creature, server just needs to go through _knownPlayers to send Server->Client Packet and check the distance between the targets.<br>
	 * <font color=#FF0000><b><u>Caution</u>: This method DOESN'T SEND Server->Client packet to this Creature (to do this use method toSelfAndKnownPlayers)</b></font>
	 * @param creature
	 * @param mov
	 * @param radiusValue
	 */
	public static void toKnownPlayersInRadius(Creature creature, IClientOutgoingPacket mov, int radiusValue)
	{
		int radius = radiusValue;
		if (radius < 0)
		{
			radius = 1500;
		}
		
		for (Player player : creature.getKnownList().getKnownPlayers().values())
		{
			if (player == null)
			{
				continue;
			}
			
			if (creature.isInsideRadius2D(player, radius))
			{
				player.sendPacket(mov);
			}
		}
	}
	
	/**
	 * Send a packet to all Player in the _KnownPlayers of the Creature and to the specified character.<br>
	 * <br>
	 * <b><u>Concept</u>:</b><br>
	 * <br>
	 * Player in the detection area of the Creature are identified in <b>_knownPlayers</b>.<br>
	 * In order to inform other players of state modification on the Creature, server just need to go through _knownPlayers to send Server->Client Packet
	 * @param creature
	 * @param mov
	 */
	public static void toSelfAndKnownPlayers(Creature creature, IClientOutgoingPacket mov)
	{
		if (creature instanceof Player)
		{
			creature.sendPacket(mov);
		}
		
		toKnownPlayers(creature, mov);
	}
	
	// To improve performance we are comparing values of radius^2 instead of calculating sqrt all the time
	public static void toSelfAndKnownPlayersInRadius(Creature creature, IClientOutgoingPacket mov, long radiusSqValue)
	{
		long radiusSq = radiusSqValue;
		if (radiusSq < 0)
		{
			radiusSq = 360000;
		}
		
		if (creature instanceof Player)
		{
			creature.sendPacket(mov);
		}
		
		for (Player player : creature.getKnownList().getKnownPlayers().values())
		{
			if ((player != null) && (creature.calculateDistanceSq3D(player) <= radiusSq))
			{
				player.sendPacket(mov);
			}
		}
	}
	
	/**
	 * Send a packet to all Player present in the world.<br>
	 * <br>
	 * <b><u>Concept</u>:</b><br>
	 * <br>
	 * In order to inform other players of state modification on the Creature, server just need to go through _allPlayers to send Server->Client Packet<br>
	 * <font color=#FF0000><b><u>Caution</u>: This method DOESN'T SEND Server->Client packet to this Creature (to do this use method toSelfAndKnownPlayers)</b></font>
	 * @param packet
	 */
	public static void toAllOnlinePlayers(IClientOutgoingPacket packet)
	{
		for (Player onlinePlayer : World.getInstance().getAllPlayers())
		{
			if (onlinePlayer == null)
			{
				continue;
			}
			
			onlinePlayer.sendPacket(packet);
		}
	}
	
	/**
	 * Send a packet to all players in a specific zone type.
	 * @param <T> ZoneType.
	 * @param zoneType : The zone type to send packets.
	 * @param packets : The packets to send.
	 */
	public static <T extends ZoneType> void toAllPlayersInZoneType(Class<T> zoneType, IClientOutgoingPacket... packets)
	{
		for (ZoneType zone : ZoneData.getInstance().getAllZones(zoneType))
		{
			for (Creature creature : zone.getCharactersInside())
			{
				if (creature == null)
				{
					continue;
				}
				
				for (IClientOutgoingPacket packet : packets)
				{
					creature.sendPacket(packet);
				}
			}
		}
	}
}
