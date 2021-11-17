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
package instances.CeremonyOfChaos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.commons.util.Chronos;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.enums.CategoryType;
import org.l2jmobius.gameserver.enums.CeremonyOfChaosResult;
import org.l2jmobius.gameserver.instancemanager.GlobalVariablesManager;
import org.l2jmobius.gameserver.instancemanager.InstanceManager;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.Party;
import org.l2jmobius.gameserver.model.Party.MessageType;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.Summon;
import org.l2jmobius.gameserver.model.actor.appearance.PlayerAppearance;
import org.l2jmobius.gameserver.model.clan.Clan;
import org.l2jmobius.gameserver.model.events.EventDispatcher;
import org.l2jmobius.gameserver.model.events.EventType;
import org.l2jmobius.gameserver.model.events.annotations.RegisterEvent;
import org.l2jmobius.gameserver.model.events.impl.ceremonyofchaos.OnCeremonyOfChaosMatchResult;
import org.l2jmobius.gameserver.model.events.impl.creature.OnCreatureDeath;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerLogout;
import org.l2jmobius.gameserver.model.events.listeners.AbstractEventListener;
import org.l2jmobius.gameserver.model.events.listeners.ConsumerEventListener;
import org.l2jmobius.gameserver.model.holders.ItemHolder;
import org.l2jmobius.gameserver.model.holders.SkillHolder;
import org.l2jmobius.gameserver.model.instancezone.Instance;
import org.l2jmobius.gameserver.model.olympiad.OlympiadManager;
import org.l2jmobius.gameserver.model.skills.Skill;
import org.l2jmobius.gameserver.model.variables.PlayerVariables;
import org.l2jmobius.gameserver.model.zone.ZoneId;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.DeleteObject;
import org.l2jmobius.gameserver.network.serverpackets.ExUserInfoAbnormalVisualEffect;
import org.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jmobius.gameserver.network.serverpackets.SkillCoolTime;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.network.serverpackets.appearance.ExCuriousHouseMemberUpdate;
import org.l2jmobius.gameserver.network.serverpackets.ceremonyofchaos.ExCuriousHouseEnter;
import org.l2jmobius.gameserver.network.serverpackets.ceremonyofchaos.ExCuriousHouseLeave;
import org.l2jmobius.gameserver.network.serverpackets.ceremonyofchaos.ExCuriousHouseMemberList;
import org.l2jmobius.gameserver.network.serverpackets.ceremonyofchaos.ExCuriousHouseObserveMode;
import org.l2jmobius.gameserver.network.serverpackets.ceremonyofchaos.ExCuriousHouseRemainTime;
import org.l2jmobius.gameserver.network.serverpackets.ceremonyofchaos.ExCuriousHouseResult;
import org.l2jmobius.gameserver.network.serverpackets.ceremonyofchaos.ExCuriousHouseState;

import ai.AbstractNpcAI;

/**
 * @author Sdw, Mobius
 */
public class CeremonyOfChaos extends AbstractNpcAI
{
	// Items
	private static final ItemHolder[] INITIAL_ITEMS =
	{
		new ItemHolder(35991, 1), // Ceremony of Chaos - Attack
		new ItemHolder(35992, 1), // Ceremony of Chaos - Magic
		new ItemHolder(35993, 1), // Ceremony of Chaos - Defense
	};
	// Skills
	private static final SkillHolder[] INITIAL_BUFFS =
	{
		new SkillHolder(7115, 1) // Energy of Chaos
	};
	private static final SkillHolder[] END_BUFFS =
	{
		new SkillHolder(9540, 1), // Mysterious Herb of Power
		new SkillHolder(9541, 1), // Mysterious Herb of Magic
		new SkillHolder(19102, 1) // Chaos Sympathy
	};
	// Templates
	private static final int[] TEMPLATES =
	{
		224,
		225,
		226,
		227
	};
	// Misc
	private static final Set<Player> REGISTERED_PLAYERS = ConcurrentHashMap.newKeySet();
	private static final Set<Player> PARTICIPANT_PLAYERS = ConcurrentHashMap.newKeySet();
	private static final String COC_DEFEATED_VAR = "COC_DEFEATED";
	private static final int MIN_PLAYERS = 2;
	private static final int MAX_PLAYERS = 18;
	private static final int MAX_ARENAS = 5;
	private boolean _registrationOpen = false;
	
