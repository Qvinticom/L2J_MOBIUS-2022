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
package org.l2jmobius.gameserver.model;

import java.util.ArrayList;
import java.util.List;

import org.l2jmobius.gameserver.model.actor.instance.ItemInstance;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.serverpackets.InventoryUpdate;
import org.l2jmobius.gameserver.network.serverpackets.PartySmallWindowAdd;
import org.l2jmobius.gameserver.network.serverpackets.PartySmallWindowAll;
import org.l2jmobius.gameserver.network.serverpackets.PartySmallWindowDelete;
import org.l2jmobius.gameserver.network.serverpackets.PartySmallWindowDeleteAll;
import org.l2jmobius.gameserver.network.serverpackets.ServerBasePacket;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.network.serverpackets.UserInfo;
import org.l2jmobius.util.Rnd;

public class Party
{
	private final List<PlayerInstance> _members = new ArrayList<>();
	private boolean _randomLoot = false;
	private int _partyLvl = 0;
	
	public Party(PlayerInstance leader, boolean randomLoot)
	{
		_randomLoot = randomLoot;
		_members.add(leader);
		_partyLvl = leader.getLevel() * leader.getLevel();
	}
	
	public int getMemberCount()
	{
		return _members.size();
	}
	
	public List<PlayerInstance> getPartyMembers()
	{
		return _members;
	}
	
	private PlayerInstance getRandomMember()
	{
		return _members.get(Rnd.get(_members.size()));
	}
	
	public boolean isLeader(PlayerInstance player)
	{
		return _members.get(0).equals(player);
	}
	
	public void broadcastToPartyMembers(ServerBasePacket msg)
	{
		for (PlayerInstance member : _members)
		{
			member.sendPacket(msg);
		}
	}
	
	public void broadcastToPartyMembers(PlayerInstance player, ServerBasePacket msg)
	{
		for (PlayerInstance member : _members)
		{
			if (member.equals(player))
			{
				continue;
			}
			member.sendPacket(msg);
		}
	}
	
	public void addPartyMember(PlayerInstance player)
	{
		final PartySmallWindowAll window = new PartySmallWindowAll();
		window.setPartyList(_members);
		player.sendPacket(window);
		SystemMessage msg = new SystemMessage(SystemMessage.YOU_JOINED_S1_PARTY);
		msg.addString(_members.get(0).getName());
		player.sendPacket(msg);
		msg = new SystemMessage(SystemMessage.S1_JOINED_PARTY);
		msg.addString(player.getName());
		this.broadcastToPartyMembers(msg);
		this.broadcastToPartyMembers(new PartySmallWindowAdd(player));
		_members.add(player);
		_partyLvl += player.getLevel() * player.getLevel();
	}
	
	public void removePartyMember(PlayerInstance player)
	{
		if (_members.contains(player))
		{
			_members.remove(player);
			_partyLvl -= player.getLevel() * player.getLevel();
			SystemMessage msg = new SystemMessage(SystemMessage.YOU_LEFT_PARTY);
			player.sendPacket(msg);
			player.sendPacket(new PartySmallWindowDeleteAll());
			player.setParty(null);
			msg = new SystemMessage(SystemMessage.S1_LEFT_PARTY);
			msg.addString(player.getName());
			this.broadcastToPartyMembers(msg);
			this.broadcastToPartyMembers(new PartySmallWindowDelete(player));
			if (_members.size() == 1)
			{
				_members.get(0).setParty(null);
			}
		}
	}
	
	private PlayerInstance getPlayerByName(String name)
	{
		for (int i = 0; i < _members.size(); ++i)
		{
			final PlayerInstance temp = _members.get(i);
			if (!temp.getName().equals(name))
			{
				continue;
			}
			return temp;
		}
		return null;
	}
	
	public void oustPartyMember(PlayerInstance player)
	{
		if (_members.contains(player))
		{
			if (isLeader(player))
			{
				dissolveParty();
			}
			else
			{
				removePartyMember(player);
			}
		}
	}
	
