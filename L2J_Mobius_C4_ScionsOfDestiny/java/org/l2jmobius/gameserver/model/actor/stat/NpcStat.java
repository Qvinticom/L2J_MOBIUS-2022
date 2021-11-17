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
package org.l2jmobius.gameserver.model.actor.stat;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.skill.Stat;

public class NpcStat extends CreatureStat
{
	public NpcStat(Npc activeChar)
	{
		super(activeChar);
		
		setLevel(getActiveChar().getTemplate().getLevel());
	}
	
	@Override
	public Npc getActiveChar()
	{
		return (Npc) super.getActiveChar();
	}
	
	@Override
	public int getMaxHp()
	{
		return (int) calcStat(Stat.MAX_HP, getActiveChar().getTemplate().getBaseHpMax(), null, null);
	}
}
