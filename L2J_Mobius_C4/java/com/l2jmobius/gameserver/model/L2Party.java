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
package com.l2jmobius.gameserver.model;

import java.util.List;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.SevenSignsFestival;
import com.l2jmobius.gameserver.datatables.ItemTable;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PetInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2SummonInstance;
import com.l2jmobius.gameserver.model.entity.DimensionalRift;
import com.l2jmobius.gameserver.network.serverpackets.ExCloseMPCC;
import com.l2jmobius.gameserver.network.serverpackets.ExManagePartyRoomMember;
import com.l2jmobius.gameserver.network.serverpackets.ExOpenMPCC;
import com.l2jmobius.gameserver.network.serverpackets.L2GameServerPacket;
import com.l2jmobius.gameserver.network.serverpackets.PartyMatchDetail;
import com.l2jmobius.gameserver.network.serverpackets.PartyMemberPosition;
import com.l2jmobius.gameserver.network.serverpackets.PartySmallWindowAdd;
import com.l2jmobius.gameserver.network.serverpackets.PartySmallWindowAll;
import com.l2jmobius.gameserver.network.serverpackets.PartySmallWindowDelete;
import com.l2jmobius.gameserver.network.serverpackets.PartySmallWindowDeleteAll;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import com.l2jmobius.gameserver.skills.Stats;
import com.l2jmobius.gameserver.util.Util;
import com.l2jmobius.util.Rnd;

import javolution.util.FastList;

/**
 * This class ...
 * @author nuocnam
 * @version $Revision: 1.6.2.2.2.6 $ $Date: 2005/04/11 19:12:16 $
 */
public class L2Party
{
	static double[] _bonusExpSp =
	{
		1,
		1.30,
		1.39,
		1.50,
		1.54,
		1.58,
		1.63,
		1.67,
		1.71
	};
	
	// private static Logger _log = Logger.getLogger(L2Party.class.getName());
	
	private List<L2PcInstance> _members = null;
	private int _partyLvl = 0;
	private int _itemDistribution = 0;
	private int _itemLastLoot = 0;
	private L2CommandChannel _commandChannel = null;
	private DimensionalRift _rift;
	
	public static final int ITEM_LOOTER = 0;
	public static final int ITEM_RANDOM = 1;
	public static final int ITEM_RANDOM_SPOIL = 2;
	public static final int ITEM_ORDER = 3;
	public static final int ITEM_ORDER_SPOIL = 4;
	
	/**
	 * constructor ensures party has always one member - leader
	 * @param leader
	 */
	public L2Party(L2PcInstance leader)
	{
		_itemDistribution = leader.getLootInvitation();
		getPartyMembers().add(leader);
		_partyLvl = leader.getLevel();
	}
	
	/**
	 * returns number of party members
	 * @return
	 */
	public int getMemberCount()
	{
		return getPartyMembers().size();
	}
	
	/**
	 * returns all party members
	 * @return
	 */
	public List<L2PcInstance> getPartyMembers()
	{
		if (_members == null)
		{
			_members = new FastList<>();
		}
		return _members;
	}
	
	/**
	 * get random member from party
	 * @param ItemId
	 * @param target
	 * @return
	 */
	private L2PcInstance getCheckedRandomMember(int ItemId, L2Character target)
	{
		final List<L2PcInstance> availableMembers = new FastList<>();
		for (final L2PcInstance member : getPartyMembers())
		{
			if (member.getInventory().validateCapacityByItemId(ItemId) && Util.checkIfInRange(Config.ALT_PARTY_RANGE2, target, member, true))
			{
				availableMembers.add(member);
			}
		}
		if (availableMembers.size() > 0)
		{
			return availableMembers.get(Rnd.get(availableMembers.size()));
		}
		return null;
	}
	
