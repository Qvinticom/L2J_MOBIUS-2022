/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jserver.gameserver.network.clientpackets;

import com.l2jserver.gameserver.instancemanager.AuctionManager;
import com.l2jserver.gameserver.instancemanager.AuctionManager.Auctions;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.serverpackets.ExResponseCommissionDelete;
import com.l2jserver.gameserver.network.serverpackets.ExResponseCommissionItemList;
import com.l2jserver.gameserver.network.serverpackets.ExResponseCommissionList;

/**
 * @author Erlandys
 */
public final class RequestCommissionDelete extends L2GameClientPacket
{
	private static final String _C__D0_9F_REQUESTCOMMISSIONDELETE = "[C] D0:9F RequestCommissionDelete";
	
	long _auctionID;
	int _category;
	int _duration;
	
	@Override
	protected void readImpl()
	{
		_auctionID = readQ();
		_category = readD();
		_duration = readD();
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		AuctionManager am = AuctionManager.getInstance();
		am.checkForAuctionsDeletion();
		Auctions auction = am.getAuctionById(_auctionID);
		if (auction != null)
		{
			player.addItem("DeleteAuction", auction.getItem(), null, false);
			player.getAuctionInventory().destroyItemByItemId("DeleteAuction", auction.getItem().getId(), auction.getCount(), player, null);
			am.deleteAuction(_auctionID);
			player.sendPacket(SystemMessageId.CANCELLATION_OF_SALE_FOR_THE_ITEM_IS_SUCCESSFUL);
			player.sendPacket(new ExResponseCommissionDelete(true));
			player.sendPacket(new ExResponseCommissionList(player));
			player.sendPacket(new ExResponseCommissionItemList(player));
		}
		else
		{
			player.sendPacket(SystemMessageId.ITEM_PURCHASE_IS_NOT_AVAILABLE_BECAUSE_THE_CORRESPONDING_ITEM_DOES_NOT_EXIST);
			player.sendPacket(new ExResponseCommissionDelete(false));
			player.sendPacket(new ExResponseCommissionList(player));
			player.sendPacket(new ExResponseCommissionItemList(player));
		}
	}
	
	@Override
	public String getType()
	{
		return _C__D0_9F_REQUESTCOMMISSIONDELETE;
	}
}
