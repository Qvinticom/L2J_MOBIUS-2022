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

import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.holders.SkillHolder;
import com.l2jmobius.gameserver.network.SystemMessageId;

/**
 * @author Mobius
 */
public class RequestExAutoFish extends L2GameClientPacket
{
	private final static SkillHolder FISHING_SKILL = new SkillHolder(1312, 1);
	
	public RequestExAutoFish()
	{
	}
	
	@Override
	protected void readImpl()
	{
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		if (activeChar.isFishing())
		{
			activeChar.endFishing(true, false);// TODO: Correct Animation
		}
		else
		{
			activeChar.sendPacket(SystemMessageId.YOU_CAST_YOUR_LINE_AND_START_TO_FISH);
			activeChar.useMagic(FISHING_SKILL.getSkill(), false, true);
		}
	}
	
	@Override
	public String getType()
	{
		return getClass().getSimpleName();
	}
}
