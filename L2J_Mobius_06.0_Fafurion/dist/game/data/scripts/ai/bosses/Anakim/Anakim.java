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

package ai.bosses.Anakim;

import java.util.Calendar;
import java.util.List;

import org.l2jmobius.Config;
import org.l2jmobius.commons.util.Chronos;
import org.l2jmobius.commons.util.CommonUtil;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.ai.CtrlIntention;
import org.l2jmobius.gameserver.enums.TeleportWhereType;
import org.l2jmobius.gameserver.instancemanager.GrandBossManager;
import org.l2jmobius.gameserver.instancemanager.MapRegionManager;
import org.l2jmobius.gameserver.instancemanager.ZoneManager;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.Party;
import org.l2jmobius.gameserver.model.Spawn;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Attackable;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.instance.GrandBoss;
import org.l2jmobius.gameserver.model.quest.QuestTimer;
import org.l2jmobius.gameserver.model.skill.AbnormalType;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.model.zone.ZoneType;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;

import ai.AbstractNpcAI;

/**
 * Anakim AI<br>
 * @author LasTravel<br>
 * @URL http://boards.lineage2.com/showpost.php?p=3386784&postcount=6<br>
 * @video http://www.youtube.com/watch?v=LecymFTJQzQ
 */
public class Anakim extends AbstractNpcAI
{
	// Status
	private static final int ALIVE = 0;
	private static final int FIGHTING = 1;
	private static final int DEAD = 2;
	// NPCs
	private static final int ANAKIM = 29348;
	private static final int EXIST_CUBIC = 31109;
	private static final int ANAKIM_CUBIC = 31111;
	//@formatter:off
    private static final int[] ANAKIM_MINIONS = {29349, 29350, 29351};
    //@formatter:on	
	private static final int[] ALL_MOBS =
	{
		ANAKIM,
		ANAKIM_MINIONS[0],
		ANAKIM_MINIONS[1],
		ANAKIM_MINIONS[2],
	};
	// Misc
	private static final Location ENTER_ANAKIM_LOC = new Location(184569, -12134, -5499);
	private static final ZoneType BOSS_ZONE = ZoneManager.getInstance().getZoneById(12003);
	// Vars
	private static long _lastAction;
	private static Npc _anakimBoss;
	private static GrandBoss _anakimTemp;
	
