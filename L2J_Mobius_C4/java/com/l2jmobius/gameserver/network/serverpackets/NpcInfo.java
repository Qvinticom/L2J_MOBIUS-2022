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
import com.l2jmobius.gameserver.model.L2Character;
import com.l2jmobius.gameserver.model.L2Summon;
import com.l2jmobius.gameserver.model.actor.instance.L2MonsterInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2NpcInstance;

/**
 * This class ...
 * @version $Revision: 1.7.2.4.2.9 $ $Date: 2005/04/11 10:05:54 $
 */
public class NpcInfo extends L2GameServerPacket
{
	// ddddddddddddddddddffffdddcccccSSddd dddddc
	// ddddddddddddddddddffffdddcccccSSddd dddddccffd
	
	private static final String _S__22_NPCINFO = "[S] 16 NpcInfo";
	private final L2Character _cha;
	private final int _x, _y, _z, _heading;
	private final int _idTemplate;
	private final boolean _isAttackable, _showSpawnAnimation;
	private final int _mAtkSpd, _pAtkSpd;
	private final int _runSpd, _walkSpd, _swimRunSpd, _swimWalkSpd;
	private int _flRunSpd;
	private int _flWalkSpd;
	private int _flyRunSpd;
	private int _flyWalkSpd;
	private final float _movementMultiplier;
	private final float _attackSpeedMultiplier;
	private final int _rhand, _lhand, _chest, _val;
	private final double collisionHeight, collisionRadius;
	private String _name = "";
	private String _title = "";
	
	/**
	 * @param cha
	 * @param attacker
	 */
	public NpcInfo(L2NpcInstance cha, L2Character attacker)
	{
		_cha = cha;
		_idTemplate = cha.getTemplate().idTemplate;
		_isAttackable = cha.isAutoAttackable(attacker);
		_showSpawnAnimation = cha.isShowSummonAnimation();
		_rhand = cha.getTemplate().rhand;
		_lhand = cha.getTemplate().lhand;
		_chest = 0;
		
		_val = 0;
		
		collisionHeight = cha.getCollisionHeight();
		
		collisionRadius = cha.getCollisionRadius();
		
		if (cha.getTemplate().serverSideName)
		{
			_name = cha.getTemplate().name;
		}
		
		if (Config.CHAMPION_ENABLE && cha.isChampion())
		{
			_title = (Config.CHAMPION_TITLE);
		}
		else if (cha.getTemplate().serverSideTitle)
		{
			_title = cha.getTemplate().title;
		}
		else
		{
			_title = cha.getTitle();
		}
		
		if (Config.SHOW_NPC_LVL && (_cha instanceof L2MonsterInstance))
		{
			String t = "Lv " + cha.getLevel() + (cha.getAggroRange() > 0 ? "*" : "");
			if (_title != null)
			{
				t += " " + _title;
			}
			
			_title = t;
		}
		
		_x = _cha.getX();
		_y = _cha.getY();
		_z = _cha.getZ();
		_heading = _cha.getHeading();
		_mAtkSpd = _cha.getMAtkSpd();
		_pAtkSpd = _cha.getPAtkSpd();
		_runSpd = _cha.getTemplate().baseRunSpd;
		_walkSpd = _cha.getTemplate().baseWalkSpd;
		_swimRunSpd = _flRunSpd = _flyRunSpd = _runSpd;
		_swimWalkSpd = _flWalkSpd = _flyWalkSpd = _walkSpd;
		_movementMultiplier = cha.getMovementSpeedMultiplier();
		_attackSpeedMultiplier = cha.getAttackSpeedMultiplier();
	}
	
