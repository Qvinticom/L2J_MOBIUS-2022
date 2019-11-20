/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.l2jmobius.gameserver.handler.skillhandlers;

import java.util.List;

import org.l2jmobius.gameserver.handler.ISkillHandler;
import org.l2jmobius.gameserver.model.Skill;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.instance.PetInstance;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.serverpackets.StatusUpdate;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

public class HealSkill implements ISkillHandler
{
	private static final int SELF_HEAL = 1216;
	private static final int DEVINE_HEAL = 45;
	private static final int ELEMENTAL_HEAL = 58;
	private static final int HEAL = 1011;
	private static final int BATTLE_HEAL = 1015;
	private static final int GROUP_HEAL = 1027;
	private static final int SERVITOR_HEAL = 1127;
	private static final int GREATER_GROUP_HEAL = 1219;
	private static int[] _skillIds = new int[]
	{
		SELF_HEAL,
		DEVINE_HEAL,
		ELEMENTAL_HEAL,
		HEAL,
		BATTLE_HEAL,
		GROUP_HEAL,
		SERVITOR_HEAL,
		GREATER_GROUP_HEAL
	};
	
	@Override
	public void useSkill(PlayerInstance activeChar, Skill skill, WorldObject target)
	{
		if (skill.getTargetType() == Skill.TARGET_PET)
		{
			PetInstance pet = activeChar.getPet();
			double hp = pet.getCurrentHp();
			pet.setCurrentHp(hp += skill.getPower());
		}
		else if ((skill.getTargetType() == Skill.TARGET_PARTY) && activeChar.isInParty())
		{
			List<PlayerInstance> players = activeChar.getParty().getPartyMembers();
			for (int i = 0; i < players.size(); ++i)
			{
				PlayerInstance player = players.get(i);
				double hp = player.getCurrentHp();
				player.setCurrentHp(hp += skill.getPower());
				StatusUpdate su = new StatusUpdate(player.getObjectId());
				su.addAttribute(StatusUpdate.CUR_HP, (int) hp);
				player.sendPacket(su);
				player.sendPacket(new SystemMessage(25));
			}
		}
		else
		{
			double hp = activeChar.getCurrentHp();
			activeChar.setCurrentHp(hp += skill.getPower());
			StatusUpdate su = new StatusUpdate(activeChar.getObjectId());
			su.addAttribute(StatusUpdate.CUR_HP, (int) hp);
			activeChar.sendPacket(su);
			activeChar.sendPacket(new SystemMessage(25));
		}
	}
	
	@Override
	public int[] getSkillIds()
	{
		return _skillIds;
	}
}