	public Anakim()
	{
		addTalkId(EXIST_CUBIC, ANAKIM_CUBIC);
		addStartNpc(EXIST_CUBIC, ANAKIM_CUBIC);
		addFirstTalkId(EXIST_CUBIC, ANAKIM_CUBIC);
		addAttackId(ALL_MOBS);
		addKillId(ALL_MOBS);
		addSkillSeeId(ALL_MOBS);
		
		// Unlock
		final StatSet info = GrandBossManager.getInstance().getStatSet(ANAKIM);
		final int status = GrandBossManager.getInstance().getBossStatus(ANAKIM);
		if (status == DEAD)
		{
			final long time = info.getLong("respawn_time") - Chronos.currentTimeMillis();
			if (time > 0)
			{
				startQuestTimer("unlock_anakim", time, null, null);
			}
			else
			{
				_anakimTemp = (GrandBoss) addSpawn(ANAKIM, -126920, -234182, -15563, 0, false, 0);
				GrandBossManager.getInstance().addBoss(_anakimTemp);
				GrandBossManager.getInstance().setBossStatus(ANAKIM, ALIVE);
			}
		}
		else
		{
			_anakimTemp = (GrandBoss) addSpawn(ANAKIM, -126920, -234182, -15563, 0, false, 0);
			GrandBossManager.getInstance().addBoss(_anakimTemp);
			GrandBossManager.getInstance().setBossStatus(ANAKIM, ALIVE);
		}
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		switch (event)
		{
			case "unlock_anakim":
			{
				_anakimTemp = (GrandBoss) addSpawn(ANAKIM, -126920, -234182, -15563, 0, false, 0);
				GrandBossManager.getInstance().addBoss(_anakimTemp);
				GrandBossManager.getInstance().setBossStatus(ANAKIM, ALIVE);
				break;
			}
			case "check_activity_task":
			{
				if ((_lastAction + 900000) < Chronos.currentTimeMillis())
				{
					GrandBossManager.getInstance().setBossStatus(ANAKIM, ALIVE);
					for (Creature creature : BOSS_ZONE.getCharactersInside())
					{
						if (creature != null)
						{
							if (creature.isNpc())
							{
								creature.deleteMe();
							}
							else if (creature.isPlayer())
							{
								creature.teleToLocation(MapRegionManager.getInstance().getTeleToLocation(creature, TeleportWhereType.TOWN));
							}
						}
					}
					startQuestTimer("end_anakim", 2000, null, null);
				}
				else
				{
					startQuestTimer("check_activity_task", 60000, null, null);
				}
				break;
			}
			case "cancel_timers":
			{
				QuestTimer activityTimer = getQuestTimer("check_activity_task", null, null);
				if (activityTimer != null)
				{
					activityTimer.cancel();
				}
				
				QuestTimer forceEnd = getQuestTimer("end_anakim", null, null);
				if (forceEnd != null)
				{
					forceEnd.cancel();
				}
				break;
			}
			case "end_anakim":
			{
				notifyEvent("cancel_timers", null, null);
				if (_anakimBoss != null)
				{
					_anakimBoss.deleteMe();
				}
				BOSS_ZONE.oustAllPlayers();
				if (GrandBossManager.getInstance().getBossStatus(ANAKIM) != DEAD)
				{
					GrandBossManager.getInstance().setBossStatus(ANAKIM, ALIVE);
				}
				break;
			}
			case "exist":
			{
				player.teleToLocation(TeleportWhereType.TOWN);
				break;
			}
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		if (npc.getId() == ANAKIM_CUBIC)
		{
			final int _anakimStatus = GrandBossManager.getInstance().getBossStatus(ANAKIM);
			if (_anakimStatus > ALIVE)
			{
				return "31101-01.html";
			}
			if (!player.isInParty())
			{
				final NpcHtmlMessage packet = new NpcHtmlMessage(npc.getObjectId());
				packet.setHtml(getHtm(player, "31101-02.html"));
				packet.replace("%min%", Integer.toString(Config.ANAKIM_MIN_PLAYERS));
				player.sendPacket(packet);
				return null;
			}
			final Party party = player.getParty();
			final boolean isInCC = party.isInCommandChannel();
			final List<Player> members = (isInCC) ? party.getCommandChannel().getMembers() : party.getMembers();
			final boolean isPartyLeader = (isInCC) ? party.getCommandChannel().isLeader(player) : party.isLeader(player);
			if (!isPartyLeader)
			{
				return "31101-03.html";
			}
			
			if ((members.size() < Config.ANAKIM_MIN_PLAYERS) || (members.size() > Config.ANAKIM_MAX_PLAYERS))
			{
				final NpcHtmlMessage packet = new NpcHtmlMessage(npc.getObjectId());
				packet.setHtml(getHtm(player, "31101-02.html"));
				packet.replace("%min%", Integer.toString(Config.ANAKIM_MIN_PLAYERS));
				player.sendPacket(packet);
				return null;
			}
			
			for (Player member : members)
			{
				if (member.getLevel() < Config.ANAKIM_MIN_PLAYER_LEVEL)
				{
					final NpcHtmlMessage packet = new NpcHtmlMessage(npc.getObjectId());
					packet.setHtml(getHtm(player, "31101-04.html"));
					packet.replace("%minLevel%", Integer.toString(Config.ANAKIM_MIN_PLAYER_LEVEL));
					player.sendPacket(packet);
					return null;
				}
			}
			
			for (Player member : members)
			{
				if (member.isInsideRadius3D(npc, 1000) && (npc.getId() == ANAKIM_CUBIC))
				{
					member.teleToLocation(ENTER_ANAKIM_LOC, true);
				}
			}
			
			if ((_anakimStatus == ALIVE) && (npc.getId() == ANAKIM_CUBIC))
			{
				GrandBossManager.getInstance().setBossStatus(ANAKIM, FIGHTING);
				// Spawn the rb
				_anakimBoss = addSpawn(ANAKIM, 185080, -12613, -5499, 16550, false, 0);
				GrandBossManager.getInstance().addBoss((GrandBoss) _anakimBoss);
				startQuestTimer("end_anakim", 60 * 60000, null, null); // 1h
			}
		}
		return super.onTalk(npc, player);
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		return npc.getId() + ".html";
	}
	
	@Override
	public String onAttack(Npc npc, Player attacker, int damage, boolean isPet)
	{
		_lastAction = Chronos.currentTimeMillis();
		if (npc.isMinion() || npc.isRaid())// Anakim and minions
		{
			// Anti BUGGERS
			if (!BOSS_ZONE.isInsideZone(attacker)) // Character attacking out of zone
			{
				attacker.doDie(null);
			}
			if (!BOSS_ZONE.isInsideZone(npc)) // Npc moved out of the zone
			{
				Spawn spawn = npc.getSpawn();
				if (spawn != null)
				{
					npc.teleToLocation(spawn.getX(), spawn.getY(), spawn.getZ());
				}
			}
		}
		
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isPet)
	{
		if (npc.getId() == ANAKIM)
		{
			notifyEvent("cancel_timers", null, null);
			addSpawn(EXIST_CUBIC, 185082, -12606, -5499, 6133, false, 900000); // 15min
			
			GrandBossManager.getInstance().setBossStatus(ANAKIM, DEAD);
			final long respawnTime = getRespawnTime();
			final StatSet info = GrandBossManager.getInstance().getStatSet(ANAKIM);
			info.set("respawn_time", Chronos.currentTimeMillis() + respawnTime);
			GrandBossManager.getInstance().setStatSet(ANAKIM, info);
			
			startQuestTimer("unlock_anakim", respawnTime, null, null);
			startQuestTimer("end_anakim", 900000, null, null);
		}
		return super.onKill(npc, killer, isPet);
	}
	
	@Override
	public String onSkillSee(Npc npc, Player caster, Skill skill, WorldObject[] targets, boolean isPet)
	{
		if (CommonUtil.contains(ANAKIM_MINIONS, npc.getId()) && Rnd.nextBoolean())
		{
			if (skill.getAbnormalType() == AbnormalType.HP_RECOVER)
			{
				if (!npc.isCastingNow() && (npc.getTarget() != npc) && (npc.getTarget() != caster) && (npc.getTarget() != _anakimBoss)) // Don't call minions if are healing Anakim
				{
					((Attackable) npc).clearAggroList();
					npc.setTarget(caster);
					((Attackable) npc).addDamageHate(caster, 500, 99999);
					npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, caster);
				}
			}
		}
		return super.onSkillSee(npc, caster, skill, targets, isPet);
	}
	