	/**
	 * get next item looter
	 * @param ItemId
	 * @param target
	 * @return
	 */
	private L2PcInstance getCheckedNextLooter(int ItemId, L2Character target)
	{
		for (int i = 0; i < getMemberCount(); i++)
		{
			_itemLastLoot++;
			if (_itemLastLoot >= getMemberCount())
			{
				_itemLastLoot = 0;
			}
			L2PcInstance member;
			try
			{
				member = getPartyMembers().get(_itemLastLoot);
				if (member.getInventory().validateCapacityByItemId(ItemId) && Util.checkIfInRange(Config.ALT_PARTY_RANGE2, target, member, true))
				{
					return member;
				}
			}
			catch (final Exception e)
			{
				// continue, take another member if this just logged off
			}
		}
		
		return null;
	}
	
	/**
	 * get next item looter
	 * @param player
	 * @param ItemId
	 * @param spoil
	 * @param target
	 * @return
	 */
	private L2PcInstance getActualLooter(L2PcInstance player, int ItemId, boolean spoil, L2Character target)
	{
		L2PcInstance looter = player;
		
		switch (_itemDistribution)
		{
			case ITEM_RANDOM:
				if (!spoil)
				{
					looter = getCheckedRandomMember(ItemId, target);
				}
				break;
			case ITEM_RANDOM_SPOIL:
				looter = getCheckedRandomMember(ItemId, target);
				break;
			case ITEM_ORDER:
				if (!spoil)
				{
					looter = getCheckedNextLooter(ItemId, target);
				}
				break;
			case ITEM_ORDER_SPOIL:
				looter = getCheckedNextLooter(ItemId, target);
				break;
		}
		
		if (looter == null)
		{
			looter = player;
		}
		return looter;
	}
	
	/**
	 * true if player is party leader
	 * @param player
	 * @return
	 */
	public boolean isLeader(L2PcInstance player)
	{
		return (getPartyMembers().get(0).equals(player));
	}
	
	/**
	 * Returns the Object ID for the party leader to be used as a unique identifier of this party
	 * @return int
	 */
	public int getPartyLeaderOID()
	{
		return getPartyMembers().get(0).getObjectId();
	}
	
	/**
	 * Broadcasts packet to every party member
	 * @param msg
	 */
	public void broadcastToPartyMembers(L2GameServerPacket msg)
	{
		for (final L2PcInstance member : getPartyMembers())
		{
			member.sendPacket(msg);
		}
	}
	
	/**
	 * Send a Server->Client packet to all other L2PcInstance of the Party.<BR>
	 * <BR>
	 * @param player
	 * @param msg
	 */
	public void broadcastToPartyMembers(L2PcInstance player, L2GameServerPacket msg)
	{
		for (final L2PcInstance member : getPartyMembers())
		{
			if ((member != null) && !member.equals(player))
			{
				member.sendPacket(msg);
			}
		}
	}
	
	public void broadcastToPartyMembersNewLeader()
	{
		for (final L2PcInstance member : getPartyMembers())
		{
			if (member != null)
			{
				member.sendPacket(new PartySmallWindowDeleteAll());
				final PartySmallWindowAll window = new PartySmallWindowAll(_itemDistribution);
				window.setPartyList(getPartyMembers());
				member.sendPacket(window);
				member.updateEffectIcons(true);
			}
		}
	}
	
	/**
	 * adds new member to party
	 * @param player
	 */
	public void addPartyMember(L2PcInstance player)
	{
		// sends new member party window for all members
		// we do all actions before adding member to a list, this speeds things up a little
		final PartySmallWindowAll window = new PartySmallWindowAll(_itemDistribution);
		window.setPartyList(getPartyMembers());
		player.sendPacket(window);
		
		SystemMessage msg = new SystemMessage(SystemMessage.YOU_JOINED_S1_PARTY);
		msg.addString(getPartyMembers().get(0).getName());
		player.sendPacket(msg);
		
		msg = new SystemMessage(SystemMessage.S1_JOINED_PARTY);
		msg.addString(player.getName());
		broadcastToPartyMembers(msg);
		broadcastToPartyMembers(new PartySmallWindowAdd(player));
		
		if (!player.isInBoat())
		{
			player.sendPacket(new PartyMemberPosition(player));
			broadcastToPartyMembers(player, new PartyMemberPosition(player));
		}
		
		// add player to party, adjust party level
		if (!getPartyMembers().contains(player))
		{
			getPartyMembers().add(player);
		}
		if (player.getLevel() > _partyLvl)
		{
			_partyLvl = player.getLevel();
		}
		
		// update partySpelled
		for (final L2PcInstance member : getPartyMembers())
		{
			member.updateEffectIcons(true); // update party icons only
			member.broadcastUserInfo();
			
		}
		
		if (isInCommandChannel())
		{
			player.sendPacket(new ExOpenMPCC());
		}
	}
	
