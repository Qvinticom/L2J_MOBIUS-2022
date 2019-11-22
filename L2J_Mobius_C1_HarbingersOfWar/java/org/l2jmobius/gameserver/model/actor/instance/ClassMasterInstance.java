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
package org.l2jmobius.gameserver.model.actor.instance;

import org.l2jmobius.gameserver.enums.CreatureState;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import org.l2jmobius.gameserver.network.serverpackets.CharInfo;
import org.l2jmobius.gameserver.network.serverpackets.MyTargetSelected;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jmobius.gameserver.network.serverpackets.SetToLocation;
import org.l2jmobius.gameserver.network.serverpackets.UserInfo;
import org.l2jmobius.gameserver.templates.Npc;

public class ClassMasterInstance extends NpcInstance
{
	public ClassMasterInstance(Npc template)
	{
		super(template);
	}
	
	@Override
	public void onAction(PlayerInstance player)
	{
		if (getObjectId() != player.getTargetId())
		{
			player.setCurrentState(CreatureState.IDLE);
			player.setTarget(this);
			MyTargetSelected my = new MyTargetSelected(getObjectId(), player.getLevel() - getLevel());
			player.sendPacket(my);
			player.sendPacket(new SetToLocation(this));
		}
		else
		{
			int classId = player.getClassId();
			int jobLevel = 0;
			int level = player.getLevel();
			switch (classId)
			{
				case 0:
				case 10:
				case 18:
				case 25:
				case 31:
				case 38:
				case 44:
				case 49:
				case 53:
				{
					jobLevel = 1;
					break;
				}
				case 1:
				case 4:
				case 7:
				case 11:
				case 15:
				case 19:
				case 22:
				case 26:
				case 29:
				case 32:
				case 35:
				case 39:
				case 42:
				case 45:
				case 47:
				case 50:
				case 54:
				case 56:
				{
					jobLevel = 2;
					break;
				}
				default:
				{
					jobLevel = 3;
				}
			}
			if (((level >= 20) && (jobLevel == 1)) || ((level >= 40) && (jobLevel == 2)))
			{
				showChatWindow(player, classId);
			}
			else
			{
				NpcHtmlMessage html = new NpcHtmlMessage(1);
				switch (jobLevel)
				{
					case 1:
					{
						html.setHtml("<html><head><body>Come back here when you reached level 20.</body></html>");
						break;
					}
					case 2:
					{
						html.setHtml("<html><head><body>Come back here when you reached level 40.</body></html>");
						break;
					}
					case 3:
					{
						html.setHtml("<html><head><body>There is nothing more you can learn.</body></html>");
					}
				}
				player.sendPacket(html);
			}
			player.sendPacket(new ActionFailed());
		}
	}
	
	@Override
	public String getHtmlPath(int npcId, int val)
	{
		return "data/html/classmaster/" + val + ".htm";
	}
	
	@Override
	public void onBypassFeedback(PlayerInstance player, String command)
	{
		if (command.startsWith("change_class"))
		{
			int val = Integer.parseInt(command.substring(13));
			changeClass(player, val);
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
	
	private void changeClass(PlayerInstance player, int val)
	{
		player.setClassId(val);
		UserInfo ui = new UserInfo(player);
		player.sendPacket(ui);
		CharInfo info = new CharInfo(player);
		player.broadcastPacket(info);
	}
}
