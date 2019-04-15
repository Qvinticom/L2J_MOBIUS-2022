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
package handlers.actionhandlers;

import org.l2jmobius.gameserver.ai.CtrlIntention;
import org.l2jmobius.gameserver.enums.InstanceType;
import org.l2jmobius.gameserver.enums.PrivateStoreType;
import org.l2jmobius.gameserver.geoengine.GeoEngine;
import org.l2jmobius.gameserver.handler.IActionHandler;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.entity.TvTEvent;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;

public class PlayerInstanceAction implements IActionHandler
{
	private static final int CURSED_WEAPON_VICTIM_MIN_LEVEL = 21;
	
	/**
	 * Manage actions when a player click on this PlayerInstance.<BR>
	 * <BR>
	 * <B><U> Actions on first click on the PlayerInstance (Select it)</U> :</B><BR>
	 * <BR>
	 * <li>Set the target of the player</li>
	 * <li>Send a Server->Client packet MyTargetSelected to the player (display the select window)</li><BR>
	 * <BR>
	 * <B><U> Actions on second click on the PlayerInstance (Follow it/Attack it/Intercat with it)</U> :</B><BR>
	 * <BR>
	 * <li>Send a Server->Client packet MyTargetSelected to the player (display the select window)</li>
	 * <li>If target PlayerInstance has a Private Store, notify the player AI with AI_INTENTION_INTERACT</li>
	 * <li>If target PlayerInstance is autoAttackable, notify the player AI with AI_INTENTION_ATTACK</li> <BR>
	 * <BR>
	 * <li>If target PlayerInstance is NOT autoAttackable, notify the player AI with AI_INTENTION_FOLLOW</li><BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>Client packet : Action, AttackRequest</li><BR>
	 * <BR>
	 * @param activeChar The player that start an action on target PlayerInstance
	 */
	@Override
	public boolean action(PlayerInstance activeChar, WorldObject target, boolean interact)
	{
		// See description in TvTEvent.java
		if (!TvTEvent.onAction(activeChar, target.getObjectId()))
		{
			return false;
		}
		
		// Check if the PlayerInstance is confused
		if (activeChar.isOutOfControl())
		{
			return false;
		}
		
		// Aggression target lock effect
		if (activeChar.isLockedTarget() && (activeChar.getLockedTarget() != target))
		{
			activeChar.sendPacket(SystemMessageId.FAILED_TO_CHANGE_ATTACK_TARGET);
			return false;
		}
		
		// Check if the activeChar already target this PlayerInstance
		if (activeChar.getTarget() != target)
		{
			// Set the target of the activeChar
			activeChar.setTarget(target);
		}
		else if (interact)
		{
			final PlayerInstance player = target.getActingPlayer();
			// Check if this PlayerInstance has a Private Store
			if (player.getPrivateStoreType() != PrivateStoreType.NONE)
			{
				activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, player);
			}
			else
			{
				// Check if this PlayerInstance is autoAttackable
				if (player.isAutoAttackable(activeChar))
				{
					if ((player.isCursedWeaponEquipped() && (activeChar.getLevel() < CURSED_WEAPON_VICTIM_MIN_LEVEL)) //
						|| (activeChar.isCursedWeaponEquipped() && (player.getLevel() < CURSED_WEAPON_VICTIM_MIN_LEVEL)))
					{
						activeChar.sendPacket(ActionFailed.STATIC_PACKET);
					}
					else
					{
						if (GeoEngine.getInstance().canSeeTarget(activeChar, player))
						{
							activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
							activeChar.onActionRequest();
						}
					}
				}
				else
				{
					// This Action Failed packet avoids activeChar getting stuck when clicking three or more times
					activeChar.sendPacket(ActionFailed.STATIC_PACKET);
					if (GeoEngine.getInstance().canSeeTarget(activeChar, player))
					{
						activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, player);
					}
				}
			}
		}
		return true;
	}
	
	@Override
	public InstanceType getInstanceType()
	{
		return InstanceType.PlayerInstance;
	}
}
