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
package org.l2jmobius.gameserver.model.actor.instance;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.ai.CtrlIntention;
import org.l2jmobius.gameserver.data.SkillTable;
import org.l2jmobius.gameserver.data.sql.AnnouncementsTable;
import org.l2jmobius.gameserver.instancemanager.CoupleManager;
import org.l2jmobius.gameserver.model.Skill;
import org.l2jmobius.gameserver.model.Wedding;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.templates.NpcTemplate;
import org.l2jmobius.gameserver.model.itemcontainer.Inventory;
import org.l2jmobius.gameserver.model.items.instance.Item;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import org.l2jmobius.gameserver.network.serverpackets.MagicSkillUse;
import org.l2jmobius.gameserver.network.serverpackets.MyTargetSelected;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jmobius.gameserver.network.serverpackets.ValidateLocation;

public class WeddingManager extends Npc
{
	/**
	 * Instantiates a new wedding manager instance.
	 * @param objectId the object id
	 * @param template the template
	 * @author evill33t & squeezed
	 */
	public WeddingManager(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onAction(Player player)
	{
		if (!canTarget(player))
		{
			return;
		}
		
		// Check if the Player already target the Npc
		if (this != player.getTarget())
		{
			// Set the target of the Player player
			player.setTarget(this);
			
			// Send a Server->Client packet MyTargetSelected to the Player player
			player.sendPacket(new MyTargetSelected(getObjectId(), 0));
			
			// Send a Server->Client packet ValidateLocation to correct the Npc position and heading on the client
			player.sendPacket(new ValidateLocation(this));
		}
		else if (!canInteract(player)) // Calculate the distance between the Player and the Npc
		{
			// Notify the Player AI with AI_INTENTION_INTERACT
			player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
		}
		else
		{
			showMessageWindow(player);
		}
		// Send a Server->Client ActionFailed to the Player in order to avoid that the client wait another packet
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	/**
	 * Show message window.
	 * @param player the player
	 */
	private void showMessageWindow(Player player)
	{
		final String filename = "data/html/mods/Wedding_start.htm";
		final String replace = String.valueOf(Config.WEDDING_PRICE);
		final NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setFile(filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%replace%", replace);
		html.replace("%npcname%", getName());
		player.sendPacket(html);
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		// standard msg
		String filename = "data/html/mods/Wedding_start.htm";
		String replace = "";
		
		// if player has no partner
		if (player.getPartnerId() == 0)
		{
			filename = "data/html/mods/Wedding_nopartner.htm";
			sendHtmlMessage(player, filename, replace);
			return;
		}
		
		final Player ptarget = (Player) World.getInstance().findObject(player.getPartnerId());
		// partner online ?
		if ((ptarget == null) || !ptarget.isOnline())
		{
			filename = "data/html/mods/Wedding_notfound.htm";
			sendHtmlMessage(player, filename, replace);
			return;
		}
		
		// already married ?
		if (player.isMarried())
		{
			filename = "data/html/mods/Wedding_already.htm";
			sendHtmlMessage(player, filename, replace);
			return;
		}
		else if (player.isMarryAccepted())
		{
			filename = "data/html/mods/Wedding_waitforpartner.htm";
			sendHtmlMessage(player, filename, replace);
			return;
		}
		else if (command.startsWith("AcceptWedding"))
		{
			// accept the wedding request
			player.setMarryAccepted(true);
			
			int type;
			if (player.getAppearance().isFemale() && ptarget.getAppearance().isFemale())
			{
				player.getAppearance().setNameColor(Config.WEDDING_NAME_COLOR_LESBO);
				ptarget.getAppearance().setNameColor(Config.WEDDING_NAME_COLOR_LESBO);
				type = 1;
			}
			else if (!player.getAppearance().isFemale() && !ptarget.getAppearance().isFemale())
			{
				player.getAppearance().setNameColor(Config.WEDDING_NAME_COLOR_GEY);
				ptarget.getAppearance().setNameColor(Config.WEDDING_NAME_COLOR_GEY);
				type = 2;
			}
			else
			{
				player.getAppearance().setNameColor(Config.WEDDING_NAME_COLOR_NORMAL);
				ptarget.getAppearance().setNameColor(Config.WEDDING_NAME_COLOR_NORMAL);
				type = 0;
			}
			
			final Wedding wedding = CoupleManager.getInstance().getCouple(player.getCoupleId());
			wedding.marry(type);
			
			// messages to the couple
			player.sendMessage("Congratulations you are married!");
			player.setMarried(true);
			player.setMaryRequest(false);
			player.setmarriedType(type);
			ptarget.sendMessage("Congratulations you are married!");
			ptarget.setMarried(true);
			ptarget.setMaryRequest(false);
			ptarget.setmarriedType(type);
			
			if (Config.GIVE_CUPID_BOW)
			{
				player.addItem("Cupids Bow", 9140, 1, player, true);
				player.getInventory().updateDatabase();
				ptarget.addItem("Cupids Bow", 9140, 1, ptarget, true);
				ptarget.getInventory().updateDatabase();
				player.sendSkillList();
				ptarget.sendSkillList();
			}
			
			// wedding march
			MagicSkillUse msu = new MagicSkillUse(player, player, 2230, 1, 1, 0);
			player.broadcastPacket(msu);
			msu = new MagicSkillUse(ptarget, ptarget, 2230, 1, 1, 0);
			ptarget.broadcastPacket(msu);
			
			// fireworks
			final Skill skill = SkillTable.getInstance().getSkill(2025, 1);
			if (skill != null)
			{
				msu = new MagicSkillUse(player, player, 2025, 1, 1, 0);
				player.sendPacket(msu);
				player.broadcastPacket(msu);
				player.useMagic(skill, false, false);
				msu = new MagicSkillUse(ptarget, ptarget, 2025, 1, 1, 0);
				ptarget.sendPacket(msu);
				ptarget.broadcastPacket(msu);
				ptarget.useMagic(skill, false, false);
			}
			
			if (Config.ANNOUNCE_WEDDING)
			{
				AnnouncementsTable.getInstance().announceToAll("Congratulations to " + player.getName() + " and " + ptarget.getName() + "! They have been married.");
			}
			
			filename = "data/html/mods/Wedding_accepted.htm";
			replace = ptarget.getName();
			sendHtmlMessage(ptarget, filename, replace);
			return;
		}
		else if (command.startsWith("DeclineWedding"))
		{
			player.setMaryRequest(false);
			ptarget.setMaryRequest(false);
			player.setMarryAccepted(false);
			ptarget.setMarryAccepted(false);
			player.getAppearance().setNameColor(0xFFFFFF);
			ptarget.getAppearance().setNameColor(0xFFFFFF);
			player.sendMessage("You declined");
			ptarget.sendMessage("Your partner declined");
			replace = ptarget.getName();
			filename = "data/html/mods/Wedding_declined.htm";
			sendHtmlMessage(ptarget, filename, replace);
			return;
		}
		else if (player.isMaryRequest())
		{
			// check for formalwear
			if (Config.WEDDING_FORMALWEAR)
			{
				final Inventory inv3 = player.getInventory();
				final Item item3 = inv3.getPaperdollItem(10);
				if (item3 == null)
				{
					player.setWearingFormalWear(false);
				}
				else
				{
					final String strItem = Integer.toString(item3.getItemId());
					final String frmWear = Integer.toString(6408);
					player.sendMessage(strItem);
					player.setWearingFormalWear(strItem.equals(frmWear));
				}
			}
			
			if (Config.WEDDING_FORMALWEAR && !player.isWearingFormalWear())
			{
				filename = "data/html/mods/Wedding_noformal.htm";
				sendHtmlMessage(player, filename, replace);
				return;
			}
			
			filename = "data/html/mods/Wedding_ask.htm";
			player.setMaryRequest(false);
			ptarget.setMaryRequest(false);
			replace = ptarget.getName();
			sendHtmlMessage(player, filename, replace);
			return;
		}
		else if (command.startsWith("AskWedding"))
		{
			// check for formalwear
			if (Config.WEDDING_FORMALWEAR)
			{
				final Inventory inv3 = player.getInventory();
				final Item item3 = inv3.getPaperdollItem(10);
				if (null == item3)
				{
					player.setWearingFormalWear(false);
				}
				else
				{
					final String frmWear = Integer.toString(6408);
					String strItem = null;
					strItem = Integer.toString(item3.getItemId());
					player.setWearingFormalWear((null != strItem) && strItem.equals(frmWear));
				}
			}
			
			if (Config.WEDDING_FORMALWEAR && !player.isWearingFormalWear())
			{
				filename = "data/html/mods/Wedding_noformal.htm";
				sendHtmlMessage(player, filename, replace);
				return;
			}
			else if (player.getAdena() < Config.WEDDING_PRICE)
			{
				filename = "data/html/mods/Wedding_adena.htm";
				replace = String.valueOf(Config.WEDDING_PRICE);
				sendHtmlMessage(player, filename, replace);
				return;
			}
			else
			{
				player.setMarryAccepted(true);
				ptarget.setMaryRequest(true);
				replace = ptarget.getName();
				filename = "data/html/mods/Wedding_requested.htm";
				player.getInventory().reduceAdena("Wedding", Config.WEDDING_PRICE, player, player.getLastFolkNPC());
				sendHtmlMessage(player, filename, replace);
				return;
			}
		}
		sendHtmlMessage(player, filename, replace);
	}
	
	/**
	 * Send html message.
	 * @param player the player
	 * @param filename the filename
	 * @param replace the replace
	 */
	private void sendHtmlMessage(Player player, String filename, String replace)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setFile(filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%replace%", replace);
		html.replace("%npcname%", getName());
		player.sendPacket(html);
	}
}
