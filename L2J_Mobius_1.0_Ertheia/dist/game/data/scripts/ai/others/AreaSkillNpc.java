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
package ai.others;

import com.l2jmobius.commons.util.CommonUtil;
import com.l2jmobius.gameserver.model.StatsSet;
import com.l2jmobius.gameserver.model.actor.L2Character;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.holders.SkillHolder;

import ai.AbstractNpcAI;

/**
 * Area Skill Npc AI.
 * @author St3eT
 */
public final class AreaSkillNpc extends AbstractNpcAI
{
	// NPCs
	private static final int[] BASIC = // area_skill_npc
	{
		13018, // Maximum Defense
		13019, // Anti-Music
		13020, // Maximum Resist Status
		13021, // Maximum Recovery
		13022, // Recover Force
		13023, // Maximize long-range weapon use
		13024, // Smokescreen
		13028, // Day of Doom
		13030, // Anti-Summoning Field
		13323, // Whisper of Fear
		13324, // Whisper of Fear
		13325, // Whisper of Fear
		13310, // Solo Dance
		13377, // Solo Dance
		13378, // Solo Dance
		13379, // Solo Dance
		13380, // Solo Dance
		13381, // Solo Dance
	};
	private static final int[] TOTEMS = // ai_totem_of_body
	{
		143, // Totem of Body
		144, // Totem of Spirit
		145, // Totem of Bravery
		146, // Totem of Fortitude
	};
	private static final int[] DECOY = // ai_decoy
	{
		13071, // Virtual Image
		13072, // Virtual Image
		13073, // Virtual Image
		13074, // Virtual Image
		13075, // Virtual Image
		13076, // Virtual Image
		13257, // Virtual Image
		13258, // Virtual Image
		13259, // Virtual Image
		13260, // Virtual Image
		13261, // Virtual Image
		13262, // Virtual Image
		13263, // Virtual Image
		13264, // Virtual Image
		13265, // Virtual Image
		13266, // Virtual Image
		13267, // Virtual Image
		13328, // Decoy
	};
	
	private AreaSkillNpc()
	{
		addSpawnId(BASIC);
		addSpawnId(TOTEMS);
		addSpawnId(DECOY);
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		final L2Character summoner = npc.getSummoner();
		if ((summoner != null) && summoner.isPlayer())
		{
			final L2PcInstance player = summoner.getActingPlayer();
			
			if (CommonUtil.contains(BASIC, npc.getId()) || CommonUtil.contains(TOTEMS, npc.getId()))
			{
				final int despawnTime = npc.getTemplate().getParameters().getInt("despawn_time", 7);
				getTimers().addTimer("SKILL_CAST_BASIC", 100, npc, player);
				getTimers().addTimer("DELETE_ME", (despawnTime * 1000), npc, player);
			}
			else if (CommonUtil.contains(DECOY, npc.getId()))
			{
				final int castTime = npc.getTemplate().getParameters().getInt("i_use_term_time", 5000);
				final int despawnTime = npc.getTemplate().getParameters().getInt("i_despawn_time", 30000);
				onTimerEvent("SKILL_CAST_BASIC", null, npc, player); // Trigger cast instantly
				getTimers().addTimer("SKILL_CAST_BASIC", castTime, npc, player);
				getTimers().addTimer("DELETE_ME", despawnTime, npc, player);
			}
		}
		return super.onSpawn(npc);
	}
	
	@Override
	public void onTimerEvent(String event, StatsSet params, L2Npc npc, L2PcInstance player)
	{
		if (event.equals("SKILL_CAST_BASIC"))
		{
			final SkillHolder skill = npc.getParameters().getSkillHolder(CommonUtil.contains(DECOY, npc.getId()) ? "decoy_skill" : "union_skill");
			final int skillDelay = npc.getParameters().getInt("skill_delay", 2);
			
			if (skill != null)
			{
				npc.doCast(skill.getSkill());
				getTimers().addTimer("SKILL_CAST_BASIC", skillDelay * 1000, npc, player);
			}
		}
		else if (event.equals("DELETE_ME"))
		{
			getTimers().cancelTimer("SKILL_CAST_BASIC", npc, player);
			npc.deleteMe();
		}
	}
	
	public static void main(String[] args)
	{
		new AreaSkillNpc();
	}
}