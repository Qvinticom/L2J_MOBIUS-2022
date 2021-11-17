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
package org.l2jmobius.gameserver.handler.voicedcommandhandlers;

import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.ai.CtrlIntention;
import org.l2jmobius.gameserver.data.SkillTable;
import org.l2jmobius.gameserver.handler.IVoicedCommandHandler;
import org.l2jmobius.gameserver.instancemanager.CastleManager;
import org.l2jmobius.gameserver.instancemanager.CoupleManager;
import org.l2jmobius.gameserver.instancemanager.GrandBossManager;
import org.l2jmobius.gameserver.model.Skill;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.zone.ZoneId;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ConfirmDlg;
import org.l2jmobius.gameserver.network.serverpackets.MagicSkillUse;
import org.l2jmobius.gameserver.network.serverpackets.SetupGauge;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.taskmanager.GameTimeTaskManager;
import org.l2jmobius.gameserver.util.Broadcast;

public class Wedding implements IVoicedCommandHandler
{
	protected static final Logger LOGGER = Logger.getLogger(Wedding.class.getName());
	
	private static final String[] VOICED_COMMANDS =
	{
		"divorce",
		"engage",
		"gotolove"
	};
	
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
		if (activeChar.isRegisteredOnEvent() || activeChar.isInOlympiadMode())
		{
			activeChar.sendMessage("Sorry, you have registered in an event.");
			return false;
		}
		
