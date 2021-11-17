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
package ai.areas.SeaOfSpores;

import java.util.ArrayList;
import java.util.List;

import org.l2jmobius.commons.util.CommonUtil;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.SkillHolder;

import ai.AbstractNpcAI;

/**
 * @author NviX
 */
public class SeaOfSpores extends AbstractNpcAI
{
	// Mobs
	private static final int[] GROUP_MOBS =
	{
		24227, // Keros
		24228, // Falena
		24229, // Atrofa
		24230, // Nuba
		24231 // Torfedo
	};
	private static final int[] SOLO_MOBS =
	{
		24234, // Lesatanas
		24235, // Arbor
		24236, // Tergus
		24237, // Skeletus
		24238, // Atrofine
	};
	// Special Mobs
	private static final int ARANEA = 24226;
	private static final int ARIMA = 24232;
	private static final int HARANE = 24233;
	private static final int ARIMUS = 24239;
	// Skills
	private static final SkillHolder ABSORB_ENERGY1 = new SkillHolder(32483, 1);
	private static final SkillHolder ABSORB_ENERGY2 = new SkillHolder(32481, 1);
	private static final SkillHolder REFINED_ENERGY = new SkillHolder(32485, 1);
	
	private SeaOfSpores()
	{
		addAttackId(ARIMA, ARIMUS);
		addKillId(SOLO_MOBS);
		addKillId(GROUP_MOBS);
		addKillId(ARIMA, ARIMUS);
	}
	
	@Override
	public String onAttack(Npc npc, Player attacker, int damage, boolean isSummon)
	{
		if ((npc.getCurrentHp() <= (npc.getMaxHp() * 0.7)) && !npc.isCastingNow())
		{
			if (npc.getId() == ARIMA)
			{
				npc.doCast(ABSORB_ENERGY2.getSkill());
			}
			else if (npc.getId() == ARIMUS)
			{
				npc.doCast(ABSORB_ENERGY1.getSkill());
			}
		}
		return super.onAttack(npc, attacker, damage, isSummon);
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		if (CommonUtil.contains(SOLO_MOBS, npc.getId()) && (getRandom(1000) < 2))
		{
			addSpawn(ARIMUS, npc, false, 300000);
		}
		else if (CommonUtil.contains(GROUP_MOBS, npc.getId()) && (getRandom(1000) < 2))
		{
			addSpawn(ARIMA, npc, false, 300000);
		}
		else if (npc.getId() == ARIMA)
		{
			List<Player> members = new ArrayList<>();
			if (killer.getParty() != null)
			{
				members = killer.getParty().getMembers();
			}
			else
			{
				members.add(killer);
			}
			for (Player member : members)
			{
				member.doCast(REFINED_ENERGY.getSkill());
			}
			if (getRandom(1000) < 2)
			{
				addSpawn(ARANEA, npc, false, 300000);
				addSpawn(HARANE, npc, false, 300000);
			}
		}
		else if (npc.getId() == ARIMUS)
		{
			List<Player> members = new ArrayList<>();
			if (killer.getParty() != null)
			{
				members = killer.getParty().getMembers();
			}
			else
			{
				members.add(killer);
			}
			for (Player member : members)
			{
				member.doCast(REFINED_ENERGY.getSkill());
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	public static void main(String[] args)
	{
		new SeaOfSpores();
	}
}
