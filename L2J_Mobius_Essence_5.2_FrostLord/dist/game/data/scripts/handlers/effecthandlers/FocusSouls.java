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

import org.l2jmobius.gameserver.enums.SoulType;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.effects.AbstractEffect;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.model.stats.Stat;
import org.l2jmobius.gameserver.network.SystemMessageId;

/**
 * Focus Souls effect implementation.
 * @author nBd, Adry_85
 */
public class FocusSouls extends AbstractEffect
{
	private final int _charge;
	private final SoulType _type;
	
	public FocusSouls(StatSet params)
	{
		_charge = params.getInt("charge", 0);
		_type = params.getEnum("type", SoulType.class, SoulType.LIGHT);
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public void instant(Creature effector, Creature effected, Skill skill, Item item)
	{
		if (!effected.isPlayer() || effected.isAlikeDead())
		{
			return;
		}
		
		final Player target = effected.getActingPlayer();
		final int maxSouls = (int) target.getStat().getValue(Stat.MAX_SOULS, 0);
		if (maxSouls > 0)
		{
			final int amount = _charge;
			if ((target.getChargedSouls(_type) < maxSouls))
			{
				final int count = ((target.getChargedSouls(_type) + amount) <= maxSouls) ? amount : (maxSouls - target.getChargedSouls(_type));
				target.increaseSouls(count, _type);
			}
			else
			{
				target.sendPacket(SystemMessageId.SOUL_CANNOT_BE_INCREASED_ANYMORE);
			}
		}
	}
}