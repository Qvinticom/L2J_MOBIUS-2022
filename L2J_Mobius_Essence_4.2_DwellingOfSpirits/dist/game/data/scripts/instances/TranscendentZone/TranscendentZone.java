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
package instances.TranscendentZone;

import java.util.concurrent.ScheduledFuture;

import org.l2jmobius.commons.concurrent.ThreadPool;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.data.xml.TimedHuntingZoneData;
import org.l2jmobius.gameserver.enums.SkillFinishType;
import org.l2jmobius.gameserver.enums.TeleportWhereType;
import org.l2jmobius.gameserver.instancemanager.InstanceManager;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.holders.SkillHolder;
import org.l2jmobius.gameserver.model.holders.TimedHuntingZoneHolder;
import org.l2jmobius.gameserver.model.instancezone.Instance;
import org.l2jmobius.gameserver.network.NpcStringId;
import org.l2jmobius.gameserver.network.serverpackets.ExSendUIEvent;
import org.l2jmobius.gameserver.network.serverpackets.huntingzones.TimedHuntingZoneExit;

import instances.AbstractInstance;

/**
 * @author Berezkin Nikolay, Mobius
 */
public class TranscendentZone extends AbstractInstance
{
	// NPCs
	private static final int JOON = 34124;
	private static final int KATE = 34120;
	private static final int DEEKHIN = 34121;
	private static final int BUNCH = 34122;
	private static final int AYAN = 34123;
	private static final int PANJI = 34125;
	// Skill
	private static final int BUFF = 45197;
	// Misc
	private static final int[] TEMPLATES =
	{
		1101, // Sea of Spores
		1102, // Enchanted Valley
		1103, // Blazing Swamp
		1104, // War-Torn Plains
		1106, // Dragon Valley
		1107, // Sel Mahum Base
	};
	
	public TranscendentZone()
	{
		super(TEMPLATES);
		addFirstTalkId(JOON, KATE, DEEKHIN, BUNCH, AYAN, PANJI);
		addInstanceLeaveId(TEMPLATES);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, PlayerInstance player)
	{
		if (event.startsWith("ENTER"))
		{
			final int zoneId = Integer.parseInt(event.split(" ")[1]);
			final TimedHuntingZoneHolder huntingZone = TimedHuntingZoneData.getInstance().getHuntingZone(zoneId);
			if (huntingZone == null)
			{
				return null;
			}
			
			if (huntingZone.isSoloInstance())
			{
				enterInstance(player, npc, huntingZone.getInstanceId());
			}
			else
			{
				Instance world = null;
				for (Instance instance : InstanceManager.getInstance().getInstances())
				{
					if (instance.getTemplateId() == huntingZone.getInstanceId())
					{
						world = instance;
						break;
					}
				}
				
				if (world == null)
				{
					world = InstanceManager.getInstance().createInstance(huntingZone.getInstanceId(), player);
				}
				
				player.teleToLocation(huntingZone.getEnterLocation(), world);
			}
		}
		else if (event.startsWith("FINISH"))
		{
			player.teleToLocation(TeleportWhereType.TOWN, null);
			finishInstance(player);
		}
		return null;
	}
	
	@Override
	public void onInstanceLeave(PlayerInstance player, Instance instance)
	{
		if (instance.getParameters().getBoolean("TranscendentZoneTaskFinished", false))
		{
			instance.setParameter("TranscendentZoneTaskFinished", false);
		}
		player.sendPacket(new ExSendUIEvent(player, true, false, 600, 0, NpcStringId.TIME_LEFT));
		player.sendPacket(TimedHuntingZoneExit.STATIC_PACKET);
		
		ThreadPool.schedule(() ->
		{
			if (player.getInstanceWorld() != instance)
			{
				finishInstance(player);
			}
		}, 300000);
		
		player.getEffectList().stopSkillEffects(SkillFinishType.REMOVED, BUFF);
		instance.setParameter("PlayerIsOut", true);
	}
	
	@Override
	public String onFirstTalk(Npc npc, PlayerInstance player)
	{
		if (player.getInstanceWorld().getParameters().getBoolean("TranscendentZoneTaskFinished", false))
		{
			return super.onFirstTalk(npc, player) + "-finished.html";
		}
		
		if (!player.getInstanceWorld().getParameters().getBoolean("PlayerEnter", false))
		{
			player.getInstanceWorld().setDuration(10);
			player.getInstanceWorld().setParameter("PlayerEnter", true);
			startEvent(player);
		}
		
		npc.setTarget(player);
		if (!player.getEffectList().isAffectedBySkill(BUFF))
		{
			npc.doCast(new SkillHolder(BUFF, 1).getSkill());
		}
		
		return super.onFirstTalk(npc, player);
	}
	
	@Override
	protected void onEnter(PlayerInstance player, Instance instance, boolean firstEnter)
	{
		super.onEnter(player, instance, firstEnter);
		instance.setParameter("PlayerIsOut", false);
		if (!firstEnter)
		{
			startEvent(player);
		}
	}
	
	private void startEvent(PlayerInstance player)
	{
		if (!player.getInstanceWorld().getParameters().getBoolean("TranscendentZoneTaskFinished", false))
		{
			final Instance instance = player.getInstanceWorld();
			player.sendPacket(new ExSendUIEvent(player, false, false, Math.min(600, (int) (instance.getRemainingTime() / 1000)), 0, NpcStringId.TIME_LEFT));
			
			final ScheduledFuture<?> spawnTask = ThreadPool.scheduleAtFixedRate(() ->
			{
				if (!instance.getParameters().getBoolean("PlayerIsOut", false))
				{
					if (Rnd.get(5) == 0)
					{
						player.getInstanceWorld().spawnGroup("treasures");
					}
					else
					{
						if (Rnd.get(3) == 0)
						{
							player.getInstanceWorld().spawnGroup("treasures");
						}
						player.getInstanceWorld().spawnGroup("monsters");
					}
				}
			}, 0, 30000);
			
			ThreadPool.schedule(() ->
			{
				instance.getNpcs().stream().filter(WorldObject::isAttackable).forEach(Npc::deleteMe);
				instance.getParameters().set("TranscendentZoneTaskFinished", true);
				if (spawnTask != null)
				{
					spawnTask.cancel(false);
				}
			}, instance.getRemainingTime() - 30000);
		}
	}
	
	public static void main(String[] args)
	{
		new TranscendentZone();
	}
}
