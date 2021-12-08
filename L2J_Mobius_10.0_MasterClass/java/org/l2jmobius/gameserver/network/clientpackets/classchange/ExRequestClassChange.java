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
package org.l2jmobius.gameserver.network.clientpackets.classchange;

import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;

import org.l2jmobius.Config;
import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.data.xml.CategoryData;
import org.l2jmobius.gameserver.data.xml.SkillData;
import org.l2jmobius.gameserver.data.xml.SkillTreeData;
import org.l2jmobius.gameserver.enums.CategoryType;
import org.l2jmobius.gameserver.enums.ClassId;
import org.l2jmobius.gameserver.enums.Race;
import org.l2jmobius.gameserver.model.SkillLearn;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.PacketLogger;
import org.l2jmobius.gameserver.network.clientpackets.IClientIncomingPacket;
import org.l2jmobius.gameserver.network.serverpackets.PlaySound;
import org.l2jmobius.gameserver.network.serverpackets.classchange.ExClassChangeSetAlarm;

/**
 * @author Mobius
 */
public class ExRequestClassChange implements IClientIncomingPacket
{
	private int _classId;
	private static final String AWAKE_POWER_REWARDED_VAR = "AWAKE_POWER_REWARDED";
	// Reward
	private static final int CHAOS_POMANDER = 37374;
	private static final int VITALITY_MAINTAINING_RUNE = 80712;
	private static final int AWAKE_POWER_EVIS = 40268;
	private static final int AWAKE_POWER_SAYHA = 40269;
	private static final Map<CategoryType, Integer> AWAKE_POWER = new EnumMap<>(CategoryType.class);
	static
	{
		AWAKE_POWER.put(CategoryType.SIXTH_SIGEL_GROUP, 32264);
		AWAKE_POWER.put(CategoryType.SIXTH_TIR_GROUP, 32265);
		AWAKE_POWER.put(CategoryType.SIXTH_OTHEL_GROUP, 32266);
		AWAKE_POWER.put(CategoryType.SIXTH_YR_GROUP, 32267);
		AWAKE_POWER.put(CategoryType.SIXTH_FEOH_GROUP, 32268);
		AWAKE_POWER.put(CategoryType.SIXTH_WYNN_GROUP, 32269);
		AWAKE_POWER.put(CategoryType.SIXTH_IS_GROUP, 32270);
		AWAKE_POWER.put(CategoryType.SIXTH_EOLH_GROUP, 32271);
	}
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_classId = packet.readD();
		return true;
	}
	
	@Override
	public void run(GameClient client)
	{
		final Player player = client.getPlayer();
		if (player == null)
		{
			return;
		}
		
		// Check if class id is valid.
		boolean canChange = false;
		for (ClassId cId : player.getClassId().getNextClassIds())
		{
			if (cId.getId() == _classId)
			{
				canChange = true;
				break;
			}
		}
		if (!canChange //
			&& (_classId != 170) && (player.getClassId().getId() != 133)) // Female Soul Hound fix.
		{
			PacketLogger.warning(player + " tried to change class from " + player.getClassId() + " to " + ClassId.getClassId(_classId) + "!");
			return;
		}
		
		// Check for player proper class group and level.
		canChange = false;
		final int playerLevel = player.getLevel();
		if (player.isInCategory(CategoryType.FIRST_CLASS_GROUP) && (playerLevel >= 18))
		{
			canChange = CategoryData.getInstance().isInCategory(player.getRace() == Race.ERTHEIA ? CategoryType.THIRD_CLASS_GROUP : CategoryType.SECOND_CLASS_GROUP, _classId);
		}
		else if (player.isInCategory(CategoryType.SECOND_CLASS_GROUP) && (playerLevel >= 38))
		{
			canChange = CategoryData.getInstance().isInCategory(CategoryType.THIRD_CLASS_GROUP, _classId);
		}
		else if (player.isInCategory(CategoryType.THIRD_CLASS_GROUP) && (playerLevel >= 76))
		{
			canChange = CategoryData.getInstance().isInCategory(CategoryType.FOURTH_CLASS_GROUP, _classId);
		}
		else if (player.isInCategory(CategoryType.FOURTH_CLASS_GROUP) && (playerLevel >= 85))
		{
			canChange = CategoryData.getInstance().isInCategory(CategoryType.SIXTH_CLASS_GROUP, _classId);
		}
		
		// Change class.
		if (canChange)
		{
			player.setClassId(_classId);
			if (player.isSubClassActive())
			{
				player.getSubClasses().get(player.getClassIndex()).setClassId(player.getActiveClass());
			}
			else
			{
				player.setBaseClass(player.getActiveClass());
			}
			
			if (player.isInCategory(CategoryType.SIXTH_CLASS_GROUP))
			{
				SkillTreeData.getInstance().cleanSkillUponChangeClass(player); // TODO: Move to skill learn method?
				for (SkillLearn skill : SkillTreeData.getInstance().getRaceSkillTree(player.getRace()))
				{
					player.addSkill(SkillData.getInstance().getSkill(skill.getSkillId(), skill.getSkillLevel()), true);
				}
				
				if (Config.DISABLE_TUTORIAL && !player.getVariables().getBoolean(AWAKE_POWER_REWARDED_VAR, false))
				{
					player.addItem("awake", VITALITY_MAINTAINING_RUNE, 1, player, true);
					player.addItem("awake", CHAOS_POMANDER, 2, player, true);
					if (player.getRace() == Race.ERTHEIA)
					{
						if (player.getClassId() == ClassId.EVISCERATOR)
						{
							player.getVariables().set(AWAKE_POWER_REWARDED_VAR, true);
							player.addItem("awake", AWAKE_POWER_EVIS, 1, player, true);
						}
						if (player.getClassId() == ClassId.SAYHA_SEER)
						{
							player.getVariables().set(AWAKE_POWER_REWARDED_VAR, true);
							player.addItem("awake", AWAKE_POWER_SAYHA, 1, player, true);
						}
					}
					else
					{
						for (Entry<CategoryType, Integer> ent : AWAKE_POWER.entrySet())
						{
							if (player.isInCategory(ent.getKey()))
							{
								player.getVariables().set(AWAKE_POWER_REWARDED_VAR, true);
								player.addItem("awake", ent.getValue().intValue(), 1, player, true);
								break;
							}
						}
					}
				}
			}
			
			if (Config.AUTO_LEARN_SKILLS)
			{
				player.giveAvailableSkills(Config.AUTO_LEARN_FS_SKILLS, Config.AUTO_LEARN_FP_SKILLS, true, Config.AUTO_LEARN_SKILLS_WITHOUT_ITEMS);
			}
			
			player.store(false); // Save player cause if server crashes before this char is saved, he will lose class.
			player.broadcastUserInfo();
			player.sendSkillList();
			player.sendPacket(new PlaySound("ItemSound.quest_fanfare_2"));
			
			if (Config.DISABLE_TUTORIAL && !player.isInCategory(CategoryType.SIXTH_CLASS_GROUP) //
				&& ((player.isInCategory(CategoryType.SECOND_CLASS_GROUP) && (playerLevel >= 38)) //
					|| (player.isInCategory(CategoryType.THIRD_CLASS_GROUP) && (playerLevel >= 76)) //
					|| (player.isInCategory(CategoryType.FOURTH_CLASS_GROUP) && (playerLevel >= 85))))
			{
				player.sendPacket(ExClassChangeSetAlarm.STATIC_PACKET);
			}
		}
	}
}
