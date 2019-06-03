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
package org.l2jmobius.gameserver.model.olympiad;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.ai.CtrlIntention;
import org.l2jmobius.gameserver.instancemanager.AntiFeedManager;
import org.l2jmobius.gameserver.instancemanager.CastleManager;
import org.l2jmobius.gameserver.instancemanager.FortManager;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.Party;
import org.l2jmobius.gameserver.model.Party.MessageType;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Summon;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.events.EventDispatcher;
import org.l2jmobius.gameserver.model.events.impl.olympiad.OnOlympiadMatchResult;
import org.l2jmobius.gameserver.model.instancezone.Instance;
import org.l2jmobius.gameserver.model.skills.Skill;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ExOlympiadMatchResult;
import org.l2jmobius.gameserver.network.serverpackets.ExOlympiadMode;
import org.l2jmobius.gameserver.network.serverpackets.ExOlympiadUserInfo;
import org.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;
import org.l2jmobius.gameserver.network.serverpackets.SkillCoolTime;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * @author GodKratos, Pere, DS
 */
public class OlympiadGameNormal
{
	private int _damageP1 = 0;
	private int _damageP2 = 0;
	
	private Participant _playerOne;
	private Participant _playerTwo;
	
	private static final Logger LOGGER = Logger.getLogger(OlympiadGameNormal.class.getName());
	private static final Logger LOGGER_OLYMPIAD = Logger.getLogger("olympiad");
	
	private static final String POINTS = "olympiad_points";
	private static final String COMP_DONE = "competitions_done";
	private static final String COMP_WON = "competitions_won";
	private static final String COMP_LOST = "competitions_lost";
	private static final String COMP_DRAWN = "competitions_drawn";
	private static final String COMP_DONE_WEEK = "competitions_done_week";
	
	private long _startTime = 0;
	private final boolean _aborted = false;
	private final int _stadiumId;
	
	public OlympiadGameNormal(int id, Participant[] opponents)
	{
		_stadiumId = id;
		
		_playerOne = opponents[0];
		_playerTwo = opponents[1];
		
		_playerOne.getPlayer().setOlympiadGameId(id);
		_playerTwo.getPlayer().setOlympiadGameId(id);
	}
	
	protected static OlympiadGameNormal createGame(int id, Set<Integer> list)
	{
		final Participant[] opponents = OlympiadGameNormal.createListOfParticipants(list);
		if (opponents == null)
		{
			return null;
		}
		
		return new OlympiadGameNormal(id, opponents);
	}
	
	protected static Participant[] createListOfParticipants(Set<Integer> set)
	{
		if ((set == null) || set.isEmpty() || (set.size() < 2))
		{
			return null;
		}
		int playerOneObjectId = 0;
		int playerTwoObjectId = 0;
		PlayerInstance playerOne = null;
		PlayerInstance playerTwo = null;
		
		while (set.size() > 1)
		{
			int random = Rnd.get(set.size());
			Iterator<Integer> iter = set.iterator();
			while (iter.hasNext())
			{
				playerOneObjectId = iter.next();
				if (--random < 0)
				{
					iter.remove();
					break;
				}
			}
			
			playerOne = World.getInstance().getPlayer(playerOneObjectId);
			if ((playerOne == null) || !playerOne.isOnline())
			{
				continue;
			}
			
			random = Rnd.get(set.size());
			iter = set.iterator();
			while (iter.hasNext())
			{
				playerTwoObjectId = iter.next();
				if (--random < 0)
				{
					iter.remove();
					break;
				}
			}
			
			playerTwo = World.getInstance().getPlayer(playerTwoObjectId);
			if ((playerTwo == null) || !playerTwo.isOnline())
			{
				set.add(playerOneObjectId);
				continue;
			}
			
			final Participant[] result = new Participant[2];
			result[0] = new Participant(playerOne, 1);
			result[1] = new Participant(playerTwo, 2);
			
			return result;
		}
		return null;
	}
	
	public final boolean containsParticipant(int playerId)
	{
		return ((_playerOne != null) && (_playerOne.getObjectId() == playerId)) || ((_playerTwo != null) && (_playerTwo.getObjectId() == playerId));
	}
	
	public final void sendOlympiadInfo(Creature creature)
	{
		creature.sendPacket(new ExOlympiadUserInfo(_playerOne));
		creature.sendPacket(new ExOlympiadUserInfo(_playerTwo));
	}
	
	public final void broadcastOlympiadInfo(OlympiadStadium stadium)
	{
		stadium.broadcastPacket(new ExOlympiadUserInfo(_playerOne));
		stadium.broadcastPacket(new ExOlympiadUserInfo(_playerTwo));
	}
	
	protected final void broadcastPacket(IClientOutgoingPacket packet)
	{
		if (_playerOne.updatePlayer())
		{
			_playerOne.getPlayer().sendPacket(packet);
		}
		
		if (_playerTwo.updatePlayer())
		{
			_playerTwo.getPlayer().sendPacket(packet);
		}
	}
	
