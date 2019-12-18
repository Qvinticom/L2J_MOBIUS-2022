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
package org.l2jmobius.gameserver.network;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.l2jmobius.gameserver.IdFactory;
import org.l2jmobius.gameserver.data.CharNameTable;
import org.l2jmobius.gameserver.data.ClanTable;
import org.l2jmobius.gameserver.data.ItemTable;
import org.l2jmobius.gameserver.data.SkillTable;
import org.l2jmobius.gameserver.model.Clan;
import org.l2jmobius.gameserver.model.ShortCut;
import org.l2jmobius.gameserver.model.Skill;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.instance.ItemInstance;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.templates.Item;
import org.l2jmobius.loginserver.LoginController;

public class ClientThread extends Thread
{
	private static Logger _log = Logger.getLogger(ClientThread.class.getName());
	private String _loginName;
	private PlayerInstance _activeChar;
	private final int _sessionId;
	private final byte[] _cryptkey =
	{
		(byte) 0x94,
		(byte) 0x35,
		(byte) 0x00,
		(byte) 0x00,
		(byte) 0xa1,
		(byte) 0x6c,
		(byte) 0x54,
		(byte) 0x87
	};
	private File _charFolder;
	private final long _autoSaveTime;
	private final Connection _connection;
	private final PacketHandler _handler;
	private final World _world;
	private int _accessLevel;
	
	public ClientThread(Socket client) throws IOException
	{
		_connection = new Connection(client, _cryptkey);
		_sessionId = 305419896;
		_handler = new PacketHandler(this);
		_world = World.getInstance();
		_autoSaveTime = 900000L;
		start();
	}
	
	@Override
	public void run()
	{
		long starttime = System.currentTimeMillis();
		try
		{
			try
			{
				do
				{
					if ((_activeChar != null) && (_autoSaveTime < (System.currentTimeMillis() - starttime)))
					{
						saveCharToDisk(_activeChar);
						starttime = System.currentTimeMillis();
					}
					final byte[] decrypt = _connection.getPacket();
					_handler.handlePacket(decrypt);
				}
				while (true);
			}
			catch (IOException io)
			{
				try
				{
					if (_activeChar != null)
					{
						_activeChar.deleteMe();
						try
						{
							saveCharToDisk(_activeChar);
						}
						catch (Exception se)
						{
							// empty catch block
						}
					}
					_connection.close();
				}
				catch (Exception ioe)
				{
					_log.warning(ioe.toString());
				}
				finally
				{
					LoginController.getInstance().removeGameServerLogin(getLoginName());
				}
			}
			catch (Exception e)
			{
				_log.warning(e.toString());
				try
				{
					if (_activeChar != null)
					{
						_activeChar.deleteMe();
						try
						{
							saveCharToDisk(_activeChar);
						}
						catch (Exception se)
						{
							// empty catch block
						}
					}
					_connection.close();
					LoginController.getInstance().removeGameServerLogin(getLoginName());
				}
				catch (Exception ex)
				{
					try
					{
					}
					catch (Throwable throwable)
					{
						LoginController.getInstance().removeGameServerLogin(getLoginName());
						throw throwable;
					}
					_log.warning(ex.toString());
					LoginController.getInstance().removeGameServerLogin(getLoginName());
				}
			}
		}
		catch (Throwable throwable)
		{
			try
			{
				if (_activeChar != null)
				{
					_activeChar.deleteMe();
					try
					{
						saveCharToDisk(_activeChar);
					}
					catch (Exception se)
					{
						// empty catch block
					}
				}
				_connection.close();
				LoginController.getInstance().removeGameServerLogin(getLoginName());
			}
			catch (Exception e)
			{
				try
				{
				}
				catch (Throwable t)
				{
					LoginController.getInstance().removeGameServerLogin(getLoginName());
					throw t;
				}
				_log.warning(e.toString());
				LoginController.getInstance().removeGameServerLogin(getLoginName());
			}
			throw throwable;
		}
	}
	
	public void saveCharToDisk(PlayerInstance cha)
	{
		if (_charFolder != null)
		{
			File saveFile = new File(_charFolder, cha.getName() + "_char.csv");
			storeChar(cha, saveFile);
			saveFile = new File(_charFolder, cha.getName() + "_items.csv");
			storeInventory(cha, saveFile);
			saveFile = new File(_charFolder, cha.getName() + "_skills.csv");
			storeSkills(cha, saveFile);
			saveFile = new File(_charFolder, cha.getName() + "_shortcuts.csv");
			storeShortcuts(cha, saveFile);
			saveFile = new File(_charFolder, cha.getName() + "_warehouse.csv");
			storeWarehouse(cha, saveFile);
		}
		IdFactory.getInstance().saveCurrentState();
	}
	
