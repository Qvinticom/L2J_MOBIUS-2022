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
package ai.areas.TowerOfInsolence.HeavenlyRift;

import org.l2jmobius.gameserver.instancemanager.GlobalVariablesManager;
import org.l2jmobius.gameserver.instancemanager.HeavenlyRiftManager;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.NpcStringId;
import org.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;

import ai.AbstractNpcAI;

/**
 * @author Brutallis
 */
public class Tower extends AbstractNpcAI
{
	// NPCs
	private static final int TOWER = 18004;
	private static final int DIVINE_ANGEL = 20139;
	
	public Tower()
	{
		addKillId(TOWER);
	}
	
	@Override
	public String onKill(Npc npc, PlayerInstance killer, boolean isSummon)
	{
		HeavenlyRiftManager.getZone().broadcastPacket(new ExShowScreenMessage(NpcStringId.YOU_HAVE_FAILED, 2, 5000));
		for (Creature creature : HeavenlyRiftManager.getZone().getCharactersInside())
		{
			if (creature.isMonster() && !creature.isDead() && (creature.getId() == DIVINE_ANGEL))
			{
				creature.decayMe();
			}
		}
		GlobalVariablesManager.getInstance().set("heavenly_rift_complete", GlobalVariablesManager.getInstance().getInt("heavenly_rift_level", 0));
		GlobalVariablesManager.getInstance().set("heavenly_rift_level", 0);
		GlobalVariablesManager.getInstance().set("heavenly_rift_reward", 0);
		return super.onKill(npc, killer, isSummon);
	}
	
	public static void main(String[] args)
	{
		new Tower();
	}
}
