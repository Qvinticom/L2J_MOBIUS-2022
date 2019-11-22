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
import java.util.Collection;
import java.util.List;

public class CharTemplate
{
	private int _classId;
	private String _className;
	private int _str;
	private int _con;
	private int _dex;
	private int _int;
	private int _wit;
	private int _men;
	private int _hp;
	private int _mp;
	private int _patk;
	private int _pdef;
	private int _pspd;
	private int _matk;
	private int _mdef;
	private int _mspd;
	private int _acc;
	private int _crit;
	private int _evas;
	private int _moveSpd;
	private int _load;
	private int _x;
	private int _y;
	private int _z;
	private double _mUnk1;
	private double _mUnk2;
	private double _mColR;
	private double _mColH;
	private double _fUnk1;
	private double _fUnk2;
	private double _fColR;
	private double _fColH;
	private int _raceId;
	private final List<Integer> _items = new ArrayList<>();
	private int _canCraft;
	
	public int getAcc()
	{
		return _acc;
	}
	
	public void setAcc(int acc)
	{
		_acc = acc;
	}
	
	public int getClassId()
	{
		return _classId;
	}
	
	public void setClassId(int classId)
	{
		_classId = classId;
	}
	
	public String getClassName()
	{
		return _className;
	}
	
	public void setClassName(String className)
	{
		_className = className;
	}
	
	public int getCon()
	{
		return _con;
	}
	
	public void setCon(int con)
	{
		_con = con;
	}
	
	public int getCrit()
	{
		return _crit;
	}
	
	public void setCrit(int crit)
	{
		_crit = crit;
	}
	
	public int getDex()
	{
		return _dex;
	}
	
	public void setDex(int dex)
	{
		_dex = dex;
	}
	
	public int getEvas()
	{
		return _evas;
	}
	
	public void setEvas(int evas)
	{
		_evas = evas;
	}
	
	public int getHp()
	{
		return _hp;
	}
	
	public void setHp(int hp)
	{
		_hp = hp;
	}
	
	public int getInt()
	{
		return _int;
	}
	
	public void setInt(int int1)
	{
		_int = int1;
	}
	
	public int getLoad()
	{
		return _load;
	}
	
	public void setLoad(int load)
	{
		_load = load;
	}
	
	public int getMatk()
	{
		return _matk;
	}
	
	public void setMatk(int matk)
	{
		_matk = matk;
	}
	
	public int getMdef()
	{
		return _mdef;
	}
	
	public void setMdef(int mdef)
	{
		_mdef = mdef;
	}
	
	public int getMen()
	{
		return _men;
	}
	
	public void setMen(int men)
	{
		_men = men;
	}
	
	public int getMoveSpd()
	{
		return _moveSpd;
	}
	
	public void setMoveSpd(int moveSpd)
	{
		_moveSpd = moveSpd;
	}
	
	public int getMp()
	{
		return _mp;
	}
	
	public void setMp(int mp)
	{
		_mp = mp;
	}
	
	public int getMspd()
	{
		return _mspd;
	}
	
	public void setMspd(int mspd)
	{
		_mspd = mspd;
	}
	
	public int getPatk()
	{
		return _patk;
	}
	
	public void setPatk(int patk)
	{
		_patk = patk;
	}
	
	public int getPdef()
	{
		return _pdef;
	}
	
	public void setPdef(int pdef)
	{
		_pdef = pdef;
	}
	
	public int getPspd()
	{
		return _pspd;
	}
	
	public void setPspd(int pspd)
	{
		_pspd = pspd;
	}
	
	public int getStr()
	{
		return _str;
	}
	
	public void setStr(int str)
	{
		_str = str;
	}
	
	public int getWit()
	{
		return _wit;
	}
	
	public void setWit(int wit)
	{
		_wit = wit;
	}
	
	public double getFColH()
	{
		return _fColH;
	}
	
	public void setFColH(double colH)
	{
		_fColH = colH;
	}
	
	public double getFColR()
	{
		return _fColR;
	}
	
	public void setFColR(double colR)
	{
		_fColR = colR;
	}
	
	public double getFUnk1()
	{
		return _fUnk1;
	}
	
	public void setFUnk1(double unk1)
	{
		_fUnk1 = unk1;
	}
	
	public double getFUnk2()
	{
		return _fUnk2;
	}
	
	public void setFUnk2(double unk2)
	{
		_fUnk2 = unk2;
	}
	
	public double getMColH()
	{
		return _mColH;
	}
	
	public void setMColH(double colH)
	{
		_mColH = colH;
	}
	
	public double getMColR()
	{
		return _mColR;
	}
	
	public void setMColR(double colR)
	{
		_mColR = colR;
	}
	
	public double getMUnk1()
	{
		return _mUnk1;
	}
	
	public void setMUnk1(double unk1)
	{
		_mUnk1 = unk1;
	}
	
	public double getMUnk2()
	{
		return _mUnk2;
	}
	
	public void setMUnk2(double unk2)
	{
		_mUnk2 = unk2;
	}
	
	public int getX()
	{
		return _x;
	}
	
	public void setX(int x)
	{
		_x = x;
	}
	
	public int getY()
	{
		return _y;
	}
	
	public void setY(int y)
	{
		_y = y;
	}
	
	public int getZ()
	{
		return _z;
	}
	
	public void setZ(int z)
	{
		_z = z;
	}
	
	public void setRaceId(int raceId)
	{
		_raceId = raceId;
	}
	
	public int getRaceId()
	{
		return _raceId;
	}
	
	public void addItem(int itemId)
	{
		_items.add(itemId);
	}
	
	public Collection<Integer> getItems()
	{
		return _items;
	}
	
	public void setCanCraft(int b)
	{
		_canCraft = b;
	}
	
	public int getCanCraft()
	{
		return _canCraft;
	}
}
