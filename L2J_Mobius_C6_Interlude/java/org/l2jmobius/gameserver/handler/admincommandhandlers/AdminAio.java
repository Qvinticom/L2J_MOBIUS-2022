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
package org.l2jmobius.gameserver.handler.admincommandhandlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.gameserver.datatables.xml.AdminData;
import org.l2jmobius.gameserver.handler.IAdminCommandHandler;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.serverpackets.EtcStatusUpdate;
import org.l2jmobius.gameserver.util.BuilderUtil;

/**
 * Give / Take Status Aio to Player Changes name color and title color if enabled Uses: setaio [<player_name>] [<time_duration in days>] removeaio [<player_name>] If <player_name> is not specified, the current target player is used.
 * @author KhayrusS
 */
public class AdminAio implements IAdminCommandHandler
{
	private static final Logger LOGGER = Logger.getLogger(AdminAio.class.getName());
	
	private static String[] _adminCommands =
	{
		"admin_setaio",
		"admin_removeaio"
	};
	
	private enum CommandEnum
	{
		admin_setaio,
		admin_removeaio
	}
	
	@Override
	public boolean useAdminCommand(String command, PlayerInstance activeChar)
	{
		final StringTokenizer st = new StringTokenizer(command);
		
		final CommandEnum comm = CommandEnum.valueOf(st.nextToken());
		
		if (comm == null)
		{
			return false;
		}
		
		switch (comm)
		{
			case admin_setaio:
			{
				boolean no_token = false;
				if (st.hasMoreTokens())
				{ // char_name not specified
					final String char_name = st.nextToken();
					final PlayerInstance player = World.getInstance().getPlayer(char_name);
					if (player != null)
					{
						if (st.hasMoreTokens()) // time
						{
							final String time = st.nextToken();
							try
							{
								final int value = Integer.parseInt(time);
								if (value > 0)
								{
									doAio(activeChar, player, char_name, time);
									if (player.isAio())
									{
										return true;
									}
								}
								else
								{
									BuilderUtil.sendSysMessage(activeChar, "Time must be bigger then 0!");
									return false;
								}
							}
							catch (NumberFormatException e)
							{
								BuilderUtil.sendSysMessage(activeChar, "Time must be a number!");
								return false;
							}
						}
						else
						{
							no_token = true;
						}
					}
					else
					{
						BuilderUtil.sendSysMessage(activeChar, "Player must be online to set AIO status");
						no_token = true;
					}
				}
				else
				{
					no_token = true;
				}
				if (no_token)
				{
					BuilderUtil.sendSysMessage(activeChar, "Usage: //setaio <char_name> [time](in days)");
					return false;
				}
			}
			case admin_removeaio:
			{
				boolean no_token = false;
				if (st.hasMoreTokens())
				{ // char_name
					final String char_name = st.nextToken();
					final PlayerInstance player = World.getInstance().getPlayer(char_name);
					if (player != null)
					{
						removeAio(activeChar, player, char_name);
						if (!player.isAio())
						{
							return true;
						}
					}
					else
					{
						BuilderUtil.sendSysMessage(activeChar, "Player must be online to remove AIO status");
						no_token = true;
					}
				}
				else
				{
					no_token = true;
				}
				if (no_token)
				{
					BuilderUtil.sendSysMessage(activeChar, "Usage: //removeaio <char_name>");
					return false;
				}
			}
		}
		
		return true;
	}
	
	public void doAio(PlayerInstance activeChar, PlayerInstance _player, String _playername, String _time)
	{
		final int days = Integer.parseInt(_time);
		if (_player == null)
		{
			BuilderUtil.sendSysMessage(activeChar, "not found char" + _playername);
			return;
		}
		
		if (days > 0)
		{
			_player.setAio(true);
			_player.setEndTime("aio", days);
			_player.getStat().addExp(_player.getStat().getExpForLevel(81));
			
			try (Connection con = DatabaseFactory.getConnection())
			{
				final PreparedStatement statement = con.prepareStatement("UPDATE characters SET aio=1, aio_end=? WHERE obj_id=?");
				statement.setLong(1, _player.getAioEndTime());
				statement.setInt(2, _player.getObjectId());
				statement.execute();
				statement.close();
				
				if (Config.ALLOW_AIO_NCOLOR && activeChar.isAio())
				{
					_player.getAppearance().setNameColor(Config.AIO_NCOLOR);
				}
				
				if (Config.ALLOW_AIO_TCOLOR && activeChar.isAio())
				{
					_player.getAppearance().setTitleColor(Config.AIO_TCOLOR);
				}
				
				_player.rewardAioSkills();
				_player.broadcastUserInfo();
				_player.sendPacket(new EtcStatusUpdate(_player));
				_player.sendSkillList();
				AdminData.broadcastMessageToGMs("GM " + activeChar.getName() + " set Aio stat for player " + _playername + " for " + _time + " day(s)");
				_player.sendMessage("You are now an Aio, Congratulations!");
				_player.broadcastUserInfo();
			}
			catch (Exception e)
			{
				LOGGER.warning("Could not set Aio stats to char: " + e);
			}
		}
		else
		{
			removeAio(activeChar, _player, _playername);
		}
	}
	
	public void removeAio(PlayerInstance activeChar, PlayerInstance _player, String _playername)
	{
		_player.setAio(false);
		_player.setAioEndTime(0);
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("UPDATE characters SET Aio=0, Aio_end=0 WHERE obj_id=?");
			statement.setInt(1, _player.getObjectId());
			statement.execute();
			statement.close();
			
			_player.lostAioSkills();
			_player.getAppearance().setNameColor(0xFFFFFF);
			_player.getAppearance().setTitleColor(0xFFFFFF);
			_player.broadcastUserInfo();
			_player.sendPacket(new EtcStatusUpdate(_player));
			_player.sendSkillList();
			AdminData.broadcastMessageToGMs("GM " + activeChar.getName() + " remove Aio stat of player " + _playername);
			_player.sendMessage("Now You are not an Aio..");
			_player.broadcastUserInfo();
		}
		catch (Exception e)
		{
			LOGGER.warning("Could not remove Aio stats of char: " + e);
		}
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return _adminCommands;
	}
}