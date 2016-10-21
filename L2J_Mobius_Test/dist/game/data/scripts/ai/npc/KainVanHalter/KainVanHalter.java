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
package ai.npc.KainVanHalter;

import com.l2jmobius.gameserver.ai.CtrlIntention;
import com.l2jmobius.gameserver.enums.Race;
import com.l2jmobius.gameserver.model.L2Object;
import com.l2jmobius.gameserver.model.actor.L2Character;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2QuestGuardInstance;
import com.l2jmobius.gameserver.model.quest.QuestState;

import ai.AbstractNpcAI;
import quests.Q10791_TheManOfMystery.Q10791_TheManOfMystery;

/**
 * Kain Ven Halter AI
 * @author Gigi and
 */
public final class KainVanHalter extends AbstractNpcAI
{
	// NPC
	private static final int KAIN_VAN_HALTER = 33993;
	// Monster
	private static final int NEEDLE_STAKATO_CAPTAIN = 27542;
	private static final int NEEDLE_STAKATO = 27543;
	
	private KainVanHalter()
	{
		super(KainVanHalter.class.getSimpleName(), "ai/npc");
		addStartNpc(KAIN_VAN_HALTER);
		addFirstTalkId(KAIN_VAN_HALTER);
		addTalkId(KAIN_VAN_HALTER);
		addSeeCreatureId(KAIN_VAN_HALTER);
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		final QuestState qs = player.getQuestState(Q10791_TheManOfMystery.class.getSimpleName());
		if ((qs != null) && (player.getRace() == Race.ERTHEIA))
		{
			return "33993.html";
		}
		return super.onFirstTalk(npc, player);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if ("thank".equals(event))
		{
			npc.deleteMe();
			return "33993-01.html";
		}
		
		if (npc instanceof L2QuestGuardInstance)
		{
			final L2QuestGuardInstance kain = (L2QuestGuardInstance) npc;
			
			//@formatter:off
			final L2Npc stacato = (L2Npc) kain.getKnownList().getKnownCharactersInRadius(150)
				.stream()
				.filter(L2Object::isMonster)
				.filter(obj -> ((obj.getId() == NEEDLE_STAKATO_CAPTAIN) || (obj.getId() == NEEDLE_STAKATO)))
				.findFirst()
				.orElse(null);
			//@formatter:on
			
			if (stacato != null)
			{
				addAttackDesire(kain, stacato);
				kain.setCanStopAttackByTime(false);
				kain.setCanReturnToSpawnPoint(false);
				kain.setIsInvul(true);
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
			startQuestTimer("START_ATTACK", 1000, npc, null);
		}
		return super.onSeeCreature(npc, creature, isSummon);
	}
	
	public static void main(String[] args)
	{
		new KainVanHalter();
	}
}