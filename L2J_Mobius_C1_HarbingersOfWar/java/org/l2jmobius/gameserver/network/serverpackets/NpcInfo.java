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

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.model.actor.instance.MonsterInstance;
import org.l2jmobius.gameserver.model.actor.instance.NpcInstance;
import org.l2jmobius.gameserver.model.actor.instance.PetInstance;

public class NpcInfo extends ServerBasePacket
{
	private NpcInstance _cha;
	private PetInstance _chaPet;
	protected String _title = "";
	
	public NpcInfo(NpcInstance cha)
	{
		_cha = cha;
	}
	
	public NpcInfo(PetInstance cha)
	{
		_chaPet = cha;
	}
	
	@Override
	public void writeImpl()
	{
		if (_chaPet == null)
		{
			writeC(0x22);
			writeD(_cha.getObjectId());
			writeD(_cha.getNpcTemplate().getNpcId() + 1000000);
			if (_cha.isAutoAttackable())
			{
				writeD(1);
			}
			else
			{
				writeD(0);
			}
			writeD(_cha.getX());
			writeD(_cha.getY());
			writeD(_cha.getZ());
			writeD(_cha.getHeading());
			writeD(0);
			writeD(_cha.getMagicalSpeed());
			writeD(_cha.getPhysicalSpeed());
			writeD(_cha.getRunSpeed());
			writeD(_cha.getWalkSpeed());
			writeD(50);
			writeD(50);
			writeD(_cha.getFloatingRunSpeed());
			writeD(_cha.getFloatingWalkSpeed());
			writeD(_cha.getFlyingRunSpeed());
			writeD(_cha.getFlyingWalkSpeed());
			writeF(_cha.getMovementMultiplier());
			writeF(_cha.getAttackSpeedMultiplier());
			writeF(_cha.getCollisionRadius());
			writeF(_cha.getCollisionHeight());
			writeD(_cha.getRightHandItem());
			writeD(0);
			writeD(_cha.getLeftHandItem());
			writeC(1);
			if (_cha.isRunning())
			{
				writeC(1);
			}
			else
			{
				writeC(0);
			}
			if (_cha.isInCombat())
			{
				writeC(1);
			}
			else
			{
				writeC(0);
			}
			if (_cha.isDead())
			{
				writeC(1);
			}
			else
			{
				writeC(0);
			}
			writeC(0);
			writeS(_cha.getName());
			
			_title = _cha.getTitle();
			// Custom level titles
			if (_cha.isMonster() && (Config.SHOW_NPC_LVL || Config.SHOW_NPC_AGGRESSION))
			{
				String t1 = "";
				if (Config.SHOW_NPC_LVL)
				{
					t1 += "Lv " + _cha.getLevel();
				}
				String t2 = "";
				if (Config.SHOW_NPC_AGGRESSION)
				{
					if (!t1.isEmpty())
					{
						t2 += " ";
					}
					if (((MonsterInstance) _cha).isAggressive())
					{
						t2 += "[A]"; // Aggressive.
					}
				}
				t1 += t2;
				if ((_title != null) && !_title.isEmpty())
				{
					t1 += " " + _title;
				}
				_title = t1;
			}
			
			writeS(_title);
			writeD(0);
			writeD(0);
			writeD(0);
		}
		else
		{
			writeC(0x22);
			writeD(_chaPet.getObjectId());
			writeD(_chaPet.getNpcId() + 1000000);
			if (_chaPet.getKarma() > 0)
			{
				writeD(1);
			}
			else
			{
				writeD(0);
			}
			writeD(_chaPet.getX());
			writeD(_chaPet.getY());
			writeD(_chaPet.getZ());
			writeD(_chaPet.getHeading());
			writeD(0);
			writeD(_chaPet.getMagicalSpeed());
			writeD(_chaPet.getPhysicalSpeed());
			writeD(_chaPet.getRunSpeed());
			writeD(_chaPet.getWalkSpeed());
			writeD(0);
			writeD(50);
			writeD(_chaPet.getFloatingRunSpeed());
			writeD(_chaPet.getFloatingWalkSpeed());
			writeD(_chaPet.getFlyingRunSpeed());
			writeD(_chaPet.getFlyingWalkSpeed());
			writeF(_chaPet.getMovementMultiplier());
			writeF(_chaPet.getAttackSpeedMultiplier());
			writeF(_chaPet.getCollisionRadius());
			writeF(_chaPet.getCollisionHeight());
			writeD(0);
			writeD(0);
			writeD(0);
			writeC(1);
			if (_chaPet.isRunning())
			{
				writeC(1);
			}
			else
			{
				writeC(0);
			}
			if (_chaPet.isInCombat())
			{
				writeC(1);
			}
			else
			{
				writeC(0);
			}
			if (_chaPet.isDead())
			{
				writeC(1);
			}
			else
			{
				writeC(0);
			}
			if (_chaPet.getOwner() == null)
			{
				writeC(2);
			}
			else
			{
				writeC(1);
			}
			writeS(_chaPet.getName());
			writeS(_chaPet.getTitle());
			writeD(0);
			writeD(0);
			writeD(0);
		}
	}
}
