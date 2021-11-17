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
package ai.areas.Rune.Roiental;

import org.l2jmobius.gameserver.instancemanager.InstanceManager;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.instancezone.Instance;

import ai.AbstractNpcAI;

/**
 * Roiental AI.
 * @author CostyKiller
 */
public class Roiental extends AbstractNpcAI
{
	// NPCs
	private static final int ROIENTAL = 34571;
	// Misc
	private static final int TOH_GB_TEMPLATE_ID = 307; // Throne of Heroes - Goldberg
	private static final int TOH_MR_TEMPLATE_ID = 308; // Throne of Heroes - Mary Reed
	private static final int TOH_TA_TEMPLATE_ID = 309; // Throne of Heroes - Tauti
	private static final int MIN_LVL = 110;
	private static final int CLAN_MIN_LVL_GB = 7;
	private static final int CLAN_MIN_LVL_MR = 10;
	private static final int CLAN_MIN_LVL_TA = 13;
	
	private Roiental()
	{
		addStartNpc(ROIENTAL);
		addFirstTalkId(ROIENTAL);
		addTalkId(ROIENTAL);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = null;
		if (event.equals("enterGoldberg"))
		{
			if (player.getLevel() < MIN_LVL)
			{
				htmltext = "Roiental-NoLevel.html";
			}
			else if ((player.getClan() == null) || (player.getClan().getLevel() < CLAN_MIN_LVL_GB))
			{
				htmltext = "Roiental-03a.html";
			}
			else if ((player.getClan() == null) || player.getClan().getVariables().hasVariable("TOH_DONE"))
			{
				htmltext = "Roiental-AlreadyDone.html";
			}
			else
			{
				htmltext = "Roiental-01a.html";
			}
		}
		if (event.equals("enterMaryReed"))
		{
			if (player.getLevel() < MIN_LVL)
			{
				htmltext = "Roiental-NoLevel.html";
			}
			else if ((player.getClan() == null) || (player.getClan().getLevel() < CLAN_MIN_LVL_MR))
			{
				htmltext = "Roiental-03b.html";
			}
			else if ((player.getClan() == null) || player.getClan().getVariables().hasVariable("TOH_DONE"))
			{
				htmltext = "Roiental-AlreadyDone.html";
			}
			else
			{
				htmltext = "Roiental-01b.html";
			}
		}
		if (event.equals("enterTauti"))
		{
			if (player.getLevel() < MIN_LVL)
			{
				htmltext = "Roiental-NoLevel.html";
			}
			else if ((player.getClan() == null) || (player.getClan().getLevel() < CLAN_MIN_LVL_TA))
			{
				htmltext = "Roiental-03c.html";
			}
			else if ((player.getClan() == null) || player.getClan().getVariables().hasVariable("TOH_DONE"))
			{
				htmltext = "Roiental-AlreadyDone.html";
			}
			else
			{
				htmltext = "Roiental-01c.html";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		String htmltext = null;
		final Instance instance = InstanceManager.getInstance().getPlayer(player, false);
		if ((instance != null) && ((instance.getTemplateId() == TOH_GB_TEMPLATE_ID)))
		{
			htmltext = "Roiental-02a.html";
		}
		else if ((instance != null) && ((instance.getTemplateId() == TOH_MR_TEMPLATE_ID)))
		{
			htmltext = "Roiental-02b.html";
		}
		else if ((instance != null) && ((instance.getTemplateId() == TOH_TA_TEMPLATE_ID)))
		{
			htmltext = "Roiental-02c.html";
		}
		else
		{
			htmltext = "Roiental-01.html";
		}
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new Roiental();
	}
}