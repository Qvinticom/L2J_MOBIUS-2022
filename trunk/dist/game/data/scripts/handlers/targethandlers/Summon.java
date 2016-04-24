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
package handlers.targethandlers;

import java.util.ArrayList;
import java.util.List;

import com.l2jmobius.gameserver.handler.ITargetTypeHandler;
import com.l2jmobius.gameserver.model.L2Object;
import com.l2jmobius.gameserver.model.actor.L2Character;
import com.l2jmobius.gameserver.model.skills.Skill;
import com.l2jmobius.gameserver.model.skills.targets.L2TargetType;

/**
 * Target Summon handler.
 * @author UnAfraid
 */
public class Summon implements ITargetTypeHandler
{
	@Override
	public L2Object[] getTargetList(Skill skill, L2Character activeChar, boolean onlyFirst, L2Character target)
	{
		if (!activeChar.hasSummon())
		{
			return EMPTY_TARGET_LIST;
		}
		if (!activeChar.hasPet() && activeChar.hasServitors())
		{
			return activeChar.getServitors().values().toArray(new L2Character[0]);
		}
		if (activeChar.hasPet() && !activeChar.hasServitors())
		{
			return new L2Character[]
			{
				activeChar.getPet()
			};
		}
		final List<L2Character> targets = new ArrayList<>(1 + activeChar.getServitors().size());
		targets.add(activeChar.getPet());
		targets.addAll(activeChar.getServitors().values());
		return targets.toArray(new L2Character[0]);
	}
	
	@Override
	public Enum<L2TargetType> getTargetType()
	{
		return L2TargetType.SUMMON;
	}
}
