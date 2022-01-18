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
package org.l2jmobius.gameserver.handler.skillhandlers;

import java.util.List;

import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.handler.ISkillHandler;
import org.l2jmobius.gameserver.instancemanager.CastleManager;
import org.l2jmobius.gameserver.instancemanager.GrandBossManager;
import org.l2jmobius.gameserver.model.Skill;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.clan.Clan;
import org.l2jmobius.gameserver.model.effects.Effect;
import org.l2jmobius.gameserver.model.siege.Castle;
import org.l2jmobius.gameserver.model.skill.SkillType;
import org.l2jmobius.gameserver.model.zone.ZoneId;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

public class ClanGate implements ISkillHandler
{
	private static final SkillType[] SKILL_TYPES =
	{
		SkillType.CLAN_GATE
	};
	
	@Override
	public void useSkill(Creature creature, Skill skill, List<Creature> targets)
	{
		Player player = null;
		if (creature instanceof Player)
		{
			player = (Player) creature;
		}
		else
		{
			return;
		}
		
		if (player.isOnEvent() || player.isInsideZone(ZoneId.NO_LANDING) || player.isInOlympiadMode() || player.isInsideZone(ZoneId.PVP) || (GrandBossManager.getInstance().getZone(player) != null))
		{
			player.sendMessage("Cannot open the portal here.");
			return;
		}
		
		final Clan clan = player.getClan();
		if ((clan != null) && (CastleManager.getInstance().getCastleByOwner(clan) != null))
		{
			final Castle castle = CastleManager.getInstance().getCastleByOwner(clan);
			if (player.isCastleLord(castle.getCastleId()))
			{
				// Please note clan gate expires in two minutes WHATEVER happens to the clan leader.
				ThreadPool.schedule(new RemoveClanGate(castle.getCastleId(), player), skill.getTotalLifeTime());
				castle.createClanGate(player.getX(), player.getY(), player.getZ() + 20);
				player.getClan().broadcastToOnlineMembers(new SystemMessage(SystemMessageId.COURT_MAGICIAN_THE_PORTAL_HAS_BEEN_CREATED));
				player.setParalyzed(true);
			}
		}
		
		final Effect effect = player.getFirstEffect(skill.getId());
		if ((effect != null) && effect.isSelfEffect())
		{
			effect.exit(false);
		}
		skill.applySelfEffects(player);
	}
	
	private class RemoveClanGate implements Runnable
	{
		private final int castle;
		private final Player player;
		
		protected RemoveClanGate(int castle, Player player)
		{
			this.castle = castle;
			this.player = player;
		}
		
		@Override
		public void run()
		{
			if (player != null)
			{
				player.setParalyzed(false);
			}
			CastleManager.getInstance().getCastleById(castle).destroyClanGate();
		}
	}
	
	@Override
	public SkillType[] getSkillTypes()
	{
		return SKILL_TYPES;
	}
}
