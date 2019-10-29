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
package instances.ResidenceOfKingProcella;

import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.instance.MonsterInstance;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.actor.instance.RaidBossInstance;
import org.l2jmobius.gameserver.model.holders.SkillHolder;
import org.l2jmobius.gameserver.model.instancezone.Instance;
import org.l2jmobius.gameserver.model.skills.SkillCaster;

import instances.AbstractInstance;

/**
 * @author RobikBobik
 * @NOTE: Retail like working
 * @TODO: Rewrite code to modern style.
 */
public class ResidenceOfKingProcella extends AbstractInstance
{
	// NPCs
	private static final int WIRI = 34048;
	private static final int PROCELLA = 29107;
	private static final int PROCELLA_GUARDIAN_1 = 29112;
	private static final int PROCELLA_GUARDIAN_2 = 29113;
	private static final int PROCELLA_GUARDIAN_3 = 29114;
	private static final int PROCELLA_STORM = 29115;
	// Skills
	private static final SkillHolder HURRICANE_SUMMON = new SkillHolder(50042, 1); // When spawn Minion
	private static final SkillHolder HURRICANE_BOLT = new SkillHolder(50043, 1); // When player in Radius + para
	// Misc
	private static final int TEMPLATE_ID = 197;
	private static int STORM_MAX_COUNT = 16; // TODO: Max is limit ?
	private int _procellaStormCount;
	private RaidBossInstance _procella;
	private MonsterInstance _minion1;
	private MonsterInstance _minion2;
	private MonsterInstance _minion3;
	
	public ResidenceOfKingProcella()
	{
		super(TEMPLATE_ID);
		addStartNpc(WIRI);
		addKillId(PROCELLA, PROCELLA_GUARDIAN_1, PROCELLA_GUARDIAN_2, PROCELLA_GUARDIAN_3);
		addInstanceEnterId(TEMPLATE_ID);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, PlayerInstance player)
	{
		switch (event)
		{
			case "ENTER":
			{
				enterInstance(player, npc, TEMPLATE_ID);
				_procella = (RaidBossInstance) addSpawn(PROCELLA, 212862, 179828, -15489, 49151, false, 0, true, player.getInstanceId());
				startQuestTimer("SPAWN_MINION", 20000, _procella, player);
				startQuestTimer("SPAWN_STORM", 5000, _procella, player);
				_procellaStormCount = 0;
				break;
			}
			case "SPAWN_MINION":
			{
				if (npc.getId() == PROCELLA)
				{
					_minion1 = (MonsterInstance) addSpawn(PROCELLA_GUARDIAN_1, 212663, 179421, -15486, 31011, true, 0, true, npc.getInstanceId());
					_minion2 = (MonsterInstance) addSpawn(PROCELLA_GUARDIAN_2, 213258, 179822, -15486, 12001, true, 0, true, npc.getInstanceId());
					_minion3 = (MonsterInstance) addSpawn(PROCELLA_GUARDIAN_3, 212558, 179974, -15486, 12311, true, 0, true, npc.getInstanceId());
					startQuestTimer("HIDE_PROCELLA", 3000, _procella, null);
				}
				break;
			}
			case "SPAWN_STORM":
			{
				if (_procellaStormCount < STORM_MAX_COUNT)
				{
					_procella.useMagic(HURRICANE_SUMMON.getSkill());
					
					final Npc procellaStorm = addSpawn(PROCELLA_STORM, _procella.getX() + getRandom(-500, 500), _procella.getY() + getRandom(-500, 500), _procella.getZ(), 31011, true, 0, true, npc.getInstanceId());
					procellaStorm.setRandomWalking(true);
					_procellaStormCount++;
					startQuestTimer("SPAWN_STORM", 300000, _procella, null);
					startQuestTimer("CHECK_CHAR_INSIDE_RADIUS_NPC", 1000, procellaStorm, player);
				}
				break;
			}
			case "HIDE_PROCELLA":
			{
				if (_procella.isInvisible())
				{
					_procella.setInvisible(false);
					_procella.broadcastSay(ChatType.NPC_SHOUT, "Im invisible");
				}
				else
				{
					_procella.setInvisible(true);
					_procella.broadcastSay(ChatType.NPC_SHOUT, "Im visible");
					startQuestTimer("SPAWN_MINION", 300000, _procella, player);
				}
				break;
			}
			case "CHECK_CHAR_INSIDE_RADIUS_NPC":
			{
				if ((player != null) && (player.isInsideRadius3D(npc, 200)))
				{
					npc.abortAttack();
					npc.abortCast();
					npc.setTarget(player);
					if (SkillCaster.checkUseConditions(npc, HURRICANE_BOLT.getSkill()))
					{
						npc.doCast(HURRICANE_BOLT.getSkill());
					}
					startQuestTimer("CHECK_CHAR_INSIDE_RADIUS_NPC", 10000, npc, player);
				}
				else
				{
					startQuestTimer("CHECK_CHAR_INSIDE_RADIUS_NPC", 10000, npc, player);
				}
				break;
			}
		}
		return null;
	}
	
	@Override
	public String onKill(Npc npc, PlayerInstance player, boolean isSummon)
	{
		if (npc.getId() == PROCELLA)
		{
			final Instance world = npc.getInstanceWorld();
			if (world != null)
			{
				world.finishInstance();
			}
		}
		else if ((_minion1.isDead()) && (_minion2.isDead()) && (_minion3.isDead()))
		{
			startQuestTimer("HIDE_PROCELLA", 1000, _procella, null);
		}
		return super.onKill(npc, player, isSummon);
	}
	
	public static void main(String[] args)
	{
		new ResidenceOfKingProcella();
	}
}
