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

import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.data.SkillTable;
import org.l2jmobius.gameserver.data.sql.NpcTable;
import org.l2jmobius.gameserver.data.xml.SummonItemData;
import org.l2jmobius.gameserver.enums.ItemLocation;
import org.l2jmobius.gameserver.handler.IItemHandler;
import org.l2jmobius.gameserver.instancemanager.IdManager;
import org.l2jmobius.gameserver.model.Skill;
import org.l2jmobius.gameserver.model.SummonItem;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Playable;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.instance.Pet;
import org.l2jmobius.gameserver.model.actor.templates.NpcTemplate;
import org.l2jmobius.gameserver.model.holders.SkillUseHolder;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.spawn.Spawn;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import org.l2jmobius.gameserver.network.serverpackets.MagicSkillLaunched;
import org.l2jmobius.gameserver.network.serverpackets.PetInfo;
import org.l2jmobius.gameserver.network.serverpackets.Ride;

public class SummonItems implements IItemHandler
{
	private static final int[] ITEM_IDS = SummonItemData.getInstance().getAllItemIds();
	
	@Override
	public void useItem(Playable playable, Item item)
	{
		if (!(playable instanceof Player))
		{
			return;
		}
		
		final Player player = (Player) playable;
		if (!player.getClient().getFloodProtectors().canUsePetSummonItem())
		{
			playable.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isOnEvent())
		{
			playable.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isSitting())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_MOVE_WHILE_SITTING);
			return;
		}
		
		if (player.isParalyzed())
		{
			player.sendMessage("You Cannot Use This While You Are Paralyzed");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.inObserverMode())
		{
			return;
		}
		
		if (player.isInOlympiadMode())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_USE_THAT_ITEM_IN_A_GRAND_OLYMPIAD_GAMES_MATCH);
			return;
		}
		
		final SummonItem sitem = SummonItemData.getInstance().getSummonItem(item.getItemId());
		if (((player.getPet() != null) || player.isMounted()) && sitem.isPetSummon())
		{
			player.sendPacket(SystemMessageId.YOU_ALREADY_HAVE_A_PET);
			return;
		}
		
		// Like L2OFF you can't summon pet in combat
		if (player.isAttackingNow() || player.isInCombat())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_SUMMON_DURING_COMBAT);
			return;
		}
		
		final int npcId = sitem.getNpcId();
		if (npcId == 0)
		{
			return;
		}
		
		final NpcTemplate npcTemplate = NpcTable.getInstance().getTemplate(npcId);
		if (npcTemplate == null)
		{
			return;
		}
		
		switch (sitem.getType())
		{
			case 0: // static summons (like christmas tree)
			{
				try
				{
					final Spawn spawn = new Spawn(npcTemplate);
					spawn.setId(IdManager.getInstance().getNextId());
					spawn.setX(player.getX());
					spawn.setY(player.getY());
					spawn.setZ(player.getZ());
					World.getInstance().storeObject(spawn.doSpawn());
					player.destroyItem("Summon", item.getObjectId(), 1, null, false);
					player.sendMessage("Created " + npcTemplate.getName() + " at x: " + spawn.getX() + " y: " + spawn.getY() + " z: " + spawn.getZ());
				}
				catch (Exception e)
				{
					player.sendMessage("Target is not ingame.");
				}
				break;
			}
			case 1: // pet summons
			{
				player.setTarget(player);
				// Skill 2046 used only for animation
				final Skill skill = SkillTable.getInstance().getSkill(2046, 1);
				player.useMagic(skill, true, true);
				player.sendPacket(SystemMessageId.SUMMONING_YOUR_PET);
				ThreadPool.schedule(new PetSummonFinalizer(player, npcTemplate, item), 4800);
				break;
			}
			case 2: // wyvern
			{
				if (!player.disarmWeapons())
				{
					return;
				}
				final Ride mount = new Ride(player.getObjectId(), Ride.ACTION_MOUNT, sitem.getNpcId());
				player.sendPacket(mount);
				player.broadcastPacket(mount);
				player.setMountType(mount.getMountType());
				player.setMountObjectID(item.getObjectId());
			}
		}
	}
	
	static class PetSummonFeedWait implements Runnable
	{
		private final Player _player;
		private final Pet _petSummon;
		
		PetSummonFeedWait(Player player, Pet petSummon)
		{
			_player = player;
			_petSummon = petSummon;
		}
		
		@Override
		public void run()
		{
			try
			{
				if (_petSummon.getCurrentFed() <= 0)
				{
					_petSummon.unSummon(_player);
				}
				else
				{
					_petSummon.startFeed(false);
				}
			}
			catch (Throwable e)
			{
			}
		}
	}
	
	static class PetSummonFinalizer implements Runnable
	{
		private final Player _player;
		private final Item _item;
		private final NpcTemplate _npcTemplate;
		
		PetSummonFinalizer(Player player, NpcTemplate npcTemplate, Item item)
		{
			_player = player;
			_npcTemplate = npcTemplate;
			_item = item;
		}
		
		@Override
		public void run()
		{
			try
			{
				final SkillUseHolder skill = _player.getCurrentSkill();
				if (!_player.isCastingNow() || ((skill != null) && (skill.getSkillId() != 2046)))
				{
					return;
				}
				
				_player.sendPacket(new MagicSkillLaunched(_player, 2046, 1));
				
				// check for summon item validity
				if ((_item == null) || (_item.getOwnerId() != _player.getObjectId()) || (_item.getItemLocation() != ItemLocation.INVENTORY))
				{
					return;
				}
				
				final Pet petSummon = Pet.spawnPet(_npcTemplate, _player, _item);
				if (petSummon == null)
				{
					return;
				}
				
				petSummon.setTitle(_player.getName());
				
				if (!petSummon.isRespawned())
				{
					petSummon.setCurrentHp(petSummon.getMaxHp());
					petSummon.setCurrentMp(petSummon.getMaxMp());
					petSummon.getStat().setExp(petSummon.getExpForThisLevel());
					petSummon.setCurrentFed(petSummon.getMaxFed());
				}
				
				petSummon.setRunning();
				
				if (!petSummon.isRespawned())
				{
					petSummon.store();
				}
				
				_player.setPet(petSummon);
				
				World.getInstance().storeObject(petSummon);
				petSummon.spawnMe(_player.getX() + 50, _player.getY() + 100, _player.getZ());
				_player.sendPacket(new PetInfo(petSummon));
				petSummon.startFeed(false);
				_item.setEnchantLevel(petSummon.getLevel());
				
				if (petSummon.getCurrentFed() <= 0)
				{
					ThreadPool.schedule(new PetSummonFeedWait(_player, petSummon), 60000);
				}
				else
				{
					petSummon.startFeed(false);
				}
				
				petSummon.setFollowStatus(true);
				petSummon.setShowSummonAnimation(false);
				petSummon.broadcastStatusUpdate();
			}
			catch (Throwable e)
			{
			}
		}
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}