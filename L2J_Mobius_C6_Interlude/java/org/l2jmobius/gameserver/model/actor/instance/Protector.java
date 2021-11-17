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

import org.l2jmobius.Config;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.data.SkillTable;
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.model.Skill;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.Summon;
import org.l2jmobius.gameserver.model.actor.templates.NpcTemplate;
import org.l2jmobius.gameserver.network.serverpackets.CreatureSay;
import org.l2jmobius.gameserver.network.serverpackets.MagicSkillUse;

/**
 * @author Ederik
 */
public class Protector extends Npc
{
	private ScheduledFuture<?> _aiTask;
	
	private class ProtectorAI implements Runnable
	{
		private final Protector _caster;
		
		protected ProtectorAI(Protector caster)
		{
			_caster = caster;
		}
		
		@Override
		public void run()
		{
			/**
			 * For each known player in range, cast sleep if pvpFlag != 0 or Karma >0 Skill use is just for buff animation
			 */
			for (Player player : getKnownList().getKnownPlayers().values())
			{
				if (((player.getKarma() > 0) && Config.PROTECTOR_PLAYER_PK) || ((player.getPvpFlag() != 0) && Config.PROTECTOR_PLAYER_PVP))
				{
					handleCast(player, Config.PROTECTOR_SKILLID, Config.PROTECTOR_SKILLLEVEL);
				}
				final Summon activePet = player.getPet();
				if (activePet == null)
				{
					continue;
				}
				
				if (((activePet.getKarma() > 0) && Config.PROTECTOR_PLAYER_PK) || ((activePet.getPvpFlag() != 0) && Config.PROTECTOR_PLAYER_PVP))
				{
					handleCastonPet(activePet, Config.PROTECTOR_SKILLID, Config.PROTECTOR_SKILLLEVEL);
				}
			}
		}
		
		// Cast for Player
		private boolean handleCast(Player player, int skillId, int skillLevel)
		{
			if (player.isGM() || player.isDead() || !player.isSpawned() || !isInsideRadius2D(player, Config.PROTECTOR_RADIUS_ACTION))
			{
				return false;
			}
			
			final Skill skill = SkillTable.getInstance().getSkill(skillId, skillLevel);
			if (player.getFirstEffect(skill) == null)
			{
				final int objId = _caster.getObjectId();
				skill.applyEffects(_caster, player, false, false, false);
				broadcastPacket(new MagicSkillUse(_caster, player, skillId, skillLevel, Config.PROTECTOR_SKILLTIME, 0));
				broadcastPacket(new CreatureSay(objId, ChatType.GENERAL, getName(), Config.PROTECTOR_MESSAGE));
				return true;
			}
			
			return false;
		}
		
		// Cast for pet
		private boolean handleCastonPet(Summon player, int skillId, int skillLevel)
		{
			if (player.isDead() || !player.isSpawned() || !isInsideRadius2D(player, Config.PROTECTOR_RADIUS_ACTION))
			{
				return false;
			}
			
			final Skill skill = SkillTable.getInstance().getSkill(skillId, skillLevel);
			if (player.getFirstEffect(skill) == null)
			{
				final int objId = _caster.getObjectId();
				skill.applyEffects(_caster, player, false, false, false);
				broadcastPacket(new MagicSkillUse(_caster, player, skillId, skillLevel, Config.PROTECTOR_SKILLTIME, 0));
				broadcastPacket(new CreatureSay(objId, ChatType.GENERAL, getName(), Config.PROTECTOR_MESSAGE));
				return true;
			}
			
			return false;
		}
	}
	
	public Protector(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		
		if (_aiTask != null)
		{
			_aiTask.cancel(true);
		}
		
		_aiTask = ThreadPool.scheduleAtFixedRate(new ProtectorAI(this), 3000, 3000);
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
	public boolean isAutoAttackable(Creature attacker)
	{
		return false;
	}
}
