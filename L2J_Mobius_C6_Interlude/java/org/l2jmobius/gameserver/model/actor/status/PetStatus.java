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
package org.l2jmobius.gameserver.model.actor.status;

import org.l2jmobius.gameserver.ai.CtrlEvent;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.instance.Pet;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

public class PetStatus extends SummonStatus
{
	private int _currentFed = 0; // Current Fed of the Pet
	
	public PetStatus(Pet activeChar)
	{
		super(activeChar);
	}
	
	@Override
	public void reduceHp(double value, Creature attacker)
	{
		reduceHp(value, attacker, true);
	}
	
	@Override
	public void reduceHp(double value, Creature attacker, boolean awake)
	{
		if (getActiveChar().isDead())
		{
			return;
		}
		
		super.reduceHp(value, attacker, awake);
		
		if (attacker != null)
		{
			final SystemMessage sm = new SystemMessage(SystemMessageId.YOUR_PET_RECEIVED_S2_DAMAGE_CAUSED_BY_S1);
			if (attacker instanceof Npc)
			{
				sm.addNpcName(((Npc) attacker).getTemplate().getDisplayId());
			}
			else
			{
				sm.addString(attacker.getName());
			}
			sm.addNumber((int) value);
			getActiveChar().getOwner().sendPacket(sm);
			
			getActiveChar().getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, attacker);
		}
	}
	
	@Override
	public Pet getActiveChar()
	{
		return (Pet) super.getActiveChar();
	}
	
	public int getCurrentFed()
	{
		return _currentFed;
	}
	
	public void setCurrentFed(int value)
	{
		_currentFed = value;
	}
}
