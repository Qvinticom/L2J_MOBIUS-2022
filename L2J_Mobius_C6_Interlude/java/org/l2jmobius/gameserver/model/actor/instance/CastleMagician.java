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
package org.l2jmobius.gameserver.model.actor.instance;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.templates.NpcTemplate;
import org.l2jmobius.gameserver.model.effects.EffectType;
import org.l2jmobius.gameserver.model.sevensigns.SevenSigns;
import org.l2jmobius.gameserver.model.zone.ZoneId;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * The Class CastleMagician.
 * @author Kerberos | ZaKaX
 */
public class CastleMagician extends Npc
{
	protected static final int COND_ALL_FALSE = 0;
	protected static final int COND_BUSY_BECAUSE_OF_SIEGE = 1;
	protected static final int COND_OWNER = 2;
	
	/**
	 * Instantiates a new castle magician instance.
	 * @param objectId the object id
	 * @param template the template
	 */
	public CastleMagician(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void showChatWindow(Player player, int value)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		String filename = "data/html/castlemagician/magician-no.htm";
		
		final int condition = validateCondition(player);
		if (condition > COND_ALL_FALSE)
		{
			if (condition == COND_BUSY_BECAUSE_OF_SIEGE)
			{
				filename = "data/html/castlemagician/magician-busy.htm"; // Busy because of siege
			}
			else if (condition == COND_OWNER) // Clan owns castle
			{
				if (value == 0)
				{
					filename = "data/html/castlemagician/magician.htm";
				}
				else
				{
					filename = "data/html/castlemagician/magician-" + value + ".htm";
				}
			}
		}
		
		final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(html);
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if (command.startsWith("Chat"))
		{
			int val = 0;
			try
			{
				val = Integer.parseInt(command.substring(5));
			}
			catch (IndexOutOfBoundsException | NumberFormatException ioobe)
			{
			}
			showChatWindow(player, val);
		}
		else if (command.equals("gotoleader"))
		{
			if (player.getClan() != null)
			{
				final Player clanLeader = player.getClan().getLeader().getPlayer();
				if (clanLeader == null)
				{
					return;
				}
				
				if (clanLeader.getFirstEffect(EffectType.CLAN_GATE) != null)
				{
					if (!validateGateCondition(clanLeader, player))
					{
						return;
					}
					
					player.teleToLocation(clanLeader.getX(), clanLeader.getY(), clanLeader.getZ());
					return;
				}
				final String filename = "data/html/castlemagician/magician-nogate.htm";
				showChatWindow(player, filename);
			}
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
	
	/**
	 * Validate condition.
	 * @param player the player
	 * @return the int
	 */
	protected int validateCondition(Player player)
	{
		if ((getCastle() != null) && (getCastle().getCastleId() > 0) && (player.getClan() != null))
		{
			if (getCastle().getZone().isSiegeActive())
			{
				return COND_BUSY_BECAUSE_OF_SIEGE; // Busy because of siege
			}
			else if (getCastle().getOwnerId() == player.getClanId())
			{
				return COND_OWNER;
			}
		}
		return COND_ALL_FALSE;
	}
	
	/**
	 * Validate gate condition.
	 * @param clanLeader the clan leader
	 * @param player the player
	 * @return true, if successful
	 */
	private static final boolean validateGateCondition(Player clanLeader, Player player)
	{
		if (clanLeader.isAlikeDead() || clanLeader.isInStoreMode() || clanLeader.isRooted() || clanLeader.isInCombat() || clanLeader.isInOlympiadMode() || clanLeader.isFestivalParticipant() || clanLeader.inObserverMode() || clanLeader.isInsideZone(ZoneId.NO_SUMMON_FRIEND))
		{
			player.sendMessage("Couldn't teleport to clan leader. The requirements was not meet.");
			return false;
		}
		
		if (player.isIn7sDungeon())
		{
			final int targetCabal = SevenSigns.getInstance().getPlayerCabal(clanLeader);
			if (SevenSigns.getInstance().isSealValidationPeriod())
			{
				if (targetCabal != SevenSigns.getInstance().getCabalHighestScore())
				{
					player.sendMessage("Couldn't teleport to clan leader. The requirements was not meet.");
					return false;
				}
			}
			else if (targetCabal == SevenSigns.CABAL_NULL)
			{
				player.sendMessage("Couldn't teleport to clan leader. The requirements was not meet.");
				return false;
			}
		}
		
		return true;
	}
}