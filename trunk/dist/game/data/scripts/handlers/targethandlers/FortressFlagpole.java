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

import com.l2jmobius.gameserver.handler.ITargetTypeHandler;
import com.l2jmobius.gameserver.model.L2Object;
import com.l2jmobius.gameserver.model.actor.L2Character;
import com.l2jmobius.gameserver.model.skills.Skill;
import com.l2jmobius.gameserver.model.skills.targets.TargetType;
import com.l2jmobius.gameserver.network.SystemMessageId;

/**
 * Target fortress flagpole
 * @author Nik
 */
public class FortressFlagpole implements ITargetTypeHandler
{
	@Override
	public Enum<TargetType> getTargetType()
	{
		return TargetType.FORTRESS_FLAGPOLE;
	}
	
	@Override
	public L2Object getTarget(L2Character activeChar, L2Object selectedTarget, Skill skill, boolean forceUse, boolean dontMove, boolean sendMessage)
	{
		final L2Object target = activeChar.getTarget();
		if (target != null)
		{
			switch (target.getId())
			{
				case 35657:
				case 35688:
				case 35726:
				case 35757:
				case 35795:
				case 35826:
				case 35857:
				case 35895:
				case 35926:
				case 35964:
				case 35002:
				case 36033:
				case 36071:
				case 36109:
				case 36140:
				case 36171:
				case 36209:
				case 36247:
				case 36285:
				case 36316:
				case 36354:
				{
					return target;
				}
			}
		}
		
		if (sendMessage)
		{
			activeChar.sendPacket(SystemMessageId.THAT_IS_AN_INCORRECT_TARGET);
		}
		
		return null;
	}
}
