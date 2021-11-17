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
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.conditions.Condition;
import org.l2jmobius.gameserver.model.effects.AbstractEffect;
import org.l2jmobius.gameserver.model.skills.BuffInfo;

/**
 * Summon Agathion effect implementation.
 * @author Zoey76
 */
public class SummonAgathion extends AbstractEffect
{
	private final int _npcId;
	
	public SummonAgathion(Condition attachCond, Condition applyCond, StatSet set, StatSet params)
	{
		super(attachCond, applyCond, set, params);
		
		if (params.isEmpty())
		{
			LOGGER.warning(getClass().getSimpleName() + ": must have parameters.");
		}
		
		_npcId = params.getInt("npcId", 0);
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public void onStart(BuffInfo info)
	{
		if ((info.getEffected() == null) || !info.getEffected().isPlayer())
		{
			return;
		}
		
		final Player player = info.getEffected().getActingPlayer();
		player.setAgathionId(_npcId);
		player.broadcastUserInfo();
	}
}
