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
package instances.SSQLibraryOfSages;

import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.instancemanager.InstanceManager;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.instancezone.InstanceWorld;
import org.l2jmobius.gameserver.network.NpcStringId;

import instances.AbstractInstance;

/**
 * Library of Sages instance zone.
 * @author Adry_85
 */
public class SSQLibraryOfSages extends AbstractInstance
{
	// NPCs
	private static final int SOPHIA1 = 32596;
	private static final int PILE_OF_BOOKS1 = 32809;
	private static final int PILE_OF_BOOKS2 = 32810;
	private static final int PILE_OF_BOOKS3 = 32811;
	private static final int PILE_OF_BOOKS4 = 32812;
	private static final int PILE_OF_BOOKS5 = 32813;
	private static final int SOPHIA2 = 32861;
	private static final int SOPHIA3 = 32863;
	private static final int ELCADIA_INSTANCE = 32785;
	// Locations
	private static final Location START_LOC = new Location(37063, -49813, -1128);
	private static final Location EXIT_LOC = new Location(37063, -49813, -1128, 0, 0);
	private static final Location LIBRARY_LOC = new Location(37355, -50065, -1127);
	// NpcString
	private static final NpcStringId[] ELCADIA_DIALOGS =
	{
		NpcStringId.I_MUST_ASK_LIBRARIAN_SOPHIA_ABOUT_THE_BOOK,
		NpcStringId.THIS_LIBRARY_IT_S_HUGE_BUT_THERE_AREN_T_MANY_USEFUL_BOOKS_RIGHT,
		NpcStringId.AN_UNDERGROUND_LIBRARY_I_HATE_DAMP_AND_SMELLY_PLACES,
		NpcStringId.THE_BOOK_THAT_WE_SEEK_IS_CERTAINLY_HERE_SEARCH_INCH_BY_INCH
	};
	// Misc
	private static final int TEMPLATE_ID = 156;
	
	private SSQLibraryOfSages()
	{
		addFirstTalkId(SOPHIA2, ELCADIA_INSTANCE, PILE_OF_BOOKS1, PILE_OF_BOOKS2, PILE_OF_BOOKS3, PILE_OF_BOOKS4, PILE_OF_BOOKS5);
		addStartNpc(SOPHIA1, SOPHIA2, SOPHIA3);
		addTalkId(SOPHIA1, SOPHIA2, SOPHIA3);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		final InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		if (world != null)
		{
			switch (event)
			{
				case "TELEPORT2":
				{
					teleportPlayer(player, LIBRARY_LOC, world.getInstanceId());
					world.getParameters().getObject("elcadia", Npc.class).teleToLocation(LIBRARY_LOC.getX(), LIBRARY_LOC.getY(), LIBRARY_LOC.getZ(), 0, world.getInstanceId());
					break;
				}
				case "exit":
				{
					cancelQuestTimer("FOLLOW", npc, player);
					player.teleToLocation(EXIT_LOC);
					world.getParameters().getObject("elcadia", Npc.class).deleteMe();
					break;
				}
				case "FOLLOW":
				{
					npc.setRunning();
					npc.getAI().startFollow(player);
					npc.broadcastSay(ChatType.NPC_GENERAL, getRandomEntry(ELCADIA_DIALOGS));
					startQuestTimer("FOLLOW", 10000, npc, player);
					break;
				}
				case "ENTER":
				{
					cancelQuestTimer("FOLLOW", npc, player);
					teleportPlayer(player, START_LOC, world.getInstanceId());
					world.getParameters().getObject("elcadia", Npc.class).teleToLocation(START_LOC.getX(), START_LOC.getY(), START_LOC.getZ(), 0, world.getInstanceId());
					break;
				}
			}
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onTalk(Npc npc, Player talker)
	{
		enterInstance(talker, TEMPLATE_ID);
		return super.onTalk(npc, talker);
	}
	
	@Override
	public void onEnterInstance(Player player, InstanceWorld world, boolean firstEntrance)
	{
		if (firstEntrance)
		{
			world.addAllowed(player);
		}
		teleportPlayer(player, START_LOC, world.getInstanceId(), false);
		spawnElcadia(player, world);
	}
	
	private void spawnElcadia(Player player, InstanceWorld world)
	{
		final Npc elcadia = world.getParameters().getObject("elcadia", Npc.class);
		if (elcadia != null)
		{
			elcadia.deleteMe();
		}
		world.setParameter("elcadia", addSpawn(ELCADIA_INSTANCE, player, false, 0, false, player.getInstanceId()));
		startQuestTimer("FOLLOW", 3000, elcadia, player);
	}
	
	public static void main(String[] args)
	{
		new SSQLibraryOfSages();
	}
}
