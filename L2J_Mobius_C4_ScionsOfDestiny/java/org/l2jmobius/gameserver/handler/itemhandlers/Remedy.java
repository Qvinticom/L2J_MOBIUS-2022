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
import org.l2jmobius.gameserver.model.actor.Playable;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.instance.Pet;
import org.l2jmobius.gameserver.model.effects.Effect;
import org.l2jmobius.gameserver.model.effects.EffectType;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.skill.SkillType;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.MagicSkillUse;

public class Remedy implements IItemHandler
{
	private static final int[] ITEM_IDS =
	{
		1831,
		1832,
		1833,
		1834,
		3889
	};
	
	@Override
	public void useItem(Playable playable, Item item)
	{
		Player player;
		if (playable instanceof Player)
		{
			player = (Player) playable;
		}
		else if (playable instanceof Pet)
		{
			player = ((Pet) playable).getOwner();
		}
		else
		{
			return;
		}
		
		if (player.isInOlympiadMode())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_USE_THAT_ITEM_IN_A_GRAND_OLYMPIAD_GAMES_MATCH);
			return;
		}
		
		final int itemId = item.getItemId();
		if (itemId == 1831) // antidote
		{
			for (Effect effect : player.getAllEffects())
			{
				if ((effect.getSkill().getSkillType() == SkillType.POISON) && (effect.getSkill().getLevel() <= 3))
				{
					effect.exit(true);
					break;
				}
			}
			final MagicSkillUse msu = new MagicSkillUse(playable, playable, 2042, 1, 0, 0);
			player.sendPacket(msu);
			player.broadcastPacket(msu);
			playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
		}
		else if (itemId == 1832) // advanced antidote
		{
			for (Effect effect : player.getAllEffects())
			{
				if ((effect.getSkill().getSkillType() == SkillType.POISON) && (effect.getSkill().getLevel() <= 7))
				{
					effect.exit(true);
					break;
				}
			}
			final MagicSkillUse msu = new MagicSkillUse(playable, playable, 2043, 1, 0, 0);
			player.sendPacket(msu);
			player.broadcastPacket(msu);
			playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
		}
		else if (itemId == 1833) // bandage
		{
			for (Effect effect : player.getAllEffects())
			{
				if ((effect.getSkill().getSkillType() == SkillType.BLEED) && (effect.getSkill().getLevel() <= 3))
				{
					effect.exit(true);
					break;
				}
			}
			final MagicSkillUse msu = new MagicSkillUse(playable, playable, 34, 1, 0, 0);
			player.sendPacket(msu);
			player.broadcastPacket(msu);
			playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
		}
		else if (itemId == 1834) // emergency dressing
		{
			for (Effect effect : player.getAllEffects())
			{
				if ((effect.getSkill().getSkillType() == SkillType.BLEED) && (effect.getSkill().getLevel() <= 7))
				{
					effect.exit(true);
					break;
				}
			}
			final MagicSkillUse msu = new MagicSkillUse(playable, playable, 2045, 1, 0, 0);
			player.sendPacket(msu);
			player.broadcastPacket(msu);
			playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
		}
		else if (itemId == 3889) // potion of recovery
		{
			for (Effect effect : player.getAllEffects())
			{
				if (effect.getSkill().getId() == 4082)
				{
					effect.exit(true);
				}
			}
			
			player.setImmobilized(false);
			
			if (player.getFirstEffect(EffectType.ROOT) == null)
			{
				player.stopRooting(null);
			}
			
			final MagicSkillUse msu = new MagicSkillUse(playable, playable, 2042, 1, 0, 0);
			player.sendPacket(msu);
			player.broadcastPacket(msu);
			playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
		}
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
