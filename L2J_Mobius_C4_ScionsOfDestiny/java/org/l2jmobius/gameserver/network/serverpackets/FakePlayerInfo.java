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
package org.l2jmobius.gameserver.network.serverpackets;

import org.l2jmobius.gameserver.datatables.xml.PlayerTemplateData;
import org.l2jmobius.gameserver.model.actor.instance.NpcInstance;

public class FakePlayerInfo extends GameServerPacket
{
	private final NpcInstance _activeChar;
	
	public FakePlayerInfo(NpcInstance cha)
	{
		_activeChar = cha;
		_activeChar.setClientX(_activeChar.getPosition().getX());
		_activeChar.setClientY(_activeChar.getPosition().getY());
		_activeChar.setClientZ(_activeChar.getPosition().getZ());
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x03);
		writeD(_activeChar.getX());
		writeD(_activeChar.getY());
		writeD(_activeChar.getZ());
		writeD(_activeChar.getHeading());
		writeD(_activeChar.getObjectId());
		writeS(_activeChar.getFakePlayerInstance().getName());
		writeD(_activeChar.getFakePlayerInstance().getRace());
		writeD(_activeChar.getFakePlayerInstance().isFemaleSex() ? 1 : 0);
		writeD(_activeChar.getFakePlayerInstance().getClassId());
		writeD(0);
		writeD(0);
		writeD(_activeChar.getFakePlayerInstance().PAPERDOLL_RHAND());
		writeD(_activeChar.getFakePlayerInstance().PAPERDOLL_LHAND());
		writeD(_activeChar.getFakePlayerInstance().PAPERDOLL_GLOVES());
		writeD(_activeChar.getFakePlayerInstance().PAPERDOLL_CHEST());
		writeD(_activeChar.getFakePlayerInstance().PAPERDOLL_LEGS());
		writeD(_activeChar.getFakePlayerInstance().PAPERDOLL_FEET());
		writeD(_activeChar.getFakePlayerInstance().PAPERDOLL_HAIR());
		writeD(_activeChar.getFakePlayerInstance().PAPERDOLL_RHAND());
		writeD(_activeChar.getFakePlayerInstance().PAPERDOLL_HAIR());
		
		writeD(_activeChar.getFakePlayerInstance().getPvpFlag() ? 1 : 0);
		writeD(_activeChar.getFakePlayerInstance().getKarma());
		writeD(_activeChar.getMAtkSpd());
		writeD(_activeChar.getPAtkSpd());
		writeD(_activeChar.getFakePlayerInstance().getPvpFlag() ? 1 : 0);
		writeD(_activeChar.getFakePlayerInstance().getKarma());
		writeD(_activeChar.getRunSpeed());
		writeD(_activeChar.getRunSpeed() / 2);
		writeD(_activeChar.getRunSpeed() / 3);
		writeD(_activeChar.getRunSpeed() / 3);
		writeD(_activeChar.getRunSpeed());
		writeD(_activeChar.getRunSpeed());
		writeD(_activeChar.getRunSpeed());
		writeD(_activeChar.getRunSpeed());
		writeF(_activeChar.getStat().getMovementSpeedMultiplier());
		writeF(_activeChar.getStat().getAttackSpeedMultiplier());
		writeF(PlayerTemplateData.getInstance().getTemplate(_activeChar.getFakePlayerInstance().getClassId()).getCollisionRadius());
		writeF(PlayerTemplateData.getInstance().getTemplate(_activeChar.getFakePlayerInstance().getClassId()).getCollisionHeight());
		writeD(_activeChar.getFakePlayerInstance().getHairStyle());
		writeD(_activeChar.getFakePlayerInstance().getHairColor());
		writeD(_activeChar.getFakePlayerInstance().getFace());
		writeS(_activeChar.getFakePlayerInstance().getTitle());
		writeD(_activeChar.getFakePlayerInstance().getClanId());
		writeD(_activeChar.getFakePlayerInstance().getClanCrestId());
		writeD(_activeChar.getFakePlayerInstance().getAllyId());
		writeD(_activeChar.getFakePlayerInstance().getAllyCrestId());
		writeD(0);
		writeC(1);
		writeC(_activeChar.isRunning() ? 1 : 0);
		writeC(_activeChar.isInCombat() ? 1 : 0);
		writeC(_activeChar.isAlikeDead() ? 1 : 0);
		writeC(0);
		writeC(0);
		writeC(0);
		writeH(0);
		writeC(0);
		writeD(_activeChar.getAbnormalEffect());
		writeC(0);
		writeH(0);
		writeD(_activeChar.getFakePlayerInstance().getClassId());
		writeD(_activeChar.getMaxCp());
		writeD((int) _activeChar.getStatus().getCurrentCp());
		writeC(_activeChar.getFakePlayerInstance().getEnchantWeapon());
		writeC(0);
		writeD(0); // clan crest
		writeC(_activeChar.getFakePlayerInstance().isNoble() ? 1 : 0);
		writeC(_activeChar.getFakePlayerInstance().isHero() ? 1 : 0);
		writeC(0);
		writeD(0);
		writeD(0);
		writeD(0);
		writeD(_activeChar.getFakePlayerInstance().nameColor());
	}
}
