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
package ai.areas.GainakUnderground;

import org.l2jmobius.Config;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.instancemanager.ZoneManager;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.events.EventType;
import org.l2jmobius.gameserver.model.events.ListenerRegisterType;
import org.l2jmobius.gameserver.model.events.annotations.RegisterEvent;
import org.l2jmobius.gameserver.model.events.annotations.RegisterType;
import org.l2jmobius.gameserver.model.events.impl.creature.OnCreatureDeath;
import org.l2jmobius.gameserver.model.zone.ZoneType;
import org.l2jmobius.gameserver.model.zone.type.PeaceZone;
import org.l2jmobius.gameserver.model.zone.type.SiegeZone;
import org.l2jmobius.gameserver.network.NpcStringId;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;
import org.l2jmobius.gameserver.network.serverpackets.OnEventTrigger;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.network.serverpackets.UserInfo;
import org.l2jmobius.gameserver.util.Broadcast;

import ai.AbstractNpcAI;

/**
 * @author LasTravel, Gigi
 * @URL http://l2wiki.com/Gainak
 */
public class GainakSiege extends AbstractNpcAI
{
	private static final int SIEGE_EFFECT = 20140700;
	private static final int SIEGE_DURATION = 30;
	private static final SiegeZone GAINAK_SIEGE_ZONE = ZoneManager.getInstance().getZoneById(60019, SiegeZone.class);
	private static final PeaceZone GAINAK_TOWN_ZONE = ZoneManager.getInstance().getZoneById(60020, PeaceZone.class);
	protected static final int[] ASSASSIN_IDS =
	{
		19471,
		19472,
		19473
	};
	private static final Location[] ASSASSIN_SPAWNS =
	{
		new Location(17085, -115385, -249, 41366),
		new Location(15452, -114531, -243, 5464),
		new Location(15862, -113121, -250, 53269)
	};
	private boolean _isInSiege = false;
	
	public GainakSiege()
	{
		addEnterZoneId(GAINAK_SIEGE_ZONE.getId(), GAINAK_TOWN_ZONE.getId());
		addKillId(ASSASSIN_IDS);
		startQuestTimer("GAINAK_WAR", getTimeBetweenSieges() * 60000, null, null);
	}
	
	private final int getTimeBetweenSieges()
	{
		return getRandom(120, 180); // 2 to 3 hours.
	}
	
	@Override
	public String onEnterZone(Creature creature, ZoneType zone)
	{
		if (_isInSiege && creature.isPlayer())
		{
			creature.broadcastPacket(new OnEventTrigger(SIEGE_EFFECT, true));
		}
		return super.onEnterZone(creature, zone);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		if (event.equalsIgnoreCase("GAINAK_WAR"))
		{
			if (_isInSiege)
			{
				_isInSiege = false;
				GAINAK_TOWN_ZONE.setEnabled(true); // enable before broadcast
				GAINAK_TOWN_ZONE.broadcastPacket(new OnEventTrigger(SIEGE_EFFECT, false));
				GAINAK_TOWN_ZONE.broadcastPacket(new ExShowScreenMessage(NpcStringId.GAINAK_IN_PEACE, ExShowScreenMessage.TOP_CENTER, 5000, true));
				GAINAK_SIEGE_ZONE.setActive(false);
				GAINAK_SIEGE_ZONE.updateZoneStatusForCharactersInside();
				startQuestTimer("GAINAK_WAR", getTimeBetweenSieges() * 60000, null, null);
				if (Config.ANNOUNCE_GAINAK_SIEGE)
				{
					final SystemMessage s = new SystemMessage(SystemMessageId.PROGRESS_EVENT_STAGE_S1);
					s.addString("Gainak is now in peace.");
					Broadcast.toAllOnlinePlayers(s);
				}
			}
			else
			{
				for (Location loc : ASSASSIN_SPAWNS)
				{
					addSpawn(getRandomEntry(ASSASSIN_IDS), loc, true, 1800000);
				}
				_isInSiege = true;
				GAINAK_TOWN_ZONE.broadcastPacket(new OnEventTrigger(SIEGE_EFFECT, true));
				GAINAK_TOWN_ZONE.broadcastPacket(new ExShowScreenMessage(NpcStringId.GAINAK_IN_WAR, ExShowScreenMessage.TOP_CENTER, 5000, true));
				GAINAK_TOWN_ZONE.setEnabled(false); // disable after broadcast
				GAINAK_SIEGE_ZONE.setActive(true);
				GAINAK_SIEGE_ZONE.updateZoneStatusForCharactersInside();
				startQuestTimer("GAINAK_WAR", SIEGE_DURATION * 60000, null, null);
				if (Config.ANNOUNCE_GAINAK_SIEGE)
				{
					final SystemMessage s = new SystemMessage(SystemMessageId.PROGRESS_EVENT_STAGE_S1);
					s.addString("Gainak is now under siege.");
					Broadcast.toAllOnlinePlayers(s);
				}
				ZoneManager.getInstance().getZoneById(GAINAK_TOWN_ZONE.getId(), PeaceZone.class).setEnabled(false);
			}
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		final SiegeZone zone = ZoneManager.getInstance().getZone(npc, SiegeZone.class);
		if ((zone != null) && (zone.getId() == 60019) && zone.isActive())
		{
			ThreadPool.schedule(new RespawnNewAssassin(npc.getLocation()), 60000);
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	private class RespawnNewAssassin implements Runnable
	{
		private final Location _loc;
		
		public RespawnNewAssassin(Location loc)
		{
			_loc = loc;
		}
		
		@Override
		public void run()
		{
			addSpawn(getRandomEntry(ASSASSIN_IDS), _loc, true, 1800000);
		}
	}
	
	@RegisterEvent(EventType.ON_CREATURE_DEATH)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void onPlayerDeath(OnCreatureDeath event)
	{
		if (_isInSiege && GAINAK_SIEGE_ZONE.getCharactersInside().contains(event.getTarget()) && event.getAttacker().isPlayer() && event.getTarget().isPlayer())
		{
			final Player attackerPlayer = event.getAttacker().getActingPlayer();
			attackerPlayer.setPvpKills(attackerPlayer.getPvpKills() + 1);
			attackerPlayer.sendPacket(new UserInfo(attackerPlayer));
		}
	}
	
	public static void main(String[] args)
	{
		new GainakSiege();
	}
}