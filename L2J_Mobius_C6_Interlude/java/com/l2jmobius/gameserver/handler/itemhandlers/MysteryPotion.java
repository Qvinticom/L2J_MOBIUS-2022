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
package com.l2jmobius.gameserver.handler.itemhandlers;

import com.l2jmobius.commons.concurrent.ThreadPoolManager;
import com.l2jmobius.gameserver.handler.IItemHandler;
import com.l2jmobius.gameserver.model.actor.L2Playable;
import com.l2jmobius.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.SystemMessageId;
import com.l2jmobius.gameserver.network.serverpackets.MagicSkillUse;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * This class ...
 * @version $Revision: 1.1.6.4 $ $Date: 2005/04/06 18:25:18 $
 */

public class MysteryPotion implements IItemHandler
{
	private static final int[] ITEM_IDS =
	{
		5234
	};
	private static final int BIGHEAD_EFFECT = 0x2000;
	private static final int MYSTERY_POTION_SKILL = 2103;
	private static final int EFFECT_DURATION = 1200000; // 20 mins
	
	@Override
	public void useItem(L2Playable playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
		{
			return;
		}
		
		L2PcInstance activeChar = (L2PcInstance) playable;
		// item.getItem().getEffects(item, activeChar);
		
		// Use a summon skill effect for fun ;)
		MagicSkillUse MSU = new MagicSkillUse(playable, playable, 2103, 1, 0, 0);
		activeChar.sendPacket(MSU);
		activeChar.broadcastPacket(MSU);
		
		activeChar.startAbnormalEffect(BIGHEAD_EFFECT);
		activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false);
		
		SystemMessage sm = new SystemMessage(SystemMessageId.USE_S1);
		sm.addSkillName(MYSTERY_POTION_SKILL);
		activeChar.sendPacket(sm);
		
		MysteryPotionStop mp = new MysteryPotionStop(playable);
		ThreadPoolManager.schedule(mp, EFFECT_DURATION);
	}
	
	public class MysteryPotionStop implements Runnable
	{
		private final L2Playable _playable;
		
		public MysteryPotionStop(L2Playable playable)
		{
			_playable = playable;
		}
		
		@Override
		public void run()
		{
			try
			{
				if (!(_playable instanceof L2PcInstance))
				{
					return;
				}
				
				((L2PcInstance) _playable).stopAbnormalEffect(BIGHEAD_EFFECT);
			}
			catch (Throwable t)
			{
			}
		}
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
