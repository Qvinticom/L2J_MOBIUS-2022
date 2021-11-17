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
package handlers.itemhandlers;

import org.l2jmobius.gameserver.handler.IItemHandler;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.actor.Playable;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.quest.QuestState;

import quests.Q10589_WhereFatesIntersect.Q10589_WhereFatesIntersect;
import quests.Q10590_ReawakenedFate.Q10590_ReawakenedFate;
import quests.Q10591_NobleMaterial.Q10591_NobleMaterial;
import quests.Q11024_PathOfDestinyBeginning.Q11024_PathOfDestinyBeginning;
import quests.Q11025_PathOfDestinyProving.Q11025_PathOfDestinyProving;
import quests.Q11026_PathOfDestinyConviction.Q11026_PathOfDestinyConviction;
import quests.Q11027_PathOfDestinyOvercome.Q11027_PathOfDestinyOvercome;

/**
 * @author Nasseka, Horus
 */
public class TeleportCube implements IItemHandler
{
	private static final Location TARTI_TELEPORT = new Location(-14180, 123840, -3120);
	private static final Location HERPHAH_TELEPORT = new Location(146532, 26802, -2208);
	private static final Location JOACHIM_TELEPORT = new Location(146524, 26722, -2208);
	private static final Location SILVAN_TELEPORT = new Location(-19353, 136854, -3760);
	private static final Location KALLESIN_TELEPORT = new Location(-41314, 122982, -2904);
	private static final Location ZENATH_TELEPORT = new Location(-46159, 109438, -3808);
	private static final Location PIO_TELEPORT = new Location(-93474, 89730, -3208);
	private static final Location RECLOUS_TELEPORT = new Location(-85049, 105814, -3592);
	private static final Location QS_TELEPORT = new Location(-17916, 143630, -3904);
	private static final Location QS_TELEPORT2 = new Location(-16744, 140209, -3872);
	private static final Location QS1_TELEPORT1 = new Location(-43688, 117592, -3560);
	private static final Location QS1_TELEPORT2 = new Location(-46450, 110273, -3808);
	private static final Location QS1_TELEPORT3 = new Location(-51637, 108721, -3720);
	private static final Location QS1_TELEPORT4 = new Location(-4983, 116607, -3344);
	private static final Location QS2_TELEPORT1 = new Location(-76775, 92186, -3688);
	private static final Location QS2_TELEPORT2 = new Location(-81155, 89637, -3728);
	private static final Location QS2_TELEPORT3 = new Location(-85476, 80753, -3048);
	private static final Location QS2_TELEPORT4 = new Location(-87952, 87062, -3416);
	private static final Location QS2_TELEPORT5 = new Location(-91374, 92270, -3360);
	private static final Location QS3_TELEPORT1 = new Location(-89443, 111717, -3336);
	private static final Location QS3_TELEPORT2 = new Location(-92290, 116512, -3472);
	private static final Location QS3_TELEPORT3 = new Location(-92680, 112394, -3696);
	private static final Location QS3_TELEPORT4 = new Location(-93023, 108834, -3856);
	private static final Location QS3_TELEPORT5 = new Location(-95920, 102192, -3544);
	private static final Location QS3_TELEPORT6 = new Location(-88533, 104054, -3416);
	private static final Location QS3_TELEPORT7 = new Location(-78669, 251000, -2971);
	private static final Location QS3_TELEPORT8 = new Location(-14180, 123840, -3120);
	private static final Location QS4_TELEPORT1 = new Location(-14088, 22168, -3621);
	private static final Location QS5_TELEPORT1 = new Location(-14218, 44794, -3595);
	private static final Location QS5_TELEPORT2 = new Location(147452, 22715, -1995);
	private static final Location QS5_TELEPORT3 = new Location(146524, 26722, -2208);
	private static final Location QS6_TELEPORT1 = new Location(111257, 221071, -3550);
	
