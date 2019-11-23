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
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import org.l2jmobius.gameserver.network.serverpackets.MyTargetSelected;
import org.l2jmobius.gameserver.network.serverpackets.SetToLocation;
import org.l2jmobius.gameserver.templates.Npc;

public class GuardInstance extends Attackable
{
	private static final int INTERACTION_DISTANCE = 150;
	private int _homeX;
	private int _homeY;
	private int _homeZ;
	private boolean _hasHome;
	
	public GuardInstance(Npc template)
	{
		super(template);
		setCurrentState(CreatureState.IDLE);
	}
	
	@Override
	public void addKnownObject(WorldObject object)
	{
		if (!_hasHome)
		{
			getHomeLocation();
		}
		super.addKnownObject(object);
		if ((object instanceof PlayerInstance) && (((PlayerInstance) object).getKarma() > 0) && !isTargetScanActive())
		{
			startTargetScan();
		}
	}
	
	@Override
	public void removeKnownObject(WorldObject object)
	{
		super.removeKnownObject(object);
		if (getKnownPlayers().isEmpty())
		{
			setActive(false);
			clearAggroList();
			removeAllKnownObjects();
			stopTargetScan();
		}
		if (noTarget())
		{
			returnHome();
		}
	}
	
	private void getHomeLocation()
	{
		_hasHome = true;
		_homeX = getX();
		_homeY = getY();
		_homeZ = getZ();
	}
	
	private void returnHome()
	{
		clearAggroList();
		moveTo(_homeX, _homeY, _homeZ, 0);
	}
	
	@Override
	public boolean getCondition2(PlayerInstance player)
	{
		return !player.isDead() && (player.getKarma() > 0) && (Math.abs(getZ() - player.getZ()) <= 100);
	}
	
	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String pom = "";
		pom = val == 0 ? "" + npcId : npcId + "-" + val;
		return "data/html/guard/" + pom + ".htm";
	}
	
	@Override
	public void onAction(PlayerInstance player)
	{
		if (getObjectId() != player.getTargetId())
		{
			player.setCurrentState(CreatureState.IDLE);
			player.setTarget(this);
			final MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
			player.sendPacket(my);
			player.sendPacket(new SetToLocation(this));
		}
		else if (containsTarget(player))
		{
			player.startAttack(this);
		}
		else
		{
			final double distance = getDistance(player.getX(), player.getY());
			if (distance > INTERACTION_DISTANCE)
			{
				player.setCurrentState(CreatureState.INTERACT);
				player.moveTo(getX(), getY(), getZ(), 150);
			}
			else
			{
				showChatWindow(player, 0);
				player.sendPacket(new ActionFailed());
				player.setCurrentState(CreatureState.IDLE);
			}
		}
	}
}
