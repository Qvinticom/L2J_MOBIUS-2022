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

import java.util.Set;

import com.l2jmobius.gameserver.enums.NpcInfoType;
import com.l2jmobius.gameserver.enums.Team;
import com.l2jmobius.gameserver.model.actor.L2Summon;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.skills.AbnormalVisualEffect;
import com.l2jmobius.gameserver.model.zone.ZoneId;

/**
 * @author Sdw
 */
public class ExPetInfo extends AbstractMaskPacket<NpcInfoType>
{
	private final L2Summon _summon;
	private final L2PcInstance _attacker;
	private final int _val;
	private final byte[] _masks = new byte[]
	{
		(byte) 0x00,
		(byte) 0x0C,
		(byte) 0x0C,
		(byte) 0x00,
		(byte) 0x00
	};
	
	private int _initSize = 0;
	private int _blockSize = 0;
	
	private int _clanCrest = 0;
	private int _clanLargeCrest = 0;
	private int _allyCrest = 0;
	private int _allyId = 0;
	private int _clanId = 0;
	private int _statusMask = 0;
	private final String _title;
	private final Set<AbnormalVisualEffect> _abnormalVisualEffects;
	
	public ExPetInfo(L2Summon summon, L2PcInstance attacker, int val)
	{
		_summon = summon;
		_attacker = attacker;
		_title = (summon.getOwner() != null) && summon.getOwner().isOnline() ? summon.getOwner().getName() : "";
		_val = val;
		_abnormalVisualEffects = summon.getCurrentAbnormalVisualEffects();
		
		if (summon.getTemplate().getDisplayId() != summon.getTemplate().getId())
		{
			_masks[2] |= 0x10;
			addComponentType(NpcInfoType.NAME);
		}
		
		addComponentType(NpcInfoType.ATTACKABLE, NpcInfoType.UNKNOWN1, NpcInfoType.TITLE, NpcInfoType.ID, NpcInfoType.POSITION, NpcInfoType.ALIVE, NpcInfoType.RUNNING);
		
		if (summon.getHeading() > 0)
		{
			addComponentType(NpcInfoType.HEADING);
		}
		
		if ((summon.getStat().getPAtkSpd() > 0) || (summon.getStat().getMAtkSpd() > 0))
		{
			addComponentType(NpcInfoType.ATK_CAST_SPEED);
		}
		
		if (summon.getRunSpeed() > 0)
		{
			addComponentType(NpcInfoType.SPEED_MULTIPLIER);
		}
		
		if ((summon.getWeapon() > 0) || (summon.getArmor() > 0))
		{
			addComponentType(NpcInfoType.EQUIPPED);
		}
		
		if (summon.getTeam() != Team.NONE)
		{
			addComponentType(NpcInfoType.TEAM);
		}
		
		if (summon.isInsideZone(ZoneId.WATER) || summon.isFlying())
		{
			addComponentType(NpcInfoType.SWIM_OR_FLY);
		}
		
		if (summon.isFlying())
		{
			addComponentType(NpcInfoType.FLYING);
		}
		
		if (summon.getMaxHp() > 0)
		{
			addComponentType(NpcInfoType.MAX_HP);
		}
		
		if (summon.getMaxMp() > 0)
		{
			addComponentType(NpcInfoType.MAX_MP);
		}
		
		if (summon.getCurrentHp() <= summon.getMaxHp())
		{
			addComponentType(NpcInfoType.CURRENT_HP);
		}
		
		if (summon.getCurrentMp() <= summon.getMaxMp())
		{
			addComponentType(NpcInfoType.CURRENT_MP);
		}
		
		if (!_abnormalVisualEffects.isEmpty())
		{
			addComponentType(NpcInfoType.ABNORMALS);
		}
		
		if (summon.getTemplate().getWeaponEnchant() > 0)
		{
			addComponentType(NpcInfoType.ENCHANT);
		}
		
		if ((summon.getTransformation() != null) && (summon.getTransformation().getId() > 0))
		{
			addComponentType(NpcInfoType.TRANSFORMATION);
		}
		
		if (summon.getOwner().getClan() != null)
		{
			_clanId = summon.getOwner().getClanId();
			_clanCrest = summon.getOwner().getClanCrestId();
			_clanLargeCrest = summon.getOwner().getClanCrestLargeId();
			_allyCrest = summon.getOwner().getAllyId();
			_allyId = summon.getOwner().getAllyCrestId();
			
			addComponentType(NpcInfoType.CLAN);
		}
		
		addComponentType(NpcInfoType.UNKNOWN8);
		
		// TODO: Confirm me
		if (summon.isInCombat())
		{
			_statusMask |= 0x01;
		}
		if (summon.isDead())
		{
			_statusMask |= 0x02;
		}
		if (summon.isTargetable())
		{
			_statusMask |= 0x04;
		}
		
		_statusMask |= 0x08;
		
		if (_statusMask != 0)
		{
			addComponentType(NpcInfoType.VISUAL_STATE);
		}
	}
	
	@Override
	protected byte[] getMasks()
	{
		return _masks;
	}
	
	@Override
	protected void onNewMaskAdded(NpcInfoType component)
	{
		calcBlockSize(_summon, component);
	}
	
