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
package ai.zones.TalkingIsland;

import com.l2jmobius.gameserver.ai.CtrlIntention;
import com.l2jmobius.gameserver.model.L2Object;
import com.l2jmobius.gameserver.model.actor.L2Character;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2QuestGuardInstance;

import ai.AbstractNpcAI;

/**
 * Trainning Soldier AI.
 * @author St3eT
 */
final class TrainningSoldier extends AbstractNpcAI
{
	// NPCs
	private static final int SOLDIER = 33201; // Trainning Soldier
	private static final int DUMMY = 33023; // Trainning Dummy
	
	private TrainningSoldier()
	{
		super(TrainningSoldier.class.getSimpleName(), "ai/zones/TalkingIsland");
		addSeeCreatureId(SOLDIER);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equals("START_ATTACK") && (npc instanceof L2QuestGuardInstance))
		{
			final L2QuestGuardInstance soldier = (L2QuestGuardInstance) npc;
			
			//@formatter:off
			final L2Npc dummy = (L2Npc) soldier.getKnownList().getKnownCharactersInRadius(150)
				.stream()
				.filter(L2Object::isNpc)
				.filter(obj -> (obj.getId() == DUMMY))
				.findFirst()
				.orElse(null);
			//@formatter:on
			
			if (dummy != null)
			{
				soldier.reduceCurrentHp(1, dummy, null); // TODO: Find better way for attack
				dummy.reduceCurrentHp(1, soldier, null);
				soldier.setCanStopAttackByTime(false);
				soldier.setCanReturnToSpawnPoint(false);
				soldier.setIsInvul(true);
			}
			else
			{
				startQuestTimer("START_ATTACK", 250, npc, null);
			}
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onSeeCreature(L2Npc npc, L2Character creature, boolean isSummon)
	{
		if (creature.isPlayer() && (npc.getAI().getIntention() != CtrlIntention.AI_INTENTION_ATTACK))
		{
			startQuestTimer("START_ATTACK", 250, npc, null);
		}
		return super.onSeeCreature(npc, creature, isSummon);
	}
	
	public static void main(String[] args)
	{
		new TrainningSoldier();
	}
}