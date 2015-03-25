/*
 * Copyright (C) 2004-2015 L2J Mobius DataPack
 * 
 * This file is part of L2J Mobius DataPack.
 * 
 * L2J Mobius DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Mobius DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package instances.KaraphonHabitat;

import instances.AbstractInstance;
import quests.Q10745_TheSecretIngredients.Q10745_TheSecretIngredients;

import com.l2jserver.gameserver.instancemanager.InstanceManager;
import com.l2jserver.gameserver.model.Location;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.instancezone.InstanceWorld;
import com.l2jserver.gameserver.model.quest.QuestState;

/**
 * @author Neanrakyr
 */
public class KaraphonHabitat extends AbstractInstance
{
	// Npcs
	private static final int DOLKIN = 33954;
	private static final int DOLKIN_INSTANCE = 34002;
	
	// Locations
	private static final Location START_LOC = new Location(-82250, 246406, -14152);
	private static final Location EXIT_LOC = new Location(-88240, 237450, -2880);
	
	// Instance
	private static final int TEMPLATE_ID = 253;
	
	protected class KHWorld extends InstanceWorld
	{
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final QuestState qs = player.getQuestState(Q10745_TheSecretIngredients.class.getSimpleName());
		if (qs == null)
		{
			return null;
		}
		
		if (event.equals("enter_instance"))
		{
			enterInstance(player, new KHWorld(), "KaraphonHabitat.xml", TEMPLATE_ID);
		}
		else if (event.equals("exit_instance"))
		{
			final InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
			world.removeAllowed(player.getObjectId());
			teleportPlayer(player, EXIT_LOC, 0);
		}
		
		return super.onAdvEvent(event, npc, player);
	}
	
	public KaraphonHabitat()
	{
		super(KaraphonHabitat.class.getSimpleName());
		addStartNpc(DOLKIN);
		addTalkId(DOLKIN, DOLKIN_INSTANCE);
	}

	@Override
	public void onEnterInstance(L2PcInstance player, InstanceWorld world, boolean firstEntrance)
	{
		if (firstEntrance)
		{
			world.addAllowed(player.getObjectId());
		}
		teleportPlayer(player, START_LOC, world.getInstanceId());
	}
}