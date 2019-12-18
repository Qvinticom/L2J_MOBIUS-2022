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

import java.util.ArrayList;
import java.util.List;

public class SystemMessage extends ServerBasePacket
{
	public static final int S1_IS_NOT_ONLINE = 3;
	public static final int CANNOT_INVITE_YOURSELF = 4;
	public static final int S1_WORKING_WITH_ANOTHER_CLAN = 10;
	public static final int NOT_ENOUGH_HP = 23;
	public static final int NOT_ENOUGH_MP = 24;
	public static final int REJUVENATING_HP = 25;
	public static final int CASTING_INTERRUPTED = 27;
	public static final int YOU_PICKED_UP_S1_ADENA = 28;
	public static final int YOU_PICKED_UP_S1_S2 = 29;
	public static final int YOU_PICKED_UP_S1 = 30;
	public static final int WELCOME_TO_LINEAGE = 34;
	public static final int YOU_DID_S1_DMG = 35;
	public static final int S1_GAVE_YOU_S2_DMG = 36;
	public static final int GETTING_READY_TO_SHOOT_AN_ARROW = 41;
	public static final int MISSED_TARGET = 43;
	public static final int CRITICAL_HIT = 44;
	public static final int USE_S1 = 46;
	public static final int S1_EQUIPPED = 49;
	public static final int EARNED_S2_S1_S = 53;
	public static final int EARNED_ITEM = 54;
	public static final int NOTHING_HAPPENED = 61;
	public static final int S1_INVITED_YOU_TO_PARTY = 66;
	public static final int EFFECT_S1_DISAPPEARED = 92;
	public static final int YOU_EARNED_S1_EXP_AND_S2_SP = 95;
	public static final int YOU_INCREASED_YOUR_LEVEL = 96;
	public static final int CANNOT_USE_ITEM_WHILE_USING_MAGIC = 104;
	public static final int YOU_INVITED_S1_TO_PARTY = 105;
	public static final int YOU_JOINED_S1_PARTY = 106;
	public static final int S1_JOINED_PARTY = 107;
	public static final int S1_LEFT_PARTY = 108;
	public static final int YOU_FEEL_S1_EFFECT = 110;
	public static final int NOT_ENOUGH_ARROWS = 112;
	public static final int REQUEST_S1_FOR_TRADE = 118;
	public static final int S1_DENIED_TRADE_REQUEST = 119;
	public static final int BEGIN_TRADE_WITH_S1 = 120;
	public static final int S1_CONFIRMED_TRADE = 121;
	public static final int TRADE_SUCCESSFUL = 123;
	public static final int S1_CANCELED_TRADE = 124;
	public static final int S1_DID_NOT_REPLY = 135;
	public static final int YOU_DID_NOT_REPLY = 136;
	public static final int ALREADY_TRADING = 142;
	public static final int TARGET_IS_INCORRECT = 144;
	public static final int TARGET_IS_NOT_FOUND_IN_THE_GAME = 145;
	public static final int CANNOT_DISCARD_DISTANCE_TOO_FAR = 151;
	public static final int S1_IS_BUSY_TRY_LATER = 153;
	public static final int ONLY_LEADER_CAN_INVITE = 154;
	public static final int PARTY_FULL = 155;
	public static final int S1_IS_ALREADY_IN_PARTY = 160;
	public static final int INVITED_USER_NOT_ONLINE = 161;
	public static final int WAITING_FOR_REPLY = 164;
	public static final int CLAN_CREATED = 189;
	public static final int FAILED_TO_CREATE_CLAN = 190;
	public static final int CLAN_MEMBER_S1_EXPELLED = 191;
	public static final int ENTERED_THE_CLAN = 195;
	public static final int S1_REFUSED_TO_JOIN_CLAN = 196;
	public static final int CLAN_MEMBERSHIP_TERMINATED = 199;
	public static final int YOU_LEFT_PARTY = 200;
	public static final int PARTY_DISPERSED = 203;
	public static final int S1_HAS_JOINED_CLAN = 222;
	public static final int CLAN_NAME_INCORRECT = 261;
	public static final int CLAN_NAME_TOO_LONG = 262;
	public static final int CLAN_LVL_3_NEEDED_TO_ENDOVE_TITLE = 271;
	public static final int ITEM_MISSING_TO_LEARN_SKILL = 276;
	public static final int LEARNED_SKILL_S1 = 277;
	public static final int NOT_ENOUGH_SP_TO_LEARN_SKILL = 278;
	public static final int YOU_NOT_ENOUGH_ADENA = 279;
	public static final int FALL_DAMAGE_S1 = 296;
	public static final int DROWN_DAMAGE_S1 = 297;
	public static final int YOU_DROPPED_S1 = 298;
	public static final int S1_PICKED_UP_S2_S3 = 299;
	public static final int S1_PICKED_UP_S2 = 300;
	public static final int CLAN_MEMBER_S1_LOGGED_IN = 304;
	public static final int PLAYER_DECLINED = 305;
	public static final int SOULSHOTS_GRADE_MISMATCH = 337;
	public static final int NOT_ENOUGH_SOULSHOTS = 338;
	public static final int CANNOT_USE_SOULSHOTS = 339;
	public static final int ENABLED_SOULSHOT = 342;
	public static final int S1_PURCHASED_S2 = 378;
	public static final int S1_PURCHASED_S2_S3 = 379;
	public static final int S1_PURCHASED_S3_S2_S = 380;
	public static final int S1_DISARMED = 417;
	public static final int WEIGHT_LIMIT_EXCEEDED = 422;
	public static final int PURCHASED_S2_FROM_S1 = 559;
	public static final int PURCHASED_S2_S3_FROM_S1 = 560;
	public static final int PURCHASED_S3_S2_S_FROM_S1 = 561;
	public static final int S1_INVITED_YOU_TO_PARTY_FINDER_KEEPER = 572;
	public static final int S1_INVITED_YOU_TO_PARTY_RANDOM = 573;
	public static final int S1_S2 = 614;
	public static final int DISSAPEARED_ADENA = 672;
	public static final int OTHER_PARTY_IS_DROZEN = 692;
	public static final int THE_PURCHASE_IS_COMPLETE = 700;
	public static final int THE_PURCHASE_PRICE_IS_HIGHER_THAN_MONEY = 720;
	
