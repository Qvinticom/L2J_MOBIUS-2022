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
package com.l2jmobius.gameserver.model.entity;

import java.util.Properties;

import com.l2jmobius.gameserver.Announcements;
import com.l2jmobius.gameserver.Olympiad;
import com.l2jmobius.gameserver.ThreadPoolManager;
import com.l2jmobius.gameserver.datatables.DoorTable;
import com.l2jmobius.gameserver.datatables.ItemTable;
import com.l2jmobius.gameserver.datatables.MapRegionTable;
import com.l2jmobius.gameserver.datatables.NpcTable;
import com.l2jmobius.gameserver.idfactory.IdFactory;
import com.l2jmobius.gameserver.instancemanager.ArenaManager;
import com.l2jmobius.gameserver.model.L2Character;
import com.l2jmobius.gameserver.model.L2ItemInstance;
import com.l2jmobius.gameserver.model.L2Party;
import com.l2jmobius.gameserver.model.L2Summon;
import com.l2jmobius.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PetInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2TvTManagerInstance;
import com.l2jmobius.gameserver.model.zone.type.L2ArenaZone;
import com.l2jmobius.util.Rnd;

import javolution.util.FastList;

/**
 * @author DnR
 */
public class TvTEvent
{
	// TvT Event States
	public static byte STARTED = 1;
	public static byte REGISTER = 2;
	public static byte ENDED = 3;
	public static byte ABORTED = 4;
	
	// TvT related lists
	private static FastList<L2PcInstance> _registered;
	private static FastList<L2PcInstance> _blueTeam;
	private static FastList<L2PcInstance> _redTeam;
	private static FastList<L2ItemInstance> _rewardList;
	
	// Team kills
	private static int _blueTeamKills = 0;
	private static int _redTeamKills = 0;
	
	// TvT state
	protected static byte _state = 0;
	
	// TvT parameters
	public static boolean isEnabled = false;
	public static int minParticipants;
	public static int maxParticipants;
	public static byte minLevel;
	public static byte maxLevel;
	public static int npcManager;
	
	private static int eventDelay;
	private static int npcX;
	private static int npcY;
	private static int npcZ;
	private static int participationTime;
	private static int eventDuration;
	
	// event zone
	private L2ArenaZone _arena;
	private L2DoorInstance[] _doors;
	
	public static void initialize(Properties settings)
	{
		// Load Event if not loaded before or else reload settings.
		if (!isEnabled && (_state == 0))
		{
			new TvTEvent(settings);
		}
		else
		{
			load(settings);
		}
	}
	
	private TvTEvent(Properties settings)
	{
		if (load(settings))
		{
			// load player listing
			getRegistered();
			getBlueTeam();
			getRedTeam();
			
			// load event zone
			_arena = ArenaManager.getInstance().getArena(11012);
			
			// load arena doors
			_doors = new L2DoorInstance[2];
			_doors[0] = DoorTable.getInstance().getDoor(24190002);
			_doors[1] = DoorTable.getInstance().getDoor(24190003);
			
			System.out.println("Initialized TvT Event");
			
			scheduleRegistration();
			System.out.println("TvT Event: Next event in " + (eventDelay / 60000) + " minute(s)");
		}
	}
	