	/**
	 * Remove player from party Overloaded method that takes player's name as parameter
	 * @param name
	 * @param hasLeft
	 */
	public void removePartyMember(String name, boolean hasLeft)
	{
		final L2PcInstance player = getPlayerByName(name);
		if (player != null)
		{
			removePartyMember(player, hasLeft);
		}
	}
	
	/**
	 * Remove player from party
	 * @param player
	 * @param hasLeft
	 */
	public void removePartyMember(L2PcInstance player, boolean hasLeft)
	{
		if (getPartyMembers().contains(player))
		{
			if (isInDimensionalRift())
			{
				_rift.partyMemberExited(player, hasLeft);
			}
			
			final boolean isLeader = isLeader(player);
			getPartyMembers().remove(player);
			recalculatePartyLevel();
			
			if (player.isFestivalParticipant())
			{
				SevenSignsFestival.getInstance().updateParticipants(player, this);
			}
			
			SystemMessage msg = new SystemMessage(SystemMessage.YOU_HAVE_BEEN_EXPELLED_FROM_PARTY);
			if (hasLeft)
			{
				msg = new SystemMessage(SystemMessage.YOU_LEFT_PARTY);
			}
			
			player.sendPacket(msg);
			player.sendPacket(new PartySmallWindowDeleteAll());
			player.setParty(null);
			
			if (hasLeft)
			{
				msg = new SystemMessage(SystemMessage.S1_LEFT_PARTY);
			}
			else
			{
				msg = new SystemMessage(SystemMessage.S1_WAS_EXPELLED_FROM_PARTY);
			}
			msg.addString(player.getName());
			broadcastToPartyMembers(msg);
			broadcastToPartyMembers(new PartySmallWindowDelete(player));
			
			if (isLeader && (getPartyMembers().size() > 1))
			{
				msg = new SystemMessage(SystemMessage.S1_HAS_BECOME_A_PARTY_LEADER);
				msg.addString(getPartyMembers().get(0).getName());
				broadcastToPartyMembers(msg);
				broadcastToPartyMembersNewLeader();
			}
			
			if (isInCommandChannel())
			{
				player.sendPacket(new ExCloseMPCC());
			}
			
			if (getPartyMembers().size() == 1)
			{
				if (isInCommandChannel())
				{
					if (getCommandChannel().getChannelLeader().equals(getPartyMembers().get(0)))
					{
						getCommandChannel().disbandChannel();
					}
					else
					{
						getCommandChannel().removeParty(this);
					}
				}
				
				if (getPartyMembers().get(0) != null)
				{
					getPartyMembers().get(0).setParty(null);
				}
				_members = null;
			}
		}
		
		if (player.isInPartyMatchRoom())
		{
			final PartyMatchRoom _room = PartyMatchRoomList.getInstance().getPlayerRoom(player);
			if (_room != null)
			{
				player.sendPacket(new PartyMatchDetail(_room));
				for (final L2PcInstance _member : _room.getPartyMembers())
				{
					if (_member == null)
					{
						continue;
					}
					
					_member.sendPacket(new ExManagePartyRoomMember(player, _room, 1));
				}
			}
			player.broadcastUserInfo();
		}
	}
	
	/**
	 * Change party leader (used for string arguments)
	 * @param name
	 */
	
