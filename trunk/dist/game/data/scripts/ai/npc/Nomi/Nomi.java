/*
 * Copyright (C) 2004-2015 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package ai.npc.Nomi;

import ai.npc.AbstractNpcAI;

import com.l2jserver.gameserver.model.Location;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.holders.SkillHolder;
import com.l2jserver.gameserver.model.skills.Skill;

/**
 * Nomi AI.
 * @author Stayway
 */
public final class Nomi extends AbstractNpcAI
{
	// NPC
	private static final int NOMI = 34007;
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
	// Others
	private static final Location[] SPAWN_LOCATIONS =
	{
		new Location(-79687, 247655, -3480, 29783), // Faeron
		new Location(83505, 148375, -3405, 31863), // Giran
		new Location(147445, 26874, -2204, 48319), // Aden
		new Location(147722, -56376, -2781, 20021), // Goddart
		new Location(42269, -48241, -803, 0), // Rune
		new Location(18599, 145485, -3126, 30873), // Dion
		new Location(82562, 53847, -1488, 65199), // Oren
		new Location(-14535, 123732, -3104, 20229), // Gludio
		new Location(-80687, 150254, -3044, 46168), // Gludin
		new Location(87340, -141620, -1341, 16383), // Schuttgart
		new Location(111728, 219962, -3659, 473), // Heine
		new Location(116514, 75899, -2730, 6558), // Hunters
		new Location(207393, 89693, -1104, 388), // Arcan
	};
	private static final int MAX_BLESS_PROTECTION_LEVEL = 40;
	private static final int MAX_BUFF_LEVEL = 90;
	
	private Nomi()
	{
		super(Nomi.class.getSimpleName(), "ai/npc");
		addStartNpc(NOMI);
		addTalkId(NOMI);
		addFirstTalkId(NOMI);
		
		for (Location loc : SPAWN_LOCATIONS)
		{
			addSpawn(NOMI, loc, false, 0);
		}
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = null;
		
		switch (event)
		{
			case "nomi.html":
			case "nomi-01.html":
			{
				htmltext = event;
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
		return "nomi.html";
	}
	
	private String applyBuffs(L2Npc npc, L2PcInstance player, Skill skill)
	{
		if (player.getLevel() > MAX_BUFF_LEVEL)
		{
			return "nomi-noBuffs.html";
		}
		
		for (SkillHolder holder : GROUP_BUFFS)
		{
			holder.getSkill().applyEffects(npc, player);
		}
		skill.applyEffects(npc, player);
		
		if ((player.getLevel() < MAX_BLESS_PROTECTION_LEVEL) && (player.getClassId().level() <= 1))
		{
			BLESS_PROTECTION.getSkill().applyEffects(npc, player);
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new Nomi();
	}
}