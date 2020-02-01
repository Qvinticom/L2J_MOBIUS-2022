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
package org.l2jmobius.gameserver.model.zone.type;

import org.l2jmobius.gameserver.enums.Race;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.zone.ZoneId;
import org.l2jmobius.gameserver.model.zone.ZoneType;
import org.l2jmobius.gameserver.network.SystemMessageId;

/**
 * A mother-trees zone
 * @author durgus
 */
public class MotherTreeZone extends ZoneType
{
	public MotherTreeZone(int id)
	{
		super(id);
	}
	
	@Override
	protected void onEnter(Creature creature)
	{
		if (creature instanceof PlayerInstance)
		{
			final PlayerInstance player = (PlayerInstance) creature;
			if (player.isInParty())
			{
				for (PlayerInstance member : player.getParty().getPartyMembers())
				{
					if (member.getRace() != Race.ELF)
					{
						return;
					}
				}
			}
			
			player.setInsideZone(ZoneId.MOTHERTREE, true);
			player.sendPacket(SystemMessageId.ENTER_SHADOW_MOTHER_TREE);
		}
	}
	
	@Override
	protected void onExit(Creature creature)
	{
		if ((creature instanceof PlayerInstance) && creature.isInsideZone(ZoneId.MOTHERTREE))
		{
			creature.setInsideZone(ZoneId.MOTHERTREE, false);
			((PlayerInstance) creature).sendPacket(SystemMessageId.EXIT_SHADOW_MOTHER_TREE);
		}
	}
	
	@Override
	protected void onDieInside(Creature creature)
	{
	}
	
	@Override
	protected void onReviveInside(Creature creature)
	{
	}
}
