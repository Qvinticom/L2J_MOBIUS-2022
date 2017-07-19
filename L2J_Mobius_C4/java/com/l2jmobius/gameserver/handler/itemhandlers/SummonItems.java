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
package com.l2jmobius.gameserver.handler.itemhandlers;

import com.l2jmobius.gameserver.GameTimeController;
import com.l2jmobius.gameserver.ThreadPoolManager;
import com.l2jmobius.gameserver.datatables.NpcTable;
import com.l2jmobius.gameserver.datatables.SkillTable;
import com.l2jmobius.gameserver.datatables.SummonItemsData;
import com.l2jmobius.gameserver.handler.IItemHandler;
import com.l2jmobius.gameserver.idfactory.IdFactory;
import com.l2jmobius.gameserver.model.L2ItemInstance;
import com.l2jmobius.gameserver.model.L2Object;
import com.l2jmobius.gameserver.model.L2Skill;
import com.l2jmobius.gameserver.model.L2Spawn;
import com.l2jmobius.gameserver.model.L2SummonItem;
import com.l2jmobius.gameserver.model.L2World;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PetInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jmobius.gameserver.network.serverpackets.MagicSkillLaunched;
import com.l2jmobius.gameserver.network.serverpackets.MagicSkillUse;
import com.l2jmobius.gameserver.network.serverpackets.PetItemList;
import com.l2jmobius.gameserver.network.serverpackets.SetupGauge;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import com.l2jmobius.gameserver.templates.L2NpcTemplate;

/**
 * @author FBIagent
 */
public class SummonItems implements IItemHandler
{
	@Override
	public void useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
		{
			return;
		}
		
		final L2PcInstance activeChar = (L2PcInstance) playable;
		
		if (!activeChar.getFloodProtectors().getItemPetSummon().tryPerformAction("summon items"))
		{
			return;
		}
		
