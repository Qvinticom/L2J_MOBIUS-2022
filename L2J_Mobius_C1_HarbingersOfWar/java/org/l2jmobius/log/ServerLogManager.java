/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.l2jmobius.log;

import java.util.logging.LogManager;

/**
 * Specialized {@link LogManager} class.<br>
 * Prevents log devices to close before shutdown sequence so the shutdown sequence can make logging.
 */
public class ServerLogManager extends LogManager
{
	@Override
	public void reset()
	{
		// do nothing
	}
	
	public void doReset()
	{
		super.reset();
	}
}
