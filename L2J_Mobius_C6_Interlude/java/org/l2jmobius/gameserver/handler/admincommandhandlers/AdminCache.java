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
package org.l2jmobius.gameserver.handler.admincommandhandlers;

import java.io.File;
import java.util.StringTokenizer;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.cache.HtmCache;
import org.l2jmobius.gameserver.data.sql.CrestTable;
import org.l2jmobius.gameserver.handler.IAdminCommandHandler;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.util.BuilderUtil;

/**
 * @author Layanere
 */
public class AdminCache implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_cache_htm_rebuild",
		"admin_cache_htm_reload",
		"admin_cache_reload_path",
		"admin_cache_reload_file",
		"admin_cache_crest_rebuild",
		"admin_cache_crest_reload"
	};
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		final StringTokenizer st = new StringTokenizer(command, " ");
		final String comm = st.nextToken();
		if (comm == null)
		{
			return false;
		}
		
		switch (comm)
		{
			case "admin_cache_htm_reload":
			case "admin_cache_htm_rebuild":
			{
				HtmCache.getInstance().reload(Config.DATAPACK_ROOT);
				BuilderUtil.sendSysMessage(activeChar, "Cache[HTML]: " + HtmCache.getInstance().getMemoryUsage() + " MB on " + HtmCache.getInstance().getLoadedFiles() + " file(s) loaded.");
				return true;
			}
			case "admin_cache_reload_path":
			{
				if (st.hasMoreTokens())
				{
					final String path = st.nextToken();
					HtmCache.getInstance().reloadPath(new File(Config.DATAPACK_ROOT, path));
					BuilderUtil.sendSysMessage(activeChar, "Cache[HTML]: " + HtmCache.getInstance().getMemoryUsage() + " MB in " + HtmCache.getInstance().getLoadedFiles() + " file(s) loaded.");
					return true;
				}
				BuilderUtil.sendSysMessage(activeChar, "Usage: //cache_reload_path <path>");
				return false;
			}
			case "admin_cache_reload_file":
			{
				if (st.hasMoreTokens())
				{
					final String path = st.nextToken();
					if (HtmCache.getInstance().loadFile(new File(Config.DATAPACK_ROOT, path)) != null)
					{
						BuilderUtil.sendSysMessage(activeChar, "Cache[HTML]: file was loaded");
					}
					else
					{
						BuilderUtil.sendSysMessage(activeChar, "Cache[HTML]: file can't be loaded");
					}
					return true;
				}
				BuilderUtil.sendSysMessage(activeChar, "Usage: //cache_reload_file <relative_path/file>");
				return false;
			}
			case "admin_cache_crest_rebuild":
			case "admin_cache_crest_reload":
			{
				CrestTable.getInstance().load();
				return true;
			}
			default:
			{
				return false;
			}
		}
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
