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
package com.l2jmobius.gameserver.model.zone.type;

import com.l2jmobius.gameserver.model.L2Character;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.base.Race;
import com.l2jmobius.gameserver.model.zone.L2ZoneType;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * A mother-trees zone
 * @author durgus
 */
public class L2MotherTreeZone extends L2ZoneType
{
	public L2MotherTreeZone(int id)
	{
		super(id);
	}
	
	@Override
	protected void onEnter(L2Character character)
	{
		if (character instanceof L2PcInstance)
		{
			final L2PcInstance player = (L2PcInstance) character;
			
			if (player.isInParty())
			{
				for (final L2PcInstance member : player.getParty().getPartyMembers())
				{
					if (member.getRace() != Race.Elf)
					{
						return;
					}
				}
			}
			
			player.setInsideZone(L2Character.ZONE_MOTHERTREE, true);
			player.sendPacket(new SystemMessage(SystemMessage.ENTER_SHADOW_MOTHER_TREE));
		}
	}
	
	@Override
	protected void onExit(L2Character character)
	{
		if ((character instanceof L2PcInstance) && character.isInsideZone(L2Character.ZONE_MOTHERTREE))
		{
			character.setInsideZone(L2Character.ZONE_MOTHERTREE, false);
			((L2PcInstance) character).sendPacket(new SystemMessage(SystemMessage.EXIT_SHADOW_MOTHER_TREE));
		}
	}
}