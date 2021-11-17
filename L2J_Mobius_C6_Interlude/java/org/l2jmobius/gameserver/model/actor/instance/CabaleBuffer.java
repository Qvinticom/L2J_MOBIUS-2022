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
package org.l2jmobius.gameserver.model.actor.instance;

import java.util.concurrent.ScheduledFuture;

import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.ai.CtrlIntention;
import org.l2jmobius.gameserver.data.SkillTable;
import org.l2jmobius.gameserver.model.Skill;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.templates.NpcTemplate;
import org.l2jmobius.gameserver.model.sevensigns.SevenSigns;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import org.l2jmobius.gameserver.network.serverpackets.MagicSkillUse;
import org.l2jmobius.gameserver.network.serverpackets.MyTargetSelected;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.network.serverpackets.ValidateLocation;

/**
 * @author Layane
 */
public class CabaleBuffer extends Npc
{
	@Override
	public void onAction(Player player)
	{
		if (!canTarget(player))
		{
			return;
		}
		
		if (this != player.getTarget())
		{
			// Set the target of the Player player
			player.setTarget(this);
			
			// Send a Server->Client packet MyTargetSelected to the Player player
			// The color to display in the select window is White
			player.sendPacket(new MyTargetSelected(getObjectId(), 0));
			
			// Send a Server->Client packet ValidateLocation to correct the Artefact position and heading on the client
			player.sendPacket(new ValidateLocation(this));
		}
		else if (!canInteract(player)) // Calculate the distance between the Player and the Npc
		{
			// Notify the Player AI with AI_INTENTION_INTERACT
			player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
		}
		// Send a Server->Client ActionFailed to the Player in order to avoid that the client wait another packet
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	private ScheduledFuture<?> _aiTask;
	
	private class CabalaAI implements Runnable
	{
		private final CabaleBuffer _caster;
		
		protected CabalaAI(CabaleBuffer caster)
		{
			_caster = caster;
		}
		
		@Override
		public void run()
		{
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
			
			/**
			 * For each known player in range, cast either the positive or negative buff.<br>
			 * The stats affected depend on the player type, either a fighter or a mystic.<br>
			 * <br>
			 * Curse of Destruction (Loser)<br>
			 * - Fighters: -25% Accuracy, -25% Effect Resistance<br>
			 * - Mystics: -25% Casting Speed, -25% Effect Resistance<br>
			 * <br>
			 * <br>
			 * Blessing of Prophecy (Winner) - Fighters: +25% Max Load, +25% Effect Resistance<br>
			 * - Mystics: +25% Magic Cancel Resist, +25% Effect Resistance<br>
			 */
			for (Player player : getKnownList().getKnownPlayers().values())
			{
				final int playerCabal = SevenSigns.getInstance().getPlayerCabal(player);
				if ((playerCabal == winningCabal) && (playerCabal != SevenSigns.CABAL_NULL) && (_caster.getNpcId() == SevenSigns.ORATOR_NPC_ID))
				{
					if (!player.isMageClass())
					{
						if (handleCast(player, 4364))
						{
							isBuffAWinner = true;
							continue;
						}
					}
					else if (handleCast(player, 4365))
					{
						isBuffAWinner = true;
						continue;
					}
				}
				else if ((playerCabal == losingCabal) && (playerCabal != SevenSigns.CABAL_NULL) && (_caster.getNpcId() == SevenSigns.PREACHER_NPC_ID))
				{
					if (!player.isMageClass())
					{
						if (handleCast(player, 4361))
						{
							isBuffALoser = true;
							continue;
						}
					}
					else if (handleCast(player, 4362))
					{
						isBuffALoser = true;
						continue;
					}
				}
				
				if (isBuffAWinner && isBuffALoser)
				{
					break;
				}
			}
		}
		
		private boolean handleCast(Player player, int skillId)
		{
			final int skillLevel = player.getLevel() > 40 ? 1 : 2;
			if (player.isDead() || !player.isSpawned() || !isInsideRadius2D(player, getDistanceToWatchObject(player)))
			{
				return false;
			}
			
			final Skill skill = SkillTable.getInstance().getSkill(skillId, skillLevel);
			if (player.getFirstEffect(skill) == null)
			{
				skill.applyEffects(_caster, player, false, false, false);
				broadcastPacket(new MagicSkillUse(_caster, player, skill.getId(), skillLevel, skill.getHitTime(), 0));
				final SystemMessage sm = new SystemMessage(SystemMessageId.THE_EFFECTS_OF_S1_FLOW_THROUGH_YOU);
				sm.addSkillName(skillId);
				player.sendPacket(sm);
				return true;
			}
			
			return false;
		}
	}
	
	public CabaleBuffer(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		
		if (_aiTask != null)
		{
			_aiTask.cancel(true);
		}
		
		_aiTask = ThreadPool.scheduleAtFixedRate(new CabalaAI(this), 3000, 3000);
	}
	
	@Override
	public void deleteMe()
	{
		if (_aiTask != null)
		{
			_aiTask.cancel(true);
			_aiTask = null;
		}
		
		super.deleteMe();
	}
	
	@Override
	public int getDistanceToWatchObject(WorldObject object)
	{
		return 900;
	}
	
	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		return false;
	}
}
