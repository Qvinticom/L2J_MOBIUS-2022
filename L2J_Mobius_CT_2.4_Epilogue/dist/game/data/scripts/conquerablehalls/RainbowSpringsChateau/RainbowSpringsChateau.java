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
package conquerablehalls.RainbowSpringsChateau;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;

import org.l2jmobius.Config;
import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.commons.util.Chronos;
import org.l2jmobius.commons.util.CommonUtil;
import org.l2jmobius.gameserver.cache.HtmCache;
import org.l2jmobius.gameserver.data.SpawnTable;
import org.l2jmobius.gameserver.data.sql.ClanTable;
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.enums.TeleportWhereType;
import org.l2jmobius.gameserver.instancemanager.CHSiegeManager;
import org.l2jmobius.gameserver.instancemanager.ZoneManager;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.Party;
import org.l2jmobius.gameserver.model.Spawn;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.clan.Clan;
import org.l2jmobius.gameserver.model.item.ItemTemplate;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.siege.clanhalls.ClanHallSiegeEngine;
import org.l2jmobius.gameserver.model.siege.clanhalls.SiegableHall;
import org.l2jmobius.gameserver.model.siege.clanhalls.SiegeStatus;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.network.serverpackets.NpcSay;
import org.l2jmobius.gameserver.util.Broadcast;

/**
 * Rainbow Springs Chateau clan hall siege script.
 * @author BiggBoss
 */
public class RainbowSpringsChateau extends ClanHallSiegeEngine
{
	protected static class SetFinalAttackers implements Runnable
	{
		@Override
		public void run()
		{
			if (_rainbow == null)
			{
				_rainbow = CHSiegeManager.getInstance().getSiegableHall(RAINBOW_SPRINGS);
			}
			
			int spotLeft = 4;
			if (_rainbow.getOwnerId() > 0)
			{
				final Clan owner = ClanTable.getInstance().getClan(_rainbow.getOwnerId());
				if (owner != null)
				{
					_rainbow.free();
					owner.setHideoutId(0);
					_acceptedClans.add(owner);
					--spotLeft;
				}
				
				for (int i = 0; i < spotLeft; i++)
				{
					long counter = 0;
					Clan clan = null;
					for (Entry<Integer, Long> entry : _warDecreesCount.entrySet())
					{
						final Clan actingClan = ClanTable.getInstance().getClan(entry.getKey());
						if ((actingClan == null) || (actingClan.getDissolvingExpiryTime() > 0))
						{
							_warDecreesCount.remove(entry.getKey());
							continue;
						}
						
						final long count = entry.getValue();
						if (count > counter)
						{
							counter = count;
							clan = actingClan;
						}
					}
					if ((clan != null) && (_acceptedClans.size() < 4))
					{
						_acceptedClans.add(clan);
						final Player leader = clan.getLeader().getPlayer();
						if (leader != null)
						{
							leader.sendMessage("Your clan has been accepted to join the RainBow Srpings Chateau siege!");
						}
					}
				}
				if (_acceptedClans.size() >= 2)
				{
					_nextSiege = ThreadPool.schedule(new SiegeStart(), 3600000);
					_rainbow.updateSiegeStatus(SiegeStatus.WAITING_BATTLE);
				}
				else
				{
					Broadcast.toAllOnlinePlayers("Rainbow Springs Chateau siege aborted due lack of population");
				}
			}
		}
	}
	
	protected static class SiegeStart implements Runnable
	{
		@Override
		public void run()
		{
			if (_rainbow == null)
			{
				_rainbow = CHSiegeManager.getInstance().getSiegableHall(RAINBOW_SPRINGS);
			}
			
			// XXX _rainbow.siegeStarts();
			
			spawnGourds();
			_siegeEnd = ThreadPool.schedule(new SiegeEnd(null), _rainbow.getSiegeLength() - 120000);
		}
	}
	
	public static final Clan _winner = null;
	
	@Override
	public Clan getWinner()
	{
		return _winner;
	}
	
