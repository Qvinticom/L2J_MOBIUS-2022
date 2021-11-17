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

import org.l2jmobius.gameserver.handler.ISkillHandler;
import org.l2jmobius.gameserver.model.Skill;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.instance.Monster;
import org.l2jmobius.gameserver.model.actor.instance.Pet;
import org.l2jmobius.gameserver.network.serverpackets.StatusUpdate;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

public class HealSkill implements ISkillHandler
{
	private static final int[] SKILL_IDS = new int[]
	{
		45, // Divine Heal
		58, // Elemental Heal
		1011, // Heal
		1015, // Battle Heal
		1027, // Group Heal
		1127, // Servitor Heal
		1216, // Self Heal
		1219, // Greater Group Heal
	};
	
	@Override
	public void useSkill(Player activeChar, Skill skill, WorldObject target)
	{
		// PvP flag.
		if (target instanceof Monster)
		{
			activeChar.updatePvPFlag(1);
		}
		final Player enemyPlayer = target.getActingPlayer();
		if ((enemyPlayer != null) && ((enemyPlayer.getPvpFlag() > 0) || (enemyPlayer.getKarma() > 0)))
		{
			activeChar.updatePvPFlag(1);
		}
		
		if (skill.getTargetType() == Skill.TARGET_PET)
		{
			final Pet pet = activeChar.getPet();
			double hp = pet.getCurrentHp();
			pet.setCurrentHp(hp += skill.getPower());
		}
		else if ((skill.getTargetType() == Skill.TARGET_PARTY) && activeChar.isInParty())
		{
			for (Player player : activeChar.getParty().getPartyMembers())
			{
				double hp = player.getCurrentHp();
				player.setCurrentHp(hp += skill.getPower());
				final StatusUpdate su = new StatusUpdate(player.getObjectId());
				su.addAttribute(StatusUpdate.CUR_HP, (int) hp);
				player.sendPacket(su);
				player.sendPacket(new SystemMessage(SystemMessage.REJUVENATING_HP));
			}
		}
		else
		{
			double hp = activeChar.getCurrentHp();
			activeChar.setCurrentHp(hp += skill.getPower());
			final StatusUpdate su = new StatusUpdate(activeChar.getObjectId());
			su.addAttribute(StatusUpdate.CUR_HP, (int) hp);
			activeChar.sendPacket(su);
			activeChar.sendPacket(new SystemMessage(SystemMessage.REJUVENATING_HP));
		}
	}
	
	@Override
	public int[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
