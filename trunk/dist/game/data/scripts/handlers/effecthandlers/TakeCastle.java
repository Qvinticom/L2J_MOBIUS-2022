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

import com.l2jmobius.gameserver.enums.CastleSide;
import com.l2jmobius.gameserver.instancemanager.CastleManager;
import com.l2jmobius.gameserver.model.StatsSet;
import com.l2jmobius.gameserver.model.actor.L2Character;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.conditions.Condition;
import com.l2jmobius.gameserver.model.effects.AbstractEffect;
import com.l2jmobius.gameserver.model.entity.Castle;
import com.l2jmobius.gameserver.model.skills.BuffInfo;

/**
 * Take Castle effect implementation.
 * @author Adry_85, St3eT
 */
public final class TakeCastle extends AbstractEffect
{
	private final CastleSide _side;
	
	public TakeCastle(Condition attachCond, Condition applyCond, StatsSet set, StatsSet params)
	{
		super(attachCond, applyCond, set, params);
		
		_side = params.getEnum("side", CastleSide.class);
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public void onStart(BuffInfo info)
	{
		if (!info.getEffector().isPlayer())
		{
			return;
		}
		
		final L2PcInstance effector = info.getEffector().getActingPlayer();
		final Castle castle = CastleManager.getInstance().getCastle(effector);
		final L2Character effected = info.getEffected();
		
		castle.engrave(effector.getClan(), effected, _side);
	}
}
