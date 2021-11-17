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
package org.l2jmobius.gameserver.handler.skillhandlers;

import java.util.List;

import org.l2jmobius.Config;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.handler.ISkillHandler;
import org.l2jmobius.gameserver.model.Skill;
import org.l2jmobius.gameserver.model.Skill.SkillType;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Attackable;
import org.l2jmobius.gameserver.model.actor.Attackable.RewardItem;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.instance.Monster;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.InventoryUpdate;
import org.l2jmobius.gameserver.network.serverpackets.ItemList;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * @author l3x
 */
public class Harvest implements ISkillHandler
{
	private static final SkillType[] SKILL_TYPES =
	{
		SkillType.HARVEST
	};
	
	private Player _player;
	private Monster _target;
	
	@Override
	public void useSkill(Creature creature, Skill skill, List<Creature> targets)
	{
		if (!(creature instanceof Player))
		{
			return;
		}
		
		_player = (Player) creature;
		
		final List<Creature> targetList = skill.getTargetList(creature);
		final InventoryUpdate iu = Config.FORCE_INVENTORY_UPDATE ? null : new InventoryUpdate();
		if (targetList == null)
		{
			return;
		}
		
		for (WorldObject aTargetList : targetList)
		{
			if (!(aTargetList instanceof Monster))
			{
				continue;
			}
			
			_target = (Monster) aTargetList;
			if (_player != _target.getSeeder())
			{
				_player.sendPacket(new SystemMessage(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_HARVEST));
				continue;
			}
			
			boolean send = false;
			int total = 0;
			int cropId = 0;
			
			// TODO: check items and amount of items player harvest
			if (_target.isSeeded())
			{
				if (calcSuccess())
				{
					final List<RewardItem> items = _target.takeHarvest();
					if ((items != null) && !items.isEmpty())
					{
						for (Attackable.RewardItem ritem : items)
						{
							cropId = ritem.getItemId(); // Always got 1 type of crop as reward.
							if (_player.isInParty())
							{
								_player.getParty().distributeItem(_player, ritem, true, _target);
							}
							else
							{
								final Item item = _player.getInventory().addItem("Manor", ritem.getItemId(), ritem.getCount(), _player, _target);
								if (iu != null)
								{
									iu.addItem(item);
								}
								send = true;
								total += ritem.getCount();
							}
						}
						if (send)
						{
							SystemMessage smsg = new SystemMessage(SystemMessageId.YOU_HAVE_OBTAINED_S2_S1);
							smsg.addNumber(total);
							smsg.addItemName(cropId);
							_player.sendPacket(smsg);
							
							if (_player.getParty() != null)
							{
								smsg = new SystemMessage(SystemMessageId.S1_HARVESTED_S3_S2_S);
								smsg.addString(_player.getName());
								smsg.addNumber(total);
								smsg.addItemName(cropId);
								_player.getParty().broadcastToPartyMembers(_player, smsg);
							}
							
							if (iu != null)
							{
								_player.sendPacket(iu);
							}
							else
							{
								_player.sendPacket(new ItemList(_player, false));
							}
						}
					}
				}
				else
				{
					_player.sendPacket(SystemMessageId.THE_HARVEST_HAS_FAILED);
				}
			}
			else
			{
				_player.sendPacket(SystemMessageId.THE_HARVEST_FAILED_BECAUSE_THE_SEED_WAS_NOT_SOWN);
			}
		}
	}
	
	private boolean calcSuccess()
	{
		int basicSuccess = 100;
		final int levelPlayer = _player.getLevel();
		final int levelTarget = _target.getLevel();
		int diff = (levelPlayer - levelTarget);
		if (diff < 0)
		{
			diff = -diff;
		}
		
		// Apply penalty, target <=> player levels.
		// 5% penalty for each level.
		if (diff > 5)
		{
			basicSuccess -= (diff - 5) * 5;
		}
		
		// Success rate can't be less than 1%.
		if (basicSuccess < 1)
		{
			basicSuccess = 1;
		}
		
		return Rnd.get(99) < basicSuccess;
	}
	
	@Override
	public SkillType[] getSkillTypes()
	{
		return SKILL_TYPES;
	}
}
