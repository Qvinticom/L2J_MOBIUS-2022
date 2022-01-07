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
package org.l2jmobius.gameserver.model.skill.effects;

import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.model.Effect;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.skill.Env;
import org.l2jmobius.gameserver.model.skill.SkillType;
import org.l2jmobius.gameserver.model.skill.Stat;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

final class EffectCancel extends Effect
{
	public EffectCancel(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.CANCEL;
	}
	
	@Override
	public void onStart()
	{
		final int landrate = (int) getEffector().calcStat(Stat.CANCEL_VULN, 90, getEffected(), null);
		if (Rnd.get(100) < landrate)
		{
			int maxdisp = (int) getSkill().getNegatePower();
			if (maxdisp == 0)
			{
				maxdisp = 5;
			}
			for (Effect effect : getEffected().getAllEffects())
			{
				switch (effect.getEffectType())
				{
					case SIGNET_GROUND:
					case SIGNET_EFFECT:
					{
						continue;
					}
				}
				
				if ((effect.getSkill().getId() != 4082) && (effect.getSkill().getId() != 4215) && (effect.getSkill().getId() != 5182) && (effect.getSkill().getId() != 4515) && (effect.getSkill().getId() != 110) && (effect.getSkill().getId() != 111) && (effect.getSkill().getId() != 1323) && (effect.getSkill().getId() != 1325) && (effect.getSkill().getSkillType() == SkillType.BUFF))
				{
					// TODO Fix cancel debuffs
					if (effect.getSkill().getSkillType() != SkillType.DEBUFF)
					{
						int rate = 100;
						final int level = effect.getLevel();
						if (level > 0)
						{
							rate = 150 / (1 + level);
						}
						
						if (rate > 95)
						{
							rate = 95;
						}
						else if (rate < 5)
						{
							rate = 5;
						}
						
						if (Rnd.get(100) < rate)
						{
							effect.exit(true);
							maxdisp--;
							if (maxdisp == 0)
							{
								break;
							}
						}
					}
				}
			}
		}
		else if (getEffector() instanceof Player)
		{
			final SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_RESISTED_YOUR_S2);
			sm.addString(getEffected().getName());
			sm.addSkillName(getSkill().getDisplayId());
			getEffector().sendPacket(sm);
		}
	}
	
	@Override
	public void onExit()
	{
	}
	
	@Override
	public boolean onActionTime()
	{
		return false;
	}
}
