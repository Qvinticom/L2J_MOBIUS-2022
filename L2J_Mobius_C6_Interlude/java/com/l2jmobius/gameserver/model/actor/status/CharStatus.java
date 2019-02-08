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

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import com.l2jmobius.commons.concurrent.ThreadPool;
import com.l2jmobius.commons.util.Rnd;
import com.l2jmobius.gameserver.ai.CtrlIntention;
import com.l2jmobius.gameserver.instancemanager.DuelManager;
import com.l2jmobius.gameserver.model.actor.L2Attackable;
import com.l2jmobius.gameserver.model.actor.L2Character;
import com.l2jmobius.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2SummonInstance;
import com.l2jmobius.gameserver.model.actor.stat.CharStat;
import com.l2jmobius.gameserver.model.entity.Duel;
import com.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import com.l2jmobius.gameserver.skills.Formulas;

public class CharStatus
{
	protected static final Logger LOGGER = Logger.getLogger(CharStatus.class.getName());
	
	final L2Character _activeChar;
	double _currentCp = 0; // Current CP of the L2Character
	double _currentHp = 0; // Current HP of the L2Character
	double _currentMp = 0; // Current MP of the L2Character
	private Set<L2Character> _StatusListener;
	private Future<?> _regTask;
	private byte _flagsRegenActive = 0;
	private static final byte REGEN_FLAG_CP = 4;
	private static final byte REGEN_FLAG_HP = 1;
	private static final byte REGEN_FLAG_MP = 2;
	
	/**
	 * Instantiates a new char status.
	 * @param activeChar the active char
	 */
	public CharStatus(L2Character activeChar)
	{
		_activeChar = activeChar;
	}
	
	/**
	 * Add the object to the list of L2Character that must be informed of HP/MP updates of this L2Character.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * Each L2Character owns a list called <B>_statusListener</B> that contains all L2PcInstance to inform of HP/MP updates. Players who must be informed are players that target this L2Character. When a RegenTask is in progress sever just need to go through this list to send Server->Client packet
	 * StatusUpdate.<BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>Target a PC or NPC</li><BR>
	 * <BR>
	 * @param object L2Character to add to the listener
	 */
	public final void addStatusListener(L2Character object)
	{
		if (object == _activeChar)
		{
			return;
		}
		
		synchronized (getStatusListener())
		{
			getStatusListener().add(object);
		}
	}
	
	/**
	 * Reduce cp.
	 * @param value the value
	 */
	public final void reduceCp(int value)
	{
		if (_currentCp > value)
		{
			setCurrentCp(_currentCp - value);
		}
		else
		{
			setCurrentCp(0);
		}
	}
	
	/**
	 * Reduce the current HP of the L2Character and launch the doDie Task if necessary.<BR>
	 * <BR>
	 * <B><U> Overriden in </U> :</B><BR>
	 * <BR>
	 * <li>L2Attackable : Update the attacker AggroInfo of the L2Attackable _aggroList</li><BR>
	 * <BR>
	 * @param value the value
	 * @param attacker The L2Character who attacks
	 */
	public void reduceHp(double value, L2Character attacker)
	{
		reduceHp(value, attacker, true);
	}
	
