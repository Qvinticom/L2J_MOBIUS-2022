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
package com.l2jmobius.gameserver.network.clientpackets;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.datatables.CharNameTable;
import com.l2jmobius.gameserver.datatables.CharTemplateTable;
import com.l2jmobius.gameserver.datatables.ItemTable;
import com.l2jmobius.gameserver.datatables.SkillTable;
import com.l2jmobius.gameserver.datatables.SkillTreeTable;
import com.l2jmobius.gameserver.idfactory.IdFactory;
import com.l2jmobius.gameserver.instancemanager.QuestManager;
import com.l2jmobius.gameserver.model.L2ItemInstance;
import com.l2jmobius.gameserver.model.L2ShortCut;
import com.l2jmobius.gameserver.model.L2SkillLearn;
import com.l2jmobius.gameserver.model.L2World;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.network.L2GameClient;
import com.l2jmobius.gameserver.network.serverpackets.CharCreateFail;
import com.l2jmobius.gameserver.network.serverpackets.CharCreateOk;
import com.l2jmobius.gameserver.network.serverpackets.CharSelectInfo;
import com.l2jmobius.gameserver.templates.L2Item;
import com.l2jmobius.gameserver.templates.L2PcTemplate;
import com.l2jmobius.gameserver.util.Util;

/**
 * This class ...
 * @version $Revision: 1.9.2.3.2.8 $ $Date: 2005/03/27 15:29:30 $
 */
@SuppressWarnings("unused")
public class CharacterCreate extends L2GameClientPacket
{
	private static final String _C__0B_CHARACTERCREATE = "[C] 0B CharacterCreate";
	private static Logger _log = Logger.getLogger(CharacterCreate.class.getName());
	
	// cSdddddddddddd
	private String _name;
	private int _race;
	private byte _sex;
	private int _classId;
	private int _int;
	private int _str;
	private int _con;
	private int _men;
	private int _dex;
	private int _wit;
	private byte _hairStyle;
	private byte _hairColor;
	private byte _face;
	
	@Override
	protected void readImpl()
	{
		_name = readS();
		_race = readD();
		_sex = (byte) readD();
		_classId = readD();
		_int = readD();
		_str = readD();
		_con = readD();
		_men = readD();
		_dex = readD();
		_wit = readD();
		_hairStyle = (byte) readD();
		_hairColor = (byte) readD();
		_face = (byte) readD();
	}
	
	@Override
	public void runImpl()
	{
		if ((_name.length() < 1) || (_name.length() > 16) || !Util.isAlphaNumeric(_name) || !isValidName(_name))
		{
			if (Config.DEBUG)
			{
				_log.fine("charname: " + _name + " is invalid. creation failed.");
			}
			final CharCreateFail ccf = new CharCreateFail(CharCreateFail.REASON_16_ENG_CHARS);
			sendPacket(ccf);
			return;
		}
		
		L2PcInstance newChar = null;
		L2PcTemplate template = null;
		
		/*
		 * DrHouse: Since checks for duplicate names are done using SQL, lock must be held until data is written to DB as well.
		 */
		synchronized (CharNameTable.getInstance())
		{
			if ((CharNameTable.getInstance().accountCharNumber(getClient().getAccountName()) >= Config.MAX_CHARACTERS_NUMBER_PER_ACCOUNT) && (Config.MAX_CHARACTERS_NUMBER_PER_ACCOUNT != 0))
			{
				if (Config.DEBUG)
				{
					_log.fine("Max number of characters reached. Creation failed.");
				}
				final CharCreateFail ccf = new CharCreateFail(CharCreateFail.REASON_TOO_MANY_CHARACTERS);
				sendPacket(ccf);
				return;
			}
			else if (CharNameTable.getInstance().doesCharNameExist(_name))
			{
				if (Config.DEBUG)
				{
					_log.fine("charname: " + _name + " already exists. creation failed.");
				}
				final CharCreateFail ccf = new CharCreateFail(CharCreateFail.REASON_NAME_ALREADY_EXISTS);
				sendPacket(ccf);
				return;
			}
			
			template = CharTemplateTable.getInstance().getTemplate(_classId);
			
			if (Config.DEBUG)
			{
				_log.fine("charname: " + _name + " classId: " + _classId + " template: " + template);
			}
			
			if ((template == null) || (template.classBaseLevel > 1))
			{
				final CharCreateFail ccf = new CharCreateFail(CharCreateFail.REASON_CREATION_FAILED);
				sendPacket(ccf);
				return;
			}
			
			final int objectId = IdFactory.getInstance().getNextId();
			newChar = L2PcInstance.create(objectId, template, getClient().getAccountName(), _name, _hairStyle, _hairColor, _face, _sex != 0);
		}
		
		newChar.setCurrentHp(newChar.getMaxHp());
		newChar.setCurrentCp(0);
		newChar.setCurrentMp(newChar.getMaxMp());
		
		// send acknowledgement
		
		sendPacket(new CharCreateOk());
		
		initNewChar(getClient(), newChar);
	}
	