	private CeremonyOfChaos()
	{
		final long currentTime = Chronos.currentTimeMillis();
		
		// Schedule event period end, 1st of next month 00:01.
		final Calendar periodEnd = Calendar.getInstance();
		periodEnd.add(Calendar.MONTH, 1);
		periodEnd.set(Calendar.DAY_OF_MONTH, 1);
		periodEnd.set(Calendar.HOUR_OF_DAY, 0);
		periodEnd.set(Calendar.MINUTE, 1);
		periodEnd.set(Calendar.SECOND, 0);
		if (periodEnd.getTimeInMillis() < currentTime)
		{
			periodEnd.add(Calendar.DAY_OF_YEAR, 1);
			while (periodEnd.get(Calendar.DAY_OF_MONTH) != 1)
			{
				periodEnd.add(Calendar.DAY_OF_YEAR, 1);
			}
		}
		ThreadPool.scheduleAtFixedRate(this::endMonth, periodEnd.getTimeInMillis() - currentTime, 2629800000L); // 2629800000 = 1 month
		
		// Daily task to start event at 18:00.
		final Calendar startTime = Calendar.getInstance();
		startTime.set(Calendar.HOUR_OF_DAY, 18);
		startTime.set(Calendar.MINUTE, 0);
		startTime.set(Calendar.SECOND, 0);
		if (startTime.getTimeInMillis() < currentTime)
		{
			startTime.add(Calendar.DAY_OF_YEAR, 1);
		}
		ThreadPool.scheduleAtFixedRate(this::startEvent, startTime.getTimeInMillis() - currentTime, 86400000); // 86400000 = 1 day
	}
	
	private void endMonth()
	{
		// Set monthly true hero.
		GlobalVariablesManager.getInstance().set(GlobalVariablesManager.COC_TRUE_HERO, GlobalVariablesManager.getInstance().getInt(GlobalVariablesManager.COC_TOP_MEMBER, 0));
		GlobalVariablesManager.getInstance().set(GlobalVariablesManager.COC_TRUE_HERO_REWARDED, false);
		// Reset monthly winner.
		GlobalVariablesManager.getInstance().set(GlobalVariablesManager.COC_TOP_MARKS, 0);
		GlobalVariablesManager.getInstance().set(GlobalVariablesManager.COC_TOP_MEMBER, 0);
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("DELETE FROM character_variables WHERE var=?"))
		{
			ps.setString(1, PlayerVariables.CEREMONY_OF_CHAOS_MARKS);
			ps.execute();
		}
		catch (Exception e)
		{
			LOGGER.severe(getClass().getSimpleName() + ": Could not reset Ceremony Of Chaos victories: " + e);
		}
		
		// Update data for online players.
		for (Player player : World.getInstance().getPlayers())
		{
			player.getVariables().remove(PlayerVariables.CEREMONY_OF_CHAOS_MARKS);
			player.getVariables().storeMe();
		}
		
