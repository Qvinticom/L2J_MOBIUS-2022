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
package ai.areas.Aden.Gallias;

import java.util.Set;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.data.xml.SkillData;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.events.EventType;
import org.l2jmobius.gameserver.model.events.ListenerRegisterType;
import org.l2jmobius.gameserver.model.events.annotations.RegisterEvent;
import org.l2jmobius.gameserver.model.events.annotations.RegisterType;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerLogin;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerSubChange;
import org.l2jmobius.gameserver.model.holders.SubClassHolder;
import org.l2jmobius.gameserver.model.itemcontainer.Inventory;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.model.variables.PlayerVariables;
import org.l2jmobius.gameserver.network.clientpackets.RequestAcquireSkill;

import ai.AbstractNpcAI;

/**
 * Gallias AI. (Based on Trandon AI)
 * @author Mobius
 */
public class Gallias extends AbstractNpcAI
{
	// NPC
	private static final int NPC_ID = 34514;
	// Items
	private static final int SUB_CERTIFICATE = 10280;
	private static final int DUAL_CERTIFICATE = 36078;
	private static final int DUAL_CERTIFICATE_ENHANCED = 81731;
	private static final int ENERGY_OF_POWER = 80924;
	// Skills
	private static final int DUAL_CLASS_RENEWED_ENERGY_OF_POWER = 30820;
	// Misc @formatter:off
	private static final int[] SUB_SKILL_LEVELS = {65, 70, 75, 80};
	private static final int[] DUAL_SKILL_LEVELS = {85, 90, 95, 99, 101, 103, 105, 107, 109, 110};
	// @formatter:on
	private static final String SUB_CERTIFICATE_COUNT_VAR = "SUB_CERTIFICATE_COUNT";
	private static final String DUAL_CERTIFICATE_COUNT_VAR = "DUAL_CERTIFICATE_COUNT";
	
