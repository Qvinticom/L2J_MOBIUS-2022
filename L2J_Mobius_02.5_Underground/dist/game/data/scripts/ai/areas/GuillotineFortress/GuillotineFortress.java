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
package ai.areas.GuillotineFortress;

import org.l2jmobius.gameserver.enums.SkillFinishType;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.SkillHolder;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.network.NpcStringId;
import org.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;

import ai.AbstractNpcAI;

/**
 * Guillotine Fortress AI.
 * @author Gigi
 */
public class GuillotineFortress extends AbstractNpcAI
{
	private static final int[] MONSTERS =
	{
		23199, // Sadiac the Killer
		23200, // Turan the Severed Ghost
		23201, // Nagdu the Deformed Merman
		23202, // Hakal the Butchered
		23203, // Centa the Standing Beast
		23204, // Adidaiu of the Killer Fangs
		23205, // Haskal the Floating Ghost
		23206, // Samita the Ex-Torture Expert
		23207, // Gazem
		23208, // Rosenia's Divine Spirit
		23209, // Kelbara the Dualsword Slaughterer
		23212, // Scaldisect the Furious
		23242, // Pafuron
		23243, // Krutati
		23244, // Alluring Irene
		23245 // Isaad
	};
	private static final SkillHolder CHAOS_SHIELD = new SkillHolder(15208, 9);
	private static final int PROOF_OF_SURVIVAL = 34898;
	private static final int SCALDISECT_THE_FURIOUS = 23212;
	
	public GuillotineFortress()
	{
		addAttackId(MONSTERS);
		addKillId(MONSTERS);
	}
	
	@Override
	public String onAttack(Npc npc, Player player, int damage, boolean isPet, Skill skill)
	{
		if ((npc.getCurrentHpPercent() <= 85) && npc.isScriptValue(1))
		{
			npc.getEffectList().stopSkillEffects(SkillFinishType.REMOVED, CHAOS_SHIELD.getSkillId());
			if (player.getParty() == null)
			{
				player.broadcastPacket(new ExShowScreenMessage(NpcStringId.CHAOS_SHIELD_BREAKTHROUGH, ExShowScreenMessage.BOTTOM_CENTER, 10000, false));
			}
			else
			{
				for (Player mem : player.getParty().getMembers())
				{
					mem.broadcastPacket(new ExShowScreenMessage(NpcStringId.CHAOS_SHIELD_BREAKTHROUGH, ExShowScreenMessage.BOTTOM_CENTER, 10000, false));
				}
			}
		}
		else if ((npc.getCurrentHpPercent() > 85) && npc.isScriptValue(0))
		{
			addSkillCastDesire(npc, npc, CHAOS_SHIELD, 23);
			npc.setScriptValue(1);
		}
		
		if ((player.getInventory().getItemByItemId(PROOF_OF_SURVIVAL) != null) && (getRandom(100) < 1))
		{
			addSpawn(SCALDISECT_THE_FURIOUS, player.getX(), player.getY(), player.getZ(), 0, true, 120000);
			takeItems(player, PROOF_OF_SURVIVAL, 1);
		}
		return super.onAttack(npc, player, damage, isPet, skill);
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		if ((killer != null) && killer.isPlayer() && (getRandom(100) < 5))
		{
			giveItems(killer, PROOF_OF_SURVIVAL, 1);
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	public static void main(String[] args)
	{
		new GuillotineFortress();
	}
}