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
import org.l2jmobius.gameserver.cache.HtmCache;
import org.l2jmobius.gameserver.enums.ItemLocation;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.instance.Fisherman;
import org.l2jmobius.gameserver.model.actor.instance.Merchant;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import org.l2jmobius.gameserver.network.serverpackets.ItemList;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jmobius.gameserver.network.serverpackets.StatusUpdate;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

public class RequestSellItem implements IClientIncomingPacket
{
	private int _listId;
	private int _count;
	private int[] _items; // count*3
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_listId = packet.readD();
		_count = packet.readD();
		if ((_count <= 0) || ((_count * 12) > packet.getReadableBytes()) || (_count > Config.MAX_ITEM_IN_PACKET))
		{
			_count = 0;
			return false;
		}
		
		_items = new int[_count * 3];
		for (int i = 0; i < _count; i++)
		{
			final int objectId = packet.readD();
			_items[(i * 3) + 0] = objectId;
			final int itemId = packet.readD();
			_items[(i * 3) + 1] = itemId;
			final long cnt = packet.readD();
			if ((cnt > Integer.MAX_VALUE) || (cnt <= 0))
			{
				_count = 0;
				return false;
			}
			_items[(i * 3) + 2] = (int) cnt;
		}
		
		return true;
	}
	
	@Override
	public void run(GameClient client)
	{
		final Player player = client.getPlayer();
		if (player == null)
		{
			return;
		}
		
		if (!client.getFloodProtectors().canPerformTransaction())
		{
			player.sendMessage("You buying too fast.");
			return;
		}
		
		if (_items == null)
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Alt game - Karma punishment
		if (!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && (player.getKarma() > 0))
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final WorldObject target = player.getTarget();
		if (!player.isGM() && (!(target instanceof Merchant) || !player.isInsideRadius2D(target, Npc.INTERACTION_DISTANCE)))
		{
			return;
		}
		
		String htmlFolder = "";
		Npc merchant = null;
		if (target instanceof Merchant)
		{
			htmlFolder = "merchant";
			merchant = (Npc) target;
		}
		else if (target instanceof Fisherman)
		{
			htmlFolder = "fisherman";
			merchant = (Npc) target;
		}
		else
		{
			return;
		}
		
		if ((_listId > 1000000) && (merchant.getTemplate().getNpcId() != (_listId - 1000000)))
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		long totalPrice = 0;
		// Proceed the sell
		for (int i = 0; i < _count; i++)
		{
			// Check count
			final int count = _items[(i * 3) + 2];
			if ((count <= 0) || (count > Integer.MAX_VALUE))
			{
				player.sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED));
				return;
			}
			
			// Check Item
			final int objectId = _items[i * 3];
			final Item item = player.checkItemManipulation(objectId, count, "sell");
			if ((item == null) || !item.getItem().isSellable() || (item.getItemLocation() != ItemLocation.INVENTORY))
			{
				continue;
			}
			
			final long price = item.getReferencePrice() / 2;
			totalPrice += price * count;
			
			// Fix exploit during Sell
			if (((Integer.MAX_VALUE / count) < price) || (totalPrice > Integer.MAX_VALUE))
			{
				player.sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED));
				return;
			}
			
			// Check totalPrice
			if (totalPrice <= 0)
			{
				player.sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED));
				return;
			}
			
			player.getInventory().destroyItem("Sell", objectId, count, player, null);
		}
		
		if (!Config.MERCHANT_ZERO_SELL_PRICE)
		{
			player.addAdena("Sell", (int) totalPrice, merchant, false);
		}
		
		final String html = HtmCache.getInstance().getHtm("data/html/" + htmlFolder + "/" + merchant.getNpcId() + "-sold.htm");
		if (html != null)
		{
			final NpcHtmlMessage soldMsg = new NpcHtmlMessage(merchant.getObjectId());
			soldMsg.setHtml(html.replace("%objectId%", String.valueOf(merchant.getObjectId())));
			player.sendPacket(soldMsg);
		}
		
		// Update current load as well
		final StatusUpdate su = new StatusUpdate(player.getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		player.sendPacket(su);
		player.sendPacket(new ItemList(player, true));
	}
}