	@Override
	public boolean useItem(Playable playable, Item item, boolean forceUse)
	{
		final Player player = playable.getActingPlayer();
		if (player == null)
		{
			return false;
		}
		
		final QuestState qs11024 = player.getQuestState(Q11024_PathOfDestinyBeginning.class.getSimpleName());
		final QuestState qs11025 = player.getQuestState(Q11025_PathOfDestinyProving.class.getSimpleName());
		final QuestState qs11026 = player.getQuestState(Q11026_PathOfDestinyConviction.class.getSimpleName());
		final QuestState qs11027 = player.getQuestState(Q11027_PathOfDestinyOvercome.class.getSimpleName());
		final QuestState qs10589 = player.getQuestState(Q10589_WhereFatesIntersect.class.getSimpleName());
		final QuestState qs10590 = player.getQuestState(Q10590_ReawakenedFate.class.getSimpleName());
		final QuestState qs10591 = player.getQuestState(Q10591_NobleMaterial.class.getSimpleName());
		if (((qs11024 == null) || qs11024.isCond(0)) //
			&& ((qs11025 == null) || qs11025.isCond(0)) //
			&& ((qs11026 == null) || qs11026.isCond(0)) //
			&& ((qs11027 == null) || qs11027.isCond(0)) //
			&& ((qs10589 == null) || qs10589.isCond(0)) //
			&& ((qs10590 == null) || qs10590.isCond(0)) //
			&& ((qs10591 == null) || qs10591.isCond(0)))
		{
			return false;
		}
		
		if (qs11024 != null)
		{
			switch (qs11024.getCond())
			{
				case 3:
				{
					player.teleToLocation(QS_TELEPORT);
					return true;
				}
				case 4:
				{
					player.teleToLocation(SILVAN_TELEPORT);
					return true;
				}
				case 6:
				{
					player.teleToLocation(QS_TELEPORT2);
					return true;
				}
				case 7:
				{
					player.teleToLocation(TARTI_TELEPORT);
					return true;
				}
			}
		}
		
		if (qs11025 != null)
		{
			switch (qs11025.getCond())
			{
				case 1:
				{
					player.teleToLocation(QS1_TELEPORT1);
					return true;
				}
				case 2:
				{
					player.teleToLocation(KALLESIN_TELEPORT);
					return true;
				}
				case 4:
				{
					player.teleToLocation(QS1_TELEPORT2);
					return true;
				}
				case 5:
				{
					player.teleToLocation(ZENATH_TELEPORT);
					return true;
				}
				case 7:
				{
					player.teleToLocation(QS1_TELEPORT3);
					return true;
				}
				case 8:
				{
					player.teleToLocation(TARTI_TELEPORT);
					return true;
				}
				case 12:
				{
					player.teleToLocation(QS1_TELEPORT4);
					return true;
				}
			}
		}
		
		if (qs11026 != null)
		{
			switch (qs11026.getCond())
			{
				case 2:
				case 5:
				case 8:
				case 11:
				{
					player.teleToLocation(PIO_TELEPORT);
					return true;
				}
				case 1:
				{
					player.teleToLocation(QS2_TELEPORT1);
					return true;
				}
				case 4:
				{
					player.teleToLocation(QS2_TELEPORT2);
					return true;
				}
				case 7:
				{
					player.teleToLocation(QS2_TELEPORT3);
					return true;
				}
				case 10:
				{
					player.teleToLocation(QS2_TELEPORT4);
					return true;
				}
				case 13:
				{
					player.teleToLocation(QS2_TELEPORT5);
					return true;
				}
				case 14:
				{
					player.teleToLocation(TARTI_TELEPORT);
					return true;
				}
			}
		}
		
		if (qs11027 != null)
		{
			switch (qs11027.getCond())
			{
				case 2:
				case 5:
				case 8:
				case 11:
				case 14:
				{
					player.teleToLocation(RECLOUS_TELEPORT);
					return true;
				}
				case 1:
				{
					player.teleToLocation(QS3_TELEPORT1);
					return true;
				}
				case 4:
				{
					player.teleToLocation(QS3_TELEPORT2);
					return true;
				}
				case 7:
				{
					player.teleToLocation(QS3_TELEPORT3);
					return true;
				}
				case 10:
				{
					player.teleToLocation(QS3_TELEPORT4);
					return true;
				}
				case 13:
				{
					player.teleToLocation(QS3_TELEPORT5);
					return true;
				}
				case 16:
				{
					player.teleToLocation(QS3_TELEPORT6);
					return true;
				}
				case 17:
				{
					player.teleToLocation(TARTI_TELEPORT);
					return true;
				}
				case 21:
				{
					player.teleToLocation(QS3_TELEPORT7);
					return true;
				}
				case 24:
				{
					player.teleToLocation(QS3_TELEPORT8);
					return true;
				}
			}
		}
		
		if (qs10589 != null)
		{
			switch (qs10589.getCond())
			{
				case 2:
				case 3:
				{
					player.teleToLocation(QS4_TELEPORT1);
					return true;
				}
				case 4:
				{
					player.teleToLocation(HERPHAH_TELEPORT);
					return true;
				}
			}
		}
		
		if (qs10590 != null)
		{
			switch (qs10590.getCond())
			{
				case 1:
				case 2:
				{
					player.teleToLocation(QS5_TELEPORT1);
					return true;
				}
				case 3:
				{
					player.teleToLocation(JOACHIM_TELEPORT);
					return true;
				}
				case 5:
				{
					player.teleToLocation(QS5_TELEPORT2);
					return true;
				}
				case 7:
				{
					player.teleToLocation(QS5_TELEPORT3);
					return true;
				}
			}
		}
		
		if (qs10591 != null)
		{
			switch (qs10591.getCond())
			{
				case 1:
				case 2:
				case 3:
				{
					player.teleToLocation(JOACHIM_TELEPORT);
					return true;
				}
				case 5:
				{
					player.teleToLocation(QS6_TELEPORT1);
					return true;
				}
			}
		}
		
		return false;
	}
}
