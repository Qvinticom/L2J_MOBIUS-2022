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
import com.l2jmobius.commons.concurrent.ThreadPoolManager;
import com.l2jmobius.gameserver.GameTimeController;
import com.l2jmobius.gameserver.ai.CtrlIntention;
import com.l2jmobius.gameserver.datatables.csv.MapRegionTable;
import com.l2jmobius.gameserver.handler.IUserCommandHandler;
import com.l2jmobius.gameserver.instancemanager.GrandBossManager;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.entity.event.CTF;
import com.l2jmobius.gameserver.model.entity.event.DM;
import com.l2jmobius.gameserver.model.entity.event.TvT;
import com.l2jmobius.gameserver.model.entity.event.VIP;
import com.l2jmobius.gameserver.network.SystemMessageId;
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
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.handler.IUserCommandHandler#useUserCommand(int, com.l2jmobius.gameserver.model.L2PcInstance)
	 */
	@Override
	public boolean useUserCommand(int id, L2PcInstance activeChar)
	{
		final int unstuckTimer = activeChar.getAccessLevel().isGm() ? 1000 : Config.UNSTUCK_INTERVAL * 1000;
		
		// Check to see if the current player is in Festival.
		if (activeChar.isFestivalParticipant())
		{
			activeChar.sendMessage("You may not use an escape command in a festival.");
			return false;
		}
		
		// Check to see if the current player is in TVT Event.
		if (activeChar._inEventTvT && TvT.is_started())
		{
			activeChar.sendMessage("You may not use an escape skill in TvT.");
			return false;
		}
		
		// Check to see if the current player is in CTF Event.
		if (activeChar._inEventCTF && CTF.is_started())
		{
			activeChar.sendMessage("You may not use an escape skill in CTF.");
			return false;
		}
		
		// Check to see if the current player is in DM Event.
		if (activeChar._inEventDM && DM.is_started())
		{
			activeChar.sendMessage("You may not use an escape skill in DM.");
			return false;
		}
		
		// Check to see if the current player is in Vip Event.
		if (activeChar._inEventVIP && VIP._started)
		{
			activeChar.sendMessage("You may not use an escape skill in VIP.");
			return false;
		}
		
		// Check to see if the current player is in Grandboss zone.
		if ((GrandBossManager.getInstance().getZone(activeChar) != null) && !activeChar.isGM())
		{
			activeChar.sendMessage("You may not use an escape command in Grand boss zone.");
			return false;
		}
		
		// Check to see if the current player is in jail.
		if (activeChar.isInJail())
		{
			activeChar.sendMessage("You can not escape from jail.");
			return false;
		}
		
		// Check to see if the current player is in fun event.
		if (activeChar.isInFunEvent())
		{
			activeChar.sendMessage("You may not escape from an Event.");
			return false;
		}
		
		// Check to see if the current player is in Observer Mode.
		if (activeChar.inObserverMode())
		{
			activeChar.sendMessage("You may not escape during Observer mode.");
			return false;
		}
		
		// Check to see if the current player is sitting.
		if (activeChar.isSitting())
		{
			activeChar.sendMessage("You may not escape when you sitting.");
			return false;
		}
		
		// Check player status.
		if (activeChar.isCastingNow() || activeChar.isMovementDisabled() || activeChar.isMuted() || activeChar.isAlikeDead() || activeChar.isInOlympiadMode() || activeChar.isAwaying())
		{
			return false;
		}
		
		SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
		
		if (unstuckTimer < 60000)
		{
			sm.addString("You use Escape: " + (unstuckTimer / 1000) + " seconds.");
		}
		else
		{
			sm.addString("You use Escape: " + (unstuckTimer / 60000) + " minutes.");
		}
		
		activeChar.sendPacket(sm);
		
		activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		// SoE Animation section
		activeChar.setTarget(activeChar);
		activeChar.disableAllSkills();
		
		MagicSkillUse msk = new MagicSkillUse(activeChar, 1050, 1, unstuckTimer, 0);
		activeChar.setTarget(null); // Like retail we haven't self target
		Broadcast.toSelfAndKnownPlayersInRadius(activeChar, msk, 810000/* 900 */);
		SetupGauge sg = new SetupGauge(0, unstuckTimer);
		activeChar.sendPacket(sg);
		// End SoE Animation section
		EscapeFinalizer ef = new EscapeFinalizer(activeChar);
		// continue execution later
		activeChar.setSkillCast(ThreadPoolManager.schedule(ef, unstuckTimer));
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
			
			_activeChar.setIsIn7sDungeon(false);
			_activeChar.enableAllSkills();
			
			try
			{
				if ((_activeChar.getKarma() > 0) && Config.ALT_KARMA_TELEPORT_TO_FLORAN)
				{
					_activeChar.teleToLocation(17836, 170178, -3507, true); // Floran
					return;
				}
				
				_activeChar.teleToLocation(MapRegionTable.TeleportWhereType.Town);
			}
			catch (Throwable e)
			{
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