	/**
	 * Reduce hp.
	 * @param value the value
	 * @param attacker the attacker
	 * @param awake the awake
	 */
	public void reduceHp(double value, L2Character attacker, boolean awake)
	{
		if (_activeChar.isInvul())
		{
			return;
		}
		
		if (_activeChar instanceof L2PcInstance)
		{
			if (((L2PcInstance) _activeChar).isInDuel())
			{
				// the duel is finishing - players do not recive damage
				if (((L2PcInstance) _activeChar).getDuelState() == Duel.DUELSTATE_DEAD)
				{
					return;
				}
				else if (((L2PcInstance) _activeChar).getDuelState() == Duel.DUELSTATE_WINNER)
				{
					return;
				}
				
				// cancel duel if player got hit by another player, that is not part of the duel or a monster
				if (!(attacker instanceof L2SummonInstance) && (!(attacker instanceof L2PcInstance) || (((L2PcInstance) attacker).getDuelId() != ((L2PcInstance) _activeChar).getDuelId())))
				{
					((L2PcInstance) _activeChar).setDuelState(Duel.DUELSTATE_INTERRUPTED);
				}
			}
			if (_activeChar.isDead() && !_activeChar.isFakeDeath())
			{
				return; // Disabled == null check so skills like Body to Mind work again untill another solution is found
			}
		}
		else
		{
			if (_activeChar.isDead())
			{
				return; // Disabled == null check so skills like Body to Mind work again untill another solution is found
			}
			
			if ((attacker instanceof L2PcInstance) && ((L2PcInstance) attacker).isInDuel() && (!(_activeChar instanceof L2SummonInstance) || (((L2SummonInstance) _activeChar).getOwner().getDuelId() != ((L2PcInstance) attacker).getDuelId()))) // Duelling player attacks mob
			{
				((L2PcInstance) attacker).setDuelState(Duel.DUELSTATE_INTERRUPTED);
			}
		}
		
		if (awake && _activeChar.isSleeping())
		{
			_activeChar.stopSleeping(null);
		}
		
		if (awake && _activeChar.isImmobileUntilAttacked())
		{
			_activeChar.stopImmobileUntilAttacked(null);
		}
		
		if (_activeChar.isStunned() && (Rnd.get(10) == 0))
		{
			_activeChar.stopStunning(null);
		}
		
		// Add attackers to npc's attacker list
		if (_activeChar instanceof L2NpcInstance)
		{
			_activeChar.addAttackerToAttackByList(attacker);
		}
		
		if (value > 0) // Reduce Hp if any
		{
			// If we're dealing with an L2Attackable Instance and the attacker hit it with an over-hit enabled skill, set the over-hit values.
			// Anything else, clear the over-hit flag
			if (_activeChar instanceof L2Attackable)
			{
				if (((L2Attackable) _activeChar).isOverhit())
				{
					((L2Attackable) _activeChar).setOverhitValues(attacker, value);
				}
				else
				{
					((L2Attackable) _activeChar).overhitEnabled(false);
				}
			}
			
			value = _currentHp - value; // Get diff of Hp vs value
			
			if (value <= 0)
			{
				// is the dieing one a duelist? if so change his duel state to dead
				if ((_activeChar instanceof L2PcInstance) && ((L2PcInstance) _activeChar).isInDuel())
				{
					_activeChar.disableAllSkills();
					stopHpMpRegeneration();
					attacker.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
					attacker.sendPacket(ActionFailed.STATIC_PACKET);
					
					// let the DuelManager know of his defeat
					DuelManager.getInstance().onPlayerDefeat((L2PcInstance) getActiveChar());
					value = 1;
				}
				else
				{
					// Set value to 0 if Hp < 0
					value = 0;
				}
			}
			setCurrentHp(value); // Set Hp
		}
		else if (_activeChar instanceof L2Attackable) // If we're dealing with an L2Attackable Instance and the attacker's hit didn't kill the mob, clear the over-hit flag.
		{
			((L2Attackable) _activeChar).overhitEnabled(false);
		}
		
		if (_activeChar.isDead())
		{
			_activeChar.abortAttack();
			_activeChar.abortCast();
			
			if (_activeChar instanceof L2PcInstance)
			{
				if (((L2PcInstance) _activeChar).isInOlympiadMode())
				{
					stopHpMpRegeneration();
					return;
				}
			}
			
			// Start the doDie process
			_activeChar.doDie(attacker);
			
			// now reset currentHp to zero
			setCurrentHp(0);
		}
		else // If we're dealing with an L2Attackable Instance and the attacker's hit didn't kill the mob, clear the over-hit flag
		if (_activeChar instanceof L2Attackable)
		{
			((L2Attackable) _activeChar).overhitEnabled(false);
		}
	}
	
	/**
	 * Reduce mp.
	 * @param value the value
	 */
	public final void reduceMp(double value)
	{
		value = _currentMp - value;
		
		if (value < 0)
		{
			value = 0;
		}
		
		setCurrentMp(value);
	}
	
	/**
	 * Remove the object from the list of L2Character that must be informed of HP/MP updates of this L2Character.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * Each L2Character owns a list called <B>_statusListener</B> that contains all L2PcInstance to inform of HP/MP updates. Players who must be informed are players that target this L2Character. When a RegenTask is in progress sever just need to go through this list to send Server->Client packet
	 * StatusUpdate.<BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>Untarget a PC or NPC</li><BR>
	 * <BR>
	 * @param object L2Character to add to the listener
	 */
	public final void removeStatusListener(L2Character object)
	{
		synchronized (getStatusListener())
		{
			getStatusListener().remove(object);
		}
	}
	
	/**
	 * Start the HP/MP/CP Regeneration task.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Calculate the regen task period</li>
	 * <li>Launch the HP/MP/CP Regeneration task with Medium priority</li><BR>
	 * <BR>
	 */
	public final synchronized void startHpMpRegeneration()
	{
		if ((_regTask == null) && !_activeChar.isDead())
		{
			// Get the Regeneration periode
			final int period = Formulas.getRegeneratePeriod(_activeChar);
			
			// Create the HP/MP/CP Regeneration task
			_regTask = ThreadPool.scheduleAtFixedRate(new RegenTask(), period, period);
		}
	}
	
