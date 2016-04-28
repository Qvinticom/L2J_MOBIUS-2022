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
package ai.npc.MonumentOfHeroes;

import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.util.Util;

import ai.AbstractNpcAI;

/**
 * Monument of Heroes AI.
 * @author Adry_85
 */
final class MonumentOfHeroes extends AbstractNpcAI
{
	// NPCs
	private static final int[] MONUMENTS =
	{
		31690,
		31769,
		31770,
		31771,
		31772
	};
	// Items
	private static final int WINGS_OF_DESTINY_CIRCLET = 6842;
	private static final int[] WEAPONS =
	{
		30392, // Infinity Shaper
		30393, // Infinity Cutter
		30394, // Infinity Slasher
		30395, // Infinity Avenger
		30396, // Infinity Fighter
		30397, // Infinity Stormer
		30398, // Infinity Thrower
		30399, // Infinity Shooter
		30400, // Infinity Buster
		30401, // Infinity Caster
		30402, // Infinity Retributer
		30403, // Infinity Dualsword
		30404, // Infinity Dual Dagger
		30405, // Infinity Dual Blunt Weapon
	};
	
	private MonumentOfHeroes()
	{
		super(MonumentOfHeroes.class.getSimpleName(), "ai/npc");
		addStartNpc(MONUMENTS);
		addTalkId(MONUMENTS);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		switch (event)
		{
			case "HeroWeapon":
			{
				if (player.isHero())
				{
					return hasAtLeastOneQuestItem(player, WEAPONS) ? "already_have_weapon.htm" : "weapon_list.htm";
				}
				return "no_hero_weapon.htm";
			}
			case "HeroCirclet":
			{
				if (player.isHero())
				{
					if (!hasQuestItems(player, WINGS_OF_DESTINY_CIRCLET))
					{
						giveItems(player, WINGS_OF_DESTINY_CIRCLET, 1);
					}
					else
					{
						return "already_have_circlet.htm";
					}
				}
				else
				{
					return "no_hero_circlet.htm";
				}
				break;
			}
			default:
			{
				final int weaponId = Integer.parseInt(event);
				if (Util.contains(WEAPONS, weaponId))
				{
					giveItems(player, weaponId, 1);
				}
				break;
			}
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	public static void main(String[] args)
	{
		new MonumentOfHeroes();
	}
}