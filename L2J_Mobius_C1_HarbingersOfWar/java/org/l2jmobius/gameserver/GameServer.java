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
package org.l2jmobius.gameserver;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.data.CharNameTable;
import org.l2jmobius.gameserver.data.CharStatsTable;
import org.l2jmobius.gameserver.data.CharTemplateTable;
import org.l2jmobius.gameserver.data.ClanTable;
import org.l2jmobius.gameserver.data.ExperienceTable;
import org.l2jmobius.gameserver.data.ItemTable;
import org.l2jmobius.gameserver.data.LevelUpData;
import org.l2jmobius.gameserver.data.MapRegionTable;
import org.l2jmobius.gameserver.data.NpcTable;
import org.l2jmobius.gameserver.data.PriceListTable;
import org.l2jmobius.gameserver.data.SkillTable;
import org.l2jmobius.gameserver.data.SkillTreeTable;
import org.l2jmobius.gameserver.data.SpawnTable;
import org.l2jmobius.gameserver.data.TeleportLocationTable;
import org.l2jmobius.gameserver.data.TradeController;
import org.l2jmobius.gameserver.handler.ItemHandler;
import org.l2jmobius.gameserver.handler.SkillHandler;
import org.l2jmobius.gameserver.handler.itemhandlers.PetSummon;
import org.l2jmobius.gameserver.handler.itemhandlers.Potions;
import org.l2jmobius.gameserver.handler.itemhandlers.ScrollOfEscape;
import org.l2jmobius.gameserver.handler.itemhandlers.SoulShots;
import org.l2jmobius.gameserver.handler.itemhandlers.WorldMap;
import org.l2jmobius.gameserver.handler.skillhandlers.DamageSkill;
import org.l2jmobius.gameserver.handler.skillhandlers.HealSkill;
import org.l2jmobius.gameserver.managers.GmListManager;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.network.ClientThread;
import org.l2jmobius.gameserver.threadpool.ThreadPool;
import org.l2jmobius.loginserver.LoginController;

public class GameServer extends Thread
{
	static Logger _log = Logger.getLogger(GameServer.class.getName());
	private ServerSocket _serverSocket;
	
	private final ItemTable _itemTable;
	private final SkillTable _skillTable;
	private final NpcTable _npcTable;
	private final ItemHandler _itemHandler;
	private final SkillHandler _skillHandler;
	private final LoginController _loginController;
	
	protected final TradeController _tradeController;
	protected final SkillTreeTable _skillTreeTable;
	protected final ClanTable _clanTable;
	
	protected final ExperienceTable _expTable;
	protected final TeleportLocationTable _teleTable;
	protected final LevelUpData _levelUpData;
	protected final CharStatsTable _modifiers;
	protected final World _world;
	protected final CharTemplateTable _charTemplates;
	protected final IdFactory _idFactory;
	protected final SpawnTable _spawnTable;
	protected final CharNameTable _charNametable;
	protected final GameTimeController _gameTimeController;
	protected final Announcements _announcements;
	protected final MapRegionTable _mapRegions;
	protected final PriceListTable _pricelist;
	protected final GmListManager _gmList;
	
	public static void main(String[] args) throws Exception
	{
		final GameServer server = new GameServer();
		_log.config("GameServer Listening on port 7777");
		server.start();
	}
	
	@Override
	public void run()
	{
		do
		{
			try
			{
				do
				{
					// _log.info("Waiting for client connection...");
					final Socket connection = _serverSocket.accept();
					new ClientThread(connection);
				}
				while (true);
			}
			catch (IOException e)
			{
				continue;
			}
		}
		while (true);
	}
	
	public long getUsedMemoryMB()
	{
		return (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024L / 1024L;
	}
	
	public GameServer() throws Exception
	{
		super("GameServer");
		
		ThreadPool.init();
		
		if (!Config.SERVER_HOST_NAME.equals("*"))
		{
			final InetAddress adr = InetAddress.getByName(Config.SERVER_HOST_NAME);
			final String ip = adr.getHostAddress();
			_serverSocket = new ServerSocket(Config.SERVER_PORT, 50, adr);
			_log.config("GameServer listening on IP:" + ip + " Port " + Config.SERVER_PORT);
		}
		else
		{
			_serverSocket = new ServerSocket(Config.SERVER_PORT);
			_log.config("GameServer listening on all available IPs on Port " + Config.SERVER_PORT);
		}
		
		_log.config("Maximum Numbers of Connected Players: " + Config.MAXIMUM_ONLINE_PLAYERS);
		new File("data/clans").mkdirs();
		new File("data/crests").mkdirs();
		_loginController = LoginController.getInstance();
		_loginController.setMaxAllowedOnlinePlayers(Config.MAXIMUM_ONLINE_PLAYERS);
		_charNametable = CharNameTable.getInstance();
		_idFactory = IdFactory.getInstance();
		_itemTable = ItemTable.getInstance();
		if (!_itemTable.isInitialized())
		{
			_log.severe("Could not find the extraced files. Please run convertData.");
			throw new Exception("Could not initialize the item table");
		}
		_tradeController = TradeController.getInstance();
		_skillTable = SkillTable.getInstance();
		if (!_skillTable.isInitialized())
		{
			_log.severe("Could not find the extraced files. Please run convertData.");
			throw new Exception("Could not initialize the skill table");
		}
		_skillTreeTable = SkillTreeTable.getInstance();
		_charTemplates = CharTemplateTable.getInstance();
		_clanTable = ClanTable.getInstance();
		_npcTable = NpcTable.getInstance();
		if (!_npcTable.isInitialized())
		{
			_log.severe("Could not find the extraced files. Please run convertData.");
			throw new Exception("Could not initialize the npc table");
		}
		_expTable = ExperienceTable.getInstance();
		_teleTable = TeleportLocationTable.getInstance();
		_levelUpData = LevelUpData.getInstance();
		_modifiers = CharStatsTable.getInstance();
		_world = World.getInstance();
		_spawnTable = SpawnTable.getInstance();
		_gameTimeController = GameTimeController.getInstance();
		_announcements = Announcements.getInstance();
		_mapRegions = MapRegionTable.getInstance();
		_pricelist = PriceListTable.getInstance();
		_itemHandler = ItemHandler.getInstance();
		_itemHandler.registerItemHandler(new PetSummon());
		_itemHandler.registerItemHandler(new ScrollOfEscape());
		_itemHandler.registerItemHandler(new SoulShots());
		_itemHandler.registerItemHandler(new WorldMap());
		_itemHandler.registerItemHandler(new Potions());
		_skillHandler = SkillHandler.getInstance();
		_skillHandler.registerSkillHandler(new HealSkill());
		_skillHandler.registerSkillHandler(new DamageSkill());
		_gmList = GmListManager.getInstance();
	}
}
