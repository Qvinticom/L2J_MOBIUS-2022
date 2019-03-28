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
package handlers.itemhandlers;

import java.util.List;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.enums.ItemSkillType;
import com.l2jmobius.gameserver.handler.IItemHandler;
import com.l2jmobius.gameserver.instancemanager.CastleManorManager;
import com.l2jmobius.gameserver.model.WorldObject;
import com.l2jmobius.gameserver.model.actor.Playable;
import com.l2jmobius.gameserver.model.actor.instance.ChestInstance;
import com.l2jmobius.gameserver.model.actor.instance.MonsterInstance;
import com.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import com.l2jmobius.gameserver.model.entity.Castle;
import com.l2jmobius.gameserver.model.holders.ItemSkillHolder;
import com.l2jmobius.gameserver.model.items.instance.ItemInstance;
import com.l2jmobius.gameserver.network.SystemMessageId;
import com.l2jmobius.gameserver.network.serverpackets.ActionFailed;

/**
 * @author l3x
 */
public class Seed implements IItemHandler
{
	@Override
	public boolean useItem(Playable playable, ItemInstance item, boolean forceUse)
	{
		if (!Config.ALLOW_MANOR)
		{
			return false;
		}
		else if (!playable.isPlayer())
		{
			playable.sendPacket(SystemMessageId.YOUR_PET_CANNOT_CARRY_THIS_ITEM);
			return false;
		}
		
		final WorldObject tgt = playable.getTarget();
		if (!tgt.isNpc())
		{
			playable.sendPacket(SystemMessageId.INVALID_TARGET);
			return false;
		}
		else if (!tgt.isMonster() || ((MonsterInstance) tgt).isRaid() || (tgt instanceof ChestInstance))
		{
			playable.sendPacket(SystemMessageId.THE_TARGET_IS_UNAVAILABLE_FOR_SEEDING);
			return false;
		}
		
		final MonsterInstance target = (MonsterInstance) tgt;
		if (target.isDead())
		{
			playable.sendPacket(SystemMessageId.INVALID_TARGET);
			return false;
		}
		else if (target.isSeeded())
		{
			playable.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		final com.l2jmobius.gameserver.model.Seed seed = CastleManorManager.getInstance().getSeed(item.getId());
		if (seed == null)
		{
			return false;
		}
		
		final Castle taxCastle = target.getTaxCastle();
		if ((taxCastle == null) || (seed.getCastleId() != taxCastle.getResidenceId()))
		{
			playable.sendPacket(SystemMessageId.THIS_SEED_MAY_NOT_BE_SOWN_HERE);
			return false;
		}
		
		final PlayerInstance player = playable.getActingPlayer();
		target.setSeeded(seed, player);
		
		final List<ItemSkillHolder> skills = item.getItem().getSkills(ItemSkillType.NORMAL);
		if (skills != null)
		{
			skills.forEach(holder -> player.useMagic(holder.getSkill(), item, false, false));
		}
		return true;
	}
}
