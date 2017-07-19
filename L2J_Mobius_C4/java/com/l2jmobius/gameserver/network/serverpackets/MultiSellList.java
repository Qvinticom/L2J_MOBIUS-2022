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

import com.l2jmobius.gameserver.datatables.ItemTable;
import com.l2jmobius.gameserver.model.L2Multisell.MultiSellEntry;
import com.l2jmobius.gameserver.model.L2Multisell.MultiSellIngredient;
import com.l2jmobius.gameserver.model.L2Multisell.MultiSellListContainer;

/**
 * This class ...
 * @version $Revision: 1.2 $ $Date: 2004/06/27 08:12:59 $
 */
public class MultiSellList extends L2GameServerPacket
{
	private static final String _S__D0_MULTISELLLIST = "[S] D0 MultiSellList";
	
	protected int _page, _finished;
	protected MultiSellListContainer _list;
	
	public MultiSellList(MultiSellListContainer list, int page, int finished)
	{
		_list = list;
		
		_page = page;
		_finished = finished;
	}
	
	@Override
	protected final void writeImpl()
	{
		// [ddddd] [dchh] [hdhdh] [hhdh]
		
		writeC(0xd0);
		writeD(_list.getListId()); // list id
		writeD(_page); // page
		writeD(_finished); // finished
		writeD(0x28); // size of pages
		writeD(_list == null ? 0 : _list.getEntries().size()); // list length
		
		if (_list != null)
		{
			for (final MultiSellEntry ent : _list.getEntries())
			{
				writeD(ent.getEntryId());
				writeC(1);
				writeH(ent.getProducts().size());
				writeH(ent.getIngredients().size());
				
				for (final MultiSellIngredient i : ent.getProducts())
				{
					writeH(i.getItemId());
					writeD(ItemTable.getInstance().getTemplate(i.getItemId()).getBodyPart());
					writeH(ItemTable.getInstance().getTemplate(i.getItemId()).getType2());
					writeD((int) i.getItemCount());
					writeH(i.getEnchantmentLevel()); // enchant lvl
				}
				
				for (final MultiSellIngredient i : ent.getIngredients())
				{
					final int typeE = ItemTable.getInstance().getTemplate(i.getItemId()).getType2();
					writeH(i.getItemId()); // ID
					writeH(typeE);
					writeD((int) i.getItemCount()); // Count
					writeH(i.getEnchantmentLevel()); // Enchant Level
				}
			}
		}
	}
	
	@Override
	public String getType()
	{
		return _S__D0_MULTISELLLIST;
	}
}