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

import org.l2jmobius.gameserver.model.actor.instance.PetInstance;

public class PetInfo extends ServerBasePacket
{
	private final PetInstance _cha;
	
	public PetInfo(PetInstance cha)
	{
		_cha = cha;
	}
	
	@Override
	public void writeImpl()
	{
		writeC(0xCA);
		writeD(_cha.getPetId());
		writeD(_cha.getObjectId());
		writeD(_cha.getNpcId() + 1000000);
		writeD(0);
		writeD(_cha.getX());
		writeD(_cha.getY());
		writeD(_cha.getZ());
		writeD(_cha.getHeading());
		writeD(0);
		writeD(_cha.getMagicalSpeed());
		writeD(_cha.getPhysicalSpeed());
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
		writeD(0);
		writeD(0);
		writeD(0);
		writeC(1);
		if (_cha.isRunning())
		{
			writeC(1);
		}
		else
		{
			writeC(0);
		}
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
		if (_cha.getOwner() == null)
		{
			writeC(2);
		}
		else
		{
			writeC(1);
		}
		writeS(_cha.getName());
		writeS(_cha.getTitle());
		writeD(1);
		writeD(0);
		writeD(_cha.getKarma());
		writeD(_cha.getCurrentFed());
		writeD(_cha.getMaxFed());
		writeD((int) _cha.getCurrentHp());
		writeD(_cha.getMaxHp());
		writeD((int) _cha.getCurrentMp());
		writeD(_cha.getMaxMp());
		writeD(_cha.getSp());
		writeD(_cha.getLevel());
		writeD(_cha.getExp());
		writeD(0);
		writeD(200000);
		writeD(_cha.getInventory().getTotalWeight());
		writeD(200000);
		writeD(_cha.getPhysicalAttack());
		writeD(_cha.getPhysicalDefense());
		writeD(_cha.getMagicalAttack());
		writeD(_cha.getMagicalDefense());
		writeD(_cha.getAccuracy());
		writeD(_cha.getEvasionRate());
		writeD(_cha.getCriticalHit());
		writeD((int) _cha.getEffectiveSpeed());
		writeD(80);
		writeD(_cha.getMagicalSpeed());
	}
}
