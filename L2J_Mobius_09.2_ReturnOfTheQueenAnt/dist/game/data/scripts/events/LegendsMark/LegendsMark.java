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
package events.LegendsMark;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Calendar;
import java.util.logging.Level;

import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.commons.util.Chronos;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.data.xml.MultisellData;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.LongTimeEvent;

/**
 * Legend's Mark event AI.
 * @author Manax
 */
public class LegendsMark extends LongTimeEvent
{
	// NPC
	private static final int DREAMER = 34588;
	// Monsters
	private static final int[] MONSTERS =
	{
		// Ivory Tower Crater
		24421, // Stone Gargoyle
		24422, // Stone Golem
		24423, // Monster Eye
		24424, // Gargoyle Hunter
		24425, // Steel Golem
		24426, // Stone Cube
		// Silent Valley
		24506, // Silence Witch
		24508, // Silence Warrior
		24510, // Silence Hannibal
		24509, // Silence Slave
		24507, // Silence Preacle
		// Alligator Island
		24377, // Swamp Tribe
		24378, // Swamp Alligator
		24379, // Swamp Warrior
		24373, // Dailaon Lad
		24376, // Nos Lad
		// Tanor Canyon
		20941, // Tanor Silenos Chieftai
		20939, // Tanor Silenos Warrior
		20937, // Tanor Silenos Soldier
		20942, // Nightmare Guide
		20938, // Tanor Silenos Scout
		20943, // Nightmare Watchman
		24587, // Tanor Silenos
		// The Forest of Mirrors
		24466, // Demonic Mirror
		24465, // Forest Evil Spirit
		24461, // Forest Ghost
		24464, // Bewildered Dwarf Adven
		24463, // Bewildered Patrol
		24462, // Bewildered Expedition
		// Field of Silence
		24523, // Krotany
		24520, // Krotania
		24521, // Krophy
		24522, // Spiz Krophy
		// Isle of Prayer
		24451, // Lizardman Defender
		24449, // Lizardman Warrior
		24448, // Lizardman Archer
		24450, // Lizardmen Wizard
		24447, // Niasis
		24445, // Lizardman Rogue
		24446, // Island Guard
		// Field of Whispers
		24304, // Groz Kropiora
		24305, // Groz Krotania
		24306, // Groz Krophy
		24307, // Groz Krotany
		24308, // Water Drake
		// Brekas Stronghold
		24420, // Breka Orc Prefect
		24416, // Breka Orc Scout Captai
		24419, // Breka Orc Slaughterer
		24415, // Breka Orc Scout
		24417, // Breka Orc Archer
		24418, // Breka Orc Shaman
		// Plains of the Lizardmen
		24496, // Tanta Lizardman Warrio
		24498, // Tanta Lizardman Wizard
		24499, // Priest Ugoros
		24497, // Tanta Lizardman Archer
		// Sel Mahum Training Grounds
		24492, // Sel Mahum Soldier
		24494, // Sel Mahum Warrior
		24493, // Sel Mahum Squad Leader
		24495, // Keltron
		// Fields of Massacre
		24486, // Dismal Pole
		24487, // Graveyard Predator
		24489, // Doom Scout
		24491, // Doom Knight
		24490, // Doom Soldier
		24488, // Doom Archer
		// Wall of Argos
		24606, // Captive Antelope
		24607, // Captive Bandersnatch
		24608, // Captive Buffalo
		24609, // Captive Grendel
		24610, // Eye of Watchman
		24611, // Elder Homunculus
		// Cemetery
		19455, // Aden Raider
		19456, // Te Ochdumann
		19457, // Travis
		20668, // Grave Guard
		23290, // Royal Knight
		23291, // Personal Magician
		23292, // Royal Guard
		23293, // Royal Guard Captain
		23294, // Chief Magician
		23295, // Operations Manager
		23296, // Chief Quartermaster
		23297, // Escort
		23298, // Royal Quartermaster
		23299, // Operations Chief of th
		23300, // Commander of Operation
		// Wasteland
		24501, // Centaur Fighter
		24504, // Centaur Warlord
		24505, // Earth Elemental Lord
		24503, // Centaur Wizard
		24500, // Sand Golem
		24502, // Centaur Marksman
		// Neutral Zone
		24641, // Tel Mahum Wizard
		24642, // Tel Mahum Legionary
		24643, // Tel Mahum Footman
		24644, // Tel Mahum Lieutenant
		// Varka Silenos Barracks
		24636, // Varka Silenos Magus
		24637, // Varka Silenos Shaman
		24638, // Varka Silenos Footman
		24639, // Varka Silenos Sergeant
		24640, // Varka Silenos Officer
		// Ketra Orc Outpost
		24631, // Ketra Orc Shaman
		24632, // Ketra Orc Prophet
		24633, // Ketra Orc Warrior
		24634, // Ketra Orc Lieutenant
		24635, // Ketra Orc Battalion Co
		// Sea Of Spores
		24226, // Aranea
		24227, // Keros
		24228, // Falena
		24229, // Atrofa
		24230, // Nuba
		24231, // Torfedo
		24234, // Lesatanas
		24235, // Arbor
		24236, // Tergus
		24237, // Skeletus
		24238, // Atrofine
		// Dragon Valley
		24480, // Dragon Legionnaire
		24482, // Dragon Officer
		24481, // Dragon Peltast
		24483, // Dragon Centurion
		24484, // Dragon Elite Guard
		24485, // Behemoth Dragon
		// Fafurion Temple
		24329, // Starving Water Dragon
		24318, // Temple Guard Captain
		24325, // Temple Wizard
		24324, // Temple Guardian Warrio
		24326, // Temple Guardian Wizard
		24323, // Temple Guard
		24321, // Temple Patrol Guard
		24322, // Temple Knight Recruit
	
	};
	// Item
	private static final int PROPHECY_FRAGMENTS = 81904;
	// Multisells
	private static final int PROPHECY_FRAGMENT = 34588001;
	private static final int LEGEND_MARK = 34588002;
	// Misc
	private static final String PROPHECY_FRAGMENTS_DROP_COUNT_VAR = "PROPHECY_FRAGMENTS_DROP_COUNT";
	private static final int PLAYER_LEVEL = 99;
	private static final int DROP_DAILY = 3;
	private static final int DROP_MIN = 1;
	private static final int DROP_MAX = 1;
	private static final double CHANCE = 1.5;
	
