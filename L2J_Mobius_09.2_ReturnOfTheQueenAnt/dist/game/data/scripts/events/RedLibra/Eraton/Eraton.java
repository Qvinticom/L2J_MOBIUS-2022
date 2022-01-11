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
package events.RedLibra.Eraton;

import java.util.List;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.data.xml.ClassListData;
import org.l2jmobius.gameserver.data.xml.SkillData;
import org.l2jmobius.gameserver.data.xml.SkillTreeData;
import org.l2jmobius.gameserver.enums.ClassId;
import org.l2jmobius.gameserver.enums.Race;
import org.l2jmobius.gameserver.enums.SubclassInfoType;
import org.l2jmobius.gameserver.model.SkillLearn;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.events.EventType;
import org.l2jmobius.gameserver.model.events.ListenerRegisterType;
import org.l2jmobius.gameserver.model.events.annotations.Id;
import org.l2jmobius.gameserver.model.events.annotations.RegisterEvent;
import org.l2jmobius.gameserver.model.events.annotations.RegisterType;
import org.l2jmobius.gameserver.model.events.impl.creature.npc.OnNpcMenuSelect;
import org.l2jmobius.gameserver.model.holders.ItemHolder;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ExSubjobInfo;
import org.l2jmobius.gameserver.network.serverpackets.ExUserInfoInvenWeight;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jmobius.gameserver.network.serverpackets.SocialAction;

import ai.AbstractNpcAI;

/**
 * Red Libra<br>
 * Step 1.<br>
 * Contact Red to move to Eraton in the Aden Temple where the class change is taking place. (Must have the Stone of Destiny and the Main Class cloak.)<br>
 * Step 2.<br>
 * Contact Eraton in the Aden Temple to learn the details of the class change. Once everything is ready, select the desired class and confirm your choice.<br>
 * Step 3.<br>
 * Congratulations! The main character class has been changed. The process is accompanied by a distinctive animation with a character jumping up.
 * @author Index, Gaikotsu
 */
public class Eraton extends AbstractNpcAI
{
	// NPC
	private static final int ERATON = 34584;
	// Items
	private static final ItemHolder STONE_OF_DESTINY = new ItemHolder(17722, 1);
	private static final ItemHolder CHAOS_POMANDER = new ItemHolder(37374, 2);
	private static final ItemHolder CLASS_CLOAK_SIGEL = new ItemHolder(30310, 1);
	private static final ItemHolder CLASS_CLOAK_TYRR = new ItemHolder(30311, 1);
	private static final ItemHolder CLASS_CLOAK_OTHELL = new ItemHolder(30312, 1);
	private static final ItemHolder CLASS_CLOAK_YUL = new ItemHolder(30313, 1);
	private static final ItemHolder CLASS_CLOAK_FEOH = new ItemHolder(30314, 1);
	private static final ItemHolder CLASS_CLOAK_WYNN = new ItemHolder(30315, 1);
	private static final ItemHolder CLASS_CLOAK_ISS = new ItemHolder(30316, 1);
	private static final ItemHolder CLASS_CLOAK_AEORE = new ItemHolder(30317, 1);
	private static final ItemHolder CLASS_CLOAK_EVISCERATOR = new ItemHolder(40200, 1);
	private static final ItemHolder CLASS_CLOAK_SAYHA_SEER = new ItemHolder(40201, 1);
	// Misc
	private static final String ITEM_NAME_PATTERN = "&#" + STONE_OF_DESTINY.getId() + ";";
	
