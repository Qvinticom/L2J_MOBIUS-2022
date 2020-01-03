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
package ai.bosses.SeerUgoros;

import java.util.concurrent.ScheduledFuture;

import org.l2jmobius.commons.concurrent.ThreadPool;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.ai.CtrlIntention;
import org.l2jmobius.gameserver.data.xml.impl.SkillData;
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Attackable;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.skills.Skill;
import org.l2jmobius.gameserver.network.serverpackets.NpcSay;

import ai.AbstractNpcAI;
import quests.Q00288_HandleWithCare.Q00288_HandleWithCare;
import quests.Q00423_TakeYourBestShot.Q00423_TakeYourBestShot;

/**
 * @author RobikBobik
 */
public class SeerUgoros extends AbstractNpcAI
{
	// Items
	private static final int SEER_UGOROS_PASS = 15496;
	private static final int HIGH_GRADE_LIZARD_SCALE = 15497;
	private static final int MIDDLE_GRADE_LIZARD_SCALE = 15498;
	// NPCs
	private static final int SEER_UGOROS = 18863;
	private static final int BATRACOS = 32740;
	private static final int WEED_ID = 18867;
	// Skill
	private static final Skill UGOROS_SKILL = SkillData.getInstance().getSkill(6426, 1);
	// State
	private static final byte ALIVE = 0;
	private static final byte FIGHTING = 1;
	private static final byte DEAD = 2;
	// Misc
	private static byte _state = DEAD;
	private static ScheduledFuture<?> _thinkTask = null;
	private static Npc _ugoros = null;
	private static Npc _weed = null;
	private static boolean _weedAttack = false;
	private static boolean _weedKilledByPlayer = false;
	private static boolean _killedOneWeed = false;
	private static PlayerInstance _player = null;
	
