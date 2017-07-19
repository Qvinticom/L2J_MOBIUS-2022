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

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Future;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.ThreadPoolManager;
import com.l2jmobius.gameserver.ai.CtrlIntention;
import com.l2jmobius.gameserver.datatables.DoorTable;
import com.l2jmobius.gameserver.instancemanager.FourSepulchersManager;
import com.l2jmobius.gameserver.model.L2ItemInstance;
import com.l2jmobius.gameserver.model.L2World;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import com.l2jmobius.gameserver.network.serverpackets.CreatureSay;
import com.l2jmobius.gameserver.network.serverpackets.MyTargetSelected;
import com.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jmobius.gameserver.network.serverpackets.SocialAction;
import com.l2jmobius.gameserver.network.serverpackets.StatusUpdate;
import com.l2jmobius.gameserver.network.serverpackets.ValidateLocation;
import com.l2jmobius.gameserver.templates.L2NpcTemplate;
import com.l2jmobius.gameserver.util.Util;
import com.l2jmobius.util.Rnd;

import javolution.util.FastMap;

/**
 * @author sandman
 */
public class L2SepulcherNpcInstance extends L2NpcInstance
{
	protected static Map<Integer, Integer> _hallGateKeepers = new FastMap<>();
	
	protected Future<?> _closeTask = null;
	protected Future<?> _spawnNextMysteriousBoxTask = null;
	protected Future<?> _spawnMonsterTask = null;
	
	private final static String HTML_FILE_PATH = "data/html/SepulcherNpc/";
	private final static int HALLS_KEY = 7260;
	
	public L2SepulcherNpcInstance(int objectID, L2NpcTemplate template)
	{
		super(objectID, template);
		
		if (_closeTask != null)
		{
			_closeTask.cancel(true);
		}
		if (_spawnNextMysteriousBoxTask != null)
		{
			_spawnNextMysteriousBoxTask.cancel(true);
		}
		if (_spawnMonsterTask != null)
		{
			_spawnMonsterTask.cancel(true);
		}
		
		_closeTask = null;
		_spawnNextMysteriousBoxTask = null;
		_spawnMonsterTask = null;
	}
	
	@Override
	public void onSpawn()
	{
		super.onSpawn();
	}
	
	@Override
	public void deleteMe()
	{
		if (_closeTask != null)
		{
			_closeTask.cancel(true);
			_closeTask = null;
		}
		
		if (_spawnNextMysteriousBoxTask != null)
		{
			_spawnNextMysteriousBoxTask.cancel(true);
			_spawnNextMysteriousBoxTask = null;
		}
		
		if (_spawnMonsterTask != null)
		{
			_spawnMonsterTask.cancel(true);
			_spawnMonsterTask = null;
		}
		super.deleteMe();
	}
	
	@Override
	public void onAction(L2PcInstance player)
	{
		if (!canTarget(player))
		{
			return;
		}
		
		// Check if the L2PcInstance already target the L2NpcInstance
		if (this != player.getTarget())
		{
			if (Config.DEBUG)
			{
				_log.info("new target selected:" + getObjectId());
			}
			
			// Set the target of the L2PcInstance player
			player.setTarget(this);
			
			// Check if the player is attackable (without a forced attack)
			if (isAutoAttackable(player))
			{
				// Send a Server->Client packet MyTargetSelected to the
				// L2PcInstance player
				// The player.getLevel() - getLevel() permit to display the
				// correct color in the select window
				final MyTargetSelected my = new MyTargetSelected(getObjectId(), player.getLevel() - getLevel());
				player.sendPacket(my);
				
				// Send a Server->Client packet StatusUpdate of the
				// L2NpcInstance to the L2PcInstance to update its HP bar
				final StatusUpdate su = new StatusUpdate(getObjectId());
				su.addAttribute(StatusUpdate.CUR_HP, (int) getStatus().getCurrentHp());
				su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
				player.sendPacket(su);
			}
			else
			{
				// Send a Server->Client packet MyTargetSelected to the
				// L2PcInstance player
				final MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
				player.sendPacket(my);
			}
			
			// Send a Server->Client packet ValidateLocation to correct the
			// L2NpcInstance position and heading on the client
			player.sendPacket(new ValidateLocation(this));
		}
		else
		{
			// Check if the player is attackable (without a forced attack) and
			// isn't dead
			if (isAutoAttackable(player) && !isAlikeDead())
			{
				// Check the height difference
				if (Math.abs(player.getZ() - getZ()) < 400) // this max heigth difference might need some tweaking
				{
					// Set the L2PcInstance Intention to AI_INTENTION_ATTACK
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
				}
				else
				{
					// Send a Server->Client packet ActionFailed (target is out
					// of attack range) to the L2PcInstance player
					player.sendPacket(new ActionFailed());
				}
			}
			
			if (!isAutoAttackable(player))
			{
				// Calculate the distance between the L2PcInstance and the L2NpcInstance
				if (!canInteract(player))
				{
					// Notify the L2PcInstance AI with AI_INTENTION_INTERACT
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
				}
				else
				{
					// Send a Server->Client packet SocialAction to the all
					// L2PcInstance on the _knownPlayer of the L2NpcInstance
					// to display a social action of the L2NpcInstance on their
					// client
					final SocialAction sa = new SocialAction(getObjectId(), Rnd.get(8));
					broadcastPacket(sa);
					doAction(player);
				}
			}
			
			// Send a Server->Client ActionFailed to the L2PcInstance in order
			// to avoid that the client wait another packet
			player.sendPacket(new ActionFailed());
		}
	}
	
