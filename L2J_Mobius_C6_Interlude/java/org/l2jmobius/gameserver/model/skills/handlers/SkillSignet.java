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
package org.l2jmobius.gameserver.model.skills.handlers;

import java.util.List;

import org.l2jmobius.gameserver.data.sql.NpcTable;
import org.l2jmobius.gameserver.instancemanager.IdManager;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.Skill;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.instance.EffectPoint;
import org.l2jmobius.gameserver.model.actor.templates.NpcTemplate;

public class SkillSignet extends Skill
{
	private final int _effectNpcId;
	public int effectId;
	
	public SkillSignet(StatSet set)
	{
		super(set);
		_effectNpcId = set.getInt("effectNpcId", -1);
		effectId = set.getInt("effectId", -1);
	}
	
	@Override
	public void useSkill(Creature caster, List<Creature> targets)
	{
		if (caster.isAlikeDead())
		{
			return;
		}
		
		final NpcTemplate template = NpcTable.getInstance().getTemplate(_effectNpcId);
		final EffectPoint effectPoint = new EffectPoint(IdManager.getInstance().getNextId(), template, caster);
		effectPoint.getStatus().setCurrentHp(effectPoint.getMaxHp());
		effectPoint.getStatus().setCurrentMp(effectPoint.getMaxMp());
		World.getInstance().storeObject(effectPoint);
		
		int x = caster.getX();
		int y = caster.getY();
		int z = caster.getZ();
		if ((caster instanceof Player) && (getTargetType() == Skill.SkillTargetType.TARGET_GROUND))
		{
			final Location wordPosition = ((Player) caster).getCurrentSkillWorldPosition();
			if (wordPosition != null)
			{
				x = wordPosition.getX();
				y = wordPosition.getY();
				z = wordPosition.getZ();
			}
		}
		applyEffects(caster, effectPoint, false, false, false);
		effectPoint.setInvul(true);
		effectPoint.spawnMe(x, y, z);
	}
}