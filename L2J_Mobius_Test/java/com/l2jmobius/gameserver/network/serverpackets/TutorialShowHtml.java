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
package com.l2jmobius.gameserver.network.serverpackets;

import com.l2jmobius.gameserver.enums.HtmlActionScope;

/**
 * TutorialShowHtml server packet implementation.
 * @author HorridoJoho
 */
public final class TutorialShowHtml extends AbstractHtmlPacket
{
	public static final int NORMAL_WINDOW = 1;
	public static final int LARGE_WINDOW = 2;
	
	private final int _type;
	
	public TutorialShowHtml(String html)
	{
		super(html);
		_type = NORMAL_WINDOW;
	}
	
	/**
	 * This constructor is just here to be able to show a tutorial html<br>
	 * window bound to an npc.
	 * @param npcObjId
	 * @param html
	 */
	public TutorialShowHtml(int npcObjId, String html)
	{
		super(npcObjId, html);
		_type = NORMAL_WINDOW;
	}
	
	public TutorialShowHtml(int npcObjId, String html, int type)
	{
		super(npcObjId, html);
		_type = type;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xA6);
		writeD(_type);
		writeS(getHtml());
	}
	
	@Override
	public HtmlActionScope getScope()
	{
		return HtmlActionScope.TUTORIAL_HTML;
	}
}