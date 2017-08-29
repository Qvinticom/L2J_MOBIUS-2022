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
package com.l2jmobius.gameserver.model.interfaces;

import java.util.List;

import com.l2jmobius.gameserver.model.zone.type.L2BannedSpawnTerritory;
import com.l2jmobius.gameserver.model.zone.type.L2SpawnTerritory;

/**
 * @author UnAfraid
 */
public interface ITerritorized
{
	void addTerritory(L2SpawnTerritory territory);
	
	List<L2SpawnTerritory> getTerritories();
	
	void addBannedTerritory(L2BannedSpawnTerritory territory);
	
	List<L2BannedSpawnTerritory> getBannedTerritories();
}
