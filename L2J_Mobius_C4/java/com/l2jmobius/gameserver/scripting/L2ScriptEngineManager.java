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
import com.l2jserver.script.jython.JythonScriptEngine;

import javolution.util.FastMap;

/**
 * Caches script engines and provides functionality for executing and managing scripts.<BR>
 * @author KenM
 */
public final class L2ScriptEngineManager
{
	private static final Logger _log;
	
	private static final L2ScriptEngineManager INSTANCE;
	
	public final static File SCRIPT_FOLDER;
	
	static
	{
		_log = Logger.getLogger(L2ScriptEngineManager.class.getName());
		SCRIPT_FOLDER = new File(Config.DATAPACK_ROOT.getAbsolutePath(), "data/scripts");
		INSTANCE = new L2ScriptEngineManager();
	}
	
	public static L2ScriptEngineManager getInstance()
	{
		return INSTANCE;
	}
	
	private final Map<String, ScriptEngine> _nameEngines = new FastMap<>();
	private final Map<String, ScriptEngine> _extEngines = new FastMap<>();
	private final List<ScriptManager<?>> _scriptManagers = new LinkedList<>();
	private File _currentLoadingScript;
	
	// Configs
	// TODO move to config file
	/**
	 * Informs(logs) the scripts being loaded.<BR>
	 * Apply only when executing script from files.<BR>
	 */
	private final boolean VERBOSE_LOADING = false;
	
	/**
	 * If the script engine supports compilation the script is compiled before execution.<BR>
	 */
	private final boolean ATTEMPT_COMPILATION = true;
	
	/**
	 * Clean an previous error log(if such exists) for the script being loaded before trying to load.<BR>
	 * Apply only when executing script from files.<BR>
	 */
	private final boolean PURGE_ERROR_LOG = true;
	
	private L2ScriptEngineManager()
	{
		final ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
		final List<ScriptEngineFactory> factories = scriptEngineManager.getEngineFactories();
		_log.severe("Initializing Script Engine Manager");
		
		for (final ScriptEngineFactory factory : factories)
		{
			try
			{
				final ScriptEngine engine = factory.getScriptEngine();
				boolean reg = false;
				for (final String name : factory.getNames())
				{
					final ScriptEngine existentEngine = _nameEngines.get(name);
					if (existentEngine != null)
					{
						final double engineVer = Double.parseDouble(factory.getEngineVersion());
						final double existentEngVer = Double.parseDouble(existentEngine.getFactory().getEngineVersion());
						
						if (engineVer <= existentEngVer)
						{
							continue;
						}
					}
					
					reg = true;
					_nameEngines.put(name, engine);
				}
				
				if (reg)
				{
					_log.info("Script Engine: " + factory.getEngineName() + " " + factory.getEngineVersion() + " - Language: " + factory.getLanguageName() + " - Language Version: " + factory.getLanguageVersion());
				}
				
				for (final String ext : factory.getExtensions())
				{
					if (!ext.equals("java") || factory.getLanguageName().equals("java"))
					{
						_extEngines.put(ext, engine);
					}
				}
			}
			catch (final Exception e)
			{
				_log.warning("Failed initializing factory. ");
				e.printStackTrace();
			}
		}
		
		preConfigure();
	}
	
	public void preConfigure()
	{
		// Jython sys.path
		final String dataPackDirForwardSlashes = SCRIPT_FOLDER.getPath().replaceAll("\\\\", "/");
		final String configScript = "import sys;sys.path.insert(0, '" + dataPackDirForwardSlashes + "');";
		
		try
		{
			eval("jython", configScript);
		}
		catch (final ScriptException e)
		{
			_log.severe("Failed preconfiguring jython: " + e.getMessage());
		}
	}
	
	private ScriptEngine getEngineByName(String name)
	{
		return _nameEngines.get(name);
	}
	
	private ScriptEngine getEngineByExtension(String ext)
	{
		return _extEngines.get(ext);
	}
	
