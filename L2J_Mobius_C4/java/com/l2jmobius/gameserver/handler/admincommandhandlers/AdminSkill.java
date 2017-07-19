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
package com.l2jmobius.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;
import java.util.logging.Logger;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.datatables.SkillTable;
import com.l2jmobius.gameserver.datatables.SkillTreeTable;
import com.l2jmobius.gameserver.handler.IAdminCommandHandler;
import com.l2jmobius.gameserver.model.GMAudit;
import com.l2jmobius.gameserver.model.L2Object;
import com.l2jmobius.gameserver.model.L2Skill;
import com.l2jmobius.gameserver.model.L2SkillLearn;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import com.l2jmobius.gameserver.network.serverpackets.MagicSkillUse;
import com.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;

import javolution.text.TextBuilder;

/**
 * This class handles following admin commands: - show_skills - remove_skills - skill_list - skill_index - add_skill - remove_skill - get_skills - reset_skills - give_all_skills - remove_all_skills
 * @version $Revision: 1.2.4.7 $ $Date: 2005/04/11 10:06:02 $
 */
public class AdminSkill implements IAdminCommandHandler
{
	private static Logger _log = Logger.getLogger(AdminSkill.class.getName());
	
	private static String[] _adminCommands =
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
		"admin_remove_all_skills"
	};
	private static final int REQUIRED_LEVEL = Config.GM_CHAR_EDIT;
	private static final int REQUIRED_LEVEL2 = Config.GM_CHAR_EDIT_OTHER;
	
	private static L2Skill[] adminSkills;
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (!Config.ALT_PRIVILEGES_ADMIN)
		{
			if (!(checkLevel(activeChar.getAccessLevel()) && activeChar.isGM()))
			{
				return false;
			}
		}
		
		final String target = (activeChar.getTarget() != null ? activeChar.getTarget().getName() : "no-target");
		GMAudit.auditGMAction(activeChar.getName(), command, target, "");
		
		if (command.equals("admin_show_skills"))
		{
			showSkillsPage(activeChar);
		}
		else if (command.startsWith("admin_remove_skills"))
		{
			try
			{
				final String val = command.substring(20);
				removeSkillsPage(activeChar, Integer.parseInt(val));
			}
			catch (final StringIndexOutOfBoundsException e)
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
			catch (final StringIndexOutOfBoundsException e)
			{
			}
		}
		else if (command.startsWith("admin_add_skill"))
		{
			try
			{
				final String val = command.substring(15);
				if ((activeChar == activeChar.getTarget()) || (activeChar.getAccessLevel() >= REQUIRED_LEVEL2))
				{
					adminAddSkill(activeChar, val);
				}
			}
			catch (final StringIndexOutOfBoundsException e)
			{ // Case of empty character name
				final SystemMessage sm = new SystemMessage(614);
				sm.addString("Error while adding skill.");
				activeChar.sendPacket(sm);
			}
		}
		else if (command.startsWith("admin_remove_skill"))
		{
			try
			{
				final String id = command.substring(19);
				final int idval = Integer.parseInt(id);
				if ((activeChar == activeChar.getTarget()) || (activeChar.getAccessLevel() >= REQUIRED_LEVEL2))
				{
					adminRemoveSkill(activeChar, idval);
				}
			}
			catch (final StringIndexOutOfBoundsException e)
			{ // Case of empty character name
				final SystemMessage sm = new SystemMessage(614);
				sm.addString("Error while removing skill.");
				activeChar.sendPacket(sm);
			}
		}
		else if (command.equals("admin_get_skills"))
		{
			adminGetSkills(activeChar);
		}
		else if (command.equals("admin_reset_skills"))
		{
			if ((activeChar == activeChar.getTarget()) || (activeChar.getAccessLevel() >= REQUIRED_LEVEL2))
			{
				adminResetSkills(activeChar);
			}
		}
		else if (command.equals("admin_give_all_skills"))
		{
			if ((activeChar == activeChar.getTarget()) || (activeChar.getAccessLevel() >= REQUIRED_LEVEL2))
			{
				adminGiveAllSkills(activeChar);
			}
		}
		else if (command.equals("admin_remove_all_skills"))
		{
			if (activeChar.getTarget() instanceof L2PcInstance)
			{
				final L2PcInstance player = (L2PcInstance) activeChar.getTarget();
				for (final L2Skill skill : player.getAllSkills())
				{
					player.removeSkill(skill);
				}
				activeChar.sendMessage("You removed all skills from " + player.getName());
				player.sendMessage("Admin removed all skills from you.");
			}
		}
		return true;
	}
	
	/**
	 * This function will give all the skills that the gm target can have at its level to the traget
	 * @param activeChar : the gm char
	 */
	private void adminGiveAllSkills(L2PcInstance activeChar)
	{
		final L2Object target = activeChar.getTarget();
		if (target == null)
		{
			return;
		}
		
		L2PcInstance player = null;
		if (target instanceof L2PcInstance)
		{
			player = (L2PcInstance) target;
		}
		else
		{
			final SystemMessage sm = new SystemMessage(614);
			sm.addString("Incorrect target.");
			activeChar.sendPacket(sm);
			return;
		}
		
		int skillCounter = 0;
		
		final L2SkillLearn[] skills = SkillTreeTable.getInstance().getMaxAvailableSkills(player, player.getClassId());
		for (final L2SkillLearn s : skills)
		{
			final L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
			if ((sk == null) || !sk.getCanLearn(player.getClassId()))
			{
				continue;
			}
			
			if (player.getSkillLevel(sk.getId()) == -1)
			{
				skillCounter++;
			}
			
			player.addSkill(sk, true);
		}
		
		// Notify player and admin
		if (skillCounter > 0)
		{
			player.sendMessage("A GM gave you " + skillCounter + " skills.");
			activeChar.sendMessage("You gave " + skillCounter + " skills to " + player.getName());
		}
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return _adminCommands;
	}
	
	private boolean checkLevel(int level)
	{
		return (level >= REQUIRED_LEVEL);
	}
	
	// ok
	private void removeSkillsPage(L2PcInstance activeChar, int page)
	{
		final L2Object target = activeChar.getTarget();
		L2PcInstance player = null;
		if (target instanceof L2PcInstance)
		{
			player = (L2PcInstance) target;
		}
		else
		{
			final SystemMessage sm = new SystemMessage(614);
			sm.addString("Incorrect target.");
			activeChar.sendPacket(sm);
			return;
		}
		
		final L2Skill[] skills = player.getAllSkills();
		
		final int MaxSkillsPerPage = 10;
		int MaxPages = skills.length / MaxSkillsPerPage;
		if (skills.length > (MaxSkillsPerPage * MaxPages))
		{
			MaxPages++;
		}
		
		if (page > MaxPages)
		{
			page = MaxPages;
		}
		
		final int SkillsStart = MaxSkillsPerPage * page;
		int SkillsEnd = skills.length;
		if ((SkillsEnd - SkillsStart) > MaxSkillsPerPage)
		{
			SkillsEnd = SkillsStart + MaxSkillsPerPage;
		}
		
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		final TextBuilder replyMSG = new TextBuilder("<html><body>");
		replyMSG.append("<table width=260><tr>");
		replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("<td width=180><center>Character Selection Menu</center></td>");
		replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_show_skills\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("</tr></table>");
		replyMSG.append("<br><br>");
		replyMSG.append("<center>Editing <font color=\"LEVEL\">" + player.getName() + "</font></center>");
		replyMSG.append("<br><table width=270><tr><td>Lv: " + player.getLevel() + " " + player.getTemplate().className + "</td></tr></table>");
		replyMSG.append("<br><table width=270><tr><td>Note: Dont forget that modifying players skills can</td></tr>");
		replyMSG.append("<tr><td>ruin the game...</td></tr></table>");
		replyMSG.append("<br><center>Click on the skill you wish to remove:</center>");
		replyMSG.append("<br>");
		String pages = "<center><table width=270><tr>";
		for (int x = 0; x < MaxPages; x++)
		{
			final int pagenr = x + 1;
			pages += "<td><a action=\"bypass -h admin_remove_skills " + x + "\">Page " + pagenr + "</a></td>";
		}
		pages += "</tr></table></center>";
		replyMSG.append(pages);
		replyMSG.append("<br><table width=270>");
		replyMSG.append("<tr><td width=80>Name:</td><td width=60>Level:</td><td width=40>Id:</td></tr>");
		
		for (int i = SkillsStart; i < SkillsEnd; i++)
		{
			replyMSG.append("<tr><td width=80><a action=\"bypass -h admin_remove_skill " + skills[i].getId() + "\">" + skills[i].getName() + "</a></td><td width=60>" + skills[i].getLevel() + "</td><td width=40>" + skills[i].getId() + "</td></tr>");
		}
		replyMSG.append("</table>");
		replyMSG.append("<br><center><table>");
		replyMSG.append("Remove custom skill:");
		replyMSG.append("<tr><td>Id: </td>");
		replyMSG.append("<td><edit var=\"id_to_remove\" width=110></td></tr>");
		replyMSG.append("</table></center>");
		replyMSG.append("<center><button value=\"Remove skill\" action=\"bypass -h admin_remove_skill $id_to_remove\" width=110 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center>");
		replyMSG.append("<br><center><button value=\"Back\" action=\"bypass -h admin_current_player\" width=40 height=15></center>");
		replyMSG.append("</body></html>");
		
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}
	
	// ok
	private void showSkillsPage(L2PcInstance activeChar)
	{
		final L2Object target = activeChar.getTarget();
		L2PcInstance player = null;
		if (target instanceof L2PcInstance)
		{
			player = (L2PcInstance) target;
		}
		else
		{
			final SystemMessage sm = new SystemMessage(614);
			sm.addString("Incorrect target.");
			activeChar.sendPacket(sm);
			return;
		}
		
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		
		final TextBuilder replyMSG = new TextBuilder("<html><body>");
		replyMSG.append("<table width=260><tr>");
		replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("<td width=180><center>Character Selection Menu</center></td>");
		replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_current_player\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("</tr></table>");
		replyMSG.append("<br><br>");
		replyMSG.append("<center>Editing <font color=\"LEVEL\">" + player.getName() + "</font></center>");
		replyMSG.append("<br><table width=270><tr><td>Lv: " + player.getLevel() + " " + player.getTemplate().className + "</td></tr></table>");
		replyMSG.append("<br><table width=270><tr><td>Note: Dont forget that modifying players skills can</td></tr>");
		replyMSG.append("<tr><td>ruin the game...</td></tr></table>");
		replyMSG.append("<br><center><table>");
		replyMSG.append("<tr><td><button value=\"Add skills\" action=\"bypass -h admin_skill_list\" width=70 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("<td><button value=\"Get skills\" action=\"bypass -h admin_get_skills\" width=70 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
		replyMSG.append("<tr><td><button value=\"Delete skills\" action=\"bypass -h admin_remove_skills 0\" width=70 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("<td><button value=\"Reset skills\" action=\"bypass -h admin_reset_skills\" width=70 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
		replyMSG.append("<tr><td><button value=\"Give All Skills\" action=\"bypass -h admin_give_all_skills\" width=70 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
		replyMSG.append("</table></center>");
		replyMSG.append("</body></html>");
		
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}
	
	private void adminGetSkills(L2PcInstance activeChar)
	{
		final L2Object target = activeChar.getTarget();
		L2PcInstance player = null;
		if (target instanceof L2PcInstance)
		{
			player = (L2PcInstance) target;
		}
		else
		{
			final SystemMessage sm = new SystemMessage(614);
			sm.addString("Incorrect target.");
			activeChar.sendPacket(sm);
			return;
		}
		
		if (player.getName().equals(activeChar.getName()))
		{
			final SystemMessage sm = new SystemMessage(614);
			sm.addString("There is no point in doing it on your character...");
			player.sendPacket(sm);
		}
		else
		{
			final L2Skill[] skills = player.getAllSkills();
			adminSkills = activeChar.getAllSkills();
			for (final L2Skill adminSkill : adminSkills)
			{
				activeChar.removeSkill(adminSkill);
			}
			for (final L2Skill skill : skills)
			{
				activeChar.addSkill(skill, true);
			}
			final SystemMessage smA = new SystemMessage(614);
			smA.addString("You now have all the skills of  " + player.getName() + ".");
			activeChar.sendPacket(smA);
		}
		showSkillsPage(activeChar);
	}
	
	private void adminResetSkills(L2PcInstance activeChar)
	{
		final L2Object target = activeChar.getTarget();
		L2PcInstance player = null;
		if (target instanceof L2PcInstance)
		{
			player = (L2PcInstance) target;
		}
		else
		{
			final SystemMessage sm = new SystemMessage(614);
			sm.addString("Incorrect target.");
			activeChar.sendPacket(sm);
			return;
		}
		
		if (adminSkills == null)
		{
			final SystemMessage smA = new SystemMessage(614);
			smA.addString("You must first get the skills of someone to do this.");
			activeChar.sendPacket(smA);
		}
		else
		{
			final L2Skill[] skills = player.getAllSkills();
			for (final L2Skill skill : skills)
			{
				player.removeSkill(skill);
			}
			for (int i = 0; i < activeChar.getAllSkills().length; i++)
			{
				player.addSkill(activeChar.getAllSkills()[i], true);
			}
			for (final L2Skill skill : skills)
			{
				activeChar.removeSkill(skill);
			}
			for (final L2Skill adminSkill : adminSkills)
			{
				activeChar.addSkill(adminSkill, true);
			}
			final SystemMessage sm = new SystemMessage(614);
			sm.addString("[GM]" + activeChar.getName() + " has updated your skills.");
			player.sendPacket(sm);
			final SystemMessage smA = new SystemMessage(614);
			smA.addString("You now have all your skills back.");
			activeChar.sendPacket(smA);
			adminSkills = null;
		}
		showSkillsPage(activeChar);
	}
	
	private void adminAddSkill(L2PcInstance activeChar, String val)
	{
		final L2Object target = activeChar.getTarget();
		L2PcInstance player = null;
		if (target instanceof L2PcInstance)
		{
			player = (L2PcInstance) target;
		}
		else
		{
			final SystemMessage sm = new SystemMessage(614);
			sm.addString("Incorrect target.");
			activeChar.sendPacket(sm);
			return;
		}
		
		final StringTokenizer st = new StringTokenizer(val);
		if (st.countTokens() != 2)
		{
			showSkillsPage(activeChar);
		}
		else
		{
			final String id = st.nextToken();
			final String level = st.nextToken();
			final int idval = Integer.parseInt(id);
			final int levelval = Integer.parseInt(level);
			
			final L2Skill skill = SkillTable.getInstance().getInfo(idval, levelval);
			
			if (skill != null)
			{
				final SystemMessage sm = new SystemMessage(614);
				sm.addString("Admin gave you the skill " + skill.getName() + ".");
				player.sendPacket(sm);
				
				player.addSkill(skill, true);
				
				// Admin information
				final SystemMessage smA = new SystemMessage(614);
				smA.addString("You gave the skill " + skill.getName() + " to " + player.getName() + ".");
				
				activeChar.sendPacket(smA);
				if (Config.DEBUG)
				{
					_log.fine("[GM]" + activeChar.getName() + "gave the skill " + skill.getName() + " to " + player.getName() + ".");
				}
			}
			else
			{
				final SystemMessage smA = new SystemMessage(614);
				smA.addString("Error: there is no such skill.");
			}
			showSkillsPage(activeChar); // Back to start
		}
	}
	
	private void adminRemoveSkill(L2PcInstance activeChar, int idval)
	{
		final L2Object target = activeChar.getTarget();
		L2PcInstance player = null;
		if (target instanceof L2PcInstance)
		{
			player = (L2PcInstance) target;
		}
		else
		{
			final SystemMessage sm = new SystemMessage(614);
			sm.addString("Incorrect target.");
			activeChar.sendPacket(sm);
			return;
		}
		
		final L2Skill skill = SkillTable.getInstance().getInfo(idval, player.getSkillLevel(idval));
		
		if (skill != null)
		{
			final SystemMessage sm = new SystemMessage(614);
			sm.addString("Admin removed the skill " + skill.getName() + ".");
			player.sendPacket(sm);
			
			player.removeSkill(skill);
			
			// Admin information
			final SystemMessage smA = new SystemMessage(614);
			smA.addString("You removed the skill " + skill.getName() + " from " + player.getName() + ".");
			
			activeChar.sendPacket(smA);
			if (Config.DEBUG)
			{
				_log.fine("[GM]" + activeChar.getName() + "removed the skill " + skill.getName() + " from " + player.getName() + ".");
			}
		}
		else
		{
			final SystemMessage smA = new SystemMessage(614);
			smA.addString("Error: there is no such skill.");
		}
		removeSkillsPage(activeChar, 0); // Back to start
	}
	
	public void showSkill(L2PcInstance activeChar, String val)
	{
		final int skillid = Integer.parseInt(val);
		final L2Skill skill = SkillTable.getInstance().getInfo(skillid, 1);
		
		if (skill != null)
		{
			if (skill.getTargetType() == L2Skill.SkillTargetType.TARGET_SELF)
			{
				activeChar.setTarget(activeChar);
				
				final MagicSkillUse msk = new MagicSkillUse(activeChar, skillid, 1, skill.getHitTime(), skill.getReuseDelay());
				activeChar.broadcastPacket(msk);
				if (Config.DEBUG)
				{
					_log.fine("showing self skill, id: " + skill.getId() + " named: " + skill.getName());
				}
			}
			else if (skill.getTargetType() == L2Skill.SkillTargetType.TARGET_ONE)
			{
				if (Config.DEBUG)
				{
					_log.fine("showing ATTACK skill, id: " + skill.getId() + " named: " + skill.getName());
				}
			}
		}
		else
		{
			if (Config.DEBUG)
			{
				_log.fine("no such skill id: " + skillid);
			}
			final ActionFailed af = new ActionFailed();
			activeChar.broadcastPacket(af);
		}
	}
}