	private static final int TYPE_TEXT = 0;
	private static final int TYPE_NUMBER = 1;
	private static final int TYPE_NPC_NAME = 2;
	private static final int TYPE_ITEM_NAME = 3;
	private static final int TYPE_SKILL_NAME = 4;
	
	private final int _messageId;
	private final List<Integer> _types = new ArrayList<>();
	private final List<Object> _values = new ArrayList<>();
	
	public SystemMessage(int messageId)
	{
		_messageId = messageId;
	}
	
	public void addString(String text)
	{
		_types.add(TYPE_TEXT);
		_values.add(text);
	}
	
	public void addNumber(int number)
	{
		_types.add(TYPE_NUMBER);
		_values.add(number);
	}
	
	public void addNpcName(int id)
	{
		_types.add(TYPE_NPC_NAME);
		_values.add(1000000 + id);
	}
	
	public void addItemName(int id)
	{
		_types.add(TYPE_ITEM_NAME);
		_values.add(id);
	}
	
	public void addSkillName(int id)
	{
		_types.add(TYPE_SKILL_NAME);
		_values.add(id);
	}
	
	@Override
	public void writeImpl()
	{
		writeC(0x7A);
		writeD(_messageId);
		writeD(_types.size());
		for (int i = 0; i < _types.size(); ++i)
		{
			final int t = _types.get(i);
			writeD(t);
			switch (t)
			{
				case TYPE_TEXT:
				{
					writeS("" + _values.get(i));
					continue;
				}
				case TYPE_NUMBER:
				case TYPE_NPC_NAME:
				case TYPE_ITEM_NAME:
				{
					writeD((Integer) _values.get(i));
					continue;
				}
				case TYPE_SKILL_NAME:
				{
					writeD((Integer) _values.get(i));
					writeD(1);
				}
			}
		}
	}
}
