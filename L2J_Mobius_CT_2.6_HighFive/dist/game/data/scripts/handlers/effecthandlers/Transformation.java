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

import org.l2jmobius.gameserver.data.xml.TransformData;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.conditions.Condition;
import org.l2jmobius.gameserver.model.effects.AbstractEffect;
import org.l2jmobius.gameserver.model.skill.BuffInfo;

/**
 * Transformation effect implementation.
 * @author nBd
 */
public class Transformation extends AbstractEffect
{
	private final int _id;
	
	public Transformation(Condition attachCond, Condition applyCond, StatSet set, StatSet params)
	{
		super(attachCond, applyCond, set, params);
		
		_id = params.getInt("id", 0);
	}
	
	@Override
	public boolean canStart(BuffInfo info)
	{
		return info.getEffected().isPlayer();
	}
	
	@Override
	public void onExit(BuffInfo info)
	{
		info.getEffected().stopTransformation(false);
	}
	
	@Override
	public void onStart(BuffInfo info)
	{
		TransformData.getInstance().transformPlayer(_id, info.getEffected().getActingPlayer());
	}
}
