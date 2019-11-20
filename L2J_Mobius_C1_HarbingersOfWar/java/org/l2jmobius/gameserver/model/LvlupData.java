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
package org.l2jmobius.gameserver.model;

public class LvlupData
{
	private int _classid;
	private double _defaulthp;
	private double _defaulthpadd;
	private double _defaulthpbonus;
	private double _defaultmp;
	private double _defaultmpadd;
	private double _defaultmpbonus;
	
	public int getClassid()
	{
		return _classid;
	}
	
	public double getDefaulthp()
	{
		return _defaulthp;
	}
	
	public double getDefaulthpadd()
	{
		return _defaulthpadd;
	}
	
	public double getDefaulthpbonus()
	{
		return _defaulthpbonus;
	}
	
	public double getDefaultmp()
	{
		return _defaultmp;
	}
	
	public double getDefaultmpadd()
	{
		return _defaultmpadd;
	}
	
	public double getDefaultmpbonus()
	{
		return _defaultmpbonus;
	}
	
	public void setClassid(int classid)
	{
		_classid = classid;
	}
	
	public void setDefaulthp(double defaulthp)
	{
		_defaulthp = defaulthp;
	}
	
	public void setDefaulthpadd(double defaulthpadd)
	{
		_defaulthpadd = defaulthpadd;
	}
	
	public void setDefaulthpbonus(double defaulthpbonus)
	{
		_defaulthpbonus = defaulthpbonus;
	}
	
	public void setDefaultmp(double defaultmp)
	{
		_defaultmp = defaultmp;
	}
	
	public void setDefaultmpadd(double defaultmpadd)
	{
		_defaultmpadd = defaultmpadd;
	}
	
	public void setDefaultmpbonus(double defaultmpbonus)
	{
		_defaultmpbonus = defaultmpbonus;
	}
}
