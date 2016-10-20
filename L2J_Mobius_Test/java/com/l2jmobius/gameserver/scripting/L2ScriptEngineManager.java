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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import com.l2jmobius.Config;
import com.l2jmobius.commons.javaengine.JavaScriptEngineFactory;

/**
 * Caches script engines and provides functionality for executing and managing scripts.
 * @author KenM
 */
public final class L2ScriptEngineManager
{
	private static final Logger _log = Logger.getLogger(L2ScriptEngineManager.class.getName());
	
	public static final File SCRIPT_FOLDER = new File(Config.DATAPACK_ROOT.getAbsolutePath(), "scripts");
	
	public static L2ScriptEngineManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private final Map<String, ScriptEngine> _extEngines = new HashMap<>();
	private final List<ScriptManager<?>> _scriptManagers = new LinkedList<>();
	
	private File _currentLoadingScript;
	
	/**
	 * If the script engine supports compilation the script is compiled before execution.<BR>
	 */
	private static final boolean ATTEMPT_COMPILATION = true;
	
	protected L2ScriptEngineManager()
	{
		final ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
		final ScriptEngineFactory factory = new JavaScriptEngineFactory();
		scriptEngineManager.registerEngineExtension("java", factory);
		_extEngines.put("java", factory.getScriptEngine());
		_log.info("Script Engine: " + factory.getEngineName() + " " + factory.getEngineVersion() + " - Language: " + factory.getLanguageName() + " - Language Version: " + factory.getLanguageVersion());
	}
	
	private ScriptEngine getEngineByExtension(String ext)
	{
		return _extEngines.get(ext);
	}
	
	public void executeScriptList(File list) throws IOException
	{
		if (Config.ALT_DEV_NO_QUESTS)
		{
			if (!Config.ALT_DEV_NO_HANDLERS)
			{
				try
				{
					executeScript(new File(SCRIPT_FOLDER, "handlers/MasterHandler.java"));
					_log.info("Handlers loaded, all other scripts skipped");
				}
				catch (ScriptException se)
				{
					_log.log(Level.WARNING, "", se);
				}
			}
			return;
		}
		
		if (!list.isFile())
		{
			throw new IllegalArgumentException("Argument must be an file containing a list of scripts to be loaded");
		}
		
		try (FileInputStream fis = new FileInputStream(list);
			InputStreamReader isr = new InputStreamReader(fis);
			LineNumberReader lnr = new LineNumberReader(isr))
		{
			String line;
			while ((line = lnr.readLine()) != null)
			{
				if (Config.ALT_DEV_NO_HANDLERS && line.contains("MasterHandler.java"))
				{
					continue;
				}
				
				final String[] parts = line.trim().split("#");
				
				if ((parts.length > 0) && !parts[0].isEmpty() && (parts[0].charAt(0) != '#'))
				{
					line = parts[0];
					
					if (line.endsWith("/**"))
					{
						line = line.substring(0, line.length() - 3);
					}
					else if (line.endsWith("/*"))
					{
						line = line.substring(0, line.length() - 2);
					}
					
					final File file = new File(SCRIPT_FOLDER, line);
					
					if (file.isDirectory() && parts[0].endsWith("/**"))
					{
						executeAllScriptsInDirectory(file, true, 32);
					}
					else if (file.isDirectory() && parts[0].endsWith("/*"))
					{
						executeAllScriptsInDirectory(file);
					}
					else if (file.isFile())
					{
						try
						{
							executeScript(file);
						}
						catch (ScriptException e)
						{
							reportScriptFileError(file, e);
						}
					}
					else
					{
						_log.warning("Failed loading: (" + file.getCanonicalPath() + ") @ " + list.getName() + ":" + lnr.getLineNumber() + " - Reason: doesnt exists or is not a file.");
					}
				}
			}
		}
	}
	
	public void executeAllScriptsInDirectory(File dir)
	{
		executeAllScriptsInDirectory(dir, false, 0);
	}
	
	public void executeAllScriptsInDirectory(File dir, boolean recurseDown, int maxDepth)
	{
		executeAllScriptsInDirectory(dir, recurseDown, maxDepth, 0);
	}
	
	private void executeAllScriptsInDirectory(File dir, boolean recurseDown, int maxDepth, int currentDepth)
	{
		if (!dir.isDirectory())
		{
			throw new IllegalArgumentException("The argument directory either doesnt exists or is not an directory.");
		}
		
		final File[] files = dir.listFiles();
		if (files == null)
		{
			return;
		}
		
		for (File file : files)
		{
			if (file.isDirectory() && recurseDown && (maxDepth > currentDepth))
			{
				executeAllScriptsInDirectory(file, recurseDown, maxDepth, currentDepth + 1);
			}
			else if (file.isFile())
			{
				try
				{
					final String name = file.getName();
					final int lastIndex = name.lastIndexOf('.');
					String extension;
					if (lastIndex != -1)
					{
						extension = name.substring(lastIndex + 1);
						final ScriptEngine engine = getEngineByExtension(extension);
						if (engine != null)
						{
							executeScript(engine, file);
						}
					}
				}
				catch (ScriptException e)
				{
					reportScriptFileError(file, e);
				}
			}
		}
	}
	
