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
package ai.individual;

import com.l2jmobius.gameserver.ai.CtrlIntention;
import com.l2jmobius.gameserver.geodata.GeoData;
import com.l2jmobius.gameserver.model.L2CharPosition;
import com.l2jmobius.gameserver.model.L2Character;
import com.l2jmobius.gameserver.model.L2Summon;
import com.l2jmobius.gameserver.model.Location;
import com.l2jmobius.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.util.Util;

/**
 * @author Mobius
 */
public class Elpy extends Quest
{
	// NPCs
	private static final int[] MOBS =
	{
		432, // Elpy
	};
	// Misc
	private static final int FLEE_DISTANCE = 500;
	
	public Elpy(int questId, String name, String descr)
	{
		super(questId, name, descr);
		registerMobs(MOBS);
	}
	
	@Override
	public String onAttack(L2NpcInstance npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		npc.disableCoreAI(true);
		npc.setRunning();
		
		final L2Summon summon = isPet ? attacker.getPet() : null;
		final L2Character actualAttacker = summon == null ? attacker : summon;
		final double radians = Math.toRadians(Util.calculateAngleFrom(actualAttacker, npc));
		final int posX = (int) (npc.getX() + (FLEE_DISTANCE * Math.cos(radians)));
		final int posY = (int) (npc.getY() + (FLEE_DISTANCE * Math.sin(radians)));
		final int posZ = npc.getZ();
		
		final Location destination = GeoData.getInstance().moveCheck(npc.getX(), npc.getY(), npc.getZ(), posX, posY, posZ);
		final L2CharPosition charPos = new L2CharPosition(destination.getX(), destination.getY(), destination.getZ(), destination.getHeading());
		npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, charPos);
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	public static void main(String[] args)
	{
		// now call the constructor (starts up the ai)
		new Elpy(-1, "eply", "ai");
	}
}
