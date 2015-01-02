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

import java.util.Set;

import com.l2jserver.Config;
import com.l2jserver.gameserver.datatables.ClanTable;
import com.l2jserver.gameserver.enums.NpcInfoType;
import com.l2jserver.gameserver.enums.Team;
import com.l2jserver.gameserver.instancemanager.TownManager;
import com.l2jserver.gameserver.model.L2Clan;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2GuardInstance;
import com.l2jserver.gameserver.model.zone.ZoneId;

/**
 * @author UnAfraid
 */
public class NpcInfo extends AbstractMaskPacket<NpcInfoType>
{
	private final L2Npc _npc;
	private final byte[] _masks = new byte[]
	{
		(byte) 0x00,
		(byte) 0x0C,
		(byte) 0x0C,
		(byte) 0x00,
		(byte) 0x04
	};
	
	private int _initSize = 0;
	private int _blockSize = 0;
	
	private int _clanCrest = 0;
	private int _clanLargeCrest = 0;
	private int _allyCrest = 0;
	private int _allyId = 0;
	private int _clanId = 0;
	private int _statusMask = 0;
	
	public NpcInfo(L2Npc npc)
	{
		_npc = npc;
		
		if (npc.getTemplate().getDisplayId() != npc.getTemplate().getId())
		{
			_masks[2] |= 0x10;
		}
		
		addComponentType(NpcInfoType.ATTACKABLE, NpcInfoType.UNKNOWN1, NpcInfoType.TITLE, NpcInfoType.ID, NpcInfoType.POSITION, NpcInfoType.ALIVE, NpcInfoType.RUNNING);
		
		if (npc.getHeading() > 0)
		{
			addComponentType(NpcInfoType.HEADING);
		}
		
		if ((npc.getStat().getPAtkSpd() > 0) || (npc.getStat().getMAtkSpd() > 0))
		{
			addComponentType(NpcInfoType.ATK_CAST_SPEED);
		}
		
		if (npc.getRunSpeed() > 0)
		{
			addComponentType(NpcInfoType.SPEED_MULTIPLIER);
		}
		
		if ((npc.getLeftHandItem() > 0) || (npc.getRightHandItem() > 0))
		{
			addComponentType(NpcInfoType.EQUIPPED);
		}
		
		if (npc.getTeam() != Team.NONE)
		{
			addComponentType(NpcInfoType.TEAM);
		}
		
		if (npc.getDisplayEffect() > 0)
		{
			addComponentType(NpcInfoType.DISPLAY_EFFECT);
		}
		
		if (npc.isInsideZone(ZoneId.WATER) || npc.isFlying())
		{
			addComponentType(NpcInfoType.SWIM_OR_FLY);
		}
		
		if (npc.isFlying())
		{
			addComponentType(NpcInfoType.FLYING);
		}
		
		if (npc.getMaxHp() > 0)
		{
			addComponentType(NpcInfoType.MAX_HP);
		}
		
		if (npc.getMaxMp() > 0)
		{
			addComponentType(NpcInfoType.MAX_MP);
		}
		
		if (npc.getCurrentHp() <= npc.getMaxHp())
		{
			addComponentType(NpcInfoType.CURRENT_HP);
		}
		
		if (npc.getCurrentMp() <= npc.getMaxMp())
		{
			addComponentType(NpcInfoType.CURRENT_MP);
		}
		
		if (npc.getTemplate().getDisplayId() != npc.getTemplate().getId())
		{
			addComponentType(NpcInfoType.NAME);
		}
		
		if (!npc.getAbnormalVisualEffectsList().isEmpty())
		{
			addComponentType(NpcInfoType.ABNORMALS);
		}
		
		if (npc.getEnchantEffect() > 0)
		{
			addComponentType(NpcInfoType.ENCHANT);
		}
		
		if ((npc.getTransformation() != null) && (npc.getTransformation().getId() > 0))
		{
			addComponentType(NpcInfoType.TRANSFORMATION);
		}
		
		if (npc.isInsideZone(ZoneId.TOWN) && (npc.getCastle() != null) && (Config.SHOW_CREST_WITHOUT_QUEST || npc.getCastle().getShowNpcCrest()) && (npc.getCastle().getOwnerId() != 0))
		{
			int townId = TownManager.getTown(npc.getX(), npc.getY(), npc.getZ()).getTownId();
			if ((townId != 33) && (townId != 22))
			{
				L2Clan clan = ClanTable.getInstance().getClan(npc.getCastle().getOwnerId());
				_clanId = clan.getId();
				_clanCrest = clan.getCrestId();
				_clanLargeCrest = clan.getCrestLargeId();
				_allyCrest = clan.getAllyCrestId();
				_allyId = clan.getAllyId();
				
				addComponentType(NpcInfoType.CLAN);
			}
		}
		
		addComponentType(NpcInfoType.UNKNOWN8);
		
		// TODO: Confirm me
		if (npc.isInCombat())
		{
			_statusMask |= 0x01;
		}
		if (npc.isDead())
		{
			_statusMask |= 0x02;
		}
		if (npc.isTargetable())
		{
			_statusMask |= 0x04;
		}
		if (npc.isShowName())
		{
			_statusMask |= 0x08;
		}
		
		// Add one byte.
		_blockSize++;
	}
	
