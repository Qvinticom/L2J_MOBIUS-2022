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
package ai.others.OlyBuffer;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.SkillHolder;
import org.l2jmobius.gameserver.model.skill.SkillCaster;

import ai.AbstractNpcAI;

/**
 * Olympiad Buffer AI.
 * @author St3eT
 */
public class OlyBuffer extends AbstractNpcAI
{
	// NPC
	private static final int OLYMPIAD_BUFFER = 36402;
	// Skills
	private static final SkillHolder BUFF = new SkillHolder(32415, 1); // Fantasia Harmony
	private static final SkillHolder[] BUFFS =
	{
		new SkillHolder(32411, 1), // Olympiad - Horn Melody
		new SkillHolder(32412, 1), // Olympiad - Drum Melody
		new SkillHolder(32413, 1), // Olympiad - Pipe Organ Melody
		new SkillHolder(32414, 1), // Olympiad - Guitar Melody
	};
	
	private OlyBuffer()
	{
		addStartNpc(OLYMPIAD_BUFFER);
		addFirstTalkId(OLYMPIAD_BUFFER);
		addTalkId(OLYMPIAD_BUFFER);
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		String htmltext = null;
		if (npc.isScriptValue(0))
		{
			htmltext = "olympiad_master001.htm";
		}
		return htmltext;
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		switch (event)
		{
			case "buff":
			{
				applyBuffs(npc, player, BUFF);
				break;
			}
		}
		npc.setScriptValue(1);
		getTimers().addTimer("DELETE_ME", 5000, evnt -> npc.deleteMe());
		return "olympiad_master003.htm";
	}
	
	private void applyBuffs(Npc npc, Player player, SkillHolder skill)
	{
		for (SkillHolder holder : BUFFS)
		{
			SkillCaster.triggerCast(npc, player, holder.getSkill());
		}
		SkillCaster.triggerCast(npc, player, skill.getSkill());
	}
	
	public static void main(String[] args)
	{
		new OlyBuffer();
	}
}