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
package org.l2jmobius.tools.gsregistering;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.math.BigInteger;
import java.util.Map.Entry;

import org.l2jmobius.Config;
import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.commons.enums.ServerMode;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.loginserver.GameServerTable;

public class GameServerRegister
{
	private static String _choice;
	
	public static void main(String[] args) throws IOException
	{
		Config.load(ServerMode.LOGIN);
		DatabaseFactory.init();
		
		try
		{
			GameServerTable.load();
		}
		catch (Exception e)
		{
			System.out.println("FATAL: Failed loading GameServerTable. Reason: " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
		
		final GameServerTable gameServerTable = GameServerTable.getInstance();
		System.out.println("Welcome to L2JMobius GameServer Registration.");
		System.out.println("Enter The id of the server you want to register or type help to get a list of ids:");
		
		try (InputStreamReader ir = new InputStreamReader(System.in);
			LineNumberReader in = new LineNumberReader(ir))
		{
			while (true)
			{
				System.out.println("Your choice:");
				_choice = in.readLine();
				
				if (_choice.equalsIgnoreCase("help"))
				{
					for (Entry<Integer, String> entry : gameServerTable._serverNames.entrySet())
					{
						System.out.println("Server: id:" + entry.getKey() + " - " + entry.getValue());
					}
					System.out.println("You can also see servername.xml");
				}
				else
				{
					try
					{
						final int id = Integer.parseInt(_choice);
						if (id > gameServerTable._serverNames.size())
						{
							System.out.println("ID is too high (max is " + (gameServerTable._serverNames.size()) + ")");
							continue;
						}
						
						if (id < 1)
						{
							System.out.println("ID must be a number above 0.");
							continue;
						}
						
						if (gameServerTable.isIDfree(id))
						{
							final byte[] hex = generateHex(16);
							gameServerTable.createServer(gameServerTable.new GameServer(hex, id));
							Config.saveHexid(Integer.valueOf(new BigInteger(hex).toString(16)), "hexid.txt");
							System.out.println("Server Registered hexid saved to 'hexid.txt'");
							System.out.println("Put this file in the /config folder of your gameserver.");
							System.exit(0);
						}
						else
						{
							System.out.println("This id is not free!");
						}
					}
					catch (NumberFormatException nfe)
					{
						System.out.println("Please, type a number or 'help'.");
					}
				}
			}
		}
	}
	
	private static byte[] generateHex(int size)
	{
		final byte[] array = new byte[size];
		Rnd.nextBytes(array);
		return array;
	}
}