	private static class SiegeEnd implements Runnable
	{
		private final Clan _winner;
		
		protected SiegeEnd(Clan winner)
		{
			_winner = winner;
		}
		
		@Override
		public void run()
		{
			if (_rainbow == null)
			{
				_rainbow = CHSiegeManager.getInstance().getSiegableHall(RAINBOW_SPRINGS);
			}
			
			unSpawnGourds();
			
			if (_winner != null)
			{
				_rainbow.setOwner(_winner);
			}
			
			// XXX _rainbow.siegeEnds();
			
			ThreadPool.schedule(new SetFinalAttackers(), _rainbow.getNextSiegeTime());
			setRegistrationEndString((_rainbow.getNextSiegeTime() + Chronos.currentTimeMillis()) - 3600000);
			// Teleport out of the arenas is made 2 mins after game ends
			ThreadPool.schedule(new TeleportBack(), 120000);
		}
	}
	
	protected static class TeleportBack implements Runnable
	{
		@Override
		public void run()
		{
			for (int arenaId : ARENA_ZONES)
			{
				final Collection<Creature> chars = ZoneManager.getInstance().getZoneById(arenaId).getCharactersInside();
				for (Creature chr : chars)
				{
					if (chr != null)
					{
						chr.teleToLocation(TeleportWhereType.TOWN);
					}
				}
			}
		}
	}
	
	private static final int RAINBOW_SPRINGS = 62;
	
	private static final int WAR_DECREES = 8034;
	private static final int RAINBOW_NECTAR = 8030;
	private static final int RAINBOW_MWATER = 8031;
	private static final int RAINBOW_WATER = 8032;
	private static final int RAINBOW_SULFUR = 8033;
	
	private static final int MESSENGER = 35604;
	private static final int CARETAKER = 35603;
	private static final int CHEST = 35593;
	
	private static final int[] GOURDS =
	{
		35588,
		35589,
		35590,
		35591
	};
	private static Spawn[] _gourds = new Spawn[4];
	
	private static final int[] YETIS =
	{
		35596,
		35597,
		35598,
		35599
	};
	
	private static final Location[] ARENAS = new Location[]
	{
		new Location(151562, -127080, -2214), // Arena 1
		new Location(153141, -125335, -2214), // Arena 2
		new Location(153892, -127530, -2214), // Arena 3
		new Location(155657, -125752, -2214), // Arena 4
	};
	
	protected static final int[] ARENA_ZONES =
	{
		112081,
		112082,
		112083,
		112084
	};
	
	private static final String[] _textPassages =
	{
		"Fight for Rainbow Springs!",
		"Are you a match for the Yetti?",
		"Did somebody order a knuckle sandwich?"
	};
	
	private static final Skill[] DEBUFFS = {};
	
	protected static Map<Integer, Long> _warDecreesCount = new HashMap<>();
	protected static List<Clan> _acceptedClans = new ArrayList<>(4);
	private static Map<String, List<Clan>> _usedTextPassages = new HashMap<>();
	private static Map<Clan, Integer> _pendingItemToGet = new HashMap<>();
	
	protected static SiegableHall _rainbow;
	protected static ScheduledFuture<?> _nextSiege;
	protected static ScheduledFuture<?> _siegeEnd;
	private static String _registrationEnds;
	
