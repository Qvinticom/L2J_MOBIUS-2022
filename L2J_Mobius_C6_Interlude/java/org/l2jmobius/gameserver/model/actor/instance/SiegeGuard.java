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
package org.l2jmobius.gameserver.model.actor.instance;

import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.ai.CreatureAI;
import org.l2jmobius.gameserver.ai.CtrlIntention;
import org.l2jmobius.gameserver.ai.SiegeGuardAI;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.actor.Attackable;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.knownlist.SiegeGuardKnownList;
import org.l2jmobius.gameserver.model.actor.templates.NpcTemplate;
import org.l2jmobius.gameserver.model.siege.clanhalls.DevastatedCastle;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import org.l2jmobius.gameserver.network.serverpackets.MyTargetSelected;
import org.l2jmobius.gameserver.network.serverpackets.SocialAction;
import org.l2jmobius.gameserver.network.serverpackets.StatusUpdate;
import org.l2jmobius.gameserver.network.serverpackets.ValidateLocation;

/**
 * This class represents all guards in the world. It inherits all methods from Attackable and adds some more such as tracking PK's or custom interactions.
 * @version $Revision: 1.11.2.1.2.7 $ $Date: 2005/04/06 16:13:40 $
 */
public class SiegeGuard extends Attackable
{
	private int _homeX;
	private int _homeY;
	private int _homeZ;
	
	public SiegeGuard(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		getKnownList(); // inits the knownlist
	}
	
	@Override
	public SiegeGuardKnownList getKnownList()
	{
		if (!(super.getKnownList() instanceof SiegeGuardKnownList))
		{
			setKnownList(new SiegeGuardKnownList(this));
		}
		return (SiegeGuardKnownList) super.getKnownList();
	}
	
	@Override
	public CreatureAI getAI()
	{
		synchronized (this)
		{
			if (_ai == null)
			{
				_ai = new SiegeGuardAI(new AIAccessor());
			}
		}
		return _ai;
	}
	
	/**
	 * Return True if a siege is in progress and the Creature attacker isn't a Defender.
	 * @param attacker The Creature that the SiegeGuard try to attack
	 */
	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		// Attackable during siege by all except defenders ( Castle or Fort )
		return (attacker instanceof Player) && (((getCastle() != null) && (getCastle().getCastleId() > 0) && getCastle().getSiege().isInProgress() && !getCastle().getSiege().checkIsDefender(((Player) attacker).getClan())) || DevastatedCastle.getInstance().isInProgress());
	}
	
	@Override
	public boolean isRandomAnimationEnabled()
	{
		return false;
	}
	
	/**
	 * Sets home location of guard. Guard will always try to return to this location after it has killed all PK's in range.
	 */
	public void getHomeLocation()
	{
		_homeX = getX();
		_homeY = getY();
		_homeZ = getZ();
	}
	
	public int getHomeX()
	{
		return _homeX;
	}
	
	public int getHomeY()
	{
		return _homeY;
	}
	
	/**
	 * This method forces guard to return to home location previously set
	 */
	public void returnHome()
	{
		if (!isInsideRadius2D(_homeX, _homeY, _homeZ, 40))
		{
			setReturningToSpawnPoint(true);
			clearAggroList();
			
			if (hasAI())
			{
				getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(_homeX, _homeY, _homeZ, 0));
			}
		}
	}
	
	/**
	 * Custom onAction behaviour. Note that super() is not called because guards need extra check to see if a player should interact or ATTACK them when clicked.
	 */
	@Override
	public void onAction(Player player)
	{
		if (!canTarget(player))
		{
			return;
		}
		
		// Check if the Player already target the Npc
		if (this != player.getTarget())
		{
			// Set the target of the Player player
			player.setTarget(this);
			
			// Send a Server->Client packet MyTargetSelected to the Player player
			player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel() - getLevel()));
			
			// Send a Server->Client packet StatusUpdate of the Npc to the Player to update its HP bar
			final StatusUpdate su = new StatusUpdate(getObjectId());
			su.addAttribute(StatusUpdate.CUR_HP, (int) getStatus().getCurrentHp());
			su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
			player.sendPacket(su);
			
			// Send a Server->Client packet ValidateLocation to correct the Npc position and heading on the client
			player.sendPacket(new ValidateLocation(this));
		}
		else
		{
			if (isAutoAttackable(player) && !isAlikeDead())
			{
				if (Math.abs(player.getZ() - getZ()) < 600) // this max heigth difference might need some tweaking
				{
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
				}
				else
				{
					// Send a Server->Client ActionFailed to the Player in order to avoid that the client wait another packet
					player.sendPacket(ActionFailed.STATIC_PACKET);
				}
			}
			if (!isAutoAttackable(player))
			{
				if (!canInteract(player))
				{
					// Notify the Player AI with AI_INTENTION_INTERACT
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
				}
				else
				{
					final SocialAction sa = new SocialAction(getObjectId(), Rnd.get(8));
					broadcastPacket(sa);
					sendPacket(sa);
					showChatWindow(player, 0);
				}
			}
		}
	}
	
	@Override
	public void addDamageHate(Creature attacker, int damage, int aggro)
	{
		if (attacker == null)
		{
			return;
		}
		
		if (!(attacker instanceof SiegeGuard))
		{
			super.addDamageHate(attacker, damage, aggro);
		}
	}
}
