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
package com.l2jmobius.gameserver.script.faenor;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import javax.script.ScriptContext;

import org.w3c.dom.Node;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.GameServer;
import com.l2jmobius.gameserver.script.Parser;
import com.l2jmobius.gameserver.script.ParserNotCreatedException;
import com.l2jmobius.gameserver.script.ScriptDocument;
import com.l2jmobius.gameserver.script.ScriptEngine;
import com.l2jmobius.gameserver.script.ScriptPackage;
import com.l2jmobius.gameserver.scripting.L2ScriptEngineManager;

/**
 * @author Luis Arias
 */
public class FaenorScriptEngine extends ScriptEngine
{
	static Logger _log = Logger.getLogger(GameServer.class.getName());
	public final static String PACKAGE_DIRECTORY = "data/faenor/";
	public final static boolean DEBUG = true;
	
	private static FaenorScriptEngine instance;
	
	private LinkedList<ScriptDocument> scripts;
	
	public static FaenorScriptEngine getInstance()
	{
		if (instance == null)
		{
			instance = new FaenorScriptEngine();
		}
		
		return instance;
	}
	
	private FaenorScriptEngine()
	{
		scripts = new LinkedList<>();
		loadPackages();
		parsePackages();
		
	}
	
	public void reloadPackages()
	{
		scripts = new LinkedList<>();
		parsePackages();
	}
	
	private void loadPackages()
	{
		final File packDirectory = new File(Config.DATAPACK_ROOT, PACKAGE_DIRECTORY);// _log.sss(packDirectory.getAbsolutePath());
		
		final FileFilter fileFilter = new FileFilter()
		{
			@Override
			public boolean accept(File file)
			{
				return file.getName().endsWith(".zip");
			}
		};
		
		final File[] files = packDirectory.listFiles(fileFilter);
		if (files == null)
		{
			return;
		}
		ZipFile zipPack;
		
		for (final File file : files)
		{
			try
			{
				zipPack = new ZipFile(file);
			}
			catch (final ZipException e)
			{
				e.printStackTrace();
				continue;
			}
			catch (final IOException e)
			{
				e.printStackTrace();
				continue;
			}
			
			final ScriptPackage module = new ScriptPackage(zipPack);
			
			final List<ScriptDocument> scrpts = module.getScriptFiles();
			for (final ScriptDocument script : scrpts)
			{
				scripts.add(script);
			}
			
			try
			{
				zipPack.close();
			}
			catch (final IOException e)
			{
			}
		}
	}
	
	public void orderScripts()
	{
		if (scripts.size() > 1)
		{
			for (int i = 0; i < scripts.size();)
			{
				if (scripts.get(i).getName().contains("NpcStatData"))
				{
					scripts.addFirst(scripts.remove(i));
				}
				else
				{
					i++;
				}
			}
		}
	}
	
	public void parsePackages()
	{
		final L2ScriptEngineManager sem = L2ScriptEngineManager.getInstance();
		final ScriptContext context = sem.getScriptContext("beanshell");
		
		try
		{
			sem.eval("beanshell", "double log1p(double d) { return Math.log1p(d); }");
			sem.eval("beanshell", "double pow(double d, double p) { return Math.pow(d,p); }");
			
			for (final ScriptDocument script : scripts)
			{
				parseScript(script, context);
			}
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void parseScript(ScriptDocument script, ScriptContext context)
	{
		if (DEBUG)
		{
			_log.fine("Parsing Script: " + script.getName());
		}
		
		final Node node = script.getDocument().getFirstChild();
		final String parserClass = "faenor.Faenor" + node.getNodeName() + "Parser";
		
		Parser parser = null;
		try
		{
			parser = createParser(parserClass);
		}
		catch (final ParserNotCreatedException e)
		{
			_log.warning("ERROR: No parser registered for Script: " + parserClass);
			e.printStackTrace();
		}
		
		if (parser == null)
		{
			_log.warning("Unknown Script Type: " + script.getName());
			return;
		}
		
		try
		{
			parser.parseScript(node, context);
			_log.fine(script.getName() + "Script Sucessfullty Parsed.");
		}
		catch (final Exception e)
		{
			e.printStackTrace();
			_log.warning("Script Parsing Failed.");
		}
	}
	
	@Override
	public String toString()
	{
		if (scripts.isEmpty())
		{
			return "No Packages Loaded.";
		}
		
		String out = "Script Packages currently loaded:\n";
		
		for (final ScriptDocument script : scripts)
		{
			out += script;
		}
		return out;
	}
}