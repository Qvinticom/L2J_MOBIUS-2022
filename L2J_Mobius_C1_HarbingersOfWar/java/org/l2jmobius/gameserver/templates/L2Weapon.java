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
package org.l2jmobius.gameserver.templates;

public class L2Weapon extends L2Item
{
	public static final int WEAPON_TYPE_NONE = 1;
	public static final int WEAPON_TYPE_SWORD = 2;
	public static final int WEAPON_TYPE_BLUNT = 3;
	public static final int WEAPON_TYPE_DAGGER = 4;
	public static final int WEAPON_TYPE_BOW = 5;
	public static final int WEAPON_TYPE_POLE = 6;
	public static final int WEAPON_TYPE_ETC = 7;
	public static final int WEAPON_TYPE_FIST = 8;
	public static final int WEAPON_TYPE_DUAL = 9;
	public static final int WEAPON_TYPE_DUALFIST = 10;
	private int _soulShotCount;
	private int _spiritShotCount;
	private int _pDam;
	private int _rndDam;
	private int _weaponType;
	private int _critical;
	private double _hitModifier;
	private int _avoidModifier;
	private int _shieldDef;
	private int _shieldDefRate;
	private int _atkSpeed;
	private int _mpConsume;
	private int _mDam;
	
	public int getSoulShotCount()
	{
		return _soulShotCount;
	}
	
	public void setSoulShotCount(int soulShotCount)
	{
		_soulShotCount = soulShotCount;
	}
	
	public int getSpiritShotCount()
	{
		return _spiritShotCount;
	}
	
	public void setSpiritShotCount(int spiritShotCount)
	{
		_spiritShotCount = spiritShotCount;
	}
	
	public int getPDamage()
	{
		return _pDam;
	}
	
	public void setPDamage(int dam)
	{
		_pDam = dam;
	}
	
	public int getRandomDamage()
	{
		return _rndDam;
	}
	
	public void setRandomDamage(int rndDam)
	{
		_rndDam = rndDam;
	}
	
	public int getWeaponType()
	{
		return _weaponType;
	}
	
	public void setWeaponType(int weaponType)
	{
		_weaponType = weaponType;
	}
	
	public int getAttackSpeed()
	{
		return _atkSpeed;
	}
	
	public void setAttackSpeed(int atkSpeed)
	{
		_atkSpeed = atkSpeed;
	}
	
	public int getAvoidModifier()
	{
		return _avoidModifier;
	}
	
	public void setAvoidModifier(int avoidModifier)
	{
		_avoidModifier = avoidModifier;
	}
	
	public int getCritical()
	{
		return _critical;
	}
	
	public void setCritical(int critical)
	{
		_critical = critical;
	}
	
	public double getHitModifier()
	{
		return _hitModifier;
	}
	
	public void setHitModifier(double hitModifier)
	{
		_hitModifier = hitModifier;
	}
	
	public int getMDamage()
	{
		return _mDam;
	}
	
	public void setMDamage(int dam)
	{
		_mDam = dam;
	}
	
	public int getMpConsume()
	{
		return _mpConsume;
	}
	
	public void setMpConsume(int mpConsume)
	{
		_mpConsume = mpConsume;
	}
	
	public int getShieldDef()
	{
		return _shieldDef;
	}
	
	public void setShieldDef(int shieldDef)
	{
		_shieldDef = shieldDef;
	}
	
	public int getShieldDefRate()
	{
		return _shieldDefRate;
	}
	
	public void setShieldDefRate(int shieldDefRate)
	{
		_shieldDefRate = shieldDefRate;
	}
}
