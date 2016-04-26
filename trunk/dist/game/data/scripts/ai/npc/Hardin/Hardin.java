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
package ai.npc.Hardin;

import com.l2jmobius.gameserver.data.xml.impl.CategoryData;
import com.l2jmobius.gameserver.enums.CategoryType;
import com.l2jmobius.gameserver.enums.Race;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;

import ai.npc.AbstractNpcAI;

/**
 * Hardin AI.
 * @author Stayway
 */
final class Hardin extends AbstractNpcAI
{
	// NPC
	private static final int HARDIN = 33870;
	// Misc
	private static final int MIN_LEVEL = 85;
	
	private Hardin()
	{
		super(Hardin.class.getSimpleName(), "ai/npc");
		addFirstTalkId(HARDIN);
		addCondRace(Race.ERTHEIA, "no_race.html"); // TODO: Find proper HTML
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = null;
		
		if ((player.getRace() != Race.ERTHEIA) && (player.getLevel() < MIN_LEVEL))
		{
			htmltext = "33870-01.html";
		}
		else if ((player.getRace() != Race.ERTHEIA) && CategoryData.getInstance().isInCategory(CategoryType.AWAKEN_GROUP, player.getBaseClassId()))
		{
			htmltext = "33870-02.html";
		}
		else if ((player.getRace() == Race.ERTHEIA) && (player.getLevel() >= MIN_LEVEL))
		{
			htmltext = "33870-03.html";
		}
		
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new Hardin();
	}
}