	public RainbowSpringsChateau()
	{
		super(RAINBOW_SPRINGS);
		
		addFirstTalkId(MESSENGER);
		addTalkId(MESSENGER);
		addFirstTalkId(CARETAKER);
		addTalkId(CARETAKER);
		addFirstTalkId(YETIS);
		addTalkId(YETIS);
		
		loadAttackers();
		
		_rainbow = CHSiegeManager.getInstance().getSiegableHall(RAINBOW_SPRINGS);
		if (_rainbow != null)
		{
			final long delay = _rainbow.getNextSiegeTime();
			if (delay > -1)
			{
				setRegistrationEndString(delay - 3600000);
				_nextSiege = ThreadPool.schedule(new SetFinalAttackers(), delay);
			}
			else
			{
				LOGGER.warning("CHSiegeManager: No Date setted for RainBow Springs Chateau Clan hall siege!. SIEGE CANCELED!");
			}
		}
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		String html = "";
		final int npcId = npc.getId();
		if (npcId == MESSENGER)
		{
			final String main = (_rainbow.getOwnerId() > 0) ? "messenger_yetti001.htm" : "messenger_yetti001a.htm";
			html = HtmCache.getInstance().getHtm(player, "data/scripts/conquerablehalls/RainbowSpringsChateau/" + main);
			html = html.replace("%time%", _registrationEnds);
			if (_rainbow.getOwnerId() > 0)
			{
				html = html.replace("%owner%", ClanTable.getInstance().getClan(_rainbow.getOwnerId()).getName());
			}
		}
		else if (npcId == CARETAKER)
		{
			if (_rainbow.isInSiege())
			{
				html = "game_manager003.htm";
			}
			else
			{
				html = "game_manager001.htm";
			}
		}
		else if (CommonUtil.contains(YETIS, npcId))
		{
			// TODO: Review.
			if (_rainbow.isInSiege())
			{
				if (!player.isClanLeader())
				{
					html = "no_clan_leader.htm";
				}
				else
				{
					final Clan clan = player.getClan();
					if (_acceptedClans.contains(clan))
					{
						final int index = _acceptedClans.indexOf(clan);
						if (npcId == YETIS[index])
						{
							html = "yeti_main.htm";
						}
					}
				}
			}
		}
		player.setLastQuestNpcObject(npc.getObjectId());
		return html;
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String html = event;
		final Clan clan = player.getClan();
		switch (npc.getId())
		{
			case MESSENGER:
			{
				switch (event)
				{
					case "register":
					{
						if (!player.isClanLeader())
						{
							html = "messenger_yetti010.htm";
						}
						else if ((clan.getCastleId() > 0) || (clan.getFortId() > 0) || (clan.getHideoutId() > 0))
						{
							html = "messenger_yetti012.htm";
						}
						else if (!_rainbow.isRegistering())
						{
							html = "messenger_yetti014.htm";
						}
						else if (_warDecreesCount.containsKey(clan.getId()))
						{
							html = "messenger_yetti013.htm";
						}
						else if ((clan.getLevel() < 3) || (clan.getMembersCount() < 5))
						{
							html = "messenger_yetti011.htm";
						}
						else
						{
							final Item warDecrees = player.getInventory().getItemByItemId(WAR_DECREES);
							if (warDecrees == null)
							{
								html = "messenger_yetti008.htm";
							}
							else
							{
								final long count = warDecrees.getCount();
								_warDecreesCount.put(clan.getId(), count);
								player.destroyItem("Rainbow Springs Registration", warDecrees, npc, true);
								updateAttacker(clan.getId(), count, false);
								html = "messenger_yetti009.htm";
							}
						}
						break;
					}
					case "cancel":
					{
						if (!player.isClanLeader())
						{
							html = "messenger_yetti010.htm";
						}
						else if (!_warDecreesCount.containsKey(clan.getId()))
						{
							html = "messenger_yetti016.htm";
						}
						else if (!_rainbow.isRegistering())
						{
							html = "messenger_yetti017.htm";
						}
						else
						{
							updateAttacker(clan.getId(), 0, true);
							html = "messenger_yetti018.htm";
						}
						break;
					}
					case "unregister":
					{
						if (_rainbow.isRegistering())
						{
							if (_warDecreesCount.containsKey(clan.getId()))
							{
								player.addItem("Rainbow Spring unregister", WAR_DECREES, _warDecreesCount.get(clan.getId()) / 2, npc, true);
								_warDecreesCount.remove(clan.getId());
								html = "messenger_yetti019.htm";
							}
							else
							{
								html = "messenger_yetti020.htm";
							}
						}
						else if (_rainbow.isWaitingBattle())
						{
							_acceptedClans.remove(clan);
							html = "messenger_yetti020.htm";
						}
						break;
					}
				}
				break;
			}
			case CARETAKER:
			{
				if (event.equals("portToArena"))
				{
					final Party party = player.getParty();
					if (clan == null)
					{
						html = "game_manager009.htm";
					}
					else if (!player.isClanLeader())
					{
						html = "game_manager004.htm";
					}
					else if (!player.isInParty())
					{
						html = "game_manager005.htm";
					}
					else if (party.getLeaderObjectId() != player.getObjectId())
					{
						html = "game_manager006.htm";
					}
					else
					{
						final int clanId = player.getClanId();
						boolean nonClanMemberInParty = false;
						for (Player member : party.getMembers())
						{
							if (member.getClanId() != clanId)
							{
								nonClanMemberInParty = true;
								break;
							}
						}
						if (nonClanMemberInParty)
						{
							html = "game_manager007.htm";
						}
						else if (party.getMemberCount() < 5)
						{
							html = "game_manager008.htm";
						}
						else if ((clan.getCastleId() > 0) || (clan.getFortId() > 0) || (clan.getHideoutId() > 0))
						{
							html = "game_manager010.htm";
						}
						else if (clan.getLevel() < Config.CHS_CLAN_MINLEVEL)
						{
							html = "game_manager011.htm";
						}
						// else if () // Something about the rules.
						// {
						// html = "game_manager012.htm";
						// }
						// else if () // Already registered.
						// {
						// html = "game_manager013.htm";
						// }
						else if (!_acceptedClans.contains(clan))
						{
							html = "game_manager014.htm";
						}
						// else if () // Not have enough cards to register.
						// {
						// html = "game_manager015.htm";
						// }
						else
						{
							portToArena(player, _acceptedClans.indexOf(clan));
						}
					}
				}
				break;
			}
		}
		
		if (event.startsWith("enterText"))
		{
			// Shouldn't happen
			if (!_acceptedClans.contains(clan))
			{
				return null;
			}
			
			final String[] split = event.split("_ ");
			if (split.length < 2)
			{
				return null;
			}
			
			final String passage = split[1];
			if (!isValidPassage(passage))
			{
				return null;
			}
			
			if (_usedTextPassages.containsKey(passage))
			{
				final List<Clan> list = _usedTextPassages.get(passage);
				if (list.contains(clan))
				{
					html = "yeti_passage_used.htm";
				}
				else
				{
					list.add(clan);
					synchronized (_pendingItemToGet)
					{
						if (_pendingItemToGet.containsKey(clan))
						{
							int left = _pendingItemToGet.get(clan);
							++left;
							_pendingItemToGet.put(clan, left);
						}
						else
						{
							_pendingItemToGet.put(clan, 1);
						}
					}
					html = "yeti_item_exchange.htm";
				}
			}
		}
		// TODO(Zoey76): Rewrite this to prevent exploits...
		// else if (event.startsWith("getItem"))
		// {
		// if (!_pendingItemToGet.containsKey(clan))
		// {
		// html = "yeti_cannot_exchange.htm";
		// }
		//
		// int left = _pendingItemToGet.get(clan);
		// if (left > 0)
		// {
		// int itemId = Integer.parseInt(event.split("_")[1]);
		// player.addItem("Rainbow Spring Chateau Siege", itemId, 1, npc, true);
		// --left;
		// _pendingItemToGet.put(clan, left);
		// html = "yeti_main.htm";
		// }
		// else
		// {
		// html = "yeti_cannot_exchange.htm";
		// }
		// }
		return html;
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		if (!_rainbow.isInSiege())
		{
			return null;
		}
		
		final Clan clan = killer.getClan();
		if ((clan == null) || !_acceptedClans.contains(clan))
		{
			return null;
		}
		
		final int npcId = npc.getId();
		final int index = _acceptedClans.indexOf(clan);
		if (npcId == CHEST)
		{
			shoutRandomText(npc);
		}
		else if (npcId == GOURDS[index])
		{
			synchronized (this)
			{
				if (_siegeEnd != null)
				{
					_siegeEnd.cancel(false);
				}
				ThreadPool.execute(new SiegeEnd(clan));
			}
		}
		
		return null;
	}
	
