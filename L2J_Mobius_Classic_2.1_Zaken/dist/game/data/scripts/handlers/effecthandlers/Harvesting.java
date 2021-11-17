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
package handlers.effecthandlers;

import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.model.Party;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.instance.Monster;
import org.l2jmobius.gameserver.model.effects.AbstractEffect;
import org.l2jmobius.gameserver.model.holders.ItemHolder;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.skills.Skill;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * Harvesting effect implementation.
 * @author l3x, Zoey76
 */
public class Harvesting extends AbstractEffect
{
	public Harvesting(StatSet params)
	{
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public void instant(Creature effector, Creature effected, Skill skill, Item item)
	{
		if (!effector.isPlayer() || !effected.isMonster() || !effected.isDead())
		{
			return;
		}
		
		final Player player = effector.getActingPlayer();
		final Monster monster = (Monster) effected;
		if (player.getObjectId() != monster.getSeederId())
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_HARVEST);
		}
		else if (monster.isSeeded())
		{
			if (calcSuccess(player, monster))
			{
				final ItemHolder harvestedItem = monster.takeHarvest();
				if (harvestedItem != null)
				{
					// Add item
					player.getInventory().addItem("Harvesting", harvestedItem.getId(), harvestedItem.getCount(), player, monster);
					
					// Send system msg
					SystemMessage sm = null;
					if (item.getCount() == 1)
					{
						sm = new SystemMessage(SystemMessageId.YOU_HAVE_OBTAINED_S1);
						sm.addItemName(harvestedItem.getId());
					}
					else
					{
						sm = new SystemMessage(SystemMessageId.YOU_HAVE_OBTAINED_S2_S1);
						sm.addItemName(item.getId());
						sm.addLong(harvestedItem.getCount());
					}
					player.sendPacket(sm);
					
					// Send msg to party
					final Party party = player.getParty();
					if (party != null)
					{
						if (item.getCount() == 1)
						{
							sm = new SystemMessage(SystemMessageId.C1_HAS_OBTAINED_S2_2);
							sm.addString(player.getName());
							sm.addItemName(harvestedItem.getId());
						}
						else
						{
							sm = new SystemMessage(SystemMessageId.C1_HARVESTED_S3_S2_S);
							sm.addString(player.getName());
							sm.addLong(harvestedItem.getCount());
							sm.addItemName(harvestedItem.getId());
						}
						party.broadcastToPartyMembers(player, sm);
					}
				}
			}
			else
			{
				player.sendPacket(SystemMessageId.THE_HARVEST_HAS_FAILED);
			}
		}
		else
		{
			player.sendPacket(SystemMessageId.THE_HARVEST_FAILED_BECAUSE_THE_SEED_WAS_NOT_SOWN);
		}
	}
	
	private static boolean calcSuccess(Player player, Monster target)
	{
		final int levelPlayer = player.getLevel();
		final int levelTarget = target.getLevel();
		
		int diff = (levelPlayer - levelTarget);
		if (diff < 0)
		{
			diff = -diff;
		}
		
		// apply penalty, target <=> player levels
		// 5% penalty for each level
		int basicSuccess = 100;
		if (diff > 5)
		{
			basicSuccess -= (diff - 5) * 5;
		}
		
		// success rate can't be less than 1%
		if (basicSuccess < 1)
		{
			basicSuccess = 1;
		}
		return Rnd.get(99) < basicSuccess;
	}
}