	private void calcBlockSize(L2Summon summon, NpcInfoType type)
	{
		switch (type)
		{
			case ATTACKABLE:
			case UNKNOWN1:
			{
				_initSize += type.getBlockLength();
				break;
			}
			case TITLE:
			{
				_initSize += type.getBlockLength() + (_title.length() * 2);
				break;
			}
			case NAME:
			{
				_blockSize += type.getBlockLength() + (summon.getName().length() * 2);
				break;
			}
			default:
			{
				_blockSize += type.getBlockLength();
				break;
			}
		}
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x15E);
		writeD(_summon.getObjectId());
		writeC(_val); // // 0=teleported 1=default 2=summoned
		writeH(37); // mask_bits_37
		writeB(_masks);
		
		// Block 1
		writeC(_initSize);
		
		if (containsMask(NpcInfoType.ATTACKABLE))
		{
			writeC(_summon.isAutoAttackable(_attacker) ? 0x01 : 0x00);
		}
		if (containsMask(NpcInfoType.UNKNOWN1))
		{
			writeD(0x00); // unknown
		}
		if (containsMask(NpcInfoType.TITLE))
		{
			writeS(_title);
		}
		
		// Block 2
		writeH(_blockSize);
		if (containsMask(NpcInfoType.ID))
		{
			writeD(_summon.getTemplate().getDisplayId() + 1000000);
		}
		if (containsMask(NpcInfoType.POSITION))
		{
			writeD(_summon.getX());
			writeD(_summon.getY());
			writeD(_summon.getZ());
		}
		if (containsMask(NpcInfoType.HEADING))
		{
			writeD(_summon.getHeading());
		}
		if (containsMask(NpcInfoType.UNKNOWN2))
		{
			writeD(0x00); // Unknown
		}
		if (containsMask(NpcInfoType.ATK_CAST_SPEED))
		{
			writeD((int) _summon.getPAtkSpd());
			writeD(_summon.getMAtkSpd());
		}
		if (containsMask(NpcInfoType.SPEED_MULTIPLIER))
		{
			_buf.putFloat((float) _summon.getStat().getMovementSpeedMultiplier());
			_buf.putFloat(_summon.getStat().getAttackSpeedMultiplier());
		}
		if (containsMask(NpcInfoType.EQUIPPED))
		{
			writeD(_summon.getWeapon());
			writeD(_summon.getArmor()); // Armor id?
			writeD(0x00);
		}
		if (containsMask(NpcInfoType.ALIVE))
		{
			writeC(_summon.isDead() ? 0x00 : 0x01);
		}
		if (containsMask(NpcInfoType.RUNNING))
		{
			writeC(_summon.isRunning() ? 0x01 : 0x00);
		}
		if (containsMask(NpcInfoType.SWIM_OR_FLY))
		{
			writeC(_summon.isInsideZone(ZoneId.WATER) ? 0x01 : _summon.isFlying() ? 0x02 : 0x00);
		}
		if (containsMask(NpcInfoType.TEAM))
		{
			writeC(_summon.getTeam().getId());
		}
		if (containsMask(NpcInfoType.ENCHANT))
		{
			writeD(_summon.getTemplate().getWeaponEnchant());
		}
		if (containsMask(NpcInfoType.FLYING))
		{
			writeD(_summon.isFlying() ? 0x01 : 00);
		}
		if (containsMask(NpcInfoType.CLONE))
		{
			writeD(0x00); // Player ObjectId with Decoy
		}
		if (containsMask(NpcInfoType.UNKNOWN8))
		{
			// No visual effect
			writeD(0x00); // Unknown
		}
		if (containsMask(NpcInfoType.DISPLAY_EFFECT))
		{
			writeD(0x00);
		}
		if (containsMask(NpcInfoType.TRANSFORMATION))
		{
			writeD(0x00); // Transformation ID
		}
		if (containsMask(NpcInfoType.CURRENT_HP))
		{
			writeD((int) _summon.getCurrentHp());
		}
		if (containsMask(NpcInfoType.CURRENT_MP))
		{
			writeD((int) _summon.getCurrentMp());
		}
		if (containsMask(NpcInfoType.MAX_HP))
		{
			writeD(_summon.getMaxHp());
		}
		if (containsMask(NpcInfoType.MAX_MP))
		{
			writeD(_summon.getMaxMp());
		}
		if (containsMask(NpcInfoType.UNKNOWN11))
		{
			writeC(0x00); // 2 - do some animation on spawn
		}
		if (containsMask(NpcInfoType.UNKNOWN12))
		{
			writeD(0x00);
			writeD(0x00);
		}
		if (containsMask(NpcInfoType.NAME))
		{
			writeS(_summon.getName());
		}
		if (containsMask(NpcInfoType.NAME_NPCSTRINGID))
		{
			writeD(-1); // NPCStringId for name
		}
		if (containsMask(NpcInfoType.TITLE_NPCSTRINGID))
		{
			writeD(-1); // NPCStringId for title
		}
		if (containsMask(NpcInfoType.PVP_FLAG))
		{
			writeC(_summon.getPvpFlag()); // PVP flag
		}
		if (containsMask(NpcInfoType.NAME_COLOR))
		{
			writeD(0x00); // Name color
		}
		if (containsMask(NpcInfoType.CLAN))
		{
			writeD(_clanId);
			writeD(_clanCrest);
			writeD(_clanLargeCrest);
			writeD(_allyId);
			writeD(_allyCrest);
		}
		
		if (containsMask(NpcInfoType.VISUAL_STATE))
		{
			writeC(_statusMask);
		}
		
		if (containsMask(NpcInfoType.ABNORMALS))
		{
			writeH(_abnormalVisualEffects.size());
			for (AbnormalVisualEffect abnormalVisualEffect : _abnormalVisualEffects)
			{
				writeH(abnormalVisualEffect.getClientId());
			}
		}
	}
}