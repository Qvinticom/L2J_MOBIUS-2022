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
package com.l2jmobius.gameserver.model.actor.instance;

import java.util.concurrent.ScheduledFuture;

import com.l2jmobius.Config;
import com.l2jmobius.commons.concurrent.ThreadPoolManager;
import com.l2jmobius.gameserver.datatables.SkillTable;
import com.l2jmobius.gameserver.model.L2Skill;
import com.l2jmobius.gameserver.model.actor.L2Character;
import com.l2jmobius.gameserver.model.actor.L2Summon;
import com.l2jmobius.gameserver.network.serverpackets.CreatureSay;
import com.l2jmobius.gameserver.network.serverpackets.MagicSkillUse;
import com.l2jmobius.gameserver.templates.chars.L2NpcTemplate;

/**
 * @author Ederik
 */
public class L2ProtectorInstance extends L2NpcInstance
{
	private ScheduledFuture<?> _aiTask;
	
	private class ProtectorAI implements Runnable
	{
		private final L2ProtectorInstance _caster;
		
		protected ProtectorAI(L2ProtectorInstance caster)
		{
			_caster = caster;
		}
		
		@SuppressWarnings("synthetic-access")
		@Override
		public void run()
		{
			/**
			 * For each known player in range, cast sleep if pvpFlag != 0 or Karma >0 Skill use is just for buff animation
			 */
			for (L2PcInstance player : getKnownList().getKnownPlayers().values())
			{
				if (((player.getKarma() > 0) && Config.PROTECTOR_PLAYER_PK) || ((player.getPvpFlag() != 0) && Config.PROTECTOR_PLAYER_PVP))
				{
					LOGGER.warning("player: " + player);
					handleCast(player, Config.PROTECTOR_SKILLID, Config.PROTECTOR_SKILLLEVEL);
				}
				final L2Summon activePet = player.getPet();
				
				if (activePet == null)
				{
					continue;
				}
				
				if (((activePet.getKarma() > 0) && Config.PROTECTOR_PLAYER_PK) || ((activePet.getPvpFlag() != 0) && Config.PROTECTOR_PLAYER_PVP))
				{
					LOGGER.warning("activePet: " + activePet);
					handleCastonPet(activePet, Config.PROTECTOR_SKILLID, Config.PROTECTOR_SKILLLEVEL);
				}
			}
		}
		
		// Cast for Player
		private boolean handleCast(L2PcInstance player, int skillId, int skillLevel)
		{
			if (player.isGM() || player.isDead() || !player.isVisible() || !isInsideRadius(player, Config.PROTECTOR_RADIUS_ACTION, false, false))
			{
				return false;
			}
			
			L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLevel);
			
			if (player.getFirstEffect(skill) == null)
			{
				final int objId = _caster.getObjectId();
				skill.getEffects(_caster, player, false, false, false);
				broadcastPacket(new MagicSkillUse(_caster, player, skillId, skillLevel, Config.PROTECTOR_SKILLTIME, 0));
				broadcastPacket(new CreatureSay(objId, 0, getName(), Config.PROTECTOR_MESSAGE));
				
				return true;
			}
			
			return false;
		}
		
		// Cast for pet
		private boolean handleCastonPet(L2Summon player, int skillId, int skillLevel)
		{
			if (player.isDead() || !player.isVisible() || !isInsideRadius(player, Config.PROTECTOR_RADIUS_ACTION, false, false))
			{
				return false;
			}
			
			L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLevel);
			if (player.getFirstEffect(skill) == null)
			{
				final int objId = _caster.getObjectId();
				skill.getEffects(_caster, player, false, false, false);
				broadcastPacket(new MagicSkillUse(_caster, player, skillId, skillLevel, Config.PROTECTOR_SKILLTIME, 0));
				broadcastPacket(new CreatureSay(objId, 0, getName(), Config.PROTECTOR_MESSAGE));
				
				return true;
			}
			
			return false;
		}
	}
	
	public L2ProtectorInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		
		if (_aiTask != null)
		{
			_aiTask.cancel(true);
		}
		
		_aiTask = ThreadPoolManager.scheduleAtFixedRate(new ProtectorAI(this), 3000, 3000);
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
	public boolean isAutoAttackable(L2Character attacker)
	{
		return false;
	}
}
