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
package handlers.effecthandlers;

import java.util.Collection;

import com.l2jmobius.Config;
import com.l2jmobius.commons.util.Rnd;
import com.l2jmobius.gameserver.ai.CtrlEvent;
import com.l2jmobius.gameserver.model.L2Party;
import com.l2jmobius.gameserver.model.StatsSet;
import com.l2jmobius.gameserver.model.actor.L2Character;
import com.l2jmobius.gameserver.model.actor.instance.L2MonsterInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.effects.AbstractEffect;
import com.l2jmobius.gameserver.model.holders.ItemHolder;
import com.l2jmobius.gameserver.model.items.instance.L2ItemInstance;
import com.l2jmobius.gameserver.model.skills.Skill;
import com.l2jmobius.gameserver.network.SystemMessageId;

/**
 * @author Sdw
 */
public final class Plunder extends AbstractEffect
{
	public Plunder(StatsSet params)
	{
	}
	
	@Override
	public boolean calcSuccess(L2Character effector, L2Character effected, Skill skill)
	{
		final int lvlDifference = (effected.getLevel() - (skill.getMagicLevel() > 0 ? skill.getMagicLevel() : effector.getLevel()));
		final double lvlModifier = Math.pow(1.3, lvlDifference);
		float targetModifier = 1;
		if (effected.isAttackable() && !effected.isRaid() && !effected.isRaidMinion() && (effected.getLevel() >= Config.MIN_NPC_LVL_MAGIC_PENALTY) && (effector.getActingPlayer() != null) && ((effected.getLevel() - effector.getActingPlayer().getLevel()) >= 3))
		{
			final int lvlDiff = effected.getLevel() - effector.getActingPlayer().getLevel() - 2;
			if (lvlDiff >= Config.NPC_SKILL_CHANCE_PENALTY.size())
			{
				targetModifier = Config.NPC_SKILL_CHANCE_PENALTY.get(Config.NPC_SKILL_CHANCE_PENALTY.size() - 1);
			}
			else
			{
				targetModifier = Config.NPC_SKILL_CHANCE_PENALTY.get(lvlDiff);
			}
		}
		return Rnd.get(100) < (100 - Math.round((float) (lvlModifier * targetModifier)));
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public void instant(L2Character effector, L2Character effected, Skill skill, L2ItemInstance item)
	{
		if (!effector.isPlayer())
		{
			return;
		}
		else if (!effected.isMonster() || effected.isDead())
		{
			effector.sendPacket(SystemMessageId.INVALID_TARGET);
			return;
		}
		
		final L2MonsterInstance monster = (L2MonsterInstance) effected;
		final L2PcInstance player = effector.getActingPlayer();
		
		if (monster.isSpoiled())
		{
			effector.sendPacket(SystemMessageId.PLUNDER_SKILL_HAS_BEEN_ALREADY_USED_ON_THIS_TARGET);
			return;
		}
		
		if (!player.getInventory().checkInventorySlotsAndWeight(monster.getSpoilLootItems(), false, false))
		{
			return;
		}
		
		monster.setSpoilerObjectId(effector.getObjectId());
		
		if (monster.isSweepActive())
		{
			final Collection<ItemHolder> items = monster.takeSweep();
			if (items != null)
			{
				for (ItemHolder sweepedItem : items)
				{
					final L2Party party = effector.getParty();
					if (party != null)
					{
						party.distributeItem(player, sweepedItem, true, monster);
					}
					else
					{
						player.addItem("Plunder", sweepedItem, effected, true);
					}
				}
			}
		}
		monster.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, effector);
	}
}