	private Gallias()
	{
		addStartNpc(NPC_ID);
		addFirstTalkId(NPC_ID);
		addTalkId(NPC_ID);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		final String[] substrings = event.split(" ");
		if (substrings.length < 1)
		{
			return null;
		}
		String htmltext = substrings[0];
		switch (htmltext)
		{
			case "34514.html":
			case "34514-01.html":
			case "34514-02.html":
			case "34514-04.html":
			case "34514-05.html":
			case "34514-06.html":
			case "34514-07.html":
			case "34514-08.html":
			case "34514-09.html":
			case "34514-10.html":
			case "34514-11.html":
			case "34514-19.html":
			{
				break;
			}
			case "34514-03.html":
			{
				if (!player.hasDualClass())
				{
					htmltext = "34514-05.html";
				}
				else if (hasAtLeastOneQuestItem(player, ENERGY_OF_POWER) || (player.getKnownSkill(DUAL_CLASS_RENEWED_ENERGY_OF_POWER) != null))
				{
					htmltext = "34514-04.html";
				}
				else
				{
					giveItems(player, ENERGY_OF_POWER, 1);
					htmltext = "34514-03.html";
				}
				break;
			}
			case "34514-12.html":
			{
				if (!player.isSubClassActive())
				{
					htmltext = "34514-13.html";
				}
				else if (!player.isInventoryUnder90(false) || (player.getWeightPenalty() >= 2))
				{
					htmltext = "34514-14.html";
				}
				break;
			}
			case "subCertify":
			{
				if ((substrings.length < 2) || !player.isSubClassActive())
				{
					return null;
				}
				
				final int index = Integer.parseInt(substrings[1]);
				if ((index < 0) || (index > 3))
				{
					return null;
				}
				
				final int level = SUB_SKILL_LEVELS[index];
				if (player.getLevel() < level)
				{
					htmltext = getHtm(player, "34514-16.html").replace("%level%", String.valueOf(level));
				}
				else if (player.getVariables().hasVariable(getSubSkillVariableName(player, level)))
				{
					htmltext = "34514-17.html";
				}
				else
				{
					htmltext = getHtm(player, "34514-18.html");
					htmltext = htmltext.replace("%level%", String.valueOf(level));
					htmltext = htmltext.replace("%index%", String.valueOf(index));
				}
				break;
			}
			case "giveSubCertify":
			{
				if ((substrings.length < 2) || !player.isSubClassActive())
				{
					return null;
				}
				
				final int index = Integer.parseInt(substrings[1]);
				if ((index < 0) || (index > 3))
				{
					return null;
				}
				
				final int level = SUB_SKILL_LEVELS[index];
				final PlayerVariables vars = player.getVariables();
				if ((player.getLevel() < level) || vars.hasVariable(getSubSkillVariableName(player, level)))
				{
					htmltext = null;
				}
				else
				{
					final int subId = player.getClassId().getId();
					final int currentCount = player.getVariables().getInt(SUB_CERTIFICATE_COUNT_VAR + subId, 0);
					if (currentCount < SUB_SKILL_LEVELS.length)
					{
						player.getVariables().set(SUB_CERTIFICATE_COUNT_VAR + subId, currentCount + 1);
						vars.set(getSubSkillVariableName(player, level), true);
						giveItems(player, SUB_CERTIFICATE, 1);
					}
					htmltext = "34514-20.html";
				}
				break;
			}
			case "learnSubSkill":
			{
				if (player.isSubClassActive() || !ownsAtLeastOneItem(player, SUB_CERTIFICATE))
				{
					htmltext = "34514-21.html";
				}
				else
				{
					RequestAcquireSkill.showSubSkillList(player);
					htmltext = null;
				}
				break;
			}
			case "deleteSubSkill":
			{
				if (player.isSubClassActive())
				{
					htmltext = "34514-21.html";
				}
				else if (player.getAdena() < Config.FEE_DELETE_SUBCLASS_SKILLS)
				{
					htmltext = "34514-22.html";
				}
				else if (!hasSubCertificate(player))
				{
					htmltext = "34514-23.html";
				}
				else
				{
					htmltext = null; // TODO: Unknown html
					takeItems(player, SUB_CERTIFICATE, -1);
					player.getWarehouse().destroyItemByItemId("Quest", SUB_CERTIFICATE, -1, player, npc);
					takeItems(player, Inventory.ADENA_ID, Config.FEE_DELETE_SUBCLASS_SKILLS);
					for (SubClassHolder subclass : player.getSubClasses().values())
					{
						player.getVariables().remove(SUB_CERTIFICATE_COUNT_VAR + subclass.getClassId());
					}
					
					final PlayerVariables vars = player.getVariables();
					for (int i = 1; i <= 3; i++)
					{
						for (int lv : SUB_SKILL_LEVELS)
						{
							vars.remove("SubSkill-" + i + "-" + lv);
						}
					}
					takeSkills(player, "SubSkillList");
				}
				break;
			}
			case "34514-26.html":
			{
				// TODO: What happens when you have all dual certificates?
				if (!player.isDualClassActive())
				{
					htmltext = "34514-24.html";
				}
				else if (!player.isInventoryUnder90(false) || (player.getWeightPenalty() >= 2))
				{
					htmltext = "34514-25.html";
				}
				break;
			}
			case "34514-35.html":
			{
				// TODO: What happens when you have all dual certificates?
				if (!player.isDualClassActive())
				{
					htmltext = "34514-24.html";
				}
				else if (!player.isInventoryUnder90(false) || (player.getWeightPenalty() >= 2))
				{
					htmltext = "34514-25.html";
				}
				break;
			}
			case "dualCertify":
			{
				if ((substrings.length < 2) || !player.isDualClassActive())
				{
					return null;
				}
				
				final int index = Integer.parseInt(substrings[1]);
				if ((index < 0) || (index > (DUAL_SKILL_LEVELS.length - 1)))
				{
					return null;
				}
				
				final int level = DUAL_SKILL_LEVELS[index];
				final PlayerVariables vars = player.getVariables();
				if (vars.hasVariable(getDualSkillVariableName(level)))
				{
					htmltext = getHtm(player, "34514-27.html");
				}
				else if ((player.getLevel() < level) || (player.getStat().getBaseLevel() < level))
				{
					htmltext = getHtm(player, "34514-28.html");
				}
				else
				{
					final int currentCount = player.getVariables().getInt(DUAL_CERTIFICATE_COUNT_VAR, 0);
					if (currentCount < DUAL_SKILL_LEVELS.length)
					{
						player.getVariables().set(DUAL_CERTIFICATE_COUNT_VAR, currentCount + 1);
						vars.set(getDualSkillVariableName(level), true);
						giveItems(player, level < 107 ? DUAL_CERTIFICATE : DUAL_CERTIFICATE_ENHANCED, 1);
					}
					htmltext = getHtm(player, "34514-29.html");
				}
				htmltext = htmltext.replace("%level%", String.valueOf(level));
				break;
			}
			case "learnDualSkill":
			{
				// TODO: What happens when you have all dual-certificates used?
				if (player.isSubClassActive())
				{
					htmltext = "34514-30.html";
				}
				else if (!ownsAtLeastOneItem(player, DUAL_CERTIFICATE) && !ownsAtLeastOneItem(player, DUAL_CERTIFICATE_ENHANCED))
				{
					htmltext = "34514-31.html";
				}
				else if ((player.getLevel() < DUAL_SKILL_LEVELS[0]) || (player.getStat().getBaseLevel() < DUAL_SKILL_LEVELS[0]))
				{
					// This case should not happen
					htmltext = null;
				}
				else
				{
					RequestAcquireSkill.showDualSkillList(player);
					htmltext = null;
				}
				break;
			}
			case "deleteDualSkill":
			{
				if (player.isSubClassActive())
				{
					htmltext = "34514-30.html";
				}
				else if (player.getAdena() < Config.FEE_DELETE_DUALCLASS_SKILLS)
				{
					htmltext = "34514-32.html";
				}
				else if (!hasDualCertificate(player))
				{
					htmltext = "34514-33.html";
				}
				else
				{
					htmltext = null; // TODO: Unknown html
					takeItems(player, DUAL_CERTIFICATE, -1);
					takeItems(player, DUAL_CERTIFICATE_ENHANCED, -1);
					player.getWarehouse().destroyItemByItemId("Quest", DUAL_CERTIFICATE, -1, player, npc);
					player.getWarehouse().destroyItemByItemId("Quest", DUAL_CERTIFICATE_ENHANCED, -1, player, npc);
					takeItems(player, Inventory.ADENA_ID, Config.FEE_DELETE_DUALCLASS_SKILLS);
					player.getVariables().remove(DUAL_CERTIFICATE_COUNT_VAR);
					
					final PlayerVariables vars = player.getVariables();
					for (int lv : DUAL_SKILL_LEVELS)
					{
						vars.remove(getDualSkillVariableName(lv));
					}
					takeSkills(player, "DualSkillList");
				}
				break;
			}
			default:
			{
				htmltext = null;
			}
		}
		return htmltext;
	}
	