	public void oustPartyMember(String name)
	{
		final PlayerInstance player = getPlayerByName(name);
		if (player != null)
		{
			if (isLeader(player))
			{
				dissolveParty();
			}
			else
			{
				removePartyMember(player);
			}
		}
	}
	
	private void dissolveParty()
	{
		final SystemMessage msg = new SystemMessage(SystemMessage.PARTY_DISPERSED);
		for (int i = 0; i < _members.size(); ++i)
		{
			final PlayerInstance temp = _members.get(i);
			temp.sendPacket(msg);
			temp.sendPacket(new PartySmallWindowDeleteAll());
			temp.setParty(null);
		}
	}
	
	public void distributeItem(PlayerInstance player, ItemInstance item)
	{
		SystemMessage smsg;
		PlayerInstance target = null;
		target = _randomLoot ? getRandomMember() : player;
		if (item.getCount() == 1)
		{
			smsg = new SystemMessage(SystemMessage.YOU_PICKED_UP_S1);
			smsg.addItemName(item.getItemId());
			target.sendPacket(smsg);
			smsg = new SystemMessage(SystemMessage.S1_PICKED_UP_S2);
			smsg.addString(target.getName());
			smsg.addItemName(item.getItemId());
			this.broadcastToPartyMembers(target, smsg);
		}
		else
		{
			smsg = new SystemMessage(SystemMessage.YOU_PICKED_UP_S1_S2);
			smsg.addNumber(item.getCount());
			smsg.addItemName(item.getItemId());
			target.sendPacket(smsg);
			smsg = new SystemMessage(SystemMessage.S1_PICKED_UP_S2_S3);
			smsg.addString(target.getName());
			smsg.addNumber(item.getCount());
			smsg.addItemName(item.getItemId());
			this.broadcastToPartyMembers(target, smsg);
		}
		final ItemInstance item2 = target.getInventory().addItem(item);
		final InventoryUpdate iu = new InventoryUpdate();
		if (item2.getLastChange() == 1)
		{
			iu.addNewItem(item);
		}
		else
		{
			iu.addModifiedItem(item2);
		}
		target.sendPacket(iu);
		final UserInfo ci = new UserInfo(target);
		target.sendPacket(ci);
	}
	
	public void distributeAdena(ItemInstance adena)
	{
		adena.setCount(adena.getCount() / _members.size());
		final SystemMessage smsg = new SystemMessage(SystemMessage.YOU_PICKED_UP_S1_ADENA);
		smsg.addNumber(adena.getCount());
		for (int i = 0; i < _members.size(); ++i)
		{
			final PlayerInstance member = _members.get(i);
			final ItemInstance item2 = member.getInventory().addItem(adena);
			final InventoryUpdate iu = new InventoryUpdate();
			if (item2.getLastChange() == 1)
			{
				iu.addNewItem(adena);
			}
			else
			{
				iu.addModifiedItem(item2);
			}
			member.sendPacket(smsg);
			member.sendPacket(iu);
		}
	}
	
	public void distributeXpAndSp(int partyDmg, int maxHp, int xpReward, int spReward)
	{
		double mul = (Math.pow(1.07, _members.size() - 1) * partyDmg) / maxHp;
		final double xpTotal = mul * xpReward;
		final double spTotal = mul * spReward;
		for (int i = 0; i < _members.size(); ++i)
		{
			final PlayerInstance player = _members.get(i);
			mul = ((double) player.getLevel() * (double) player.getLevel()) / _partyLvl;
			final int xp = (int) (mul * xpTotal);
			final int sp = (int) (mul * spTotal);
			player.addExpAndSp(xp, sp);
		}
	}
	
	public void recalculatePartyLevel()
	{
		int newlevel = 0;
		for (int i = 0; i < _members.size(); ++i)
		{
			final int plLevel = _members.get(i).getLevel();
			newlevel += plLevel * plLevel;
		}
		_partyLvl = newlevel;
	}
}
