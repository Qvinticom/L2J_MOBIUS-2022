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
package com.l2jmobius.gameserver.network.serverpackets;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jmobius.Config;
import com.l2jmobius.commons.database.DatabaseFactory;
import com.l2jmobius.gameserver.data.sql.impl.ClanTable;
import com.l2jmobius.gameserver.data.xml.impl.ExperienceData;
import com.l2jmobius.gameserver.model.CharSelectInfoPackage;
import com.l2jmobius.gameserver.model.L2Clan;
import com.l2jmobius.gameserver.model.itemcontainer.Inventory;
import com.l2jmobius.gameserver.network.L2GameClient;

public class CharSelectionInfo extends L2GameServerPacket
{
	private static Logger _log = Logger.getLogger(CharSelectionInfo.class.getName());
	private final String _loginName;
	private final int _sessionId;
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
	}
	
	public List<CharSelectInfoPackage> getCharInfo()
	{
		return _characterPackages;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x09); // packet id
		final int size = (_characterPackages.size());
		writeD(size); // How many char there is on this account
		
		// Can prevent players from creating new characters (if 0); (if 1, the client will ask if chars may be created (0x13) Response: (0x0D) )
		writeD(Config.MAX_CHARACTERS_NUMBER_PER_ACCOUNT);
		writeC(size == Config.MAX_CHARACTERS_NUMBER_PER_ACCOUNT ? 0x01 : 0x00); // if 1 can't create new char
		writeC(0x01); // play mode, if 1 can create only 2 char in regular lobby
		writeC(0x02); // play mode, if 1 can create only 2 char in regular lobby
		writeD(0x00); // if 1, korean client
		
		int charId = 0;
		if (!_characterPackages.isEmpty())
		{
			long lastAccess = -1L;
			charId = _characterPackages.get(0).getObjectId();
			
			for (CharSelectInfoPackage info : _characterPackages)
			{
				if (info.isAvailable() && (lastAccess < info.getLastAccess()))
				{
					lastAccess = info.getLastAccess();
					charId = info.getObjectId();
				}
			}
		}
		
		for (CharSelectInfoPackage charInfoPackage : _characterPackages)
		{
			writeS(charInfoPackage.getName());
			writeD(charInfoPackage.getObjectId());
			writeS(_loginName);
			writeD(_sessionId);
			writeD(charInfoPackage.getClanId());
			writeD(0x00);
			writeD(charInfoPackage.getSex());
			writeD(charInfoPackage.getRace());
			writeD(charInfoPackage.getBaseClassId());
			writeD(Config.SERVER_ID);
			writeD(charInfoPackage.getX());
			writeD(charInfoPackage.getY());
			writeD(charInfoPackage.getZ());
			writeF(charInfoPackage.getCurrentHp());
			writeF(charInfoPackage.getCurrentMp());
			
			writeQ(charInfoPackage.getSp());
			writeQ(charInfoPackage.getExp());
			writeF((float) (charInfoPackage.getExp() - ExperienceData.getInstance().getExpForLevel(charInfoPackage.getLevel())) / (ExperienceData.getInstance().getExpForLevel(charInfoPackage.getLevel() + 1) - ExperienceData.getInstance().getExpForLevel(charInfoPackage.getLevel()))); // High Five
			writeD(charInfoPackage.getLevel());
			
			writeD(charInfoPackage.getReputation());
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
			
			for (int slot : Inventory.PAPERDOLL_ORDER_ALL)
			{
				writeD(charInfoPackage.getPaperdollItemId(slot));
			}
			
			writeD(charInfoPackage.getPaperdollItemVisualId(Inventory.PAPERDOLL_RHAND));
			writeD(charInfoPackage.getPaperdollItemVisualId(Inventory.PAPERDOLL_LHAND));
			writeD(charInfoPackage.getPaperdollItemVisualId(Inventory.PAPERDOLL_GLOVES));
			writeD(charInfoPackage.getPaperdollItemVisualId(Inventory.PAPERDOLL_CHEST));
			writeD(charInfoPackage.getPaperdollItemVisualId(Inventory.PAPERDOLL_LEGS));
			writeD(charInfoPackage.getPaperdollItemVisualId(Inventory.PAPERDOLL_FEET));
			writeD(charInfoPackage.getPaperdollItemVisualId(Inventory.PAPERDOLL_LRHAND));
			writeD(charInfoPackage.getPaperdollItemVisualId(Inventory.PAPERDOLL_HAIR));
			writeD(charInfoPackage.getPaperdollItemVisualId(Inventory.PAPERDOLL_DHAIR));
			
			writeH(charInfoPackage.getEnchantEffect(Inventory.PAPERDOLL_CHEST));
			writeH(charInfoPackage.getEnchantEffect(Inventory.PAPERDOLL_LEGS));
			writeH(charInfoPackage.getEnchantEffect(Inventory.PAPERDOLL_HEAD));
			writeH(charInfoPackage.getEnchantEffect(Inventory.PAPERDOLL_GLOVES));
			writeH(charInfoPackage.getEnchantEffect(Inventory.PAPERDOLL_FEET));
			
			writeD(charInfoPackage.getHairStyle());
			writeD(charInfoPackage.getHairColor());
			writeD(charInfoPackage.getFace());
			
			writeF(charInfoPackage.getMaxHp());
			writeF(charInfoPackage.getMaxMp());
			
			writeD(charInfoPackage.getDeleteTimer() > 0 ? (int) ((charInfoPackage.getDeleteTimer() - System.currentTimeMillis()) / 1000) : 0);
			writeD(charInfoPackage.getClassId());
			writeD(charId == charInfoPackage.getObjectId() ? 0x01 : 0x00);
			writeC(charInfoPackage.getWeaponEnchantEffect());
			writeD(charInfoPackage.get1stAugmentationId());
			writeD(charInfoPackage.get2ndAugmentationId());
			writeD(charInfoPackage.getTransformationId());
			
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			
			writeF(0x00);
			writeF(0x00);
			writeD(charInfoPackage.getVitalityPoints());
			writeD(charInfoPackage.getVitalityPercent());
			writeD(charInfoPackage.getVitalityItemCount());
			writeD(charInfoPackage.isAvailable());
			writeC(0x00);
			writeC(charInfoPackage.isHero()); // hero glow
			writeC(charInfoPackage.isHairAccessoryEnabled() ? 0x01 : 0x00); // show hair accessory if enabled
		}
	}
	
	private static List<CharSelectInfoPackage> loadCharacterSelectInfo(String loginName)
	{
		final List<CharSelectInfoPackage> characterList = new ArrayList<>();
		try (Connection con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT * FROM characters WHERE account_name=? ORDER BY createDate"))
		{
			statement.setString(1, loginName);
			try (ResultSet charList = statement.executeQuery())
			{
				while (charList.next())// fills the package
				{
					final CharSelectInfoPackage charInfopackage = restoreChar(charList);
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
		try (Connection con = DatabaseFactory.getInstance().getConnection();
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
		final int objectId = chardata.getInt("charId");
		final String name = chardata.getString("char_name");
		
		// See if the char must be deleted
		final long deletetime = chardata.getLong("deletetime");
		if ((deletetime > 0) && (System.currentTimeMillis() > deletetime))
		{
			final L2Clan clan = ClanTable.getInstance().getClan(chardata.getInt("clanid"));
			if (clan != null)
			{
				clan.removeClanMember(objectId, 0);
			}
			L2GameClient.deleteCharByObjId(objectId);
			return null;
		}
		
		final CharSelectInfoPackage charInfopackage = new CharSelectInfoPackage(objectId, name);
		charInfopackage.setAccessLevel(chardata.getInt("accesslevel"));
		charInfopackage.setLevel(chardata.getInt("level"));
		charInfopackage.setMaxHp(chardata.getInt("maxhp"));
		charInfopackage.setCurrentHp(chardata.getDouble("curhp"));
		charInfopackage.setMaxMp(chardata.getInt("maxmp"));
		charInfopackage.setCurrentMp(chardata.getDouble("curmp"));
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
			try (Connection con = DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement("SELECT augAttributes FROM item_attributes WHERE itemId=?"))
			{
				statement.setInt(1, weaponObjId);
				try (ResultSet result = statement.executeQuery())
				{
					if (result.next())
					{
						final int augment = result.getInt("augAttributes");
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
