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
package handlers.effecthandlers;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.instancemanager.InstanceManager;
import com.l2jmobius.gameserver.model.StatsSet;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.conditions.Condition;
import com.l2jmobius.gameserver.model.effects.AbstractEffect;
import com.l2jmobius.gameserver.model.entity.TvTEvent;
import com.l2jmobius.gameserver.model.holders.SummonRequestHolder;
import com.l2jmobius.gameserver.model.olympiad.OlympiadManager;
import com.l2jmobius.gameserver.model.skills.BuffInfo;
import com.l2jmobius.gameserver.model.zone.ZoneId;
import com.l2jmobius.gameserver.network.SystemMessageId;
import com.l2jmobius.gameserver.network.serverpackets.ConfirmDlg;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * Call Pc effect implementation.
 * @author Adry_85
 */
public final class CallPc extends AbstractEffect
{
	private final int _itemId;
	private final int _itemCount;
	
	public CallPc(Condition attachCond, Condition applyCond, StatsSet set, StatsSet params)
	{
		super(attachCond, applyCond, set, params);
		
		_itemId = params.getInt("itemId", 0);
		_itemCount = params.getInt("itemCount", 0);
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public void onStart(BuffInfo info)
	{
		if (info.getEffected() == info.getEffector())
		{
			return;
		}
		
		final L2PcInstance target = info.getEffected().getActingPlayer();
		final L2PcInstance activeChar = info.getEffector().getActingPlayer();
		if (checkSummonTargetStatus(target, activeChar))
		{
			if ((_itemId != 0) && (_itemCount != 0))
			{
				if (target.getInventory().getInventoryItemCount(_itemId, 0) < _itemCount)
				{
					final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_IS_REQUIRED_FOR_SUMMONING);
					sm.addItemName(_itemId);
					target.sendPacket(sm);
					return;
				}
				target.getInventory().destroyItemByItemId("Consume", _itemId, _itemCount, activeChar, target);
				final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED);
				sm.addItemName(_itemId);
				target.sendPacket(sm);
			}
			
			target.addScript(new SummonRequestHolder(activeChar, info.getSkill()));
			final ConfirmDlg confirm = new ConfirmDlg(SystemMessageId.C1_WISHES_TO_SUMMON_YOU_FROM_S2_DO_YOU_ACCEPT.getId());
			confirm.addCharName(activeChar);
			confirm.addZoneName(activeChar.getX(), activeChar.getY(), activeChar.getZ());
			confirm.addTime(30000);
			confirm.addRequesterId(activeChar.getObjectId());
			target.sendPacket(confirm);
		}
	}
	
	public static boolean checkSummonTargetStatus(L2PcInstance target, L2PcInstance activeChar)
	{
		if (target == activeChar)
		{
			return false;
		}
		
		if (target.isAlikeDead())
		{
			final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_DEAD_AT_THE_MOMENT_AND_CANNOT_BE_SUMMONED_OR_TELEPORTED);
			sm.addPcName(target);
			activeChar.sendPacket(sm);
			return false;
		}
		
		if (target.isInStoreMode())
		{
			final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_CURRENTLY_TRADING_OR_OPERATING_A_PRIVATE_STORE_AND_CANNOT_BE_SUMMONED_OR_TELEPORTED);
			sm.addPcName(target);
			activeChar.sendPacket(sm);
			return false;
		}
		
		if (target.isRooted() || target.isInCombat())
		{
			final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_ENGAGED_IN_COMBAT_AND_CANNOT_BE_SUMMONED_OR_TELEPORTED);
			sm.addPcName(target);
			activeChar.sendPacket(sm);
			return false;
		}
		
		if (target.isInOlympiadMode() || OlympiadManager.getInstance().isRegisteredInComp(target))
		{
			activeChar.sendPacket(SystemMessageId.A_USER_PARTICIPATING_IN_THE_OLYMPIAD_CANNOT_USE_SUMMONING_OR_TELEPORTING);
			return false;
		}
		
		if (target.isFlyingMounted() || target.isCombatFlagEquipped() || !TvTEvent.onEscapeUse(target.getObjectId()))
		{
			activeChar.sendPacket(SystemMessageId.YOU_CANNOT_USE_SUMMONING_OR_TELEPORTING_IN_THIS_AREA);
			return false;
		}
		
		if (target.inObserverMode())
		{
			final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING_OR_TELEPORTING);
			sm.addCharName(target);
			activeChar.sendPacket(sm);
			return false;
		}
		
		if (target.isInsideZone(ZoneId.NO_SUMMON_FRIEND) || target.isInsideZone(ZoneId.JAIL))
		{
			final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING_OR_TELEPORTING);
			sm.addString(target.getName());
			activeChar.sendPacket(sm);
			return false;
		}
		
		if ((activeChar.getInstanceId() > 0) && (!Config.ALLOW_SUMMON_IN_INSTANCE || !InstanceManager.getInstance().getInstance(activeChar.getInstanceId()).isSummonAllowed()))
		{
			activeChar.sendPacket(SystemMessageId.YOU_MAY_NOT_SUMMON_FROM_YOUR_CURRENT_LOCATION);
			return false;
		}
		return true;
	}
}
