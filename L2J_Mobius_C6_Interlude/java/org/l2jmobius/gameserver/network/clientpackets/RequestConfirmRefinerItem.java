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

import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.items.ItemTemplate;
import org.l2jmobius.gameserver.model.items.instance.Item;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ExConfirmVariationRefiner;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * Fromat(ch) dd
 * @author -Wooden-
 */
public class RequestConfirmRefinerItem implements IClientIncomingPacket
{
	private static final int GEMSTONE_D = 2130;
	private static final int GEMSTONE_C = 2131;
	
	private int _targetItemObjId;
	private int _refinerItemObjId;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_targetItemObjId = packet.readD();
		_refinerItemObjId = packet.readD();
		return true;
	}
	
	@Override
	public void run(GameClient client)
	{
		final Player player = client.getPlayer();
		final Item targetItem = (Item) World.getInstance().findObject(_targetItemObjId);
		final Item refinerItem = (Item) World.getInstance().findObject(_refinerItemObjId);
		if ((targetItem == null) || (refinerItem == null))
		{
			return;
		}
		
		final int itemGrade = targetItem.getItem().getItemGrade();
		final int refinerItemId = refinerItem.getItem().getItemId();
		
		// is the item a life stone?
		if ((refinerItemId < 8723) || (refinerItemId > 8762))
		{
			player.sendPacket(SystemMessageId.THIS_IS_NOT_A_SUITABLE_ITEM);
			return;
		}
		
		int gemstoneCount = 0;
		int gemstoneItemId = 0;
		@SuppressWarnings("unused")
		final int lifeStoneLevel = getLifeStoneLevel(refinerItemId);
		final SystemMessage sm = new SystemMessage(SystemMessageId.REQUIRES_S2_S1);
		
		switch (itemGrade)
		{
			case ItemTemplate.CRYSTAL_C:
			{
				gemstoneCount = 20;
				gemstoneItemId = GEMSTONE_D;
				sm.addNumber(gemstoneCount);
				sm.addString("Gemstone D");
				break;
			}
			case ItemTemplate.CRYSTAL_B:
			{
				gemstoneCount = 30;
				gemstoneItemId = GEMSTONE_D;
				sm.addNumber(gemstoneCount);
				sm.addString("Gemstone D");
				break;
			}
			case ItemTemplate.CRYSTAL_A:
			{
				gemstoneCount = 20;
				gemstoneItemId = GEMSTONE_C;
				sm.addNumber(gemstoneCount);
				sm.addString("Gemstone C");
				break;
			}
			case ItemTemplate.CRYSTAL_S:
			{
				gemstoneCount = 25;
				gemstoneItemId = GEMSTONE_C;
				sm.addNumber(gemstoneCount);
				sm.addString("Gemstone C");
				break;
			}
		}
		
		player.sendPacket(new ExConfirmVariationRefiner(_refinerItemObjId, refinerItemId, gemstoneItemId, gemstoneCount));
		player.sendPacket(sm);
	}
	
	private int getLifeStoneGrade(int itemIdValue)
	{
		int itemId = itemIdValue - 8723;
		if (itemId < 10)
		{
			return 0; // normal grade
		}
		
		if (itemId < 20)
		{
			return 1; // mid grade
		}
		
		if (itemId < 30)
		{
			return 2; // high grade
		}
		
		return 3; // top grade
	}
	
	private int getLifeStoneLevel(int itemIdValue)
	{
		int itemId = itemIdValue - (10 * getLifeStoneGrade(itemIdValue));
		itemId -= 8722;
		return itemId;
	}
}