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
package instances.MysticTavern;

import java.util.ArrayList;
import java.util.List;

import com.l2jmobius.gameserver.ai.CtrlIntention;
import com.l2jmobius.gameserver.enums.ChatType;
import com.l2jmobius.gameserver.instancemanager.InstanceManager;
import com.l2jmobius.gameserver.instancemanager.ZoneManager;
import com.l2jmobius.gameserver.model.L2Party;
import com.l2jmobius.gameserver.model.Location;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.model.zone.type.L2ScriptZone;
import com.l2jmobius.gameserver.network.NpcStringId;
import com.l2jmobius.gameserver.network.serverpackets.NpcSay;
import com.l2jmobius.gameserver.network.serverpackets.OnEventTrigger;
import com.l2jmobius.gameserver.network.serverpackets.PlaySound;

import ai.AbstractNpcAI;
import instances.MysticTavern.StoryOfFreya.StoryOfFreya;
import quests.Q10297_GrandOpeningComeToOurPub.Q10297_GrandOpeningComeToOurPub;

/**
 * This AI manages the entry to the Mystic Tavern instances.
 * @URL https://l2wiki.com/Mystic_Tavern
 * @VIDEO FREYA: https://www.youtube.com/watch?v=-pUB6ghrsLI
 * @VIDEO TAUTI: https://www.youtube.com/watch?v=_Wz-kxXzJK4
 * @VIDEO KELBIM: https://www.youtube.com/watch?v=wL1D49u6vxE
 * @author Mobius, Gigi
 */
public class MysticTavern extends AbstractNpcAI
{
	// NPC
	private static final int GLOBE = 34200;
	// Employee
	private static final int LOLLIA = 34182;
	private static final int HANNA = 34183;
	private static final int BRODIEN = 34184;
	private static final int LUPIA = 34185;
	private static final int MEY = 34186;
	// Instances
	// private static final int INSTANCE_TAUTI = 261;
	// private static final int INSTANCE_KELBIM = 262;
	private static final int INSTANCE_FREYA = 263;
	// Zones
	private static final L2ScriptZone GLOBE_1_ZONE = ZoneManager.getInstance().getZoneById(80019, L2ScriptZone.class);
	private static final L2ScriptZone GLOBE_2_ZONE = ZoneManager.getInstance().getZoneById(80020, L2ScriptZone.class);
	private static final L2ScriptZone GLOBE_3_ZONE = ZoneManager.getInstance().getZoneById(80021, L2ScriptZone.class);
	private static final L2ScriptZone GLOBE_4_ZONE = ZoneManager.getInstance().getZoneById(80022, L2ScriptZone.class);
	private static final L2ScriptZone GLOBE_5_ZONE = ZoneManager.getInstance().getZoneById(80023, L2ScriptZone.class);
	// Misc
	private static final int MINIMUM_PLAYER_LEVEL = 99;
	private static final int MINIMUM_PARTY_MEMBERS = 5;
	private static L2Npc _lollia;
	private static L2Npc _hanna;
	private static L2Npc _brodien;
	private static L2Npc _lupia;
	private static L2Npc _mey;
	
