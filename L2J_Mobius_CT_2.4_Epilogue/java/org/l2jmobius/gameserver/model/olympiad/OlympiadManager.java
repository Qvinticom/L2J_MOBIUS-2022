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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.instancemanager.AntiFeedManager;
import org.l2jmobius.gameserver.model.actor.Player;

/**
 * @author GodKratos
 */
class OlympiadManager implements Runnable
{
	protected static final Logger _log = Logger.getLogger(OlympiadManager.class.getName());
	private final Map<Integer, OlympiadGame> _olympiadInstances;
	
	protected static final OlympiadStadium[] STADIUMS =
	{
		new OlympiadStadium(-88000, -252637, -3331, 17100001, 17100002),
		new OlympiadStadium(-83760, -252637, -3331, 17100003, 17100004),
		new OlympiadStadium(-79600, -252637, -3331, 17100005, 17100006),
		new OlympiadStadium(-75648, -252637, -3331, 17100007, 17100008),
		new OlympiadStadium(-88000, -249762, -3331, 17100009, 17100010),
		new OlympiadStadium(-83760, -249762, -3331, 17100011, 17100012),
		new OlympiadStadium(-79600, -249762, -3331, 17100013, 17100014),
		new OlympiadStadium(-75648, -249762, -3331, 17100015, 17100016),
		new OlympiadStadium(-88000, -246951, -3331, 17100017, 17100018),
		new OlympiadStadium(-83760, -246951, -3331, 17100019, 17100020),
		new OlympiadStadium(-79600, -246951, -3331, 17100021, 17100022),
		new OlympiadStadium(-75648, -246951, -3331, 17100023, 17100024),
		new OlympiadStadium(-88000, -244290, -3331, 17100025, 17100026),
		new OlympiadStadium(-83760, -244290, -3331, 17100027, 17100028),
		new OlympiadStadium(-79600, -244290, -3331, 17100029, 17100030),
		new OlympiadStadium(-75648, -244290, -3331, 17100031, 17100032),
		new OlympiadStadium(-88000, -241490, -3331, 17100033, 17100034),
		new OlympiadStadium(-83760, -241490, -3331, 17100035, 17100036),
		new OlympiadStadium(-79600, -241490, -3331, 17100037, 17100038),
		new OlympiadStadium(-75648, -241490, -3331, 17100039, 17100040),
		new OlympiadStadium(-88000, -238825, -3331, 17100041, 17100042),
		new OlympiadStadium(-83760, -238825, -3331, 17100043, 17100044)
	};
	
	OlympiadManager()
	{
		_olympiadInstances = new HashMap<>();
	}
	
