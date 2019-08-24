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
package org.l2jmobius.gameserver.network.clientpackets;

import org.l2jmobius.gameserver.ai.CtrlIntention;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.instance.PetInstance;
import org.l2jmobius.gameserver.model.actor.instance.SummonInstance;
import org.l2jmobius.gameserver.model.items.instance.ItemInstance;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;

public class RequestPetGetItem extends GameClientPacket
{
	private int _objectId;
	
	@Override
	protected void readImpl()
	{
		_objectId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final World world = World.getInstance();
		final ItemInstance item = (ItemInstance) world.findObject(_objectId);
		
		if ((item == null) || (getClient().getPlayer() == null))
		{
			return;
		}
		
		if (getClient().getPlayer().getPet() instanceof SummonInstance)
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final PetInstance pet = (PetInstance) getClient().getPlayer().getPet();
		
		if ((pet == null) || pet.isDead() || pet.isOutOfControl())
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		pet.getAI().setIntention(CtrlIntention.AI_INTENTION_PICK_UP, item);
	}
}