	// TODO: Move this to char skills
	@RegisterEvent(EventType.ON_PLAYER_SUB_CHANGE)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void onSubChange(OnPlayerSubChange evt)
	{
		final Player player = evt.getPlayer();
		if (player.isDualClassActive() || !player.isSubClassActive())
		{
			giveSkills(player, "DualSkillList");
		}
		giveSkills(player, "SubSkillList");
	}
	
	@RegisterEvent(EventType.ON_PLAYER_LOGIN)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void onLogin(OnPlayerLogin evt)
	{
		final Player player = evt.getPlayer();
		if (player.isDualClassActive() || !player.isSubClassActive())
		{
			giveSkills(player, "DualSkillList");
		}
		giveSkills(player, "SubSkillList");
	}
	
	/**
	 * Checks if player has any sub certification
	 * @param player
	 * @return
	 */
	private final boolean hasSubCertificate(Player player)
	{
		final PlayerVariables vars = player.getVariables();
		final Set<Integer> subs = player.getSubClasses().keySet();
		for (int index : subs)
		{
			for (int lv : SUB_SKILL_LEVELS)
			{
				if (vars.hasVariable("SubSkill-" + index + "-" + lv))
				{
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Checks if player has any dual certification
	 * @param player
	 * @return
	 */
	private final boolean hasDualCertificate(Player player)
	{
		final PlayerVariables vars = player.getVariables();
		for (int lv : DUAL_SKILL_LEVELS)
		{
			if (vars.hasVariable(getDualSkillVariableName(lv)))
			{
				return true;
			}
		}
		return false;
	}
	
	private final String getSubSkillVariableName(Player player, int level)
	{
		return "SubSkill-" + player.getClassIndex() + "-" + level;
	}
	
	private final String getDualSkillVariableName(int level)
	{
		return "DualSkill-" + level;
	}
	
	private final void takeSkills(Player player, String type)
	{
		final PlayerVariables vars = player.getVariables();
		final String list = vars.getString(type, "");
		if (!list.isEmpty())
		{
			final String[] skills = list.split(";");
			for (String skill : skills)
			{
				final String[] str = skill.split("-");
				final Skill sk = SkillData.getInstance().getSkill(Integer.parseInt(str[0]), Integer.parseInt(str[1]));
				player.removeSkill(sk);
			}
			vars.remove(type);
			player.sendSkillList();
		}
	}
	
	private final void giveSkills(Player player, String type)
	{
		final String list = player.getVariables().getString(type, "");
		if (!list.isEmpty())
		{
			final String[] skills = list.split(";");
			for (String skill : skills)
			{
				final String[] str = skill.split("-");
				final Skill sk = SkillData.getInstance().getSkill(Integer.parseInt(str[0]), Integer.parseInt(str[1]));
				player.addSkill(sk, false);
			}
		}
	}
	
	public static void main(String[] args)
	{
		new Gallias();
	}
}