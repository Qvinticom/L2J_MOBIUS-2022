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

import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Playable;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.Summon;
import org.l2jmobius.gameserver.model.actor.instance.Servitor;
import org.l2jmobius.gameserver.model.skill.Stat;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.util.Util;

public class PlayerStatus extends PlayableStatus
{
	public PlayerStatus(Player player)
	{
		super(player);
	}
	
	@Override
	public void reduceHp(double value, Creature attacker)
	{
		reduceHp(value, attacker, true);
	}
	
	@Override
	public void reduceHp(double amount, Creature attacker, boolean awake)
	{
		if (getActiveChar().isInvul() && (getActiveChar() != attacker))
		{
			return;
		}
		
		if (attacker instanceof Player)
		{
			if (getActiveChar().isDead() && !getActiveChar().isFakeDeath())
			{
				return;
			}
		}
		else
		{
			if (getActiveChar().isDead())
			{
				return;
			}
		}
		
		double value = amount;
		int fullValue = (int) amount;
		if ((attacker != null) && (attacker != getActiveChar()))
		{
			// Check and calculate transfered damage
			final Summon summon = getActiveChar().getPet();
			
			// TODO correct range
			if ((summon instanceof Servitor) && Util.checkIfInRange(900, getActiveChar(), summon, true))
			{
				int tDmg = ((int) value * (int) getActiveChar().getStat().calcStat(Stat.TRANSFER_DAMAGE_PERCENT, 0, null, null)) / 100;
				
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
			
			if (attacker instanceof Playable)
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
		
		if (!getActiveChar().isDead() && getActiveChar().isSitting())
		{
			if (getActiveChar().getPrivateStoreType() != 0)
			{
				getActiveChar().setPrivateStoreType(Player.STORE_PRIVATE_NONE);
				getActiveChar().broadcastUserInfo();
			}
			
			getActiveChar().standUp();
		}
		
		if (getActiveChar().isFakeDeath())
		{
			getActiveChar().stopFakeDeath(null);
		}
		
		if ((attacker != null) && (attacker != getActiveChar()) && (fullValue > 0))
		{
			// Send a System Message to the Player
			final SystemMessage smsg = new SystemMessage(SystemMessageId.S1_HIT_YOU_FOR_S2_DAMAGE);
			if (attacker instanceof Npc)
			{
				smsg.addString(((Npc) attacker).getTemplate().getName());
			}
			else if (attacker instanceof Summon)
			{
				smsg.addString(((Summon) attacker).getTemplate().getName());
			}
			else
			{
				smsg.addString(attacker.getName());
			}
			smsg.addNumber(fullValue);
			getActiveChar().sendPacket(smsg);
		}
	}
	
	@Override
	public Player getActiveChar()
	{
		return (Player) super.getActiveChar();
	}
}
