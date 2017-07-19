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
package com.l2jmobius.gameserver.handler.usercommandhandlers;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.GameTimeController;
import com.l2jmobius.gameserver.ThreadPoolManager;
import com.l2jmobius.gameserver.ai.CtrlIntention;
import com.l2jmobius.gameserver.datatables.MapRegionTable;
import com.l2jmobius.gameserver.handler.IUserCommandHandler;
import com.l2jmobius.gameserver.instancemanager.DimensionalRiftManager;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.serverpackets.MagicSkillUse;
import com.l2jmobius.gameserver.network.serverpackets.SetupGauge;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import com.l2jmobius.gameserver.util.Broadcast;

/**
 * 
 *
 */
public class Escape implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS =
	{
		52
	};
	private static final int REQUIRED_LEVEL = Config.GM_ESCAPE;
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.handler.IUserCommandHandler#useUserCommand(int, com.l2jmobius.gameserver.model.L2PcInstance)
	 */
	@Override
	public boolean useUserCommand(int id, L2PcInstance activeChar)
	{
		if (activeChar.isCastingNow() || activeChar.isMovementDisabled() || activeChar.isAlikeDead() || activeChar.isInOlympiadMode())
		{
			return false;
		}
		
		final int unstuckTimer = (activeChar.getAccessLevel() >= REQUIRED_LEVEL ? 5000 : Config.UNSTUCK_INTERVAL * 1000);
		
		// Check to see if the player is in a festival.
		if (activeChar.isFestivalParticipant())
		{
			activeChar.sendPacket(SystemMessage.sendString("You may not use an escape command in a festival."));
			return false;
		}
		
		if ((activeChar.isInParty() && activeChar.getParty().isInDimensionalRift()) || DimensionalRiftManager.getInstance().checkIfInRiftZone(activeChar.getX(), activeChar.getY(), activeChar.getZ(), true))
		{
			activeChar.sendMessage("Once a party is ported in another dimension, its members cannot get out of it.");
			return false;
		}
		
		// Check to see if player is in jail
		if (activeChar.isInJail())
		{
			activeChar.sendPacket(SystemMessage.sendString("You cannot escape from jail."));
			return false;
		}
		
		if (activeChar.getEventTeam() > 0)
		{
			activeChar.sendMessage("You may not use an escape skill in TvT Event.");
			return false;
		}
		
		activeChar.sendMessage("You use Escape: 5 minutes.");
		activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		// SoE Animation section
		activeChar.setTarget(activeChar);
		activeChar.disableAllSkills();
		
		final MagicSkillUse msk = new MagicSkillUse(activeChar, 1050, 1, unstuckTimer, 0);
		Broadcast.toSelfAndKnownPlayersInRadius(activeChar, msk, 810000);
		final SetupGauge sg = new SetupGauge(0, unstuckTimer);
		activeChar.sendPacket(sg);
		// End SoE Animation section
		
		final EscapeFinalizer ef = new EscapeFinalizer(activeChar);
		// continue execution later
		activeChar.setSkillCast(ThreadPoolManager.getInstance().scheduleGeneral(ef, unstuckTimer));
		activeChar.setSkillCastEndTime(10 + GameTimeController.getGameTicks() + (unstuckTimer / GameTimeController.MILLIS_IN_TICK));
		
		return true;
	}
	
	static class EscapeFinalizer implements Runnable
	{
		private final L2PcInstance _activeChar;
		
		EscapeFinalizer(L2PcInstance activeChar)
		{
			_activeChar = activeChar;
		}
		
		@Override
		public void run()
		{
			if (_activeChar.isDead())
			{
				return;
			}
			
			_activeChar.enableAllSkills();
			
			try
			{
				_activeChar.teleToLocation(MapRegionTable.TeleportWhereType.Town);
			}
			catch (final Throwable e)
			{
				if (Config.DEBUG)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.handler.IUserCommandHandler#getUserCommandList()
	 */
	@Override
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}