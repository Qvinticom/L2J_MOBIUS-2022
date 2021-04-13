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

import java.util.ArrayList;
import java.util.List;

import org.l2jmobius.Config;
import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.instancemanager.CastleManager;
import org.l2jmobius.gameserver.instancemanager.CastleManorManager;
import org.l2jmobius.gameserver.instancemanager.CastleManorManager.CropProcure;
import org.l2jmobius.gameserver.network.GameClient;

/**
 * Format: (ch) dd [dddc] d - manor id d - size [ d - crop id d - sales d - price c - reward type ]
 * @author l3x
 */
public class RequestSetCrop implements IClientIncomingPacket
{
	private int _size;
	private int _manorId;
	private int[] _items; // _size*4
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_manorId = packet.readD();
		_size = packet.readD();
		if (((_size * 13) > packet.getReadableBytes()) || (_size > 500) || (_size < 1))
		{
			_size = 0;
			return false;
		}
		
		_items = new int[_size * 4];
		for (int i = 0; i < _size; i++)
		{
			final int itemId = packet.readD();
			_items[(i * 4) + 0] = itemId;
			final int sales = packet.readD();
			_items[(i * 4) + 1] = sales;
			final int price = packet.readD();
			_items[(i * 4) + 2] = price;
			final int type = packet.readC();
			_items[(i * 4) + 3] = type;
		}
		
		return true;
	}
	
	@Override
	public void run(GameClient client)
	{
		if (_size < 1)
		{
			return;
		}
		
		final List<CropProcure> crops = new ArrayList<>();
		for (int i = 0; i < _size; i++)
		{
			final int id = _items[(i * 4) + 0];
			final int sales = _items[(i * 4) + 1];
			final int price = _items[(i * 4) + 2];
			final int type = _items[(i * 4) + 3];
			if (id > 0)
			{
				final CropProcure s = CastleManorManager.getInstance().getNewCropProcure(id, sales, type, price, sales);
				crops.add(s);
			}
		}
		
		CastleManager.getInstance().getCastleById(_manorId).setCropProcure(crops, CastleManorManager.PERIOD_NEXT);
		if (Config.ALT_MANOR_SAVE_ALL_ACTIONS)
		{
			CastleManager.getInstance().getCastleById(_manorId).saveCropData(CastleManorManager.PERIOD_NEXT);
		}
	}
}
