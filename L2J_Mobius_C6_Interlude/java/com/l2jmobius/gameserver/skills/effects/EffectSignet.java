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
package com.l2jmobius.gameserver.skills.effects;

import com.l2jmobius.gameserver.datatables.SkillTable;
import com.l2jmobius.gameserver.model.L2Effect;
import com.l2jmobius.gameserver.model.L2Skill;
import com.l2jmobius.gameserver.model.actor.L2Character;
import com.l2jmobius.gameserver.model.actor.instance.L2EffectPointInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.SystemMessageId;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import com.l2jmobius.gameserver.skills.Env;
import com.l2jmobius.gameserver.skills.l2skills.L2SkillSignet;
import com.l2jmobius.gameserver.skills.l2skills.L2SkillSignetCasttime;

public final class EffectSignet extends L2Effect
{
	private L2Skill _skill;
	private L2EffectPointInstance _actor;
	
	public EffectSignet(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.SIGNET_EFFECT;
	}
	
	@Override
	public void onStart()
	{
		if (getSkill() instanceof L2SkillSignet)
		{
			_skill = SkillTable.getInstance().getInfo(((L2SkillSignet) getSkill()).effectId, getLevel());
		}
		else if (getSkill() instanceof L2SkillSignetCasttime)
		{
			_skill = SkillTable.getInstance().getInfo(((L2SkillSignetCasttime) getSkill()).effectId, getLevel());
		}
		_actor = (L2EffectPointInstance) getEffected();
	}
	
	@Override
	public boolean onActionTime()
	{
		if (_skill == null)
		{
			return true;
		}
		final int mpConsume = _skill.getMpConsume();
		final L2PcInstance caster = (L2PcInstance) getEffector();
		
		if (mpConsume > getEffector().getStatus().getCurrentMp())
		{
			getEffector().sendPacket(new SystemMessage(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP));
			return false;
		}
		
		getEffector().reduceCurrentMp(mpConsume);
		
		for (L2Character cha : _actor.getKnownList().getKnownCharactersInRadius(getSkill().getSkillRadius()))
		{
			if ((cha == null) || (cha == caster) || cha.isDead())
			{
				continue;
			}
			
			if (_skill.isOffensive())
			{
				if (cha instanceof L2PcInstance)
				{
					
					if (((((L2PcInstance) cha).getClanId() > 0) && (caster.getClanId() > 0) && (((L2PcInstance) cha).getClanId() != caster.getClanId())) || ((((L2PcInstance) cha).getAllyId() > 0) && (caster.getAllyId() > 0) && (((L2PcInstance) cha).getAllyId() != caster.getAllyId())) || ((cha.getParty() != null) && (caster.getParty() != null) && !cha.getParty().equals(caster.getParty())))
					{
						_skill.getEffects(_actor, cha, false, false, false);
						continue;
					}
				}
			}
			else if (cha instanceof L2PcInstance)
			{
				if (((cha.getParty() != null) && (caster.getParty() != null) && cha.getParty().equals(caster.getParty())) || ((((L2PcInstance) cha).getClanId() > 0) && (caster.getClanId() > 0) && (((L2PcInstance) cha).getClanId() == caster.getClanId())) || ((((L2PcInstance) cha).getAllyId() > 0) && (caster.getAllyId() > 0) && (((L2PcInstance) cha).getAllyId() == caster.getAllyId())))
				{
					_skill.getEffects(_actor, cha, false, false, false);
					_skill.getEffects(_actor, caster, false, false, false); // Affect caster too.
					continue;
				}
			}
		}
		return true;
	}
	
	@Override
	public void onExit()
	{
		if (_actor != null)
		{
			_actor.deleteMe();
		}
	}
}