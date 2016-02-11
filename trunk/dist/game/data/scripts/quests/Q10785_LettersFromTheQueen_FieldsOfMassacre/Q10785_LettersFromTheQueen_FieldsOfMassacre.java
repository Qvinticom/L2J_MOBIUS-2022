package quests.Q10785_LettersFromTheQueen_FieldsOfMassacre;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.ai.CtrlIntention;
import com.l2jmobius.gameserver.cache.HtmCache;
import com.l2jmobius.gameserver.enums.QuestSound;
import com.l2jmobius.gameserver.enums.Race;
import com.l2jmobius.gameserver.handler.BypassHandler;
import com.l2jmobius.gameserver.handler.IBypassHandler;
import com.l2jmobius.gameserver.model.Location;
import com.l2jmobius.gameserver.model.actor.L2Character;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.events.EventType;
import com.l2jmobius.gameserver.model.events.ListenerRegisterType;
import com.l2jmobius.gameserver.model.events.annotations.RegisterEvent;
import com.l2jmobius.gameserver.model.events.annotations.RegisterType;
import com.l2jmobius.gameserver.model.events.impl.character.player.OnPlayerLevelChanged;
import com.l2jmobius.gameserver.model.holders.ItemHolder;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.model.quest.State;
import com.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;
import com.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * Letters From The Queen: Fields Of Massacre (10785)
 * @URL https://l2wiki.com/Letters_from_the_Queen:_Fields_of_Massacre
 * @author Gigi
 */
public class Q10785_LettersFromTheQueen_FieldsOfMassacre extends Quest implements IBypassHandler
{
	// NPCs
	private static final int ORVEN = 30857;
	private static final int SHUVANN = 33867;
	// Items
	private static final ItemHolder SCROLL_OF_ESCAPE_FIELDS_OF_MASSACRE = new ItemHolder(37029, 1);
	private static final ItemHolder STEEL_DOOR_GUILD = new ItemHolder(37045, 71);
	private static final ItemHolder EWA = new ItemHolder(729, 1);
	// Reward
	private static final int EXP_REWARD = 807240;
	private static final int SP_REWARD = 193;
	// Misc
	private static final int MIN_LEVEL = 61;
	private static final int MAX_LEVEL = 64;
	// Teleport
	private static final Location TP_LOC = new Location(147446, 22761, -1984);
	private static final String[] TP_COMMANDS =
	{
		"Q10785_Teleport"
	};
	
