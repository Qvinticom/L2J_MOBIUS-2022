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

import org.l2jmobius.gameserver.datatables.sql.CharTemplateTable;
import org.l2jmobius.gameserver.model.actor.templates.PlayerTemplate;
import org.l2jmobius.gameserver.model.base.ClassId;
import org.l2jmobius.gameserver.network.serverpackets.CharTemplates;

public class NewCharacter extends GameClientPacket
{
	@Override
	protected void readImpl()
	{
	}
	
	@Override
	protected void runImpl()
	{
		final CharTemplates ct = new CharTemplates();
		
		PlayerTemplate template = CharTemplateTable.getInstance().getTemplate(0);
		ct.addChar(template);
		
		template = CharTemplateTable.getInstance().getTemplate(ClassId.fighter); // Human Fighter
		ct.addChar(template);
		
		template = CharTemplateTable.getInstance().getTemplate(ClassId.mage); // Human Mage
		ct.addChar(template);
		
		template = CharTemplateTable.getInstance().getTemplate(ClassId.elvenFighter); // Elf Fighter
		ct.addChar(template);
		
		template = CharTemplateTable.getInstance().getTemplate(ClassId.elvenMage); // Elf Mage
		ct.addChar(template);
		
		template = CharTemplateTable.getInstance().getTemplate(ClassId.darkFighter); // DE Fighter
		ct.addChar(template);
		
		template = CharTemplateTable.getInstance().getTemplate(ClassId.darkMage); // DE Mage
		ct.addChar(template);
		
		template = CharTemplateTable.getInstance().getTemplate(ClassId.orcFighter); // Orc Fighter
		ct.addChar(template);
		
		template = CharTemplateTable.getInstance().getTemplate(ClassId.orcMage); // Orc Mage
		ct.addChar(template);
		
		template = CharTemplateTable.getInstance().getTemplate(ClassId.dwarvenFighter); // Dwarf Fighter
		ct.addChar(template);
		
		// Finally
		sendPacket(ct);
	}
}