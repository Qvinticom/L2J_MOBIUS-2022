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
package org.l2jmobius.gameserver.model.actor.tasks.player;

import java.util.List;

import org.l2jmobius.gameserver.model.Skill;
import org.l2jmobius.gameserver.model.actor.Player;

public class CancelSkillRestoreTask implements Runnable
{
	private Player _player = null;
	private List<Skill> _buffs = null;
	
	public CancelSkillRestoreTask(Player player, List<Skill> buffs)
	{
		_player = player;
		_buffs = buffs;
	}
	
	@Override
	public void run()
	{
		if ((_player == null) || !_player.isOnline())
		{
			return;
		}
		
		for (Skill skill : _buffs)
		{
			if (skill == null)
			{
				continue;
			}
			skill.applyEffects(_player, _player);
		}
	}
}