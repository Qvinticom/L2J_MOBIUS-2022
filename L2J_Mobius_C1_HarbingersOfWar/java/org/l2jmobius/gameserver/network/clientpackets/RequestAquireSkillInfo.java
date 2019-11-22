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
package org.l2jmobius.gameserver.network.clientpackets;

import java.io.IOException;

import org.l2jmobius.gameserver.data.SkillTable;
import org.l2jmobius.gameserver.data.SkillTreeTable;
import org.l2jmobius.gameserver.model.Skill;
import org.l2jmobius.gameserver.model.SkillLearn;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.ClientThread;
import org.l2jmobius.gameserver.network.Connection;
import org.l2jmobius.gameserver.network.serverpackets.AquireSkillInfo;

public class RequestAquireSkillInfo extends ClientBasePacket
{
	private static final String _C__6B_REQUESTAQUIRESKILLINFO = "[C] 6B RequestAquireSkillInfo";
	
	public RequestAquireSkillInfo(byte[] rawPacket, ClientThread client) throws IOException
	{
		super(rawPacket);
		PlayerInstance activeChar = client.getActiveChar();
		Connection con = client.getConnection();
		int id = readD();
		int level = readD();
		Skill skill = SkillTable.getInstance().getInfo(id, level);
		int requiredSp = 0;
		for (SkillLearn skill2 : SkillTreeTable.getInstance().getAvailableSkills(activeChar))
		{
			if (skill2.getId() != id)
			{
				continue;
			}
			requiredSp = skill2.getSpCost();
			break;
		}
		AquireSkillInfo asi = new AquireSkillInfo(skill.getId(), skill.getLevel(), requiredSp);
		con.sendPacket(asi);
	}
	
	@Override
	public String getType()
	{
		return _C__6B_REQUESTAQUIRESKILLINFO;
	}
}
