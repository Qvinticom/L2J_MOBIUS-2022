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
package com.l2jmobius.gameserver.network.serverpackets;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.data.xml.impl.ExperienceData;
import com.l2jmobius.gameserver.enums.UserInfoType;
import com.l2jmobius.gameserver.instancemanager.CursedWeaponsManager;
import com.l2jmobius.gameserver.model.Elementals;
import com.l2jmobius.gameserver.model.L2Clan;
import com.l2jmobius.gameserver.model.L2Party;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.items.L2Weapon;
import com.l2jmobius.gameserver.model.items.type.WeaponType;
import com.l2jmobius.gameserver.model.zone.ZoneId;

/**
 * @author Sdw, UnAfraid
 */
public class UserInfo extends AbstractMaskPacket<UserInfoType>
{
	private final L2PcInstance _activeChar;
	
	private final int _relation;
	private final int _runSpd;
	private final int _walkSpd;
	private final int _swimRunSpd;
	private final int _swimWalkSpd;
	private final int _flRunSpd = 0;
	private final int _flWalkSpd = 0;
	private final int _flyRunSpd;
	private final int _flyWalkSpd;
	private final double _moveMultiplier;
	private int _enchantLevel = 0;
	private int _armorEnchant = 0;
	private String _title;
	
	private final byte[] _masks = new byte[]
	{
		(byte) 0x00,
		(byte) 0x00,
		(byte) 0x00
	};
	
	private int _initSize = 5;
	
	public UserInfo(L2PcInstance cha)
	{
		this(cha, true);
	}
	
	public UserInfo(L2PcInstance cha, boolean addAll)
	{
		_activeChar = cha;
		
		_relation = calculateRelation(cha);
		_moveMultiplier = cha.getMovementSpeedMultiplier();
		_runSpd = (int) Math.round(cha.getRunSpeed() / _moveMultiplier);
		_walkSpd = (int) Math.round(cha.getWalkSpeed() / _moveMultiplier);
		_swimRunSpd = (int) Math.round(cha.getSwimRunSpeed() / _moveMultiplier);
		_swimWalkSpd = (int) Math.round(cha.getSwimWalkSpeed() / _moveMultiplier);
		_flyRunSpd = cha.isFlying() ? _runSpd : 0;
		_flyWalkSpd = cha.isFlying() ? _walkSpd : 0;
		_enchantLevel = cha.getInventory().getWeaponEnchant();
		_armorEnchant = cha.getInventory().getArmorMinEnchant();
		
		_title = cha.getTitle();
		if (cha.isGM() && cha.isInvisible())
		{
			_title = "[Invisible]";
		}
		
		if (addAll)
		{
			addComponentType(UserInfoType.values());
		}
		// Visual Fix
		_activeChar.sendPacket(new ExUserInfoEquipSlot(_activeChar));
		_activeChar.sendPacket(new ExUserInfoCubic(_activeChar));
		_activeChar.sendPacket(new ExUserInfoAbnormalVisualEffect(_activeChar));
	}
	
	@Override
	protected byte[] getMasks()
	{
		return _masks;
	}
	
	@Override
	protected void onNewMaskAdded(UserInfoType component)
	{
		calcBlockSize(component);
	}
	
	private void calcBlockSize(UserInfoType type)
	{
		switch (type)
		{
			case BASIC_INFO:
			{
				_initSize += type.getBlockLength() + (_activeChar.getName().length() * 2);
				break;
			}
			case CLAN:
			{
				_initSize += type.getBlockLength() + (_title.length() * 2);
				break;
			}
			default:
			{
				_initSize += type.getBlockLength();
				break;
			}
		}
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0x32);
		
		writeD(_activeChar.getObjectId());
		writeD(_initSize);
		writeH(23);
		writeB(_masks);
		
		if (containsMask(UserInfoType.RELATION))
		{
			writeD(_relation);
		}
		
		if (containsMask(UserInfoType.BASIC_INFO))
		{
			writeH(16 + (_activeChar.getName().length() * 2));
			writeString(_activeChar.getName());
			writeC(_activeChar.isGM() ? 0x01 : 0x00);
			writeC(_activeChar.getRace().ordinal());
			writeC(_activeChar.getAppearance().getSex() ? 0x01 : 0x00);
			writeD(_activeChar.getInitialClassId());
			writeD(_activeChar.getClassId().getId());
			writeC(_activeChar.getLevel());
		}
		
		if (containsMask(UserInfoType.BASE_STATS))
		{
			writeH(18);
			writeH(_activeChar.getSTR());
			writeH(_activeChar.getDEX());
			writeH(_activeChar.getCON());
			writeH(_activeChar.getINT());
			writeH(_activeChar.getWIT());
			writeH(_activeChar.getMEN());
			writeH(_activeChar.getLUC());
			writeH(_activeChar.getCHA());
		}
		
