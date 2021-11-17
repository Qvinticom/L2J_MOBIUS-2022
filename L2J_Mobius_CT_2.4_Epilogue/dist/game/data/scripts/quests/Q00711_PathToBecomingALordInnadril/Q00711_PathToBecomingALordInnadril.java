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
package quests.Q00711_PathToBecomingALordInnadril;

import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.instancemanager.CastleManager;
import org.l2jmobius.gameserver.instancemanager.FortManager;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.clan.Clan;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;
import org.l2jmobius.gameserver.model.siege.Castle;
import org.l2jmobius.gameserver.model.siege.Fort;
import org.l2jmobius.gameserver.network.NpcStringId;
import org.l2jmobius.gameserver.network.serverpackets.NpcSay;

/**
 * Path to Becoming a Lord - Innadril (711)
 * @author Sacrifice
 */
public final class Q00711_PathToBecomingALordInnadril extends Quest
{
	private static final int NEURATH = 35316;
	private static final int IASON_HEINE = 30969;
	
	private static final int[] MOBS =
	{
		20789, // Crokian
		20790, // Dailaon
		20791, // Crokian Warrior
		20792, // Farhite
		20793, // Nos
		20804, // Crokian Lad
		20805, // Dailaon Lad
		20806, // Crokian Lad Warrior
		20807, // Farhite Lad
		20808 // Nos Lad
	};
	
	private static final int INNADRIL_CASTLE = 6;
	
	public Q00711_PathToBecomingALordInnadril()
	{
		super(711);
		addStartNpc(NEURATH);
		addKillId(MOBS);
		addTalkId(NEURATH, IASON_HEINE);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		final QuestState qs = player.getQuestState(getName());
		final Castle castle = CastleManager.getInstance().getCastleById(INNADRIL_CASTLE);
		final Player castleOwner = castle.getOwner().getLeader().getPlayer();
		switch (event)
		{
			case "35316-03.html":
			{
				qs.startQuest();
				break;
			}
			case "35316-05.html":
			{
				qs.setCond(2);
				break;
			}
			case "35316-08.html":
			{
				if (isLordAvailable(2, qs))
				{
					castleOwner.getQuestState(getName()).set("confidant", String.valueOf(qs.getPlayer().getObjectId()));
					castleOwner.getQuestState(getName()).setCond(3);
					qs.setState(State.STARTED);
				}
				else
				{
					htmltext = "35316-07a.html";
				}
				break;
			}
			case "30969-03.html":
			{
				if (isLordAvailable(3, qs))
				{
					castleOwner.getQuestState(getName()).setCond(4);
				}
				else
				{
					htmltext = "30969-00a.html";
				}
				break;
			}
			case "35316-12.html":
			{
				if (castleOwner != null)
				{
					final NpcSay packet = new NpcSay(npc.getObjectId(), ChatType.NPC_SHOUT, npc.getId(), NpcStringId.S1_HAS_BECOME_THE_LORD_OF_THE_TOWN_OF_INNADRIL_MAY_THERE_BE_GLORY_IN_THE_TERRITORY_OF_INNADRIL);
					packet.addStringParameter(player.getName());
					npc.broadcastPacket(packet);
					qs.exitQuest(true, true);
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		final QuestState qs = killer.getQuestState(getName());
		if ((qs != null) && qs.isCond(5))
		{
			if (qs.getInt("mobs") < 99)
			{
				qs.set("mobs", String.valueOf(qs.getInt("mobs") + 1));
			}
			else
			{
				qs.setCond(6);
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	@Override
	public String onTalk(Npc npc, Player talker)
	{
		final QuestState qs = getQuestState(talker, true);
		String htmltext = getNoQuestMsg(talker);
		final Castle castle = CastleManager.getInstance().getCastleById(INNADRIL_CASTLE);
		if (castle.getOwner() == null)
		{
			return "Castle has no lord.";
		}
		
		final Player castleOwner = castle.getOwner().getLeader().getPlayer();
		
		switch (npc.getId())
		{
			case NEURATH:
			{
				if (qs.isCond(0))
				{
					if (castleOwner == qs.getPlayer())
					{
						if (!hasFort())
						{
							htmltext = "35316-01.html";
						}
						else
						{
							htmltext = "35316-00.html";
							qs.exitQuest(true);
						}
					}
					else if (isLordAvailable(2, qs))
					{
						if (castleOwner.calculateDistance2D(npc) <= 200)
						{
							htmltext = "35316-07.html";
						}
						else
						{
							htmltext = "35316-07a.html";
						}
					}
					else if (qs.getState() == State.STARTED)
					{
						htmltext = "35316-00b.html";
					}
					else
					{
						htmltext = "35316-00a.html";
						qs.exitQuest(true);
					}
				}
				else if (qs.isCond(1))
				{
					htmltext = "35316-04.html";
				}
				else if (qs.isCond(2))
				{
					htmltext = "35316-06.html";
				}
				else if (qs.isCond(3))
				{
					htmltext = "35316-09.html";
				}
				else if (qs.isCond(4))
				{
					qs.setCond(5);
					htmltext = "35316-10.html";
				}
				else if (qs.isCond(5))
				{
					htmltext = "35316-10.html";
				}
				else if (qs.isCond(6))
				{
					htmltext = "35316-11.html";
				}
				break;
			}
			case IASON_HEINE:
			{
				if ((qs.getState() == State.STARTED) && qs.isCond(0))
				{
					if (isLordAvailable(3, qs))
					{
						if (castleOwner.getQuestState(getName()).getInt("confidant") == qs.getPlayer().getObjectId())
						{
							htmltext = "30969-01.html";
						}
						else
						{
							htmltext = "30969-00.html";
						}
					}
					else if (isLordAvailable(4, qs))
					{
						if (castleOwner.getQuestState(getName()).getInt("confidant") == qs.getPlayer().getObjectId())
						{
							htmltext = "30969-03.html";
						}
						else
						{
							htmltext = "30969-00.html";
						}
					}
					else
					{
						htmltext = "30969-00a.html";
					}
				}
				break;
			}
		}
		return htmltext;
	}
	
	private boolean hasFort()
	{
		for (Fort fortress : FortManager.getInstance().getForts())
		{
			if (fortress.getContractedCastleId() == INNADRIL_CASTLE)
			{
				return true;
			}
		}
		return false;
	}
	
	private boolean isLordAvailable(int cond, QuestState qs)
	{
		final Castle castle = CastleManager.getInstance().getCastleById(INNADRIL_CASTLE);
		final Clan owner = castle.getOwner();
		final Player castleOwner = castle.getOwner().getLeader().getPlayer();
		return (owner != null) && (castleOwner != null) && (castleOwner != qs.getPlayer()) && (owner == qs.getPlayer().getClan()) && (castleOwner.getQuestState(getName()) != null) && (castleOwner.getQuestState(getName()).isCond(cond));
	}
}