	public MysticTavern()
	{
		addFirstTalkId(GLOBE);
		addSpawnId(LOLLIA, HANNA, BRODIEN, LUPIA, MEY);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final List<Integer> availableInstances = new ArrayList<>();
		availableInstances.add(INSTANCE_FREYA);
		// availableInstances.add(INSTANCE_TAUTI);
		// availableInstances.add(INSTANCE_KELBIM);
		switch (event)
		{
			case "tellStory":
			{
				if (!player.isGM())
				{
					final L2Party party = player.getParty();
					if (party == null)
					{
						return "34200-no-party.html";
					}
					if (party.getLeader() != player)
					{
						return "34200-no-leader.html";
					}
					if (party.getMemberCount() < MINIMUM_PARTY_MEMBERS)
					{
						return "34200-not-enough-members.html";
					}
					for (L2PcInstance member : party.getMembers())
					{
						if ((member == null) || !member.isSitting() || (member.calculateDistance3D(player) > 500))
						{
							return "34200-not-sitting.html";
						}
						if (member.getLevel() < MINIMUM_PLAYER_LEVEL)
						{
							return "34200-no-level.html";
						}
						final QuestState qs = member.getQuestState(Q10297_GrandOpeningComeToOurPub.class.getSimpleName());
						if ((qs == null) || !qs.isCompleted())
						{
							return "34200-no-quest.html";
						}
						if (InstanceManager.getInstance().getInstanceTime(member, INSTANCE_FREYA) > 0)
						{
							availableInstances.remove(INSTANCE_FREYA);
						}
						// if (InstanceManager.getInstance().getInstanceTime(member, INSTANCE_TAUTI) > 0)
						// {
						// availableInstances.remove(INSTANCE_TAUTI);
						// }
						// if (InstanceManager.getInstance().getInstanceTime(member, INSTANCE_KELBIM) > 0)
						// {
						// availableInstances.remove(INSTANCE_KELBIM);
						// }
					}
					if (availableInstances.isEmpty())
					{
						return "34200-not-available.html";
					}
					startQuestTimer("npcRoute", 3000, npc, player);
				}
				startQuestTimer("npcRoute", 3000, npc, player); // TODO only for test
				break;
			}
			case "npcRoute":
			{
				if (GLOBE_1_ZONE.isInsideZone(npc))
				{
					npc.broadcastPacket(new OnEventTrigger(18133000, true));
					_brodien.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(-50000, -148560, -14152));
					_brodien.setHeading(48440);
					startQuestTimer("msg_text_brodien_1", 16000, _brodien, null);
					startQuestTimer("msg_text_brodien_2", 23000, _brodien, null);
					startQuestTimer("msg_text_brodien_3", 31000, _brodien, null);
				}
				else if (GLOBE_2_ZONE.isInsideZone(npc))
				{
					npc.broadcastPacket(new OnEventTrigger(18132000, true));
					_lupia.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(-50161, -148356, -14152));
					_lupia.setHeading(45808);
					startQuestTimer("msg_text_lupia_1", 12000, _lupia, null);
					startQuestTimer("msg_text_lupia_2", 19000, _lupia, null);
					startQuestTimer("msg_text_lupia_3", 27000, _lupia, null);
				}
				else if (GLOBE_3_ZONE.isInsideZone(npc))
				{
					npc.broadcastPacket(new OnEventTrigger(18131000, true));
					_mey.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(-50092, -148096, -14152));
					_mey.setHeading(34669);
					startQuestTimer("msg_text_mey_1", 8000, _mey, null);
					startQuestTimer("msg_text_mey_2", 15000, _mey, null);
					startQuestTimer("msg_text_mey_3", 23000, _mey, null);
				}
				else if (GLOBE_4_ZONE.isInsideZone(npc))
				{
					npc.broadcastPacket(new OnEventTrigger(18135000, true));
					_lollia.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(-49480, -148074, -14152));
					_lollia.setHeading(13255);
					startQuestTimer("msg_text_lollia_1", 10000, _lollia, null);
					startQuestTimer("msg_text_lollia_2", 17000, _lollia, null);
					startQuestTimer("msg_text_lollia_3", 25000, _lollia, null);
				}
				else if (GLOBE_5_ZONE.isInsideZone(npc))
				{
					npc.broadcastPacket(new OnEventTrigger(18134000, true));
					_hanna.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(-49283, -148179, -14152));
					_hanna.setHeading(26747);
					startQuestTimer("msg_text_hanna_1", 12000, _hanna, null);
					startQuestTimer("msg_text_hanna_2", 19000, _hanna, null);
					startQuestTimer("msg_text_hanna_3", 27000, _hanna, null);
				}
				break;
			}
			case "msg_text_brodien_1":
			{
				_brodien.broadcastPacket(new NpcSay(_brodien.getObjectId(), ChatType.NPC_GENERAL, _brodien.getId(), NpcStringId.I_HAVE_MANY_STORIES_TO_TELL));
				GLOBE_1_ZONE.broadcastPacket(new PlaySound(3, "Npcdialog1.brodien_inzone_1", 0, 0, 0, 0, 0));
				break;
			}
			case "msg_text_brodien_2":
			{
				_brodien.broadcastPacket(new NpcSay(_brodien.getObjectId(), ChatType.NPC_GENERAL, _brodien.getId(), NpcStringId.PLEASE_SIT_DOWN_SO_THAT_I_CAN_START));
				GLOBE_1_ZONE.broadcastPacket(new PlaySound(3, "Npcdialog1.brodien_inzone_2", 0, 0, 0, 0, 0));
				break;
			}
			case "msg_text_brodien_3":
			{
				_brodien.broadcastPacket(new NpcSay(_brodien.getObjectId(), ChatType.NPC_GENERAL, _brodien.getId(), NpcStringId.WELL_WHOSE_STORY_SHOULD_I_TELL_YOU_TODAY));
				GLOBE_1_ZONE.broadcastPacket(new PlaySound(3, "Npcdialog1.brodien_inzone_3", 0, 0, 0, 0, 0));
				startQuestTimer("enter_instance", 3000, npc, player);
				startQuestTimer("return", 6000, npc, null);
				npc.broadcastPacket(new OnEventTrigger(18133000, false));
				break;
			}
			case "msg_text_lupia_1":
			{
				_lupia.broadcastPacket(new NpcSay(_lupia.getObjectId(), ChatType.NPC_GENERAL, _lupia.getId(), NpcStringId.HURRY_SIT_DOWN));
				GLOBE_2_ZONE.broadcastPacket(new PlaySound(3, "Npcdialog1.rupia_inzone_1", 0, 0, 0, 0, 0));
				break;
			}
			case "msg_text_lupia_2":
			{
				_lupia.broadcastPacket(new NpcSay(_lupia.getObjectId(), ChatType.NPC_GENERAL, _lupia.getId(), NpcStringId.WHOSE_STORY_DO_YOU_WANT_TO_HEAR));
				GLOBE_2_ZONE.broadcastPacket(new PlaySound(3, "Npcdialog1.rupia_inzone_2", 0, 0, 0, 0, 0));
				break;
			}
			case "msg_text_lupia_3":
			{
				_lupia.broadcastPacket(new NpcSay(_lupia.getObjectId(), ChatType.NPC_GENERAL, _lupia.getId(), NpcStringId.YOU_HAVE_TO_BE_READY));
				GLOBE_2_ZONE.broadcastPacket(new PlaySound(3, "Npcdialog1.rupia_inzone_3", 0, 0, 0, 0, 0));
				startQuestTimer("enter_instance", 3000, npc, player);
				startQuestTimer("return", 6000, npc, null);
				npc.broadcastPacket(new OnEventTrigger(18132000, false));
				break;
			}
			case "msg_text_mey_1":
			{
				_mey.broadcastPacket(new NpcSay(_mey.getObjectId(), ChatType.NPC_GENERAL, _mey.getId(), NpcStringId.SHOULD_I_START_LET_S_SEE_IF_WE_RE_READY));
				GLOBE_3_ZONE.broadcastPacket(new PlaySound(3, "Npcdialog1.mae_inzone_1", 0, 0, 0, 0, 0));
				break;
			}
			case "msg_text_mey_2":
			{
				_mey.broadcastPacket(new NpcSay(_mey.getObjectId(), ChatType.NPC_GENERAL, _mey.getId(), NpcStringId.I_LL_BE_STARTING_NOW_SO_TAKE_A_SEAT));
				GLOBE_3_ZONE.broadcastPacket(new PlaySound(3, "Npcdialog1.mae_inzone_2", 0, 0, 0, 0, 0));
				break;
			}
			case "msg_text_mey_3":
			{
				_mey.broadcastPacket(new NpcSay(_mey.getObjectId(), ChatType.NPC_GENERAL, _mey.getId(), NpcStringId.WHICH_STORY_DO_YOU_WANT_TO_HEAR));
				GLOBE_3_ZONE.broadcastPacket(new PlaySound(3, "Npcdialog1.mae_inzone_3", 0, 0, 0, 0, 0));
				startQuestTimer("enter_instance", 3000, npc, player);
				startQuestTimer("return", 6000, npc, null);
				npc.broadcastPacket(new OnEventTrigger(18131000, false));
				break;
			}
			case "msg_text_lollia_1":
			{
				_lollia.broadcastPacket(new NpcSay(_lollia.getObjectId(), ChatType.NPC_GENERAL, _lollia.getId(), NpcStringId.ARE_YOU_READY_TO_HEAR_THE_STORY));
				GLOBE_4_ZONE.broadcastPacket(new PlaySound(3, "Npcdialog1.lollia_inzone_1", 0, 0, 0, 0, 0));
				break;
			}
			case "msg_text_lollia_2":
			{
				_lollia.broadcastPacket(new NpcSay(_lollia.getObjectId(), ChatType.NPC_GENERAL, _lollia.getId(), NpcStringId.I_LL_START_ONCE_EVERYONE_IS_SEATED));
				GLOBE_4_ZONE.broadcastPacket(new PlaySound(3, "Npcdialog1.lollia_inzone_2", 0, 0, 0, 0, 0));
				break;
			}
			case "msg_text_lollia_3":
			{
				_lollia.broadcastPacket(new NpcSay(_lollia.getObjectId(), ChatType.NPC_GENERAL, _lollia.getId(), NpcStringId.HEH_WHAT_SHOULD_I_TALK_ABOUT_NEXT_HMM));
				GLOBE_4_ZONE.broadcastPacket(new PlaySound(3, "Npcdialog1.lollia_inzone_3", 0, 0, 0, 0, 0));
				startQuestTimer("enter_instance", 3000, npc, player);
				startQuestTimer("return", 6000, npc, null);
				npc.broadcastPacket(new OnEventTrigger(18135000, false));
				break;
			}
			case "msg_text_hanna_1":
			{
				_hanna.setHeading(26747);
				_hanna.broadcastPacket(new NpcSay(_hanna.getObjectId(), ChatType.NPC_GENERAL, _hanna.getId(), NpcStringId.WELL_WHICH_STORY_DO_YOU_WANT_TO_HEAR));
				GLOBE_5_ZONE.broadcastPacket(new PlaySound(3, "Npcdialog1.hanna_inzone_1", 0, 0, 0, 0, 0));
				break;
			}
			case "msg_text_hanna_2":
			{
				_hanna.broadcastPacket(new NpcSay(_hanna.getObjectId(), ChatType.NPC_GENERAL, _hanna.getId(), NpcStringId.I_WONDER_WHAT_KIND_OF_STORIES_ARE_POPULAR_WITH_THE_CUSTOMERS));
				GLOBE_5_ZONE.broadcastPacket(new PlaySound(3, "Npcdialog1.hanna_inzone_2", 0, 0, 0, 0, 0));
				break;
			}
			case "msg_text_hanna_3":
			{
				_hanna.broadcastPacket(new NpcSay(_hanna.getObjectId(), ChatType.NPC_GENERAL, _hanna.getId(), NpcStringId.SIT_DOWN_FIRST_I_CAN_T_START_OTHERWISE));
				GLOBE_5_ZONE.broadcastPacket(new PlaySound(3, "Npcdialog1.hanna_inzone_3", 0, 0, 0, 0, 0));
				startQuestTimer("enter_instance", 3000, npc, player);
				startQuestTimer("return", 6000, npc, null);
				npc.broadcastPacket(new OnEventTrigger(18134000, false));
				break;
			}
			case "return":
			{
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, npc.getSpawn());
				npc.setHeading(npc.getSpawn().getHeading());
				break;
			}
			case "enter_instance":
			{
				// enter randomly
				switch (availableInstances.get(getRandom(availableInstances.size())))
				{
					case INSTANCE_FREYA:
					{
						player.processQuestEvent(StoryOfFreya.class.getSimpleName(), "start_story");
						break;
					}
					// case INSTANCE_TAUTI:
					// {
					// player.processQuestEvent(StoryOfTauti.class.getSimpleName(), "start_story");
					// break;
					// }
					// case INSTANCE_KELBIM:
					// {
					// player.processQuestEvent(StoryOfKelbim.class.getSimpleName(), "start_story");
					// break;
					// }
				}
				break;
			}
		}
		return null;
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		switch (npc.getId())
		{
			case LOLLIA:
			{
				_lollia = npc;
				break;
			}
			case HANNA:
			{
				_hanna = npc;
				break;
			}
			case BRODIEN:
			{
				_brodien = npc;
				break;
			}
			case LUPIA:
			{
				_lupia = npc;
				break;
			}
			case MEY:
			{
				_mey = npc;
				break;
			}
		}
		return super.onSpawn(npc);
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		return "34200.html";
	}
	
	public static void main(String[] args)
	{
		new MysticTavern();
	}
}
