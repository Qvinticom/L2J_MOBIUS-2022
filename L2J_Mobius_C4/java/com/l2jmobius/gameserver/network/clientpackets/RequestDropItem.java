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
import com.l2jmobius.gameserver.datatables.GmListTable;
import com.l2jmobius.gameserver.model.GMAudit;
import com.l2jmobius.gameserver.model.L2ItemInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jmobius.gameserver.network.serverpackets.ItemList;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import com.l2jmobius.gameserver.templates.L2EtcItemType;
import com.l2jmobius.gameserver.templates.L2Item;
import com.l2jmobius.gameserver.util.IllegalPlayerAction;
import com.l2jmobius.gameserver.util.Util;

/**
 * This class ...
 * @version $Revision: 1.11.2.1.2.7 $ $Date: 2005/04/02 21:25:21 $
 */
public class RequestDropItem extends L2GameClientPacket
{
	private static final String _C__12_REQUESTDROPITEM = "[C] 12 RequestDropItem";
	private static Logger _log = Logger.getLogger(RequestDropItem.class.getName());
	
	private int _objectId;
	private int _count;
	private int _x;
	private int _y;
	private int _z;
	
	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_count = readD();
		_x = readD();
		_y = readD();
		_z = readD();
	}
	
	@Override
	public void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if (activeChar.isDead())
		{
			return;
		}
		
		if (activeChar.inObserverMode())
		{
			return;
		}
		
		// Flood protect drop to avoid packet lag
		if (!getClient().getFloodProtectors().getDropItem().tryPerformAction("drop item"))
		{
			return;
		}
		
		final L2ItemInstance item = activeChar.getInventory().getItemByObjectId(_objectId);
		
		if ((item == null) || (_count == 0) || !activeChar.validateItemManipulation(_objectId, "drop")
			
			|| (!Config.ALLOW_DISCARDITEM && !activeChar.isGM()) || !item.isDropable())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.CANNOT_DISCARD_THIS_ITEM));
			return;
		}
		
		if ((item.getItemType() == L2EtcItemType.QUEST) && !activeChar.isGM())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.CANNOT_DISCARD_THIS_ITEM));
			return;
		}
		
		if (_count > item.getCount())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.NOTHING_HAPPENED));
			return;
		}
		
		if (_count < 0)
		{
			Util.handleIllegalPlayerAction(activeChar, "[RequestDropItem] count <= 0! ban! oid: " + _objectId + " owner: " + activeChar.getName(), IllegalPlayerAction.PUNISH_KICK);
			return;
		}
		
		if (!item.isStackable() && (_count > 1))
		{
			Util.handleIllegalPlayerAction(activeChar, "[RequestDropItem] count > 1 but item is not stackable! ban! oid: " + _objectId + " owner: " + activeChar.getName(), IllegalPlayerAction.PUNISH_KICK);
			return;
		}
		
		if (Config.GM_DISABLE_TRANSACTION && (activeChar.getAccessLevel() >= Config.GM_TRANSACTION_MIN) && (activeChar.getAccessLevel() <= Config.GM_TRANSACTION_MAX))
		{
			activeChar.sendMessage("Transactions are disable for your Access Level");
			activeChar.sendPacket(new SystemMessage(SystemMessage.NOTHING_HAPPENED));
			return;
		}
		
		if (activeChar.isProcessingTransaction() || (activeChar.getPrivateStoreType() != 0))
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE));
			return;
		}
		
		if (activeChar.isFishing())
		{
			// You can't mount, dismount, break and drop items while fishing
			activeChar.sendPacket(new SystemMessage(SystemMessage.CANNOT_DO_WHILE_FISHING_2));
			return;
		}
		
		// Cannot discard an equipped item or an item that the skill is consumming
		if (activeChar.isCastingNow())
		{
			if (item.isEquipped() || ((activeChar.getCurrentSkill() != null) && (activeChar.getCurrentSkill().getSkill().getItemConsumeId() == item.getItemId())))
			
			{
				activeChar.sendPacket(new SystemMessage(SystemMessage.CANNOT_DISCARD_THIS_ITEM));
				
				return;
			}
		}
		
		if ((L2Item.TYPE2_QUEST == item.getItem().getType2()) && !activeChar.isGM())
		{
			if (Config.DEBUG)
			{
				_log.finest(activeChar.getObjectId() + ":player tried to drop quest item");
			}
			activeChar.sendPacket(new SystemMessage(SystemMessage.CANNOT_DISCARD_EXCHANGE_ITEM));
			
			return;
		}
		
		if (!activeChar.isInsideRadius(_x, _y, 150, false) || (Math.abs(_z - activeChar.getZ()) > 50))
		{
			if (Config.DEBUG)
			{
				_log.finest(activeChar.getObjectId() + ": trying to drop too far away");
			}
			activeChar.sendPacket(new SystemMessage(SystemMessage.CANNOT_DISCARD_DISTANCE_TOO_FAR));
			return;
		}
		
		if (Config.DEBUG)
		{
			_log.fine("requested drop item " + _objectId + "(" + item.getCount() + ") at " + _x + "/" + _y + "/" + _z);
		}
		
		if (item.isEquipped())
		{
			final L2ItemInstance[] unequiped = activeChar.getInventory().unEquipItemInBodySlotAndRecord(item.getItem().getBodyPart());
			final InventoryUpdate iu = new InventoryUpdate();
			for (final L2ItemInstance element : unequiped)
			{
				activeChar.checkSShotsMatch(null, element);
				
				iu.addModifiedItem(element);
			}
			activeChar.sendPacket(iu);
			activeChar.broadcastUserInfo();
			
			final ItemList il = new ItemList(activeChar, true);
			activeChar.sendPacket(il);
		}
		
		final L2ItemInstance dropedItem = activeChar.dropItem("Drop", _objectId, _count, _x, _y, _z, null, false, true);
		
		if (Config.DEBUG)
		{
			_log.fine("dropping " + _objectId + " item(" + _count + ") at: " + _x + " " + _y + " " + _z);
		}
		
		if (activeChar.isGM())
		{
			final String target = (activeChar.getTarget() != null ? activeChar.getTarget().getName() : "no-target");
			GMAudit.auditGMAction(activeChar.getName(), "drop", target, dropedItem.getItemId() + " - " + dropedItem.getItemName() + " - " + dropedItem.getObjectId());
		}
		
		if ((dropedItem != null) && (dropedItem.getItemId() == 57) && (dropedItem.getCount() >= 1000000))
		{
			final String msg = "Character (" + activeChar.getName() + ") has dropped (" + dropedItem.getCount() + ")adena at (" + _x + "," + _y + "," + _z + ")";
			_log.warning(msg);
			GmListTable.broadcastMessageToGMs(msg);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.network.clientpackets.L2GameClientPacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__12_REQUESTDROPITEM;
	}
}