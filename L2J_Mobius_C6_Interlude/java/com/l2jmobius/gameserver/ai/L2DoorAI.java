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

import com.l2jmobius.commons.concurrent.ThreadPoolManager;
import com.l2jmobius.gameserver.model.L2Object;
import com.l2jmobius.gameserver.model.L2Skill;
import com.l2jmobius.gameserver.model.actor.L2Character;
import com.l2jmobius.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2FortSiegeGuardInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2SiegeGuardInstance;
import com.l2jmobius.gameserver.model.actor.position.Location;

/**
 * @author mkizub TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style - Code Templates
 */
public class L2DoorAI extends L2CharacterAI
{
	public L2DoorAI(L2DoorInstance.AIAccessor accessor)
	{
		super(accessor);
	}
	
	// rather stupid AI... well, it's for doors :D
	@Override
	protected void onIntentionIdle()
	{
		// null;
	}
	
	@Override
	protected void onIntentionActive()
	{
		// null;
	}
	
	@Override
	protected void onIntentionRest()
	{
		// null;
	}
	
	@Override
	protected void onIntentionAttack(L2Character target)
	{
		// null;
	}
	
	@Override
	protected void onIntentionCast(L2Skill skill, L2Object target)
	{
		// null;
	}
	
	@Override
	protected void onIntentionMoveTo(Location destination)
	{
		// null;
	}
	
	@Override
	protected void onIntentionFollow(L2Character target)
	{
		// null;
	}
	
	@Override
	protected void onIntentionPickUp(L2Object item)
	{
		// null;
	}
	
	@Override
	protected void onIntentionInteract(L2Object object)
	{
		// null;
	}
	
	@Override
	protected void onEvtThink()
	{
		// null;
	}
	
	@Override
	protected void onEvtAttacked(L2Character attacker)
	{
		L2DoorInstance me = (L2DoorInstance) _actor;
		ThreadPoolManager.execute(new onEventAttackedDoorTask(me, attacker));
	}
	
	@Override
	protected void onEvtAggression(L2Character target, int aggro)
	{
		// null;
	}
	
	@Override
	protected void onEvtStunned(L2Character attacker)
	{
		// null;
	}
	
	@Override
	protected void onEvtSleeping(L2Character attacker)
	{
		// null;
	}
	
	@Override
	protected void onEvtRooted(L2Character attacker)
	{
		// null;
	}
	
	@Override
	protected void onEvtReadyToAct()
	{
		// null;
	}
	
	@Override
	protected void onEvtUserCmd(Object arg0, Object arg1)
	{
		// null;
	}
	
	@Override
	protected void onEvtArrived()
	{
		// null;
	}
	
	@Override
	protected void onEvtArrivedRevalidate()
	{
		// null;
	}
	
	@Override
	protected void onEvtArrivedBlocked(Location blocked_at_pos)
	{
		// null;
	}
	
	@Override
	protected void onEvtForgetObject(L2Object object)
	{
		// null;
	}
	
	@Override
	protected void onEvtCancel()
	{
		// null;
	}
	
	@Override
	protected void onEvtDead()
	{
		// null;
	}
	
	private class onEventAttackedDoorTask implements Runnable
	{
		private final L2DoorInstance _door;
		private final L2Character _attacker;
		
		public onEventAttackedDoorTask(L2DoorInstance door, L2Character attacker)
		{
			_door = door;
			_attacker = attacker;
		}
		
		/*
		 * (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run()
		{
			_door.getKnownList().updateKnownObjects();
			
			for (L2SiegeGuardInstance guard : _door.getKnownSiegeGuards())
			{
				if ((guard != null) && (guard.getAI() != null) && _actor.isInsideRadius(guard, guard.getFactionRange(), false, true) && (Math.abs(_attacker.getZ() - guard.getZ()) < 200))
				{
					guard.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, _attacker, 15);
				}
			}
			for (L2FortSiegeGuardInstance guard : _door.getKnownFortSiegeGuards())
			{
				if ((guard != null) && (guard.getAI() != null) && _actor.isInsideRadius(guard, guard.getFactionRange(), false, true) && (Math.abs(_attacker.getZ() - guard.getZ()) < 200))
				{
					guard.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, _attacker, 15);
				}
			}
		}
	}
}
