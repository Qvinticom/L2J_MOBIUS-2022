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
package ai.areas.RaidersCrossroads.Kaysen;

import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.instance.Door;
import org.l2jmobius.gameserver.model.holders.ItemHolder;
import org.l2jmobius.gameserver.network.NpcStringId;
import org.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;

import ai.AbstractNpcAI;

/**
 * Saving the Treasure Dealer<br>
 * Raider's Crossroads belongs to the Nerva Orcs who have built many barracks there. Among other buildings, there's a prison where the Treasure Dealer is held captive.<br>
 * When you kill the Nerva Orcs on the Raider's Crossroads, there's a certain probability of getting Nerva's Temporary Prison Key. The key is used to open the door to Nerva's Temporary Prison.<br>
 * If you save Treasure Dealer Kaysen and then talk to him, there's a low probability of getting an additional reward. However, Kaysen may turn into a monster - Nerva Kaiser.<br>
 * @author Index
 */
public class Kaysen extends AbstractNpcAI
{
	// NPCs
	private static final int KAYSEN = 19458;
	private static final int NERVAS_TEMPORARY_PRISON = 19459;
	private static final int NERVAS_KAYSEN = 23329;
	// Reward
	private static final ItemHolder REWARD = new ItemHolder(57, 1000000);
	// Locations
	private static final Location[] SPAWN_LOCATIONS =
	{
		new Location(6636, -139744, -648, 26632),
		new Location(8662, -146437, -312, 39968),
		new Location(10668, -136336, -1192, 21368),
		new Location(11820, -141016, -592, 57448),
		new Location(18232, -136944, -896, 51256),
		new Location(20032, -142116, -576, 46192),
		new Location(22684, -139168, -744, 9416),
		new Location(23120, -146104, -464, 8732)
	};
	// Misc
	private static final int REWARD_CHANCE = 10;
	private static final long EXP_REWARD = 1000000000;
	
	private Kaysen()
	{
		addStartNpc(KAYSEN);
		addFirstTalkId(KAYSEN);
		addTalkId(KAYSEN);
		addSpawnId(KAYSEN);
		addKillId(NERVAS_KAYSEN);
		
		for (Location location : SPAWN_LOCATIONS)
		{
			addSpawn(KAYSEN, location);
		}
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		switch (event)
		{
			case "NPC_SHOUT":
			{
				if (npc.isSpawned())
				{
					for (Door door : World.getInstance().getVisibleObjectsInRange(npc, Door.class, Npc.INTERACTION_DISTANCE))
					{
						if (!door.isOpen())
						{
							npc.broadcastSay(ChatType.NPC_GENERAL, NpcStringId.FIND_THE_PRISON_KEY_CARRIED_BY_THE_NERVA_ORCS_AND_GET_ME_OUT_OF_HERE_I_LL_MAKE_IT_WORTH_YOUR_WHILE_I_PROMISE);
							startQuestTimer("NPC_SHOUT", getRandom(10, 15) * 1000, npc, null);
							break;
						}
					}
				}
				break;
			}
			case "KAYSEN_OKAY":
			{
				if (player.calculateDistance3D(npc) < Npc.INTERACTION_DISTANCE)
				{
					if (npc.isScriptValue(1))
					{
						break;
					}
					npc.setScriptValue(1);
					
					if (getRandom(100) < REWARD_CHANCE)
					{
						npc.broadcastSay(ChatType.NPC_GENERAL, NpcStringId.YOU_THOUGHT_I_D_BE_A_PUSHOVER_DIDN_T_YOU);
						giveItems(player, REWARD);
						startQuestTimer("KAYSEN_DELETE", 3000, npc, null);
					}
					else
					{
						npc.broadcastSay(ChatType.NPC_GENERAL, NpcStringId.WHAT_WHAT_S_HAPPENING_TO_MY_BODY_ARGH);
						startQuestTimer("KAYSEN_TRANSFORM", 3000, npc, null);
					}
				}
				break;
			}
			case "KAYSEN_DELETE":
			{
				npc.deleteMe();
				startQuestTimer("KAYSEN_RESPAWN", 3600000, npc, null);
				break;
			}
			case "KAYSEN_TRANSFORM":
			{
				npc.deleteMe();
				addSpawn(NERVAS_KAYSEN, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), false, 180000);
				startQuestTimer("KAYSEN_RESPAWN", 3600000, npc, null);
				break;
			}
			case "KAYSEN_RESPAWN":
			{
				addSpawn(KAYSEN, npc);
				break;
			}
		}
		return null;
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		for (Npc nearby : World.getInstance().getVisibleObjectsInRange(npc, Npc.class, Npc.INTERACTION_DISTANCE))
		{
			if (nearby.getId() == NERVAS_TEMPORARY_PRISON)
			{
				return "19458-no.html";
			}
		}
		return "19458.html";
	}
	
	@Override
	public String onSpawn(Npc npc)
	{
		startQuestTimer("NPC_SHOUT", getRandom(10, 15) * 1000, npc, null);
		return super.onSpawn(npc);
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		final Player player = killer.getActingPlayer();
		final int diff = player.getLevel() - npc.getLevel();
		if (diff > -9)
		{
			if (diff > 9)
			{
				player.addExpAndSp(EXP_REWARD / diff, 0);
			}
			else
			{
				player.addExpAndSp(EXP_REWARD, 0);
			}
			player.broadcastPacket(new ExShowScreenMessage(NpcStringId.YOU_HAVE_OBTAINED_EXTRA_XP, ExShowScreenMessage.TOP_CENTER, 10000));
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	public static void main(String[] args)
	{
		new Kaysen();
	}
}
