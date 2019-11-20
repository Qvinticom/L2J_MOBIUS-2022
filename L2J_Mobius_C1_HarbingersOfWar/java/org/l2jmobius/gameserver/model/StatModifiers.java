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

public class StatModifiers
{
	private int _classid;
	private int _modstr;
	private int _modcon;
	private int _moddex;
	private int _modint;
	private int _modmen;
	private int _modwit;
	
	public int getClassid()
	{
		return _classid;
	}
	
	public int getModcon()
	{
		return _modcon;
	}
	
	public int getModdex()
	{
		return _moddex;
	}
	
	public int getModint()
	{
		return _modint;
	}
	
	public int getModmen()
	{
		return _modmen;
	}
	
	public int getModstr()
	{
		return _modstr;
	}
	
	public int getModwit()
	{
		return _modwit;
	}
	
	public void setClassid(int classid)
	{
		_classid = classid;
	}
	
	public void setModcon(int modcon)
	{
		_modcon = modcon;
	}
	
	public void setModdex(int moddex)
	{
		_moddex = moddex;
	}
	
	public void setModint(int modint)
	{
		_modint = modint;
	}
	
	public void setModmen(int modmen)
	{
		_modmen = modmen;
	}
	
	public void setModstr(int modstr)
	{
		_modstr = modstr;
	}
	
	public void setModwit(int modwit)
	{
		_modwit = modwit;
	}
}