	@Override
	protected byte[] getMasks()
	{
		return _masks;
	}
	
	@Override
	protected void onNewMaskAdded(NpcInfoType component)
	{
		calcBlockSize(_npc, component);
	}
	
	private void calcBlockSize(L2Npc npc, NpcInfoType type)
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
				_initSize += type.getBlockLength() + (npc.getTitle().length() * 2);
				break;
			}
			case NAME:
			{
				_blockSize += type.getBlockLength() + (npc.getName().length() * 2);
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
		writeC(0x0C);
		writeD(_npc.getObjectId());
		writeC(0x00); // // 0=teleported 1=default 2=summoned
		writeH(37); // mask_bits_37
		writeB(_masks);
		
		// Block 1
		writeC(_initSize);
		
		if (containsMask(NpcInfoType.ATTACKABLE))
		{
			writeC(_npc.isAttackable() && !(_npc instanceof L2GuardInstance) ? 0x01 : 0x00);
		}
		if (containsMask(NpcInfoType.UNKNOWN1))
		{
			writeD(0x00); // unknown
		}
		if (containsMask(NpcInfoType.TITLE))
		{
			writeS(_npc.getTitle());
		}
		
		// Block 2
		writeH(_blockSize);
		if (containsMask(NpcInfoType.ID))
		{
			writeD(_npc.getId() + 1000000);
		}
		if (containsMask(NpcInfoType.POSITION))
		{
			writeD(_npc.getX());
			writeD(_npc.getY());
			writeD(_npc.getZ());
		}
		if (containsMask(NpcInfoType.HEADING))
		{
			writeD(_npc.getHeading());
		}
		if (containsMask(NpcInfoType.UNKNOWN2))
		{
			writeD(0x00); // Unknown
		}
		if (containsMask(NpcInfoType.ATK_CAST_SPEED))
		{
			writeD(_npc.getPAtkSpd());
			writeD(_npc.getMAtkSpd());
		}
		if (containsMask(NpcInfoType.SPEED_MULTIPLIER))
		{
			_buf.putFloat((float) _npc.getStat().getMovementSpeedMultiplier());
			_buf.putFloat(_npc.getStat().getAttackSpeedMultiplier());
		}
		if (containsMask(NpcInfoType.EQUIPPED))
		{
			writeD(_npc.getRightHandItem());
			writeD(0x00); // Armor id?
			writeD(_npc.getLeftHandItem());
		}
		if (containsMask(NpcInfoType.ALIVE))
		{
			writeC(_npc.isDead() ? 0x00 : 0x01);
		}
		if (containsMask(NpcInfoType.RUNNING))
		{
			writeC(_npc.isRunning() ? 0x01 : 0x00);
		}
		if (containsMask(NpcInfoType.SWIM_OR_FLY))
		{
			writeC(_npc.isInsideZone(ZoneId.WATER) ? 0x01 : _npc.isFlying() ? 0x02 : 0x00);
		}
		if (containsMask(NpcInfoType.TEAM))
		{
			writeC(_npc.getTeam().getId());
		}
		if (containsMask(NpcInfoType.ENCHANT))
		{
			writeD(_npc.getEnchantEffect());
		}
		if (containsMask(NpcInfoType.FLYING))
		{
			writeD(_npc.isFlying() ? 0x01 : 00);
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
			writeD(_npc.getDisplayEffect());
		}
		if (containsMask(NpcInfoType.TRANSFORMATION))
		{
			writeD(0x00); // Transformation ID
		}
		if (containsMask(NpcInfoType.CURRENT_HP))
		{
			writeD((int) _npc.getCurrentHp());
		}
		if (containsMask(NpcInfoType.CURRENT_MP))
		{
			writeD((int) _npc.getCurrentMp());
		}
		if (containsMask(NpcInfoType.MAX_HP))
		{
			writeD(_npc.getMaxHp());
		}
		if (containsMask(NpcInfoType.MAX_MP))
		{
			writeD(_npc.getMaxMp());
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
			writeS(_npc.getName());
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
			writeC(0x00); // PVP flag
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
		
		writeC(_statusMask);
		
		if (containsMask(NpcInfoType.ABNORMALS))
		{
			final Set<Integer> visualEffects = _npc.getAbnormalVisualEffectsList();
			writeH(visualEffects.size());
			for (int visualEffect : visualEffects)
			{
				writeH(visualEffect);
			}
		}
	}
}