	@Override
	public String onItemUse(ItemTemplate item, Player player)
	{
		if (!_rainbow.isInSiege())
		{
			return null;
		}
		
		final WorldObject target = player.getTarget();
		if (!(target instanceof Npc))
		{
			return null;
		}
		
		final int yeti = target.getId();
		if (!isYetiTarget(yeti))
		{
			return null;
		}
		
		final Clan clan = player.getClan();
		if ((clan == null) || !_acceptedClans.contains(clan))
		{
			return null;
		}
		
		// Nectar must spawn the enraged yeti. Dunno if it makes any other thing
		// Also, the items must execute:
		// - Reduce gourd hpb ( reduceGourdHp(int, Player) )
		// - Cast debuffs on enemy clans ( castDebuffsOnEnemies(int) )
		// - Change arena gourds ( moveGourds() )
		// - Increase gourd hp ( increaseGourdHp(int) )
		
		final int itemId = item.getId();
		if (itemId == RAINBOW_NECTAR)
		{
			// Spawn enraged (where?)
			reduceGourdHp(_acceptedClans.indexOf(clan), player);
		}
		else if (itemId == RAINBOW_MWATER)
		{
			increaseGourdHp(_acceptedClans.indexOf(clan));
		}
		else if (itemId == RAINBOW_WATER)
		{
			moveGourds();
		}
		else if (itemId == RAINBOW_SULFUR)
		{
			castDebuffsOnEnemies(_acceptedClans.indexOf(clan));
		}
		return null;
	}
	
