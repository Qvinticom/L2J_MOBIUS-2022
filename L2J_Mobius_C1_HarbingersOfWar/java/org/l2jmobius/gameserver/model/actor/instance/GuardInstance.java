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

import java.util.logging.Logger;

import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Attackable;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import org.l2jmobius.gameserver.network.serverpackets.MyTargetSelected;
import org.l2jmobius.gameserver.network.serverpackets.SetToLocation;
import org.l2jmobius.gameserver.templates.L2Npc;

public class GuardInstance extends Attackable
{
	private static Logger _log = Logger.getLogger(GuardInstance.class.getName());
	private static final int INTERACTION_DISTANCE = 150;
	private int _homeX;
	private int _homeY;
	private int _homeZ;
	private boolean _hasHome;
	
	public GuardInstance(L2Npc template)
	{
		super(template);
		setCurrentState((byte) 0);
	}
	
	@Override
	public void addKnownObject(WorldObject object)
	{
		PlayerInstance player;
		if (!_hasHome)
		{
			getHomeLocation();
		}
		super.addKnownObject(object);
		if ((object instanceof PlayerInstance) && ((player = (PlayerInstance) object).getKarma() > 0) && !isTargetScanActive())
		{
			_log.fine(getObjectId() + ": PK " + player.getObjectId() + " entered scan range");
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
		_log.finer(getObjectId() + ": Home location set to X:" + _homeX + " Y:" + _homeY + " Z:" + _homeZ);
	}
	
	private void returnHome()
	{
		_log.fine(getObjectId() + ": moving home");
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
			player.setCurrentState((byte) 0);
			_log.fine(player.getObjectId() + ": Targetted guard " + getObjectId());
			player.setTarget(this);
			MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
			player.sendPacket(my);
			player.sendPacket(new SetToLocation(this));
		}
		else if (containsTarget(player))
		{
			_log.fine(player.getObjectId() + ": Attacked guard " + getObjectId());
			player.startAttack(this);
		}
		else
		{
			double distance = getDistance(player.getX(), player.getY());
			if (distance > INTERACTION_DISTANCE)
			{
				player.setCurrentState((byte) 7);
				player.moveTo(getX(), getY(), getZ(), 150);
			}
			else
			{
				showChatWindow(player, 0);
				player.sendPacket(new ActionFailed());
				player.setCurrentState((byte) 0);
			}
		}
	}
}