	private static boolean load(Properties settings)
	{
		// TvT Event settings
		if (settings == null)
		{
			return false;
		}
		
		isEnabled = Boolean.valueOf(settings.getProperty("TvTEventEnable", "false"));
		
		// don't load if TvT is disabled
		if (!isEnabled)
		{
			return false;
		}
		
		maxParticipants = Integer.parseInt(settings.getProperty("TvTMaxParticipants", "40"));
		minParticipants = Integer.parseInt(settings.getProperty("TvTMinParticipants", "6"));
		minLevel = Byte.parseByte(settings.getProperty("TvTEventMinLevel", "60"));
		if ((minLevel < 1) || (minLevel > 78))
		{
			minLevel = 60;
		}
		maxLevel = Byte.parseByte(settings.getProperty("TvTEventMaxLevel", "78"));
		if ((maxLevel < 1) || (maxLevel > 78))
		{
			maxLevel = 78;
		}
		eventDelay = Integer.parseInt(settings.getProperty("TvTEventDelay", "18000")) * 1000;
		npcManager = Integer.parseInt(settings.getProperty("TvTNpcManager", "12371"));
		npcX = Integer.parseInt(settings.getProperty("TvTNpcX", "151808"));
		npcY = Integer.parseInt(settings.getProperty("TvTNpcY", "46864"));
		npcZ = Integer.parseInt(settings.getProperty("TvTNpcZ", "-3408"));
		participationTime = Integer.parseInt(settings.getProperty("TvTEventParticipationTime", "1200"));
		eventDuration = Integer.parseInt(settings.getProperty("TvTEventDuration", "1800"));
		
		_rewardList = new FastList<>();
		String[] propertySplit;
		propertySplit = settings.getProperty("TvTEventRewardList", "").split(";");
		for (final String item : propertySplit)
		{
			final String[] itemSplit = item.split(",");
			if (itemSplit.length != 2)
			{
				System.out.println("[TvTEventRewardList]: invalid config property -> TvTEventRewardList \"" + item + "\"");
			}
			else
			{
				try
				{
					final L2ItemInstance reward = ItemTable.getInstance().createDummyItem(Integer.valueOf(itemSplit[0]));
					if (reward == null)
					{
						System.out.println("[TvTEventRewardList]: Invalid item " + itemSplit[0]);
						continue;
					}
					
					reward.setCount(Integer.valueOf(itemSplit[1]));
					_rewardList.add(reward);
				}
				catch (final NumberFormatException nfe)
				{
					if (!item.isEmpty())
					{
						System.out.println("[TvTEventRewardList]: invalid config property -> ItemList \"" + itemSplit[0] + "\"" + itemSplit[1]);
					}
				}
			}
		}
		
		return true;
	}
	
