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

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.ThreadPoolManager;
import com.l2jmobius.gameserver.ai.CtrlIntention;
import com.l2jmobius.gameserver.ai.L2CharacterAI;
import com.l2jmobius.gameserver.ai.L2DoorAI;
import com.l2jmobius.gameserver.instancemanager.CastleManager;
import com.l2jmobius.gameserver.model.L2CharPosition;
import com.l2jmobius.gameserver.model.L2Character;
import com.l2jmobius.gameserver.model.L2ItemInstance;
import com.l2jmobius.gameserver.model.L2Object;
import com.l2jmobius.gameserver.model.L2Skill;
import com.l2jmobius.gameserver.model.actor.knownlist.DoorKnownList;
import com.l2jmobius.gameserver.model.actor.stat.DoorStat;
import com.l2jmobius.gameserver.model.actor.status.DoorStatus;
import com.l2jmobius.gameserver.model.entity.Castle;
import com.l2jmobius.gameserver.model.entity.ClanHall;
import com.l2jmobius.gameserver.network.L2GameClient;
import com.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import com.l2jmobius.gameserver.network.serverpackets.ConfirmDlg;
import com.l2jmobius.gameserver.network.serverpackets.DoorStatusUpdate;
import com.l2jmobius.gameserver.network.serverpackets.MyTargetSelected;
import com.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jmobius.gameserver.network.serverpackets.ValidateLocation;
import com.l2jmobius.gameserver.templates.L2CharTemplate;
import com.l2jmobius.gameserver.templates.L2Weapon;

import javolution.text.TextBuilder;
import javolution.util.FastList;

/**
 * This class ...
 * @version $Revision: 1.3.2.2.2.5 $ $Date: 2005/03/27 15:29:32 $
 */
public class L2DoorInstance extends L2Character
{
	protected static Logger log = Logger.getLogger(L2DoorInstance.class.getName());
	
	/** The castle index in the array of L2Castle this L2NpcInstance belongs to */
	private int _CastleIndex = -2;
	private int _mapRegion = -1;
	
	// when door is closed, the dimensions are
	private int _rangeXMin = 0;
	private int _rangeYMin = 0;
	private int _rangeZMin = 0;
	private int _rangeXMax = 0;
	private int _rangeYMax = 0;
	private int _rangeZMax = 0;
	
	// these variables assist in see-through calculation only
	private int _A = 0;
	private int _B = 0;
	private int _C = 0;
	private int _D = 0;
	
	protected final int _doorId;
	protected final String _name;
	private int _open;
	private final boolean _unlockable;
	
	private ClanHall _clanhall;
	
	protected long _autoActionDelay = 0;
	
	/** This class may be created only by L2Character and only for AI */
	public class AIAccessor extends L2Character.AIAccessor
	{
		protected AIAccessor()
		{
		}
		
		@Override
		public L2DoorInstance getActor()
		{
			return L2DoorInstance.this;
		}
		
		@Override
		public void moveTo(int x, int y, int z, int offset)
		{
		}
		
		@Override
		public void moveTo(int x, int y, int z)
		{
		}
		
		@Override
		public void stopMove(L2CharPosition pos)
		{
		}
		
		@Override
		public void doAttack(L2Character target)
		{
		}
		
		@Override
		public void doCast(L2Skill skill)
		{
		}
	}
	
	@Override
	public L2CharacterAI getAI()
	{
		if (_ai == null)
		{
			synchronized (this)
			{
				if (_ai == null)
				{
					_ai = new L2DoorAI(new AIAccessor());
				}
			}
		}
		return _ai;
	}
	
