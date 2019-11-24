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

import java.util.Collection;

import org.l2jmobius.gameserver.data.SkillTable;
import org.l2jmobius.gameserver.data.SkillTreeTable;
import org.l2jmobius.gameserver.model.ShortCut;
import org.l2jmobius.gameserver.model.Skill;
import org.l2jmobius.gameserver.model.SkillLearn;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.actor.instance.TrainerInstance;
import org.l2jmobius.gameserver.network.ClientThread;
import org.l2jmobius.gameserver.network.serverpackets.AquireSkillList;
import org.l2jmobius.gameserver.network.serverpackets.ShortCutRegister;
import org.l2jmobius.gameserver.network.serverpackets.StatusUpdate;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

public class RequestAquireSkill extends ClientBasePacket
{
	private static final String _C__6C_REQUESTAQUIRESKILL = "[C] 6C RequestAquireSkill";
	
	public RequestAquireSkill(byte[] rawPacket, ClientThread client)
	{
		super(rawPacket);
		final int id = readD();
		final int level = readD();
		
		final PlayerInstance player = client.getActiveChar();
		
		// Prevent learning skills far from trainer.
		if (!(player.getTarget() instanceof TrainerInstance))
		{
			return;
		}
		boolean found = false;
		for (WorldObject object : player.getKnownObjects())
		{
			if ((object instanceof TrainerInstance) && (player.calculateDistance2D(object) < 250))
			{
				found = true;
				break;
			}
		}
		if (!found)
		{
			return;
		}
		
		final Skill skill = SkillTable.getInstance().getInfo(id, level);
		Collection<SkillLearn> skills = SkillTreeTable.getInstance().getAvailableSkills(player);
		
		int requiredSp = 0;
		for (SkillLearn skillLearn : skills)
		{
			if (skillLearn.getId() != id)
			{
				continue;
			}
			requiredSp = skillLearn.getSpCost();
			break;
		}
		if (player.getSp() >= requiredSp)
		{
			player.addSkill(skill);
			player.setSp(player.getSp() - requiredSp);
			final StatusUpdate su = new StatusUpdate(player.getObjectId());
			su.addAttribute(StatusUpdate.SP, player.getSp());
			player.sendPacket(su);
			final SystemMessage sm = new SystemMessage(SystemMessage.LEARNED_SKILL_S1);
			sm.addSkillName(id);
			player.sendPacket(sm);
			if (level > 1)
			{
				for (int i = 0; i <= 100; ++i)
				{
					if ((player.getShortCut(i) == null) || (player.getShortCut(i).getId() != id) || (player.getShortCut(i).getType() != 2))
					{
						continue;
					}
					player.sendPacket(new ShortCutRegister(i, player.getShortCut(i).getType(), id, level, player.getShortCut(i).getUnk()));
					player.registerShortCut(new ShortCut(i, player.getShortCut(i).getType(), id, level, player.getShortCut(i).getUnk()));
				}
			}
		}
		else
		{
			player.sendPacket(new SystemMessage(SystemMessage.NOT_ENOUGH_SP_TO_LEARN_SKILL));
		}
		skills = SkillTreeTable.getInstance().getAvailableSkills(player);
		final AquireSkillList asl = new AquireSkillList();
		for (SkillLearn skillLearn : skills)
		{
			asl.addSkill(skillLearn.getId(), skillLearn.getLevel(), skillLearn.getLevel(), skillLearn.getSpCost(), 0);
		}
		player.sendPacket(asl);
	}
	
	@Override
	public String getType()
	{
		return _C__6C_REQUESTAQUIRESKILL;
	}
}