	private SeerUgoros()
	{
		addStartNpc(BATRACOS);
		addTalkId(BATRACOS);
		addKillId(SEER_UGOROS);
		addAttackId(WEED_ID);
		
		startQuestTimer("ugoros_respawn", 60000, null, null);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, PlayerInstance player)
	{
		switch (event)
		{
			case "ugoros_respawn":
			{
				if (_ugoros == null)
				{
					_ugoros = addSpawn(SEER_UGOROS, 96804, 85604, -3720, 34360, false, 0);
					broadcastInRegion(_ugoros, "Listen, oh Tantas! I have returned! The Prophet Yugoros of the Black Abyss is with me, so do not be afraid!");
					_state = ALIVE;
					startQuestTimer("ugoros_shout", 120000, null, null);
				}
				break;
			}
			case "ugoros_shout":
			{
				if (_state == FIGHTING)
				{
					if (_player == null)
					{
						_state = ALIVE;
					}
					else if ((_ugoros != null) && (_player.calculateDistance2D(_ugoros) < 2000))
					{
						_state = ALIVE;
						_player = null;
					}
				}
				else if (_state == ALIVE)
				{
					broadcastInRegion(_ugoros, "Listen, oh Tantas! The Black Abyss is famished! Find some fresh offerings!");
				}
				startQuestTimer("ugoros_shout", 120000, null, null);
				break;
			}
			case "ugoros_attack":
			{
				if (_player != null)
				{
					changeAttackTarget(_player);
					broadcastInRegion(_ugoros, "Welcome, " + _player.getName() + "! Let us see if you have broght a worthy offering for the Black Abyss!");
					if (_thinkTask != null)
					{
						_thinkTask.cancel(true);
					}
					_thinkTask = ThreadPool.scheduleAtFixedRate(new ThinkTask(), 1000, 3000);
				}
				break;
			}
			case "weed_check":
			{
				if (_weedAttack && (_ugoros != null) && (_weed != null))
				{
					if (_weed.isDead() && !_weedKilledByPlayer)
					{
						_killedOneWeed = true;
						_weed = null;
						_weedAttack = false;
						_ugoros.getStatus().setCurrentHp(_ugoros.getStatus().getCurrentHp() + (_ugoros.getMaxHp() * 0.2));
						_ugoros.broadcastPacket(new NpcSay(_ugoros.getObjectId(), ChatType.NPC_GENERAL, _ugoros.getId(), "What a formidable foe! But i have the Abyss Weed given to me by the Black Abyss! Let me see..."));
					}
					else
					{
						startQuestTimer("weed_check", 2000, null, null);
					}
				}
				else
				{
					_weed = null;
					_weedAttack = false;
				}
				break;
			}
			case "ugoros_expel":
			{
				if (_player != null)
				{
					_player.teleToLocation(94701, 83053, -3580);
					_player = null;
				}
				break;
			}
			case "teleportInside":
			{
				if ((player != null) && (_state == ALIVE))
				{
					if (player.getInventory().getItemByItemId(SEER_UGOROS_PASS) != null)
					{
						_state = FIGHTING;
						_player = player;
						_killedOneWeed = false;
						player.teleToLocation(95984, 85692, -3720);
						player.destroyItemByItemId("SeerUgoros", SEER_UGOROS_PASS, 1, npc, true);
						startQuestTimer("ugoros_attack", 2000, null, null);
						
						final QuestState st = player.getQuestState(Q00288_HandleWithCare.class.getSimpleName());
						if (st != null)
						{
							st.set("drop", "1");
						}
					}
					else
					{
						final QuestState st = player.getQuestState(Q00423_TakeYourBestShot.class.getSimpleName());
						if (st == null)
						{
							return "<html><body>Gatekeeper Batracos:<br>You look too inexperienced to make a journey to see Tanta Seer Ugoros. If you can convince Chief Investigator Johnny that you should go, then I will let you pass. Johnny has been everywhere and done everything. He may not be of my people but he has my respect, and anyone who has his will in turn have mine as well.<br></body></html>";
						}
						return "<html><body>Gatekeeper Batracos:<br>Tanta Seer Ugoros is hard to find. You'll just have to keep looking.<br></body></html>";
					}
				}
				else
				{
					return "<html><body>Gatekeeper Batracos:<br>Tanta Seer Ugoros is hard to find. You'll just have to keep looking.<br></body></html>";
				}
				break;
			}
			case "teleport_back":
			{
				if (player != null)
				{
					player.teleToLocation(94701, 83053, -3580);
					_player = null;
				}
				break;
			}
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onAttack(Npc npc, PlayerInstance attacker, int damage, boolean isPet)
	{
		if (npc.isDead())
		{
			return null;
		}
		
		if (npc.getId() == WEED_ID)
		{
			if ((_ugoros != null) && (_weed != null) && npc.equals(_weed))
			{
				// Reset weed
				_weed = null;
				// Reset attack state
				_weedAttack = false;
				// Set it
				_weedKilledByPlayer = true;
				// Complain
				_ugoros.broadcastPacket(new NpcSay(_ugoros.getObjectId(), ChatType.NPC_GENERAL, _ugoros.getId(), "No! How dare you to stop me from using the Abyss Weed... Do you know what you have done?!"));
				// Cancel current think-task
				if (_thinkTask != null)
				{
					_thinkTask.cancel(true);
				}
				// Re-setup task to re-think attack again
				_thinkTask = ThreadPool.scheduleAtFixedRate(new ThinkTask(), 500, 3000);
			}
			
			npc.doDie(attacker);
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	@Override
	public String onKill(Npc npc, PlayerInstance player, boolean isPet)
	{
		if (npc.getId() == SEER_UGOROS)
		{
			if (_thinkTask != null)
			{
				_thinkTask.cancel(true);
				_thinkTask = null;
			}
			
			_state = DEAD;
			broadcastInRegion(_ugoros, "Ah... How could I lose... Oh, Black Abyss, receive me...");
			_ugoros = null;
			
			addSpawn(BATRACOS, 96782, 85918, -3720, 34360, false, 50000);
			
			startQuestTimer("ugoros_expel", 50000, null, null);
			startQuestTimer("ugoros_respawn", 60000, null, null);
			
			final QuestState st = player.getQuestState(Q00288_HandleWithCare.class.getSimpleName());
			if ((st != null) && st.isCond(1) && (st.getInt("drop") == 1))
			{
				if (_killedOneWeed)
				{
					player.addItem("SeerUgoros", MIDDLE_GRADE_LIZARD_SCALE, 1, npc, true);
					st.set("cond", "2");
				}
				else
				{
					player.addItem("SeerUgoros", HIGH_GRADE_LIZARD_SCALE, 1, npc, true);
					st.set("cond", "3");
				}
				st.unset("drop");
			}
		}
		return null;
	}
	
	private void broadcastInRegion(Npc npc, String text)
	{
		if (npc == null)
		{
			return;
		}
		
		final NpcSay npcSay = new NpcSay(npc.getObjectId(), ChatType.NPC_SHOUT, npc.getId(), text);
		for (PlayerInstance player : World.getInstance().getVisibleObjectsInRange(npc, PlayerInstance.class, 6000))
		{
			player.sendPacket(npcSay);
		}
	}
	
	private class ThinkTask implements Runnable
	{
		@Override
		public void run()
		{
			if ((_state == FIGHTING) && (_player != null) && !_player.isDead())
			{
				if (_weedAttack && (_weed != null))
				{
					// Dummy, just wait.
				}
				else if (Rnd.get(10) < 6)
				{
					_weed = null;
					
					for (Attackable attackable : World.getInstance().getVisibleObjectsInRange(_ugoros, Attackable.class, 2000))
					{
						if (!attackable.isDead() && (attackable.getId() == WEED_ID))
						{
							_weedAttack = true;
							_weed = attackable;
							changeAttackTarget(_weed);
							startQuestTimer("weed_check", 1000, null, null);
							break;
						}
					}
					if (_weed == null)
					{
						changeAttackTarget(_player);
					}
				}
				else
				{
					changeAttackTarget(_player);
				}
			}
			else
			{
				_state = ALIVE;
				_player = null;
				
				if (_thinkTask != null)
				{
					_thinkTask.cancel(true);
					_thinkTask = null;
				}
			}
		}
	}
	
	private void changeAttackTarget(Creature attacker)
	{
		((Attackable) _ugoros).getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		((Attackable) _ugoros).clearAggroList();
		((Attackable) _ugoros).setTarget(attacker);
		
		if (attacker instanceof Attackable)
		{
			_weedKilledByPlayer = false;
			
			_ugoros.disableSkill(UGOROS_SKILL, 100000);
			
			((Attackable) _ugoros).setRunning();
			((Attackable) _ugoros).addDamageHate(attacker, 0, Integer.MAX_VALUE);
		}
		else
		{
			_ugoros.enableSkill(UGOROS_SKILL);
			
			((Attackable) _ugoros).addDamageHate(attacker, 0, 99);
			((Attackable) _ugoros).setWalking();
		}
		((Attackable) _ugoros).getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
	}
	
	public static void main(String[] args)
	{
		new SeerUgoros();
	}
}