		if (containsMask(UserInfoType.MAX_HPCPMP))
		{
			writeH(14);
			writeD(_activeChar.getMaxHp());
			writeD(_activeChar.getMaxMp());
			writeD(_activeChar.getMaxCp());
		}
		
		if (containsMask(UserInfoType.CURRENT_HPMPCP_EXP_SP))
		{
			writeH(38);
			writeD((int) Math.round(_activeChar.getCurrentHp()));
			writeD((int) Math.round(_activeChar.getCurrentMp()));
			writeD((int) Math.round(_activeChar.getCurrentCp()));
			writeQ(_activeChar.getSp());
			writeQ(_activeChar.getExp());
			writeF((float) (_activeChar.getExp() - ExperienceData.getInstance().getExpForLevel(_activeChar.getLevel())) / (ExperienceData.getInstance().getExpForLevel(_activeChar.getLevel() + 1) - ExperienceData.getInstance().getExpForLevel(_activeChar.getLevel())));
		}
		
		if (containsMask(UserInfoType.ENCHANTLEVEL))
		{
			writeH(4);
			writeC(_enchantLevel);
			writeC(_armorEnchant);
		}
		
		if (containsMask(UserInfoType.APPAREANCE))
		{
			writeH(15);
			writeD(_activeChar.getVisualHair());
			writeD(_activeChar.getVisualHairColor());
			writeD(_activeChar.getVisualFace());
			writeC(_activeChar.isHairAccessoryEnabled() ? 0x01 : 0x00);
		}
		
		if (containsMask(UserInfoType.STATUS))
		{
			writeH(6);
			writeC(_activeChar.getMountType().ordinal());
			writeC(_activeChar.getPrivateStoreType().getId());
			writeC(_activeChar.hasDwarvenCraft() || (_activeChar.getSkillLevel(248) > 0) ? 1 : 0);
			writeC(_activeChar.getAbilityPointsUsed());
		}
		
		if (containsMask(UserInfoType.STATS))
		{
			writeH(56);
			if (_activeChar.getActiveWeaponItem() != null)
			{
				final L2Weapon weaponItem = _activeChar.getActiveWeaponItem();
				if (weaponItem.getItemType() == WeaponType.POLE)
				{
					writeH(80);
				}
				else if (weaponItem.getItemType() == WeaponType.BOW)
				{
					writeH(500);
				}
				else if (weaponItem.getItemType() == WeaponType.CROSSBOW)
				{
					writeH(400);
				}
				else
				{
					writeH(40);
				}
			}
			else
			{
				writeH(0);
			}
			writeD((int) _activeChar.getPAtk(null));
			writeD((int) _activeChar.getPAtkSpd());
			writeD((int) _activeChar.getPDef(null));
			writeD(_activeChar.getEvasionRate(null));
			writeD(_activeChar.getAccuracy());
			writeD(_activeChar.getCriticalHit(null, null));
			writeD((int) _activeChar.getMAtk(null, null));
			writeD(_activeChar.getMAtkSpd());
			writeD((int) _activeChar.getPAtkSpd()); // Seems like atk speed - 1
			writeD(_activeChar.getMagicEvasionRate(null));
			writeD((int) _activeChar.getMDef(null, null));
			writeD(_activeChar.getMagicAccuracy());
			writeD(_activeChar.getMCriticalHit(null, null));
		}
		
		if (containsMask(UserInfoType.ELEMENTALS))
		{
			writeH(14);
			writeH(_activeChar.getDefenseElementValue(Elementals.FIRE));
			writeH(_activeChar.getDefenseElementValue(Elementals.WATER));
			writeH(_activeChar.getDefenseElementValue(Elementals.WIND));
			writeH(_activeChar.getDefenseElementValue(Elementals.EARTH));
			writeH(_activeChar.getDefenseElementValue(Elementals.HOLY));
			writeH(_activeChar.getDefenseElementValue(Elementals.DARK));
		}
		
		if (containsMask(UserInfoType.POSITION))
		{
			writeH(18);
			writeD(_activeChar.getX());
			writeD(_activeChar.getY());
			writeD(_activeChar.getZ());
			writeD(_activeChar.isInVehicle() ? _activeChar.getVehicle().getObjectId() : 0);
		}
		
		if (containsMask(UserInfoType.SPEED))
		{
			writeH(18);
			writeH(_runSpd);
			writeH(_walkSpd);
			writeH(_swimRunSpd);
			writeH(_swimWalkSpd);
			writeH(_flRunSpd);
			writeH(_flWalkSpd);
			writeH(_flyRunSpd);
			writeH(_flyWalkSpd);
		}
		
