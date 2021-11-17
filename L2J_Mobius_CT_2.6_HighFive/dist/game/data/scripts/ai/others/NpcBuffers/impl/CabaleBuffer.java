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
package ai.others.NpcBuffers.impl;

import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.data.xml.SkillData;
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.sevensigns.SevenSigns;
import org.l2jmobius.gameserver.model.skills.BuffInfo;
import org.l2jmobius.gameserver.model.skills.Skill;
import org.l2jmobius.gameserver.network.NpcStringId;

import ai.AbstractNpcAI;

/**
 * Preacher of Doom and Orator of Revelations AI
 * @author UnAfraid, malyelfik
 */
public class CabaleBuffer extends AbstractNpcAI
{
	private static final int DISTANCE_TO_WATCH_OBJECT = 900;
	
	// Messages
	protected static final NpcStringId[] ORATOR_MSG =
	{
		NpcStringId.THE_DAY_OF_JUDGMENT_IS_NEAR,
		NpcStringId.THE_PROPHECY_OF_DARKNESS_HAS_BEEN_FULFILLED,
		NpcStringId.AS_FORETOLD_IN_THE_PROPHECY_OF_DARKNESS_THE_ERA_OF_CHAOS_HAS_BEGUN,
		NpcStringId.THE_PROPHECY_OF_DARKNESS_HAS_COME_TO_PASS
	};
	protected static final NpcStringId[] PREACHER_MSG =
	{
		NpcStringId.THIS_WORLD_WILL_SOON_BE_ANNIHILATED,
		NpcStringId.ALL_IS_LOST_PREPARE_TO_MEET_THE_GODDESS_OF_DEATH,
		NpcStringId.ALL_IS_LOST_THE_PROPHECY_OF_DESTRUCTION_HAS_BEEN_FULFILLED,
		NpcStringId.THE_END_OF_TIME_HAS_COME_THE_PROPHECY_OF_DESTRUCTION_HAS_BEEN_FULFILLED
	};
	
	// Skills
	private static final int ORATOR_FIGTER = 4364;
	private static final int ORATOR_MAGE = 4365;
	private static final int PREACHER_FIGTER = 4361;
	private static final int PREACHER_MAGE = 4362;
	
	private CabaleBuffer()
	{
		addFirstTalkId(SevenSigns.ORATOR_NPC_ID, SevenSigns.PREACHER_NPC_ID);
		addSpawnId(SevenSigns.ORATOR_NPC_ID, SevenSigns.PREACHER_NPC_ID);
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		return null;
	}
	
	@Override
	public String onSpawn(Npc npc)
	{
		ThreadPool.schedule(new CabaleAI(npc), 3000);
		ThreadPool.schedule(new Talk(npc), 60000);
		return super.onSpawn(npc);
	}
	
	protected class Talk implements Runnable
	{
		private final Npc _npc;
		
		protected Talk(Npc npc)
		{
			_npc = npc;
		}
		
		@Override
		public void run()
		{
			if ((_npc != null) && !_npc.isDecayed())
			{
				NpcStringId[] messages = ORATOR_MSG;
				if (_npc.getId() == SevenSigns.PREACHER_NPC_ID)
				{
					messages = PREACHER_MSG;
				}
				broadcastSay(_npc, getRandomEntry(messages), null, -1);
				ThreadPool.schedule(this, 60000);
			}
		}
	}
	
	protected class CabaleAI implements Runnable
	{
		private final Npc _npc;
		
		protected CabaleAI(Npc npc)
		{
			_npc = npc;
		}
		
