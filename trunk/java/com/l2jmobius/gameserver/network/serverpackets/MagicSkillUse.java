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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.l2jmobius.gameserver.datatables.SkillData;
import com.l2jmobius.gameserver.model.Location;
import com.l2jmobius.gameserver.model.actor.L2Character;
import com.l2jmobius.gameserver.model.interfaces.IPositionable;

/**
 * MagicSkillUse server packet implementation.
 * @author UnAfraid, NosBit
 */
public final class MagicSkillUse extends L2GameServerPacket
{
	private final int _skillId;
	private final int _skillLevel;
	private final int _maxLevel;
	private final int _hitTime;
	private final int _reuseDelay;
	private final L2Character _activeChar;
	private final L2Character _target;
	private List<Integer> _unknown = Collections.emptyList();
	private final List<Location> _groundLocations;
	
	public MagicSkillUse(L2Character cha, L2Character target, int skillId, int skillLevel, int hitTime, int reuseDelay)
	{
		_activeChar = cha;
		_target = target;
		_skillId = skillId;
		_skillLevel = skillLevel;
		_maxLevel = SkillData.getInstance().getMaxLevel(_skillId);
		_hitTime = hitTime;
		_reuseDelay = reuseDelay;
		_groundLocations = cha.isPlayer() && (cha.getActingPlayer().getCurrentSkillWorldPosition() != null) ? Arrays.asList(cha.getActingPlayer().getCurrentSkillWorldPosition()) : Collections.<Location> emptyList();
	}
	
	public MagicSkillUse(L2Character cha, int skillId, int skillLevel, int hitTime, int reuseDelay)
	{
		this(cha, cha, skillId, skillLevel, hitTime, reuseDelay);
	}
	
	/**
	 * @param l2Character
	 * @param target
	 * @param displayId
	 * @param displayLevel
	 * @param skillTime
	 * @param reuseDelay
	 * @param blowSuccess
	 */
	public MagicSkillUse(L2Character l2Character, L2Character target, int displayId, int displayLevel, int skillTime, int reuseDelay, boolean blowSuccess)
	{
		this(l2Character, target, displayId, displayLevel, skillTime, reuseDelay);
		if (blowSuccess)
		{
			_unknown = Arrays.asList(0);
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x48);
		writeD(0x00); // TODO: Find me!
		writeD(_activeChar.getObjectId());
		writeD(_target.getObjectId());
		writeD(_skillId);
		if (_skillLevel < 100)
		{
			writeD(_skillLevel);
		}
		else
		{
			writeH(_maxLevel);
			writeH(_skillLevel);
		}
		writeD(_hitTime);
		writeD(-1); // TODO: Find me!
		writeD(_reuseDelay);
		writeLoc(_activeChar);
		writeH(_unknown.size()); // TODO: Implement me!
		for (int unknown : _unknown)
		{
			writeH(unknown);
		}
		writeH(_groundLocations.size());
		for (IPositionable target : _groundLocations)
		{
			writeLoc(target);
		}
		writeLoc(_target);
		writeD(0x00); // TODO: Find me!
		writeD(0x00); // TODO: Find me!
	}
}
