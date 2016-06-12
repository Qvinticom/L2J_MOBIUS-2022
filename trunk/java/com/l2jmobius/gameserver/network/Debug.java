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
package com.l2jmobius.gameserver.network;

import java.util.Map.Entry;

import com.l2jmobius.gameserver.datatables.ItemTable;
import com.l2jmobius.gameserver.enums.AttributeType;
import com.l2jmobius.gameserver.model.StatsSet;
import com.l2jmobius.gameserver.model.actor.L2Character;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.items.instance.L2ItemInstance;
import com.l2jmobius.gameserver.model.skills.Skill;
import com.l2jmobius.gameserver.model.stats.Stats;
import com.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jmobius.gameserver.network.serverpackets.TutorialShowHtml;
import com.l2jmobius.gameserver.util.Util;

/**
 * @author UnAfraid
 */
public class Debug
{
	public static void sendStatsDebug(L2Character creature, Stats stat, StatsSet set)
	{
		if (!creature.isPlayer())
		{
			return;
		}
		
		final StringBuilder sb = new StringBuilder();
		final L2ItemInstance weapon = creature.getActiveWeaponInstance();
		for (Entry<String, Object> entry : set.getSet().entrySet())
		{
			sb.append("<tr><td>" + entry.getKey() + "</td><td><font color=\"LEVEL\">" + parseValue(entry.getValue()) + "</font></td></tr>");
		}
		
		final NpcHtmlMessage msg = new NpcHtmlMessage();
		msg.setFile(creature.getActingPlayer().getHtmlPrefix(), "data/html/admin/statsdebug.htm");
		msg.replace("%stat%", String.valueOf(stat));
		msg.replace("%mulValue%", Util.formatDouble(creature.getStat().getMul(stat), "#.##"));
		msg.replace("%addValue%", creature.getStat().getAdd(stat));
		msg.replace("%templateValue%", Util.formatDouble(creature.getTemplate().getBaseValue(stat, 0), "#.##"));
		if (weapon != null)
		{
			msg.replace("%weaponBaseValue%", Util.formatDouble(weapon.getItem().getStats(stat, 0), "#.##"));
		}
		msg.replace("%details%", sb.toString());
		creature.sendPacket(new TutorialShowHtml(msg.getHtml()));
	}
	
	public static void sendSkillDebug(L2Character attacker, L2Character target, Skill skill, StatsSet set)
	{
		if (!attacker.isPlayer())
		{
			return;
		}
		
		final StringBuilder sb = new StringBuilder();
		for (Entry<String, Object> entry : set.getSet().entrySet())
		{
			sb.append("<tr><td>" + entry.getKey() + "</td><td><font color=\"LEVEL\">" + parseValue(entry.getValue()) + "</font></td></tr>");
		}
		
		final NpcHtmlMessage msg = new NpcHtmlMessage();
		msg.setFile(attacker.getActingPlayer().getHtmlPrefix(), "data/html/admin/skilldebug.htm");
		msg.replace("%patk%", target.getPAtk());
		msg.replace("%matk%", target.getMAtk());
		msg.replace("%pdef%", target.getPDef());
		msg.replace("%mdef%", target.getMDef());
		msg.replace("%acc%", target.getAccuracy());
		msg.replace("%evas%", target.getEvasionRate());
		msg.replace("%crit%", target.getCriticalHit());
		msg.replace("%speed%", target.getRunSpeed());
		msg.replace("%pAtkSpd%", target.getPAtkSpd());
		msg.replace("%mAtkSpd%", target.getMAtkSpd());
		msg.replace("%str%", target.getSTR());
		msg.replace("%dex%", target.getDEX());
		msg.replace("%con%", target.getCON());
		msg.replace("%int%", target.getINT());
		msg.replace("%wit%", target.getWIT());
		msg.replace("%men%", target.getMEN());
		msg.replace("%atkElemType%", target.getAttackElement().name());
		msg.replace("%atkElemVal%", target.getAttackElementValue(target.getAttackElement()));
		msg.replace("%fireDef%", target.getDefenseElementValue(AttributeType.FIRE));
		msg.replace("%waterDef%", target.getDefenseElementValue(AttributeType.WATER));
		msg.replace("%windDef%", target.getDefenseElementValue(AttributeType.WIND));
		msg.replace("%earthDef%", target.getDefenseElementValue(AttributeType.EARTH));
		msg.replace("%holyDef%", target.getDefenseElementValue(AttributeType.HOLY));
		msg.replace("%darkDef%", target.getDefenseElementValue(AttributeType.DARK));
		msg.replace("%skill%", String.valueOf(skill));
		msg.replace("%details%", sb.toString());
		attacker.sendPacket(new TutorialShowHtml(msg.getHtml()));
	}
	
	public static void sendItemDebug(L2PcInstance player, L2ItemInstance item, StatsSet set)
	{
		final StringBuilder sb = new StringBuilder();
		for (Entry<String, Object> entry : set.getSet().entrySet())
		{
			sb.append("<tr><td>" + entry.getKey() + "</td><td><font color=\"LEVEL\">" + parseValue(entry.getValue()) + "</font></td></tr>");
		}
		
		final NpcHtmlMessage msg = new NpcHtmlMessage();
		msg.setFile(player.getHtmlPrefix(), "data/html/admin/itemdebug.htm");
		msg.replace("%itemName%", item.getName());
		msg.replace("%itemSlot%", getBodyPart(item.getItem().getBodyPart()));
		msg.replace("%itemType%", item.isArmor() ? "Armor" : item.isWeapon() ? "Weapon" : "Etc");
		msg.replace("%enchantLevel%", item.getEnchantLevel());
		msg.replace("%isMagicWeapon%", item.getItem().isMagicWeapon());
		msg.replace("%item%", item.toString());
		msg.replace("%details%", sb.toString());
		player.sendPacket(new TutorialShowHtml(msg.getHtml()));
	}
	
	private static String parseValue(Object value)
	{
		if (value instanceof Double)
		{
			return Util.formatDouble((double) value, "#.##");
		}
		return String.valueOf(value);
	}
	
	private static String getBodyPart(int bodyPart)
	{
		for (Entry<String, Integer> entry : ItemTable._slots.entrySet())
		{
			if ((entry.getValue() & bodyPart) == bodyPart)
			{
				return entry.getKey();
			}
		}
		return "Unknown";
	}
}
