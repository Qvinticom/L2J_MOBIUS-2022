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
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.network.OutgoingPackets;

public class FakePlayerInfo implements IClientOutgoingPacket
{
	private final Npc _activeChar;
	
	public FakePlayerInfo(Npc cha)
	{
		_activeChar = cha;
		_activeChar.setClientX(_activeChar.getX());
		_activeChar.setClientY(_activeChar.getY());
		_activeChar.setClientZ(_activeChar.getZ());
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
		packet.writeS(_activeChar.getFakePlayer().getName());
		packet.writeD(_activeChar.getFakePlayer().getRace());
		packet.writeD(_activeChar.getFakePlayer().isFemaleSex() ? 1 : 0);
		packet.writeD(_activeChar.getFakePlayer().getClassId());
		packet.writeD(_activeChar.getFakePlayer().PAPERDOLL_HAIR());
		packet.writeD(0);
		packet.writeD(_activeChar.getFakePlayer().PAPERDOLL_RHAND());
		packet.writeD(_activeChar.getFakePlayer().PAPERDOLL_LHAND());
		packet.writeD(_activeChar.getFakePlayer().PAPERDOLL_GLOVES());
		packet.writeD(_activeChar.getFakePlayer().PAPERDOLL_CHEST());
		packet.writeD(_activeChar.getFakePlayer().PAPERDOLL_LEGS());
		packet.writeD(_activeChar.getFakePlayer().PAPERDOLL_FEET());
		packet.writeD(_activeChar.getFakePlayer().PAPERDOLL_HAIR());
		packet.writeD(_activeChar.getFakePlayer().PAPERDOLL_RHAND());
		packet.writeD(_activeChar.getFakePlayer().PAPERDOLL_HAIR());
		packet.writeD(_activeChar.getFakePlayer().PAPERDOLL_HAIR2());
		
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
		
		packet.writeD(_activeChar.getFakePlayer().getPvpFlag() ? 1 : 0);
		packet.writeD(_activeChar.getFakePlayer().getKarma());
		packet.writeD(_activeChar.getMAtkSpd());
		packet.writeD(_activeChar.getPAtkSpd());
		packet.writeD(_activeChar.getFakePlayer().getPvpFlag() ? 1 : 0);
		packet.writeD(_activeChar.getFakePlayer().getKarma());
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
		packet.writeF(PlayerTemplateData.getInstance().getTemplate(_activeChar.getFakePlayer().getClassId()).getCollisionRadius());
		packet.writeF(PlayerTemplateData.getInstance().getTemplate(_activeChar.getFakePlayer().getClassId()).getCollisionHeight());
		packet.writeD(_activeChar.getFakePlayer().getHairStyle());
		packet.writeD(_activeChar.getFakePlayer().getHairColor());
		packet.writeD(_activeChar.getFakePlayer().getFace());
		packet.writeS(_activeChar.getFakePlayer().getTitle());
		packet.writeD(_activeChar.getFakePlayer().getClanId());
		packet.writeD(_activeChar.getFakePlayer().getClanCrestId());
		packet.writeD(_activeChar.getFakePlayer().getAllyId());
		packet.writeD(_activeChar.getFakePlayer().getAllyCrestId());
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
		packet.writeD(_activeChar.getFakePlayer().getClassId());
		packet.writeD(_activeChar.getMaxCp());
		packet.writeD((int) _activeChar.getStatus().getCurrentCp());
		packet.writeC(_activeChar.getFakePlayer().getEnchantWeapon());
		packet.writeC(0);
		packet.writeD(0); // clan crest
		packet.writeC(_activeChar.getFakePlayer().isNoble() ? 1 : 0);
		packet.writeC(_activeChar.getFakePlayer().isHero() ? 1 : 0);
		packet.writeC(0);
		packet.writeD(0);
		packet.writeD(0);
		packet.writeD(0);
		packet.writeD(_activeChar.getFakePlayer().nameColor());
		packet.writeD(0);
		packet.writeD(_activeChar.getFakePlayer().getPledgeClass());
		packet.writeD(0);
		packet.writeD(_activeChar.getFakePlayer().titleColor());
		packet.writeD(0x00);
		return true;
	}
}
