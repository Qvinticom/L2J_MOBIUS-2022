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
package org.l2jmobius.gameserver.network.clientpackets.classchange;

import org.l2jmobius.Config;
import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.enums.CategoryType;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.clientpackets.IClientIncomingPacket;
import org.l2jmobius.gameserver.network.serverpackets.classchange.ExRequestClassChangeUi;

/**
 * @author Mobius
 */
public class ExRequestClassChangeVerifying implements IClientIncomingPacket
{
	private int _classId;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_classId = packet.readD();
		return true;
	}
	
	@Override
	public void run(GameClient client)
	{
		final PlayerInstance player = client.getPlayer();
		if (player == null)
		{
			return;
		}
		
		if (_classId != player.getClassId().getId())
		{
			return;
		}
		
		if (player.isInCategory(CategoryType.SIXTH_CLASS_GROUP))
		{
			return;
		}
		
		if (player.isInCategory(CategoryType.FOURTH_CLASS_GROUP))
		{
			if (!fourthClassCheck(player))
			{
				return;
			}
		}
		else if (player.isInCategory(CategoryType.THIRD_CLASS_GROUP))
		{
			if (!thirdClassCheck(player))
			{
				return;
			}
		}
		else if (player.isInCategory(CategoryType.SECOND_CLASS_GROUP))
		{
			if (!secondClassCheck(player))
			{
				return;
			}
		}
		else if (player.isInCategory(CategoryType.FIRST_CLASS_GROUP))
		{
			if (!firstClassCheck(player))
			{
				return;
			}
		}
		
		player.sendPacket(ExRequestClassChangeUi.STATIC_PACKET);
	}
	
	private boolean firstClassCheck(PlayerInstance player)
	{
		if (Config.DISABLE_TUTORIAL)
		{
			return true;
		}
		
		final QuestState qs = player.getQuestState("Q11032_CurseOfUndying");
		return (qs != null) && qs.isCompleted();
	}
	
	private boolean secondClassCheck(PlayerInstance player)
	{
		if (Config.DISABLE_TUTORIAL)
		{
			return true;
		}
		
		final QuestState qs = player.getQuestState("Q11025_PathOfDestinyProving");
		return (qs != null) && qs.isCompleted();
	}
	
	private boolean thirdClassCheck(PlayerInstance player)
	{
		if (Config.DISABLE_TUTORIAL)
		{
			return true;
		}
		
		final QuestState qs = player.getQuestState("Q11026_PathOfDestinyConviction");
		return (qs != null) && qs.isCompleted();
	}
	
	private boolean fourthClassCheck(PlayerInstance player)
	{
		if (Config.DISABLE_TUTORIAL)
		{
			return true;
		}
		
		final QuestState qs = player.getQuestState("Q11027_PathOfDestinyOvercome");
		return (qs != null) && qs.isCompleted();
	}
}