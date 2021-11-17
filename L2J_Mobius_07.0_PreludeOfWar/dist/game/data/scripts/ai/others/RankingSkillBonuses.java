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
package ai.others;

import org.l2jmobius.gameserver.data.xml.SkillData;
import org.l2jmobius.gameserver.enums.SkillFinishType;
import org.l2jmobius.gameserver.instancemanager.RankManager;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.events.EventType;
import org.l2jmobius.gameserver.model.events.ListenerRegisterType;
import org.l2jmobius.gameserver.model.events.annotations.RegisterEvent;
import org.l2jmobius.gameserver.model.events.annotations.RegisterType;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerLogin;
import org.l2jmobius.gameserver.model.skill.Skill;

import ai.AbstractNpcAI;

/**
 * @author Mobius
 */
public class RankingSkillBonuses extends AbstractNpcAI
{
	// Skills
	private static final Skill SERVER_LEVEL_RANKING_1ST_CLASS = SkillData.getInstance().getSkill(32874, 1);
	private static final Skill SERVER_LEVEL_RANKING_2ND_CLASS = SkillData.getInstance().getSkill(32875, 1);
	private static final Skill SERVER_LEVEL_RANKING_3RD_CLASS = SkillData.getInstance().getSkill(32876, 1);
	private static final Skill HUMAN_LEVEL_RANKING_1ST_CLASS = SkillData.getInstance().getSkill(32877, 1);
	private static final Skill ELF_LEVEL_RANKING_1ST_CLASS = SkillData.getInstance().getSkill(32878, 1);
	private static final Skill DARK_ELF_LEVEL_RANKING_1ST_CLASS = SkillData.getInstance().getSkill(32879, 1);
	private static final Skill ORC_LEVEL_RANKING_1ST_CLASS = SkillData.getInstance().getSkill(32880, 1);
	private static final Skill DWARF_LEVEL_RANKING_1ST_CLASS = SkillData.getInstance().getSkill(32881, 1);
	private static final Skill KAMAEL_LEVEL_RANKING_1ST_CLASS = SkillData.getInstance().getSkill(32882, 1);
	private static final Skill ERTHEIA_LEVEL_RANKING_1ST_CLASS = SkillData.getInstance().getSkill(32883, 1);
	private static final Skill SERVER_RANKING_BENEFIT_1 = SkillData.getInstance().getSkill(32884, 1);
	private static final Skill SERVER_RANKING_BENEFIT_2 = SkillData.getInstance().getSkill(32885, 1);
	private static final Skill SERVER_RANKING_BENEFIT_3 = SkillData.getInstance().getSkill(32886, 1);
	private static final Skill RACE_RANKING_BENEFIT = SkillData.getInstance().getSkill(32887, 1);
	
	@RegisterEvent(EventType.ON_PLAYER_LOGIN)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void onPlayerLogin(OnPlayerLogin event)
	{
		final Player player = event.getPlayer();
		if (player == null)
		{
			return;
		}
		
		// Remove existing effects and skills.
		player.getEffectList().stopSkillEffects(SkillFinishType.REMOVED, SERVER_LEVEL_RANKING_1ST_CLASS);
		player.getEffectList().stopSkillEffects(SkillFinishType.REMOVED, SERVER_LEVEL_RANKING_2ND_CLASS);
		player.getEffectList().stopSkillEffects(SkillFinishType.REMOVED, SERVER_LEVEL_RANKING_3RD_CLASS);
		player.getEffectList().stopSkillEffects(SkillFinishType.REMOVED, HUMAN_LEVEL_RANKING_1ST_CLASS);
		player.getEffectList().stopSkillEffects(SkillFinishType.REMOVED, ELF_LEVEL_RANKING_1ST_CLASS);
		player.getEffectList().stopSkillEffects(SkillFinishType.REMOVED, DARK_ELF_LEVEL_RANKING_1ST_CLASS);
		player.getEffectList().stopSkillEffects(SkillFinishType.REMOVED, ORC_LEVEL_RANKING_1ST_CLASS);
		player.getEffectList().stopSkillEffects(SkillFinishType.REMOVED, DWARF_LEVEL_RANKING_1ST_CLASS);
		player.getEffectList().stopSkillEffects(SkillFinishType.REMOVED, KAMAEL_LEVEL_RANKING_1ST_CLASS);
		player.getEffectList().stopSkillEffects(SkillFinishType.REMOVED, ERTHEIA_LEVEL_RANKING_1ST_CLASS);
		player.removeSkill(SERVER_RANKING_BENEFIT_1);
		player.removeSkill(SERVER_RANKING_BENEFIT_2);
		player.removeSkill(SERVER_RANKING_BENEFIT_3);
		player.removeSkill(RACE_RANKING_BENEFIT);
		
		// Add global rank skills.
		int rank = RankManager.getInstance().getPlayerGlobalRank(player);
		if (rank > 0)
		{
			if (rank <= 10)
			{
				SERVER_LEVEL_RANKING_1ST_CLASS.applyEffects(player, player);
				player.addSkill(SERVER_RANKING_BENEFIT_1, false);
				player.addSkill(SERVER_RANKING_BENEFIT_2, false);
				player.addSkill(SERVER_RANKING_BENEFIT_3, false);
			}
			else if (rank <= 50)
			{
				SERVER_LEVEL_RANKING_2ND_CLASS.applyEffects(player, player);
				player.addSkill(SERVER_RANKING_BENEFIT_1, false);
				player.addSkill(SERVER_RANKING_BENEFIT_2, false);
			}
			else if (rank <= 100)
			{
				SERVER_LEVEL_RANKING_3RD_CLASS.applyEffects(player, player);
				player.addSkill(SERVER_RANKING_BENEFIT_1, false);
			}
		}
		
		// Apply race rank effects.
		final int raceRank = RankManager.getInstance().getPlayerRaceRank(player);
		if ((raceRank > 0) && (raceRank <= 10))
		{
			switch (player.getRace())
			{
				case HUMAN:
				{
					HUMAN_LEVEL_RANKING_1ST_CLASS.applyEffects(player, player);
					break;
				}
				case ELF:
				{
					ELF_LEVEL_RANKING_1ST_CLASS.applyEffects(player, player);
					break;
				}
				case DARK_ELF:
				{
					DARK_ELF_LEVEL_RANKING_1ST_CLASS.applyEffects(player, player);
					break;
				}
				case ORC:
				{
					ORC_LEVEL_RANKING_1ST_CLASS.applyEffects(player, player);
					break;
				}
				case DWARF:
				{
					DWARF_LEVEL_RANKING_1ST_CLASS.applyEffects(player, player);
					break;
				}
				case KAMAEL:
				{
					KAMAEL_LEVEL_RANKING_1ST_CLASS.applyEffects(player, player);
					break;
				}
				case ERTHEIA:
				{
					ERTHEIA_LEVEL_RANKING_1ST_CLASS.applyEffects(player, player);
					break;
				}
			}
			player.addSkill(RACE_RANKING_BENEFIT, false);
		}
	}
	
	public static void main(String[] args)
	{
		new RankingSkillBonuses();
	}
}
