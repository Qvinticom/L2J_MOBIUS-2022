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
package org.l2jmobius.gameserver.model.siege.clanhalls;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.commons.util.Chronos;
import org.l2jmobius.gameserver.data.sql.ClanHallTable;
import org.l2jmobius.gameserver.data.sql.ClanTable;
import org.l2jmobius.gameserver.data.sql.NpcTable;
import org.l2jmobius.gameserver.data.xml.DoorData;
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.instancemanager.IdManager;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.instance.Deco;
import org.l2jmobius.gameserver.model.actor.instance.Monster;
import org.l2jmobius.gameserver.model.actor.templates.NpcTemplate;
import org.l2jmobius.gameserver.model.clan.Clan;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.residences.ClanHall;
import org.l2jmobius.gameserver.model.siege.ClanHallSiege;
import org.l2jmobius.gameserver.model.zone.type.ClanHallZone;
import org.l2jmobius.gameserver.network.serverpackets.CreatureSay;
import org.l2jmobius.gameserver.taskmanager.ExclusiveTask;

/**
 * @author MHard
 */
public class BanditStrongholdSiege extends ClanHallSiege
{
	protected static final Logger LOGGER = Logger.getLogger(BanditStrongholdSiege.class.getName());
	
	boolean _registrationPeriod = false;
	private int _clanCounter = 0;
	protected Map<Integer, clanPlayersInfo> _clansInfo = new HashMap<>();
	public ClanHall clanhall = ClanHallTable.getInstance().getClanHallById(35);
	protected clanPlayersInfo _ownerClanInfo = new clanPlayersInfo();
	protected boolean _finalStage = false;
	protected ScheduledFuture<?> _midTimer;
	
	protected BanditStrongholdSiege()
	{
		LOGGER.info("SiegeManager of Bandits Stronghold");
		final long siegeDate = restoreSiegeDate(35);
		final Calendar tmpDate = Calendar.getInstance();
		tmpDate.setTimeInMillis(siegeDate);
		setSiegeDate(tmpDate);
		setNewSiegeDate(siegeDate, 35, 22);
		// Schedule siege auto start
		_startSiegeTask.schedule(1000);
	}
	
	public void startSiege()
	{
		setRegistrationPeriod(false);
		if (_clansInfo.isEmpty())
		{
			endSiege(false);
			return;
		}
		
		if ((_clansInfo.size() == 1) && (clanhall.getOwnerClan() == null))
		{
			endSiege(false);
			return;
		}
		
		if ((_clansInfo.size() == 1) && (clanhall.getOwnerClan() != null))
		{
			Clan clan = null;
			for (clanPlayersInfo a : _clansInfo.values())
			{
				clan = ClanTable.getInstance().getClanByName(a._clanName);
			}
			setInProgress(true);
			startSecondStep(clan);
			_siegeEndDate = Calendar.getInstance();
			_siegeEndDate.add(Calendar.MINUTE, 20);
			_endSiegeTask.schedule(1000);
			return;
		}
		setInProgress(true);
		spawnFlags();
		gateControl(1);
		anonce("Take place at the siege of his headquarters.", 1);
		ThreadPool.schedule(new startFirstStep(), 5 * 60000);
		_midTimer = ThreadPool.schedule(new midSiegeStep(), 25 * 60000);
		_siegeEndDate = Calendar.getInstance();
		_siegeEndDate.add(Calendar.MINUTE, 60);
		_endSiegeTask.schedule(1000);
	}
	
	public void startSecondStep(Clan winner)
	{
		final List<String> winPlayers = getInstance().getRegisteredPlayers(winner);
		unSpawnAll();
		_clansInfo.clear();
		final clanPlayersInfo regPlayers = new clanPlayersInfo();
		regPlayers._clanName = winner.getName();
		regPlayers._players = winPlayers;
		_clansInfo.put(winner.getClanId(), regPlayers);
		_clansInfo.put(clanhall.getOwnerClan().getClanId(), _ownerClanInfo);
		spawnFlags();
		gateControl(1);
		_finalStage = true;
		anonce("Take place at the siege of his headquarters.", 1);
		ThreadPool.schedule(new startFirstStep(), 5 * 60000);
	}
	
	public void endSiege(boolean par)
	{
		_mobControlTask.cancel();
		_finalStage = false;
		if (par)
		{
			final Clan winner = checkHaveWinner();
			if (winner != null)
			{
				ClanHallTable.getInstance().setOwner(clanhall.getId(), winner);
				anonce("Attention! Clan hall, castle was conquered by the clan of robbers " + winner.getName(), 2);
			}
			else
			{
				anonce("Attention! Clan hall, Fortress robbers did not get a new owner", 2);
			}
		}
		setInProgress(false);
		unSpawnAll();
		_clansInfo.clear();
		_clanCounter = 0;
		teleportPlayers();
		setNewSiegeDate(getSiegeDate().getTimeInMillis(), 35, 22);
		_startSiegeTask.schedule(1000);
	}
	
