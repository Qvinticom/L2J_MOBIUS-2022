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
package handlers.effecthandlers;

import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.effects.AbstractEffect;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.skill.BuffInfo;
import org.l2jmobius.gameserver.model.skill.Skill;

/**
 * @author Ofelin
 */
public class LimitSkill extends AbstractEffect
{
	private static final int LIMIT_OF_AEORE = 11833;
	private static final int LIMIT_OF_SIGEL = 19526;
	private static final int LIMIT_OF_ISS = 19527;
	private static final int BATTLE_RAPSODY = 11544;
	private static final int OVERLORDS_DIGNITY = 19439;
	private static final int PROTECTION_OF_FATE = 10019;
	private static final int NINE_AEGIS = 10024;
	private static final int CELESTIAL_PROTECTION = 11758;
	private static final int CELESTIAL_PARTY_PROTECTION = 11759;
	
	public LimitSkill(StatSet params)
	{
	}
	
	@Override
	public void onStart(Creature effector, Creature effected, Skill skill, Item item)
	{
		switch (skill.getId())
		{
			case LIMIT_OF_AEORE: // Limit of Aeore
			{
				decreaseAeoreBuffDuration(effector, effected, skill);
				break;
			}
			case LIMIT_OF_SIGEL: // Limit of Sigel
			{
				decreaseSigelBuffDuration(effector, effected, skill);
				break;
			}
			case LIMIT_OF_ISS: // Limit of Iss
			{
				decreaseIssBuffDuration(effector, effected, skill);
				break;
			}
		}
	}
	
	private void decreaseAeoreBuffDuration(Creature effector, Creature effected, Skill skill)
	{
		switch (skill.getLevel())
		{
			case 1:
			case 2:
			{
				modifyDuration(CELESTIAL_PROTECTION, effected, (int) (10 * 0.50)); // Decrease active effect of Celestial Protection by 50%
				modifyDuration(CELESTIAL_PARTY_PROTECTION, effected, (int) (10 * 0.50)); // Decrease active effect of Celestial Party Protection by 50%
				break;
			}
		}
	}
	
	private void decreaseSigelBuffDuration(Creature effector, Creature effected, Skill skill)
	{
		switch (skill.getLevel())
		{
			case 1:
			{
				modifyDuration(PROTECTION_OF_FATE, effected, (int) (30 * 0.80)); // Decrease active effect of Protection of Fate by 20%
				modifyDuration(NINE_AEGIS, effected, (int) (30 * 0.80)); // Decrease active effect of Nine Aegis by 20%
				break;
			}
			case 2:
			{
				modifyDuration(PROTECTION_OF_FATE, effected, (int) (30 * 0.20)); // Decrease active effect of Protection of Fate by 80%
				modifyDuration(NINE_AEGIS, effected, (int) (30 * 0.20)); // Decrease active effect of Nine Aegis by 80%
				break;
			}
		}
	}
	
	private void decreaseIssBuffDuration(Creature effector, Creature effected, Skill skill)
	{
		switch (skill.getLevel())
		{
			case 1:
			{
				modifyDuration(BATTLE_RAPSODY, effected, (int) (30 * 0.80)); // Decrease active effect of Battle Rhapsody by 20%
				modifyDuration(OVERLORDS_DIGNITY, effected, (int) (30 * 0.80)); // Decrease active effect of Overlord's Dignity by 20%
				break;
			}
			case 2:
			{
				modifyDuration(BATTLE_RAPSODY, effected, (int) (30 * 0.20)); // Decrease active effect of Battle Rhapsody by 80%
				modifyDuration(OVERLORDS_DIGNITY, effected, (int) (30 * 0.20)); // Decrease active effect of Overlord's Dignity by 80%
				break;
			}
		}
	}
	
	private void modifyDuration(int skillId, Creature effected, int duration)
	{
		for (BuffInfo buff : effected.getEffectList().getEffects())
		{
			if ((buff.getSkill().getId() == skillId) && (duration > 0))
			{
				buff.setAbnormalTime(duration);
			}
		}
	}
}
