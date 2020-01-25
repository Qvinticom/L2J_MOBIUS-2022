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
package org.l2jmobius.gameserver.script;

import org.l2jmobius.gameserver.GameTimeController;
import org.l2jmobius.gameserver.RecipeController;
import org.l2jmobius.gameserver.datatables.ItemTable;
import org.l2jmobius.gameserver.datatables.SkillTable;
import org.l2jmobius.gameserver.datatables.csv.MapRegionTable;
import org.l2jmobius.gameserver.datatables.sql.CharNameTable;
import org.l2jmobius.gameserver.datatables.sql.CharTemplateTable;
import org.l2jmobius.gameserver.datatables.sql.ClanTable;
import org.l2jmobius.gameserver.datatables.sql.LevelUpData;
import org.l2jmobius.gameserver.datatables.sql.NpcTable;
import org.l2jmobius.gameserver.datatables.sql.SkillTreeTable;
import org.l2jmobius.gameserver.datatables.sql.SpawnTable;
import org.l2jmobius.gameserver.datatables.sql.TeleportLocationTable;
import org.l2jmobius.gameserver.idfactory.IdFactory;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.entity.Announcements;

/**
 * @author Luis Arias
 */
public interface EngineInterface
{
	// * keep the references of Singletons to prevent garbage collection
	CharNameTable charNametable = CharNameTable.getInstance();
	
	IdFactory idFactory = IdFactory.getInstance();
	ItemTable itemTable = ItemTable.getInstance();
	
	SkillTable skillTable = SkillTable.getInstance();
	
	RecipeController recipeController = RecipeController.getInstance();
	
	SkillTreeTable skillTreeTable = SkillTreeTable.getInstance();
	CharTemplateTable charTemplates = CharTemplateTable.getInstance();
	ClanTable clanTable = ClanTable.getInstance();
	
	NpcTable npcTable = NpcTable.getInstance();
	
	TeleportLocationTable teleTable = TeleportLocationTable.getInstance();
	LevelUpData levelUpData = LevelUpData.getInstance();
	World world = World.getInstance();
	SpawnTable spawnTable = SpawnTable.getInstance();
	GameTimeController gameTimeController = GameTimeController.getInstance();
	Announcements announcements = Announcements.getInstance();
	MapRegionTable mapRegions = MapRegionTable.getInstance();
	
	void addQuestDrop(int npcID, int itemID, int min, int max, int chance, String questID, String[] states);
	
	void addEventDrop(int[] items, int[] count, double chance, DateRange range);
	
	void onPlayerLogin(String[] message, DateRange range);
}
