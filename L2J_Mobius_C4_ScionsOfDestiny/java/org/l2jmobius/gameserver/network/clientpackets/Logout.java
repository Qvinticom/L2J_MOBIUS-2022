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
package org.l2jmobius.gameserver.network.clientpackets;

import org.l2jmobius.Config;
import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.commons.util.Chronos;
import org.l2jmobius.gameserver.data.SkillTable;
import org.l2jmobius.gameserver.model.Party;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.olympiad.Olympiad;
import org.l2jmobius.gameserver.model.sevensigns.SevenSignsFestival;
import org.l2jmobius.gameserver.model.zone.ZoneId;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.taskmanager.AttackStanceTaskManager;

public class Logout implements IClientIncomingPacket
{
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		return true;
	}
	
	@Override
	public void run(GameClient client)
	{
		// Do not allow leaving if player is fighting
		final Player player = client.getPlayer();
		if (player == null)
		{
			return;
		}
		
		if (player.isRegisteredOnEvent())
		{
			player.sendMessage("You cannot logout while registered in an event.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isInsideZone(ZoneId.NO_RESTART))
		{
			player.sendPacket(SystemMessageId.YOU_MAY_NOT_LOG_OUT_FROM_THIS_LOCATION);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (AttackStanceTaskManager.getInstance().hasAttackStanceTask(player) && (!player.isGM() || !Config.GM_RESTART_FIGHTING))
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_EXIT_WHILE_IN_COMBAT);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Do not allow leaving if player is in combat
		if (player.isInCombat() && !player.isGM())
		{
			player.sendMessage("You cannot logout while in combat mode.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Do not allow leaving if player is teleporting
		if (player.isTeleporting() && !player.isGM())
		{
			player.sendMessage("You cannot logout while teleporting.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isInOlympiadMode() || Olympiad.getInstance().isRegistered(player))
		{
			player.sendMessage("You can't Logout in Olympiad mode.");
			return;
		}
		
		// Prevent player from logging out if they are a festival participant nd it is in progress,
		// otherwise notify party members that the player is not longer a participant.
		if (player.isFestivalParticipant())
		{
			if (SevenSignsFestival.getInstance().isFestivalInitialized())
			{
				player.sendMessage("You cannot logout while you are a participant in a festival.");
				return;
			}
			
			final Party playerParty = player.getParty();
			if (playerParty != null)
			{
				player.getParty().broadcastToPartyMembers(SystemMessage.sendString(player.getName() + " has been removed from the upcoming festival."));
			}
		}
		
		player.getInventory().updateDatabase();
		
		if (player.isFlying())
		{
			player.removeSkill(SkillTable.getInstance().getSkill(4289, 1));
		}
		
		if (Config.OFFLINE_LOGOUT && player.isSitting())
		{
			if ((player.isInStoreMode() && Config.OFFLINE_TRADE_ENABLE) || (player.isCrafting() && Config.OFFLINE_CRAFT_ENABLE))
			{
				// Sleep effect, not official feature but however L2OFF features (like offline trade)
				if (Config.OFFLINE_SLEEP_EFFECT)
				{
					player.startAbnormalEffect(Creature.ABNORMAL_EFFECT_SLEEP);
				}
				
				player.store();
				player.closeNetConnection();
				
				if (player.getOfflineStartTime() == 0)
				{
					player.setOfflineStartTime(Chronos.currentTimeMillis());
				}
				return;
			}
		}
		else if (player.isStored())
		{
			player.store();
			player.closeNetConnection();
			
			if (player.getOfflineStartTime() == 0)
			{
				player.setOfflineStartTime(Chronos.currentTimeMillis());
			}
			return;
		}
		
		if (player.isCastingNow())
		{
			player.abortCast();
			player.sendPacket(new ActionFailed());
		}
		
		player.deleteMe();
	}
}