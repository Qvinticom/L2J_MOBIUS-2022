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
 * along with this program. If not, see <http:// www.gnu.org/licenses/>.
 */
package ai.others;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Calendar;
import java.util.logging.Level;

import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.commons.util.Chronos;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;

import ai.AbstractNpcAI;

/**
 * @author Mobius
 */
public class AetherDrops extends AbstractNpcAI
{
	// Monsters
	private static final int[] MONSTERS =
	{
		23487, // Magma Ailith
		23488, // Magma Apophis
		23489, // Lava Wyrm
		23490, // Lava Drake
		23491, // Lava Wendigo
		23492, // Lava Stone Golem
		23493, // Lava Leviah
		24577, // Black Hammer Artisan
		24578, // Black Hammer Collector
		24579, // Black Hammer Protector
		// Cemetery Lv.117
		24844, // Royal Guard Captain
		24846, // Commander of Operations
		24843, // Royal Guard
		24845, // Royal Field Officer
		24848, // Wizard Captain
		24847, // Elite Wizard
		// Fields of Massacre Lv.115
		24488, // Doom Archer
		24489, // Doom Scout
		24490, // Doom Soldier
		24487, // Graveyard Predator
		24486, // Dismal Pole
		24491, // Doom Knight
		// Silent Valley 105
		24506, // Silence Witch
		24507, // Silence Preacle
		24508, // Silence Warrior
		24509, // Silence Slave
		24510, // Silence Hannibal
		// Ivory Tower Crater Lv.105
		24421, // Stone Gargoyle
		24422, // Stone Golem
		24423, // Monster Eye
		24424, // Gargoyle Hunter
		24425, // Steel Golem
		24426, // Stone Cube
		// Enchanted Valley Lv.103
		23569, // Nymph Lily
		23571, // Nymph Tulip
		23572, // Nymph Cosmos
		23567, // Nymph Rose
		23578, // Nymph Guardian
		19600, // Flower Bud
		23581, // Apherus
		// Forest of Mirrors Lv.109
		24461, // Forest Ghost
		24462, // Bewildered Expedition Member
		24463, // Bewildered Patrol
		24464, // Bewildered Dwarf Adventurer
		24465, // Forest Evil Spirit
		24466, // Demonic Mirror
		// Desert Query Lv.101
		23811, // Cantera Tanya
		23812, // Cantera Deathmoz
		23813, // Cantera Floxis
		23814, // Cantera Belika
		23815, // Cantera Bridget
		// Beleth's Magic Circle Lv.101
		23354, // Decay Hannibal
		23355, // Armor Beast
		23356, // Klein Soldier
		23357, // Disorder Warrior
		23360, // Bizuard
		23361, // Mutated Fly
		// Phantasmal Ridge Lv.103
		24511, // Lunatikan
		24512, // Garion Neti
		24513, // Desert Wendigo
		24514, // Koraza
		24515, // Kandiloth
		// Wasteland Lv.117
		24500, // Sand Golem
		24501, // Centaur Fighter
		24502, // Centaur Marksman
		24503, // Centaur Wizard
		24504, // Centaur Warlord
		24505, // Earth Elemental Lord
		// Tanor Canyon Lv. 107
		24587, // Tanor Silenos
		20936, // Tanor Silenos
		20937, // Tanor Silenos Soldier
		20938, // Tanor Silenos Scout
		20939, // Tanor Silenos Warrior
		20942, // Nightmare Guide
		20943, // Nightmare Watchman
		// Alligator Island Lv.107
		24373, // Dailaon Lad
		24376, // Nos Lad
		24377, // Swamp Tribe
		24378, // Swamp Alligator
		24379, // Swamp Warrior
		// Field of Silence Lv.109
		24517, // Kropiora
		24520, // Krotania
		24521, // Krophy
		24522, // Spiz Krphy
		24523, // Krotany
		// Field of Whispers Lv.111
		24304, // Groz Kropiora
		24305, // Groz Krotania
		24306, // Groz Krophy
		24307, // Groz Krotany
		24308, // Groz Water Drake
		// Isle of Prayer
		24445, // Lizardman Rouge
		24446, // Island Guard
		24447, // Niasis
		24448, // Lizardman Archer
		24449, // Lizardman Warrior
		24450, // Lizardman Wizard
		24451, // Lizardman Defender
		// Fafurion Temple
		24318, // Temple Guard Captain
		24321, // Temple Patrol Guard
		24322, // Temple Knight Recruit
		24323, // Temple Guard
		24324, // Temple Guardian Warrior
		24325, // Temple Wizard
		24326, // Temple Guardian Wizard
		24329, // Starving Water Dragon
		// Superion Fortress Lv.102
		23774, // Delta Bathus
		23775, // Delta Krakos
		23776, // Delta Kshana
		23777, // Royal Templar
		23778, // Royal Shooter
		23779, // Royal Wizard
		23780, // Royal Templar Colonel
		23781, // Royal Sharpshooter
		23782, // Royal Archmage
		// Breka's Stronghold Lv.113
		24415, // Breka Orc Scout
		24416, // Breka Orc Scout Captain
		24417, // Breka Orc Archer
		24418, // Breka Orc Shaman
		24419, // Breka Orc Slaughterer
		24420, // Breka Orc prefect
		// Dragon Valley Lv.119
		24480, // Dragon Legionary
		24481, // Dragon Peltast
		24482, // Dragon Officer
		24483, // Dragon Centurion
		24484, // Dragon Elite Guard
		24485, // Behemoth Dragon
		// Swamp of Screams
		24570, // Dire Stakato Drone
		24571, // Dire Stakato Berserker
		24572, // Dire Stakato Shaman
		24573, // Dire Stakato Witch
		// Sel Mahum Training Grounds Lv.113
		24492, // Sel Mahum Soldier
		24493, // Sel Mahum Squad Leader
		24494, // Sel Mahum Warrior
		24495, // Keltron
		// Plains of the Lizardman Lv.113
		24496, // Tanta Lizardman Warrior
		24497, // Tanta Lizardman Archer
		24498, // Tanta Lizardman Wizard
		24499, // Priest Uguros
		// Varka Silenos Barracks Lv.111
		24636, // Varka Silenos Magus
		24637, // Varka Silenos Shaman
		24638, // Varka Silenos Footman
		24639, // Varka Silenos Seargeant
		24640, // Varka Silenos Officer
		// Ketra Orc Barracks Lv.111
		24631, // Ketra Orc Shaman
		24632, // Ketra Orc Prophet
		24633, // Ketra Orc Warrior
		24634, // Ketra Orc Lieutenant
		24635, // Battalion Commander
		// Wall of Argos Lv.115
		24606, // Captive Antelope
		24607, // Captive Bandersnatch
		24608, // Captive Buffalo
		24609, // Captive Grendel
		24610, // Eye of Watchman
		24611, // Elder Homunculus
		// Neutral Zone
		24641, // Tel Mahum Wizard
		24642, // Tel Mahum Legionary
		24643, // Tel Mahum Footman
		24644, // Tel Mahum Lieutenant
		// Sea of Spores
		24621, // Laikel
		24622, // Harane
		24623, // Lesatanas
		24624, // Arbor
	};
	// Item
	private static final int AETHER = 81215;
	// Misc
	private static final String AETHER_DROP_COUNT_VAR = "AETHER_DROP_COUNT";
	private static final int PLAYER_LEVEL = 85;
	private static final int DROP_DAILY = 120;
	private static final int DROP_MIN = 1;
	private static final int DROP_MAX = 1;
	private static final double CHANCE = 1.5;
	
