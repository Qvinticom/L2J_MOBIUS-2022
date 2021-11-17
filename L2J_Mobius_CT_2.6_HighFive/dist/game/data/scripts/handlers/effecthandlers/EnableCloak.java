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
import org.l2jmobius.gameserver.model.conditions.Condition;
import org.l2jmobius.gameserver.model.effects.AbstractEffect;
import org.l2jmobius.gameserver.model.skill.BuffInfo;

/**
 * Enable Cloak effect implementation.
 * @author Adry_85
 */
public class EnableCloak extends AbstractEffect
{
	public EnableCloak(Condition attachCond, Condition applyCond, StatSet set, StatSet params)
	{
		super(attachCond, applyCond, set, params);
	}
	
	@Override
	public boolean canStart(BuffInfo info)
	{
		return (info.getEffector() != null) && (info.getEffected() != null) && info.getEffected().isPlayer();
	}
	
	@Override
	public void onStart(BuffInfo info)
	{
		info.getEffected().getActingPlayer().getStat().setCloakSlotStatus(true);
	}
	
	@Override
	public boolean onActionTime(BuffInfo info)
	{
		return info.getSkill().isPassive();
	}
	
	@Override
	public void onExit(BuffInfo info)
	{
		info.getEffected().getActingPlayer().getStat().setCloakSlotStatus(false);
	}
}
