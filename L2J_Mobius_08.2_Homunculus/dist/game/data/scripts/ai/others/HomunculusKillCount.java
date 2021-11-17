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
package ai.others;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.model.Party;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.events.EventType;
import org.l2jmobius.gameserver.model.events.ListenerRegisterType;
import org.l2jmobius.gameserver.model.events.annotations.RegisterEvent;
import org.l2jmobius.gameserver.model.events.annotations.RegisterType;
import org.l2jmobius.gameserver.model.events.impl.creature.npc.OnAttackableKill;
import org.l2jmobius.gameserver.model.variables.PlayerVariables;
import org.l2jmobius.gameserver.network.serverpackets.homunculus.ExHomunculusPointInfo;

import ai.AbstractNpcAI;

/**
 * @author CostyKiller
 */
public class HomunculusKillCount extends AbstractNpcAI
{
	private static final int LEVEL_DIFFERENCE = 9;
	
	@RegisterEvent(EventType.ON_ATTACKABLE_KILL)
	@RegisterType(ListenerRegisterType.GLOBAL_MONSTERS)
	public void onAttackableKill(OnAttackableKill event)
	{
		final Creature creature = event.getTarget();
		if ((creature != null) && creature.isMonster())
		{
			final Player player = event.getAttacker().getActingPlayer();
			if ((player != null) && (Math.abs(player.getLevel() - creature.getLevel()) <= LEVEL_DIFFERENCE))
			{
				if (player.isInParty())
				{
					final Party party = player.getParty();
					for (Player member : party.getMembers())
					{
						if (member.isInsideRadius3D(creature, Config.ALT_PARTY_RANGE))
						{
							final int killedMobs = member.getVariables().getInt(PlayerVariables.HOMUNCULUS_KILLED_MOBS, 0);
							if (killedMobs < 500)
							{
								member.getVariables().set(PlayerVariables.HOMUNCULUS_KILLED_MOBS, killedMobs + 1);
								member.sendPacket(new ExHomunculusPointInfo(member));
							}
						}
					}
				}
				else
				{
					final int killedMobs = player.getVariables().getInt(PlayerVariables.HOMUNCULUS_KILLED_MOBS, 0);
					if (killedMobs < 500)
					{
						player.getVariables().set(PlayerVariables.HOMUNCULUS_KILLED_MOBS, killedMobs + 1);
						player.sendPacket(new ExHomunculusPointInfo(player));
					}
				}
			}
		}
	}
	
	public static void main(String[] args)
	{
		new HomunculusKillCount();
	}
}