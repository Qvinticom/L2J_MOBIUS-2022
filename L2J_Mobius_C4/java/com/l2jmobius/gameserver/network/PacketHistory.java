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
package com.l2jmobius.gameserver.network;

import java.util.Date;
import java.util.Map;

import javolution.xml.XMLFormat;
import javolution.xml.stream.XMLStreamException;

class PacketHistory
{
	protected Map<Class<?>, Long> _info;
	protected long _timeStamp;
	
	protected static final XMLFormat<PacketHistory> PACKET_HISTORY_XML = new XMLFormat<PacketHistory>(PacketHistory.class)
	{
		@Override
		public void read(InputElement xml, PacketHistory packetHistory) throws XMLStreamException
		{
			packetHistory._timeStamp = xml.getAttribute("time-stamp", 0);
			packetHistory._info = xml.<Map<Class<?>, Long>> get("info");
		}
		
		@Override
		public void write(PacketHistory packetHistory, OutputElement xml) throws XMLStreamException
		{
			xml.setAttribute("time-stamp", new Date(packetHistory._timeStamp).toString());
			
			for (final Class<?> cls : packetHistory._info.keySet())
			{
				xml.setAttribute(cls.getSimpleName(), packetHistory._info.get(cls));
			}
		}
	};
}