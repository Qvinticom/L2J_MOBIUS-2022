/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.l2jmobius.gameserver.model.actor.instance;

import org.l2jmobius.gameserver.enums.CreatureState;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Attackable;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.templates.NpcTemplate;

public class Monster extends Attackable
{
	public Monster(NpcTemplate template)
	{
		super(template);
		// this.setMoveRadius(2000);
		setCurrentState(CreatureState.RANDOM_WALK);
	}
	
	@Override
	public void addKnownObject(WorldObject object)
	{
		super.addKnownObject(object);
		if ((object instanceof Player) && !isActive())
		{
			setActive(true);
			startRandomWalking();
			if (isAggressive() && !isTargetScanActive())
			{
				startTargetScan();
			}
		}
	}
	
	@Override
	public void removeKnownObject(WorldObject object)
	{
		super.removeKnownObject(object);
		final Creature temp = (Creature) object;
		if (getTarget() == temp)
		{
			setTarget(null);
			stopMove();
			setPawnTarget(null);
			setMovingToPawn(false);
		}
		if (getKnownPlayers().isEmpty())
		{
			setActive(false);
			clearAggroList();
			removeAllKnownObjects();
			stopRandomWalking();
			if (isAggressive())
			{
				stopTargetScan();
			}
			return;
		}
		if ((getCurrentState() != CreatureState.RANDOM_WALK) && !isDead() && (getTarget() == null))
		{
			startRandomWalking();
			if (isAggressive())
			{
				startTargetScan();
			}
		}
	}
	
	@Override
	public boolean getCondition2(Player player)
	{
		return !player.isInvul() && !player.isDead() && (Math.abs(getZ() - player.getZ()) <= 100);
	}
	
	@Override
	public boolean isMonster()
	{
		return true;
	}
}
