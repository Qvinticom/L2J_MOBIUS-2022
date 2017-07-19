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
import com.l2jmobius.gameserver.instancemanager.QuestManager;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.network.serverpackets.QuestList;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * This class ...
 * @version $Revision: 1.3.4.2 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestQuestAbort extends L2GameClientPacket
{
	private static final String _C__64_REQUESTQUESTABORT = "[C] 64 RequestQuestAbort";
	private static Logger _log = Logger.getLogger(RequestQuestAbort.class.getName());
	
	private int _QuestID;
	
	@Override
	protected void readImpl()
	{
		_QuestID = readD();
	}
	
	@Override
	public void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		final Quest qe = QuestManager.getInstance().getQuest(_QuestID);
		if (qe != null)
		{
			final QuestState qs = activeChar.getQuestState(qe.getName());
			if (qs != null)
			{
				qs.exitQuest(true);
				SystemMessage sm = new SystemMessage(SystemMessage.S1_S2);
				sm.addString("Quest aborted.");
				activeChar.sendPacket(sm);
				sm = null;
				final QuestList ql = new QuestList();
				activeChar.sendPacket(ql);
			}
			else
			{
				if (Config.DEBUG)
				{
					_log.info("Player '" + activeChar.getName() + "' try to abort quest " + qe.getName() + " but he didn't have it started.");
				}
			}
		}
		else
		{
			if (Config.DEBUG)
			{
				_log.warning("Quest (id='" + _QuestID + "') not found.");
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.network.clientpackets.L2GameClientPacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__64_REQUESTQUESTABORT;
	}
}