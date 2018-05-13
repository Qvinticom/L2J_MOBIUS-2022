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
package com.l2jmobius.xml;

public class DescriptorLink
{
	private final String dir;
	private final String namePattern;
	private final String linkFile;
	private final String linkVersion;
	
	public DescriptorLink(String dir, String namePattern, String linkFile, String linkVersion)
	{
		this.dir = dir;
		this.namePattern = namePattern;
		this.linkFile = linkFile;
		this.linkVersion = linkVersion;
	}
	
	public String getFilePattern()
	{
		return dir;
	}
	
	public String getNamePattern()
	{
		return namePattern;
	}
	
	public String getLinkFile()
	{
		return linkFile;
	}
	
	public String getLinkVersion()
	{
		return linkVersion;
	}
}
