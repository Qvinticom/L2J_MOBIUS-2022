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
package ai.bosses;

import com.l2jmobius.gameserver.model.quest.Quest;

/*
 * @author m095 (L2EmuRT)
 */
public class Barakiel extends Quest
{
	// Barakiel NpcID
	private static final int BARAKIEL = 25325;
	
	public Barakiel(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addEventId(BARAKIEL, Quest.QuestEventType.ON_ATTACK);
	}
	
	// FIXME: Mobius - AI does nothing?
	
	public static void main(String[] args)
	{
		new Barakiel(-1, "Barakiel", "ai");
	}
}
