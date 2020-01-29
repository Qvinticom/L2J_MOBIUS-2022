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
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import org.l2jmobius.gameserver.network.serverpackets.SendTradeRequest;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.util.Util;

public class TradeRequest extends GameClientPacket
{
	private int _objectId;
	
	@Override
	protected void readImpl()
	{
		_objectId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final PlayerInstance player = getClient().getPlayer();
		if (player == null)
		{
			return;
		}
		
		if (!player.getAccessLevel().allowTransaction())
		{
			player.sendMessage("Transactions are disable for your access level.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final WorldObject target = World.getInstance().findObject(_objectId);
		if ((target == null) || !player.getKnownList().knowsObject(target) || !(target instanceof PlayerInstance) || (target.getObjectId() == player.getObjectId()))
		{
			player.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final PlayerInstance partner = (PlayerInstance) target;
		
		if (partner.isInOlympiadMode() || player.isInOlympiadMode())
		{
			player.sendMessage("You or your target can't request trade in Olympiad mode.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (partner.isStunned())
		{
			player.sendMessage("You can't request a trade when partner is stunned.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (partner.isConfused())
		{
			player.sendMessage("You can't request a trade when partner is confused.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (partner.isCastingNow() || partner.isCastingPotionNow())
		{
			player.sendMessage("You can't request a trade when partner is casting.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (partner.isInDuel())
		{
			player.sendMessage("You can't request a trade when partner is in duel.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (partner.isImmobilized())
		{
			player.sendMessage("You can't request a trade when partner is immobilized.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (partner.isInFunEvent())
		{
			player.sendMessage("You can't request a trade when partner in event.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (partner.getActiveEnchantItem() != null)
		{
			player.sendMessage("You can't request a trade when partner is enchanting.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (partner.isParalyzed())
		{
			player.sendMessage("You can't request a trade when partner is paralyzed.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (partner.inObserverMode())
		{
			player.sendMessage("You can't request a trade when partner is in observation mode.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (partner.isAttackingNow())
		{
			player.sendMessage("You can't request a trade when partner is attacking.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isStunned())
		{
			player.sendMessage("You can't request a trade when you are stunned.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isConfused())
		{
			player.sendMessage("You can't request a trade when you are confused.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isCastingNow() || player.isCastingPotionNow())
		{
			player.sendMessage("You can't request a trade when you are casting.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isInDuel())
		{
			player.sendMessage("You can't request a trade when you are in duel.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isImmobilized())
		{
			player.sendMessage("You can't request a trade when you are immobilized.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isInFunEvent())
		{
			player.sendMessage("You can't request a trade when you are in event.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.getActiveEnchantItem() != null)
		{
			player.sendMessage("You can't request a trade when you enchanting.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isParalyzed())
		{
			player.sendMessage("You can't request a trade when you are paralyzed.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.inObserverMode())
		{
			player.sendMessage("You can't request a trade when you in observation mode.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.getDistanceSq(partner) > 22500) // 150
		{
			player.sendPacket(SystemMessageId.TARGET_TOO_FAR);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Alt game - Karma punishment
		if (!Config.ALT_GAME_KARMA_PLAYER_CAN_TRADE && ((player.getKarma() > 0) || (partner.getKarma() > 0)))
		{
			player.sendMessage("Chaotic players can't use trade.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if ((player.getPrivateStoreType() != 0) || (partner.getPrivateStoreType() != 0))
		{
			player.sendPacket(SystemMessageId.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (!Config.ALLOW_LOW_LEVEL_TRADE && (((player.getLevel() < 76) && (partner.getLevel() >= 76)) || (partner.getLevel() < 76) || (player.getLevel() >= 76)))
		{
			player.sendMessage("You cannot trade a lower level character.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isProcessingTransaction())
		{
			player.sendPacket(SystemMessageId.ALREADY_TRADING);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (partner.isProcessingRequest() || partner.isProcessingTransaction())
		{
			final SystemMessage sm = new SystemMessage(SystemMessageId.S1_IS_BUSY_TRY_LATER);
			sm.addString(partner.getName());
			player.sendPacket(sm);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (Util.calculateDistance(player, partner, true) > 150)
		{
			final SystemMessage sm = new SystemMessage(SystemMessageId.TARGET_TOO_FAR);
			player.sendPacket(sm);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		player.onTransactionRequest(partner);
		partner.sendPacket(new SendTradeRequest(player.getObjectId()));
		final SystemMessage sm = new SystemMessage(SystemMessageId.REQUEST_S1_FOR_TRADE);
		sm.addString(partner.getName());
		player.sendPacket(sm);
	}
}