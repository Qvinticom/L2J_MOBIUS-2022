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

public class ShortCutInit extends ServerBasePacket
{
	private static final String _S__57_SHORTCUTINIT = "[S] 57 ShortCutInit";
	private final Vector<ShortCut> _shortCuts = new Vector<>();
	
	public void addSkillShotCut(int slot, int skillId, int level, int dat2)
	{
		_shortCuts.add(new ShortCut(slot, 2, skillId, level, dat2));
	}
	
	public void addItemShotCut(int slot, int inventoryId, int dat2)
	{
		_shortCuts.add(new ShortCut(slot, 1, inventoryId, -1, dat2));
	}
	
	public void addActionShotCut(int slot, int actionId, int dat2)
	{
		_shortCuts.add(new ShortCut(slot, 3, actionId, -1, dat2));
	}
	
	@Override
	public byte[] getContent()
	{
		writeC(87);
		writeD(_shortCuts.size());
		for (int i = 0; i < _shortCuts.size(); ++i)
		{
			final ShortCut temp = _shortCuts.get(i);
			writeD(temp.type);
			writeD(temp.slot);
			writeD(temp.typeId);
			if (temp.level > -1)
			{
				writeD(temp.level);
			}
			writeD(temp.dat2);
		}
		return getBytes();
	}
	
	@Override
	public String getType()
	{
		return _S__57_SHORTCUTINIT;
	}
	
	class ShortCut
	{
		public int slot;
		public int type;
		public int typeId;
		public int level;
		public int dat2;
		
		ShortCut(int slot, int type, int typeId, int level, int dat2)
		{
			this.slot = slot;
			this.type = type;
			this.typeId = typeId;
			this.level = level;
			this.dat2 = dat2;
		}
	}
	
}
