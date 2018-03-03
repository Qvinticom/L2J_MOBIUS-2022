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

import com.l2jmobius.gameserver.datatables.SkillTable;
import com.l2jmobius.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.quest.Quest;

/**
 * @author Mobius
 * @note Based on python script
 */
public class AncientEgg extends Quest
{
	// NPC
	private static final int EGG = 18344; // Ancient Egg
	// Skill
	private static final int SIGNAL = 5088; // Signal
	
	private AncientEgg(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addAttackId(EGG);
	}
	
	@Override
	public String onAttack(L2NpcInstance npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		attacker.setTarget(attacker);
		attacker.doCast(SkillTable.getInstance().getInfo(SIGNAL, 1));
		return null;
	}
	
	public static void main(String[] args)
	{
		new AncientEgg(-1, "AncientEgg", "ai");
	}
}
