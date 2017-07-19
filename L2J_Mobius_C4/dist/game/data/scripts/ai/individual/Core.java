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

import java.util.List;

import com.l2jmobius.gameserver.instancemanager.GrandBossManager;
import com.l2jmobius.gameserver.model.L2Attackable;
import com.l2jmobius.gameserver.model.actor.instance.L2GrandBossInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.network.serverpackets.CreatureSay;
import com.l2jmobius.gameserver.network.serverpackets.PlaySound;
import com.l2jmobius.gameserver.templates.StatsSet;
import com.l2jmobius.util.Rnd;

import javolution.util.FastList;

/**
 * Core AI
 * @author DrLecter Revised By Emperorc
 */
public class Core extends Quest
{
	private static final int CORE = 12052;
	private static final int DEATH_KNIGHT = 12054;
	private static final int DEATH_WRAITH = 12055;
	private static final int SUSCEPTOR = 12058;
	
	// CORE Status Tracking :
	private static final byte ALIVE = 0; // Core is spawned.
	private static final byte DEAD = 1; // Core has been killed.
	
	private static boolean _FirstAttacked;
	
	List<L2Attackable> Minions = new FastList<>();
	
	public Core(int id, String name, String descr)
	{
		super(id, name, descr);
		
		final int[] mobs =
		{
			CORE,
			DEATH_KNIGHT,
			DEATH_WRAITH,
			SUSCEPTOR
		};
		registerMobs(mobs);
		
		_FirstAttacked = false;
		final StatsSet info = GrandBossManager.getInstance().getStatsSet(CORE);
		final int status = GrandBossManager.getInstance().getBossStatus(CORE);
		if (status == DEAD)
		{
			// load the unlock date and time for Core from DB
			final long temp = (info.getLong("respawn_time") - System.currentTimeMillis());
			// if Core is locked until a certain time, mark it so and start the unlock timer
			// the unlock time has not yet expired.
			if (temp > 0)
			{
				startQuestTimer("core_unlock", temp, null, null);
			}
			else
			{
				// the time has already expired while the server was offline. Immediately spawn Core.
				final L2GrandBossInstance core = (L2GrandBossInstance) addSpawn(CORE, 17726, 108915, -6490, 0, false, 0);
				GrandBossManager.getInstance().setBossStatus(CORE, ALIVE);
				spawnBoss(core);
			}
		}
		else
		{
			final String test = loadGlobalQuestVar("Core_Attacked");
			if (test.equalsIgnoreCase("true"))
			{
				_FirstAttacked = true;
			}
			final int loc_x = info.getInteger("loc_x");
			final int loc_y = info.getInteger("loc_y");
			final int loc_z = info.getInteger("loc_z");
			final int heading = info.getInteger("heading");
			final int hp = info.getInteger("currentHP");
			final int mp = info.getInteger("currentMP");
			final L2GrandBossInstance core = (L2GrandBossInstance) addSpawn(CORE, loc_x, loc_y, loc_z, heading, false, 0);
			core.setCurrentHpMp(hp, mp);
			spawnBoss(core);
		}
	}
	
	@Override
	public void saveGlobalData()
	{
		final String val = "" + _FirstAttacked;
		saveGlobalQuestVar("Core_Attacked", val);
	}
	
	public void spawnBoss(L2GrandBossInstance npc)
	{
		GrandBossManager.getInstance().addBoss(npc);
		npc.broadcastPacket(new PlaySound(1, "BS01_A", 1, npc.getObjectId(), npc.getX(), npc.getY(), npc.getZ()));
		
		// Spawn minions
		for (int i = 0; i < 5; i++)
		{
			final int x = 16800 + (i * 360);
			Minions.add((L2Attackable) addSpawn(DEATH_KNIGHT, x, 110000, npc.getZ(), 280 + Rnd.get(40), false, 0));
			Minions.add((L2Attackable) addSpawn(DEATH_KNIGHT, x, 109000, npc.getZ(), 280 + Rnd.get(40), false, 0));
			final int x2 = 16800 + (i * 600);
			Minions.add((L2Attackable) addSpawn(DEATH_WRAITH, x2, 109300, npc.getZ(), 280 + Rnd.get(40), false, 0));
		}
		
		for (int i = 0; i < 4; i++)
		{
			final int x = 16800 + (i * 450);
			Minions.add((L2Attackable) addSpawn(SUSCEPTOR, x, 110300, npc.getZ(), 280 + Rnd.get(40), false, 0));
		}
	}
	
