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
package ai.bosses;

import static org.l2jmobius.gameserver.ai.CtrlIntention.AI_INTENTION_FOLLOW;
import static org.l2jmobius.gameserver.ai.CtrlIntention.AI_INTENTION_IDLE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.commons.util.Chronos;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.data.SkillTable;
import org.l2jmobius.gameserver.data.sql.AnnouncementsTable;
import org.l2jmobius.gameserver.geoengine.GeoEngine;
import org.l2jmobius.gameserver.instancemanager.GrandBossManager;
import org.l2jmobius.gameserver.model.Skill;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.Summon;
import org.l2jmobius.gameserver.model.actor.instance.GrandBoss;
import org.l2jmobius.gameserver.model.actor.instance.Monster;
import org.l2jmobius.gameserver.model.effects.Effect;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestTimer;
import org.l2jmobius.gameserver.model.zone.type.BossZone;
import org.l2jmobius.gameserver.network.serverpackets.Earthquake;
import org.l2jmobius.gameserver.network.serverpackets.MoveToPawn;
import org.l2jmobius.gameserver.network.serverpackets.PlaySound;
import org.l2jmobius.gameserver.network.serverpackets.SocialAction;
import org.l2jmobius.gameserver.util.Util;

/**
 * Baium AI Note1: if the server gets rebooted while players are still fighting Baium, there is no lock, but players also lose their ability to wake baium up. However, should another person enter the room and wake him up, the players who had stayed inside may join the raid. This can be helpful for
 * players who became victims of a reboot (they only need 1 new player to enter and wake up baium) and is not too exploitable since any player wishing to exploit it would have to suffer 5 days of being parked in an empty room. Note2: Neither version of Baium should be a permanent spawn. This script
 * is fully capable of spawning the statue-version when the lock expires and switching it to the mob version promptly. Additional notes ( source http://aleenaresron.blogspot.com/2006_08_01_archive.html ): * Baium only first respawns five days after his last death. And from those five days he will
 * respawn within 1-8 hours of his last death. So, you have to know his last time of death. * If by some freak chance you are the only one in Baium's chamber and NO ONE comes in [ha, ha] you or someone else will have to wake Baium. There is a good chance that Baium will automatically kill whoever
 * wakes him. There are some people that have been able to wake him and not die, however if you've already gone through the trouble of getting the bloody fabric and camped him out and researched his spawn time, are you willing to take that chance that you'll wake him and not be able to finish your
 * quest? Doubtful. [ this powerful attack vs the player who wakes him up is NOT yet implemented here] * once someone starts attacking Baium no one else can port into the chamber where he is. Unlike with the other raid bosses, you can just show up at any time as long as you are there when they die.
 * Not true with Baium. Once he gets attacked, the port to Baium closes. byebye, see you in 5 days. If nobody attacks baium for 30 minutes, he auto-despawns and unlocks the vortex
 * @author Fulminus version 0.1
 */
public class Baium extends Quest
{
	protected static final Logger LOGGER = Logger.getLogger(Baium.class.getName());
	
	// Baium status.
	private static final byte ASLEEP = 0; // Baium is in the stone version, waiting to be woken up. Entry is unlocked.
	private static final byte AWAKE = 1; // Baium is awake and fighting. Entry is locked.
	private static final byte DEAD = 2; // Baium has been killed and has not yet spawned. Entry is locked.
	private static final int STONE_BAIUM = 29025;
	private static final int ANGELIC_VORTEX = 31862;
	private static final int LIVE_BAIUM = 29020;
	private static final int ARCHANGEL = 29021;
	// @formatter:off
	private static final int[][] ANGEL_LOCATION =
	{
		{114239, 17168, 10080, 63544},
		{115780, 15564, 10080, 13620},
		{114880, 16236, 10080, 5400},
		{115168, 17200, 10080, 0},
		{115792, 16608, 10080, 0},
	};
	// @formatter:on
	// Misc.
	private long _lastAttackVsBaiumTime = 0;
	private final List<Npc> _minions = new CopyOnWriteArrayList<>();
	private BossZone _zone;
	private Creature _target;
	private Skill _skill;
	