	protected final boolean portPlayersToArena(List<Location> spawns, Instance instance)
	{
		boolean result = true;
		try
		{
			result &= portPlayerToArena(_playerOne, spawns.get(0), _stadiumId, instance);
			result &= portPlayerToArena(_playerTwo, spawns.get(spawns.size() / 2), _stadiumId, instance);
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, "", e);
			return false;
		}
		return result;
	}
	
	protected boolean needBuffers()
	{
		return true;
	}
	
	protected final void removals()
	{
		if (_aborted)
		{
			return;
		}
		
		removals(_playerOne.getPlayer(), true);
		removals(_playerTwo.getPlayer(), true);
	}
	
	protected final boolean makeCompetitionStart()
	{
		if ((_playerOne.getPlayer() == null) || (_playerTwo.getPlayer() == null))
		{
			return false;
		}
		
		_playerOne.getPlayer().setIsOlympiadStart(true);
		_playerOne.getPlayer().updateEffectIcons();
		_playerTwo.getPlayer().setIsOlympiadStart(true);
		_playerTwo.getPlayer().updateEffectIcons();
		return true;
	}
	
	protected final void cleanEffects()
	{
		if (checkOnline(_playerTwo) && (_playerOne.getPlayer().getOlympiadGameId() == _stadiumId))
		{
			cleanEffects(_playerOne.getPlayer());
		}
		
		if (checkOnline(_playerTwo) && (_playerTwo.getPlayer().getOlympiadGameId() == _stadiumId))
		{
			cleanEffects(_playerTwo.getPlayer());
		}
	}
	
	protected final void portPlayersBack()
	{
		if (checkOnline(_playerTwo))
		{
			portPlayerBack(_playerOne.getPlayer());
		}
		if (checkOnline(_playerTwo))
		{
			portPlayerBack(_playerTwo.getPlayer());
		}
	}
	
	protected final void playersStatusBack()
	{
		if (checkOnline(_playerTwo) && (_playerOne.getPlayer().getOlympiadGameId() == _stadiumId))
		{
			playerStatusBack(_playerOne.getPlayer());
		}
		
		if (checkOnline(_playerTwo) && (_playerTwo.getPlayer().getOlympiadGameId() == _stadiumId))
		{
			playerStatusBack(_playerTwo.getPlayer());
		}
	}
	
	private boolean checkOnline(Participant player)
	{
		return (player.getPlayer() != null) && !player.isDefaulted() && !player.isDisconnected();
	}
	
	protected final void clearPlayers()
	{
		_playerOne.setPlayer(null);
		_playerOne = null;
		_playerTwo.setPlayer(null);
		_playerTwo = null;
	}
	
	protected final void handleDisconnect(PlayerInstance player)
	{
		if (player.getObjectId() == _playerOne.getObjectId())
		{
			_playerOne.setDisconnected(true);
		}
		else if (player.getObjectId() == _playerTwo.getObjectId())
		{
			_playerTwo.setDisconnected(true);
		}
	}
	
	protected final boolean checkBattleStatus()
	{
		if (_aborted)
		{
			return false;
		}
		
		if ((_playerOne.getPlayer() == null) || _playerOne.isDisconnected())
		{
			return false;
		}
		
		if ((_playerTwo.getPlayer() == null) || _playerTwo.isDisconnected())
		{
			return false;
		}
		
		return true;
	}
	
	protected final boolean haveWinner()
	{
		if (!checkBattleStatus())
		{
			return true;
		}
		
		boolean playerOneLost = true;
		try
		{
			if (_playerOne.getPlayer().getOlympiadGameId() == _stadiumId)
			{
				playerOneLost = _playerOne.getPlayer().isDead();
			}
		}
		catch (Exception e)
		{
			playerOneLost = true;
		}
		
		boolean playerTwoLost = true;
		try
		{
			if (_playerTwo.getPlayer().getOlympiadGameId() == _stadiumId)
			{
				playerTwoLost = _playerTwo.getPlayer().isDead();
			}
		}
		catch (Exception e)
		{
			playerTwoLost = true;
		}
		
		return playerOneLost || playerTwoLost;
	}
	
	protected void validateWinner(OlympiadStadium stadium)
	{
		if (_aborted)
		{
			return;
		}
		
		ExOlympiadMatchResult result = null;
		
		boolean tie = false;
		int winside = 0;
		
		final List<OlympiadInfo> list1 = new ArrayList<>(1);
		final List<OlympiadInfo> list2 = new ArrayList<>(1);
		
		final boolean _pOneCrash = ((_playerOne.getPlayer() == null) || _playerOne.isDisconnected());
		final boolean _pTwoCrash = ((_playerTwo.getPlayer() == null) || _playerTwo.isDisconnected());
		
		final int playerOnePoints = _playerOne.getStats().getInt(POINTS);
		final int playerTwoPoints = _playerTwo.getStats().getInt(POINTS);
		int pointDiff = Math.min(playerOnePoints, playerTwoPoints) / Config.ALT_OLY_DIVIDER;
		if (pointDiff <= 0)
		{
			pointDiff = 1;
		}
		else if (pointDiff > Config.ALT_OLY_MAX_POINTS)
		{
			pointDiff = Config.ALT_OLY_MAX_POINTS;
		}
		
		int points;
		SystemMessage sm;
		
		// Check for if a player defaulted before battle started
		if (_playerOne.isDefaulted() || _playerTwo.isDefaulted())
		{
			try
			{
				if (_playerOne.isDefaulted())
				{
					try
					{
						points = Math.min(playerOnePoints / 3, Config.ALT_OLY_MAX_POINTS);
						removePointsFromParticipant(_playerOne, points);
						list1.add(new OlympiadInfo(_playerOne.getName(), _playerOne.getClanName(), _playerOne.getClanId(), _playerOne.getBaseClass(), _damageP1, playerOnePoints - points, -points));
						
						winside = 2;
						
						if (Config.ALT_OLY_LOG_FIGHTS)
						{
							LOGGER_OLYMPIAD.info(_playerOne.getName() + " default," + _playerOne + "," + _playerTwo + ",0,0,0,0," + points);
						}
					}
					catch (Exception e)
					{
						LOGGER.log(Level.WARNING, "Exception on validateWinner(): " + e.getMessage(), e);
					}
				}
				if (_playerTwo.isDefaulted())
				{
					try
					{
						points = Math.min(playerTwoPoints / 3, Config.ALT_OLY_MAX_POINTS);
						removePointsFromParticipant(_playerTwo, points);
						list2.add(new OlympiadInfo(_playerTwo.getName(), _playerTwo.getClanName(), _playerTwo.getClanId(), _playerTwo.getBaseClass(), _damageP2, playerTwoPoints - points, -points));
						
						if (winside == 2)
						{
							tie = true;
						}
						else
						{
							winside = 1;
						}
						
						if (Config.ALT_OLY_LOG_FIGHTS)
						{
							LOGGER_OLYMPIAD.info(_playerTwo.getName() + " default," + _playerOne + "," + _playerTwo + ",0,0,0,0," + points);
						}
					}
					catch (Exception e)
					{
						LOGGER.log(Level.WARNING, "Exception on validateWinner(): " + e.getMessage(), e);
					}
				}
				if (winside == 1)
				{
					result = new ExOlympiadMatchResult(tie, winside, list1, list2);
				}
				else
				{
					result = new ExOlympiadMatchResult(tie, winside, list2, list1);
				}
				stadium.broadcastPacket(result);
				return;
			}
			catch (Exception e)
			{
				LOGGER.log(Level.WARNING, "Exception on validateWinner(): " + e.getMessage(), e);
				return;
			}
		}
		
		// Create results for players if a player crashed
		if (_pOneCrash || _pTwoCrash)
		{
			try
			{
				if (_pTwoCrash && !_pOneCrash)
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.CONGRATULATIONS_C1_YOU_WIN_THE_MATCH);
					sm.addString(_playerOne.getName());
					stadium.broadcastPacket(sm);
					
					_playerOne.updateStat(COMP_WON, 1);
					addPointsToParticipant(_playerOne, pointDiff);
					list1.add(new OlympiadInfo(_playerOne.getName(), _playerOne.getClanName(), _playerOne.getClanId(), _playerOne.getBaseClass(), _damageP1, playerOnePoints + pointDiff, pointDiff));
					
					_playerTwo.updateStat(COMP_LOST, 1);
					removePointsFromParticipant(_playerTwo, pointDiff);
					list2.add(new OlympiadInfo(_playerTwo.getName(), _playerTwo.getClanName(), _playerTwo.getClanId(), _playerTwo.getBaseClass(), _damageP2, playerTwoPoints - pointDiff, -pointDiff));
					
					winside = 1;
					
					if (Config.ALT_OLY_LOG_FIGHTS)
					{
						LOGGER_OLYMPIAD.info(_playerTwo.getName() + " crash," + _playerOne + "," + _playerTwo + ",0,0,0,0," + pointDiff);
					}
					
					// Notify to scripts
					EventDispatcher.getInstance().notifyEventAsync(new OnOlympiadMatchResult(_playerOne, _playerTwo), Olympiad.getInstance());
				}
				else if (_pOneCrash && !_pTwoCrash)
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.CONGRATULATIONS_C1_YOU_WIN_THE_MATCH);
					sm.addString(_playerTwo.getName());
					stadium.broadcastPacket(sm);
					
					_playerTwo.updateStat(COMP_WON, 1);
					addPointsToParticipant(_playerTwo, pointDiff);
					list2.add(new OlympiadInfo(_playerTwo.getName(), _playerTwo.getClanName(), _playerTwo.getClanId(), _playerTwo.getBaseClass(), _damageP2, playerTwoPoints + pointDiff, pointDiff));
					
					_playerOne.updateStat(COMP_LOST, 1);
					removePointsFromParticipant(_playerOne, pointDiff);
					list1.add(new OlympiadInfo(_playerOne.getName(), _playerOne.getClanName(), _playerOne.getClanId(), _playerOne.getBaseClass(), _damageP1, playerOnePoints - pointDiff, -pointDiff));
					
					winside = 2;
					
					if (Config.ALT_OLY_LOG_FIGHTS)
					{
						LOGGER_OLYMPIAD.info(_playerOne.getName() + " crash," + _playerOne + "," + _playerTwo + ",0,0,0,0," + pointDiff);
					}
					// Notify to scripts
					EventDispatcher.getInstance().notifyEventAsync(new OnOlympiadMatchResult(_playerTwo, _playerOne), Olympiad.getInstance());
				}
				else if (_pOneCrash && _pTwoCrash)
				{
					stadium.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.THERE_IS_NO_VICTOR_THE_MATCH_ENDS_IN_A_TIE));
					
					_playerOne.updateStat(COMP_LOST, 1);
					removePointsFromParticipant(_playerOne, pointDiff);
					list1.add(new OlympiadInfo(_playerOne.getName(), _playerOne.getClanName(), _playerOne.getClanId(), _playerOne.getBaseClass(), _damageP1, playerOnePoints - pointDiff, -pointDiff));
					
					_playerTwo.updateStat(COMP_LOST, 1);
					removePointsFromParticipant(_playerTwo, pointDiff);
					list2.add(new OlympiadInfo(_playerTwo.getName(), _playerTwo.getClanName(), _playerTwo.getClanId(), _playerTwo.getBaseClass(), _damageP2, playerTwoPoints - pointDiff, -pointDiff));
					
					tie = true;
					
					if (Config.ALT_OLY_LOG_FIGHTS)
					{
						LOGGER_OLYMPIAD.info("both crash," + _playerOne.getName() + "," + _playerOne + ",0,0,0,0," + _playerTwo + "," + pointDiff);
					}
				}
				
				_playerOne.updateStat(COMP_DONE, 1);
				_playerTwo.updateStat(COMP_DONE, 1);
				_playerOne.updateStat(COMP_DONE_WEEK, 1);
				_playerTwo.updateStat(COMP_DONE_WEEK, 1);
				
				if (winside == 1)
				{
					result = new ExOlympiadMatchResult(tie, winside, list1, list2);
				}
				else
				{
					result = new ExOlympiadMatchResult(tie, winside, list2, list1);
				}
				stadium.broadcastPacket(result);
				
				// Notify to scripts
				EventDispatcher.getInstance().notifyEventAsync(new OnOlympiadMatchResult(null, _playerOne), Olympiad.getInstance());
				EventDispatcher.getInstance().notifyEventAsync(new OnOlympiadMatchResult(null, _playerTwo), Olympiad.getInstance());
				return;
			}
			catch (Exception e)
			{
				LOGGER.log(Level.WARNING, "Exception on validateWinner(): " + e.getMessage(), e);
				return;
			}
		}
		
		try
		{
			String winner = "draw";
			
			// Calculate Fight time
			final long _fightTime = (System.currentTimeMillis() - _startTime);
			
			double playerOneHp = 0;
			if ((_playerOne.getPlayer() != null) && !_playerOne.getPlayer().isDead())
			{
				playerOneHp = _playerOne.getPlayer().getCurrentHp() + _playerOne.getPlayer().getCurrentCp();
				if (playerOneHp < 0.5)
				{
					playerOneHp = 0;
				}
			}
			
			double playerTwoHp = 0;
			if ((_playerTwo.getPlayer() != null) && !_playerTwo.getPlayer().isDead())
			{
				playerTwoHp = _playerTwo.getPlayer().getCurrentHp() + _playerTwo.getPlayer().getCurrentCp();
				if (playerTwoHp < 0.5)
				{
					playerTwoHp = 0;
				}
			}
			
			// if players crashed, search if they've relogged
			_playerOne.updatePlayer();
			_playerTwo.updatePlayer();
			
			if (((_playerOne.getPlayer() == null) || !_playerOne.getPlayer().isOnline()) && ((_playerTwo.getPlayer() == null) || !_playerTwo.getPlayer().isOnline()))
			{
				_playerOne.updateStat(COMP_DRAWN, 1);
				_playerTwo.updateStat(COMP_DRAWN, 1);
				sm = SystemMessage.getSystemMessage(SystemMessageId.THERE_IS_NO_VICTOR_THE_MATCH_ENDS_IN_A_TIE);
				stadium.broadcastPacket(sm);
			}
			else if ((_playerTwo.getPlayer() == null) || !_playerTwo.getPlayer().isOnline() || ((playerTwoHp == 0) && (playerOneHp != 0)) || ((_damageP1 > _damageP2) && (playerTwoHp != 0) && (playerOneHp != 0)))
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.CONGRATULATIONS_C1_YOU_WIN_THE_MATCH);
				sm.addString(_playerOne.getName());
				stadium.broadcastPacket(sm);
				
				_playerOne.updateStat(COMP_WON, 1);
				_playerTwo.updateStat(COMP_LOST, 1);
				
				addPointsToParticipant(_playerOne, pointDiff);
				list1.add(new OlympiadInfo(_playerOne.getName(), _playerOne.getClanName(), _playerOne.getClanId(), _playerOne.getBaseClass(), _damageP1, playerOnePoints + pointDiff, pointDiff));
				
				removePointsFromParticipant(_playerTwo, pointDiff);
				list2.add(new OlympiadInfo(_playerTwo.getName(), _playerTwo.getClanName(), _playerTwo.getClanId(), _playerTwo.getBaseClass(), _damageP2, playerTwoPoints - pointDiff, -pointDiff));
				winner = _playerOne.getName() + " won";
				
				winside = 1;
				
				// Save Fight Result
				saveResults(_playerOne, _playerTwo, 1, _startTime, _fightTime);
				
				// Notify to scripts
				EventDispatcher.getInstance().notifyEventAsync(new OnOlympiadMatchResult(_playerOne, _playerTwo), Olympiad.getInstance());
			}
			else if ((_playerOne.getPlayer() == null) || !_playerOne.getPlayer().isOnline() || ((playerOneHp == 0) && (playerTwoHp != 0)) || ((_damageP2 > _damageP1) && (playerOneHp != 0) && (playerTwoHp != 0)))
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.CONGRATULATIONS_C1_YOU_WIN_THE_MATCH);
				sm.addString(_playerTwo.getName());
				stadium.broadcastPacket(sm);
				
				_playerTwo.updateStat(COMP_WON, 1);
				_playerOne.updateStat(COMP_LOST, 1);
				
				addPointsToParticipant(_playerTwo, pointDiff);
				list2.add(new OlympiadInfo(_playerTwo.getName(), _playerTwo.getClanName(), _playerTwo.getClanId(), _playerTwo.getBaseClass(), _damageP2, playerTwoPoints + pointDiff, pointDiff));
				
				removePointsFromParticipant(_playerOne, pointDiff);
				list1.add(new OlympiadInfo(_playerOne.getName(), _playerOne.getClanName(), _playerOne.getClanId(), _playerOne.getBaseClass(), _damageP1, playerOnePoints - pointDiff, -pointDiff));
				
				winner = _playerTwo.getName() + " won";
				winside = 2;
				
				// Save Fight Result
				saveResults(_playerOne, _playerTwo, 2, _startTime, _fightTime);
				
				// Notify to scripts
				EventDispatcher.getInstance().notifyEventAsync(new OnOlympiadMatchResult(_playerTwo, _playerOne), Olympiad.getInstance());
			}
			else
			{
				// Save Fight Result
				saveResults(_playerOne, _playerTwo, 0, _startTime, _fightTime);
				
				sm = SystemMessage.getSystemMessage(SystemMessageId.THERE_IS_NO_VICTOR_THE_MATCH_ENDS_IN_A_TIE);
				stadium.broadcastPacket(sm);
				
				int value = Math.min(playerOnePoints / Config.ALT_OLY_DIVIDER, Config.ALT_OLY_MAX_POINTS);
				
				removePointsFromParticipant(_playerOne, value);
				list1.add(new OlympiadInfo(_playerOne.getName(), _playerOne.getClanName(), _playerOne.getClanId(), _playerOne.getBaseClass(), _damageP1, playerOnePoints - value, -value));
				
				value = Math.min(playerTwoPoints / Config.ALT_OLY_DIVIDER, Config.ALT_OLY_MAX_POINTS);
				removePointsFromParticipant(_playerTwo, value);
				list2.add(new OlympiadInfo(_playerTwo.getName(), _playerTwo.getClanName(), _playerTwo.getClanId(), _playerTwo.getBaseClass(), _damageP2, playerTwoPoints - value, -value));
				
				tie = true;
			}
			
			_playerOne.updateStat(COMP_DONE, 1);
			_playerTwo.updateStat(COMP_DONE, 1);
			_playerOne.updateStat(COMP_DONE_WEEK, 1);
			_playerTwo.updateStat(COMP_DONE_WEEK, 1);
			
			if (winside == 1)
			{
				result = new ExOlympiadMatchResult(tie, winside, list1, list2);
			}
			else
			{
				result = new ExOlympiadMatchResult(tie, winside, list2, list1);
			}
			stadium.broadcastPacket(result);
			
			if (Config.ALT_OLY_LOG_FIGHTS)
			{
				LOGGER_OLYMPIAD.info(winner + "," + _playerOne.getName() + "," + _playerOne + "," + _playerTwo + "," + playerOneHp + "," + playerTwoHp + "," + _damageP1 + "," + _damageP2 + "," + pointDiff);
			}
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, "Exception on validateWinner(): " + e.getMessage(), e);
		}
	}
	
	protected final void addDamage(PlayerInstance player, int damage)
	{
		if ((_playerOne.getPlayer() == null) || (_playerTwo.getPlayer() == null))
		{
			return;
		}
		if (player == _playerOne.getPlayer())
		{
			_damageP1 += damage;
		}
		else if (player == _playerTwo.getPlayer())
		{
			_damageP2 += damage;
		}
	}
	
	public final String[] getPlayerNames()
	{
		return new String[]
		{
			_playerOne.getName(),
			_playerTwo.getName()
		};
	}
	
	public boolean checkDefaulted()
	{
		SystemMessage reason;
		_playerOne.updatePlayer();
		_playerTwo.updatePlayer();
		
		reason = checkDefaulted(_playerOne.getPlayer());
		if (reason != null)
		{
			_playerOne.setDefaulted(true);
			if (_playerTwo.getPlayer() != null)
			{
				_playerTwo.getPlayer().sendPacket(reason);
			}
		}
		
		reason = checkDefaulted(_playerTwo.getPlayer());
		if (reason != null)
		{
			_playerTwo.setDefaulted(true);
			if (_playerOne.getPlayer() != null)
			{
				_playerOne.getPlayer().sendPacket(reason);
			}
		}
		
		return _playerOne.isDefaulted() || _playerTwo.isDefaulted();
	}
	
	public final void resetDamage()
	{
		_damageP1 = 0;
		_damageP2 = 0;
	}
	
	protected static void saveResults(Participant one, Participant two, int winner, long startTime, long fightTime)
	{
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement statement = con.prepareStatement("INSERT INTO olympiad_fights (charOneId, charTwoId, charOneClass, charTwoClass, winner, start, time) values(?,?,?,?,?,?,?)"))
		{
			statement.setInt(1, one.getObjectId());
			statement.setInt(2, two.getObjectId());
			statement.setInt(3, one.getBaseClass());
			statement.setInt(4, two.getBaseClass());
			statement.setInt(5, winner);
			statement.setLong(6, startTime);
			statement.setLong(7, fightTime);
			statement.execute();
		}
		catch (SQLException e)
		{
			LOGGER.log(Level.SEVERE, "SQL exception while saving olympiad fight.", e);
		}
	}
	
	protected void healPlayers()
	{
		final PlayerInstance player1 = _playerOne.getPlayer();
		if (player1 != null)
		{
			player1.setCurrentCp(player1.getMaxCp());
			player1.setCurrentHp(player1.getMaxHp());
			player1.setCurrentMp(player1.getMaxMp());
		}
		
		final PlayerInstance player2 = _playerTwo.getPlayer();
		if (player2 != null)
		{
			player2.setCurrentCp(player2.getMaxCp());
			player2.setCurrentHp(player2.getMaxHp());
			player2.setCurrentMp(player2.getMaxMp());
		}
	}
	
	protected void untransformPlayers()
	{
		final PlayerInstance player1 = _playerOne.getPlayer();
		if ((player1 != null) && player1.isTransformed())
		{
			player1.stopTransformation(true);
		}
		
		final PlayerInstance player2 = _playerTwo.getPlayer();
		if ((player2 != null) && player2.isTransformed())
		{
			player2.stopTransformation(true);
		}
	}
	
	public final void makePlayersInvul()
	{
		if (_playerOne.getPlayer() != null)
		{
			_playerOne.getPlayer().setIsInvul(true);
		}
		if (_playerTwo.getPlayer() != null)
		{
			_playerTwo.getPlayer().setIsInvul(true);
		}
	}
	
	public final void removePlayersInvul()
	{
		if (_playerOne.getPlayer() != null)
		{
			_playerOne.getPlayer().setIsInvul(false);
		}
		if (_playerTwo.getPlayer() != null)
		{
			_playerTwo.getPlayer().setIsInvul(false);
		}
	}
	
	public final boolean isAborted()
	{
		return _aborted;
	}
	
	public final int getStadiumId()
	{
		return _stadiumId;
	}
	
	protected boolean makeCompetitionStart1()
	{
		_startTime = System.currentTimeMillis();
		return !_aborted;
	}
	
	protected final void addPointsToParticipant(Participant par, int points)
	{
		par.updateStat(POINTS, points);
		final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_HAS_EARNED_S2_POINTS_IN_THE_OLYMPIAD_GAMES);
		sm.addString(par.getName());
		sm.addInt(points);
		broadcastPacket(sm);
	}
	
	protected final void removePointsFromParticipant(Participant par, int points)
	{
		par.updateStat(POINTS, -points);
		final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_HAS_LOST_S2_POINTS_IN_THE_OLYMPIAD_GAMES);
		sm.addString(par.getName());
		sm.addInt(points);
		broadcastPacket(sm);
	}
	
	/**
	 * Function return null if player passed all checks or SystemMessage with reason for broadcast to opponent(s).
	 * @param player
	 * @return
	 */
	protected static SystemMessage checkDefaulted(PlayerInstance player)
	{
		if ((player == null) || !player.isOnline())
		{
			return SystemMessage.getSystemMessage(SystemMessageId.YOUR_OPPONENT_MADE_HASTE_WITH_THEIR_TAIL_BETWEEN_THEIR_LEGS_THE_MATCH_HAS_BEEN_CANCELLED);
		}
		
		if ((player.getClient() == null) || player.getClient().isDetached())
		{
			return SystemMessage.getSystemMessage(SystemMessageId.YOUR_OPPONENT_MADE_HASTE_WITH_THEIR_TAIL_BETWEEN_THEIR_LEGS_THE_MATCH_HAS_BEEN_CANCELLED);
		}
		
		// safety precautions
		if (player.inObserverMode())
		{
			return SystemMessage.getSystemMessage(SystemMessageId.YOUR_OPPONENT_DOES_NOT_MEET_THE_REQUIREMENTS_TO_DO_BATTLE_THE_MATCH_HAS_BEEN_CANCELLED);
		}
		
		SystemMessage sm;
		if (player.isDead())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_CURRENTLY_DEAD_AND_CANNOT_PARTICIPATE_IN_THE_OLYMPIAD);
			sm.addPcName(player);
			player.sendPacket(sm);
			return SystemMessage.getSystemMessage(SystemMessageId.YOUR_OPPONENT_DOES_NOT_MEET_THE_REQUIREMENTS_TO_DO_BATTLE_THE_MATCH_HAS_BEEN_CANCELLED);
		}
		if (player.isSubClassActive())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_DOES_NOT_MEET_THE_PARTICIPATION_REQUIREMENTS_YOU_CANNOT_PARTICIPATE_IN_THE_OLYMPIAD_BECAUSE_YOU_HAVE_CHANGED_YOUR_CLASS_TO_SUBCLASS);
			sm.addPcName(player);
			player.sendPacket(sm);
			return SystemMessage.getSystemMessage(SystemMessageId.YOUR_OPPONENT_DOES_NOT_MEET_THE_REQUIREMENTS_TO_DO_BATTLE_THE_MATCH_HAS_BEEN_CANCELLED);
		}
		if (player.isCursedWeaponEquipped())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_DOES_NOT_MEET_THE_PARTICIPATION_REQUIREMENTS_THE_OWNER_OF_S2_CANNOT_PARTICIPATE_IN_THE_OLYMPIAD);
			sm.addPcName(player);
			sm.addItemName(player.getCursedWeaponEquippedId());
			player.sendPacket(sm);
			return SystemMessage.getSystemMessage(SystemMessageId.YOUR_OPPONENT_DOES_NOT_MEET_THE_REQUIREMENTS_TO_DO_BATTLE_THE_MATCH_HAS_BEEN_CANCELLED);
		}
		if (!player.isInventoryUnder90(true))
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_DOES_NOT_MEET_THE_PARTICIPATION_REQUIREMENTS_AS_THE_INVENTORY_WEIGHT_SLOT_IS_FILLED_BEYOND_80);
			sm.addPcName(player);
			player.sendPacket(sm);
			return SystemMessage.getSystemMessage(SystemMessageId.YOUR_OPPONENT_DOES_NOT_MEET_THE_REQUIREMENTS_TO_DO_BATTLE_THE_MATCH_HAS_BEEN_CANCELLED);
		}
		
		return null;
	}
	
	protected static boolean portPlayerToArena(Participant par, Location loc, int id, Instance instance)
	{
		final PlayerInstance player = par.getPlayer();
		if ((player == null) || !player.isOnline())
		{
			return false;
		}
		
		try
		{
			player.setLastLocation();
			if (player.isSitting())
			{
				player.standUp();
			}
			player.setTarget(null);
			
			player.setOlympiadGameId(id);
			player.setIsInOlympiadMode(true);
			player.setIsOlympiadStart(false);
			player.setOlympiadSide(par.getSide());
			player.teleToLocation(loc, instance);
			player.sendPacket(new ExOlympiadMode(2));
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, e.getMessage(), e);
			return false;
		}
		return true;
	}
	
	protected static void removals(PlayerInstance player, boolean removeParty)
	{
		try
		{
			if (player == null)
			{
				return;
			}
			
			// Remove Buffs
			player.stopAllEffectsExceptThoseThatLastThroughDeath();
			
			// Remove Clan Skills
			if (player.getClan() != null)
			{
				player.getClan().removeSkillEffects(player);
				if (player.getClan().getCastleId() > 0)
				{
					CastleManager.getInstance().getCastleByOwner(player.getClan()).removeResidentialSkills(player);
				}
				if (player.getClan().getFortId() > 0)
				{
					FortManager.getInstance().getFortByOwner(player.getClan()).removeResidentialSkills(player);
				}
			}
			// Abort casting if player casting
			player.abortAttack();
			player.abortCast();
			
			// Force the character to be visible
			player.setInvisible(false);
			
			// Heal Player fully
			player.setCurrentCp(player.getMaxCp());
			player.setCurrentHp(player.getMaxHp());
			player.setCurrentMp(player.getMaxMp());
			
			// Remove Summon's Buffs
			if (player.hasSummon())
			{
				final Summon pet = player.getPet();
				if (pet != null)
				{
					pet.unSummon(player);
				}
				
				player.getServitors().values().forEach(s ->
				{
					s.stopAllEffectsExceptThoseThatLastThroughDeath();
					s.abortAttack();
					s.abortCast();
				});
			}
			
			// stop any cubic that has been given by other player.
			player.stopCubicsByOthers();
			
			// Remove player from his party
			if (removeParty)
			{
				final Party party = player.getParty();
				if (party != null)
				{
					party.removePartyMember(player, MessageType.EXPELLED);
				}
			}
			// Remove Agathion
			if (player.getAgathionId() > 0)
			{
				player.setAgathionId(0);
				player.broadcastUserInfo();
			}
			
			player.checkItemRestriction();
			
			// Remove shot automation
			player.disableAutoShotsAll();
			
			// Discharge any active shots
			player.unchargeAllShots();
			
			// enable skills with cool time <= 15 minutes
			for (Skill skill : player.getAllSkills())
			{
				if (skill.getReuseDelay() <= 900000)
				{
					player.enableSkill(skill);
				}
			}
			
			player.sendSkillList();
			player.sendPacket(new SkillCoolTime(player));
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, e.getMessage(), e);
		}
	}
	
	protected static void cleanEffects(PlayerInstance player)
	{
		try
		{
			// prevent players kill each other
			player.setIsOlympiadStart(false);
			player.setTarget(null);
			player.abortAttack();
			player.abortCast();
			player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			
			if (player.isDead())
			{
				player.setIsDead(false);
			}
			
			player.stopAllEffectsExceptThoseThatLastThroughDeath();
			player.clearSouls();
			player.clearCharges();
			if (player.getAgathionId() > 0)
			{
				player.setAgathionId(0);
			}
			final Summon pet = player.getPet();
			if ((pet != null) && !pet.isDead())
			{
				pet.setTarget(null);
				pet.abortAttack();
				pet.abortCast();
				pet.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
				pet.stopAllEffectsExceptThoseThatLastThroughDeath();
			}
			
			player.getServitors().values().stream().filter(s -> !s.isDead()).forEach(s ->
			{
				s.setTarget(null);
				s.abortAttack();
				s.abortCast();
				s.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
				s.stopAllEffectsExceptThoseThatLastThroughDeath();
			});
			
			player.setCurrentCp(player.getMaxCp());
			player.setCurrentHp(player.getMaxHp());
			player.setCurrentMp(player.getMaxMp());
			player.getStatus().startHpMpRegeneration();
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, e.getMessage(), e);
		}
	}
	
	protected static void playerStatusBack(PlayerInstance player)
	{
		try
		{
			if (player.isTransformed())
			{
				player.untransform();
			}
			
			player.setIsInOlympiadMode(false);
			player.setIsOlympiadStart(false);
			player.setOlympiadSide(-1);
			player.setOlympiadGameId(-1);
			player.sendPacket(new ExOlympiadMode(0));
			
			// Add Clan Skills
			if (player.getClan() != null)
			{
				player.getClan().addSkillEffects(player);
				if (player.getClan().getCastleId() > 0)
				{
					CastleManager.getInstance().getCastleByOwner(player.getClan()).giveResidentialSkills(player);
				}
				if (player.getClan().getFortId() > 0)
				{
					FortManager.getInstance().getFortByOwner(player.getClan()).giveResidentialSkills(player);
				}
				player.sendSkillList();
			}
			
			// heal again after adding clan skills
			player.setCurrentCp(player.getMaxCp());
			player.setCurrentHp(player.getMaxHp());
			player.setCurrentMp(player.getMaxMp());
			player.getStatus().startHpMpRegeneration();
			
			if (Config.DUALBOX_CHECK_MAX_OLYMPIAD_PARTICIPANTS_PER_IP > 0)
			{
				AntiFeedManager.getInstance().removePlayer(AntiFeedManager.OLYMPIAD_ID, player);
			}
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, "playerStatusBack()", e);
		}
	}
	
	private static void portPlayerBack(PlayerInstance player)
	{
		if (player == null)
		{
			return;
		}
		final Location loc = player.getLastLocation();
		if (loc != null)
		{
			player.setIsPendingRevive(false);
			player.teleToLocation(loc, null);
			player.unsetLastLocation();
		}
	}
}
