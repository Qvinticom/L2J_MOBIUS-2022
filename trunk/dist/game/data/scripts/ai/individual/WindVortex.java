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

import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.NpcStringId;
import com.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;

import ai.npc.AbstractNpcAI;

/**
 * @author Stayway
 */
final class WindVortex extends AbstractNpcAI
{
	// NPCs
	private static final int WIND_VORTEX = 23417;
	private static final int GIANT_WINDIMA = 23419;
	private static final int IMMENSE_WINDIMA = 23420;
	
	private WindVortex()
	{
		super(WindVortex.class.getSimpleName(), "ai/individual");
		addKillId(WIND_VORTEX);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		final L2Npc newSpawn = addSpawn(getRandomBoolean() ? IMMENSE_WINDIMA : GIANT_WINDIMA, npc.getLocation(), false, 300000); // 5 minute despawn time
		addAttackDesire(newSpawn, killer);
		showOnScreenMsg(killer, NpcStringId.A_POWERFUL_MONSTER_HAS_COME_TO_FACE_YOU, ExShowScreenMessage.TOP_CENTER, 4500);
		return super.onKill(npc, killer, isSummon);
	}
	
	public static void main(String[] args)
	{
		new WindVortex();
	}
}
