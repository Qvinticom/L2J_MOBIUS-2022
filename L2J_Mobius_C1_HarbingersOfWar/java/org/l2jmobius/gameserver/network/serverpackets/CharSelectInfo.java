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
package org.l2jmobius.gameserver.network.serverpackets;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.LineNumberReader;
import java.util.StringTokenizer;

import org.l2jmobius.gameserver.data.ItemTable;
import org.l2jmobius.gameserver.model.CharSelectInfoPackage;
import org.l2jmobius.gameserver.model.Inventory;
import org.l2jmobius.gameserver.model.actor.instance.ItemInstance;
import org.l2jmobius.gameserver.templates.Item;

public class CharSelectInfo extends ServerBasePacket
{
	private final String _loginName;
	private final int _sessionId;
	private CharSelectInfoPackage _charInfopackage;
	private final CharSelectInfoPackage[] _characterPackage;
	private String[] _charNameList;
	
	public CharSelectInfo(String loginName, int sessionId)
	{
		_sessionId = sessionId;
		_loginName = loginName;
		_characterPackage = loadCharacterSelectInfoFromDisk();
	}
	
	@Override
	public void writeImpl()
	{
		writeC(0x1F);
		writeD(_characterPackage.length);
		for (CharSelectInfoPackage info : _characterPackage)
		{
			writeS(info.getName());
			writeD(info.getCharId());
			writeS(_loginName);
			writeD(_sessionId);
			writeD(info.getClanId());
			writeD(0);
			writeD(info.getSex());
			writeD(info.getRace());
			writeD(info.getClassId());
			writeD(1);
			writeD(0);
			writeD(0);
			writeD(0);
			writeF(info.getCurrentHp());
			writeF(info.getCurrentMp());
			writeD(info.getSp());
			writeD(info.getExp());
			writeD(info.getLevel());
			writeD(info.getKarma());
			writeD(0);
			writeD(0);
			writeD(0);
			writeD(0);
			writeD(0);
			writeD(0);
			writeD(0);
			writeD(0);
			writeD(0);
			writeD(0);
			writeD(info.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_REAR));
			writeD(info.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LEAR));
			writeD(info.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_NECK));
			writeD(info.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_RFINGER));
			writeD(info.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LFINGER));
			writeD(info.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_HEAD));
			writeD(info.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_RHAND));
			writeD(info.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LHAND));
			writeD(info.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_GLOVES));
			writeD(info.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_CHEST));
			writeD(info.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LEGS));
			writeD(info.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_FEET));
			writeD(info.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_BACK));
			writeD(info.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LRHAND));
			writeD(0);
			writeD(info.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_REAR));
			writeD(info.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LEAR));
			writeD(info.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_NECK));
			writeD(info.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_RFINGER));
			writeD(info.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LFINGER));
			writeD(info.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_HEAD));
			writeD(info.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_RHAND));
			writeD(info.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LHAND));
			writeD(info.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_GLOVES));
			writeD(info.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_CHEST));
			writeD(info.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LEGS));
			writeD(info.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_FEET));
			writeD(info.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_BACK));
			writeD(info.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LRHAND));
			writeD(info.getHairStyle());
			writeD(info.getHairColor());
			writeD(info.getFace());
			writeF(info.getMaxHp());
			writeF(info.getMaxMp());
			writeD(info.getDeleteTimer());
		}
	}
	
	public CharSelectInfoPackage[] loadCharacterSelectInfoFromDisk()
	{
		final File charFolder = new File("data/accounts", _loginName);
		charFolder.mkdirs();
		final File[] chars = charFolder.listFiles((FilenameFilter) (dir, name) -> name.endsWith("_char.csv"));
		_charNameList = new String[chars.length];
		final CharSelectInfoPackage[] characters = new CharSelectInfoPackage[chars.length];
		for (int i = 0; i < chars.length; ++i)
		{
			_charInfopackage = new CharSelectInfoPackage();
			restoreChar(chars[i]);
			if (_charInfopackage != null)
			{
				restoreInventory(new File(charFolder, _charInfopackage.getName() + "_items.csv"));
				characters[i] = _charInfopackage;
				_charNameList[i] = _charInfopackage.getName();
			}
		}
		return characters;
	}
	
	private void restoreInventory(File inventory)
	{
		BufferedReader lnr = null;
		try
		{
			lnr = new LineNumberReader(new BufferedReader(new FileReader(inventory)));
			((LineNumberReader) lnr).readLine();
			String line = null;
			while ((line = ((LineNumberReader) lnr).readLine()) != null)
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
				_charInfopackage.getInventory().addItem(item);
				if (!item.isEquipped())
				{
					continue;
				}
				_charInfopackage.getInventory().equipItem(item);
			}
		}
		catch (Exception e)
		{
			// _log.warning("could not restore inventory:" + e);
		}
		finally
		{
			try
			{
				if (lnr != null)
				{
					lnr.close();
				}
			}
			catch (Exception e1)
			{
			}
		}
	}
	
	private void restoreChar(File charFile)
	{
		BufferedReader lnr = null;
		try
		{
			lnr = new LineNumberReader(new BufferedReader(new FileReader(charFile)));
			((LineNumberReader) lnr).readLine();
			final String line = ((LineNumberReader) lnr).readLine();
			final StringTokenizer st = new StringTokenizer(line, ";");
			st.nextToken();
			_charInfopackage.setName(st.nextToken());
			_charInfopackage.setLevel(Integer.parseInt(st.nextToken()));
			_charInfopackage.setMaxHp(Integer.parseInt(st.nextToken()));
			_charInfopackage.setCurrentHp(Double.parseDouble(st.nextToken()));
			_charInfopackage.setMaxMp(Integer.parseInt(st.nextToken()));
			_charInfopackage.setCurrentMp(Double.parseDouble(st.nextToken()));
			st.nextToken();
			st.nextToken();
			st.nextToken();
			st.nextToken();
			st.nextToken();
			st.nextToken();
			st.nextToken();
			st.nextToken();
			st.nextToken();
			st.nextToken();
			st.nextToken();
			st.nextToken();
			st.nextToken();
			st.nextToken();
			st.nextToken();
			st.nextToken();
			st.nextToken();
			_charInfopackage.setFace(Integer.parseInt(st.nextToken()));
			_charInfopackage.setHairStyle(Integer.parseInt(st.nextToken()));
			_charInfopackage.setHairColor(Integer.parseInt(st.nextToken()));
			_charInfopackage.setSex(Integer.parseInt(st.nextToken()));
			st.nextToken();
			st.nextToken();
			st.nextToken();
			st.nextToken();
			st.nextToken();
			st.nextToken();
			st.nextToken();
			st.nextToken();
			_charInfopackage.setExp(Integer.parseInt(st.nextToken()));
			_charInfopackage.setSp(Integer.parseInt(st.nextToken()));
			_charInfopackage.setKarma(Integer.parseInt(st.nextToken()));
			st.nextToken();
			st.nextToken();
			_charInfopackage.setClanId(Integer.parseInt(st.nextToken()));
			st.nextToken();
			_charInfopackage.setRace(Integer.parseInt(st.nextToken()));
			_charInfopackage.setClassId(Integer.parseInt(st.nextToken()));
			_charInfopackage.setDeleteTimer(Integer.parseInt(st.nextToken()));
			st.nextToken();
			st.nextToken().trim();
			st.nextToken();
		}
		catch (Exception e)
		{
			// _log.warning("error while loading charfile:" + charFile + " " + e.toString());
		}
		finally
		{
			try
			{
				if (lnr != null)
				{
					lnr.close();
				}
			}
			catch (Exception e1)
			{
			}
		}
	}
	
	public String[] getCharacterlist()
	{
		return _charNameList;
	}
}
