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
package com.l2jmobius.gameserver.network.clientpackets;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.enums.PrivateStoreType;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.actor.request.EnchantItemAttributeRequest;
import com.l2jmobius.gameserver.model.items.instance.L2ItemInstance;
import com.l2jmobius.gameserver.network.SystemMessageId;
import com.l2jmobius.gameserver.network.serverpackets.ExChangeAttributeFail;
import com.l2jmobius.gameserver.network.serverpackets.ExChangeAttributeItemList;
import com.l2jmobius.gameserver.network.serverpackets.ExChangeAttributeOk;
import com.l2jmobius.gameserver.network.serverpackets.ExStorageMaxCount;
import com.l2jmobius.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import com.l2jmobius.gameserver.network.serverpackets.UserInfo;
import com.l2jmobius.util.Rnd;

/**
 * @author Erlandys
 */
public class RequestChangeAttributeItem extends L2GameClientPacket
{
	private static final String _C__D0_B7_SENDCHANGEATTRIBUTETARGETITEM = "[C] D0:B7 SendChangeAttributeTargetItem";
	
	private int _attributeOID;
	private int _itemOID;
	private int _newAttributeID;
	
	@Override
	protected void readImpl()
	{
		_attributeOID = readD();
		_itemOID = readD();
		_newAttributeID = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		
		final EnchantItemAttributeRequest request = player.getRequest(EnchantItemAttributeRequest.class);
		if (request == null)
		{
			return;
		}
		request.setProcessing(true);
		
		final L2ItemInstance item = player.getInventory().getItemByObjectId(_itemOID);
		
		if (player.getPrivateStoreType() != PrivateStoreType.NONE)
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_CHANGE_AN_ATTRIBUTE_WHILE_USING_A_PRIVATE_STORE_OR_WORKSHOP);
			player.removeRequest(request.getClass());
			return;
		}
		
		if (player.getActiveTradeList() != null)
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_CHANGE_ATTRIBUTES_WHILE_EXCHANGING);
			player.removeRequest(request.getClass());
			return;
		}
		
		if (!item.isWeapon())
		{
			player.removeRequest(request.getClass());
			player.sendPacket(new ExChangeAttributeItemList(player, _attributeOID));
			return;
		}
		
		if (_newAttributeID == -1)
		{
			player.removeRequest(request.getClass());
			player.sendPacket(new ExChangeAttributeItemList(player, _attributeOID));
			return;
		}
		final L2ItemInstance attribute = player.getInventory().getItemByObjectId(_attributeOID);
		player.getInventory().destroyItem("ChangingAttribute", _attributeOID, 1, player, null);
		
		if (Rnd.get(100) < Config.CHANGE_CHANCE_ELEMENT)
		{
			final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_S_S2_ATTRIBUTE_HAS_SUCCESSFULLY_CHANGED_TO_S3_ATTRIBUTE);
			sm.addItemName(item);
			sm.addElemental(item.getAttackElementType());
			sm.addElemental(_newAttributeID);
			
			item.changeAttribute((byte) _newAttributeID, item.getAttackElementPower());
			if (item.isEquipped())
			{
				item.updateElementAttrBonus(player);
			}
			
			player.sendPacket(sm);
			player.sendPacket(new ExChangeAttributeOk());
			player.sendPacket(new UserInfo(player));
		}
		else
		{
			player.sendPacket(new ExChangeAttributeFail());
			player.sendPacket(SystemMessageId.CHANGING_ATTRIBUTES_HAS_BEEN_FAILED);
		}
		
		// send packets
		player.sendPacket(new ExStorageMaxCount(player));
		final InventoryUpdate iu = new InventoryUpdate();
		iu.addModifiedItem(item);
		if (player.getInventory().getItemByObjectId(_attributeOID) == null)
		{
			iu.addRemovedItem(attribute);
		}
		else
		{
			iu.addModifiedItem(attribute);
		}
		player.sendPacket(iu);
		
		player.removeRequest(request.getClass());
	}
	
	@Override
	public String getType()
	{
		return _C__D0_B7_SENDCHANGEATTRIBUTETARGETITEM;
	}
}