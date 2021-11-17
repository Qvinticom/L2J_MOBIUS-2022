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

import org.l2jmobius.gameserver.ai.CtrlIntention;
import org.l2jmobius.gameserver.instancemanager.SiegeManager;
import org.l2jmobius.gameserver.model.SiegeClan;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.templates.NpcTemplate;
import org.l2jmobius.gameserver.model.siege.Siege;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import org.l2jmobius.gameserver.network.serverpackets.MyTargetSelected;
import org.l2jmobius.gameserver.network.serverpackets.StatusUpdate;
import org.l2jmobius.gameserver.network.serverpackets.ValidateLocation;

public class SiegeFlag extends Npc
{
	private final Player _player;
	private final Siege _siege;
	
	public SiegeFlag(Player player, int objectId, NpcTemplate template)
	{
		super(objectId, template);
		
		_player = player;
		_siege = SiegeManager.getInstance().getSiege(_player.getX(), _player.getY(), _player.getZ());
		if ((_player.getClan() == null) || (_siege == null))
		{
			deleteMe();
		}
		else
		{
			final SiegeClan sc = _siege.getAttackerClan(_player.getClan());
			if (sc == null)
			{
				deleteMe();
			}
			else
			{
				sc.addFlag(this);
			}
		}
	}
	
	@Override
	public boolean isAttackable()
	{
		// Attackable during siege by attacker only
		return (getCastle() != null) && (getCastle().getCastleId() > 0) && getCastle().getSiege().isInProgress();
	}
	
	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		// Attackable during siege by attacker only
		return (attacker instanceof Player) && (getCastle() != null) && (getCastle().getCastleId() > 0) && getCastle().getSiege().isInProgress();
	}
	
	@Override
	public boolean doDie(Creature killer)
	{
		if (!super.doDie(killer))
		{
			return false;
		}
		final SiegeClan sc = _siege.getAttackerClan(_player.getClan());
		if (sc != null)
		{
			sc.removeFlag(this);
		}
		return true;
	}
	
	@Override
	public void onForcedAttack(Player player)
	{
		onAction(player);
	}
	
	@Override
	public void onAction(Player player)
	{
		if ((player == null) || !canTarget(player))
		{
			return;
		}
		
		// Check if the Player already target the Npc
		if (this != player.getTarget())
		{
			// Set the target of the Player player
			player.setTarget(this);
			
			// Send a Server->Client packet MyTargetSelected to the Player player
			player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel() - getLevel()));
			
			// Send a Server->Client packet StatusUpdate of the Npc to the Player to update its HP bar
			final StatusUpdate su = new StatusUpdate(getObjectId());
			su.addAttribute(StatusUpdate.CUR_HP, (int) getStatus().getCurrentHp());
			su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
			player.sendPacket(su);
			
			// Send a Server->Client packet ValidateLocation to correct the Npc position and heading on the client
			player.sendPacket(new ValidateLocation(this));
		}
		else if (isAutoAttackable(player) && (Math.abs(player.getZ() - getZ()) < 100))
		{
			player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
		}
		else
		{
			// Send a Server->Client ActionFailed to the Player in order to avoid that the client wait another packet
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}
}