	public void unSpawnAll()
	{
		for (String clanName : getRegisteredClans())
		{
			final Clan clan = ClanTable.getInstance().getClanByName(clanName);
			final Monster mob = getQuestMob(clan);
			final Deco flag = getSiegeFlag(clan);
			
			if (mob != null)
			{
				mob.deleteMe();
			}
			
			if (flag != null)
			{
				flag.deleteMe();
			}
		}
	}
	
	public void gateControl(int value)
	{
		if (value == 1)
		{
			DoorData.getInstance().getDoor(22170001).openMe();
			DoorData.getInstance().getDoor(22170002).openMe();
			DoorData.getInstance().getDoor(22170003).closeMe();
			DoorData.getInstance().getDoor(22170004).closeMe();
		}
		else if (value == 2)
		{
			DoorData.getInstance().getDoor(22170001).closeMe();
			DoorData.getInstance().getDoor(22170002).closeMe();
			DoorData.getInstance().getDoor(22170003).closeMe();
			DoorData.getInstance().getDoor(22170004).closeMe();
		}
	}
	
	public void teleportPlayers()
	{
		final ClanHallZone zone = clanhall.getZone();
		for (Creature creature : zone.getCharactersInside())
		{
			if (creature instanceof Player)
			{
				final Clan clan = ((Player) creature).getClan();
				if (!isPlayerRegister(clan, creature.getName()))
				{
					creature.teleToLocation(88404, -21821, -2276);
				}
			}
		}
	}
	
	public Clan checkHaveWinner()
	{
		Clan res = null;
		int questMobCount = 0;
		for (String clanName : getRegisteredClans())
		{
			final Clan clan = ClanTable.getInstance().getClanByName(clanName);
			if (getQuestMob(clan) != null)
			{
				res = clan;
				questMobCount++;
			}
		}
		
		if (questMobCount > 1)
		{
			return null;
		}
		return res;
	}
	
	protected class midSiegeStep implements Runnable
	{
		@Override
		public void run()
		{
			_mobControlTask.cancel();
			final Clan winner = checkHaveWinner();
			if (winner != null)
			{
				if (clanhall.getOwnerClan() == null)
				{
					ClanHallTable.getInstance().setOwner(clanhall.getId(), winner);
					anonce("Attention! Clan hall, castle was conquered by the clan of robbers " + winner.getName(), 2);
					endSiege(false);
				}
				else
				{
					startSecondStep(winner);
				}
			}
			else
			{
				endSiege(true);
			}
		}
	}
	
	protected class startFirstStep implements Runnable
	{
		@Override
		public void run()
		{
			teleportPlayers();
			gateControl(2);
			int mobCounter = 1;
			for (String clanName : getRegisteredClans())
			{
				NpcTemplate template;
				final Clan clan = ClanTable.getInstance().getClanByName(clanName);
				if (clan == clanhall.getOwnerClan())
				{
					continue;
				}
				template = NpcTable.getInstance().getTemplate(35427 + mobCounter);
				final Monster questMob = new Monster(IdManager.getInstance().getNextId(), template);
				questMob.setHeading(100);
				questMob.getStatus().setCurrentHpMp(questMob.getMaxHp(), questMob.getMaxMp());
				if (mobCounter == 1)
				{
					questMob.spawnMe(83752, -17354, -1828);
				}
				else if (mobCounter == 2)
				{
					questMob.spawnMe(82018, -15126, -1829);
				}
				else if (mobCounter == 3)
				{
					questMob.spawnMe(85320, -16191, -1823);
				}
				else if (mobCounter == 4)
				{
					questMob.spawnMe(81522, -16503, -1829);
				}
				else if (mobCounter == 5)
				{
					questMob.spawnMe(83786, -15369, -1828);
				}
				final clanPlayersInfo regPlayers = _clansInfo.get(clan.getClanId());
				regPlayers._mob = questMob;
				mobCounter++;
			}
			_mobControlTask.schedule(3000);
			anonce("The battle began. Kill the enemy NPC", 1);
		}
	}
	