	private boolean isValidName(String text)
	{
		boolean result = true;
		final String test = text;
		Pattern pattern;
		try
		{
			pattern = Pattern.compile(Config.CNAME_TEMPLATE);
		}
		catch (final PatternSyntaxException e) // case of illegal pattern
		{
			_log.warning("ERROR : Character name pattern of config is wrong!");
			pattern = Pattern.compile(".*");
		}
		final Matcher regexp = pattern.matcher(test);
		if (!regexp.matches())
		{
			result = false;
		}
		return result;
	}
	
	private void initNewChar(L2GameClient client, L2PcInstance newChar)
	{
		if (Config.DEBUG)
		{
			_log.fine("Character init start");
		}
		L2World.getInstance().storeObject(newChar);
		
		final L2PcTemplate template = newChar.getTemplate();
		
		newChar.addAdena("Init", Config.STARTING_ADENA, null, false);
		
		if (Config.CUSTOM_STARTING_SPAWN)
		{
			newChar.setXYZInvisible(Config.CUSTOM_SPAWN_X, Config.CUSTOM_SPAWN_Y, Config.CUSTOM_SPAWN_Z);
		}
		else
		{
			newChar.setXYZInvisible(template.spawnX, template.spawnY, template.spawnZ);
		}
		
		newChar.setTitle("");
		
		L2ShortCut shortcut;
		// add attack shortcut
		shortcut = new L2ShortCut(0, 0, 3, 2, -1, 1);
		newChar.registerShortCut(shortcut);
		// add take shortcut
		shortcut = new L2ShortCut(3, 0, 3, 5, -1, 1);
		newChar.registerShortCut(shortcut);
		// add sit shortcut
		shortcut = new L2ShortCut(10, 0, 3, 0, -1, 1);
		newChar.registerShortCut(shortcut);
		
		final ItemTable itemTable = ItemTable.getInstance();
		final L2Item[] items = template.getItems();
		for (final L2Item item2 : items)
		{
			final L2ItemInstance item = newChar.getInventory().addItem("Init", item2.getItemId(), 1, newChar, null);
			
			if (item == null)
			{
				_log.warning("Could not create item during char creation: itemId " + item2.getItemId() + ".");
				continue;
			}
			
			if (item.getItemId() == 5588)
			{
				// add tutbook shortcut
				shortcut = new L2ShortCut(11, 0, 1, item.getObjectId(), -1, 1);
				newChar.registerShortCut(shortcut);
			}
			
			if (item.isEquipable())
			{
				if ((newChar.getActiveWeaponItem() == null) || !(item.getItem().getType2() != L2Item.TYPE2_WEAPON))
				{
					newChar.getInventory().equipItemAndRecord(item);
				}
			}
		}
		
		final L2SkillLearn[] startSkills = SkillTreeTable.getInstance().getAvailableSkills(newChar, newChar.getClassId());
		for (final L2SkillLearn startSkill : startSkills)
		{
			newChar.addSkill(SkillTable.getInstance().getInfo(startSkill.getId(), startSkill.getLevel()), true);
			if ((startSkill.getId() == 1001) || (startSkill.getId() == 1177))
			{
				shortcut = new L2ShortCut(1, 0, 2, startSkill.getId(), startSkill.getLevel(), 1);
				newChar.registerShortCut(shortcut);
			}
			
			if (startSkill.getId() == 1216)
			{
				shortcut = new L2ShortCut(9, 0, 2, startSkill.getId(), startSkill.getLevel(), 1);
				newChar.registerShortCut(shortcut);
			}
			
			if (Config.DEBUG)
			{
				_log.fine("adding starter skill:" + startSkill.getId() + " / " + startSkill.getLevel());
			}
		}
		
		if (Config.ALT_ENABLE_TUTORIAL)
		{
			startTutorialQuest(newChar);
		}
		
		newChar.logout();
		
		// send char list
		final CharSelectInfo cl = new CharSelectInfo(client.getAccountName(), client.getSessionId().playOkID1);
		client.getConnection().sendPacket(cl);
		client.setCharSelection(cl.getCharInfo());
		if (Config.DEBUG)
		{
			_log.fine("Character init end");
		}
	}
	
	public void startTutorialQuest(L2PcInstance player)
	{
		final QuestState qs = player.getQuestState("255_Tutorial");
		Quest q = null;
		if (qs == null)
		{
			q = QuestManager.getInstance().getQuest("255_Tutorial");
		}
		if (q != null)
		{
			q.newQuestState(player);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.network.clientpackets.L2GameClientPacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__0B_CHARACTERCREATE;
	}
}