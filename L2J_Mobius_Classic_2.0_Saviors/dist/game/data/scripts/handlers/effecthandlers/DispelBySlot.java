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
import java.util.EnumMap;
import java.util.Map;

import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.effects.AbstractEffect;
import org.l2jmobius.gameserver.model.effects.EffectType;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.skills.AbnormalType;
import org.l2jmobius.gameserver.model.skills.Skill;

/**
 * Dispel By Slot effect implementation.
 * @author Gnacik, Zoey76, Adry_85
 */
public class DispelBySlot extends AbstractEffect
{
	private final String _dispel;
	private final Map<AbnormalType, Short> _dispelAbnormals;
	
	public DispelBySlot(StatSet params)
	{
		_dispel = params.getString("dispel");
		if ((_dispel != null) && !_dispel.isEmpty())
		{
			_dispelAbnormals = new EnumMap<>(AbnormalType.class);
			for (String ngtStack : _dispel.split(";"))
			{
				final String[] ngt = ngtStack.split(",");
				_dispelAbnormals.put(AbnormalType.getAbnormalType(ngt[0]), Short.parseShort(ngt[1]));
			}
		}
		else
		{
			_dispelAbnormals = Collections.<AbnormalType, Short> emptyMap();
		}
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.DISPEL_BY_SLOT;
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public void instant(Creature effector, Creature effected, Skill skill, Item item)
	{
		if (_dispelAbnormals.isEmpty())
		{
			return;
		}
		
		// Continue only if target has any of the abnormals. Save useless cycles.
		if (effected.getEffectList().hasAbnormalType(_dispelAbnormals.keySet()))
		{
			// Dispel transformations (buff and by GM)
			final Short transformToDispel = _dispelAbnormals.get(AbnormalType.TRANSFORM);
			if ((transformToDispel != null) && ((transformToDispel.intValue() == effected.getTransformationId()) || (transformToDispel.intValue() < 0)))
			{
				effected.stopTransformation(true);
			}
			
			effected.getEffectList().stopEffects(info ->
			{
				// We have already dealt with transformation from above.
				if (info.isAbnormalType(AbnormalType.TRANSFORM))
				{
					return false;
				}
				
				final Short abnormalLevel = _dispelAbnormals.get(info.getSkill().getAbnormalType());
				return (abnormalLevel != null) && ((abnormalLevel.shortValue() < 0) || (abnormalLevel.shortValue() >= info.getSkill().getAbnormalLevel()));
			}, true, true);
		}
	}
}
