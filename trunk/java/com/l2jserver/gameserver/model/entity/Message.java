/*
 * Copyright (C) 2004-2015 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jserver.gameserver.model.entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ScheduledFuture;

import com.l2jserver.gameserver.ThreadPoolManager;
import com.l2jserver.gameserver.data.sql.impl.CharNameTable;
import com.l2jserver.gameserver.enums.MailType;
import com.l2jserver.gameserver.idfactory.IdFactory;
import com.l2jserver.gameserver.instancemanager.MailManager;
import com.l2jserver.gameserver.model.itemcontainer.Mail;
import com.l2jserver.gameserver.model.items.instance.L2ItemInstance;
import com.l2jserver.util.Rnd;

/**
 * @author Migi, DS
 */
public class Message
{
	private static final int EXPIRATION = 360; // 15 days
	private static final int COD_EXPIRATION = 12; // 12 hours
	
	private static final int UNLOAD_ATTACHMENTS_INTERVAL = 900000; // 15-30 mins
	
	// post state
	public static final int DELETED = 0;
	public static final int READED = 1;
	public static final int REJECTED = 2;
	
	private final int _messageId, _senderId, _receiverId;
	private final long _expiration;
	private String _senderName = null;
	private String _receiverName = null;
	private final String _subject, _content;
	private boolean _unread, _returned;
	private MailType _messageType;
	private boolean _deletedBySender;
	private boolean _deletedByReceiver;
	private final long _reqAdena;
	private boolean _hasAttachments;
	private Mail _attachments = null;
	private ScheduledFuture<?> _unloadTask = null;
	private int _systemMessage1 = 0;
	private int _systemMessage2 = 0;
	
	private int _itemId;
	private int _enchantLvl;
	private final int[] _elementals = new int[6];
	
	/*
	 * Constructor for restoring from DB.
	 */
	public Message(ResultSet rset) throws SQLException
	{
		_messageId = rset.getInt("messageId");
		_senderId = rset.getInt("senderId");
		_receiverId = rset.getInt("receiverId");
		_subject = rset.getString("subject");
		_content = rset.getString("content");
		_expiration = rset.getLong("expiration");
		_reqAdena = rset.getLong("reqAdena");
		_hasAttachments = rset.getBoolean("hasAttachments");
		_unread = rset.getBoolean("isUnread");
		_deletedBySender = rset.getBoolean("isDeletedBySender");
		_deletedByReceiver = rset.getBoolean("isDeletedByReceiver");
		_messageType = MailType.values()[rset.getInt("sendBySystem")];
		_returned = rset.getBoolean("isReturned");
		_itemId = rset.getInt("itemId");
		_enchantLvl = rset.getInt("enchantLvl");
		final String elemental = rset.getString("elementals");
		if (elemental != null)
		{
			final String[] elemDef = elemental.split(";");
			for (int i = 0; i < 6; i++)
			{
				_elementals[i] = Integer.parseInt(elemDef[i]);
			}
		}
		_systemMessage1 = rset.getInt("systemMessage1");
		_systemMessage2 = rset.getInt("systemMessage2");
	}
	
	/*
	 * This constructor used for creating new message.
	 */
	public Message(int senderId, int receiverId, boolean isCod, String subject, String text, long reqAdena)
	{
		_messageId = IdFactory.getInstance().getNextId();
		_senderId = senderId;
		_receiverId = receiverId;
		_subject = subject;
		_content = text;
		_expiration = (isCod ? System.currentTimeMillis() + (COD_EXPIRATION * 3600000) : System.currentTimeMillis() + (EXPIRATION * 3600000));
		_hasAttachments = false;
		_unread = true;
		_deletedBySender = false;
		_deletedByReceiver = false;
		_reqAdena = reqAdena;
	}
	
	/*
	 * This constructor used for System Mails
	 */
	public Message(int receiverId, String subject, String content, int systemMessage1, int systemMessage2, MailType sendBySystem)
	{
		_messageId = IdFactory.getInstance().getNextId();
		_senderId = -1;
		_receiverId = receiverId;
		_subject = subject;
		_content = content;
		_expiration = System.currentTimeMillis() + (EXPIRATION * 3600000);
		_reqAdena = 0;
		_hasAttachments = false;
		_unread = true;
		_deletedBySender = true;
		_deletedByReceiver = false;
		_messageType = sendBySystem;
		_returned = false;
		_systemMessage1 = systemMessage1;
		_systemMessage2 = systemMessage2;
	}
	
	/*
	 * This constructor used for System Mails
	 */
	public Message(int receiverId, String subject, String content, MailType sendBySystem)
	{
		_messageId = IdFactory.getInstance().getNextId();
		_senderId = -1;
		_receiverId = receiverId;
		_subject = subject;
		_content = content;
		_expiration = System.currentTimeMillis() + (EXPIRATION * 3600000);
		_reqAdena = 0;
		_hasAttachments = false;
		_unread = true;
		_deletedBySender = true;
		_deletedByReceiver = false;
		_messageType = sendBySystem;
		_returned = false;
	}
	