	public static OlympiadManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	@Override
	public synchronized void run()
	{
		if (Olympiad.getInstance().isOlympiadEnd())
		{
			return;
		}
		
		final Map<Integer, OlympiadGameTask> gamesQueue = new HashMap<>();
		while (Olympiad.getInstance().inCompPeriod())
		{
			if (Olympiad.getNobleCount() == 0)
			{
				try
				{
					wait(60000);
				}
				catch (InterruptedException ex)
				{
					return;
				}
				continue;
			}
			
			int gamesQueueSize = 0;
			
			// _compStarted = true;
			final List<Integer> readyClasses = Olympiad.hasEnoughRegisteredClassed();
			final boolean readyNonClassed = Olympiad.hasEnoughRegisteredNonClassed();
			if ((readyClasses != null) || readyNonClassed)
			{
				// set up the games queue
				for (int i = 0; i < STADIUMS.length; i++)
				{
					if (!existNextOpponents(Olympiad.getRegisteredNonClassBased()) && !existNextOpponents(getRandomClassList(Olympiad.getRegisteredClassBased(), readyClasses)))
					{
						break;
					}
					if (STADIUMS[i].isFreeToUse())
					{
						if (i < (STADIUMS.length / 2))
						{
							if (readyNonClassed && existNextOpponents(Olympiad.getRegisteredNonClassBased()))
							{
								try
								{
									_olympiadInstances.put(i, new OlympiadGame(i, CompetitionType.NON_CLASSED, nextOpponents(Olympiad.getRegisteredNonClassBased())));
									gamesQueue.put(i, new OlympiadGameTask(_olympiadInstances.get(i)));
									STADIUMS[i].setStadiaBusy();
								}
								catch (Exception ex)
								{
									if (_olympiadInstances.get(i) != null)
									{
										for (Player player : _olympiadInstances.get(i).getPlayers())
										{
											player.sendMessage("Your olympiad registration was canceled due to an error");
											player.setInOlympiadMode(false);
											player.setOlympiadStart(false);
											player.setOlympiadSide(-1);
											player.setOlympiadGameId(-1);
										}
										_olympiadInstances.remove(i);
									}
									if (gamesQueue.get(i) != null)
									{
										gamesQueue.remove(i);
									}
									STADIUMS[i].setStadiaFree();
									
									// try to reuse this stadia next time
									i--;
								}
							}
							else if ((readyClasses != null) && existNextOpponents(getRandomClassList(Olympiad.getRegisteredClassBased(), readyClasses)))
							{
								try
								{
									_olympiadInstances.put(i, new OlympiadGame(i, CompetitionType.CLASSED, nextOpponents(getRandomClassList(Olympiad.getRegisteredClassBased(), readyClasses))));
									gamesQueue.put(i, new OlympiadGameTask(_olympiadInstances.get(i)));
									STADIUMS[i].setStadiaBusy();
								}
								catch (Exception ex)
								{
									if (_olympiadInstances.get(i) != null)
									{
										for (Player player : _olympiadInstances.get(i).getPlayers())
										{
											player.sendMessage("Your olympiad registration was canceled due to an error");
											player.setInOlympiadMode(false);
											player.setOlympiadStart(false);
											player.setOlympiadSide(-1);
											player.setOlympiadGameId(-1);
										}
										_olympiadInstances.remove(i);
									}
									if (gamesQueue.get(i) != null)
									{
										gamesQueue.remove(i);
									}
									STADIUMS[i].setStadiaFree();
									
									// try to reuse this stadia next time
									i--;
								}
							}
						}
						else
						{
							if ((readyClasses != null) && existNextOpponents(getRandomClassList(Olympiad.getRegisteredClassBased(), readyClasses)))
							{
								try
								{
									_olympiadInstances.put(i, new OlympiadGame(i, CompetitionType.CLASSED, nextOpponents(getRandomClassList(Olympiad.getRegisteredClassBased(), readyClasses))));
									gamesQueue.put(i, new OlympiadGameTask(_olympiadInstances.get(i)));
									STADIUMS[i].setStadiaBusy();
								}
								catch (Exception ex)
								{
									if (_olympiadInstances.get(i) != null)
									{
										for (Player player : _olympiadInstances.get(i).getPlayers())
										{
											player.sendMessage("Your olympiad registration was canceled due to an error");
											player.setInOlympiadMode(false);
											player.setOlympiadStart(false);
											player.setOlympiadSide(-1);
											player.setOlympiadGameId(-1);
										}
										_olympiadInstances.remove(i);
									}
									if (gamesQueue.get(i) != null)
									{
										gamesQueue.remove(i);
									}
									STADIUMS[i].setStadiaFree();
									
									// try to reuse this stadia next time
									i--;
								}
							}
							else if (readyNonClassed && existNextOpponents(Olympiad.getRegisteredNonClassBased()))
							{
								try
								{
									_olympiadInstances.put(i, new OlympiadGame(i, CompetitionType.NON_CLASSED, nextOpponents(Olympiad.getRegisteredNonClassBased())));
									gamesQueue.put(i, new OlympiadGameTask(_olympiadInstances.get(i)));
									STADIUMS[i].setStadiaBusy();
								}
								catch (Exception ex)
								{
									if (_olympiadInstances.get(i) != null)
									{
										for (Player player : _olympiadInstances.get(i).getPlayers())
										{
											player.sendMessage("Your olympiad registration was canceled due to an error");
											player.setInOlympiadMode(false);
											player.setOlympiadStart(false);
											player.setOlympiadSide(-1);
											player.setOlympiadGameId(-1);
										}
										_olympiadInstances.remove(i);
									}
									if (gamesQueue.get(i) != null)
									{
										gamesQueue.remove(i);
									}
									STADIUMS[i].setStadiaFree();
									
									// try to reuse this stadia next time
									i--;
								}
							}
						}
					}
					else
					{
						if ((gamesQueue.get(i) == null) || gamesQueue.get(i).isTerminated() || (gamesQueue.get(i)._game == null))
						{
							try
							{
								_olympiadInstances.remove(i);
								gamesQueue.remove(i);
								STADIUMS[i].setStadiaFree();
								i--;
							}
							catch (Exception e)
							{
								_log.log(Level.WARNING, "Exception on OlympiadManager.run(): " + e.getMessage(), e);
							}
						}
					}
				}
				
				/*
				 * try { wait(30000); } catch (InterruptedException e) { }
				 */
				
				// Start games
				gamesQueueSize = gamesQueue.size();
				for (int i = 0; i < gamesQueueSize; i++)
				{
					if ((gamesQueue.get(i) != null) && !gamesQueue.get(i).isTerminated() && !gamesQueue.get(i).isStarted())
					{
						// start new games
						final Thread thread = new Thread(gamesQueue.get(i));
						thread.start();
					}
					
					// Pause one second between games starting to reduce OlympiadManager shout spam.
					try
					{
						wait(1000);
					}
					catch (InterruptedException e)
					{
						return;
					}
				}
			}
			
			// wait 30 sec for !stress the server
			try
			{
				wait(30000);
			}
			catch (InterruptedException e)
			{
				return;
			}
		}
		
		// when comp time finish wait for all games terminated before execute
		// the cleanup code
		boolean allGamesTerminated = false;
		// wait for all games terminated
		while (!allGamesTerminated)
		{
			try
			{
				wait(30000);
			}
			catch (InterruptedException e)
			{
				// Ignore.
			}
			
			if (gamesQueue.isEmpty())
			{
				allGamesTerminated = true;
			}
			else
			{
				for (OlympiadGameTask game : gamesQueue.values())
				{
					allGamesTerminated = allGamesTerminated || game.isTerminated();
				}
			}
		}
		// when all games terminated clear all
		gamesQueue.clear();
		_olympiadInstances.clear();
		Olympiad.clearRegistered();
		OlympiadGame._battleStarted = false;
		AntiFeedManager.getInstance().clear(AntiFeedManager.OLYMPIAD_ID);
	}
	
