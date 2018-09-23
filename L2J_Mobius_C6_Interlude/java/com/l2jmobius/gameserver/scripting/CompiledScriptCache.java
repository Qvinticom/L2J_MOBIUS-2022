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
package com.l2jmobius.gameserver.scripting;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import com.l2jmobius.Config;

/**
 * Cache of Compiled Scripts
 * @author KenM
 */
public class CompiledScriptCache implements Serializable
{
	private static final Logger LOG = Logger.getLogger(CompiledScriptCache.class.getName());
	
	private final Map<String, CompiledScriptHolder> _compiledScriptCache = new HashMap<>();
	private transient boolean _modified = false;
	
	public CompiledScript loadCompiledScript(ScriptEngine engine, File file) throws ScriptException
	{
		final int len = L2ScriptEngineManager.SCRIPT_FOLDER.getPath().length() + 1;
		final String relativeName = file.getPath().substring(len);
		
		final CompiledScriptHolder csh = _compiledScriptCache.get(relativeName);
		if ((csh != null) && csh.matches(file))
		{
			if (Config.DEBUG)
			{
				LOG.info("Reusing cached compiled script: " + file);
			}
			return csh.getCompiledScript();
		}
		
		if (Config.DEBUG)
		{
			LOG.info("Compiling script: " + file);
		}
		
		final Compilable eng = (Compilable) engine;
		FileInputStream fis = null;
		
		BufferedReader buff = null;
		InputStreamReader isr = null;
		CompiledScript cs = null;
		
		try
		{
			fis = new FileInputStream(file);
			isr = new InputStreamReader(fis);
			buff = new BufferedReader(isr);
			
			cs = eng.compile(buff);
			if (cs instanceof Serializable)
			{
				synchronized (_compiledScriptCache)
				{
					_compiledScriptCache.put(relativeName, new CompiledScriptHolder(cs, file));
					_modified = true;
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (buff != null)
			{
				try
				{
					buff.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
			if (isr != null)
			{
				try
				{
					isr.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
			if (fis != null)
			{
				try
				{
					fis.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		
		return cs;
	}
	
	public boolean isModified()
	{
		return _modified;
	}
	
	public void purge()
	{
		synchronized (_compiledScriptCache)
		{
			for (String path : _compiledScriptCache.keySet())
			{
				final File file = new File(L2ScriptEngineManager.SCRIPT_FOLDER, path);
				if (!file.isFile())
				{
					_compiledScriptCache.remove(path);
					_modified = true;
				}
			}
		}
	}
	
	public void save()
	{
		synchronized (_compiledScriptCache)
		{
			File file = null;
			FileOutputStream out = null;
			ObjectOutputStream oos = null;
			
			try
			{
				file = new File(L2ScriptEngineManager.SCRIPT_FOLDER, "CompiledScripts.cache");
				out = new FileOutputStream(file);
				oos = new ObjectOutputStream(out);
				oos.writeObject(this);
				_modified = false;
			}
			catch (FileNotFoundException e)
			{
				e.printStackTrace();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			finally
			{
				if (oos != null)
				{
					try
					{
						oos.close();
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
				
				if (out != null)
				{
					try
					{
						out.close();
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
			}
		}
	}
}
