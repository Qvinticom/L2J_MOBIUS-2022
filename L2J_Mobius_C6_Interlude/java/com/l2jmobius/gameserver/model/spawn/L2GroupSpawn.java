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
package com.l2jmobius.gameserver.model.spawn;

import java.lang.reflect.Constructor;

import com.l2jmobius.Config;
import com.l2jmobius.commons.util.Rnd;
import com.l2jmobius.gameserver.datatables.sql.TerritoryTable;
import com.l2jmobius.gameserver.idfactory.IdFactory;
import com.l2jmobius.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jmobius.gameserver.templates.chars.L2NpcTemplate;

/**
 * @author littlecrow A special spawn implementation to spawn controllable mob
 */
public class L2GroupSpawn extends L2Spawn
{
	private final Constructor<?> _constructor;
	private final L2NpcTemplate _template;
	
	public L2GroupSpawn(L2NpcTemplate mobTemplate) throws SecurityException, ClassNotFoundException, NoSuchMethodException
	{
		super(mobTemplate);
		_constructor = Class.forName("com.l2jmobius.gameserver.model.actor.instance.L2ControllableMobInstance").getConstructors()[0];
		_template = mobTemplate;
		
		setAmount(1);
	}
	
	public L2NpcInstance doGroupSpawn()
	{
		L2NpcInstance mob = null;
		
		try
		{
			if (_template.type.equalsIgnoreCase("L2Pet") || _template.type.equalsIgnoreCase("L2Minion"))
			{
				return null;
			}
			
			Object[] parameters =
			{
				IdFactory.getInstance().getNextId(),
				_template
			};
			Object tmp = _constructor.newInstance(parameters);
			
			if (!(tmp instanceof L2NpcInstance))
			{
				return null;
			}
			
			mob = (L2NpcInstance) tmp;
			
			int newlocx;
			int newlocy;
			int newlocz;
			
			if ((getX() == 0) && (getY() == 0))
			{
				if (getLocation() == 0)
				{
					return null;
				}
				
				final int p[] = TerritoryTable.getInstance().getRandomPoint(getLocation());
				newlocx = p[0];
				newlocy = p[1];
				newlocz = p[2];
			}
			else
			{
				newlocx = getX();
				newlocy = getY();
				newlocz = getZ();
			}
			
			mob.setCurrentHpMp(mob.getMaxHp(), mob.getMaxMp());
			
			if (getHeading() == -1)
			{
				mob.setHeading(Rnd.nextInt(61794));
			}
			else
			{
				mob.setHeading(getHeading());
			}
			
			mob.setSpawn(this);
			mob.spawnMe(newlocx, newlocy, newlocz);
			mob.onSpawn();
			
			if (Config.DEBUG)
			{
				LOGGER.info("spawned Mob ID: " + _template.npcId + " ,at: " + mob.getX() + " x, " + mob.getY() + " y, " + mob.getZ() + " z");
			}
			
			return mob;
			
		}
		catch (Exception e)
		{
			LOGGER.warning("NPC class not found: " + e);
			return null;
		}
	}
}
