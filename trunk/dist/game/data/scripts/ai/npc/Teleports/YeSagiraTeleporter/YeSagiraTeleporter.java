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
package ai.npc.Teleports.YeSagiraTeleporter;

import com.l2jmobius.gameserver.model.Location;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.events.EventType;
import com.l2jmobius.gameserver.model.events.ListenerRegisterType;
import com.l2jmobius.gameserver.model.events.annotations.RegisterEvent;
import com.l2jmobius.gameserver.model.events.annotations.RegisterType;
import com.l2jmobius.gameserver.model.events.impl.character.player.OnPlayerCreate;

import ai.npc.AbstractNpcAI;

/**
 * @author Neanrakyr
 */
public final class YeSagiraTeleporter extends AbstractNpcAI
{
	// NPC
	private static final int YE_SAGIRA_TELEPORTER = 33180;
	// Location
	private static final Location TELEPORT = new Location(-114675, 230171, -1648);
	// Misc
	private static final int MAX_LEVEL = 20;
	// Variables names
	private static final String MOVIE_VAR = "TI_YeSagira_movie";
	// Movies
	public static final int YE_SAGIRA = 103;
	
	private YeSagiraTeleporter()
	{
		super(YeSagiraTeleporter.class.getSimpleName(), "ai/npc/Teleports");
		addStartNpc(YE_SAGIRA_TELEPORTER);
		addFirstTalkId(YE_SAGIRA_TELEPORTER);
		addTalkId(YE_SAGIRA_TELEPORTER);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equals("teleport"))
		{
			if (player.getVariables().getBoolean(MOVIE_VAR, false))
			{
				if (player.getLevel() <= MAX_LEVEL)
				{
					player.showQuestMovie(YE_SAGIRA);
				}
				player.getVariables().remove(MOVIE_VAR);
			}
			player.teleToLocation(TELEPORT);
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@RegisterEvent(EventType.ON_PLAYER_CREATE)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void OnPlayerCreate(OnPlayerCreate event)
	{
		event.getActiveChar().getVariables().set(MOVIE_VAR, true);
	}
	
	public static void main(String[] args)
	{
		new YeSagiraTeleporter();
	}
}
