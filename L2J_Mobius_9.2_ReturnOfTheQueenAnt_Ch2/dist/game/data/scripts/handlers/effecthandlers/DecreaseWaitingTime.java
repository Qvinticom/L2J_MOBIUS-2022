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
package handlers.effecthandlers;

import org.l2jmobius.commons.util.Chronos;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.effects.AbstractEffect;
import org.l2jmobius.gameserver.model.items.instance.ItemInstance;
import org.l2jmobius.gameserver.model.skills.Skill;
import org.l2jmobius.gameserver.model.variables.PlayerVariables;
import org.l2jmobius.gameserver.network.serverpackets.homunculus.ExShowHomunculusBirthInfo;

/**
 * @author CostyKiller
 */
public class DecreaseWaitingTime extends AbstractEffect
{
	private final long _time;
	
	public DecreaseWaitingTime(StatSet params)
	{
		_time = params.getLong("time", 0);
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public void instant(Creature effector, Creature effected, Skill skill, ItemInstance item)
	{
		final PlayerInstance player = effected.getActingPlayer();
		if (player == null)
		{
			return;
		}
		
		final long currentTime = Chronos.currentTimeMillis();
		long creationTime = player.getVariables().getLong(PlayerVariables.HOMUNCULUS_CREATION_TIME, 0);
		final long waitTime = 0; // 86400 = 24 Hours
		if (creationTime == 0)
		{
			player.getInventory().addItem("DecreaseWaitingTime effect refund", item.getId(), 1, player, player);
			player.sendMessage("You don't have any Homunculus in progress.");
		}
		else if (((currentTime / 1000) - (creationTime / 1000)) >= waitTime)
		{
			player.getInventory().addItem("DecreaseWaitingTime effect refund", item.getId(), 1, player, player);
			player.sendMessage("You cannot decrease the waiting time anymore.");
		}
		else if (((currentTime / 1000) - (creationTime / 1000)) < waitTime)
		{
			player.getVariables().set(PlayerVariables.HOMUNCULUS_CREATION_TIME, creationTime - (_time));
			player.sendPacket(new ExShowHomunculusBirthInfo(player));
		}
		else
		{
			player.sendMessage("You cannot use this item yet.");
		}
	}
}
