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
import com.l2jmobius.gameserver.model.L2World;
import com.l2jmobius.gameserver.model.actor.instance.L2CubicInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2SiegeSummonInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2SummonInstance;
import com.l2jmobius.gameserver.model.base.Experience;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import com.l2jmobius.gameserver.templates.L2NpcTemplate;
import com.l2jmobius.gameserver.templates.StatsSet;

public class L2SkillSummon extends L2Skill
{
	private final int npcId;
	private final float expPenalty;
	private final boolean isCubic;
	
	public L2SkillSummon(StatsSet set)
	{
		super(set);
		
		npcId = set.getInteger("npcId", 0); // default for undescribed skills
		expPenalty = set.getFloat("expPenalty", 0.f);
		isCubic = set.getBool("isCubic", false);
	}
	
	@Override
	public boolean checkCondition(L2Character activeChar, boolean itemOrWeapon)
	{
		if (activeChar instanceof L2PcInstance)
		{
			final L2PcInstance player = (L2PcInstance) activeChar;
			
			if (player.inObserverMode())
			{
				return false;
			}
			
			if (!isCubic)
			{
				if ((player.getPet() != null) || player.isMounted())
				{
					player.sendMessage("You already have a pet.");
					return false;
				}
				
				if (player.isAttackingNow() || player.isRooted())
				{
					player.sendPacket(new SystemMessage(SystemMessage.YOU_CANNOT_SUMMON_IN_COMBAT));
					return false;
				}
			}
			
			// If summon siege golem (13), Summon Wild Hog Cannon (299), check if its ok to summon
			if (((getId() == 13) || (getId() == 299)) && !SiegeManager.getInstance().checkIfOkToSummon(player, false))
			{
				return false;
			}
			
		}
		return super.checkCondition(activeChar, itemOrWeapon);
	}
	
	@Override
	public void useSkill(L2Character caster, L2Object[] targets)
	{
		if (caster.isAlikeDead() || !(caster instanceof L2PcInstance))
		{
			return;
		}
		
		final L2PcInstance activeChar = (L2PcInstance) caster;
		
		if (npcId == 0)
		{
			return;
		}
		
		if (isCubic)
		{
			for (int index = 0; index < targets.length; index++)
			{
				
				if (!(targets[index] instanceof L2PcInstance))
				{
					continue;
				}
				
				final L2PcInstance target = (L2PcInstance) targets[index];
				
				int mastery = target.getSkillLevel(L2Skill.SKILL_CUBIC_MASTERY);
				if (mastery < 0)
				{
					mastery = 0;
				}
				
				if ((mastery == 0) && (target.getCubics().size() > 0) && !target.getCubics().containsKey(npcId))
				{
					// Player can have only 1 cubic - we should replace old cubic with new one
					for (final L2CubicInstance c : target.getCubics().values())
					{
						c.stopAction();
						c.cancelDisappear();
					}
					target.getCubics().clear();
				}
				
				if ((target.getCubics().size() > mastery) || target.getCubics().containsKey(npcId))
				{
					continue;
				}
				
				if (target == activeChar)
				{
					target.addCubic(npcId, getLevel(), false);
				}
				else
				{
					target.addCubic(npcId, getLevel(), true);
				}
				target.broadcastUserInfo();
				
			}
			
			return;
		}
		
		final L2NpcTemplate summonTemplate = NpcTable.getInstance().getTemplate(npcId);
		
		L2SummonInstance summon;
		if (summonTemplate.type.equalsIgnoreCase("L2SiegeSummon"))
		{
			summon = new L2SiegeSummonInstance(IdFactory.getInstance().getNextId(), summonTemplate, activeChar, this);
		}
		else
		{
			summon = new L2SummonInstance(IdFactory.getInstance().getNextId(), summonTemplate, activeChar, this);
		}
		
		summon.setName(summonTemplate.name);
		summon.setTitle(activeChar.getName());
		summon.setExpPenalty(expPenalty);
		if (summon.getLevel() >= Experience.LEVEL.length)
		{
			summon.getStat().setExp(Experience.LEVEL[Experience.LEVEL.length - 1]);
			_log.warning("Summon (" + summon.getName() + ") NpcID: " + summon.getNpcId() + " has a level above 78. Please rectify.");
		}
		else
		{
			summon.getStat().setExp(Experience.LEVEL[(summon.getLevel() % Experience.LEVEL.length)]);
		}
		
		summon.setCurrentHp(summon.getMaxHp());
		summon.setCurrentMp(summon.getMaxMp());
		summon.setHeading(activeChar.getHeading());
		summon.setRunning();
		activeChar.setPet(summon);
		
		L2World.getInstance().storeObject(summon);
		summon.spawnMe(activeChar.getX() + 50, activeChar.getY() + 100, activeChar.getZ());
	}
}