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
package org.l2jmobius.gameserver.handler.itemhandlers;

import org.l2jmobius.gameserver.handler.IItemHandler;
import org.l2jmobius.gameserver.model.Effect;
import org.l2jmobius.gameserver.model.Skill;
import org.l2jmobius.gameserver.model.actor.Playable;
import org.l2jmobius.gameserver.model.actor.instance.PetInstance;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.items.instance.ItemInstance;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.MagicSkillUse;

public class Remedy implements IItemHandler
{
	private static int[] ITEM_IDS =
	{
		1831,
		1832,
		1833,
		1834,
		3889
	};
	
	@Override
	public void useItem(Playable playable, ItemInstance item)
	{
		PlayerInstance player;
		
		if (playable instanceof PlayerInstance)
		{
			player = (PlayerInstance) playable;
		}
		else if (playable instanceof PetInstance)
		{
			player = ((PetInstance) playable).getOwner();
		}
		else
		{
			return;
		}
		
		if (player.isInOlympiadMode())
		{
			player.sendPacket(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
			return;
		}
		
		final int itemId = item.getItemId();
		if (itemId == 1831) // antidote
		{
			Effect[] effects = player.getAllEffects();
			for (Effect e : effects)
			{
				if ((e.getSkill().getSkillType() == Skill.SkillType.POISON) && (e.getSkill().getLevel() <= 3))
				{
					e.exit(true);
					break;
				}
			}
			MagicSkillUse MSU = new MagicSkillUse(playable, playable, 2042, 1, 0, 0);
			player.sendPacket(MSU);
			player.broadcastPacket(MSU);
			playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
		}
		else if (itemId == 1832) // advanced antidote
		{
			Effect[] effects = player.getAllEffects();
			for (Effect e : effects)
			{
				if ((e.getSkill().getSkillType() == Skill.SkillType.POISON) && (e.getSkill().getLevel() <= 7))
				{
					e.exit(true);
					break;
				}
			}
			MagicSkillUse MSU = new MagicSkillUse(playable, playable, 2043, 1, 0, 0);
			player.sendPacket(MSU);
			player.broadcastPacket(MSU);
			playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
		}
		else if (itemId == 1833) // bandage
		{
			Effect[] effects = player.getAllEffects();
			for (Effect e : effects)
			{
				if ((e.getSkill().getSkillType() == Skill.SkillType.BLEED) && (e.getSkill().getLevel() <= 3))
				{
					e.exit(true);
					break;
				}
			}
			MagicSkillUse MSU = new MagicSkillUse(playable, playable, 34, 1, 0, 0);
			player.sendPacket(MSU);
			player.broadcastPacket(MSU);
			playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
		}
		else if (itemId == 1834) // emergency dressing
		{
			Effect[] effects = player.getAllEffects();
			for (Effect e : effects)
			{
				if ((e.getSkill().getSkillType() == Skill.SkillType.BLEED) && (e.getSkill().getLevel() <= 7))
				{
					e.exit(true);
					break;
				}
			}
			MagicSkillUse MSU = new MagicSkillUse(playable, playable, 2045, 1, 0, 0);
			player.sendPacket(MSU);
			player.broadcastPacket(MSU);
			playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
		}
		else if (itemId == 3889) // potion of recovery
		{
			Effect[] effects = player.getAllEffects();
			for (Effect e : effects)
			{
				if (e.getSkill().getId() == 4082)
				{
					e.exit(true);
				}
			}
			
			player.setIsImobilised(false);
			
			if (player.getFirstEffect(Effect.EffectType.ROOT) == null)
			{
				player.stopRooting(null);
			}
			
			MagicSkillUse MSU = new MagicSkillUse(playable, playable, 2042, 1, 0, 0);
			player.sendPacket(MSU);
			player.broadcastPacket(MSU);
			playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
		}
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