		if (activeChar.isSitting())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.CANT_MOVE_SITTING));
			return;
		}
		
		if (activeChar.inObserverMode())
		{
			return;
		}
		
		if (activeChar.isInOlympiadMode())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT));
			return;
		}
		
		if (activeChar.getEventTeam() > 0)
		{
			activeChar.sendMessage("You may not summon a pet in TvT Event.");
			return;
		}
		
		if (activeChar.isAllSkillsDisabled())
		{
			return;
		}
		
		final L2SummonItem sitem = SummonItemsData.getInstance().getSummonItem(item.getItemId());
		
		if (((activeChar.getPet() != null) || activeChar.isMounted()) && sitem.isPetSummon())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_ALREADY_HAVE_A_PET));
			return;
		}
		
		if (activeChar.isAttackingNow() || activeChar.isRooted())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_CANNOT_SUMMON_IN_COMBAT));
			return;
		}
		
		final int npcID = sitem.getNpcId();
		if (npcID == 0)
		{
			return;
		}
		
		final L2NpcTemplate npcTemplate = NpcTable.getInstance().getTemplate(npcID);
		if (npcTemplate == null)
		{
			return;
		}
		
		activeChar.stopMove(null);
		
		switch (sitem.getType())
		{
			case 0: // static summons (like christmas tree)
				try
				{
					final L2Spawn spawn = new L2Spawn(npcTemplate);
					// if (spawn == null)
					// {
					// return;
					// }
					
					spawn.setId(IdFactory.getInstance().getNextId());
					spawn.setLocx(activeChar.getX());
					spawn.setLocy(activeChar.getY());
					spawn.setLocz(activeChar.getZ());
					L2World.getInstance().storeObject(spawn.spawnOne());
					activeChar.destroyItem("Summon", item.getObjectId(), 1, null, false);
					activeChar.sendMessage("Created " + npcTemplate.name + " at x: " + spawn.getLocx() + " y: " + spawn.getLocy() + " z: " + spawn.getLocz());
				}
				catch (final Exception e)
				{
					activeChar.sendMessage("Target is not in game.");
				}
				break;
			case 1: // pet summons
				final L2Object oldtarget = activeChar.getTarget();
				activeChar.setTarget(activeChar);
				
				final L2Skill skill = SkillTable.getInstance().getInfo(2046, 1);
				if (skill == null)
				{
					return;
				}
				
				activeChar.broadcastPacket(new MagicSkillUse(activeChar, skill.getId(), 1, skill.getHitTime(), 0));
				activeChar.setTarget(oldtarget);
				activeChar.sendPacket(new SetupGauge(0, 5000));
				activeChar.sendPacket(new SystemMessage(SystemMessage.SUMMON_A_PET));
				
				activeChar.disableAllSkills();
				
				final PetSummonFinalizer psf = new PetSummonFinalizer(activeChar, npcTemplate, item);
				activeChar.setSkillCast(ThreadPoolManager.getInstance().scheduleEffect(psf, skill.getHitTime()));
				activeChar.setSkillCastEndTime(10 + GameTimeController.getGameTicks() + (skill.getHitTime() / GameTimeController.MILLIS_IN_TICK));
				break;
			case 2: // wyvern
				activeChar.mount(sitem.getNpcId(), item.getObjectId(), true);
				break;
		}
	}
	
	static class PetSummonFeedWait implements Runnable
	{
		private final L2PcInstance _activeChar;
		private final L2PetInstance _petSummon;
		
		PetSummonFeedWait(L2PcInstance activeChar, L2PetInstance petSummon)
		{
			_activeChar = activeChar;
			_petSummon = petSummon;
		}
		
		@Override
		public void run()
		{
			try
			{
				if (_petSummon.getCurrentFed() <= 0)
				{
					_petSummon.unSummon(_activeChar);
				}
				else
				{
					_petSummon.startFeed();
				}
			}
			catch (final Throwable e)
			{
			}
		}
	}
	
	static class PetSummonFinalizer implements Runnable
	{
		private final L2PcInstance _activeChar;
		private final L2ItemInstance _item;
		private final L2NpcTemplate _npcTemplate;
		
		PetSummonFinalizer(L2PcInstance activeChar, L2NpcTemplate npcTemplate, L2ItemInstance item)
		{
			_activeChar = activeChar;
			_npcTemplate = npcTemplate;
			_item = item;
		}
		
		@Override
		public void run()
		{
			try
			{
				_activeChar.sendPacket(new MagicSkillLaunched(_activeChar, 2046, 1));
				
				_activeChar.enableAllSkills();
				
				final L2PetInstance petSummon = L2PetInstance.spawnPet(_npcTemplate, _activeChar, _item);
				if (petSummon == null)
				{
					return;
				}
				
				petSummon.setTitle(_activeChar.getName());
				
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
				
				_activeChar.setPet(petSummon);
				
				L2World.getInstance().storeObject(petSummon);
				petSummon.spawnMe(_activeChar.getX() + 50, _activeChar.getY() + 100, _activeChar.getZ());
				petSummon.startFeed();
				_item.setEnchantLevel(petSummon.getLevel());
				
				if (petSummon.getCurrentFed() <= 0)
				{
					ThreadPoolManager.getInstance().scheduleGeneral(new PetSummonFeedWait(_activeChar, petSummon), 60000);
				}
				else
				{
					petSummon.startFeed();
				}
				
				petSummon.setFollowStatus(true);
				petSummon.setShowSummonAnimation(false);
				final int weaponId = petSummon.getWeapon();
				final int armorId = petSummon.getArmor();
				final int jewelId = petSummon.getJewel();
				if ((weaponId > 0) && (petSummon.getOwner().getInventory().getItemByItemId(weaponId) != null))
				{
					final L2ItemInstance item = petSummon.getOwner().getInventory().getItemByItemId(weaponId);
					final L2ItemInstance newItem = petSummon.getOwner().transferItem("Transfer", item.getObjectId(), 1, petSummon.getInventory(), petSummon);
					
					if (newItem == null)
					{
						petSummon.setWeapon(0);
					}
					else
					{
						petSummon.getInventory().equipItem(newItem);
					}
				}
				else
				{
					petSummon.setWeapon(0);
				}
				
				if ((armorId > 0) && (petSummon.getOwner().getInventory().getItemByItemId(armorId) != null))
				{
					final L2ItemInstance item = petSummon.getOwner().getInventory().getItemByItemId(armorId);
					final L2ItemInstance newItem = petSummon.getOwner().transferItem("Transfer", item.getObjectId(), 1, petSummon.getInventory(), petSummon);
					
					if (newItem == null)
					{
						petSummon.setArmor(0);
					}
					else
					{
						petSummon.getInventory().equipItem(newItem);
					}
				}
				else
				{
					petSummon.setArmor(0);
				}
				
				if ((jewelId > 0) && (petSummon.getOwner().getInventory().getItemByItemId(jewelId) != null))
				{
					final L2ItemInstance item = petSummon.getOwner().getInventory().getItemByItemId(jewelId);
					final L2ItemInstance newItem = petSummon.getOwner().transferItem("Transfer", item.getObjectId(), 1, petSummon.getInventory(), petSummon);
					
					if (newItem == null)
					{
						petSummon.setJewel(0);
					}
					else
					{
						petSummon.getInventory().equipItem(newItem);
					}
				}
				else
				{
					petSummon.setJewel(0);
				}
				
				petSummon.getOwner().sendPacket(new PetItemList(petSummon));
				petSummon.broadcastStatusUpdate();
			}
			catch (final Throwable e)
			{
			}
		}
	}
	
	@Override
	public int[] getItemIds()
	{
		return SummonItemsData.getInstance().itemIDs();
	}
}