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

import com.l2jmobius.gameserver.instancemanager.CastleManager;
import com.l2jmobius.gameserver.model.L2Object;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2StaticObjectInstance;
import com.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import com.l2jmobius.gameserver.network.serverpackets.ChairSit;

/**
 * This class ...
 * @version $Revision: 1.1.4.3 $ $Date: 2005/03/27 15:29:30 $
 */
public class ChangeWaitType2 extends L2GameClientPacket
{
	private static final String _C__1D_CHANGEWAITTYPE2 = "[C] 1D ChangeWaitType2";
	
	private boolean _typeStand;
	
	@Override
	protected void readImpl()
	{
		_typeStand = (readD() == 1);
	}
	
	@Override
	public void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		if ((getClient() != null) && (player != null))
		{
			if (player.isOutOfControl())
			{
				player.sendPacket(new ActionFailed());
				return;
			}
			
			if (player.getMountType() != 0)
			{
				return;
			}
			
			final L2Object target = player.getTarget();
			
			if ((target != null) && !player.isSitting() && !_typeStand && (target instanceof L2StaticObjectInstance) && (((L2StaticObjectInstance) target).getType() == 1) && (CastleManager.getInstance().getCastle(target) != null) && player.isInsideRadius(target, L2StaticObjectInstance.INTERACTION_DISTANCE, false, false))
			{
				final ChairSit cs = new ChairSit(player, ((L2StaticObjectInstance) target).getStaticObjectId());
				player.sitDown();
				player.broadcastPacket(cs);
			}
			
			if (_typeStand)
			{
				player.standUp();
			}
			else
			{
				if (!player.isPendingSitting())
				{
					if (player.isMoving())
					{
						player.setIsPendingSitting(true);
					}
					else
					{
						if (player.isAttackingDisabled() || player.isImmobilized() || player.isCastingNow())
						{
							return;
						}
						
						player.sitDown();
					}
				}
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.network.clientpackets.L2GameClientPacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__1D_CHANGEWAITTYPE2;
	}
}