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

import java.util.logging.Logger;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.model.L2World;
import com.l2jmobius.gameserver.model.TradeList;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import com.l2jmobius.gameserver.network.serverpackets.TradeOtherAdd;
import com.l2jmobius.gameserver.network.serverpackets.TradeOwnAdd;
import com.l2jmobius.gameserver.network.serverpackets.TradeUpdate;

/**
 * This class ...
 * @version $Revision: 1.5.2.2.2.5 $ $Date: 2005/03/27 15:29:29 $
 */
public class AddTradeItem extends L2GameClientPacket
{
	private static final String _C__16_ADDTRADEITEM = "[C] 16 AddTradeItem";
	private static Logger _log = Logger.getLogger(AddTradeItem.class.getName());
	
	private int _tradeId;
	private int _objectId;
	private int _count;
	
	@Override
	protected void readImpl()
	{
		_tradeId = readD();
		_objectId = readD();
		_count = readD();
	}
	
	@Override
	public void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		
		final TradeList trade = player.getActiveTradeList();
		if (trade == null)
		{
			_log.warning("Character: " + player.getName() + " requested item:" + _objectId + " add without active tradelist:" + _tradeId);
			return;
		}
		
		final L2PcInstance partner = trade.getPartner();
		if ((partner == null) || (L2World.getInstance().findObject(partner.getObjectId()) == null) || (partner.getActiveTradeList() == null))
		{
			// Trade partner not found, cancel trade
			if (trade.getPartner() != null)
			{
				_log.warning("Character:" + player.getName() + " requested invalid trade object: " + _objectId);
			}
			
			player.sendPacket(new SystemMessage(SystemMessage.TARGET_IS_NOT_FOUND_IN_THE_GAME));
			player.cancelActiveTrade();
			
			return;
		}
		
		if (trade.isConfirmed() || partner.getActiveTradeList().isConfirmed())
		{
			player.sendPacket(new SystemMessage(122));
			return;
		}
		
		if (Config.GM_DISABLE_TRANSACTION && (player.getAccessLevel() >= Config.GM_TRANSACTION_MIN) && (player.getAccessLevel() <= Config.GM_TRANSACTION_MAX))
		{
			player.sendMessage("Transactions are disabled for your Access Level.");
			player.cancelActiveTrade();
			return;
		}
		
		if (!player.validateItemManipulation(_objectId, "trade"))
		{
			player.sendPacket(new SystemMessage(SystemMessage.NOTHING_HAPPENED));
			return;
		}
		
		final TradeList.TradeItem item = trade.addItem(_objectId, _count);
		if (item != null)
		{
			// Trade start packet updates tradelist here
			player.sendPacket(new TradeOwnAdd(item));
			player.sendPacket(new TradeUpdate(player));
			trade.getPartner().sendPacket(new TradeOtherAdd(item));
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.network.clientpackets.L2GameClientPacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__16_ADDTRADEITEM;
	}
}
