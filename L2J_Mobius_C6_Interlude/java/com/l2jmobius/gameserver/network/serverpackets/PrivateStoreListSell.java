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
package com.l2jmobius.gameserver.network.serverpackets;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.model.TradeList;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;

/**
 * This class ...
 * @version $Revision: 1.2.2.3.2.6 $ $Date: 2005/03/27 15:29:57 $
 */
public class PrivateStoreListSell extends L2GameServerPacket
{
	private final L2PcInstance _storePlayer;
	private final L2PcInstance _activeChar;
	private int _playerAdena;
	private final boolean _packageSale;
	private final TradeList.TradeItem[] _items;
	
	// player's private shop
	public PrivateStoreListSell(L2PcInstance player, L2PcInstance storePlayer)
	{
		_activeChar = player;
		_storePlayer = storePlayer;
		
		if (Config.SELL_BY_ITEM)
		{
			final CreatureSay cs11 = new CreatureSay(0, 15, "", "ATTENTION: Store System is not based on Adena, be careful!"); // 8D
			_activeChar.sendPacket(cs11);
			_playerAdena = _activeChar.getItemCount(Config.SELL_ITEM, -1);
		}
		else
		{
			_playerAdena = _activeChar.getAdena();
		}
		
		_storePlayer.getSellList().updateItems();
		_items = _storePlayer.getSellList().getItems();
		_packageSale = _storePlayer.getSellList().isPackaged();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x9b);
		writeD(_storePlayer.getObjectId());
		writeD(_packageSale ? 1 : 0);
		writeD(_playerAdena);
		
		writeD(_items.length);
		for (TradeList.TradeItem item : _items)
		{
			writeD(item.getItem().getType2());
			writeD(item.getObjectId());
			writeD(item.getItem().getItemId());
			writeD(item.getCount());
			writeH(0x00);
			writeH(item.getEnchant());
			writeH(0x00);
			writeD(item.getItem().getBodyPart());
			writeD(item.getPrice()); // your price
			writeD(item.getItem().getReferencePrice()); // store price
		}
	}
}