	/**
	 * Stop the HP/MP/CP Regeneration task.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Set the RegenActive flag to False</li>
	 * <li>Stop the HP/MP/CP Regeneration task</li><BR>
	 * <BR>
	 */
	public final synchronized void stopHpMpRegeneration()
	{
		if (_regTask != null)
		{
			// Stop the HP/MP/CP Regeneration task
			_regTask.cancel(false);
			_regTask = null;
			
			// Set the RegenActive flag to false
			_flagsRegenActive = 0;
		}
	}
	
	/**
	 * Gets the active char.
	 * @return the active char
	 */
	public L2Character getActiveChar()
	{
		return _activeChar;
	}
	
	/**
	 * Gets the current cp.
	 * @return the current cp
	 */
	public final double getCurrentCp()
	{
		return _currentCp;
	}
	
	/**
	 * Sets the current cp direct.
	 * @param newCp the new current cp direct
	 */
	public final void setCurrentCpDirect(double newCp)
	{
		setCurrentCp(newCp, true, true);
	}
	
	/**
	 * Sets the current cp.
	 * @param newCp the new current cp
	 */
	public final void setCurrentCp(double newCp)
	{
		setCurrentCp(newCp, true, false);
	}
	
	/**
	 * Sets the current cp.
	 * @param newCp the new cp
	 * @param broadcastPacket the broadcast packet
	 */
	public final void setCurrentCp(double newCp, boolean broadcastPacket)
	{
		setCurrentCp(newCp, broadcastPacket, false);
	}
	
	/**
	 * Sets the current cp.
	 * @param newCp the new cp
	 * @param broadcastPacket the broadcast packet
	 * @param direct the direct
	 */
	public final void setCurrentCp(double newCp, boolean broadcastPacket, boolean direct)
	{
		synchronized (this)
		{
			// Get the Max CP of the L2Character
			final int maxCp = _activeChar.getStat().getMaxCp();
			
			if (newCp < 0)
			{
				newCp = 0;
			}
			
			if ((newCp >= maxCp) && !direct)
			{
				// Set the RegenActive flag to false
				_currentCp = maxCp;
				_flagsRegenActive &= ~REGEN_FLAG_CP;
				
				// Stop the HP/MP/CP Regeneration task
				if (_flagsRegenActive == 0)
				{
					stopHpMpRegeneration();
				}
			}
			else
			{
				// Set the RegenActive flag to true
				_currentCp = newCp;
				_flagsRegenActive |= REGEN_FLAG_CP;
				
				// Start the HP/MP/CP Regeneration task with Medium priority
				startHpMpRegeneration();
			}
		}
		
		// Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform
		if (broadcastPacket)
		{
			_activeChar.broadcastStatusUpdate();
		}
	}
	
	/**
	 * Gets the current hp.
	 * @return the current hp
	 */
	public final double getCurrentHp()
	{
		return _currentHp;
	}
	
	/**
	 * Sets the current hp.
	 * @param newHp the new current hp
	 */
	public final void setCurrentHp(double newHp)
	{
		setCurrentHp(newHp, true);
	}
	
	/**
	 * Sets the current hp direct.
	 * @param newHp the new current hp direct
	 */
	public final void setCurrentHpDirect(double newHp)
	{
		setCurrentHp(newHp, true, true);
	}
	
	/**
	 * Sets the current mp direct.
	 * @param newMp the new current mp direct
	 */
	public final void setCurrentMpDirect(double newMp)
	{
		setCurrentMp(newMp, true, true);
	}
	
	/**
	 * Sets the current hp.
	 * @param newHp the new hp
	 * @param broadcastPacket the broadcast packet
	 */
	public final void setCurrentHp(double newHp, boolean broadcastPacket)
	{
		setCurrentHp(newHp, true, false);
	}
	
	/**
	 * Sets the current hp.
	 * @param newHp the new hp
	 * @param broadcastPacket the broadcast packet
	 * @param direct the direct
	 */
	public final void setCurrentHp(double newHp, boolean broadcastPacket, boolean direct)
	{
		synchronized (this)
		{
			// Get the Max HP of the L2Character
			final double maxHp = _activeChar.getStat().getMaxHp();
			
			if ((newHp >= maxHp) && !direct)
			{
				// Set the RegenActive flag to false
				_currentHp = maxHp;
				_flagsRegenActive &= ~REGEN_FLAG_HP;
				_activeChar.setIsKilledAlready(false);
				
				// Stop the HP/MP/CP Regeneration task
				if (_flagsRegenActive == 0)
				{
					stopHpMpRegeneration();
				}
			}
			else
			{
				// Set the RegenActive flag to true
				_currentHp = newHp;
				_flagsRegenActive |= REGEN_FLAG_HP;
				
				if (!_activeChar.isDead())
				{
					_activeChar.setIsKilledAlready(false);
				}
				
				// Start the HP/MP/CP Regeneration task with Medium priority
				startHpMpRegeneration();
			}
			
		}
		
		// Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform
		if (broadcastPacket)
		{
			_activeChar.broadcastStatusUpdate();
		}
	}
	
