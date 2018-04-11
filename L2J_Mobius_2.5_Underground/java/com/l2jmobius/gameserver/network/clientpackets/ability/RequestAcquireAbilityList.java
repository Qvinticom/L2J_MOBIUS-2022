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
package com.l2jmobius.gameserver.network.clientpackets.ability;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.l2jmobius.commons.network.PacketReader;
import com.l2jmobius.gameserver.data.xml.impl.SkillData;
import com.l2jmobius.gameserver.data.xml.impl.SkillTreesData;
import com.l2jmobius.gameserver.model.L2SkillLearn;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.ceremonyofchaos.CeremonyOfChaosEvent;
import com.l2jmobius.gameserver.model.holders.SkillHolder;
import com.l2jmobius.gameserver.model.skills.Skill;
import com.l2jmobius.gameserver.network.L2GameClient;
import com.l2jmobius.gameserver.network.SystemMessageId;
import com.l2jmobius.gameserver.network.clientpackets.IClientIncomingPacket;
import com.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import com.l2jmobius.gameserver.network.serverpackets.ability.ExAcquireAPSkillList;

/**
 * @author UnAfraid
 */
public class RequestAcquireAbilityList implements IClientIncomingPacket
{
	private static final int TREE_SIZE = 3;
	private final Map<Integer, SkillHolder> _skills = new LinkedHashMap<>();
	
	@Override
	public boolean read(L2GameClient client, PacketReader packet)
	{
		packet.readD(); // Total size
		for (int i = 0; i < TREE_SIZE; i++)
		{
			final int size = packet.readD();
			for (int j = 0; j < size; j++)
			{
				final SkillHolder holder = new SkillHolder(packet.readD(), packet.readD());
				if (holder.getSkillLevel() < 1)
				{
					LOGGER.warning("Player " + client + " is trying to learn skill " + holder + " by sending packet with level 0!");
					return false;
				}
				if (_skills.putIfAbsent(holder.getSkillId(), holder) != null)
				{
					LOGGER.warning("Player " + client + " is trying to send two times one skill " + holder + " to learn!");
					return false;
				}
			}
		}
		return true;
	}
	
	@Override
	public void run(L2GameClient client)
	{
		final L2PcInstance activeChar = client.getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if (activeChar.isSubClassActive() && !activeChar.isDualClassActive())
		{
			return;
		}
		
		if ((activeChar.getAbilityPoints() == 0) || (activeChar.getAbilityPoints() == activeChar.getAbilityPointsUsed()))
		{
			LOGGER.warning("Player " + activeChar + " is trying to learn ability without ability points!");
			return;
		}
		
		if ((activeChar.getLevel() < 99) || (activeChar.getNobleLevel() == 0))
		{
			activeChar.sendPacket(SystemMessageId.ABILITIES_CAN_BE_USED_BY_NOBLESSE_EXALTED_LV_99_OR_ABOVE);
			return;
		}
		else if (activeChar.isInOlympiadMode() || activeChar.isOnEvent(CeremonyOfChaosEvent.class))
		{
			activeChar.sendPacket(SystemMessageId.YOU_CANNOT_USE_OR_RESET_ABILITY_POINTS_WHILE_PARTICIPATING_IN_THE_OLYMPIAD_OR_CEREMONY_OF_CHAOS);
			return;
		}
		else if (activeChar.isOnEvent()) // custom event message
		{
			activeChar.sendMessage("You cannot use or reset Ability Points while participating in an event.");
			return;
		}
		
		final int[] pointsSpent = new int[TREE_SIZE];
		Arrays.fill(pointsSpent, 0);
		
		final List<L2SkillLearn> skillsToLearn = new ArrayList<>(_skills.size());
		for (SkillHolder holder : _skills.values())
		{
			final L2SkillLearn learn = SkillTreesData.getInstance().getAbilitySkill(holder.getSkillId(), holder.getSkillLevel());
			if (learn == null)
			{
				LOGGER.warning("SkillLearn " + holder.getSkillId() + " (" + holder.getSkillLevel() + ") not found!");
				client.sendPacket(ActionFailed.STATIC_PACKET);
				break;
			}
			
			final Skill skill = holder.getSkill();
			if (skill == null)
			{
				LOGGER.warning("Skill " + holder.getSkillId() + " (" + holder.getSkillLevel() + ") not found!");
				client.sendPacket(ActionFailed.STATIC_PACKET);
				break;
			}
			
			if (activeChar.getSkillLevel(skill.getId()) > 0)
			{
				pointsSpent[learn.getTreeId() - 1] += skill.getLevel();
			}
			
			skillsToLearn.add(learn);
		}
		
		// Sort the skills by their tree id -> row -> column
		skillsToLearn.sort(Comparator.comparingInt(L2SkillLearn::getTreeId).thenComparing(L2SkillLearn::getRow).thenComparing(L2SkillLearn::getColumn));
		
		for (L2SkillLearn learn : skillsToLearn)
		{
			final Skill skill = SkillData.getInstance().getSkill(learn.getSkillId(), learn.getSkillLevel());
			final int points;
			final int knownLevel = activeChar.getSkillLevel(skill.getId());
			if (knownLevel == 0) // player didn't knew it at all!
			{
				points = learn.getSkillLevel();
			}
			else
			{
				points = learn.getSkillLevel() - knownLevel;
			}
			
			// Case 1: Learning skill without having X points spent on the specific tree
			if (learn.getPointsRequired() > pointsSpent[learn.getTreeId() - 1])
			{
				LOGGER.warning("Player " + activeChar + " is trying to learn " + skill + " without enough ability points spent!");
				client.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			// Case 2: Learning skill without having its parent
			for (SkillHolder required : learn.getPreReqSkills())
			{
				if (activeChar.getSkillLevel(required.getSkillId()) < required.getSkillLevel())
				{
					LOGGER.warning("Player " + activeChar + " is trying to learn " + skill + " without having prerequsite skill: " + required.getSkill() + "!");
					client.sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
			}
			
			// Case 3 Learning a skill without having enough points
			if ((activeChar.getAbilityPoints() - activeChar.getAbilityPointsUsed()) < points)
			{
				LOGGER.warning("Player " + activeChar + " is trying to learn ability without ability points!");
				client.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			pointsSpent[learn.getTreeId() - 1] += points;
			
			activeChar.addSkill(skill, true);
			activeChar.setAbilityPointsUsed(activeChar.getAbilityPointsUsed() + points);
		}
		activeChar.sendPacket(new ExAcquireAPSkillList(activeChar));
	}
}