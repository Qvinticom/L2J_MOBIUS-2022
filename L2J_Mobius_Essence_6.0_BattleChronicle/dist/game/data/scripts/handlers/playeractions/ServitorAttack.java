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
package handlers.playeractions;

import org.l2jmobius.gameserver.handler.IPlayerActionHandler;
import org.l2jmobius.gameserver.model.ActionDataHolder;
import org.l2jmobius.gameserver.model.actor.Summon;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.SystemMessageId;

/**
 * Servitor Attack player action handler.
 * @author St3eT
 */
public class ServitorAttack implements IPlayerActionHandler
{
	@Override
	public void useAction(PlayerInstance player, ActionDataHolder data, boolean ctrlPressed, boolean shiftPressed)
	{
		if (player.hasServitors())
		{
			for (Summon summon : player.getServitors().values())
			{
				if (summon.canAttack(player.getTarget(), ctrlPressed))
				{
					summon.doAttack(player.getTarget());
				}
			}
		}
		else
		{
			player.sendPacket(SystemMessageId.YOU_DON_T_HAVE_A_SERVITOR);
		}
	}
}