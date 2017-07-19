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
package com.l2jmobius.gameserver.model.actor.instance;

import com.l2jmobius.gameserver.ai.CtrlEvent;
import com.l2jmobius.gameserver.datatables.SpawnTable;
import com.l2jmobius.gameserver.model.L2Character;
import com.l2jmobius.gameserver.model.L2Spawn;
import com.l2jmobius.gameserver.network.clientpackets.Say2;
import com.l2jmobius.gameserver.network.serverpackets.CreatureSay;
import com.l2jmobius.gameserver.templates.L2NpcTemplate;
import com.l2jmobius.util.Rnd;

public class L2PenaltyMonsterInstance extends L2MonsterInstance
{
	private L2PcInstance _ptk;
	
	public L2PenaltyMonsterInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public L2Character getMostHated()
	{
		return _ptk;
	}
	
	public void NotifyPlayerDead()
	{
		// Monster kill player and can by deleted
		deleteMe();
		
		final L2Spawn spawn = getSpawn();
		if (spawn != null)
		{
			spawn.stopRespawn();
			SpawnTable.getInstance().deleteSpawn(spawn, false);
		}
	}
	
	public void SetPlayerToKill(L2PcInstance ptk)
	{
		if (Rnd.nextInt(100) <= 80)
		{
			final CreatureSay cs = new CreatureSay(getObjectId(), Say2.ALL, getName(), "mmm your bait was delicious.");
			broadcastPacket(cs);
		}
		_ptk = ptk;
		addDamageHate(ptk, 0, 10);
		getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, ptk);
		addAttackerToAttackByList(ptk);
	}
	
	@Override
	public boolean doDie(L2Character killer)
	{
		if (!super.doDie(killer))
		{
			return false;
		}
		
		if (Rnd.nextInt(100) <= 75)
		{
			final CreatureSay cs = new CreatureSay(getObjectId(), Say2.ALL, getName(), "I will tell fishes not to take your bait!");
			broadcastPacket(cs);
		}
		return true;
	}
}