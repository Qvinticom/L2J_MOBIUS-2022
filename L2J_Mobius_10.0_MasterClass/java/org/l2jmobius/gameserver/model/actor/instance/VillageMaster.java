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

import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.l2jmobius.Config;
import org.l2jmobius.commons.util.Chronos;
import org.l2jmobius.gameserver.data.sql.ClanTable;
import org.l2jmobius.gameserver.enums.InstanceType;
import org.l2jmobius.gameserver.instancemanager.CastleManager;
import org.l2jmobius.gameserver.instancemanager.FortManager;
import org.l2jmobius.gameserver.instancemanager.FortSiegeManager;
import org.l2jmobius.gameserver.instancemanager.SiegeManager;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.templates.NpcTemplate;
import org.l2jmobius.gameserver.model.clan.Clan;
import org.l2jmobius.gameserver.model.clan.ClanMember;
import org.l2jmobius.gameserver.model.siege.Castle;
import org.l2jmobius.gameserver.model.siege.Fort;
import org.l2jmobius.gameserver.model.zone.ZoneId;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.MagicSkillLaunched;
import org.l2jmobius.gameserver.network.serverpackets.MagicSkillUse;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * @version $Revision: 1.4.2.3.2.8 $ $Date: 2005/03/29 23:15:15 $
 */
public class VillageMaster extends Folk
{
	private static final Logger LOGGER = Logger.getLogger(VillageMaster.class.getName());
	
