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
import org.l2jmobius.gameserver.model.skill.BuffInfo;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.network.serverpackets.UserInfo;

/**
 * Give Recommendation effect implementation.
 * @author NosBit
 */
public class GiveRecommendation extends AbstractEffect
{
	private final int _amount;
	
	public GiveRecommendation(Condition attachCond, Condition applyCond, StatSet set, StatSet params)
	{
		super(attachCond, applyCond, set, params);
		
		_amount = params.getInt("amount", 0);
		if (_amount == 0)
		{
			LOGGER.warning(getClass().getSimpleName() + ": amount parameter is missing or set to 0. id:" + set.getInt("id", -1));
		}
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public void onStart(BuffInfo info)
	{
		final Player target = (info.getEffected() != null) && info.getEffected().isPlayer() ? (Player) info.getEffected() : null;
		if (target != null)
		{
			int recommendationsGiven = _amount;
			if ((target.getRecomHave() + _amount) >= 255)
			{
				recommendationsGiven = 255 - target.getRecomHave();
			}
			
			if (recommendationsGiven > 0)
			{
				target.setRecomHave(target.getRecomHave() + recommendationsGiven);
				
				final SystemMessage sm = new SystemMessage(SystemMessageId.YOU_OBTAINED_S1_RECOMMENDATION_S);
				sm.addInt(recommendationsGiven);
				target.sendPacket(sm);
				target.sendPacket(new UserInfo(target));
			}
			else
			{
				final Player player = (info.getEffector() != null) && info.getEffector().isPlayer() ? (Player) info.getEffector() : null;
				if (player != null)
				{
					player.sendPacket(SystemMessageId.NOTHING_HAPPENED);
				}
			}
		}
	}
}
