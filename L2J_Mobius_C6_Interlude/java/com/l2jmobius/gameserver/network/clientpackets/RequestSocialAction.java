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
package com.l2jmobius.gameserver.network.clientpackets;

import java.util.logging.Logger;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.ai.CtrlIntention;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.SystemMessageId;
import com.l2jmobius.gameserver.network.serverpackets.SocialAction;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import com.l2jmobius.gameserver.util.Util;

public class RequestSocialAction extends L2GameClientPacket
{
	private static Logger LOGGER = Logger.getLogger(RequestSocialAction.class.getName());
	private int _actionId;
	
	@Override
	protected void readImpl()
	{
		_actionId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		// You cannot do anything else while fishing
		if (activeChar.isFishing())
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.CANNOT_DO_WHILE_FISHING_3);
			activeChar.sendPacket(sm);
			return;
		}
		
		// check if its the actionId is allowed
		if ((_actionId < 2) || (_actionId > 13))
		{
			Util.handleIllegalPlayerAction(activeChar, "Warning!! Character " + activeChar.getName() + " of account " + activeChar.getAccountName() + " requested an internal Social Action.", Config.DEFAULT_PUNISH);
			return;
		}
		
		if ((activeChar.getPrivateStoreType() == 0) && (activeChar.getActiveRequester() == null) && !activeChar.isAlikeDead() && (!activeChar.isAllSkillsDisabled() || activeChar.isInDuel()) && (activeChar.getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE))
		{
			if (Config.DEBUG)
			{
				LOGGER.info("Social Action:" + _actionId);
			}
			
			final SocialAction atk = new SocialAction(activeChar.getObjectId(), _actionId);
			activeChar.broadcastPacket(atk);
			/*
			 * // Schedule a social task to wait for the animation to finish ThreadPoolManager.scheduleGeneral(new SocialTask(this), 2600); activeChar.setIsParalyzed(true);
			 */
		}
	}
}