	private void storeShortcuts(PlayerInstance cha, File saveFile)
	{
		try
		{
			final OutputStreamWriter out = new FileWriter(saveFile);
			out.write("slot;type;id;level;unknown\r\n");
			for (ShortCut sc : cha.getAllShortCuts())
			{
				out.write(sc.getSlot() + ";");
				out.write(sc.getType() + ";");
				out.write(sc.getId() + ";");
				out.write(sc.getLevel() + ";");
				out.write(sc.getUnk() + "\r\n");
			}
			out.close();
		}
		catch (Exception e)
		{
			_log.warning("could not store shortcuts:" + e.toString());
		}
	}
	
	public void deleteCharFromDisk(int charslot)
	{
		if (getActiveChar() != null)
		{
			saveCharToDisk(getActiveChar());
			_activeChar = null;
		}
		File[] chars = _charFolder.listFiles((FilenameFilter) (dir, name) -> name.endsWith("_char.csv"));
		chars[charslot].delete();
		chars = _charFolder.listFiles((FilenameFilter) (dir, name) -> name.endsWith("_items.csv"));
		chars[charslot].delete();
		chars = _charFolder.listFiles((FilenameFilter) (dir, name) -> name.endsWith("_skills.csv"));
		chars[charslot].delete();
		chars = _charFolder.listFiles((FilenameFilter) (dir, name) -> name.endsWith("_shortcuts.csv"));
		chars[charslot].delete();
		chars = _charFolder.listFiles((FilenameFilter) (dir, name) -> name.endsWith("_warehouse.csv"));
		chars[charslot].delete();
		CharNameTable.getInstance().deleteCharName(chars[charslot].getName().replaceAll("_warehouse.csv", "").toLowerCase());
	}
	
	public PlayerInstance loadCharFromDisk(int charslot)
	{
		PlayerInstance character = new PlayerInstance();
		final File[] chars = _charFolder.listFiles((FilenameFilter) (dir, name) -> name.endsWith("_char.csv"));
		character = restoreChar(chars[charslot]);
		if (character != null)
		{
			restoreInventory(new File(_charFolder, character.getName() + "_items.csv"), character);
			restoreSkills(new File(_charFolder, character.getName() + "_skills.csv"), character);
			restoreShortCuts(new File(_charFolder, character.getName() + "_shortcuts.csv"), character);
			restoreWarehouse(new File(_charFolder, character.getName() + "_warehouse.csv"), character);
			if (character.getClanId() != 0)
			{
				final Clan clan = ClanTable.getInstance().getClan(character.getClanId());
				if (!clan.isMember(character.getName()))
				{
					character.setClanId(0);
					character.setTitle("");
				}
				else
				{
					character.setClan(clan);
					character.setIsClanLeader(clan.getLeaderId() == character.getObjectId());
				}
			}
		}
		else
		{
			_log.warning("Could not restore " + chars[charslot]);
		}
		return character;
	}
	
	private void restoreShortCuts(File file, PlayerInstance restored)
	{
		try
		{
			final LineNumberReader lnr = new LineNumberReader(new BufferedReader(new FileReader(file)));
			lnr.readLine();
			String line = null;
			while ((line = lnr.readLine()) != null)
			{
				final StringTokenizer st = new StringTokenizer(line, ";");
				final int slot = Integer.parseInt(st.nextToken());
				final int type = Integer.parseInt(st.nextToken());
				final int id = Integer.parseInt(st.nextToken());
				final int level = Integer.parseInt(st.nextToken());
				final int unk = Integer.parseInt(st.nextToken());
				final ShortCut sc = new ShortCut(slot, type, id, level, unk);
				restored.registerShortCut(sc);
			}
			lnr.close();
		}
		catch (Exception e)
		{
			_log.warning("could not restore shortcuts:" + e);
		}
	}
	