		LOGGER.info(getClass().getSimpleName() + ": Ceremony of Chaos variables have been reset.");
		LOGGER.info(getClass().getSimpleName() + ": Ceremony of Chaos period has ended!");
	}
	
	private void startEvent()
	{
		// Enabled Tuesday, Wednesday, Thursday
		final Calendar calendar = Calendar.getInstance();
		final int day = calendar.get(Calendar.DAY_OF_WEEK);
		if ((day != Calendar.TUESDAY) && (day != Calendar.WEDNESDAY) && (day != Calendar.THURSDAY))
		{
			return;
		}
		
		// Event starts at 18, should stop if past 00:00.
		if (calendar.get(Calendar.HOUR_OF_DAY) < 18)
		{
			return;
		}
		
		registrationStart();
	}
	
	private void registrationStart()
	{
		_registrationOpen = true;
		
		// Message all players.
		for (Player player : World.getInstance().getPlayers())
		{
			if (player.isOnline())
			{
				player.sendPacket(SystemMessageId.REGISTRATION_FOR_THE_CEREMONY_OF_CHAOS_HAS_BEGUN);
				if (canRegister(player, false))
				{
					player.sendPacket(ExCuriousHouseState.REGISTRATION_PACKET);
				}
			}
		}
		
		ThreadPool.schedule(this::registrationEnd, 300000); // 300000 = 5 minutes
	}
	
	private void registrationEnd()
	{
		if (REGISTERED_PLAYERS.size() >= MIN_PLAYERS)
		{
			_registrationOpen = false;
			
			for (Player player : World.getInstance().getPlayers())
			{
				if (player.isOnline())
				{
					player.sendPacket(SystemMessageId.REGISTRATION_FOR_THE_CEREMONY_OF_CHAOS_HAS_ENDED);
					if (!REGISTERED_PLAYERS.contains(player))
					{
						player.sendPacket(ExCuriousHouseState.IDLE_PACKET);
					}
				}
			}
			
			final StatSet params = new StatSet();
			params.set("time", 60);
			getTimers().addTimer("count_down", params, 60000, null, null);
			
			ThreadPool.schedule(this::prepareForFight, 60000); // 60000 = 1 minute
		}
		else // Try again in 10 minutes.
		{
			ThreadPool.schedule(this::startEvent, 600000); // 600000 = 10 minutes
		}
	}
	
	private void prepareForFight()
	{
		PARTICIPANT_PLAYERS.clear();
		final List<Player> players = REGISTERED_PLAYERS.stream().sorted(Comparator.comparingInt(Player::getLevel)).collect(Collectors.toList());
		for (Player player : players)
		{
			if (player.isOnline() && canRegister(player, true))
			{
				PARTICIPANT_PLAYERS.add(player);
			}
			else
			{
				player.sendPacket(ExCuriousHouseState.IDLE_PACKET);
			}
		}
		
		// Clear previously registered players.
		REGISTERED_PLAYERS.clear();
		
		// Prepare all event players for start.
		preparePlayers();
	}
	
	public void preparePlayers()
	{
		final ExCuriousHouseMemberList membersList = new ExCuriousHouseMemberList(0, MAX_PLAYERS, PARTICIPANT_PLAYERS);
		final NpcHtmlMessage msg = new NpcHtmlMessage(0);
		int index = 0;
		int position = 1;
		final int templateId = getRandomEntry(TEMPLATES);
		for (Player player : PARTICIPANT_PLAYERS)
		{
			if (player.inObserverMode())
			{
				player.leaveObserverMode();
			}
			
			if (player.isInDuel())
			{
				player.setInDuel(0);
			}
			
			// Remember player's last location
			player.setLastLocation();
			
			// Hide player information
			final PlayerAppearance app = player.getAppearance();
			app.setVisibleName("Challenger" + position++);
			app.setVisibleTitle("");
			app.setVisibleClanData(0, 0, 0, 0, 0);
			
			// Load the html
			msg.setFile(player, "data/scripts/instances/CeremonyOfChaos/started.htm");
			
			// Remove buffs
			player.stopAllEffectsExceptThoseThatLastThroughDeath();
			player.getEffectList().stopEffects(info -> info.getSkill().isBlockedInOlympiad(), true, true);
			
			// Player shouldn't be able to move and is hidden
			player.setImmobilized(true);
			player.setInvisible(true);
			
			// Same goes for summon
			player.getServitors().values().forEach(s ->
			{
				s.stopAllEffectsExceptThoseThatLastThroughDeath();
				s.setInvisible(true);
				s.setImmobilized(true);
			});
			
			if (player.isFlyingMounted())
			{
				player.untransform();
			}
			
			// If player is dead, revive it
			if (player.isDead())
			{
				player.doRevive();
			}
			
			// If player is sitting, stand up
			if (player.isSitting())
			{
				player.standUp();
			}
			
			// If player in party, leave it
			final Party party = player.getParty();
			if (party != null)
			{
				party.removePartyMember(player, MessageType.EXPELLED);
			}
			
			// Cancel any started action
			player.abortAttack();
			player.abortCast();
			player.stopMove(null);
			player.setTarget(null);
			
			// Unsummon pet
			final Summon pet = player.getPet();
			if (pet != null)
			{
				pet.unSummon(player);
			}
			
			// Unsummon agathion
			if (player.getAgathionId() > 0)
			{
				player.setAgathionId(0);
			}
			
			// The character HP, MP, and CP are fully recovered.
			player.setCurrentHp(player.getMaxHp());
			player.setCurrentMp(player.getMaxMp());
			player.setCurrentCp(player.getMaxCp());
			
			// Skill reuse timers for all skills that have less than 15 minutes of cooldown time are reset.
			for (Skill skill : player.getAllSkills())
			{
				if (skill.getReuseDelay() <= 900000)
				{
					player.enableSkill(skill);
				}
			}
			
			player.sendSkillList();
			player.sendPacket(new SkillCoolTime(player));
			
			// Apply the Energy of Chaos skill
			for (SkillHolder holder : INITIAL_BUFFS)
			{
				holder.getSkill().activateSkill(player, player);
			}
			
			// Send Enter packet
			player.sendPacket(ExCuriousHouseEnter.STATIC_PACKET);
			
			// Send all members
			player.sendPacket(membersList);
			
			// Send the entrance html
			player.sendPacket(msg);
			
			// Send support items to player
			for (ItemHolder holder : INITIAL_ITEMS)
			{
				player.addItem("CoC", holder, null, true);
			}
			
			// Event flags.
			player.setRegisteredOnEvent(false);
			player.setOnSoloEvent(true);
			player.setOnEvent(true);
			
			// Add death listener.
			addDeathListener(player);
			
			// Variables.
			player.getVariables().set(PlayerVariables.CEREMONY_OF_CHAOS_SCORE, 0);
			player.getVariables().set(COC_DEFEATED_VAR, false);
			
			// Teleport player to the arena
			Instance world = null;
			for (Instance instance : InstanceManager.getInstance().getInstances())
			{
				if (instance.getTemplateId() == templateId)
				{
					world = instance;
					break;
				}
			}
			
			if (world == null)
			{
				world = InstanceManager.getInstance().createInstance(templateId, player);
			}
			
			final List<Location> enterLocations = world.getEnterLocations();
			if (index >= enterLocations.size())
			{
				index = 0;
			}
			player.teleToLocation(enterLocations.get(index++), 0, world);
		}
		
		final StatSet params = new StatSet();
		params.set("time", 60);
		getTimers().addTimer("match_start_countdown", params, 100, null, null);
		getTimers().addTimer("teleport_message1", 10000, null, null);
		getTimers().addTimer("teleport_message2", 14000, null, null);
		getTimers().addTimer("teleport_message3", 18000, null, null);
	}
	
	public void startFight()
	{
		for (Player player : PARTICIPANT_PLAYERS)
		{
			if (player != null)
			{
				player.sendPacket(SystemMessageId.THE_MATCH_HAS_STARTED_FIGHT);
				player.setImmobilized(false);
				player.setInvisible(false);
				player.broadcastInfo();
				player.sendPacket(new ExUserInfoAbnormalVisualEffect(player));
				player.getServitors().values().forEach(s ->
				{
					s.setInvisible(false);
					s.setImmobilized(false);
					s.broadcastInfo();
				});
			}
		}
		getTimers().addRepeatingTimer("update", 1000, null, null);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		switch (event)
		{
			case "RequestCuriousHouseHtml":
			{
				if (!_registrationOpen)
				{
					break;
				}
				
				if (REGISTERED_PLAYERS.contains(player))
				{
					player.sendPacket(SystemMessageId.YOU_ARE_ON_THE_WAITING_LIST_FOR_THE_CEREMONY_OF_CHAOS);
					break;
				}
				
				if (canRegister(player, true))
				{
					final NpcHtmlMessage message = new NpcHtmlMessage(0);
					message.setFile(player, "data/scripts/instances/CeremonyOfChaos/invite.htm");
					player.sendPacket(message);
				}
				break;
			}
			case "RegisterPlayer":
			{
				if (!_registrationOpen)
				{
					break;
				}
				
				if (REGISTERED_PLAYERS.contains(player))
				{
					player.sendPacket(SystemMessageId.YOU_ARE_ON_THE_WAITING_LIST_FOR_THE_CEREMONY_OF_CHAOS);
					break;
				}
				
				if (REGISTERED_PLAYERS.size() >= MAX_PLAYERS)
				{
					player.sendPacket(SystemMessageId.THERE_ARE_TOO_MANY_CHALLENGERS_YOU_CANNOT_PARTICIPATE_NOW);
					break;
				}
				
				addLogoutListener(player);
				REGISTERED_PLAYERS.add(player);
				player.setRegisteredOnEvent(true);
				player.sendPacket(SystemMessageId.YOU_ARE_NOW_ON_THE_WAITING_LIST_YOU_WILL_AUTOMATICALLY_BE_TELEPORTED_WHEN_THE_TOURNAMENT_STARTS_AND_WILL_BE_REMOVED_FROM_THE_WAITING_LIST_IF_YOU_LOG_OUT_IF_YOU_CANCEL_REGISTRATION_WITHIN_THE_LAST_MIN_OF_ENTERING_THE_ARENA_AFTER_SIGNING_UP_30_TIMES_OR_MORE_OR_FORFEIT_AFTER_ENTERING_THE_ARENA_30_TIMES_OR_MORE_DURING_A_CYCLE_YOU_BECOME_INELIGIBLE_FOR_PARTICIPATION_IN_THE_CEREMONY_OF_CHAOS_UNTIL_THE_NEXT_CYCLE_ALL_THE_BUFFS_EXCEPT_THE_VITALITY_BUFF_WILL_BE_REMOVED_ONCE_YOU_ENTER_THE_ARENAS);
				player.sendPacket(SystemMessageId.EXCEPT_THE_VITALITY_BUFF_ALL_BUFFS_INCLUDING_ART_OF_SEDUCTION_WILL_BE_DELETED);
				player.sendPacket(ExCuriousHouseState.PREPARE_PACKET);
				break;
			}
			case "UnregisterPlayer":
			{
				if (REGISTERED_PLAYERS.remove(player))
				{
					removeListeners(player);
					player.setRegisteredOnEvent(false);
					player.sendPacket(SystemMessageId.YOU_HAVE_BEEN_TAKEN_OFF_THE_WAIT_LIST_YOU_MAY_ONLY_ENTER_THE_WAIT_LIST_ON_MON_THURS_EVERY_QUARTER_OF_AN_HR_FOR_5_MIN_BETWEEN_20_00_AND_23_40_IF_YOU_CANCEL_REGISTRATION_OR_CHOOSE_TO_FORFEIT_AFTER_ENTERING_A_MATCH_30_TIMES_OR_MORE_DURING_A_CYCLE_YOU_MUST_WAIT_UNTIL_THE_NEXT_CYCLE_TO_PARTICIPATE_IN_THE_CEREMONY_OF_CHAOS_UPON_ENTERING_THE_ARENA_ALL_BUFFS_EXCLUDING_VITALITY_BUFFS_ARE_REMOVED);
					player.sendPacket(ExCuriousHouseState.IDLE_PACKET);
				}
				break;
			}
			case "RequestQuit":
			{
				if (PARTICIPANT_PLAYERS.remove(player))
				{
					// Mark player as defeated
					player.getVariables().set(COC_DEFEATED_VAR, true);
					
					// Delete target player
					final DeleteObject deleteObject = new DeleteObject(player);
					for (Player member : PARTICIPANT_PLAYERS)
					{
						if (member.getObjectId() != player.getObjectId())
						{
							deleteObject.sendTo(member);
						}
					}
					
					// Make the target observer
					player.setObserving(true);
					
					// Make the target spectator
					player.sendPacket(ExCuriousHouseObserveMode.STATIC_ENABLED);
				}
				break;
			}
			
		}
		return null;
	}
	
	@Override
	public void onTimerEvent(String event, StatSet params, Npc npc, Player player)
	{
		switch (event)
		{
			case "count_down":
			{
				final int time = params.getInt("time", 0);
				final SystemMessage countdown = new SystemMessage(SystemMessageId.YOU_WILL_BE_MOVED_TO_THE_ARENA_IN_S1_SEC);
				countdown.addByte(time);
				for (Player member : REGISTERED_PLAYERS)
				{
					member.sendPacket(countdown);
				}
				
				// Reschedule
				if (time == 60)
				{
					params.set("time", 10);
					getTimers().addTimer(event, params, 50000, null, null);
				}
				else if (time == 10)
				{
					params.set("time", 5);
					getTimers().addTimer(event, params, 5000, null, null);
				}
				else if ((time > 1) && (time <= 5))
				{
					params.set("time", time - 1);
					getTimers().addTimer(event, params, 1000, null, null);
				}
				break;
			}
			case "update":
			{
				int time = -1;
				if (!PARTICIPANT_PLAYERS.isEmpty())
				{
					while (time == -1)
					{
						final Player random = PARTICIPANT_PLAYERS.stream().findAny().get();
						if ((random != null) && random.isInInstance())
						{
							time = (int) (random.getInstanceWorld().getRemainingTime() / 1000);
						}
					}
				}
				
				broadcastPacket(new ExCuriousHouseRemainTime(time));
				for (Player member : PARTICIPANT_PLAYERS)
				{
					broadcastPacket(new ExCuriousHouseMemberUpdate(member));
				}
				
				// Validate winner
				int count = 0;
				for (Player member : PARTICIPANT_PLAYERS)
				{
					if (!member.getVariables().getBoolean(COC_DEFEATED_VAR))
					{
						count++;
					}
				}
				if (count <= 1)
				{
					stopFight();
				}
				break;
			}
			case "teleport_message1":
			{
				broadcastPacket(new SystemMessage(SystemMessageId.PROVE_YOUR_ABILITIES));
				break;
			}
			case "teleport_message2":
			{
				broadcastPacket(new SystemMessage(SystemMessageId.THERE_ARE_NO_ALLIES_HERE_EVERYONE_IS_AN_ENEMY));
				break;
			}
			case "teleport_message3":
			{
				broadcastPacket(new SystemMessage(SystemMessageId.IT_WILL_BE_A_LONELY_BATTLE_BUT_I_WISH_YOU_VICTORY));
				break;
			}
			case "match_start_countdown":
			{
				final int time = params.getInt("time", 0);
				final SystemMessage countdown = new SystemMessage(SystemMessageId.THE_MATCH_WILL_START_IN_S1_SEC);
				countdown.addByte(time);
				broadcastPacket(countdown);
				
				// Reschedule
				if (time == 60)
				{
					params.set("time", 30);
					getTimers().addTimer(event, params, 30000, null, null);
				}
				else if ((time == 30) || (time == 20))
				{
					params.set("time", time - 10);
					getTimers().addTimer(event, params, 10000, null, null);
				}
				else if (time == 10)
				{
					params.set("time", 5);
					getTimers().addTimer(event, params, 5000, null, null);
				}
				else if ((time > 1) && (time <= 5))
				{
					params.set("time", time - 1);
					getTimers().addTimer(event, params, 1000, null, null);
				}
				else if (time == 1)
				{
					startFight();
				}
				break;
			}
			case "match_end_countdown":
			{
				final int time = params.getInt("time", 0);
				final SystemMessage countdown = new SystemMessage(SystemMessageId.IN_S1_SEC_YOU_WILL_BE_MOVED_TO_WHERE_YOU_WERE_BEFORE_PARTICIPATING_IN_THE_CEREMONY_OF_CHAOS);
				countdown.addByte(time);
				broadcastPacket(countdown);
				
				// Reschedule
				if ((time == 30) || (time == 20))
				{
					params.set("time", time - 10);
					getTimers().addTimer(event, params, 10000, null, null);
				}
				else if ((time > 0) && (time <= 10))
				{
					params.set("time", time - 1);
					getTimers().addTimer(event, params, 1000, null, null);
				}
				else if (time == 0)
				{
					teleportPlayersOut();
				}
				break;
			}
		}
	}
	
	public void stopFight()
	{
		final List<Player> winners = getWinners();
		final List<Player> memberList = new ArrayList<>(PARTICIPANT_PLAYERS.size());
		SystemMessage msg = null;
		if (winners.isEmpty() || (winners.size() > 1))
		{
			msg = new SystemMessage(SystemMessageId.THERE_IS_NO_VICTOR_THE_MATCH_ENDS_IN_A_TIE);
		}
		else
		{
			final Player winner = winners.get(0);
			if (winner != null)
			{
				msg = new SystemMessage(SystemMessageId.CONGRATULATIONS_C1_YOU_WIN_THE_MATCH);
				msg.addString(winner.getName());
				
				// Rewards according to https://l2wiki.com/Ceremony_of_Chaos
				final int marksRewarded = Rnd.get(2, 5); // Guessed
				final int boxs = Rnd.get(1, 5);
				winner.addItem("CoC-Winner", 45584, marksRewarded, winner, true); // Mark of battle
				winner.addItem("CoC-Winner", 36333, boxs, winner, true); // Mysterious Box
				// Possible additional rewards
				
				// Improved Life Stone
				if (Rnd.get(10) < 3) // Chance to get reward (30%)
				{
					switch (Rnd.get(4))
					{
						case 0:
						{
							winner.addItem("CoC-Winner", 18570, 1, winner, true); // Improved Life Stone (R95-grade)
							break;
						}
						case 1:
						{
							winner.addItem("CoC-Winner", 18571, 1, winner, true); // Improved Life Stone (R95-grade)
							break;
						}
						case 2:
						{
							winner.addItem("CoC-Winner", 18575, 1, winner, true); // Improved Life Stone (R99-grade)
							break;
						}
						case 3:
						{
							winner.addItem("CoC-Winner", 18576, 1, winner, true); // Improved Life Stone (R99-grade)
							break;
						}
					}
				}
				// Soul Crystal Fragment
				else if (Rnd.get(10) < 3) // Chance to get reward (30%)
				{
					switch (Rnd.get(6))
					{
						case 0:
						{
							winner.addItem("CoC-Winner", 19467, 1, winner, true); // Yellow Soul Crystal Fragment (R99-Grade)
							break;
						}
						case 1:
						{
							winner.addItem("CoC-Winner", 19468, 1, winner, true); // Teal Soul Crystal Fragment (R99-Grade)
							break;
						}
						case 2:
						{
							winner.addItem("CoC-Winner", 19469, 1, winner, true); // Purple Soul Crystal Fragment (R99-Grade)
							break;
						}
						case 3:
						{
							winner.addItem("CoC-Winner", 19511, 1, winner, true); // Yellow Soul Crystal Fragment (R95-Grade)
							break;
						}
						case 4:
						{
							winner.addItem("CoC-Winner", 19512, 1, winner, true); // Teal Soul Crystal Fragment (R95-Grade)
							break;
						}
						case 5:
						{
							winner.addItem("CoC-Winner", 19513, 1, winner, true); // Purple Soul Crystal Fragment (R95-Grade)
							break;
						}
					}
				}
				// Mysterious Belt
				else if (Rnd.get(10) < 1) // Chance to get reward (10%)
				{
					winner.addItem("CoC-Winner", 35565, 1, winner, true); // Mysterious Belt
				}
				
				// Save monthly progress.
				final int totalMarks = winner.getVariables().getInt(PlayerVariables.CEREMONY_OF_CHAOS_MARKS, 0) + marksRewarded;
				winner.getVariables().set(PlayerVariables.CEREMONY_OF_CHAOS_MARKS, totalMarks);
				if (totalMarks > GlobalVariablesManager.getInstance().getInt(GlobalVariablesManager.COC_TOP_MARKS, 0))
				{
					GlobalVariablesManager.getInstance().set(GlobalVariablesManager.COC_TOP_MARKS, totalMarks);
					GlobalVariablesManager.getInstance().set(GlobalVariablesManager.COC_TOP_MEMBER, winner.getObjectId());
				}
			}
		}
		
		int time = -1;
		if (!PARTICIPANT_PLAYERS.isEmpty())
		{
			while (time == -1)
			{
				final Player random = PARTICIPANT_PLAYERS.stream().findAny().get();
				if ((random != null) && random.isInInstance())
				{
					time = (int) (random.getInstanceWorld().getRemainingTime() / 1000);
				}
			}
		}
		
		for (Player player : PARTICIPANT_PLAYERS)
		{
			if (player != null)
			{
				// Send winner message
				if (msg != null)
				{
					player.sendPacket(msg);
				}
				
				// Send result
				player.sendPacket(new ExCuriousHouseResult(CeremonyOfChaosResult.WIN, winners, time));
				memberList.add(player);
			}
		}
		
		getTimers().cancelTimer("update", null, null);
		final StatSet params = new StatSet();
		params.set("time", 30);
		getTimers().addTimer("match_end_countdown", params, 30000, null, null);
		EventDispatcher.getInstance().notifyEvent(new OnCeremonyOfChaosMatchResult(winners, memberList));
	}
	
	private void teleportPlayersOut()
	{
		Instance instance = null;
		for (Player player : PARTICIPANT_PLAYERS)
		{
			if (player == null)
			{
				continue;
			}
			
			if (player.isInInstance())
			{
				instance = player.getInstanceWorld();
			}
			
			// Leaves observer mode
			if (player.inObserverMode())
			{
				player.setObserving(false);
			}
			
			// Revive the player
			player.doRevive();
			
			// Remove Energy of Chaos
			for (SkillHolder holder : INITIAL_BUFFS)
			{
				player.stopSkillEffects(holder.getSkill());
			}
			
			// Apply buffs on players
			for (SkillHolder holder : END_BUFFS)
			{
				holder.getSkill().activateSkill(player, player);
			}
			
			// Remove quit button
			player.sendPacket(ExCuriousHouseLeave.STATIC_PACKET);
			
			// Remove spectator mode
			player.setObserving(false);
			player.sendPacket(ExCuriousHouseObserveMode.STATIC_DISABLED);
			
			// Teleport player back
			final Location lastLocation = player.getLastLocation();
			player.teleToLocation(lastLocation != null ? lastLocation : new Location(82201, 147587, -3473), null);
			
			// Restore player information
			final PlayerAppearance app = player.getAppearance();
			app.setVisibleName(null);
			app.setVisibleTitle(null);
			app.setVisibleClanData(-1, -1, -1, -1, -1);
			
			// Remove player from event
			player.setOnSoloEvent(false);
			player.setOnEvent(false);
			removeListeners(player);
		}
		
		PARTICIPANT_PLAYERS.clear();
		if (instance != null)
		{
			instance.destroy();
		}
		
		ThreadPool.schedule(this::startEvent, 60000); // 60000 = 1 minute
	}
	
	public List<Player> getWinners()
	{
		int topScore = -1;
		for (Player player : PARTICIPANT_PLAYERS)
		{
			final int score = player.getVariables().getInt(PlayerVariables.CEREMONY_OF_CHAOS_SCORE, 0);
			if (score > topScore)
			{
				topScore = score;
			}
		}
		
		final List<Player> winners = new ArrayList<>();
		for (Player player : PARTICIPANT_PLAYERS)
		{
			final int score = player.getVariables().getInt(PlayerVariables.CEREMONY_OF_CHAOS_SCORE, 0);
			if (score == topScore)
			{
				winners.add(player);
			}
		}
		return winners;
	}
	
	private void addLogoutListener(Player player)
	{
		player.addListener(new ConsumerEventListener(player, EventType.ON_PLAYER_LOGOUT, (OnPlayerLogout event) -> onPlayerLogout(event), this));
	}
	
	private void addDeathListener(Player player)
	{
		player.addListener(new ConsumerEventListener(player, EventType.ON_CREATURE_DEATH, (OnCreatureDeath event) -> onPlayerDeath(event), this));
	}
	
	private void removeListeners(Player player)
	{
		for (AbstractEventListener listener : player.getListeners(EventType.ON_PLAYER_LOGOUT))
		{
			if (listener.getOwner() == this)
			{
				listener.unregisterMe();
			}
		}
		for (AbstractEventListener listener : player.getListeners(EventType.ON_CREATURE_DEATH))
		{
			if (listener.getOwner() == this)
			{
				listener.unregisterMe();
			}
		}
	}
	
	@RegisterEvent(EventType.ON_PLAYER_LOGOUT)
	private void onPlayerLogout(OnPlayerLogout event)
	{
		final Player player = event.getPlayer();
		if (player != null)
		{
			REGISTERED_PLAYERS.remove(player);
			if (PARTICIPANT_PLAYERS.contains(player))
			{
				PARTICIPANT_PLAYERS.remove(player);
				if (PARTICIPANT_PLAYERS.size() <= 1)
				{
					stopFight();
				}
			}
			removeListeners(player);
		}
	}
	
	@RegisterEvent(EventType.ON_CREATURE_DEATH)
	public void onPlayerDeath(OnCreatureDeath event)
	{
		if (event.getAttacker().isPlayer() && event.getTarget().isPlayer())
		{
			final Player attackerPlayer = event.getAttacker().getActingPlayer();
			final Player targetPlayer = event.getTarget().getActingPlayer();
			if (PARTICIPANT_PLAYERS.contains(attackerPlayer) && PARTICIPANT_PLAYERS.contains(targetPlayer))
			{
				attackerPlayer.getVariables().increaseInt(PlayerVariables.CEREMONY_OF_CHAOS_SCORE, 1);
				
				// Mark player as defeated
				targetPlayer.getVariables().set(COC_DEFEATED_VAR, true);
				
				// Delete target player
				final DeleteObject deleteObject = new DeleteObject(targetPlayer);
				for (Player member : PARTICIPANT_PLAYERS)
				{
					if (member.getObjectId() != targetPlayer.getObjectId())
					{
						deleteObject.sendTo(member);
					}
				}
				
				// Make the target observer
				targetPlayer.setObserving(true);
				
				// Make the target spectator
				targetPlayer.sendPacket(ExCuriousHouseObserveMode.STATIC_ENABLED);
			}
		}
	}
	
	private boolean canRegister(Player player, boolean sendMessage)
	{
		boolean canRegister = true;
		
		final Clan clan = player.getClan();
		SystemMessageId sm = null;
		if (player.getLevel() < 85)
		{
			sm = SystemMessageId.ONLY_CHARACTERS_LEVEL_85_OR_ABOVE_MAY_PARTICIPATE_IN_THE_TOURNAMENT;
			canRegister = false;
		}
		else if (player.isFlyingMounted())
		{
			sm = SystemMessageId.YOU_CANNOT_PARTICIPATE_IN_THE_CEREMONY_OF_CHAOS_AS_A_FLYING_TRANSFORMED_OBJECT;
			canRegister = false;
		}
		else if (!player.isInCategory(CategoryType.SIXTH_CLASS_GROUP))
		{
			sm = SystemMessageId.ONLY_CHARACTERS_WHO_HAVE_COMPLETED_THE_3RD_CLASS_TRANSFER_MAY_PARTICIPATE;
			canRegister = false;
		}
		else if (!player.isInventoryUnder80(false) || (player.getWeightPenalty() != 0))
		{
			sm = SystemMessageId.UNABLE_TO_PROCESS_THIS_REQUEST_UNTIL_YOUR_INVENTORY_S_WEIGHT_AND_SLOT_COUNT_ARE_LESS_THAN_80_PERCENT_OF_CAPACITY;
			canRegister = false;
		}
		else if ((clan == null) || (clan.getLevel() < 6))
		{
			sm = SystemMessageId.ONLY_CHARACTERS_WHO_ARE_A_PART_OF_A_CLAN_OF_LEVEL_6_OR_ABOVE_MAY_PARTICIPATE;
			canRegister = false;
		}
		else if ((REGISTERED_PLAYERS.size() >= (MAX_ARENAS * MAX_PLAYERS)) && !PARTICIPANT_PLAYERS.contains(player))
		{
			sm = SystemMessageId.THERE_ARE_TOO_MANY_CHALLENGERS_YOU_CANNOT_PARTICIPATE_NOW;
			canRegister = false;
		}
		else if (player.isCursedWeaponEquipped() || (player.getReputation() < 0))
		{
			sm = SystemMessageId.WAITING_LIST_REGISTRATION_IS_NOT_ALLOWED_WHILE_THE_CURSED_SWORD_IS_BEING_USED_OR_THE_STATUS_IS_IN_A_CHAOTIC_STATE;
			canRegister = false;
		}
		else if (player.isInDuel())
		{
			sm = SystemMessageId.YOU_CANNOT_REGISTER_IN_THE_WAITING_LIST_DURING_A_DUEL;
			canRegister = false;
		}
		else if (player.isInOlympiadMode() || OlympiadManager.getInstance().isRegistered(player))
		{
			sm = SystemMessageId.YOU_CANNOT_REGISTER_IN_THE_WAITING_LIST_WHILE_PARTICIPATING_IN_OLYMPIAD;
			canRegister = false;
		}
		else if ((player.isRegisteredOnEvent() && !REGISTERED_PLAYERS.contains(player)) || (player.getBlockCheckerArena() > -1))
		{
			sm = SystemMessageId.YOU_CANNOT_REGISTER_FOR_THE_WAITING_LIST_WHILE_PARTICIPATING_IN_THE_BLOCK_CHECKER_COLISEUM_OLYMPIAD_KRATEI_S_CUBE_CEREMONY_OF_CHAOS;
			canRegister = false;
		}
		else if (player.isInInstance())
		{
			sm = SystemMessageId.YOU_MAY_NOT_REGISTER_WHILE_USING_THE_INSTANT_ZONE;
			canRegister = false;
		}
		else if (player.isInSiege())
		{
			sm = SystemMessageId.YOU_CANNOT_REGISTER_FOR_THE_WAITING_LIST_ON_THE_BATTLEFIELD_CASTLE_SIEGE_FORTRESS_SIEGE;
			canRegister = false;
		}
		else if (player.isInsideZone(ZoneId.SIEGE))
		{
			sm = SystemMessageId.YOU_CANNOT_REGISTER_IN_THE_WAITING_LIST_WHILE_BEING_INSIDE_OF_A_BATTLEGROUND_CASTLE_SIEGE_FORTRESS_SIEGE;
			canRegister = false;
		}
		else if (player.isFlyingMounted())
		{
			sm = SystemMessageId.YOU_CANNOT_PARTICIPATE_IN_THE_CEREMONY_OF_CHAOS_AS_A_FLYING_TRANSFORMED_OBJECT;
			canRegister = false;
		}
		else if (player.isFishing())
		{
			sm = SystemMessageId.YOU_CANNOT_PARTICIPATE_IN_THE_CEREMONY_OF_CHAOS_WHILE_FISHING;
			canRegister = false;
		}
		
		if ((sm != null) && sendMessage)
		{
			player.sendPacket(sm);
		}
		
		return canRegister;
	}
	
	private void broadcastPacket(IClientOutgoingPacket packet)
	{
		for (Player player : PARTICIPANT_PLAYERS)
		{
			packet.sendTo(player);
		}
	}
	
	public static void main(String[] args)
	{
		new CeremonyOfChaos();
	}
}
