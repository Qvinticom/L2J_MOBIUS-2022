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
package org.l2jmobius.gameserver.handler.admincommandhandlers;

import java.util.List;
import java.util.StringTokenizer;

import org.l2jmobius.gameserver.data.SkillTable;
import org.l2jmobius.gameserver.data.sql.SkillTreeTable;
import org.l2jmobius.gameserver.handler.IAdminCommandHandler;
import org.l2jmobius.gameserver.model.Skill;
import org.l2jmobius.gameserver.model.SkillLearn;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jmobius.gameserver.network.serverpackets.PledgeSkillList;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.util.BuilderUtil;

/**
 * This class handles following admin commands: - show_skills - remove_skills - skill_list - skill_index - add_skill - remove_skill - get_skills - reset_skills - give_all_skills - remove_all_skills - add_clan_skills
 * @version $Revision: 1.2.4.7 $ $Date: 2005/04/11 10:06:02 $
 */
public class AdminSkill implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_show_skills",
		"admin_remove_skills",
		"admin_skill_list",
		"admin_skill_index",
		"admin_add_skill",
		"admin_remove_skill",
		"admin_get_skills",
		"admin_reset_skills",
		"admin_give_all_skills",
		"admin_remove_all_skills",
		"admin_add_clan_skill"
	};
	
	private static Skill[] adminSkills;
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		if (command.equals("admin_show_skills"))
		{
			showMainPage(activeChar);
		}
		else if (command.startsWith("admin_remove_skills"))
		{
			try
			{
				final String val = command.substring(20);
				removeSkillsPage(activeChar, Integer.parseInt(val));
			}
			catch (StringIndexOutOfBoundsException e)
			{
			}
		}
		else if (command.startsWith("admin_skill_list"))
		{
			AdminHelpPage.showHelpPage(activeChar, "skills.htm");
		}
		else if (command.startsWith("admin_skill_index"))
		{
			try
			{
				final String val = command.substring(18);
				AdminHelpPage.showHelpPage(activeChar, "skills/" + val + ".htm");
			}
			catch (StringIndexOutOfBoundsException e)
			{
			}
		}
		else if (command.startsWith("admin_add_skill"))
		{
			try
			{
				final String val = command.substring(15);
				if ((activeChar == activeChar.getTarget()) || activeChar.getAccessLevel().isGm())
				{
					adminAddSkill(activeChar, val);
				}
			}
			catch (Exception e)
			{
				BuilderUtil.sendSysMessage(activeChar, "Usage: //add_skill <skill_id> <level>");
			}
		}
		else if (command.startsWith("admin_remove_skill"))
		{
			try
			{
				final String id = command.substring(19);
				final int idval = Integer.parseInt(id);
				if ((activeChar == activeChar.getTarget()) || activeChar.getAccessLevel().isGm())
				{
					adminRemoveSkill(activeChar, idval);
				}
			}
			catch (Exception e)
			{
				BuilderUtil.sendSysMessage(activeChar, "Usage: //remove_skill <skill_id>");
			}
		}
		else if (command.equals("admin_get_skills"))
		{
			adminGetSkills(activeChar);
		}
		else if (command.equals("admin_reset_skills"))
		{
			if ((activeChar == activeChar.getTarget()) || activeChar.getAccessLevel().isGm())
			{
				adminResetSkills(activeChar);
			}
		}
		else if (command.equals("admin_give_all_skills"))
		{
			if ((activeChar == activeChar.getTarget()) || activeChar.getAccessLevel().isGm())
			{
				adminGiveAllSkills(activeChar);
			}
		}
		
		else if (command.equals("admin_remove_all_skills"))
		{
			if (activeChar.getTarget() instanceof Player)
			{
				final Player player = (Player) activeChar.getTarget();
				for (Skill skill : player.getAllSkills())
				{
					player.removeSkill(skill);
				}
				
				BuilderUtil.sendSysMessage(activeChar, "You removed all skills from " + player.getName());
				player.sendMessage("Admin removed all skills from you.");
				player.sendSkillList();
			}
		}
		else if (command.startsWith("admin_add_clan_skill"))
		{
			try
			{
				final String[] val = command.split(" ");
				if ((activeChar == activeChar.getTarget()) || activeChar.getAccessLevel().isGm())
				{
					adminAddClanSkill(activeChar, Integer.parseInt(val[1]), Integer.parseInt(val[2]));
				}
			}
			catch (Exception e)
			{
				BuilderUtil.sendSysMessage(activeChar, "Usage: //add_clan_skill <skill_id> <level>");
			}
		}
		return true;
	}
	
	/**
	 * This function will give all the skills that the target can learn at his/her level
	 * @param activeChar the GM char
	 */
	private void adminGiveAllSkills(Player activeChar)
	{
		final WorldObject target = activeChar.getTarget();
		Player player = null;
		if (target instanceof Player)
		{
			player = (Player) target;
		}
		else
		{
			activeChar.sendPacket(SystemMessageId.INVALID_TARGET);
			return;
		}
		
		boolean countUnlearnable = true;
		int unLearnable = 0;
		int skillCounter = 0;
		List<SkillLearn> skills = SkillTreeTable.getInstance().getAvailableSkills(player, player.getClassId());
		
		while (skills.size() > unLearnable)
		{
			for (SkillLearn s : skills)
			{
				final Skill sk = SkillTable.getInstance().getSkill(s.getId(), s.getLevel());
				if ((sk == null) || !sk.getCanLearn(player.getClassId()))
				{
					if (countUnlearnable)
					{
						unLearnable++;
					}
					
					continue;
				}
				
				if (player.getSkillLevel(sk.getId()) == 0)
				{
					skillCounter++;
				}
				
				player.addSkill(sk, true);
			}
			
			countUnlearnable = false;
			skills = SkillTreeTable.getInstance().getAvailableSkills(player, player.getClassId());
		}
		
		// Notify player and admin
		player.sendMessage("A GM gave you " + skillCounter + " skills.");
		BuilderUtil.sendSysMessage(activeChar, "You gave " + skillCounter + " skills to " + player.getName());
		player.sendSkillList();
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	private void removeSkillsPage(Player activeChar, int pageValue)
	{
		// TODO: Externalize HTML
		final WorldObject target = activeChar.getTarget();
		Player player = null;
		if (target instanceof Player)
		{
			player = (Player) target;
		}
		else
		{
			activeChar.sendPacket(SystemMessageId.THAT_IS_THE_INCORRECT_TARGET);
			return;
		}
		
		final Skill[] skills = player.getAllSkills().toArray(new Skill[0]);
		final int maxSkillsPerPage = 10;
		int maxPages = skills.length / maxSkillsPerPage;
		if (skills.length > (maxSkillsPerPage * maxPages))
		{
			maxPages++;
		}
		
		int page = pageValue;
		if (page > maxPages)
		{
			page = maxPages;
		}
		
		final int SkillsStart = maxSkillsPerPage * page;
		int skillsEnd = skills.length;
		if ((skillsEnd - SkillsStart) > maxSkillsPerPage)
		{
			skillsEnd = SkillsStart + maxSkillsPerPage;
		}
		
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		final StringBuilder replyMSG = new StringBuilder("<html><body>");
		replyMSG.append("<table width=260><tr>");
		replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("<td width=180><center>Character Selection Menu</center></td>");
		replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_show_skills\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("</tr></table>");
		replyMSG.append("<br><br>");
		replyMSG.append("<center>Editing <font color=\"LEVEL\">" + player.getName() + "</font></center>");
		replyMSG.append("<br><table width=270><tr><td>Lv: " + player.getLevel() + " " + player.getTemplate().getClassName() + "</td></tr></table>");
		replyMSG.append("<br><table width=270><tr><td>Note: Dont forget that modifying players skills can</td></tr>");
		replyMSG.append("<tr><td>ruin the game...</td></tr></table>");
		replyMSG.append("<br><center>Click on the skill you wish to remove:</center>");
		replyMSG.append("<br>");
		String pages = "<center><table width=270><tr>";
		for (int x = 0; x < maxPages; x++)
		{
			final int pagenr = x + 1;
			pages += "<td><a action=\"bypass -h admin_remove_skills " + x + "\">Page " + pagenr + "</a></td>";
		}
		
		pages += "</tr></table></center>";
		replyMSG.append(pages);
		replyMSG.append("<br><table width=270>");
		replyMSG.append("<tr><td width=80>Name:</td><td width=60>Level:</td><td width=40>Id:</td></tr>");
		for (int i = SkillsStart; i < skillsEnd; i++)
		{
			replyMSG.append("<tr><td width=80><a action=\"bypass -h admin_remove_skill " + skills[i].getId() + "\">" + skills[i].getName() + "</a></td><td width=60>" + skills[i].getLevel() + "</td><td width=40>" + skills[i].getId() + "</td></tr>");
		}
		
		replyMSG.append("</table>");
		replyMSG.append("<br><center><table>");
		replyMSG.append("Remove skill by ID :");
		replyMSG.append("<tr><td>Id: </td>");
		replyMSG.append("<td><edit var=\"id_to_remove\" width=110></td></tr>");
		replyMSG.append("</table></center>");
		replyMSG.append("<center><button value=\"Remove skill\" action=\"bypass -h admin_remove_skill $id_to_remove\" width=110 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center>");
		replyMSG.append("<br><center><button value=\"Back\" action=\"bypass -h admin_current_player\" width=40 height=15></center>");
		replyMSG.append("</body></html>");
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}
	
	private void showMainPage(Player activeChar)
	{
		final WorldObject target = activeChar.getTarget();
		Player player = null;
		if (target instanceof Player)
		{
			player = (Player) target;
		}
		else
		{
			activeChar.sendPacket(SystemMessageId.INVALID_TARGET);
			return;
		}
		
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile("data/html/admin/charskills.htm");
		adminReply.replace("%name%", player.getName());
		adminReply.replace("%level%", String.valueOf(player.getLevel()));
		adminReply.replace("%class%", player.getTemplate().getClassName());
		activeChar.sendPacket(adminReply);
	}
	
	private void adminGetSkills(Player activeChar)
	{
		final WorldObject target = activeChar.getTarget();
		Player player = null;
		if (target instanceof Player)
		{
			player = (Player) target;
		}
		else
		{
			activeChar.sendPacket(SystemMessageId.INVALID_TARGET);
			return;
		}
		
		if (player.getName().equals(activeChar.getName()))
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_USE_THIS_ON_YOURSELF);
		}
		else
		{
			final Skill[] skills = player.getAllSkills().toArray(new Skill[0]);
			adminSkills = activeChar.getAllSkills().toArray(new Skill[0]);
			for (Skill adminSkill : adminSkills)
			{
				activeChar.removeSkill(adminSkill);
			}
			
			for (Skill skill : skills)
			{
				activeChar.addSkill(skill, true);
			}
			
			BuilderUtil.sendSysMessage(activeChar, "You now have all the skills of " + player.getName() + ".");
			activeChar.sendSkillList();
		}
		
		showMainPage(activeChar);
	}
	
	private void adminResetSkills(Player activeChar)
	{
		final WorldObject target = activeChar.getTarget();
		Player player = null;
		if (target instanceof Player)
		{
			player = (Player) target;
		}
		else
		{
			activeChar.sendPacket(SystemMessageId.INVALID_TARGET);
			return;
		}
		
		if (adminSkills == null)
		{
			BuilderUtil.sendSysMessage(activeChar, "You must get the skills of someone in order to do this.");
		}
		else
		{
			final Skill[] skills = player.getAllSkills().toArray(new Skill[0]);
			for (Skill skill : skills)
			{
				player.removeSkill(skill);
			}
			
			final Skill[] skills2 = activeChar.getAllSkills().toArray(new Skill[0]);
			for (Skill skill : skills2)
			{
				player.addSkill(skill, true);
			}
			
			for (Skill skill : skills)
			{
				activeChar.removeSkill(skill);
			}
			
			for (Skill adminSkill : adminSkills)
			{
				activeChar.addSkill(adminSkill, true);
			}
			
			player.sendMessage("[GM]" + activeChar.getName() + " updated your skills.");
			BuilderUtil.sendSysMessage(activeChar, "You now have all your skills back.");
			activeChar.sendSkillList();
		}
		
		showMainPage(activeChar);
	}
	
	private void adminAddSkill(Player activeChar, String value)
	{
		final WorldObject target = activeChar.getTarget();
		Player player = null;
		if (target instanceof Player)
		{
			if ((target == activeChar) || (activeChar.getAccessLevel().getLevel() > 70))
			{
				player = (Player) target;
			}
			else
			{
				showMainPage(activeChar);
				activeChar.sendPacket(SystemMessage.sendString("You have not right to add skills to other players"));
				return;
			}
		}
		else
		{
			showMainPage(activeChar);
			activeChar.sendPacket(SystemMessageId.INVALID_TARGET);
			return;
		}
		
		final StringTokenizer st = new StringTokenizer(value);
		if (st.countTokens() != 2)
		{
			showMainPage(activeChar);
		}
		else
		{
			Skill skill = null;
			
			try
			{
				final String id = st.nextToken();
				final String level = st.nextToken();
				final int idval = Integer.parseInt(id);
				final int levelval = Integer.parseInt(level);
				skill = SkillTable.getInstance().getSkill(idval, levelval);
			}
			catch (Exception e)
			{
			}
			
			if (skill != null)
			{
				final String name = skill.getName();
				player.sendMessage("Admin gave you the skill " + name + ".");
				player.addSkill(skill, true);
				// Admin information
				BuilderUtil.sendSysMessage(activeChar, "You gave the skill " + name + " to " + player.getName() + ".");
				activeChar.sendSkillList();
			}
			else
			{
				BuilderUtil.sendSysMessage(activeChar, "Error: there is no such skill.");
			}
			
			showMainPage(activeChar); // Back to start
		}
	}
	
	private void adminRemoveSkill(Player activeChar, int idval)
	{
		final WorldObject target = activeChar.getTarget();
		Player player = null;
		if (target instanceof Player)
		{
			player = (Player) target;
		}
		else
		{
			activeChar.sendPacket(SystemMessageId.INVALID_TARGET);
			return;
		}
		
		final Skill skill = SkillTable.getInstance().getSkill(idval, player.getSkillLevel(idval));
		if (skill != null)
		{
			final String skillname = skill.getName();
			player.sendMessage("Admin removed the skill " + skillname + " from your skills list.");
			player.removeSkill(skill);
			// Admin information
			BuilderUtil.sendSysMessage(activeChar, "You removed the skill " + skillname + " from " + player.getName() + ".");
			activeChar.sendSkillList();
		}
		else
		{
			BuilderUtil.sendSysMessage(activeChar, "Error: there is no such skill.");
		}
		
		// Back to previous page
		removeSkillsPage(activeChar, 0);
	}
	
	private void adminAddClanSkill(Player activeChar, int id, int level)
	{
		final WorldObject target = activeChar.getTarget();
		Player player = null;
		if (target instanceof Player)
		{
			player = (Player) target;
		}
		else
		{
			showMainPage(activeChar);
			activeChar.sendPacket(SystemMessageId.INVALID_TARGET);
			
			return;
		}
		
		if (!player.isClanLeader())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.S1_IS_NOT_A_CLAN_LEADER).addString(player.getName()));
			showMainPage(activeChar);
			
			return;
		}
		
		if ((id < 370) || (id > 391) || (level < 1) || (level > 3))
		{
			BuilderUtil.sendSysMessage(activeChar, "Usage: //add_clan_skill <skill_id> <level>");
			showMainPage(activeChar);
			
			return;
		}
		
		final Skill skill = SkillTable.getInstance().getSkill(id, level);
		if (skill != null)
		{
			final String skillname = skill.getName();
			final SystemMessage sm = new SystemMessage(SystemMessageId.THE_CLAN_SKILL_S1_HAS_BEEN_ADDED);
			sm.addSkillName(id);
			player.sendPacket(sm);
			player.getClan().broadcastToOnlineMembers(sm);
			player.getClan().addNewSkill(skill);
			BuilderUtil.sendSysMessage(activeChar, "You gave the Clan Skill: " + skillname + " to the clan " + player.getClan().getName() + ".");
			activeChar.getClan().broadcastToOnlineMembers(new PledgeSkillList(activeChar.getClan()));
			for (Player member : activeChar.getClan().getOnlineMembers())
			{
				member.sendSkillList();
			}
			
			showMainPage(activeChar);
			return;
		}
		BuilderUtil.sendSysMessage(activeChar, "Error: there is no such skill.");
	}
}
