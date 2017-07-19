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
package com.l2jmobius.util;

import java.util.LinkedList;

public class EventData
{
	public int eventX;
	public int eventY;
	public int eventZ;
	public int eventkarma;
	public int eventpvpkills;
	public int eventpkkills;
	public String eventTitle;
	public LinkedList<String> kills = new LinkedList<>();
	public boolean eventSitForced = false;
	
	public EventData(int pEventX, int pEventY, int pEventZ, int pEventkarma, int pEventpvpkills, int pEventpkkills, String pEventTitle, LinkedList<String> pKills, boolean pEventSitForced)
	{
		eventX = pEventX;
		eventY = pEventY;
		eventZ = pEventZ;
		eventkarma = pEventkarma;
		eventpvpkills = pEventpvpkills;
		eventpkkills = pEventpkkills;
		eventTitle = pEventTitle;
		kills = pKills;
		eventSitForced = pEventSitForced;
	}
}