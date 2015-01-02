/*
 * Copyright (C) 2004-2015 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jserver.gameserver.network.serverpackets;

import com.l2jserver.Config;
import com.l2jserver.gameserver.instancemanager.CursedWeaponsManager;
import com.l2jserver.gameserver.model.PcCondOverride;
import com.l2jserver.gameserver.model.actor.L2Decoy;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.itemcontainer.Inventory;
import com.l2jserver.gameserver.model.zone.ZoneId;

public class CharInfo extends L2GameServerPacket
{
	private final L2PcInstance _activeChar;
	private int _objId;
	private int _x, _y, _z, _heading;
	private final int _mAtkSpd, _pAtkSpd;
	
	private final int _runSpd, _walkSpd;
	private final int _swimRunSpd;
	private final int _swimWalkSpd;
	private final int _flyRunSpd;
	private final int _flyWalkSpd;
	private final double _moveMultiplier;
	private final float _attackSpeedMultiplier;
	private int _enchantLevel = 0;
	private int _armorEnchant = 0;
	
	private int _vehicleId = 0;
	
	private static final int[] PAPERDOLL_ORDER = new int[]
	{
		Inventory.PAPERDOLL_UNDER,
		Inventory.PAPERDOLL_HEAD,
		Inventory.PAPERDOLL_RHAND,
		Inventory.PAPERDOLL_LHAND,
		Inventory.PAPERDOLL_GLOVES,
		Inventory.PAPERDOLL_CHEST,
		Inventory.PAPERDOLL_LEGS,
		Inventory.PAPERDOLL_FEET,
		Inventory.PAPERDOLL_CLOAK,
		Inventory.PAPERDOLL_RHAND,
		Inventory.PAPERDOLL_HAIR,
		Inventory.PAPERDOLL_HAIR2
	};
	
	public CharInfo(L2PcInstance cha)
	{
		_activeChar = cha;
		_objId = cha.getObjectId();
		if ((_activeChar.getVehicle() != null) && (_activeChar.getInVehiclePosition() != null))
		{
			_x = _activeChar.getInVehiclePosition().getX();
			_y = _activeChar.getInVehiclePosition().getY();
			_z = _activeChar.getInVehiclePosition().getZ();
			_vehicleId = _activeChar.getVehicle().getObjectId();
		}
		else
		{
			_x = _activeChar.getX();
			_y = _activeChar.getY();
			_z = _activeChar.getZ();
		}
		_heading = _activeChar.getHeading();
		_mAtkSpd = _activeChar.getMAtkSpd();
		_pAtkSpd = _activeChar.getPAtkSpd();
		_attackSpeedMultiplier = _activeChar.getAttackSpeedMultiplier();
		setInvisible(cha.isInvisible());
		
		_moveMultiplier = cha.getMovementSpeedMultiplier();
		_runSpd = (int) Math.round(cha.getRunSpeed() / _moveMultiplier);
		_walkSpd = (int) Math.round(cha.getWalkSpeed() / _moveMultiplier);
		_swimRunSpd = (int) Math.round(cha.getSwimRunSpeed() / _moveMultiplier);
		_swimWalkSpd = (int) Math.round(cha.getSwimWalkSpeed() / _moveMultiplier);
		_flyRunSpd = cha.isFlying() ? _runSpd : 0;
		_flyWalkSpd = cha.isFlying() ? _walkSpd : 0;
		_enchantLevel = cha.getInventory().getWeaponEnchant();
		_armorEnchant = cha.getInventory().getArmorMinEnchant();
	}
	
	public CharInfo(L2Decoy decoy)
	{
		this(decoy.getActingPlayer()); // init
		_objId = decoy.getObjectId();
		_x = decoy.getX();
		_y = decoy.getY();
		_z = decoy.getZ();
		_heading = decoy.getHeading();
	}
	
	@Override
	protected final void writeImpl()
	{
		boolean gmSeeInvis = false;
		
		if (isInvisible())
		{
			final L2PcInstance activeChar = getClient().getActiveChar();
			if ((activeChar != null) && activeChar.canOverrideCond(PcCondOverride.SEE_ALL_PLAYERS))
			{
				gmSeeInvis = true;
			}
		}
		
		writeC(0x31);
		writeD(_x); // Confirmed
		writeD(_y); // Confirmed
		writeD(_z); // Confirmed
		writeD(_vehicleId); // Confirmed
		writeD(_objId); // Confirmed
		writeS(_activeChar.getAppearance().getVisibleName()); // Confirmed
		writeH(_activeChar.getRace().ordinal()); // Confirmed
		writeC(_activeChar.getAppearance().getSex() ? 0x01 : 0x00); // Confirmed
		writeD(_activeChar.getBaseClass()); // Confirmed
		
		for (int slot : getPaperdollOrder())
		{
			writeD(_activeChar.getInventory().getPaperdollItemDisplayId(slot)); // Confirmed
		}
		
		for (int slot : getPaperdollOrderAugument())
		{
			writeD(_activeChar.getInventory().getPaperdollAugmentationId(slot)); // Confirmed
		}
		
		writeC(_armorEnchant);
		
		writeD(0x00); // rhand item visual id
		writeD(0x00); // lhand item visual id
		writeD(0x00); // lrhand item visual id
		writeD(0x00); // gloves item visual id
		writeD(0x00); // chest item visual id
		writeD(0x00); // legs item visual id
		writeD(0x00); // feet item visual id
		writeD(0x00); // hair item visual id
		writeD(0x00); // hair 2 item visual id
		
		writeC(_activeChar.getPvpFlag());
		writeD(_activeChar.getKarma());
		
		writeD(_mAtkSpd);
		writeD(_pAtkSpd);
		
		writeH(_runSpd);
		writeH(_walkSpd);
		writeH(_swimRunSpd);
		writeH(_swimWalkSpd);
		writeH(_flyRunSpd);
		writeH(_flyWalkSpd);
		writeH(_flyRunSpd);
		writeH(_flyWalkSpd);
		writeF(_moveMultiplier);
		writeF(_attackSpeedMultiplier);
		
		writeF(_activeChar.getCollisionRadius());
		writeF(_activeChar.getCollisionHeight());
		
		writeD(_activeChar.getVisualHair());
		writeD(_activeChar.getVisualHairColor());
		writeD(_activeChar.getVisualFace());
		
		writeS(gmSeeInvis ? "Invisible" : _activeChar.getAppearance().getVisibleTitle());
		
		if (!_activeChar.isCursedWeaponEquipped())
		{
			writeD(_activeChar.getClanId());
			writeD(_activeChar.getClanCrestId());
			writeD(_activeChar.getAllyId());
			writeD(_activeChar.getAllyCrestId());
		}
		else
		{
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
		}
		
		writeC(_activeChar.isSitting() ? 0x00 : 0x01); // Confirmed
		writeC(_activeChar.isRunning() ? 0x01 : 0x00); // Confirmed
		writeC(_activeChar.isInCombat() ? 0x01 : 0x00); // Confirmed
		
		writeC(!_activeChar.isInOlympiadMode() && _activeChar.isAlikeDead() ? 0x01 : 0x00); // Confirmed
		
		writeC(!gmSeeInvis && isInvisible() ? 0x01 : 0x00); // TODO: Find me!
		
		writeC(_activeChar.getMountType().ordinal()); // 1-on Strider, 2-on Wyvern, 3-on Great Wolf, 0-no mount
		writeC(_activeChar.getPrivateStoreType().getId()); // Confirmed
		
		writeH(_activeChar.getCubics().size()); // Confirmed
		for (int cubicId : _activeChar.getCubics().keySet())
		{
			writeH(cubicId); // Confirmed
		}
		
		writeC(_activeChar.isInPartyMatchRoom() ? 0x01 : 0x00); // Confirmed
		
		writeC(_activeChar.isInsideZone(ZoneId.WATER) ? 1 : _activeChar.isFlyingMounted() ? 2 : 0);
		writeH(_activeChar.getRecomHave()); // Confirmed
		writeD(_activeChar.getMountNpcId() == 0 ? 0 : _activeChar.getMountNpcId() + 1000000);
		
		writeD(_activeChar.getClassId().getId()); // Confirmed
		writeD(0x00); // TODO: Find me!
		writeC(_activeChar.isMounted() ? 0 : _enchantLevel); // Confirmed
		
		writeC(_activeChar.getTeam().getId()); // Confirmed
		
		writeD(_activeChar.getClanCrestLargeId());
		writeC(_activeChar.isNoble() ? 1 : 0); // Confirmed
		writeC(_activeChar.isHero() || (_activeChar.isGM() && Config.GM_HERO_AURA) ? 1 : 0); // Confirmed
		
		writeC(_activeChar.isFishing() ? 1 : 0); // Confirmed
		writeD(_activeChar.getFishx()); // Confirmed
		writeD(_activeChar.getFishy()); // Confirmed
		writeD(_activeChar.getFishz()); // Confirmed
		
		writeD(_activeChar.getAppearance().getNameColor()); // Confirmed
		
		writeD(_heading); // Confirmed
		
		writeC(_activeChar.getPledgeClass());
		writeH(_activeChar.getPledgeType());
		
		writeD(_activeChar.getAppearance().getTitleColor()); // Confirmed
		
		writeC(_activeChar.isCursedWeaponEquipped() ? CursedWeaponsManager.getInstance().getLevel(_activeChar.getCursedWeaponEquippedId()) : 0); // TODO: Find me!
		
		writeD(_activeChar.getClanId() > 0 ? _activeChar.getClan().getReputationScore() : 0);
		writeD(_activeChar.getTransformationDisplayId()); // Confirmed
		writeD(_activeChar.getAgathionId()); // Confirmed
		
		writeC(0x00); // TODO: Find me!
		
		writeD((int) Math.round(_activeChar.getCurrentCp())); // Confirmed
		writeD(_activeChar.getMaxHp()); // Confirmed
		writeD((int) Math.round(_activeChar.getCurrentHp())); // Confirmed
		writeD(_activeChar.getMaxMp()); // Confirmed
		writeD((int) Math.round(_activeChar.getCurrentMp())); // Confirmed
		
		writeC(0x00); // TODO: Find me!
		writeD(_activeChar.getAbnormalVisualEffectsList().size()); // Confirmed
		for (int abnormalId : _activeChar.getAbnormalVisualEffectsList())
		{
			writeH(abnormalId); // Confirmed
		}
		writeC(0x00); // TODO: Find me!
		writeC(_activeChar.isHairAccessoryEnabled() ? 0x01 : 0x00); // Hair accessory
		writeC(_activeChar.getAbilityPointsUsed()); // Used Ability Points
	}
	
	@Override
	protected int[] getPaperdollOrder()
	{
		return PAPERDOLL_ORDER;
	}
}