		if (command.startsWith("engage"))
		{
			return Engage(activeChar);
		}
		else if (command.startsWith("divorce"))
		{
			return Divorce(activeChar);
		}
		else if (command.startsWith("gotolove"))
		{
			return GoToLove(activeChar);
		}
		return false;
	}
	
	public boolean Divorce(Player activeChar)
	{
		if (activeChar.getPartnerId() == 0)
		{
			return false;
		}
		
		final int partnerId = activeChar.getPartnerId();
		final int coupleId = activeChar.getCoupleId();
		int adenaAmount = 0;
		if (activeChar.isMarried())
		{
			activeChar.sendMessage("You are now divorced.");
			adenaAmount = (activeChar.getAdena() / 100) * Config.WEDDING_DIVORCE_COSTS;
			activeChar.getInventory().reduceAdena("Wedding", adenaAmount, activeChar, null);
		}
		else
		{
			activeChar.sendMessage("You have broken up as a couple.");
		}
		
		Player partner;
		partner = (Player) World.getInstance().findObject(partnerId);
		if (partner != null)
		{
			partner.setPartnerId(0);
			if (partner.isMarried())
			{
				partner.sendMessage("Your spouse has decided to divorce you.");
			}
			else
			{
				partner.sendMessage("Your fiance has decided to break the engagement with you.");
			}
			
			// give adena
			if (adenaAmount > 0)
			{
				partner.addAdena("WEDDING", adenaAmount, null, false);
			}
		}
		
		CoupleManager.getInstance().deleteCouple(coupleId);
		return true;
	}
	
	public boolean Engage(Player activeChar)
	{
		// check target
		if (activeChar.getTarget() == null)
		{
			activeChar.sendMessage("You have no one targeted.");
			return false;
		}
		
		// check if target is a l2pcinstance
		if (!(activeChar.getTarget() instanceof Player))
		{
			activeChar.sendMessage("You can only ask another player to engage you.");
			return false;
		}
		
		final Player ptarget = (Player) activeChar.getTarget();
		
		// check if player is already engaged
		if (activeChar.getPartnerId() != 0)
		{
			activeChar.sendMessage("You are already engaged.");
			if (Config.WEDDING_PUNISH_INFIDELITY)
			{
				activeChar.startAbnormalEffect((short) 0x2000); // give player a Big Head
				// lets recycle the sevensigns debuffs
				int skillId;
				int skillLevel = 1;
				if (activeChar.getLevel() > 40)
				{
					skillLevel = 2;
				}
				
				if (activeChar.isMageClass())
				{
					skillId = 4361;
				}
				else
				{
					skillId = 4362;
				}
				
				final Skill skill = SkillTable.getInstance().getSkill(skillId, skillLevel);
				if (activeChar.getFirstEffect(skill) == null)
				{
					skill.applyEffects(activeChar, activeChar, false, false, false);
					final SystemMessage sm = new SystemMessage(SystemMessageId.THE_EFFECTS_OF_S1_FLOW_THROUGH_YOU);
					sm.addSkillName(skillId);
					activeChar.sendPacket(sm);
				}
			}
			return false;
		}
		
		// check if player target himself
		if (ptarget.getObjectId() == activeChar.getObjectId())
		{
			activeChar.sendMessage("Is there something wrong with you, are you trying to go out with yourself?");
			return false;
		}
		
		if (ptarget.isMarried())
		{
			activeChar.sendMessage("Player already married.");
			return false;
		}
		
		if (ptarget.isEngageRequest())
		{
			activeChar.sendMessage("Player already asked by someone else.");
			return false;
		}
		
		if (ptarget.getPartnerId() != 0)
		{
			activeChar.sendMessage("Player already engaged with someone else.");
			return false;
		}
		
		if ((ptarget.getAppearance().isFemale() == activeChar.getAppearance().isFemale()) && !Config.WEDDING_SAMESEX)
		{
			activeChar.sendMessage("Gay marriage is not allowed on this server!");
			return false;
		}
		
		if (!activeChar.getFriendList().contains(ptarget.getObjectId()))
		{
			activeChar.sendMessage("The player you want to ask is not on your friends list, you must first be on each others friends list before you choose to engage.");
			return false;
		}
		
		ptarget.setEngageRequest(true, activeChar.getObjectId());
		final ConfirmDlg dlg = new ConfirmDlg(614);
		dlg.addString(activeChar.getName() + " asking you to engage. Do you want to start a new relationship?");
		ptarget.sendPacket(dlg);
		
		return true;
	}
	
	public boolean GoToLove(Player activeChar)
	{
		if (!activeChar.isMarried())
		{
			activeChar.sendMessage("You're not married.");
			return false;
		}
		
		// Check to see if the current player is in an event.
		if (activeChar.isOnEvent())
		{
			activeChar.sendMessage("You are in an event.");
			return false;
		}
		
		if (activeChar.getPartnerId() == 0)
		{
			activeChar.sendMessage("Couldn't find your fiance in the Database - Inform a Gamemaster.");
			LOGGER.warning("Married but couldn't find parter for " + activeChar.getName());
			return false;
		}
		
		if (GrandBossManager.getInstance().getZone(activeChar) != null)
		{
			activeChar.sendMessage("You're partener is in a Grand boss zone.");
			return false;
		}
		
		Player partner;
		partner = (Player) World.getInstance().findObject(activeChar.getPartnerId());
		if (partner == null)
		{
			activeChar.sendMessage("Your partner is not online.");
			return false;
		}
		else if (partner.isInJail())
		{
			activeChar.sendMessage("Your partner is in Jail.");
			return false;
		}
		else if (partner.isInOlympiadMode())
		{
			activeChar.sendMessage("Your partner is in the Olympiad now.");
			return false;
		}
		else if (partner.isOnEvent())
		{
			activeChar.sendMessage("Your partner is in an event.");
			return false;
		}
		else if (partner.isFestivalParticipant())
		{
			activeChar.sendMessage("Your partner is in a festival.");
			return false;
		}
		else if (GrandBossManager.getInstance().getZone(partner) != null)
		{
			activeChar.sendMessage("Your partner is inside a Boss Zone.");
			return false;
		}
		else if (partner.isInParty() && partner.getParty().isInDimensionalRift())
		{
			activeChar.sendMessage("Your partner is in dimensional rift.");
			return false;
		}
		else if (partner.inObserverMode())
		{
			activeChar.sendMessage("Your partner is in the observation.");
			return false;
		}
		else if ((partner.getClan() != null) && (CastleManager.getInstance().getCastleByOwner(partner.getClan()) != null) && CastleManager.getInstance().getCastleByOwner(partner.getClan()).getSiege().isInProgress())
		{
			activeChar.sendMessage("Your partner is in siege, you can't go to your partner.");
			return false;
		}
		else if (activeChar.isInJail())
		{
			activeChar.sendMessage("You are in Jail!");
			return false;
		}
		else if (activeChar.isInOlympiadMode())
		{
			activeChar.sendMessage("You are in the Olympiad now.");
			return false;
		}
		else if (activeChar.inObserverMode())
		{
			activeChar.sendMessage("You are in the observation.");
			return false;
		}
		else if ((activeChar.getClan() != null) && (CastleManager.getInstance().getCastleByOwner(activeChar.getClan()) != null) && CastleManager.getInstance().getCastleByOwner(activeChar.getClan()).getSiege().isInProgress())
		{
			activeChar.sendMessage("You are in siege, you can't go to your partner.");
			return false;
		}
		else if (activeChar.isFestivalParticipant())
		{
			activeChar.sendMessage("You are in a festival.");
			return false;
		}
		else if (activeChar.isInParty() && activeChar.getParty().isInDimensionalRift())
		{
			activeChar.sendMessage("You are in the dimensional rift.");
			return false;
		}
		else if (activeChar.isInsideZone(ZoneId.NO_SUMMON_FRIEND))
		{
			activeChar.sendMessage("You are in area which blocks summoning.");
			return false;
		}
		
		final int teleportTimer = Config.WEDDING_TELEPORT_DURATION * 1000;
		activeChar.sendMessage("After " + (teleportTimer / 60000) + " min. you will be teleported to your fiance.");
		activeChar.getInventory().reduceAdena("Wedding", Config.WEDDING_TELEPORT_PRICE, activeChar, null);
		activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		
		// SoE Animation section
		activeChar.setTarget(activeChar);
		activeChar.disableAllSkills();
		
		// Cast escape animation.
		Broadcast.toSelfAndKnownPlayersInRadius(activeChar, new MagicSkillUse(activeChar, activeChar, 1050, 1, teleportTimer, 0), 810000 /* 900 */);
		activeChar.sendPacket(new SetupGauge(0, teleportTimer));
		
		// Continue execution later.
		final EscapeFinalizer escapeFinalizer = new EscapeFinalizer(activeChar, partner.getX(), partner.getY(), partner.getZ(), partner.isIn7sDungeon());
		activeChar.setSkillCast(ThreadPool.schedule(escapeFinalizer, teleportTimer));
		activeChar.setSkillCastEndTime(10 + GameTimeTaskManager.getGameTicks() + (teleportTimer / GameTimeTaskManager.MILLIS_IN_TICK));
		return true;
	}
	
	static class EscapeFinalizer implements Runnable
	{
		private final Player _player;
		private final int _partnerx;
		private final int _partnery;
		private final int _partnerz;
		private final boolean _to7sDungeon;
		
		EscapeFinalizer(Player activeChar, int x, int y, int z, boolean to7sDungeon)
		{
			_player = activeChar;
			_partnerx = x;
			_partnery = y;
			_partnerz = z;
			_to7sDungeon = to7sDungeon;
		}
		
		@Override
		public void run()
		{
			if (_player.isDead())
			{
				return;
			}
			
			_player.setIn7sDungeon(_to7sDungeon);
			_player.enableAllSkills();
			
			try
			{
				_player.teleToLocation(_partnerx, _partnery, _partnerz);
			}
			catch (Throwable e)
			{
				LOGGER.warning(e.getMessage());
			}
		}
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}