	private void portToArena(Player leader, int arena)
	{
		if ((arena < 0) || (arena > 3))
		{
			LOGGER.warning("RainbowSptringChateau siege: Wrong arena id passed: " + arena);
			return;
		}
		for (Player pc : leader.getParty().getMembers())
		{
			if (pc != null)
			{
				pc.stopAllEffects();
				if (pc.hasSummon())
				{
					pc.getSummon().unSummon(pc);
				}
				pc.teleToLocation(ARENAS[arena]);
			}
		}
	}
	
	protected static void spawnGourds()
	{
		for (int i = 0; i < _acceptedClans.size(); i++)
		{
			if (_gourds[i] == null)
			{
				try
				{
					_gourds[i] = new Spawn(GOURDS[i]);
					_gourds[i].setXYZ(ARENAS[i].getX() + 150, ARENAS[i].getY() + 150, ARENAS[i].getZ());
					_gourds[i].setHeading(1);
					_gourds[i].setAmount(1);
				}
				catch (Exception e)
				{
					LOGGER.warning("Problem at RainbowSpringsChateau: " + e.getMessage());
				}
			}
			SpawnTable.getInstance().addNewSpawn(_gourds[i], false);
			_gourds[i].init();
		}
	}
	
	protected static void unSpawnGourds()
	{
		for (int i = 0; i < _acceptedClans.size(); i++)
		{
			_gourds[i].getLastSpawn().deleteMe();
			SpawnTable.getInstance().deleteSpawn(_gourds[i], false);
		}
	}
	
	private void moveGourds()
	{
		final Spawn[] tempArray = _gourds;
		final int iterator = _acceptedClans.size();
		for (int i = 0; i < iterator; i++)
		{
			final Spawn oldSpawn = _gourds[(iterator - 1) - i];
			final Spawn curSpawn = tempArray[i];
			_gourds[(iterator - 1) - i] = curSpawn;
			curSpawn.getLastSpawn().teleToLocation(oldSpawn.getLocation());
		}
	}
	
	private void reduceGourdHp(int index, Player player)
	{
		final Spawn gourd = _gourds[index];
		gourd.getLastSpawn().reduceCurrentHp(1000, player, null);
	}
	