	private Eraton()
	{
		addStartNpc(ERATON);
		addFirstTalkId(ERATON);
		addTalkId(ERATON);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = null;
		switch (event)
		{
			case ("back"):
			{
				htmltext = getHtm(player, "34584.html").replace("%required_item%", ITEM_NAME_PATTERN);
				break;
			}
			case ("ERATON_HELP"):
			{
				// TODO: NEED TO BE FOUND!!
				htmltext = getHtm(player, "34584-9.html").replace("%required_item%", ITEM_NAME_PATTERN).replace("%required_item_count%", String.valueOf(STONE_OF_DESTINY.getCount()));
				break;
			}
			case ("ERATON_LIST"):
			{
				final StringBuilder sb = new StringBuilder();
				sb.append("<button align=\"LEFT\" icon=\"NORMAL\" action=\"bypass -h Quest Eraton ERATON_" + "SIGEL" + "\">Sigel Knight</button>");
				sb.append("<button align=\"LEFT\" icon=\"NORMAL\" action=\"bypass -h Quest Eraton ERATON_" + "TYRR" + "\">Tyrr Warrior</button>");
				sb.append("<button align=\"LEFT\" icon=\"NORMAL\" action=\"bypass -h Quest Eraton ERATON_" + "OTHELL" + "\">Othell Rogue</button>");
				sb.append("<button align=\"LEFT\" icon=\"NORMAL\" action=\"bypass -h Quest Eraton ERATON_" + "YUL" + "\">Yul Archer</button>");
				sb.append("<button align=\"LEFT\" icon=\"NORMAL\" action=\"bypass -h Quest Eraton ERATON_" + "FEOH" + "\">Feoh Wizard</button>");
				sb.append("<button align=\"LEFT\" icon=\"NORMAL\" action=\"bypass -h Quest Eraton ERATON_" + "ISS" + "\">Iss Enchanter</button>");
				sb.append("<button align=\"LEFT\" icon=\"NORMAL\" action=\"bypass -h Quest Eraton ERATON_" + "WYNN" + "\">Wynn Summoner</button>");
				sb.append("<button align=\"LEFT\" icon=\"NORMAL\" action=\"bypass -h Quest Eraton ERATON_" + "AEORE" + "\">Aeore Healer</button>");
				htmltext = getHtm(player, "34584-1.html").replace("%CLASS_NAMES%", sb.toString());
				break;
			}
			case ("ERATON_SIGEL"):
			{
				final StringBuilder sb = new StringBuilder();
				sb.append("<button align=\"LEFT\" icon=\"NORMAL\" action=\"bypass -h Quest Eraton ERATON_" + ClassId.SIGEL_PHOENIX_KNIGHT + "\">Sigel Phoenix Knight</button>");
				sb.append("<button align=\"LEFT\" icon=\"NORMAL\" action=\"bypass -h Quest Eraton ERATON_" + ClassId.SIGEL_HELL_KNIGHT + "\">Sigel Hell Knight</button>");
				sb.append("<button align=\"LEFT\" icon=\"NORMAL\" action=\"bypass -h Quest Eraton ERATON_" + ClassId.SIGEL_EVA_TEMPLAR + "\">Sigel Eva Templar</button>");
				sb.append("<button align=\"LEFT\" icon=\"NORMAL\" action=\"bypass -h Quest Eraton ERATON_" + ClassId.SIGEL_SHILLIEN_TEMPLAR + "\">Sigel Shillie Templar</button>");
				// Death Knights cannot use the Stone of Destiny.
				// sb.append("<button align=\"LEFT\" icon=\"NORMAL\" action=\"bypass -h Quest Eraton ERATON_" + ClassId.SIGEL_DEATH_KNIGHT + "\">Sigel Death Knight</button>");
				htmltext = getHtm(player, "34584-2.html").replace("%CLASS_LIST%", sb.toString());
				break;
			}
			case ("ERATON_TYRR"):
			{
				final StringBuilder sb = new StringBuilder();
				sb.append("<button align=\"LEFT\" icon=\"NORMAL\" action=\"bypass -h Quest Eraton ERATON_" + ClassId.TYRR_DUELIST + "\">Tyrr Duelist</button>");
				sb.append("<button align=\"LEFT\" icon=\"NORMAL\" action=\"bypass -h Quest Eraton ERATON_" + ClassId.TYRR_DREADNOUGHT + "\">Tyrr Drearnought</button>");
				sb.append("<button align=\"LEFT\" icon=\"NORMAL\" action=\"bypass -h Quest Eraton ERATON_" + ClassId.TYRR_TITAN + "\">Tyrr Titan</button>");
				sb.append("<button align=\"LEFT\" icon=\"NORMAL\" action=\"bypass -h Quest Eraton ERATON_" + ClassId.TYRR_GRAND_KHAVATARI + "\">Tyrr Grand Khavatari</button>");
				if (player.getRace() == Race.DWARF)
				{
					sb.append("<button align=\"LEFT\" icon=\"NORMAL\" action=\"bypass -h Quest Eraton ERATON_" + ClassId.TYRR_MAESTRO + "\">Tyrr Maestro</button>");
				}
				if ((player.getRace() == Race.KAMAEL) && !player.getAppearance().isFemale())
				{
					sb.append("<button align=\"LEFT\" icon=\"NORMAL\" action=\"bypass -h Quest Eraton ERATON_" + ClassId.TYRR_DOOMBRINGER + "\">Tyrr Doombringer</button>");
				}
				if (player.getRace() == Race.ERTHEIA)
				{
					sb.append("<button align=\"LEFT\" icon=\"NORMAL\" action=\"bypass -h Quest Eraton ERATON_" + ClassId.EVISCERATOR + "\">Eviscerator</button>");
				}
				htmltext = getHtm(player, "34584-2.html").replace("%CLASS_LIST%", sb.toString());
				break;
			}
			case ("ERATON_OTHELL"):
			{
				final StringBuilder sb = new StringBuilder();
				sb.append("<button align=\"LEFT\" icon=\"NORMAL\" action=\"bypass -h Quest Eraton ERATON_" + ClassId.OTHELL_ADVENTURER + "\">OTHELL_ADVENTURER</button>");
				sb.append("<button align=\"LEFT\" icon=\"NORMAL\" action=\"bypass -h Quest Eraton ERATON_" + ClassId.OTHELL_WIND_RIDER + "\">OTHELL_WIND_RIDER</button>");
				sb.append("<button align=\"LEFT\" icon=\"NORMAL\" action=\"bypass -h Quest Eraton ERATON_" + ClassId.OTHELL_GHOST_HUNTER + "\">OTHELL_GHOST_HUNTER</button>");
				if (player.getRace() == Race.DWARF)
				{
					sb.append("<button align=\"LEFT\" icon=\"NORMAL\" action=\"bypass -h Quest Eraton ERATON_" + ClassId.OTHELL_FORTUNE_SEEKER + "\">OTHELL_FORTUNE_SEEKER</button>");
				}
				htmltext = getHtm(player, "34584-2.html").replace("%CLASS_LIST%", sb.toString());
				break;
			}
			case ("ERATON_YUL"):
			{
				final StringBuilder sb = new StringBuilder();
				sb.append("<button align=\"LEFT\" icon=\"NORMAL\" action=\"bypass -h Quest Eraton ERATON_" + ClassId.YUL_SAGITTARIUS + "\">YUL_SAGITTARIUS</button>");
				sb.append("<button align=\"LEFT\" icon=\"NORMAL\" action=\"bypass -h Quest Eraton ERATON_" + ClassId.YUL_MOONLIGHT_SENTINEL + "\">YUL_MOONLIGHT_SENTINEL</button>");
				sb.append("<button align=\"LEFT\" icon=\"NORMAL\" action=\"bypass -h Quest Eraton ERATON_" + ClassId.YUL_GHOST_SENTINEL + "\">YUL_GHOST_SENTINEL</button>");
				if ((player.getRace() == Race.KAMAEL) && player.getAppearance().isFemale())
				{
					sb.append("<button align=\"LEFT\" icon=\"NORMAL\" action=\"bypass -h Quest Eraton ERATON_" + ClassId.YUL_TRICKSTER + "\">YUL_TRICKSTER</button>");
				}
				htmltext = getHtm(player, "34584-2.html").replace("%CLASS_LIST%", sb.toString());
				break;
			}
			case ("ERATON_FEOH"):
			{
				final StringBuilder sb = new StringBuilder();
				sb.append("<button align=\"LEFT\" icon=\"NORMAL\" action=\"bypass -h Quest Eraton ERATON_" + ClassId.FEOH_ARCHMAGE + "\">FEOH_ARCHMAGE</button>");
				sb.append("<button align=\"LEFT\" icon=\"NORMAL\" action=\"bypass -h Quest Eraton ERATON_" + ClassId.FEOH_SOULTAKER + "\">FEOH_SOULTAKER</button>");
				sb.append("<button align=\"LEFT\" icon=\"NORMAL\" action=\"bypass -h Quest Eraton ERATON_" + ClassId.FEOH_MYSTIC_MUSE + "\">FEOH_MYSTIC_MUSE</button>");
				sb.append("<button align=\"LEFT\" icon=\"NORMAL\" action=\"bypass -h Quest Eraton ERATON_" + ClassId.FEOH_STORM_SCREAMER + "\">FEOH_STORM_SCREAMER</button>");
				sb.append("<button align=\"LEFT\" icon=\"NORMAL\" action=\"bypass -h Quest Eraton ERATON_" + ClassId.FEOH_SOUL_HOUND + "\">FEOH_SOUL_HOUND</button>");
				if (player.getRace() == Race.ERTHEIA)
				{
					sb.append("<button align=\"LEFT\" icon=\"NORMAL\" action=\"bypass -h Quest Eraton ERATON_" + ClassId.SAYHA_SEER + "\">SAYHA_SEER</button>");
				}
				htmltext = getHtm(player, "34584-2.html").replace("%CLASS_LIST%", sb.toString());
				break;
			}
			case ("ERATON_ISS"):
			{
				final StringBuilder sb = new StringBuilder();
				sb.append("<button align=\"LEFT\" icon=\"NORMAL\" action=\"bypass -h Quest Eraton ERATON_" + ClassId.ISS_HIEROPHANT + "\">ISS_HIEROPHANT</button>");
				sb.append("<button align=\"LEFT\" icon=\"NORMAL\" action=\"bypass -h Quest Eraton ERATON_" + ClassId.ISS_SWORD_MUSE + "\">ISS_SWORD_MUSE</button>");
				sb.append("<button align=\"LEFT\" icon=\"NORMAL\" action=\"bypass -h Quest Eraton ERATON_" + ClassId.ISS_SPECTRAL_DANCER + "\">ISS_SPECTRAL_DANCER</button>");
				sb.append("<button align=\"LEFT\" icon=\"NORMAL\" action=\"bypass -h Quest Eraton ERATON_" + ClassId.ISS_DOOMCRYER + "\">ISS_DOOMCRYER</button>");
				if (player.getRace() == Race.ORC)
				{
					sb.append("<button align=\"LEFT\" icon=\"NORMAL\" action=\"bypass -h Quest Eraton ERATON_" + ClassId.ISS_DOMINATOR + "\">ISS_DOMINATOR</button>");
				}
				htmltext = getHtm(player, "34584-2.html").replace("%CLASS_LIST%", sb.toString());
				break;
			}
			case ("ERATON_WYNN"):
			{
				final StringBuilder sb = new StringBuilder();
				sb.append("<button align=\"LEFT\" icon=\"NORMAL\" action=\"bypass -h Quest Eraton ERATON_" + ClassId.WYNN_ARCANA_LORD + "\">WYNN_ARCANA_LORD</button>");
				sb.append("<button align=\"LEFT\" icon=\"NORMAL\" action=\"bypass -h Quest Eraton ERATON_" + ClassId.WYNN_ELEMENTAL_MASTER + "\">WYNN_ELEMENTAL_MASTER</button>");
				sb.append("<button align=\"LEFT\" icon=\"NORMAL\" action=\"bypass -h Quest Eraton ERATON_" + ClassId.WYNN_SPECTRAL_MASTER + "\">WYNN_SPECTRAL_MASTER</button>");
				htmltext = getHtm(player, "34584-2.html").replace("%CLASS_LIST%", sb.toString());
				break;
			}
			case ("ERATON_AEORE"):
			{
				final StringBuilder sb = new StringBuilder();
				sb.append("<button align=\"LEFT\" icon=\"NORMAL\" action=\"bypass -h Quest Eraton ERATON_" + ClassId.AEORE_CARDINAL + "\">AEORE_CARDINAL</button>");
				sb.append("<button align=\"LEFT\" icon=\"NORMAL\" action=\"bypass -h Quest Eraton ERATON_" + ClassId.AEORE_EVA_SAINT + "\">AEORE_EVA_SAINT</button>");
				sb.append("<button align=\"LEFT\" icon=\"NORMAL\" action=\"bypass -h Quest Eraton ERATON_" + ClassId.AEORE_SHILLIEN_SAINT + "\">AEORE_SHILLIEN_SAINT</button>");
				htmltext = getHtm(player, "34584-2.html").replace("%CLASS_LIST%", sb.toString());
				break;
			}
			default:
			{
				final ClassId classId = ClassId.valueOf(event.replace("ERATON_", ""));
				if (classId != null)
				{
					final StringBuilder sb = new StringBuilder();
					sb.append("<Button ALIGN=LEFT ICON=NORMAL action=\"bypass -h menu_select?ask=1&reply=" + classId.getId() + "\">" + "Select " + ClassListData.getInstance().getClass(classId.getId()).getClassName() + "</Button>");
					htmltext = getHtm(player, "34584-3.html").replace("%CONFIRM_BUTTON%", sb.toString());
				}
			}
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		String htmltext;
		htmltext = getHtm(player, "34584.html").replace("%required_item%", ITEM_NAME_PATTERN);
		return htmltext;
	}
	
	@RegisterEvent(EventType.ON_NPC_MENU_SELECT)
	@RegisterType(ListenerRegisterType.NPC)
	@Id(ERATON)
	public void onNpcMenuSelect(OnNpcMenuSelect event)
	{
		final Player player = event.getTalker();
		final int ask = event.getAsk();
		switch (ask)
		{
			case 1:
			{
				final int classId = event.getReply();
				if (!player.isAwakenedClass())
				{
					player.sendPacket(new NpcHtmlMessage(getHtm(player, "34584-7.html")));
					return;
				}
				if (!hasQuestItems(player, STONE_OF_DESTINY.getId()) || !hasAtLeastOneQuestItem(player, CLASS_CLOAK_SIGEL.getId(), CLASS_CLOAK_TYRR.getId(), CLASS_CLOAK_OTHELL.getId(), CLASS_CLOAK_YUL.getId(), CLASS_CLOAK_FEOH.getId(), CLASS_CLOAK_WYNN.getId(), CLASS_CLOAK_ISS.getId(), CLASS_CLOAK_AEORE.getId(), CLASS_CLOAK_EVISCERATOR.getId(), CLASS_CLOAK_SAYHA_SEER.getId()))
				{
					player.sendPacket(new NpcHtmlMessage(getHtm(player, "34584-4.html").replace("%required_item%", ITEM_NAME_PATTERN).replace("%required_item_count%", String.valueOf(STONE_OF_DESTINY.getCount()))));
					return;
				}
				if ((player.getDualClass() != null) && (player.getDualClass().getClassId() == classId))
				{
					player.sendPacket(new NpcHtmlMessage(getHtm(player, "34584-6.html").replace("%s1%", "Main").replace("%s2%", "Dual")));
					return;
				}
				if ((player.getClass() != null) && (player.getBaseClass() == classId))
				{
					player.sendPacket(new NpcHtmlMessage(getHtm(player, "34584-6.html").replace("%s1%", "Main").replace("%s2%", "Current")));
					return;
				}
				if (player.isTransformed() || player.hasSummon() || player.isDualClassActive())
				{
					player.sendPacket(new NpcHtmlMessage(getHtm(player, "34584-5.html")));
					return;
				}
				if (player.isHero() || player.isTrueHero())
				{
					player.sendPacket(SystemMessageId.YOU_CANNOT_AWAKEN_WHEN_YOU_ARE_A_HERO_OR_ON_THE_WAIT_LIST_FOR_HERO_STATUS);
					return;
				}
				// TODO: SET 1000 points for Olympiad after change main class.
				if (player.getOriginalClass() == null)
				{
					player.setOriginalClass(player.getClassId());
				}
				takeItem(player, STONE_OF_DESTINY);
				takeItem(player, getCloakItemId(player));
				player.abortCast();
				player.stopAllEffectsExceptThoseThatLastThroughDeath();
				player.stopAllEffects();
				player.stopCubics();
				player.setClassId(classId);
				player.setBaseClass(player.getActiveClass());
				SkillTreeData.getInstance().cleanSkillUponChangeClass(player);
				for (SkillLearn skill : SkillTreeData.getInstance().getRaceSkillTree(player.getRace()))
				{
					player.addSkill(SkillData.getInstance().getSkill(skill.getSkillId(), skill.getSkillLevel()), true);
				}
				final List<Integer> removedSkillIds = Config.HARDIN_REMOVED_SKILLS.get(classId);
				if (removedSkillIds != null)
				{
					for (int skillId : removedSkillIds)
					{
						final Skill skill = player.getKnownSkill(skillId);
						if (skill != null)
						{
							player.removeSkill(skill);
						}
					}
				}
				if (player.getWarehouse().getItemByItemId(CHAOS_POMANDER.getId()) != null)
				{
					final long warehouseCount = (player.getWarehouse().getItemByItemId(CHAOS_POMANDER.getId())).getCount();
					if (warehouseCount > 0)
					{
						player.getWarehouse().destroyItemByItemId("Eraton", CHAOS_POMANDER.getId(), warehouseCount, player, null);
					}
				}
				if (hasAtLeastOneQuestItem(player, CHAOS_POMANDER.getId()))
				{
					takeItems(player, CHAOS_POMANDER.getId(), -1);
				}
				player.restoreDualSkills();
				player.store(false);
				player.broadcastUserInfo();
				player.sendSkillList();
				player.sendPacket(new ExSubjobInfo(player, SubclassInfoType.CLASS_CHANGED));
				player.sendPacket(new ExUserInfoInvenWeight(player));
				giveItems(player, getCloakItemId(player));
				giveItems(player, CHAOS_POMANDER);
				player.sendPacket(new SocialAction(player.getObjectId(), 20));
				break;
			}
		}
	}
	
	private ItemHolder getCloakItemId(Player player)
	{
		switch (player.getClassId())
		{
			case SIGEL_PHOENIX_KNIGHT:
			case SIGEL_HELL_KNIGHT:
			case SIGEL_EVA_TEMPLAR:
			case SIGEL_SHILLIEN_TEMPLAR:
			{
				return CLASS_CLOAK_SIGEL;
			}
			case TYRR_DUELIST:
			case TYRR_DREADNOUGHT:
			case TYRR_TITAN:
			case TYRR_GRAND_KHAVATARI:
			case TYRR_MAESTRO:
			case TYRR_DOOMBRINGER:
			{
				return CLASS_CLOAK_TYRR;
			}
			case OTHELL_ADVENTURER:
			case OTHELL_WIND_RIDER:
			case OTHELL_GHOST_HUNTER:
			case OTHELL_FORTUNE_SEEKER:
			{
				return CLASS_CLOAK_OTHELL;
			}
			case YUL_SAGITTARIUS:
			case YUL_MOONLIGHT_SENTINEL:
			case YUL_GHOST_SENTINEL:
			case YUL_TRICKSTER:
			{
				return CLASS_CLOAK_YUL;
			}
			case FEOH_ARCHMAGE:
			case FEOH_SOULTAKER:
			case FEOH_MYSTIC_MUSE:
			case FEOH_STORM_SCREAMER:
			case FEOH_SOUL_HOUND:
			{
				return CLASS_CLOAK_FEOH;
			}
			case ISS_HIEROPHANT:
			case ISS_SWORD_MUSE:
			case ISS_SPECTRAL_DANCER:
			case ISS_DOOMCRYER:
			case ISS_DOMINATOR:
			{
				return CLASS_CLOAK_ISS;
			}
			case WYNN_ARCANA_LORD:
			case WYNN_ELEMENTAL_MASTER:
			case WYNN_SPECTRAL_MASTER:
			{
				return CLASS_CLOAK_WYNN;
			}
			case AEORE_CARDINAL:
			case AEORE_EVA_SAINT:
			case AEORE_SHILLIEN_SAINT:
			{
				return CLASS_CLOAK_WYNN;
			}
			case EVISCERATOR:
			{
				return CLASS_CLOAK_EVISCERATOR;
			}
			case SAYHA_SEER:
			{
				return CLASS_CLOAK_SAYHA_SEER;
			}
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new Eraton();
	}
}