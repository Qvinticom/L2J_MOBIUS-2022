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
package com.l2jserver.gameserver.network.serverpackets;

import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.items.instance.L2ItemInstance;

/**
 * @author Erlandys
 */
public class ExResponseCommissionItemList extends AbstractItemPacket
{
	private final L2PcInstance _player;
	
	public ExResponseCommissionItemList(L2PcInstance player)
	{
		_player = player;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0xF3);
		
		writeD(_player.getInventory().getSize(false));
		
		for (L2ItemInstance item : _player.getInventory().getItems())
		{
			if (!item.isSellable() || !item.isTradeable() || item.isEquipped() || (item.getId() == 57) || item.isQuestItem())
			{
				continue;
			}
			writeItem(item);
		}
	}
}
