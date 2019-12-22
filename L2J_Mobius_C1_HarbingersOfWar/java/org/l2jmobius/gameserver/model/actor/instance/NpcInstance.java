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

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.enums.CreatureState;
import org.l2jmobius.gameserver.model.Spawn;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.network.ClientThread;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import org.l2jmobius.gameserver.network.serverpackets.ChangeMoveType;
import org.l2jmobius.gameserver.network.serverpackets.MyTargetSelected;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jmobius.gameserver.network.serverpackets.SetToLocation;
import org.l2jmobius.gameserver.network.serverpackets.StatusUpdate;
import org.l2jmobius.gameserver.templates.Npc;
import org.l2jmobius.gameserver.templates.Weapon;
import org.l2jmobius.gameserver.threadpool.ThreadPool;

public class NpcInstance extends Creature
{
	private static Logger _log = Logger.getLogger(NpcInstance.class.getName());
	private static final int INTERACTION_DISTANCE = 150;
	private final Npc _npcTemplate;
	private boolean _attackable;
	private int _rightHandItem;
	private int _leftHandItem;
	private int _expReward;
	private int _spReward;
	private int _attackRange;
	private boolean _aggressive;
	private ScheduledFuture<?> _decayTask;
	private Spawn _spawn;
	
	public NpcInstance(Npc template)
	{
		_npcTemplate = template;
		setCollisionHeight(template.getHeight());
		setCollisionRadius(template.getRadius());
		// TODO: Datapack support for name and title.
		// setName(template.getName());
		// setTitle(template.getTitle());
	}
	
	public boolean isAggressive()
	{
		return _aggressive;
	}
	
	@Override
	public void startAttack(Creature target)
	{
		if (!isRunning())
		{
			setRunning(true);
			final ChangeMoveType move = new ChangeMoveType(this, ChangeMoveType.RUN);
			broadcastPacket(move);
		}
		super.startAttack(target);
	}
	
	public void setAggressive(boolean aggressive)
	{
		_aggressive = aggressive;
	}
	
	public Npc getNpcTemplate()
	{
		return _npcTemplate;
	}
	
	public boolean isAutoAttackable()
	{
		return _attackable;
	}
	
	public void setAutoAttackable(boolean value)
	{
		_attackable = value;
	}
	
	public int getLeftHandItem()
	{
		return _leftHandItem;
	}
	
	public int getRightHandItem()
	{
		return _rightHandItem;
	}
	
	public void setLeftHandItem(int i)
	{
		_leftHandItem = i;
	}
	
	public void setRightHandItem(int i)
	{
		_rightHandItem = i;
	}
	
	@Override
	public void onAction(PlayerInstance player)
	{
		if (this != player.getTarget())
		{
			if (player.getCurrentState() == CreatureState.CASTING)
			{
				player.cancelCastMagic();
			}
			player.setCurrentState(CreatureState.IDLE);
			player.setTarget(this);
			final MyTargetSelected my = new MyTargetSelected(getObjectId(), player.getLevel() - getLevel());
			player.sendPacket(my);
			if (isAutoAttackable())
			{
				final StatusUpdate su = new StatusUpdate(getObjectId());
				su.addAttribute(StatusUpdate.CUR_HP, (int) getCurrentHp());
				su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
				player.sendPacket(su);
			}
			player.sendPacket(new SetToLocation(this));
		}
		else
		{
			if (isAutoAttackable() && !isDead() && !player.isInCombat() && (Math.abs(player.getZ() - getZ()) < 200))
			{
				player.startAttack(this);
			}
			if (!isAutoAttackable())
			{
				final double distance = getDistance(player.getX(), player.getY());
				if (distance > INTERACTION_DISTANCE)
				{
					player.setCurrentState(CreatureState.INTERACT);
					player.setInteractTarget(this);
					player.moveTo(getX(), getY(), getZ(), 150);
				}
				else
				{
					showChatWindow(player, 0);
					player.sendPacket(new ActionFailed());
					player.setCurrentState(CreatureState.IDLE);
				}
			}
		}
	}
	
