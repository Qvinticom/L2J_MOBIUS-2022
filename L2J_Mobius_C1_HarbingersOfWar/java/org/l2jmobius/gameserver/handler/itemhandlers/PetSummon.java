/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.l2jmobius.gameserver.handler.itemhandlers;

import org.l2jmobius.gameserver.IdFactory;
import org.l2jmobius.gameserver.data.ExperienceTable;
import org.l2jmobius.gameserver.data.NpcTable;
import org.l2jmobius.gameserver.handler.IItemHandler;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.instance.ItemInstance;
import org.l2jmobius.gameserver.model.actor.instance.PetInstance;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.serverpackets.MagicSkillLaunched;
import org.l2jmobius.gameserver.network.serverpackets.MagicSkillUser;
import org.l2jmobius.gameserver.network.serverpackets.NpcInfo;
import org.l2jmobius.gameserver.network.serverpackets.PetInfo;
import org.l2jmobius.gameserver.network.serverpackets.PetItemList;
import org.l2jmobius.gameserver.templates.Npc;

public class PetSummon implements IItemHandler
{
	private static int[] _itemIds = new int[]
	{
		2375,
		3500,
		3501,
		3502
	};
	
	@Override
	public int useItem(PlayerInstance activeChar, ItemInstance item)
	{
		int npcId;
		if (activeChar.getPet() != null)
		{
			return 0;
		}
		switch (item.getItemId())
		{
			case 2375:
			{
				npcId = 12077;
				break;
			}
			case 3500:
			{
				npcId = 12311;
				break;
			}
			case 3501:
			{
				npcId = 12312;
				break;
			}
			case 3502:
			{
				npcId = 12313;
				break;
			}
			default:
			{
				return 0;
			}
		}
		Npc petTemplate = NpcTable.getInstance().getTemplate(npcId);
		PetInstance newpet = new PetInstance(petTemplate);
		newpet.setTitle(activeChar.getName());
		newpet.setControlItemId(item.getObjectId());
		newpet.setObjectId(IdFactory.getInstance().getNextId());
		newpet.setX(activeChar.getX() + 50);
		newpet.setY(activeChar.getY() + 100);
		newpet.setZ(activeChar.getZ());
		newpet.setLevel(petTemplate.getLevel());
		newpet.setExp(ExperienceTable.getInstance().getExp(newpet.getLevel()));
		newpet.setLastLevel(ExperienceTable.getInstance().getExp(newpet.getLevel()));
		newpet.setNextLevel(ExperienceTable.getInstance().getExp(newpet.getLevel() + 1));
		newpet.setMaxHp(petTemplate.getHp());
		newpet.setSummonHp(petTemplate.getHp());
		newpet.setWalkSpeed(petTemplate.getWalkSpeed());
		newpet.setRunSpeed(petTemplate.getRunSpeed());
		newpet.setPhysicalAttack(petTemplate.getPatk());
		newpet.setPhysicalDefense(petTemplate.getPdef());
		newpet.setHeading(activeChar.getHeading());
		newpet.setMovementMultiplier(1.08);
		newpet.setAttackSpeedMultiplier(0.9983664);
		newpet.setAttackRange(petTemplate.getAttackRange());
		newpet.setRunning(true);
		World.getInstance().storeObject(newpet);
		World.getInstance().addVisibleObject(newpet);
		MagicSkillUser msk = new MagicSkillUser(activeChar, 2046, 1, 1000, 600000);
		activeChar.sendPacket(msk);
		PetInfo ownerni = new PetInfo(newpet);
		NpcInfo ni = new NpcInfo(newpet);
		activeChar.broadcastPacket(ni);
		activeChar.sendPacket(ownerni);
		activeChar.sendPacket(new PetItemList(newpet));
		try
		{
			Thread.sleep(900L);
		}
		catch (InterruptedException e)
		{
			// empty catch block
		}
		activeChar.sendPacket(new MagicSkillLaunched(activeChar, 2046, 1));
		activeChar.setPet(newpet);
		newpet.setOwner(activeChar);
		newpet.addKnownObject(activeChar);
		newpet.setFollowStatus(true);
		newpet.followOwner(activeChar);
		return 0;
	}
	
	@Override
	public int[] getItemIds()
	{
		return _itemIds;
	}
}