	class CloseTask implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				closeMe();
			}
			catch (final Throwable e)
			{
				log.log(Level.SEVERE, "", e);
			}
		}
	}
	
	/**
	 * Manages the auto open and closing of a door.
	 */
	class AutoOpenClose implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				if (getOpen() == 1)
				{
					openMe();
				}
				else
				{
					closeMe();
				}
			}
			catch (final Exception e)
			{
				log.warning("Could not auto open/close door ID " + _doorId + " (" + _name + ")");
			}
		}
	}
	
	/**
	 * @param objectId
	 * @param template
	 * @param doorId
	 * @param name
	 * @param unlockable
	 */
	public L2DoorInstance(int objectId, L2CharTemplate template, int doorId, String name, boolean unlockable)
	{
		super(objectId, template);
		getKnownList();
		getStat();
		getStatus();
		_doorId = doorId;
		_name = name;
		_unlockable = unlockable;
	}
	
	@Override
	public final DoorKnownList getKnownList()
	{
		if ((super.getKnownList() == null) || !(super.getKnownList() instanceof DoorKnownList))
		{
			setKnownList(new DoorKnownList(this));
		}
		return (DoorKnownList) super.getKnownList();
	}
	
	@Override
	public final DoorStat getStat()
	{
		if ((super.getStat() == null) || !(super.getStat() instanceof DoorStat))
		{
			setStat(new DoorStat(this));
		}
		return (DoorStat) super.getStat();
	}
	
	@Override
	public final DoorStatus getStatus()
	{
		if ((super.getStatus() == null) || !(super.getStatus() instanceof DoorStatus))
		{
			setStatus(new DoorStatus(this));
		}
		return (DoorStatus) super.getStatus();
	}
	
	public final boolean isUnlockable()
	{
		return _unlockable;
	}
	
	@Override
	public final int getLevel()
	{
		return 1;
	}
	
	/**
	 * @return Returns the doorId.
	 */
	public int getDoorId()
	{
		return _doorId;
	}
	
	/**
	 * @return Returns the open.
	 */
	public int getOpen()
	{
		return _open;
	}
	
	/**
	 * @param open The open to set.
	 */
	public void setOpen(int open)
	{
		_open = open;
	}
	
	/**
	 * Sets the delay in milliseconds for automatic opening/closing of this door instance. <BR>
	 * <B>Note:</B> A value of -1 cancels the auto open/close task.
	 * @param actionDelay
	 */
	public void setAutoActionDelay(long actionDelay)
	{
		if (_autoActionDelay >= actionDelay)
		{
			return;
		}
		
		ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new AutoOpenClose(), actionDelay, actionDelay);
		_autoActionDelay = actionDelay;
	}
	
	public int getDamage()
	{
		final int dmg = 6 - (int) Math.ceil((getCurrentHp() / getMaxHp()) * 6);
		if (dmg > 6)
		{
			return 6;
		}
		if (dmg < 0)
		{
			return 0;
		}
		return dmg;
	}
	
	public final Castle getCastle()
	{
		if (_CastleIndex < 0)
		{
			_CastleIndex = CastleManager.getInstance().getCastleIndex(this);
		}
		if (_CastleIndex < 0)
		{
			return null;
		}
		return CastleManager.getInstance().getCastles().get(_CastleIndex);
	}
	
	public void setClanHall(ClanHall clanhall)
	{
		_clanhall = clanhall;
	}
	
	public ClanHall getClanHall()
	{
		return _clanhall;
	}
	
	public boolean isEnemyOf(L2Character cha)
	{
		return true;
	}
	
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		if ((attacker == null) || !(attacker instanceof L2PlayableInstance))
		{
			return false;
		}
		
		if (getClanHall() != null)
		{
			return false;
		}
		
		// Attackable during siege by attacker only
		return ((getCastle() != null) && (getCastle().getCastleId() > 0) && getCastle().getSiege().getIsInProgress() && getCastle().getSiege().checkIsAttacker(((L2PcInstance) attacker).getClan()));
	}
	
	public boolean isAttackable(L2Character attacker)
	{
		return isAutoAttackable(attacker);
	}
	
	@Override
	public void updateAbnormalEffect()
	{
	}
	
	public int getDistanceToWatchObject(L2Object object)
	{
		if (!(object instanceof L2PcInstance))
		{
			return 0;
		}
		return 2000;
	}
	
	/**
	 * Return the distance after which the object must be remove from _knownObject according to the type of the object.<BR>
	 * <BR>
	 * <B><U> Values </U> :</B><BR>
	 * <BR>
	 * <li>object is a L2PcInstance : 4000</li>
	 * <li>object is not a L2PcInstance : 0</li><BR>
	 * <BR>
	 * @param object
	 * @return
	 */
	public int getDistanceToForgetObject(L2Object object)
	{
		if (!(object instanceof L2PcInstance))
		{
			return 0;
		}
		
		return 4000;
	}
	
	/**
	 * Return null.<BR>
	 * <BR>
	 */
	@Override
	public L2ItemInstance getActiveWeaponInstance()
	{
		return null;
	}
	
	@Override
	public L2Weapon getActiveWeaponItem()
	{
		return null;
	}
	
	@Override
	public L2ItemInstance getSecondaryWeaponInstance()
	{
		return null;
	}
	
	@Override
	public L2Weapon getSecondaryWeaponItem()
	{
		return null;
	}
	
	@Override
	public void onAction(L2PcInstance player)
	{
		if (player == null)
		{
			return;
		}
		
		// Check if the L2PcInstance already target the L2NpcInstance
		if (this != player.getTarget())
		{
			// Set the target of the L2PcInstance player
			player.setTarget(this);
			
			// Send a Server->Client packet MyTargetSelected to the L2PcInstance player
			final MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
			player.sendPacket(my);
			
			final DoorStatusUpdate su = new DoorStatusUpdate(this);
			player.sendPacket(su);
			
			// Send a Server->Client packet ValidateLocation to correct the L2NpcInstance position and heading on the client
			player.sendPacket(new ValidateLocation(this));
		}
		else
		{
			if (isAutoAttackable(player))
			{
				if (Math.abs(player.getZ() - getZ()) < 400)
				{
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
				}
				
			}
			else if ((player.getClan() != null) && (getClanHall() != null) && (player.getClanId() == getClanHall().getOwnerId()))
			{
				if (!isInsideRadius(player, L2NpcInstance.INTERACTION_DISTANCE, false, false))
				{
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
				}
				else
				{
					player.gatesRequest(this);
					
					if (getOpen() == 1)
					{
						player.sendPacket(new ConfirmDlg(1140, null));
					}
					else
					{
						player.sendPacket(new ConfirmDlg(1141, null));
					}
				}
			}
		}
		// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
		player.sendPacket(new ActionFailed());
	}
	
	@Override
	public void onActionShift(L2GameClient client)
	{
		final L2PcInstance player = client.getActiveChar();
		if (player == null)
		{
			return;
		}
		
		if (player.getAccessLevel() >= Config.GM_ACCESSLEVEL)
		{
			player.setTarget(this);
			final MyTargetSelected my = new MyTargetSelected(getObjectId(), player.getLevel());
			player.sendPacket(my);
			
			if (isAutoAttackable(player))
			{
				final DoorStatusUpdate su = new DoorStatusUpdate(this);
				player.sendPacket(su);
			}
			
			final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			final TextBuilder html1 = new TextBuilder("<html><body><table border=0>");
			html1.append("<tr><td>S.Y.L. Says:</td></tr>");
			html1.append("<tr><td>Current HP  " + getCurrentHp() + "</td></tr>");
			html1.append("<tr><td>Max HP      " + getMaxHp() + "</td></tr>");
			html1.append("<tr><td>Max X       " + getXMax() + "</td></tr>");
			html1.append("<tr><td>Max Y       " + getYMax() + "</td></tr>");
			html1.append("<tr><td>Max Z       " + getZMax() + "</td></tr>");
			html1.append("<tr><td>Min X       " + getXMin() + "</td></tr>");
			html1.append("<tr><td>Min Y       " + getYMin() + "</td></tr>");
			html1.append("<tr><td>Min Z       " + getZMin() + "</td></tr>");
			html1.append("<tr><td>Object ID:  " + getObjectId() + "</td></tr>");
			html1.append("<tr><td>Door ID:<br>" + getDoorId() + "</td></tr>");
			html1.append("<tr><td><br></td></tr>");
			
			html1.append("<tr><td>Class: " + getClass().getName() + "</td></tr>");
			html1.append("<tr><td><br></td></tr>");
			html1.append("</table>");
			
			html1.append("<table><tr>");
			html1.append("<td><button value=\"Open\" action=\"bypass -h admin_open " + getDoorId() + "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
			html1.append("<td><button value=\"Close\" action=\"bypass -h admin_close " + getDoorId() + "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
			html1.append("<td><button value=\"Kill\" action=\"bypass -h admin_kill\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
			html1.append("<td><button value=\"Delete\" action=\"bypass -h admin_delete\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
			html1.append("</tr></table></body></html>");
			
			html.setHtml(html1.toString());
			player.sendPacket(html);
		}
		
		player.sendPacket(new ActionFailed());
	}
	
	@Override
	public void broadcastStatusUpdate()
	{
		final Collection<L2PcInstance> knownPlayers = getKnownList().getKnownPlayers().values();
		if ((knownPlayers == null) || knownPlayers.isEmpty())
		{
			return;
		}
		
		final DoorStatusUpdate su = new DoorStatusUpdate(this);
		for (final L2PcInstance player : knownPlayers)
		{
			player.sendPacket(su);
		}
	}
	
	private void startAutoCloseTask()
	{
		long delay = 60000;
		
		if (getDoorName().startsWith("goe"))
		{
			delay = 420000; // Garden of Eva (every 7 minutes)
		}
		else if (getDoorName().startsWith("aden_tower"))
		{
			delay = 300000; // Tower of Insolence (every 5 minutes)
		}
		else if (getDoorName().startsWith("pirate_isle"))
		{
			delay = 300000; // devils (every 5 minutes)
		}
		else if (getDoorName().startsWith("cruma"))
		{
			delay = 1200000; // Cruma Tower (every 20 minutes)
		}
		
		ThreadPoolManager.getInstance().scheduleGeneral(new CloseTask(), delay);
	}
	
	public final void closeMe()
	{
		setOpen(1);
		broadcastStatusUpdate();
	}
	
	public final void openMe()
	{
		setOpen(0);
		broadcastStatusUpdate();
		
		// execute a task to close the door if needed
		if (isUnlockable())
		{
			startAutoCloseTask();
		}
	}
	
	@Override
	public String toString()
	{
		return "door " + _doorId;
	}
	
	public String getDoorName()
	{
		return _name;
	}
	
	public int getXMin()
	{
		return _rangeXMin;
	}
	
	public int getYMin()
	{
		return _rangeYMin;
	}
	
	public int getZMin()
	{
		return _rangeZMin;
	}
	
	public int getXMax()
	{
		return _rangeXMax;
	}
	
	public int getYMax()
	{
		return _rangeYMax;
	}
	
	public int getZMax()
	{
		return _rangeZMax;
	}
	
	public int getA()
	{
		return _A;
	}
	
	public int getB()
	{
		return _B;
	}
	
	public int getC()
	{
		return _C;
	}
	
	public int getD()
	{
		return _D;
	}
	
	public void setRange(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax)
	{
		_rangeXMin = xMin;
		_rangeYMin = yMin;
		_rangeZMin = zMin;
		
		_rangeXMax = xMax;
		_rangeYMax = yMax;
		_rangeZMax = zMax;
		
		_A = (_rangeYMax * (_rangeZMax - _rangeZMin)) + (_rangeYMin * (_rangeZMin - _rangeZMax));
		_B = (_rangeZMin * (_rangeXMax - _rangeXMin)) + (_rangeZMax * (_rangeXMin - _rangeXMax));
		_C = (_rangeXMin * (_rangeYMax - _rangeYMin)) + (_rangeXMin * (_rangeYMin - _rangeYMax));
		_D = -1 * ((_rangeXMin * ((_rangeYMax * _rangeZMax) - (_rangeYMin * _rangeZMax))) + (_rangeXMax * ((_rangeYMin * _rangeZMin) - (_rangeYMin * _rangeZMax))) + (_rangeXMin * ((_rangeYMin * _rangeZMax) - (_rangeYMax * _rangeZMin))));
	}
	
	public int getMapRegion()
	{
		return _mapRegion;
	}
	
	public void setMapRegion(int region)
	{
		_mapRegion = region;
	}
	
	public Collection<L2SiegeGuardInstance> getKnownSiegeGuards()
	{
		final FastList<L2SiegeGuardInstance> result = new FastList<>();
		
		for (final L2Object obj : getKnownList().getKnownObjects().values())
		{
			if (obj instanceof L2SiegeGuardInstance)
			{
				result.add((L2SiegeGuardInstance) obj);
			}
		}
		
		return result;
	}
}