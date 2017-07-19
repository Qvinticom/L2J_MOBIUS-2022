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
package com.l2jmobius.gameserver.script;

import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javolution.util.FastList;

/**
 * @author Luis Arias TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style - Code Templates
 */
public class ScriptPackage
{
	private final List<ScriptDocument> scriptFiles;
	private final List<String> otherFiles;
	private final String name;
	
	public ScriptPackage(ZipFile pack)
	{
		scriptFiles = new FastList<>();
		otherFiles = new FastList<>();
		name = pack.getName();
		addFiles(pack);
	}
	
	/**
	 * @return Returns the otherFiles.
	 */
	public List<String> getOtherFiles()
	
	{
		return otherFiles;
	}
	
	/**
	 * @return Returns the scriptFiles.
	 */
	public List<ScriptDocument> getScriptFiles()
	{
		return scriptFiles;
	}
	
	/**
	 * @param pack The scriptFiles to set.
	 */
	private void addFiles(ZipFile pack)
	{
		for (final Enumeration<? extends ZipEntry> e = pack.entries(); e.hasMoreElements();)
		{
			final ZipEntry entry = e.nextElement();
			if (entry.getName().endsWith(".xml"))
			{
				try
				{
					final ScriptDocument newScript = new ScriptDocument(entry.getName(), pack.getInputStream(entry));
					scriptFiles.add(newScript);
				}
				catch (final IOException e1)
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			else if (!entry.isDirectory())
			{
				otherFiles.add(entry.getName());
			}
		}
	}
	
	/**
	 * @return Returns the name.
	 */
	public String getName()
	{
		return name;
	}
	
	@Override
	public String toString()
	{
		if (getScriptFiles().isEmpty() && getOtherFiles().isEmpty())
		{
			return "Empty Package.";
		}
		
		String out = "Package Name: " + getName() + "\n";
		
		if (!getScriptFiles().isEmpty())
		{
			out += "Xml Script Files...\n";
			for (final ScriptDocument script : getScriptFiles())
			{
				out += script.getName() + "\n";
			}
		}
		
		if (!getOtherFiles().isEmpty())
		{
			out += "Other Files...\n";
			for (final String fileName : getOtherFiles())
			{
				out += fileName + "\n";
			}
		}
		return out;
	}
}
