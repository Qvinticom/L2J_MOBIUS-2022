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
package handlers.itemhandlers;

import org.l2jmobius.gameserver.ai.CtrlIntention;
import org.l2jmobius.gameserver.handler.IItemHandler;
import org.l2jmobius.gameserver.model.actor.Playable;
import org.l2jmobius.gameserver.model.holders.SkillHolder;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.skills.Skill;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * Template for item skills handler.
 * @author Zoey76
 */
public class ItemSkillsTemplate implements IItemHandler
{
	@Override
	public boolean useItem(Playable playable, Item item, boolean forceUse)
	{
		if (!playable.isPlayer() && !playable.isPet())
		{
			return false;
		}
		
		if (playable.isOnEvent())
		{
			playable.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		// Pets can use items only when they are tradable.
		if (playable.isPet() && !item.isTradeable())
		{
			playable.sendPacket(SystemMessageId.YOUR_PET_CANNOT_CARRY_THIS_ITEM);
			return false;
		}
		
		// Verify that item is not under reuse.
		if (!checkReuse(playable, null, item))
		{
			return false;
		}
		
		final SkillHolder[] skills = item.getEtcItem().getSkills();
		if (skills == null)
		{
			LOGGER.info("Item " + item + " does not have registered any skill for handler.");
			return false;
		}
		
		boolean hasConsumeSkill = false;
		for (SkillHolder skillInfo : skills)
		{
			if (skillInfo == null)
			{
				continue;
			}
			
			final Skill itemSkill = skillInfo.getSkill();
			if (itemSkill != null)
			{
				if (itemSkill.getItemConsumeId() > 0)
				{
					hasConsumeSkill = true;
				}
				
				if (!itemSkill.checkCondition(playable, playable.getTarget(), false))
				{
					return false;
				}
				
				if (playable.isSkillDisabled(itemSkill))
				{
					return false;
				}
				
				// Verify that skill is not under reuse.
				if (!checkReuse(playable, itemSkill, item))
				{
					return false;
				}
				
				if (!item.isPotion() && !item.isElixir() && !item.isScroll() && playable.isCastingNow())
				{
					return false;
				}
				
				// Send message to the master.
				if (playable.isPet())
				{
					final SystemMessage sm = new SystemMessage(SystemMessageId.YOUR_PET_USES_S1);
					sm.addSkillName(itemSkill);
					playable.sendPacket(sm);
				}
				
				if (itemSkill.isSimultaneousCast() || ((item.getItem().hasImmediateEffect() || item.getItem().hasExImmediateEffect()) && itemSkill.isStatic()))
				{
					playable.doSimultaneousCast(itemSkill);
				}
				else
				{
					playable.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
					if (!playable.useMagic(itemSkill, forceUse, false))
					{
						return false;
					}
				}
				
				if (itemSkill.getReuseDelay() > 0)
				{
					playable.addTimeStamp(itemSkill, itemSkill.getReuseDelay());
				}
			}
		}
		
		if (checkConsume(item, hasConsumeSkill) && !playable.destroyItem("Consume", item.getObjectId(), 1, playable, false))
		{
			playable.sendPacket(SystemMessageId.INCORRECT_ITEM_COUNT_2);
			return false;
		}
		
		return true;
	}
	
	/**
	 * @param item the item being used
	 * @param hasConsumeSkill
	 * @return {@code true} check if item use consume item, {@code false} otherwise
	 */
	private boolean checkConsume(Item item, boolean hasConsumeSkill)
	{
		switch (item.getItem().getDefaultAction())
		{
			case CAPSULE:
			case SKILL_REDUCE:
			{
				if (!hasConsumeSkill && item.getItem().hasImmediateEffect())
				{
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * @param playable the character using the item or skill
	 * @param skill the skill being used, can be null
	 * @param item the item being used
	 * @return {@code true} if the the item or skill to check is available, {@code false} otherwise
	 */
	private boolean checkReuse(Playable playable, Skill skill, Item item)
	{
		final long remainingTime = (skill != null) ? playable.getSkillRemainingReuseTime(skill.getReuseHashCode()) : playable.getItemRemainingReuseTime(item.getObjectId());
		final boolean isAvailable = remainingTime <= 0;
		if (playable.isPlayer() && !isAvailable)
		{
			final int hours = (int) (remainingTime / 3600000);
			final int minutes = (int) (remainingTime % 3600000) / 60000;
			final int seconds = (int) ((remainingTime / 1000) % 60);
			SystemMessage sm = null;
			if (hours > 0)
			{
				sm = new SystemMessage(SystemMessageId.THERE_ARE_S2_HOUR_S_S3_MINUTE_S_AND_S4_SECOND_S_REMAINING_IN_S1_S_RE_USE_TIME);
				if ((skill == null) || skill.isStatic())
				{
					sm.addItemName(item);
				}
				else
				{
					sm.addSkillName(skill);
				}
				sm.addInt(hours);
				sm.addInt(minutes);
			}
			else if (minutes > 0)
			{
				sm = new SystemMessage(SystemMessageId.THERE_ARE_S2_MINUTE_S_S3_SECOND_S_REMAINING_IN_S1_S_RE_USE_TIME);
				if ((skill == null) || skill.isStatic())
				{
					sm.addItemName(item);
				}
				else
				{
					sm.addSkillName(skill);
				}
				sm.addInt(minutes);
			}
			else
			{
				sm = new SystemMessage(SystemMessageId.THERE_ARE_S2_SECOND_S_REMAINING_IN_S1_S_RE_USE_TIME);
				if ((skill == null) || skill.isStatic())
				{
					sm.addItemName(item);
				}
				else
				{
					sm.addSkillName(skill);
				}
			}
			sm.addInt(seconds);
			playable.sendPacket(sm);
		}
		return isAvailable;
	}
}