		@Override
		public void run()
		{
			if ((_npc == null) || !_npc.isSpawned())
			{
				return;
			}
			
			boolean isBuffAWinner = false;
			boolean isBuffALoser = false;
			
			final int winningCabal = SevenSigns.getInstance().getCabalHighestScore();
			int losingCabal = SevenSigns.CABAL_NULL;
			if (winningCabal == SevenSigns.CABAL_DAWN)
			{
				losingCabal = SevenSigns.CABAL_DUSK;
			}
			else if (winningCabal == SevenSigns.CABAL_DUSK)
			{
				losingCabal = SevenSigns.CABAL_DAWN;
			}
			
			for (Player player : World.getInstance().getVisibleObjects(_npc, Player.class))
			{
				if ((player == null) || player.isInvul())
				{
					continue;
				}
				
				final int playerCabal = SevenSigns.getInstance().getPlayerCabal(player.getObjectId());
				if ((playerCabal == winningCabal) && (playerCabal != SevenSigns.CABAL_NULL) && (_npc.getId() == SevenSigns.ORATOR_NPC_ID))
				{
					if (!player.isMageClass())
					{
						if (handleCast(player, ORATOR_FIGTER))
						{
							if (getAbnormalLevel(player, ORATOR_FIGTER) == 2)
							{
								broadcastSay(_npc, NpcStringId.S1_I_GIVE_YOU_THE_BLESSING_OF_PROPHECY, player.getName(), 500);
							}
							else
							{
								broadcastSay(_npc, NpcStringId.I_BESTOW_UPON_YOU_A_BLESSING, null, 1);
							}
							isBuffAWinner = true;
							continue;
						}
					}
					else
					{
						if (handleCast(player, ORATOR_MAGE))
						{
							if (getAbnormalLevel(player, ORATOR_MAGE) == 2)
							{
								broadcastSay(_npc, NpcStringId.S1_I_BESTOW_UPON_YOU_THE_AUTHORITY_OF_THE_ABYSS, player.getName(), 500);
							}
							else
							{
								broadcastSay(_npc, NpcStringId.HERALD_OF_THE_NEW_ERA_OPEN_YOUR_EYES, null, 1);
							}
							isBuffAWinner = true;
							continue;
						}
					}
				}
				else if ((playerCabal == losingCabal) && (playerCabal != SevenSigns.CABAL_NULL) && (_npc.getId() == SevenSigns.PREACHER_NPC_ID))
				{
					if (!player.isMageClass())
					{
						if (handleCast(player, PREACHER_FIGTER))
						{
							if (getAbnormalLevel(player, PREACHER_FIGTER) == 2)
							{
								broadcastSay(_npc, NpcStringId.A_CURSE_UPON_YOU, player.getName(), 500);
							}
							else
							{
								broadcastSay(_npc, NpcStringId.YOU_DON_T_HAVE_ANY_HOPE_YOUR_END_HAS_COME, null, 1);
							}
							isBuffALoser = true;
							continue;
						}
					}
					else
					{
						if (handleCast(player, PREACHER_MAGE))
						{
							if (getAbnormalLevel(player, PREACHER_MAGE) == 2)
							{
								broadcastSay(_npc, NpcStringId.S1_YOU_MIGHT_AS_WELL_GIVE_UP, player.getName(), 500);
							}
							else
							{
								broadcastSay(_npc, NpcStringId.S1_YOU_BRING_AN_ILL_WIND, player.getName(), 1);
							}
							isBuffALoser = true;
							continue;
						}
					}
				}
				
				if (isBuffAWinner && isBuffALoser)
				{
					break;
				}
			}
			ThreadPool.schedule(this, 3000);
		}
		
		/**
		 * For each known player in range, cast either the positive or negative buff.<br>
		 * The stats affected depend on the player type, either a fighter or a mystic.<br>
		 * <br>
		 * Curse of Destruction (Loser)<br>
		 * - Fighters: -25% Accuracy, -25% Effect Resistance<br>
		 * - Mystics: -25% Casting Speed, -25% Effect Resistance<br>
		 * <br>
		 * <br>
		 * Blessing of Prophecy (Winner)<br>
		 * - Fighters: +25% Max Load, +25% Effect Resistance<br>
		 * - Mystics: +25% Magic Cancel Resist, +25% Effect Resistance<br>
		 * @param player
		 * @param skillId
		 * @return
		 */
		private boolean handleCast(Player player, int skillId)
		{
			if (player.isDead() || !player.isSpawned() || !_npc.isInsideRadius2D(player, DISTANCE_TO_WATCH_OBJECT))
			{
				return false;
			}
			
			boolean doCast = false;
			int skillLevel = 1;
			
			final int level = getAbnormalLevel(player, skillId);
			if (level == 0)
			{
				doCast = true;
			}
			else if ((level == 1) && (getRandom(100) < 5))
			{
				doCast = true;
				skillLevel = 2;
			}
			
			if (doCast)
			{
				final Skill skill = SkillData.getInstance().getSkill(skillId, skillLevel);
				_npc.setTarget(player);
				_npc.doCast(skill);
				return true;
			}
			return false;
		}
	}
	
	public void broadcastSay(Npc npc, NpcStringId message, String param, int chance)
	{
		if (chance == -1)
		{
			npc.broadcastSay(ChatType.NPC_GENERAL, message);
		}
		else if (getRandom(10000) < chance)
		{
			npc.broadcastSay(ChatType.NPC_GENERAL, message, param);
		}
	}
	
	public int getAbnormalLevel(Player player, int skillId)
	{
		final BuffInfo info = player.getEffectList().getBuffInfoBySkillId(skillId);
		return (info != null) ? info.getSkill().getAbnormalLevel() : 0;
	}
	
	public static void main(String[] args)
	{
		new CabaleBuffer();
	}
}
