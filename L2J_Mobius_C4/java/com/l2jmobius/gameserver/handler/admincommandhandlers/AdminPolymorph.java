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
package com.l2jmobius.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.handler.IAdminCommandHandler;
import com.l2jmobius.gameserver.model.L2Character;
import com.l2jmobius.gameserver.model.L2Object;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.serverpackets.MagicSkillUse;
import com.l2jmobius.gameserver.network.serverpackets.SetupGauge;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * This class handles following admin commands: - delete = deletes target
 * @version $Revision: 1.2.2.1.2.4 $ $Date: 2005/04/11 10:05:56 $
 */
public class AdminPolymorph implements IAdminCommandHandler
{
	// private static Logger _log = Logger.getLogger(AdminDelete.class.getName());
	
	private static String[] _adminCommands =
	{
		"admin_polymorph"
	};
	
	private static final int REQUIRED_LEVEL = Config.GM_NPC_EDIT;
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (!Config.ALT_PRIVILEGES_ADMIN)
		{
			if (!(checkLevel(activeChar.getAccessLevel()) && activeChar.isGM()))
			{
				return false;
			}
		}
		
		if (command.startsWith("admin_polymorph"))
		{
			final StringTokenizer st = new StringTokenizer(command);
			try
			{
				st.nextToken();
				final String type = st.nextToken();
				final String id = st.nextToken();
				final L2Object target = activeChar.getTarget();
				doPolymorph(activeChar, target, id, type);
			}
			catch (final Exception e)
			{
			}
		}
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return _adminCommands;
	}
	
	private boolean checkLevel(int level)
	{
		return (level >= REQUIRED_LEVEL);
	}
	
	private void doPolymorph(L2PcInstance activeChar, L2Object obj, String id, String type)
	{
		
		if (obj != null)
		{
			obj.getPoly().setPolyInfo(type, id);
			// animation
			if (obj instanceof L2Character)
			{
				final L2Character Char = (L2Character) obj;
				final MagicSkillUse msk = new MagicSkillUse(Char, 1008, 1, 4000, 0);
				Char.broadcastPacket(msk);
				final SetupGauge sg = new SetupGauge(0, 4000);
				Char.sendPacket(sg);
			}
			// end of animation
			// L2Character target = (L2Character) obj;
			obj.decayMe();
			obj.spawnMe(obj.getX(), obj.getY(), obj.getZ());
			
		}
		else
		{
			final SystemMessage sm = new SystemMessage(SystemMessage.S1_S2);
			sm.addString("Incorrect target.");
			activeChar.sendPacket(sm);
		}
	}
}