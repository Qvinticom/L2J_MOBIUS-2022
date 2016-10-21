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
package ai.npc.BlackJudge;

import com.l2jmobius.gameserver.datatables.SkillData;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.itemcontainer.Inventory;
import com.l2jmobius.gameserver.model.skills.BuffInfo;
import com.l2jmobius.gameserver.model.skills.CommonSkill;
import com.l2jmobius.gameserver.model.skills.Skill;
import com.l2jmobius.gameserver.network.SystemMessageId;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;

import ai.AbstractNpcAI;

/**
 * Black Judge AI.
 * @author St3eT
 */
public final class BlackJudge extends AbstractNpcAI
{
	// NPC
	private static final int BLACK_JUDGE = 30981;
	// Misc
	// @formatter:off
	private static final int[] COSTS =
	{
		3600, 8640, 25200, 50400, 86400, 144000
	};
	// @formatter:on
	
	private BlackJudge()
	{
		super(BlackJudge.class.getSimpleName(), "ai/npc");
		addStartNpc(BLACK_JUDGE);
		addTalkId(BLACK_JUDGE);
		addFirstTalkId(BLACK_JUDGE);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = null;
		final int level = (player.getExpertiseLevel() < 5) ? player.getExpertiseLevel() : 5;
		switch (event)
		{
			case "remove_info":
			{
				htmltext = "30981-0" + (level + 1) + ".html";
				break;
			}
			case "remove_dp":
			{
				if (player.getShilensBreathDebuffLevel() > 0)
				{
					if (player.getAdena() >= COSTS[level])
					{
						takeItems(player, Inventory.ADENA_ID, COSTS[level]);
						final int nextLv = player.getShilensBreathDebuffLevel() - 1;
						if (nextLv > 0)
						{
							final Skill skill = SkillData.getInstance().getSkill(CommonSkill.SHILENS_BREATH.getId(), nextLv);
							if (skill != null)
							{
								skill.applyEffects(player, player);
								player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_VE_BEEN_AFFLICTED_BY_SHILEN_S_BREATH_LEVEL_S1).addInt(nextLv));
							}
						}
						else
						{
							final BuffInfo buff = player.getEffectList().getBuffInfoBySkillId(CommonSkill.SHILENS_BREATH.getId());
							if (buff != null)
							{
								player.getEffectList().remove(true, buff);
							}
							player.sendPacket(SystemMessageId.SHILEN_S_BREATH_HAS_BEEN_PURIFIED);
						}
					}
					else
					{
						htmltext = "30981-07.html";
					}
				}
				else
				{
					htmltext = "30981-08.html";
				}
				break;
			}
		}
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new BlackJudge();
	}
}