	private void storeInventory(PlayerInstance cha, File saveFile)
	{
		try
		{
			final OutputStreamWriter out = new FileWriter(saveFile);
			out.write("objectId;itemId;name;count;price;equipSlot;\r\n");
			for (ItemInstance item : cha.getInventory().getItems())
			{
				out.write(item.getObjectId() + ";");
				out.write(item.getItemId() + ";");
				out.write(item.getItem().getName() + ";");
				out.write(item.getCount() + ";");
				out.write(item.getPrice() + ";");
				if ((item.getItemId() == 17) || (item.getItemId() == 1341) || (item.getItemId() == 1342) || (item.getItemId() == 1343) || (item.getItemId() == 1344) || (item.getItemId() == 1345))
				{
					out.write("-1\r\n");
					continue;
				}
				out.write(item.getEquipSlot() + "\r\n");
			}
			out.close();
		}
		catch (Exception e)
		{
			_log.warning("could not store inventory:" + e);
		}
	}
	
	private void storeSkills(PlayerInstance cha, File saveFile)
	{
		try
		{
			final OutputStreamWriter out = new FileWriter(saveFile);
			out.write("skillId;skillLevel;skillName\r\n");
			for (Skill skill : cha.getAllSkills())
			{
				out.write(skill.getId() + ";");
				out.write(skill.getLevel() + ";");
				out.write(skill.getName() + "\r\n");
			}
			out.close();
		}
		catch (Exception e)
		{
			_log.warning("could not store skills:" + e);
		}
	}
	
	private void storeChar(PlayerInstance cha, File charFile)
	{
		try
		{
			final FileWriter out = new FileWriter(charFile);
			out.write("objId;charName;level;maxHp;curHp;maxMp;curMp;acc;crit;evasion;mAtk;mDef;mSpd;pAtk;pDef;pSpd;runSpd;walkSpd;str;con;dex;int;men;wit;face;hairStyle;hairColor;sex;heading;x;y;z;unk1;unk2;colRad;colHeight;exp;sp;karma;pvpkills;pkkills;clanid;maxload;race;classid;deletetime;cancraft;title;allyId\r\n");
			out.write(cha.getObjectId() + ";");
			out.write(cha.getName() + ";");
			out.write(cha.getLevel() + ";");
			out.write(cha.getMaxHp() + ";");
			out.write(cha.getCurrentHp() + ";");
			out.write(cha.getMaxMp() + ";");
			out.write(cha.getCurrentMp() + ";");
			out.write(cha.getAccuracy() + ";");
			out.write(cha.getCriticalHit() + ";");
			out.write(cha.getEvasionRate() + ";");
			out.write(cha.getMagicalAttack() + ";");
			out.write(cha.getMagicalDefense() + ";");
			out.write(cha.getMagicalSpeed() + ";");
			out.write(cha.getPhysicalAttack() + ";");
			out.write(cha.getPhysicalDefense() + ";");
			out.write(cha.getPhysicalSpeed() + ";");
			out.write(cha.getRunSpeed() + ";");
			out.write(cha.getWalkSpeed() + ";");
			out.write(cha.getStr() + ";");
			out.write(cha.getCon() + ";");
			out.write(cha.getDex() + ";");
			out.write(cha.getInt() + ";");
			out.write(cha.getMen() + ";");
			out.write(cha.getWit() + ";");
			out.write(cha.getFace() + ";");
			out.write(cha.getHairStyle() + ";");
			out.write(cha.getHairColor() + ";");
			out.write(cha.getSex() + ";");
			out.write(cha.getHeading() + ";");
			out.write(cha.getX() + ";");
			out.write(cha.getY() + ";");
			out.write(cha.getZ() + ";");
			out.write(cha.getMovementMultiplier() + ";");
			out.write(cha.getAttackSpeedMultiplier() + ";");
			out.write(cha.getCollisionRadius() + ";");
			out.write(cha.getCollisionHeight() + ";");
			out.write(cha.getExp() + ";");
			out.write(cha.getSp() + ";");
			out.write(cha.getKarma() + ";");
			out.write(cha.getPvpKills() + ";");
			out.write(cha.getPkKills() + ";");
			out.write(cha.getClanId() + ";");
			out.write(cha.getMaxLoad() + ";");
			out.write(cha.getRace() + ";");
			out.write(cha.getClassId() + ";");
			out.write(cha.getDeleteTimer() + ";");
			out.write(cha.getCanCraft() + ";");
			out.write(" " + cha.getTitle() + ";");
			out.write(cha.getAllyId() + ";");
			out.close();
		}
		catch (IOException e)
		{
			_log.warning("could not store char data:" + e);
		}
	}
	
