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
package ai.bosses.QueenShyeed;

import org.l2jmobius.commons.util.Chronos;
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.instancemanager.GlobalVariablesManager;
import org.l2jmobius.gameserver.instancemanager.ZoneManager;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.zone.type.EffectZone;
import org.l2jmobius.gameserver.network.NpcStringId;

import ai.AbstractNpcAI;

/**
 * Queen Shyeed AI
 * @author malyelfik
 */
public class QueenShyeed extends AbstractNpcAI
{
	// NPC
	private static final int SHYEED = 25671;
	private static final Location SHYEED_LOC = new Location(79634, -55428, -6104, 0);
	// Respawn
	private static final int RESPAWN = 86400000; // 24 h
	private static final int RANDOM_RESPAWN = 43200000; // 12 h
	// Zones
	// private static final EffectZone MOB_BUFF_ZONE = ZoneManager.getInstance().getZoneById(200103, EffectZone.class);
	private static final EffectZone MOB_BUFF_DISPLAY_ZONE = ZoneManager.getInstance().getZoneById(200104, EffectZone.class);
	private static final EffectZone PC_BUFF_ZONE = ZoneManager.getInstance().getZoneById(200105, EffectZone.class);
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		switch (event)
		{
			case "respawn":
			{
				spawnShyeed();
				break;
			}
			case "despawn":
			{
				if (!npc.isDead())
				{
					startRespawn();
					npc.deleteMe();
				}
				break;
			}
		}
		return null;
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		npc.broadcastSay(ChatType.NPC_GENERAL, NpcStringId.SHYEED_S_CRY_IS_STEADILY_DYING_DOWN);
		startRespawn();
		PC_BUFF_ZONE.setEnabled(true);
		return super.onKill(npc, killer, isSummon);
	}
	
	private QueenShyeed()
	{
		addKillId(SHYEED);
		spawnShyeed();
	}
	
	private void spawnShyeed()
	{
		final long respawn = GlobalVariablesManager.getInstance().getLong("QueenShyeedRespawn", 0);
		final long remain = respawn != 0 ? respawn - Chronos.currentTimeMillis() : 0;
		if (remain > 0)
		{
			startQuestTimer("respawn", remain, null, null);
			return;
		}
		final Npc npc = addSpawn(SHYEED, SHYEED_LOC, false, 0);
		startQuestTimer("despawn", 10800000, npc, null);
		PC_BUFF_ZONE.setEnabled(false);
		// MOB_BUFF_ZONE.setEnabled(true);
		MOB_BUFF_DISPLAY_ZONE.setEnabled(true);
	}
	
	private void startRespawn()
	{
		final int respawnTime = RESPAWN - getRandom(RANDOM_RESPAWN);
		GlobalVariablesManager.getInstance().set("QueenShyeedRespawn", Long.toString(Chronos.currentTimeMillis() + respawnTime));
		startQuestTimer("respawn", respawnTime, null, null);
		// MOB_BUFF_ZONE.setEnabled(false);
		MOB_BUFF_DISPLAY_ZONE.setEnabled(false);
	}
	
	public static void main(String[] args)
	{
		new QueenShyeed();
	}
}