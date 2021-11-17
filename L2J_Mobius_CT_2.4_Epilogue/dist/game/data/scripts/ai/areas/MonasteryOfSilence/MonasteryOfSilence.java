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
package ai.areas.MonasteryOfSilence;

import java.util.ArrayList;

import org.l2jmobius.commons.util.CommonUtil;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.ai.CtrlIntention;
import org.l2jmobius.gameserver.data.xml.SkillData;
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Attackable;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Playable;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.Summon;
import org.l2jmobius.gameserver.model.actor.instance.Pet;
import org.l2jmobius.gameserver.model.effects.EffectType;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.network.serverpackets.NpcSay;
import org.l2jmobius.gameserver.util.Util;

import ai.AbstractNpcAI;

public class MonasteryOfSilence extends AbstractNpcAI
{
	static final int[] mobs1 =
	{
		22124,
		22125,
		22126,
		22127,
		22129
	};
	static final int[] mobs2 =
	{
		22134,
		22135
	};
	static final String[] text =
	{
		"You cannot carry a weapon without authorization!",
		"name, why would you choose the path of darkness?!",
		"name! How dare you defy the will of Einhasad!"
	};
	
	private MonasteryOfSilence()
	{
		registerMobs(mobs1);
		registerMobs(mobs2);
	}
	
	@Override
	public String onAggroRangeEnter(Npc npc, Player player, boolean isSummon)
	{
		if (CommonUtil.contains(mobs1, npc.getId()) && !npc.isInCombat() && (npc.getTarget() == null))
		{
			if (player.getActiveWeaponInstance() != null)
			{
				npc.setTarget(player);
				npc.broadcastPacket(new NpcSay(npc.getObjectId(), ChatType.GENERAL, npc.getId(), text[0]));
				switch (npc.getId())
				{
					case 22124:
					case 22126:
					{
						final Skill skill = SkillData.getInstance().getSkill(4589, 8);
						npc.doCast(skill);
						break;
					}
					default:
					{
						npc.setRunning();
						((Attackable) npc).addDamageHate(player, 0, 999);
						npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
						break;
					}
				}
			}
			else if (((Attackable) npc).getMostHated() == null)
			{
				return null;
			}
		}
		return super.onAggroRangeEnter(npc, player, isSummon);
	}
	
	@Override
	public String onSkillSee(Npc npc, Player caster, Skill skill, WorldObject[] targets, boolean isSummon)
	{
		if (CommonUtil.contains(mobs2, npc.getId()))
		{
			if (skill.hasEffectType(EffectType.AGGRESSION) && (targets.length != 0))
			{
				for (WorldObject obj : targets)
				{
					if (obj.equals(npc))
					{
						npc.broadcastPacket(new NpcSay(npc.getObjectId(), ChatType.GENERAL, npc.getId(), text[Rnd.get(2) + 1].replace("name", caster.getName())));
						((Attackable) npc).addDamageHate(caster, 0, 999);
						npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, caster);
						break;
					}
				}
			}
		}
		return super.onSkillSee(npc, caster, skill, targets, isSummon);
	}
	
	@Override
	public String onSpawn(Npc npc)
	{
		if (CommonUtil.contains(mobs1, npc.getId()))
		{
			final ArrayList<Playable> result = new ArrayList<>();
			for (WorldObject obj : World.getInstance().getVisibleObjects(npc, WorldObject.class))
			{
				if ((obj instanceof Player) || (obj instanceof Pet))
				{
					if (Util.checkIfInRange(npc.getAggroRange(), npc, obj, true) && !((Creature) obj).isDead())
					{
						result.add((Playable) obj);
					}
				}
			}
			if (!result.isEmpty() && (result.size() != 0))
			{
				final Object[] characters = result.toArray();
				for (Object obj : characters)
				{
					final Playable target = (Playable) (obj instanceof Player ? obj : ((Summon) obj).getOwner());
					if ((target.getActiveWeaponInstance() != null) && !npc.isInCombat() && (npc.getTarget() == null))
					{
						npc.setTarget(target);
						npc.broadcastPacket(new NpcSay(npc.getObjectId(), ChatType.GENERAL, npc.getId(), text[0]));
						switch (npc.getId())
						{
							case 22124:
							case 22126:
							case 22127:
							{
								final Skill skill = SkillData.getInstance().getSkill(4589, 8);
								npc.doCast(skill);
								break;
							}
							default:
							{
								npc.setRunning();
								((Attackable) npc).addDamageHate(target, 0, 999);
								npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
								break;
							}
						}
					}
				}
			}
		}
		return super.onSpawn(npc);
	}
	
	@Override
	public String onSpellFinished(Npc npc, Player player, Skill skill)
	{
		if (CommonUtil.contains(mobs1, npc.getId()) && (skill.getId() == 4589))
		{
			npc.setRunning();
			((Attackable) npc).addDamageHate(player, 0, 999);
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
		}
		return super.onSpellFinished(npc, player, skill);
	}
	
	public static void main(String[] args)
	{
		new MonasteryOfSilence();
	}
}
