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
package com.l2jmobius.gameserver.skills;

import com.l2jmobius.gameserver.model.L2Skill;
import com.l2jmobius.gameserver.model.actor.L2Character;
import com.l2jmobius.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author ProGramMoS, eX1steam, An Env object is just a class to pass parameters to a calculator such as L2PcInstance, L2ItemInstance, Initial value.
 */
public final class Env
{
	public L2Character player;
	public L2Character target;
	public L2ItemInstance item;
	public L2Skill skill;
	public double value;
	public double baseValue;
	public boolean skillMastery = false;
	private L2Character character;
	private L2Character _target;
	
	public L2Character getCharacter()
	{
		return character;
	}
	
	public L2PcInstance getPlayer()
	{
		return character == null ? null : character.getActingPlayer();
	}
	
	public L2Character getTarget()
	{
		return _target;
	}
}