	public Q10785_LettersFromTheQueen_FieldsOfMassacre()
	{
		super(10785, Q10785_LettersFromTheQueen_FieldsOfMassacre.class.getSimpleName(), "Letters from the Queen: FieldsOfMassacre");
		addStartNpc(ORVEN);
		addTalkId(ORVEN, SHUVANN);
		addCondRace(Race.ERTHEIA, "noErtheia.html");
		addCondMinLevel(MIN_LEVEL, "no_level.html");
		BypassHandler.getInstance().registerHandler(this);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return null;
		}
		String htmltext = null;
		switch (event)
		{
			case "30857-02.htm":
			case "30857-04.html":
			case "33867-02.htm":
			{
				htmltext = event;
				break;
			}
			case "30857-03.htm": // start the quest
			{
				qs.startQuest();
				if (getQuestItemsCount(player, SCROLL_OF_ESCAPE_FIELDS_OF_MASSACRE.getId()) < 1)
				{
					giveItems(player, SCROLL_OF_ESCAPE_FIELDS_OF_MASSACRE);
					player.sendPacket(new ExShowScreenMessage("Try using the teleport scroll Orven gave you to go to Fields of Massacre.", 10000));
					qs.setCond(2, true);
					htmltext = event;
				}
				break;
			}
			case "33867-03.htm":
			{
				if (qs.isCond(2))
				{
					player.sendPacket(new ExShowScreenMessage("Grow stronger here until you receive the next letter from Queen Navari at Lv. 65!", 5000));
					giveItems(player, STEEL_DOOR_GUILD);
					giveItems(player, EWA);
					addExpAndSp(player, EXP_REWARD, SP_REWARD);
					playSound(player, QuestSound.ITEMSOUND_QUEST_FINISH);
					qs.exitQuest(false, true);
					htmltext = event;
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		final QuestState qs = getQuestState(player, true);
		String htmltext = null;
		if (qs == null)
		{
			return htmltext;
		}
		
		if (player.getRace() != Race.ERTHEIA)
		{
			return "noErtheia.html";
		}
		
		switch (qs.getState())
		{
			case State.CREATED:
			{
				switch (npc.getId())
				{
					case ORVEN:
					{
						htmltext = "30857-01.html";
						break;
					}
					case SHUVANN:
					{
						if (player.getRace() != Race.ERTHEIA)
						{
							htmltext = getNoQuestMsg(player);
						}
						else if (qs.isCreated())
						{
							htmltext = getNoQuestMsg(player);
						}
						break;
					}
				}
				break;
			}
			case State.STARTED:
			{
				switch (npc.getId())
				{
					case ORVEN:
					{
						if (qs.isCond(1))
						{
							playSound(player, QuestSound.ITEMSOUND_QUEST_MIDDLE);
							htmltext = "30857-03.htm";
						}
						else if (qs.isCond(2))
						{
							htmltext = "30857-04.html";
						}
						break;
					}
					case SHUVANN:
					{
						if (qs.isCond(2))
						{
							htmltext = "33867-01.html";
						}
						break;
					}
				}
				break;
			}
			case State.COMPLETED:
			{
				htmltext = getAlreadyCompletedMsg(player);
				break;
			}
		}
		return htmltext;
	}
	
	@RegisterEvent(EventType.ON_PLAYER_LEVEL_CHANGED)
	@RegisterType(ListenerRegisterType.GLOBAL)
	public void OnPlayerLevelChanged(OnPlayerLevelChanged event)
	{
		if (Config.DISABLE_TUTORIAL)
		{
			return;
		}
		final L2PcInstance player = event.getActiveChar();
		final int oldLevel = event.getOldLevel();
		final int newLevel = event.getNewLevel();
		if ((oldLevel == (newLevel - 1)) && (player.getLevel() >= MIN_LEVEL) && (player.getLevel() <= MAX_LEVEL) && (player.getRace() == Race.ERTHEIA))
		{
			final QuestState qs = getQuestState(player, false);
			if (qs == null)
			{
				final NpcHtmlMessage html = new NpcHtmlMessage(0, 0);
				html.setHtml(HtmCache.getInstance().getHtm(player.getHtmlPrefix(), "scripts/quests/Q10785_LettersFromTheQueen_FieldsOfMassacre/Announce.html"));
				player.sendPacket(html);
			}
		}
	}
	
	@Override
	public boolean useBypass(String command, L2PcInstance player, L2Character bypassOrigin)
	{
		final QuestState qs = getQuestState(player, false);
		if ((qs != null) || (player.getLevel() < MIN_LEVEL) || (player.getLevel() > MAX_LEVEL) || (player.getRace() != Race.ERTHEIA))
		{
			return false;
		}
		if (player.isInParty())
		{
			player.sendPacket(new ExShowScreenMessage("You cannot teleport when you are in party.", 5000));
		}
		else if (player.isInCombat())
		{
			player.sendPacket(new ExShowScreenMessage("You cannot teleport when you in combat status.", 5000));
		}
		else if (player.isInDuel())
		{
			player.sendPacket(new ExShowScreenMessage("You cannot teleport when you are in a duel.", 5000));
		}
		else if (player.isInOlympiadMode())
		{
			player.sendPacket(new ExShowScreenMessage("You cannot teleport when you are in Olympiad.", 5000));
		}
		else if (player.isInVehicle())
		{
			player.sendPacket(new ExShowScreenMessage("You cannot teleport when you are in any vehicle or mount.", 5000));
		}
		else
		{
			player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			player.teleToLocation(TP_LOC);
		}
		return true;
	}
	
	@Override
	public String[] getBypassList()
	{
		return TP_COMMANDS;
	}
}