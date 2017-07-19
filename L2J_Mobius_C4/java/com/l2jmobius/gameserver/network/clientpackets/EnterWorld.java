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
import com.l2jmobius.gameserver.Announcements;
import com.l2jmobius.gameserver.Olympiad;
import com.l2jmobius.gameserver.SevenSigns;
import com.l2jmobius.gameserver.cache.HtmCache;
import com.l2jmobius.gameserver.communitybbs.Manager.RegionBBSManager;
import com.l2jmobius.gameserver.datatables.GmListTable;
import com.l2jmobius.gameserver.datatables.MapRegionTable;
import com.l2jmobius.gameserver.handler.AdminCommandHandler;
import com.l2jmobius.gameserver.instancemanager.ClanHallManager;
import com.l2jmobius.gameserver.instancemanager.PetitionManager;
import com.l2jmobius.gameserver.instancemanager.SiegeManager;
import com.l2jmobius.gameserver.model.L2Character;
import com.l2jmobius.gameserver.model.L2Clan;
import com.l2jmobius.gameserver.model.L2Effect;
import com.l2jmobius.gameserver.model.L2World;
import com.l2jmobius.gameserver.model.actor.instance.L2ClassMasterInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.entity.ClanHall;
import com.l2jmobius.gameserver.model.entity.L2Event;
import com.l2jmobius.gameserver.model.entity.Siege;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.network.serverpackets.Die;
import com.l2jmobius.gameserver.network.serverpackets.EtcStatusUpdate;
import com.l2jmobius.gameserver.network.serverpackets.ExStorageMaxCount;
import com.l2jmobius.gameserver.network.serverpackets.GameGuardQuery;
import com.l2jmobius.gameserver.network.serverpackets.HennaInfo;
import com.l2jmobius.gameserver.network.serverpackets.ItemList;
import com.l2jmobius.gameserver.network.serverpackets.MagicEffectIcons;
import com.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jmobius.gameserver.network.serverpackets.PledgeShowMemberListAll;
import com.l2jmobius.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import com.l2jmobius.gameserver.network.serverpackets.PledgeStatusChanged;
import com.l2jmobius.gameserver.network.serverpackets.QuestList;
import com.l2jmobius.gameserver.network.serverpackets.ShortCutInit;
import com.l2jmobius.gameserver.network.serverpackets.SignsSky;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import com.l2jmobius.gameserver.network.serverpackets.UserInfo;

/**
 * Enter World Packet Handler
 * <p>
 * <p>
 * 0000: 03
 * <p>
 * packet format rev656 cbdddd
 * <p>
 * @version $Revision: 1.16.2.1.2.7 $ $Date: 2005/03/29 23:15:33 $
 */
public class EnterWorld extends L2GameClientPacket
{
	private static final String _C__03_ENTERWORLD = "[C] 03 EnterWorld";
	private static Logger _log = Logger.getLogger(EnterWorld.class.getName());
	
	@Override
	protected void readImpl()
	{
	}
	
