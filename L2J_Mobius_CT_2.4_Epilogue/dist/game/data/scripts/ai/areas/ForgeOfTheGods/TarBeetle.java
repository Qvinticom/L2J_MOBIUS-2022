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
package ai.areas.ForgeOfTheGods;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.SkillHolder;
import org.l2jmobius.gameserver.model.skill.BuffInfo;
import org.l2jmobius.gameserver.model.skill.Skill;

import ai.AbstractNpcAI;

/**
 * Tar Beetle AI
 * @author nonom, malyelfik
 */
public class TarBeetle extends AbstractNpcAI
{
	// NPC
	private static final int TAR_BEETLE = 18804;
	// Skills
	private static final int TAR_SPITE = 6142;
	private static SkillHolder[] SKILLS =
	{
		new SkillHolder(TAR_SPITE, 1),
		new SkillHolder(TAR_SPITE, 2),
		new SkillHolder(TAR_SPITE, 3)
	};
	
	private static final TarBeetleSpawn spawn = new TarBeetleSpawn();
	
	private TarBeetle()
	{
		addAggroRangeEnterId(TAR_BEETLE);
		addSpellFinishedId(TAR_BEETLE);
	}
	
	@Override
	public String onAggroRangeEnter(Npc npc, Player player, boolean isSummon)
	{
		if (npc.getScriptValue() > 0)
		{
			final BuffInfo info = player.getEffectList().getBuffInfoBySkillId(TAR_SPITE);
			final int level = (info != null) ? info.getSkill().getAbnormalLevel() : 0;
			if (level < 3)
			{
				final Skill skill = SKILLS[level].getSkill();
				if (!npc.isSkillDisabled(skill))
				{
					npc.setTarget(player);
					npc.doCast(skill);
				}
			}
		}
		return super.onAggroRangeEnter(npc, player, isSummon);
	}
	
	@Override
	public String onSpellFinished(Npc npc, Player player, Skill skill)
	{
		if ((skill != null) && (skill.getId() == TAR_SPITE))
		{
			final int val = npc.getScriptValue() - 1;
			if ((val <= 0) || (SKILLS[0].getSkill().getMpConsume() > npc.getCurrentMp()))
			{
				spawn.removeBeetle(npc);
			}
			else
			{
				npc.setScriptValue(val);
			}
		}
		return super.onSpellFinished(npc, player, skill);
	}
	
	@Override
	public boolean unload()
	{
		spawn.unload();
		return super.unload();
	}
	
	public static void main(String[] args)
	{
		new TarBeetle();
	}
}