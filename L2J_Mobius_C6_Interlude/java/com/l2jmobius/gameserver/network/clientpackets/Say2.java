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

import java.nio.BufferUnderflowException;
import java.util.Collection;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.datatables.csv.MapRegionTable;
import com.l2jmobius.gameserver.handler.IVoicedCommandHandler;
import com.l2jmobius.gameserver.handler.VoicedCommandHandler;
import com.l2jmobius.gameserver.instancemanager.PetitionManager;
import com.l2jmobius.gameserver.model.L2Object;
import com.l2jmobius.gameserver.model.L2World;
import com.l2jmobius.gameserver.model.actor.L2Character;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance.PunishLevel;
import com.l2jmobius.gameserver.network.SystemChatChannelId;
import com.l2jmobius.gameserver.network.SystemMessageId;
import com.l2jmobius.gameserver.network.serverpackets.CreatureSay;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import com.l2jmobius.gameserver.util.Util;

public final class Say2 extends L2GameClientPacket
{
	private static Logger LOGGER = Logger.getLogger(Say2.class.getName());
	private static java.util.logging.Logger _logChat = java.util.logging.Logger.getLogger("chat");
	
	public static final int ALL = 0;
	public static final int SHOUT = 1; // !
	public static final int TELL = 2;
	public static final int PARTY = 3; // #
	public static final int CLAN = 4; // @
	public static final int GM = 5; // //gmchat
	public static final int PETITION_PLAYER = 6; // used for petition
	public static final int PETITION_GM = 7; // * used for petition
	public static final int TRADE = 8; // +
	public static final int ALLIANCE = 9; // $
	public static final int ANNOUNCEMENT = 10; // //announce
	public static final int PARTYROOM_ALL = 16; // (Red)
	public static final int PARTYROOM_COMMANDER = 15; // (Yellow)
	public static final int HERO_VOICE = 17; // %
	public static final int CRITICAL_ANNOUNCE = 18;
	
	private static final String[] CHAT_NAMES =
	{
		"ALL  ",
		"SHOUT",
		"TELL ",
		"PARTY",
		"CLAN ",
		"GM   ",
		"PETITION_PLAYER",
		"PETITION_GM",
		"TRADE",
		"ALLIANCE",
		"ANNOUNCEMENT", // 10
		"WILLCRASHCLIENT:)",
		"FAKEALL?",
		"FAKEALL?",
		"FAKEALL?",
		"PARTYROOM_ALL",
		"PARTYROOM_COMMANDER",
		"CRITICAL_ANNOUNCE",
		"HERO_VOICE"
	};
	
	private String _text;
	private int _type;
	private SystemChatChannelId _type2Check;
	private String _target;
	
	@Override
	protected void readImpl()
	{
		_text = readS();
		try
		{
			_type = readD();
			_type2Check = SystemChatChannelId.getChatType(_type);
		}
		catch (BufferUnderflowException e)
		{
			_type = CHAT_NAMES.length;
			_type2Check = SystemChatChannelId.CHAT_NONE;
		}
		_target = _type == TELL ? readS() : null;
	}
	
