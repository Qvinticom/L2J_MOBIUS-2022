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

import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;

public class GMViewCharacterInfo extends ServerBasePacket
{
	private static final String _S__04_USERINFO = "[S] A8 GMViewCharacterInfo";
	private final PlayerInstance _cha;
	// private static int _test = 1;
	
	public GMViewCharacterInfo(PlayerInstance cha)
	{
		_cha = cha;
	}
	
	@Override
	public byte[] getContent()
	{
		_bao.write(168);
		writeD(_cha.getX());
		writeD(_cha.getY());
		writeD(_cha.getZ());
		writeD(_cha.getHeading());
		writeD(_cha.getObjectId());
		writeS(_cha.getName());
		writeD(_cha.getRace());
		writeD(_cha.getSex());
		writeD(_cha.getClassId());
		writeD(_cha.getLevel());
		writeD(_cha.getExp());
		writeD(_cha.getStr());
		writeD(_cha.getDex());
		writeD(_cha.getCon());
		writeD(_cha.getInt());
		writeD(_cha.getWit());
		writeD(_cha.getMen());
		writeD(_cha.getMaxHp());
		writeD((int) _cha.getCurrentHp());
		writeD(_cha.getMaxMp());
		writeD((int) _cha.getCurrentMp());
		writeD(_cha.getSp());
		writeD(_cha.getCurrentLoad());
		writeD(_cha.getMaxLoad());
		writeD(40);
		writeD(_cha.getInventory().getPaperdollObjectId(0));
		writeD(_cha.getInventory().getPaperdollObjectId(2));
		writeD(_cha.getInventory().getPaperdollObjectId(1));
		writeD(_cha.getInventory().getPaperdollObjectId(3));
		writeD(_cha.getInventory().getPaperdollObjectId(5));
		writeD(_cha.getInventory().getPaperdollObjectId(4));
		writeD(_cha.getInventory().getPaperdollObjectId(6));
		writeD(_cha.getInventory().getPaperdollObjectId(7));
		writeD(_cha.getInventory().getPaperdollObjectId(8));
		writeD(_cha.getInventory().getPaperdollObjectId(9));
		writeD(_cha.getInventory().getPaperdollObjectId(10));
		writeD(_cha.getInventory().getPaperdollObjectId(11));
		writeD(_cha.getInventory().getPaperdollObjectId(12));
		writeD(_cha.getInventory().getPaperdollObjectId(13));
		writeD(_cha.getInventory().getPaperdollObjectId(14));
		writeD(_cha.getInventory().getPaperdollItemId(0));
		writeD(_cha.getInventory().getPaperdollItemId(2));
		writeD(_cha.getInventory().getPaperdollItemId(1));
		writeD(_cha.getInventory().getPaperdollItemId(3));
		writeD(_cha.getInventory().getPaperdollItemId(5));
		writeD(_cha.getInventory().getPaperdollItemId(4));
		writeD(_cha.getInventory().getPaperdollItemId(6));
		writeD(_cha.getInventory().getPaperdollItemId(7));
		writeD(_cha.getInventory().getPaperdollItemId(8));
		writeD(_cha.getInventory().getPaperdollItemId(9));
		writeD(_cha.getInventory().getPaperdollItemId(10));
		writeD(_cha.getInventory().getPaperdollItemId(11));
		writeD(_cha.getInventory().getPaperdollItemId(12));
		writeD(_cha.getInventory().getPaperdollItemId(13));
		writeD(_cha.getInventory().getPaperdollItemId(14));
		writeD(_cha.getPhysicalAttack());
		writeD(_cha.getPhysicalSpeed());
		writeD(_cha.getPhysicalDefense());
		writeD(_cha.getEvasionRate());
		writeD(_cha.getAccuracy());
		writeD(_cha.getCriticalHit());
		writeD(_cha.getMagicalAttack());
		writeD(_cha.getMagicalSpeed());
		writeD(_cha.getPhysicalSpeed());
		writeD(_cha.getMagicalDefense());
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
		if (_cha.isGM())
		{
			writeD(1);
		}
		else
		{
			writeD(0);
		}
		writeS(_cha.getTitle());
		writeD(_cha.getClanId());
		writeD(_cha.getClanId());
		writeD(_cha.getAllyId());
		writeD(_cha.getAllyId());
		if (_cha.isClanLeader())
		{
			writeD(96);
		}
		else
		{
			writeD(0);
		}
		return _bao.toByteArray();
	}
	
	@Override
	public String getType()
	{
		return _S__04_USERINFO;
	}
}
