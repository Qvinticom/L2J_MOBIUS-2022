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
package com.l2jmobius.gameserver.model.zone.type;

import java.util.concurrent.Future;
import java.util.logging.Logger;

import com.l2jmobius.commons.concurrent.ThreadPool;
import com.l2jmobius.commons.util.Rnd;
import com.l2jmobius.gameserver.datatables.SkillTable;
import com.l2jmobius.gameserver.model.L2Skill;
import com.l2jmobius.gameserver.model.actor.L2Character;
import com.l2jmobius.gameserver.model.actor.L2Playable;
import com.l2jmobius.gameserver.model.actor.instance.L2MonsterInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.zone.L2ZoneType;

public class L2PoisonZone extends L2ZoneType
{
	protected final Logger LOGGER = Logger.getLogger(L2PoisonZone.class.getName());
	protected int _skillId;
	private int _chance;
	private int _initialDelay;
	protected int _skillLvl;
	private int _reuse;
	private boolean _enabled;
	private String _target;
	private Future<?> _task;
	
	public L2PoisonZone(int id)
	{
		super(id);
		_skillId = 4070;
		_skillLvl = 1;
		_chance = 100;
		_initialDelay = 0;
		_reuse = 30000;
		_enabled = true;
		_target = "pc";
	}
	
	@Override
	public void setParameter(String name, String value)
	{
		switch (name)
		{
			case "skillId":
			{
				_skillId = Integer.parseInt(value);
				break;
			}
			case "skillLvl":
			{
				_skillLvl = Integer.parseInt(value);
				break;
			}
			case "chance":
			{
				_chance = Integer.parseInt(value);
				break;
			}
			case "initialDelay":
			{
				_initialDelay = Integer.parseInt(value);
				break;
			}
			case "default_enabled":
			{
				_enabled = Boolean.parseBoolean(value);
				break;
			}
			case "target":
			{
				_target = value;
				break;
			}
			case "reuse":
			{
				_reuse = Integer.parseInt(value);
				break;
			}
			default:
			{
				super.setParameter(name, value);
				break;
			}
		}
	}
	
	@Override
	protected void onEnter(L2Character character)
	{
		if ((((character instanceof L2Playable) && _target.equalsIgnoreCase("pc")) || ((character instanceof L2PcInstance) && _target.equalsIgnoreCase("pc_only")) || ((character instanceof L2MonsterInstance) && _target.equalsIgnoreCase("npc"))) && (_task == null))
		{
			_task = ThreadPool.scheduleAtFixedRate(new ApplySkill(/* this */), _initialDelay, _reuse);
		}
	}
	
	@Override
	protected void onExit(L2Character character)
	{
		if (_characterList.isEmpty() && (_task != null))
		{
			_task.cancel(true);
			_task = null;
		}
	}
	
	public L2Skill getSkill()
	{
		return SkillTable.getInstance().getInfo(_skillId, _skillLvl);
	}
	
	public String getTargetType()
	{
		return _target;
	}
	
	public boolean isEnabled()
	{
		return _enabled;
	}
	
	public int getChance()
	{
		return _chance;
	}
	
	public void setZoneEnabled(boolean val)
	{
		_enabled = val;
	}
	
	/*
	 * protected Collection getCharacterList() { return _characterList.values(); }
	 */
	
	class ApplySkill implements Runnable
	{
		// private L2PoisonZone _poisonZone;
		
		// ApplySkill(/*L2PoisonZone zone*/)
		// {
		// _poisonZone = zone;
		// }
		
		@Override
		public void run()
		{
			if (isEnabled())
			{
				for (L2Character temp : _characterList.values())
				{
					if ((temp != null) && !temp.isDead())
					{
						if ((((temp instanceof L2Playable) && getTargetType().equalsIgnoreCase("pc")) || ((temp instanceof L2PcInstance) && getTargetType().equalsIgnoreCase("pc_only")) || ((temp instanceof L2MonsterInstance) && getTargetType().equalsIgnoreCase("npc"))) && (Rnd.get(100) < getChance()))
						{
							L2Skill skill = getSkill();
							if (skill == null)
							{
								LOGGER.warning("ATTENTION: error on zone with id " + getId());
								LOGGER.warning("Skill " + _skillId + "," + _skillLvl + " not present between skills");
							}
							else
							{
								skill.getEffects(temp, temp, false, false, false);
							}
						}
					}
				}
			}
		}
	}
	
	@Override
	public void onDieInside(L2Character l2character)
	{
	}
	
	@Override
	public void onReviveInside(L2Character l2character)
	{
	}
}
