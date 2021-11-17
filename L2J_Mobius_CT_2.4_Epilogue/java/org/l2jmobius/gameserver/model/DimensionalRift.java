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
package org.l2jmobius.gameserver.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import org.l2jmobius.Config;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.instancemanager.DimensionalRiftManager;
import org.l2jmobius.gameserver.instancemanager.QuestManager;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.network.serverpackets.Earthquake;

/**
 * Thanks to Fortress and balancer.ru - kombat
 */
public class DimensionalRift
{
	protected byte _type;
	protected Party _party;
	protected List<Byte> _completedRooms = new ArrayList<>();
	private static final long FIVE_SECONDS = 5000;
	protected byte jumps_current = 0;
	
	private Timer teleporterTimer;
	private TimerTask teleporterTimerTask;
	private Timer spawnTimer;
	private TimerTask spawnTimerTask;
	
	private Future<?> earthQuakeTask;
	
	protected byte _choosenRoom = -1;
	private boolean _hasJumped = false;
	protected Collection<Player> _deadPlayers = ConcurrentHashMap.newKeySet();
	protected Collection<Player> _revivedInWaitingRoom = ConcurrentHashMap.newKeySet();
	private boolean isBossRoom = false;
	
	public DimensionalRift(Party party, byte type, byte room)
	{
		DimensionalRiftManager.getInstance().getRoom(type, room).setPartyInside(true);
		_type = type;
		_party = party;
		_choosenRoom = room;
		final Location coords = getRoomCoord(room);
		party.setDimensionalRift(this);
		for (Player p : party.getMembers())
		{
			final Quest riftQuest = QuestManager.getInstance().getQuest(635);
			if (riftQuest != null)
			{
				QuestState qs = p.getQuestState(riftQuest.getName());
				if (qs == null)
				{
					qs = riftQuest.newQuestState(p);
				}
				if (!qs.isStarted())
				{
					qs.startQuest();
				}
			}
			p.teleToLocation(coords);
		}
		createSpawnTimer(_choosenRoom);
		createTeleporterTimer(true);
	}
	
	public byte getType()
	{
		return _type;
	}
	
	public byte getCurrentRoom()
	{
		return _choosenRoom;
	}
	
	protected void createTeleporterTimer(boolean reasonTP)
	{
		if (_party == null)
		{
			return;
		}
		
		if (teleporterTimerTask != null)
		{
			teleporterTimerTask.cancel();
			teleporterTimerTask = null;
		}
		
		if (teleporterTimer != null)
		{
			teleporterTimer.cancel();
			teleporterTimer = null;
		}
		
		if (earthQuakeTask != null)
		{
			earthQuakeTask.cancel(false);
			earthQuakeTask = null;
		}
		
		teleporterTimer = new Timer();
		teleporterTimerTask = new TimerTask()
		{
			@Override
			public void run()
			{
				if (_choosenRoom > -1)
				{
					DimensionalRiftManager.getInstance().getRoom(_type, _choosenRoom).unspawn().setPartyInside(false);
				}
				
				if (reasonTP && (jumps_current < getMaxJumps()) && (_party.getMemberCount() > _deadPlayers.size()))
				{
					jumps_current++;
					
					_completedRooms.add(_choosenRoom);
					_choosenRoom = -1;
					
					for (Player p : _party.getMembers())
					{
						if (!_revivedInWaitingRoom.contains(p))
						{
							teleportToNextRoom(p);
						}
					}
					createTeleporterTimer(true);
					createSpawnTimer(_choosenRoom);
				}
				else
				{
					for (Player p : _party.getMembers())
					{
						if (!_revivedInWaitingRoom.contains(p))
						{
							teleportToWaitingRoom(p);
						}
					}
					killRift();
					cancel();
				}
			}
		};
		
		if (reasonTP)
		{
			final long jumpTime = calcTimeToNextJump();
			teleporterTimer.schedule(teleporterTimerTask, jumpTime); // Teleporter task, 8-10 minutes
			
			earthQuakeTask = ThreadPool.schedule(() ->
			{
				for (Player p : _party.getMembers())
				{
					if (!_revivedInWaitingRoom.contains(p))
					{
						p.sendPacket(new Earthquake(p.getX(), p.getY(), p.getZ(), 65, 9));
					}
				}
			}, jumpTime - 7000);
		}
		else
		{
			teleporterTimer.schedule(teleporterTimerTask, FIVE_SECONDS); // incorrect party member invited.
		}
	}
	
