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
package com.l2jmobius.gameserver.model.actor.status;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.model.L2Character;
import com.l2jmobius.gameserver.model.L2Summon;
import com.l2jmobius.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2SummonInstance;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import com.l2jmobius.gameserver.skills.Stats;
import com.l2jmobius.gameserver.util.Util;

public class PcStatus extends PlayableStatus
{
	// =========================================================
	// Data Field
	
	// =========================================================
	// Constructor
	public PcStatus(L2PcInstance activeChar)
	{
		super(activeChar);
	}
	
	// =========================================================
	// Method - Public
	@Override
	public final void reduceHp(double value, L2Character attacker)
	{
		reduceHp(value, attacker, true);
	}
	
	@Override
	public final void reduceHp(double value, L2Character attacker, boolean awake)
	{
		if (getActiveChar().isInvul())
		{
			if (attacker != getActiveChar())
			{
				return;
			}
		}
		
		if (getActiveChar().isDead())
		{
			return;
		}
		
		int fullValue = (int) value;
		if ((attacker != null) && (attacker != getActiveChar()))
		{
			// Check and calculate transfered damage
			final L2Summon summon = getActiveChar().getPet();
			if ((summon != null) && (summon instanceof L2SummonInstance) && Util.checkIfInRange(900, getActiveChar(), summon, true))
			{
				int tDmg = ((int) value * (int) getActiveChar().getStat().calcStat(Stats.TRANSFER_DAMAGE_PERCENT, 0, null, null)) / 100;
				
				// Only transfer dmg up to current HP, it should not be killed
				if (summon.getCurrentHp() < tDmg)
				{
					tDmg = (int) summon.getCurrentHp() - 1;
				}
				if (tDmg > 0)
				{
					summon.reduceCurrentHp(tDmg, attacker);
					value -= tDmg;
					fullValue = (int) value; // reduce the annouced value here as player will get a message about summon dammage
				}
			}
			
			if (attacker instanceof L2PlayableInstance)
			{
				if (getCurrentCp() >= value)
				{
					setCurrentCp(getCurrentCp() - value); // Set Cp to diff of Cp vs value
					value = 0; // No need to subtract anything from Hp
				}
				else
				{
					value -= getCurrentCp(); // Get diff from value vs Cp; will apply diff to Hp
					setCurrentCp(0); // Set Cp to 0
				}
			}
		}
		
		super.reduceHp(value, attacker, awake);
		
		if (!getActiveChar().isDead() && getActiveChar().isSitting() && awake)
		{
			getActiveChar().standUp();
		}
		
		if (getActiveChar().isFakeDeath() && awake)
		{
			getActiveChar().stopFakeDeath(null);
		}
		
		if ((attacker != null) && (attacker != getActiveChar()) && (fullValue > 0))
		{
			// Send a System Message to the L2PcInstance
			final SystemMessage smsg = new SystemMessage(SystemMessage.S1_GAVE_YOU_S2_DMG);
			
			if (Config.DEBUG)
			{
				_log.fine("Attacker:" + attacker.getName());
			}
			
			if (attacker instanceof L2NpcInstance)
			{
				final int mobId = ((L2NpcInstance) attacker).getTemplate().idTemplate;
				
				if (Config.DEBUG)
				{
					_log.fine("mob id:" + mobId);
				}
				
				smsg.addNpcName(mobId);
				
				if (Config.AUTO_TARGET_NPC)
				{
					if (getActiveChar().getTarget() == null)
					{
						((L2NpcInstance) attacker).onAction(getActiveChar());
					}
				}
			}
			else if (attacker instanceof L2Summon)
			{
				final int mobId = ((L2Summon) attacker).getTemplate().idTemplate;
				
				smsg.addNpcName(mobId);
			}
			else
			{
				smsg.addString(attacker.getName());
			}
			
			smsg.addNumber(fullValue);
			getActiveChar().sendPacket(smsg);
		}
	}
	
	// =========================================================
	// Method - Private
	
	// =========================================================
	// Property - Public
	@Override
	public L2PcInstance getActiveChar()
	{
		return (L2PcInstance) super.getActiveChar();
	}
}