	public void spawnFlags()
	{
		int flagCounter = 1;
		for (String clanName : getRegisteredClans())
		{
			NpcTemplate template;
			final Clan clan = ClanTable.getInstance().getClanByName(clanName);
			if (clan == clanhall.getOwnerClan())
			{
				template = NpcTable.getInstance().getTemplate(35422);
			}
			else
			{
				template = NpcTable.getInstance().getTemplate(35422 + flagCounter);
			}
			final Deco flag = new Deco(IdManager.getInstance().getNextId(), template);
			flag.setTitle(clan.getName());
			flag.setHeading(100);
			flag.getStatus().setCurrentHpMp(flag.getMaxHp(), flag.getMaxMp());
			if (clan == clanhall.getOwnerClan())
			{
				flag.spawnMe(81700, -16300, -1828);
				final clanPlayersInfo regPlayers = _clansInfo.get(clan.getClanId());
				regPlayers._flag = flag;
				continue;
			}
			
			if (flagCounter == 1)
			{
				flag.spawnMe(83452, -17654, -1828);
			}
			else if (flagCounter == 2)
			{
				flag.spawnMe(81718, -14826, -1829);
			}
			else if (flagCounter == 3)
			{
				flag.spawnMe(85020, -15891, -1823);
			}
			else if (flagCounter == 4)
			{
				flag.spawnMe(81222, -16803, -1829);
			}
			else if (flagCounter == 5)
			{
				flag.spawnMe(83486, -15069, -1828);
			}
			final clanPlayersInfo regPlayers = _clansInfo.get(clan.getClanId());
			regPlayers._flag = flag;
			flagCounter++;
		}
	}
	
	public void setRegistrationPeriod(boolean par)
	{
		_registrationPeriod = par;
	}
	
	public boolean isRegistrationPeriod()
	{
		return _registrationPeriod;
	}
	
	public boolean isPlayerRegister(Clan playerClan, String playerName)
	{
		if (playerClan == null)
		{
			return false;
		}
		final clanPlayersInfo regPlayers = _clansInfo.get(playerClan.getClanId());
		return (regPlayers != null) && regPlayers._players.contains(playerName);
	}
	
	public boolean isClanOnSiege(Clan playerClan)
	{
		if (playerClan == clanhall.getOwnerClan())
		{
			return true;
		}
		final clanPlayersInfo regPlayers = _clansInfo.get(playerClan.getClanId());
		return regPlayers != null;
	}
	
	public synchronized int registerClanOnSiege(Player player, Clan playerClan)
	{
		if (_clanCounter == 5)
		{
			return 2;
		}
		final Item item = player.getInventory().getItemByItemId(5009);
		if ((item != null) && player.destroyItemWithoutTrace("Consume", item.getObjectId(), 1, null, false))
		{
			_clanCounter++;
			clanPlayersInfo regPlayers = _clansInfo.get(playerClan.getClanId());
			if (regPlayers == null)
			{
				regPlayers = new clanPlayersInfo();
				regPlayers._clanName = playerClan.getName();
				_clansInfo.put(playerClan.getClanId(), regPlayers);
			}
		}
		else
		{
			return 1;
		}
		return 0;
	}
	
	public boolean unRegisterClan(Clan playerClan)
	{
		if (_clansInfo.remove(playerClan.getClanId()) != null)
		{
			_clanCounter--;
			return true;
		}
		return false;
	}
	
	public List<String> getRegisteredClans()
	{
		final List<String> clans = new ArrayList<>();
		for (clanPlayersInfo a : _clansInfo.values())
		{
			clans.add(a._clanName);
		}
		return clans;
	}
	
	public List<String> getRegisteredPlayers(Clan playerClan)
	{
		if (playerClan == clanhall.getOwnerClan())
		{
			return _ownerClanInfo._players;
		}
		final clanPlayersInfo regPlayers = _clansInfo.get(playerClan.getClanId());
		if (regPlayers != null)
		{
			return regPlayers._players;
		}
		return null;
	}
	
	public Deco getSiegeFlag(Clan playerClan)
	{
		final clanPlayersInfo clanInfo = _clansInfo.get(playerClan.getClanId());
		if (clanInfo != null)
		{
			return clanInfo._flag;
		}
		return null;
	}
	
	public Monster getQuestMob(Clan clan)
	{
		final clanPlayersInfo clanInfo = _clansInfo.get(clan.getClanId());
		if (clanInfo != null)
		{
			return clanInfo._mob;
		}
		return null;
	}
	
	public int getPlayersCount(String playerClan)
	{
		for (clanPlayersInfo a : _clansInfo.values())
		{
			if (a._clanName.equalsIgnoreCase(playerClan))
			{
				return a._players.size();
			}
		}
		return 0;
	}
	