	public void createSpawnTimer(byte room)
	{
		if (spawnTimerTask != null)
		{
			spawnTimerTask.cancel();
			spawnTimerTask = null;
		}
		
		if (spawnTimer != null)
		{
			spawnTimer.cancel();
			spawnTimer = null;
		}
		
		spawnTimer = new Timer();
		spawnTimerTask = new TimerTask()
		{
			@Override
			public void run()
			{
				DimensionalRiftManager.getInstance().getRoom(_type, room).spawn();
			}
		};
		
		spawnTimer.schedule(spawnTimerTask, Config.RIFT_SPAWN_DELAY);
	}
	
	public void partyMemberInvited()
	{
		createTeleporterTimer(false);
	}
	
	public void partyMemberExited(Player player)
	{
		if (_deadPlayers.contains(player))
		{
			_deadPlayers.remove(player);
		}
		
		if (_revivedInWaitingRoom.contains(player))
		{
			_revivedInWaitingRoom.remove(player);
		}
		
		if ((_party.getMemberCount() < Config.RIFT_MIN_PARTY_SIZE) || (_party.getMemberCount() == 1))
		{
			for (Player p : _party.getMembers())
			{
				teleportToWaitingRoom(p);
			}
			killRift();
		}
	}
	
	public void manualTeleport(Player player, Npc npc)
	{
		if (!player.isInParty() || !player.getParty().isInDimensionalRift())
		{
			return;
		}
		
		if (player.getObjectId() != player.getParty().getLeaderObjectId())
		{
			DimensionalRiftManager.getInstance().showHtmlFile(player, "data/html/seven_signs/rift/NotPartyLeader.htm", npc);
			return;
		}
		
		if (_hasJumped)
		{
			DimensionalRiftManager.getInstance().showHtmlFile(player, "data/html/seven_signs/rift/AlreadyTeleported.htm", npc);
			return;
		}
		
		_hasJumped = true;
		DimensionalRiftManager.getInstance().getRoom(_type, _choosenRoom).unspawn().setPartyInside(false);
		_completedRooms.add(_choosenRoom);
		_choosenRoom = -1;
		
		for (Player p : _party.getMembers())
		{
			teleportToNextRoom(p);
		}
		
		DimensionalRiftManager.getInstance().getRoom(_type, _choosenRoom).setPartyInside(true);
		
		createSpawnTimer(_choosenRoom);
		createTeleporterTimer(true);
	}
	
	public void manualExitRift(Player player, Npc npc)
	{
		if (!player.isInParty() || !player.getParty().isInDimensionalRift())
		{
			return;
		}
		
		if (player.getObjectId() != player.getParty().getLeaderObjectId())
		{
			DimensionalRiftManager.getInstance().showHtmlFile(player, "data/html/seven_signs/rift/NotPartyLeader.htm", npc);
			return;
		}
		
		for (Player p : player.getParty().getMembers())
		{
			teleportToWaitingRoom(p);
		}
		killRift();
	}
	
	protected void teleportToNextRoom(Player player)
	{
		if (_choosenRoom == -1)
		{
			List<Byte> emptyRooms;
			do
			{
				emptyRooms = DimensionalRiftManager.getInstance().getFreeRooms(_type);
				// Do not tp in the same room a second time
				emptyRooms.removeAll(_completedRooms);
				// If no room left, find any empty
				if (emptyRooms.isEmpty())
				{
					emptyRooms = DimensionalRiftManager.getInstance().getFreeRooms(_type);
				}
				_choosenRoom = emptyRooms.get(Rnd.get(1, emptyRooms.size()) - 1);
			}
			while (DimensionalRiftManager.getInstance().getRoom(_type, _choosenRoom).isPartyInside());
		}
		
		DimensionalRiftManager.getInstance().getRoom(_type, _choosenRoom).setPartyInside(true);
		checkBossRoom(_choosenRoom);
		player.teleToLocation(getRoomCoord(_choosenRoom));
	}
	
