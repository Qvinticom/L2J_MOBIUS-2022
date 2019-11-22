/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.l2jmobius.gameserver.model;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import org.l2jmobius.gameserver.model.actor.Creature;

public class Potion extends WorldObject
{
	private static final Logger _log = Logger.getLogger(Potion.class.getName());
	Creature _target;
	private static Timer _regenTimer = new Timer(true);
	private PotionHpHealing _potionhpRegTask;
	// private boolean _potionhpRegenActive;
	private PotionMpHealing _potionmpRegTask;
	// private boolean _potionmpRegenActive;
	private int _seconds;
	private double _effect;
	private int _duration;
	private int _potion;
	private final Object _mpLock;
	private final Object _hpLock;
	
	public Potion()
	{
		_potionhpRegTask = new PotionHpHealing(_target);
		_potionmpRegTask = new PotionMpHealing(_target);
		_mpLock = new Object();
		_hpLock = new Object();
	}
	
	private void startPotionHpRegeneration(Creature activeChar)
	{
		_potionhpRegTask = new PotionHpHealing(activeChar);
		_regenTimer.schedule(_potionhpRegTask, 1000L, _seconds);
		// this._potionhpRegenActive = true;
	}
	
	public void stopPotionHpRegeneration()
	{
		if (_potionhpRegTask != null)
		{
			_potionhpRegTask.cancel();
		}
		_potionhpRegTask = null;
		// this._potionhpRegenActive = false;
	}
	
	public void setCurrentHpPotion2(Creature activeChar)
	{
		if (_duration == 0)
		{
			stopPotionHpRegeneration();
		}
	}
	
	public void setCurrentHpPotion1(Creature activeChar, int item)
	{
		_potion = item;
		_target = activeChar;
		switch (_potion)
		{
			case 65:
			{
				_seconds = 3000;
				_duration = 15;
				_effect = 2.0;
				startPotionHpRegeneration(activeChar);
				break;
			}
			case 725:
			{
				_seconds = 1000;
				_duration = 20;
				_effect = 1.5;
				startPotionHpRegeneration(activeChar);
				break;
			}
			case 727:
			{
				_seconds = 1000;
				_duration = 20;
				_effect = 1.5;
				startPotionHpRegeneration(activeChar);
				break;
			}
			case 1060:
			{
				_seconds = 3000;
				_duration = 15;
				_effect = 4.0;
				startPotionHpRegeneration(activeChar);
				break;
			}
			case 1061:
			{
				_seconds = 3000;
				_duration = 15;
				_effect = 14.0;
				startPotionHpRegeneration(activeChar);
				break;
			}
			case 1073:
			{
				_seconds = 3000;
				_duration = 15;
				_effect = 2.0;
				startPotionHpRegeneration(activeChar);
				break;
			}
			case 1539:
			{
				_seconds = 3000;
				_duration = 15;
				_effect = 32.0;
				startPotionHpRegeneration(activeChar);
				break;
			}
			case 1540:
			{
				double nowHp = activeChar.getCurrentHp();
				nowHp += 435.0;
				if (nowHp >= activeChar.getMaxHp())
				{
					nowHp = activeChar.getMaxHp();
				}
				activeChar.setCurrentHp(nowHp);
			}
		}
	}
	
	private void startPotionMpRegeneration(Creature activeChar)
	{
		_potionmpRegTask = new PotionMpHealing(activeChar);
		_regenTimer.schedule(_potionmpRegTask, 1000L, _seconds);
		// this._potionmpRegenActive = true;
	}
	
	public void stopPotionMpRegeneration()
	{
		if (_potionmpRegTask != null)
		{
			_potionmpRegTask.cancel();
		}
		_potionmpRegTask = null;
		// this._potionmpRegenActive = false;
	}
	
	public void setCurrentMpPotion2(Creature activeChar)
	{
		if (_duration == 0)
		{
			stopPotionMpRegeneration();
		}
	}
	
	public void setCurrentMpPotion1(Creature activeChar, int item)
	{
		_potion = item;
		_target = activeChar;
		switch (_potion)
		{
			case 726:
			{
				_seconds = 1000;
				_duration = 20;
				_effect = 1.5;
				startPotionMpRegeneration(activeChar);
				break;
			}
			case 728:
			{
				double nowMp = activeChar.getMaxMp();
				nowMp += 435.0;
				if (nowMp >= activeChar.getMaxMp())
				{
					nowMp = activeChar.getMaxMp();
				}
				activeChar.setCurrentMp(nowMp);
			}
		}
	}
	
	class PotionMpHealing extends TimerTask
	{
		Creature _instance;
		
		public PotionMpHealing(Creature instance)
		{
			_instance = instance;
		}
		
		@Override
		public void run()
		{
			try
			{
				Object object = _mpLock;
				synchronized (object)
				{
					double nowMp = _instance.getCurrentMp();
					if (_duration == 0)
					{
						stopPotionMpRegeneration();
					}
					if (_duration != 0)
					{
						_instance.setCurrentMp(nowMp += _effect);
						_duration = _duration - (_seconds / 1000);
						setCurrentMpPotion2(_instance);
					}
				}
			}
			catch (Exception e)
			{
				_log.warning("error in mp potion task:" + e);
			}
		}
	}
	
	class PotionHpHealing extends TimerTask
	{
		Creature _instance;
		
		public PotionHpHealing(Creature instance)
		{
			_instance = instance;
		}
		
		@Override
		public void run()
		{
			try
			{
				Object object = _hpLock;
				synchronized (object)
				{
					double nowHp = _instance.getCurrentHp();
					if (_duration == 0)
					{
						stopPotionHpRegeneration();
					}
					if (_duration != 0)
					{
						_instance.setCurrentHp(nowHp += _effect);
						_duration = _duration - (_seconds / 1000);
						setCurrentHpPotion2(_instance);
					}
				}
			}
			catch (Exception e)
			{
				_log.warning("Error in hp potion task:" + e);
			}
		}
	}
	
}