	public void addPlayer(Clan playerClan, String playerName)
	{
		if ((playerClan == clanhall.getOwnerClan()) && (_ownerClanInfo._players.size() < 18) && !_ownerClanInfo._players.contains(playerName))
		{
			_ownerClanInfo._players.add(playerName);
			return;
		}
		final clanPlayersInfo regPlayers = _clansInfo.get(playerClan.getClanId());
		if ((regPlayers != null) && (regPlayers._players.size() < 18) && !regPlayers._players.contains(playerName))
		{
			regPlayers._players.add(playerName);
		}
	}
	
	public void removePlayer(Clan playerClan, String playerName)
	{
		if ((playerClan == clanhall.getOwnerClan()) && _ownerClanInfo._players.contains(playerName))
		{
			_ownerClanInfo._players.remove(playerName);
			return;
		}
		final clanPlayersInfo regPlayers = _clansInfo.get(playerClan.getClanId());
		if ((regPlayers != null) && regPlayers._players.contains(playerName))
		{
			regPlayers._players.remove(playerName);
		}
	}
	
	private final ExclusiveTask _startSiegeTask = new ExclusiveTask()
	{
		@Override
		protected void onElapsed()
		{
			if (isInProgress())
			{
				cancel();
				return;
			}
			final Calendar siegeStart = Calendar.getInstance();
			siegeStart.setTimeInMillis(getSiegeDate().getTimeInMillis());
			final long registerTimeRemaining = siegeStart.getTimeInMillis() - Chronos.currentTimeMillis();
			siegeStart.add(Calendar.MINUTE, 60); // HOUR
			final long siegeTimeRemaining = siegeStart.getTimeInMillis() - Chronos.currentTimeMillis();
			long remaining = registerTimeRemaining;
			if ((registerTimeRemaining <= 0) && !_registrationPeriod)
			{
				if (clanhall.getOwnerClan() != null)
				{
					_ownerClanInfo._clanName = clanhall.getOwnerClan().getName();
				}
				else
				{
					_ownerClanInfo._clanName = "";
				}
				setRegistrationPeriod(true);
				anonce("Attention! The period of registration at the siege clan hall, castle robbers.", 2);
				remaining = siegeTimeRemaining;
			}
			if (siegeTimeRemaining <= 0)
			{
				startSiege();
				cancel();
				return;
			}
			schedule(remaining);
		}
	};
	
	public void anonce(String text, int type)
	{
		if (type == 1)
		{
			final CreatureSay cs = new CreatureSay(0, ChatType.SHOUT, "Journal", text);
			for (String clanName : getRegisteredClans())
			{
				final Clan clan = ClanTable.getInstance().getClanByName(clanName);
				for (String playerName : getRegisteredPlayers(clan))
				{
					final Player cha = World.getInstance().getPlayer(playerName);
					if (cha != null)
					{
						cha.sendPacket(cs);
					}
				}
			}
		}
		else
		{
			final CreatureSay cs = new CreatureSay(0, ChatType.SHOUT, "Journal", text);
			for (Player player : World.getInstance().getAllPlayers())
			{
				if (player.getInstanceId() == 0)
				{
					player.sendPacket(cs);
				}
			}
		}
	}
	
	protected final ExclusiveTask _endSiegeTask = new ExclusiveTask()
	{
		@Override
		protected void onElapsed()
		{
			if (!isInProgress())
			{
				cancel();
				return;
			}
			final long timeRemaining = _siegeEndDate.getTimeInMillis() - Chronos.currentTimeMillis();
			if (timeRemaining <= 0)
			{
				endSiege(true);
				cancel();
				return;
			}
			schedule(timeRemaining);
		}
	};
	
	protected final ExclusiveTask _mobControlTask = new ExclusiveTask()
	{
		@Override
		protected void onElapsed()
		{
			int mobCount = 0;
			for (clanPlayersInfo cl : _clansInfo.values())
			{
				if (cl._mob.isDead())
				{
					final Clan clan = ClanTable.getInstance().getClanByName(cl._clanName);
					unRegisterClan(clan);
				}
				else
				{
					mobCount++;
				}
			}
			teleportPlayers();
			if (mobCount < 2)
			{
				if (_finalStage)
				{
					_siegeEndDate = Calendar.getInstance();
					_endSiegeTask.cancel();
					_endSiegeTask.schedule(5000);
				}
				else
				{
					_midTimer.cancel(false);
					ThreadPool.schedule(new midSiegeStep(), 5000);
				}
			}
			else
			{
				schedule(3000);
			}
		}
	};
	
	protected class clanPlayersInfo
	{
		public String _clanName;
		public Deco _flag = null;
		public Monster _mob = null;
		public List<String> _players = new ArrayList<>();
	}
	
	public static BanditStrongholdSiege getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final BanditStrongholdSiege INSTANCE = new BanditStrongholdSiege();
	}
}
