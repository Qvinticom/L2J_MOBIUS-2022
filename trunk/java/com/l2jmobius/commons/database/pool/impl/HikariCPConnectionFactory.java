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

import java.util.logging.Logger;

import javax.sql.DataSource;

import com.l2jmobius.commons.database.pool.AbstractConnectionFactory;
import com.l2jmobius.commons.database.pool.IConnectionFactory;

/**
 * HikariCP Connection Factory implementation.<br>
 * <b>Note that this class is not public to prevent external initialization.</b><br>
 * <b>Access it through {@link ConnectionFactory} and proper configuration.</b>
 * @author Zoey76
 */
final class HikariCPConnectionFactory extends AbstractConnectionFactory
{
	private static final Logger LOG = Logger.getLogger(HikariCPConnectionFactory.class.getName());
	
	private final DataSource _dataSource = null;
	
	public HikariCPConnectionFactory()
	{
		LOG.severe("HikariCP is not supported yet, nothing is going to work!");
	}
	
	@Override
	public void close()
	{
		throw new UnsupportedOperationException("HikariCP is not supported yet!");
	}
	
	@Override
	public DataSource getDataSource()
	{
		return _dataSource;
	}
	
	public static IConnectionFactory getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final IConnectionFactory INSTANCE = new HikariCPConnectionFactory();
	}
}
