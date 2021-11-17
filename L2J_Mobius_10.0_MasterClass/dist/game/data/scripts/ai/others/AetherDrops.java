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
		20936, // Tanor Silenos
		20937, // Tanor Silenos Soldier
		20938, // Tanor Silenos Scout
		20939, // Tanor Silenos Warrior
		20942, // Nightmare Guide
		20943, // Nightmare Watchman
		23487, // Magma Ailith
		23488, // Magma Apophis
		23489, // Lava Wyrm
		23490, // Lava Drake
		23491, // Lava Wendigo
		23492, // Lava Stone Golem
		23493, // Lava Leviah
		23780, // Royal Templar Colonel
		23781, // Royal Sharpshooter
		23782, // Royal Archmage
		24506, // Silence Witch
		24507, // Silence Preacle
		24508, // Silence Warrior
		24509, // Silence Slave
		24510, // Silence Hannibal
		24511, // Lunatikan
		24512, // Garion Neti
		24513, // Desert Wendigo
		24514, // Koraza
		24515, // Kandiloth
		24577, // Black Hammer Artisan
		24578, // Black Hammer Collector
		24579, // Black Hammer Protector
		24587, // Tanor Silenos
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
			World.getInstance().getPlayers().stream().forEach(plr ->
			{
				plr.getVariables().remove(AETHER_DROP_COUNT_VAR);
				plr.getVariables().storeMe();
			});
			
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