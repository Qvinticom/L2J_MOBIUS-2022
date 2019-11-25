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
package org.l2jmobius.gameserver.network.serverpackets;

import org.l2jmobius.gameserver.model.Inventory;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;

public class CharInfo extends ServerBasePacket
{
	private final PlayerInstance _cha;
	
	public CharInfo(PlayerInstance cha)
	{
		_cha = cha;
	}
	
	@Override
	public void writeImpl()
	{
		writeC(0x03);
		writeD(_cha.getX());
		writeD(_cha.getY());
		writeD(_cha.getZ());
		writeD(_cha.getHeading());
		writeD(_cha.getObjectId());
		writeS(_cha.getName());
		writeD(_cha.getRace());
		writeD(_cha.getSex());
		writeD(_cha.getClassId());
		writeD(0);
		writeD(_cha.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_HEAD));
		writeD(_cha.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_RHAND));
		writeD(_cha.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LHAND));
		writeD(_cha.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_GLOVES));
		writeD(_cha.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_CHEST));
		writeD(_cha.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LEGS));
		writeD(_cha.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_FEET));
		writeD(_cha.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_BACK));
		writeD(_cha.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LRHAND));
		writeD(_cha.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_UNDER));
		writeD(0);
		writeD(_cha.getMagicalSpeed());
		writeD(_cha.getPhysicalSpeed());
		writeD(_cha.getPvpFlag());
		writeD(_cha.getKarma());
		writeD(_cha.getRunSpeed());
		writeD(_cha.getWalkSpeed());
		writeD(50);
		writeD(50);
		writeD(_cha.getFloatingRunSpeed());
		writeD(_cha.getFloatingWalkSpeed());
		writeD(_cha.getFlyingRunSpeed());
		writeD(_cha.getFlyingWalkSpeed());
		writeF(_cha.getMovementMultiplier());
		writeF(_cha.getAttackSpeedMultiplier());
		writeF(_cha.getCollisionRadius());
		writeF(_cha.getCollisionHeight());
		writeD(_cha.getHairStyle());
		writeD(_cha.getHairColor());
		writeD(_cha.getFace());
		writeS(_cha.getTitle());
		writeD(_cha.getClanId());
		writeD(_cha.getClanId());
		writeD(16);
		writeD(_cha.getAllyId());
		writeD(0);
		writeC(_cha.getWaitType());
		writeC(_cha.getMoveType());
		if (_cha.isInCombat())
		{
			writeC(1);
		}
		else
		{
			writeC(0);
		}
		if (_cha.isDead())
		{
			writeC(1);
		}
		else
		{
			writeC(0);
		}
		writeC(0);
		writeC(0);
		writeC(0);
		writeH(0);
		writeC(0);
	}
}
