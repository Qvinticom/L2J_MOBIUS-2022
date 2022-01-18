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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.l2jmobius.Config;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.commons.util.Chronos;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.ai.CtrlIntention;
import org.l2jmobius.gameserver.data.SkillTable;
import org.l2jmobius.gameserver.instancemanager.GrandBossManager;
import org.l2jmobius.gameserver.model.Skill;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.Summon;
import org.l2jmobius.gameserver.model.actor.instance.GrandBoss;
import org.l2jmobius.gameserver.model.effects.Effect;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestTimer;
import org.l2jmobius.gameserver.model.zone.type.BossZone;
import org.l2jmobius.gameserver.network.serverpackets.PlaySound;
import org.l2jmobius.gameserver.network.serverpackets.SocialAction;
import org.l2jmobius.gameserver.network.serverpackets.SpecialCamera;
import org.l2jmobius.gameserver.util.Util;

/**
 * Valakas AI
 * @author Kerberos
 */
public class Valakas extends Quest
{
	// Valakas status
	private static final byte DORMANT = 0; // Valakas is spawned and no one has entered yet. Entry is unlocked.
	private static final byte WAITING = 1; // Valakas is spawend and someone has entered, triggering a 30 minute window for additional people to enter before he unleashes his attack. Entry is unlocked.
	private static final byte FIGHTING = 2; // Valakas is engaged in battle, annihilating his foes. Entry is locked.
	private static final byte DEAD = 3; // Valakas has been killed. Entry is locked.
	// NPC
	private static final int VALAKAS = 29028;
	// Misc
	private int i_ai0 = 0;
	private int i_ai1 = 0;
	private int i_ai2 = 0;
	private int i_ai3 = 0;
	private int i_ai4 = 0;
	private int i_quest0 = 0;
	private long lastAttackTime = 0; // Time to tracking valakas when was last time attacked.
	private int i_quest2 = 0; // Hate value for 1st player.
	private int i_quest3 = 0; // Hate value for 2nd player.
	private int i_quest4 = 0; // Hate value for 3rd player.
	private Creature c_quest2 = null; // 1st most hated target.
	private Creature c_quest3 = null; // 2nd most hated target.
	private Creature c_quest4 = null; // 3rd most hated target.
	private static BossZone _zone;
	