	/**
	 * Sets the current hp mp.
	 * @param newHp the new hp
	 * @param newMp the new mp
	 */
	public final void setCurrentHpMp(double newHp, double newMp)
	{
		setCurrentHp(newHp, false, false);
		setCurrentMp(newMp, true, false); // send the StatusUpdate only once
	}
	
	/**
	 * Gets the current mp.
	 * @return the current mp
	 */
	public final double getCurrentMp()
	{
		return _currentMp;
	}
	
	/**
	 * Sets the current mp.
	 * @param newMp the new current mp
	 */
	public final void setCurrentMp(double newMp)
	{
		setCurrentMp(newMp, true);
	}
	
	/**
	 * Sets the current mp.
	 * @param newMp the new mp
	 * @param broadcastPacket the broadcast packet
	 */
	public final void setCurrentMp(double newMp, boolean broadcastPacket)
	{
		setCurrentMp(newMp, broadcastPacket, false);
	}
	
	/**
	 * Sets the current mp.
	 * @param newMp the new mp
	 * @param broadcastPacket the broadcast packet
	 * @param direct the direct
	 */
	public final void setCurrentMp(double newMp, boolean broadcastPacket, boolean direct)
	{
		synchronized (this)
		{
			// Get the Max MP of the L2Character
			final int maxMp = _activeChar.getStat().getMaxMp();
			
			if ((newMp >= maxMp) && !direct)
			{
				// Set the RegenActive flag to false
				_currentMp = maxMp;
				_flagsRegenActive &= ~REGEN_FLAG_MP;
				
				// Stop the HP/MP/CP Regeneration task
				if (_flagsRegenActive == 0)
				{
					stopHpMpRegeneration();
				}
			}
			else
			{
				// Set the RegenActive flag to true
				_currentMp = newMp;
				_flagsRegenActive |= REGEN_FLAG_MP;
				
				// Start the HP/MP/CP Regeneration task with Medium priority
				startHpMpRegeneration();
			}
		}
		
		// Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform
		if (broadcastPacket)
		{
			_activeChar.broadcastStatusUpdate();
		}
	}
	
	/**
	 * Return the list of L2Character that must be informed of HP/MP updates of this L2Character.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * Each L2Character owns a list called <B>_statusListener</B> that contains all L2PcInstance to inform of HP/MP updates. Players who must be informed are players that target this L2Character. When a RegenTask is in progress sever just need to go through this list to send Server->Client packet
	 * StatusUpdate.<BR>
	 * <BR>
	 * @return The list of L2Character to inform or null if empty
	 */
	public final Set<L2Character> getStatusListener()
	{
		if (_StatusListener == null)
		{
			_StatusListener = new CopyOnWriteArraySet<>();
		}
		
		return _StatusListener;
	}
	
	/**
	 * Task of HP/MP/CP regeneration.
	 */
	class RegenTask implements Runnable
	{
		
		/*
		 * (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run()
		{
			try
			{
				final CharStat charstat = _activeChar.getStat();
				
				// Modify the current CP of the L2Character and broadcast Server->Client packet StatusUpdate
				if (_currentCp < charstat.getMaxCp())
				{
					setCurrentCp(_currentCp + Formulas.calcCpRegen(_activeChar), false);
				}
				
				// Modify the current HP of the L2Character and broadcast Server->Client packet StatusUpdate
				if (_currentHp < charstat.getMaxHp())
				{
					setCurrentHp(_currentHp + Formulas.calcHpRegen(_activeChar), false);
				}
				
				// Modify the current MP of the L2Character and broadcast Server->Client packet StatusUpdate
				if (_currentMp < charstat.getMaxMp())
				{
					setCurrentMp(_currentMp + Formulas.calcMpRegen(_activeChar), false);
				}
				
				if (!_activeChar.isInActiveRegion())
				{
					// no broadcast necessary for characters that are in inactive regions.
					// stop regeneration for characters who are filled up and in an inactive region.
					if ((_currentCp == charstat.getMaxCp()) && (_currentHp == charstat.getMaxHp()) && (_currentMp == charstat.getMaxMp()))
					{
						stopHpMpRegeneration();
					}
				}
				else
				{
					_activeChar.broadcastStatusUpdate(); // send the StatusUpdate packet
				}
			}
			catch (Throwable e)
			{
				LOGGER.warning("RegenTask failed for " + _activeChar.getName() + " " + e);
			}
		}
	}
}