	@Override
	public void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			_log.warning("EnterWorld failed! activeChar is null...");
			getClient().closeNow();
			return;
		}
		
		if (L2World.getInstance().findObject(activeChar.getObjectId()) != null)
		{
			if (Config.DEBUG)
			{
				_log.warning("User already exist in OID map! User " + activeChar.getName() + " is a character clone");
			}
		}
		
		// Dual Box check
		if (Config.PREVENT_DUAL_BOXING)
		{
			// Allow dual boxing for GMs
			if (!activeChar.isGM())
			{
				// DualBox condition
				boolean dualBox = false;
				
				// Check all players inside world
				for (final L2PcInstance character : L2World.getInstance().getAllPlayers())
				{
					// Check if other client exists
					if (character.getClient() == null)
					{
						continue;
					}
					
					// Allow login if second character is offline
					if (character.inOfflineMode())
					{
						continue;
					}
					
					// Check if IPs are identical
					final String ip1 = getClient().getConnection().getInetAddress().getHostAddress();
					final String ip2 = character.getClient().getConnection().getInetAddress().getHostAddress();
					if ((ip1 != null) && (ip2 != null) && ip1.equals(ip2))
					{
						// Allow dual boxing for GMs
						if (character.isGM())
						{
							dualBox = false;
							break;
						}
						dualBox = true;
					}
				}
				
				// Now, kick logging character
				if (dualBox)
				{
					activeChar.logout();
					return;
				}
			}
		}
		
		if (activeChar.isGM())
		{
			if (Config.GM_STARTUP_INVULNERABLE && ((!Config.ALT_PRIVILEGES_ADMIN && (activeChar.getAccessLevel() >= Config.GM_GODMODE)) || (Config.ALT_PRIVILEGES_ADMIN && AdminCommandHandler.getInstance().checkPrivileges(activeChar, "admin_invul"))))
			{
				activeChar.setIsInvul(true);
			}
			
			if (Config.GM_STARTUP_INVISIBLE && ((!Config.ALT_PRIVILEGES_ADMIN && (activeChar.getAccessLevel() >= Config.GM_GODMODE)) || (Config.ALT_PRIVILEGES_ADMIN && AdminCommandHandler.getInstance().checkPrivileges(activeChar, "admin_invisible"))))
			{
				activeChar.getAppearance().setInvisible();
			}
			
			if (Config.GM_STARTUP_SILENCE && ((!Config.ALT_PRIVILEGES_ADMIN && (activeChar.getAccessLevel() >= Config.GM_MENU)) || (Config.ALT_PRIVILEGES_ADMIN && AdminCommandHandler.getInstance().checkPrivileges(activeChar, "admin_silence"))))
			{
				activeChar.setMessageRefusal(true);
			}
			
			if (Config.GM_STARTUP_AUTO_LIST && ((!Config.ALT_PRIVILEGES_ADMIN && (activeChar.getAccessLevel() >= Config.GM_MENU)) || (Config.ALT_PRIVILEGES_ADMIN && AdminCommandHandler.getInstance().checkPrivileges(activeChar, "admin_gmliston"))))
			{
				GmListTable.getInstance().addGm(activeChar, false);
			}
			else
			{
				GmListTable.getInstance().addGm(activeChar, true);
			}
			
			if (Config.GM_NAME_COLOR_ENABLED)
			{
				if (activeChar.getAccessLevel() >= 100)
				{
					activeChar.getAppearance().setNameColor(Config.ADMIN_NAME_COLOR);
				}
				else if (activeChar.getAccessLevel() >= 75)
				{
					activeChar.getAppearance().setNameColor(Config.GM_NAME_COLOR);
				}
			}
		}
		
		if (activeChar.getCurrentHp() < 0.5)
		{
			activeChar.setIsDead(true);
		}
		
		if (activeChar.getClan() != null)
		{
			if (activeChar.isClanLeader() && (activeChar.getClan().getLevel() > 3))
			{
				SiegeManager.getInstance().addSiegeSkills(activeChar);
			}
			
			for (final Siege siege : SiegeManager.getInstance().getSieges())
			{
				if (!siege.getIsInProgress())
				{
					continue;
				}
				if (siege.checkIsAttacker(activeChar.getClan()))
				{
					activeChar.setSiegeState((byte) 1);
				}
				if (siege.checkIsDefender(activeChar.getClan()))
				{
					activeChar.setSiegeState((byte) 2);
				}
			}
		}
		
		sendPacket(new UserInfo(activeChar));
		
		activeChar.getMacroses().sendUpdate();
		
		sendPacket(new ItemList(activeChar, false));
		
		sendPacket(new ShortCutInit(activeChar));
		
		sendPacket(new HennaInfo(activeChar));
		
		Quest.playerEnter(activeChar);
		
		activeChar.sendPacket(new QuestList());
		loadTutorial(activeChar);
		
		if (Config.PLAYER_SPAWN_PROTECTION > 0)
		{
			activeChar.setProtection(true);
		}
		
		activeChar.spawnMe(activeChar.getX(), activeChar.getY(), activeChar.getZ());
		
		if (L2Event.active && L2Event.connectionLossData.containsKey(activeChar.getName()) && L2Event.isOnEvent(activeChar))
		{
			L2Event.restoreChar(activeChar);
		}
		else if (L2Event.connectionLossData.containsKey(activeChar.getName()))
		{
			L2Event.restoreAndTeleChar(activeChar);
		}
		
		if (SevenSigns.getInstance().isSealValidationPeriod())
		{
			sendPacket(new SignsSky());
		}
		
		updateLoginEffectIcons(activeChar);
		
		activeChar.sendPacket(new EtcStatusUpdate(activeChar));
		
		// Expand Skill
		
		activeChar.sendPacket(new ExStorageMaxCount(activeChar));
		
		// Welcome to Lineage II
		sendPacket(new SystemMessage(34));
		
		SevenSigns.getInstance().sendCurrentPeriodMsg(activeChar);
		Announcements.getInstance().showAnnouncements(activeChar);
		
		final String serverNews = HtmCache.getInstance().getHtm("data/html/servnews.htm");
		if (serverNews != null)
		{
			final NpcHtmlMessage htmlMsg = new NpcHtmlMessage(0);
			htmlMsg.setHtml(serverNews);
			sendPacket(htmlMsg);
		}
		
		// just in case player gets disconnected
		L2ClassMasterInstance.showQuestionMark(activeChar);
		
		PetitionManager.getInstance().checkPetitionMessages(activeChar);
		
		if ((activeChar.getClanId() != 0) && (activeChar.getClan() != null))
		{
			sendPacket(new PledgeShowMemberListAll(activeChar.getClan(), activeChar));
			sendPacket(new PledgeStatusChanged(activeChar.getClan()));
		}
		
		notifyClanMembers(activeChar);
		
		activeChar.onPlayerEnter();
		
		if (Olympiad.getInstance().playerInStadium(activeChar))
		{
			activeChar.doRevive();
			if (!activeChar.isGM())
			{
				activeChar.sendMessage("You have been teleported to the nearest town due to being in an Olympiad Stadium.");
			}
		}
		
		if (activeChar.isAlikeDead())
		{
			// no broadcast needed since the player will already spawn dead to others
			sendPacket(new Die(activeChar));
		}
		
		if (!activeChar.isGM() && (activeChar.getSiegeState() < 2) && activeChar.isInsideZone(L2Character.ZONE_SIEGE))
		{
			activeChar.teleToLocation(MapRegionTable.TeleportWhereType.Town);
			activeChar.sendMessage("You have been teleported to the nearest town due to being in a siege zone.");
		}
		
		if (activeChar.getClanJoinExpiryTime() > System.currentTimeMillis())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.CLAN_MEMBERSHIP_TERMINATED));
		}
		
		if (activeChar.getClan() != null)
		{
			// Add message if clanHall not paid. Possibly this is custom...
			final ClanHall clanHall = ClanHallManager.getInstance().getClanHallByOwner(activeChar.getClan());
			if ((clanHall != null) && !clanHall.getPaid())
			{
				activeChar.sendPacket(new SystemMessage(SystemMessage.PAYMENT_FOR_YOUR_CLAN_HALL_HAS_NOT_BEEN_MADE_PLEASE_MAKE_PAYMENT_TO_YOUR_CLAN_WAREHOUSE_BY_S1_TOMORROW));
			}
		}
		
		RegionBBSManager.getInstance().changeCommunityBoard();
		
		if (Config.GAMEGUARD_ENFORCE)
		{
			activeChar.sendPacket(new GameGuardQuery());
		}
	}
	
	private void loadTutorial(L2PcInstance player)
	{
		final QuestState qs = player.getQuestState("255_Tutorial");
		if (qs != null)
		{
			qs.getQuest().notifyEvent("UC", null, player);
		}
	}
	
	/**
	 * @param activeChar
	 */
	private void notifyClanMembers(L2PcInstance activeChar)
	{
		final L2Clan clan = activeChar.getClan();
		if (clan != null)
		{
			clan.getClanMember(activeChar.getObjectId()).setPlayerInstance(activeChar);
			SystemMessage msg = new SystemMessage(SystemMessage.CLAN_MEMBER_S1_LOGGED_IN);
			msg.addString(activeChar.getName());
			
			clan.broadcastToOtherOnlineMembers(msg, activeChar);
			
			msg = null;
			
			clan.broadcastToOtherOnlineMembers(new PledgeShowMemberListUpdate(activeChar), activeChar);
		}
	}
	
	private void updateLoginEffectIcons(L2PcInstance activeChar)
	{
		final L2Effect[] effects = activeChar.getAllEffects();
		if ((effects != null) && (effects.length > 0))
		{
			final MagicEffectIcons mi = new MagicEffectIcons();
			for (final L2Effect e : activeChar.getAllEffects())
			{
				if (e == null)
				{
					continue;
				}
				
				if (e.getEffectType() == L2Effect.EffectType.HEAL_OVER_TIME)
				{
					e.exit();
				}
				else if (e.getEffectType() == L2Effect.EffectType.COMBAT_POINT_HEAL_OVER_TIME)
				{
					e.exit();
				}
				else if (e.getSkill().getId() == 4082)
				{
					e.exit();
				}
				else
				{
					if (e.getShowIcon() && e.getInUse())
					{
						e.addIcon(mi);
					}
				}
			}
			
			if (mi._effects.size() > 0)
			{
				activeChar.sendPacket(mi);
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.network.clientpackets.L2GameClientPacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__03_ENTERWORLD;
	}
}