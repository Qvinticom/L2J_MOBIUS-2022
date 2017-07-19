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

import com.l2jmobius.gameserver.ThreadPoolManager;
import com.l2jmobius.gameserver.datatables.SkillTable;
import com.l2jmobius.gameserver.model.L2Character;
import com.l2jmobius.gameserver.model.L2Skill;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.zone.L2ZoneType;
import com.l2jmobius.gameserver.network.serverpackets.EtcStatusUpdate;

/**
 * Effect zones give entering players a special effect
 * @author durgus
 */
public class L2EffectZone extends L2ZoneType
{
	private int _initialDelay;
	private int _reuse;
	private boolean _enabled;
	private int _skillId;
	private int _skillLevel;
	private Future<?> _task;
	
	public L2EffectZone(int id)
	{
		super(id);
		_initialDelay = 0;
		_reuse = 30000;
		_enabled = true;
		_skillLevel = 1;
	}
	
	@Override
	public void setParameter(String name, String value)
	{
		if (name.equals("InitialDelay"))
		{
			_initialDelay = Integer.parseInt(value);
		}
		else if (name.equals("Reuse"))
		{
			_reuse = Integer.parseInt(value);
		}
		else if (name.equals("EnabledByDefault"))
		{
			_enabled = Boolean.parseBoolean(value);
		}
		else if (name.equals("SkillId"))
		{
			_skillId = Integer.parseInt(value);
		}
		else if (name.equals("SkillLevel"))
		{
			_skillLevel = Integer.parseInt(value);
		}
		else
		{
			super.setParameter(name, value);
		}
	}
	
	@Override
	protected void onEnter(L2Character character)
	{
		if (character instanceof L2PcInstance)
		{
			if (!_enabled)
			{
				return;
			}
			
			if (_task == null)
			{
				synchronized (this)
				{
					if (_task == null)
					{
						_task = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new ApplySkill(_skillId, _skillLevel), _initialDelay, _reuse);
					}
				}
			}
			
			character.setInsideZone(L2Character.ZONE_EFFECT, true);
			character.sendPacket(new EtcStatusUpdate((L2PcInstance) character));
		}
	}
	
	@Override
	protected void onExit(L2Character character)
	{
		if (_characterList.isEmpty() && (_task != null))
		{
			_task.cancel(false);
			_task = null;
		}
		
		if (character instanceof L2PcInstance)
		{
			if (!_enabled)
			{
				return;
			}
			
			character.setInsideZone(L2Character.ZONE_EFFECT, false);
			character.sendPacket(new EtcStatusUpdate((L2PcInstance) character));
		}
	}
	
	private class ApplySkill implements Runnable
	{
		private final int _effectId;
		private final int _effectLevel;
		
		ApplySkill(int skillId, int skillLevel)
		{
			_effectId = skillId;
			_effectLevel = skillLevel;
		}
		
		@Override
		public void run()
		{
			for (final L2Character temp : L2EffectZone.this._characterList.values())
			{
				if (temp == null)
				{
					continue;
				}
				
				final L2Skill skill = SkillTable.getInstance().getInfo(_effectId, _effectLevel);
				if (skill != null)
				{
					skill.getEffects(temp, temp, false);
				}
			}
		}
	}
}