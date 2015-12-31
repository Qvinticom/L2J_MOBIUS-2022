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
package com.l2jmobius.commons.database.pool.impl;

import com.l2jmobius.Config;
import com.l2jmobius.commons.database.pool.IConnectionFactory;

/**
 * Connection Factory implementation.
 * @author Zoey76
 */
public class ConnectionFactory
{
	public static IConnectionFactory getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final IConnectionFactory INSTANCE;
		static
		{
			switch (Config.DATABASE_CONNECTION_POOL)
			{
				default:
				case "C3P0":
				{
					INSTANCE = new C3P0ConnectionFactory();
					break;
				}
				case "HikariCP":
				{
					INSTANCE = new HikariCPConnectionFactory();
					break;
				}
				case "BoneCP":
				{
					INSTANCE = new BoneCPConnectionFactory();
					break;
				}
			}
		}
	}
}
