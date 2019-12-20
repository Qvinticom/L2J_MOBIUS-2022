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

import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.serverpackets.CharInfo;
import org.l2jmobius.gameserver.network.serverpackets.GameServerPacket;
import org.l2jmobius.gameserver.network.serverpackets.RelationChanged;

public class Broadcast
{
	/**
	 * Send a packet to all PlayerInstance in the _KnownPlayers of the Creature that have the Character targeted.<BR>
	 * <B><U> Concept</U> :</B><BR>
	 * PlayerInstance in the detection area of the Creature are identified in <B>_knownPlayers</B>.<BR>
	 * In order to inform other players of state modification on the Creature, server just need to go through _knownPlayers to send Server->Client Packet<BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND Server->Client packet to this Creature (to do this use method toSelfAndKnownPlayers)</B></FONT><BR>
	 * @param creature
	 * @param mov
	 */
	public static void toPlayersTargettingMyself(Creature creature, GameServerPacket mov)
	{
		for (PlayerInstance player : creature.getKnownList().getKnownPlayers().values())
		{
			if ((player == null) || (player.getTarget() != creature))
			{
				continue;
			}
			
			player.sendPacket(mov);
		}
	}
	
	/**
	 * Send a packet to all PlayerInstance in the _KnownPlayers of the Creature.<BR>
	 * <B><U> Concept</U> :</B><BR>
	 * PlayerInstance in the detection area of the Creature are identified in <B>_knownPlayers</B>.<BR>
	 * In order to inform other players of state modification on the Creature, server just need to go through _knownPlayers to send Server->Client Packet<BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND Server->Client packet to this Creature (to do this use method toSelfAndKnownPlayers)</B></FONT><BR>
	 * @param creature
	 * @param mov
	 */
	public static void toKnownPlayers(Creature creature, GameServerPacket mov)
	{
		for (PlayerInstance player : creature.getKnownList().getKnownPlayers().values())
		{
			if (player == null)
			{
				continue;
			}
			
			// TEMP FIX: If player is not visible don't send packets broadcast to all his KnowList. This will avoid GM detection with l2net and olympiad's crash. We can now find old problems with invisible mode.
			if ((creature instanceof PlayerInstance) && !player.isGM() && (((PlayerInstance) creature).getAppearance().isInvisible() || ((PlayerInstance) creature).inObserverMode()))
			{
				return;
			}
			
			try
			{
				player.sendPacket(mov);
				if ((mov instanceof CharInfo) && (creature instanceof PlayerInstance))
				{
					final int relation = ((PlayerInstance) creature).getRelation(player);
					
					if ((creature.getKnownList().getKnownRelations().get(player.getObjectId()) != null) && (creature.getKnownList().getKnownRelations().get(player.getObjectId()) != relation))
					{
						player.sendPacket(new RelationChanged((PlayerInstance) creature, relation, player.isAutoAttackable(creature)));
					}
				}
			}
			catch (NullPointerException e)
			{
			}
		}
	}
	
	/**
	 * Send a packet to all PlayerInstance in the _KnownPlayers (in the specified radius) of the Creature.<BR>
	 * <B><U> Concept</U> :</B><BR>
	 * PlayerInstance in the detection area of the Creature are identified in <B>_knownPlayers</B>.<BR>
	 * In order to inform other players of state modification on the Creature, server just needs to go through _knownPlayers to send Server->Client Packet and check the distance between the targets.<BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND Server->Client packet to this Creature (to do this use method toSelfAndKnownPlayers)</B></FONT><BR>
	 * @param creature
	 * @param mov
	 * @param radius
	 */
	public static void toKnownPlayersInRadius(Creature creature, GameServerPacket mov, int radius)
	{
		if (radius < 0)
		{
			radius = 1500;
		}
		
		for (PlayerInstance player : creature.getKnownList().getKnownPlayers().values())
		{
			if (player == null)
			{
				continue;
			}
			
			if (creature.isInsideRadius(player, radius, false, false))
			{
				player.sendPacket(mov);
			}
		}
	}
	
	/**
	 * Send a packet to all PlayerInstance in the _KnownPlayers of the Creature and to the specified character.<BR>
	 * <B><U> Concept</U> :</B><BR>
	 * PlayerInstance in the detection area of the Creature are identified in <B>_knownPlayers</B>.<BR>
	 * In order to inform other players of state modification on the Creature, server just need to go through _knownPlayers to send Server->Client Packet<BR>
	 * @param creature
	 * @param mov
	 */
	public static void toSelfAndKnownPlayers(Creature creature, GameServerPacket mov)
	{
		if (creature instanceof PlayerInstance)
		{
			creature.sendPacket(mov);
		}
		
		toKnownPlayers(creature, mov);
	}
	
	// To improve performance we are comparing values of radius^2 instead of calculating sqrt all the time
	public static void toSelfAndKnownPlayersInRadius(Creature creature, GameServerPacket mov, long radiusSq)
	{
		if (radiusSq < 0)
		{
			radiusSq = 360000;
		}
		
		if (creature instanceof PlayerInstance)
		{
			creature.sendPacket(mov);
		}
		
		for (PlayerInstance player : creature.getKnownList().getKnownPlayers().values())
		{
			if ((player != null) && (creature.getDistanceSq(player) <= radiusSq))
			{
				player.sendPacket(mov);
			}
		}
	}
	
	/**
	 * Send a packet to all PlayerInstance present in the world.<BR>
	 * <B><U> Concept</U> :</B><BR>
	 * In order to inform other players of state modification on the Creature, server just need to go through _allPlayers to send Server->Client Packet<BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND Server->Client packet to this Creature (to do this use method toSelfAndKnownPlayers)</B></FONT><BR>
	 * @param packet
	 */
	public static void toAllOnlinePlayers(GameServerPacket packet)
	{
		for (PlayerInstance onlinePlayer : World.getInstance().getAllPlayers())
		{
			if (onlinePlayer == null)
			{
				continue;
			}
			
			onlinePlayer.sendPacket(packet);
		}
	}
}
