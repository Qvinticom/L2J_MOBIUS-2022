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
package org.l2jmobius.gameserver.handler.usercommandhandlers;

import org.l2jmobius.Config;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.ai.CtrlIntention;
import org.l2jmobius.gameserver.enums.TeleportWhereType;
import org.l2jmobius.gameserver.handler.IUserCommandHandler;
import org.l2jmobius.gameserver.instancemanager.GrandBossManager;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.serverpackets.MagicSkillUse;
import org.l2jmobius.gameserver.network.serverpackets.SetupGauge;
import org.l2jmobius.gameserver.taskmanager.GameTimeTaskManager;
import org.l2jmobius.gameserver.util.Broadcast;

public class Escape implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS =
	{
		52
	};
	
	@Override
	public boolean useUserCommand(int id, Player player)
	{
		final int unstuckTimer = player.isGM() ? 1000 : Config.UNSTUCK_INTERVAL * 1000;
		
		// Check to see if the current player is in Festival.
		if (player.isFestivalParticipant())
		{
			player.sendMessage("You may not use an escape command in a festival.");
			return false;
		}
		
		// Check to see if the current player is in an event.
		if (player.isOnEvent())
		{
			player.sendMessage("You may not use an escape skill in an event.");
			return false;
		}
		
		// Check to see if the current player is in Grandboss zone.
		if ((GrandBossManager.getInstance().getZone(player) != null) && !player.isGM())
		{
			player.sendMessage("You may not use an escape command in a grand boss zone.");
			return false;
		}
		
		// Check to see if the current player is in jail.
		if (player.isInJail())
		{
			player.sendMessage("You can not escape from jail.");
			return false;
		}
		
		// Check to see if the current player is in Observer Mode.
		if (player.inObserverMode())
		{
			player.sendMessage("You may not escape during observer mode.");
			return false;
		}
		
		// Check to see if the current player is sitting.
		if (player.isSitting())
		{
			player.sendMessage("You may not escape when you sitting.");
			return false;
		}
		
		// Check player status.
		if (player.isCastingNow() || player.isMovementDisabled() || player.isMuted() || player.isAlikeDead() || player.isInOlympiadMode())
		{
			return false;
		}
		
		if (unstuckTimer < 60000)
		{
			player.sendMessage("You use Escape: " + (unstuckTimer / 1000) + " seconds.");
		}
		else
		{
			player.sendMessage("You use Escape: " + (unstuckTimer / 60000) + " minutes.");
		}
		
		// Abort combat.
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.abortAttack();
		player.abortCast(true);
		player.disableAllSkills();
		player.setTarget(null); // Like retail we haven't self target.
		
		// Cast escape animation.
		Broadcast.toSelfAndKnownPlayersInRadius(player, new MagicSkillUse(player, player, 1050, 1, unstuckTimer, 0), 810000);
		player.sendPacket(new SetupGauge(0, unstuckTimer));
		
		// Continue execution later.
		final EscapeFinalizer escapeFinalizer = new EscapeFinalizer(player);
		player.setSkillCast(ThreadPool.schedule(escapeFinalizer, unstuckTimer));
		player.setSkillCastEndTime(10 + GameTimeTaskManager.getGameTicks() + (unstuckTimer / GameTimeTaskManager.MILLIS_IN_TICK));
		
		return true;
	}
	
	private static class EscapeFinalizer implements Runnable
	{
		private final Player _player;
		
		EscapeFinalizer(Player player)
		{
			_player = player;
		}
		
		@Override
		public void run()
		{
			if (_player.isDead())
			{
				return;
			}
			
			_player.setIn7sDungeon(false);
			_player.enableAllSkills();
			
			try
			{
				if ((_player.getKarma() > 0) && Config.ALT_KARMA_TELEPORT_TO_FLORAN)
				{
					_player.teleToLocation(17836, 170178, -3507, true); // Floran
					return;
				}
				
				_player.teleToLocation(TeleportWhereType.TOWN);
			}
			catch (Throwable e)
			{
			}
		}
	}
	
	@Override
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}
