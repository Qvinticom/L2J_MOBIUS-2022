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
package com.l2jmobius.gameserver.model.actor.instance;

import com.l2jmobius.gameserver.model.Skill;
import com.l2jmobius.gameserver.model.zone.ZoneId;
import com.l2jmobius.gameserver.templates.creatures.NpcTemplate;

public class SiegeSummonInstance extends SummonInstance
{
	public static final int SIEGE_GOLEM_ID = 14737;
	public static final int HOG_CANNON_ID = 14768;
	public static final int SWOOP_CANNON_ID = 14839;
	
	public SiegeSummonInstance(int objectId, NpcTemplate template, PlayerInstance owner, Skill skill)
	{
		super(objectId, template, owner, skill);
	}
	
	@Override
	public void onSpawn()
	{
		super.onSpawn();
		
		if (!getOwner().isGM() && !isInsideZone(ZoneId.SIEGE))
		{
			unSummon(getOwner());
			getOwner().sendMessage("Summon was unsummoned because it exited siege zone");
		}
	}
}
