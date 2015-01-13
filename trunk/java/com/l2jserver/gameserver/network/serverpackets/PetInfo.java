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

import com.l2jserver.gameserver.model.actor.L2Summon;
import com.l2jserver.gameserver.model.actor.instance.L2PetInstance;
import com.l2jserver.gameserver.model.actor.instance.L2ServitorInstance;
import com.l2jserver.gameserver.model.skills.AbnormalVisualEffect;

public class PetInfo extends L2GameServerPacket
{
	private final L2Summon _summon;
	private final int _val;
	private final int _runSpd, _walkSpd;
	private final int _swimRunSpd;
	private final int _swimWalkSpd;
	private final int _flRunSpd = 0;
	private final int _flWalkSpd = 0;
	private final int _flyRunSpd;
	private final int _flyWalkSpd;
	private final double _moveMultiplier;
	private int _maxFed, _curFed;
	
	public PetInfo(L2Summon summon, int val)
	{
		_summon = summon;
		_moveMultiplier = summon.getMovementSpeedMultiplier();
		_runSpd = (int) Math.round(summon.getRunSpeed() / _moveMultiplier);
		_walkSpd = (int) Math.round(summon.getWalkSpeed() / _moveMultiplier);
		_swimRunSpd = (int) Math.round(summon.getSwimRunSpeed() / _moveMultiplier);
		_swimWalkSpd = (int) Math.round(summon.getSwimWalkSpeed() / _moveMultiplier);
		_flyRunSpd = summon.isFlying() ? _runSpd : 0;
		_flyWalkSpd = summon.isFlying() ? _walkSpd : 0;
		_val = val;
		if (summon.isPet())
		{
			final L2PetInstance pet = (L2PetInstance) _summon;
			_curFed = pet.getCurrentFed(); // how fed it is
			_maxFed = pet.getMaxFed(); // max fed it can be
		}
		else if (summon.isServitor())
		{
			final L2ServitorInstance sum = (L2ServitorInstance) _summon;
			_curFed = sum.getLifeTimeRemaining();
			_maxFed = sum.getLifeTime();
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xB2);
		writeC(_summon.getSummonType());
		writeD(_summon.getObjectId());
		writeD(_summon.getTemplate().getDisplayId() + 1000000);
		
		writeD(_summon.getX());
		writeD(_summon.getY());
		writeD(_summon.getZ());
		writeD(_summon.getHeading());
		
		writeD(_summon.getStat().getMAtkSpd());
		writeD(_summon.getStat().getPAtkSpd());
		
		writeH(_runSpd);
		writeH(_walkSpd);
		writeH(_swimRunSpd);
		writeH(_swimWalkSpd);
		writeH(_flRunSpd);
		writeH(_flWalkSpd);
		writeH(_flyRunSpd);
		writeH(_flyWalkSpd);
		
		writeF(_moveMultiplier);
		writeF(_summon.getAttackSpeedMultiplier()); // attack speed multiplier
		writeF(_summon.getTemplate().getfCollisionRadius());
		writeF(_summon.getTemplate().getfCollisionHeight());
		
		writeD(_summon.getWeapon()); // right hand weapon
		writeD(_summon.getArmor()); // body armor
		writeD(0x00); // left hand weapon
		
		writeC(_summon.isShowSummonAnimation() ? 2 : _val); // 0=teleported 1=default 2=summoned
		writeD(-1); // High Five NPCString ID
		if (_summon.isPet())
		{
			writeS(_summon.getName()); // Pet name.
		}
		else
		{
			writeS(_summon.getTemplate().isUsingServerSideName() ? _summon.getName() : ""); // Summon name.
		}
		writeD(-1); // High Five NPCString ID
		writeS(_summon.getTitle()); // owner name
		
		writeC(_summon.getPvpFlag()); // ?
		writeD(_summon.getKarma());
		
		writeD(_curFed); // how fed it is
		writeD(_maxFed); // max fed it can be
		writeD((int) _summon.getCurrentHp());// current hp
		writeD(_summon.getMaxHp());// max hp
		writeD((int) _summon.getCurrentMp());// current mp
		writeD(_summon.getMaxMp());// max mp
		
		writeQ(_summon.getStat().getSp()); // sp
		writeC(_summon.getLevel());// lvl
		writeQ(_summon.getStat().getExp());
		
		if (_summon.getExpForThisLevel() > _summon.getStat().getExp())
		{
			writeQ(_summon.getStat().getExp());// 0% absolute value
		}
		else
		{
			writeQ(_summon.getExpForThisLevel());// 0% absolute value
		}
		
		writeQ(_summon.getExpForNextLevel());// 100% absoulte value
		
		writeD(_summon.isPet() ? _summon.getInventory().getTotalWeight() : 0);// weight
		writeD(_summon.getMaxLoad());// max weight it can carry
		writeD(_summon.getPAtk(null));// patk
		writeD(_summon.getPDef(null));// pdef
		writeD(_summon.getAccuracy());// accuracy
		writeD(_summon.getEvasionRate(null));// evasion
		writeD(_summon.getCriticalHit(null, null));// critical
		writeD(_summon.getMAtk(null, null));// matk
		writeD(_summon.getMDef(null, null));// mdef
		writeD(_summon.getMagicAccuracy()); // magic accuracy
		writeD(_summon.getMagicEvasionRate(null)); // magic evasion
		writeD(_summon.getMCriticalHit(null, null)); // mcritical
		writeD((int) _summon.getStat().getMoveSpeed());// speed
		writeD(_summon.getPAtkSpd());// atkspeed
		writeD(_summon.getMAtkSpd());// casting speed
		
		writeC(0); // TODO: Check me, might be ride status
		writeC(_summon.getTeam().getId()); // Confirmed
		writeC(_summon.getSoulShotsPerHit()); // How many soulshots this servitor uses per hit - Confirmed
		writeC(_summon.getSpiritShotsPerHit()); // How many spiritshots this servitor uses per hit - - Confirmed
		
		writeD(0x00); // TODO: Find me
		writeD(_summon.getFormId()); // Transformation ID - Confirmed
		
		writeC(0x00); // Used Summon Points
		writeC(0x00); // Maximum Summon Points
		
		final Set<AbnormalVisualEffect> aves = _summon.getCurrentAbnormalVisualEffects();
		writeH(aves.size()); // Confirmed
		for (AbnormalVisualEffect ave : aves)
		{
			writeH(ave.getClientId()); // Confirmed
		}
		
		writeC(0x00); // TODO: Find me
	}
}
