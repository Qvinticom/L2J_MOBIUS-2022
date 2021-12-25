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
package ai.bosses.Balok;

import java.util.ArrayList;
import java.util.List;

import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.ai.CtrlIntention;
import org.l2jmobius.gameserver.enums.Movie;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Attackable;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.SkillHolder;
import org.l2jmobius.gameserver.model.instancezone.Instance;
import org.l2jmobius.gameserver.model.skill.BuffInfo;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

import instances.AbstractInstance;

/**
 * Balok Warzone instance zone.
 * @author LasTravel, Gigi
 * @Video https://www.youtube.com/watch?v=w_-SNNPoulo&t=204s
 */
public class BalokWarzone extends AbstractInstance
{
	// NPCs
	private static final int BALOK = 29218;
	private static final int MINION = 23123;
	private static final int HELL_DISCIPLE = 29219;
	private static final int ENTRANCE_PORTAL = 33523;
	private static final int INVISIBLE_NPC_1 = 29106;
	private static final int HELLS_GATE = 19040;
	// Item
	private static final int PRISON_KEY = 10015;
	// Skills
	private static final SkillHolder DARKNESS_DRAIN = new SkillHolder(14367, 1);
	private static final SkillHolder REAR_DESTROY = new SkillHolder(14576, 1);
	private static final SkillHolder EARTH_DEMOLITION = new SkillHolder(14246, 1);
	private static final SkillHolder IMPRISION = new SkillHolder(5226, 1);
	private static final SkillHolder INVINCIBILITY_ACTIVATION = new SkillHolder(14190, 1);
	// Misc
	private static final int TEMPLATE_ID = 167;
	//@formatter:off
	private static final int[][] MINION_SPAWN = 
	{
		{154592, 141488, -12738, 26941},
        {154759, 142073, -12738, 32333},
        {154158, 143112, -12738, 43737},
        {152963, 143102, -12738, 53988},
        {152360, 142067, -12740, 0},
        {152530, 141457, -12740, 7246},
        {153571, 140878, -12738, 16756},
        {154174, 141057, -12738, 22165}
	};
	private static final int[][] PRISONS_SPAWN = 
	{
		{154428, 140551, -12712},
        {155061, 141204, -12704},
        {155268, 142097, -12712},
        {154438, 143581, -12712},
        {152695, 143560, -12704},
        {151819, 142063, -12712},
        {152055, 141231, -12712},
        {153608, 140371, -12712}
	};
	//@formatter:on	
	private final List<Npc> _minionList = new ArrayList<>();
	private Npc _currentMinion;
	private Npc _balok;
	