	public void changePartyLeader(String name)
	{
		final L2PcInstance player = getPlayerByName(name);
		
		if (player != null)
		{
			if (getPartyMembers().contains(player))
			{
				if (isLeader(player))
				{
					player.sendPacket(new SystemMessage(SystemMessage.YOU_CANNOT_TRANSFER_RIGHTS_TO_YOURSELF));
				}
				else
				{
					// Swap party members
					L2PcInstance temp;
					final int p1 = getPartyMembers().indexOf(player);
					temp = getPartyMembers().get(0);
					getPartyMembers().set(0, getPartyMembers().get(p1));
					getPartyMembers().set(p1, temp);
					
					SystemMessage msg = new SystemMessage(SystemMessage.S1_HAS_BECOME_A_PARTY_LEADER);
					msg.addString(getPartyMembers().get(0).getName());
					broadcastToPartyMembers(msg);
					broadcastToPartyMembersNewLeader();
					player.updateEffectIcons(true);
					if (isInCommandChannel() && temp.equals(_commandChannel.getChannelLeader()))
					{
						_commandChannel.setChannelLeader(getPartyMembers().get(0));
						msg = new SystemMessage(SystemMessage.COMMAND_CHANNEL_LEADER_NOW_S1);
						msg.addString(_commandChannel.getChannelLeader().getName());
						_commandChannel.broadcastToChannelMembers(msg);
					}
					
					if (player.isInPartyMatchRoom())
					{
						final PartyMatchRoom room = PartyMatchRoomList.getInstance().getPlayerRoom(player);
						room.changeLeader(player);
					}
				}
			}
			else
			{
				player.sendPacket(new SystemMessage(SystemMessage.YOU_CAN_TRANSFER_RIGHTS_ONLY_TO_ANOTHER_PARTY_MEMBER));
			}
		}
	}
	
	/**
	 * finds a player in the party by name
	 * @param name
	 * @return
	 */
	private L2PcInstance getPlayerByName(String name)
	{
		for (final L2PcInstance member : getPartyMembers())
		{
			if (member.getName().equals(name))
			{
				return member;
			}
		}
		return null;
	}
	
	/**
	 * distribute item(s) to party members
	 * @param player
	 * @param item
	 */
	public void distributeItem(L2PcInstance player, L2ItemInstance item)
	{
		if (item.getItemId() == 57)
		{
			distributeAdena(player, item.getCount(), player);
			ItemTable.getInstance().destroyItem("Party", item, player, null);
			return;
		}
		
		final L2PcInstance target = getActualLooter(player, item.getItemId(), false, player);
		target.addItem("Party", item, player, true);
		
		// Send messages to other party members about reward
		if (item.getCount() > 1)
		{
			final SystemMessage msg = new SystemMessage(SystemMessage.S1_PICKED_UP_S2_S3);
			msg.addString(target.getName());
			msg.addItemName(item.getItemId());
			msg.addNumber(item.getCount());
			broadcastToPartyMembers(target, msg);
		}
		else
		{
			final SystemMessage msg = new SystemMessage(SystemMessage.S1_PICKED_UP_S2);
			msg.addString(target.getName());
			msg.addItemName(item.getItemId());
			broadcastToPartyMembers(target, msg);
		}
	}
	
	/**
	 * distribute item(s) to party members
	 * @param player
	 * @param item
	 * @param spoil
	 * @param target
	 */
	public void distributeItem(L2PcInstance player, L2Attackable.RewardItem item, boolean spoil, L2Attackable target)
	{
		if (item == null)
		{
			return;
		}
		
		if (item.getItemId() == 57)
		{
			distributeAdena(player, item.getCount(), target);
			return;
		}
		
		final L2PcInstance looter = getActualLooter(player, item.getItemId(), spoil, target);
		
		looter.addItem(spoil ? "Sweep" : "Party", item.getItemId(), item.getCount(), player, true);
		
		// Send messages to other aprty members about reward
		if (item.getCount() > 1)
		{
			final SystemMessage msg = spoil ? new SystemMessage(SystemMessage.S1_SWEEPED_UP_S2_S3) : new SystemMessage(SystemMessage.S1_PICKED_UP_S2_S3);
			msg.addString(looter.getName());
			msg.addItemName(item.getItemId());
			msg.addNumber(item.getCount());
			broadcastToPartyMembers(looter, msg);
		}
		else
		{
			final SystemMessage msg = spoil ? new SystemMessage(SystemMessage.S1_SWEEPED_UP_S2) : new SystemMessage(SystemMessage.S1_PICKED_UP_S2);
			msg.addString(looter.getName());
			msg.addItemName(item.getItemId());
			broadcastToPartyMembers(looter, msg);
		}
	}
	