	public Valakas()
	{
		super(-1, "ai/bosses");
		
		registerMobs(VALAKAS);
		
		i_ai0 = 0;
		i_ai1 = 0;
		i_ai2 = 0;
		i_ai3 = 0;
		i_ai4 = 0;
		i_quest0 = 0;
		lastAttackTime = Chronos.currentTimeMillis();
		_zone = GrandBossManager.getInstance().getZone(212852, -114842, -1632);
		
		final StatSet info = GrandBossManager.getInstance().getStatSet(VALAKAS);
		final Integer status = GrandBossManager.getInstance().getBossStatus(VALAKAS);
		if (status == DEAD)
		{
			// Load the unlock date and time for valakas from DB.
			final long temp = (info.getLong("respawn_time") - Chronos.currentTimeMillis());
			// If valakas is locked until a certain time, mark it so and start the unlock timer the unlock time has not yet expired.
			// Mark valakas as currently locked. Setup a timer to fire at the correct time (calculate the time between now and the unlock time, setup a timer to fire after that many msec).
			if (temp > 0)
			{
				startQuestTimer("valakas_unlock", temp, null, null);
			}
			else
			{
				// The time has already expired while the server was offline.
				// The status needs to be changed to DORMANT.
				GrandBossManager.getInstance().setBossStatus(VALAKAS, DORMANT);
			}
		}
		else if (status == FIGHTING)
		{
			// Respawn to original location.
			final int hp = info.getInt("currentHP");
			final int mp = info.getInt("currentMP");
			final GrandBoss valakas = (GrandBoss) addSpawn(VALAKAS, 213004, -114890, -1595, 0, false, 0);
			GrandBossManager.getInstance().addBoss(valakas);
			ThreadPool.schedule(() ->
			{
				try
				{
					valakas.setCurrentHpMp(hp, mp);
					valakas.setRunning();
				}
				catch (Throwable e)
				{
				}
			}, 100);
			
			startQuestTimer("launch_random_skill", 60000, valakas, null, true);
			// Start repeating timer to check for inactivity.
			startQuestTimer("check_activity_and_do_actions", 60000, valakas, null, true);
		}
		else if (status == WAITING)
		{
			// Start timer to lock entry after 30 minutes and spawn Valakas.
			startQuestTimer("lock_entry_and_spawn_valakas", (Config.VALAKAS_WAIT_TIME * 60000), null, null);
		}
		// If it was dormant, just leave it as it was. The valakas NPC is not spawned yet and his instance is not loaded.
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		if (npc != null)
		{
			switch (event)
			{
				case "check_activity_and_do_actions":
				{
					int level = 0;
					int sk4691 = 0;
					for (Effect e : npc.getAllEffects())
					{
						if (e.getSkill().getId() == 4629)
						{
							sk4691 = 1;
							level = e.getSkill().getLevel();
							break;
						}
					}
					final Integer status = GrandBossManager.getInstance().getBossStatus(VALAKAS);
					if ((status == FIGHTING) && ((Chronos.currentTimeMillis() - lastAttackTime) > (Config.VALAKAS_DESPAWN_TIME * 60000))) // 15 mins by default.
					{
						npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
						
						// delete the actual boss
						final GrandBoss boss = GrandBossManager.getInstance().deleteBoss(VALAKAS);
						boss.decayMe();
						GrandBossManager.getInstance().setBossStatus(VALAKAS, DORMANT);
						// npc.setCurrentHpMp(npc.getMaxHp(), npc.getMaxMp());
						_zone.oustAllPlayers();
						cancelQuestTimer("check_activity_and_do_actions", npc, null);
						i_quest2 = 0;
						i_quest3 = 0;
						i_quest4 = 0;
					}
					else if (npc.getCurrentHp() > ((npc.getMaxHp() * 1) / 4))
					{
						if ((sk4691 == 0) || ((sk4691 == 1) && (level != 4)))
						{
							npc.setTarget(npc);
							npc.doCast(SkillTable.getInstance().getSkill(4691, 4));
						}
					}
					else if (npc.getCurrentHp() > ((npc.getMaxHp() * 2) / 4.0))
					{
						if ((sk4691 == 0) || ((sk4691 == 1) && (level != 3)))
						{
							npc.setTarget(npc);
							npc.doCast(SkillTable.getInstance().getSkill(4691, 3));
						}
					}
					else if (npc.getCurrentHp() > ((npc.getMaxHp() * 3) / 4.0))
					{
						if ((sk4691 == 0) || ((sk4691 == 1) && (level != 2)))
						{
							npc.setTarget(npc);
							npc.doCast(SkillTable.getInstance().getSkill(4691, 2));
						}
					}
					else if ((sk4691 == 0) || ((sk4691 == 1) && (level != 1)))
					{
						npc.setTarget(npc);
						npc.doCast(SkillTable.getInstance().getSkill(4691, 1));
					}
					break;
				}
				case "launch_random_skill":
				{
					if (!npc.isInvul())
					{
						getRandomSkill(npc);
					}
					else
					{
						npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
					}
					break;
				}
				case "1004":
				{
					startQuestTimer("1102", 1500, npc, null);
					npc.broadcastPacket(new SpecialCamera(npc.getObjectId(), 1300, 180, -5, 3000, 15000));
					break;
				}
				case "1102":
				{
					startQuestTimer("1103", 3300, npc, null);
					npc.broadcastPacket(new SpecialCamera(npc.getObjectId(), 500, 180, -8, 600, 15000));
					break;
				}
				case "1103":
				{
					startQuestTimer("1104", 2900, npc, null);
					npc.broadcastPacket(new SpecialCamera(npc.getObjectId(), 800, 180, -8, 2700, 15000));
					break;
				}
				case "1104":
				{
					startQuestTimer("1105", 2700, npc, null);
					npc.broadcastPacket(new SpecialCamera(npc.getObjectId(), 200, 250, 70, 0, 15000));
					break;
				}
				case "1105":
				{
					startQuestTimer("1106", 1, npc, null);
					npc.broadcastPacket(new SpecialCamera(npc.getObjectId(), 1100, 250, 70, 2500, 15000));
					break;
				}
				case "1106":
				{
					startQuestTimer("1107", 3200, npc, null);
					npc.broadcastPacket(new SpecialCamera(npc.getObjectId(), 700, 150, 30, 0, 15000));
					break;
				}
				case "1107":
				{
					startQuestTimer("1108", 1400, npc, null);
					npc.broadcastPacket(new SpecialCamera(npc.getObjectId(), 1200, 150, 20, 2900, 15000));
					break;
				}
				case "1108":
				{
					startQuestTimer("1109", 6700, npc, null);
					npc.broadcastPacket(new SpecialCamera(npc.getObjectId(), 750, 170, 15, 3400, 15000));
					break;
				}
				case "1109":
				{
					startQuestTimer("1110", 5700, npc, null);
					npc.broadcastPacket(new SpecialCamera(npc.getObjectId(), 750, 170, -10, 3400, 15000));
					break;
				}
				case "1110":
				{
					GrandBossManager.getInstance().setBossStatus(VALAKAS, FIGHTING);
					startQuestTimer("check_activity_and_do_actions", 60000, npc, null, true);
					npc.setInvul(false);
					getRandomSkill(npc);
					break;
				}
				case "1111":
				{
					startQuestTimer("1112", 3500, npc, null);
					npc.broadcastPacket(new SpecialCamera(npc.getObjectId(), 1100, 210, -5, 3000, 10000));
					break;
				}
				case "1112":
				{
					startQuestTimer("1113", 4500, npc, null);
					npc.broadcastPacket(new SpecialCamera(npc.getObjectId(), 1300, 200, -8, 3000, 10000));
					break;
				}
				case "1113":
				{
					startQuestTimer("1114", 500, npc, null);
					npc.broadcastPacket(new SpecialCamera(npc.getObjectId(), 1000, 190, 0, 3000, 10000));
					break;
				}
				case "1114":
				{
					startQuestTimer("1115", 4600, npc, null);
					npc.broadcastPacket(new SpecialCamera(npc.getObjectId(), 1700, 120, 0, 2500, 10000));
					break;
				}
				case "1115":
				{
					startQuestTimer("1116", 750, npc, null);
					npc.broadcastPacket(new SpecialCamera(npc.getObjectId(), 1700, 20, 0, 3000, 10000));
					break;
				}
				case "1116":
				{
					startQuestTimer("1117", 2500, npc, null);
					npc.broadcastPacket(new SpecialCamera(npc.getObjectId(), 1700, 10, 0, 3000, 10000));
					break;
				}
				case "1117":
				{
					npc.broadcastPacket(new SpecialCamera(npc.getObjectId(), 1700, 10, 0, 3000, 250));
					addSpawn(31759, 212852, -114842, -1632, 0, false, 900000);
					final int radius = 1500;
					for (int i = 0; i < 20; i++)
					{
						final int x = (int) (radius * Math.cos(i * .331)); // .331~2pi/19
						final int y = (int) (radius * Math.sin(i * .331));
						addSpawn(31759, 212852 + x, -114842 + y, -1632, 0, false, 900000);
					}
					startQuestTimer("remove_players", 900000, null, null);
					cancelQuestTimer("check_activity_and_do_actions", npc, null);
					break;
				}
			}
		}
		else if (event.equals("lock_entry_and_spawn_valakas"))
		{
			final int loc_x = 213004;
			final int loc_y = -114890;
			final int loc_z = -1595;
			final int heading = 0;
			final GrandBoss valakas = (GrandBoss) addSpawn(VALAKAS, loc_x, loc_y, loc_z, heading, false, 0);
			GrandBossManager.getInstance().addBoss(valakas);
			
			lastAttackTime = Chronos.currentTimeMillis();
			ThreadPool.schedule(() ->
			{
				try
				{
					broadcastSpawn(valakas);
				}
				catch (Throwable e)
				{
				}
			}, 1L);
			startQuestTimer("1004", 2000, valakas, null);
		}
		else if (event.equals("valakas_unlock"))
		{
			// GrandBoss valakas = (GrandBoss) addSpawn(VALAKAS, -105200, -253104, -15264, 32768, false, 0);
			// GrandBossManager.getInstance().addBoss(valakas);
			GrandBossManager.getInstance().setBossStatus(VALAKAS, DORMANT);
		}
		else if (event.equals("remove_players"))
		{
			_zone.oustAllPlayers();
		}
		
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onAttack(Npc npc, Player attacker, int damage, boolean isPet)
	{
		if (npc.isInvul())
		{
			return null;
		}
		
		lastAttackTime = Chronos.currentTimeMillis();
		// if (!Config.ALLOW_DIRECT_TP_TO_BOSS_ROOM && GrandBossManager.getInstance().getBossStatus(VALAKAS) != FIGHTING && !npc.getSpawn().isCustomBossInstance()) { attacker.teleToLocation(150037, -57255, -2976); }
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
		if (attacker.getZ() < (npc.getZ() + 200))
		{
			if (i_ai2 == 0)
			{
				i_ai1 = (i_ai1 + damage);
			}
			if (i_quest0 == 0)
			{
				i_ai4 = (i_ai4 + damage);
			}
			if (i_quest0 == 0)
			{
				i_ai3 = (i_ai3 + damage);
			}
			else if (i_ai2 == 0)
			{
				i_ai0 = (i_ai0 + damage);
			}
			if ((i_quest0 == 0) && (((i_ai4 / npc.getMaxHp()) * 100) > 1) && (i_ai3 > (i_ai4 - i_ai3)))
			{
				i_ai3 = 0;
				i_ai4 = 0;
				npc.setTarget(npc);
				npc.doCast(SkillTable.getInstance().getSkill(4687, 1));
				i_quest0 = 1;
			}
		}
		int i1 = 0;
		if (attacker == c_quest2)
		{
			if (((damage * 1000) + 1000) > i_quest2)
			{
				i_quest2 = ((damage * 1000) + Rnd.get(3000));
			}
		}
		else if (attacker == c_quest3)
		{
			if (((damage * 1000) + 1000) > i_quest3)
			{
				i_quest3 = ((damage * 1000) + Rnd.get(3000));
			}
		}
		else if (attacker == c_quest4)
		{
			if (((damage * 1000) + 1000) > i_quest4)
			{
				i_quest4 = ((damage * 1000) + Rnd.get(3000));
			}
		}
		else if (i_quest2 > i_quest3)
		{
			i1 = 3;
		}
		else if (i_quest2 == i_quest3)
		{
			if (Rnd.get(100) < 50)
			{
				i1 = 2;
			}
			else
			{
				i1 = 3;
			}
		}
		else if (i_quest2 < i_quest3)
		{
			i1 = 2;
		}
		if (i1 == 2)
		{
			if (i_quest2 > i_quest4)
			{
				i1 = 4;
			}
			else if (i_quest2 == i_quest4)
			{
				if (Rnd.get(100) < 50)
				{
					i1 = 2;
				}
				else
				{
					i1 = 4;
				}
			}
			else if (i_quest2 < i_quest4)
			{
				i1 = 2;
			}
		}
		else if (i1 == 3)
		{
			if (i_quest3 > i_quest4)
			{
				i1 = 4;
			}
			else if (i_quest3 == i_quest4)
			{
				if (Rnd.get(100) < 50)
				{
					i1 = 3;
				}
				else
				{
					i1 = 4;
				}
			}
			else if (i_quest3 < i_quest4)
			{
				i1 = 3;
			}
		}
		if (i1 == 2)
		{
			i_quest2 = (damage * 1000) + Rnd.get(3000);
			c_quest2 = attacker;
		}
		else if (i1 == 3)
		{
			i_quest3 = (damage * 1000) + Rnd.get(3000);
			c_quest3 = attacker;
		}
		else if (i1 == 4)
		{
			i_quest4 = (damage * 1000) + Rnd.get(3000);
			c_quest4 = attacker;
		}
		
		if (i1 == 2)
		{
			if (i_quest2 > i_quest4)
			{
				i1 = 4;
			}
			else if (i_quest2 == i_quest4)
			{
				if (Rnd.get(100) < 50)
				{
					i1 = 2;
				}
				else
				{
					i1 = 4;
				}
			}
			else if (i_quest2 < i_quest4)
			{
				i1 = 2;
			}
		}
		else if (i1 == 3)
		{
			if (i_quest3 > i_quest4)
			{
				i1 = 4;
			}
			else if (i_quest3 == i_quest4)
			{
				if (Rnd.get(100) < 50)
				{
					i1 = 3;
				}
				else
				{
					i1 = 4;
				}
			}
			else if (i_quest3 < i_quest4)
			{
				i1 = 3;
			}
		}
		if (i1 == 2)
		{
			i_quest2 = (((damage / 150) * 1000) + Rnd.get(3000));
			c_quest2 = attacker;
		}
		else if (i1 == 3)
		{
			i_quest3 = (((damage / 150) * 1000) + Rnd.get(3000));
			c_quest3 = attacker;
		}
		else if (i1 == 4)
		{
			i_quest4 = (((damage / 150) * 1000) + Rnd.get(3000));
			c_quest4 = attacker;
		}
		getRandomSkill(npc);
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isPet)
	{
		npc.broadcastPacket(new SpecialCamera(npc.getObjectId(), 1700, 2000, 130, -1, 0));
		npc.broadcastPacket(new PlaySound(1, "B03_D", npc));
		startQuestTimer("1111", 500, npc, null);
		GrandBossManager.getInstance().setBossStatus(VALAKAS, DEAD);
		final long respawnTime = (Config.VALAKAS_RESP_FIRST + Rnd.get(Config.VALAKAS_RESP_SECOND)) * 3600000;
		startQuestTimer("valakas_unlock", respawnTime, null, null);
		// Also save the respawn time so that the info is maintained past restarts.
		final StatSet info = GrandBossManager.getInstance().getStatSet(VALAKAS);
		info.set("respawn_time", (Chronos.currentTimeMillis() + respawnTime));
		GrandBossManager.getInstance().setStatSet(VALAKAS, info);
		return super.onKill(npc, killer, isPet);
	}
	
	public void getRandomSkill(Npc npc)
	{
		if (npc.isInvul() || npc.isCastingNow())
		{
			return;
		}
		Skill skill = null;
		int i0 = 0;
		int i1 = 0;
		int i2 = 0;
		Creature c2 = null;
		if (c_quest2 == null)
		{
			i_quest2 = 0;
		}
		else if (!Util.checkIfInRange(5000, npc, c_quest2, true) || c_quest2.isDead())
		{
			i_quest2 = 0;
		}
		if (c_quest3 == null)
		{
			i_quest3 = 0;
		}
		else if (!Util.checkIfInRange(5000, npc, c_quest3, true) || c_quest3.isDead())
		{
			i_quest3 = 0;
		}
		if (c_quest4 == null)
		{
			i_quest4 = 0;
		}
		else if (!Util.checkIfInRange(5000, npc, c_quest4, true) || c_quest4.isDead())
		{
			i_quest4 = 0;
		}
		if (i_quest2 > i_quest3)
		{
			i1 = 2;
			i2 = i_quest2;
			c2 = c_quest2;
		}
		else
		{
			i1 = 3;
			i2 = i_quest3;
			c2 = c_quest3;
		}
		if (i_quest4 > i2)
		{
			i1 = 4;
			i2 = i_quest4;
			c2 = c_quest4;
		}
		if (i2 == 0)
		{
			c2 = getRandomTarget(npc);
		}
		if (i2 > 0)
		{
			if (Rnd.get(100) < 70)
			{
				if (i1 == 2)
				{
					i_quest2 = 500;
				}
				else if (i1 == 3)
				{
					i_quest3 = 500;
				}
				else if (i1 == 4)
				{
					i_quest4 = 500;
				}
			}
			if (npc.getCurrentHp() > ((npc.getMaxHp() * 1) / 4))
			{
				i0 = 0;
				i1 = 0;
				if (Util.checkIfInRange(1423, npc, c2, true))
				{
					i0 = 1;
					i1 = 1;
				}
				if (c2.getZ() < (npc.getZ() + 200))
				{
					if (Rnd.get(100) < 20)
					{
						skill = SkillTable.getInstance().getSkill(4690, 1);
					}
					else if (Rnd.get(100) < 15)
					{
						skill = SkillTable.getInstance().getSkill(4689, 1);
					}
					else if ((Rnd.get(100) < 15) && (i0 == 1) && (i_quest0 == 1))
					{
						skill = SkillTable.getInstance().getSkill(4685, 1);
						i_quest0 = 0;
					}
					else if ((Rnd.get(100) < 10) && (i1 == 1))
					{
						skill = SkillTable.getInstance().getSkill(4688, 1);
					}
					else if (Rnd.get(100) < 35)
					{
						skill = SkillTable.getInstance().getSkill(4683, 1);
					}
					else if (Rnd.nextBoolean())
					{
						skill = SkillTable.getInstance().getSkill(4681, 1); // left hand
					}
					else
					{
						skill = SkillTable.getInstance().getSkill(4682, 1); // right hand
					}
				}
				else if (Rnd.get(100) < 20)
				{
					skill = SkillTable.getInstance().getSkill(4690, 1);
				}
				else if (Rnd.get(100) < 15)
				{
					skill = SkillTable.getInstance().getSkill(4689, 1);
				}
				else
				{
					skill = SkillTable.getInstance().getSkill(4684, 1);
				}
			}
			else if (npc.getCurrentHp() > ((npc.getMaxHp() * 2) / 4))
			{
				i0 = 0;
				i1 = 0;
				if (Util.checkIfInRange(1423, npc, c2, true))
				{
					i0 = 1;
					i1 = 1;
				}
				if (c2.getZ() < (npc.getZ() + 200))
				{
					if (Rnd.get(100) < 5)
					{
						skill = SkillTable.getInstance().getSkill(4690, 1);
					}
					else if (Rnd.get(100) < 10)
					{
						skill = SkillTable.getInstance().getSkill(4689, 1);
					}
					else if ((Rnd.get(100) < 10) && (i0 == 1) && (i_quest0 == 1))
					{
						skill = SkillTable.getInstance().getSkill(4685, 1);
						i_quest0 = 0;
					}
					else if ((Rnd.get(100) < 10) && (i1 == 1))
					{
						skill = SkillTable.getInstance().getSkill(4688, 1);
					}
					else if (Rnd.get(100) < 20)
					{
						skill = SkillTable.getInstance().getSkill(4683, 1);
					}
					else if (Rnd.nextBoolean())
					{
						skill = SkillTable.getInstance().getSkill(4681, 1); // left hand
					}
					else
					{
						skill = SkillTable.getInstance().getSkill(4682, 1); // right hand
					}
				}
				else if (Rnd.get(100) < 5)
				{
					skill = SkillTable.getInstance().getSkill(4690, 1);
				}
				else if (Rnd.get(100) < 10)
				{
					skill = SkillTable.getInstance().getSkill(4689, 1);
				}
				else
				{
					skill = SkillTable.getInstance().getSkill(4684, 1);
				}
			}
			else if (npc.getCurrentHp() > ((npc.getMaxHp() * 3) / 4.0))
			{
				i0 = 0;
				i1 = 0;
				if (Util.checkIfInRange(1423, npc, c2, true))
				{
					i0 = 1;
					i1 = 1;
				}
				if (c2.getZ() < (npc.getZ() + 200))
				{
					if (Rnd.get(100) < 0)
					{
						skill = SkillTable.getInstance().getSkill(4690, 1);
					}
					else if (Rnd.get(100) < 5)
					{
						skill = SkillTable.getInstance().getSkill(4689, 1);
					}
					else if ((Rnd.get(100) < 5) && (i0 == 1) && (i_quest0 == 1))
					{
						skill = SkillTable.getInstance().getSkill(4685, 1);
						i_quest0 = 0;
					}
					else if ((Rnd.get(100) < 10) && (i1 == 1))
					{
						skill = SkillTable.getInstance().getSkill(4688, 1);
					}
					else if (Rnd.get(100) < 15)
					{
						skill = SkillTable.getInstance().getSkill(4683, 1);
					}
					else if (Rnd.nextBoolean())
					{
						skill = SkillTable.getInstance().getSkill(4681, 1); // left hand
					}
					else
					{
						skill = SkillTable.getInstance().getSkill(4682, 1); // right hand
					}
				}
				else if (Rnd.get(100) < 0)
				{
					skill = SkillTable.getInstance().getSkill(4690, 1);
				}
				else if (Rnd.get(100) < 5)
				{
					skill = SkillTable.getInstance().getSkill(4689, 1);
				}
				else
				{
					skill = SkillTable.getInstance().getSkill(4684, 1);
				}
			}
			else
			{
				i0 = 0;
				i1 = 0;
				if (Util.checkIfInRange(1423, npc, c2, true))
				{
					i0 = 1;
					i1 = 1;
				}
				if (c2.getZ() < (npc.getZ() + 200))
				{
					if (Rnd.get(100) < 0)
					{
						skill = SkillTable.getInstance().getSkill(4690, 1);
					}
					else if (Rnd.get(100) < 10)
					{
						skill = SkillTable.getInstance().getSkill(4689, 1);
					}
					else if ((Rnd.get(100) < 5) && (i0 == 1) && (i_quest0 == 1))
					{
						skill = SkillTable.getInstance().getSkill(4685, 1);
						i_quest0 = 0;
					}
					else if ((Rnd.get(100) < 10) && (i1 == 1))
					{
						skill = SkillTable.getInstance().getSkill(4688, 1);
					}
					else if (Rnd.get(100) < 15)
					{
						skill = SkillTable.getInstance().getSkill(4683, 1);
					}
					else if (Rnd.nextBoolean())
					{
						skill = SkillTable.getInstance().getSkill(4681, 1); // left hand
					}
					else
					{
						skill = SkillTable.getInstance().getSkill(4682, 1); // right hand
					}
				}
				else if (Rnd.get(100) < 0)
				{
					skill = SkillTable.getInstance().getSkill(4690, 1);
				}
				else if (Rnd.get(100) < 10)
				{
					skill = SkillTable.getInstance().getSkill(4689, 1);
				}
				else
				{
					skill = SkillTable.getInstance().getSkill(4684, 1);
				}
			}
		}
		if (skill != null)
		{
			callSkillAI(npc, c2, skill);
		}
	}
	
	public void callSkillAI(Npc npc, Creature creature, Skill skill)
	{
		final QuestTimer timer = getQuestTimer("launch_random_skill", npc, null);
		if (npc == null)
		{
			if (timer != null)
			{
				timer.cancel();
			}
			return;
		}
		
		if (npc.isInvul())
		{
			return;
		}
		
		Creature currentTarget = creature;
		if ((currentTarget == null) || currentTarget.isDead() || (timer == null))
		{
			currentTarget = getRandomTarget(npc); // just in case if hate AI fail
			if (timer == null)
			{
				startQuestTimer("launch_random_skill", 500, npc, null, true);
				return;
			}
		}
		
		final Creature target = currentTarget;
		if ((target == null) || target.isDead())
		{
			return;
		}
		
		if (Util.checkIfInRange(skill.getCastRange(), npc, target, true))
		{
			timer.cancel();
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			// npc.setCastingNow(true);
			npc.setTarget(target);
			npc.doCast(skill);
		}
		else
		{
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, target, null);
			// npc.setCastingNow(false);
		}
	}
	
