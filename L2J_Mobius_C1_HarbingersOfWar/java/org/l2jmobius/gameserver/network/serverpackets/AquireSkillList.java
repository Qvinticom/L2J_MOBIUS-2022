/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.l2jmobius.gameserver.network.serverpackets;

import java.util.Vector;

public class AquireSkillList extends ServerBasePacket
{
	private static final String _S__A3_AQUIRESKILLLIST = "[S] A3 AquireSkillList";
	private final Vector<Skill> _skills = new Vector<>();
	
	public void addSkill(int id, int nextLevel, int maxLevel, int spCost, int requirements)
	{
		_skills.add(new Skill(id, nextLevel, maxLevel, spCost, requirements));
	}
	
	@Override
	public byte[] getContent()
	{
		writeC(163);
		writeD(_skills.size());
		for (int i = 0; i < _skills.size(); ++i)
		{
			Skill temp = _skills.get(i);
			writeD(temp.id);
			writeD(temp.nextLevel);
			writeD(temp.maxLevel);
			writeD(temp.spCost);
			writeD(temp.requirements);
		}
		return getBytes();
	}
	
	@Override
	public String getType()
	{
		return _S__A3_AQUIRESKILLLIST;
	}
	
	class Skill
	{
		public int id;
		public int nextLevel;
		public int maxLevel;
		public int spCost;
		public int requirements;
		
		Skill(int id, int nextLevel, int maxLevel, int spCost, int requirements)
		{
			this.id = id;
			this.nextLevel = nextLevel;
			this.maxLevel = maxLevel;
			this.spCost = spCost;
			this.requirements = requirements;
		}
	}
	
}
