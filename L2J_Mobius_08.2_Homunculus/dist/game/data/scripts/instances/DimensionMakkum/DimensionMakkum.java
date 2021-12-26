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
package instances.DimensionMakkum;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.instancezone.Instance;

import instances.AbstractInstance;

/**
 * Dimension Makkum instance
 * @author Gigi
 */
public class DimensionMakkum extends AbstractInstance
{
	// NPCs
	private static final int PIORE = 34290;
	private static final int DIMENSIONAL_MAKKUM = 26195;
	// Misc
	private static final int TEMPLATE_ID = 10658;
	
	public DimensionMakkum()
	{
		super(TEMPLATE_ID);
		addFirstTalkId(PIORE);
		addKillId(DIMENSIONAL_MAKKUM);
		addInstanceCreatedId(TEMPLATE_ID);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		final Instance world = getPlayerInstance(player);
		switch (event)
		{
			case "enter_instance":
			{
				enterInstance(player, npc, TEMPLATE_ID);
				break;
			}
			case "spawn_piore":
			{
				addSpawn(PIORE, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), false, 300000, false, world.getId());
				break;
			}
			case "exitInstance":
			{
				if (world != null)
				{
					teleportPlayerOut(player, world);
				}
				break;
			}
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public void onInstanceCreated(Instance instance, Player player)
	{
		addSpawn(DIMENSIONAL_MAKKUM, 185064, -9610, -5488, 19610, false, 430000, true, instance.getId());
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		final Instance world = npc.getInstanceWorld();
		if (world == null)
		{
			return super.onKill(npc, killer, isSummon);
		}
		
		startQuestTimer("spawn_piore", 4000, npc, killer);
		world.finishInstance();
		
		return super.onKill(npc, killer, isSummon);
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		return "34290.html";
	}
	
	public static void main(String[] args)
	{
		new DimensionMakkum();
	}
}