	public BalokWarzone()
	{
		super(TEMPLATE_ID);
		addStartNpc(ENTRANCE_PORTAL);
		addTalkId(ENTRANCE_PORTAL);
		addInstanceCreatedId(TEMPLATE_ID);
		addAttackId(BALOK);
		addSkillSeeId(BALOK);
		addKillId(BALOK, MINION);
		addSpellFinishedId(BALOK);
		addCreatureSeeId(INVISIBLE_NPC_1);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		if (event.equals("enterInstance"))
		{
			enterInstance(player, npc, TEMPLATE_ID);
			if (hasQuestItems(player, PRISON_KEY))
			{
				takeItems(player, PRISON_KEY, -1);
			}
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public void onTimerEvent(String event, StatSet params, Npc npc, Player player)
	{
		final Instance world = npc.getInstanceWorld();
		if (isInInstance(world))
		{
			switch (event)
			{
				case "stage_1_start":
				{
					playMovie(world.getPlayers(), Movie.SI_BARLOG_STORY);
					getTimers().addTimer("stage_1_balok_intro", 72500, npc, null);
					break;
				}
				case "stage_1_balok_intro":
				{
					playMovie(world.getPlayers(), Movie.SI_BARLOG_OPENING);
					getTimers().addTimer("stage_1_spawn_balok", 21300, npc, null);
					break;
				}
				case "stage_1_spawn_balok":
				{
					_balok = addSpawn(BALOK, 153573, 142071, -12738, 16565, false, 0, false, world.getId());
					world.setStatus(1);
					break;
				}
				case "stage_last_send_minions":
				{
					final Npc minion = _minionList.get(Rnd.get(_minionList.size()));
					if (minion != null)
					{
						minion.setRunning();
						((Attackable) minion).setCanReturnToSpawnPoint(false);
						_currentMinion = minion;
						getTimers().addTimer("stage_last_minion_walk", 2000, minion, player);
					}
					break;
				}
				case "stage_last_minion_walk":
				{
					if (npc.getId() == MINION)
					{
						if (npc.calculateDistanceSq2D(_balok) > 113125)
						{
							npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(_balok.getX() + 100, _balok.getY() + 50, _balok.getZ(), _balok.getHeading()));
							getTimers().addTimer("stage_last_minion_walk", 2000, npc, player);
						}
						else
						{
							npc.stopSkillEffects(INVINCIBILITY_ACTIVATION.getSkill());
							_balok.setTarget(npc);
							_balok.doCast(DARKNESS_DRAIN.getSkill());
						}
					}
					break;
				}
				case "stage_spawn_apostols":
				{
					for (int i = 0; i < 4; i++)
					{
						final Npc disciple = addSpawn(HELL_DISCIPLE, npc.getX(), npc.getY(), npc.getZ(), 0, true, 600000, false, world.getId());
						addAttackPlayerDesire(disciple, player);
					}
					break;
				}
				case "imprission_minions":
				{
					final int[] randomJail = PRISONS_SPAWN[Rnd.get(PRISONS_SPAWN.length)]; // Random jail
					player.teleToLocation(randomJail[0], randomJail[1], randomJail[2]);
					world.broadcastPacket(new ExShowScreenMessage("$s1, locked away in the prison.".replace("$s1", player.getName()), 5000));
					break;
				}
			}
		}
	}
	
	@Override
	public String onAttack(Npc npc, Player attacker, int damage, boolean isSummon)
	{
		final Instance world = npc.getInstanceWorld();
		if (isInInstance(world) && (npc.getId() == BALOK))
		{
			if ((npc.getCurrentHp() < (npc.getMaxHp() * 0.85)) && (world.getStatus() == 1))
			{
				for (int[] a : MINION_SPAWN)
				{
					final Npc minion = addSpawn(MINION, a[0], a[1], a[2], a[3], false, 0, false, world.getId());
					_minionList.add(minion);
					INVINCIBILITY_ACTIVATION.getSkill().applyEffects(minion, minion);
					world.setStatus(2);
				}
			}
			if ((npc.getCurrentHp() < (npc.getMaxHp() * 0.50)) && (world.getStatus() == 2))
			{
				if (npc.isScriptValue(0))
				{
					INVINCIBILITY_ACTIVATION.getSkill().applyEffects(npc, npc);
					npc.setScriptValue(1);
				}
				World.getInstance().forEachVisibleObjectInRange(npc, Player.class, 300, instPlayer ->
				{
					if ((instPlayer == null) || (Rnd.get(100) > 2))
					{
						return;
					}
					npc.setTarget(instPlayer);
					npc.doCast(IMPRISION.getSkill());
					getTimers().addTimer("imprission_minions", 4000, npc, instPlayer);
				});
				getTimers().addTimer("stage_last_send_minions", 2000, npc, null);
			}
			if ((npc.getCurrentHp() < (npc.getMaxHp() * 0.25)) && npc.isScriptValue(1))
			{
				npc.setScriptValue(2);
				npc.doCast(EARTH_DEMOLITION.getSkill());
				addSpawn(HELLS_GATE, npc.getX() + 100, npc.getY() + 50, npc.getZ(), npc.getHeading(), false, 0, false, world.getId());
				getTimers().addTimer("stage_spawn_apostols", 2000, npc, attacker);
			}
		}
		return super.onAttack(npc, attacker, damage, isSummon);
	}
	
	@Override
	public String onSpellFinished(Npc npc, Player player, Skill skill)
	{
		final Instance world = npc.getInstanceWorld();
		if (isInInstance(world) && (world != null) && (skill.getId() == DARKNESS_DRAIN.getSkillId()) && !_currentMinion.isDead())
		{
			_balok.setCurrentHp(_balok.getCurrentHp() + _currentMinion.getMaxHp());
		}
		return super.onSpellFinished(npc, player, skill);
	}
	
	@Override
	public String onKill(Npc npc, Player player, boolean isPet)
	{
		final Instance world = npc.getInstanceWorld();
		if (npc == _balok)
		{
			if (world.getAliveNpcCount(BALOK) == 0)
			{
				world.getAliveNpcs(MINION, HELL_DISCIPLE, HELLS_GATE).forEach(guard -> guard.doDie(null));
				world.removeNpcs();
				world.finishInstance();
				world.broadcastPacket(new SystemMessage(SystemMessageId.THIS_DUNGEON_WILL_EXPIRE_IN_S1_MINUTE_S_YOU_WILL_BE_FORCED_OUT_OF_THE_DUNGEON_WHEN_THE_TIME_EXPIRES).addInt((int) 5.0D));
			}
			else
			{
				world.setReenterTime();
			}
		}
		else if (npc == _currentMinion)
		{
			synchronized (_minionList)
			{
				if (_minionList.contains(npc))
				{
					_minionList.remove(npc);
					
					if (!_minionList.isEmpty())
					{
						startQuestTimer("stage_last_send_minions", 2000, npc, null);
					}
					else
					{
						_balok.stopSkillEffects(INVINCIBILITY_ACTIVATION.getSkill());
					}
				}
			}
		}
		return super.onKill(npc, player, isPet);
	}
	
	@Override
	public String onCreatureSee(Npc npc, Creature creature)
	{
		final Instance world = npc.getInstanceWorld();
		if (isInInstance(world) && creature.isPlayer() && npc.isScriptValue(0))
		{
			npc.setScriptValue(1);
			getTimers().addTimer("stage_1_start", 60000, npc, null);
		}
		return super.onCreatureSee(npc, creature);
	}
	
	@Override
	public String onSkillSee(Npc npc, Player caster, Skill skill, WorldObject[] targets, boolean isSummon)
	{
		if (!npc.isDead() && caster.isBehind(npc))
		{
			final BuffInfo info = npc.getEffectList().getBuffInfoBySkillId(INVINCIBILITY_ACTIVATION.getSkillId());
			if ((info != null) && (getRandom(100) < 40))
			{
				npc.stopSkillEffects(INVINCIBILITY_ACTIVATION.getSkill());
			}
			npc.setTarget(caster);
			npc.doCast(REAR_DESTROY.getSkill());
		}
		return super.onSkillSee(npc, caster, skill, targets, isSummon);
	}
	
	public static void main(String[] args)
	{
		new BalokWarzone();
	}
}