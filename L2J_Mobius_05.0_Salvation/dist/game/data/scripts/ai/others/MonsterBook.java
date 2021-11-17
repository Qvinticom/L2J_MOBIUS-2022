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

import java.util.ArrayList;
import java.util.List;

import org.l2jmobius.gameserver.data.xml.MonsterBookData;
import org.l2jmobius.gameserver.model.Party;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.MonsterBookCardHolder;

import ai.AbstractNpcAI;

/**
 * @author Mobius
 */
public class MonsterBook extends AbstractNpcAI
{
	private static final int MAXIMUM_REWARD_RANGE = 2500;
	private static final int MINIMUM_PARTY_LEVEL = 99;
	
	private MonsterBook()
	{
		for (MonsterBookCardHolder card : MonsterBookData.getInstance().getMonsterBookCards())
		{
			addKillId(card.getMonsterId());
		}
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		final Party party = killer.getParty();
		List<Player> rewardedPlayers = new ArrayList<>();
		if (party != null)
		{
			rewardedPlayers = party.isInCommandChannel() ? party.getCommandChannel().getMembers() : party.getMembers();
		}
		else
		{
			rewardedPlayers.add(killer);
		}
		
		final MonsterBookCardHolder card = MonsterBookData.getInstance().getMonsterBookCardByMonsterId(npc.getId());
		for (Player player : rewardedPlayers)
		{
			if (((player != null) && (player.calculateDistance2D(killer) < MAXIMUM_REWARD_RANGE)) && (player.getLevel() >= MINIMUM_PARTY_LEVEL))
			{
				player.updateMonsterBook(card);
			}
		}
		
		return super.onKill(npc, killer, isSummon);
	}
	
	public static void main(String[] args)
	{
		new MonsterBook();
	}
}
