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
package com.l2jmobius.gameserver.handler.skillhandlers;

import com.l2jmobius.commons.concurrent.ThreadPoolManager;
import com.l2jmobius.gameserver.handler.ISkillHandler;
import com.l2jmobius.gameserver.instancemanager.CastleManager;
import com.l2jmobius.gameserver.instancemanager.GrandBossManager;
import com.l2jmobius.gameserver.model.L2Clan;
import com.l2jmobius.gameserver.model.L2Effect;
import com.l2jmobius.gameserver.model.L2Object;
import com.l2jmobius.gameserver.model.L2Skill;
import com.l2jmobius.gameserver.model.L2Skill.SkillType;
import com.l2jmobius.gameserver.model.actor.L2Character;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.entity.siege.Castle;
import com.l2jmobius.gameserver.model.zone.ZoneId;
import com.l2jmobius.gameserver.network.SystemMessageId;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;

public class ClanGate implements ISkillHandler
{
	private static final SkillType[] SKILL_IDS =
	{
		SkillType.CLAN_GATE
	};
	
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		L2PcInstance player = null;
		if (activeChar instanceof L2PcInstance)
		{
			player = (L2PcInstance) activeChar;
		}
		else
		{
			return;
		}
		// need more checking...
		if (player.isInFunEvent() || player.isInsideZone(ZoneId.NOLANDING) || player.isInOlympiadMode() || player.isInsideZone(ZoneId.PVP) || (GrandBossManager.getInstance().getZone(player) != null))
		{
			player.sendMessage("Cannot open the portal here.");
			return;
		}
		
		L2Clan clan = player.getClan();
		if (clan != null)
		{
			if (CastleManager.getInstance().getCastleByOwner(clan) != null)
			{
				Castle castle = CastleManager.getInstance().getCastleByOwner(clan);
				if (player.isCastleLord(castle.getCastleId()))
				{
					// please note clan gate expires in two minutes WHATEVER happens to the clan leader.
					ThreadPoolManager.schedule(new RemoveClanGate(castle.getCastleId(), player), skill.getTotalLifeTime());
					castle.createClanGate(player.getX(), player.getY(), player.getZ() + 20);
					player.getClan().broadcastToOnlineMembers(new SystemMessage(SystemMessageId.COURT_MAGICIAN_CREATED_PORTAL));
					player.setIsParalyzed(true);
				}
			}
		}
		
		final L2Effect effect = player.getFirstEffect(skill.getId());
		if ((effect != null) && effect.isSelfEffect())
		{
			effect.exit(false);
		}
		skill.getEffectsSelf(player);
	}
	
	private class RemoveClanGate implements Runnable
	{
		private final int castle;
		private final L2PcInstance player;
		
		protected RemoveClanGate(int castle, L2PcInstance player)
		{
			this.castle = castle;
			this.player = player;
		}
		
		@Override
		public void run()
		{
			if (player != null)
			{
				player.setIsParalyzed(false);
			}
			CastleManager.getInstance().getCastleById(castle).destroyClanGate();
		}
	}
	
	@Override
	public SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
