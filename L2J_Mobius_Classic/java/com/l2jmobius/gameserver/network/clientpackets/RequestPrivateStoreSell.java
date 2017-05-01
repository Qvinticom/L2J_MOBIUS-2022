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
package com.l2jmobius.gameserver.network.clientpackets;

import static com.l2jmobius.gameserver.model.actor.L2Npc.INTERACTION_DISTANCE;

import com.l2jmobius.Config;
import com.l2jmobius.commons.network.PacketReader;
import com.l2jmobius.gameserver.data.sql.impl.OfflineTradersTable;
import com.l2jmobius.gameserver.enums.PrivateStoreType;
import com.l2jmobius.gameserver.model.ItemRequest;
import com.l2jmobius.gameserver.model.L2World;
import com.l2jmobius.gameserver.model.TradeList;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.ceremonyofchaos.CeremonyOfChaosEvent;
import com.l2jmobius.gameserver.network.L2GameClient;
import com.l2jmobius.gameserver.network.SystemMessageId;
import com.l2jmobius.gameserver.network.serverpackets.ActionFailed;

public final class RequestPrivateStoreSell implements IClientIncomingPacket
{
	private int _storePlayerId;
	private ItemRequest[] _items = null;
	
	@Override
	public boolean read(L2GameClient client, PacketReader packet)
	{
		_storePlayerId = packet.readD();
		final int count = packet.readD();
		if ((count <= 0) || (count > Config.MAX_ITEM_IN_PACKET))
		{
			return false;
		}
		_items = new ItemRequest[count];
		
		for (int i = 0; i < count; i++)
		{
			final int objectId = packet.readD();
			final int itemId = packet.readD();
			@SuppressWarnings("unused")
			final int enchantLevel = packet.readH(); // TODO: use this
			@SuppressWarnings("unused")
			final int nameExists = packet.readH(); // TODO: use this
			final long cnt = packet.readQ();
			final long price = packet.readQ();
			@SuppressWarnings("unused")
			final int augmentationEffect1 = packet.readD(); // TODO: use this
			@SuppressWarnings("unused")
			final int augmentationEffect2 = packet.readD(); // TODO: use this
			@SuppressWarnings("unused")
			final int visualId = packet.readD(); // TODO: use this
			final int primarySpecialAbilities = packet.readC();
			for (int a = 0; a < primarySpecialAbilities; a++)
			{
				packet.readD(); // sa effect
			}
			final int secondarySpecialAbilities = packet.readC();
			for (int a = 0; a < secondarySpecialAbilities; a++)
			{
				packet.readD(); // sa effect
			}
			if (/* (objectId < 1) || */ (itemId < 1) || (cnt < 1) || (price < 0))
			{
				_items = null;
				return false;
			}
			_items[i] = new ItemRequest(objectId, itemId, cnt, price);
		}
		return true;
	}
	
	@Override
	public void run(L2GameClient client)
	{
		final L2PcInstance player = client.getActiveChar();
		if (player == null)
		{
			return;
		}
		
		if (_items == null)
		{
			client.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Cannot set private store in Ceremony of Chaos event.
		if (player.isOnEvent(CeremonyOfChaosEvent.class))
		{
			client.sendPacket(SystemMessageId.YOU_CANNOT_OPEN_A_PRIVATE_STORE_OR_WORKSHOP_IN_THE_CEREMONY_OF_CHAOS);
			return;
		}
		
		if (!client.getFloodProtectors().getTransaction().tryPerformAction("privatestoresell"))
		{
			player.sendMessage("You are selling items too fast.");
			return;
		}
		
		final L2PcInstance storePlayer = L2World.getInstance().getPlayer(_storePlayerId);
		if ((storePlayer == null) || !player.isInsideRadius(storePlayer, INTERACTION_DISTANCE, true, false))
		{
			return;
		}
		
		if (player.getInstanceWorld() != storePlayer.getInstanceWorld())
		{
			return;
		}
		
		if ((storePlayer.getPrivateStoreType() != PrivateStoreType.BUY) || player.isCursedWeaponEquipped())
		{
			return;
		}
		
		final TradeList storeList = storePlayer.getBuyList();
		if (storeList == null)
		{
			return;
		}
		
		if (!player.getAccessLevel().allowTransaction())
		{
			player.sendMessage("Transactions are disabled for your Access Level.");
			client.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (!storeList.privateStoreSell(player, _items))
		{
			client.sendPacket(ActionFailed.STATIC_PACKET);
			_log.warning("PrivateStore sell has failed due to invalid list or request. Player: " + player.getName() + ", Private store of: " + storePlayer.getName());
			return;
		}
		
		// Update offline trade record, if realtime saving is enabled
		if (Config.OFFLINE_TRADE_ENABLE && Config.STORE_OFFLINE_TRADE_IN_REALTIME && ((storePlayer.getClient() == null) || storePlayer.getClient().isDetached()))
		{
			OfflineTradersTable.onTransaction(storePlayer, storeList.getItemCount() == 0, false);
		}
		
		if (storeList.getItemCount() == 0)
		{
			storePlayer.setPrivateStoreType(PrivateStoreType.NONE);
			storePlayer.broadcastUserInfo();
		}
	}
}