	protected OlympiadGame getOlympiadGame(int index)
	{
		if ((_olympiadInstances != null) && !_olympiadInstances.isEmpty())
		{
			return _olympiadInstances.get(index);
		}
		return null;
	}
	
	protected void removeGame(OlympiadGame game)
	{
		if ((_olympiadInstances != null) && !_olympiadInstances.isEmpty())
		{
			for (int i = 0; i < _olympiadInstances.size(); i++)
			{
				if (_olympiadInstances.get(i) == game)
				{
					_olympiadInstances.remove(i);
				}
			}
		}
	}
	
	protected Map<Integer, OlympiadGame> getOlympiadGames()
	{
		return (_olympiadInstances == null) ? null : _olympiadInstances;
	}
	
	protected List<Player> getRandomClassList(Map<Integer, List<Player>> list, List<Integer> classList)
	{
		if ((list == null) || (classList == null) || list.isEmpty() || classList.isEmpty())
		{
			return null;
		}
		return list.get(classList.get(Rnd.get(classList.size())));
	}
	
	protected List<Player> nextOpponents(List<Player> list)
	{
		final List<Player> opponents = new ArrayList<>();
		if (list.isEmpty())
		{
			return opponents;
		}
		final int loopCount = (list.size() / 2);
		int first;
		int second;
		if (loopCount < 1)
		{
			return opponents;
		}
		
		first = Rnd.get(list.size());
		opponents.add(list.get(first));
		list.remove(first);
		
		second = Rnd.get(list.size());
		opponents.add(list.get(second));
		list.remove(second);
		
		return opponents;
	}
	
	protected boolean existNextOpponents(List<Player> list)
	{
		if (list == null)
		{
			return false;
		}
		if (list.isEmpty())
		{
			return false;
		}
		final int loopCount = list.size() >> 1;
		if (loopCount < 1)
		{
			return false;
		}
		return true;
	}
	
	protected Map<Integer, String> getAllTitles()
	{
		final Map<Integer, String> titles = new HashMap<>();
		for (OlympiadGame instance : _olympiadInstances.values())
		{
			if (!instance._gamestarted)
			{
				continue;
			}
			
			titles.put(instance._stadiumID, instance.getTitle());
		}
		
		return titles;
	}
	
	private static class SingletonHolder
	{
		protected static final OlympiadManager _instance = new OlympiadManager();
	}
}