	private int getRespawnTime()
	{
		return (int) calcReuseFromDays(0, 21, Calendar.TUESDAY, 0, 16, Calendar.SATURDAY);
	}
	
	private long calcReuseFromDays(int day1Minute, int day1Hour, int day1Day, int day2Minute, int day2Hour, int day2Day)
	{
		Calendar now = Calendar.getInstance();
		Calendar day1 = (Calendar) now.clone();
		day1.set(Calendar.MINUTE, day1Minute);
		day1.set(Calendar.HOUR_OF_DAY, day1Hour);
		day1.set(Calendar.DAY_OF_WEEK, day1Day);
		
		Calendar day2 = (Calendar) day1.clone();
		day2.set(Calendar.MINUTE, day2Minute);
		day2.set(Calendar.HOUR_OF_DAY, day2Hour);
		day2.set(Calendar.DAY_OF_WEEK, day2Day);
		
		if (now.after(day1))
		{
			day1.add(Calendar.WEEK_OF_MONTH, 1);
		}
		if (now.after(day2))
		{
			day2.add(Calendar.WEEK_OF_MONTH, 1);
		}
		
		Calendar reenter = day1;
		if (day2.before(day1))
		{
			reenter = day2;
		}
		return reenter.getTimeInMillis() - Chronos.currentTimeMillis();
	}
	
	public static void main(String[] args)
	{
		new Anakim();
	}
}
