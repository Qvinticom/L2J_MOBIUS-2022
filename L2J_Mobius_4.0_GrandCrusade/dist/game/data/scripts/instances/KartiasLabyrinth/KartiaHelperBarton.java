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
package instances.KartiasLabyrinth;

import com.l2jmobius.commons.util.CommonUtil;
import com.l2jmobius.gameserver.enums.ChatType;
import com.l2jmobius.gameserver.model.Location;
import com.l2jmobius.gameserver.model.StatsSet;
import com.l2jmobius.gameserver.model.actor.L2Character;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.FriendlyNpcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.events.impl.character.OnCreatureAttacked;
import com.l2jmobius.gameserver.model.events.impl.character.OnCreatureDeath;
import com.l2jmobius.gameserver.model.events.impl.instance.OnInstanceStatusChange;
import com.l2jmobius.gameserver.model.holders.SkillHolder;
import com.l2jmobius.gameserver.model.instancezone.Instance;
import com.l2jmobius.gameserver.model.skills.SkillCaster;
import com.l2jmobius.gameserver.network.NpcStringId;

import ai.AbstractNpcAI;

/**
 * Kartia Helper Barton AI.
 * @author St3eT
 */
public final class KartiaHelperBarton extends AbstractNpcAI
{
	// NPCs
	private static final int[] KARTIA_BARTON =
	{
		33611, // Barton (Kartia 85)
		33622, // Barton (Kartia 90)
		33633, // Barton (Kartia 95)
	};
	private static final int[] KARTIA_ADOLPH =
	{
		33609, // Adolph (Kartia 85)
		33620, // Adolph (Kartia 90)
		33631, // Adolph (Kartia 95)
	};
	// Misc
	private static final int[] KARTIA_SOLO_INSTANCES =
	{
		205, // Solo 85
		206, // Solo 90
		207, // Solo 95
	};
	
	private KartiaHelperBarton()
	{
		setCreatureKillId(this::onCreatureKill, KARTIA_BARTON);
		setCreatureAttackedId(this::onCreatureAttacked, KARTIA_BARTON);
		addSeeCreatureId(KARTIA_BARTON);
		setInstanceStatusChangeId(this::onInstanceStatusChange, KARTIA_SOLO_INSTANCES);
	}
	
	@Override
	public void onTimerEvent(String event, StatsSet params, L2Npc npc, L2PcInstance player)
	{
		final Instance instance = npc.getInstanceWorld();
		if ((instance != null) && event.equals("CHECK_ACTION"))
		{
			final FriendlyNpcInstance adolph = npc.getVariables().getObject("ADOLPH_OBJECT", FriendlyNpcInstance.class);
			if (adolph != null)
			{
				final double distance = npc.calculateDistance(adolph, false, false);
				if (distance > 200)
				{
					final Location loc = new Location(adolph.getX() + getRandom(-100, 100), adolph.getY() + getRandom(-100, 100), adolph.getZ() + 50);
					if (distance > 500)
					{
						npc.teleToLocation(loc);
					}
					else
					{
						npc.setRunning();
						addMoveToDesire(npc, loc, 23);
					}
				}
				else if (!npc.isInCombat() || !npc.isAttackingNow() || (npc.getTarget() == null))
				{
					final L2Character monster = (L2Character) adolph.getTarget();
					if ((monster != null) && adolph.isInCombat())
					{
						addAttackDesire(npc, monster);
					}
				}
			}
		}
	}
	
	public void onInstanceStatusChange(OnInstanceStatusChange event)
	{
		final Instance instance = event.getWorld();
		final int status = event.getStatus();
		switch (status)
		{
			case 1:
			{
				instance.getAliveNpcs(KARTIA_BARTON).forEach(barton -> getTimers().addRepeatingTimer("CHECK_ACTION", 3000, barton, null));
				break;
			}
			case 2:
			case 3:
			{
				final Location loc = instance.getTemplateParameters().getLocation("bartonTeleportStatus" + status);
				if (loc != null)
				{
					instance.getAliveNpcs(KARTIA_BARTON).forEach(barton -> barton.teleToLocation(loc));
				}
				break;
			}
		}
	}
	
	@Override
	public String onSeeCreature(L2Npc npc, L2Character creature, boolean isSummon)
	{
		if (creature.isPlayer())
		{
			npc.getVariables().set("PLAYER_OBJECT", creature.getActingPlayer());
		}
		else if (CommonUtil.contains(KARTIA_ADOLPH, creature.getId()))
		{
			npc.getVariables().set("ADOLPH_OBJECT", creature);
		}
		return super.onSeeCreature(npc, creature, isSummon);
	}
	
	public void onCreatureAttacked(OnCreatureAttacked event)
	{
		final L2Npc npc = (L2Npc) event.getTarget();
		final Instance instance = npc.getInstanceWorld();
		if ((instance != null) && !event.getAttacker().isPlayable())
		{
			final StatsSet instParams = instance.getTemplateParameters();
			final int random = getRandom(1000);
			
			if (random < 333)
			{
				final SkillHolder infinitySkill = instParams.getSkillHolder("bartonInfinity");
				if ((infinitySkill != null) && SkillCaster.checkUseConditions(npc, infinitySkill.getSkill()))
				{
					npc.broadcastSay(ChatType.NPC_GENERAL, NpcStringId.DIE3);
					addSkillCastDesire(npc, npc.getTarget(), infinitySkill, 23);
				}
			}
			else if ((npc.getCurrentHpPercent() < 50) && npc.isScriptValue(0))
			{
				final SkillHolder berserkerSkill = instParams.getSkillHolder("bartonBerserker");
				if ((berserkerSkill != null) && !npc.isAffectedBySkill(berserkerSkill.getSkillId()) && SkillCaster.checkUseConditions(npc, berserkerSkill.getSkill()))
				{
					npc.broadcastSay(ChatType.NPC_GENERAL, NpcStringId.WAAAAAAAAHHHHHH);
					addSkillCastDesire(npc, npc.getTarget(), berserkerSkill, 23);
					getTimers().addTimer("RESTORE_SCRIPTVAL", 10000, n -> npc.setScriptValue(0));
				}
			}
		}
	}
	
	public void onCreatureKill(OnCreatureDeath event)
	{
		final L2Npc npc = (L2Npc) event.getTarget();
		final Instance world = npc.getInstanceWorld();
		if (world != null)
		{
			getTimers().cancelTimersOf(npc);
		}
	}
	
	public static void main(String[] args)
	{
		new KartiaHelperBarton();
	}
}