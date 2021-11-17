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
package ai.others.DimensionalMerchant;

import org.l2jmobius.gameserver.data.xml.MultisellData;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.events.EventType;
import org.l2jmobius.gameserver.model.events.ListenerRegisterType;
import org.l2jmobius.gameserver.model.events.annotations.RegisterEvent;
import org.l2jmobius.gameserver.model.events.annotations.RegisterType;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerBypass;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.itemcontainer.PlayerFreight;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.PackageToList;
import org.l2jmobius.gameserver.network.serverpackets.WareHouseWithdrawalList;

import ai.AbstractNpcAI;

/**
 * Dimensional Merchant AI.
 * @author Mobius
 */
public class DimensionalMerchant extends AbstractNpcAI
{
	// NPC
	private static final int MERCHANT = 32478; // Dimensional Merchant
	// Others
	private static final int ATTENDANCE_REWARD_MULTISELL = 3247801;
	private static final String COMMAND_BYPASS = "Quest DimensionalMerchant ";
	
	private DimensionalMerchant()
	{
		addStartNpc(MERCHANT);
		addFirstTalkId(MERCHANT);
		addTalkId(MERCHANT);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		final String htmltext = null;
		switch (event)
		{
			case "package_deposit":
			{
				if (player.getAccountChars().size() < 1)
				{
					player.sendPacket(SystemMessageId.THAT_CHARACTER_DOES_NOT_EXIST);
				}
				else
				{
					player.sendPacket(new PackageToList(player.getAccountChars()));
				}
				break;
			}
			case "package_withdraw":
			{
				final PlayerFreight freight = player.getFreight();
				if (freight != null)
				{
					if (freight.getSize() > 0)
					{
						player.setActiveWarehouse(freight);
						for (Item i : player.getActiveWarehouse().getItems())
						{
							if (i.isTimeLimitedItem() && (i.getRemainingTime() <= 0))
							{
								player.getActiveWarehouse().destroyItem("ItemInstance", i, player, null);
							}
						}
						player.sendPacket(new WareHouseWithdrawalList(1, player, WareHouseWithdrawalList.FREIGHT));
						player.sendPacket(new WareHouseWithdrawalList(2, player, WareHouseWithdrawalList.FREIGHT));
					}
					else
					{
						player.sendPacket(SystemMessageId.YOU_HAVE_NOT_DEPOSITED_ANY_ITEMS_IN_YOUR_WAREHOUSE);
					}
				}
				break;
			}
			case "attendance_rewards":
			{
				MultisellData.getInstance().separateAndSend(ATTENDANCE_REWARD_MULTISELL, player, null, false);
				break;
			}
		}
		return htmltext;
	}
	
	@RegisterEvent(EventType.ON_PLAYER_BYPASS)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void OnPlayerBypass(OnPlayerBypass event)
	{
		final Player player = event.getPlayer();
		if (event.getCommand().startsWith(COMMAND_BYPASS))
		{
			notifyEvent(event.getCommand().replace(COMMAND_BYPASS, ""), null, player);
		}
	}
	
	public static void main(String[] args)
	{
		new DimensionalMerchant();
	}
}