	/**
	 * distribute adena to party members
	 * @param player
	 * @param adena
	 * @param target
	 */
	public void distributeAdena(L2PcInstance player, int adena, L2Character target)
	{
		// Get all the party members
		final List<L2PcInstance> membersList = getPartyMembers();
		
		// Check the number of party members that must be rewarded
		// (The party member must be in range to receive its reward)
		final List<L2PcInstance> ToReward = new FastList<>();
		
		for (final L2PcInstance member : membersList)
		{
			if (!Util.checkIfInRange(Config.ALT_PARTY_RANGE2, target, member, true))
			{
				continue;
			}
			
			ToReward.add(member);
		}
		
		// Avoid null exceptions, if any
		if (ToReward.isEmpty())
		{
			return;
		}
		
		// Now we can actually distribute the adena reward
		// (Total adena splitted by the number of party members that are in range and must be rewarded)
		final int count = adena / ToReward.size();
		for (final L2PcInstance member : ToReward)
		{
			member.addAdena("Party", count, player, true);
		}
	}
	
	/**
	 * Distribute Experience and SP rewards to L2PcInstance Party members in the known area of the last attacker.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Get the L2PcInstance owner of the L2SummonInstance (if necessary)</li>
	 * <li>Calculate the Experience and SP reward distribution rate</li>
	 * <li>Add Experience and SP to the L2PcInstance</li><BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T GIVE rewards to L2PetInstance</B></FONT><BR>
	 * <BR>
	 * @param xpReward The Experience reward to distribute
	 * @param spReward The SP reward to distribute
	 * @param rewardedMembers The list of L2PcInstance to reward
	 * @param topLvl
	 */
	public void distributeXpAndSp(long xpReward, int spReward, List<L2PlayableInstance> rewardedMembers, int topLvl)
	{
		L2SummonInstance summon = null;
		final List<L2PlayableInstance> validMembers = getValidMembers(rewardedMembers, topLvl);
		
		float penalty;
		double sqLevel;
		double preCalculation;
		
		xpReward *= getExpBonus(validMembers.size());
		spReward *= getSpBonus(validMembers.size());
		
		double sqLevelSum = 0;
		for (final L2PlayableInstance character : validMembers)
		{
			sqLevelSum += (character.getLevel() * character.getLevel());
		}
		
		// Go through the L2PcInstances and L2PetInstances (not L2SummonInstances) that must be rewarded
		synchronized (rewardedMembers)
		{
			for (final L2Character member : rewardedMembers)
			{
				if (member.isDead())
				{
					continue;
				}
				
				penalty = 0;
				
				// The L2SummonInstance penalty
				if ((member.getPet() != null) && (member.getPet() instanceof L2SummonInstance))
				{
					summon = (L2SummonInstance) member.getPet();
					penalty = summon.getExpPenalty();
				}
				
				// Pets that leech xp from the owner (like babypets) do not get rewarded directly
				if (member instanceof L2PetInstance)
				{
					if (((L2PetInstance) member).getPetData().getOwnerExpTaken() > 0)
					{
						continue;
					}
					penalty = (float) 0.85;
				}
				
				// Calculate and add the EXP and SP reward to the member
				if (validMembers.contains(member))
				{
					sqLevel = member.getLevel() * member.getLevel();
					preCalculation = (sqLevel / sqLevelSum) * (1 - penalty);
					
					// Add the XP/SP points to the requested party member
					if (!member.isDead())
					{
						member.addExpAndSp(Math.round(member.calcStat(Stats.EXPSP_RATE, xpReward * preCalculation, null, null)), (int) member.calcStat(Stats.EXPSP_RATE, spReward * preCalculation, null, null));
					}
				}
				else
				{
					member.addExpAndSp(0, 0);
				}
			}
		}
	}
	
