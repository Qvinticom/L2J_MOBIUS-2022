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

import com.l2jmobius.gameserver.data.xml.impl.MonsterBookData;
import com.l2jmobius.gameserver.model.L2Party;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.holders.MonsterBookCardHolder;

import ai.AbstractNpcAI;

/**
 * @author Mobius
 */
public class MonsterBook extends AbstractNpcAI
{
	private static final int MAXIMUM_REWARD_RANGE = 2500;
	
	private MonsterBook()
	{
		for (MonsterBookCardHolder card : MonsterBookData.getInstance().getMonsterBookCards())
		{
			addKillId(card.getMonsterId());
		}
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		final L2Party party = killer.getParty();
		List<L2PcInstance> rewardedPlayers = new ArrayList<>();
		if (party != null)
		{
			rewardedPlayers = party.isInCommandChannel() ? party.getCommandChannel().getMembers() : party.getMembers();
		}
		else
		{
			rewardedPlayers.add(killer);
		}
		
		final MonsterBookCardHolder card = MonsterBookData.getInstance().getMonsterBookCardByMonsterId(npc.getId());
		for (L2PcInstance player : rewardedPlayers)
		{
			if ((player != null) && (player.calculateDistance2D(killer) < MAXIMUM_REWARD_RANGE))
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
