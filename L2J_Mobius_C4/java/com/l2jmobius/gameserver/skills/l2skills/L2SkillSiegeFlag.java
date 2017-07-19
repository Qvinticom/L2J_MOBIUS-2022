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
package com.l2jmobius.gameserver.skills.l2skills;

import com.l2jmobius.gameserver.datatables.NpcTable;
import com.l2jmobius.gameserver.idfactory.IdFactory;
import com.l2jmobius.gameserver.instancemanager.SiegeManager;
import com.l2jmobius.gameserver.model.L2Character;
import com.l2jmobius.gameserver.model.L2Object;
import com.l2jmobius.gameserver.model.L2Skill;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2SiegeFlagInstance;
import com.l2jmobius.gameserver.model.entity.Siege;
import com.l2jmobius.gameserver.templates.StatsSet;

public class L2SkillSiegeFlag extends L2Skill
{
	private final int npcId;
	private L2PcInstance player;
	private Siege siege;
	
	public L2SkillSiegeFlag(StatsSet set)
	
	{
		super(set);
		npcId = set.getInteger("npcId", 0); // default for undescribed skills
	}
	
	@Override
	public boolean checkCondition(L2Character activeChar, boolean itemOrWeapon)
	{
		player = (L2PcInstance) activeChar;
		if (player == null)
		{
			return false;
		}
		
		siege = SiegeManager.getInstance().getSiege(player);
		
		if (siege == null)
		{
			player.sendMessage("You may only place a Siege Headquarter during a siege.");
			return false;
		}
		
		if ((player.getClan() == null) || !player.isClanLeader())
		{
			player.sendMessage("Only clan leaders may place a Siege Headquarter.");
			return false;
		}
		
		if (siege.getAttackerClan(player.getClan()) == null)
		{
			player.sendMessage("You may only place a Siege Headquarter provided that you are an attacker.");
			return false;
		}
		
		if (player.isInsideZone(L2Character.ZONE_NOHQ))
		{
			player.sendMessage("You may not place a Siege Headquarter inside a castle.");
			return false;
		}
		
		if (siege.getAttackerClan(player.getClan()).getNumFlags() >= SiegeManager.getInstance().getFlagMaxCount())
		{
			player.sendMessage("You have already placed a Siege Headquarter.");
			return false;
		}
		
		return super.checkCondition(activeChar, itemOrWeapon);
	}
	
	@Override
	public void useSkill(L2Character caster, L2Object[] targets)
	{
		try
		{
			// Spawn a new flag
			final L2SiegeFlagInstance flag = new L2SiegeFlagInstance(player, IdFactory.getInstance().getNextId(), NpcTable.getInstance().getTemplate(npcId));
			
			// Build Advanced Headquarters
			if (getId() == 326)
			{
				flag.setMaxSiegeHp(flag.getMaxHp() * 2);
			}
			
			flag.setTitle(player.getClan().getName());
			
			flag.setCurrentHpMp(flag.getMaxHp(), flag.getMaxMp());
			flag.setHeading(player.getHeading());
			flag.spawnMe(player.getX(), player.getY(), player.getZ() + 50);
			siege.getFlag(player.getClan()).add(flag);
		}
		catch (final Exception e)
		{
			player.sendMessage("Error placing flag:" + e);
		}
		
	}
}