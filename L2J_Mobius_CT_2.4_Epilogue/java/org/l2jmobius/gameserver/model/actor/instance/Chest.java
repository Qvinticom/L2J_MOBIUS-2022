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
package org.l2jmobius.gameserver.model.actor.instance;

import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.data.xml.NpcData;
import org.l2jmobius.gameserver.data.xml.SkillData;
import org.l2jmobius.gameserver.enums.InstanceType;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.templates.NpcTemplate;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.network.serverpackets.MagicSkillUse;

/**
 * This class manages all chest.
 * @author Julian
 */
public class Chest extends Monster
{
	private volatile boolean _isInteracted;
	private volatile boolean _specialDrop;
	
	/**
	 * Creates a chest.
	 * @param template the chest NPC template
	 */
	public Chest(NpcTemplate template)
	{
		super(template);
		setInstanceType(InstanceType.Chest);
		setRandomWalking(false);
		_specialDrop = false;
	}
	
	@Override
	public void onSpawn()
	{
		super.onSpawn();
		_isInteracted = false;
		_specialDrop = false;
		setMustRewardExpSp(true);
	}
	
	public synchronized boolean isInteracted()
	{
		return _isInteracted;
	}
	
	public synchronized void setInteracted()
	{
		_isInteracted = true;
	}
	
	public synchronized void setSpecialDrop()
	{
		_specialDrop = true;
	}
	
	@Override
	public void doItemDrop(NpcTemplate npcTemplate, Creature lastAttacker)
	{
		int id = getTemplate().getId();
		if (!_specialDrop)
		{
			if ((id >= 18265) && (id <= 18286))
			{
				id += 3536;
			}
			else if ((id == 18287) || (id == 18288))
			{
				id = 21671;
			}
			else if ((id == 18289) || (id == 18290))
			{
				id = 21694;
			}
			else if ((id == 18291) || (id == 18292))
			{
				id = 21717;
			}
			else if ((id == 18293) || (id == 18294))
			{
				id = 21740;
			}
			else if ((id == 18295) || (id == 18296))
			{
				id = 21763;
			}
			else if ((id == 18297) || (id == 18298))
			{
				id = 21786;
			}
		}
		super.doItemDrop(NpcData.getInstance().getTemplate(id), lastAttacker);
	}
	
	// cast - trap chest
	public void chestTrap(Creature player)
	{
		int trapSkillId = 0;
		final int rnd = Rnd.get(120);
		if (getTemplate().getLevel() >= 61)
		{
			if (rnd >= 90)
			{
				trapSkillId = 4139; // explosion
			}
			else if (rnd >= 50)
			{
				trapSkillId = 4118; // area paralysys
			}
			else if (rnd >= 20)
			{
				trapSkillId = 1167; // poison cloud
			}
			else
			{
				trapSkillId = 223; // sting
			}
		}
		else if (getTemplate().getLevel() >= 41)
		{
			if (rnd >= 90)
			{
				trapSkillId = 4139; // explosion
			}
			else if (rnd >= 60)
			{
				trapSkillId = 96; // bleed
			}
			else if (rnd >= 20)
			{
				trapSkillId = 1167; // poison cloud
			}
			else
			{
				trapSkillId = 4118; // area paralysys
			}
		}
		else if (getTemplate().getLevel() >= 21)
		{
			if (rnd >= 80)
			{
				trapSkillId = 4139; // explosion
			}
			else if (rnd >= 50)
			{
				trapSkillId = 96; // bleed
			}
			else if (rnd >= 20)
			{
				trapSkillId = 1167; // poison cloud
			}
			else
			{
				trapSkillId = 129; // poison
			}
		}
		else
		{
			if (rnd >= 80)
			{
				trapSkillId = 4139; // explosion
			}
			else if (rnd >= 50)
			{
				trapSkillId = 96; // bleed
			}
			else
			{
				trapSkillId = 129; // poison
			}
		}
		
		player.sendMessage("There was a trap!");
		handleCast(player, trapSkillId);
	}
	
	// <--
	// cast casse
	// <--
	private boolean handleCast(Creature player, int skillId)
	{
		int skillLevel = 1;
		final byte level = getTemplate().getLevel();
		if ((level > 20) && (level <= 40))
		{
			skillLevel = 3;
		}
		else if ((level > 40) && (level <= 60))
		{
			skillLevel = 5;
		}
		else if (level > 60)
		{
			skillLevel = 6;
		}
		
		if (player.isDead() || player.isInvisible() || !player.isInsideRadius2D(this, 1500))
		{
			return false;
		}
		
		final Skill skill = SkillData.getInstance().getSkill(skillId, skillLevel);
		if (!player.isAffectedBySkill(skillId))
		{
			skill.applyEffects(this, player);
			broadcastPacket(new MagicSkillUse(this, player, skillId, skillLevel, skill.getHitTime(), 0));
			return true;
		}
		return false;
	}
	
	@Override
	public boolean isMovementDisabled()
	{
		return true;
	}
	
	@Override
	public boolean hasRandomAnimation()
	{
		return false;
	}
}
