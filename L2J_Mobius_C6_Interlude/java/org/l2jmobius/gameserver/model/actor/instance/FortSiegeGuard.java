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

import org.l2jmobius.gameserver.ai.CreatureAI;
import org.l2jmobius.gameserver.ai.CtrlIntention;
import org.l2jmobius.gameserver.ai.FortSiegeGuardAI;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.actor.Attackable;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Playable;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.Summon;
import org.l2jmobius.gameserver.model.actor.knownlist.FortSiegeGuardKnownList;
import org.l2jmobius.gameserver.model.actor.templates.NpcTemplate;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import org.l2jmobius.gameserver.network.serverpackets.MyTargetSelected;
import org.l2jmobius.gameserver.network.serverpackets.StatusUpdate;
import org.l2jmobius.gameserver.network.serverpackets.ValidateLocation;

public class FortSiegeGuard extends Attackable
{
	public FortSiegeGuard(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		getKnownList(); // inits the knownlist
	}
	
	@Override
	public FortSiegeGuardKnownList getKnownList()
	{
		if (!(super.getKnownList() instanceof FortSiegeGuardKnownList))
		{
			setKnownList(new FortSiegeGuardKnownList(this));
		}
		return (FortSiegeGuardKnownList) super.getKnownList();
	}
	
	@Override
	public CreatureAI getAI()
	{
		final CreatureAI ai = _ai; // copy handle
		if (ai == null)
		{
			synchronized (this)
			{
				if (_ai == null)
				{
					_ai = new FortSiegeGuardAI(new AIAccessor());
				}
				return _ai;
			}
		}
		return ai;
	}
	
	/**
	 * Return True if a siege is in progress and the Creature attacker isn't a Defender.
	 * @param attacker The Creature that the SiegeGuard try to attack
	 */
	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		if (!(attacker instanceof Playable))
		{
			return false;
		}
		
		boolean isFort = false;
		if (attacker instanceof Player)
		{
			isFort = ((getFort() != null) && (getFort().getFortId() > 0) && getFort().getSiege().isInProgress() && !getFort().getSiege().checkIsDefender(((Player) attacker).getClan()));
		}
		else
		{
			isFort = ((getFort() != null) && (getFort().getFortId() > 0) && getFort().getSiege().isInProgress() && !getFort().getSiege().checkIsDefender(((Summon) attacker).getOwner().getClan()));
		}
		
		// Attackable during siege by all except defenders
		return isFort;
	}
	
	@Override
	public boolean isRandomAnimationEnabled()
	{
		return false;
	}
	
	/**
	 * This method forces guard to return to home location previously set
	 */
	public void returnHome()
	{
		if (getWalkSpeed() <= 0)
		{
			return;
		}
		if (!isInsideRadius2D(getSpawn().getX(), getSpawn().getY(), getSpawn().getZ(), 40))
		{
			setReturningToSpawnPoint(true);
			clearAggroList();
			
			if (hasAI())
			{
				getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(getSpawn().getX(), getSpawn().getY(), getSpawn().getZ(), 0));
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
			player.sendPacket(ActionFailed.STATIC_PACKET);
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
			// this max heigth difference might need some tweaking
			if (isAutoAttackable(player) && !isAlikeDead() && (Math.abs(player.getZ() - getZ()) < 600))
			{
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
			}
			if (!isAutoAttackable(player) && !canInteract(player))
			{
				// Notify the Player AI with AI_INTENTION_INTERACT
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
			}
		}
		// Send a Server->Client ActionFailed to the Player in order to avoid that the client wait another packet
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	@Override
	public void addDamageHate(Creature attacker, int damage, int aggro)
	{
		if (attacker == null)
		{
			return;
		}
		
		if (!(attacker instanceof FortSiegeGuard))
		{
			if (attacker instanceof Playable)
			{
				Player player = null;
				if (attacker instanceof Player)
				{
					player = (Player) attacker;
				}
				else if (attacker instanceof Summon)
				{
					player = ((Summon) attacker).getOwner();
				}
				if ((player != null) && (player.getClan() != null) && (player.getClan().getFortId() == getFort().getFortId()))
				{
					return;
				}
			}
			super.addDamageHate(attacker, damage, aggro);
		}
	}
}
