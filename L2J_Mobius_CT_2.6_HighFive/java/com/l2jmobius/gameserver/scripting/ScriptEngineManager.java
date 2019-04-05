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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;

import com.l2jmobius.Config;
import com.l2jmobius.commons.util.IXmlReader;
import com.l2jmobius.gameserver.scripting.java.JavaScriptingEngine;

/**
 * Caches script engines and provides functionality for executing and managing scripts.
 * @author KenM, HorridoJoho
 */
public final class ScriptEngineManager implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(ScriptEngineManager.class.getName());
	public static final Path SCRIPT_FOLDER = Paths.get(Config.DATAPACK_ROOT.getAbsolutePath(), "data", "scripts");
	public static final Path MASTER_HANDLER_FILE = Paths.get(SCRIPT_FOLDER.toString(), "handlers", "MasterHandler.java");
	public static final Path EFFECT_MASTER_HANDLER_FILE = Paths.get(SCRIPT_FOLDER.toString(), "handlers", "EffectMasterHandler.java");
	
	private IExecutionContext _javaExecutionContext = null;
	static final List<String> _exclusions = new ArrayList<>();
	
	protected ScriptEngineManager()
	{
		final Properties props = loadProperties();
		
		// Default java engine implementation
		registerEngine(new JavaScriptingEngine(), props);
		
		// Load external script engines
		ServiceLoader.load(IScriptingEngine.class).forEach(engine -> registerEngine(engine, props));
		
		// Load Scripts.xml
		load();
	}
	
	@Override
	public void load()
	{
		_exclusions.clear();
		parseDatapackFile("config/Scripts.xml");
		LOGGER.info("Loaded " + _exclusions.size() + " files to exclude.");
	}
	
	@Override
	public void parseDocument(Document doc, File f)
	{
		try
		{
			final Map<String, List<String>> excludePaths = new HashMap<>();
			forEach(doc, "list", listNode -> forEach(listNode, "exclude", excludeNode ->
			{
				final String excludeFile = parseString(excludeNode.getAttributes(), "file");
				excludePaths.putIfAbsent(excludeFile, new ArrayList<>());
				
				forEach(excludeNode, "include", includeNode -> excludePaths.get(excludeFile).add(parseString(includeNode.getAttributes(), "file")));
			}));
			
			final int nameCount = SCRIPT_FOLDER.getNameCount();
			Files.walkFileTree(SCRIPT_FOLDER, new SimpleFileVisitor<Path>()
			{
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
				{
					final String fileName = file.getFileName().toString();
					if (fileName.endsWith(".java"))
					{
						final Iterator<Path> relativePath = file.subpath(nameCount, file.getNameCount()).iterator();
						while (relativePath.hasNext())
						{
							final String nextPart = relativePath.next().toString();
							if (excludePaths.containsKey(nextPart))
							{
								boolean excludeScript = true;
								
								final List<String> includePath = excludePaths.get(nextPart);
								if (includePath != null)
								{
									while (relativePath.hasNext())
									{
										if (includePath.contains(relativePath.next().toString()))
										{
											excludeScript = false;
											break;
										}
									}
								}
								if (excludeScript)
								{
									_exclusions.add(fileName);
									break;
								}
							}
						}
					}
					
					return super.visitFile(file, attrs);
				}
			});
		}
		catch (final IOException e)
		{
			LOGGER.log(Level.WARNING, "Couldn't load script exclusions.", e);
		}
	}
	
	private Properties loadProperties()
	{
		Properties props = null;
		try (FileInputStream fis = new FileInputStream("config/ScriptEngine.ini"))
		{
			props = new Properties();
			props.load(fis);
		}
		catch (Exception e)
		{
			props = null;
			LOGGER.warning("Couldn't load ScriptEngine.ini: " + e.getMessage());
		}
		return props;
	}
	
	private void registerEngine(IScriptingEngine engine, Properties props)
	{
		maybeSetProperties("language." + engine.getLanguageName() + ".", props, engine);
		_javaExecutionContext = engine.createExecutionContext();
		LOGGER.info("ScriptEngine: " + engine.getEngineName() + " " + engine.getEngineVersion() + " (" + engine.getLanguageName() + " " + engine.getLanguageVersion() + ")");
	}
	
	private void maybeSetProperties(String propPrefix, Properties props, IScriptingEngine engine)
	{
		if (props == null)
		{
			return;
		}
		
		for (Entry<Object, Object> prop : props.entrySet())
		{
			String key = (String) prop.getKey();
			String value = (String) prop.getValue();
			
			if (key.startsWith(propPrefix))
			{
				key = key.substring(propPrefix.length());
				if (value.startsWith("%") && value.endsWith("%"))
				{
					value = System.getProperty(value.substring(1, value.length() - 1));
				}
				
				engine.setProperty(key, value);
			}
		}
	}
	
	public void executeScriptList() throws Exception
	{
		if (Config.ALT_DEV_NO_QUESTS)
		{
			return;
		}
		
		final List<Path> files = new ArrayList<>();
		processDirectory(SCRIPT_FOLDER.toFile(), files);
		
		final Map<Path, Throwable> invokationErrors = _javaExecutionContext.executeScripts(files);
		for (Entry<Path, Throwable> entry : invokationErrors.entrySet())
				{
			LOGGER.log(Level.WARNING, "ScriptEngine: " + entry.getKey() + " failed execution!", entry.getValue());
		}
	}
	
	private void processDirectory(File dir, List<Path> files)
	{
		for (File file : dir.listFiles())
		{
			if (file.isFile())
			{
				final String fileName = file.getName();
				if (fileName.endsWith(".java") && !_exclusions.contains(fileName))
				{
					files.add(file.toPath().toAbsolutePath());
				}
			}
			else if (file.isDirectory())
			{
				processDirectory(file, files);
			}
		}
	}
	
	public void executeScript(Path sourceFile) throws Exception
	{
		Objects.requireNonNull(sourceFile);
		
		if (!sourceFile.isAbsolute())
		{
			sourceFile = SCRIPT_FOLDER.resolve(sourceFile);
		}
		
		sourceFile = sourceFile.toAbsolutePath();
		Objects.requireNonNull(sourceFile, "ScriptFile: " + sourceFile + " does not have an extension to determine the script engine!");
		
		final Entry<Path, Throwable> error = _javaExecutionContext.executeScript(sourceFile);
		if (error != null)
		{
			throw new Exception("ScriptEngine: " + error.getKey() + " failed execution!", error.getValue());
		}
	}
	
	public Path getCurrentLoadingScript()
	{
		return _javaExecutionContext.getCurrentExecutingScript();
	}
	
	public static ScriptEngineManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final ScriptEngineManager _instance = new ScriptEngineManager();
	}
}