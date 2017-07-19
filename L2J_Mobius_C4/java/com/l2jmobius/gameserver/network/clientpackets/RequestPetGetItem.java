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
package com.l2jmobius.gameserver.network.clientpackets;

import com.l2jmobius.gameserver.ai.CtrlIntention;
import com.l2jmobius.gameserver.instancemanager.MercTicketManager;
import com.l2jmobius.gameserver.model.L2ItemInstance;
import com.l2jmobius.gameserver.model.L2World;
import com.l2jmobius.gameserver.model.actor.instance.L2PetInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2SummonInstance;
import com.l2jmobius.gameserver.network.serverpackets.ActionFailed;

/**
 * This class ...
 * @version $Revision: 1.2.4.4 $ $Date: 2005/03/29 23:15:33 $
 */
public class RequestPetGetItem extends L2GameClientPacket
{
	// private static Logger _log = Logger.getLogger(RequestPetGetItem.class.getName());
	private static final String _C__8f_REQUESTPETGETITEM = "[C] 8F RequestPetGetItem";
	
	private int _objectId;
	
	@Override
	protected void readImpl()
	{
		_objectId = readD();
	}
	
	@Override
	public void runImpl()
	{
		final L2World world = L2World.getInstance();
		final L2ItemInstance item = (L2ItemInstance) world.findObject(_objectId);
		if ((item == null) || (getClient().getActiveChar() == null))
		{
			return;
		}
		
		final int castleId = MercTicketManager.getInstance().getTicketCastleId(item.getItemId());
		if (castleId > 0)
		{
			sendPacket(new ActionFailed());
			return;
		}
		
		if (getClient().getActiveChar().getPet() instanceof L2SummonInstance)
		{
			sendPacket(new ActionFailed());
			return;
		}
		
		final L2PetInstance pet = (L2PetInstance) getClient().getActiveChar().getPet();
		if ((pet == null) || pet.isDead() || pet.isOutOfControl())
		{
			sendPacket(new ActionFailed());
			return;
		}
		
		pet.getAI().setIntention(CtrlIntention.AI_INTENTION_PICK_UP, item);
	}
	
	@Override
	public String getType()
	{
		return _C__8f_REQUESTPETGETITEM;
	}
}