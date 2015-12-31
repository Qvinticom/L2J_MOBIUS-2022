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
package ai.npc.AdventurersGuide;

import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.holders.SkillHolder;
import com.l2jmobius.gameserver.model.skills.Skill;

import ai.npc.AbstractNpcAI;

/**
 * Adventurers Guide AI.
 * @author St3eT, Updated by Stayway, Mobius
 */
public final class AdventurersGuide extends AbstractNpcAI
{
	// NPCs
	private static final int[] ADVENTURER_GUIDES =
	{
		32327,
		33454, // Newbie Helper
		33950, // Apprentice
	};
	// Skills
	private static final SkillHolder BLESS_PROTECTION = new SkillHolder(5182, 1); // Blessing of Protection
	private static final SkillHolder KNIGHT = new SkillHolder(15648, 1); // Knight's Harmony (Adventurer)
	private static final SkillHolder WARRIOR = new SkillHolder(15649, 1); // Warrior's Harmony (Adventurer)
	private static final SkillHolder WIZARD = new SkillHolder(15650, 1); // Wizard's Harmony (Adventurer)
	private static final SkillHolder[] GROUP_BUFFS =
	{
		new SkillHolder(15642, 1), // Horn Melody (Adventurer)
		new SkillHolder(15643, 1), // Drum Melody (Adventurer)
		new SkillHolder(15644, 1), // Pipe Organ Melody (Adventurer)
		new SkillHolder(15645, 1), // Guitar Melody (Adventurer)
		new SkillHolder(15646, 1), // Harp Melody (Adventurer)
		new SkillHolder(15647, 1), // Lute Melody (Adventurer)
		new SkillHolder(15651, 1), // Prevailing Sonata (Adventurer)
		new SkillHolder(15652, 1), // Daring Sonata (Adventurer)
		new SkillHolder(15653, 1), // Refreshing Sonata (Adventurer)
	};
	
	private AdventurersGuide()
	{
		super(AdventurersGuide.class.getSimpleName(), "ai/npc");
		addStartNpc(ADVENTURER_GUIDES);
		addTalkId(ADVENTURER_GUIDES);
		addFirstTalkId(ADVENTURER_GUIDES);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = null;
		
		switch (event)
		{
			case "guide-01.html":
			case "guide-02.html":
			case "guide-03.html":
			case "guide-04.html":
			{
				htmltext = event;
				break;
			}
			case "weakenBreath":
			{
				if (player.getShilensBreathDebuffLevel() < 3)
				{
					htmltext = "guide-noBreath.html";
					break;
				}
				
				player.setShilensBreathDebuffLevel(2);
				htmltext = ""; // TODO: Any success html?
				break;
			}
			case "knight":
			{
				htmltext = applyBuffs(npc, player, KNIGHT.getSkill());
				break;
			}
			case "warrior":
			{
				htmltext = applyBuffs(npc, player, WARRIOR.getSkill());
				break;
			}
			case "wizard":
			{
				htmltext = applyBuffs(npc, player, WIZARD.getSkill());
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		return npc.getId() + ".html";
	}
	
	private String applyBuffs(L2Npc npc, L2PcInstance player, Skill skill)
	{
		if (player.getLevel() > 90)
		{
			return "guide-noBuffs.html";
		}
		
		for (SkillHolder holder : GROUP_BUFFS)
		{
			holder.getSkill().applyEffects(npc, player);
		}
		skill.applyEffects(npc, player);
		
		if ((player.getLevel() < 40) && (player.getClassId().level() <= 1))
		{
			BLESS_PROTECTION.getSkill().applyEffects(npc, player);
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new AdventurersGuide();
	}
}