	public void executeScript(File file) throws ScriptException
	{
		final String name = file.getName();
		final int lastIndex = name.lastIndexOf('.');
		String extension;
		if (lastIndex == -1)
		{
			throw new ScriptException("Script file (" + name + ") doesnt has an extension that identifies the ScriptEngine to be used.");
		}
		extension = name.substring(lastIndex + 1);
		final ScriptEngine engine = getEngineByExtension(extension);
		if (engine == null)
		{
			throw new ScriptException("No engine registered for extension (" + extension + ")");
		}
		executeScript(engine, file);
	}
	
	public void executeScript(ScriptEngine engine, File file) throws ScriptException
	{
		{
			final String name = file.getAbsolutePath() + ".error.log";
			final File errorLog = new File(name);
			if (errorLog.isFile())
			{
				errorLog.delete();
			}
		}
		
		final String relativeName = file.getAbsolutePath().substring(SCRIPT_FOLDER.getAbsolutePath().length() + 1).replace('\\', '/');
		try (FileInputStream fis = new FileInputStream(file);
			InputStreamReader isr = new InputStreamReader(fis);
			BufferedReader reader = new BufferedReader(isr))
		{
			final ScriptContext context = new SimpleScriptContext();
			context.setAttribute("mainClass", getClassForFile(file).replace('/', '.').replace('\\', '.'), ScriptContext.ENGINE_SCOPE);
			context.setAttribute(ScriptEngine.FILENAME, relativeName, ScriptContext.ENGINE_SCOPE);
			context.setAttribute("classpath", SCRIPT_FOLDER.getAbsolutePath(), ScriptContext.ENGINE_SCOPE);
			context.setAttribute("sourcepath", SCRIPT_FOLDER.getAbsolutePath(), ScriptContext.ENGINE_SCOPE);
			setCurrentLoadingScript(file);
			try
			{
				engine.eval(reader, context);
			}
			finally
			{
				setCurrentLoadingScript(null);
				engine.getContext().removeAttribute(ScriptEngine.FILENAME, ScriptContext.ENGINE_SCOPE);
				engine.getContext().removeAttribute("mainClass", ScriptContext.ENGINE_SCOPE);
			}
		}
		catch (IOException e)
		{
			_log.log(Level.WARNING, "Error executing script!", e);
		}
	}
	
	public static String getClassForFile(File script)
	{
		final String path = script.getAbsolutePath();
		final String scpPath = SCRIPT_FOLDER.getAbsolutePath();
		return path.startsWith(scpPath) ? path.substring(scpPath.length() + 1, path.lastIndexOf('.')) : null;
	}
	
	public ScriptContext getScriptContext(ScriptEngine engine)
	{
		return engine.getContext();
	}
	
	public Object eval(ScriptEngine engine, String script, ScriptContext context) throws ScriptException
	{
		if (!(engine instanceof Compilable) || !ATTEMPT_COMPILATION)
		{
			return context != null ? engine.eval(script, context) : engine.eval(script);
		}
		final Compilable eng = (Compilable) engine;
		final CompiledScript cs = eng.compile(script);
		return context != null ? cs.eval(context) : cs.eval();
	}
	
	public Object eval(ScriptEngine engine, String script) throws ScriptException
	{
		return eval(engine, script, null);
	}
	
	public void reportScriptFileError(File script, ScriptException e)
	{
		final String dir = script.getParent();
		if (dir != null)
		{
			final File file = new File(dir + "/" + script.getName() + ".error.log");
			try (FileOutputStream fos = new FileOutputStream(file))
			{
				final String errorHeader = "Error on: " + file.getCanonicalPath() + Config.EOL + "Line: " + e.getLineNumber() + " - Column: " + e.getColumnNumber() + Config.EOL + Config.EOL;
				fos.write(errorHeader.getBytes());
				fos.write(e.getMessage().getBytes());
				_log.warning("Failed executing script: " + script.getAbsolutePath() + ". See " + file.getName() + " for details.");
			}
			catch (IOException ioe)
			{
				_log.log(Level.WARNING, "Failed executing script: " + script.getAbsolutePath() + Config.EOL + e.getMessage() + "Additionally failed when trying to write an error report on script directory. Reason: " + ioe.getMessage(), ioe);
			}
		}
		else
		{
			_log.log(Level.WARNING, "Failed executing script: " + script.getAbsolutePath() + Config.EOL + e.getMessage() + "Additionally failed when trying to write an error report on script directory.", e);
		}
	}
	
	public void registerScriptManager(ScriptManager<?> manager)
	{
		_scriptManagers.add(manager);
	}
	
	public void removeScriptManager(ScriptManager<?> manager)
	{
		_scriptManagers.remove(manager);
	}
	
	public List<ScriptManager<?>> getScriptManagers()
	{
		return _scriptManagers;
	}
	
	/**
	 * @param currentLoadingScript The currentLoadingScript to set.
	 */
	protected void setCurrentLoadingScript(File currentLoadingScript)
	{
		_currentLoadingScript = currentLoadingScript;
	}
	
	/**
	 * @return Returns the currentLoadingScript.
	 */
	public File getCurrentLoadingScript()
	{
		return _currentLoadingScript;
	}
	
	private static class SingletonHolder
	{
		protected static final L2ScriptEngineManager _instance = new L2ScriptEngineManager();
	}
}