	/*
	 * This constructor is used for creating new System message
	 */
	public Message(int senderId, int receiverId, String subject, String content, MailType sendBySystem)
	{
		_messageId = IdFactory.getInstance().getNextId();
		_senderId = senderId;
		_receiverId = receiverId;
		_subject = subject;
		_content = content;
		_expiration = System.currentTimeMillis() + (EXPIRATION * 3600000);
		_hasAttachments = false;
		_unread = true;
		_deletedBySender = true;
		_deletedByReceiver = false;
		_reqAdena = 0;
		_messageType = sendBySystem;
	}
	
	/*
	 * This constructor used for auto-generation of the "return attachments" message
	 */
	public Message(Message msg)
	{
		_messageId = IdFactory.getInstance().getNextId();
		_senderId = msg.getSenderId();
		_receiverId = msg.getSenderId();
		_subject = "";
		_content = "";
		_expiration = System.currentTimeMillis() + (EXPIRATION * 3600000);
		_unread = true;
		_deletedBySender = true;
		_deletedByReceiver = false;
		_messageType = MailType.REGULAR;
		_returned = true;
		_reqAdena = 0;
		_hasAttachments = true;
		_attachments = msg.getAttachments();
		msg.removeAttachments();
		_attachments.setNewMessageId(_messageId);
		_unloadTask = ThreadPoolManager.getInstance().scheduleGeneral(new AttachmentsUnloadTask(this), UNLOAD_ATTACHMENTS_INTERVAL + Rnd.get(UNLOAD_ATTACHMENTS_INTERVAL));
	}
	
	public Message(int receiverId, L2ItemInstance item, MailType mailType)
	{
		_messageId = IdFactory.getInstance().getNextId();
		_senderId = -1;
		_receiverId = receiverId;
		_subject = "";
		_content = item.getName();
		_expiration = System.currentTimeMillis() + (EXPIRATION * 3600000);
		_unread = true;
		_deletedBySender = true;
		_messageType = mailType;
		_returned = false;
		_reqAdena = 0;
		
		if (mailType == MailType.COMMISSION_ITEM_SOLD)
		{
			_hasAttachments = false;
			_itemId = item.getId();
			_enchantLvl = item.getEnchantLevel();
			if (item.isArmor())
			{
				for (int i = 0; i < 6; i++)
				{
					_elementals[i] = item.getElementDefAttr((byte) i);
				}
			}
			else if (item.isWeapon() && (item.getAttackElementType() >= 0))
			{
				_elementals[item.getAttackElementType()] = item.getAttackElementPower();
			}
		}
		else if (mailType == MailType.SYSTEM)
		{
			final Mail attachement = createAttachments();
			attachement.addItem("CommissionReturnItem", item, null, null);
		}
	}
	
