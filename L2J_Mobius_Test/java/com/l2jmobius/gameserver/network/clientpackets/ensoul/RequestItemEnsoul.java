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
package com.l2jmobius.gameserver.network.clientpackets.ensoul;

import com.l2jmobius.gameserver.data.xml.impl.SoulCrystalOptionsData;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.items.instance.L2ItemInstance;
import com.l2jmobius.gameserver.model.items.type.CrystalType;
import com.l2jmobius.gameserver.network.clientpackets.L2GameClientPacket;
import com.l2jmobius.gameserver.network.serverpackets.ExUserInfoInvenWeight;
import com.l2jmobius.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jmobius.gameserver.network.serverpackets.ensoul.ExEnsoulResult;

/**
 * @author Mathael
 */
public class RequestItemEnsoul extends L2GameClientPacket
{
	private static final String _C__D0_107_REQUESTITEMENSOUL = "[C] D0:107 RequestItemEnsoul";
	
	private static final int GEMSTONE_C = 2131;
	private static final int GEMSTONE_B = 2132;
	private static final int GEMSTONE_A = 2133;
	private static final int GEMSTONE_S = 2134;
	private static final int GEMSTONE_R = 19440;
	
	private int _objectId;
	private final SoulCrystalOption[] _common = new SoulCrystalOption[2]; // client can accept more.
	private SoulCrystalOption _special; // client can accept more.
	
	@Override
	protected void readImpl()
	{
		_objectId = readD(); // weapon object id
		final int changeCnt = readC();
		
		for (int i = 0; i < changeCnt; i++)
		{
			final boolean special = readC() == 2; // Ensoul Type 1 = Common Soul Crystal ; 2 = Special Soul Crystal
			final int slot = readC(); // [1,2] => Common slots ; [1] => Special slots
			final int SCObjectId = readD(); // Soul Crystal objectId
			final int effectId = readD(); // EffectId
			
			final SoulCrystalOption sco = SoulCrystalOptionsData.getInstance().getByEffectId(effectId);
			sco.setSoulCrystalObjectId(SCObjectId);
			sco.setSpecial(special);
			sco.setSlot(slot);
			
			if (sco.isSpecial())
			{
				_special = sco;
			}
			else
			{
				_common[sco.getSlot() - 1] = sco;
			}
		}
	}
	
	@Override
	protected void runImpl()
	{
		// You can add a Soul Crystal effect to weapon via any Blacksmith in any township.
		// There's no limit for Soul Crystal levels depending on your weapon grade.
		// To weapon grade C-S80 you can apply 1 common Soul Crystal and 1 special Soul Crystal.
		// To weapon grade R-R99 you can apply 2 common Soul Crystals and 1 special Soul Crystal.
		// Source: https://l2wiki.com/Special_Abilities#Special_Abilities
		
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		final L2ItemInstance targetItem = activeChar.getInventory().getItemByObjectId(_objectId);
		if (targetItem == null)
		{
			activeChar.sendPacket(ExEnsoulResult.FAILED);
			return;
		}
		
		for (SoulCrystalOption sco : _common)
		{
			if (sco != null)
			{
				final L2ItemInstance soulcrystal = activeChar.getInventory().getItemByObjectId(sco.getSoulCrystalObjectId());
				final boolean changing = targetItem.getCommonSoulCrystalOptions()[sco.getSlot() - 1] != null;
				
				if (!checkAndConsume(activeChar, soulcrystal, targetItem, changing, false))
				{
					activeChar.sendPacket(ExEnsoulResult.FAILED);
					return;
				}
				
				targetItem.addSoulCrystalOption(sco);
			}
		}
		
		if (_special != null)
		{
			final L2ItemInstance specialsoulcrystal = activeChar.getInventory().getItemByObjectId(_special.getSoulCrystalObjectId());
			final boolean changing = targetItem.getSpecialSoulCrystalOption() != null;
			if (!checkAndConsume(activeChar, specialsoulcrystal, targetItem, changing, true))
			{
				activeChar.sendPacket(ExEnsoulResult.FAILED);
				return;
			}
			
			targetItem.addSoulCrystalOption(_special);
		}
		
		activeChar.sendPacket(new ExEnsoulResult(1, targetItem.getCommonSoulCrystalOptions(), targetItem.getSpecialSoulCrystalOption()));
		
		final InventoryUpdate iu = new InventoryUpdate();
		iu.addModifiedItem(targetItem);
		activeChar.sendPacket(iu);
		
		activeChar.sendPacket(new ExUserInfoInvenWeight(activeChar));
	}
	
	private static boolean checkAndConsume(L2PcInstance activeChar, L2ItemInstance soulcrystal, L2ItemInstance targetItem, boolean changing, boolean special)
	{
		final CrystalType targetItemGrade = targetItem.getItem().getCrystalType();
		final int gemstoneId = getGemStoneId(targetItemGrade);
		final long count = getGemstoneCount(targetItemGrade, targetItem.getCurrentCommonSAOptions() == 2, changing, special);
		
		if ((gemstoneId == 0) || (count == 0))
		{
			return false;
		}
		
		if ((soulcrystal == null) || (activeChar.getInventory().getInventoryItemCount(soulcrystal.getId(), -1) < 1))
		{
			return false;
		}
		
		if (activeChar.getInventory().getInventoryItemCount(gemstoneId, -1) < count)
		{
			return false;
		}
		
		if (!activeChar.destroyItem("RequestItemEnsoul", soulcrystal, 1, activeChar, true))
		{
			return false;
		}
		
		if (!activeChar.destroyItemByItemId("RequestItemEnsoul", gemstoneId, count, activeChar, true))
		{
			return false;
		}
		
		return true;
	}
	
	private static long getGemstoneCount(CrystalType itemGrade, boolean price2x, boolean changing, boolean special)
	{
		switch (itemGrade)
		{
			case C:
			{
				return changing ? special ? 30 : 89 : special ? 60 : 177;
			}
			case B:
			{
				return changing ? special ? 19 : 56 : special ? 38 : 112;
			}
			case A:
			{
				return changing ? special ? 4 : 12 : special ? 8 : 24;
			}
			case S:
			{
				return changing ? special ? 4 : 10 : special ? 7 : 19;
			}
			case S80:
			case S84:
			{
				return changing ? special ? 8 : 24 : special ? 16 : 48;
			}
			case R:
			{
				return changing ? special ? 4 : 10 : special ? 7 : price2x ? 40 : 20;
			}
			case R95:
			{
				return changing ? special ? 6 : 65 : special ? 11 : price2x ? 1249 : 129;
			}
			case R99:
			{
				return changing ? special ? 8 : 168 : special ? 16 : price2x ? 5266 : 335;
			}
			default:
			{
				return 0;
			}
		}
	}
	
	private static int getGemStoneId(CrystalType itemGrade)
	{
		switch (itemGrade)
		{
			case C:
			{
				return GEMSTONE_C;
			}
			case B:
			{
				return GEMSTONE_B;
			}
			case A:
			{
				return GEMSTONE_A;
			}
			case S:
			case S80:
			case S84:
			{
				return GEMSTONE_S;
			}
			case R:
			case R95:
			case R99:
			{
				return GEMSTONE_R;
			}
			default:
			{
				return 0;
			}
		}
	}
	
	@Override
	public String getType()
	{
		return _C__D0_107_REQUESTITEMENSOUL;
	}
}
