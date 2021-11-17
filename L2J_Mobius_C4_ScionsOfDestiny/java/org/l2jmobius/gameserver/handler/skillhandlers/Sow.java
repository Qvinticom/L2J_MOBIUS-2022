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
package org.l2jmobius.gameserver.handler.skillhandlers;

import java.util.List;

import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.ai.CtrlIntention;
import org.l2jmobius.gameserver.data.xml.ManorSeedData;
import org.l2jmobius.gameserver.handler.ISkillHandler;
import org.l2jmobius.gameserver.model.Skill;
import org.l2jmobius.gameserver.model.Skill.SkillType;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.instance.Monster;
import org.l2jmobius.gameserver.model.items.instance.Item;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import org.l2jmobius.gameserver.network.serverpackets.PlaySound;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * @author l3x
 */
public class Sow implements ISkillHandler
{
	private static final SkillType[] SKILL_TYPES =
	{
		SkillType.SOW
	};
	
	private Player _player;
	private Monster _target;
	private int _seedId;
	
	@Override
	public void useSkill(Creature creature, Skill skill, List<Creature> targets)
	{
		if (!(creature instanceof Player))
		{
			return;
		}
		
		_player = (Player) creature;
		
		final List<Creature> targetList = skill.getTargetList(creature);
		if (targetList == null)
		{
			return;
		}
		
		for (@SuppressWarnings("unused")
		Creature element : targetList)
		{
			if (!(targetList.get(0) instanceof Monster))
			{
				continue;
			}
			
			_target = (Monster) targetList.get(0);
			if (_target.isSeeded())
			{
				_player.sendPacket(ActionFailed.STATIC_PACKET);
				continue;
			}
			
			if (_target.isDead())
			{
				_player.sendPacket(ActionFailed.STATIC_PACKET);
				continue;
			}
			
			if (_target.getSeeder() != _player)
			{
				_player.sendPacket(ActionFailed.STATIC_PACKET);
				continue;
			}
			
			_seedId = _target.getSeedType();
			if (_seedId == 0)
			{
				_player.sendPacket(ActionFailed.STATIC_PACKET);
				continue;
			}
			
			final Item item = _player.getInventory().getItemByItemId(_seedId);
			if (item == null)
			{
				_player.sendPacket(ActionFailed.STATIC_PACKET);
				break;
			}
			
			// Consuming used seed
			_player.destroyItem("Consume", item.getObjectId(), 1, null, false);
			SystemMessage sm = null;
			if (calcSuccess())
			{
				_player.sendPacket(new PlaySound("Itemsound.quest_itemget"));
				_target.setSeeded();
				sm = new SystemMessage(SystemMessageId.THE_SEED_WAS_SUCCESSFULLY_SOWN);
			}
			else
			{
				sm = new SystemMessage(SystemMessageId.THE_SEED_WAS_NOT_SOWN);
			}
			
			if (_player.getParty() == null)
			{
				_player.sendPacket(sm);
			}
			else
			{
				_player.getParty().broadcastToPartyMembers(sm);
			}
			
			// TODO: Mob should not aggro on player, this way doesn't work really nice.
			_target.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		}
	}
	
	private boolean calcSuccess()
	{
		if ((_player == null) || (_target == null))
		{
			return false;
		}
		
		// TODO: check all the chances
		int basicSuccess = (ManorSeedData.getInstance().isAlternative(_seedId) ? 20 : 90);
		int minlevelSeed = 0;
		int maxlevelSeed = 0;
		minlevelSeed = ManorSeedData.getInstance().getSeedMinLevel(_seedId);
		maxlevelSeed = ManorSeedData.getInstance().getSeedMaxLevel(_seedId);
		
		final int levelPlayer = _player.getLevel(); // Attacker Level.
		final int levelTarget = _target.getLevel(); // Target Level.
		
		// 5% decrease in chance if player level.
		// Is more then +/- 5 levels to _seed's level.
		if (levelTarget < minlevelSeed)
		{
			basicSuccess -= 5;
		}
		if (levelTarget > maxlevelSeed)
		{
			basicSuccess -= 5;
		}
		
		// 5% decrease in chance if player level.
		// Is more than +/- 5 levels to _target's level.
		int diff = (levelPlayer - levelTarget);
		if (diff < 0)
		{
			diff = -diff;
		}
		
		if (diff > 5)
		{
			basicSuccess -= 5 * (diff - 5);
		}
		
		// Chance can't be less than 1%.
		if (basicSuccess < 1)
		{
			basicSuccess = 1;
		}
		
		final int rate = Rnd.get(99);
		return (rate < basicSuccess);
	}
	
	@Override
	public SkillType[] getSkillTypes()
	{
		return SKILL_TYPES;
	}
}
