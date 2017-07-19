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
package com.l2jmobius.gameserver.ai;

import com.l2jmobius.gameserver.model.L2CharPosition;
import com.l2jmobius.gameserver.model.L2Character;
import com.l2jmobius.gameserver.model.L2Object;
import com.l2jmobius.gameserver.model.L2Skill;
import com.l2jmobius.gameserver.model.actor.instance.L2BoatInstance;
import com.l2jmobius.gameserver.network.serverpackets.VehicleDeparture;
import com.l2jmobius.gameserver.network.serverpackets.VehicleInfo;
import com.l2jmobius.gameserver.network.serverpackets.VehicleStarted;

/**
 * @author DS
 */
public class L2BoatAI extends L2CharacterAI
{
	public L2BoatAI(L2BoatInstance.AIAccessor accessor)
	{
		super(accessor);
	}
	
	@Override
	protected void onIntentionAttack(L2Character target)
	{
	}
	
	@Override
	protected void onIntentionCast(L2Skill skill, L2Object target)
	{
	}
	
	@Override
	protected void onIntentionFollow(L2Character target)
	{
	}
	
	@Override
	protected void onIntentionPickUp(L2Object item)
	{
	}
	
	@Override
	protected void onIntentionInteract(L2Object object)
	{
	}
	
	@Override
	protected void onEvtAttacked(L2Character attacker)
	{
	}
	
	@Override
	protected void onEvtAggression(L2Character target, int aggro)
	{
	}
	
	@Override
	protected void onEvtStunned(L2Character attacker)
	{
	}
	
	@Override
	protected void onEvtSleeping(L2Character attacker)
	{
	}
	
	@Override
	protected void onEvtRooted(L2Character attacker)
	{
	}
	
	@Override
	protected void onEvtForgetObject(L2Object object)
	{
	}
	
	@Override
	protected void onEvtCancel()
	{
	}
	
	@Override
	protected void onEvtDead()
	{
	}
	
	@Override
	protected void onEvtFakeDeath()
	{
	}
	
	@Override
	protected void onEvtFinishCasting()
	{
	}
	
	@Override
	protected void clientActionFailed()
	{
	}
	
	@Override
	protected void moveToPawn(L2Object pawn, int offset)
	{
	}
	
	@Override
	protected void moveTo(int x, int y, int z)
	{
		if (!_actor.isMovementDisabled())
		{
			if (!_client_moving)
			{
				_actor.broadcastPacket(new VehicleStarted(_actor.getObjectId(), 1));
			}
			
			_client_moving = true;
			_accessor.moveTo(x, y, z);
			_actor.broadcastPacket(new VehicleDeparture(getActor()));
		}
	}
	
	@Override
	protected void clientStoppedMoving()
	{
		_client_moving = false;
		_actor.broadcastPacket(new VehicleStarted(_actor.getObjectId(), 0));
		_actor.broadcastPacket(new VehicleInfo(getActor()));
	}
	
	@Override
	protected void clientStopMoving(L2CharPosition pos)
	{
		if (_actor.isMoving())
		{
			_accessor.stopMove(pos);
		}
		
		if (_client_moving || (pos != null))
		{
			_client_moving = false;
			_actor.broadcastPacket(new VehicleStarted(_actor.getObjectId(), 0));
			_actor.broadcastPacket(new VehicleInfo(getActor()));
		}
	}
	
	@Override
	public L2BoatInstance getActor()
	{
		return (L2BoatInstance) _actor;
	}
}