		if (containsMask(UserInfoType.MULTIPLIER))
		{
			writeH(18);
			writeF(_moveMultiplier);
			writeF(_activeChar.getAttackSpeedMultiplier());
		}
		
		if (containsMask(UserInfoType.COL_RADIUS_HEIGHT))
		{
			writeH(18);
			writeF(_activeChar.getCollisionRadius());
			writeF(_activeChar.getCollisionHeight());
		}
		
		if (containsMask(UserInfoType.ATK_ELEMENTAL))
		{
			writeH(5);
			final byte attackAttribute = _activeChar.getAttackElement();
			writeC(attackAttribute);
			writeH(_activeChar.getAttackElementValue(attackAttribute));
		}
		
		if (containsMask(UserInfoType.CLAN))
		{
			writeH(32 + (_title.length() * 2));
			writeString(_title);
			writeH(_activeChar.getPledgeType());
			writeD(_activeChar.getClanId());
			writeD(_activeChar.getClanCrestLargeId());
			writeD(_activeChar.getClanCrestId());
			writeD(_activeChar.getClanPrivileges().getBitmask());
			writeC(_activeChar.isClanLeader() ? 1 : 0);
			writeD(_activeChar.getAllyId());
			writeD(_activeChar.getAllyCrestId());
			writeC(_activeChar.isInPartyMatchRoom() ? 0x01 : 0x00);
		}
		
		if (containsMask(UserInfoType.SOCIAL))
		{
			writeH(22);
			writeC(_activeChar.getPvpFlag());
			writeD(_activeChar.getReputation());
			writeC(_activeChar.isNoble() ? 0x01 : 0x00);
			writeC(_activeChar.isHero() || (_activeChar.isGM() && Config.GM_HERO_AURA) ? 1 : 0);
			writeC(_activeChar.getPledgeClass());
			writeD(_activeChar.getPkKills());
			writeD(_activeChar.getPvpKills());
			writeH(_activeChar.getRecomLeft());
			writeH(_activeChar.getRecomHave());
		}
		
		if (containsMask(UserInfoType.VITA_FAME))
		{
			writeH(15);
			writeD(_activeChar.getVitalityPoints());
			writeC(0x00); // Vita Bonus
			writeD(_activeChar.getFame());
			writeD(_activeChar.getRaidPoints());
		}
		
		if (containsMask(UserInfoType.SLOTS))
		{
			writeH(9);
			writeC(_activeChar.getInventory().getTalismanSlots()); // Confirmed
			writeC(_activeChar.getInventory().getBroochJewelSlots()); // Confirmed
			writeC(_activeChar.getTeam().getId()); // Confirmed
			writeC(0x00); // (1 = Red, 2 = White, 3 = White Pink) dotted ring on the floor
			writeC(0x00);
			writeC(0x00);
			writeC(0x00);
		}
		
		if (containsMask(UserInfoType.MOVEMENTS))
		{
			writeH(4);
			writeC(_activeChar.isInsideZone(ZoneId.WATER) ? 1 : _activeChar.isFlyingMounted() ? 2 : 0);
			writeC(_activeChar.isRunning() ? 0x01 : 0x00);
		}
		
		if (containsMask(UserInfoType.COLOR))
		{
			writeH(10);
			writeD(_activeChar.getAppearance().getNameColor());
			writeD(_activeChar.getAppearance().getTitleColor());
		}
		
		if (containsMask(UserInfoType.INVENTORY_LIMIT))
		{
			writeH(9);
			writeH(0x00);
			writeH(0x00);
			writeH(_activeChar.getInventoryLimit());
			writeC(_activeChar.isCursedWeaponEquipped() ? CursedWeaponsManager.getInstance().getLevel(_activeChar.getCursedWeaponEquippedId()) : 0);
		}
		
		if (containsMask(UserInfoType.UNK_3))
		{
			writeH(9);
			writeC(0x01);
			writeH(0x00);
			writeD(0x00);
		}
	}
	
	private int calculateRelation(L2PcInstance activeChar)
	{
		int relation = 0;
		final L2Party party = activeChar.getParty();
		final L2Clan clan = activeChar.getClan();
		
		if (party != null)
		{
			relation |= 0x08; // Party member
			if (party.getLeader() == _activeChar)
			{
				relation |= 0x10; // Party leader
			}
		}
		
		if (clan != null)
		{
			relation |= 0x20; // Clan member
			if (clan.getLeaderId() == activeChar.getObjectId())
			{
				relation |= 0x40; // Clan leader
			}
		}
		
		if (activeChar.isInSiege())
		{
			relation |= 0x80; // In siege
		}
		
		return relation;
	}
}
