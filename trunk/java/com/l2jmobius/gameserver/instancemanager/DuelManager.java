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
package com.l2jmobius.gameserver.instancemanager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.l2jmobius.gameserver.enums.PrivateStoreType;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.entity.Duel;
import com.l2jmobius.gameserver.model.skills.Skill;
import com.l2jmobius.gameserver.model.zone.ZoneId;
import com.l2jmobius.gameserver.network.SystemMessageId;
import com.l2jmobius.gameserver.network.serverpackets.L2GameServerPacket;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;

public final class DuelManager
{
	private final Map<Integer, Duel> _duels = new ConcurrentHashMap<>();
	private final AtomicInteger _currentDuelId = new AtomicInteger();
	
	protected DuelManager()
	{
	}
	
	public Duel getDuel(int duelId)
	{
		return _duels.get(duelId);
	}
	
	public void addDuel(L2PcInstance playerA, L2PcInstance playerB, boolean partyDuel)
	{
		if ((playerA == null) || (playerB == null))
		{
			return;
		}
		
		// return if a player has PvPFlag
		final String engagedInPvP = "The duel was canceled because a duelist engaged in PvP combat.";
		if (partyDuel)
		{
			boolean playerInPvP = false;
			for (L2PcInstance temp : playerA.getParty().getMembers())
			{
				if (temp.getPvpFlag() != 0)
				{
					playerInPvP = true;
					break;
				}
			}
			if (!playerInPvP)
			{
				for (L2PcInstance temp : playerB.getParty().getMembers())
				{
					if (temp.getPvpFlag() != 0)
					{
						playerInPvP = true;
						break;
					}
				}
			}
			// A player has PvP flag
			if (playerInPvP)
			{
				for (L2PcInstance temp : playerA.getParty().getMembers())
				{
					temp.sendMessage(engagedInPvP);
				}
				for (L2PcInstance temp : playerB.getParty().getMembers())
				{
					temp.sendMessage(engagedInPvP);
				}
				return;
			}
		}
		else
		{
			if ((playerA.getPvpFlag() != 0) || (playerB.getPvpFlag() != 0))
			{
				playerA.sendMessage(engagedInPvP);
				playerB.sendMessage(engagedInPvP);
				return;
			}
		}
		final int duelId = _currentDuelId.incrementAndGet();
		_duels.put(duelId, new Duel(playerA, playerB, partyDuel, duelId));
	}
	
	public void removeDuel(Duel duel)
	{
		_duels.remove(duel.getId());
	}
	
	public void doSurrender(L2PcInstance player)
	{
		if ((player == null) || !player.isInDuel())
		{
			return;
		}
		final Duel duel = getDuel(player.getDuelId());
		duel.doSurrender(player);
	}
	
	/**
	 * Updates player states.
	 * @param player - the dying player
	 */
	public void onPlayerDefeat(L2PcInstance player)
	{
		if ((player == null) || !player.isInDuel())
		{
			return;
		}
		final Duel duel = getDuel(player.getDuelId());
		if (duel != null)
		{
			duel.onPlayerDefeat(player);
		}
	}
	
	/**
	 * Registers a buff which will be removed if the duel ends
	 * @param player
	 * @param buff
	 */
	public void onBuff(L2PcInstance player, Skill buff)
	{
		if ((player == null) || !player.isInDuel() || (buff == null))
		{
			return;
		}
		final Duel duel = getDuel(player.getDuelId());
		if (duel != null)
		{
			duel.onBuff(player, buff);
		}
	}
	
	/**
	 * Broadcasts a packet to the team opposing the given player.
	 * @param player
	 * @param packet
	 */
	public void broadcastToOppositTeam(L2PcInstance player, L2GameServerPacket packet)
	{
		if ((player == null) || !player.isInDuel())
		{
			return;
		}
		final Duel duel = getDuel(player.getDuelId());
		
		if (duel == null)
		{
			return;
		}
		if (duel.getTeamA().contains(player))
		{
			duel.broadcastToTeam2(packet);
		}
		else
		{
			duel.broadcastToTeam1(packet);
		}
	}
	
	/**
	 * Checks if this player might join / start a duel.<br>
	 * @param player
	 * @param target
	 * @param partyDuel
	 * @return true if the player might join/start a duel.
	 */
	public static boolean canDuel(L2PcInstance player, L2PcInstance target, boolean partyDuel)
	{
		SystemMessageId reason = null;
		if (target.isInCombat() || target.isJailed())
		{
			reason = SystemMessageId.C1_CANNOT_DUEL_BECAUSE_C1_IS_CURRENTLY_ENGAGED_IN_BATTLE;
		}
		else if (target.isTransformed())
		{
			reason = SystemMessageId.C1_CANNOT_DUEL_BECAUSE_C1_IS_CURRENTLY_POLYMORPHED;
		}
		else if (target.isDead() || target.isAlikeDead() || ((target.getCurrentHp() < (target.getMaxHp() / 2)) || (target.getCurrentMp() < (target.getMaxMp() / 2))))
		{
			reason = SystemMessageId.C1_CANNOT_DUEL_BECAUSE_C1_S_HP_OR_MP_IS_BELOW_50;
		}
		else if (target.isInDuel())
		{
			reason = SystemMessageId.C1_CANNOT_DUEL_BECAUSE_C1_IS_ALREADY_ENGAGED_IN_A_DUEL;
		}
		else if (target.isInOlympiadMode())
		{
			reason = SystemMessageId.C1_CANNOT_DUEL_BECAUSE_C1_IS_PARTICIPATING_IN_THE_OLYMPIAD_OR_THE_CEREMONY_OF_CHAOS;
		}
		else if (target.isCursedWeaponEquipped())
		{
			reason = SystemMessageId.C1_CANNOT_DUEL_BECAUSE_C1_IS_IN_A_CHAOTIC_OR_PURPLE_STATE;
		}
		else if (target.getPrivateStoreType() != PrivateStoreType.NONE)
		{
			reason = SystemMessageId.C1_CANNOT_DUEL_BECAUSE_C1_IS_CURRENTLY_ENGAGED_IN_A_PRIVATE_STORE_OR_MANUFACTURE;
		}
		else if (target.isMounted() || target.isInBoat())
		{
			reason = SystemMessageId.C1_CANNOT_DUEL_BECAUSE_C1_IS_CURRENTLY_RIDING_A_BOAT_FENRIR_OR_STRIDER;
		}
		else if (target.isFishing())
		{
			reason = SystemMessageId.C1_CANNOT_DUEL_BECAUSE_C1_IS_CURRENTLY_FISHING;
		}
		else if ((!partyDuel && target.isInsideZone(ZoneId.PEACE)) || target.isInsideZone(ZoneId.PVP) || target.isInsideZone(ZoneId.SIEGE))
		{
			reason = SystemMessageId.C1_CANNOT_MAKE_A_CHALLENGE_TO_A_DUEL_BECAUSE_C1_IS_CURRENTLY_IN_A_DUEL_PROHIBITED_AREA_PEACEFUL_ZONE_BATTLE_ZONE_NEAR_WATER_RESTART_PROHIBITED_AREA;
		}
		
		if (reason != null)
		{
			final SystemMessage msg = SystemMessage.getSystemMessage(reason);
			msg.addString(target.getName());
			player.sendPacket(msg);
			return false;
		}
		
		return true;
	}
	
	public static final DuelManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final DuelManager _instance = new DuelManager();
	}
}