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

import com.l2jmobius.commons.util.CommonUtil;
import com.l2jmobius.gameserver.data.xml.impl.SkillData;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.skills.Skill;

import ai.AbstractNpcAI;

/**
 * Olympiad Buffer AI.
 * @author St3eT
 */
public final class OlyBuffer extends AbstractNpcAI
{
	// NPC
	private static final int OLYMPIAD_BUFFER = 36402;
	// Skills
	private static final int[] ALLOWED_BUFFS =
	{
		14738, // Olympiad - Horn Melody
		14739, // Olympiad - Drum Melody
		14740, // Olympiad - Pipe Organ Melody
		14741, // Olympiad - Guitar Melody
		14742, // Olympiad - Harp Melody
		14743, // Olympiad - Lute Melody
		14744, // Olympiad - Knight's Harmony
		14745, // Olympiad - Warrior's Harmony
		14746, // Olympiad - Wizard's Harmony
	};
	
	private OlyBuffer()
	{
		addStartNpc(OLYMPIAD_BUFFER);
		addFirstTalkId(OLYMPIAD_BUFFER);
		addTalkId(OLYMPIAD_BUFFER);
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = null;
		if (npc.getScriptValue() < 5)
		{
			htmltext = "OlyBuffer-index.html";
		}
		return htmltext;
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = null;
		if (event.startsWith("giveBuff;") && (npc.getScriptValue() < 5))
		{
			final int buffId = Integer.parseInt(event.replace("giveBuff;", ""));
			if (CommonUtil.contains(ALLOWED_BUFFS, buffId))
			{
				final Skill buff = SkillData.getInstance().getSkill(buffId, 1);
				if (buff != null)
				{
					npc.setScriptValue(npc.getScriptValue() + 1);
					addSkillCastDesire(npc, player, buff, 23);
					htmltext = "OlyBuffer-afterBuff.html";
				}
				
				if (npc.getScriptValue() >= 5)
				{
					htmltext = "OlyBuffer-noMore.html";
					startQuestTimer("DELETE_ME", 5000, npc, null);
				}
			}
		}
		else if (event.equals("DELETE_ME") && (npc != null))
		{
			npc.deleteMe();
		}
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new OlyBuffer();
	}
}