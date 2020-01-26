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
package org.l2jmobius.gameserver.network.clientpackets;

import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.commons.concurrent.ThreadPool;
import org.l2jmobius.gameserver.datatables.csv.MapRegionTable;
import org.l2jmobius.gameserver.instancemanager.CastleManager;
import org.l2jmobius.gameserver.instancemanager.ClanHallManager;
import org.l2jmobius.gameserver.instancemanager.FortManager;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.SiegeClan;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.entity.ClanHall;
import org.l2jmobius.gameserver.model.entity.event.CTF;
import org.l2jmobius.gameserver.model.entity.event.DM;
import org.l2jmobius.gameserver.model.entity.event.TvT;
import org.l2jmobius.gameserver.model.entity.siege.Castle;
import org.l2jmobius.gameserver.model.entity.siege.Fort;
import org.l2jmobius.gameserver.network.serverpackets.Revive;
import org.l2jmobius.gameserver.util.IllegalPlayerAction;
import org.l2jmobius.gameserver.util.Util;

/**
 * @version $Revision: 1.7.2.3.2.6 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestRestartPoint extends GameClientPacket
{
	private static final Logger LOGGER = Logger.getLogger(RequestRestartPoint.class.getName());
	
	protected int _requestedPointType;
	protected boolean _continuation;
	
	@Override
	protected void readImpl()
	{
		_requestedPointType = readD();
	}
	
	class DeathTask implements Runnable
	{
		PlayerInstance _player;
		
		DeathTask(PlayerInstance player)
		{
			_player = player;
		}
		
		@Override
		public void run()
		{
			if ((_player._inEventTvT && TvT.isStarted()) || (_player._inEventDM && DM.hasStarted()) || (_player._inEventCTF && CTF.isStarted()))
			{
				_player.sendMessage("You can't restart in Event!");
				return;
			}
			try
			{
				Location loc = null;
				Castle castle = null;
				Fort fort = null;
				
				if (_player.isInJail())
				{
					_requestedPointType = 27;
				}
				else if (_player.isFestivalParticipant())
				{
					_requestedPointType = 4;
				}
				
				if (_player.isPhoenixBlessed())
				{
					_player.stopPhoenixBlessing(null);
				}
				
				switch (_requestedPointType)
				{
					case 1: // to clanhall
					{
						if (_player.getClan() != null)
						{
							if (_player.getClan().getHasHideout() == 0)
							{
								// cheater
								_player.sendMessage("You may not use this respawn point!");
								Util.handleIllegalPlayerAction(_player, "Player " + _player.getName() + " used respawn cheat.", IllegalPlayerAction.PUNISH_KICK);
								return;
							}
							loc = MapRegionTable.getInstance().getTeleToLocation(_player, MapRegionTable.TeleportWhereType.ClanHall);
							if ((ClanHallManager.getInstance().getClanHallByOwner(_player.getClan()) != null) && (ClanHallManager.getInstance().getClanHallByOwner(_player.getClan()).getFunction(ClanHall.FUNC_RESTORE_EXP) != null))
							{
								_player.restoreExp(ClanHallManager.getInstance().getClanHallByOwner(_player.getClan()).getFunction(ClanHall.FUNC_RESTORE_EXP).getLvl());
							}
							break;
						}
						loc = MapRegionTable.getInstance().getTeleToLocation(_player, MapRegionTable.TeleportWhereType.Town);
						break;
					}
					case 2: // to castle
					{
						Boolean isInDefense = false;
						castle = CastleManager.getInstance().getCastle(_player);
						fort = FortManager.getInstance().getFort(_player);
						MapRegionTable.TeleportWhereType teleportWhere = MapRegionTable.TeleportWhereType.Town;
						if ((castle != null) && castle.getSiege().isInProgress() && castle.getSiege().checkIsDefender(_player.getClan()))
						{
							isInDefense = true;
						}
						if ((fort != null) && fort.getSiege().isInProgress() && fort.getSiege().checkIsDefender(_player.getClan()))
						{
							isInDefense = true;
						}
						if ((_player.getClan().getHasCastle() == 0) && (_player.getClan().getHasFort() == 0) && !isInDefense)
						{
							// cheater
							_player.sendMessage("You may not use this respawn point!");
							Util.handleIllegalPlayerAction(_player, "Player " + _player.getName() + " used respawn cheat.", IllegalPlayerAction.PUNISH_KICK);
							return;
						}
						if (CastleManager.getInstance().getCastleByOwner(_player.getClan()) != null)
						{
							teleportWhere = MapRegionTable.TeleportWhereType.Castle;
						}
						else if (FortManager.getInstance().getFortByOwner(_player.getClan()) != null)
						{
							teleportWhere = MapRegionTable.TeleportWhereType.Fortress;
						}
						loc = MapRegionTable.getInstance().getTeleToLocation(_player, teleportWhere);
						break;
					}
					case 3: // to siege HQ
					{
						SiegeClan siegeClan = null;
						castle = CastleManager.getInstance().getCastle(_player);
						fort = FortManager.getInstance().getFort(_player);
						if ((castle != null) && castle.getSiege().isInProgress())
						{
							siegeClan = castle.getSiege().getAttackerClan(_player.getClan());
						}
						else if ((fort != null) && fort.getSiege().isInProgress())
						{
							siegeClan = fort.getSiege().getAttackerClan(_player.getClan());
						}
						if ((siegeClan == null) || siegeClan.getFlag().isEmpty())
						{
							// cheater
							_player.sendMessage("You may not use this respawn point!");
							Util.handleIllegalPlayerAction(_player, "Player " + _player.getName() + " used respawn cheat.", IllegalPlayerAction.PUNISH_KICK);
							return;
						}
						loc = MapRegionTable.getInstance().getTeleToLocation(_player, MapRegionTable.TeleportWhereType.SiegeFlag);
						break;
					}
					case 4: // Fixed or Player is a festival participant
					{
						if (!_player.isGM() && !_player.isFestivalParticipant())
						{
							// cheater
							_player.sendMessage("You may not use this respawn point!");
							Util.handleIllegalPlayerAction(_player, "Player " + _player.getName() + " used respawn cheat.", IllegalPlayerAction.PUNISH_KICK);
							return;
						}
						loc = new Location(_player.getX(), _player.getY(), _player.getZ()); // spawn them where they died
						break;
					}
					case 27: // to jail
					{
						if (!_player.isInJail())
						{
							return;
						}
						loc = new Location(-114356, -249645, -2984);
						break;
					}
					default:
					{
						if ((_player.getKarma() > 0) && Config.ALT_KARMA_TELEPORT_TO_FLORAN)
						{
							loc = new Location(17836, 170178, -3507); // Floran Village
							break;
						}
						loc = MapRegionTable.getInstance().getTeleToLocation(_player, MapRegionTable.TeleportWhereType.Town);
						break;
					}
				}
				
				// Stand up and teleport, proof dvp video.
				_player.setIn7sDungeon(false);
				_player.setIsPendingRevive(true);
				_player.teleToLocation(loc, true);
			}
			catch (Throwable e)
			{
				LOGGER.warning(e.toString());
			}
		}
	}
	
	@Override
	protected void runImpl()
	{
		final PlayerInstance player = getClient().getPlayer();
		
		if (player == null)
		{
			return;
		}
		
		if (player.isFakeDeath())
		{
			player.stopFakeDeath(null);
			player.broadcastPacket(new Revive(player));
			return;
		}
		else if (!player.isAlikeDead())
		{
			LOGGER.warning("Living player [" + player.getName() + "] called RestartPointPacket! Ban this player!");
			return;
		}
		
		final Castle castle = CastleManager.getInstance().getCastle(player.getX(), player.getY(), player.getZ());
		if ((castle != null) && castle.getSiege().isInProgress() && (player.getClan() != null) && castle.getSiege().checkIsAttacker(player.getClan()))
		{
			// Schedule respawn delay for attacker
			ThreadPool.schedule(new DeathTask(player), castle.getSiege().getAttackerRespawnDelay());
			player.sendMessage("You will be re-spawned in " + (castle.getSiege().getAttackerRespawnDelay() / 1000) + " seconds");
			return;
		}
		// run immediately (no need to schedule)
		new DeathTask(player).run();
	}
}