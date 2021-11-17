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
package ai.others.CastleFlagOfProtection;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.QuestState;

import ai.AbstractNpcAI;

/**
 * Castle Flag of Protection AI.
 * @author CostyKiller
 */
public class CastleFlagOfProtection extends AbstractNpcAI
{
	// Flag of Protection NPCs
	private static final int FLAG_GLUDIO = 36741; // 1 Gludio Castle
	private static final int FLAG_DION = 36742; // 2 Dion Castle
	private static final int FLAG_GIRAN = 36743; // 3 Giran Castle
	private static final int FLAG_OREN = 36744; // 4 Oren Castle
	private static final int FLAG_ADEN = 36745; // 5 Aden Castle
	private static final int FLAG_INNADRIL = 36746; // 6 Innadril Castle
	private static final int FLAG_GODDARD = 36747; // 7 Goddard Castle
	private static final int FLAG_RUNE = 36748; // 8 Rune Castle
	private static final int FLAG_SCHUTTGART = 36749; // 9 Schuttgart Castle
	
	private CastleFlagOfProtection()
	{
		addFirstTalkId(FLAG_GLUDIO, FLAG_DION, FLAG_GIRAN, FLAG_OREN, FLAG_ADEN, FLAG_INNADRIL, FLAG_GODDARD, FLAG_RUNE, FLAG_SCHUTTGART);
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		String htmltext;
		final QuestState qs = player.getQuestState("Q10825_ForVictory");
		if (((qs != null) && qs.isCond(1)))
		{
			htmltext = "CastleFlagOfProtection.html";
		}
		else
		{
			htmltext = "CastleFlagOfProtection-01.html";
		}
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new CastleFlagOfProtection();
	}
}