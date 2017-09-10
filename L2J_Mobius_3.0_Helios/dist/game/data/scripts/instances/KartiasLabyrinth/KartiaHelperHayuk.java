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

import com.l2jmobius.gameserver.model.Location;
import com.l2jmobius.gameserver.model.events.impl.instance.OnInstanceStatusChange;
import com.l2jmobius.gameserver.model.instancezone.Instance;

import ai.AbstractNpcAI;

/**
 * Kartia Helper Hayuk AI.
 * @author St3eT
 */
public final class KartiaHelperHayuk extends AbstractNpcAI
{
	// NPCs
	private static final int[] KARTIA_HAYUK =
	{
		33613, // Hayuk (Kartia 85)
		33624, // Hayuk (Kartia 90)
		33635, // Hayuk (Kartia 95)
	};
	// Misc
	private static final int[] KARTIA_SOLO_INSTANCES =
	{
		205, // Solo 85
		206, // Solo 90
		207, // Solo 95
	};
	
	private KartiaHelperHayuk()
	{
		setInstanceStatusChangeId(this::onInstanceStatusChange, KARTIA_SOLO_INSTANCES);
	}
	
	public void onInstanceStatusChange(OnInstanceStatusChange event)
	{
		final Instance instance = event.getWorld();
		final int status = event.getStatus();
		switch (status)
		{
			case 1:
			{
				// Nothing for now
				break;
			}
			case 2:
			case 3:
			{
				final Location loc = instance.getTemplateParameters().getLocation("hayukTeleportStatus" + status);
				if (loc != null)
				{
					instance.getAliveNpcs(KARTIA_HAYUK).forEach(hayuk -> hayuk.teleToLocation(loc));
				}
				break;
			}
		}
	}
	
	public static void main(String[] args)
	{
		new KartiaHelperHayuk();
	}
}