	private AetherDrops()
	{
		addKillId(MONSTERS);
		startQuestTimer("schedule", 1000, null, null);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		if ((npc != null) || (player != null))
		{
			return null;
		}
		
		if (event.equals("schedule"))
		{
			final Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.HOUR_OF_DAY, 6);
			calendar.set(Calendar.MINUTE, 30);
			
			cancelQuestTimers("reset");
			startQuestTimer("reset", calendar.getTimeInMillis() - Chronos.currentTimeMillis(), null, null);
		}
		else if (event.equals("reset"))
		{
			// Update data for offline players.
			try (Connection con = DatabaseFactory.getConnection();
				PreparedStatement ps = con.prepareStatement("DELETE FROM character_variables WHERE var=?"))
			{
				ps.setString(1, AETHER_DROP_COUNT_VAR);
				ps.executeUpdate();
			}
			catch (Exception e)
			{
				LOGGER.log(Level.SEVERE, "Could not reset Aether drop count: ", e);
			}
			
			// Update data for online players.
			for (Player plr : World.getInstance().getPlayers())
			{
				plr.getVariables().remove(AETHER_DROP_COUNT_VAR);
				plr.getVariables().storeMe();
			}
			
			cancelQuestTimers("schedule");
			startQuestTimer("schedule", 1000, null, null);
		}
		
		return null;
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		if ((killer.getLevel() >= PLAYER_LEVEL) && (Rnd.get(100) < CHANCE))
		{
			final int count = killer.getVariables().getInt(AETHER_DROP_COUNT_VAR, 0);
			if (count < DROP_DAILY)
			{
				killer.getVariables().set(AETHER_DROP_COUNT_VAR, count + 1);
				giveItems(killer, AETHER, getRandom(DROP_MIN, DROP_MAX));
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	public static void main(String[] args)
	{
		new AetherDrops();
	}
}