	private void increaseGourdHp(int index)
	{
		final Spawn gourd = _gourds[index];
		final Npc gourdNpc = gourd.getLastSpawn();
		gourdNpc.setCurrentHp(gourdNpc.getCurrentHp() + 1000);
	}
	
	private void castDebuffsOnEnemies(int myArena)
	{
		for (int id : ARENA_ZONES)
		{
			if (id == myArena)
			{
				continue;
			}
			
			final Collection<Creature> chars = ZoneManager.getInstance().getZoneById(id).getCharactersInside();
			for (Creature chr : chars)
			{
				if (chr != null)
				{
					for (Skill sk : DEBUFFS)
					{
						sk.applyEffects(chr, chr);
					}
				}
			}
		}
	}
	
	private void shoutRandomText(Npc npc)
	{
		final int length = _textPassages.length;
		if (_usedTextPassages.size() >= length)
		{
			return;
		}
		
		final int randomPos = getRandom(length);
		final String message = _textPassages[randomPos];
		if (_usedTextPassages.containsKey(message))
		{
			shoutRandomText(npc);
		}
		else
		{
			_usedTextPassages.put(message, new ArrayList<>());
			final int objId = npc.getObjectId();
			npc.broadcastPacket(new NpcSay(objId, ChatType.NPC_SHOUT, npc.getId(), message));
		}
	}
	
	private static boolean isValidPassage(String text)
	{
		for (String st : _textPassages)
		{
			if (st.equalsIgnoreCase(text))
			{
				return true;
			}
		}
		return false;
	}
	
	private static boolean isYetiTarget(int npcId)
	{
		for (int yeti : YETIS)
		{
			if (yeti == npcId)
			{
				return true;
			}
		}
		return false;
	}
	
	private void updateAttacker(int clanId, long count, boolean remove)
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement statement;
			if (remove)
			{
				statement = con.prepareStatement("DELETE FROM rainbowsprings_attacker_list WHERE clanId = ?");
				statement.setInt(1, clanId);
			}
			else
			{
				statement = con.prepareStatement("INSERT INTO rainbowsprings_attacker_list VALUES (?,?)");
				statement.setInt(1, clanId);
				statement.setLong(2, count);
			}
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public void loadAttackers()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("SELECT * FROM rainbowsprings_attacker_list");
			final ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				final int clanId = rset.getInt("clan_id");
				final long count = rset.getLong("decrees_count");
				_warDecreesCount.put(clanId, count);
			}
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("Problem at RainbowSpringsChateau: " + e.getMessage());
		}
	}
	
	protected static void setRegistrationEndString(long time)
	{
		final Calendar c = Calendar.getInstance();
		c.setTime(new Date(time));
		final int year = c.get(Calendar.YEAR);
		final int month = c.get(Calendar.MONTH) + 1;
		final int day = c.get(Calendar.DAY_OF_MONTH);
		final int hour = c.get(Calendar.HOUR_OF_DAY);
		final int mins = c.get(Calendar.MINUTE);
		_registrationEnds = year + "-" + month + "-" + day + " " + hour + (mins < 10 ? ":0" : ":") + mins;
	}
	
	public static void launchSiege()
	{
		_nextSiege.cancel(false);
		ThreadPool.execute(new SiegeStart());
	}
	
	@Override
	public void endSiege()
	{
		if (_siegeEnd != null)
		{
			_siegeEnd.cancel(false);
		}
		ThreadPool.execute(new SiegeEnd(null));
	}
	
	public static void updateAdminDate(long date)
	{
		if (_rainbow == null)
		{
			_rainbow = CHSiegeManager.getInstance().getSiegableHall(RAINBOW_SPRINGS);
		}
		
		_rainbow.setNextSiegeDate(date);
		if (_nextSiege != null)
		{
			_nextSiege.cancel(true);
		}
		setRegistrationEndString(date - 3600000);
		_nextSiege = ThreadPool.schedule(new SetFinalAttackers(), _rainbow.getNextSiegeTime());
	}
	
	public static void main(String[] args)
	{
		new RainbowSpringsChateau();
	}
}
