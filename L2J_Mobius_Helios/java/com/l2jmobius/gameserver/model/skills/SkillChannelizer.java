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
package com.l2jmobius.gameserver.model.skills;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jmobius.gameserver.ThreadPoolManager;
import com.l2jmobius.gameserver.data.xml.impl.SkillData;
import com.l2jmobius.gameserver.enums.ShotType;
import com.l2jmobius.gameserver.geodata.GeoData;
import com.l2jmobius.gameserver.model.L2Object;
import com.l2jmobius.gameserver.model.actor.L2Character;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.SystemMessageId;
import com.l2jmobius.gameserver.network.serverpackets.MagicSkillLaunched;
import com.l2jmobius.gameserver.util.Util;

/**
 * Skill Channelizer implementation.
 * @author UnAfraid
 */
public class SkillChannelizer implements Runnable
{
	private static final Logger _log = Logger.getLogger(SkillChannelizer.class.getName());
	
	private final L2Character _channelizer;
	private List<L2Character> _channelized;
	
	private Skill _skill;
	private volatile ScheduledFuture<?> _task = null;
	
	public SkillChannelizer(L2Character channelizer)
	{
		_channelizer = channelizer;
	}
	
	public L2Character getChannelizer()
	{
		return _channelizer;
	}
	
	public List<L2Character> getChannelized()
	{
		return _channelized;
	}
	
	public boolean hasChannelized()
	{
		return _channelized != null;
	}
	
	public void startChanneling(Skill skill)
	{
		// Verify for same status.
		if (isChanneling())
		{
			_log.warning("Character: " + toString() + " is attempting to channel skill but he already does!");
			return;
		}
		
		// Start channeling.
		_skill = skill;
		_task = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(this, skill.getChannelingTickInitialDelay(), skill.getChannelingTickInterval());
	}
	
	public void stopChanneling()
	{
		// Verify for same status.
		if (!isChanneling())
		{
			_log.warning("Character: " + toString() + " is attempting to stop channel skill but he does not!");
			return;
		}
		
		// Cancel the task and unset it.
		_task.cancel(false);
		_task = null;
		
		// Cancel target channelization and unset it.
		if (_channelized != null)
		{
			for (L2Character chars : _channelized)
			{
				chars.getSkillChannelized().removeChannelizer(_skill.getChannelingSkillId(), getChannelizer());
			}
			_channelized = null;
		}
		
		// unset skill.
		_skill = null;
	}
	
	public Skill getSkill()
	{
		return _skill;
	}
	
	public boolean isChanneling()
	{
		return _task != null;
	}
	
	@Override
	public void run()
	{
		if (!isChanneling())
		{
			return;
		}
		
		final Skill skill = _skill;
		List<L2Character> channelized = _channelized;
		
		try
		{
			if (skill.getMpPerChanneling() > 0)
			{
				// Validate mana per tick.
				if (_channelizer.getCurrentMp() < skill.getMpPerChanneling())
				{
					if (_channelizer.isPlayer())
					{
						_channelizer.sendPacket(SystemMessageId.YOUR_SKILL_WAS_DEACTIVATED_DUE_TO_LACK_OF_MP);
					}
					_channelizer.abortCast();
					return;
				}
				
				// Reduce mana per tick
				_channelizer.reduceCurrentMp(skill.getMpPerChanneling());
			}
			
			// Apply channeling skills on the targets.
			if (skill.getChannelingSkillId() > 0)
			{
				final Skill baseSkill = SkillData.getInstance().getSkill(skill.getChannelingSkillId(), 1);
				if (baseSkill == null)
				{
					_log.warning(getClass().getSimpleName() + ": skill " + skill + " couldn't find effect id skill: " + skill.getChannelingSkillId() + " !");
					_channelizer.abortCast();
					return;
				}
				
				final List<L2Character> targetList = new ArrayList<>();
				final L2Object target = skill.getTarget(_channelizer, false, false, false);
				if (target != null)
				{
					skill.forEachTargetAffected(_channelizer, target, o ->
					{
						if (o.isCharacter())
						{
							targetList.add((L2Character) o);
							((L2Character) o).getSkillChannelized().addChannelizer(skill.getChannelingSkillId(), getChannelizer());
						}
					});
				}
				
				if (targetList.isEmpty())
				{
					return;
				}
				channelized = targetList;
				
				for (L2Character character : channelized)
				{
					if (!Util.checkIfInRange(skill.getEffectRange(), _channelizer, character, true))
					{
						continue;
					}
					else if (!GeoData.getInstance().canSeeTarget(_channelizer, character))
					{
						continue;
					}
					else
					{
						final int maxSkillLevel = SkillData.getInstance().getMaxLevel(skill.getChannelingSkillId());
						final int skillLevel = Math.min(character.getSkillChannelized().getChannerlizersSize(skill.getChannelingSkillId()), maxSkillLevel);
						final BuffInfo info = character.getEffectList().getBuffInfoBySkillId(skill.getChannelingSkillId());
						
						if ((info == null) || (info.getSkill().getLevel() < skillLevel))
						{
							final Skill channeledSkill = SkillData.getInstance().getSkill(skill.getChannelingSkillId(), skillLevel);
							if (channeledSkill == null)
							{
								_log.warning(getClass().getSimpleName() + ": Non existent channeling skill requested: " + skill);
								_channelizer.abortCast();
								return;
							}
							
							// Update PvP status
							if (character.isPlayable() && getChannelizer().isPlayer())
							{
								((L2PcInstance) getChannelizer()).updatePvPStatus(character);
							}
							
							// Be warned, this method has the possibility to call doDie->abortCast->stopChanneling method. Variable cache above try"+ +" is used in this case to avoid NPEs.
							channeledSkill.applyEffects(getChannelizer(), character);
							
							// Reduce shots.
							if (skill.useSpiritShot())
							{
								_channelizer.setChargedShot(_channelizer.isChargedShot(ShotType.BLESSED_SPIRITSHOTS) ? ShotType.BLESSED_SPIRITSHOTS : ShotType.SPIRITSHOTS, false);
							}
							else
							{
								_channelizer.setChargedShot(_channelizer.isChargedShot(ShotType.BLESSED_SOULSHOTS) ? ShotType.BLESSED_SOULSHOTS : ShotType.SOULSHOTS, false);
							}
							
							// Shots are re-charged every cast.
							_channelizer.rechargeShots(skill.useSoulShot(), skill.useSpiritShot(), false);
						}
						if (!skill.isToggle())
						{
							_channelizer.broadcastPacket(new MagicSkillLaunched(_channelizer, skill.getId(), skill.getLevel(), SkillCastingType.NORMAL, character));
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Error while channelizing skill: " + skill + " channelizer: " + _channelizer + " channelized: " + channelized, e);
		}
	}
}