	@Override
	public String onAdvEvent(String event, L2NpcInstance npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("core_unlock"))
		{
			final L2GrandBossInstance core = (L2GrandBossInstance) addSpawn(CORE, 17726, 108915, -6490, 0, false, 0);
			GrandBossManager.getInstance().setBossStatus(CORE, ALIVE);
			spawnBoss(core);
		}
		else if (event.equalsIgnoreCase("spawn_minion"))
		{
			Minions.add((L2Attackable) addSpawn(npc.getNpcId(), npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), false, 0));
		}
		else if (event.equalsIgnoreCase("despawn_minions"))
		{
			for (int i = 0; i < Minions.size(); i++)
			{
				final L2Attackable mob = Minions.get(i);
				if (mob != null)
				{
					mob.decayMe();
				}
			}
			Minions.clear();
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onAttack(L2NpcInstance npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if (npc.getNpcId() == CORE)
		{
			if (_FirstAttacked)
			{
				if (Rnd.get(100) == 0)
				{
					npc.broadcastPacket(new CreatureSay(npc.getObjectId(), 0, npc.getName(), "Removing intruders."));
				}
			}
			else
			{
				_FirstAttacked = true;
				npc.broadcastPacket(new CreatureSay(npc.getObjectId(), 0, npc.getName(), "A non-permitted target has been discovered."));
				npc.broadcastPacket(new CreatureSay(npc.getObjectId(), 0, npc.getName(), "Starting intruder removal system."));
			}
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	@Override
	public String onKill(L2NpcInstance npc, L2PcInstance killer, boolean isPet)
	{
		if (npc.getNpcId() == CORE)
		{
			final int objId = npc.getObjectId();
			npc.broadcastPacket(new PlaySound(1, "BS02_D", 1, objId, npc.getX(), npc.getY(), npc.getZ()));
			npc.broadcastPacket(new CreatureSay(objId, 0, npc.getName(), "A fatal error has occurred."));
			npc.broadcastPacket(new CreatureSay(objId, 0, npc.getName(), "System is being shut down..."));
			npc.broadcastPacket(new CreatureSay(objId, 0, npc.getName(), "......"));
			_FirstAttacked = false;
			addSpawn(12053, 16502, 110165, -6394, 0, false, 900000);
			addSpawn(12053, 18948, 110166, -6397, 0, false, 900000);
			GrandBossManager.getInstance().setBossStatus(CORE, DEAD);
			// time is 60hour +/- 23hour
			final long respawnTime = (27 + Rnd.get(47)) * 3600000;
			startQuestTimer("core_unlock", respawnTime, null, null);
			// also save the respawn time so that the info is maintained past reboots
			final StatsSet info = GrandBossManager.getInstance().getStatsSet(CORE);
			info.set("respawn_time", (System.currentTimeMillis() + respawnTime));
			GrandBossManager.getInstance().setStatsSet(CORE, info);
			startQuestTimer("despawn_minions", 20000, null, null);
			cancelQuestTimers("spawn_minion");
		}
		else if ((GrandBossManager.getInstance().getBossStatus(CORE) == ALIVE) && (Minions != null) && Minions.contains(npc))
		{
			Minions.remove(npc);
			startQuestTimer("spawn_minion", 60000, npc, null);
		}
		return super.onKill(npc, killer, isPet);
	}
	
	public static void main(String[] args)
	{
		// now call the constructor (starts up the ai)
		new Core(-1, "core", "ai");
	}
}