	public void broadcastSpawn(Npc npc)
	{
		for (WorldObject obj : npc.getKnownList().getKnownObjects().values())
		{
			if ((obj instanceof Player) && Util.checkIfInRange(10000, npc, obj, true))
			{
				((Creature) obj).sendPacket(new PlaySound(1, "B03_A", npc));
				((Creature) obj).sendPacket(new SocialAction(npc.getObjectId(), 3));
			}
		}
	}
	
	public Creature getRandomTarget(Npc npc)
	{
		final List<Creature> result = new ArrayList<>();
		final Collection<WorldObject> objs = npc.getKnownList().getKnownObjects().values();
		{
			for (WorldObject obj : objs)
			{
				if (((obj instanceof Player) || (obj instanceof Summon)) && Util.checkIfInRange(5000, npc, obj, true) && !((Creature) obj).isDead() && (obj instanceof Player) && !((Player) obj).isGM())
				{
					result.add((Creature) obj);
				}
			}
		}
		if (!result.isEmpty())
		{
			return result.get(Rnd.get(result.size()));
		}
		return null;
	}
	
	@Override
	public String onSpellFinished(Npc npc, Player player, Skill skill)
	{
		if (npc.isInvul())
		{
			return null;
		}
		else if ((npc.getNpcId() == VALAKAS) && !npc.isInvul())
		{
			getRandomSkill(npc);
		}
		return super.onSpellFinished(npc, player, skill);
	}
	
