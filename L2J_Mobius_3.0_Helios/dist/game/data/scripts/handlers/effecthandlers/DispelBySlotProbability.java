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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.l2jmobius.commons.util.Rnd;
import com.l2jmobius.gameserver.model.StatsSet;
import com.l2jmobius.gameserver.model.actor.L2Character;
import com.l2jmobius.gameserver.model.effects.AbstractEffect;
import com.l2jmobius.gameserver.model.effects.L2EffectType;
import com.l2jmobius.gameserver.model.items.instance.L2ItemInstance;
import com.l2jmobius.gameserver.model.skills.AbnormalType;
import com.l2jmobius.gameserver.model.skills.Skill;

/**
 * Dispel By Slot Probability effect implementation.
 * @author Adry_85, Zoey76
 */
public final class DispelBySlotProbability extends AbstractEffect
{
	private final String _dispel;
	private final Map<AbnormalType, Short> _dispelAbnormals;
	private final int _rate;
	
	public DispelBySlotProbability(StatsSet params)
	{
		_dispel = params.getString("dispel");
		_rate = params.getInt("rate", 100);
		if ((_dispel != null) && !_dispel.isEmpty())
		{
			_dispelAbnormals = new HashMap<>();
			for (String ngtStack : _dispel.split(";"))
			{
				String[] ngt = ngtStack.split(",");
				_dispelAbnormals.put(AbnormalType.getAbnormalType(ngt[0]), Short.MAX_VALUE);
			}
		}
		else
		{
			_dispelAbnormals = Collections.<AbnormalType, Short> emptyMap();
		}
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.DISPEL;
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public void instant(L2Character effector, L2Character effected, Skill skill, L2ItemInstance item)
	{
		if (_dispelAbnormals.isEmpty())
		{
			return;
		}
		
		// Dispel transformations (buff and by GM)
		if ((Rnd.get(100) < _rate))
		{
			final Short transformToDispel = _dispelAbnormals.get(AbnormalType.TRANSFORM);
			if ((transformToDispel != null) && ((transformToDispel == effected.getTransformationId()) || (transformToDispel < 0)))
			{
				effected.stopTransformation(true);
			}
		}
		
		effected.getEffectList().stopEffects(info ->
		{
			// We have already dealt with transformation from above.
			if (info.isAbnormalType(AbnormalType.TRANSFORM))
			{
				return false;
			}
			
			final Short abnormalLevel = (Rnd.get(100) < _rate) ? _dispelAbnormals.get(info.getSkill().getAbnormalType()) : null;
			return (abnormalLevel != null) && ((abnormalLevel < 0) || (abnormalLevel >= info.getSkill().getAbnormalLvl()));
		}, true);
	}
}
