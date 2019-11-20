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
import org.l2jmobius.gameserver.model.actor.instance.ItemInstance;
import org.l2jmobius.gameserver.templates.L2Item;

public class CharSelectInfo extends ServerBasePacket
{
	private static final String _S__1F_CHARSELECTINFO = "[S] 1F CharSelectInfo";
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
	public byte[] getContent()
	{
		int size = _characterPackage.length;
		_bao.write(31);
		writeD(size);
		// long count = 123371L;
		for (int i = 0; i < size; ++i)
		{
			CharSelectInfoPackage charInfoPackage = _characterPackage[i];
			writeS(charInfoPackage.getName());
			writeD(charInfoPackage.getCharId());
			writeS(_loginName);
			writeD(_sessionId);
			writeD(charInfoPackage.getClanId());
			writeD(0);
			writeD(charInfoPackage.getSex());
			writeD(charInfoPackage.getRace());
			writeD(charInfoPackage.getClassId());
			writeD(1);
			writeD(0);
			writeD(0);
			writeD(0);
			writeF(charInfoPackage.getCurrentHp());
			writeF(charInfoPackage.getCurrentMp());
			writeD(charInfoPackage.getSp());
			writeD(charInfoPackage.getExp());
			writeD(charInfoPackage.getLevel());
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
			writeD(0);
			writeD(charInfoPackage.getInventory().getPaperdollObjectId(2));
			writeD(charInfoPackage.getInventory().getPaperdollObjectId(1));
			writeD(charInfoPackage.getInventory().getPaperdollObjectId(3));
			writeD(charInfoPackage.getInventory().getPaperdollObjectId(5));
			writeD(charInfoPackage.getInventory().getPaperdollObjectId(4));
			writeD(charInfoPackage.getInventory().getPaperdollObjectId(6));
			writeD(charInfoPackage.getInventory().getPaperdollObjectId(7));
			writeD(charInfoPackage.getInventory().getPaperdollObjectId(8));
			writeD(charInfoPackage.getInventory().getPaperdollObjectId(9));
			writeD(charInfoPackage.getInventory().getPaperdollObjectId(10));
			writeD(charInfoPackage.getInventory().getPaperdollObjectId(11));
			writeD(charInfoPackage.getInventory().getPaperdollObjectId(12));
			writeD(charInfoPackage.getInventory().getPaperdollObjectId(13));
			writeD(charInfoPackage.getInventory().getPaperdollObjectId(14));
			writeD(0);
			writeD(charInfoPackage.getInventory().getPaperdollItemId(2));
			writeD(charInfoPackage.getInventory().getPaperdollItemId(1));
			writeD(charInfoPackage.getInventory().getPaperdollItemId(3));
			writeD(charInfoPackage.getInventory().getPaperdollItemId(5));
			writeD(charInfoPackage.getInventory().getPaperdollItemId(4));
			writeD(charInfoPackage.getInventory().getPaperdollItemId(6));
			writeD(charInfoPackage.getInventory().getPaperdollItemId(7));
			writeD(charInfoPackage.getInventory().getPaperdollItemId(8));
			writeD(charInfoPackage.getInventory().getPaperdollItemId(9));
			writeD(charInfoPackage.getInventory().getPaperdollItemId(10));
			writeD(charInfoPackage.getInventory().getPaperdollItemId(11));
			writeD(charInfoPackage.getInventory().getPaperdollItemId(12));
			writeD(charInfoPackage.getInventory().getPaperdollItemId(13));
			writeD(charInfoPackage.getInventory().getPaperdollItemId(14));
			writeD(charInfoPackage.getHairStyle());
			writeD(charInfoPackage.getHairColor());
			writeD(charInfoPackage.getFace());
			writeF(charInfoPackage.getMaxHp());
			writeF(charInfoPackage.getMaxMp());
			writeD(charInfoPackage.getDeleteTimer());
		}
		return _bao.toByteArray();
	}
	
	public CharSelectInfoPackage[] loadCharacterSelectInfoFromDisk()
	{
		File _charFolder = new File("data/accounts", _loginName);
		_charFolder.mkdirs();
		File[] chars = _charFolder.listFiles((FilenameFilter) (dir, name) -> name.endsWith("_char.csv"));
		// _log.fine("found " + chars.length + " characters on disk.");
		_charNameList = new String[chars.length];
		CharSelectInfoPackage[] characters = new CharSelectInfoPackage[chars.length];
		for (int i = 0; i < chars.length; ++i)
		{
			_charInfopackage = new CharSelectInfoPackage();
			restoreChar(chars[i]);
			if (_charInfopackage != null)
			{
				restoreInventory(new File(_charFolder, _charInfopackage.getName() + "_items.csv"));
				characters[i] = _charInfopackage;
				_charNameList[i] = _charInfopackage.getName();
				continue;
			}
			// _log.warning("could not restore " + chars[i]);
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
				StringTokenizer st = new StringTokenizer(line, ";");
				ItemInstance item = new ItemInstance();
				item.setObjectId(Integer.parseInt(st.nextToken()));
				int itemId = Integer.parseInt(st.nextToken());
				L2Item itemTemp = ItemTable.getInstance().getTemplate(itemId);
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
			String line = ((LineNumberReader) lnr).readLine();
			StringTokenizer st = new StringTokenizer(line, ";");
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
			st.nextToken();
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
	
	@Override
	public String getType()
	{
		return _S__1F_CHARSELECTINFO;
	}
}