	private void doAction(L2PcInstance player)
	{
		if (isDead())
		{
			player.sendPacket(new ActionFailed());
			return;
		}
		
		switch (getNpcId())
		{
			case 8468:
			case 8469:
			case 8470:
			case 8471:
			case 8472:
			case 8473:
			case 8474:
			case 8475:
			case 8476:
			case 8477:
			case 8478:
			case 8479:
			case 8480:
			case 8481:
			case 8482:
			case 8483:
			case 8484:
			case 8485:
			case 8486:
			case 8487:
				setIsInvul(false);
				reduceCurrentHp(getMaxHp() + 1, player);
				if (_spawnMonsterTask != null)
				{
					_spawnMonsterTask.cancel(true);
				}
				_spawnMonsterTask = ThreadPoolManager.getInstance().scheduleEffect(new SpawnMonster(getNpcId()), 3500);
				break;
			case 8455:
			case 8456:
			case 8457:
			case 8458:
			case 8459:
			case 8460:
			case 8461:
			case 8462:
			case 8463:
			case 8464:
			case 8465:
			case 8466:
			case 8467:
				setIsInvul(false);
				reduceCurrentHp(getMaxHp() + 1, player);
				if ((player.getParty() != null) && !player.getParty().isLeader(player))
				{
					player = player.getParty().getPartyMembers().get(0);
				}
				player.addItem("Quest", HALLS_KEY, 1, player, true);
				break;
			default:
			{
				final Quest[] qlsa = getTemplate().getEventQuests(Quest.QuestEventType.QUEST_START);
				if ((qlsa != null) && (qlsa.length > 0))
				{
					player.setLastQuestNpcObject(getObjectId());
				}
				
				final Quest[] qlst = getTemplate().getEventQuests(Quest.QuestEventType.ON_FIRST_TALK);
				if ((qlst != null) && (qlst.length == 1))
				{
					qlst[0].notifyFirstTalk(this, player);
				}
				else
				{
					showChatWindow(player, 0);
				}
			}
		}
		player.sendPacket(new ActionFailed());
	}
	
	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String pom = "";
		if (val == 0)
		{
			pom = "" + npcId;
		}
		else
		{
			pom = npcId + "-" + val;
		}
		
