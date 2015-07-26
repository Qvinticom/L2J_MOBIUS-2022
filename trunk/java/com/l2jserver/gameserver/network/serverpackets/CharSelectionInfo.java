/*
 * Copyright (C) 2004-2015 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jserver.gameserver.network.serverpackets;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jserver.Config;
import com.l2jserver.commons.database.pool.impl.ConnectionFactory;
import com.l2jserver.gameserver.data.sql.impl.ClanTable;
import com.l2jserver.gameserver.data.xml.impl.ExperienceData;
import com.l2jserver.gameserver.model.CharSelectInfoPackage;
import com.l2jserver.gameserver.model.L2Clan;
import com.l2jserver.gameserver.model.entity.Hero;
import com.l2jserver.gameserver.model.itemcontainer.Inventory;
import com.l2jserver.gameserver.network.L2GameClient;

public class CharSelectionInfo extends L2GameServerPacket
{
	private static Logger _log = Logger.getLogger(CharSelectionInfo.class.getName());
	private final String _loginName;
	private final int _sessionId;
	private int _activeId;
	private final List<CharSelectInfoPackage> _characterPackages;
	
	/**
	 * Constructor for CharSelectionInfo.
	 * @param loginName
	 * @param sessionId
	 */
	public CharSelectionInfo(String loginName, int sessionId)
	{
		_sessionId = sessionId;
		_loginName = loginName;
		_characterPackages = loadCharacterSelectInfo(_loginName);
		_activeId = -1;
	}
	
	public CharSelectionInfo(String loginName, int sessionId, int activeId)
	{
		_sessionId = sessionId;
		_loginName = loginName;
		_characterPackages = loadCharacterSelectInfo(_loginName);
		_activeId = activeId;
	}
	
	public List<CharSelectInfoPackage> getCharInfo()
	{
		return _characterPackages;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x09); // packet id
		int size = (_characterPackages.size());
		writeD(size); // How many char there is on this account
		
		// Can prevent players from creating new characters (if 0); (if 1, the client will ask if chars may be created (0x13) Response: (0x0D) )
		writeD(Config.MAX_CHARACTERS_NUMBER_PER_ACCOUNT);
		writeC(size == Config.MAX_CHARACTERS_NUMBER_PER_ACCOUNT ? 0x01 : 0x00); // if 1 can't create new char
		writeC(0x01); // play mode, if 1 can create only 2 char in regular lobby
		writeD(0x02); // if 1, korean client
		writeC(0x00); // if 1 suggest premium account
		
		long lastAccess = 0L;
		if (_activeId == -1)
		{
			for (int i = 0; i < size; i++)
			{
				final CharSelectInfoPackage charInfoPackage = _characterPackages.get(i);
				if (lastAccess < charInfoPackage.getLastAccess())
				{
					lastAccess = charInfoPackage.getLastAccess();
					_activeId = i;
				}
			}
		}
		
		for (int i = 0; i < size; i++)
		{
			final CharSelectInfoPackage charInfoPackage = _characterPackages.get(i);
			
			writeS(charInfoPackage.getName()); // char name
			writeD(charInfoPackage.getObjectId()); // char id
			writeS(_loginName); // login
			writeD(_sessionId); // session id
			writeD(0x00); // ??
			writeD(0x00); // ??
			
			writeD(charInfoPackage.getSex()); // sex
			writeD(charInfoPackage.getRace()); // race
			
			if (charInfoPackage.getClassId() == charInfoPackage.getBaseClassId())
			{
				writeD(charInfoPackage.getClassId());
			}
			else
			{
				writeD(charInfoPackage.getBaseClassId());
			}
			
			writeD(0x01); // server id ??
			
			writeD(charInfoPackage.getX());
			writeD(charInfoPackage.getY());
			writeD(charInfoPackage.getZ());
			writeF(charInfoPackage.getCurrentHp());
			writeF(charInfoPackage.getCurrentMp());
			
			writeQ(charInfoPackage.getSp());
			writeQ(charInfoPackage.getExp());
			writeF((float) (charInfoPackage.getExp() - ExperienceData.getInstance().getExpForLevel(charInfoPackage.getLevel())) / (ExperienceData.getInstance().getExpForLevel(charInfoPackage.getLevel() + 1) - ExperienceData.getInstance().getExpForLevel(charInfoPackage.getLevel()))); // High Five
			writeD(charInfoPackage.getLevel());
			
			writeD(charInfoPackage.getKarma() > 0 ? charInfoPackage.getKarma() * -1 : charInfoPackage.getReputation());
			writeD(charInfoPackage.getPkKills());
			writeD(charInfoPackage.getPvPKills());
			
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			
			writeD(0x00); // Ertheia
			writeD(0x00); // Ertheia
			
			for (int slot : getPaperdollOrder())
			{
				writeD(charInfoPackage.getPaperdollItemId(slot));
			}
			
			for (int slot : getPaperdollOrderVisualId())
			{
				writeD(charInfoPackage.getPaperdollItemVisualId(slot));
			}
			
			writeD(0x00); // ??
			writeD(0x00); // ??
			writeH(0x00); // ??
			
			writeD(charInfoPackage.getHairStyle());
			writeD(charInfoPackage.getHairColor());
			writeD(charInfoPackage.getFace());
			
			writeF(charInfoPackage.getMaxHp()); // hp max
			writeF(charInfoPackage.getMaxMp()); // mp max
			
			writeD(charInfoPackage.getDeleteTimer() > 0 ? (int) ((charInfoPackage.getDeleteTimer() - System.currentTimeMillis()) / 1000) : 0);
			writeD(charInfoPackage.getClassId());
			writeD(i == _activeId ? 1 : 0);
			
			writeC(charInfoPackage.getEnchantEffect() > 127 ? 127 : charInfoPackage.getEnchantEffect());
			writeD(charInfoPackage.getAugmentationId());
			
			// writeD(charInfoPackage.getTransformId()); // Used to display Transformations
			writeD(0x00); // Currently on retail when you are on character select you don't see your transformation.
			
			// Freya by Vistall:
			writeD(0x00); // npdid - 16024 Tame Tiny Baby Kookaburra A9E89C
			writeD(0x00); // level
			writeD(0x00); // ?
			writeD(0x00); // food? - 1200
			writeF(0x00); // max Hp
			writeF(0x00); // cur Hp
			
			// High Five by Vistall:
			writeD(charInfoPackage.getVitalityPoints()); // H5 Vitality
			writeD(0x00); // Vitality Exp Bonus
			writeD(0x00); // Vitality items used, such as potion
			writeD(charInfoPackage.getAccessLevel() == -100 ? 0x00 : 0x01); // Char is active or not
			writeC(0x00); // is noble
			writeC(Hero.getInstance().isHero(charInfoPackage.getObjectId()) ? 0x01 : 0x00); // hero glow
			writeC(charInfoPackage.isHairAccessoryEnabled() ? 0x01 : 0x00); // show hair accessory if enabled
		}
	}
	
	private static List<CharSelectInfoPackage> loadCharacterSelectInfo(String loginName)
	{
		final List<CharSelectInfoPackage> characterList = new ArrayList<>();
		try (Connection con = ConnectionFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT * FROM characters WHERE account_name=? ORDER BY createDate"))
		{
			statement.setString(1, loginName);
			try (ResultSet charList = statement.executeQuery())
			{
				while (charList.next())// fills the package
				{
					CharSelectInfoPackage charInfopackage = restoreChar(charList);
					if (charInfopackage != null)
					{
						characterList.add(charInfopackage);
					}
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Could not restore char info: " + e.getMessage(), e);
		}
		return characterList;
	}
	
	private static void loadCharacterSubclassInfo(CharSelectInfoPackage charInfopackage, int ObjectId, int activeClassId)
	{
		try (Connection con = ConnectionFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT exp, sp, level, vitality_points FROM character_subclasses WHERE charId=? && class_id=? ORDER BY charId"))
		{
			statement.setInt(1, ObjectId);
			statement.setInt(2, activeClassId);
			try (ResultSet charList = statement.executeQuery())
			{
				if (charList.next())
				{
					charInfopackage.setExp(charList.getLong("exp"));
					charInfopackage.setSp(charList.getInt("sp"));
					charInfopackage.setLevel(charList.getInt("level"));
					charInfopackage.setVitalityPoints(charList.getInt("vitality_points"));
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Could not restore char subclass info: " + e.getMessage(), e);
		}
	}
	
	private static CharSelectInfoPackage restoreChar(ResultSet chardata) throws Exception
	{
		int objectId = chardata.getInt("charId");
		String name = chardata.getString("char_name");
		
		// See if the char must be deleted
		long deletetime = chardata.getLong("deletetime");
		if (deletetime > 0)
		{
			if (System.currentTimeMillis() > deletetime)
			{
				L2Clan clan = ClanTable.getInstance().getClan(chardata.getInt("clanid"));
				if (clan != null)
				{
					clan.removeClanMember(objectId, 0);
				}
				
				L2GameClient.deleteCharByObjId(objectId);
				return null;
			}
		}
		
		final CharSelectInfoPackage charInfopackage = new CharSelectInfoPackage(objectId, name);
		charInfopackage.setAccessLevel(chardata.getInt("accesslevel"));
		charInfopackage.setLevel(chardata.getInt("level"));
		charInfopackage.setMaxHp(chardata.getInt("maxhp"));
		charInfopackage.setCurrentHp(chardata.getDouble("curhp"));
		charInfopackage.setMaxMp(chardata.getInt("maxmp"));
		charInfopackage.setCurrentMp(chardata.getDouble("curmp"));
		charInfopackage.setKarma(chardata.getInt("karma"));
		charInfopackage.setReputation(chardata.getInt("reputation"));
		charInfopackage.setPkKills(chardata.getInt("pkkills"));
		charInfopackage.setPvPKills(chardata.getInt("pvpkills"));
		charInfopackage.setFace(chardata.getInt("face"));
		charInfopackage.setHairStyle(chardata.getInt("hairstyle"));
		charInfopackage.setHairColor(chardata.getInt("haircolor"));
		charInfopackage.setSex(chardata.getInt("sex"));
		
		charInfopackage.setExp(chardata.getLong("exp"));
		charInfopackage.setSp(chardata.getLong("sp"));
		charInfopackage.setVitalityPoints(chardata.getInt("vitality_points"));
		charInfopackage.setClanId(chardata.getInt("clanid"));
		
		charInfopackage.setRace(chardata.getInt("race"));
		
		final int baseClassId = chardata.getInt("base_class");
		final int activeClassId = chardata.getInt("classid");
		
		charInfopackage.setX(chardata.getInt("x"));
		charInfopackage.setY(chardata.getInt("y"));
		charInfopackage.setZ(chardata.getInt("z"));
		
		final int faction = chardata.getInt("faction");
		if (faction == 1)
		{
			charInfopackage.setGood();
		}
		if (faction == 2)
		{
			charInfopackage.setEvil();
		}
		
		if (Config.L2JMOD_MULTILANG_ENABLE)
		{
			String lang = chardata.getString("language");
			if (!Config.L2JMOD_MULTILANG_ALLOWED.contains(lang))
			{
				lang = Config.L2JMOD_MULTILANG_DEFAULT;
			}
			charInfopackage.setHtmlPrefix("lang/" + lang + "/");
		}
		
		// if is in subclass, load subclass exp, sp, lvl info
		if (baseClassId != activeClassId)
		{
			loadCharacterSubclassInfo(charInfopackage, objectId, activeClassId);
		}
		
		charInfopackage.setClassId(activeClassId);
		
		// Get the augmentation id for equipped weapon
		int weaponObjId = charInfopackage.getPaperdollObjectId(Inventory.PAPERDOLL_RHAND);
		if (weaponObjId < 1)
		{
			weaponObjId = charInfopackage.getPaperdollObjectId(Inventory.PAPERDOLL_RHAND);
		}
		
		if (weaponObjId > 0)
		{
			try (Connection con = ConnectionFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement("SELECT augAttributes FROM item_attributes WHERE itemId=?"))
			{
				statement.setInt(1, weaponObjId);
				try (ResultSet result = statement.executeQuery())
				{
					if (result.next())
					{
						int augment = result.getInt("augAttributes");
						charInfopackage.setAugmentationId(augment == -1 ? 0 : augment);
					}
				}
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, "Could not restore augmentation info: " + e.getMessage(), e);
			}
		}
		
		// Check if the base class is set to zero and also doesn't match with the current active class, otherwise send the base class ID. This prevents chars created before base class was introduced from being displayed incorrectly.
		if ((baseClassId == 0) && (activeClassId > 0))
		{
			charInfopackage.setBaseClassId(activeClassId);
		}
		else
		{
			charInfopackage.setBaseClassId(baseClassId);
		}
		
		charInfopackage.setDeleteTimer(deletetime);
		charInfopackage.setLastAccess(chardata.getLong("lastAccess"));
		return charInfopackage;
	}
}
