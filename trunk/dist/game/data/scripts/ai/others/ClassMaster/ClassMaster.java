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
package ai.others.ClassMaster;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.l2jmobius.commons.util.IGameXmlReader;
import com.l2jmobius.commons.util.Rnd;
import com.l2jmobius.gameserver.data.xml.impl.CategoryData;
import com.l2jmobius.gameserver.data.xml.impl.ClassListData;
import com.l2jmobius.gameserver.datatables.ItemTable;
import com.l2jmobius.gameserver.enums.CategoryType;
import com.l2jmobius.gameserver.enums.Race;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.base.ClassId;
import com.l2jmobius.gameserver.model.events.EventType;
import com.l2jmobius.gameserver.model.events.ListenerRegisterType;
import com.l2jmobius.gameserver.model.events.annotations.RegisterEvent;
import com.l2jmobius.gameserver.model.events.annotations.RegisterType;
import com.l2jmobius.gameserver.model.events.impl.character.player.OnPlayerBypass;
import com.l2jmobius.gameserver.model.events.impl.character.player.OnPlayerLevelChanged;
import com.l2jmobius.gameserver.model.events.impl.character.player.OnPlayerLogin;
import com.l2jmobius.gameserver.model.events.impl.character.player.OnPlayerPressTutorialMark;
import com.l2jmobius.gameserver.model.events.impl.character.player.OnPlayerProfessionChange;
import com.l2jmobius.gameserver.model.holders.ItemChanceHolder;
import com.l2jmobius.gameserver.model.spawns.SpawnTemplate;
import com.l2jmobius.gameserver.network.serverpackets.AcquireSkillList;
import com.l2jmobius.gameserver.network.serverpackets.PlaySound;
import com.l2jmobius.gameserver.network.serverpackets.TutorialCloseHtml;
import com.l2jmobius.gameserver.network.serverpackets.TutorialShowQuestionMark;

import ai.AbstractNpcAI;

/**
 * Class Master AI.
 * @author Nik
 */
public final class ClassMaster extends AbstractNpcAI implements IGameXmlReader
{
	// NPCs
	private static final int[] CLASS_MASTER =
	{
		31756, // Mr. Cat
		31757, // Queen of Hearts
	};
	// Misc
	private boolean _isEnabled;
	private boolean _spawnClassMasters;
	private boolean _showPopupWindow;
	private static final Logger LOGGER = Logger.getLogger(ClassMaster.class.getName());
	private final List<ClassChangeData> _classChangeData = new LinkedList<>();
	
	public ClassMaster()
	{
		load();
		addStartNpc(CLASS_MASTER);
		addTalkId(CLASS_MASTER);
		addFirstTalkId(CLASS_MASTER);
	}
	
	@Override
	public void load()
	{
		_classChangeData.clear();
		parseDatapackFile("config/ClassMaster.xml");
		
		LOGGER.info(getClass().getSimpleName() + ": Loaded " + _classChangeData.size() + " class change options.");
	}
	
	@Override
	public boolean isValidating()
	{
		return false;
	}
	
