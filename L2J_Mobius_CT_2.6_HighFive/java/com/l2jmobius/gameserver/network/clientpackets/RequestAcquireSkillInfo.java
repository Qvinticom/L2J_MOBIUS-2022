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

import com.l2jmobius.commons.network.PacketReader;
import com.l2jmobius.gameserver.data.xml.impl.SkillData;
import com.l2jmobius.gameserver.data.xml.impl.SkillTreesData;
import com.l2jmobius.gameserver.model.SkillLearn;
import com.l2jmobius.gameserver.model.actor.Npc;
import com.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import com.l2jmobius.gameserver.model.base.AcquireSkillType;
import com.l2jmobius.gameserver.model.clan.ClanPrivilege;
import com.l2jmobius.gameserver.model.skills.Skill;
import com.l2jmobius.gameserver.network.GameClient;
import com.l2jmobius.gameserver.network.serverpackets.AcquireSkillInfo;

/**
 * Request Acquire Skill Info client packet implementation.
 * @author Zoey76
 */
public final class RequestAcquireSkillInfo implements IClientIncomingPacket
{
	private int _id;
	private int _level;
	private AcquireSkillType _skillType;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_id = packet.readD();
		_level = packet.readD();
		_skillType = AcquireSkillType.getAcquireSkillType(packet.readD());
		return true;
	}
	
	@Override
	public void run(GameClient client)
	{
		if ((_id <= 0) || (_level <= 0))
		{
			LOGGER.warning(RequestAcquireSkillInfo.class.getSimpleName() + ": Invalid Id: " + _id + " or level: " + _level + "!");
			return;
		}
		
		final PlayerInstance player = client.getPlayer();
		if (player == null)
		{
			return;
		}
		
		final Npc trainer = player.getLastFolkNPC();
		if ((trainer == null) || !trainer.isNpc() || (!trainer.canInteract(player) && !player.isGM()))
		{
			return;
		}
		
		final Skill skill = SkillData.getInstance().getSkill(_id, _level);
		if (skill == null)
		{
			LOGGER.warning(RequestAcquireSkillInfo.class.getSimpleName() + ": Skill Id: " + _id + " level: " + _level + " is undefined. " + RequestAcquireSkillInfo.class.getName() + " failed.");
			return;
		}
		
		// Hack check. Doesn't apply to all Skill Types
		final int prevSkillLevel = player.getSkillLevel(_id);
		if ((prevSkillLevel > 0) && !((_skillType == AcquireSkillType.TRANSFER) || (_skillType == AcquireSkillType.SUBPLEDGE)))
		{
			if (prevSkillLevel == _level)
			{
				LOGGER.warning(RequestAcquireSkillInfo.class.getSimpleName() + ": Player " + player.getName() + " is requesting info for a skill that already knows, Id: " + _id + " level: " + _level + "!");
			}
			else if (prevSkillLevel != (_level - 1))
			{
				LOGGER.warning(RequestAcquireSkillInfo.class.getSimpleName() + ": Player " + player.getName() + " is requesting info for skill Id: " + _id + " level " + _level + " without knowing it's previous level!");
			}
		}
		
		final SkillLearn s = SkillTreesData.getInstance().getSkillLearn(_skillType, _id, _level, player);
		if (s == null)
		{
			return;
		}
		
		switch (_skillType)
		{
			case TRANSFORM:
			case FISHING:
			case SUBCLASS:
			case COLLECT:
			case TRANSFER:
			{
				client.sendPacket(new AcquireSkillInfo(_skillType, s));
				break;
			}
			case CLASS:
			{
				if (trainer.getTemplate().canTeach(player.getLearningClass()))
				{
					final int customSp = s.getCalculatedLevelUpSp(player.getClassId(), player.getLearningClass());
					client.sendPacket(new AcquireSkillInfo(_skillType, s, customSp));
				}
				break;
			}
			case PLEDGE:
			{
				if (!player.isClanLeader())
				{
					return;
				}
				client.sendPacket(new AcquireSkillInfo(_skillType, s));
				break;
			}
			case SUBPLEDGE:
			{
				if (!player.isClanLeader() || !player.hasClanPrivilege(ClanPrivilege.CL_TROOPS_FAME))
				{
					return;
				}
				client.sendPacket(new AcquireSkillInfo(_skillType, s));
				break;
			}
		}
	}
}
