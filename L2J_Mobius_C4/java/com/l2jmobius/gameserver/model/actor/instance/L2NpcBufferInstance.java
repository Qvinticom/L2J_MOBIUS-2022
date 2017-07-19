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
package com.l2jmobius.gameserver.model.actor.instance;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.Olympiad;
import com.l2jmobius.gameserver.cache.HtmCache;
import com.l2jmobius.gameserver.datatables.NpcBufferTable;
import com.l2jmobius.gameserver.datatables.SkillTable;
import com.l2jmobius.gameserver.model.L2Character;
import com.l2jmobius.gameserver.model.L2ItemInstance;
import com.l2jmobius.gameserver.model.L2Skill;
import com.l2jmobius.gameserver.model.entity.TvTEvent;
import com.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import com.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import com.l2jmobius.gameserver.taskmanager.AttackStanceTaskManager;
import com.l2jmobius.gameserver.templates.L2NpcTemplate;

public class L2NpcBufferInstance extends L2FolkInstance
{
	public L2NpcBufferInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void showChatWindow(L2PcInstance playerInstance, int val)
	{
		if (playerInstance == null)
		{
			return;
		}
		
		String htmContent = HtmCache.getInstance().getHtm("data/html/npcbuffer/NpcBuffer.htm");
		if (!Config.NPC_BUFFER_ENABLED)
		{
			htmContent = HtmCache.getInstance().getHtm("data/html/npcdefault.htm");
		}
		else if (val > 0)
		{
			htmContent = HtmCache.getInstance().getHtm("data/html/npcbuffer/NpcBuffer-" + val + ".htm");
		}
		
		if (htmContent != null)
		{
			final NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(getObjectId());
			
			npcHtmlMessage.setHtml(htmContent);
			npcHtmlMessage.replace("%objectId%", String.valueOf(getObjectId()));
			playerInstance.sendPacket(npcHtmlMessage);
		}
		playerInstance.sendPacket(new ActionFailed());
	}
	
	int pageVal = 0;
	
	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		if ((player == null) || (player.getLastFolkNPC() == null) || (player.getLastFolkNPC().getObjectId() != getObjectId()))
		{
			return;
		}
		
		if (Olympiad.getInstance().isRegisteredInComp(player))
		{
			return;
		}
		
		if ((player.getEventTeam() > 0) || TvTEvent.isRegistered(player))
		{
			return;
		}
		
		if (command.startsWith("Chat"))
		
		{
			
			final int val = Integer.parseInt(command.substring(5));
			
			pageVal = val;
			
			showChatWindow(player, val);
			
		}
		else if (command.startsWith("Buff") || command.startsWith("PetBuff"))
		{
			L2Character target = player;
			if (command.startsWith("Pet"))
			{
				if (player.getPet() == null)
				{
					player.sendMessage("You do not have a pet.");
					showChatWindow(player, 0); // 0 = main window
					return;
				}
				target = player.getPet();
			}
			
			final String[] buffGroupArray = command.substring(command.indexOf("Buff") + 5).split(" ");
			
			for (final String buffGroupList : buffGroupArray)
			{
				if (buffGroupList == null)
				{
					_log.warning("NPC Buffer Warning: NPC Buffer has no buffGroup set in the bypass for the buff selected.");
					return;
				}
				
				final int buffGroup = Integer.parseInt(buffGroupList);
				final int[] buffGroupInfo = NpcBufferTable.getInstance().getSkillGroupInfo(buffGroup);
				
				if (buffGroupInfo == null)
				{
					_log.warning("Player: " + player.getName() + " has tried to use skill group (" + buffGroup + ") not assigned to the NPC Buffer!");
					
					return;
				}
				
				final int skillId = buffGroupInfo[0];
				final int skillLevel = buffGroupInfo[1];
				final int skillFeeId = buffGroupInfo[2];
				final int skillFeeAmount = buffGroupInfo[3];
				
				if (skillFeeId != 0)
				{
					final L2ItemInstance itemInstance = player.getInventory().getItemByItemId(skillFeeId);
					if ((itemInstance == null) || (!itemInstance.isStackable() && (player.getInventory().getInventoryItemCount(skillFeeId, -1) < skillFeeAmount)))
					{
						final SystemMessage sm = new SystemMessage(SystemMessage.NOT_ENOUGH_ITEMS);
						player.sendPacket(sm);
						continue;
					}
					
					if (itemInstance.isStackable())
					{
						if (!player.destroyItemByItemId("Npc Buffer", skillFeeId, skillFeeAmount, player.getTarget(), true))
						{
							final SystemMessage sm = new SystemMessage(SystemMessage.NOT_ENOUGH_ITEMS);
							player.sendPacket(sm);
							continue;
						}
					}
					else
					{
						for (int i = 0; i < skillFeeAmount; ++i)
						{
							player.destroyItemByItemId("Npc Buffer", skillFeeId, 1, player.getTarget(), true);
						}
					}
				}
				
				final L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLevel);
				if (skill != null)
				{
					skill.getEffects(this, target);
				}
			}
			showChatWindow(player, pageVal);
		}
		else if (command.startsWith("Heal"))
		{
			if (!player.isInCombat() && !AttackStanceTaskManager.getInstance().getAttackStanceTask(player))
			{
				final String[] healArray = command.substring(5).split(" ");
				for (final String healType : healArray)
				{
					if (healType.equalsIgnoreCase("HP"))
					{
						player.setCurrentHp(player.getMaxHp());
					}
					else if (healType.equalsIgnoreCase("MP"))
					{
						player.setCurrentMp(player.getMaxMp());
					}
					else if (healType.equalsIgnoreCase("CP"))
					{
						player.setCurrentCp(player.getMaxCp());
					}
				}
			}
			showChatWindow(player, 0);
		}
		else if (command.startsWith("RemoveBuffs"))
		{
			player.stopAllEffects();
			showChatWindow(player, 0);
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
	
	@Override
	public boolean isAIOBuffer()
	{
		return true;
	}
}