	private void scheduleRegistration()
	{
		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			@Override
			public void run()
			{
				if (isEnabled)
				{
					scheduleEvent();
				}
				else
				{
					_state = 0; // Default state
				}
			}
		}, eventDelay);
	}
	
	protected void scheduleEvent()
	{
		// spawn TvT manager NPC
		final L2TvTManagerInstance npc = new L2TvTManagerInstance(IdFactory.getInstance().getNextId(), NpcTable.getInstance().getTemplate(npcManager));
		
		npc.setTitle("TvT Registration");
		npc.spawnMe(npcX, npcY, npcZ);
		
		_state = REGISTER;
		
		Announcements.getInstance().announceToAll("TvT Event: Registration opened for " + (participationTime / 60) + " minute(s).");
		eventTimer(participationTime);
		
		if ((_registered.size() >= minParticipants) && (_state != ABORTED))
		{
			// prepare Coliseum arena for event
			prepareColiseum();
			
			// port players and start event
			Announcements.getInstance().announceToAll("TvT Event: Event has started!");
			portTeamsToArena();
			eventTimer(eventDuration);
			
			if (_state == ABORTED)
			{
				Announcements.getInstance().announceToAll("TvT Event: Event was cancelled.");
			}
			else
			{
				Announcements.getInstance().announceToAll("TvT Event: Blue Team kills: " + _blueTeamKills + " , Red Team kills: " + _redTeamKills + ".");
			}
			
			// shutting down event
			eventRemovals();
		}
		else
		{
			if (_state == ABORTED)
			{
				Announcements.getInstance().announceToAll("TvT Event: Event was cancelled.");
			}
			else
			{
				Announcements.getInstance().announceToAll("TvT Event: Event was cancelled due to lack of participation.");
			}
			_registered.clear();
		}
		
		// event finished
		Announcements.getInstance().announceToAll("TvT Event: Next event in " + (eventDelay / 60000) + " minute(s).");
		
		npc.deleteMe();
		openArenaDoors();
		scheduleRegistration();
	}
	
	private void prepareColiseum()
	{
		if (_arena != null)
		{
			for (final L2Character activeChar : _arena._characterList.values())
			{
				final L2PcInstance player = activeChar.getActingPlayer();
				if (player == null)
				{
					continue;
				}
				
				player.teleToLocation(MapRegionTable.TeleportWhereType.Town);
			}
		}
		
		for (final L2DoorInstance door : _doors)
		{
			if (door != null)
			{
				door.closeMe();
			}
		}
	}
	
	private void openArenaDoors()
	{
		for (final L2DoorInstance door : _doors)
		{
			if (door != null)
			{
				door.openMe();
			}
		}
	}
	
	private void eventRemovals()
	{
		if ((_blueTeam.size() == 0) && (_redTeam.size() == 0))
		{
			_state = ENDED;
			_blueTeam.clear();
			_redTeam.clear();
			_blueTeamKills = 0;
			_redTeamKills = 0;
			return;
		}
		
		_state = ENDED;
		
		if (_blueTeamKills > _redTeamKills)
		{
			for (final L2PcInstance blue : _blueTeam)
			{
				if (blue == null)
				{
					continue;
				}
				
				for (final L2ItemInstance reward : _rewardList)
				{
					if (reward == null)
					{
						continue;
					}
					
					blue.addItem("TvTReward", reward.getItemId(), reward.getCount(), null, true);
				}
			}
		}
		else if (_redTeamKills > _blueTeamKills)
		{
			for (final L2PcInstance red : _redTeam)
			{
				if (red == null)
				{
					continue;
				}
				
				for (final L2ItemInstance reward : _rewardList)
				{
					if (reward == null)
					{
						continue;
					}
					
					red.addItem("TvTReward", reward.getItemId(), reward.getCount(), null, true);
				}
			}
		}
		else
		{
			Announcements.getInstance().announceToAll("TvT Event: Event ended in a Tie. No rewards will be given!");
		}
		
		for (final L2PcInstance blue : _blueTeam)
		{
			if (blue == null)
			{
				continue;
			}
			
			if (blue.isDead())
			{
				blue.doRevive();
			}
			
			blue.setEventTeam(0);
			blue.teleToLocation(npcX, npcY, npcZ, true);
		}
		
		for (final L2PcInstance red : _redTeam)
		{
			if (red == null)
			{
				continue;
			}
			
			if (red.isDead())
			{
				red.doRevive();
			}
			
			red.setEventTeam(0);
			red.teleToLocation(npcX, npcY, npcZ, true);
		}
		
		_blueTeam.clear();
		_redTeam.clear();
		_blueTeamKills = 0;
		_redTeamKills = 0;
	}
	
	private void eventTimer(int time)
	{
		for (int seconds = time; (seconds > 0) && (_state != ABORTED); seconds--)
		{
			switch (seconds)
			{
				case 900:
				case 600:
				case 300:
				case 60:
					if (_state == STARTED)
					{
						Announcements.getInstance().announceToAll("TvT Event: " + (seconds / 60) + " minute(s) until event is finished!");
					}
					else
					{
						Announcements.getInstance().announceToAll("TvT Event: " + (seconds / 60) + " minute(s) until registration is closed!");
					}
					break;
				case 30:
				case 5:
					if (_state == STARTED)
					{
						Announcements.getInstance().announceToAll("TvT Event: " + seconds + " second(s) until event is finished!");
					}
					else
					{
						Announcements.getInstance().announceToAll("TvT Event: " + seconds + " second(s) until registration is closed!");
					}
					break;
				case 20:
					if (_state == REGISTER)
					{
						for (final L2PcInstance registered : _registered)
						{
							if (registered == null)
							{
								continue;
							}
							
							registered.sendMessage("You will be teleported to arena in 20 seconds.");
						}
					}
					break;
			}
			
			final long oneSecWaitStart = System.currentTimeMillis();
			while ((oneSecWaitStart + 1000L) > System.currentTimeMillis())
			{
				try
				{
					Thread.sleep(1);
				}
				catch (final InterruptedException ie)
				{
				}
			}
		}
	}
	
	private void portTeamsToArena()
	{
		L2PcInstance player;
		while (_registered.size() > 0)
		{
			player = _registered.get(Rnd.get(_registered.size()));
			
			// First create 2 event teams
			if (_blueTeam.size() > _redTeam.size())
			{
				_redTeam.add(player);
				player.setEventTeam(2);
			}
			else
			{
				_blueTeam.add(player);
				player.setEventTeam(1);
			}
			
			// Abort casting if player casting
			if (player.isCastingNow())
			{
				player.abortCast();
			}
			
			player.getAppearance().setVisible();
			
			if (player.isDead())
			{
				player.doRevive();
			}
			else
			{
				player.setCurrentCp(player.getMaxCp());
				player.setCurrentHp(player.getMaxHp());
				player.setCurrentMp(player.getMaxMp());
			}
			
			// Remove Buffs
			player.stopAllEffects();
			player.addCharge(-player.getCharges());
			
			// Remove Summon's Buffs
			if (player.getPet() != null)
			{
				final L2Summon summon = player.getPet();
				summon.stopAllEffects();
				
				if (summon.isCastingNow())
				{
					summon.abortCast();
				}
				
				if (summon instanceof L2PetInstance)
				{
					summon.unSummon(player);
				}
			}
			
			// Remove player from his party
			if (player.getParty() != null)
			{
				final L2Party party = player.getParty();
				party.removePartyMember(player, true);
			}
			
			_registered.remove(player);
		}
		
		_state = STARTED;
		
		// Port teams
		for (final L2PcInstance blue : _blueTeam)
		{
			if (blue == null)
			{
				continue;
			}
			
			blue.teleToLocation(148476, 46061, -3411, true);
		}
		
		for (final L2PcInstance red : _redTeam)
		{
			if (red == null)
			{
				continue;
			}
			
			red.teleToLocation(150480, 47444, -3411, true);
		}
	}
	
	public static void registerPlayer(L2PcInstance player)
	{
		if (_state != REGISTER)
		{
			player.sendMessage("TvT Registration is not in progress.");
			return;
		}
		
		if (player.isFestivalParticipant())
		{
			player.sendMessage("Festival participants cannot register to the event.");
			return;
		}
		
		if (player.isInJail())
		{
			player.sendMessage("Jailed players cannot register to the event.");
			return;
		}
		
		if (player.isDead())
		{
			player.sendMessage("Dead players cannot register to the event.");
			return;
		}
		
		if (Olympiad.getInstance().isRegisteredInComp(player))
		{
			player.sendMessage("Grand Olympiad participants cannot register to the event.");
			return;
		}
		
		if ((player.getLevel() < minLevel) || (player.getLevel() > maxLevel))
		{
			player.sendMessage("You have not reached the appropriate level to join the event.");
			return;
		}
		
		if (_registered.size() == maxParticipants)
		{
			player.sendMessage("There is no more room for you to register to the event.");
			return;
		}
		
		for (final L2PcInstance registered : _registered)
		{
			if (registered == null)
			{
				continue;
			}
			
			if (registered.getObjectId() == player.getObjectId())
			{
				player.sendMessage("You are already registered in the TvT event.");
				return;
			}
			
			if ((registered.getClient() == null) || (player.getClient() == null))
			{
				continue;
			}
			
			final String ip1 = player.getClient().getConnection().getInetAddress().getHostAddress();
			final String ip2 = registered.getClient().getConnection().getInetAddress().getHostAddress();
			if ((ip1 != null) && (ip2 != null) && ip1.equals(ip2))
			{
				player.sendMessage("Your IP is already registered in the TvT event.");
				return;
			}
		}
		
		_registered.add(player);
		
		player.sendMessage("You have registered to participate in the TvT Event.");
	}
	
	public static void removePlayer(L2PcInstance player)
	{
		if (_registered.contains(player))
		{
			_registered.remove(player);
			player.sendMessage("You have been removed from the TvT Event registration list.");
		}
		else if (player.getEventTeam() == 1)
		{
			_blueTeam.remove(player);
		}
		else if (player.getEventTeam() == 2)
		{
			_redTeam.remove(player);
		}
		
		// If no participants left, abort event
		if ((player.getEventTeam() > 0) && (_blueTeam.size() == 0) && (_redTeam.size() == 0))
		{
			_state = ABORTED;
		}
		
		// Now, remove team status
		player.setEventTeam(0);
	}
	
	public static boolean isRegistered(L2PcInstance player)
	{
		if (_registered == null)
		{
			return false;
		}
		
		return _registered.contains(player);
	}
	
	public static FastList<L2PcInstance> getBlueTeam()
	{
		if (_blueTeam == null)
		{
			_blueTeam = new FastList<>();
		}
		return _blueTeam;
	}
	
	public static FastList<L2PcInstance> getRedTeam()
	{
		if (_redTeam == null)
		{
			_redTeam = new FastList<>();
		}
		return _redTeam;
	}
	
	public static FastList<L2PcInstance> getRegistered()
	{
		if (_registered == null)
		{
			_registered = new FastList<>();
		}
		return _registered;
	}
	
	public static int getBlueTeamKills()
	{
		return _blueTeamKills;
	}
	
	public static int getRedTeamKills()
	{
		return _redTeamKills;
	}
	
	public static byte getEventState()
	{
		return _state;
	}
	
	public static void setEventState(byte state)
	{
		_state = state;
	}
	
	public static void increaseBlueKills()
	{
		_blueTeamKills++;
	}
	
	public static void increaseRedKills()
	{
		_redTeamKills++;
	}
}