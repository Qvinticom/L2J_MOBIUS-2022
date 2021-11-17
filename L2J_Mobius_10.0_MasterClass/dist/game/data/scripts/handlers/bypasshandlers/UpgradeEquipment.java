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

import org.l2jmobius.commons.util.CommonUtil;
import org.l2jmobius.gameserver.handler.IBypassHandler;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.serverpackets.equipmentupgrade.ExShowUpgradeSystem;

/**
 * @author Mobius
 */
public class UpgradeEquipment implements IBypassHandler
{
	private static final int[] HEAD_BLACKSMITH =
	{
		30847, // Ferris (Aden)
		31272, // Noel (Goddard)
		30512, // Kusto (Giran)
		30595, // Opix (Dion)
		31317, // Lombert (Rune)
		30897, // Roman (Heine)
		31961, // Newyear (Schuttgart)
		30677, // Flutter (Oren)
		30499, // Tapoy (Gludin)
		30687, // Vergara (Hunters)
	};
	
	private static final String[] COMMANDS =
	{
		"UpgradeEquipment"
	};
	
	@Override
	public boolean useBypass(String command, Player player, Creature target)
	{
		if ((target == null) || !target.isNpc() || !CommonUtil.contains(HEAD_BLACKSMITH, ((Npc) target).getId()))
		{
			return false;
		}
		
		player.sendPacket(new ExShowUpgradeSystem());
		return true;
	}
	
	@Override
	public String[] getBypassList()
	{
		return COMMANDS;
	}
}
