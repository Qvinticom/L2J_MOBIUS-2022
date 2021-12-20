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
package org.l2jmobius.gameserver.network.serverpackets;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.commons.util.Chronos;
import org.l2jmobius.gameserver.model.CharSelectInfoPackage;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.clan.Clan;
import org.l2jmobius.gameserver.model.itemcontainer.Inventory;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.OutgoingPackets;
import org.l2jmobius.gameserver.network.PacketLogger;

/**
 * @version $Revision: 1.8.2.4.2.6 $ $Date: 2005/04/06 16:13:46 $
 */
public class CharSelectInfo implements IClientOutgoingPacket
{
	private final String _loginName;
	private final int _sessionId;
	private int _activeId;
	private final List<CharSelectInfoPackage> _characterPackages;
	
	/**
	 * @param loginName
	 * @param sessionId
	 */
	public CharSelectInfo(String loginName, int sessionId)
	{
		_sessionId = sessionId;
		_loginName = loginName;
		_characterPackages = loadCharacterSelectInfo();
		_activeId = -1;
	}
	
	public CharSelectInfo(String loginName, int sessionId, int activeId)
	{
		_sessionId = sessionId;
		_loginName = loginName;
		_characterPackages = loadCharacterSelectInfo();
		_activeId = activeId;
	}
	
