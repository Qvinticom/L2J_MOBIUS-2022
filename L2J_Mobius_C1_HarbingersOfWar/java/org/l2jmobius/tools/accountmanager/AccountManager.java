/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.l2jmobius.tools.accountmanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class AccountManager
{
	private static String _uname = "";
	private static String _pass = "";
	private static String _level = "";
	private static String _mode = "";
	
	public static void main(String[] args) throws IOException, NoSuchAlgorithmException
	{
		System.out.println("Welcome to l2j account manager.");
		System.out.println("Please choose one: ");
		System.out.println("1 - Create new account or update existing one (change pass and access level)");
		System.out.println("2 - Change access level");
		System.out.println("3 - Delete existing account (this option _keeps_ character files)");
		System.out.println("4 - List accounts & access levels");
		System.out.println("5 - exit");
		final LineNumberReader _in = new LineNumberReader(new InputStreamReader(System.in));
		while (!(_mode.equals("1") || _mode.equals("2") || _mode.equals("3") || _mode.equals("4") || _mode.equals("5")))
		{
			System.out.print("Your choice: ");
			_mode = _in.readLine();
		}
		if (_mode.equals("1") || _mode.equals("2") || _mode.equals("3"))
		{
			if (_mode.equals("1") || _mode.equals("2") || _mode.equals("3"))
			{
				while (_uname.isEmpty())
				{
					System.out.print("username: ");
					_uname = _in.readLine();
				}
			}
			if (_mode.equals("1"))
			{
				while (_pass.isEmpty())
				{
					System.out.print("password: ");
					_pass = _in.readLine();
				}
			}
			if (_mode.equals("1") || _mode.equals("2"))
			{
				while (_level.isEmpty())
				{
					System.out.print("access level: ");
					_level = _in.readLine();
				}
			}
		}
		if (_mode.equals("3"))
		{
			System.out.print("Do you really want to delete this account ? Y/N : ");
			final String yesno = _in.readLine();
			if (!yesno.equals("Y"))
			{
				_mode = "5";
			}
		}
		if (!_mode.equals("4") && !_mode.equals("5"))
		{
			AccountManager.updateAccounts("data/accounts.txt", "data/logins.tmp");
		}
		if (_mode.equals("4"))
		{
			AccountManager.printAccInfo("data/accounts.txt");
		}
		System.out.println("Have fun playing lineage2.");
	}
	
	private static void printAccInfo(String fin) throws FileNotFoundException, IOException
	{
		final File test = new File(fin);
		if (!test.exists())
		{
			test.createNewFile();
		}
		final FileInputStream in = new FileInputStream(fin);
		@SuppressWarnings("resource")
		final LineNumberReader lnr = new LineNumberReader(new InputStreamReader(in));
		String line = null;
		int count = 0;
		System.out.println("--------------");
		System.out.println("Account -> Access Level");
		while ((line = lnr.readLine()) != null)
		{
			System.out.println(line.substring(0, line.indexOf("\t")) + " -> " + line.substring(line.lastIndexOf("\t")));
			++count;
		}
		System.out.println("Number of accounts: " + count);
	}
	
	private static void updateAccounts(String fin, String fout) throws FileNotFoundException, IOException, NoSuchAlgorithmException
	{
		final File test = new File(fin);
		if (!test.exists())
		{
			test.createNewFile();
		}
		final FileInputStream in = new FileInputStream(fin);
		final FileWriter out = new FileWriter(fout);
		final MessageDigest md = MessageDigest.getInstance("SHA");
		byte[] newpass = _pass.getBytes(StandardCharsets.UTF_8);
		newpass = md.digest(newpass);
		try
		{
			@SuppressWarnings("resource")
			final LineNumberReader lnr = new LineNumberReader(new InputStreamReader(in));
			String line = null;
			boolean added = false;
			while ((line = lnr.readLine()) != null)
			{
				if (line.startsWith(_uname))
				{
					if (_mode.equals("1"))
					{
						line = _uname + "\t" + Base64.getEncoder().encodeToString(newpass) + "\t" + _level;
						added = true;
						System.out.println("Password/access level changed for user " + _uname);
					}
					else if (_mode.equals("2"))
					{
						line = line.substring(0, line.lastIndexOf("\t") + 1) + _level;
						added = true;
						System.out.println("Access level changed for user " + _uname);
					}
					else if (_mode.equals("3"))
					{
						line = "";
						System.out.println("Account for user " + _uname + " deleted");
					}
				}
				if (line.equals(""))
				{
					continue;
				}
				out.write(line + "\r\n");
			}
			if (!added && _mode.equals("1"))
			{
				out.write(_uname + "\t" + Base64.getEncoder().encodeToString(newpass) + "\t" + _level + "\r\n");
				System.out.println("New account added for user " + _uname);
			}
		}
		catch (Throwable throwable)
		{
			try
			{
				out.close();
				in.close();
				final File fileIn = new File(fin);
				final File fileOut = new File(fout);
				fileIn.delete();
				fileOut.renameTo(fileIn);
			}
			catch (Exception e)
			{
			}
			throw throwable;
		}
		try
		{
			out.close();
			in.close();
			final File fileIn = new File(fin);
			final File fileOut = new File(fout);
			fileIn.delete();
			fileOut.renameTo(fileIn);
		}
		catch (Exception e)
		{
		}
	}
}