	protected void teleportToWaitingRoom(Player player)
	{
		DimensionalRiftManager.getInstance().teleportToWaitingRoom(player);
		final Quest riftQuest = QuestManager.getInstance().getQuest(635);
		if (riftQuest != null)
		{
			final QuestState qs = player.getQuestState(riftQuest.getName());
			if ((qs != null) && qs.isCond(1))
			{
				qs.exitQuest(true, true);
			}
		}
	}
	
	public void killRift()
	{
		_completedRooms.clear();
		
		if (_party != null)
		{
			_party.setDimensionalRift(null);
		}
		
		_party = null;
		_revivedInWaitingRoom = null;
		_deadPlayers = null;
		
		if (earthQuakeTask != null)
		{
			earthQuakeTask.cancel(false);
			earthQuakeTask = null;
		}
		
		DimensionalRiftManager.getInstance().getRoom(_type, _choosenRoom).unspawn().setPartyInside(false);
		DimensionalRiftManager.getInstance().killRift(this);
	}
	
	public Timer getTeleportTimer()
	{
		return teleporterTimer;
	}
	
	public TimerTask getTeleportTimerTask()
	{
		return teleporterTimerTask;
	}
	
	public Timer getSpawnTimer()
	{
		return spawnTimer;
	}
	
	public TimerTask getSpawnTimerTask()
	{
		return spawnTimerTask;
	}
	
	public void setTeleportTimer(Timer t)
	{
		teleporterTimer = t;
	}
	
	public void setTeleportTimerTask(TimerTask tt)
	{
		teleporterTimerTask = tt;
	}
	
	public void setSpawnTimer(Timer t)
	{
		spawnTimer = t;
	}
	
	public void setSpawnTimerTask(TimerTask st)
	{
		spawnTimerTask = st;
	}
	
	private long calcTimeToNextJump()
	{
		final int time = Rnd.get(Config.RIFT_AUTO_JUMPS_TIME_MIN, Config.RIFT_AUTO_JUMPS_TIME_MAX) * 1000;
		
		if (isBossRoom)
		{
			return (long) (time * Config.RIFT_BOSS_ROOM_TIME_MUTIPLY);
		}
		return time;
	}
	
	public void memberDead(Player player)
	{
		if (!_deadPlayers.contains(player))
		{
			_deadPlayers.add(player);
		}
	}
	
	public void memberRessurected(Player player)
	{
		if (_deadPlayers.contains(player))
		{
			_deadPlayers.remove(player);
		}
	}
	
	public void usedTeleport(Player player)
	{
		if (!_revivedInWaitingRoom.contains(player))
		{
			_revivedInWaitingRoom.add(player);
		}
		
		if (!_deadPlayers.contains(player))
		{
			_deadPlayers.add(player);
		}
		
		if ((_party.getMemberCount() - _revivedInWaitingRoom.size()) < Config.RIFT_MIN_PARTY_SIZE)
		{
			// int pcm = _party.getMemberCount();
			// int rev = revivedInWaitingRoom.size();
			// int min = Config.RIFT_MIN_PARTY_SIZE;
			
			for (Player p : _party.getMembers())
			{
				if ((p != null) && !_revivedInWaitingRoom.contains(p))
				{
					teleportToWaitingRoom(p);
				}
			}
			killRift();
		}
	}
	
	public Collection<Player> getDeadMemberList()
	{
		return _deadPlayers;
	}
	
	public Collection<Player> getRevivedAtWaitingRoom()
	{
		return _revivedInWaitingRoom;
	}
	
	public void checkBossRoom(byte room)
	{
		isBossRoom = DimensionalRiftManager.getInstance().getRoom(_type, room).isBossRoom();
	}
	
	public Location getRoomCoord(byte room)
	{
		return DimensionalRiftManager.getInstance().getRoom(_type, room).getTeleportCoorinates();
	}
	
	public byte getMaxJumps()
	{
		if ((Config.RIFT_MAX_JUMPS <= 8) && (Config.RIFT_MAX_JUMPS >= 1))
		{
			return (byte) Config.RIFT_MAX_JUMPS;
		}
		return 4;
	}
}