	@Override
	public void onActionShift(ClientThread client)
	{
		final PlayerInstance player = client.getActiveChar();
		if (client.getAccessLevel() >= 100)
		{
			final NpcHtmlMessage html = new NpcHtmlMessage(1);
			final StringBuilder html1 = new StringBuilder("<html><body><table border=0>");
			html1.append("<tr><td>Current Target:</td></tr>");
			html1.append("<tr><td><br></td></tr>");
			html1.append("<tr><td>Object ID: " + getObjectId() + "</td></tr>");
			html1.append("<tr><td>Template ID: " + getNpcTemplate().getNpcId() + "</td></tr>");
			html1.append("<tr><td><br></td></tr>");
			html1.append("<tr><td>HP: " + getCurrentHp() + "</td></tr>");
			html1.append("<tr><td>MP: " + getCurrentMp() + "</td></tr>");
			html1.append("<tr><td>Level: " + getLevel() + "</td></tr>");
			html1.append("<tr><td><br></td></tr>");
			html1.append("<tr><td>Class: " + getClass().getSimpleName() + "</td></tr>");
			html1.append("<tr><td><br></td></tr>");
			html1.append("<tr><td><button value=\"Kill\" action=\"bypass -h admin_kill " + getObjectId() + "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
			html1.append("<tr><td><button value=\"Delete\" action=\"bypass -h admin_delete " + getObjectId() + "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
			html1.append("</table></body></html>");
			html.setHtml(html1.toString());
			player.sendPacket(html);
		}
		player.sendPacket(new ActionFailed());
	}
	
	public void onBypassFeedback(PlayerInstance player, String command)
	{
		final double distance = getDistance(player.getX(), player.getY());
		if (distance > 150.0)
		{
			player.moveTo(getX(), getY(), getZ(), 150);
		}
		else if (command.startsWith("Quest"))
		{
			final int val = Integer.parseInt(command.substring(6));
			showQuestWindow(player, val);
		}
		else if (command.startsWith("Chat"))
		{
			final int val = Integer.parseInt(command.substring(5));
			showChatWindow(player, val);
		}
	}
	
	@Override
	public Weapon getActiveWeapon()
	{
		return null;
	}
	
	public void insertObjectIdAndShowChatWindow(PlayerInstance player, String content)
	{
		content = content.replace("%objectId%", String.valueOf(getObjectId()));
		final NpcHtmlMessage npcReply = new NpcHtmlMessage(5);
		npcReply.setHtml(content);
		player.sendPacket(npcReply);
	}
	
	protected void showQuestWindow(PlayerInstance player, int val)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setHtml("<html><head><body>There is no quests here yet.</body></html>");
		player.sendPacket(html);
		player.sendPacket(new ActionFailed());
	}
	
	public String getHtmlPath(int npcId, int val)
	{
		String pom = "";
		pom = val == 0 ? "" + npcId : npcId + "-" + val;
		final String temp = "data/html/default/" + pom + ".htm";
		final File mainText = new File(temp);
		if (mainText.exists())
		{
			return temp;
		}
		return "data/html/npcdefault.htm";
	}
	
	public void showChatWindow(PlayerInstance player, int val)
	{
		final int npcId = getNpcTemplate().getNpcId();
		final String filename = getHtmlPath(npcId, val);
		final File file = new File(filename);
		if (!file.exists())
		{
			final NpcHtmlMessage html = new NpcHtmlMessage(1);
			html.setHtml("<html><head><body>My Text is missing:<br>" + filename + "</body></html>");
			player.sendPacket(html);
			player.sendPacket(new ActionFailed());
		}
		else
		{
			try
			{
				final FileInputStream fis = new FileInputStream(file);
				final byte[] raw = new byte[fis.available()];
				fis.read(raw);
				final String content = new String(raw, StandardCharsets.UTF_8);
				insertObjectIdAndShowChatWindow(player, content);
				fis.close();
			}
			catch (Exception e)
			{
				_log.warning("problem with npc text " + e);
			}
		}
		player.sendPacket(new ActionFailed());
	}
	
	public void setExpReward(int exp)
	{
		_expReward = exp;
	}
	
	public void setSpReward(int sp)
	{
		_spReward = sp;
	}
	
	public int getExpReward()
	{
		return (int) (_expReward * Config.RATE_XP);
	}
	
	public int getSpReward()
	{
		return (int) (_spReward * Config.RATE_SP);
	}
	
	@Override
	public int getAttackRange()
	{
		return _attackRange;
	}
	
	public void setAttackRange(int range)
	{
		_attackRange = range;
	}
	
	@Override
	public void reduceCurrentHp(int i, Creature attacker)
	{
		super.reduceCurrentHp(i, attacker);
		if (isDead())
		{
			synchronized (this)
			{
				if ((_decayTask == null) || _decayTask.isCancelled() || _decayTask.isDone())
				{
					_decayTask = ThreadPool.schedule(new Creature.DecayTask(this), 7000);
				}
			}
		}
	}
	
	public void setSpawn(Spawn spawn)
	{
		_spawn = spawn;
	}
	
	@Override
	public void onDecay()
	{
		super.onDecay();
		_spawn.decreaseCount(_npcTemplate.getNpcId());
	}
	
	public void deleteMe()
	{
		World.getInstance().removeVisibleObject(this);
		World.getInstance().removeObject(this);
		removeAllKnownObjects();
	}
	
	@Override
	public boolean isNpc()
	{
		return true;
	}
}