		return HTML_FILE_PATH + pom + ".htm";
	}
	
	@Override
	public void showChatWindow(L2PcInstance player, int val)
	{
		final String filename = getHtmlPath(getNpcId(), val);
		final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(html);
		player.sendPacket(new ActionFailed());
	}
	
	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		if (isBusy() && (getBusyMessage().length() > 0))
		{
			final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			html.setFile("data/html/npcbusy.htm");
			html.replace("%busymessage%", getBusyMessage());
			html.replace("%npcname%", getName());
			html.replace("%playername%", player.getName());
			player.sendPacket(html);
		}
		else if (command.startsWith("Chat"))
		{
			int val = 0;
			try
			{
				val = Integer.parseInt(command.substring(5));
			}
			catch (final IndexOutOfBoundsException ioobe)
			{
			}
			catch (final NumberFormatException nfe)
			{
			}
			showChatWindow(player, val);
		}
		else if (command.startsWith("open_gate"))
		{
			final L2ItemInstance hallsKey = player.getInventory().getItemByItemId(HALLS_KEY);
			if (hallsKey == null)
			{
				showHtmlFile(player, "Gatekeeper-no.htm");
			}
			else if (FourSepulchersManager.getInstance().isAttackTime())
			{
				if (player.getParty() != null)
				{
					for (final L2PcInstance mem : player.getParty().getPartyMembers())
					{
						if (mem.getInventory().getItemByItemId(HALLS_KEY) != null)
						{
							mem.destroyItemByItemId("Quest", HALLS_KEY, mem.getInventory().getItemByItemId(HALLS_KEY).getCount(), mem, true);
						}
					}
				}
				else
				{
					player.destroyItemByItemId("Quest", HALLS_KEY, hallsKey.getCount(), player, true);
				}
				
				openNextDoor(getNpcId());
			}
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
	
	public void openNextDoor(int npcId)
	{
		final int doorId = FourSepulchersManager.getInstance().getHallGateKeepers().get(npcId).intValue();
		final DoorTable _doorTable = DoorTable.getInstance();
		_doorTable.getDoor(doorId).openMe();
		
		switch (getNpcId())
		{
			case 13197:
			case 13202:
			case 13207:
			case 13212:
				FourSepulchersManager.getInstance().spawnShadow(getNpcId());
				break;
			default:
				if (_spawnNextMysteriousBoxTask != null)
				{
					_spawnNextMysteriousBoxTask.cancel(true);
				}
				_spawnNextMysteriousBoxTask = ThreadPoolManager.getInstance().scheduleEffect(new SpawnNextMysteriousBox(npcId), 0);
		}
		
		if (_closeTask != null)
		{
			_closeTask.cancel(true);
		}
		_closeTask = ThreadPoolManager.getInstance().scheduleEffect(new CloseNextDoor(doorId), 15000);
	}
	
	private class CloseNextDoor implements Runnable
	{
		final DoorTable _DoorTable = DoorTable.getInstance();
		
		private final int _DoorId;
		
		public CloseNextDoor(int doorId)
		{
			_DoorId = doorId;
		}
		
		@Override
		public void run()
		{
			try
			{
				_DoorTable.getDoor(_DoorId).closeMe();
			}
			catch (final Exception e)
			{
				// _log.warning(e.getMessage());
			}
		}
	}
	
	private class SpawnNextMysteriousBox implements Runnable
	{
		private final int _NpcId;
		
		public SpawnNextMysteriousBox(int npcId)
		{
			_NpcId = npcId;
		}
		
		@Override
		public void run()
		{
			FourSepulchersManager.getInstance().spawnMysteriousBox(_NpcId);
		}
	}
	
	private class SpawnMonster implements Runnable
	{
		private final int _NpcId;
		
		public SpawnMonster(int npcId)
		{
			_NpcId = npcId;
		}
		
		@Override
		public void run()
		{
			FourSepulchersManager.getInstance().spawnMonster(_NpcId);
		}
	}
	
	public void sayInShout(String msg)
	{
		if ((msg == null) || msg.isEmpty())
		{
			return;// wrong usage
		}
		
		final Collection<L2PcInstance> knownPlayers = L2World.getInstance().getAllPlayers();
		if ((knownPlayers == null) || knownPlayers.isEmpty())
		{
			return;
		}
		
		final CreatureSay sm = new CreatureSay(0, 1, getName(), msg);
		for (final L2PcInstance player : knownPlayers)
		{
			if (player == null)
			{
				continue;
			}
			
			if (Util.checkIfInRange(15000, player, this, true))
			{
				player.sendPacket(sm);
			}
		}
	}
	
	public void showHtmlFile(L2PcInstance player, String file)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile("data/html/SepulcherNpc/" + file);
		html.replace("%npcname%", getName());
		player.sendPacket(html);
	}
}