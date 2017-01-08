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

import com.l2jmobius.commons.network.PacketWriter;
import com.l2jmobius.gameserver.model.actor.L2Character;
import com.l2jmobius.gameserver.model.actor.instance.L2TrapInstance;
import com.l2jmobius.gameserver.network.OutgoingPackets;

public abstract class AbstractNpcInfo implements IClientOutgoingPacket
{
	protected int _x, _y, _z, _heading;
	protected int _idTemplate;
	protected boolean _isAttackable, _isSummoned;
	protected int _mAtkSpd, _pAtkSpd;
	protected final int _runSpd, _walkSpd;
	protected final int _swimRunSpd, _swimWalkSpd;
	protected final int _flyRunSpd, _flyWalkSpd;
	protected double _moveMultiplier;
	
	protected int _rhand, _lhand, _chest, _enchantEffect;
	protected double _collisionHeight, _collisionRadius;
	protected String _name = "";
	protected String _title = "";
	
	public AbstractNpcInfo(L2Character cha)
	{
		_isSummoned = cha.isShowSummonAnimation();
		_x = cha.getX();
		_y = cha.getY();
		_z = cha.getZ();
		_heading = cha.getHeading();
		_mAtkSpd = cha.getMAtkSpd();
		_pAtkSpd = cha.getPAtkSpd();
		_moveMultiplier = cha.getMovementSpeedMultiplier();
		_runSpd = (int) Math.round(cha.getRunSpeed() / _moveMultiplier);
		_walkSpd = (int) Math.round(cha.getWalkSpeed() / _moveMultiplier);
		_swimRunSpd = (int) Math.round(cha.getSwimRunSpeed() / _moveMultiplier);
		_swimWalkSpd = (int) Math.round(cha.getSwimWalkSpeed() / _moveMultiplier);
		_flyRunSpd = cha.isFlying() ? _runSpd : 0;
		_flyWalkSpd = cha.isFlying() ? _walkSpd : 0;
	}
	
	public static class TrapInfo extends AbstractNpcInfo
	{
		private final L2TrapInstance _trap;
		
		public TrapInfo(L2TrapInstance cha, L2Character attacker)
		{
			super(cha);
			
			_trap = cha;
			_idTemplate = cha.getTemplate().getDisplayId();
			_isAttackable = cha.isAutoAttackable(attacker);
			_rhand = 0;
			_lhand = 0;
			_collisionHeight = _trap.getTemplate().getfCollisionHeight();
			_collisionRadius = _trap.getTemplate().getfCollisionRadius();
			if (cha.getTemplate().isUsingServerSideName())
			{
				_name = cha.getName();
			}
			_title = cha.getOwner() != null ? cha.getOwner().getName() : "";
		}
		
		@Override
		public boolean write(PacketWriter packet)
		{
			OutgoingPackets.NPC_INFO.writeId(packet);
			
			packet.writeD(_trap.getObjectId());
			packet.writeD(_idTemplate + 1000000); // npctype id
			packet.writeD(_isAttackable ? 1 : 0);
			packet.writeD(_x);
			packet.writeD(_y);
			packet.writeD(_z);
			packet.writeD(_heading);
			packet.writeD(0x00);
			packet.writeD(_mAtkSpd);
			packet.writeD(_pAtkSpd);
			packet.writeD(_runSpd);
			packet.writeD(_walkSpd);
			packet.writeD(_swimRunSpd);
			packet.writeD(_swimWalkSpd);
			packet.writeD(_flyRunSpd);
			packet.writeD(_flyWalkSpd);
			packet.writeD(_flyRunSpd);
			packet.writeD(_flyWalkSpd);
			packet.writeF(_moveMultiplier);
			packet.writeF(_trap.getAttackSpeedMultiplier());
			packet.writeF(_collisionRadius);
			packet.writeF(_collisionHeight);
			packet.writeD(_rhand); // right hand weapon
			packet.writeD(_chest);
			packet.writeD(_lhand); // left hand weapon
			packet.writeC(1); // name above char 1=true ... ??
			packet.writeC(1);
			packet.writeC(_trap.isInCombat() ? 1 : 0);
			packet.writeC(_trap.isAlikeDead() ? 1 : 0);
			packet.writeC(_isSummoned ? 2 : 0); // invisible ?? 0=false 1=true 2=summoned (only works if model has a summon animation)
			packet.writeD(-1); // High Five NPCString ID
			packet.writeS(_name);
			packet.writeD(-1); // High Five NPCString ID
			packet.writeS(_title);
			packet.writeD(0x00); // title color 0 = client default
			
			packet.writeD(_trap.getPvpFlag());
			packet.writeD(_trap.getReputation());
			
			packet.writeD(0); // was AVE and was adding stealth
			packet.writeD(0x00); // clan id
			packet.writeD(0x00); // crest id
			packet.writeD(0000); // C2
			packet.writeD(0000); // C2
			packet.writeC(0000); // C2
			
			packet.writeC(_trap.getTeam().getId());
			
			packet.writeF(_collisionRadius);
			packet.writeF(_collisionHeight);
			packet.writeD(0x00); // C4
			packet.writeD(0x00); // C6
			packet.writeD(0x00);
			packet.writeD(0); // CT1.5 Pet form and skills
			packet.writeC(0x01);
			packet.writeC(0x01);
			packet.writeD(0x00);
			return true;
		}
	}
}