	@Override
	public String onAggroRangeEnter(Npc npc, Player player, boolean isPet)
	{
		int i1 = 0;
		
		final Integer status = GrandBossManager.getInstance().getBossStatus(VALAKAS);
		if (status == FIGHTING)
		{
			if (npc.getCurrentHp() > ((npc.getMaxHp() * 1) / 4))
			{
				if (player == c_quest2)
				{
					if (((10 * 1000) + 1000) > i_quest2)
					{
						i_quest2 = ((10 * 1000) + Rnd.get(3000));
					}
				}
				else if (player == c_quest3)
				{
					if (((10 * 1000) + 1000) > i_quest3)
					{
						i_quest3 = ((10 * 1000) + Rnd.get(3000));
					}
				}
				else if (player == c_quest4)
				{
					if (((10 * 1000) + 1000) > i_quest4)
					{
						i_quest4 = ((10 * 1000) + Rnd.get(3000));
					}
				}
				else if (i_quest2 > i_quest3)
				{
					i1 = 3;
				}
				else if (i_quest2 == i_quest3)
				{
					if (Rnd.get(100) < 50)
					{
						i1 = 2;
					}
					else
					{
						i1 = 3;
					}
				}
				else if (i_quest2 < i_quest3)
				{
					i1 = 2;
				}
				if (i1 == 2)
				{
					if (i_quest2 > i_quest4)
					{
						i1 = 4;
					}
					else if (i_quest2 == i_quest4)
					{
						if (Rnd.get(100) < 50)
						{
							i1 = 2;
						}
						else
						{
							i1 = 4;
						}
					}
					else if (i_quest2 < i_quest4)
					{
						i1 = 2;
					}
				}
				else if (i1 == 3)
				{
					if (i_quest3 > i_quest4)
					{
						i1 = 4;
					}
					else if (i_quest3 == i_quest4)
					{
						if (Rnd.get(100) < 50)
						{
							i1 = 3;
						}
						else
						{
							i1 = 4;
						}
					}
					else if (i_quest3 < i_quest4)
					{
						i1 = 3;
					}
				}
				if (i1 == 2)
				{
					i_quest2 = ((10 * 1000) + Rnd.get(3000));
					c_quest2 = player;
				}
				else if (i1 == 3)
				{
					i_quest3 = ((10 * 1000) + Rnd.get(3000));
					c_quest3 = player;
				}
				else if (i1 == 4)
				{
					i_quest4 = ((10 * 1000) + Rnd.get(3000));
					c_quest4 = player;
				}
			}
			else if (npc.getCurrentHp() > ((npc.getMaxHp() * 2) / 4))
			{
				if (player == c_quest2)
				{
					if (((6 * 1000) + 1000) > i_quest2)
					{
						i_quest2 = ((6 * 1000) + Rnd.get(3000));
					}
				}
				else if (player == c_quest3)
				{
					if (((6 * 1000) + 1000) > i_quest3)
					{
						i_quest3 = ((6 * 1000) + Rnd.get(3000));
					}
				}
				else if (player == c_quest4)
				{
					if (((6 * 1000) + 1000) > i_quest4)
					{
						i_quest4 = ((6 * 1000) + Rnd.get(3000));
					}
				}
				else if (i_quest2 > i_quest3)
				{
					i1 = 3;
				}
				else if (i_quest2 == i_quest3)
				{
					if (Rnd.get(100) < 50)
					{
						i1 = 2;
					}
					else
					{
						i1 = 3;
					}
				}
				else if (i_quest2 < i_quest3)
				{
					i1 = 2;
				}
				if (i1 == 2)
				{
					if (i_quest2 > i_quest4)
					{
						i1 = 4;
					}
					else if (i_quest2 == i_quest4)
					{
						if (Rnd.get(100) < 50)
						{
							i1 = 2;
						}
						else
						{
							i1 = 4;
						}
					}
					else if (i_quest2 < i_quest4)
					{
						i1 = 2;
					}
				}
				else if (i1 == 3)
				{
					if (i_quest3 > i_quest4)
					{
						i1 = 4;
					}
					else if (i_quest3 == i_quest4)
					{
						if (Rnd.get(100) < 50)
						{
							i1 = 3;
						}
						else
						{
							i1 = 4;
						}
					}
					else if (i_quest3 < i_quest4)
					{
						i1 = 3;
					}
				}
				if (i1 == 2)
				{
					i_quest2 = ((6 * 1000) + Rnd.get(3000));
					c_quest2 = player;
				}
				else if (i1 == 3)
				{
					i_quest3 = ((6 * 1000) + Rnd.get(3000));
					c_quest3 = player;
				}
				else if (i1 == 4)
				{
					i_quest4 = ((6 * 1000) + Rnd.get(3000));
					c_quest4 = player;
				}
			}
			else if (npc.getCurrentHp() > ((npc.getMaxHp() * 3) / 4.0))
			{
				if (player == c_quest2)
				{
					if (((3 * 1000) + 1000) > i_quest2)
					{
						i_quest2 = ((3 * 1000) + Rnd.get(3000));
					}
				}
				else if (player == c_quest3)
				{
					if (((3 * 1000) + 1000) > i_quest3)
					{
						i_quest3 = ((3 * 1000) + Rnd.get(3000));
					}
				}
				else if (player == c_quest4)
				{
					if (((3 * 1000) + 1000) > i_quest4)
					{
						i_quest4 = ((3 * 1000) + Rnd.get(3000));
					}
				}
				else if (i_quest2 > i_quest3)
				{
					i1 = 3;
				}
				else if (i_quest2 == i_quest3)
				{
					if (Rnd.get(100) < 50)
					{
						i1 = 2;
					}
					else
					{
						i1 = 3;
					}
				}
				else if (i_quest2 < i_quest3)
				{
					i1 = 2;
				}
				if (i1 == 2)
				{
					if (i_quest2 > i_quest4)
					{
						i1 = 4;
					}
					else if (i_quest2 == i_quest4)
					{
						if (Rnd.get(100) < 50)
						{
							i1 = 2;
						}
						else
						{
							i1 = 4;
						}
					}
					else if (i_quest2 < i_quest4)
					{
						i1 = 2;
					}
				}
				else if (i1 == 3)
				{
					if (i_quest3 > i_quest4)
					{
						i1 = 4;
					}
					else if (i_quest3 == i_quest4)
					{
						if (Rnd.get(100) < 50)
						{
							i1 = 3;
						}
						else
						{
							i1 = 4;
						}
					}
					else if (i_quest3 < i_quest4)
					{
						i1 = 3;
					}
				}
				if (i1 == 2)
				{
					i_quest2 = ((3 * 1000) + Rnd.get(3000));
					c_quest2 = player;
				}
				else if (i1 == 3)
				{
					i_quest3 = ((3 * 1000) + Rnd.get(3000));
					c_quest3 = player;
				}
				else if (i1 == 4)
				{
					i_quest4 = ((3 * 1000) + Rnd.get(3000));
					c_quest4 = player;
				}
			}
			else if (player == c_quest2)
			{
				if (((2 * 1000) + 1000) > i_quest2)
				{
					i_quest2 = ((2 * 1000) + Rnd.get(3000));
				}
			}
			else if (player == c_quest3)
			{
				if (((2 * 1000) + 1000) > i_quest3)
				{
					i_quest3 = ((2 * 1000) + Rnd.get(3000));
				}
			}
			else if (player == c_quest4)
			{
				if (((2 * 1000) + 1000) > i_quest4)
				{
					i_quest4 = ((2 * 1000) + Rnd.get(3000));
				}
			}
			else if (i_quest2 > i_quest3)
			{
				i1 = 3;
			}
			else if (i_quest2 == i_quest3)
			{
				if (Rnd.get(100) < 50)
				{
					i1 = 2;
				}
				else
				{
					i1 = 3;
				}
			}
			else if (i_quest2 < i_quest3)
			{
				i1 = 2;
			}
			if (i1 == 2)
			{
				if (i_quest2 > i_quest4)
				{
					i1 = 4;
				}
				else if (i_quest2 == i_quest4)
				{
					if (Rnd.get(100) < 50)
					{
						i1 = 2;
					}
					else
					{
						i1 = 4;
					}
				}
				else if (i_quest2 < i_quest4)
				{
					i1 = 2;
				}
			}
			else if (i1 == 3)
			{
				if (i_quest3 > i_quest4)
				{
					i1 = 4;
				}
				else if (i_quest3 == i_quest4)
				{
					if (Rnd.get(100) < 50)
					{
						i1 = 3;
					}
					else
					{
						i1 = 4;
					}
				}
				else if (i_quest3 < i_quest4)
				{
					i1 = 3;
				}
			}
			if (i1 == 2)
			{
				i_quest2 = ((2 * 1000) + Rnd.get(3000));
				c_quest2 = player;
			}
			else if (i1 == 3)
			{
				i_quest3 = ((2 * 1000) + Rnd.get(3000));
				c_quest3 = player;
			}
			else if (i1 == 4)
			{
				i_quest4 = ((2 * 1000) + Rnd.get(3000));
				c_quest4 = player;
			}
		}
		else if (player == c_quest2)
		{
			if (((1 * 1000) + 1000) > i_quest2)
			{
				i_quest2 = ((1 * 1000) + Rnd.get(3000));
			}
		}
		else if (player == c_quest3)
		{
			if (((1 * 1000) + 1000) > i_quest3)
			{
				i_quest3 = ((1 * 1000) + Rnd.get(3000));
			}
		}
		else if (player == c_quest4)
		{
			if (((1 * 1000) + 1000) > i_quest4)
			{
				i_quest4 = ((1 * 1000) + Rnd.get(3000));
			}
		}
		else if (i_quest2 > i_quest3)
		{
			i1 = 3;
		}
		else if (i_quest2 == i_quest3)
		{
			if (Rnd.get(100) < 50)
			{
				i1 = 2;
			}
			else
			{
				i1 = 3;
			}
		}
		else if (i_quest2 < i_quest3)
		{
			i1 = 2;
		}
		if (i1 == 2)
		{
			if (i_quest2 > i_quest4)
			{
				i1 = 4;
			}
			else if (i_quest2 == i_quest4)
			{
				if (Rnd.get(100) < 50)
				{
					i1 = 2;
				}
				else
				{
					i1 = 4;
				}
			}
			else if (i_quest2 < i_quest4)
			{
				i1 = 2;
			}
		}
		else if (i1 == 3)
		{
			if (i_quest3 > i_quest4)
			{
				i1 = 4;
			}
			else if (i_quest3 == i_quest4)
			{
				if (Rnd.get(100) < 50)
				{
					i1 = 3;
				}
				else
				{
					i1 = 4;
				}
			}
			else if (i_quest3 < i_quest4)
			{
				i1 = 3;
			}
		}
		if (i1 == 2)
		{
			i_quest2 = ((1 * 1000) + Rnd.get(3000));
			c_quest2 = player;
		}
		else if (i1 == 3)
		{
			i_quest3 = ((1 * 1000) + Rnd.get(3000));
			c_quest3 = player;
		}
		else if (i1 == 4)
		{
			i_quest4 = ((1 * 1000) + Rnd.get(3000));
			c_quest4 = player;
		}
		if (status == FIGHTING)
		{
			getRandomSkill(npc);
		}
		else
		{
			return null;
		}
		return super.onAggroRangeEnter(npc, player, isPet);
	}
	
	@Override
	public String onSkillUse(Npc npc, Player caster, Skill skill)
	{
		if (npc.isInvul())
		{
			return null;
		}
		npc.setTarget(caster);
		return super.onSkillUse(npc, caster, skill);
	}
	
	public static void main(String[] args)
	{
		new Valakas();
	}
}