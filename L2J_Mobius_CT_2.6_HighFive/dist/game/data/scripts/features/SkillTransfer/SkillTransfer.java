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
package features.SkillTransfer;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.data.xml.ClassListData;
import org.l2jmobius.gameserver.data.xml.SkillTreeData;
import org.l2jmobius.gameserver.enums.ClassId;
import org.l2jmobius.gameserver.enums.IllegalActionPunishmentType;
import org.l2jmobius.gameserver.model.PlayerCondOverride;
import org.l2jmobius.gameserver.model.SkillLearn;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerProfessionCancel;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerProfessionChange;
import org.l2jmobius.gameserver.model.holders.ItemHolder;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.itemcontainer.PlayerInventory;
import org.l2jmobius.gameserver.model.skills.Skill;
import org.l2jmobius.gameserver.util.Util;

import ai.AbstractNpcAI;

/**
 * Skill Transfer feature.
 * @author Zoey76
 */
public class SkillTransfer extends AbstractNpcAI
{
	private static final String HOLY_POMANDER = "HOLY_POMANDER_";
	private static final ItemHolder[] PORMANDERS =
	{
		// Cardinal (97)
		new ItemHolder(15307, 1),
		// Eva's Saint (105)
		new ItemHolder(15308, 1),
		// Shillen Saint (112)
		new ItemHolder(15309, 4)
	};
	
	private SkillTransfer()
	{
		setPlayerProfessionChangeId(this::onProfessionChange);
		setPlayerProfessionCancelId(this::onProfessionCancel);
		setOnEnterWorld(Config.SKILL_CHECK_ENABLE);
	}
	
	public void onProfessionChange(OnPlayerProfessionChange event)
	{
		final Player player = event.getPlayer();
		final int index = getTransferClassIndex(player);
		if (index < 0)
		{
			return;
		}
		
		final String name = HOLY_POMANDER + player.getClassId().getId();
		if (!player.getVariables().getBoolean(name, false))
		{
			player.getVariables().set(name, true);
			giveItems(player, PORMANDERS[index]);
		}
	}
	
	public void onProfessionCancel(OnPlayerProfessionCancel event)
	{
		final Player player = event.getPlayer();
		final ClassId classId = ClassId.getClassId(event.getClassId());
		final int index = getTransferClassIndex(classId);
		
		// is a transfer class
		if (index < 0)
		{
			return;
		}
		
		final int pomanderId = PORMANDERS[index].getId();
		// remove unsused HolyPomander
		final PlayerInventory inv = player.getInventory();
		for (Item itemI : inv.getAllItemsByItemId(pomanderId))
		{
			inv.destroyItem("[HolyPomander - remove]", itemI, player, null);
		}
		// remove holy pomander variable
		final String name = HOLY_POMANDER + event.getClassId();
		player.getVariables().remove(name);
	}
	
	@Override
	public String onEnterWorld(Player player)
	{
		if (!player.canOverrideCond(PlayerCondOverride.SKILL_CONDITIONS) || Config.SKILL_CHECK_GM)
		{
			final int index = getTransferClassIndex(player);
			if (index < 0)
			{
				return super.onEnterWorld(player);
			}
			long count = PORMANDERS[index].getCount() - player.getInventory().getInventoryItemCount(PORMANDERS[index].getId(), -1, false);
			for (Skill sk : player.getAllSkills())
			{
				for (SkillLearn s : SkillTreeData.getInstance().getTransferSkillTree(player.getClassId()).values())
				{
					if (s.getSkillId() == sk.getId())
					{
						// Holy Weapon allowed for Shilien Saint/Inquisitor stance
						if ((sk.getId() == 1043) && (index == 2) && player.isInStance())
						{
							continue;
						}
						
						count--;
						if (count < 0)
						{
							final String className = ClassListData.getInstance().getClass(player.getClassId()).getClassName();
							Util.handleIllegalPlayerAction(player, "Player " + player.getName() + " has too many transfered skills or items, skill:" + s.getName() + " (" + sk.getId() + "/" + sk.getLevel() + "), class:" + className, IllegalActionPunishmentType.BROADCAST);
							if (Config.SKILL_CHECK_REMOVE)
							{
								player.removeSkill(sk);
							}
						}
					}
				}
			}
			// SkillTransfer or HolyPomander missing
			if (count > 0)
			{
				player.getInventory().addItem("[HolyPomander - missing]", PORMANDERS[index].getId(), count, player, null);
			}
		}
		return super.onEnterWorld(player);
	}
	
	private static int getTransferClassIndex(Player player)
	{
		return getTransferClassIndex(player.getClassId());
	}
	
	private static int getTransferClassIndex(ClassId classId)
	{
		switch (classId)
		{
			case CARDINAL:
			{
				return 0;
			}
			case EVA_SAINT:
			{
				return 1;
			}
			case SHILLIEN_SAINT:
			{
				return 2;
			}
			default:
			{
				return -1;
			}
		}
	}
	
	public static void main(String[] args)
	{
		new SkillTransfer();
	}
}
