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
package com.l2jmobius.gameserver.network.clientpackets.mentoring;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.data.sql.impl.CharNameTable;
import com.l2jmobius.gameserver.instancemanager.MentorManager;
import com.l2jmobius.gameserver.model.L2Mentee;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.events.EventDispatcher;
import com.l2jmobius.gameserver.model.events.impl.character.player.mentoring.OnPlayerMenteeLeft;
import com.l2jmobius.gameserver.model.events.impl.character.player.mentoring.OnPlayerMenteeRemove;
import com.l2jmobius.gameserver.network.SystemMessageId;
import com.l2jmobius.gameserver.network.clientpackets.L2GameClientPacket;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * @author UnAfraid
 */
public class RequestMentorCancel extends L2GameClientPacket
{
	private int _confirmed;
	private String _name;
	
	@Override
	protected void readImpl()
	{
		_confirmed = readD();
		_name = readS();
	}
	
	@Override
	protected void runImpl()
	{
		if (_confirmed != 1)
		{
			return;
		}
		
		final L2PcInstance player = getClient().getActiveChar();
		final int objectId = CharNameTable.getInstance().getIdByName(_name);
		if (player != null)
		{
			if (player.isMentor())
			{
				final L2Mentee mentee = MentorManager.getInstance().getMentee(player.getObjectId(), objectId);
				if (mentee != null)
				{
					MentorManager.getInstance().cancelMentoringBuffs(mentee.getPlayerInstance());
					
					if (MentorManager.getInstance().isAllMenteesOffline(player.getObjectId(), mentee.getObjectId()))
					{
						MentorManager.getInstance().cancelMentoringBuffs(player);
					}
					
					player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THE_MENTORING_RELATIONSHIP_WITH_S1_HAS_BEEN_CANCELED_THE_MENTOR_CANNOT_OBTAIN_ANOTHER_MENTEE_FOR_TWO_DAYS).addString(_name));
					MentorManager.getInstance().setPenalty(player.getObjectId(), Config.MENTOR_PENALTY_FOR_MENTEE_LEAVE);
					MentorManager.getInstance().deleteMentor(player.getObjectId(), mentee.getObjectId());
					
					// Notify to scripts
					EventDispatcher.getInstance().notifyEventAsync(new OnPlayerMenteeRemove(player, mentee), player);
				}
			}
			else if (player.isMentee())
			{
				final L2Mentee mentor = MentorManager.getInstance().getMentor(player.getObjectId());
				if ((mentor != null) && (mentor.getObjectId() == objectId))
				{
					MentorManager.getInstance().cancelMentoringBuffs(player);
					
					if (MentorManager.getInstance().isAllMenteesOffline(mentor.getObjectId(), player.getObjectId()))
					{
						MentorManager.getInstance().cancelMentoringBuffs(mentor.getPlayerInstance());
					}
					
					MentorManager.getInstance().setPenalty(mentor.getObjectId(), Config.MENTOR_PENALTY_FOR_MENTEE_LEAVE);
					MentorManager.getInstance().deleteMentor(mentor.getObjectId(), player.getObjectId());
					
					// Notify to scripts
					EventDispatcher.getInstance().notifyEventAsync(new OnPlayerMenteeLeft(mentor, player), player);
					
					mentor.getPlayerInstance().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THE_MENTORING_RELATIONSHIP_WITH_S1_HAS_BEEN_CANCELED_THE_MENTOR_CANNOT_OBTAIN_ANOTHER_MENTEE_FOR_TWO_DAYS).addString(_name));
				}
			}
		}
	}
	
	@Override
	public String getType()
	{
		return getClass().getSimpleName();
	}
}
