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
package com.l2jmobius.gameserver.handler.skillhandlers;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.datatables.ZoneTable;
import com.l2jmobius.gameserver.geodata.GeoData;
import com.l2jmobius.gameserver.handler.ISkillHandler;
import com.l2jmobius.gameserver.model.Inventory;
import com.l2jmobius.gameserver.model.L2Character;
import com.l2jmobius.gameserver.model.L2ItemInstance;
import com.l2jmobius.gameserver.model.L2Object;
import com.l2jmobius.gameserver.model.L2Skill;
import com.l2jmobius.gameserver.model.L2Skill.SkillType;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.zone.L2ZoneType;
import com.l2jmobius.gameserver.model.zone.type.L2FishingZone;
import com.l2jmobius.gameserver.model.zone.type.L2WaterZone;
import com.l2jmobius.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import com.l2jmobius.gameserver.templates.L2Weapon;
import com.l2jmobius.gameserver.templates.L2WeaponType;
import com.l2jmobius.gameserver.util.Util;
import com.l2jmobius.util.Rnd;

public class Fishing implements ISkillHandler
{
	// private static Logger _log = Logger.getLogger(SiegeFlag.class.getName());
	protected SkillType[] _skillIds =
	{
		SkillType.FISHING
	};
	
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets, boolean crit)
	{
		if ((activeChar == null) || !(activeChar instanceof L2PcInstance))
		{
			return;
		}
		
		final L2PcInstance player = (L2PcInstance) activeChar;
		
		/*
		 * If fishing is disabled, there isn't much point in doing anything else, unless you are GM. so this got moved up here, before anything else.
		 */
		if (!Config.ALLOWFISHING && !player.isGM())
		{
			player.sendMessage("Fishing server is currently offline.");
			return;
		}
		
		if (player.isFishing())
		
		{
			
			if (player.isFishing())
			{
				if (player.getFishCombat() != null)
				{
					player.getFishCombat().doDie(false);
				}
				else
				{
					player.endFishing(false);
				}
				
				// Cancels fishing
				player.sendPacket(new SystemMessage(SystemMessage.FISHING_ATTEMPT_CANCELLED));
				return;
			}
		}
		
		final L2Weapon weaponItem = player.getActiveWeaponItem();
		if ((weaponItem == null) || (weaponItem.getItemType() != L2WeaponType.ROD))
		{
			// Fishing poles are not equipped
			player.sendPacket(new SystemMessage(SystemMessage.FISHING_POLE_NOT_EQUIPPED));
			return;
		}
		
		final L2ItemInstance lure = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
		if (lure == null)
		{
			// Bait not equiped.
			player.sendPacket(new SystemMessage(SystemMessage.BAIT_ON_HOOK_BEFORE_FISHING));
			
			return;
		}
		
		player.setLure(lure);
		L2ItemInstance lure2 = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
		
		if ((lure2 == null) || (lure2.getCount() < 1)) // Not enough bait.
		{
			
			player.sendPacket(new SystemMessage(SystemMessage.NOT_ENOUGH_BAIT));
			
			return;
		}
		
		if (player.isInBoat())
		{
			// You can't fish while you are on boat
			player.sendPacket(new SystemMessage(SystemMessage.CANNOT_FISH_ON_BOAT));
			
			return;
		}
		
		if (player.isInCraftMode() || player.isInStoreMode())
		
		{
			
			player.sendPacket(new SystemMessage(SystemMessage.CANNOT_FISH_WHILE_USING_RECIPE_BOOK));
			
			return;
		}
		
		if (player.isInsideZone(L2Character.ZONE_WATER))
		{
			// You can't fish in water
			player.sendPacket(new SystemMessage(SystemMessage.CANNOT_FISH_UNDER_WATER));
			return;
		}
		
		final int rnd = Rnd.get(150) + 50;
		final double angle = Util.convertHeadingToDegree(player.getHeading());
		final double radian = Math.toRadians(angle);
		final double sin = Math.sin(radian);
		final double cos = Math.cos(radian);
		final int x = player.getX() + (int) (cos * rnd);
		final int y = player.getY() + (int) (sin * rnd);
		int z = player.getZ() + 50;
		
		/*
		 * ...and if the spot is in a fishing zone. If it is, it will then position the hook on the water surface. If not, you have to be GM to proceed past here... in that case, the hook will be positioned using the old Z lookup method.
		 */
		L2FishingZone aimingTo = null;
		L2WaterZone water = null;
		boolean canFish = false;
		for (final L2ZoneType zone : ZoneTable.getInstance().getZones(x, y))
		{
			if (zone instanceof L2FishingZone)
			{
				aimingTo = (L2FishingZone) zone;
				continue;
			}
			
			if (zone instanceof L2WaterZone)
			{
				water = (L2WaterZone) zone;
			}
		}
		
		if (aimingTo != null)
		{
			// fishing zone found, we can fish here
			if (Config.PATHFINDING > 0)
			{
				// geodata enabled, checking if we can see end of the pole
				if (GeoData.getInstance().canSeeTarget(player.getX(), player.getY(), z, x, y, z))
				{
					// finding z level for hook
					if (water != null)
					{
						// water zone exist
						if (GeoData.getInstance().getHeight(x, y, z) < water.getWaterZ())
						{
							// water Z is higher than geo Z
							z = water.getWaterZ() + 10;
							canFish = true;
						}
					}
					else
					{
						// no water zone, using fishing zone
						if (GeoData.getInstance().getHeight(x, y, z) < aimingTo.getWaterZ())
						{
							// fishing Z is higher than geo Z
							z = aimingTo.getWaterZ() + 10;
							canFish = true;
						}
					}
				}
			}
			else
			{
				// geodata disabled
				// if water zone exist using it, if not - using fishing zone
				if (water != null)
				{
					z = water.getWaterZ() + 10;
				}
				else
				{
					z = aimingTo.getWaterZ() + 10;
				}
				canFish = true;
			}
		}
		
		if (!canFish)
		{
			// You can't fish here
			player.sendPacket(new SystemMessage(SystemMessage.CANNOT_FISH_HERE));
			return;
		}
		
		// Has enough bait, consume 1 and update inventory. Start fishing follows.
		
		lure2 = player.getInventory().destroyItem("Consume", player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LHAND), 1, player, null);
		final InventoryUpdate iu = new InventoryUpdate();
		iu.addModifiedItem(lure2);
		player.sendPacket(iu);
		// If everything else checks out, actually cast the hook and start fishing
		player.startFishing(x, y, z);
	}
	
	@Override
	public SkillType[] getSkillIds()
	{
		return _skillIds;
	}
}