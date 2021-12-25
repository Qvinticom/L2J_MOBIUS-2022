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
package instances.KastiaLow;

import org.l2jmobius.commons.util.Chronos;
import org.l2jmobius.gameserver.instancemanager.InstanceManager;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.ItemHolder;
import org.l2jmobius.gameserver.model.instancezone.Instance;
import org.l2jmobius.gameserver.network.NpcStringId;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

import instances.AbstractInstance;

/**
 * @author Mobius
 */
public class KastiaLow extends AbstractInstance
{
	// NPC
	private static final int KARINIA = 34541;
	// Monsters
	private static final int[] MONSTERS =
	{
		24535, // Kastia's Keeper
		24536, // Kastia's Overseer
		24537, // Kastia's Warder
		24538, // Kalix
	};
	// Item
	private static final ItemHolder KASTIAS_SMALL_PACK = new ItemHolder(81147, 1);
	// Misc
	private static final int TEMPLATE_ID = 298;
	
	public KastiaLow()
	{
		super(TEMPLATE_ID);
		addStartNpc(KARINIA);
		addTalkId(KARINIA);
		addKillId(MONSTERS);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		switch (event)
		{
			case "enterInstance":
			{
				// Cannot enter if player finished another Kastia instance.
				final long currentTime = Chronos.currentTimeMillis();
				if ((currentTime < InstanceManager.getInstance().getInstanceTime(player, 299)) //
					|| (currentTime < InstanceManager.getInstance().getInstanceTime(player, 300)) //
					|| (currentTime < InstanceManager.getInstance().getInstanceTime(player, 305)) //
					|| (currentTime < InstanceManager.getInstance().getInstanceTime(player, 306)))
				{
					player.sendPacket(new SystemMessage(SystemMessageId.SINCE_C1_ENTERED_ANOTHER_INSTANCE_ZONE_THEREFORE_YOU_CANNOT_ENTER_THIS_DUNGEON).addString(player.getName()));
					return null;
				}
				
				enterInstance(player, npc, TEMPLATE_ID);
				if (player.getInstanceWorld() != null)
				{
					startQuestTimer("check_status", 10000, null, player);
				}
				return null;
			}
			case "check_status":
			{
				final Instance world = player.getInstanceWorld();
				if (!isInInstance(world))
				{
					return null;
				}
				switch (world.getStatus())
				{
					case 0:
					{
						showOnScreenMsg(world, NpcStringId.STAGE_1, ExShowScreenMessage.TOP_CENTER, 10000, true);
						world.setStatus(1);
						world.spawnGroup("wave_1");
						startQuestTimer("check_status", 10000, null, player);
						break;
					}
					case 1:
					{
						if (world.getAliveNpcCount() == 0)
						{
							showOnScreenMsg(world, NpcStringId.STAGE_2, ExShowScreenMessage.TOP_CENTER, 10000, true);
							world.setStatus(2);
							world.spawnGroup("wave_2");
						}
						startQuestTimer("check_status", 10000, null, player);
						break;
					}
					case 2:
					{
						if (world.getAliveNpcCount() == 0)
						{
							showOnScreenMsg(world, NpcStringId.STAGE_3, ExShowScreenMessage.TOP_CENTER, 10000, true);
							world.setStatus(3);
							world.spawnGroup("wave_3");
						}
						startQuestTimer("check_status", 10000, null, player);
						break;
					}
					case 3:
					{
						if (world.getAliveNpcCount() == 0)
						{
							showOnScreenMsg(world, NpcStringId.STAGE_4, ExShowScreenMessage.TOP_CENTER, 10000, true);
							world.setStatus(4);
							world.spawnGroup("wave_4");
						}
						startQuestTimer("check_status", 10000, null, player);
						break;
					}
					case 4:
					{
						if (world.getAliveNpcCount() == 0)
						{
							showOnScreenMsg(world, NpcStringId.STAGE_5, ExShowScreenMessage.TOP_CENTER, 10000, true);
							world.setStatus(5);
							world.spawnGroup("wave_5");
						}
						startQuestTimer("check_status", 10000, null, player);
						break;
					}
					case 5:
					{
						if (world.getAliveNpcCount() == 0)
						{
							showOnScreenMsg(world, NpcStringId.STAGE_6, ExShowScreenMessage.TOP_CENTER, 10000, true);
							world.setStatus(6);
							world.spawnGroup("wave_6");
						}
						startQuestTimer("check_status", 10000, null, player);
						break;
					}
					case 6:
					{
						if (world.getAliveNpcCount() == 0)
						{
							showOnScreenMsg(world, NpcStringId.STAGE_7, ExShowScreenMessage.TOP_CENTER, 10000, true);
							world.setStatus(7);
							world.spawnGroup("wave_7");
						}
						startQuestTimer("check_status", 10000, null, player);
						break;
					}
					case 7:
					{
						if (world.getAliveNpcCount() == 0)
						{
							world.spawnGroup("NPC");
							giveItems(player, KASTIAS_SMALL_PACK);
							world.finishInstance();
						}
						else
						{
							startQuestTimer("check_status", 10000, null, player);
						}
						break;
					}
				}
				return null;
			}
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	public static void main(String[] args)
	{
		new KastiaLow();
	}
}
