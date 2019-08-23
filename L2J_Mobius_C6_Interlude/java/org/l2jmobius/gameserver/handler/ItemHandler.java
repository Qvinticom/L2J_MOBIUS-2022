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
package org.l2jmobius.gameserver.handler;

import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import org.l2jmobius.gameserver.GameServer;
import org.l2jmobius.gameserver.handler.itemhandlers.BeastSoulShot;
import org.l2jmobius.gameserver.handler.itemhandlers.BeastSpice;
import org.l2jmobius.gameserver.handler.itemhandlers.BeastSpiritShot;
import org.l2jmobius.gameserver.handler.itemhandlers.BlessedSpiritShot;
import org.l2jmobius.gameserver.handler.itemhandlers.Book;
import org.l2jmobius.gameserver.handler.itemhandlers.BreakingArrow;
import org.l2jmobius.gameserver.handler.itemhandlers.CharChangePotions;
import org.l2jmobius.gameserver.handler.itemhandlers.ChestKey;
import org.l2jmobius.gameserver.handler.itemhandlers.ChristmasTree;
import org.l2jmobius.gameserver.handler.itemhandlers.CrystalCarol;
import org.l2jmobius.gameserver.handler.itemhandlers.Crystals;
import org.l2jmobius.gameserver.handler.itemhandlers.CustomPotions;
import org.l2jmobius.gameserver.handler.itemhandlers.EnchantScrolls;
import org.l2jmobius.gameserver.handler.itemhandlers.EnergyStone;
import org.l2jmobius.gameserver.handler.itemhandlers.ExtractableItems;
import org.l2jmobius.gameserver.handler.itemhandlers.Firework;
import org.l2jmobius.gameserver.handler.itemhandlers.FishShots;
import org.l2jmobius.gameserver.handler.itemhandlers.Harvester;
import org.l2jmobius.gameserver.handler.itemhandlers.HeroCustomItem;
import org.l2jmobius.gameserver.handler.itemhandlers.JackpotSeed;
import org.l2jmobius.gameserver.handler.itemhandlers.MOSKey;
import org.l2jmobius.gameserver.handler.itemhandlers.MapForestOfTheDead;
import org.l2jmobius.gameserver.handler.itemhandlers.Maps;
import org.l2jmobius.gameserver.handler.itemhandlers.MercTicket;
import org.l2jmobius.gameserver.handler.itemhandlers.MysteryPotion;
import org.l2jmobius.gameserver.handler.itemhandlers.Nectar;
import org.l2jmobius.gameserver.handler.itemhandlers.NobleCustomItem;
import org.l2jmobius.gameserver.handler.itemhandlers.PaganKeys;
import org.l2jmobius.gameserver.handler.itemhandlers.Potions;
import org.l2jmobius.gameserver.handler.itemhandlers.Recipes;
import org.l2jmobius.gameserver.handler.itemhandlers.Remedy;
import org.l2jmobius.gameserver.handler.itemhandlers.RollingDice;
import org.l2jmobius.gameserver.handler.itemhandlers.ScrollOfEscape;
import org.l2jmobius.gameserver.handler.itemhandlers.ScrollOfResurrection;
import org.l2jmobius.gameserver.handler.itemhandlers.Scrolls;
import org.l2jmobius.gameserver.handler.itemhandlers.Seed;
import org.l2jmobius.gameserver.handler.itemhandlers.SevenSignsRecord;
import org.l2jmobius.gameserver.handler.itemhandlers.SoulCrystals;
import org.l2jmobius.gameserver.handler.itemhandlers.SoulShots;
import org.l2jmobius.gameserver.handler.itemhandlers.SpecialXMas;
import org.l2jmobius.gameserver.handler.itemhandlers.SpiritShot;
import org.l2jmobius.gameserver.handler.itemhandlers.SummonItems;

public class ItemHandler
{
	private static final Logger LOGGER = Logger.getLogger(GameServer.class.getName());
	
	private final Map<Integer, IItemHandler> _datatable;
	
	/**
	 * Returns the number of elements contained in datatable
	 * @return int : Size of the datatable
	 */
	public int size()
	{
		return _datatable.size();
	}
	
	/**
	 * Constructor of ItemHandler
	 */
	private ItemHandler()
	{
		_datatable = new TreeMap<>();
		registerItemHandler(new ScrollOfEscape());
		registerItemHandler(new ScrollOfResurrection());
		registerItemHandler(new SoulShots());
		registerItemHandler(new SpiritShot());
		registerItemHandler(new BlessedSpiritShot());
		registerItemHandler(new BeastSoulShot());
		registerItemHandler(new BeastSpiritShot());
		registerItemHandler(new ChestKey());
		registerItemHandler(new CustomPotions());
		registerItemHandler(new PaganKeys());
		registerItemHandler(new Maps());
		registerItemHandler(new MapForestOfTheDead());
		registerItemHandler(new Potions());
		registerItemHandler(new Recipes());
		registerItemHandler(new RollingDice());
		registerItemHandler(new MysteryPotion());
		registerItemHandler(new EnchantScrolls());
		registerItemHandler(new EnergyStone());
		registerItemHandler(new Book());
		registerItemHandler(new Remedy());
		registerItemHandler(new Scrolls());
		registerItemHandler(new CrystalCarol());
		registerItemHandler(new SoulCrystals());
		registerItemHandler(new SevenSignsRecord());
		registerItemHandler(new CharChangePotions());
		registerItemHandler(new Firework());
		registerItemHandler(new Seed());
		registerItemHandler(new Harvester());
		registerItemHandler(new MercTicket());
		registerItemHandler(new Nectar());
		registerItemHandler(new FishShots());
		registerItemHandler(new ExtractableItems());
		registerItemHandler(new SpecialXMas());
		registerItemHandler(new SummonItems());
		registerItemHandler(new BeastSpice());
		registerItemHandler(new JackpotSeed());
		registerItemHandler(new NobleCustomItem());
		registerItemHandler(new HeroCustomItem());
		registerItemHandler(new MOSKey());
		registerItemHandler(new BreakingArrow());
		registerItemHandler(new ChristmasTree());
		registerItemHandler(new Crystals());
		LOGGER.info("ItemHandler: Loaded " + _datatable.size() + " handlers.");
	}
	
	/**
	 * Adds handler of item type in <I>datatable</I>.<BR>
	 * <BR>
	 * <B><I>Concept :</I></U><BR>
	 * This handler is put in <I>datatable</I> Map &lt;Integer ; IItemHandler &gt; for each ID corresponding to an item type (existing in classes of package itemhandlers) sets as key of the Map.
	 * @param handler (IItemHandler)
	 */
	public void registerItemHandler(IItemHandler handler)
	{
		// Get all ID corresponding to the item type of the handler
		final int[] ids = handler.getItemIds();
		
		// Add handler for each ID found
		for (int id : ids)
		{
			_datatable.put(id, handler);
		}
	}
	
	/**
	 * Returns the handler of the item
	 * @param itemId : int designating the itemID
	 * @return IItemHandler
	 */
	public IItemHandler getItemHandler(int itemId)
	{
		return _datatable.get(itemId);
	}
	
	public static ItemHandler getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final ItemHandler INSTANCE = new ItemHandler();
	}
}
