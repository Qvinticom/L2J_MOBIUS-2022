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
package com.l2jmobius.gameserver.ai;

import java.util.List;

import com.l2jmobius.Config;
import com.l2jmobius.commons.concurrent.ThreadPool;
import com.l2jmobius.gameserver.datatables.csv.NpcWalkerRoutesTable;
import com.l2jmobius.gameserver.model.L2NpcWalkerNode;
import com.l2jmobius.gameserver.model.actor.L2Character;
import com.l2jmobius.gameserver.model.actor.instance.L2NpcWalkerInstance;
import com.l2jmobius.gameserver.model.actor.position.Location;

public class L2NpcWalkerAI extends L2CharacterAI implements Runnable
{
	private static final int DEFAULT_MOVE_DELAY = 0;
	
	private long _nextMoveTime;
	
	private boolean _walkingToNextPoint = false;
	
	/**
	 * home points for xyz
	 */
	int _homeX, _homeY, _homeZ;
	
	/**
	 * route of the current npc
	 */
	private final List<L2NpcWalkerNode> _route = NpcWalkerRoutesTable.getInstance().getRouteForNpc(getActor().getNpcId());
	
	/**
	 * current node
	 */
	private int _currentPos;
	
	/**
	 * Constructor of L2CharacterAI.<BR>
	 * <BR>
	 * @param accessor The AI accessor of the L2Character
	 */
	public L2NpcWalkerAI(L2Character.AIAccessor accessor)
	{
		super(accessor);
		// Do we really need 2 minutes delay before start?
		// no we dont... :)
		ThreadPool.scheduleAtFixedRate(this, 0, 1000);
	}
	
	@Override
	public void run()
	{
		onEvtThink();
	}
	
	@Override
	protected void onEvtThink()
	{
		if (!Config.ALLOW_NPC_WALKERS)
		{
			return;
		}
		
		if (_walkingToNextPoint)
		{
			checkArrived();
			return;
		}
		
		if (_nextMoveTime < System.currentTimeMillis())
		{
			walkToLocation();
		}
	}
	
	/**
	 * If npc can't walk to it's target then just teleport to next point
	 * @param blocked_at_pos ignoring it
	 */
	@Override
	protected void onEvtArrivedBlocked(Location blocked_at_pos)
	{
		LOGGER.warning("NpcWalker ID: " + getActor().getNpcId() + ": Blocked at rote position [" + _currentPos + "], coords: " + blocked_at_pos.getX() + ", " + blocked_at_pos.getY() + ", " + blocked_at_pos.getZ() + ". Teleporting to next point");
		
		if (_route.size() <= _currentPos)
		{
			return;
		}
		
		final int destinationX = _route.get(_currentPos).getMoveX();
		final int destinationY = _route.get(_currentPos).getMoveY();
		final int destinationZ = _route.get(_currentPos).getMoveZ();
		
		getActor().teleToLocation(destinationX, destinationY, destinationZ, false);
		super.onEvtArrivedBlocked(blocked_at_pos);
	}
	
	private void checkArrived()
	{
		if (_route.size() <= _currentPos)
		{
			return;
		}
		
		final int destinationX = _route.get(_currentPos).getMoveX();
		final int destinationY = _route.get(_currentPos).getMoveY();
		final int destinationZ = _route.get(_currentPos).getMoveZ();
		
		if ((getActor().getX() == destinationX) && (getActor().getY() == destinationY) && (getActor().getZ() == destinationZ))
		{
			String chat = _route.get(_currentPos).getChatText();
			
			if ((chat != null) && !chat.equals("NULL"))
			{
				try
				{
					getActor().broadcastChat(chat);
				}
				catch (ArrayIndexOutOfBoundsException e)
				{
					LOGGER.info("L2NpcWalkerInstance: Error, " + e);
				}
			}
			
			// time in millis
			long delay = _route.get(_currentPos).getDelay() * 1000;
			
			// sleeps between each move
			if (delay < 0)
			{
				delay = DEFAULT_MOVE_DELAY;
				if (Config.DEVELOPER)
				{
					LOGGER.warning("Wrong Delay Set in Npc Walker Functions = " + delay + " secs, using default delay: " + DEFAULT_MOVE_DELAY + " secs instead.");
				}
			}
			
			_nextMoveTime = System.currentTimeMillis() + delay;
			setWalkingToNextPoint(false);
		}
	}
	
	private void walkToLocation()
	{
		if (_currentPos < (_route.size() - 1))
		{
			_currentPos++;
		}
		else
		{
			_currentPos = 0;
		}
		
		if (_route.size() <= _currentPos)
		{
			return;
		}
		
		final boolean moveType = _route.get(_currentPos).getRunning();
		
		/**
		 * false - walking true - Running
		 */
		if (moveType)
		{
			getActor().setRunning();
		}
		else
		{
			getActor().setWalking();
		}
		
		// now we define destination
		final int destinationX = _route.get(_currentPos).getMoveX();
		final int destinationY = _route.get(_currentPos).getMoveY();
		final int destinationZ = _route.get(_currentPos).getMoveZ();
		
		// notify AI of MOVE_TO
		setWalkingToNextPoint(true);
		
		setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(destinationX, destinationY, destinationZ, 0));
	}
	
	@Override
	public L2NpcWalkerInstance getActor()
	{
		return (L2NpcWalkerInstance) super.getActor();
	}
	
	public int getHomeX()
	{
		return _homeX;
	}
	
	public int getHomeY()
	{
		return _homeY;
	}
	
	public int getHomeZ()
	{
		return _homeZ;
	}
	
	public void setHomeX(int homeX)
	{
		_homeX = homeX;
	}
	
	public void setHomeY(int homeY)
	{
		_homeY = homeY;
	}
	
	public void setHomeZ(int homeZ)
	{
		_homeZ = homeZ;
	}
	
	public boolean isWalkingToNextPoint()
	{
		return _walkingToNextPoint;
	}
	
	public void setWalkingToNextPoint(boolean value)
	{
		_walkingToNextPoint = value;
	}
}