	private void restoreWarehouse(File wfile, PlayerInstance cha)
	{
		try
		{
			final LineNumberReader lnr = new LineNumberReader(new BufferedReader(new FileReader(wfile)));
			lnr.readLine();
			String line = null;
			while ((line = lnr.readLine()) != null)
			{
				final StringTokenizer st = new StringTokenizer(line, ";");
				final ItemInstance item = new ItemInstance();
				item.setObjectId(Integer.parseInt(st.nextToken()));
				final int itemId = Integer.parseInt(st.nextToken());
				final Item itemTemp = ItemTable.getInstance().getTemplate(itemId);
				item.setItem(itemTemp);
				st.nextToken();
				item.setCount(Integer.parseInt(st.nextToken()));
				cha.getWarehouse().addItem(item);
				_world.storeObject(item);
			}
			lnr.close();
		}
		catch (Exception e)
		{
			_log.warning("could not restore warehouse:" + e);
		}
	}
	
	private void storeWarehouse(PlayerInstance cha, File saveFile)
	{
		try
		{
			final List<ItemInstance> items = cha.getWarehouse().getItems();
			final OutputStreamWriter out = new FileWriter(saveFile);
			out.write("#objectId;itemId;name;count;\n");
			for (int i = 0; i < items.size(); ++i)
			{
				final ItemInstance item = items.get(i);
				out.write(item.getObjectId() + ";");
				out.write(item.getItemId() + ";");
				out.write(item.getItem().getName() + ";");
				out.write(item.getCount() + "\n");
			}
			out.close();
		}
		catch (Exception e)
		{
			_log.warning("could not store warehouse:" + e);
		}
	}
	
	private void restoreInventory(File inventory, PlayerInstance cha)
	{
		try
		{
			final LineNumberReader lnr = new LineNumberReader(new BufferedReader(new FileReader(inventory)));
			lnr.readLine();
			String line = null;
			while ((line = lnr.readLine()) != null)
			{
				final StringTokenizer st = new StringTokenizer(line, ";");
				final ItemInstance item = new ItemInstance();
				item.setObjectId(Integer.parseInt(st.nextToken()));
				final int itemId = Integer.parseInt(st.nextToken());
				final Item itemTemp = ItemTable.getInstance().getTemplate(itemId);
				item.setItem(itemTemp);
				st.nextToken();
				item.setCount(Integer.parseInt(st.nextToken()));
				item.setPrice(Integer.parseInt(st.nextToken()));
				item.setEquipSlot(Integer.parseInt(st.nextToken()));
				cha.getInventory().addItem(item);
				if (item.isEquipped())
				{
					cha.getInventory().equipItem(item);
				}
				_world.storeObject(item);
			}
			lnr.close();
		}
		catch (Exception e)
		{
			_log.warning("could not restore inventory:" + e);
		}
	}
	
	private void restoreSkills(File inventory, PlayerInstance cha)
	{
		try
		{
			final LineNumberReader lnr = new LineNumberReader(new BufferedReader(new FileReader(inventory)));
			lnr.readLine();
			String line = null;
			while ((line = lnr.readLine()) != null)
			{
				final StringTokenizer st = new StringTokenizer(line, ";");
				final int id = Integer.parseInt(st.nextToken());
				final int level = Integer.parseInt(st.nextToken());
				st.nextToken();
				final Skill skill = SkillTable.getInstance().getInfo(id, level);
				cha.addSkill(skill);
			}
			lnr.close();
		}
		catch (Exception e)
		{
			_log.warning("could not restore skills:" + e);
		}
	}
	
