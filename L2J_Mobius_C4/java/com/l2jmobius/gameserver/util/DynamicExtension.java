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
package com.l2jmobius.gameserver.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jmobius.Config;

/**
 * extension loader for l2j
 * @author galun
 * @version $Id: DynamicExtension.java,v 1.3 2006/05/14 17:19:39 galun Exp $
 */
public class DynamicExtension
{
	private static Logger log = Logger.getLogger(DynamicExtension.class.getCanonicalName());
	private JarClassLoader classLoader;
	private Properties prop;
	private ConcurrentHashMap<String, Object> loadedExtensions;
	private static DynamicExtension instance;
	private final ConcurrentHashMap<String, ExtensionFunction> getters;
	private final ConcurrentHashMap<String, ExtensionFunction> setters;
	
	/**
	 * create an instance of DynamicExtension this will be done by GameServer according to the altsettings.properties
	 */
	private DynamicExtension()
	{
		if (instance == null)
		{
			instance = this;
		}
		getters = new ConcurrentHashMap<>();
		setters = new ConcurrentHashMap<>();
		initExtensions();
	}
	
	/**
	 * get the singleton of DynamicInstance
	 * @return the singleton instance
	 */
	public static DynamicExtension getInstance()
	{
		if (instance == null)
		{
			instance = new DynamicExtension();
		}
		return instance;
	}
	
	/**
	 * get an extension object by class name
	 * @param className he class name as defined in the extension properties
	 * @return the object or null if not found
	 */
	public Object getExtension(String className)
	{
		return loadedExtensions.get(className);
	}
	
	/**
	 * initialize all configured extensions
	 * @return
	 */
	public String initExtensions()
	{
		prop = new Properties();
		String res = "";
		loadedExtensions = new ConcurrentHashMap<>();
		
		try (FileInputStream fs = new FileInputStream(Config.EXTENSIONS_CONFIGURATION_FILE))
		{
			prop.load(fs);
		}
		catch (final FileNotFoundException ex)
		{
			log.info(ex.getMessage() + ": no extensions to load");
		}
		catch (final Exception ex)
		{
			log.log(Level.WARNING, "could not load properties", ex);
		}
		classLoader = new JarClassLoader();
		for (final Object o : prop.keySet())
		{
			final String k = (String) o;
			if (k.endsWith("Class"))
			{
				res += initExtension(prop.getProperty(k)) + "\n";
			}
		}
		return res;
	}
	
	/**
	 * init a named extension
	 * @param name the class name and optionally a jar file name delimited with a '@' if the jar file is not in the class path
	 * @return
	 */
	public String initExtension(String name)
	{
		String className = name;
		final String[] p = name.split("@");
		String res = name + " loaded";
		if (p.length > 1)
		{
			classLoader.addJarFile(p[1]);
			className = p[0];
		}
		if (loadedExtensions.containsKey(className))
		{
			return "already loaded";
		}
		try
		{
			final Class<?> extension = Class.forName(className, true, classLoader);
			final Object obj = extension.newInstance();
			extension.getMethod("init", new Class[0]).invoke(obj, new Object[0]);
			log.info("Extension " + className + " loaded.");
			loadedExtensions.put(className, obj);
		}
		catch (final Exception ex)
		{
			log.log(Level.WARNING, name, ex);
			res = ex.toString();
		}
		return res;
	}
	
	/**
	 * create a new class loader which resets the cache (jar files and loaded classes) on next class loading request it will read the jar again
	 */
	protected void clearCache()
	{
		classLoader = new JarClassLoader();
	}
	
	/**
	 * call unloadExtension() for all known extensions
	 * @return
	 */
	public String unloadExtensions()
	{
		String res = "";
		for (final String e : loadedExtensions.keySet())
		{
			res += unloadExtension(e) + "\n";
		}
		return res;
	}
	
	/**
	 * get all loaded extensions
	 * @return a String array with the class names
	 */
	public String[] getExtensions()
	{
		final String[] l = new String[loadedExtensions.size()];
		loadedExtensions.keySet().toArray(l);
		return l;
	}
	
	/**
	 * unload a named extension
	 * @param name the class name and optionally a jar file name delimited with a '@'
	 * @return
	 */
	public String unloadExtension(String name)
	{
		String className = name;
		final String[] p = name.split("@");
		if (p.length > 1)
		{
			classLoader.addJarFile(p[1]);
			className = p[0];
		}
		String res = className + " unloaded";
		try
		{
			final Object obj = loadedExtensions.get(className);
			final Class<?> extension = obj.getClass();
			loadedExtensions.remove(className);
			extension.getMethod("unload", new Class[0]).invoke(obj, new Object[0]);
			log.info("Extension " + className + " unloaded.");
		}
		catch (final Exception ex)
		{
			log.log(Level.WARNING, "could not unload " + className, ex);
			res = ex.toString();
		}
		return res;
	}
	
	/**
	 * unloads all extensions, resets the cache and initializes all configured extensions
	 */
	public void reload()
	{
		unloadExtensions();
		clearCache();
		initExtensions();
	}
	
	/**
	 * unloads a named extension, resets the cache and initializes the extension
	 * @param name the class name and optionally a jar file name delimited with a '@' if the jar file is not in the class path
	 */
	public void reload(String name)
	{
		unloadExtension(name);
		clearCache();
		initExtension(name);
	}
	
	/**
	 * register a getter function given a (hopefully) unique name
	 * @param name the name of the function
	 * @param function the ExtensionFunction implementation
	 */
	public void addGetter(String name, ExtensionFunction function)
	{
		getters.put(name, function);
	}
	
	/**
	 * deregister a getter function
	 * @param name the name used for registering
	 */
	public void removeGetter(String name)
	{
		getters.remove(name);
	}
	
	/**
	 * call a getter function registered with DynamicExtension
	 * @param name the function name
	 * @param arg a function argument
	 * @return an object from the extension
	 */
	public Object get(String name, String arg)
	{
		final ExtensionFunction func = getters.get(name);
		if (func != null)
		{
			return func.get(arg);
		}
		return "<none>";
	}
	
	/**
	 * register a setter function given a (hopefully) unique name
	 * @param name the name of the function
	 * @param function the ExtensionFunction implementation
	 */
	public void addSetter(String name, ExtensionFunction function)
	{
		setters.put(name, function);
	}
	
	/**
	 * deregister a setter function
	 * @param name the name used for registering
	 */
	public void removeSetter(String name)
	{
		setters.remove(name);
	}
	
	/**
	 * call a setter function registered with DynamicExtension
	 * @param name the function name
	 * @param arg a function argument
	 * @param obj an object to set
	 */
	public void set(String name, String arg, Object obj)
	{
		final ExtensionFunction func = setters.get(name);
		if (func != null)
		{
			func.set(arg, obj);
		}
	}
	
	public JarClassLoader getClassLoader()
	{
		return classLoader;
	}
}