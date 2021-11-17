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
package ai.bosses.Zaken;

import org.l2jmobius.Config;
import org.l2jmobius.commons.util.Chronos;
import org.l2jmobius.gameserver.instancemanager.GrandBossManager;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.instance.GrandBoss;
import org.l2jmobius.gameserver.network.serverpackets.PlaySound;

import ai.AbstractNpcAI;

/**
 * Zaken AI<br>
 * TODO: Skill cast?<br>
 * TODO: Day/Night spawn? TODO: Boss message broadcast.
 * @author Mobius
 */
public class Zaken extends AbstractNpcAI
{
	// NPC
	private static final int ZAKEN = 29022;
	// Location
	private static final int ZAKEN_X = 42675;
	private static final int ZAKEN_Y = 207567;
	private static final int ZAKEN_Z = -3752;
	private static final int ZAKEN_HEADING = 7835;
	// Misc
	private static final byte ALIVE = 0;
	private static final byte DEAD = 1;
	
	private Zaken()
	{
		addKillId(ZAKEN);
		
		final StatSet info = GrandBossManager.getInstance().getStatSet(ZAKEN);
		final int status = GrandBossManager.getInstance().getBossStatus(ZAKEN);
		if (status == DEAD)
		{
			// load the unlock date and time from DB
			final long temp = info.getLong("respawn_time") - Chronos.currentTimeMillis();
			if (temp > 0)
			{
				startQuestTimer("zaken_unlock", temp, null, null);
			}
			else // the time has already expired while the server was offline
			{
				spawnBoss();
			}
		}
		else
		{
			spawnBoss();
		}
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		if (event.equals("zaken_unlock"))
		{
			spawnBoss();
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	private void spawnBoss()
	{
		final GrandBoss zaken = (GrandBoss) addSpawn(ZAKEN, ZAKEN_X, ZAKEN_Y, ZAKEN_Z, ZAKEN_HEADING, false, 0);
		GrandBossManager.getInstance().setBossStatus(ZAKEN, ALIVE);
		GrandBossManager.getInstance().addBoss(zaken);
		zaken.broadcastPacket(new PlaySound(1, "BS01_A", 1, zaken.getObjectId(), zaken.getX(), zaken.getY(), zaken.getZ()));
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		npc.broadcastPacket(new PlaySound(1, "BS02_D", 1, npc.getObjectId(), npc.getX(), npc.getY(), npc.getZ()));
		GrandBossManager.getInstance().setBossStatus(ZAKEN, DEAD);
		// Calculate Min and Max respawn times randomly.
		final long respawnTime = (Config.ZAKEN_SPAWN_INTERVAL + getRandom(-Config.ZAKEN_SPAWN_RANDOM, Config.ZAKEN_SPAWN_RANDOM)) * 3600000;
		startQuestTimer("zaken_unlock", respawnTime, null, null);
		// also save the respawn time so that the info is maintained past reboots
		final StatSet info = GrandBossManager.getInstance().getStatSet(ZAKEN);
		info.set("respawn_time", Chronos.currentTimeMillis() + respawnTime);
		GrandBossManager.getInstance().setStatSet(ZAKEN, info);
		return super.onKill(npc, killer, isSummon);
	}
	
	public static void main(String[] args)
	{
		new Zaken();
	}
}
