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
import org.l2jmobius.gameserver.data.sql.ClanTable;
import org.l2jmobius.gameserver.data.xml.FakePlayerData;
import org.l2jmobius.gameserver.enums.Sex;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.clan.Clan;
import org.l2jmobius.gameserver.model.holders.FakePlayerHolder;
import org.l2jmobius.gameserver.model.zone.ZoneId;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * @author Mobius
 */
public class FakePlayerInfo implements IClientOutgoingPacket
{
	private final Npc _npc;
	private final int _objId;
	private final int _x;
	private final int _y;
	private final int _z;
	private final int _heading;
	private final int _mAtkSpd;
	private final int _pAtkSpd;
	private final int _runSpd;
	private final int _walkSpd;
	private final int _swimRunSpd;
	private final int _swimWalkSpd;
	private final int _flyRunSpd;
	private final int _flyWalkSpd;
	private final double _moveMultiplier;
	private final float _attackSpeedMultiplier;
	private final FakePlayerHolder _fpcHolder;
	private final Clan _clan;
	
	public FakePlayerInfo(Npc npc)
	{
		_npc = npc;
		_objId = npc.getObjectId();
		_x = npc.getX();
		_y = npc.getY();
		_z = npc.getZ();
		_heading = npc.getHeading();
		_mAtkSpd = npc.getMAtkSpd();
		_pAtkSpd = (int) npc.getPAtkSpd();
		_attackSpeedMultiplier = npc.getAttackSpeedMultiplier();
		_moveMultiplier = npc.getMovementSpeedMultiplier();
		_runSpd = (int) Math.round(npc.getRunSpeed() / _moveMultiplier);
		_walkSpd = (int) Math.round(npc.getWalkSpeed() / _moveMultiplier);
		_swimRunSpd = (int) Math.round(npc.getSwimRunSpeed() / _moveMultiplier);
		_swimWalkSpd = (int) Math.round(npc.getSwimWalkSpeed() / _moveMultiplier);
		_flyRunSpd = npc.isFlying() ? _runSpd : 0;
		_flyWalkSpd = npc.isFlying() ? _walkSpd : 0;
		_fpcHolder = FakePlayerData.getInstance().getInfo(npc.getId());
		_clan = ClanTable.getInstance().getClan(_fpcHolder.getClanId());
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.CHAR_INFO.writeId(packet);
		packet.writeD(_x);
		packet.writeD(_y);
		packet.writeD(_z);
		packet.writeD(0); // vehicleId
		packet.writeD(_objId);
		packet.writeS(_npc.getName());
		packet.writeD(_npc.getRace().ordinal());
		packet.writeD(_npc.getTemplate().getSex() == Sex.FEMALE ? 1 : 0);
		packet.writeD(_fpcHolder.getClassId());
		packet.writeD(0); // Inventory.PAPERDOLL_UNDER
		packet.writeD(_fpcHolder.getEquipHead());
		packet.writeD(_fpcHolder.getEquipRHand());
		packet.writeD(_fpcHolder.getEquipLHand());
		packet.writeD(_fpcHolder.getEquipGloves());
		packet.writeD(_fpcHolder.getEquipChest());
		packet.writeD(_fpcHolder.getEquipLegs());
		packet.writeD(_fpcHolder.getEquipFeet());
		packet.writeD(_fpcHolder.getEquipCloak());
		packet.writeD(_fpcHolder.getEquipRHand()); // dual hand
		packet.writeD(_fpcHolder.getEquipHair());
		packet.writeD(_fpcHolder.getEquipHair2());
		packet.writeD(0); // Inventory.PAPERDOLL_RBRACELET
		packet.writeD(0); // Inventory.PAPERDOLL_LBRACELET
		packet.writeD(0); // Inventory.PAPERDOLL_DECO1
		packet.writeD(0); // Inventory.PAPERDOLL_DECO2
		packet.writeD(0); // Inventory.PAPERDOLL_DECO3
		packet.writeD(0); // Inventory.PAPERDOLL_DECO4
		packet.writeD(0); // Inventory.PAPERDOLL_DECO5
		packet.writeD(0); // Inventory.PAPERDOLL_DECO6
		packet.writeD(0); // Inventory.PAPERDOLL_BELT
		for (int i = 0; i < 21; i++)
		{
			packet.writeD(0);
		}
		packet.writeD(0); // getTalismanSlots
		packet.writeD(1); // canEquipCloak
		packet.writeD(_npc.getScriptValue()); // getPvpFlag()
		packet.writeD(_npc.getKarma());
		packet.writeD(_mAtkSpd);
		packet.writeD(_pAtkSpd);
		packet.writeD(_npc.getScriptValue()); // getPvpFlag()
		packet.writeD(_npc.getKarma());
		packet.writeD(_runSpd);
		packet.writeD(_walkSpd);
		packet.writeD(_swimRunSpd);
		packet.writeD(_swimWalkSpd);
		packet.writeD(_flyRunSpd);
		packet.writeD(_flyWalkSpd);
		packet.writeD(_flyRunSpd);
		packet.writeD(_flyWalkSpd);
		packet.writeF(_moveMultiplier);
		packet.writeF(_attackSpeedMultiplier);
		packet.writeF(_npc.getCollisionRadius());
		packet.writeF(_npc.getCollisionHeight());
		packet.writeD(_fpcHolder.getHair());
		packet.writeD(_fpcHolder.getHairColor());
		packet.writeD(_fpcHolder.getFace());
		packet.writeS(_npc.getTemplate().getTitle());
		if (_clan != null)
		{
			packet.writeD(_clan.getId());
			packet.writeD(_clan.getCrestId());
			packet.writeD(_clan.getAllyId());
			packet.writeD(_clan.getAllyCrestId());
		}
		else
		{
			packet.writeD(0);
			packet.writeD(0);
			packet.writeD(0);
			packet.writeD(0);
		}
		// In UserInfo leader rights and siege flags, but here found nothing??
		// Therefore RelationChanged packet with that info is required
		packet.writeD(0);
		packet.writeC(1); // isSitting() ? 0 : 1 (at some initial tests it worked)
		packet.writeC(_npc.isRunning() ? 1 : 0);
		packet.writeC(_npc.isInCombat() ? 1 : 0);
		packet.writeC(_npc.isAlikeDead() ? 1 : 0);
		packet.writeC(_npc.isInvisible() ? 1 : 0);
		packet.writeC(0); // 1-on Strider, 2-on Wyvern, 3-on Great Wolf, 0-no mount
		packet.writeC(0); // getPrivateStoreType().getId()
		packet.writeH(0); // getCubics().size()
		// getCubics().keySet().forEach(packet::writeH);
		packet.writeC(0); // isInPartyMatchRoom
		packet.writeD(_npc.getAbnormalVisualEffects());
		packet.writeC(_npc.isInsideZone(ZoneId.WATER) ? 1 : 0);
		packet.writeH(_fpcHolder.getRecommends()); // Blue value for name (0 = white, 255 = pure blue)
		packet.writeD(0); // getMountNpcId() == 0 ? 0 : getMountNpcId() + 1000000
		packet.writeD(_fpcHolder.getClassId());
		packet.writeD(0); // ?
		packet.writeC(_fpcHolder.getWeaponEnchantLevel()); // isMounted() ? 0 : _enchantLevel
		packet.writeC(_npc.getTeam().getId());
		packet.writeD(_clan != null ? _clan.getCrestLargeId() : 0);
		packet.writeC(_fpcHolder.getNobleLevel());
		packet.writeC(_fpcHolder.isHero() ? 1 : 0);
		packet.writeC(_fpcHolder.isFishing() ? 1 : 0);
		packet.writeD(_fpcHolder.getBaitLocationX());
		packet.writeD(_fpcHolder.getBaitLocationY());
		packet.writeD(_fpcHolder.getBaitLocationZ());
		packet.writeD(_fpcHolder.getNameColor());
		packet.writeD(_heading);
		packet.writeD(_fpcHolder.getPledgeStatus());
		packet.writeD(0); // getPledgeType()
		packet.writeD(_fpcHolder.getTitleColor());
		packet.writeD(0); // isCursedWeaponEquipped
		packet.writeD(0); // getClanId() > 0 ? getClan().getReputationScore() : 0
		// T1
		packet.writeD(0); // getTransformationDisplayId()
		packet.writeD(_fpcHolder.getAgathionId());
		// T2
		packet.writeD(1);
		// T2.3
		packet.writeD(_npc.getAbnormalVisualEffectSpecial());
		packet.writeD(0); // territory Id
		packet.writeD(0); // is Disguised
		packet.writeD(0); // territory Id
		return true;
	}
}
