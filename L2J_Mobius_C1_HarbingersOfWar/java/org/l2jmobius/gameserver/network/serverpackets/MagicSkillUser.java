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

import org.l2jmobius.gameserver.model.actor.Creature;

public class MagicSkillUser extends ServerBasePacket
{
	private static final String _S__5A_MAGICSKILLUSER = "[S] 5A MagicSkillUser";
	private final Creature _cha;
	private final int _targetId;
	private final int _skillId;
	private final int _skillLevel;
	private final int _hitTime;
	private final int _reuseDelay;
	
	public MagicSkillUser(Creature cha, Creature target, int skillId, int skillLevel, int hitTime, int reuseDelay)
	{
		_cha = cha;
		_targetId = target.getObjectId();
		_skillId = skillId;
		_skillLevel = skillLevel;
		_hitTime = hitTime;
		_reuseDelay = reuseDelay;
	}
	
	public MagicSkillUser(Creature cha, int skillId, int skillLevel, int hitTime, int reuseDelay)
	{
		_cha = cha;
		_targetId = cha.getTargetId();
		_skillId = skillId;
		_skillLevel = skillLevel;
		_hitTime = hitTime;
		_reuseDelay = reuseDelay;
	}
	
	@Override
	public byte[] getContent()
	{
		_bao.write(90);
		writeD(_cha.getObjectId());
		writeD(_targetId);
		writeD(_skillId);
		writeD(_skillLevel);
		writeD(_hitTime);
		writeD(_reuseDelay);
		writeD(_cha.getX());
		writeD(_cha.getY());
		writeD(_cha.getZ());
		writeH(0);
		return _bao.toByteArray();
	}
	
	@Override
	public String getType()
	{
		return _S__5A_MAGICSKILLUSER;
	}
}