	public static final PreparedStatement getStatement(Message msg, Connection con) throws SQLException
	{
		PreparedStatement stmt = con.prepareStatement("INSERT INTO messages (messageId, senderId, receiverId, subject, content, expiration, reqAdena, hasAttachments, isUnread, isDeletedBySender, isDeletedByReceiver, sendBySystem, isReturned, itemId, enchantLvl, elementals, systemMessage1, systemMessage2) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
		
		stmt.setInt(1, msg._messageId);
		stmt.setInt(2, msg._senderId);
		stmt.setInt(3, msg._receiverId);
		stmt.setString(4, msg._subject);
		stmt.setString(5, msg._content);
		stmt.setLong(6, msg._expiration);
		stmt.setLong(7, msg._reqAdena);
		stmt.setString(8, String.valueOf(msg._hasAttachments));
		stmt.setString(9, String.valueOf(msg._unread));
		stmt.setString(10, String.valueOf(msg._deletedBySender));
		stmt.setString(11, String.valueOf(msg._deletedByReceiver));
		stmt.setInt(12, msg._messageType.ordinal());
		stmt.setString(13, String.valueOf(msg._returned));
		stmt.setInt(14, msg._itemId);
		stmt.setInt(15, msg._enchantLvl);
		stmt.setString(16, msg._elementals[0] + ";" + msg._elementals[1] + ";" + msg._elementals[2] + ";" + msg._elementals[3] + ";" + msg._elementals[4] + ";" + msg._elementals[5]);
		stmt.setInt(17, msg._systemMessage1);
		stmt.setInt(18, msg._systemMessage2);
		
		return stmt;
	}
	
	public final int getId()
	{
		return _messageId;
	}
	
	public final int getSenderId()
	{
		return _senderId;
	}
	
	public final int getReceiverId()
	{
		return _receiverId;
	}
	
	public final String getSenderName()
	{
		switch (_messageType)
		{
			case REGULAR:
			{
				_senderName = CharNameTable.getInstance().getNameById(_senderId);
				break;
			}
			case PRIME_SHOP_GIFT: // Not in client, tbd
			{
				break;
			}
			case NEWS_INFORMER: // Handled by Sysstring in client
			case NPC: // Handled by NpcName in client
			case BIRTHDAY: // Handled by Sysstring in client
			case COMMISSION_ITEM_SOLD: // Handled by Sysstring in client
			case SYSTEM: // Handled by Sysstring in client
			case MENTOR_NPC: // Handled in client
			default:
			{
				break;
			}
		}
		return _senderName;
	}
	
	public final String getReceiverName()
	{
		if (_receiverName == null)
		{
			_receiverName = CharNameTable.getInstance().getNameById(_receiverId);
			if (_receiverName == null)
			{
				_receiverName = "";
			}
		}
		return _receiverName;
	}
	
	public final String getSubject()
	{
		return _subject;
	}
	
	public final String getContent()
	{
		return _content;
	}
	
	public final boolean isLocked()
	{
		return _reqAdena > 0;
	}
	
	public final long getExpiration()
	{
		return _expiration;
	}
	
	public final int getExpirationSeconds()
	{
		return (int) (_expiration / 1000);
	}
	
	public final boolean isUnread()
	{
		return _unread;
	}
	
	public final int getSystemMessage1()
	{
		return _systemMessage1;
	}
	
	public final int getSystemMessage2()
	{
		return _systemMessage2;
	}
	
	public final void markAsRead()
	{
		if (_unread)
		{
			_unread = false;
			MailManager.getInstance().markAsReadInDb(_messageId);
		}
	}
	
	public final boolean isDeletedBySender()
	{
		return _deletedBySender;
	}
	
	public final void setDeletedBySender()
	{
		if (!_deletedBySender)
		{
			_deletedBySender = true;
			if (_deletedByReceiver)
			{
				MailManager.getInstance().deleteMessageInDb(_messageId);
			}
			else
			{
				MailManager.getInstance().markAsDeletedBySenderInDb(_messageId);
			}
		}
	}
	
	public final boolean isDeletedByReceiver()
	{
		return _deletedByReceiver;
	}
	
	public final void setDeletedByReceiver()
	{
		if (!_deletedByReceiver)
		{
			_deletedByReceiver = true;
			if (_deletedBySender)
			{
				MailManager.getInstance().deleteMessageInDb(_messageId);
			}
			else
			{
				MailManager.getInstance().markAsDeletedByReceiverInDb(_messageId);
			}
		}
	}
	
	public final MailType getMailType()
	{
		return _messageType;
	}
	
	public final boolean isReturned()
	{
		return _returned;
	}
	
	public final void setIsReturned(boolean val)
	{
		_returned = val;
	}
	
	public final long getReqAdena()
	{
		return _reqAdena;
	}
	
	public final synchronized Mail getAttachments()
	{
		if (!_hasAttachments)
		{
			return null;
		}
		
		if (_attachments == null)
		{
			_attachments = new Mail(_senderId, _messageId);
			_attachments.restore();
			_unloadTask = ThreadPoolManager.getInstance().scheduleGeneral(new AttachmentsUnloadTask(this), UNLOAD_ATTACHMENTS_INTERVAL + Rnd.get(UNLOAD_ATTACHMENTS_INTERVAL));
		}
		return _attachments;
	}
	
	public final boolean hasAttachments()
	{
		return _hasAttachments;
	}
	
	public int getItemId()
	{
		return _itemId;
	}
	
	public int getEnchantLvl()
	{
		return _enchantLvl;
	}
	
	public int[] getElementals()
	{
		return _elementals;
	}
	
	public final synchronized void removeAttachments()
	{
		if (_attachments != null)
		{
			_attachments = null;
			_hasAttachments = false;
			MailManager.getInstance().removeAttachmentsInDb(_messageId);
			if (_unloadTask != null)
			{
				_unloadTask.cancel(false);
			}
		}
	}
	
	public final synchronized Mail createAttachments()
	{
		if (_hasAttachments || (_attachments != null))
		{
			return null;
		}
		
		_attachments = new Mail(_senderId, _messageId);
		_hasAttachments = true;
		_unloadTask = ThreadPoolManager.getInstance().scheduleGeneral(new AttachmentsUnloadTask(this), UNLOAD_ATTACHMENTS_INTERVAL + Rnd.get(UNLOAD_ATTACHMENTS_INTERVAL));
		return _attachments;
	}
	
	protected final synchronized void unloadAttachments()
	{
		if (_attachments != null)
		{
			_attachments.deleteMe();
			_attachments = null;
		}
	}
	
	static class AttachmentsUnloadTask implements Runnable
	{
		private Message _msg;
		
		AttachmentsUnloadTask(Message msg)
		{
			_msg = msg;
		}
		
		@Override
		public void run()
		{
			if (_msg != null)
			{
				_msg.unloadAttachments();
				_msg = null;
			}
		}
	}
}