	/**
	 * refresh party level
	 */
	public void recalculatePartyLevel()
	{
		int newLevel = 0;
		for (final L2PcInstance member : getPartyMembers())
		{
			if (member == null)
			{
				getPartyMembers().remove(member);
				continue;
			}
			
			if (member.getLevel() > newLevel)
			{
				newLevel = member.getLevel();
			}
		}
		_partyLvl = newLevel;
		
	}
	
	private List<L2PlayableInstance> getValidMembers(List<L2PlayableInstance> members, int topLvl)
	{
		final List<L2PlayableInstance> validMembers = new FastList<>();
		
		// Fixed LevelDiff cutoff point
		if (Config.PARTY_XP_CUTOFF_METHOD.equalsIgnoreCase("level"))
		{
			for (final L2PlayableInstance member : members)
			{
				if ((topLvl - member.getLevel()) <= Config.PARTY_XP_CUTOFF_LEVEL)
				{
					validMembers.add(member);
				}
			}
		}
		// Fixed MinPercentage cutoff point
		else if (Config.PARTY_XP_CUTOFF_METHOD.equalsIgnoreCase("percentage"))
		{
			int sqLevelSum = 0;
			for (final L2PlayableInstance member : members)
			{
				sqLevelSum += (member.getLevel() * member.getLevel());
			}
			
			for (final L2PlayableInstance member : members)
			{
				final int sqLevel = member.getLevel() * member.getLevel();
				if ((sqLevel * 100) >= (sqLevelSum * Config.PARTY_XP_CUTOFF_PERCENT))
				{
					validMembers.add(member);
				}
			}
		}
		// Automatic cutoff method
		else if (Config.PARTY_XP_CUTOFF_METHOD.equalsIgnoreCase("auto"))
		{
			int sqLevelSum = 0;
			for (final L2PlayableInstance member : members)
			{
				sqLevelSum += (member.getLevel() * member.getLevel());
			}
			
			int i = members.size() - 1;
			if (i < 1)
			{
				return members;
			}
			if (i >= _bonusExpSp.length)
			{
				i = _bonusExpSp.length - 1;
			}
			
			for (final L2PlayableInstance member : members)
			{
				final int sqLevel = member.getLevel() * member.getLevel();
				if (sqLevel >= (sqLevelSum * (1 - (1 / ((1 + _bonusExpSp[i]) - _bonusExpSp[i - 1])))))
				{
					validMembers.add(member);
				}
			}
		}
		return validMembers;
	}
	
	private double getBaseExpSpBonus(int membersCount)
	{
		int i = membersCount - 1;
		if (i < 1)
		{
			return 1;
		}
		if (i >= _bonusExpSp.length)
		{
			i = _bonusExpSp.length - 1;
		}
		
		return _bonusExpSp[i];
	}
	
	private double getExpBonus(int membersCount)
	{
		if (membersCount < 2)
		{
			// not is a valid party
			return getBaseExpSpBonus(membersCount);
		}
		return getBaseExpSpBonus(membersCount) * Config.RATE_PARTY_XP;
	}
	
	private double getSpBonus(int membersCount)
	{
		if (membersCount < 2)
		{
			// not is a valid party
			return getBaseExpSpBonus(membersCount);
		}
		return getBaseExpSpBonus(membersCount) * Config.RATE_PARTY_SP;
	}
	
	public int getLevel()
	{
		return _partyLvl;
	}
	
	public int getLootDistribution()
	{
		return _itemDistribution;
	}
	
	public boolean isInCommandChannel()
	{
		return _commandChannel != null;
	}
	
	public L2CommandChannel getCommandChannel()
	{
		return _commandChannel;
	}
	
	public void setCommandChannel(L2CommandChannel channel)
	{
		_commandChannel = channel;
	}
	
	public boolean isInDimensionalRift()
	{
		return _rift != null;
	}
	
	public void setDimensionalRift(DimensionalRift dr)
	{
		_rift = dr;
	}
	
	public DimensionalRift getDimensionalRift()
	{
		return _rift;
	}
}