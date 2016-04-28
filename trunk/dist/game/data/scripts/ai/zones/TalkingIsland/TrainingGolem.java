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
package ai.zones.TalkingIsland;

import com.l2jmobius.gameserver.model.actor.L2Npc;

import ai.npc.AbstractNpcAI;

/**
 * Training Golem AI.
 * @author Gladicek
 */
final class TrainingGolem extends AbstractNpcAI
{
	// NPCs
	private static final int TRAINING_GOLEM = 27532;
	
	private TrainingGolem()
	{
		super(TrainingGolem.class.getSimpleName(), "ai/zones/TalkingIsland");
		addSpawnId(TRAINING_GOLEM);
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		npc.setIsImmobilized(true);
		return super.onSpawn(npc);
	}
	
	public static void main(String[] args)
	{
		new TrainingGolem();
	}
}
