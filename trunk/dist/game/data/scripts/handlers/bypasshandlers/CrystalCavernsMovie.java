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
package handlers.bypasshandlers;

import com.l2jmobius.gameserver.handler.IBypassHandler;
import com.l2jmobius.gameserver.model.actor.L2Character;
import com.l2jmobius.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author Gigi
 */
public class CrystalCavernsMovie implements IBypassHandler
{
	// NPC
	private static final int FIOREN = 33044;
	// Others
	private static final int CRYSTAL_CAVERNS_MOVIE = 106;
	private static final String[] COMMANDS =
	{
		"CrystalCavernsMovie"
	};
	
	@Override
	public boolean useBypass(String command, L2PcInstance activeChar, L2Character target)
	{
		if (!target.isNpc() || (((L2NpcInstance) target).getId() != FIOREN))
		{
			return false;
		}
		
		activeChar.showQuestMovie(CRYSTAL_CAVERNS_MOVIE);
		return true;
	}
	
	@Override
	public String[] getBypassList()
	{
		return COMMANDS;
	}
}