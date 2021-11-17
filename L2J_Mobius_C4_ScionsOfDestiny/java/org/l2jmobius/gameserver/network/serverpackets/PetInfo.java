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
import org.l2jmobius.gameserver.model.actor.Summon;
import org.l2jmobius.gameserver.model.actor.instance.Pet;
import org.l2jmobius.gameserver.model.actor.instance.Servitor;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * @version $Revision: 1.6.2.5.2.12 $ $Date: 2005/03/31 09:19:16 $
 */
public class PetInfo implements IClientOutgoingPacket
{
	private final Summon _summon;
	private final int _x;
	private final int _y;
	private final int _z;
	private final int _heading;
	private final boolean _isSummoned;
	private final int _mAtkSpd;
	private final int _pAtkSpd;
	private final int _runSpd;
	private final int _walkSpd;
	private final int _swimRunSpd;
	private final int _swimWalkSpd;
	private int _flRunSpd;
	private int _flWalkSpd;
	private int _flyRunSpd;
	private int _flyWalkSpd;
	private final int _maxHp;
	private final int _maxMp;
	private int _maxFed;
	private int _curFed;
	
	/**
	 * rev 478 dddddddddddddddddddffffdddcccccSSdddddddddddddddddddddddddddhc
	 * @param summon
	 */
	public PetInfo(Summon summon)
	{
		_summon = summon;
		_isSummoned = _summon.isShowSummonAnimation();
		_x = _summon.getX();
		_y = _summon.getY();
		_z = _summon.getZ();
		_heading = _summon.getHeading();
		_mAtkSpd = _summon.getMAtkSpd();
		_pAtkSpd = _summon.getPAtkSpd();
		_runSpd = _summon.getRunSpeed();
		_walkSpd = _summon.getWalkSpeed();
		_swimRunSpd = _flRunSpd = _flyRunSpd = _runSpd;
		_swimWalkSpd = _flWalkSpd = _flyWalkSpd = _walkSpd;
		_maxHp = _summon.getMaxHp();
		_maxMp = _summon.getMaxMp();
		if (_summon instanceof Pet)
		{
			final Pet pet = (Pet) _summon;
			_curFed = pet.getCurrentFed(); // how fed it is
			_maxFed = pet.getMaxFed(); // max fed it can be
		}
		else if (_summon instanceof Servitor)
		{
			final Servitor sum = (Servitor) _summon;
			_curFed = sum.getTimeRemaining();
			_maxFed = sum.getTotalLifeTime();
		}
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.PET_INFO.writeId(packet);
		packet.writeD(_summon.getSummonType());
		packet.writeD(_summon.getObjectId());
		packet.writeD(_summon.getTemplate().getDisplayId() + 1000000);
		packet.writeD(0); // 1=attackable
		
		packet.writeD(_x);
		packet.writeD(_y);
		packet.writeD(_z);
		packet.writeD(_heading);
		packet.writeD(0);
		packet.writeD(_mAtkSpd);
		packet.writeD(_pAtkSpd);
		packet.writeD(_runSpd);
		packet.writeD(_walkSpd);
		packet.writeD(_swimRunSpd);
		packet.writeD(_swimWalkSpd);
		packet.writeD(_flRunSpd);
		packet.writeD(_flWalkSpd);
		packet.writeD(_flyRunSpd);
		packet.writeD(_flyWalkSpd);
		
		packet.writeF(1/* _cha.getProperMultiplier() */);
		packet.writeF(1/* _cha.getAttackSpeedMultiplier() */);
		packet.writeF(_summon.getTemplate().getFCollisionRadius());
		packet.writeF(_summon.getTemplate().getFCollisionHeight());
		packet.writeD(0); // right hand weapon
		packet.writeD(0);
		packet.writeD(0); // left hand weapon
		packet.writeC(1); // name above char 1=true ... ??
		packet.writeC(_summon.isRunning() ? 1 : 0); // running=1
		packet.writeC(_summon.isInCombat() ? 1 : 0); // attacking 1=true
		packet.writeC(_summon.isAlikeDead() ? 1 : 0); // dead 1=true
		packet.writeC(_isSummoned ? 2 : 0); // invisible ?? 0=false 1=true 2=summoned (only works if model has a summon animation)
		packet.writeS(_summon.getName());
		packet.writeS(_summon.getTitle());
		packet.writeD(1);
		packet.writeD(_summon.getOwner() != null ? _summon.getOwner().getPvpFlag() : 0); // 0 = white,2= purpleblink, if its greater then karma = purple
		packet.writeD(_summon.getOwner() != null ? _summon.getOwner().getKarma() : 0); // karma
		packet.writeD(_curFed); // how fed it is
		packet.writeD(_maxFed); // max fed it can be
		packet.writeD((int) _summon.getCurrentHp()); // current hp
		packet.writeD(_maxHp); // max hp
		packet.writeD((int) _summon.getCurrentMp()); // current mp
		packet.writeD(_maxMp); // max mp
		packet.writeD(_summon.getStat().getSp()); // sp
		packet.writeD(_summon.getLevel()); // level
		packet.writeD((int) _summon.getStat().getExp());
		packet.writeD((int) _summon.getExpForThisLevel()); // 0% absolute value
		packet.writeD((int) _summon.getExpForNextLevel()); // 100% absoulte value
		packet.writeD(_summon instanceof Pet ? _summon.getInventory().getTotalWeight() : 0); // weight
		packet.writeD(_summon.getMaxLoad()); // max weight it can carry
		packet.writeD(_summon.getPAtk(null)); // patk
		packet.writeD(_summon.getPDef(null)); // pdef
		packet.writeD(_summon.getMAtk(null, null)); // matk
		packet.writeD(_summon.getMDef(null, null)); // mdef
		packet.writeD(_summon.getAccuracy()); // accuracy
		packet.writeD(_summon.getEvasionRate(null)); // evasion
		packet.writeD(_summon.getCriticalHit(null, null)); // critical
		packet.writeD(_runSpd); // speed
		packet.writeD(_summon.getPAtkSpd()); // atkspeed
		packet.writeD(_summon.getMAtkSpd()); // casting speed
		
		packet.writeD(0); // c2 abnormal visual effect... bleed=1; poison=2; poison & bleed=3; flame=4;
		final int npcId = _summon.getTemplate().getNpcId();
		if ((npcId >= 12526) && (npcId <= 12528))
		{
			packet.writeH(1); // c2 ride button
		}
		else
		{
			packet.writeH(0);
		}
		
		packet.writeC(0); // c2
		
		// Following all added in C4.
		packet.writeH(0); // ??
		packet.writeC(0); // team aura (1 = blue, 2 = red)
		packet.writeD(_summon.getSoulShotsPerHit()); // How many soulshots this servitor uses per hit
		packet.writeD(_summon.getSpiritShotsPerHit()); // How many spiritshots this servitor uses per hit
		return true;
	}
}
