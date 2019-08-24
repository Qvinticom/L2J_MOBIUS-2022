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

import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Summon;
import org.l2jmobius.gameserver.model.actor.instance.BoatInstance;
import org.l2jmobius.gameserver.model.actor.instance.DoorInstance;
import org.l2jmobius.gameserver.model.actor.instance.NpcInstance;
import org.l2jmobius.gameserver.model.actor.instance.PetInstance;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.actor.instance.StaticObjectInstance;
import org.l2jmobius.gameserver.model.items.instance.ItemInstance;
import org.l2jmobius.gameserver.network.serverpackets.CharInfo;
import org.l2jmobius.gameserver.network.serverpackets.DoorInfo;
import org.l2jmobius.gameserver.network.serverpackets.DoorStatusUpdate;
import org.l2jmobius.gameserver.network.serverpackets.GetOnVehicle;
import org.l2jmobius.gameserver.network.serverpackets.NpcInfo;
import org.l2jmobius.gameserver.network.serverpackets.PetInfo;
import org.l2jmobius.gameserver.network.serverpackets.PetItemList;
import org.l2jmobius.gameserver.network.serverpackets.RelationChanged;
import org.l2jmobius.gameserver.network.serverpackets.SpawnItem;
import org.l2jmobius.gameserver.network.serverpackets.SpawnItemPoly;
import org.l2jmobius.gameserver.network.serverpackets.StaticObject;
import org.l2jmobius.gameserver.network.serverpackets.UserInfo;
import org.l2jmobius.gameserver.network.serverpackets.VehicleInfo;

public class RequestRecordInfo extends GameClientPacket
{
	@Override
	protected void readImpl()
	{
		// trigger
	}
	
	@Override
	protected void runImpl()
	{
		final PlayerInstance _player = getClient().getPlayer();
		
		if (_player == null)
		{
			return;
		}
		
		_player.getKnownList().updateKnownObjects();
		_player.sendPacket(new UserInfo(_player));
		
		for (WorldObject object : _player.getKnownList().getKnownObjects().values())
		{
			if (object == null)
			{
				continue;
			}
			
			if (object.getPoly().isMorphed() && object.getPoly().getPolyType().equals("item"))
			{
				_player.sendPacket(new SpawnItemPoly(object));
			}
			else
			{
				if (object instanceof ItemInstance)
				{
					_player.sendPacket(new SpawnItem((ItemInstance) object));
				}
				else if (object instanceof DoorInstance)
				{
					if (((DoorInstance) object).getCastle() != null)
					{
						_player.sendPacket(new DoorInfo((DoorInstance) object, true));
					}
					else
					{
						_player.sendPacket(new DoorInfo((DoorInstance) object, false));
					}
					_player.sendPacket(new DoorStatusUpdate((DoorInstance) object));
				}
				else if (object instanceof BoatInstance)
				{
					if (!_player.isInBoat() && (object != _player.getBoat()))
					{
						_player.sendPacket(new VehicleInfo((BoatInstance) object));
						((BoatInstance) object).sendVehicleDeparture(_player);
					}
				}
				else if (object instanceof StaticObjectInstance)
				{
					_player.sendPacket(new StaticObject((StaticObjectInstance) object));
				}
				else if (object instanceof NpcInstance)
				{
					_player.sendPacket(new NpcInfo((NpcInstance) object, _player));
				}
				else if (object instanceof Summon)
				{
					final Summon summon = (Summon) object;
					
					// Check if the PlayerInstance is the owner of the Pet
					if (_player.equals(summon.getOwner()))
					{
						_player.sendPacket(new PetInfo(summon));
						
						if (summon instanceof PetInstance)
						{
							_player.sendPacket(new PetItemList((PetInstance) summon));
						}
					}
					else
					{
						_player.sendPacket(new NpcInfo(summon, _player));
					}
					
					// The PetInfo packet wipes the PartySpelled (list of active spells' icons). Re-add them
					summon.updateEffectIcons(true);
				}
				else if (object instanceof PlayerInstance)
				{
					final PlayerInstance otherPlayer = (PlayerInstance) object;
					
					if (otherPlayer.isInBoat())
					{
						otherPlayer.getPosition().setWorldPosition(otherPlayer.getBoat().getPosition().getWorldPosition());
						_player.sendPacket(new CharInfo(otherPlayer));
						final int relation = otherPlayer.getRelation(_player);
						
						if ((otherPlayer.getKnownList().getKnownRelations().get(_player.getObjectId()) != null) && (otherPlayer.getKnownList().getKnownRelations().get(_player.getObjectId()) != relation))
						{
							_player.sendPacket(new RelationChanged(otherPlayer, relation, _player.isAutoAttackable(otherPlayer)));
						}
						
						_player.sendPacket(new GetOnVehicle(otherPlayer, otherPlayer.getBoat(), otherPlayer.getInBoatPosition().getX(), otherPlayer.getInBoatPosition().getY(), otherPlayer.getInBoatPosition().getZ()));
					}
					else
					{
						_player.sendPacket(new CharInfo(otherPlayer));
						final int relation = otherPlayer.getRelation(_player);
						
						if ((otherPlayer.getKnownList().getKnownRelations().get(_player.getObjectId()) != null) && (otherPlayer.getKnownList().getKnownRelations().get(_player.getObjectId()) != relation))
						{
							_player.sendPacket(new RelationChanged(otherPlayer, relation, _player.isAutoAttackable(otherPlayer)));
						}
					}
				}
				
				if (object instanceof Creature)
				{
					// Update the state of the Creature object client side by sending Server->Client packet MoveToPawn/CharMoveToLocation and AutoAttackStart to the PlayerInstance
					final Creature obj = (Creature) object;
					obj.getAI().describeStateToPlayer(_player);
				}
			}
		}
	}
}