	public List<CharSelectInfoPackage> getCharInfo()
	{
		return _characterPackages;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.CHAR_SELECT_INFO.writeId(packet);
		final int size = _characterPackages.size();
		packet.writeD(size);
		long lastAccess = 0;
		if (_activeId == -1)
		{
			for (int i = 0; i < size; i++)
			{
				if (lastAccess < _characterPackages.get(i).getLastAccess())
				{
					lastAccess = _characterPackages.get(i).getLastAccess();
					_activeId = i;
				}
			}
		}
		for (int i = 0; i < size; i++)
		{
			final CharSelectInfoPackage charInfoPackage = _characterPackages.get(i);
			packet.writeS(charInfoPackage.getName());
			packet.writeD(charInfoPackage.getCharId());
			packet.writeS(_loginName);
			packet.writeD(_sessionId);
			packet.writeD(charInfoPackage.getClanId());
			packet.writeD(0); // ??
			packet.writeD(charInfoPackage.getSex());
			packet.writeD(charInfoPackage.getRace());
			packet.writeD(charInfoPackage.getBaseClassId());
			packet.writeD(1); // active ??
			packet.writeD(0); // x
			packet.writeD(0); // y
			packet.writeD(0); // z
			packet.writeF(charInfoPackage.getCurrentHp()); // hp cur
			packet.writeF(charInfoPackage.getCurrentMp()); // mp cur
			packet.writeD(charInfoPackage.getSp());
			packet.writeD((int) charInfoPackage.getExp());
			packet.writeD(charInfoPackage.getLevel());
			packet.writeD(charInfoPackage.getKarma()); // karma
			packet.writeD(0);
			packet.writeD(0);
			packet.writeD(0);
			packet.writeD(0);
			packet.writeD(0);
			packet.writeD(0);
			packet.writeD(0);
			packet.writeD(0);
			packet.writeD(0);
			packet.writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_UNDER));
			packet.writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_REAR));
			packet.writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_LEAR));
			packet.writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_NECK));
			packet.writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_RFINGER));
			packet.writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_LFINGER));
			packet.writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_HEAD));
			packet.writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_RHAND));
			packet.writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_LHAND));
			packet.writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_GLOVES));
			packet.writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_CHEST));
			packet.writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_LEGS));
			packet.writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_FEET));
			packet.writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_BACK));
			packet.writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_LRHAND));
			packet.writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_HAIR));
			packet.writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_UNDER));
			packet.writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_REAR));
			packet.writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_LEAR));
			packet.writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_NECK));
			packet.writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_RFINGER));
			packet.writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_LFINGER));
			packet.writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_HEAD));
			packet.writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_RHAND));
			packet.writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_LHAND));
			packet.writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_GLOVES));
			packet.writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_CHEST));
			packet.writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_LEGS));
			packet.writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_FEET));
			packet.writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_BACK));
			packet.writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_LRHAND));
			packet.writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_HAIR));
			packet.writeD(charInfoPackage.getHairStyle());
			packet.writeD(charInfoPackage.getHairColor());
			packet.writeD(charInfoPackage.getFace());
			packet.writeF(charInfoPackage.getMaxHp()); // hp max
			packet.writeF(charInfoPackage.getMaxMp()); // mp max
			final long deleteTime = charInfoPackage.getDeleteTimer();
			final int accesslevels = charInfoPackage.getAccessLevel();
			int deletedays = 0;
			if (deleteTime > 0)
			{
				deletedays = (int) ((deleteTime - Chronos.currentTimeMillis()) / 1000);
			}
			else if (accesslevels < 0)
			{
				deletedays = -1; // like L2OFF player looks dead if he is banned.
			}
			packet.writeD(deletedays); // days left before
			// delete .. if != 0
			// then char is inactive
			packet.writeD(charInfoPackage.getClassId());
			if (i == _activeId)
			{
				packet.writeD(1);
			}
			else
			{
				packet.writeD(0); // c3 auto-select char
			}
			packet.writeC(charInfoPackage.getEnchantEffect() > 127 ? 127 : charInfoPackage.getEnchantEffect());
		}
		return true;
	}
	
	private List<CharSelectInfoPackage> loadCharacterSelectInfo()
	{
		CharSelectInfoPackage charInfopackage;
		final List<CharSelectInfoPackage> characterList = new ArrayList<>();
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("SELECT account_name, charId, char_name, level, maxHp, curHp, maxMp, curMp, acc, crit, evasion, mAtk, mDef, mSpd, pAtk, pDef, pSpd, runSpd, walkSpd, str, con, dex, _int, men, wit, face, hairStyle, hairColor, sex, heading, x, y, z, movement_multiplier, attack_speed_multiplier, colRad, colHeight, exp, sp, karma, pvpkills, pkkills, clanid, maxload, race, classid, deletetime, cancraft, title, rec_have, rec_left, accesslevel, online, char_slot, lastAccess, base_class FROM characters WHERE account_name=?");
			statement.setString(1, _loginName);
			final ResultSet charList = statement.executeQuery();
			while (charList.next())// fills the package
			{
				charInfopackage = restoreChar(charList);
				if (charInfopackage != null)
				{
					characterList.add(charInfopackage);
				}
			}
			statement.close();
		}
		catch (Exception e)
		{
			PacketLogger.warning(getClass().getSimpleName() + ": " + e.getMessage());
		}
		return characterList;
	}
	
	private void loadCharacterSubclassInfo(CharSelectInfoPackage charInfopackage, int objectId, int activeClassId)
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("SELECT exp, sp, level FROM character_subclasses WHERE char_obj_id=? && class_id=? ORDER BY char_obj_id");
			statement.setInt(1, objectId);
			statement.setInt(2, activeClassId);
			final ResultSet charList = statement.executeQuery();
			if (charList.next())
			{
				charInfopackage.setExp(charList.getLong("exp"));
				charInfopackage.setSp(charList.getInt("sp"));
				charInfopackage.setLevel(charList.getInt("level"));
			}
			charList.close();
			statement.close();
		}
		catch (Exception e)
		{
			PacketLogger.warning(getClass().getSimpleName() + ": " + e.getMessage());
		}
	}
	
	private CharSelectInfoPackage restoreChar(ResultSet chardata) throws Exception
	{
		final int objectId = chardata.getInt("charId");
		// See if the char must be deleted
		final long deletetime = chardata.getLong("deletetime");
		if ((deletetime > 0) && (Chronos.currentTimeMillis() > deletetime))
		{
			final Player cha = Player.load(objectId);
			final Clan clan = cha.getClan();
			if (clan != null)
			{
				clan.removeClanMember(cha.getName(), 0);
			}
			GameClient.deleteCharByObjId(objectId);
			return null;
		}
		final String name = chardata.getString("char_name");
		final CharSelectInfoPackage charInfopackage = new CharSelectInfoPackage(objectId, name);
		charInfopackage.setLevel(chardata.getInt("level"));
		charInfopackage.setMaxHp(chardata.getInt("maxhp"));
		charInfopackage.setCurrentHp(chardata.getDouble("curhp"));
		charInfopackage.setMaxMp(chardata.getInt("maxmp"));
		charInfopackage.setCurrentMp(chardata.getDouble("curmp"));
		charInfopackage.setKarma(chardata.getInt("karma"));
		charInfopackage.setFace(chardata.getInt("face"));
		charInfopackage.setHairStyle(chardata.getInt("hairstyle"));
		charInfopackage.setHairColor(chardata.getInt("haircolor"));
		charInfopackage.setSex(chardata.getInt("sex"));
		charInfopackage.setExp(chardata.getLong("exp"));
		charInfopackage.setSp(chardata.getInt("sp"));
		charInfopackage.setClanId(chardata.getInt("clanid"));
		charInfopackage.setRace(chardata.getInt("race"));
		charInfopackage.setAccessLevel(chardata.getInt("accesslevel"));
		final int baseClassId = chardata.getInt("base_class");
		final int activeClassId = chardata.getInt("classid");
		// if is in subclass, load subclass exp, sp, level info
		if (baseClassId != activeClassId)
		{
			loadCharacterSubclassInfo(charInfopackage, objectId, activeClassId);
		}
		charInfopackage.setClassId(activeClassId);
		// Get the augmentation id for equipped weapon
		int weaponObjId = charInfopackage.getPaperdollObjectId(Inventory.PAPERDOLL_LRHAND);
		if (weaponObjId < 1)
		{
			weaponObjId = charInfopackage.getPaperdollObjectId(Inventory.PAPERDOLL_RHAND);
		}
		/*
		 * Check if the base class is set to zero and alse doesn't match with the current active class, otherwise send the base class ID. This prevents chars created before base class was introduced from being displayed incorrectly.
		 */
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