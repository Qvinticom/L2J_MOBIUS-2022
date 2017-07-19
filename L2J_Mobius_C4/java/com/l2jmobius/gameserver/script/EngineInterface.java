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

import com.l2jmobius.gameserver.Announcements;
import com.l2jmobius.gameserver.GameTimeController;
import com.l2jmobius.gameserver.RecipeController;
import com.l2jmobius.gameserver.datatables.CharNameTable;
import com.l2jmobius.gameserver.datatables.CharTemplateTable;
import com.l2jmobius.gameserver.datatables.ClanTable;
import com.l2jmobius.gameserver.datatables.ItemTable;
import com.l2jmobius.gameserver.datatables.LevelUpData;
import com.l2jmobius.gameserver.datatables.MapRegionTable;
import com.l2jmobius.gameserver.datatables.NpcTable;
import com.l2jmobius.gameserver.datatables.SkillTable;
import com.l2jmobius.gameserver.datatables.SkillTreeTable;
import com.l2jmobius.gameserver.datatables.SpawnTable;
import com.l2jmobius.gameserver.datatables.TeleportLocationTable;
import com.l2jmobius.gameserver.idfactory.IdFactory;
import com.l2jmobius.gameserver.model.L2World;

/**
 * @author Luis Arias TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style - Code Templates
 */
public interface EngineInterface
{
	// * keep the references of Singletons to prevent garbage collection
	public CharNameTable _charNametable = CharNameTable.getInstance();
	
	public IdFactory _idFactory = IdFactory.getInstance();
	public ItemTable _itemTable = ItemTable.getInstance();
	
	public SkillTable _skillTable = SkillTable.getInstance();
	
	public RecipeController _recipeController = RecipeController.getInstance();
	
	public SkillTreeTable _skillTreeTable = SkillTreeTable.getInstance();
	public CharTemplateTable _charTemplates = CharTemplateTable.getInstance();
	public ClanTable _clanTable = ClanTable.getInstance();
	
	public NpcTable _npcTable = NpcTable.getInstance();
	
	public TeleportLocationTable _teleTable = TeleportLocationTable.getInstance();
	public LevelUpData _levelUpData = LevelUpData.getInstance();
	public L2World _world = L2World.getInstance();
	public SpawnTable _spawnTable = SpawnTable.getInstance();
	public GameTimeController _gameTimeController = GameTimeController.getInstance();
	public Announcements _announcements = Announcements.getInstance();
	public MapRegionTable _mapRegions = MapRegionTable.getInstance();
	
	public void addQuestDrop(int npcID, int itemID, int min, int max, int chance, String questID, String[] states);
	
	public void addEventDrop(int[] items, int[] count, double chance, DateRange range);
	
	public void onPlayerLogin(String[] message, DateRange range);
}