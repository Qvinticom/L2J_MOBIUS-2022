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

import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.effects.AbstractEffect;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.model.variables.PlayerVariables;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.homunculus.ExHomunculusPointInfo;

/**
 * Item Effect: Increase Homunculus points permanently.
 * @author `NasSeKa`
 */
public class AddHomunculusPoints extends AbstractEffect
{
	private final int _amount;
	
	public AddHomunculusPoints(StatSet params)
	{
		_amount = params.getInt("amount", 0);
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public void instant(Creature effector, Creature effected, Skill skill, Item item)
	{
		if (!effected.isPlayer())
		{
			return;
		}
		
		final Player player = effected.getActingPlayer();
		final int upgradePoints = player.getVariables().getInt(PlayerVariables.HOMUNCULUS_UPGRADE_POINTS, 0) + _amount;
		player.getVariables().set(PlayerVariables.HOMUNCULUS_UPGRADE_POINTS, upgradePoints);
		player.sendPacket(SystemMessageId.YOU_VE_OBTAINED_UPGRADE_POINTS);
		player.sendPacket(new ExHomunculusPointInfo(player));
	}
}
