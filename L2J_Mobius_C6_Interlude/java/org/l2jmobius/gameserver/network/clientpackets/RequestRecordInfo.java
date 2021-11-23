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
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.Summon;
import org.l2jmobius.gameserver.model.actor.instance.Boat;
import org.l2jmobius.gameserver.model.actor.instance.Door;
import org.l2jmobius.gameserver.model.actor.instance.Pet;
import org.l2jmobius.gameserver.model.actor.instance.StaticObject;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.serverpackets.CharInfo;
import org.l2jmobius.gameserver.network.serverpackets.DoorInfo;
import org.l2jmobius.gameserver.network.serverpackets.DoorStatusUpdate;
import org.l2jmobius.gameserver.network.serverpackets.GetOnVehicle;
import org.l2jmobius.gameserver.network.serverpackets.NpcInfo;
import org.l2jmobius.gameserver.network.serverpackets.PetInfo;
import org.l2jmobius.gameserver.network.serverpackets.PetItemList;
import org.l2jmobius.gameserver.network.serverpackets.RelationChanged;
import org.l2jmobius.gameserver.network.serverpackets.SpawnItem;
import org.l2jmobius.gameserver.network.serverpackets.StaticObjectInfo;
import org.l2jmobius.gameserver.network.serverpackets.UserInfo;
import org.l2jmobius.gameserver.network.serverpackets.VehicleInfo;

public class RequestRecordInfo implements IClientIncomingPacket
{
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
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
		
		player.getKnownList().updateKnownObjects();
		player.sendPacket(new UserInfo(player));
		for (WorldObject object : player.getKnownList().getKnownObjects().values())
		{
			if (object == null)
			{
				continue;
			}
			
			if (object instanceof Item)
			{
				player.sendPacket(new SpawnItem((Item) object));
			}
			else if (object instanceof Door)
			{
				if (((Door) object).getCastle() != null)
				{
					player.sendPacket(new DoorInfo((Door) object, true));
				}
				else
				{
					player.sendPacket(new DoorInfo((Door) object, false));
				}
				player.sendPacket(new DoorStatusUpdate((Door) object, player));
			}
			else if (object instanceof Boat)
			{
				if (!player.isInBoat() && (object != player.getBoat()))
				{
					player.sendPacket(new VehicleInfo((Boat) object));
					((Boat) object).sendVehicleDeparture(player);
				}
			}
			else if (object instanceof StaticObject)
			{
				player.sendPacket(new StaticObjectInfo((StaticObject) object));
			}
			else if (object instanceof Npc)
			{
				player.sendPacket(new NpcInfo((Npc) object, player));
			}
			else if (object instanceof Summon)
			{
				final Summon summon = (Summon) object;
				
				// Check if the Player is the owner of the Pet
				if (player.equals(summon.getOwner()))
				{
					player.sendPacket(new PetInfo(summon));
					if (summon instanceof Pet)
					{
						player.sendPacket(new PetItemList((Pet) summon));
					}
				}
				else
				{
					player.sendPacket(new NpcInfo(summon, player));
				}
				
				// The PetInfo packet wipes the PartySpelled (list of active spells' icons). Re-add them
				summon.updateEffectIcons(true);
			}
			else if (object instanceof Player)
			{
				final Player otherPlayer = (Player) object;
				if (otherPlayer.isInBoat())
				{
					otherPlayer.getLocation().setLocation(otherPlayer.getBoat().getLocation());
					player.sendPacket(new CharInfo(otherPlayer, player.isGM() && otherPlayer.getAppearance().isInvisible()));
					final int relation = otherPlayer.getRelation(player);
					if ((otherPlayer.getKnownList().getKnownRelations().get(player.getObjectId()) != null) && (otherPlayer.getKnownList().getKnownRelations().get(player.getObjectId()) != relation))
					{
						player.sendPacket(new RelationChanged(otherPlayer, relation, player.isAutoAttackable(otherPlayer)));
					}
					player.sendPacket(new GetOnVehicle(otherPlayer, otherPlayer.getBoat(), otherPlayer.getBoatPosition().getX(), otherPlayer.getBoatPosition().getY(), otherPlayer.getBoatPosition().getZ()));
				}
				else
				{
					player.sendPacket(new CharInfo(otherPlayer, player.isGM() && otherPlayer.getAppearance().isInvisible()));
					final int relation = otherPlayer.getRelation(player);
					if ((otherPlayer.getKnownList().getKnownRelations().get(player.getObjectId()) != null) && (otherPlayer.getKnownList().getKnownRelations().get(player.getObjectId()) != relation))
					{
						player.sendPacket(new RelationChanged(otherPlayer, relation, player.isAutoAttackable(otherPlayer)));
					}
				}
			}
			
			if (object instanceof Creature)
			{
				// Update the state of the Creature object client side by sending Server->Client packet MoveToPawn/MoveToLocation and AutoAttackStart to the Player
				final Creature obj = (Creature) object;
				if (obj.hasAI())
				{
					obj.getAI().describeStateToPlayer(player);
				}
			}
		}
	}
}