	@Override
	public void parseDocument(Document doc, File f)
	{
		NamedNodeMap attrs;
		for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equals(n.getNodeName()))
			{
				for (Node cm = n.getFirstChild(); cm != null; cm = cm.getNextSibling())
				{
					attrs = cm.getAttributes();
					if ("classMaster".equals(cm.getNodeName()))
					{
						_isEnabled = parseBoolean(attrs, "classChangeEnabled", false);
						if (!_isEnabled)
						{
							return;
						}
						
						_spawnClassMasters = parseBoolean(attrs, "spawnClassMasters", true);
						_showPopupWindow = parseBoolean(attrs, "showPopupWindow", false);
						
						for (Node c = cm.getFirstChild(); c != null; c = c.getNextSibling())
						{
							attrs = c.getAttributes();
							if ("classChangeOption".equals(c.getNodeName()))
							{
								final List<CategoryType> appliedCategories = new LinkedList<>();
								final List<ItemChanceHolder> requiredItems = new LinkedList<>();
								final List<ItemChanceHolder> rewardedItems = new LinkedList<>();
								boolean setNoble = false;
								boolean setHero = false;
								final String optionName = parseString(attrs, "name", "");
								for (Node b = c.getFirstChild(); b != null; b = b.getNextSibling())
								{
									attrs = b.getAttributes();
									if ("appliesTo".equals(b.getNodeName()))
									{
										for (Node r = b.getFirstChild(); r != null; r = r.getNextSibling())
										{
											attrs = r.getAttributes();
											if ("category".equals(r.getNodeName()))
											{
												final CategoryType category = CategoryType.findByName(r.getTextContent().trim());
												if (category == null)
												{
													LOGGER.severe(getClass().getSimpleName() + ": Incorrect category type: " + r.getNodeValue());
													continue;
												}
												
												appliedCategories.add(category);
											}
										}
									}
									if ("rewards".equals(b.getNodeName()))
									{
										for (Node r = b.getFirstChild(); r != null; r = r.getNextSibling())
										{
											attrs = r.getAttributes();
											if ("item".equals(r.getNodeName()))
											{
												final int itemId = parseInteger(attrs, "id");
												final int count = parseInteger(attrs, "count", 1);
												final int chance = parseInteger(attrs, "chance", 100);
												
												rewardedItems.add(new ItemChanceHolder(itemId, chance, count));
											}
											else if ("setNoble".equals(r.getNodeName()))
											{
												setNoble = true;
											}
											else if ("setHero".equals(r.getNodeName()))
											{
												setHero = true;
											}
										}
									}
									else if ("conditions".equals(b.getNodeName()))
									{
										for (Node r = b.getFirstChild(); r != null; r = r.getNextSibling())
										{
											attrs = r.getAttributes();
											if ("item".equals(r.getNodeName()))
											{
												final int itemId = parseInteger(attrs, "id");
												final int count = parseInteger(attrs, "count", 1);
												final int chance = parseInteger(attrs, "chance", 100);
												
												requiredItems.add(new ItemChanceHolder(itemId, chance, count));
											}
										}
									}
								}
								
								if (appliedCategories.isEmpty())
								{
									LOGGER.warning(getClass().getSimpleName() + ": Class change option: " + optionName + " has no categories to be applied on. Skipping!");
									continue;
								}
								
								final ClassChangeData classChangeData = new ClassChangeData(optionName, appliedCategories);
								classChangeData.setItemsRequired(requiredItems);
								classChangeData.setItemsRewarded(rewardedItems);
								classChangeData.setRewardHero(setHero);
								classChangeData.setRewardNoblesse(setNoble);
								
								_classChangeData.add(classChangeData);
							}
						}
					}
				}
			}
		}
	}
	
	@Override
	public void onSpawnActivate(SpawnTemplate template)
	{
		if (_spawnClassMasters)
		{
			template.spawnAllIncludingNotDefault(null);
		}
	}
	
	@Override
	public void onSpawnDeactivate(SpawnTemplate template)
	{
		template.despawnAll();
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		return "test_server_helper001.html";
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (!_isEnabled)
		{
			return null;
		}
		
		String htmltext = null;
		final StringTokenizer st = new StringTokenizer(event);
		event = st.nextToken();
		switch (event)
		{
			case "buyitems":
			{
				htmltext = npc.getId() == CLASS_MASTER[0] ? "test_server_helper001a.html" : "test_server_helper001b.html";
				break;
			}
			case "setnoble":
			{
				if (player.isNoble())
				{
					htmltext = "test_server_helper025b.html";
				}
				else if (player.getLevel() < 75)
				{
					htmltext = "test_server_helper025a.html";
				}
				else
				{
					player.setNoble(true);
					player.broadcastUserInfo();
					// TODO: SetOneTimeQuestFlag(talker, 10385, 1);
					htmltext = "test_server_helper025.html";
				}
				break;
			}
			case "firstclass":
			{
				if (player.isInCategory(CategoryType.FIRST_CLASS_GROUP))
				{
					if (player.getRace() == Race.ERTHEIA)
					{
						htmltext = "test_server_helper027a.html";
					}
					else if (player.getLevel() < 20)
					{
						htmltext = "test_server_helper027.html";
					}
					else
					{
						htmltext = getFirstOccupationChangeHtml(player);
					}
				}
				else if (player.isInCategory(CategoryType.SECOND_CLASS_GROUP))
				{
					htmltext = "test_server_helper028.html";
				}
				else if (player.isInCategory(CategoryType.THIRD_CLASS_GROUP))
				{
					htmltext = "test_server_helper010.html";
				}
				else if (player.isInCategory(CategoryType.FOURTH_CLASS_GROUP))
				{
					htmltext = "test_server_helper011.html";
				}
				else if (player.isInCategory(CategoryType.AWAKEN_GROUP))
				{
					htmltext = "test_server_helper011a.html";
				}
				break;
			}
			case "secondclass":
			{
				if (player.isInCategory(CategoryType.SECOND_CLASS_GROUP) || player.isInCategory(CategoryType.FIRST_CLASS_GROUP))
				{
					htmltext = player.getLevel() < 40 ? "test_server_helper023.html" : getSecondOccupationChangeHtml(player);
				}
				else if (player.isInCategory(CategoryType.THIRD_CLASS_GROUP))
				{
					htmltext = "test_server_helper010.html";
				}
				else if (player.isInCategory(CategoryType.FOURTH_CLASS_GROUP))
				{
					htmltext = "test_server_helper011.html";
				}
				else if (player.isInCategory(CategoryType.AWAKEN_GROUP))
				{
					htmltext = "test_server_helper011a.html";
				}
				else
				{
					htmltext = "test_server_helper029.html";
				}
				break;
			}
			case "thirdclass":
			{
				if (player.isInCategory(CategoryType.THIRD_CLASS_GROUP) && (player.getLevel() > 75))
				{
					if (changeToNextClass(player))
					{
						player.sendPacket(new PlaySound("ItemSound.quest_fanfare_2"));
						player.broadcastUserInfo();
						htmltext = "test_server_helper021.html";
					}
				}
				else if (player.isInCategory(CategoryType.FOURTH_CLASS_GROUP))
				{
					htmltext = "test_server_helper011.html";
				}
				else if (player.isInCategory(CategoryType.AWAKEN_GROUP))
				{
					htmltext = "test_server_helper011a.html";
				}
				else
				{
					htmltext = "test_server_helper024.html";
				}
				break;
			}
			case "awaken":
			{
				if (player.isInCategory(CategoryType.FOURTH_CLASS_GROUP) && (player.getLevel() > 84))
				{
					if (changeToNextClass(player))
					{
						player.sendPacket(new PlaySound("ItemSound.quest_fanfare_2"));
						player.broadcastUserInfo();
						player.store(false); // Save player cause if server crashes before this char is saved, he will lose class and the money payed for class change.
						htmltext = "test_server_helper021.html";
					}
				}
				else if (player.isInCategory(CategoryType.AWAKEN_GROUP))
				{
					htmltext = "test_server_helper011a.html";
				}
				else
				{
					htmltext = "test_server_helper011b.html";
				}
				break;
			}
			case "setclass":
			{
				if (!st.hasMoreTokens())
				{
					return null;
				}
				
				final int classId = Integer.parseInt(st.nextToken());
				
				boolean canChange = false;
				if ((player.isInCategory(CategoryType.SECOND_CLASS_GROUP) || player.isInCategory(CategoryType.FIRST_CLASS_GROUP)) && (player.getLevel() >= 40)) // In retail you can skip first occupation
				{
					canChange = CategoryData.getInstance().isInCategory(CategoryType.THIRD_CLASS_GROUP, classId) || (player.isInCategory(CategoryType.FIRST_CLASS_GROUP) && CategoryData.getInstance().isInCategory(CategoryType.SECOND_CLASS_GROUP, classId));
				}
				else if (player.isInCategory(CategoryType.FIRST_CLASS_GROUP) && (player.getLevel() >= 20))
				{
					canChange = CategoryData.getInstance().isInCategory(CategoryType.SECOND_CLASS_GROUP, classId);
				}
				else if (player.isInCategory(CategoryType.THIRD_CLASS_GROUP) && (player.getLevel() >= 76))
				{
					canChange = CategoryData.getInstance().isInCategory(CategoryType.FOURTH_CLASS_GROUP, classId);
				}
				else if (player.isInCategory(CategoryType.FOURTH_CLASS_GROUP) && (player.getLevel() >= 85)) // 9
				{
					canChange = CategoryData.getInstance().isInCategory(CategoryType.AWAKEN_GROUP, classId); // 11
				}
				
				if (canChange)
				{
					int classDataIndex = -1;
					if (st.hasMoreTokens())
					{
						classDataIndex = Integer.parseInt(st.nextToken());
					}
					
					if (checkIfClassChangeHasOptions(player))
					{
						if (classDataIndex == -1)
						{
							htmltext = getHtm(player.getHtmlPrefix(), "cc_options.html");
							htmltext = htmltext.replace("%name%", ClassListData.getInstance().getClass(classId).getClassName()); // getEscapedClientCode());
							htmltext = htmltext.replace("%options%", getClassChangeOptions(player, classId));
							return htmltext;
						}
						
						final ClassChangeData data = getClassChangeData(classDataIndex);
						if (data == null)
						{
							return null;
						}
						
						//@formatter:off
						final boolean paid = data.getItemsRequired().stream()
						.filter(ich -> ich.getChance() > Rnd.get(100)) // Chance to pay the price
						.filter(ih -> player.getInventory().getInventoryItemCount(ih.getId(), -1) >= ih.getCount())
						.allMatch(ih -> player.destroyItemByItemId(getClass().getSimpleName(), ih.getId(), ih.getCount(), npc, true));
						//@formatter:on
						
						if (paid)
						{
							//@formatter:off
							data.getItemsRewarded().stream()
							.filter(ich -> ich.getChance() > Rnd.get(100)) // Chance to receive the reward
							.forEach(ih -> player.addItem(getClass().getSimpleName(), ih.getId(), ih.getCount(), npc, true));
							//@formatter:on
						}
						else
						{
							return null; // No class change if payment failed.
						}
					}
					
					player.setClassId(classId);
					if (player.isSubClassActive())
					{
						player.getSubClasses().get(player.getClassIndex()).setClassId(player.getActiveClass());
					}
					else
					{
						player.setBaseClass(player.getActiveClass());
					}
					player.sendPacket(new PlaySound("ItemSound.quest_fanfare_2"));
					player.broadcastUserInfo();
					player.sendPacket(new AcquireSkillList(player));
					player.store(false); // Save player cause if server crashes before this char is saved, he will lose class and the money payed for class change.
					return "test_server_helper021.html";
				}
				break;
			}
			case "clanlevel":
			{
				htmltext = player.isClanLeader() ? "test_server_helper022.html" : "pl014.html";
				break;
			}
			case "learnskills":
			{
				// Retail class master only lets you learn all third class skills.
				if (player.isInCategory(CategoryType.AWAKEN_GROUP))
				{
					htmltext = "test_server_helper001_failed.html";
				}
				else
				{
					player.giveAvailableSkills(true, true);
				}
				break;
			}
			case "clanlevelup":
			{
				if ((player.getClan() == null) || !player.isClanLeader())
				{
					return null;
				}
				
				if (player.getClan().getLevel() >= 10)
				{
					htmltext = "test_server_helper022a.html";
				}
				else
				{
					player.getClan().setLevel(player.getClan().getLevel() + 1);
					player.getClan().broadcastClanStatus();
				}
				break;
			}
		}
		
		return htmltext;
	}
	
	private String getFirstOccupationChangeHtml(L2PcInstance player)
	{
		switch (player.getClassId())
		{
			case FIGHTER:
				return "test_server_helper026a.html";
			case MAGE:
				return "test_server_helper026b.html";
			case ELVEN_FIGHTER:
				return "test_server_helper026c.html";
			case ELVEN_MAGE:
				return "test_server_helper026d.html";
			case DARK_FIGHTER:
				return "test_server_helper026e.html";
			case DARK_MAGE:
				return "test_server_helper026f.html";
			case ORC_FIGHTER:
				return "test_server_helper026g.html";
			case ORC_MAGE:
				return "test_server_helper026h.html";
			case DWARVEN_FIGHTER:
				return "test_server_helper026i.html";
			case MALE_SOLDIER:
				return "test_server_helper026j.html";
			case FEMALE_SOLDIER:
				return "test_server_helper026k.html";
			default:
				return null;
		}
	}
	
	private String getSecondOccupationChangeHtml(L2PcInstance player)
	{
		switch (player.getClassId())
		{
			case FIGHTER:
				return "test_server_helper012.html";
			case WARRIOR:
				return "test_server_helper012a.html";
			case KNIGHT:
				return "test_server_helper012b.html";
			case ROGUE:
				return "test_server_helper012c.html";
			case MAGE:
				return "test_server_helper013.html";
			case WIZARD:
				return "test_server_helper013a.html";
			case CLERIC:
				return "test_server_helper013b.html";
			case ELVEN_FIGHTER:
				return "test_server_helper014.html";
			case ELVEN_KNIGHT:
				return "test_server_helper014a.html";
			case ELVEN_SCOUT:
				return "test_server_helper014b.html";
			case ELVEN_MAGE:
				return "test_server_helper015.html";
			case ELVEN_WIZARD:
				return "test_server_helper015a.html";
			case ORACLE:
				return "test_server_helper015b.html";
			case DARK_FIGHTER:
				return "test_server_helper016.html";
			case PALUS_KNIGHT:
				return "test_server_helper016a.html";
			case ASSASSIN:
				return "test_server_helper016b.html";
			case DARK_MAGE:
				return "test_server_helper017.html";
			case DARK_WIZARD:
				return "test_server_helper017a.html";
			case SHILLIEN_ORACLE:
				return "test_server_helper017b.html";
			case ORC_FIGHTER:
				return "test_server_helper018.html";
			case ORC_RAIDER:
				return "test_server_helper018a.html";
			case ORC_MONK:
				return "test_server_helper018b.html";
			case ORC_MAGE:
			case ORC_SHAMAN:
				return "test_server_helper019.html";
			case DWARVEN_FIGHTER:
				return "test_server_helper020.html";
			case ARTISAN:
				return "test_server_helper020b.html";
			case SCAVENGER:
				return "test_server_helper020a.html";
			case TROOPER:
				return "test_server_helper020c.html";
			case WARDER:
				return "test_server_helper020d.html";
			case ERTHEIA_FIGHTER:
				return "test_server_helper020e.html";
			case ERTHEIA_WIZARD:
				return "test_server_helper020f.html";
			default:
				return null;
		}
	}
	
	private boolean changeToNextClass(L2PcInstance player)
	{
		final ClassId newClass = Arrays.stream(ClassId.values()).filter(cid -> player.getClassId() == cid.getParent()).findAny().orElse(null);
		
		if (newClass == null)
		{
			LOGGER.warning(getClass().getSimpleName() + ": No new classId found for player " + player);
			return false;
		}
		else if (newClass == player.getClassId())
		{
			LOGGER.warning(getClass().getSimpleName() + ": New classId found for player " + player + " is exactly the same as the one he currently is!");
			return false;
		}
		else if (checkIfClassChangeHasOptions(player))
		{
			String html = getHtm(player.getHtmlPrefix(), "cc_options.html");
			html = html.replace("%name%", ClassListData.getInstance().getClass(newClass.getId()).getClassName()); // getEscapedClientCode());
			html = html.replace("%options%", getClassChangeOptions(player, newClass.getId()));
			showResult(player, html);
			return false;
		}
		else
		{
			player.setClassId(newClass.getId());
			if (player.isSubClassActive())
			{
				player.getSubClasses().get(player.getClassIndex()).setClassId(player.getActiveClass());
			}
			else
			{
				player.setBaseClass(player.getActiveClass());
			}
			player.sendPacket(new AcquireSkillList(player));
			return true;
		}
	}
	
	private void showPopupWindow(L2PcInstance player)
	{
		if (!_showPopupWindow)
		{
			return;
		}
		
		//@formatter:off
		if ((player.isInCategory(CategoryType.FIRST_CLASS_GROUP) && (player.getLevel() >= 20)) ||
			((player.isInCategory(CategoryType.SECOND_CLASS_GROUP) || player.isInCategory(CategoryType.FIRST_CLASS_GROUP)) && (player.getLevel() >= 40)) ||
			(player.isInCategory(CategoryType.THIRD_CLASS_GROUP) && (player.getLevel() >= 76)) ||
			(player.isInCategory(CategoryType.FOURTH_CLASS_GROUP) && (player.getLevel() >= 85)))
		//@formatter:on
		{
			player.sendPacket(new TutorialShowQuestionMark(1001));
		}
	}
	
	@RegisterEvent(EventType.ON_PLAYER_PRESS_TUTORIAL_MARK)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void onPlayerPressTutorialMark(OnPlayerPressTutorialMark event)
	{
		final L2PcInstance player = event.getActiveChar();
		
		if (!_showPopupWindow || (event.getMarkId() != 1001))
		{
			return;
		}
		
		String html = null;
		if ((player.isInCategory(CategoryType.SECOND_CLASS_GROUP) || player.isInCategory(CategoryType.FIRST_CLASS_GROUP)) && (player.getLevel() >= 40)) // In retail you can skip first occupation
		{
			html = getHtm(player.getHtmlPrefix(), onAdvEvent("secondclass", null, player));
		}
		else if (player.isInCategory(CategoryType.FIRST_CLASS_GROUP) && (player.getLevel() >= 20))
		{
			html = getHtm(player.getHtmlPrefix(), onAdvEvent("firstclass", null, player));
		}
		else if (player.isInCategory(CategoryType.THIRD_CLASS_GROUP) && (player.getLevel() >= 76))
		{
			html = getHtm(player.getHtmlPrefix(), "qm_thirdclass.html");
		}
		else if (player.isInCategory(CategoryType.FOURTH_CLASS_GROUP) && (player.getLevel() >= 85)) // 9
		{
			html = getHtm(player.getHtmlPrefix(), "qm_awaken.html");
		}
		
		if (html != null)
		{
			showResult(event.getActiveChar(), html);
			// player.sendPacket(new TutorialShowHtml(html));
		}
	}
	
	@RegisterEvent(EventType.ON_PLAYER_BYPASS)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void OnPlayerBypass(OnPlayerBypass event)
	{
		if (event.getCommand().startsWith("Quest ClassMaster "))
		{
			final String html = onAdvEvent(event.getCommand().substring(18), null, event.getActiveChar());
			event.getActiveChar().sendPacket(TutorialCloseHtml.STATIC_PACKET);
			showResult(event.getActiveChar(), html);
		}
	}
	
	@RegisterEvent(EventType.ON_PLAYER_PROFESSION_CHANGE)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void OnPlayerProfessionChange(OnPlayerProfessionChange event)
	{
		showPopupWindow(event.getActiveChar());
	}
	
	@RegisterEvent(EventType.ON_PLAYER_LEVEL_CHANGED)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void OnPlayerLevelChanged(OnPlayerLevelChanged event)
	{
		showPopupWindow(event.getActiveChar());
	}
	
	@RegisterEvent(EventType.ON_PLAYER_LOGIN)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void OnPlayerLogin(OnPlayerLogin event)
	{
		showPopupWindow(event.getActiveChar());
	}
	
	private String getClassChangeOptions(L2PcInstance player, int selectedClassId)
	{
		final StringBuilder sb = new StringBuilder();
		
		for (int i = 0; i < _classChangeData.size(); i++)
		{
			final ClassChangeData option = getClassChangeData(i);
			if ((option == null) || !option.getCategories().stream().anyMatch(ct -> player.isInCategory(ct)))
			{
				continue;
			}
			
			sb.append("<tr><td><img src=L2UI_CT1.ChatBalloon_DF_TopCenter width=276 height=1 /></td></tr>");
			sb.append("<tr><td><table bgcolor=3f3f3f width=100%>");
			sb.append("<tr><td align=center><a action=\"bypass -h Quest ClassMaster setclass " + selectedClassId + " " + i + "\">" + option.getName() + ":</a></td></tr>");
			sb.append("<tr><td><table width=276>");
			sb.append("<tr><td>Requirements:</td></tr>");
			if (option.getItemsRequired().isEmpty())
			{
				sb.append("<tr><td><font color=LEVEL>Free</font></td></tr>");
			}
			else
			{
				option.getItemsRequired().forEach(ih ->
				{
					if (ih.getChance() >= 100)
					{
						sb.append("<tr><td><font color=\"LEVEL\">" + ih.getCount() + "</font></td><td>" + ItemTable.getInstance().getTemplate(ih.getId()).getName() + "</td><td width=30></td></tr>");
					}
					else
					{
						sb.append("<tr><td><font color=\"LEVEL\">" + ih.getCount() + "</font></td><td>" + ItemTable.getInstance().getTemplate(ih.getId()).getName() + "</td><td width=30><font color=LEVEL>" + ih.getChance() + "%</font></td></tr>");
					}
				});
			}
			sb.append("<tr><td>Rewards:</td></tr>");
			if (option.getItemsRewarded().isEmpty())
			{
				if (option.isRewardNoblesse())
				{
					sb.append("<tr><td><font color=\"LEVEL\">Noblesse status.</font></td></tr>");
				}
				
				if (option.isRewardHero())
				{
					sb.append("<tr><td><font color=\"LEVEL\">Hero status.</font></td></tr>");
				}
				
				if (!option.isRewardNoblesse() && !option.isRewardHero())
				{
					sb.append("<tr><td><font color=LEVEL>none</font></td></tr>");
				}
			}
			else
			{
				option.getItemsRewarded().forEach(ih ->
				{
					if (ih.getChance() >= 100)
					{
						sb.append("<tr><td><font color=\"LEVEL\">" + ih.getCount() + "</font></td><td>" + ItemTable.getInstance().getTemplate(ih.getId()).getName() + "</td><td width=30></td></tr>");
					}
					else
					{
						sb.append("<tr><td><font color=\"LEVEL\">" + ih.getCount() + "</font></td><td>" + ItemTable.getInstance().getTemplate(ih.getId()).getName() + "</td><td width=30><font color=LEVEL>" + ih.getChance() + "%</font></td></tr>");
					}
				});
				
				if (option.isRewardNoblesse())
				{
					sb.append("<tr><td><font color=\"LEVEL\">Noblesse status.</font></td></tr>");
				}
				if (option.isRewardHero())
				{
					sb.append("<tr><td><font color=\"LEVEL\">Hero status.</font></td></tr>");
				}
			}
			sb.append("</table></td></tr>");
			sb.append("</table></td></tr>");
			sb.append("<tr><td><img src=L2UI_CT1.ChatBalloon_DF_TopCenter width=276 height=1 /></td></tr>");
		}
		
		return sb.toString();
	}
	
	private static class ClassChangeData
	{
		private final String _name;
		private final List<CategoryType> _appliedCategories;
		private boolean _rewardNoblesse;
		private boolean _rewardHero;
		private List<ItemChanceHolder> _itemsRequired;
		private List<ItemChanceHolder> _itemsRewarded;
		
		public ClassChangeData(String name, List<CategoryType> appliedCategories)
		{
			_name = name;
			_appliedCategories = appliedCategories != null ? appliedCategories : Collections.emptyList();
		}
		
		public String getName()
		{
			return _name;
		}
		
		public List<CategoryType> getCategories()
		{
			return _appliedCategories != null ? _appliedCategories : Collections.emptyList();
		}
		
		public boolean isInCategory(L2PcInstance player)
		{
			if (_appliedCategories != null)
			{
				for (CategoryType category : _appliedCategories)
				{
					if (player.isInCategory(category))
					{
						return true;
					}
				}
			}
			
			return false;
		}
		
		public boolean isRewardNoblesse()
		{
			return _rewardNoblesse;
		}
		
		public void setRewardNoblesse(boolean rewardNoblesse)
		{
			_rewardNoblesse = rewardNoblesse;
		}
		
		public boolean isRewardHero()
		{
			return _rewardHero;
		}
		
		public void setRewardHero(boolean rewardHero)
		{
			_rewardHero = rewardHero;
		}
		
		void setItemsRequired(List<ItemChanceHolder> itemsRequired)
		{
			_itemsRequired = itemsRequired;
		}
		
		public List<ItemChanceHolder> getItemsRequired()
		{
			return _itemsRequired != null ? _itemsRequired : Collections.emptyList();
		}
		
		void setItemsRewarded(List<ItemChanceHolder> itemsRewarded)
		{
			_itemsRewarded = itemsRewarded;
		}
		
		public List<ItemChanceHolder> getItemsRewarded()
		{
			return _itemsRewarded != null ? _itemsRewarded : Collections.emptyList();
		}
	}
	
	private boolean checkIfClassChangeHasOptions(L2PcInstance player)
	{
		boolean showOptions = _classChangeData.stream().filter(ccd -> !ccd.getItemsRequired().isEmpty()).anyMatch(ccd -> ccd.isInCategory(player)); // Check if there are requirements
		if (!showOptions)
		{
			showOptions = _classChangeData.stream().filter(ccd -> !ccd.getItemsRewarded().isEmpty()).filter(ccd -> ccd.isInCategory(player)).count() > 1; // Check if there is more than 1 reward to chose.
		}
		
		return showOptions;
	}
	
	private ClassChangeData getClassChangeData(int index)
	{
		if ((index >= 0) && (index < _classChangeData.size()))
		{
			return _classChangeData.get(index);
		}
		
		return null;
	}
	
	public static void main(String[] args)
	{
		new ClassMaster();
	}
}