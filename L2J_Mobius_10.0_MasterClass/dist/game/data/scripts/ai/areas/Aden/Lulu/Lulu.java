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
package ai.areas.Aden.Lulu;

import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.cache.HtmCache;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.events.EventType;
import org.l2jmobius.gameserver.model.events.ListenerRegisterType;
import org.l2jmobius.gameserver.model.events.annotations.RegisterEvent;
import org.l2jmobius.gameserver.model.events.annotations.RegisterType;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerDlgAnswer;
import org.l2jmobius.gameserver.model.holders.SkillHolder;
import org.l2jmobius.gameserver.model.skill.SkillCaster;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ConfirmDlg;
import org.l2jmobius.gameserver.network.serverpackets.ExPremiumManagerShowHtml;

import ai.AbstractNpcAI;

/**
 * @author Index
 */
public class Lulu extends AbstractNpcAI
{
	// NPCs
	private static final int LULU = 34545;
	private static final int WAEL = 34546;
	private static final int EILEEN = 34547;
	// Skills
	private static final SkillHolder LULU_LUCK_LV1 = new SkillHolder(32966, 1);
	private static final SkillHolder LULU_LUCK_LV2 = new SkillHolder(32966, 2);
	// Misc
	private static final ConfirmDlg CONFIRM_DIALOG = new ConfirmDlg(SystemMessageId.I_CAN_GIVE_YOU_A_GOOD_LUCK_BUFF_WILL_YOU_ACCEPT_IT_IT_WILL_COST_YOU_7_000_000_ADENA);
	private static final int ADENA_COST = 7000000;
	private static final int BUFF_CHANCE = 30;
	private static Npc _luluNpc;
	
	private Lulu()
	{
		addStartNpc(LULU);
		addFirstTalkId(LULU, WAEL, EILEEN);
		addTalkId(LULU, WAEL, EILEEN);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		if (event.equals("LULU_BLESSING") && (npc != null) && (npc.getId() == LULU))
		{
			if ((_luluNpc != null) && _luluNpc.isDead())
			{
				_luluNpc = null;
			}
			if ((_luluNpc == null) && !npc.isDead())
			{
				_luluNpc = npc;
			}
			player.sendPacket(CONFIRM_DIALOG);
		}
		return null;
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		final int npcId = npc.getId();
		if (npcId == LULU)
		{
			player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/areas/Aden/Lulu/34545.html")));
			return null;
		}
		return npcId + ".html";
	}
	
	@RegisterEvent(EventType.ON_PLAYER_DLG_ANSWER)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void onPlayerDlgAnswer(OnPlayerDlgAnswer event)
	{
		final Player player = event.getPlayer();
		if (player == null)
		{
			return;
		}
		
		if (event.getMessageId() != SystemMessageId.I_CAN_GIVE_YOU_A_GOOD_LUCK_BUFF_WILL_YOU_ACCEPT_IT_IT_WILL_COST_YOU_7_000_000_ADENA.getId())
		{
			return;
		}
		
		if (event.getAnswer() != 1)
		{
			return;
		}
		
		if (_luluNpc == null)
		{
			return;
		}
		
		if (!World.getInstance().getVisibleObjects(player, Npc.class).contains(_luluNpc))
		{
			return;
		}
		
		if (player.calculateDistance3D(_luluNpc) > Npc.INTERACTION_DISTANCE)
		{
			player.sendPacket(SystemMessageId.YOU_ARE_TOO_FAR_FROM_THE_NPC_FOR_THAT_TO_WORK);
			return;
		}
		
		if (player.reduceAdena("Lulu", ADENA_COST, _luluNpc, true))
		{
			SkillCaster.triggerCast(_luluNpc, player, Rnd.get(100) < BUFF_CHANCE ? LULU_LUCK_LV2.getSkill() : LULU_LUCK_LV1.getSkill());
		}
	}
	
	public static void main(String[] args)
	{
		new Lulu();
	}
}