	public VillageMaster(NpcTemplate template)
	{
		super(template);
		setInstanceType(InstanceType.VillageMaster);
	}
	
	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		if (attacker.isMonster())
		{
			return true;
		}
		return super.isAutoAttackable(attacker);
	}
	
	@Override
	public String getHtmlPath(int npcId, int value, Player player)
	{
		String pom = "";
		if (value == 0)
		{
			pom = Integer.toString(npcId);
		}
		else
		{
			pom = npcId + "-" + value;
		}
		return "data/html/villagemaster/" + pom + ".htm";
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		final String[] commandStr = command.split(" ");
		final String actualCommand = commandStr[0]; // Get actual command
		String cmdParams = "";
		String cmdParams2 = "";
		if (commandStr.length >= 2)
		{
			cmdParams = commandStr[1];
		}
		if (commandStr.length >= 3)
		{
			cmdParams2 = commandStr[2];
		}
		
		if (actualCommand.equalsIgnoreCase("create_clan"))
		{
			if (cmdParams.isEmpty())
			{
				return;
			}
			
			if (!cmdParams2.isEmpty() || !isValidName(cmdParams))
			{
				player.sendPacket(SystemMessageId.CLAN_NAME_IS_INVALID);
				return;
			}
			
			ClanTable.getInstance().createClan(player, cmdParams);
		}
		else if (actualCommand.equalsIgnoreCase("create_ally"))
		{
			if (cmdParams.isEmpty())
			{
				return;
			}
			
			if (player.getClan() == null)
			{
				player.sendPacket(SystemMessageId.ONLY_CLAN_LEADERS_MAY_CREATE_ALLIANCES);
			}
			else
			{
				player.getClan().createAlly(player, cmdParams);
			}
		}
		else if (actualCommand.equalsIgnoreCase("dissolve_ally"))
		{
			player.getClan().dissolveAlly(player);
		}
		else if (actualCommand.equalsIgnoreCase("dissolve_clan"))
		{
			dissolveClan(player);
		}
		else if (actualCommand.equalsIgnoreCase("change_clan_leader"))
		{
			if (cmdParams.isEmpty())
			{
				return;
			}
			
			if (!player.isClanLeader())
			{
				player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
				return;
			}
			
			if (player.getName().equalsIgnoreCase(cmdParams))
			{
				return;
			}
			
			final Clan clan = player.getClan();
			final ClanMember member = clan.getClanMember(cmdParams);
			if (member == null)
			{
				final SystemMessage sm = new SystemMessage(SystemMessageId.S1_DOES_NOT_EXIST);
				sm.addString(cmdParams);
				player.sendPacket(sm);
				return;
			}
			
			if (!member.isOnline())
			{
				player.sendPacket(SystemMessageId.THAT_PLAYER_IS_NOT_CURRENTLY_ONLINE);
				return;
			}
			
			// To avoid clans with null clan leader, academy members shouldn't be eligible for clan leader.
			if (member.getPlayer().isAcademyMember())
			{
				player.sendPacket(SystemMessageId.THAT_PRIVILEGE_CANNOT_BE_GRANTED_TO_A_CLAN_ACADEMY_MEMBER);
				return;
			}
			
			if (Config.ALT_CLAN_LEADER_INSTANT_ACTIVATION)
			{
				clan.setNewLeader(member);
			}
			else
			{
				final NpcHtmlMessage msg = new NpcHtmlMessage(getObjectId());
				if (clan.getNewLeaderId() == 0)
				{
					clan.setNewLeaderId(member.getObjectId(), true);
					msg.setFile(player, "data/scripts/village_master/ClanMaster/9000-07-success.htm");
				}
				else
				{
					msg.setFile(player, "data/scripts/village_master/ClanMaster/9000-07-in-progress.htm");
				}
				player.sendPacket(msg);
			}
		}
		else if (actualCommand.equalsIgnoreCase("cancel_clan_leader_change"))
		{
			if (!player.isClanLeader())
			{
				player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
				return;
			}
			
			final Clan clan = player.getClan();
			final NpcHtmlMessage msg = new NpcHtmlMessage(getObjectId());
			if (clan.getNewLeaderId() != 0)
			{
				clan.setNewLeaderId(0, true);
				msg.setFile(player, "data/scripts/village_master/ClanMaster/9000-07-canceled.htm");
			}
			else
			{
				msg.setHtml("<html><body>You don't have clan leader delegation applications submitted yet!</body></html>");
			}
			
			player.sendPacket(msg);
		}
		else if (actualCommand.equalsIgnoreCase("recover_clan"))
		{
			recoverClan(player);
		}
		else if (actualCommand.equalsIgnoreCase("increase_clan_level"))
		{
			if (player.getClan().levelUpClan(player))
			{
				player.broadcastPacket(new MagicSkillUse(player, 5103, 1, 0, 0));
				player.broadcastPacket(new MagicSkillLaunched(player, 5103, 1));
			}
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
	
	private void dissolveClan(Player player)
	{
		if (!player.isClanLeader())
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}
		
		final Clan clan = player.getClan();
		if (clan.getAllyId() != 0)
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_DISPERSE_THE_CLANS_IN_YOUR_ALLIANCE);
			return;
		}
		if (clan.isAtWar())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_DISSOLVE_A_CLAN_WHILE_ENGAGED_IN_A_WAR);
			return;
		}
		if ((clan.getCastleId() != 0) || (clan.getHideoutId() != 0) || (clan.getFortId() != 0))
		{
			player.sendPacket(SystemMessageId.YOU_CAN_T_DISBAND_THE_CLAN_THAT_HAS_A_CLAN_HALL_OR_CASTLE);
			return;
		}
		
		for (Castle castle : CastleManager.getInstance().getCastles())
		{
			if (SiegeManager.getInstance().checkIsRegistered(clan, castle.getResidenceId()))
			{
				player.sendPacket(SystemMessageId.YOU_CANNOT_DISSOLVE_A_CLAN_DURING_A_SIEGE_OR_WHILE_PROTECTING_A_CASTLE);
				return;
			}
		}
		for (Fort fort : FortManager.getInstance().getForts())
		{
			if (FortSiegeManager.getInstance().checkIsRegistered(clan, fort.getResidenceId()))
			{
				player.sendPacket(SystemMessageId.YOU_CANNOT_DISSOLVE_A_CLAN_DURING_A_SIEGE_OR_WHILE_PROTECTING_A_CASTLE);
				return;
			}
		}
		
		if (player.isInsideZone(ZoneId.SIEGE))
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_DISSOLVE_A_CLAN_DURING_A_SIEGE_OR_WHILE_PROTECTING_A_CASTLE);
			return;
		}
		if (clan.getDissolvingExpiryTime() > Chronos.currentTimeMillis())
		{
			player.sendPacket(SystemMessageId.YOU_HAVE_ALREADY_REQUESTED_THE_DISSOLUTION_OF_YOUR_CLAN);
			return;
		}
		
		clan.setDissolvingExpiryTime(Chronos.currentTimeMillis() + (Config.ALT_CLAN_DISSOLVE_DAYS * 86400000)); // 24*60*60*1000 = 86400000
		clan.updateClanInDB();
		
		// The clan leader should take the XP penalty of a full death.
		player.calculateDeathExpPenalty(null);
		ClanTable.getInstance().scheduleRemoveClan(clan.getId());
	}
	
	private void recoverClan(Player player)
	{
		if (!player.isClanLeader())
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}
		
		final Clan clan = player.getClan();
		clan.setDissolvingExpiryTime(0);
		clan.updateClanInDB();
	}
	
	private static boolean isValidName(String name)
	{
		Pattern pattern;
		try
		{
			pattern = Pattern.compile(Config.CLAN_NAME_TEMPLATE);
		}
		catch (PatternSyntaxException e)
		{
			LOGGER.warning("ERROR: Wrong pattern for clan name!");
			pattern = Pattern.compile(".*");
		}
		return pattern.matcher(name).matches();
	}
}