	public Baium()
	{
		super(-1, "ai/bosses");
		
		registerMobs(LIVE_BAIUM);
		
		// Quest NPC starter initialization
		addStartNpc(STONE_BAIUM);
		addStartNpc(ANGELIC_VORTEX);
		addTalkId(STONE_BAIUM);
		addTalkId(ANGELIC_VORTEX);
		
		_zone = GrandBossManager.getInstance().getZone(113100, 14500, 10077);
		
		final StatSet info = GrandBossManager.getInstance().getStatSet(LIVE_BAIUM);
		final Integer status = GrandBossManager.getInstance().getBossStatus(LIVE_BAIUM);
		if (status == DEAD)
		{
			// Load the unlock date and time for baium from DB.
			final long temp = (info.getLong("respawn_time") - Chronos.currentTimeMillis());
			if (temp > 0)
			{
				// The unlock time has not yet expired. Mark Baium as currently locked (dead).
				// Setup a timer to fire at the correct time (calculate the time between now and the unlock time, setup a timer to fire after that many msec).
				startQuestTimer("baium_unlock", temp, null, null);
			}
			else
			{
				// The time has already expired while the server was offline. Delete the saved time and immediately spawn the stone-baium. Also the state need not be changed from ASLEEP.
				addSpawn(STONE_BAIUM, 116033, 17447, 10104, 40188, false, 0);
				if (Config.ANNOUNCE_TO_ALL_SPAWN_RB)
				{
					AnnouncementsTable.getInstance().announceToAll("Raid boss Baium Stone spawned in world.");
				}
				GrandBossManager.getInstance().setBossStatus(LIVE_BAIUM, ASLEEP);
			}
		}
		else if (status == AWAKE)
		{
			final int x = info.getInt("loc_x");
			final int y = info.getInt("loc_y");
			final int z = info.getInt("loc_z");
			final int heading = info.getInt("heading");
			final int hp = info.getInt("currentHP");
			final int mp = info.getInt("currentMP");
			final GrandBoss baium = (GrandBoss) addSpawn(LIVE_BAIUM, x, y, z, heading, false, 0);
			if (Config.ANNOUNCE_TO_ALL_SPAWN_RB)
			{
				AnnouncementsTable.getInstance().announceToAll("Raid boss " + baium.getName() + " spawned in world.");
			}
			GrandBossManager.getInstance().addBoss(baium);
			ThreadPool.schedule(() ->
			{
				try
				{
					baium.setCurrentHpMp(hp, mp);
					baium.setInvul(true);
					// _baium.setImobilised(true);
					baium.broadcastPacket(new SocialAction(baium.getObjectId(), 2));
					startQuestTimer("baium_wakeup", 15000, baium, null);
				}
				catch (Exception e)
				{
					LOGGER.warning(e.getMessage());
				}
			}, 100);
		}
		else
		{
			addSpawn(STONE_BAIUM, 116033, 17447, 10104, 40188, false, 0);
			if (Config.ANNOUNCE_TO_ALL_SPAWN_RB)
			{
				AnnouncementsTable.getInstance().announceToAll("Raid boss Baium Stone spawned in world.");
			}
		}
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		if (event.equals("baium_unlock"))
		{
			GrandBossManager.getInstance().setBossStatus(LIVE_BAIUM, ASLEEP);
			addSpawn(STONE_BAIUM, 116033, 17447, 10104, 40188, false, 0);
			if (Config.ANNOUNCE_TO_ALL_SPAWN_RB)
			{
				AnnouncementsTable.getInstance().announceToAll("Raid boss Baium Stone spawned in world.");
			}
		}
		else if (event.equals("skill_range") && (npc != null))
		{
			callSkillAI(npc);
		}
		else if (event.equals("clean_player"))
		{
			_target = getRandomTarget(npc);
		}
		else if (event.equals("baium_wakeup") && (npc != null))
		{
			if (npc.getNpcId() == LIVE_BAIUM)
			{
				npc.broadcastPacket(new SocialAction(npc.getObjectId(), 1));
				npc.broadcastPacket(new Earthquake(npc.getX(), npc.getY(), npc.getZ(), 40, 5));
				
				// Start monitoring baium's inactivity.
				_lastAttackVsBaiumTime = Chronos.currentTimeMillis();
				startQuestTimer("baium_despawn", 60000, npc, null, true);
				if (player != null)
				{
					player.reduceCurrentHp(99999999, player);
				}
				
				npc.setRunning();
				
				startQuestTimer("skill_range", 500, npc, null, true);
				final Npc baium = npc;
				ThreadPool.schedule(() ->
				{
					try
					{
						baium.setInvul(false);
						// baium.setImobilised(false);
						// for (Npc minion : _Minions)
						// minion.setShowSummonAnimation(false);
						baium.getAttackByList().addAll(_zone.getCharactersInside());
					}
					catch (Exception e)
					{
						LOGGER.warning(e.getMessage());
					}
				}, 11100);
				// TODO: the person who woke baium up should be knocked across the room, onto a wall, and lose massive amounts of HP.
				for (int[] element : ANGEL_LOCATION)
				{
					final Monster angel = (Monster) addSpawn(ARCHANGEL, element[0], element[1], element[2], element[3], false, 0);
					angel.setInvul(true);
					_minions.add(angel);
					angel.getAttackByList().addAll(_zone.getCharactersInside());
					angel.isAggressive();
				}
			}
		}
		else if (event.equals("baium_despawn") && (npc != null))
		{
			// Despawn the live baium after 30 minutes of inactivity also check if the players are cheating, having pulled Baium outside his zone...
			if (npc.getNpcId() == LIVE_BAIUM)
			{
				// Just in case the zone reference has been lost (somehow...), restore the reference.
				if (_zone == null)
				{
					_zone = GrandBossManager.getInstance().getZone(113100, 14500, 10077);
				}
				if ((_lastAttackVsBaiumTime + (Config.BAIUM_SLEEP * 1000)) < Chronos.currentTimeMillis())
				{
					npc.deleteMe(); // Despawn the live-baium.
					for (Npc minion : _minions)
					{
						if (minion != null)
						{
							minion.getSpawn().stopRespawn();
							minion.deleteMe();
						}
					}
					_minions.clear();
					addSpawn(STONE_BAIUM, 116033, 17447, 10104, 40188, false, 0); // Spawn stone-baium.
					GrandBossManager.getInstance().setBossStatus(LIVE_BAIUM, ASLEEP); // Mark that Baium is not awake any more.
					_zone.oustAllPlayers();
					cancelQuestTimer("baium_despawn", npc, null);
				}
				else if (((_lastAttackVsBaiumTime + 300000) < Chronos.currentTimeMillis()) && (npc.getCurrentHp() < ((npc.getMaxHp() * 3) / 4.0)))
				{
					npc.setTarget(npc);
					npc.doCast(SkillTable.getInstance().getSkill(4135, 1));
					if (GrandBossManager.getInstance().getBossStatus(LIVE_BAIUM) != AWAKE)
					{
						cancelQuestTimer("baium_despawn", npc, null);
					}
				}
			}
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		final int npcId = npc.getNpcId();
		String htmltext = "";
		if (_zone == null)
		{
			_zone = GrandBossManager.getInstance().getZone(113100, 14500, 10077);
		}
		if (_zone == null)
		{
			return "<html><body>Angelic Vortex:<br>You may not enter while admin disabled this zone</body></html>";
		}
		
		final Integer status = GrandBossManager.getInstance().getBossStatus(LIVE_BAIUM);
		if ((npcId == STONE_BAIUM) && (status == ASLEEP))
		{
			if (Config.ALLOW_DIRECT_TP_TO_BOSS_ROOM || _zone.isPlayerAllowed(player))
			{
				// Once Baium is awaken, no more people may enter until he dies, the server reboots, or 30 minutes pass with no attacks made against Baium.
				GrandBossManager.getInstance().setBossStatus(LIVE_BAIUM, AWAKE);
				npc.deleteMe();
				final GrandBoss baium = (GrandBoss) addSpawn(LIVE_BAIUM, npc);
				GrandBossManager.getInstance().addBoss(baium);
				ThreadPool.schedule(() ->
				{
					try
					{
						baium.setInvul(true);
						baium.setRunning();
						baium.broadcastPacket(new SocialAction(baium.getObjectId(), 2));
						startQuestTimer("baium_wakeup", 15000, baium, player);
						// _baium.setShowSummonAnimation(false);
					}
					catch (Throwable e)
					{
						LOGGER.warning(e.getMessage());
					}
				}, 100L);
			}
			else
			{
				htmltext = "Conditions are not right to wake up Baium.";
			}
		}
		else if (npcId == ANGELIC_VORTEX)
		{
			if (player.isFlying())
			{
				return "<html><body>Angelic Vortex:<br>You may not enter while flying a wyvern.</body></html>";
			}
			
			if ((status == ASLEEP) && (player.getQuestState(getName()).getQuestItemsCount(4295) > 0)) // Bloody fabric.
			{
				player.getQuestState(getName()).takeItems(4295, 1);
				// Allow entry for the player for the next 30 secs (more than enough time for the TP to happen).
				// Note: this just means 30secs to get in, no limits on how long it takes before we get out.
				_zone.allowPlayerEntry(player, 30);
				player.teleToLocation(113100, 14500, 10077);
			}
			else
			{
				npc.showChatWindow(player, 1);
			}
		}
		return htmltext;
	}
	
	@Override
	public String onSpellFinished(Npc npc, Player player, Skill skill)
	{
		if (npc.isInvul())
		{
			npc.getAI().setIntention(AI_INTENTION_IDLE);
			return null;
		}
		else if ((npc.getNpcId() == LIVE_BAIUM) && !npc.isInvul())
		{
			callSkillAI(npc);
		}
		return super.onSpellFinished(npc, player, skill);
	}
	
	@Override
	public String onAttack(Npc npc, Player attacker, int damage, boolean isPet)
	{
		if (!_zone.isInsideZone(attacker))
		{
			attacker.reduceCurrentHp(attacker.getCurrentHp(), attacker, false);
			return super.onAttack(npc, attacker, damage, isPet);
		}
		if (npc.isInvul())
		{
			npc.getAI().setIntention(AI_INTENTION_IDLE);
			return super.onAttack(npc, attacker, damage, isPet);
		}
		else if ((npc.getNpcId() == LIVE_BAIUM) && !npc.isInvul())
		{
			if (attacker.getMountType() == 1)
			{
				int sk4258 = 0;
				for (Effect e : attacker.getAllEffects())
				{
					if (e.getSkill().getId() == 4258)
					{
						sk4258 = 1;
					}
				}
				if (sk4258 == 0)
				{
					npc.setTarget(attacker);
					npc.doCast(SkillTable.getInstance().getSkill(4258, 1));
				}
			}
			// Update a variable with the last action against Baium.
			_lastAttackVsBaiumTime = Chronos.currentTimeMillis();
			callSkillAI(npc);
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isPet)
	{
		npc.broadcastPacket(new PlaySound(1, "BS01_D", npc));
		cancelQuestTimer("baium_despawn", npc, null);
		// Spawn the "Teleportation Cubic" for 15 minutes (to allow players to exit the lair).
		addSpawn(29055, 115203, 16620, 10078, 0, false, 900000); // Should we teleport everyone out if the cubic despawns??
		// Lock baium for 5 days and 1 to 8 hours [i.e. 432,000,000 + 1*3,600,000 + random-less-than(8*3,600,000) millisecs]
		final long respawnTime = (Config.BAIUM_RESP_FIRST + Rnd.get(Config.BAIUM_RESP_SECOND)) * 3600000;
		GrandBossManager.getInstance().setBossStatus(LIVE_BAIUM, DEAD);
		startQuestTimer("baium_unlock", respawnTime, null, null);
		// Also save the respawn time so that the info is maintained past reboots.
		final StatSet info = GrandBossManager.getInstance().getStatSet(LIVE_BAIUM);
		info.set("respawn_time", Chronos.currentTimeMillis() + respawnTime);
		GrandBossManager.getInstance().setStatSet(LIVE_BAIUM, info);
		for (Npc minion : _minions)
		{
			if (minion != null)
			{
				minion.getSpawn().stopRespawn();
				minion.deleteMe();
			}
		}
		_minions.clear();
		
		if (getQuestTimer("skill_range", npc, null) != null)
		{
			getQuestTimer("skill_range", npc, null).cancel();
		}
		
		return super.onKill(npc, killer, isPet);
	}
	
	public Creature getRandomTarget(Npc npc)
	{
		final List<Creature> result = new ArrayList<>();
		final Collection<WorldObject> objs = npc.getKnownList().getKnownObjects().values();
		{
			for (WorldObject obj : objs)
			{
				if ((obj instanceof Creature) && (((((Creature) obj).getZ() < (npc.getZ() - 100)) && (((Creature) obj).getZ() > (npc.getZ() + 100))) || !GeoEngine.getInstance().canSeeTarget(obj, npc)))
				{
					continue;
				}
				if ((obj instanceof Player) && Util.checkIfInRange(9000, npc, obj, true) && !((Creature) obj).isDead())
				{
					result.add((Player) obj);
				}
				if ((obj instanceof Summon) && Util.checkIfInRange(9000, npc, obj, true) && !((Creature) obj).isDead())
				{
					result.add((Summon) obj);
				}
			}
		}
		if (result.isEmpty())
		{
			for (Npc minion : _minions)
			{
				if (minion != null)
				{
					result.add(minion);
				}
			}
		}
		
		if (result.isEmpty())
		{
			return null;
		}
		
		final QuestTimer timer = getQuestTimer("clean_player", npc, null);
		if (timer != null)
		{
			timer.cancel();
		}
		startQuestTimer("clean_player", 20000, npc, null);
		
		return getRandomEntry(result);
	}
	
	public synchronized void callSkillAI(Npc npc)
	{
		if (npc.isInvul() || npc.isCastingNow())
		{
			return;
		}
		
		if ((_target == null) || _target.isDead() || !(_zone.isInsideZone(_target)))
		{
			_target = getRandomTarget(npc);
			if (_target != null)
			{
				_skill = SkillTable.getInstance().getSkill(getRandomSkill(npc), 1);
			}
		}
		
		final Creature target = _target;
		Skill skill = _skill;
		if (skill == null)
		{
			skill = SkillTable.getInstance().getSkill(getRandomSkill(npc), 1);
		}
		if ((target == null) || target.isDead() || !(_zone.isInsideZone(target)))
		{
			// npc.setCastingNow(false);
			return;
		}
		
		if (Util.checkIfInRange(skill.getCastRange(), npc, target, true))
		{
			npc.getAI().setIntention(AI_INTENTION_IDLE);
			npc.setTarget(target);
			// npc.setCastingNow(true);
			if (getDist(skill.getCastRange()) > 0)
			{
				npc.broadcastPacket(new MoveToPawn(npc, target, getDist(skill.getCastRange())));
			}
			try
			{
				wait(1000);
				npc.stopMove(null);
				npc.doCast(skill);
			}
			catch (Exception e)
			{
				LOGGER.warning(e.getMessage());
			}
		}
		else
		{
			npc.getAI().setIntention(AI_INTENTION_FOLLOW, target, null);
			// npc.setCastingNow(false);
		}
	}
	
	public int getRandomSkill(Npc npc)
	{
		int skill;
		if (npc.getCurrentHp() > ((npc.getMaxHp() * 3) / 4.0))
		{
			if (Rnd.get(100) < 10)
			{
				skill = 4128;
			}
			else if (Rnd.get(100) < 10)
			{
				skill = 4129;
			}
			else
			{
				skill = 4127;
			}
		}
		else if (npc.getCurrentHp() > ((npc.getMaxHp() * 2) / 4.0))
		{
			if (Rnd.get(100) < 10)
			{
				skill = 4131;
			}
			else if (Rnd.get(100) < 10)
			{
				skill = 4128;
			}
			else if (Rnd.get(100) < 10)
			{
				skill = 4129;
			}
			else
			{
				skill = 4127;
			}
		}
		else if (npc.getCurrentHp() > ((npc.getMaxHp() * 1) / 4.0))
		{
			if (Rnd.get(100) < 10)
			{
				skill = 4130;
			}
			else if (Rnd.get(100) < 10)
			{
				skill = 4131;
			}
			else if (Rnd.get(100) < 10)
			{
				skill = 4128;
			}
			else if (Rnd.get(100) < 10)
			{
				skill = 4129;
			}
			else
			{
				skill = 4127;
			}
		}
		else if (Rnd.get(100) < 10)
		{
			skill = 4130;
		}
		else if (Rnd.get(100) < 10)
		{
			skill = 4131;
		}
		else if (Rnd.get(100) < 10)
		{
			skill = 4128;
		}
		else if (Rnd.get(100) < 10)
		{
			skill = 4129;
		}
		else
		{
			skill = 4127;
		}
		return skill;
	}
	
	@Override
	public String onSkillUse(Npc npc, Player caster, Skill skill)
	{
		if (npc.isInvul())
		{
			npc.getAI().setIntention(AI_INTENTION_IDLE);
			return null;
		}
		npc.setTarget(caster);
		return super.onSkillUse(npc, caster, skill);
	}
	
	public int getDist(int range)
	{
		int dist = 0;
		switch (range)
		{
			case -1:
			{
				break;
			}
			case 100:
			{
				dist = 85;
				break;
			}
			default:
			{
				dist = range - 85;
				break;
			}
		}
		return dist;
	}
	
	public static void main(String[] args)
	{
		new Baium();
	}
}