	private LegendsMark()
	{
		addStartNpc(DREAMER);
		addFirstTalkId(DREAMER);
		addTalkId(DREAMER);
		addKillId(MONSTERS);
		startQuestTimer("schedule", 1000, null, null);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = null;
		switch (event)
		{
			case "34588.html":
			case "34588-1.html":
			case "34588-2.html":
			{
				htmltext = event;
				break;
			}
			case "prophecy_fragment":
			{
				MultisellData.getInstance().separateAndSend(PROPHECY_FRAGMENT, player, npc, false);
				break;
			}
			case "legend_mark":
			{
				MultisellData.getInstance().separateAndSend(LEGEND_MARK, player, npc, false);
				break;
			}
			case "schedule":
			{
				final Calendar calendar = Calendar.getInstance();
				calendar.set(Calendar.HOUR_OF_DAY, 6);
				calendar.set(Calendar.MINUTE, 30);
				cancelQuestTimers("reset");
				startQuestTimer("reset", calendar.getTimeInMillis() - Chronos.currentTimeMillis(), null, null);
				break;
			}
			case "reset":
			{
				// Update data for offline players.
				try (Connection con = DatabaseFactory.getConnection();
					PreparedStatement ps = con.prepareStatement("DELETE FROM character_variables WHERE var=?"))
				{
					ps.setString(1, PROPHECY_FRAGMENTS_DROP_COUNT_VAR);
					ps.executeUpdate();
				}
				catch (Exception e)
				{
					LOGGER.log(Level.SEVERE, "Could not reset Aether drop count: ", e);
				}
				
				// Update data for online players.
				for (Player plr : World.getInstance().getPlayers())
				{
					plr.getVariables().remove(PROPHECY_FRAGMENTS_DROP_COUNT_VAR);
					plr.getVariables().storeMe();
				}
				
				cancelQuestTimers("schedule");
				startQuestTimer("schedule", 1000, null, null);
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		return npc.getId() + ".html";
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		if (isEventPeriod() && (killer.getLevel() >= PLAYER_LEVEL) && (Rnd.get(100) < CHANCE))
		{
			final int count = killer.getVariables().getInt(PROPHECY_FRAGMENTS_DROP_COUNT_VAR, 0);
			if (count < DROP_DAILY)
			{
				killer.getVariables().set(PROPHECY_FRAGMENTS_DROP_COUNT_VAR, count + 1);
				giveItems(killer, PROPHECY_FRAGMENTS, getRandom(DROP_MIN, DROP_MAX));
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	public static void main(String[] args)
	{
		new LegendsMark();
	}
}
