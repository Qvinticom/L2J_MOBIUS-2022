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

import java.util.ArrayList;
import java.util.List;

import org.l2jmobius.gameserver.model.DropData;

public class Npc
{
	private int _npcId;
	private String _type;
	private double _radius;
	private double _height;
	private String _name;
	private String _title;
	private String _sex;
	private int _level;
	private int _attackRange;
	private int _hp;
	private int _mp;
	private int _exp;
	private int _sp;
	private int _patk;
	private int _pdef;
	private int _matk;
	private int _mdef;
	private int _atkspd;
	private boolean _agro;
	private int _matkspd;
	private int _rhand;
	private int _lhand;
	private int _armor;
	private int _walkSpeed;
	private int _runSpeed;
	private final List<DropData> _drops = new ArrayList<>();
	
	public boolean getAgro()
	{
		return _agro;
	}
	
	public int getArmor()
	{
		return _armor;
	}
	
	public int getAtkspd()
	{
		return _atkspd;
	}
	
	public int getAttackRange()
	{
		return _attackRange;
	}
	
	public int getExp()
	{
		return _exp;
	}
	
	public int getHp()
	{
		return _hp;
	}
	
	public int getLevel()
	{
		return _level;
	}
	
	public int getLhand()
	{
		return _lhand;
	}
	
	public int getMatk()
	{
		return _matk;
	}
	
	public int getMatkspd()
	{
		return _matkspd;
	}
	
	public int getMdef()
	{
		return _mdef;
	}
	
	public int getMp()
	{
		return _mp;
	}
	
	public int getPatk()
	{
		return _patk;
	}
	
	public int getPdef()
	{
		return _pdef;
	}
	
	public int getRhand()
	{
		return _rhand;
	}
	
	public int getRunSpeed()
	{
		return _runSpeed;
	}
	
	public String getSex()
	{
		return _sex;
	}
	
	public int getSp()
	{
		return _sp;
	}
	
	public int getWalkSpeed()
	{
		return _walkSpeed;
	}
	
	public void setAgro(boolean agro)
	{
		_agro = agro;
	}
	
	public void setArmor(int armor)
	{
		_armor = armor;
	}
	
	public void setAtkspd(int atkspd)
	{
		_atkspd = atkspd;
	}
	
	public void setAttackRange(int attackrange)
	{
		_attackRange = attackrange;
	}
	
	public void setExp(int exp)
	{
		_exp = exp;
	}
	
	public void setHp(int hp)
	{
		_hp = hp;
	}
	
	public void setLevel(int level)
	{
		_level = level;
	}
	
	public void setLhand(int lhand)
	{
		_lhand = lhand;
	}
	
	public void setMatk(int matk)
	{
		_matk = matk;
	}
	
	public void setMatkspd(int matkspd)
	{
		_matkspd = matkspd;
	}
	
	public void setMdef(int mdef)
	{
		_mdef = mdef;
	}
	
	public void setMp(int mp)
	{
		_mp = mp;
	}
	
	public void setPatk(int patk)
	{
		_patk = patk;
	}
	
	public void setPdef(int pdef)
	{
		_pdef = pdef;
	}
	
	public void setRhand(int rhand)
	{
		_rhand = rhand;
	}
	
	public void setRunSpeed(int runspd)
	{
		_runSpeed = runspd;
	}
	
	public void setSex(String sex)
	{
		_sex = sex;
	}
	
	public void setSp(int sp)
	{
		_sp = sp;
	}
	
	public void setWalkSpeed(int walkspd)
	{
		_walkSpeed = walkspd;
	}
	
	public void setNpcId(int id)
	{
		_npcId = id;
	}
	
	public void setName(String name)
	{
		_name = name;
	}
	
	public void setTitle(String title)
	{
		_title = title;
	}
	
	public void setType(String type)
	{
		_type = type;
	}
	
	public void setRadius(double radius)
	{
		_radius = radius;
	}
	
	public void setHeight(double height)
	{
		_height = height;
	}
	
	public double getHeight()
	{
		return _height;
	}
	
	public String getName()
	{
		return _name;
	}
	
	public String getTitle()
	{
		return _title;
	}
	
	public int getNpcId()
	{
		return _npcId;
	}
	
	public double getRadius()
	{
		return _radius;
	}
	
	public String getType()
	{
		return _type;
	}
	
	public void addDropData(DropData drop)
	{
		_drops.add(drop);
	}
	
	public List<DropData> getDropData()
	{
		return _drops;
	}
}
