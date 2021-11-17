/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.l2jmobius.gameserver.network.serverpackets;

import org.l2jmobius.gameserver.model.actor.instance.Pet;

public class PetStatusUpdate extends ServerBasePacket
{
	private final Pet _pet;
	
	public PetStatusUpdate(Pet pet)
	{
		_pet = pet;
	}
	
	@Override
	public void writeImpl()
	{
		writeC(0xCE);
		writeD(_pet.getPetId());
		writeD(_pet.getObjectId());
		writeD(_pet.getX());
		writeD(_pet.getY());
		writeD(_pet.getZ());
		writeS(_pet.getTitle());
		writeD(_pet.getCurrentFed());
		writeD(_pet.getMaxFed());
		writeD((int) _pet.getCurrentHp());
		writeD(_pet.getMaxHp());
		writeD((int) _pet.getCurrentMp());
		writeD(_pet.getMaxMp());
		writeD(_pet.getLevel());
		writeD(_pet.getExp());
		writeD(_pet.getLastLevel());
		writeD(_pet.getNextLevel());
	}
}
