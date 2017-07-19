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
package com.l2jmobius.gameserver.communitybbs.Manager;

import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.serverpackets.ShowBoard;

public class AdminBBSManager extends BaseBBSManager
{
	private static AdminBBSManager _Instance = null;
	
	/**
	 * @return
	 */
	public static AdminBBSManager getInstance()
	{
		if (_Instance == null)
		{
			_Instance = new AdminBBSManager();
		}
		return _Instance;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.communitybbs.Manager.BaseBBSManager#parsecmd(java.lang.String, com.l2jmobius.gameserver.model.actor.instance.L2PcInstance)
	 */
	@Override
	public void parsecmd(String command, L2PcInstance activeChar)
	{
		if (!activeChar.isGM())
		{
			return;
		}
		
		if (command.startsWith("admin_bbs"))
		{
			separateAndSend("<html><body><br><br><center>This Page is only an exemple :)<br><br>command=" + command + "</center></body></html>", activeChar);
		}
		else
		{
			final ShowBoard sb = new ShowBoard("<html><body><br><br><center>the command: " + command + " is not implemented yet</center><br><br></body></html>", "101");
			activeChar.sendPacket(sb);
			activeChar.sendPacket(new ShowBoard(null, "102"));
			activeChar.sendPacket(new ShowBoard(null, "103"));
		}
	}
	
	@Override
	public void parsewrite(String ar1, String ar2, String ar3, String ar4, String ar5, L2PcInstance activeChar)
	{
		if (!activeChar.isGM())
		{
			return;
		}
	}
}