	public NpcInfo(L2Summon cha, L2Character attacker, int val)
	{
		_cha = cha;
		_idTemplate = cha.getTemplate().idTemplate;
		_isAttackable = cha.isAutoAttackable(attacker);
		_showSpawnAnimation = cha.isShowSummonAnimation();
		_rhand = cha.getWeapon();
		_lhand = 0;
		_chest = cha.getArmor();
		_val = val;
		
		collisionHeight = _cha.getTemplate().collisionHeight;
		
		collisionRadius = _cha.getTemplate().collisionRadius;
		
		_name = _cha.getName();
		
		_title = cha.getOwner() != null ? (cha.getOwner().isOnline() == 0 ? "" : cha.getOwner().getName()) : "";
		
		_x = _cha.getX();
		_y = _cha.getY();
		_z = _cha.getZ();
		_heading = _cha.getHeading();
		_mAtkSpd = _cha.getMAtkSpd();
		_pAtkSpd = _cha.getPAtkSpd();
		_runSpd = cha.getPetSpeed();
		_walkSpd = cha.isMountable() ? 45 : 30;
		_swimRunSpd = _flRunSpd = _flyRunSpd = _runSpd;
		_swimWalkSpd = _flWalkSpd = _flyWalkSpd = _walkSpd;
		_movementMultiplier = cha.getMovementSpeedMultiplier();
		_attackSpeedMultiplier = cha.getAttackSpeedMultiplier();
	}
	
	@Override
	protected final void writeImpl()
	{
		if (_cha instanceof L2Summon)
		{
			if (((L2Summon) _cha).getOwner() != null)
			{
				if (((L2Summon) _cha).getOwner().getAppearance().getInvisible())
				{
					return;
				}
				
				if (((L2Summon) _cha).getOwner().isInOlympiadMode())
				{
					if (getClient().getActiveChar() != null)
					{
						if (!getClient().getActiveChar().isGM() && !getClient().getActiveChar().isInOlympiadMode() && !getClient().getActiveChar().inObserverMode())
						{
							return;
						}
					}
				}
				
				if (((L2Summon) _cha).getOwner().getEventTeam() > 0)
				{
					if (getClient().getActiveChar() != null)
					{
						if (!getClient().getActiveChar().isGM() && !(getClient().getActiveChar().getEventTeam() > 0) && !getClient().getActiveChar().inObserverMode())
						{
							return;
						}
					}
				}
			}
			
		}
		
		writeC(0x16);
		writeD(_cha.getObjectId());
		writeD(_idTemplate + 1000000); // npctype id
		writeD(_isAttackable ? 1 : 0);
		writeD(_x);
		writeD(_y);
		writeD(_z);
		writeD(_heading);
		writeD(0x00);
		writeD(_mAtkSpd);
		writeD(_pAtkSpd);
		writeD(_runSpd);
		writeD(_walkSpd);
		writeD(_swimRunSpd); // swimspeed
		writeD(_swimWalkSpd); // swimspeed
		writeD(_flRunSpd);
		writeD(_flWalkSpd);
		writeD(_flyRunSpd);
		writeD(_flyWalkSpd);
		writeF(_movementMultiplier);
		writeF(_attackSpeedMultiplier);
		writeF(collisionRadius);
		writeF(collisionHeight);
		writeD(_rhand); // right hand weapon
		writeD(_chest);
		writeD(_lhand); // left hand weapon
		writeC(1); // name above char 1=true ... ??
		writeC(_cha.isRunning() ? 1 : 0); // char always running
		writeC(_cha.isInCombat() ? 1 : 0);
		writeC(_cha.isAlikeDead() ? 1 : 0);
		writeC(_showSpawnAnimation ? 2 : _val); // 0=teleported 1=default 2=summoned
		writeS(_name);
		writeS(_title);
		if (_cha instanceof L2Summon)
		{
			writeD(1);
			writeD(((L2Summon) _cha).getOwner().getPvpFlag());
			writeD(((L2Summon) _cha).getOwner().getKarma()); // hmm karma ??
		}
		else
		{
			writeD(0);
			writeD(0);
			writeD(0000); // hmm karma ??
		}
		
		writeD(_cha.getAbnormalEffect()); // C2
		writeD(0000); // C2
		writeD(0000); // C2
		writeD(0000); // C2
		writeD(0000); // C2
		writeC(0000); // C2
		
		if ((_cha instanceof L2Summon) && (((L2Summon) _cha).getOwner().getEventTeam() > 0))
		{
			writeC(((L2Summon) _cha).getOwner().getEventTeam());// Title color 0=client default
		}
		else
		{
			writeC(_cha.getAuraColor());
		}
		
		writeF(collisionRadius);
		writeF(collisionHeight);
		writeD(0x00); // C4
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.network.serverpackets.L2GameServerPacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__22_NPCINFO;
	}
}