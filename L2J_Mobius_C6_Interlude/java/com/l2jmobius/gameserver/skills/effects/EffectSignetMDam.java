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

import java.util.ArrayList;
import java.util.List;

import com.l2jmobius.commons.util.Point3D;
import com.l2jmobius.gameserver.ai.CtrlEvent;
import com.l2jmobius.gameserver.datatables.sql.NpcTable;
import com.l2jmobius.gameserver.idfactory.IdFactory;
import com.l2jmobius.gameserver.model.L2Effect;
import com.l2jmobius.gameserver.model.L2Skill;
import com.l2jmobius.gameserver.model.L2World;
import com.l2jmobius.gameserver.model.actor.L2Attackable;
import com.l2jmobius.gameserver.model.actor.L2Character;
import com.l2jmobius.gameserver.model.actor.L2Playable;
import com.l2jmobius.gameserver.model.actor.L2Summon;
import com.l2jmobius.gameserver.model.actor.instance.L2EffectPointInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.SystemMessageId;
import com.l2jmobius.gameserver.network.serverpackets.MagicSkillLaunched;
import com.l2jmobius.gameserver.skills.Env;
import com.l2jmobius.gameserver.skills.Formulas;
import com.l2jmobius.gameserver.skills.l2skills.L2SkillSignetCasttime;
import com.l2jmobius.gameserver.templates.chars.L2NpcTemplate;

/**
 * @author Shyla
 */
public final class EffectSignetMDam extends L2Effect
{
	private L2EffectPointInstance _actor;
	private boolean bss;
	private boolean sps;
	
	// private SigmetMDAMTask skill_task;
	
	public EffectSignetMDam(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.SIGNET_GROUND;
	}
	
	@Override
	public void onStart()
	{
		L2NpcTemplate template;
		if (getSkill() instanceof L2SkillSignetCasttime)
		{
			template = NpcTable.getInstance().getTemplate(((L2SkillSignetCasttime) getSkill())._effectNpcId);
		}
		else
		{
			return;
		}
		
		final L2EffectPointInstance effectPoint = new L2EffectPointInstance(IdFactory.getInstance().getNextId(), template, getEffector());
		effectPoint.getStatus().setCurrentHp(effectPoint.getMaxHp());
		effectPoint.getStatus().setCurrentMp(effectPoint.getMaxMp());
		
		L2World.getInstance().storeObject(effectPoint);
		
		int x = getEffector().getX();
		int y = getEffector().getY();
		int z = getEffector().getZ();
		
		if ((getEffector() instanceof L2PcInstance) && (getSkill().getTargetType() == L2Skill.SkillTargetType.TARGET_GROUND))
		{
			final Point3D wordPosition = ((L2PcInstance) getEffector()).getCurrentSkillWorldPosition();
			
			if (wordPosition != null)
			{
				x = wordPosition.getX();
				y = wordPosition.getY();
				z = wordPosition.getZ();
			}
		}
		effectPoint.setIsInvul(true);
		effectPoint.spawnMe(x, y, z);
		
		_actor = effectPoint;
		
		// skill_task = new SigmetMDAMTask();
	}
	
	@Override
	public boolean onActionTime()
	{
		if (getCount() >= (getTotalCount() - 2))
		{
			return true; // do nothing first 2 times
		}
		
		final int mpConsume = getSkill().getMpConsume();
		final L2PcInstance caster = (L2PcInstance) getEffector();
		
		sps = caster.checkSps();
		bss = caster.checkBss();
		
		final List<L2Character> targets = new ArrayList<>();
		
		for (L2Character cha : _actor.getKnownList().getKnownCharactersInRadius(getSkill().getSkillRadius()))
		{
			if ((cha == null) || (cha == caster))
			{
				continue;
			}
			
			if ((cha instanceof L2Attackable) || (cha instanceof L2Playable))
			{
				if (cha.isAlikeDead())
				{
					continue;
				}
				
				if (mpConsume > caster.getStatus().getCurrentMp())
				{
					caster.sendPacket(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP);
					return false;
				}
				
				caster.reduceCurrentMp(mpConsume);
				
				if (cha instanceof L2Playable)
				{
					if ((!(cha instanceof L2Summon) || (((L2Summon) cha).getOwner() != caster)))
					{
						caster.updatePvPStatus(cha);
					}
				}
				
				targets.add(cha);
			}
		}
		
		if (!targets.isEmpty())
		{
			caster.broadcastPacket(new MagicSkillLaunched(caster, getSkill().getDisplayId(), getSkill().getLevel(), targets.toArray(new L2Character[targets.size()])));
			for (L2Character target : targets)
			{
				final boolean mcrit = Formulas.calcMCrit(caster.getMCriticalHit(target, getSkill()));
				final int mdam = (int) Formulas.calcMagicDam(caster, target, getSkill(), sps, bss, mcrit);
				
				if (target instanceof L2Summon)
				{
					target.broadcastStatusUpdate();
				}
				
				if (mdam > 0)
				{
					if (!target.isRaid() && Formulas.calcAtkBreak(target, mdam))
					{
						target.breakAttack();
						target.breakCast();
					}
					caster.sendDamageMessage(target, mdam, mcrit, false, false);
					target.reduceCurrentHp(mdam, caster);
				}
				target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, caster);
			}
		}
		
		return true;
	}
	
	@Override
	public void onExit()
	{
		if (_actor != null)
		{
			final L2PcInstance caster = (L2PcInstance) getEffector();
			
			// remove shots
			if (bss)
			{
				caster.removeBss();
				
			}
			else if (sps)
			{
				caster.removeSps();
			}
			_actor.deleteMe();
		}
	}
	
}
