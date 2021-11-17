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
package org.l2jmobius.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * 01 // Packet Identifier<br>
 * c6 37 50 40 // ObjectId<br>
 * <br>
 * 01 00 // Number of Attribute Trame of the Packet<br>
 * <br>
 * c6 37 50 40 // Attribute Identifier : 01-Level, 02-Experience, 03-STR, 04-DEX, 05-CON, 06-INT, 07-WIT, 08-MEN, 09-Current HP, 0a, Max HP...<br>
 * cd 09 00 00 // Attribute Value<br>
 * format d d(dd)
 * @version $Revision: 1.3.2.1.2.5 $ $Date: 2005/03/27 15:29:39 $
 */
public class StatusUpdate implements IClientOutgoingPacket
{
	public static final int LEVEL = 0x01;
	public static final int EXP = 0x02;
	public static final int STR = 0x03;
	public static final int DEX = 0x04;
	public static final int CON = 0x05;
	public static final int INT = 0x06;
	public static final int WIT = 0x07;
	public static final int MEN = 0x08;
	
	public static final int CUR_HP = 0x09;
	public static final int MAX_HP = 0x0a;
	public static final int CUR_MP = 0x0b;
	public static final int MAX_MP = 0x0c;
	
	public static final int SP = 0x0d;
	public static final int CUR_LOAD = 0x0e;
	public static final int MAX_LOAD = 0x0f;
	
	public static final int P_ATK = 0x11;
	public static final int ATK_SPD = 0x12;
	public static final int P_DEF = 0x13;
	public static final int EVASION = 0x14;
	public static final int ACCURACY = 0x15;
	public static final int CRITICAL = 0x16;
	public static final int M_ATK = 0x17;
	public static final int CAST_SPD = 0x18;
	public static final int M_DEF = 0x19;
	public static final int PVP_FLAG = 0x1a;
	public static final int KARMA = 0x1b;
	
	public static final int CUR_CP = 0x21;
	public static final int MAX_CP = 0x22;
	
	private Player _actor;
	
	private List<Attribute> _attributes;
	public int _objectId;
	
	class Attribute
	{
		// id values 09 - current health 0a - max health 0b - current mana 0c - max mana
		public int id;
		public int value;
		
		Attribute(int pId, int pValue)
		{
			id = pId;
			value = pValue;
		}
	}
	
	public StatusUpdate(Player actor)
	{
		_actor = actor;
	}
	
	public StatusUpdate(int objectId)
	{
		_attributes = new ArrayList<>();
		_objectId = objectId;
	}
	
	public void addAttribute(int id, int level)
	{
		_attributes.add(new Attribute(id, level));
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.STATUS_UPDATE.writeId(packet);
		
		if (_actor != null)
		{
			packet.writeD(_actor.getObjectId());
			packet.writeD(28); // all the attributes
			
			packet.writeD(LEVEL);
			packet.writeD(_actor.getLevel());
			packet.writeD(EXP);
			packet.writeD((int) _actor.getExp());
			packet.writeD(STR);
			packet.writeD(_actor.getSTR());
			packet.writeD(DEX);
			packet.writeD(_actor.getDEX());
			packet.writeD(CON);
			packet.writeD(_actor.getCON());
			packet.writeD(INT);
			packet.writeD(_actor.getINT());
			packet.writeD(WIT);
			packet.writeD(_actor.getWIT());
			packet.writeD(MEN);
			packet.writeD(_actor.getMEN());
			
			packet.writeD(CUR_HP);
			packet.writeD((int) _actor.getCurrentHp());
			packet.writeD(MAX_HP);
			packet.writeD(_actor.getMaxHp());
			packet.writeD(CUR_MP);
			packet.writeD((int) _actor.getCurrentMp());
			packet.writeD(MAX_MP);
			packet.writeD(_actor.getMaxMp());
			packet.writeD(SP);
			packet.writeD(_actor.getSp());
			packet.writeD(CUR_LOAD);
			packet.writeD(_actor.getCurrentLoad());
			packet.writeD(MAX_LOAD);
			packet.writeD(_actor.getMaxLoad());
			
			packet.writeD(P_ATK);
			packet.writeD(_actor.getPAtk(null));
			packet.writeD(ATK_SPD);
			packet.writeD(_actor.getPAtkSpd());
			packet.writeD(P_DEF);
			packet.writeD(_actor.getPDef(null));
			packet.writeD(EVASION);
			packet.writeD(_actor.getEvasionRate(null));
			packet.writeD(ACCURACY);
			packet.writeD(_actor.getAccuracy());
			packet.writeD(CRITICAL);
			packet.writeD(_actor.getCriticalHit(null, null));
			packet.writeD(M_ATK);
			packet.writeD(_actor.getMAtk(null, null));
			
			packet.writeD(CAST_SPD);
			packet.writeD(_actor.getMAtkSpd());
			packet.writeD(M_DEF);
			packet.writeD(_actor.getMDef(null, null));
			packet.writeD(PVP_FLAG);
			packet.writeD(_actor.getPvpFlag());
			packet.writeD(KARMA);
			packet.writeD(_actor.getKarma());
			packet.writeD(CUR_CP);
			packet.writeD((int) _actor.getCurrentCp());
			packet.writeD(MAX_CP);
			packet.writeD(_actor.getMaxCp());
		}
		else
		{
			packet.writeD(_objectId);
			packet.writeD(_attributes.size());
			
			for (int i = 0; i < _attributes.size(); i++)
			{
				final Attribute temp = _attributes.get(i);
				packet.writeD(temp.id);
				packet.writeD(temp.value);
			}
		}
		return true;
	}
}