	public void executeScriptList(File list)
	{
		if (Config.ALT_DEV_NO_QUESTS)
		{
			return;
		}
		
		if (list.isFile())
		{
			try (FileInputStream fis = new FileInputStream(list);
				InputStreamReader isr = new InputStreamReader(fis);
				LineNumberReader lnr = new LineNumberReader(isr))
			{
				String line;
				File file;
				
				while ((line = lnr.readLine()) != null)
				{
					final String[] parts = line.trim().split("#");
					
					if ((parts.length > 0) && !parts[0].startsWith("#") && (parts[0].length() > 0))
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
						
						file = new File(SCRIPT_FOLDER, line);
						
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
							catch (final ScriptException e)
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
			catch (final IOException e)
			{
				_log.log(Level.WARNING, "Error executing script!", e);
			}
		}
		else
		{
			throw new IllegalArgumentException("Argument must be an file containing a list of scripts to be loaded");
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
		if (dir.isDirectory())
		{
			for (final File file : dir.listFiles())
			{
				if (file.isDirectory() && recurseDown && (maxDepth > currentDepth))
				{
					if (VERBOSE_LOADING)
					{
						_log.info("Entering folder: " + file.getName());
					}
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
					catch (final ScriptException e)
					{
						reportScriptFileError(file, e);
						// e.printStackTrace();
					}
				}
			}
		}
		else
		{
			throw new IllegalArgumentException("The argument directory either doesnt exists or is not an directory.");
		}
	}
	
	public void executeScript(File file) throws ScriptException
	{
		final String name = file.getName();
		final int lastIndex = name.lastIndexOf('.');
		String extension;
		if (lastIndex != -1)
		{
			extension = name.substring(lastIndex + 1);
		}
		else
		{
			throw new ScriptException("Script file (" + name + ") doesnt has an extension that identifies the ScriptEngine to be used.");
		}
		
		final ScriptEngine engine = getEngineByExtension(extension);
		if (engine == null)
		{
			throw new ScriptException("No engine registered for extension (" + extension + ")");
		}
		executeScript(engine, file);
	}
	
	public void executeScript(String engineName, File file) throws ScriptException
	{
		final ScriptEngine engine = getEngineByName(engineName);
		if (engine == null)
		{
			throw new ScriptException("No engine registered with name (" + engineName + ")");
		}
		executeScript(engine, file);
	}
	
	public void executeScript(ScriptEngine engine, File file) throws ScriptException
	{
		if (VERBOSE_LOADING)
		{
			_log.info("Loading Script: " + file.getAbsolutePath());
		}
		
		if (PURGE_ERROR_LOG)
		{
			final String name = file.getAbsolutePath() + ".error.log";
			final File errorLog = new File(name);
			if (errorLog.isFile())
			{
				errorLog.delete();
			}
		}
		
		try (FileInputStream fis = new FileInputStream(file);
			InputStreamReader isr = new InputStreamReader(fis);
			BufferedReader reader = new BufferedReader(isr))
		{
			if ((engine instanceof Compilable) && ATTEMPT_COMPILATION)
			{
				final ScriptContext context = new SimpleScriptContext();
				context.setAttribute("mainClass", getClassForFile(file).replace('/', '.').replace('\\', '.'), ScriptContext.ENGINE_SCOPE);
				context.setAttribute(ScriptEngine.FILENAME, file.getName(), ScriptContext.ENGINE_SCOPE);
				context.setAttribute("classpath", SCRIPT_FOLDER.getAbsolutePath(), ScriptContext.ENGINE_SCOPE);
				context.setAttribute("sourcepath", SCRIPT_FOLDER.getAbsolutePath(), ScriptContext.ENGINE_SCOPE);
				context.setAttribute(JythonScriptEngine.JYTHON_ENGINE_INSTANCE, engine, ScriptContext.ENGINE_SCOPE);
				
				setCurrentLoadingScript(file);
				final ScriptContext ctx = engine.getContext();
				try
				{
					engine.setContext(context);
					final Compilable eng = (Compilable) engine;
					final CompiledScript cs = eng.compile(reader);
					cs.eval(context);
				}
				finally
				{
					engine.setContext(ctx);
					setCurrentLoadingScript(null);
					context.removeAttribute(ScriptEngine.FILENAME, ScriptContext.ENGINE_SCOPE);
					context.removeAttribute("mainClass", ScriptContext.ENGINE_SCOPE);
				}
			}
			else
			{
				final ScriptContext context = new SimpleScriptContext();
				context.setAttribute("mainClass", getClassForFile(file).replace('/', '.').replace('\\', '.'), ScriptContext.ENGINE_SCOPE);
				context.setAttribute(ScriptEngine.FILENAME, file.getName(), ScriptContext.ENGINE_SCOPE);
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
		}
		catch (final IOException e)
		{
			_log.log(Level.WARNING, "Error executing script!", e);
		}
	}
	
	public static String getClassForFile(File script)
	{
		final String path = script.getAbsolutePath();
		final String scpPath = SCRIPT_FOLDER.getAbsolutePath();
		if (path.startsWith(scpPath))
		{
			final int idx = path.lastIndexOf('.');
			return path.substring(scpPath.length() + 1, idx);
		}
		return null;
	}
	
	public ScriptContext getScriptContext(ScriptEngine engine)
	{
		return engine.getContext();
	}
	
	public ScriptContext getScriptContext(String engineName)
	{
		final ScriptEngine engine = getEngineByName(engineName);
		if (engine == null)
		{
			throw new IllegalStateException("No engine registered with name (" + engineName + ")");
		}
		return getScriptContext(engine);
	}
	
	public Object eval(ScriptEngine engine, String script, ScriptContext context) throws ScriptException
	{
		if ((engine instanceof Compilable) && ATTEMPT_COMPILATION)
		{
			final Compilable eng = (Compilable) engine;
			final CompiledScript cs = eng.compile(script);
			return context != null ? cs.eval(context) : cs.eval();
		}
		return context != null ? engine.eval(script, context) : engine.eval(script);
	}
	
	public Object eval(String engineName, String script) throws ScriptException
	{
		return eval(engineName, script, null);
	}
	
	public Object eval(String engineName, String script, ScriptContext context) throws ScriptException
	{
		final ScriptEngine engine = getEngineByName(engineName);
		if (engine == null)
		{
			throw new ScriptException("No engine registered with name (" + engineName + ")");
		}
		return eval(engine, script, context);
	}
	
	public Object eval(ScriptEngine engine, String script) throws ScriptException
	{
		return eval(engine, script, null);
	}
	
	public void reportScriptFileError(File script, ScriptException e)
	{
		final String dir = script.getParent();
		final String name = script.getName() + ".error.log";
		if (dir != null)
		{
			final File file = new File(dir + "/" + name);
			try (FileOutputStream fos = new FileOutputStream(file))
			{
				final String errorHeader = "Error on: " + file.getCanonicalPath() + "\r\nLine: " + e.getLineNumber() + " - Column: " + e.getColumnNumber() + "\r\n\r\n";
				fos.write(errorHeader.getBytes());
				fos.write(e.getMessage().getBytes());
				_log.warning("Failed executing script: " + script.getAbsolutePath() + ". See " + file.getName() + " for details.");
			}
			catch (final IOException ioe)
			{
				_log.warning("Failed executing script: " + script.getAbsolutePath() + "\r\n" + e.getMessage() + "Additionally failed when trying to write an error report on script directory. Reason: " + ioe.getMessage());
				ioe.printStackTrace();
			}
		}
		else
		{
			_log.warning("Failed executing script: " + script.getAbsolutePath() + "\r\n" + e.getMessage() + "Additionally failed when trying to write an error report on script directory.");
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
	protected File getCurrentLoadingScript()
	{
		return _currentLoadingScript;
	}
}