	private PlayerInstance restoreChar(File charFile)
	{
		final PlayerInstance oldChar = new PlayerInstance();
		try
		{
			final LineNumberReader lnr = new LineNumberReader(new BufferedReader(new FileReader(charFile)));
			lnr.readLine();
			final String line = lnr.readLine();
			final StringTokenizer st = new StringTokenizer(line, ";");
			oldChar.setObjectId(Integer.parseInt(st.nextToken()));
			oldChar.setName(st.nextToken());
			oldChar.setLevel(Integer.parseInt(st.nextToken()));
			oldChar.setMaxHp(Integer.parseInt(st.nextToken()));
			oldChar.setCurrentHp(Double.parseDouble(st.nextToken()));
			oldChar.setMaxMp(Integer.parseInt(st.nextToken()));
			oldChar.setCurrentMp(Double.parseDouble(st.nextToken()));
			oldChar.setAccuracy(Integer.parseInt(st.nextToken()));
			oldChar.setCriticalHit(Integer.parseInt(st.nextToken()));
			oldChar.setEvasionRate(Integer.parseInt(st.nextToken()));
			oldChar.setMagicalAttack(Integer.parseInt(st.nextToken()));
			oldChar.setMagicalDefense(Integer.parseInt(st.nextToken()));
			oldChar.setMagicalSpeed(Integer.parseInt(st.nextToken()));
			oldChar.setPhysicalAttack(Integer.parseInt(st.nextToken()));
			oldChar.setPhysicalDefense(Integer.parseInt(st.nextToken()));
			oldChar.setPhysicalSpeed(Integer.parseInt(st.nextToken()));
			oldChar.setRunSpeed(Integer.parseInt(st.nextToken()));
			oldChar.setWalkSpeed(Integer.parseInt(st.nextToken()));
			oldChar.setStr(Integer.parseInt(st.nextToken()));
			oldChar.setCon(Integer.parseInt(st.nextToken()));
			oldChar.setDex(Integer.parseInt(st.nextToken()));
			oldChar.setInt(Integer.parseInt(st.nextToken()));
			oldChar.setMen(Integer.parseInt(st.nextToken()));
			oldChar.setWit(Integer.parseInt(st.nextToken()));
			oldChar.setFace(Integer.parseInt(st.nextToken()));
			oldChar.setHairStyle(Integer.parseInt(st.nextToken()));
			oldChar.setHairColor(Integer.parseInt(st.nextToken()));
			oldChar.setSex(Integer.parseInt(st.nextToken()));
			oldChar.setHeading(Integer.parseInt(st.nextToken()));
			oldChar.setX(Integer.parseInt(st.nextToken()));
			oldChar.setY(Integer.parseInt(st.nextToken()));
			oldChar.setZ(Integer.parseInt(st.nextToken()));
			oldChar.setMovementMultiplier(Double.parseDouble(st.nextToken()));
			oldChar.setAttackSpeedMultiplier(Double.parseDouble(st.nextToken()));
			oldChar.setCollisionRadius(Double.parseDouble(st.nextToken()));
			oldChar.setCollisionHeight(Double.parseDouble(st.nextToken()));
			oldChar.setExp(Integer.parseInt(st.nextToken()));
			oldChar.setSp(Integer.parseInt(st.nextToken()));
			oldChar.setKarma(Integer.parseInt(st.nextToken()));
			oldChar.setPvpKills(Integer.parseInt(st.nextToken()));
			oldChar.setPkKills(Integer.parseInt(st.nextToken()));
			oldChar.setClanId(Integer.parseInt(st.nextToken()));
			oldChar.setMaxLoad(Integer.parseInt(st.nextToken()));
			oldChar.setRace(Integer.parseInt(st.nextToken()));
			oldChar.setClassId(Integer.parseInt(st.nextToken()));
			oldChar.setFistsWeaponItem(oldChar.findFistsWeaponItem(oldChar.getClassId()));
			oldChar.setDeleteTimer(Integer.parseInt(st.nextToken()));
			oldChar.setCanCraft(Integer.parseInt(st.nextToken()));
			oldChar.setTitle(st.nextToken().trim());
			oldChar.setAllyId(Integer.parseInt(st.nextToken()));
			World.getInstance().storeObject(oldChar);
			oldChar.setUptime(System.currentTimeMillis());
			lnr.close();
		}
		catch (Exception e)
		{
			_log.warning("could not restore char data:" + e);
		}
		return oldChar;
	}
	
	public Connection getConnection()
	{
		return _connection;
	}
	
	public PlayerInstance getActiveChar()
	{
		return _activeChar;
	}
	
	public int getSessionId()
	{
		return _sessionId;
	}
	
	public String getLoginName()
	{
		return _loginName;
	}
	
	public void setLoginFolder(String folder)
	{
		_charFolder = new File("data/accounts", _loginName);
		_charFolder.mkdirs();
	}
	
	public void setLoginName(String loginName)
	{
		_loginName = loginName;
	}
	
	public void setActiveChar(PlayerInstance cha)
	{
		_activeChar = cha;
		if (cha != null)
		{
			_activeChar.setNetConnection(_connection);
			_world.storeObject(_activeChar);
		}
	}
	
	public void setAccessLevel(int access)
	{
		_accessLevel = access;
	}
	
	public int getAccessLevel()
	{
		return _accessLevel;
	}
}