	@Override
	protected void runImpl()
	{
		if (Config.DEBUG)
		{
			LOGGER.info("Say2: Msg Type = '" + _type + "' Text = '" + _text + "'.");
		}
		
		if ((_type < 0) || (_type >= CHAT_NAMES.length))
		{
			LOGGER.warning("Say2: Invalid type: " + _type);
			return;
		}
		
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		// Anti-PHX Announce
		if ((_type2Check == SystemChatChannelId.CHAT_NONE) || (_type2Check == SystemChatChannelId.CHAT_ANNOUNCE) || (_type2Check == SystemChatChannelId.CHAT_CRITICAL_ANNOUNCE) || (_type2Check == SystemChatChannelId.CHAT_SYSTEM) || (_type2Check == SystemChatChannelId.CHAT_CUSTOM) || ((_type2Check == SystemChatChannelId.CHAT_GM_PET) && !activeChar.isGM()))
		{
			LOGGER.warning("[Anti-PHX Announce] Illegal Chat ( " + _type2Check + " ) channel was used by character: [" + activeChar.getName() + "]");
			return;
		}
		
		if (activeChar == null)
		{
			LOGGER.warning("[Say2.java] Active Character is null.");
			return;
		}
		
		if (activeChar.isChatBanned() && !activeChar.isGM() && (_type != CLAN) && (_type != ALLIANCE) && (_type != PARTY))
		{
			activeChar.sendMessage("You may not chat while a chat ban is in effect.");
			return;
		}
		
		if (activeChar.isInJail() && Config.JAIL_DISABLE_CHAT)
		{
			if ((_type == TELL) || (_type == SHOUT) || (_type == TRADE) || (_type == HERO_VOICE))
			{
				activeChar.sendMessage("You can not chat with players outside of the jail.");
				return;
			}
		}
		
		if (!getClient().getFloodProtectors().getSayAction().tryPerformAction("Say2"))
		{
			activeChar.sendMessage("You cannot speak too fast.");
			return;
		}
		
		if (activeChar.isCursedWeaponEquiped() && ((_type == TRADE) || (_type == SHOUT)))
		{
			activeChar.sendMessage("Shout and trade chatting cannot be used while possessing a cursed weapon.");
			return;
		}
		
		if ((_type == PETITION_PLAYER) && activeChar.isGM())
		{
			_type = PETITION_GM;
		}
		
		if (_text.length() > Config.MAX_CHAT_LENGTH)
		{
			if (Config.DEBUG)
			{
				LOGGER.info("Say2: Msg Type = '" + _type + "' Text length more than " + Config.MAX_CHAT_LENGTH + " truncate them.");
			}
			_text = _text.substring(0, Config.MAX_CHAT_LENGTH);
			// return;
		}
		
		if (Config.LOG_CHAT)
		{
			final LogRecord record = new LogRecord(Level.INFO, _text);
			record.setLoggerName("chat");
			
			if (_type == TELL)
			{
				record.setParameters(new Object[]
				{
					CHAT_NAMES[_type],
					"[" + activeChar.getName() + " to " + _target + "]"
				});
			}
			else
			{
				record.setParameters(new Object[]
				{
					CHAT_NAMES[_type],
					"[" + activeChar.getName() + "]"
				});
			}
			
			_logChat.log(record);
		}
		
		if (Config.L2WALKER_PROTEC && (_type == TELL) && checkBot(_text))
		{
			Util.handleIllegalPlayerAction(activeChar, "Client Emulator Detect: Player " + activeChar.getName() + " using l2walker.", Config.DEFAULT_PUNISH);
			return;
		}
		_text = _text.replaceAll("\\\\n", "");
		
		// Say Filter implementation
		if (Config.USE_SAY_FILTER)
		{
			checkText(activeChar);
		}
		
		final L2Object saymode = activeChar.getSayMode();
		if (saymode != null)
		{
			final String name = saymode.getName();
			final int actor = saymode.getObjectId();
			_type = 0;
			final Collection<L2Object> list = saymode.getKnownList().getKnownObjects().values();
			
			final CreatureSay cs = new CreatureSay(actor, _type, name, _text);
			for (L2Object obj : list)
			{
				if ((obj == null) || !(obj instanceof L2Character))
				{
					continue;
				}
				final L2Character chara = (L2Character) obj;
				chara.sendPacket(cs);
			}
			return;
		}
		
		final CreatureSay cs = new CreatureSay(activeChar.getObjectId(), _type, activeChar.getName(), _text);
		switch (_type)
		{
			case TELL:
			{
				final L2PcInstance receiver = L2World.getInstance().getPlayer(_target);
				if (receiver == null)
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.S1_IS_NOT_ONLINE);
					sm.addString(_target);
					activeChar.sendPacket(sm);
					return;
				}
				if (!receiver.getBlockList().isInBlockList(activeChar) || activeChar.isGM())
				{
					if (receiver.isAway())
					{
						activeChar.sendMessage("Player is Away try again later.");
					}
					if (Config.JAIL_DISABLE_CHAT && receiver.isInJail())
					{
						activeChar.sendMessage("Player is in jail.");
						return;
					}
					if (receiver.isChatBanned() && !activeChar.isGM())
					{
						activeChar.sendMessage("Player is chat banned.");
						return;
					}
					if (receiver.isInOfflineMode())
					{
						activeChar.sendMessage("Player is in offline mode.");
						return;
					}
					if (!receiver.isInRefusalMode())
					{
						receiver.sendPacket(cs);
						activeChar.sendPacket(new CreatureSay(activeChar.getObjectId(), _type, "->" + receiver.getName(), _text));
					}
					else
					{
						activeChar.sendPacket(SystemMessageId.THE_PERSON_IS_IN_MESSAGE_REFUSAL_MODE);
					}
				}
				else if (receiver.getBlockList().isInBlockList(activeChar))
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_ADDED_YOU_TO_IGNORE_LIST);
					sm.addString(_target);
					activeChar.sendPacket(sm);
				}
				break;
			}
			case SHOUT:
			{
				// Flood protect Say
				if (!getClient().getFloodProtectors().getGlobalChat().tryPerformAction("global chat"))
				{
					return;
				}
				if (Config.DEFAULT_GLOBAL_CHAT.equalsIgnoreCase("on") || (Config.DEFAULT_GLOBAL_CHAT.equalsIgnoreCase("gm") && activeChar.isGM()))
				{
					if (Config.GLOBAL_CHAT_WITH_PVP)
					{
						if ((activeChar.getPvpKills() < Config.GLOBAL_PVP_AMOUNT) && !activeChar.isGM())
						{
							activeChar.sendMessage("You must have at least " + Config.GLOBAL_PVP_AMOUNT + " pvp kills in order to speak in global chat");
							return;
						}
						final int region = MapRegionTable.getInstance().getMapRegion(activeChar.getX(), activeChar.getY());
						for (L2PcInstance player : L2World.getInstance().getAllPlayers())
						{
							if (region == MapRegionTable.getInstance().getMapRegion(player.getX(), player.getY()))
							{
								// Like L2OFF if player is blocked can't read the message
								if (!player.getBlockList().isInBlockList(activeChar))
								{
									player.sendPacket(cs);
								}
							}
						}
					}
					else
					{
						final int region = MapRegionTable.getInstance().getMapRegion(activeChar.getX(), activeChar.getY());
						for (L2PcInstance player : L2World.getInstance().getAllPlayers())
						{
							if (region == MapRegionTable.getInstance().getMapRegion(player.getX(), player.getY()))
							{
								// Like L2OFF if player is blocked can't read the message
								if (!player.getBlockList().isInBlockList(activeChar))
								{
									player.sendPacket(cs);
								}
							}
						}
					}
				}
				else if (Config.DEFAULT_GLOBAL_CHAT.equalsIgnoreCase("GLOBAL"))
				{
					if (Config.GLOBAL_CHAT_WITH_PVP)
					{
						if ((activeChar.getPvpKills() < Config.GLOBAL_PVP_AMOUNT) && !activeChar.isGM())
						{
							activeChar.sendMessage("You must have at least " + Config.GLOBAL_PVP_AMOUNT + " pvp kills in order to speak in global chat");
							return;
						}
						for (L2PcInstance player : L2World.getInstance().getAllPlayers())
						{
							// Like L2OFF if player is blocked can't read the message
							if (!player.getBlockList().isInBlockList(activeChar))
							{
								player.sendPacket(cs);
							}
						}
					}
					else
					{
						for (L2PcInstance player : L2World.getInstance().getAllPlayers())
						{
							// Like L2OFF if player is blocked can't read the message
							if (!player.getBlockList().isInBlockList(activeChar))
							{
								player.sendPacket(cs);
							}
						}
					}
				}
				break;
			}
			case TRADE:
			{
				if (Config.DEFAULT_TRADE_CHAT.equalsIgnoreCase("ON"))
				{
					if (Config.TRADE_CHAT_WITH_PVP)
					{
						if ((activeChar.getPvpKills() <= Config.TRADE_PVP_AMOUNT) && !activeChar.isGM())
						{
							activeChar.sendMessage("You must have at least " + Config.TRADE_PVP_AMOUNT + "  pvp kills in order to speak in trade chat");
							return;
						}
						for (L2PcInstance player : L2World.getInstance().getAllPlayers())
						{
							// Like L2OFF if player is blocked can't read the message
							if (!player.getBlockList().isInBlockList(activeChar))
							{
								player.sendPacket(cs);
							}
						}
					}
					else
					{
						for (L2PcInstance player : L2World.getInstance().getAllPlayers())
						{
							// Like L2OFF if player is blocked can't read the message
							if (!player.getBlockList().isInBlockList(activeChar))
							{
								player.sendPacket(cs);
							}
						}
					}
				}
				else if (Config.DEFAULT_TRADE_CHAT.equalsIgnoreCase("limited"))
				{
					if (Config.TRADE_CHAT_WITH_PVP)
					{
						if ((activeChar.getPvpKills() <= Config.TRADE_PVP_AMOUNT) && !activeChar.isGM())
						{
							activeChar.sendMessage("You must have at least " + Config.TRADE_PVP_AMOUNT + "  pvp kills in order to speak in trade chat");
							return;
						}
						final int region = MapRegionTable.getInstance().getMapRegion(activeChar.getX(), activeChar.getY());
						for (L2PcInstance player : L2World.getInstance().getAllPlayers())
						{
							if (region == MapRegionTable.getInstance().getMapRegion(player.getX(), player.getY()))
							{
								// Like L2OFF if player is blocked can't read the message
								if (!player.getBlockList().isInBlockList(activeChar))
								{
									player.sendPacket(cs);
								}
							}
						}
					}
					else if (Config.TRADE_CHAT_IS_NOOBLE)
					{
						if (!activeChar.isNoble() && !activeChar.isGM())
						{
							activeChar.sendMessage("Only Nobless Players Can Use This Chat");
							return;
						}
						final int region = MapRegionTable.getInstance().getMapRegion(activeChar.getX(), activeChar.getY());
						for (L2PcInstance player : L2World.getInstance().getAllPlayers())
						{
							if (region == MapRegionTable.getInstance().getMapRegion(player.getX(), player.getY()))
							{
								// Like L2OFF if player is blocked can't read the message
								if (!player.getBlockList().isInBlockList(activeChar))
								{
									player.sendPacket(cs);
								}
							}
						}
					}
					else
					{
						final int region = MapRegionTable.getInstance().getMapRegion(activeChar.getX(), activeChar.getY());
						for (L2PcInstance player : L2World.getInstance().getAllPlayers())
						{
							if (region == MapRegionTable.getInstance().getMapRegion(player.getX(), player.getY()))
							{
								// Like L2OFF if player is blocked can't read the message
								if (!player.getBlockList().isInBlockList(activeChar))
								{
									player.sendPacket(cs);
								}
							}
						}
					}
				}
				break;
			}
			case ALL:
			{
				if (_text.startsWith("."))
				{
					final StringTokenizer st = new StringTokenizer(_text);
					IVoicedCommandHandler vch;
					String command = "";
					String target = "";
					if (st.countTokens() > 1)
					{
						command = st.nextToken().substring(1);
						target = _text.substring(command.length() + 2);
						vch = VoicedCommandHandler.getInstance().getVoicedCommandHandler(command);
					}
					else
					{
						command = _text.substring(1);
						if (Config.DEBUG)
						{
							LOGGER.info("Command: " + command);
						}
						vch = VoicedCommandHandler.getInstance().getVoicedCommandHandler(command);
					}
					if (vch != null)
					{
						vch.useVoicedCommand(command, activeChar, target);
						break;
					}
				}
				for (L2PcInstance player : activeChar.getKnownList().getKnownPlayers().values())
				{
					if ((player != null) && activeChar.isInsideRadius(player, 1250, false, true))
					{
						// Like L2OFF if player is blocked can't read the message
						if (!player.getBlockList().isInBlockList(activeChar))
						{
							player.sendPacket(cs);
						}
					}
				}
				activeChar.sendPacket(cs);
				break;
			}
			case CLAN:
			{
				if (activeChar.getClan() != null)
				{
					activeChar.getClan().broadcastToOnlineMembers(cs);
				}
				break;
			}
			case ALLIANCE:
			{
				if (activeChar.getClan() != null)
				{
					activeChar.getClan().broadcastToOnlineAllyMembers(cs);
				}
				break;
			}
			case PARTY:
			{
				if (activeChar.isInParty())
				{
					activeChar.getParty().broadcastToPartyMembers(cs);
				}
				break;
			}
			case PETITION_PLAYER:
			case PETITION_GM:
			{
				if (!PetitionManager.getInstance().isPlayerInConsultation(activeChar))
				{
					activeChar.sendPacket(SystemMessageId.YOU_ARE_NOT_IN_PETITION_CHAT);
					break;
				}
				PetitionManager.getInstance().sendActivePetitionMessage(activeChar, _text);
				break;
			}
			case PARTYROOM_ALL:
			{
				if (activeChar.isInParty())
				{
					if (activeChar.getParty().isInCommandChannel() && activeChar.getParty().isLeader(activeChar))
					{
						activeChar.getParty().getCommandChannel().broadcastCSToChannelMembers(cs, activeChar);
					}
				}
				break;
			}
			case PARTYROOM_COMMANDER:
			{
				if (activeChar.isInParty())
				{
					if (activeChar.getParty().isInCommandChannel() && activeChar.getParty().getCommandChannel().getChannelLeader().equals(activeChar))
					{
						activeChar.getParty().getCommandChannel().broadcastCSToChannelMembers(cs, activeChar);
					}
				}
				break;
			}
			case HERO_VOICE:
			{
				if (activeChar.isGM())
				{
					for (L2PcInstance player : L2World.getInstance().getAllPlayers())
					{
						if (player == null)
						{
							continue;
						}
						player.sendPacket(cs);
					}
				}
				else if (activeChar.isHero())
				{
					// Flood protect Hero Voice
					if (!getClient().getFloodProtectors().getHeroVoice().tryPerformAction("hero voice"))
					{
						return;
					}
					for (L2PcInstance player : L2World.getInstance().getAllPlayers())
					{
						if (player == null)
						{
							continue;
						}
						// Like L2OFF if player is blocked can't read the message
						if (!player.getBlockList().isInBlockList(activeChar))
						{
							player.sendPacket(cs);
						}
					}
				}
				break;
			}
		}
	}
	
	private static final String[] WALKER_COMMAND_LIST =
	{
		"USESKILL",
		"USEITEM",
		"BUYITEM",
		"SELLITEM",
		"SAVEITEM",
		"LOADITEM",
		"MSG",
		"SET",
		"DELAY",
		"LABEL",
		"JMP",
		"CALL",
		"RETURN",
		"MOVETO",
		"NPCSEL",
		"NPCDLG",
		"DLGSEL",
		"CHARSTATUS",
		"POSOUTRANGE",
		"POSINRANGE",
		"GOHOME",
		"SAY",
		"EXIT",
		"PAUSE",
		"STRINDLG",
		"STRNOTINDLG",
		"CHANGEWAITTYPE",
		"FORCEATTACK",
		"ISMEMBER",
		"REQUESTJOINPARTY",
		"REQUESTOUTPARTY",
		"QUITPARTY",
		"MEMBERSTATUS",
		"CHARBUFFS",
		"ITEMCOUNT",
		"FOLLOWTELEPORT"
	};
	
	private boolean checkBot(String text)
	{
		for (String botCommand : WALKER_COMMAND_LIST)
		{
			if (text.startsWith(botCommand))
			{
				return true;
			}
		}
		return false;
	}
	
	private void checkText(L2PcInstance activeChar)
	{
		if (Config.USE_SAY_FILTER)
		{
			String filteredText = _text.toLowerCase();
			
			for (String pattern : Config.FILTER_LIST)
			{
				filteredText = filteredText.replaceAll("(?i)" + pattern, Config.CHAT_FILTER_CHARS);
			}
			
			if (!filteredText.equalsIgnoreCase(_text))
			{
				if (Config.CHAT_FILTER_PUNISHMENT.equalsIgnoreCase("chat"))
				{
					activeChar.setPunishLevel(PunishLevel.CHAT, Config.CHAT_FILTER_PUNISHMENT_PARAM1);
					activeChar.sendMessage("Administrator banned you chat from " + Config.CHAT_FILTER_PUNISHMENT_PARAM1 + " minutes");
				}
				else if (Config.CHAT_FILTER_PUNISHMENT.equalsIgnoreCase("karma"))
				{
					activeChar.setKarma(Config.CHAT_FILTER_PUNISHMENT_PARAM2);
					activeChar.sendMessage("You have get " + Config.CHAT_FILTER_PUNISHMENT_PARAM2 + " karma for bad words");
				}
				else if (Config.CHAT_FILTER_PUNISHMENT.equalsIgnoreCase("jail"))
				{
					activeChar.setPunishLevel(PunishLevel.JAIL, Config.CHAT_FILTER_PUNISHMENT_PARAM1);
				}
				activeChar.sendMessage("The word " + _text + " is not allowed!");
				_text = filteredText;
			}
		}
	}
}