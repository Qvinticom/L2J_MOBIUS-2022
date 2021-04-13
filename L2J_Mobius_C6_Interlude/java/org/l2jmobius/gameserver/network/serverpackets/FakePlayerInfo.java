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

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.data.xml.PlayerTemplateData;
import org.l2jmobius.gameserver.model.actor.instance.NpcInstance;
import org.l2jmobius.gameserver.network.OutgoingPackets;

public class FakePlayerInfo implements IClientOutgoingPacket
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
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.CHAR_INFO.writeId(packet);
		
		packet.writeD(_activeChar.getX());
		packet.writeD(_activeChar.getY());
		packet.writeD(_activeChar.getZ());
		packet.writeD(_activeChar.getHeading());
		packet.writeD(_activeChar.getObjectId());
		packet.writeS(_activeChar.getFakePlayerInstance().getName());
		packet.writeD(_activeChar.getFakePlayerInstance().getRace());
		packet.writeD(_activeChar.getFakePlayerInstance().isFemaleSex() ? 1 : 0);
		packet.writeD(_activeChar.getFakePlayerInstance().getClassId());
		packet.writeD(_activeChar.getFakePlayerInstance().PAPERDOLL_HAIR());
		packet.writeD(0);
		packet.writeD(_activeChar.getFakePlayerInstance().PAPERDOLL_RHAND());
		packet.writeD(_activeChar.getFakePlayerInstance().PAPERDOLL_LHAND());
		packet.writeD(_activeChar.getFakePlayerInstance().PAPERDOLL_GLOVES());
		packet.writeD(_activeChar.getFakePlayerInstance().PAPERDOLL_CHEST());
		packet.writeD(_activeChar.getFakePlayerInstance().PAPERDOLL_LEGS());
		packet.writeD(_activeChar.getFakePlayerInstance().PAPERDOLL_FEET());
		packet.writeD(_activeChar.getFakePlayerInstance().PAPERDOLL_HAIR());
		packet.writeD(_activeChar.getFakePlayerInstance().PAPERDOLL_RHAND());
		packet.writeD(_activeChar.getFakePlayerInstance().PAPERDOLL_HAIR());
		packet.writeD(_activeChar.getFakePlayerInstance().PAPERDOLL_HAIR2());
		
		packet.writeH(0);
		packet.writeH(0);
		packet.writeH(0);
		packet.writeH(0);
		packet.writeH(0);
		packet.writeH(0);
		packet.writeH(0);
		packet.writeH(0);
		packet.writeH(0);
		packet.writeH(0);
		packet.writeH(0);
		packet.writeH(0);
		packet.writeH(0);
		packet.writeH(0);
		packet.writeH(0);
		packet.writeH(0);
		packet.writeH(0);
		packet.writeH(0);
		packet.writeH(0);
		packet.writeH(0);
		packet.writeH(0);
		packet.writeH(0);
		packet.writeH(0);
		packet.writeH(0);
		
		packet.writeD(_activeChar.getFakePlayerInstance().getPvpFlag() ? 1 : 0);
		packet.writeD(_activeChar.getFakePlayerInstance().getKarma());
		packet.writeD(_activeChar.getMAtkSpd());
		packet.writeD(_activeChar.getPAtkSpd());
		packet.writeD(_activeChar.getFakePlayerInstance().getPvpFlag() ? 1 : 0);
		packet.writeD(_activeChar.getFakePlayerInstance().getKarma());
		packet.writeD(_activeChar.getRunSpeed());
		packet.writeD(_activeChar.getRunSpeed() / 2);
		packet.writeD(_activeChar.getRunSpeed() / 3);
		packet.writeD(_activeChar.getRunSpeed() / 3);
		packet.writeD(_activeChar.getRunSpeed());
		packet.writeD(_activeChar.getRunSpeed());
		packet.writeD(_activeChar.getRunSpeed());
		packet.writeD(_activeChar.getRunSpeed());
		packet.writeF(_activeChar.getStat().getMovementSpeedMultiplier());
		packet.writeF(_activeChar.getStat().getAttackSpeedMultiplier());
		packet.writeF(PlayerTemplateData.getInstance().getTemplate(_activeChar.getFakePlayerInstance().getClassId()).getCollisionRadius());
		packet.writeF(PlayerTemplateData.getInstance().getTemplate(_activeChar.getFakePlayerInstance().getClassId()).getCollisionHeight());
		packet.writeD(_activeChar.getFakePlayerInstance().getHairStyle());
		packet.writeD(_activeChar.getFakePlayerInstance().getHairColor());
		packet.writeD(_activeChar.getFakePlayerInstance().getFace());
		packet.writeS(_activeChar.getFakePlayerInstance().getTitle());
		packet.writeD(_activeChar.getFakePlayerInstance().getClanId());
		packet.writeD(_activeChar.getFakePlayerInstance().getClanCrestId());
		packet.writeD(_activeChar.getFakePlayerInstance().getAllyId());
		packet.writeD(_activeChar.getFakePlayerInstance().getAllyCrestId());
		packet.writeD(0);
		packet.writeC(1);
		packet.writeC(_activeChar.isRunning() ? 1 : 0);
		packet.writeC(_activeChar.isInCombat() ? 1 : 0);
		packet.writeC(_activeChar.isAlikeDead() ? 1 : 0);
		packet.writeC(0);
		packet.writeC(0);
		packet.writeC(0);
		packet.writeH(0);
		packet.writeC(0);
		packet.writeD(_activeChar.getAbnormalEffect());
		packet.writeC(0);
		packet.writeH(0);
		packet.writeD(_activeChar.getFakePlayerInstance().getClassId());
		packet.writeD(_activeChar.getMaxCp());
		packet.writeD((int) _activeChar.getStatus().getCurrentCp());
		packet.writeC(_activeChar.getFakePlayerInstance().getEnchantWeapon());
		packet.writeC(0);
		packet.writeD(0); // clan crest
		packet.writeC(_activeChar.getFakePlayerInstance().isNoble() ? 1 : 0);
		packet.writeC(_activeChar.getFakePlayerInstance().isHero() ? 1 : 0);
		packet.writeC(0);
		packet.writeD(0);
		packet.writeD(0);
		packet.writeD(0);
		packet.writeD(_activeChar.getFakePlayerInstance().nameColor());
		packet.writeD(0);
		packet.writeD(_activeChar.getFakePlayerInstance().getPledgeClass());
		packet.writeD(0);
		packet.writeD(_activeChar.getFakePlayerInstance().titleColor());
		packet.writeD(0x00);
		return true;
	}
}
