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
package org.l2jmobius.gameserver.network.clientpackets;

import java.io.IOException;

import org.l2jmobius.gameserver.IdFactory;
import org.l2jmobius.gameserver.data.CharNameTable;
import org.l2jmobius.gameserver.data.CharTemplateTable;
import org.l2jmobius.gameserver.data.ItemTable;
import org.l2jmobius.gameserver.data.SkillTable;
import org.l2jmobius.gameserver.data.SkillTreeTable;
import org.l2jmobius.gameserver.model.SkillLearn;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.instance.ItemInstance;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.ClientThread;
import org.l2jmobius.gameserver.network.serverpackets.CharCreateFail;
import org.l2jmobius.gameserver.network.serverpackets.CharCreateOk;
import org.l2jmobius.gameserver.network.serverpackets.CharSelectInfo;
import org.l2jmobius.gameserver.templates.CharTemplate;

public class CharacterCreate extends ClientBasePacket
{
	public CharacterCreate(byte[] decrypt, ClientThread client) throws IOException
	{
		super(decrypt);
		final PlayerInstance newChar = new PlayerInstance();
		newChar.setName(readS());
		newChar.setRace(readD());
		newChar.setSex(readD());
		newChar.setClassId(readD());
		newChar.setInt(readD());
		newChar.setStr(readD());
		newChar.setCon(readD());
		newChar.setMen(readD());
		newChar.setDex(readD());
		newChar.setWit(readD());
		newChar.setHairStyle(readD());
		newChar.setHairColor(readD());
		newChar.setFace(readD());
		
		if (CharNameTable.getInstance().doesCharNameExist(newChar.getName()))
		{
			final CharCreateFail ccf = new CharCreateFail(CharCreateFail.REASON_NAME_ALREADY_EXISTS);
			client.getConnection().sendPacket(ccf);
		}
		else if ((newChar.getName().length() <= 16) && isAlphaNumeric(newChar.getName()))
		{
			final CharCreateOk cco = new CharCreateOk();
			client.getConnection().sendPacket(cco);
			initNewChar(client, newChar);
			CharNameTable.getInstance().addCharName(newChar.getName());
		}
		else
		{
			final CharCreateFail ccf = new CharCreateFail(CharCreateFail.REASON_16_ENG_CHARS);
			client.getConnection().sendPacket(ccf);
		}
	}
	
	private boolean isAlphaNumeric(String text)
	{
		boolean result = true;
		final char[] chars = text.toCharArray();
		for (char c : chars)
		{
			if (Character.isLetterOrDigit(c))
			{
				continue;
			}
			result = false;
			break;
		}
		return result;
	}
	
	private void initNewChar(ClientThread client, PlayerInstance newChar) throws IOException
	{
		newChar.setObjectId(IdFactory.getInstance().getNextId());
		World.getInstance().storeObject(newChar);
		final CharTemplate template = CharTemplateTable.getInstance().getTemplate(newChar.getClassId());
		newChar.setAccuracy(template.getAcc());
		newChar.setCon(template.getCon());
		newChar.setCriticalHit(template.getCrit());
		newChar.setMaxHp(template.getHp());
		newChar.setCurrentHp(template.getHp());
		newChar.setMaxLoad(template.getLoad());
		newChar.setMaxMp(template.getMp());
		newChar.setCurrentMp(template.getMp());
		newChar.setDex(template.getDex());
		newChar.setEvasionRate(template.getEvas());
		newChar.setExp(0);
		newChar.setInt(template.getInt());
		newChar.setKarma(0);
		newChar.setLevel(1);
		newChar.setMagicalAttack(template.getMatk());
		newChar.setMagicalDefense(template.getMdef());
		newChar.setMagicalSpeed(template.getMspd());
		newChar.setPhysicalAttack(template.getPatk());
		newChar.setPhysicalDefense(template.getPdef());
		newChar.setPhysicalSpeed(template.getPspd());
		newChar.setMen(template.getMen());
		newChar.setPvpKills(0);
		newChar.setPkKills(0);
		newChar.setSp(0);
		newChar.setStr(template.getStr());
		newChar.setRunSpeed(template.getMoveSpd());
		newChar.setWalkSpeed((int) (template.getMoveSpd() * 0.7));
		newChar.setWit(template.getWit());
		newChar.addAdena(5000);
		newChar.setCanCraft(template.getCanCraft());
		newChar.setX(template.getX());
		newChar.setY(template.getY());
		newChar.setZ(template.getZ());
		if (newChar.isMale())
		{
			newChar.setMovementMultiplier(template.getMUnk1());
			newChar.setAttackSpeedMultiplier(template.getMUnk2());
			newChar.setCollisionRadius(template.getMColR());
			newChar.setCollisionHeight(template.getMColH());
		}
		else
		{
			newChar.setMovementMultiplier(template.getFUnk1());
			newChar.setAttackSpeedMultiplier(template.getFUnk2());
			newChar.setCollisionRadius(template.getFColR());
			newChar.setCollisionHeight(template.getFColH());
		}
		final ItemTable itemTable = ItemTable.getInstance();
		for (Integer item2 : template.getItems())
		{
			final ItemInstance item = itemTable.createItem(item2);
			newChar.getInventory().addItem(item);
		}
		newChar.setTitle("");
		newChar.setClanId(0);
		for (SkillLearn startSkill : SkillTreeTable.getInstance().getAvailableSkills(newChar))
		{
			newChar.addSkill(SkillTable.getInstance().getInfo(startSkill.getId(), startSkill.getLevel()));
		}
		client.saveCharToDisk(newChar);
		final CharSelectInfo cl = new CharSelectInfo(client.getLoginName(), client.getSessionId());
		client.getConnection().sendPacket(cl);
	}
}
