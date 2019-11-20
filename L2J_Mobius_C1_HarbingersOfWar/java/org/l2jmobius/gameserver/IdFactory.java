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
package org.l2jmobius.gameserver;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Stack;
import java.util.logging.Logger;

public class IdFactory
{
	private static Logger _log = Logger.getLogger(IdFactory.class.getName());
	private int _curOID;
	private Stack<Integer> _oldOIDs;
	private static int FIRST_OID = 268435456;
	private static IdFactory _instance;
	
	@SuppressWarnings("unchecked")
	private IdFactory()
	{
		try
		{
			FileInputStream fis = new FileInputStream("data/idstate.dat");
			BufferedInputStream bis = new BufferedInputStream(fis);
			ObjectInputStream oos = new ObjectInputStream(bis);
			_curOID = (Integer) oos.readObject();
			_oldOIDs = (Stack<Integer>) oos.readObject();
			oos.close();
		}
		catch (Exception e)
		{
			_curOID = FIRST_OID;
			_oldOIDs = new Stack<>();
		}
	}
	
	public static IdFactory getInstance()
	{
		if (_instance == null)
		{
			_instance = new IdFactory();
		}
		return _instance;
	}
	
	public synchronized int getNextId()
	{
		if (_oldOIDs.isEmpty())
		{
			return _curOID++;
		}
		return _oldOIDs.pop();
	}
	
	public void releaseId(int id)
	{
		_oldOIDs.push(id);
	}
	
	public void saveCurrentState()
	{
		try
		{
			FileOutputStream fos = new FileOutputStream("data/idstate.dat");
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(_curOID);
			oos.writeObject(_oldOIDs);
			oos.close();
		}
		catch (IOException e)
		{
			_log.warning("IdState couldnt be saved." + e.getMessage());
		}
	}
}
