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

import org.l2jmobius.Config;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.geoengine.GeoEngine;
import org.l2jmobius.gameserver.handler.ISkillHandler;
import org.l2jmobius.gameserver.instancemanager.FishingZoneManager;
import org.l2jmobius.gameserver.model.Inventory;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.Skill;
import org.l2jmobius.gameserver.model.Skill.SkillType;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.instance.ItemInstance;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.items.Weapon;
import org.l2jmobius.gameserver.model.items.type.WeaponType;
import org.l2jmobius.gameserver.model.zone.type.FishingZone;
import org.l2jmobius.gameserver.model.zone.type.WaterZone;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.InventoryUpdate;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.util.Util;

public class Fishing implements ISkillHandler
{
	private static final SkillType[] SKILL_IDS =
	{
		SkillType.FISHING
	};
	
	@Override
	public void useSkill(Creature creature, Skill skill, WorldObject[] targets)
	{
		if ((creature == null) || !(creature instanceof PlayerInstance))
		{
			return;
		}
		
		final PlayerInstance player = (PlayerInstance) creature;
		
		// If fishing is disabled, there isn't much point in doing anything else, unless you are GM. so this got moved up here, before anything else.
		if (!Config.ALLOWFISHING && !player.isGM())
		{
			player.sendMessage("Fishing server is currently ofline");
			return;
		}
		
		if (player.isFishing())
		{
			if (player.GetFishCombat() != null)
			{
				player.GetFishCombat().doDie(false);
			}
			else
			{
				player.EndFishing(false);
			}
			// Cancels fishing
			player.sendPacket(SystemMessageId.FISHING_ATTEMPT_CANCELLED);
			return;
		}
		
		Weapon weaponItem = player.getActiveWeaponItem();
		if (((weaponItem == null) || (weaponItem.getItemType() != WeaponType.ROD)))
		{
			return;
		}
		
		ItemInstance lure = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
		if (lure == null)
		{
			// Bait not equiped.
			player.sendPacket(SystemMessageId.CANNOT_FISH_HERE);
			final SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
			sm.addString(skill.getName());
			player.sendPacket(sm);
			return;
		}
		
		player.SetLure(lure);
		ItemInstance lure2 = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
		
		if ((lure2 == null) || (lure2.getCount() < 1)) // Not enough bait.
		{
			player.sendPacket(SystemMessageId.NOT_ENOUGH_BAIT);
			return;
		}
		
		if (player.isInBoat())
		{
			// You can't fish while you are on boat
			player.sendPacket(SystemMessageId.CANNOT_FISH_ON_BOAT);
			return;
		}
		
		if (player.isCrafting() || player.isInStoreMode())
		{
			player.sendPacket(SystemMessageId.CANNOT_FISH_WHILE_USING_RECIPE_BOOK);
			// if(!player.isGM())
			return;
		}
		
		// If fishing is enabled, here is the code that was striped from startFishing() in PlayerInstance. Decide now where will the hook be cast...
		final int rnd = Rnd.get(200) + 200;
		final double angle = Util.convertHeadingToDegree(player.getHeading());
		final double radian = Math.toRadians(angle);
		final double sin = Math.sin(radian);
		final double cos = Math.cos(radian);
		final int x1 = (int) (cos * rnd);
		final int y1 = (int) (sin * rnd);
		final int x = player.getX() + x1;
		final int y = player.getY() + y1;
		int z = player.getZ() - 30;
		// ...and if the spot is in a fishing zone. If it is, it will then position the hook on the water surface. If not, you have to be GM to proceed past here... in that case, the hook will be positioned using the old Z lookup method.
		FishingZone aimingTo = FishingZoneManager.getInstance().isInsideFishingZone(x, y, z);
		WaterZone water = FishingZoneManager.getInstance().isInsideWaterZone(x, y, z);
		if ((water != null))
		{
			final Location waterLocation = new Location(x, y, water.getWaterZ() - 50);
			if ((aimingTo != null) && GeoEngine.getInstance().canSeeTarget(player, waterLocation))
			{
				z = water.getWaterZ() + 10;
			}
			else if ((aimingTo != null) && GeoEngine.getInstance().canSeeTarget(player, waterLocation))
			{
				z = aimingTo.getWaterZ() + 10;
			}
		}
		else
		{
			// You can't fish here
			player.sendPacket(SystemMessageId.BAIT_ON_HOOK_BEFORE_FISHING);
			final SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
			sm.addString(skill.getName());
			player.sendPacket(sm);
			return;
		}
		
		// Of course since you can define fishing water volumes of any height, the function needs to be changed to cope with that. Still, this is assuming that fishing zones water surfaces, are always above "sea level".
		if ((player.getZ() <= -3800) || (player.getZ() < (z - 32)))
		{
			// You can't fish in water
			player.sendPacket(SystemMessageId.CANNOT_FISH_UNDER_WATER);
			return;
		}
		// Has enough bait, consume 1 and update inventory. Start fishing follows.
		lure2 = player.getInventory().destroyItem("Consume", player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LHAND), 1, player, null);
		InventoryUpdate iu = new InventoryUpdate();
		iu.addModifiedItem(lure2);
		player.sendPacket(iu);
		// If everything else checks out, actually cast the hook and start fishing... :P
		player.startFishing(x, y, z);
	}
	
	@Override
	public SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
