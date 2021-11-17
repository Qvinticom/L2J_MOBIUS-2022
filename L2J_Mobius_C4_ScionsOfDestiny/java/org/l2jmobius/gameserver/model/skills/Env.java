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
package org.l2jmobius.gameserver.model.skills;

import org.l2jmobius.gameserver.model.Skill;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.item.instance.Item;

/**
 * @author ProGramMoS, eX1steam, An Env object is just a class to pass parameters to a calculator such as Player, Item, Initial value.
 */
public class Env
{
	public Creature player;
	public Creature target;
	public Item item;
	public Skill skill;
	public double value;
	public double baseValue;
	public boolean skillMastery = false;
	private Creature creature;
	private Creature _target;
	
	public Creature getCharacter()
	{
		return creature;
	}
	
	public Player getPlayer()
	{
		return creature == null ? null : creature.getActingPlayer();
	}
	
	public Creature getTarget()
	{
		return _target;
	}
}
